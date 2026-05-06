package com.xianguoji.server.common.aspect;

import com.xianguoji.server.common.service.SecurityEventService;
import com.xianguoji.server.common.util.IpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求链路追踪 + 慢日志过滤器
 * - 为每个请求生成 traceId 写入 MDC，日志自动携带
 * - 请求耗时超过阈值时输出 WARN 级别慢日志
 * - 慢请求告警接入 SecurityEventService
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final long SLOW_THRESHOLD_MS = 1000;

    private final SecurityEventService securityEventService;

    public TraceFilter(SecurityEventService securityEventService) {
        this.securityEventService = securityEventService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        MDC.put(TRACE_ID, traceId);
        response.setHeader("X-Trace-Id", traceId);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > SLOW_THRESHOLD_MS) {
                log.warn("SLOW_REQUEST {} {} {}ms traceId={}",
                        request.getMethod(), request.getRequestURI(), elapsed, traceId);
                // 慢请求告警
                String ip = IpUtil.getClientIp(request);
                securityEventService.log("SLOW_REQUEST", ip,
                        "method=" + request.getMethod() + " uri=" + request.getRequestURI()
                                + " elapsed=" + elapsed + "ms traceId=" + traceId);
            } else {
                log.debug("{} {} {}ms traceId={}",
                        request.getMethod(), request.getRequestURI(), elapsed, traceId);
            }
            MDC.remove(TRACE_ID);
        }
    }
}
