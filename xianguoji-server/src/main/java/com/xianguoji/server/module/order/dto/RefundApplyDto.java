package com.xianguoji.server.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundApplyDto {

    @NotNull(message = "订单号不能为空")
    private String orderNo;
    @NotNull(message = "退款类型不能为空")
    private Integer type; // 1仅退款 2退货退款
    private String reason;
    private java.util.List<String> images;
}
