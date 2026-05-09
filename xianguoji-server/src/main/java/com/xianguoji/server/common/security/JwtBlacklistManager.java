package com.xianguoji.server.common.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * JWT 黑名单管理 —— S-7: 以 jti 为 key，替代全 token 字符串
 * 用户登出 / 管理员强制下线 / refresh 旋转时将 jti 加入黑名单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtBlacklistManager {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String BLACKLIST_PREFIX = "xgj:jwt:bl:";
    private static final String ACTIVE_TOKEN_PREFIX = "xgj:jwt:active:";

    /**
     * 将 token 的 jti 加入黑名单（登出时调用）
     *
     * @param token     JWT token
     * @param remainMs  token 剩余有效时间（毫秒）
     */
    public void blacklist(String token, long remainMs) {
        if (remainMs <= 0) return;
        String jti = extractJti(token);
        stringRedisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + jti, "1",
                Duration.ofMillis(remainMs));
        // 同时删除 active token 记录
        stringRedisTemplate.delete(ACTIVE_TOKEN_PREFIX + jti);
        log.info("[JwtBlacklist] jti 加入黑名单, remainMs={}", remainMs);
    }

    /**
     * 将 jti 直接加入黑名单（refresh 旋转时调用）
     */
    public void blacklistJti(String jti, long remainMs) {
        if (remainMs <= 0 || jti == null || jti.isBlank()) return;
        stringRedisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + jti, "1",
                Duration.ofMillis(remainMs));
        log.info("[JwtBlacklist] jti 加入黑名单 (refresh旋转), remainMs={}", remainMs);
    }

    /**
     * 检查 token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        String jti = extractJti(token);
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }

    /**
     * 记录活跃 token（登录时调用），用于后续续期判断
     */
    public void registerActiveToken(String token, Long uid, long ttlMs) {
        String jti = extractJti(token);
        stringRedisTemplate.opsForValue().set(
                ACTIVE_TOKEN_PREFIX + jti,
                String.valueOf(uid),
                ttlMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Token 续期：当剩余有效期不足阈值时，延长 active token 的 TTL
     */
    public boolean tryRenew(String token, Long uid, long remainMs,
                            long thresholdMs, long extendMs) {
        if (isBlacklisted(token)) return false;
        if (remainMs > thresholdMs) return false;

        String jti = extractJti(token);
        stringRedisTemplate.opsForValue().set(
                ACTIVE_TOKEN_PREFIX + jti,
                String.valueOf(uid),
                remainMs + extendMs, TimeUnit.MILLISECONDS);
        log.info("[JwtBlacklist] token 续期, uid={}, extendMs={}", uid, extendMs);
        return true;
    }

    /**
     * 获取活跃 token 对应的 uid
     */
    public Long getActiveTokenUid(String token) {
        String jti = extractJti(token);
        String uid = stringRedisTemplate.opsForValue().get(ACTIVE_TOKEN_PREFIX + jti);
        return uid != null ? Long.valueOf(uid) : null;
    }

    /**
     * 将指定用户的所有活跃 token 加入黑名单（排除当前 jti）
     *
     * @param uid        用户/员工 ID
     * @param currentJti 当前请求的 jti，不加入黑名单
     */
    public int blacklistAllForUser(Long uid, String currentJti) {
        var keys = stringRedisTemplate.keys(ACTIVE_TOKEN_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return 0;
        int count = 0;
        for (String key : keys) {
            String val = stringRedisTemplate.opsForValue().get(key);
            if (val != null && val.equals(String.valueOf(uid))) {
                String jti = key.substring(ACTIVE_TOKEN_PREFIX.length());
                if (jti.equals(currentJti)) continue;
                // 加入黑名单，TTL 取 active token 剩余时间
                Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
                if (ttl != null && ttl > 0) {
                    stringRedisTemplate.opsForValue().set(
                            BLACKLIST_PREFIX + jti, "1", Duration.ofMillis(ttl));
                }
                stringRedisTemplate.delete(key);
                count++;
                log.info("[JwtBlacklist] 强制下线 uid={}, jti={}", uid, jti);
            }
        }
        return count;
    }

    private String extractJti(String token) {
        try {
            // 从 token claims 中提取 jti，不依赖 JwtUtil 避免循环依赖
            String[] parts = token.split("\\.");
            if (parts.length < 3) return token; // fallback
            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]),
                    java.nio.charset.StandardCharsets.UTF_8);
            if (payloadJson.contains("\"jti\"")) {
                int idx = payloadJson.indexOf("\"jti\"");
                int colon = payloadJson.indexOf(':', idx);
                int start = payloadJson.indexOf('"', colon + 1);
                int end = payloadJson.indexOf('"', start + 1);
                return payloadJson.substring(start + 1, end);
            }
        } catch (Exception ignored) {}
        // fallback: 用 token 哈希前 16 位
        return token.hashCode() + "";
    }
}
