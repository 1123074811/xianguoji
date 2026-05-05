package com.xianguoji.server.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /** Caffeine L1 local cache — P1-4 雪崩防护二级缓存 */
    private final Cache<String, String> localCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    /** 空值标记 — P1-2 穿透防护 */
    private static final String NULL_MARKER = "##NULL##";
    /** 空值缓存短TTL (秒) */
    private static final long NULL_TTL_SECONDS = 120;
    /** 互斥锁超时 (秒) — P1-3 击穿防护 */
    private static final long LOCK_TTL_SECONDS = 10;
    /** TTL 随机偏移上限 (秒) — P1-4 雪崩防护 */
    private static final long TTL_JITTER_SECONDS = 300;

    /** 异步刷新线程池 (逻辑过期回源) */
    private static final ExecutorService REFRESH_POOL = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "cache-refresh");
        t.setDaemon(true);
        return t;
    });

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // ===== P1-2 缓存穿透防护: queryWithPassThrough =====

    /**
     * 查询缓存（穿透防护版）。DB 返回 null 时缓存空值标记，短TTL。
     */
    public <R> R queryWithPassThrough(String key, Class<R> clazz,
                                      Duration ttl, Function<String, R> dbFallback) {
        // 1. 查 L1
        String l1 = localCache.getIfPresent(key);
        if (l1 != null) {
            return deserialize(l1, clazz);
        }

        // 2. 查 Redis
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            if (NULL_MARKER.equals(json)) {
                return null;
            }
            localCache.put(key, json);
            return deserialize(json, clazz);
        }

        // 3. 查 DB
        R result = dbFallback.apply(key);
        if (result == null) {
            // 缓存空值，短TTL
            stringRedisTemplate.opsForValue().set(key, NULL_MARKER,
                    Duration.ofSeconds(NULL_TTL_SECONDS + randomJitter(30)));
            return null;
        }

        // 4. 写 Redis + L1
        String serialized = serialize(result);
        stringRedisTemplate.opsForValue().set(key, serialized, withJitter(ttl));
        localCache.put(key, serialized);
        return result;
    }

    // ===== P1-3 缓存击穿防护: queryWithMutex =====

    /**
     * 查询缓存（互斥锁版）。热点 key 过期时只放一个线程回源，其余等待重试。
     */
    public <R> R queryWithMutex(String key, Class<R> clazz,
                                Duration ttl, Function<String, R> dbFallback) {
        // 1. 查 Redis
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            if (NULL_MARKER.equals(json)) return null;
            localCache.put(key, json);
            return deserialize(json, clazz);
        }

        // 2. 获取互斥锁
        String lockKey = key + ":lock";
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(locked)) {
            try {
                // 双重检查
                json = stringRedisTemplate.opsForValue().get(key);
                if (json != null) {
                    if (NULL_MARKER.equals(json)) return null;
                    return deserialize(json, clazz);
                }
                // 回源
                R result = dbFallback.apply(key);
                if (result == null) {
                    stringRedisTemplate.opsForValue().set(key, NULL_MARKER,
                            Duration.ofSeconds(NULL_TTL_SECONDS + randomJitter(30)));
                    return null;
                }
                String serialized = serialize(result);
                stringRedisTemplate.opsForValue().set(key, serialized, withJitter(ttl));
                localCache.put(key, serialized);
                return result;
            } finally {
                stringRedisTemplate.delete(lockKey);
            }
        }

        // 3. 未获锁 → 短暂休眠后重试
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return queryWithMutex(key, clazz, ttl, dbFallback);
    }

    // ===== P1-3 缓存击穿防护: queryWithLogicalExpire =====

    /**
     * 查询缓存（逻辑过期版）。热点 key 永不过期，但数据带逻辑过期时间；
     * 过期后异步刷新，当前请求返回旧数据。
     */
    public <R> R queryWithLogicalExpire(String key, Class<R> clazz,
                                         Duration ttl, Function<String, R> dbFallback) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            // 冷数据首次加载
            R result = dbFallback.apply(key);
            if (result == null) return null;
            String serialized = serialize(result);
            LogicalExpireData wrapper = new LogicalExpireData();
            wrapper.setData(serialized);
            wrapper.setExpireTime(LocalDateTime.now().plus(ttl));
            stringRedisTemplate.opsForValue().set(key, serialize(wrapper));
            localCache.put(key, serialized);
            return result;
        }

        LogicalExpireData wrapper = deserialize(json, LogicalExpireData.class);
        if (wrapper == null || wrapper.getData() == null) {
            return null;
        }

        // 未过期 → 直接返回
        if (wrapper.getExpireTime() != null && wrapper.getExpireTime().isAfter(LocalDateTime.now())) {
            localCache.put(key, wrapper.getData());
            return deserialize(wrapper.getData(), clazz);
        }

        // 已过期 → 尝试获取锁异步刷新
        String lockKey = key + ":lock";
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(locked)) {
            // 获锁 → 异步刷新（此处用新线程简化，生产可换线程池）
            REFRESH_POOL.submit(() -> {
                try {
                    R result = dbFallback.apply(key);
                    if (result != null) {
                        String serialized = serialize(result);
                        LogicalExpireData newWrapper = new LogicalExpireData();
                        newWrapper.setData(serialized);
                        newWrapper.setExpireTime(LocalDateTime.now().plus(ttl));
                        stringRedisTemplate.opsForValue().set(key, serialize(newWrapper));
                        localCache.put(key, serialized);
                    }
                } finally {
                    stringRedisTemplate.delete(lockKey);
                }
            });
        }

        // 无论是否获锁，返回旧数据
        return deserialize(wrapper.getData(), clazz);
    }

    // ===== P1-4 雪崩防护: 随机TTL + L1降级 =====

    /**
     * Redis 不可用时从 L1 本地缓存兜底。
     */
    public <R> R queryWithDegradation(String key, Class<R> clazz,
                                       Duration ttl, Function<String, R> dbFallback) {
        try {
            return queryWithPassThrough(key, clazz, ttl, dbFallback);
        } catch (Exception e) {
            log.warn("Redis 异常, 降级到本地缓存: key={}", key, e);
            String l1 = localCache.getIfPresent(key);
            if (l1 != null && !NULL_MARKER.equals(l1)) {
                return deserialize(l1, clazz);
            }
            // L1 也没有 → 直接查 DB
            return dbFallback.apply(key);
        }
    }

    // ===== 通用操作 =====

    public void delete(String key) {
        stringRedisTemplate.delete(key);
        localCache.invalidate(key);
    }

    public void set(String key, Object value, Duration ttl) {
        String serialized = serialize(value);
        stringRedisTemplate.opsForValue().set(key, serialized, withJitter(ttl));
        localCache.put(key, serialized);
    }

    // ===== 内部方法 =====

    private Duration withJitter(Duration base) {
        long seconds = base.getSeconds() + randomJitter(TTL_JITTER_SECONDS);
        return Duration.ofSeconds(Math.max(seconds, 60));
    }

    private long randomJitter(long bound) {
        if (bound <= 0) return 0;
        return ThreadLocalRandom.current().nextLong(0, bound);
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化失败", e);
            return null;
        }
    }

    private <T> T deserialize(String json, Class<T> clazz) {
        if (json == null || NULL_MARKER.equals(json)) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("反序列化失败: {}", json, e);
            return null;
        }
    }

    @Data
    public static class LogicalExpireData {
        private String data;
        private LocalDateTime expireTime;
    }
}
