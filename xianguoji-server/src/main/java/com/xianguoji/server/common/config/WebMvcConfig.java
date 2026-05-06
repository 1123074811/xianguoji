package com.xianguoji.server.common.config;

import com.xianguoji.server.common.security.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    @Value("${xianguoji.upload.base-dir:D:/xianguoji/upload}")
    private String uploadBaseDir;

    @Value("${xianguoji.security.cors.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/u/**", "/api/admin/**")
                .excludePathPatterns("/api/pub/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins;
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            origins = allowedOrigins.split(",");
        } else {
            // 非 prod 环境允许 localhost
            origins = new String[]{"http://localhost:*", "http://127.0.0.1:*"};
        }
        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Token-Renewal", "X-Trace-Id")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:" + uploadBaseDir + "/");
    }
}
