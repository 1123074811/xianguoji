package com.xianguoji.server.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductUvService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "xgj:uv:product:";
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 记录商品 UV（用户访问商品详情时调用）
     */
    public void recordUv(Long productId, Long userId) {
        String key = KEY_PREFIX + productId + ":" + LocalDate.now().format(F);
        stringRedisTemplate.opsForHyperLogLog().add(key, String.valueOf(userId));
        // 48h 过期
        stringRedisTemplate.expire(key, Duration.ofHours(48));
    }

    /**
     * 获取当日某商品 UV 数
     */
    public long getDailyUv(Long productId) {
        String key = KEY_PREFIX + productId + ":" + LocalDate.now().format(F);
        Long count = stringRedisTemplate.opsForHyperLogLog().size(key);
        return count != null ? count : 0;
    }

    /**
     * 获取指定日期某商品 UV 数
     */
    public long getUvByDate(Long productId, LocalDate date) {
        String key = KEY_PREFIX + productId + ":" + date.format(F);
        Long count = stringRedisTemplate.opsForHyperLogLog().size(key);
        return count != null ? count : 0;
    }
}
