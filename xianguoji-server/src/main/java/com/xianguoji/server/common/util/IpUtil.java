package com.xianguoji.server.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 反向代理友好的客户端 IP 提取工具
 * 优先读取 X-Forwarded-For / X-Real-IP 等代理头
 */
public final class IpUtil {

    private IpUtil() {}

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };

    public static String getClientIp(HttpServletRequest req) {
        for (String header : IP_HEADERS) {
            String value = req.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                // X-Forwarded-For 可能含多个 IP，取第一个
                return value.split(",")[0].trim();
            }
        }
        return req.getRemoteAddr();
    }
}
