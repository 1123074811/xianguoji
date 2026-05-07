package com.xianguoji.server.module.stat.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.common.cache.SalesRankService;
import com.xianguoji.server.module.catalog.entity.Category;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.mapper.CategoryMapper;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.entity.Refund;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import com.xianguoji.server.module.order.mapper.RefundMapper;
import com.xianguoji.server.module.promo.mapper.UserCouponMapper;
import com.xianguoji.server.module.review.mapper.ReviewMapper;
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
 * TC-API-STAT series: Stat controller API tests
 */
@DisplayName("Stat Controller API Tests")
class StatControllerTest extends AbstractApiTest {

    @MockBean
    private OrderMapper orderMapper;
    
    @MockBean
    private UserMapper userMapper;
    
    @MockBean
    private ProductMapper productMapper;
    
    @MockBean
    private CategoryMapper categoryMapper;
    
    @MockBean
    private ReviewMapper reviewMapper;
    
    @MockBean
    private RefundMapper refundMapper;
    
    @MockBean
    private SalesRankService salesRankService;

    private String adminToken = "test_admin_token";

    @Nested
    @DisplayName("GET /api/admin/stat/dashboard")
    class DashboardTests {

        @Test
        @DisplayName("TC-API-STAT-001: Should return dashboard metrics")
        void dashboard_shouldReturnMetrics_success() throws Exception {
            // Arrange
            when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
            List<Order> paidOrders = List.of(createOrder(1L, new BigDecimal("100"), 1, 1));
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(paidOrders, new ArrayList<>());
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            // Act
            MvcResult result = performGet("/api/admin/stat/dashboard", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-STAT-002: Should return 403 when not admin")
        void dashboard_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/stat/dashboard", "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/stat/order-trend")
    class OrderTrendTests {

        @Test
        @DisplayName("TC-API-STAT-003: Should return order trend")
        void orderTrend_shouldReturnTrend_success() throws Exception {
            // Arrange
            when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            // Act
            MvcResult result = performGet("/api/admin/stat/order-trend?days=7", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/stat/order-status")
    class OrderStatusTests {

        @Test
        @DisplayName("TC-API-STAT-004: Should return order status distribution")
        void orderStatus_shouldReturnDistribution_success() throws Exception {
            // Arrange
            when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L, 2L, 1L);

            // Act
            MvcResult result = performGet("/api/admin/stat/order-status", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/stat/top-products")
    class TopProductsTests {

        @Test
        @DisplayName("TC-API-STAT-005: Should return top products")
        void topProducts_shouldReturnTopProducts_success() throws Exception {
            // Arrange
            List<Product> products = List.of(createProduct(1L, "Apple", 100));
            when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(products);
            when(salesRankService.getTodaySalesDelta()).thenReturn(Map.of(1L, 5));

            // Act
            MvcResult result = performGet("/api/admin/stat/top-products?limit=10", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/stat/todo")
    class TodoTests {

        @Test
        @DisplayName("TC-API-STAT-006: Should return todo list")
        void todo_shouldReturnTodo_success() throws Exception {
            // Arrange
            when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
            when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(refundMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            // Act
            MvcResult result = performGet("/api/admin/stat/todo", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/stat/analysis/kpi")
    class AnalysisKpiTests {

        @Test
        @DisplayName("TC-API-STAT-007: Should return KPI analysis")
        void analysisKpi_shouldReturnKpi_success() throws Exception {
            // Arrange
            List<Order> paidOrders = List.of(createOrder(1L, new BigDecimal("100"), 1, 1));
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(paidOrders);
            when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

            // Act
            MvcResult result = performGet("/api/admin/stat/analysis/kpi?days=7", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/stat/analysis/revenue-trend")
    class RevenueTrendTests {

        @Test
        @DisplayName("TC-API-STAT-008: Should return revenue trend")
        void revenueTrend_shouldReturnTrend_success() throws Exception {
            // Arrange
            List<Order> orders = List.of(createOrder(1L, new BigDecimal("100"), 1, 1));
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(orders);

            // Act
            MvcResult result = performGet("/api/admin/stat/analysis/revenue-trend?months=6", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/stat/analysis/category-distribution")
    class CategoryDistributionTests {

        @Test
        @DisplayName("TC-API-STAT-009: Should return category distribution")
        void categoryDistribution_shouldReturnDistribution_success() throws Exception {
            // Arrange
            List<Product> products = List.of(createProduct(1L, "Apple", 100));
            List<Category> categories = List.of(createCategory(1L, "Fruits"));
            when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(products);
            when(categoryMapper.selectList(any())).thenReturn(categories);

            // Act
            MvcResult result = performGet("/api/admin/stat/analysis/category-distribution", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/stat/analysis/customer-segments")
    class CustomerSegmentsTests {

        @Test
        @DisplayName("TC-API-STAT-010: Should return customer segments")
        void customerSegments_shouldReturnSegments_success() throws Exception {
            // Arrange
            List<Order> paidOrders = List.of(createOrder(1L, new BigDecimal("500"), 1, 1));
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(paidOrders, new ArrayList<>());
            List<User> oldUsers = new ArrayList<>();
            when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(oldUsers);

            // Act
            MvcResult result = performGet("/api/admin/stat/analysis/customer-segments", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/stat/analysis/product-performance")
    class ProductPerformanceTests {

        @Test
        @DisplayName("TC-API-STAT-011: Should return product performance")
        void productPerformance_shouldReturnPerformance_success() throws Exception {
            // Arrange
            List<Product> products = List.of(createProduct(1L, "Apple", 100));
            when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(products);
            when(salesRankService.getTodaySalesDelta()).thenReturn(Map.of());

            // Act
            MvcResult result = performGet("/api/admin/stat/analysis/product-performance?limit=10", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/stat/analysis/hourly-heatmap")
    class HourlyHeatmapTests {

        @Test
        @DisplayName("TC-API-STAT-012: Should return hourly heatmap")
        void hourlyHeatmap_shouldReturnHeatmap_success() throws Exception {
            // Arrange
            when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            // Act
            MvcResult result = performGet("/api/admin/stat/analysis/hourly-heatmap", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    // Helper methods
    private Order createOrder(Long id, BigDecimal payAmount, Integer payStatus, Integer status) {
        Order order = new Order();
        order.setId(id);
        order.setPayAmount(payAmount);
        order.setPayStatus(payStatus);
        order.setStatus(status);
        order.setUserId(1L);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private Product createProduct(Long id, String name, Integer sales) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCategoryId(1L);
        product.setMinPrice(new BigDecimal("10"));
        product.setSales(sales);
        product.setStatus(1);
        return product;
    }

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }
}

