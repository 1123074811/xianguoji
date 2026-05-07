package com.xianguoji.server.module.stat.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import com.xianguoji.server.module.promo.entity.Coupon;
import com.xianguoji.server.module.promo.mapper.CouponMapper;
import com.xianguoji.server.module.promo.mapper.UserCouponMapper;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-CUSTOMER series: Customer controller API tests
 */
@DisplayName("Customer Controller API Tests")
class CustomerControllerTest extends AbstractApiTest {

    @MockBean
    private UserMapper userMapper;
    
    @MockBean
    private OrderMapper orderMapper;
    
    @MockBean
    private UserCouponMapper userCouponMapper;
    
    @MockBean
    private CouponMapper couponMapper;

    private String adminToken = "test_admin_token";

    @Nested
    @DisplayName("GET /api/admin/customer/page")
    class CustomerPageTests {

        @Test
        @DisplayName("TC-API-CUSTOMER-001: Should return customer page")
        void page_shouldReturnPage_success() throws Exception {
            // Arrange
            List<User> users = List.of(createUser(1L, "Test User", "13800138000"));
            var page = new Page<User>(1, 20);
            page.setRecords(users);
            page.setTotal(1);
            
            when(userMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
            List<Order> orders = List.of(createOrder(1L, new BigDecimal("100"), 1));
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(orders);
            when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(orders.get(0));

            // Act
            MvcResult result = performGet("/api/admin/customer/page?page=1&size=20", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-CUSTOMER-002: Should return 403 when not admin")
        void page_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/customer/page", "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/customer/{id}")
    class CustomerDetailTests {

        @Test
        @DisplayName("TC-API-CUSTOMER-003: Should return customer detail")
        void detail_shouldReturnDetail_success() throws Exception {
            // Arrange
            Long id = 1L;
            User user = createUser(id, "Test User", "13800138000");
            
            when(userMapper.selectById(id)).thenReturn(user);
            when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
            List<Order> orders = List.of(createOrder(1L, new BigDecimal("100"), 1));
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(orders);

            // Act
            MvcResult result = performGet("/api/admin/customer/" + id, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-CUSTOMER-004: Should return error when customer not found")
        void detail_shouldReturnError_whenCustomerNotFound() throws Exception {
            // Arrange
            Long id = 999L;
            when(userMapper.selectById(999L)).thenReturn(null);

            // Act
            MvcResult result = performGet("/api/admin/customer/" + id, adminToken)
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            assertFalse(response.contains("\"code\":200"));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/customer/{id}/coupon")
    class SendCouponTests {

        @Test
        @DisplayName("TC-API-CUSTOMER-005: Should send coupon successfully")
        void sendCoupon_shouldSend_success() throws Exception {
            // Arrange
            Long id = 1L;
            Long couponId = 1L;
            Map<String, Long> body = Map.of("couponId", couponId);
            
            Coupon coupon = createCoupon(couponId, "Test Coupon", 1, 30);
            when(couponMapper.selectById(couponId)).thenReturn(coupon);
            when(userCouponMapper.insert(any())).thenReturn(1);

            // Act
            MvcResult result = performPost("/api/admin/customer/" + id + "/coupon", body, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(userCouponMapper).insert(any());
        }

        @Test
        @DisplayName("TC-API-CUSTOMER-006: Should return error when coupon not found")
        void sendCoupon_shouldReturnError_whenCouponNotFound() throws Exception {
            // Arrange
            Long id = 1L;
            Long couponId = 999L;
            Map<String, Long> body = Map.of("couponId", couponId);
            
            when(couponMapper.selectById(999L)).thenReturn(null);

            // Act
            MvcResult result = performPost("/api/admin/customer/" + id + "/coupon", body, adminToken)
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            assertFalse(response.contains("\"code\":200"));
        }

        @Test
        @DisplayName("TC-API-CUSTOMER-007: Should return 403 when not admin")
        void sendCoupon_shouldReturn403_whenNotAdmin() throws Exception {
            // Arrange
            Map<String, Long> body = Map.of("couponId", 1L);

            // Act
            MvcResult result = performPost("/api/admin/customer/1/coupon", body, "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    // Helper methods
    private User createUser(Long id, String nickname, String phone) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setAvatar("http://test.com/avatar.jpg");
        user.setRegisterTime(LocalDateTime.now());
        return user;
    }

    private Order createOrder(Long id, BigDecimal payAmount, Integer payStatus) {
        Order order = new Order();
        order.setId(id);
        order.setPayAmount(payAmount);
        order.setPayStatus(payStatus);
        order.setUserId(1L);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private Coupon createCoupon(Long id, String name, Integer validType, Integer validDays) {
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setName(name);
        coupon.setValidType(validType);
        coupon.setValidDays(validDays);
        coupon.setEndTime(LocalDateTime.now().plusDays(30));
        return coupon;
    }
}

