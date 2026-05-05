package com.xianguoji.server.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * JWT 黑名单管理
 * 用户登出 / 管理员强制下线时将 token 加入黑名单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtBlacklistManager {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String BLACKLIST_PREFIX = "xgj:jwt:blacklist:";
    private static final String ACTIVE_TOKEN_PREFIX = "xgj:jwt:active:";

    /**
     * 将 token 加入黑名单（登出时调用）
     *
     * @param token     JWT token
     * @param remainMs  token 剩余有效时间（毫秒）
     */
    public void blacklist(String token, long remainMs) {
        if (remainMs <= 0) return;
        stringRedisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token, "1",
                Duration.ofMillis(remainMs));
        // 同时删除 active token 记录
        stringRedisTemplate.delete(ACTIVE_TOKEN_PREFIX + token);
        log.info("[JwtBlacklist] token 加入黑名单, remainMs={}", remainMs);
    }

    /**
     * 检查 token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }

    /**
     * 记录活跃 token（登录时调用），用于后续续期判断
     *
     * @param token JWT token
     * @param uid   用户 ID
     * @param ttlMs token 有效期（毫秒）
     */
    public void registerActiveToken(String token, Long uid, long ttlMs) {
        stringRedisTemplate.opsForValue().set(
                ACTIVE_TOKEN_PREFIX + token,
                String.valueOf(uid),
                ttlMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Token 续期：当剩余有效期不足阈值时，延长 active token 的 TTL
     *
     * @param token       JWT token
     * @param uid         用户 ID
     * @param remainMs    token 剩余有效时间（毫秒）
     * @param thresholdMs 续期阈值（毫秒），剩余时间低于此值时触发续期
     * @param extendMs    续期时长（毫秒）
     * @return true 表示已续期
     */
    public boolean tryRenew(String token, Long uid, long remainMs,
                            long thresholdMs, long extendMs) {
        if (isBlacklisted(token)) return false;
        if (remainMs > thresholdMs) return false;

        // 续期 active token 的 TTL
        stringRedisTemplate.opsForValue().set(
                ACTIVE_TOKEN_PREFIX + token,
                String.valueOf(uid),
                remainMs + extendMs, TimeUnit.MILLISECONDS);
        log.info("[JwtBlacklist] token 续期, uid={}, extendMs={}", uid, extendMs);
        return true;
    }

    /**
     * 获取活跃 token 对应的 uid
     */
    public Long getActiveTokenUid(String token) {
        String uid = stringRedisTemplate.opsForValue().get(ACTIVE_TOKEN_PREFIX + token);
        return uid != null ? Long.valueOf(uid) : null;
    }
}
