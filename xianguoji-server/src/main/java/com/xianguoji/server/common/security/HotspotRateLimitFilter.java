package com.xianguoji.server.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.service.SecurityEventService;
import com.xianguoji.server.common.util.IpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 热点路径分级限流过滤器
 * - 按路径前缀滑动窗口限流（10秒窗口）
 * - 三级威胁机制：1级=429, 2级=告警, 3级=封禁24h
 * - 白名单 IP 豁免
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class HotspotRateLimitFilter extends OncePerRequestFilter {

    private final SecurityEventService securityEventService;
    private final ObjectMapper objectMapper;

    /** 路径前缀 → (窗口秒数, 阈值)；注意：必须前缀更具体的规则在前。
     *  实际登录/短信路径在 /api/pub/auth，原 /api/auth 仅用于 logout 等已认证场景。 */
    private static final List<Rule> RULES = List.of(
            new Rule("/api/pub/auth", 10, 30),
            new Rule("/api/u/auth", 10, 30),
            new Rule("/api/u/order", 10, 60),
            new Rule("/api/u/cart", 10, 80),
            // 管理端仪表盘并发拉取多接口，阈值需放宽到 ~20 RPS
            new Rule("/api/admin", 10, 200),
            new Rule("/api", 10, 300)
    );

    /** 白名单 IP */
    private Set<String> whitelistIps;

    /** 可疑 User-Agent 关键词 */
    private static final Set<String> SUSPICIOUS_UA_KEYWORDS = Set.of(
            "python-requests", "curl/", "wget/", "httpclient", "go-http"
    );

    public HotspotRateLimitFilter(SecurityEventService securityEventService, ObjectMapper objectMapper) {
        this.securityEventService = securityEventService;
        this.objectMapper = objectMapper;
    }

    @Value("${xianguoji.security.hotspot.whitelist:}")
    public void setWhitelistIps(String ips) {
        if (ips == null || ips.isBlank()) {
            this.whitelistIps = Set.of();
        } else {
            this.whitelistIps = Set.of(ips.split(","));
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = IpUtil.getClientIp(request);

        // 可疑 User-Agent 检测（记录但不立即封禁，累计后联动 IpBanFilter）
        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isBlank()) {
            securityEventService.log("SUSPICIOUS_UA", ip, "ua=EMPTY uri=" + request.getRequestURI());
        } else {
            String uaLower = ua.toLowerCase();
            for (String keyword : SUSPICIOUS_UA_KEYWORDS) {
                if (uaLower.contains(keyword)) {
                    securityEventService.log("SUSPICIOUS_UA", ip, "ua=" + ua + " uri=" + request.getRequestURI());
                    break;
                }
            }
        }

        // 白名单豁免
        if (whitelistIps.contains(ip)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 检查威胁等级
        int threatLevel = securityEventService.getThreatLevel(ip);
        if (threatLevel >= 3) {
            securityEventService.banIp(ip, "hotspot_level3", java.time.Duration.ofHours(24));
            writeBlocked(response, 403, "您的访问已被限制");
            return;
        }

        // 匹配路径规则
        String uri = request.getRequestURI();
        Rule matched = null;
        for (Rule rule : RULES) {
            if (uri.startsWith(rule.prefix)) {
                matched = rule;
                break;
            }
        }
        if (matched == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 滑动窗口计数
        String countKey = "hotspot:" + ip + ":" + matched.prefix;
        long count = securityEventService.incrementWithTtl(countKey, matched.windowSeconds);

        if (count > matched.limit) {
            // 超限 → 按 count 区间映射目标等级，避免每个超限请求都 +1 升级造成误封
            int targetLevel;
            if (count > matched.limit * 5L) {
                targetLevel = 3;
            } else if (count > matched.limit * 3L) {
                targetLevel = 2;
            } else {
                targetLevel = 1;
            }
            // 仅当目标等级高于当前等级时才更新（防止反复抖动）
            if (targetLevel > threatLevel) {
                securityEventService.setThreatLevel(ip, targetLevel, 600);
                securityEventService.log("HOTSPOT_LIMIT", ip, "uri=" + uri + " count=" + count + " level=" + targetLevel);
            }
            int effectiveLevel = Math.max(threatLevel, targetLevel);

            if (effectiveLevel >= 3) {
                securityEventService.banIp(ip, "hotspot_level3", java.time.Duration.ofHours(24));
                writeBlocked(response, 403, "您的访问已被限制");
            } else if (effectiveLevel == 2) {
                writeBlocked(response, 429, "请求过于频繁，请稍后再试");
            } else {
                writeBlocked(response, 429, "请求过于频繁");
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeBlocked(HttpServletResponse response, int status, String msg) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        R<Void> body = R.fail(status, msg);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private record Rule(String prefix, int windowSeconds, int limit) {}
}
