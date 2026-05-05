package com.xianguoji.server.module.catalog.controller;

import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.cache.ProductUvService;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.catalog.dto.ProductQry;
import com.xianguoji.server.module.catalog.service.CatalogService;
import com.xianguoji.server.module.catalog.vo.*;
import com.xianguoji.server.module.user.entity.SearchHistory;
import com.xianguoji.server.module.user.mapper.SearchHistoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "分类/商品/搜索")
@RestController
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;
    private final SearchHistoryMapper searchHistoryMapper;
    private final ProductUvService productUvService;

    @Operation(summary = "分类树")
    @GetMapping("/api/pub/category/tree")
    public R<List<CategoryTreeVO>> categoryTree() {
        return R.ok(catalogService.getCategoryTree());
    }

    @Operation(summary = "Banner列表")
    @GetMapping("/api/pub/banner/list")
    public R<List<BannerVO>> bannerList() {
        return R.ok(catalogService.getBannerList());
    }

    @Operation(summary = "热门搜索")
    @GetMapping("/api/pub/hot-search/list")
    public R<List<String>> hotSearchList() {
        return R.ok(catalogService.getHotSearchList());
    }

    @Operation(summary = "商品分页")
    @GetMapping("/api/pub/product/page")
    public R<PageVO<ProductVO>> productPage(ProductQry qry) {
        return R.ok(catalogService.getProductPage(qry));
    }

    @Operation(summary = "店主推荐")
    @GetMapping("/api/pub/product/recommend")
    public R<List<ProductVO>> productRecommend() {
        return R.ok(catalogService.getRecommendProducts());
    }

    @Operation(summary = "商品详情")
    @GetMapping("/api/pub/product/{id}")
    public R<ProductDetailVO> productDetail(@PathVariable Long id) {
        // P1-7: UV 必须放在缓存外，否则缓存命中后无法记录
        try {
            Long uid = LoginContext.uid();
            if (uid != null) productUvService.recordUv(id, uid);
        } catch (Exception ignored) {}
        return R.ok(catalogService.getProductDetail(id));
    }

    @Operation(summary = "我的搜索历史")
    @GetMapping("/api/u/search/history")
    @LoginRequired
    public R<List<String>> searchHistory() {
        List<SearchHistory> list = searchHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SearchHistory>()
                        .eq(SearchHistory::getUserId, LoginContext.uid())
                        .orderByDesc(SearchHistory::getCreatedAt)
                        .last("LIMIT 20"));
        return R.ok(list.stream().map(SearchHistory::getKeyword).toList());
    }

    @Operation(summary = "清空搜索历史")
    @DeleteMapping("/api/u/search/history")
    @LoginRequired
    public R<Void> clearSearchHistory() {
        searchHistoryMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SearchHistory>()
                        .eq(SearchHistory::getUserId, LoginContext.uid()));
        return R.ok();
    }

    @Operation(summary = "记录搜索词")
    @PostMapping("/api/u/search/record")
    @LoginRequired
    public R<Void> recordSearch(@RequestBody java.util.Map<String, String> body) {
        String keyword = body.get("keyword");
        if (keyword != null && !keyword.isBlank()) {
            SearchHistory sh = new SearchHistory();
            sh.setUserId(LoginContext.uid());
            sh.setKeyword(keyword);
            sh.setCreatedAt(LocalDateTime.now());
            searchHistoryMapper.insert(sh);
        }
        return R.ok();
    }
}
