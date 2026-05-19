package com.xianguoji.server.module.order.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundVO {

    private Long id;
    private String refundNo;
    private String orderNo;
    private Integer type;
    private BigDecimal amount;
    private String reason;
    private java.util.List<String> images;
    private Integer status;
    private String refundChannel;
    private String refundTransactionId;
    private String rejectReason;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
