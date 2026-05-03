package com.xianguoji.server.module.catalog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.catalog.entity.Banner;
import com.xianguoji.server.module.catalog.entity.Category;
import com.xianguoji.server.module.catalog.entity.HotSearch;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.entity.ProductImage;
import com.xianguoji.server.module.catalog.entity.ProductSku;
import com.xianguoji.server.module.catalog.mapper.BannerMapper;
import com.xianguoji.server.module.catalog.mapper.CategoryMapper;
import com.xianguoji.server.module.catalog.mapper.HotSearchMapper;
import com.xianguoji.server.module.catalog.mapper.ProductImageMapper;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.catalog.mapper.ProductSkuMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "分类/商品管理-商家端")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductImageMapper productImageMapper;
    private final BannerMapper bannerMapper;
    private final HotSearchMapper hotSearchMapper;

    // ===== 分类 =====
    @Operation(summary = "分类列表")
    @GetMapping("/category/list")
    @AdminRequired
    public R<List<Category>> categoryList() {
        return R.ok(categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort)));
    }

    @Operation(summary = "新增分类")
    @PostMapping("/category")
    @AdminRequired
    public R<Void> addCategory(@RequestBody Category dto) {
        categoryMapper.insert(dto);
        return R.ok();
    }

    @Operation(summary = "更新分类")
    @PutMapping("/category/{id}")
    @AdminRequired
    public R<Void> updateCategory(@PathVariable Long id, @RequestBody Category dto) {
        dto.setId(id);
        categoryMapper.updateById(dto);
        return R.ok();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/category/{id}")
    @AdminRequired
    public R<Void> deleteCategory(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return R.ok();
    }

    // ===== 商品 =====
    @Operation(summary = "商品分页(商家端)")
    @GetMapping("/product/page")
    @AdminRequired
    public R<PageVO<Product>> productPage(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "20") Integer size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Product::getStatus, status);
        wrapper.orderByDesc(Product::getCreatedAt);
        var p = productMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), wrapper);
        return R.ok(new PageVO<>(p.getTotal(), p.getRecords(), page, size));
    }

    @Operation(summary = "新增商品")
    @PostMapping("/product")
    @AdminRequired
    @Transactional(rollbackFor = Exception.class)
    public R<Void> addProduct(@RequestBody Map<String, Object> body) {
        Product product = new Product();
        product.setName((String) body.get("name"));
        product.setSubtitle((String) body.get("subtitle"));
        product.setCategoryId(Long.valueOf(body.get("categoryId").toString()));
        product.setMainImage((String) body.get("mainImage"));
        product.setVideoUrl((String) body.get("videoUrl"));
        product.setDescription((String) body.get("description"));
        product.setIsRecommend(body.get("isRecommend") != null ? (Integer) body.get("isRecommend") : 0);
        product.setSupportDelivery(body.get("supportDelivery") != null ? (Integer) body.get("supportDelivery") : 1);
        product.setSupportPickup(body.get("supportPickup") != null ? (Integer) body.get("supportPickup") : 1);
        product.setStatus(1);
        product.setSales(0);
        product.setSort(0);
        productMapper.insert(product);

        // SKU
        List<Map<String, Object>> skus = (List<Map<String, Object>>) body.get("skus");
        if (skus != null) {
            for (Map<String, Object> s : skus) {
                ProductSku sku = new ProductSku();
                sku.setProductId(product.getId());
                sku.setSpecName((String) s.get("specName"));
                sku.setSkuCode((String) s.get("skuCode"));
                sku.setPrice(new BigDecimal(s.get("price").toString()));
                sku.setOriginalPrice(s.get("originalPrice") != null ? new BigDecimal(s.get("originalPrice").toString()) : null);
                sku.setCostPrice(s.get("costPrice") != null ? new BigDecimal(s.get("costPrice").toString()) : null);
                sku.setStock(s.get("stock") != null ? (Integer) s.get("stock") : 0);
                sku.setIsDefault(s.get("isDefault") != null ? (Integer) s.get("isDefault") : 0);
                sku.setStatus(1);
                skuMapper.insert(sku);
            }
        }

        // 图片
        List<Map<String, Object>> images = (List<Map<String, Object>>) body.get("images");
        if (images != null) {
            int sort = 0;
            for (Map<String, Object> img : images) {
                ProductImage pi = new ProductImage();
                pi.setProductId(product.getId());
                pi.setUrl((String) img.get("url"));
                pi.setType(img.get("type") != null ? (Integer) img.get("type") : 1);
                pi.setSort(sort++);
                productImageMapper.insert(pi);
            }
        }

        // 同步冗余字段
        syncProductFields(product.getId());
        return R.ok();
    }

    @Operation(summary = "编辑商品")
    @PutMapping("/product/{id}")
    @AdminRequired
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Product product = productMapper.selectById(id);
        if (product == null) throw new BizException(ResultCode.NOT_FOUND);
        if (body.get("name") != null) product.setName((String) body.get("name"));
        if (body.get("subtitle") != null) product.setSubtitle((String) body.get("subtitle"));
        if (body.get("categoryId") != null) product.setCategoryId(Long.valueOf(body.get("categoryId").toString()));
        if (body.get("mainImage") != null) product.setMainImage((String) body.get("mainImage"));
        if (body.get("description") != null) product.setDescription((String) body.get("description"));
        if (body.get("isRecommend") != null) product.setIsRecommend((Integer) body.get("isRecommend"));
        productMapper.updateById(product);

        // 更新 SKU: 先删后增
        if (body.containsKey("skus")) {
            skuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));
            List<Map<String, Object>> skus = (List<Map<String, Object>>) body.get("skus");
            if (skus != null) {
                for (Map<String, Object> s : skus) {
                    ProductSku sku = new ProductSku();
                    sku.setProductId(id);
                    sku.setSpecName((String) s.get("specName"));
                    sku.setSkuCode((String) s.get("skuCode"));
                    sku.setPrice(new BigDecimal(s.get("price").toString()));
                    sku.setOriginalPrice(s.get("originalPrice") != null ? new BigDecimal(s.get("originalPrice").toString()) : null);
                    sku.setCostPrice(s.get("costPrice") != null ? new BigDecimal(s.get("costPrice").toString()) : null);
                    sku.setStock(s.get("stock") != null ? (Integer) s.get("stock") : 0);
                    sku.setIsDefault(s.get("isDefault") != null ? (Integer) s.get("isDefault") : 0);
                    sku.setStatus(1);
                    skuMapper.insert(sku);
                }
            }
            syncProductFields(id);
        }
        return R.ok();
    }

    @Operation(summary = "上下架")
    @PutMapping("/product/{id}/status")
    @AdminRequired
    public R<Void> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Product p = new Product();
        p.setId(id);
        p.setStatus(body.get("status"));
        productMapper.updateById(p);
        return R.ok();
    }

    @Operation(summary = "复制商品")
    @PostMapping("/product/{id}/copy")
    @AdminRequired
    @Transactional(rollbackFor = Exception.class)
    public R<Void> copyProduct(@PathVariable Long id) {
        Product src = productMapper.selectById(id);
        if (src == null) throw new BizException(ResultCode.NOT_FOUND);
        src.setId(null);
        src.setName(src.getName() + "(副本)");
        src.setStatus(0);
        src.setSales(0);
        productMapper.insert(src);

        List<ProductSku> skus = skuMapper.selectList(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));
        for (ProductSku s : skus) {
            s.setId(null);
            s.setProductId(src.getId());
            s.setSales(0);
            skuMapper.insert(s);
        }
        return R.ok();
    }

    // ===== Banner =====
    @Operation(summary = "Banner列表")
    @GetMapping("/banner/list")
    @AdminRequired
    public R<List<Banner>> bannerList() {
        return R.ok(bannerMapper.selectList(new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSort)));
    }

    @Operation(summary = "新增Banner")
    @PostMapping("/banner")
    @AdminRequired
    public R<Void> addBanner(@RequestBody Banner dto) {
        bannerMapper.insert(dto);
        return R.ok();
    }

    @Operation(summary = "更新Banner")
    @PutMapping("/banner/{id}")
    @AdminRequired
    public R<Void> updateBanner(@PathVariable Long id, @RequestBody Banner dto) {
        dto.setId(id);
        bannerMapper.updateById(dto);
        return R.ok();
    }

    @Operation(summary = "删除Banner")
    @DeleteMapping("/banner/{id}")
    @AdminRequired
    public R<Void> deleteBanner(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return R.ok();
    }

    // ===== 热门搜索 =====
    @Operation(summary = "热门搜索列表")
    @GetMapping("/hot-search/list")
    @AdminRequired
    public R<List<HotSearch>> hotSearchList() {
        return R.ok(hotSearchMapper.selectList(new LambdaQueryWrapper<HotSearch>().orderByAsc(HotSearch::getSort)));
    }

    @Operation(summary = "新增热门搜索")
    @PostMapping("/hot-search")
    @AdminRequired
    public R<Void> addHotSearch(@RequestBody HotSearch dto) {
        hotSearchMapper.insert(dto);
        return R.ok();
    }

    @Operation(summary = "更新热门搜索")
    @PutMapping("/hot-search/{id}")
    @AdminRequired
    public R<Void> updateHotSearch(@PathVariable Long id, @RequestBody HotSearch dto) {
        dto.setId(id);
        hotSearchMapper.updateById(dto);
        return R.ok();
    }

    @Operation(summary = "删除热门搜索")
    @DeleteMapping("/hot-search/{id}")
    @AdminRequired
    public R<Void> deleteHotSearch(@PathVariable Long id) {
        hotSearchMapper.deleteById(id);
        return R.ok();
    }

    private void syncProductFields(Long productId) {
        Product p = new Product();
        p.setId(productId);
        p.setMinPrice(skuMapper.selectMinPrice(productId));
        p.setMaxPrice(skuMapper.selectMaxPrice(productId));
        p.setTotalStock(skuMapper.selectTotalStock(productId));
        productMapper.updateById(p);
    }
}
