package com.xianguoji.server.module.catalog.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductDetailVO {

    private Long id;
    private String name;
    private String subtitle;
    private Long categoryId;
    private String mainImage;
    private String videoUrl;
    private String description;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer totalStock;
    private Integer sales;
    private Integer isRecommend;
    private Integer supportDelivery;
    private Integer supportPickup;
    private List<SkuVO> skuList;
    private List<String> carouselImages;
    private List<String> detailImages;
    private ReviewSummaryVO reviewSummary;

    @Data
    @Builder
    public static class SkuVO {
        private Long id;
        private String specName;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer stock;
        private Integer isDefault;
    }

    @Data
    @Builder
    public static class ReviewSummaryVO {
        private long totalCount;
        private Double avgRating;
        private long withImageCount;
    }
}
