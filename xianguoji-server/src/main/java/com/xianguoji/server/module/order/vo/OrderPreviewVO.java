package com.xianguoji.server.module.order.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPreviewVO {

    private List<OrderItemPreview> items;
    private BigDecimal goodsAmount;
    private BigDecimal discountAmount;
    private BigDecimal couponAmount;
    private BigDecimal deliveryFee;
    private BigDecimal payAmount;
    private String promotionTip;
    private AddressVO address;
    private PickupPointVO pickupPoint;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemPreview {
        private Long skuId;
        private String productName;
        private String specName;
        private String image;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer quantity;
        private BigDecimal subtotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressVO {
        private Long id;
        private String consignee;
        private String phone;
        private String fullAddress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PickupPointVO {
        private Long id;
        private String name;
        private String address;
    }
}
