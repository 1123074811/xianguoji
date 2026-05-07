package com.xianguoji.server.module.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.AbstractServiceTest;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TC-UT-REPORT series: Report service unit tests
 */
@DisplayName("Report Service Unit Tests")
class ReportServiceTest extends AbstractServiceTest {

    @Mock
    private OrderMapper orderMapper;
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ReportService reportService;

    @Nested
    @DisplayName("generate - sales report")
    class GenerateSalesTests {

        @Test
        @DisplayName("TC-UT-REPORT-001: Should generate sales report in xlsx format")
        void generateSales_shouldGenerateXlsx_success() {
            // Arrange
            LocalDate start = LocalDate.now().minusDays(7);
            LocalDate end = LocalDate.now();
            List<Order> orders = List.of(createOrder(1L, "ORD001", new BigDecimal("100"), new BigDecimal("5"), new BigDecimal("105")));
            
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(orders);

            // Act
            byte[] result = reportService.generate("sales", start, end, "xlsx");

            // Assert
            assertNotNull(result);
            assertTrue(result.length > 0);
            verify(orderMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-UT-REPORT-002: Should generate sales report in csv format")
        void generateSales_shouldGenerateCsv_success() {
            // Arrange
            LocalDate start = LocalDate.now().minusDays(7);
            LocalDate end = LocalDate.now();
            List<Order> orders = List.of(createOrder(1L, "ORD001", new BigDecimal("100"), new BigDecimal("5"), new BigDecimal("105")));
            
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(orders);

            // Act
            byte[] result = reportService.generate("sales", start, end, "csv");

            // Assert
            assertNotNull(result);
            assertTrue(result.length > 0);
            // CSV should have UTF-8 BOM
            assertEquals((byte) 0xEF, result[0]);
            assertEquals((byte) 0xBB, result[1]);
            assertEquals((byte) 0xBF, result[2]);
        }
    }

    @Nested
    @DisplayName("generate - inventory report")
    class GenerateInventoryTests {

        @Test
        @DisplayName("TC-UT-REPORT-003: Should generate inventory report")
        void generateInventory_shouldGenerate_success() {
            // Arrange
            LocalDate start = LocalDate.now().minusDays(7);
            LocalDate end = LocalDate.now();
            List<Product> products = List.of(createProduct(1L, "Apple", new BigDecimal("10"), new BigDecimal("15"), 100, 50));
            
            when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(products);

            // Act
            byte[] result = reportService.generate("inventory", start, end, "xlsx");

            // Assert
            assertNotNull(result);
            assertTrue(result.length > 0);
            verify(productMapper).selectList(any(LambdaQueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("generate - customer report")
    class GenerateCustomerTests {

        @Test
        @DisplayName("TC-UT-REPORT-004: Should generate customer report")
        void generateCustomer_shouldGenerate_success() {
            // Arrange
            LocalDate start = LocalDate.now().minusDays(7);
            LocalDate end = LocalDate.now();
            List<User> users = List.of(createUser(1L, "Test User", "13800138000"));
            
            when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(users);

            // Act
            byte[] result = reportService.generate("customer", start, end, "xlsx");

            // Assert
            assertNotNull(result);
            assertTrue(result.length > 0);
            verify(userMapper).selectList(any(LambdaQueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("generate - delivery report")
    class GenerateDeliveryTests {

        @Test
        @DisplayName("TC-UT-REPORT-005: Should generate delivery report")
        void generateDelivery_shouldGenerate_success() {
            // Arrange
            LocalDate start = LocalDate.now().minusDays(7);
            LocalDate end = LocalDate.now();
            List<Order> orders = List.of(createOrder(1L, "ORD001", new BigDecimal("100"), new BigDecimal("5"), new BigDecimal("105")));
            
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(orders);

            // Act
            byte[] result = reportService.generate("delivery", start, end, "xlsx");

            // Assert
            assertNotNull(result);
            assertTrue(result.length > 0);
        }
    }

    @Nested
    @DisplayName("generate - finance report")
    class GenerateFinanceTests {

        @Test
        @DisplayName("TC-UT-REPORT-006: Should generate finance report")
        void generateFinance_shouldGenerate_success() {
            // Arrange
            LocalDate start = LocalDate.now().minusDays(7);
            LocalDate end = LocalDate.now();
            List<Order> orders = List.of(createOrder(1L, "ORD001", new BigDecimal("100"), new BigDecimal("5"), new BigDecimal("105")));
            
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(orders);

            // Act
            byte[] result = reportService.generate("finance", start, end, "xlsx");

            // Assert
            assertNotNull(result);
            assertTrue(result.length > 0);
        }
    }

    @Nested
    @DisplayName("generate - unknown type")
    class GenerateUnknownTests {

        @Test
        @DisplayName("TC-UT-REPORT-007: Should handle unknown report type")
        void generateUnknown_shouldHandle_success() {
            // Arrange
            LocalDate start = LocalDate.now().minusDays(7);
            LocalDate end = LocalDate.now();

            // Act
            byte[] result = reportService.generate("unknown", start, end, "xlsx");

            // Assert
            assertNotNull(result);
            assertTrue(result.length > 0);
        }
    }

    @Nested
    @DisplayName("writeTo")
    class WriteToTests {

        @Test
        @DisplayName("TC-UT-REPORT-008: Should write bytes to output stream")
        void writeTo_shouldWrite_success() throws Exception {
            // Arrange
            byte[] bytes = "test data".getBytes();
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

            // Act
            reportService.writeTo(outputStream, bytes);

            // Assert
            assertEquals("test data", outputStream.toString());
        }
    }

    // Helper methods
    private Order createOrder(Long id, String orderNo, BigDecimal goodsAmount, BigDecimal couponAmount, BigDecimal payAmount) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setUserId(1L);
        order.setGoodsAmount(goodsAmount);
        order.setCouponAmount(couponAmount);
        order.setDeliveryFee(new BigDecimal("5"));
        order.setPayAmount(payAmount);
        order.setPayStatus(1);
        order.setStatus(2);
        order.setDeliveryType(1);
        order.setConsignee("Test User");
        order.setConsigneePhone("13800138000");
        order.setConsigneeAddress("Test Address");
        order.setPayMethod("wechat");
        order.setPayTradeNo("TXN001");
        order.setCreatedAt(LocalDateTime.now());
        order.setPayTime(LocalDateTime.now());
        return order;
    }

    private Product createProduct(Long id, String name, BigDecimal minPrice, BigDecimal maxPrice, Integer totalStock, Integer sales) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCategoryId(1L);
        product.setMinPrice(minPrice);
        product.setMaxPrice(maxPrice);
        product.setTotalStock(totalStock);
        product.setSales(sales);
        product.setStatus(1);
        return product;
    }

    private User createUser(Long id, String nickname, String phone) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setTag("VIP");
        user.setStatus(1);
        user.setRegisterTime(LocalDateTime.now());
        user.setLastLoginTime(LocalDateTime.now());
        return user;
    }
}
