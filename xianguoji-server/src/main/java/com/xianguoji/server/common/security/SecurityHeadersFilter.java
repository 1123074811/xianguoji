package com.xianguoji.server.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 安全响应头过滤器
 * - 最高优先级，为所有响应添加安全头
 * - 敏感接口额外添加 Cache-Control: no-store
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // HSTS: 强制 HTTPS，1年有效期
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        // 禁止 MIME 嗅探
        response.setHeader("X-Content-Type-Options", "nosniff");
        // 禁止嵌入 iframe（防点击劫持）
        response.setHeader("X-Frame-Options", "DENY");
        // Referrer 策略
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        // 权限策略：禁用地理位置/麦克风/摄像头
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        // CSP
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; "
                + "img-src 'self' data: https://*.xianguoji.com https://*.aliyuncs.com; "
                + "script-src 'self'; "
                + "style-src 'self' 'unsafe-inline'; "
                + "connect-src 'self' https://api.xianguoji.com wss://api.xianguoji.com");

        // 敏感接口不缓存
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/pub/auth") || uri.startsWith("/api/u/") || uri.startsWith("/api/admin/")) {
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
        }

        filterChain.doFilter(request, response);
    }
}
