package com.xianguoji.server.common.util;

import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * SSRF 防御工具 —— 校验用户提供的 URL 是否指向公网
 * - scheme 仅允许 http/https
 * - host 解析后不在内网网段
 * - 不允许 userinfo 段
 * - 禁用敏感端口
 */
public final class UrlSecurityUtil {

    private UrlSecurityUtil() {}

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    private static final Set<Integer> BLOCKED_PORTS = Set.of(
            22, 25, 3306, 6379, 9200, 9300, 27017, 11211, 2375, 2376, 4443, 5555
    );

    /**
     * 校验 URL 是否为合法公网 HTTP/HTTPS 地址，不满足则抛 BizException
     */
    public static void validatePublicHttpUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "URL不能为空");
        }

        URI uri;
        try {
            uri = URI.create(url);
        } catch (Exception e) {
            throw new BizException(ResultCode.PARAM_ERROR, "URL格式非法");
        }

        // scheme 校验
        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new BizException(ResultCode.PARAM_ERROR, "Only HTTP/HTTPS URLs are allowed");
        }

        // userinfo 禁止
        if (uri.getRawUserInfo() != null) {
            throw new BizException(ResultCode.PARAM_ERROR, "URL不允许包含用户信息段");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "URL缺少主机地址");
        }

        // 端口校验
        int port = uri.getPort();
        if (port == -1) {
            port = "https".equals(scheme) ? 443 : 80;
        }
        if (BLOCKED_PORTS.contains(port)) {
            throw new BizException(ResultCode.PARAM_ERROR, "不允许访问敏感端口: " + port);
        }
        if (port < 0 || port > 65535) {
            throw new BizException(ResultCode.PARAM_ERROR, "端口号非法");
        }

        // 主机名黑名单
        String hostLower = host.toLowerCase();
        if ("localhost".equals(hostLower) || hostLower.endsWith(".local") || hostLower.endsWith(".internal")) {
            throw new BizException(ResultCode.PARAM_ERROR, "Private or local network addresses are not allowed");
        }

        // DNS 解析后校验内网 IP
        try {
            InetAddress address = InetAddress.getByName(host);
            if (address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()
                    || address.isAnyLocalAddress()) {
                throw new BizException(ResultCode.PARAM_ERROR, "Private or local network addresses are not allowed");
            }
            // IPv6 唯一本地地址 fc00::/7
            byte[] bytes = address.getAddress();
            if (bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc) {
                throw new BizException(ResultCode.PARAM_ERROR, "Private or local network addresses are not allowed");
            }
        } catch (UnknownHostException e) {
            throw new BizException(ResultCode.PARAM_ERROR, "无法解析主机地址: " + host);
        }
    }

    /**
     * 校验 URL 是否匹配指定前缀白名单
     */
    public static void validateUrlPrefix(String url, Set<String> allowedPrefixes) {
        if (url == null || url.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "URL不能为空");
        }
        boolean match = false;
        for (String prefix : allowedPrefixes) {
            if (url.startsWith(prefix)) {
                match = true;
                break;
            }
        }
        if (!match) {
            throw new BizException(ResultCode.PARAM_ERROR, "URL不在允许的白名单范围内");
        }
    }
}
