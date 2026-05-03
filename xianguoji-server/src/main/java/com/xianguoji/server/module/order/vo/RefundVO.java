package com.xianguoji.server.module.order.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RefundVO {

    private Long id;
    private String refundNo;
    private String orderNo;
    private Integer type;
    private BigDecimal amount;
    private String reason;
    private java.util.List<String> images;
    private Integer status;
    private String rejectReason;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
