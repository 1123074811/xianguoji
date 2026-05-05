package com.xianguoji.server.module.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.excel.ExcelExportTemplate;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "订单导出")
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
@AdminRequired
public class OrderExportController {

    private final OrderMapper orderMapper;
    private final ExcelExportTemplate excelExportTemplate;

    @Operation(summary = "导出Excel")
    @GetMapping("/export")
    public void export(@RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       HttpServletResponse response) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Order::getStatus, status);
        if (startDate != null) wrapper.ge(Order::getCreatedAt, startDate + " 00:00:00");
        if (endDate != null) wrapper.le(Order::getCreatedAt, endDate + " 23:59:59");
        wrapper.orderByDesc(Order::getCreatedAt);

        List<Order> orders = orderMapper.selectList(wrapper);
        List<OrderExportRow> rows = new ArrayList<>();
        for (Order o : orders) {
            OrderExportRow row = new OrderExportRow();
            row.setOrderNo(o.getOrderNo());
            row.setStatus(o.getStatus());
            row.setPayStatus(o.getPayStatus());
            row.setDeliveryType(o.getDeliveryType());
            row.setConsignee(o.getConsignee());
            row.setConsigneePhone(o.getConsigneePhone());
            row.setGoodsAmount(o.getGoodsAmount());
            row.setPayAmount(o.getPayAmount());
            row.setCreatedAt(o.getCreatedAt());
            rows.add(row);
        }

        excelExportTemplate.export(response, "orders", "订单", OrderExportRow.class, rows);
    }

    @Data
    public static class OrderExportRow {
        @com.alibaba.excel.annotation.ExcelProperty("订单号")
        private String orderNo;
        @com.alibaba.excel.annotation.ExcelProperty("状态")
        private Integer status;
        @com.alibaba.excel.annotation.ExcelProperty("支付状态")
        private Integer payStatus;
        @com.alibaba.excel.annotation.ExcelProperty("配送方式")
        private Integer deliveryType;
        @com.alibaba.excel.annotation.ExcelProperty("收货人")
        private String consignee;
        @com.alibaba.excel.annotation.ExcelProperty("手机号")
        private String consigneePhone;
        @com.alibaba.excel.annotation.ExcelProperty("商品金额")
        private BigDecimal goodsAmount;
        @com.alibaba.excel.annotation.ExcelProperty("实付金额")
        private BigDecimal payAmount;
        @com.alibaba.excel.annotation.ExcelProperty("下单时间")
        private LocalDateTime createdAt;
    }
}
