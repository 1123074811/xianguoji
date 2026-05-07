package com.xianguoji.server.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TC-UT-ORDER-004: StockService.deduct 并发扣减
 */
@ExtendWith(MockitoExtension.class)
class StockRedisHelperTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private StockRedisHelper stockRedisHelper;

    /**
     * 测试设置库存
     */
    @Test
    void setStock_shouldSetValueToRedis() {
        // Arrange
        Long skuId = 1L;
        int stock = 100;

        // Act
        stockRedisHelper.setStock(skuId, stock);

        // Assert
        verify(stringRedisTemplate.opsForValue()).set("xgj:stock:sku:" + skuId, String.valueOf(stock));
    }

    /**
     * TC-UT-ORDER-004: 库存扣减成功
     */
    @Test
    void deduct_shouldReturnTrue_whenStockIsSufficient() {
        // Arrange
        Long skuId = 1L;
        int quantity = 5;
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString()))
                .thenReturn(1L);

        // Act
        boolean result = stockRedisHelper.deduct(skuId, quantity);

        // Assert
        assertTrue(result);
        verify(stringRedisTemplate).execute(any(DefaultRedisScript.class), 
                eq(Collections.singletonList("xgj:stock:sku:" + skuId)), 
                eq(String.valueOf(quantity)));
    }

    /**
     * TC-UT-ORDER-004: 库存不足扣减失败
     */
    @Test
    void deduct_shouldReturnFalse_whenStockIsInsufficient() {
        // Arrange
        Long skuId = 1L;
        int quantity = 5;
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString()))
                .thenReturn(0L);

        // Act
        boolean result = stockRedisHelper.deduct(skuId, quantity);

        // Assert
        assertFalse(result);
    }

    /**
     * TC-UT-ORDER-004: Redis执行异常
     */
    @Test
    void deduct_shouldReturnFalse_whenRedisReturnsNull() {
        // Arrange
        Long skuId = 1L;
        int quantity = 5;
        when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString()))
                .thenReturn(null);

        // Act
        boolean result = stockRedisHelper.deduct(skuId, quantity);

        // Assert
        assertFalse(result);
    }

    /**
     * 测试库存回滚
     */
    @Test
    void rollback_shouldExecuteRollbackScript() {
        // Arrange
        Long skuId = 1L;
        int quantity = 5;

        // Act
        stockRedisHelper.rollback(skuId, quantity);

        // Assert
        verify(stringRedisTemplate).execute(any(DefaultRedisScript.class), 
                eq(Collections.singletonList("xgj:stock:sku:" + skuId)), 
                eq(String.valueOf(quantity)));
    }

    /**
     * 测试获取库存 - key存在
     */
    @Test
    void getStock_shouldReturnValue_whenKeyExists() {
        // Arrange
        Long skuId = 1L;
        when(stringRedisTemplate.opsForValue().get("xgj:stock:sku:" + skuId)).thenReturn("50");

        // Act
        int stock = stockRedisHelper.getStock(skuId);

        // Assert
        assertEquals(50, stock);
    }

    /**
     * 测试获取库存 - key不存在
     */
    @Test
    void getStock_shouldReturnMinusOne_whenKeyNotExists() {
        // Arrange
        Long skuId = 1L;
        when(stringRedisTemplate.opsForValue().get("xgj:stock:sku:" + skuId)).thenReturn(null);

        // Act
        int stock = stockRedisHelper.getStock(skuId);

        // Assert
        assertEquals(-1, stock);
    }
}
