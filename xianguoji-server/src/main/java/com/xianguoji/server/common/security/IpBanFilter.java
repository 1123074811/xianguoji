package com.xianguoji.server.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.service.SecurityEventService;
import com.xianguoji.server.common.util.IpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * IP 封禁过滤器
 * - 在过滤链最前（TraceFilter 之后、Xss 之前）
 * - 检查 Redis xgj:sec:ban:{ip}，命中直接返回 403
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
@RequiredArgsConstructor
public class IpBanFilter extends OncePerRequestFilter {

    private final SecurityEventService securityEventService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = IpUtil.getClientIp(request);

        if (securityEventService.isBanned(ip)) {
            String reason = securityEventService.getBanReason(ip);
            log.warn("[SEC] ip={} BANNED uri={} reason={}", ip, request.getRequestURI(), reason);
            writeBlocked(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeBlocked(HttpServletResponse response) throws IOException {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        R<Void> body = R.fail(403, "您的访问已被限制");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
