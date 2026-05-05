package com.xianguoji.server.common.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockRedisHelper {

    private final StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> deductScript;
    private DefaultRedisScript<Long> rollbackScript;

    @PostConstruct
    public void init() {
        deductScript = new DefaultRedisScript<>();
        deductScript.setLocation(new org.springframework.core.io.ClassPathResource("lua/stock_deduct.lua"));
        deductScript.setResultType(Long.class);

        rollbackScript = new DefaultRedisScript<>();
        rollbackScript.setLocation(new org.springframework.core.io.ClassPathResource("lua/stock_rollback.lua"));
        rollbackScript.setResultType(Long.class);
    }

    private static final String KEY_PREFIX = "xgj:stock:sku:";

    /**
     * 设置 SKU 库存到 Redis（商品上架/库存变动时调用）
     */
    public void setStock(Long skuId, int stock) {
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + skuId, String.valueOf(stock));
    }

    /**
     * Redis Lua 原子预扣库存
     *
     * @return true=扣减成功, false=库存不足
     */
    public boolean deduct(Long skuId, int quantity) {
        String key = KEY_PREFIX + skuId;
        Long result = stringRedisTemplate.execute(deductScript,
                Collections.singletonList(key),
                String.valueOf(quantity));
        boolean success = result != null && result == 1L;
        if (!success) {
            log.warn("Redis预扣库存失败: skuId={}, quantity={}", skuId, quantity);
        }
        return success;
    }

    /**
     * Redis 回滚库存（DB 写订单失败时调用）
     */
    public void rollback(Long skuId, int quantity) {
        String key = KEY_PREFIX + skuId;
        stringRedisTemplate.execute(rollbackScript,
                Collections.singletonList(key),
                String.valueOf(quantity));
        log.info("Redis库存回滚: skuId={}, quantity={}", skuId, quantity);
    }

    /**
     * 获取 Redis 中的库存值，-1 表示 key 不存在
     */
    public int getStock(Long skuId) {
        String val = stringRedisTemplate.opsForValue().get(KEY_PREFIX + skuId);
        if (val == null) return -1;
        return Integer.parseInt(val);
    }
}
