package com.xianguoji.server.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
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
import java.util.Set;

/**
 * XSS 过滤器
 * - 紧跟 TraceFilter 之后执行
 * - 路径排除：验证码、上传、swagger、actuator、静态资源
 * - 内容排除：multipart/form-data、image/*
 * - 富文本路径仅做 header/param 转义，跳过 body 转义
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class XssFilter extends OncePerRequestFilter {

    /** 排除的路径前缀 */
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/api/pub/captcha",
            "/api/admin/upload",
            "/api/u/upload",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator",
            "/static",
            "/ws",
            "/webjars",
            "/druid"
    );

    /** 富文本路径：仅转义 header/param，跳过 body */
    private List<String> richTextPaths;

    @Value("${xianguoji.security.xss.rich-text-paths:/api/admin/goods,/api/admin/announcement}")
    public void setRichTextPaths(String paths) {
        this.richTextPaths = List.of(paths.split(","));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 路径排除
        if (isExcludedPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 内容类型排除：multipart / image 不读 body
        String contentType = request.getContentType();
        if (contentType != null && (
                contentType.startsWith("multipart/form-data")
                || contentType.startsWith("image/"))) {
            filterChain.doFilter(request, response);
            return;
        }

        // 富文本路径：包装但跳过 body 清洗
        if (isRichTextPath(uri)) {
            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(request) {
                @Override
                public ServletInputStream getInputStream() throws IOException {
                    // 富文本路径不清洗 body，直接透传原始流
                    return super.getRequest().getInputStream();
                }
            };
            filterChain.doFilter(wrapper, response);
            return;
        }

        // 普通路径：完整 XSS 包装
        filterChain.doFilter(new XssHttpServletRequestWrapper(request), response);
    }

    private boolean isExcludedPath(String uri) {
        for (String prefix : EXCLUDED_PATHS) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRichTextPath(String uri) {
        if (richTextPaths == null) return false;
        for (String prefix : richTextPaths) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
