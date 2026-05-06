package com.xianguoji.server.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 安全事件服务
 * - 威胁等级管理（Redis 计数 + TTL）
 * - IP 封禁
 * - 安全事件日志写入 Redis Stream
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityEventService {

    private final StringRedisTemplate redis;

    private static final String PREFIX = "xgj:sec:";
    private static final String BAN_PREFIX = PREFIX + "ban:";
    private static final String THREAT_PREFIX = PREFIX + "threat:";
    private static final String COUNT_PREFIX = PREFIX + "count:";
    private static final String EVENTS_STREAM = PREFIX + "events";

    // ==================== 威胁等级 ====================

    /**
     * 递增计数并设 TTL
     * @return 递增后的值
     */
    public long incrementWithTtl(String key, long ttlSeconds) {
        String redisKey = COUNT_PREFIX + key;
        Long val = redis.opsForValue().increment(redisKey);
        if (val != null && val == 1L) {
            redis.expire(redisKey, Duration.ofSeconds(ttlSeconds));
        }
        return val != null ? val : 0;
    }

    /**
     * 获取 IP 当前威胁等级（0=正常, 1/2/3=递增）
     */
    public int getThreatLevel(String ip) {
        String val = redis.opsForValue().get(THREAT_PREFIX + ip);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 设置 IP 威胁等级
     */
    public void setThreatLevel(String ip, int level, long ttlSeconds) {
        redis.opsForValue().set(THREAT_PREFIX + ip, String.valueOf(level), Duration.ofSeconds(ttlSeconds));
        log.warn("[SEC] ip={} threatLevel={}", ip, level);
    }

    // ==================== IP 封禁 ====================

    /**
     * 封禁 IP
     */
    public void banIp(String ip, String reason, Duration duration) {
        redis.opsForValue().set(BAN_PREFIX + ip, reason, duration);
        log.warn("[SEC] ip={} BANNED reason={} duration={}", ip, reason, duration);
    }

    /**
     * 检查 IP 是否被封禁
     */
    public boolean isBanned(String ip) {
        return Boolean.TRUE.equals(redis.hasKey(BAN_PREFIX + ip));
    }

    /**
     * 获取封禁原因
     */
    public String getBanReason(String ip) {
        return redis.opsForValue().get(BAN_PREFIX + ip);
    }

    // ==================== 安全事件日志 ====================

    /**
     * 记录安全事件到 Redis Stream
     */
    public void log(String eventType, String ip, String detail) {
        try {
            Map<String, String> body = Map.of(
                    "type", eventType,
                    "ip", ip,
                    "detail", detail != null ? detail : "",
                    "ts", String.valueOf(System.currentTimeMillis())
            );
            MapRecord<String, String, String> record = StreamRecords.newRecord()
                    .ofMap(body)
                    .withStreamKey(EVENTS_STREAM);
            redis.opsForStream().add(record);
        } catch (Exception e) {
            log.error("[SEC] Failed to write security event stream: {}", e.getMessage());
        }
        log.info("[SEC] type={} ip={} detail={}", eventType, ip, detail);
    }
}
