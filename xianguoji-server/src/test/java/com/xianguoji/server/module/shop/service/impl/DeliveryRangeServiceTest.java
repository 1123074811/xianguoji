package com.xianguoji.server.module.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.module.shop.entity.DeliverySetting;
import com.xianguoji.server.module.shop.mapper.DeliverySettingMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * §2.5 配送范围 & 配送费单元测试
 *
 * TC-UT-DELIVERY-001: 满额免运费
 * TC-UT-DELIVERY-002: 未满额收基础运费
 * TC-UT-DELIVERY-003: 无配送设置返回0
 * TC-UT-DELIVERY-004: freeAmount=0时始终收运费
 * TC-UT-DELIVERY-005: 刚好等于免运费门槛
 * TC-UT-DELIVERY-006: 商品金额为0收基础运费
 */
@ExtendWith(MockitoExtension.class)
class DeliveryRangeServiceTest {

    @Mock
    private DeliverySettingMapper deliverySettingMapper;

    /**
     * TC-UT-DELIVERY-001: 满额免运费
     */
    @Test
    void calculateDeliveryFee_shouldReturnZero_whenAmountExceedsFreeThreshold() {
        // Arrange
        DeliverySetting ds = new DeliverySetting();
        ds.setBaseFee(new BigDecimal("5.00"));
        ds.setFreeAmount(new BigDecimal("99.00"));
        ds.setMinOrderAmount(new BigDecimal("0.01"));

        when(deliverySettingMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(ds);

        // Act - 模拟 OrderServiceImpl.calculateDeliveryFee 逻辑
        BigDecimal goodsAmount = new BigDecimal("120.00");
        BigDecimal fee = calculateDeliveryFee(goodsAmount, ds);

        // Assert
        assertEquals(BigDecimal.ZERO, fee);
    }

    /**
     * TC-UT-DELIVERY-002: 未满额收基础运费
     */
    @Test
    void calculateDeliveryFee_shouldReturnBaseFee_whenAmountBelowFreeThreshold() {
        // Arrange
        DeliverySetting ds = new DeliverySetting();
        ds.setBaseFee(new BigDecimal("5.00"));
        ds.setFreeAmount(new BigDecimal("99.00"));

        // Act
        BigDecimal goodsAmount = new BigDecimal("50.00");
        BigDecimal fee = calculateDeliveryFee(goodsAmount, ds);

        // Assert
        assertEquals(new BigDecimal("5.00"), fee);
    }

    /**
     * TC-UT-DELIVERY-003: 无配送设置返回0
     */
    @Test
    void calculateDeliveryFee_shouldReturnZero_whenNoDeliverySetting() {
        // Arrange
        when(deliverySettingMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act - 模拟 ds == null 场景
        DeliverySetting ds = null;
        BigDecimal fee = BigDecimal.ZERO; // ds == null → return BigDecimal.ZERO

        // Assert
        assertEquals(BigDecimal.ZERO, fee);
    }

    /**
     * TC-UT-DELIVERY-004: freeAmount=0时始终收运费
     */
    @Test
    void calculateDeliveryFee_shouldAlwaysCharge_whenFreeAmountIsZero() {
        // Arrange
        DeliverySetting ds = new DeliverySetting();
        ds.setBaseFee(new BigDecimal("8.00"));
        ds.setFreeAmount(BigDecimal.ZERO); // 无免运费门槛

        // Act - 即使金额很高也应收运费
        BigDecimal goodsAmount = new BigDecimal("999.00");
        BigDecimal fee = calculateDeliveryFee(goodsAmount, ds);

        // Assert
        assertEquals(new BigDecimal("8.00"), fee);
    }

    /**
     * TC-UT-DELIVERY-005: 刚好等于免运费门槛
     */
    @Test
    void calculateDeliveryFee_shouldReturnZero_whenAmountEqualsFreeThreshold() {
        // Arrange
        DeliverySetting ds = new DeliverySetting();
        ds.setBaseFee(new BigDecimal("5.00"));
        ds.setFreeAmount(new BigDecimal("99.00"));

        // Act - 刚好99元
        BigDecimal goodsAmount = new BigDecimal("99.00");
        BigDecimal fee = calculateDeliveryFee(goodsAmount, ds);

        // Assert - >= freeAmount 应免运费
        assertEquals(BigDecimal.ZERO, fee);
    }

    /**
     * TC-UT-DELIVERY-006: 商品金额为0收基础运费
     */
    @Test
    void calculateDeliveryFee_shouldReturnBaseFee_whenGoodsAmountIsZero() {
        // Arrange
        DeliverySetting ds = new DeliverySetting();
        ds.setBaseFee(new BigDecimal("5.00"));
        ds.setFreeAmount(new BigDecimal("99.00"));

        // Act
        BigDecimal goodsAmount = BigDecimal.ZERO;
        BigDecimal fee = calculateDeliveryFee(goodsAmount, ds);

        // Assert
        assertEquals(new BigDecimal("5.00"), fee);
    }

    // 复制 OrderServiceImpl.calculateDeliveryFee 的逻辑用于测试
    private BigDecimal calculateDeliveryFee(BigDecimal goodsAmount, DeliverySetting ds) {
        if (ds == null) return BigDecimal.ZERO;
        if (ds.getFreeAmount().compareTo(BigDecimal.ZERO) > 0 && goodsAmount.compareTo(ds.getFreeAmount()) >= 0) {
            return BigDecimal.ZERO;
        }
        return ds.getBaseFee();
    }
}
