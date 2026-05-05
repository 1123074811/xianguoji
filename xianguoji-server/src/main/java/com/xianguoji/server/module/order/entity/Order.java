package com.xianguoji.server.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Integer status;
    private Integer payStatus;
    private Integer deliveryType;
    private String deliveryTime;
    private Long addressId;
    private String consignee;
    private String consigneePhone;
    private String consigneeAddress;
    private Long pickupPointId;
    private String pickupCode;
    private BigDecimal goodsAmount;
    private BigDecimal couponAmount;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal payAmount;
    private Long userCouponId;
    private String payMethod;
    private LocalDateTime payTime;
    private String payTradeNo;
    private String userRemark;
    private String cancelReason;
    private Long groupBuyInstanceId;
    private String courierName;
    private String courierPhone;
    private LocalDateTime deliveredAt;
    private LocalDateTime finishedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
}
