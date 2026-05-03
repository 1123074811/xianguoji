package com.xianguoji.server.module.promo.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CouponVO {

    private Long id;
    private String name;
    private Integer type;
    private BigDecimal amount;
    private BigDecimal minAmount;
    private Integer total;
    private Integer receivedCount;
    private Integer perUserLimit;
    private Integer validType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer validDays;
    private Integer scope;
    private Integer status;
}
