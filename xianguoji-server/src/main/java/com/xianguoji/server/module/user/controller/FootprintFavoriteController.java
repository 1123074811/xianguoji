package com.xianguoji.server.module.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.catalog.vo.ProductVO;
import com.xianguoji.server.module.user.entity.Favorite;
import com.xianguoji.server.module.user.entity.Footprint;
import com.xianguoji.server.module.user.mapper.FavoriteMapper;
import com.xianguoji.server.module.user.mapper.FootprintMapper;
import com.xianguoji.server.module.user.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "足迹/收藏")
@RestController
@RequestMapping("/api/u")
@RequiredArgsConstructor
public class FootprintFavoriteController {

    private final FootprintMapper footprintMapper;
    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    @Operation(summary = "分页足迹")
    @GetMapping("/footprint/page")
    @LoginRequired
    public R<PageVO<ProductVO>> footprintPage(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "20") Integer size) {
        Page<Footprint> p = footprintMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Footprint>()
                        .eq(Footprint::getUserId, LoginContext.uid())
                        .orderByDesc(Footprint::getViewedAt));
        var voList = p.getRecords().stream().map(fp -> {
            Product product = productMapper.selectById(fp.getProductId());
            return product != null ? toProductVO(product) : null;
        }).filter(java.util.Objects::nonNull).toList();
        return R.ok(new PageVO<>(p.getTotal(), voList, page, size));
    }

    @Operation(summary = "记录足迹")
    @PostMapping("/footprint/{productId}")
    @LoginRequired
    public R<Void> addFootprint(@PathVariable Long productId) {
        Long uid = LoginContext.uid();
        Footprint existing = footprintMapper.selectOne(
                new LambdaQueryWrapper<Footprint>()
                        .eq(Footprint::getUserId, uid)
                        .eq(Footprint::getProductId, productId));
        if (existing != null) {
            existing.setViewedAt(LocalDateTime.now());
            footprintMapper.updateById(existing);
        } else {
            Footprint fp = new Footprint();
            fp.setUserId(uid);
            fp.setProductId(productId);
            fp.setViewedAt(LocalDateTime.now());
            footprintMapper.insert(fp);
        }
        return R.ok();
    }

    @Operation(summary = "清空足迹")
    @DeleteMapping("/footprint")
    @LoginRequired
    public R<Void> clearFootprint() {
        footprintMapper.delete(new LambdaQueryWrapper<Footprint>().eq(Footprint::getUserId, LoginContext.uid()));
        return R.ok();
    }

    @Operation(summary = "分页收藏")
    @GetMapping("/favorite/page")
    @LoginRequired
    public R<PageVO<ProductVO>> favoritePage(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "20") Integer size) {
        Page<Favorite> p = favoriteMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, LoginContext.uid())
                        .orderByDesc(Favorite::getCreatedAt));
        var voList = p.getRecords().stream().map(f -> {
            Product product = productMapper.selectById(f.getProductId());
            return product != null ? toProductVO(product) : null;
        }).filter(java.util.Objects::nonNull).toList();
        return R.ok(new PageVO<>(p.getTotal(), voList, page, size));
    }

    @Operation(summary = "收藏")
    @PostMapping("/favorite/{productId}")
    @LoginRequired
    public R<Void> addFavorite(@PathVariable Long productId) {
        Long uid = LoginContext.uid();
        Favorite existing = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, uid).eq(Favorite::getProductId, productId));
        if (existing == null) {
            Favorite f = new Favorite();
            f.setUserId(uid);
            f.setProductId(productId);
            f.setCreatedAt(LocalDateTime.now());
            favoriteMapper.insert(f);
        }
        return R.ok();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/favorite/{productId}")
    @LoginRequired
    public R<Void> removeFavorite(@PathVariable Long productId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, LoginContext.uid())
                .eq(Favorite::getProductId, productId));
        return R.ok();
    }

    private ProductVO toProductVO(Product p) {
        return ProductVO.builder()
                .id(p.getId()).name(p.getName()).subtitle(p.getSubtitle())
                .categoryId(p.getCategoryId()).mainImage(p.getMainImage())
                .minPrice(p.getMinPrice()).maxPrice(p.getMaxPrice())
                .totalStock(p.getTotalStock()).sales(p.getSales())
                .isRecommend(p.getIsRecommend())
                .supportDelivery(p.getSupportDelivery()).supportPickup(p.getSupportPickup())
                .build();
    }
}
