package com.xianguoji.server.support;

import com.xianguoji.server.module.catalog.entity.Category;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.entity.ProductSku;
import com.xianguoji.server.module.catalog.mapper.CategoryMapper;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.catalog.mapper.ProductSkuMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Factory for creating test products, categories, and SKUs.
 */
@Component
public class TestProductFactory {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;

    public TestProductFactory(CategoryMapper categoryMapper, ProductMapper productMapper,
                             ProductSkuMapper productSkuMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
    }

    /**
     * Create a test category.
     */
    public Long createTestCategory(String name, Long parentId, Integer sort) {
        Category category = new Category();
        category.setName(name);
        category.setParentId(parentId);
        category.setIcon(null);
        category.setSort(sort);
        category.setStatus(1);
        category.setCreatedAt(LocalDateTime.now());
        categoryMapper.insert(category);
        return category.getId();
    }

    /**
     * Create a test product.
     */
    public Long createTestProduct(Long categoryId, String name, BigDecimal minPrice, 
                                  BigDecimal maxPrice, Integer totalStock) {
        Product product = new Product();
        product.setName(name);
        product.setSubtitle("Test subtitle");
        product.setCategoryId(categoryId);
        product.setMainImage("http://test.com/image.jpg");
        product.setDescription("<p>Test detail</p>");
        product.setMinPrice(minPrice);
        product.setMaxPrice(maxPrice);
        product.setTotalStock(totalStock);
        product.setSales(0);
        product.setStatus(1); // ON_SHELF
        product.setIsRecommend(0);
        product.setSupportDelivery(1);
        product.setSupportPickup(1);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(product);
        return product.getId();
    }

    /**
     * Create a test SKU.
     */
    public Long createTestSku(Long productId, String specs, BigDecimal price, Integer stock) {
        ProductSku sku = new ProductSku();
        sku.setProductId(productId);
        sku.setSpecName(specs);
        sku.setStatus(1);
        sku.setPrice(price);
        sku.setStock(stock);
        sku.setCreatedAt(LocalDateTime.now());
        productSkuMapper.insert(sku);
        return sku.getId();
    }

    /**
     * Create a complete test product with category and SKU.
     */
    public Long createCompleteTestProduct() {
        Long categoryId = createTestCategory("Fruits", null, 1);
        Long productId = createTestProduct(categoryId, "Apple", 
                new BigDecimal("9.90"), new BigDecimal("19.90"), 100);
        createTestSku(productId, "[{\"key\":\"size\",\"value\":\"500g\"}]", 
                new BigDecimal("9.90"), 50);
        createTestSku(productId, "[{\"key\":\"size\",\"value\":\"1kg\"}]", 
                new BigDecimal("19.90"), 50);
        return productId;
    }

    /**
     * Clean up test data.
     */
    public void cleanupProduct(Long productId) {
        if (productId != null) {
            productSkuMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xianguoji.server.module.catalog.entity.ProductSku>()
                    .eq(com.xianguoji.server.module.catalog.entity.ProductSku::getProductId, productId));
            productMapper.deleteById(productId);
        }
    }

    public void cleanupCategory(Long categoryId) {
        if (categoryId != null) {
            categoryMapper.deleteById(categoryId);
        }
    }
}
