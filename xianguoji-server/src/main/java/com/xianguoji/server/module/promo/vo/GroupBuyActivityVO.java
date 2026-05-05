package com.xianguoji.server.module.promo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyActivityVO {

    private Long id;
    private Long productId;
    private Long skuId;
    private String productName;
    private String mainImage;
    private BigDecimal groupPrice;
    private BigDecimal originalPrice;
    private Integer groupSize;
    private Integer validHours;
    private LocalDateTime endTime;
    private Integer totalJoinCount;
    private Integer successCount;
    private Integer status;
}
