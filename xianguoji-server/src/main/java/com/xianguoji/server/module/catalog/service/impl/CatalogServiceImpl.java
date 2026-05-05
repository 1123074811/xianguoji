package com.xianguoji.server.module.catalog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.catalog.dto.ProductQry;
import com.xianguoji.server.module.catalog.entity.*;
import com.xianguoji.server.module.catalog.mapper.*;
import com.xianguoji.server.module.catalog.service.CatalogService;
import com.xianguoji.server.module.catalog.vo.*;
import com.xianguoji.server.module.review.entity.Review;
import com.xianguoji.server.module.review.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final CategoryMapper categoryMapper;
    private final BannerMapper bannerMapper;
    private final HotSearchMapper hotSearchMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductImageMapper productImageMapper;
    private final ReviewMapper reviewMapper;

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        List<Category> all = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1).orderByAsc(Category::getSort));
        Map<Long, List<Category>> childrenMap = all.stream()
                .filter(c -> c.getParentId() != 0)
                .collect(Collectors.groupingBy(Category::getParentId));

        return all.stream()
                .filter(c -> c.getParentId() == 0)
                .map(c -> CategoryTreeVO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .icon(c.getIcon())
                        .sort(c.getSort())
                        .children(childrenMap.getOrDefault(c.getId(), List.of()).stream()
                                .map(child -> CategoryTreeVO.builder()
                                        .id(child.getId())
                                        .name(child.getName())
                                        .icon(child.getIcon())
                                        .sort(child.getSort())
                                        .children(null)
                                        .build())
                                .toList())
                        .build())
                .toList();
    }

    @Override
    public List<BannerVO> getBannerList() {
        List<Banner> list = bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .orderByAsc(Banner::getSort));
        return list.stream().map(b -> BannerVO.builder()
                .id(b.getId())
                .title(b.getTitle())
                .image(b.getImage())
                .linkType(b.getLinkType())
                .linkValue(b.getLinkValue())
                .build()).toList();
    }

    @Override
    public List<String> getHotSearchList() {
        List<HotSearch> list = hotSearchMapper.selectList(
                new LambdaQueryWrapper<HotSearch>()
                        .eq(HotSearch::getStatus, 1)
                        .orderByAsc(HotSearch::getSort));
        return list.stream().map(HotSearch::getKeyword).toList();
    }

    @Override
    public PageVO<ProductVO> getProductPage(ProductQry qry) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1);

        if (qry.getCategoryId() != null) {
            Category category = categoryMapper.selectById(qry.getCategoryId());
            if (category != null && category.getParentId() == 0) {
                List<Long> childIds = categoryMapper.selectList(
                                new LambdaQueryWrapper<Category>()
                                        .eq(Category::getParentId, qry.getCategoryId())
                                        .eq(Category::getStatus, 1))
                        .stream()
                        .map(Category::getId)
                        .toList();
                if (childIds.isEmpty()) {
                    wrapper.eq(Product::getCategoryId, qry.getCategoryId());
                } else {
                    // 同时包含挂在父分类本身的商品 + 所有子分类下的商品
                    java.util.List<Long> allIds = new java.util.ArrayList<>(childIds);
                    allIds.add(qry.getCategoryId());
                    wrapper.in(Product::getCategoryId, allIds);
                }
            } else {
                wrapper.eq(Product::getCategoryId, qry.getCategoryId());
            }
        }
        if (qry.getKeyword() != null && !qry.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(Product::getName, qry.getKeyword())
                    .or().like(Product::getSubtitle, qry.getKeyword()));
        }

        String sort = qry.getSort();
        if ("sales".equals(sort)) {
            wrapper.orderByDesc(Product::getSales);
        } else if ("priceAsc".equals(sort) || "price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getMinPrice);
        } else if ("priceDesc".equals(sort) || "price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getMinPrice);
        } else {
            wrapper.orderByDesc(Product::getIsRecommend).orderByDesc(Product::getSales);
        }

        Page<Product> page = productMapper.selectPage(new Page<>(qry.getPage(), qry.getSize()), wrapper);
        List<ProductVO> voList = page.getRecords().stream().map(this::toProductVO).toList();
        return new PageVO<>(page.getTotal(), voList, qry.getPage(), qry.getSize());
    }

    @Override
    public List<ProductVO> getRecommendProducts() {
        List<Product> list = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .eq(Product::getIsRecommend, 1)
                        .orderByDesc(Product::getCreatedAt)
                        .orderByDesc(Product::getSales));
        return list.stream().map(this::toProductVO).toList();
    }

    @Override
    public ProductDetailVO getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) throw new BizException(ResultCode.NOT_FOUND, "商品不存在");

        List<ProductSku> skuList = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, id)
                        .eq(ProductSku::getStatus, 1)
                        .orderByAsc(ProductSku::getSort));

        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, id)
                        .orderByAsc(ProductImage::getSort));

        List<String> carouselImages = images.stream()
                .filter(i -> i.getType() == 1).map(ProductImage::getUrl).toList();
        List<String> detailImages = images.stream()
                .filter(i -> i.getType() == 2).map(ProductImage::getUrl).toList();

        // 评价摘要
        Long reviewCount = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>().eq(Review::getProductId, id).eq(Review::getIsHidden, 0));
        Long withImageCount = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>().eq(Review::getProductId, id).eq(Review::getIsHidden, 0)
                        .isNotNull(Review::getImages).apply("JSON_LENGTH(images) > 0"));

        ProductDetailVO.ReviewSummaryVO reviewSummary = ProductDetailVO.ReviewSummaryVO.builder()
                .totalCount(reviewCount)
                .avgRating(null) // TODO: 需要聚合查询
                .withImageCount(withImageCount)
                .build();

        return ProductDetailVO.builder()
                .id(product.getId())
                .name(product.getName())
                .subtitle(product.getSubtitle())
                .categoryId(product.getCategoryId())
                .mainImage(product.getMainImage())
                .videoUrl(product.getVideoUrl())
                .description(product.getDescription())
                .minPrice(product.getMinPrice())
                .maxPrice(product.getMaxPrice())
                .totalStock(product.getTotalStock())
                .sales(product.getSales())
                .isRecommend(product.getIsRecommend())
                .supportDelivery(product.getSupportDelivery())
                .supportPickup(product.getSupportPickup())
                .skuList(skuList.stream().map(s -> ProductDetailVO.SkuVO.builder()
                        .id(s.getId())
                        .specName(s.getSpecName())
                        .price(s.getPrice())
                        .originalPrice(s.getOriginalPrice())
                        .stock(s.getStock())
                        .isDefault(s.getIsDefault())
                        .build()).toList())
                .carouselImages(carouselImages)
                .detailImages(detailImages)
                .reviewSummary(reviewSummary)
                .build();
    }

    private ProductVO toProductVO(Product p) {
        return ProductVO.builder()
                .id(p.getId())
                .name(p.getName())
                .subtitle(p.getSubtitle())
                .categoryId(p.getCategoryId())
                .mainImage(p.getMainImage())
                .minPrice(p.getMinPrice())
                .maxPrice(p.getMaxPrice())
                .totalStock(p.getTotalStock())
                .sales(p.getSales())
                .isRecommend(p.getIsRecommend())
                .supportDelivery(p.getSupportDelivery())
                .supportPickup(p.getSupportPickup())
                .build();
    }
}
