package com.xianguoji.server.module.promo.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.module.promo.dto.UsableCouponQry;
import com.xianguoji.server.module.promo.service.PromoService;
import com.xianguoji.server.module.promo.vo.CouponVO;
import com.xianguoji.server.module.promo.vo.UserCouponVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-PROMO-COUPON series: Coupon controller API tests
 */
@DisplayName("Coupon Controller API Tests")
class CouponControllerTest extends AbstractApiTest {

    @MockBean
    private PromoService promoService;

    private String userToken = "test_user_token";

    @Nested
    @DisplayName("GET /api/pub/coupon/list")
    class CouponListTests {

        @Test
        @DisplayName("TC-API-PROMO-COUPON-001: Should return available coupons")
        void couponList_shouldReturnCoupons_success() throws Exception {
            // Arrange
            List<CouponVO> coupons = new ArrayList<>();
            CouponVO coupon = CouponVO.builder()
                    .id(1L)
                    .name("Test Coupon")
                    .amount(new BigDecimal("10"))
                    .minAmount(new BigDecimal("50"))
                    .total(100)
                    .receivedCount(50)
                    .perUserLimit(1)
                    .status(1)
                    .build();
            coupons.add(coupon);
            
            when(promoService.getAvailableCoupons(any())).thenReturn(coupons);

            // Act
            MvcResult result = performGet("/api/pub/coupon/list")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(promoService).getAvailableCoupons(any());
        }

        @Test
        @DisplayName("TC-API-PROMO-COUPON-002: Should return empty list when no coupons")
        void couponList_shouldReturnEmptyList_whenNoCoupons() throws Exception {
            // Arrange
            when(promoService.getAvailableCoupons(any())).thenReturn(new ArrayList<>());

            // Act
            MvcResult result = performGet("/api/pub/coupon/list")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("POST /api/u/coupon/{id}/receive")
    class ReceiveCouponTests {

        @Test
        @DisplayName("TC-API-PROMO-COUPON-003: Should receive coupon successfully")
        void receive_shouldReceiveCoupon_success() throws Exception {
            // Arrange
            Long couponId = 1L;
            doNothing().when(promoService).receiveCoupon(anyLong(), anyLong());

            // Act
            MvcResult result = performPost("/api/u/coupon/" + couponId + "/receive", null, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(promoService).receiveCoupon(anyLong(), eq(couponId));
        }

        @Test
        @DisplayName("TC-API-PROMO-COUPON-004: Should return 401 when not logged in")
        void receive_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performPost("/api/u/coupon/1/receive", null)
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/u/coupon/list")
    class MyCouponsTests {

        @Test
        @DisplayName("TC-API-PROMO-COUPON-005: Should return user coupons")
        void myCoupons_shouldReturnCoupons_success() throws Exception {
            // Arrange
            List<UserCouponVO> coupons = new ArrayList<>();
            UserCouponVO coupon = UserCouponVO.builder()
                    .id(1L)
                    .couponId(1L)
                    .status(0)
                    .expireAt(LocalDateTime.now().plusDays(7))
                    .build();
            coupons.add(coupon);
            
            when(promoService.getUserCoupons(anyLong(), any())).thenReturn(coupons);

            // Act
            MvcResult result = performGet("/api/u/coupon/list?status=0", userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(promoService).getUserCoupons(anyLong(), eq(0));
        }

        @Test
        @DisplayName("TC-API-PROMO-COUPON-006: Should return all coupons when status not provided")
        void myCoupons_shouldReturnAllCoupons_whenStatusNull() throws Exception {
            // Arrange
            List<UserCouponVO> coupons = new ArrayList<>();
            when(promoService.getUserCoupons(anyLong(), isNull())).thenReturn(coupons);

            // Act
            MvcResult result = performGet("/api/u/coupon/list", userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(promoService).getUserCoupons(anyLong(), isNull());
        }

        @Test
        @DisplayName("TC-API-PROMO-COUPON-007: Should return 401 when not logged in")
        void myCoupons_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performGet("/api/u/coupon/list")
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("POST /api/u/coupon/usable")
    class UsableCouponsTests {

        @Test
        @DisplayName("TC-API-PROMO-COUPON-008: Should return usable coupons")
        void usableCoupons_shouldReturnUsableCoupons_success() throws Exception {
            // Arrange
            UsableCouponQry qry = new UsableCouponQry();
            qry.setTotalAmount(new BigDecimal("100"));
            qry.setProductIds(List.of(1L, 2L));
            
            List<UserCouponVO> coupons = new ArrayList<>();
            UserCouponVO coupon = UserCouponVO.builder()
                    .id(1L)
                    .couponId(1L)
                    .status(0)
                    .build();
            coupons.add(coupon);
            
            when(promoService.getUsableCoupons(anyLong(), any(UsableCouponQry.class))).thenReturn(coupons);

            // Act
            MvcResult result = performPost("/api/u/coupon/usable", qry, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(promoService).getUsableCoupons(anyLong(), any(UsableCouponQry.class));
        }

        @Test
        @DisplayName("TC-API-PROMO-COUPON-009: Should return 401 when not logged in")
        void usableCoupons_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Arrange
            UsableCouponQry qry = new UsableCouponQry();
            qry.setTotalAmount(new BigDecimal("100"));

            // Act
            MvcResult result = performPost("/api/u/coupon/usable", qry)
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }
}

