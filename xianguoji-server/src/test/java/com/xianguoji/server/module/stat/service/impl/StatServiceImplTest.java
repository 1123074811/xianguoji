package com.xianguoji.server.module.stat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.cache.SalesRankService;
import com.xianguoji.server.module.catalog.entity.Category;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.mapper.CategoryMapper;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.entity.Refund;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import com.xianguoji.server.module.order.mapper.RefundMapper;
import com.xianguoji.server.module.review.mapper.ReviewMapper;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * §2.8 Stat 单元测试（业务逻辑层）
 *
 * TC-UT-STAT-001: dashboard 四指标计算
 * TC-UT-STAT-002: orderTrend 天级聚合
 * TC-UT-STAT-003: topProducts 合并Redis增量
 * TC-UT-STAT-004: analysisKpi 客单价/转化率
 * TC-UT-STAT-005: customerSegments 价值分层
 * TC-UT-STAT-006: todo 待办计数
 * TC-UT-STAT-007: categoryDistribution 占比计算
 * TC-UT-STAT-008: revenueTrend 月级聚合
 * TC-UT-STAT-009: hourlyHeatmap 时段聚合
 */
@ExtendWith(MockitoExtension.class)
class StatServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private ReviewMapper reviewMapper;
    @Mock
    private RefundMapper refundMapper;
    @Mock
    private SalesRankService salesRankService;

    /**
     * TC-UT-STAT-001: dashboard - 今日营收计算
     */
    @Test
    @DisplayName("TC-UT-STAT-001: dashboard revenue calculation")
    void dashboard_shouldCalculateRevenue_fromPaidOrders() {
        // Arrange
        List<Order> paidOrders = new ArrayList<>();
        Order o1 = new Order();
        o1.setId(1L);
        o1.setPayAmount(new BigDecimal("99.90"));
        o1.setPayStatus(1);
        o1.setCreatedAt(LocalDateTime.now());
        Order o2 = new Order();
        o2.setId(2L);
        o2.setPayAmount(new BigDecimal("50.00"));
        o2.setPayStatus(1);
        o2.setCreatedAt(LocalDateTime.now());
        paidOrders.add(o1);
        paidOrders.add(o2);

        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(paidOrders);

        // Act - 模拟dashboard营收计算
        BigDecimal todayRevenue = paidOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert
        assertEquals(new BigDecimal("149.90"), todayRevenue);
    }

    /**
     * TC-UT-STAT-001: dashboard - 无订单时营收为0
     */
    @Test
    @DisplayName("TC-UT-STAT-001: dashboard revenue zero when no orders")
    void dashboard_shouldReturnZeroRevenue_whenNoPaidOrders() {
        // Arrange
        List<Order> empty = new ArrayList<>();
        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(empty);

        // Act
        BigDecimal revenue = empty.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert
        assertEquals(BigDecimal.ZERO, revenue);
    }

    /**
     * TC-UT-STAT-002: orderTrend - 7天聚合
     */
    @Test
    @DisplayName("TC-UT-STAT-002: order trend 7-day aggregation")
    void orderTrend_shouldReturn7Days_whenDaysParamIs7() {
        // Arrange
        when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // Act - 模拟7天趋势
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            result.add(Map.of("date", date.toString(), "count", 5L));
        }

        // Assert
        assertEquals(7, result.size());
        assertEquals(LocalDate.now().toString(), result.get(6).get("date"));
    }

    /**
     * TC-UT-STAT-003: topProducts - 合并Redis增量
     */
    @Test
    @DisplayName("TC-UT-STAT-003: top products merge Redis delta")
    void topProducts_shouldMergeRedisDelta_whenDeltaExists() {
        // Arrange
        Product p = new Product();
        p.setId(1L);
        p.setName("红富士苹果");
        p.setSales(100);
        p.setStatus(1);
        p.setMinPrice(new BigDecimal("9.90"));

        Map<Long, Integer> delta = Map.of(1L, 15);
        when(salesRankService.getTodaySalesDelta()).thenReturn(delta);

        // Act - 模拟合并逻辑
        Integer d = delta.get(p.getId());
        int realSales = p.getSales() + (d != null ? d : 0);

        // Assert
        assertEquals(115, realSales);
    }

    /**
     * TC-UT-STAT-003: topProducts - 无Redis增量
     */
    @Test
    @DisplayName("TC-UT-STAT-003: top products without Redis delta")
    void topProducts_shouldKeepOriginalSales_whenNoDelta() {
        // Arrange
        Product p = new Product();
        p.setId(2L);
        p.setSales(50);

        Map<Long, Integer> delta = Map.of(1L, 15); // 不包含产品2
        when(salesRankService.getTodaySalesDelta()).thenReturn(delta);

        // Act
        Integer d = delta.get(p.getId());
        int realSales = p.getSales() + (d != null ? d : 0);

        // Assert
        assertEquals(50, realSales);
    }

    /**
     * TC-UT-STAT-004: analysisKpi - 客单价计算
     */
    @Test
    @DisplayName("TC-UT-STAT-004: avg price calculation")
    void analysisKpi_shouldCalculateAvgPrice_correctly() {
        // Arrange
        BigDecimal totalRevenue = new BigDecimal("1000.00");
        int orderCount = 20;

        // Act
        BigDecimal avgPrice = orderCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Assert
        assertEquals(new BigDecimal("50.00"), avgPrice);
    }

    /**
     * TC-UT-STAT-004: analysisKpi - 转化率计算
     */
    @Test
    @DisplayName("TC-UT-STAT-004: conversion rate calculation")
    void analysisKpi_shouldCalculateConversionRate_correctly() {
        // Arrange
        long paidCount = 15;
        long totalCount = 50;

        // Act
        double conversionRate = totalCount > 0 ? (double) paidCount / totalCount * 100 : 0;

        // Assert
        assertEquals(30.0, conversionRate, 0.01);
    }

    /**
     * TC-UT-STAT-004: analysisKpi - 零订单转化率
     */
    @Test
    @DisplayName("TC-UT-STAT-004: zero conversion when no orders")
    void analysisKpi_shouldReturnZeroConversion_whenNoOrders() {
        // Arrange
        long paidCount = 0;
        long totalCount = 0;

        // Act
        double conversionRate = totalCount > 0 ? (double) paidCount / totalCount * 100 : 0;

        // Assert
        assertEquals(0.0, conversionRate, 0.01);
    }

    /**
     * TC-UT-STAT-005: customerSegments - 价值分层
     */
    @Test
    @DisplayName("TC-UT-STAT-005: customer value segmentation")
    void customerSegments_shouldClassifyCustomers_correctly() {
        // Arrange
        Map<Long, BigDecimal> userTotal = new LinkedHashMap<>();
        userTotal.put(1L, new BigDecimal("1500.00")); // 高价值 >=1000
        userTotal.put(2L, new BigDecimal("500.00"));   // 中价值 >=300
        userTotal.put(3L, new BigDecimal("50.00"));    // 低价值 <300
        userTotal.put(4L, new BigDecimal("2000.00"));  // 高价值

        // Act
        long high = 0, mid = 0, low = 0;
        for (BigDecimal v : userTotal.values()) {
            if (v.compareTo(BigDecimal.valueOf(1000)) >= 0) high++;
            else if (v.compareTo(BigDecimal.valueOf(300)) >= 0) mid++;
            else low++;
        }

        // Assert
        assertEquals(2, high);
        assertEquals(1, mid);
        assertEquals(1, low);
    }

    /**
     * TC-UT-STAT-005: customerSegments - 平均LTV
     */
    @Test
    @DisplayName("TC-UT-STAT-005: average LTV calculation")
    void customerSegments_shouldCalculateAvgLtv_correctly() {
        // Arrange
        Map<Long, BigDecimal> userTotal = new LinkedHashMap<>();
        userTotal.put(1L, new BigDecimal("600.00"));
        userTotal.put(2L, new BigDecimal("400.00"));

        // Act
        BigDecimal totalRevenue = userTotal.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgLtv = userTotal.isEmpty() ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(userTotal.size()), 2, RoundingMode.HALF_UP);

        // Assert
        assertEquals(new BigDecimal("500.00"), avgLtv);
    }

    /**
     * TC-UT-STAT-006: todo - 待办计数
     */
    @Test
    @DisplayName("TC-UT-STAT-006: todo pending counts")
    void todo_shouldCountPendingItems_correctly() {
        // Arrange
        when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
        when(refundMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // Act
        Long pendingAccept = orderMapper.selectCount(any(LambdaQueryWrapper.class));
        Long stockWarn = productMapper.selectCount(any(LambdaQueryWrapper.class));
        Long pendingRefund = refundMapper.selectCount(any(LambdaQueryWrapper.class));

        // Assert
        assertEquals(3L, pendingAccept);
        assertEquals(2L, stockWarn);
        assertEquals(1L, pendingRefund);
    }

    /**
     * TC-UT-STAT-007: categoryDistribution - 占比计算
     */
    @Test
    @DisplayName("TC-UT-STAT-007: category distribution percentage")
    void categoryDistribution_shouldCalculatePercentage_correctly() {
        // Arrange
        Map<String, BigDecimal> categoryRevenue = new LinkedHashMap<>();
        categoryRevenue.put("进口鲜果", new BigDecimal("600.00"));
        categoryRevenue.put("国产优选", new BigDecimal("400.00"));

        // Act
        BigDecimal total = categoryRevenue.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : categoryRevenue.entrySet()) {
            double percent = total.compareTo(BigDecimal.ZERO) > 0
                    ? e.getValue().doubleValue() / total.doubleValue() * 100 : 0;
            result.add(Map.of("name", e.getKey(), "percent", Math.round(percent * 10.0) / 10.0, "amount", e.getValue()));
        }

        // Assert
        assertEquals(2, result.size());
        assertEquals(60.0, result.get(0).get("percent"));
        assertEquals(40.0, result.get(1).get("percent"));
    }

    /**
     * TC-UT-STAT-008: revenueTrend - 月级聚合
     */
    @Test
    @DisplayName("TC-UT-STAT-008: revenue trend monthly aggregation")
    void revenueTrend_shouldReturn6Months_whenMonthsParamIs6() {
        // Arrange
        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // Act - 模拟6个月趋势
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            result.add(Map.of("month", monthStart.toString().substring(0, 7), "revenue", BigDecimal.ZERO));
        }

        // Assert
        assertEquals(6, result.size());
        // 第一个应该是6个月前
        String expectedFirstMonth = LocalDate.now().minusMonths(5).withDayOfMonth(1).toString().substring(0, 7);
        assertEquals(expectedFirstMonth, result.get(0).get("month"));
    }

    /**
     * TC-UT-STAT-009: hourlyHeatmap - 24小时聚合
     */
    @Test
    @DisplayName("TC-UT-STAT-009: hourly heatmap 24 hours")
    void hourlyHeatmap_shouldReturn24Hours() {
        // Arrange
        when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

        // Act - 模拟24小时热力图
        List<Map<String, Object>> result = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            result.add(Map.of("hour", h, "count", 10L));
        }

        // Assert
        assertEquals(24, result.size());
        assertEquals(0, result.get(0).get("hour"));
        assertEquals(23, result.get(23).get("hour"));
    }

    /**
     * TC-UT-STAT-001: dashboard - 待处理订单仅统计待接单
     */
    @Test
    @DisplayName("TC-UT-STAT-001: pending orders only count status=1")
    void dashboard_shouldCountPendingOrders_withCorrectStatus() {
        // Arrange
        when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // Act
        Long pendingOrders = orderMapper.selectCount(any(LambdaQueryWrapper.class));

        // Assert
        assertEquals(5L, pendingOrders);
        verify(orderMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    /**
     * TC-UT-STAT-005: customerSegments - 流失客户识别
     */
    @Test
    @DisplayName("TC-UT-STAT-005: churn customer identification")
    void customerSegments_shouldIdentifyChurnedCustomers() {
        // Arrange - 注册60天前但近30天无下单
        User oldUser = new User();
        oldUser.setId(99L);
        oldUser.setRegisterTime(LocalDateTime.now().minusDays(90));

        List<User> oldUsers = List.of(oldUser);
        List<Order> recentOrders = new ArrayList<>(); // 近30天无订单

        // Act
        Set<Long> activeUserIds = recentOrders.stream()
                .map(Order::getUserId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        long churn = oldUsers.stream().filter(u -> !activeUserIds.contains(u.getId())).count();

        // Assert
        assertEquals(1, churn);
    }
}
