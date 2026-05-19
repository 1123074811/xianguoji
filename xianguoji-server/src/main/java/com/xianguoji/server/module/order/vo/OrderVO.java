package com.xianguoji.server.module.order.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.xianguoji.server.module.promo.vo.GroupBuyInstanceVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {

    private Long id;
    private String orderNo;
    private Integer status;
    private Integer payStatus;
    private Integer deliveryType;
    private String deliveryTime;
    private String consignee;
    private String consigneePhone;
    private String consigneeAddress;
    private String pickupCode;
    private BigDecimal goodsAmount;
    private BigDecimal couponAmount;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal payAmount;
    private String payMethod;
    private String payTradeNo;
    private String userRemark;
    private String cancelReason;
    private Long groupBuyInstanceId;
    private GroupBuyInstanceVO groupBuyInstance;
    private LocalDateTime payTime;
    private LocalDateTime deliveredAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemVO {
        private Long id;
        private Long productId;
        private Long skuId;
        private String productName;
        private String specName;
        private String image;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer quantity;
        private BigDecimal subtotal;
        private Integer isReviewed;
    }
}
