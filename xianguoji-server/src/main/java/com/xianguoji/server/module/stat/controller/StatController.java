package com.xianguoji.server.module.stat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import com.xianguoji.server.module.order.mapper.RefundMapper;
import com.xianguoji.server.module.promo.entity.UserCoupon;
import com.xianguoji.server.module.promo.mapper.UserCouponMapper;
import com.xianguoji.server.module.review.mapper.ReviewMapper;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "工作台统计")
@RestController
@RequestMapping("/api/admin/stat")
@RequiredArgsConstructor
@AdminRequired
public class StatController {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ReviewMapper reviewMapper;
    private final RefundMapper refundMapper;

    @Operation(summary = "工作台四指标卡")
    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 今日订单数
        Long todayOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().ge(Order::getCreatedAt, todayStart));
        // 今日营收
        List<Order> paidOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().ge(Order::getCreatedAt, todayStart).eq(Order::getPayStatus, 1));
        BigDecimal todayRevenue = paidOrders.stream().map(Order::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        // 今日新客
        Long todayNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getRegisterTime, todayStart));
        // 待处理订单
        Long pendingOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1));

        return R.ok(Map.of("todayOrders", todayOrders, "todayRevenue", todayRevenue,
                "todayNewUsers", todayNewUsers, "pendingOrders", pendingOrders));
    }

    @Operation(summary = "近N天订单趋势")
    @GetMapping("/order-trend")
    public R<List<Map<String, Object>>> orderTrend(@RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Long count = orderMapper.selectCount(
                    new LambdaQueryWrapper<Order>()
                            .ge(Order::getCreatedAt, date.atStartOfDay())
                            .lt(Order::getCreatedAt, date.plusDays(1).atStartOfDay()));
            result.add(Map.of("date", date.toString(), "count", count));
        }
        return R.ok(result);
    }

    @Operation(summary = "今日订单状态分布")
    @GetMapping("/order-status")
    public R<Map<Integer, Long>> orderStatus() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Map<Integer, Long> map = new LinkedHashMap<>();
        for (int s = 0; s <= 8; s++) {
            Long count = orderMapper.selectCount(
                    new LambdaQueryWrapper<Order>().eq(Order::getStatus, s).ge(Order::getCreatedAt, todayStart));
            if (count > 0) map.put(s, count);
        }
        return R.ok(map);
    }

    @Operation(summary = "热销TopN")
    @GetMapping("/top-products")
    public R<List<Product>> topProducts(@RequestParam(defaultValue = "10") int limit) {
        List<Product> list = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .orderByDesc(Product::getSales)
                        .last("LIMIT " + limit));
        return R.ok(list);
    }

    @Operation(summary = "待办")
    @GetMapping("/todo")
    public R<Map<String, Long>> todo() {
        Long pendingAccept = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1));
        Long pendingReview = (long) 0; // TODO: 待回复评价数
        Long stockWarn = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)
                        .apply("total_stock <= stock_warn_threshold"));
        Long pendingRefund = refundMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xianguoji.server.module.order.entity.Refund>()
                        .eq(com.xianguoji.server.module.order.entity.Refund::getStatus, 0));

        return R.ok(Map.of("pendingAccept", pendingAccept, "pendingReview", pendingReview,
                "stockWarn", stockWarn, "pendingRefund", pendingRefund));
    }
}
