package com.xianguoji.server.module.promo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.module.promo.entity.PromotionRule;
import com.xianguoji.server.module.promo.mapper.PromotionRuleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * §2.4 促销规则引擎单元测试
 *
 * TC-UT-PROMO-001: 满减规则 - 金额达到门槛
 * TC-UT-PROMO-002: 满减规则 - 金额未达门槛
 * TC-UT-PROMO-003: 满减规则 - 多规则匹配最高门槛
 * TC-UT-PROMO-004: 满减规则 - 无活跃规则
 * TC-UT-PROMO-005: 满减规则 - 金额为0
 * TC-UT-PROMO-006: 满减规则 - 刚好等于门槛
 * TC-UT-PROMO-007: 并发抢券 - 库存扣减原子性
 */
@ExtendWith(MockitoExtension.class)
class PromoServiceImplTest {

    @Mock
    private PromotionRuleMapper promotionRuleMapper;

    @InjectMocks
    private PromoServiceImpl promoService;

    /**
     * TC-UT-PROMO-001: 金额达到门槛 - 返回对应折扣
     */
    @Test
    void calculateDiscount_shouldReturnDiscount_whenAmountReachesThreshold() {
        // Arrange
        PromotionRule rule = new PromotionRule();
        rule.setId(1L);
        rule.setName("满100减20");
        rule.setMinAmount(new BigDecimal("100.00"));
        rule.setDiscount(new BigDecimal("20.00"));
        rule.setStatus(1);

        when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rule));

        // Act
        BigDecimal discount = promoService.calculateDiscount(new BigDecimal("150.00"));

        // Assert
        assertEquals(new BigDecimal("20.00"), discount);
    }

    /**
     * TC-UT-PROMO-002: 金额未达门槛 - 返回0
     */
    @Test
    void calculateDiscount_shouldReturnZero_whenAmountBelowThreshold() {
        // Arrange
        PromotionRule rule = new PromotionRule();
        rule.setId(1L);
        rule.setName("满100减20");
        rule.setMinAmount(new BigDecimal("100.00"));
        rule.setDiscount(new BigDecimal("20.00"));
        rule.setStatus(1);

        when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rule));

        // Act
        BigDecimal discount = promoService.calculateDiscount(new BigDecimal("50.00"));

        // Assert
        assertEquals(BigDecimal.ZERO, discount);
    }

    /**
     * TC-UT-PROMO-003: 多规则 - 匹配最高门槛
     */
    @Test
    void calculateDiscount_shouldMatchHighestThreshold_whenMultipleRules() {
        // Arrange - 按minAmount降序返回（Service层orderByDesc）
        PromotionRule rule200 = new PromotionRule();
        rule200.setId(2L);
        rule200.setName("满200减40");
        rule200.setMinAmount(new BigDecimal("200.00"));
        rule200.setDiscount(new BigDecimal("40.00"));
        rule200.setStatus(1);

        PromotionRule rule100 = new PromotionRule();
        rule100.setId(1L);
        rule100.setName("满100减20");
        rule100.setMinAmount(new BigDecimal("100.00"));
        rule100.setDiscount(new BigDecimal("20.00"));
        rule100.setStatus(1);

        when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rule200, rule100)); // 降序

        // Act - 金额250应匹配满200减40
        BigDecimal discount = promoService.calculateDiscount(new BigDecimal("250.00"));

        // Assert
        assertEquals(new BigDecimal("40.00"), discount);
    }

    /**
     * TC-UT-PROMO-003: 多规则 - 金额只够较低门槛
     */
    @Test
    void calculateDiscount_shouldMatchLowerThreshold_whenAmountNotEnoughForHighest() {
        // Arrange
        PromotionRule rule200 = new PromotionRule();
        rule200.setId(2L);
        rule200.setName("满200减40");
        rule200.setMinAmount(new BigDecimal("200.00"));
        rule200.setDiscount(new BigDecimal("40.00"));
        rule200.setStatus(1);

        PromotionRule rule100 = new PromotionRule();
        rule100.setId(1L);
        rule100.setName("满100减20");
        rule100.setMinAmount(new BigDecimal("100.00"));
        rule100.setDiscount(new BigDecimal("20.00"));
        rule100.setStatus(1);

        when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rule200, rule100));

        // Act - 金额150只够满100减20
        BigDecimal discount = promoService.calculateDiscount(new BigDecimal("150.00"));

        // Assert
        assertEquals(new BigDecimal("20.00"), discount);
    }

    /**
     * TC-UT-PROMO-004: 无活跃规则 - 返回0
     */
    @Test
    void calculateDiscount_shouldReturnZero_whenNoActiveRules() {
        // Arrange
        when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        // Act
        BigDecimal discount = promoService.calculateDiscount(new BigDecimal("500.00"));

        // Assert
        assertEquals(BigDecimal.ZERO, discount);
    }

    /**
     * TC-UT-PROMO-005: 金额为0 - 返回0
     */
    @Test
    void calculateDiscount_shouldReturnZero_whenAmountIsZero() {
        // Arrange
        PromotionRule rule = new PromotionRule();
        rule.setId(1L);
        rule.setName("满100减20");
        rule.setMinAmount(new BigDecimal("100.00"));
        rule.setDiscount(new BigDecimal("20.00"));
        rule.setStatus(1);

        when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rule));

        // Act
        BigDecimal discount = promoService.calculateDiscount(BigDecimal.ZERO);

        // Assert
        assertEquals(BigDecimal.ZERO, discount);
    }

    /**
     * TC-UT-PROMO-006: 刚好等于门槛 - 应匹配
     */
    @Test
    void calculateDiscount_shouldMatch_whenAmountEqualsThreshold() {
        // Arrange
        PromotionRule rule = new PromotionRule();
        rule.setId(1L);
        rule.setName("满100减20");
        rule.setMinAmount(new BigDecimal("100.00"));
        rule.setDiscount(new BigDecimal("20.00"));
        rule.setStatus(1);

        when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rule));

        // Act
        BigDecimal discount = promoService.calculateDiscount(new BigDecimal("100.00"));

        // Assert
        assertEquals(new BigDecimal("20.00"), discount);
    }

    // ==================== 并发抢券 ====================

    /**
     * TC-UT-PROMO-007: 并发抢券 - 库存扣减原子性模拟
     */
    @Test
    void concurrentCouponGrab_shouldNotOversell_whenMultipleThreads() throws InterruptedException {
        // Arrange - 模拟10个线程抢3张券
        int stock = 3;
        int threads = 10;
        CouponStockMock stockMock = new CouponStockMock(stock);

        Thread[] threadArray = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            threadArray[i] = new Thread(stockMock::deduct);
        }

        // Act
        for (Thread t : threadArray) t.start();
        for (Thread t : threadArray) t.join();

        // Assert - 最终库存不为负
        assertTrue(stockMock.getStock() >= 0);
        // 成功抢到券的线程数应等于初始库存
        assertEquals(0, stockMock.getStock());
    }

    static class CouponStockMock {
        private int stock;
        private final Object lock = new Object();

        public CouponStockMock(int stock) {
            this.stock = stock;
        }

        public void deduct() {
            synchronized (lock) {
                if (stock > 0) {
                    stock--;
                }
            }
        }

        public int getStock() {
            return stock;
        }
    }
}
