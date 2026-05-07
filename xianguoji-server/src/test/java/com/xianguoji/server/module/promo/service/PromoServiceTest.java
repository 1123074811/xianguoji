package com.xianguoji.server.module.promo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.AbstractServiceTest;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.promo.dto.UsableCouponQry;
import com.xianguoji.server.module.promo.entity.Coupon;
import com.xianguoji.server.module.promo.entity.PromotionRule;
import com.xianguoji.server.module.promo.entity.UserCoupon;
import com.xianguoji.server.module.promo.mapper.CouponMapper;
import com.xianguoji.server.module.promo.mapper.PromotionRuleMapper;
import com.xianguoji.server.module.promo.mapper.UserCouponMapper;
import com.xianguoji.server.module.promo.service.impl.PromoServiceImpl;
import com.xianguoji.server.module.promo.vo.CouponVO;
import com.xianguoji.server.module.promo.vo.UserCouponVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * TC-UT-PROMO series: Promo service unit tests (Coupon)
 */
@DisplayName("Promo Service Unit Tests (Coupon)")
class PromoServiceTest extends AbstractServiceTest {

    @Mock
    private CouponMapper couponMapper;
    
    @Mock
    private UserCouponMapper userCouponMapper;
    
    @Mock
    private PromotionRuleMapper promotionRuleMapper;
    
    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private PromoServiceImpl promoService;

    @Nested
    @DisplayName("getAvailableCoupons")
    class GetAvailableCouponsTests {

        @Test
        @DisplayName("TC-UT-PROMO-001: Should return available coupons")
        void getAvailableCoupons_shouldReturnCoupons_whenCouponsExist() {
            // Arrange
            Long uid = 1L;
            Coupon coupon = createCoupon(1L, "Test Coupon", 1);
            when(couponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(coupon));
            when(userCouponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // Act
            List<CouponVO> result = promoService.getAvailableCoupons(uid);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Test Coupon", result.get(0).getName());
        }

        @Test
        @DisplayName("TC-UT-PROMO-002: Should return user received count")
        void getAvailableCoupons_shouldIncludeUserReceivedCount_whenUserHasReceived() {
            // Arrange
            Long uid = 1L;
            Coupon coupon = createCoupon(1L, "Test Coupon", 1);
            when(couponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(coupon));
            when(userCouponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

            // Act
            List<CouponVO> result = promoService.getAvailableCoupons(uid);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(2, result.get(0).getUserReceivedCount());
        }

        @Test
        @DisplayName("TC-UT-PROMO-003: Should return empty list when no coupons")
        void getAvailableCoupons_shouldReturnEmptyList_whenNoCoupons() {
            // Arrange
            Long uid = 1L;
            when(couponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            // Act
            List<CouponVO> result = promoService.getAvailableCoupons(uid);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("receiveCoupon")
    class ReceiveCouponTests {

        @Test
        @DisplayName("TC-UT-PROMO-004: Should throw exception when coupon not available")
        void receiveCoupon_shouldThrowException_whenCouponNotAvailable() {
            // Arrange
            Long uid = 1L;
            Long couponId = 1L;
            when(couponMapper.selectById(couponId)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                promoService.receiveCoupon(uid, couponId);
            });
            assertEquals(ResultCode.COUPON_NOT_AVAILABLE, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-PROMO-005: Should throw exception when coupon status is not active")
        void receiveCoupon_shouldThrowException_whenCouponNotActive() {
            // Arrange
            Long uid = 1L;
            Long couponId = 1L;
            Coupon coupon = createCoupon(1L, "Test Coupon", 0);
            when(couponMapper.selectById(couponId)).thenReturn(coupon);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                promoService.receiveCoupon(uid, couponId);
            });
            assertEquals(ResultCode.COUPON_NOT_AVAILABLE, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-PROMO-006: Should throw exception when Lua script returns 0 (sold out)")
        void receiveCoupon_shouldThrowException_whenLuaReturnsZero() {
            // Arrange
            Long uid = 1L;
            Long couponId = 1L;
            Coupon coupon = createCoupon(1L, "Test Coupon", 1);
            when(couponMapper.selectById(couponId)).thenReturn(coupon);
            when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString()))
                    .thenReturn(0L);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                promoService.receiveCoupon(uid, couponId);
            });
            assertEquals(ResultCode.COUPON_NOT_AVAILABLE, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-PROMO-007: Should throw exception when Lua script returns -1 (user limit exceeded)")
        void receiveCoupon_shouldThrowException_whenLuaReturnsMinusOne() {
            // Arrange
            Long uid = 1L;
            Long couponId = 1L;
            Coupon coupon = createCoupon(1L, "Test Coupon", 1);
            when(couponMapper.selectById(couponId)).thenReturn(coupon);
            when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString()))
                    .thenReturn(-1L);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                promoService.receiveCoupon(uid, couponId);
            });
            assertEquals(ResultCode.COUPON_NOT_AVAILABLE, exception.getResultCode());
            assertTrue(exception.getMessage().contains("每人限领"));
        }

        @Test
        @DisplayName("TC-UT-PROMO-008: Should successfully receive coupon")
        void receiveCoupon_shouldReceiveCoupon_success() {
            // Arrange
            Long uid = 1L;
            Long couponId = 1L;
            Coupon coupon = createCoupon(1L, "Test Coupon", 1);
            coupon.setValidType(1);
            coupon.setEndTime(LocalDateTime.now().plusDays(7));
            
            when(couponMapper.selectById(couponId)).thenReturn(coupon);
            when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString()))
                    .thenReturn(1L);
            when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);
            when(couponMapper.update(any(), any(LambdaQueryWrapper.class))).thenReturn(1);

            // Act
            promoService.receiveCoupon(uid, couponId);

            // Assert
            verify(userCouponMapper).insert(any(UserCoupon.class));
            verify(couponMapper).update(any(), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-UT-PROMO-009: Should rollback Redis count on DB failure")
        void receiveCoupon_shouldRollbackRedis_onDbFailure() {
            // Arrange
            Long uid = 1L;
            Long couponId = 1L;
            Coupon coupon = createCoupon(1L, "Test Coupon", 1);
            coupon.setValidType(1);
            coupon.setEndTime(LocalDateTime.now().plusDays(7));
            
            when(couponMapper.selectById(couponId)).thenReturn(coupon);
            when(stringRedisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString()))
                    .thenReturn(1L);
            when(userCouponMapper.insert(any(UserCoupon.class))).thenThrow(new RuntimeException("DB Error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                promoService.receiveCoupon(uid, couponId);
            });

            // Verify rollback was attempted
            verify(stringRedisTemplate, atLeastOnce()).opsForValue();
        }
    }

    @Nested
    @DisplayName("getUserCoupons")
    class GetUserCouponsTests {

        @Test
        @DisplayName("TC-UT-PROMO-010: Should return user coupons with status filter")
        void getUserCoupons_shouldReturnCouponsWithStatus_success() {
            // Arrange
            Long uid = 1L;
            Integer status = 0;
            UserCoupon userCoupon = createUserCoupon(1L, uid, 1L, status);
            Coupon coupon = createCoupon(1L, "Test Coupon", 1);
            
            when(userCouponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(userCoupon));
            when(couponMapper.selectById(1L)).thenReturn(coupon);

            // Act
            List<UserCouponVO> result = promoService.getUserCoupons(uid, status);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Test Coupon", result.get(0).getCoupon().getName());
        }

        @Test
        @DisplayName("TC-UT-PROMO-011: Should return all user coupons when status is null")
        void getUserCoupons_shouldReturnAllCoupons_whenStatusIsNull() {
            // Arrange
            Long uid = 1L;
            UserCoupon userCoupon = createUserCoupon(1L, uid, 1L, 0);
            Coupon coupon = createCoupon(1L, "Test Coupon", 1);
            
            when(userCouponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(userCoupon));
            when(couponMapper.selectById(1L)).thenReturn(coupon);

            // Act
            List<UserCouponVO> result = promoService.getUserCoupons(uid, null);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getUsableCoupons")
    class GetUsableCouponsTests {

        @Test
        @DisplayName("TC-UT-PROMO-012: Should return usable coupons for order")
        void getUsableCoupons_shouldReturnUsableCoupons_success() {
            // Arrange
            Long uid = 1L;
            UsableCouponQry qry = new UsableCouponQry();
            qry.setTotalAmount(new BigDecimal("100"));
            qry.setProductIds(Arrays.asList(1L, 2L));
            
            UserCoupon userCoupon = createUserCoupon(1L, uid, 1L, 0);
            userCoupon.setExpireAt(LocalDateTime.now().plusDays(7));
            Coupon coupon = createCoupon(1L, "Test Coupon", 1);
            coupon.setMinAmount(new BigDecimal("50"));
            coupon.setScope(0); // All products
            
            when(userCouponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(userCoupon));
            when(couponMapper.selectById(1L)).thenReturn(coupon);

            // Act
            List<UserCouponVO> result = promoService.getUsableCoupons(uid, qry);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertNull(result.get(0).getUnavailableReason());
        }

        @Test
        @DisplayName("TC-UT-PROMO-013: Should mark coupon as unavailable when min amount not met")
        void getUsableCoupons_shouldMarkUnavailable_whenMinAmountNotMet() {
            // Arrange
            Long uid = 1L;
            UsableCouponQry qry = new UsableCouponQry();
            qry.setTotalAmount(new BigDecimal("30"));
            
            UserCoupon userCoupon = createUserCoupon(1L, uid, 1L, 0);
            userCoupon.setExpireAt(LocalDateTime.now().plusDays(7));
            Coupon coupon = createCoupon(1L, "Test Coupon", 1);
            coupon.setMinAmount(new BigDecimal("50"));
            
            when(userCouponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(userCoupon));
            when(couponMapper.selectById(1L)).thenReturn(coupon);

            // Act
            List<UserCouponVO> result = promoService.getUsableCoupons(uid, qry);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertNotNull(result.get(0).getUnavailableReason());
            assertTrue(result.get(0).getUnavailableReason().contains("未满"));
        }

        @Test
        @DisplayName("TC-UT-PROMO-014: Should filter expired coupons")
        void getUsableCoupons_shouldFilterExpiredCoupons_success() {
            // Arrange
            Long uid = 1L;
            UsableCouponQry qry = new UsableCouponQry();
            qry.setTotalAmount(new BigDecimal("100"));
            
            UserCoupon userCoupon = createUserCoupon(1L, uid, 1L, 0);
            userCoupon.setExpireAt(LocalDateTime.now().minusDays(1)); // Expired
            
            when(userCouponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(userCoupon));

            // Act
            List<UserCouponVO> result = promoService.getUsableCoupons(uid, qry);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("calculateDiscount")
    class CalculateDiscountTests {

        @Test
        @DisplayName("TC-UT-PROMO-015: Should return discount when rule matches")
        void calculateDiscount_shouldReturnDiscount_whenRuleMatches() {
            // Arrange
            BigDecimal amount = new BigDecimal("200");
            PromotionRule rule = new PromotionRule();
            rule.setMinAmount(new BigDecimal("100"));
            rule.setDiscount(new BigDecimal("20"));
            
            when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(rule));

            // Act
            BigDecimal result = promoService.calculateDiscount(amount);

            // Assert
            assertEquals(new BigDecimal("20"), result);
        }

        @Test
        @DisplayName("TC-UT-PROMO-016: Should return zero when no rule matches")
        void calculateDiscount_shouldReturnZero_whenNoRuleMatches() {
            // Arrange
            BigDecimal amount = new BigDecimal("50");
            PromotionRule rule = new PromotionRule();
            rule.setMinAmount(new BigDecimal("100"));
            rule.setDiscount(new BigDecimal("20"));
            
            when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(rule));

            // Act
            BigDecimal result = promoService.calculateDiscount(amount);

            // Assert
            assertEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("TC-UT-PROMO-017: Should return zero when no rules exist")
        void calculateDiscount_shouldReturnZero_whenNoRules() {
            // Arrange
            BigDecimal amount = new BigDecimal("100");
            when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            // Act
            BigDecimal result = promoService.calculateDiscount(amount);

            // Assert
            assertEquals(BigDecimal.ZERO, result);
        }
    }

    // Helper methods
    private Coupon createCoupon(Long id, String name, Integer status) {
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setName(name);
        coupon.setType(1); // Fixed amount
        coupon.setAmount(new BigDecimal("10"));
        coupon.setMinAmount(new BigDecimal("50"));
        coupon.setTotal(100);
        coupon.setReceivedCount(50);
        coupon.setPerUserLimit(1);
        coupon.setValidType(1);
        coupon.setStartTime(LocalDateTime.now());
        coupon.setEndTime(LocalDateTime.now().plusDays(7));
        coupon.setScope(0);
        coupon.setStatus(status);
        coupon.setCreatedAt(LocalDateTime.now());
        return coupon;
    }

    private UserCoupon createUserCoupon(Long id, Long userId, Long couponId, Integer status) {
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setId(id);
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(status);
        userCoupon.setReceivedAt(LocalDateTime.now());
        userCoupon.setExpireAt(LocalDateTime.now().plusDays(7));
        return userCoupon;
    }
}

