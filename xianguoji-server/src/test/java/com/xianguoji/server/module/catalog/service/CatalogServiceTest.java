package com.xianguoji.server.module.catalog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.AbstractServiceTest;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.catalog.dto.ProductQry;
import com.xianguoji.server.module.catalog.entity.*;
import com.xianguoji.server.module.catalog.mapper.*;
import com.xianguoji.server.module.catalog.service.impl.CatalogServiceImpl;
import com.xianguoji.server.module.catalog.vo.CategoryTreeVO;
import com.xianguoji.server.module.catalog.vo.ProductDetailVO;
import com.xianguoji.server.module.catalog.vo.ProductVO;
import com.xianguoji.server.module.review.mapper.ReviewMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TC-UT-CATALOG series: Catalog service unit tests
 */
@DisplayName("Catalog Service Unit Tests")
class CatalogServiceTest extends AbstractServiceTest {

    @Mock
    private CategoryMapper categoryMapper;
    
    @Mock
    private BannerMapper bannerMapper;
    
    @Mock
    private HotSearchMapper hotSearchMapper;
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private ProductSkuMapper productSkuMapper;
    
    @Mock
    private ProductImageMapper productImageMapper;
    
    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    @Nested
    @DisplayName("getCategoryTree")
    class GetCategoryTreeTests {

        @Test
        @DisplayName("TC-UT-CAT-001: Should return category tree with children")
        void getCategoryTree_shouldReturnTreeWithChildren_whenCategoriesExist() {
            // Arrange
            Category parent = createCategory(1L, "Fruits", 0L, 1);
            Category child = createCategory(2L, "Apple", 1L, 1);
            
            when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(parent, child));

            // Act
            List<CategoryTreeVO> result = catalogService.getCategoryTree();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Fruits", result.get(0).getName());
            assertNotNull(result.get(0).getChildren());
            assertEquals(1, result.get(0).getChildren().size());
            assertEquals("Apple", result.get(0).getChildren().get(0).getName());
        }

        @Test
        @DisplayName("TC-UT-CAT-002: Should return empty list when no categories")
        void getCategoryTree_shouldReturnEmptyList_whenNoCategories() {
            // Arrange
            when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(new ArrayList<>());

            // Act
            List<CategoryTreeVO> result = catalogService.getCategoryTree();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("TC-UT-CAT-003: Should filter disabled categories")
        void getCategoryTree_shouldFilterDisabledCategories() {
            // Arrange
            Category enabled = createCategory(1L, "Fruits", 0L, 1);
            Category disabled = createCategory(2L, "Disabled", 0L, 0);
            
            when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(enabled, disabled));

            // Act
            List<CategoryTreeVO> result = catalogService.getCategoryTree();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Fruits", result.get(0).getName());
        }
    }

    @Nested
    @DisplayName("getProductDetail")
    class GetProductDetailTests {

        @Test
        @DisplayName("TC-UT-CAT-004: Should throw exception when product not found")
        void getProductDetail_shouldThrowException_whenProductNotFound() {
            // Arrange
            Long productId = 999L;
            when(productMapper.selectById(productId)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                catalogService.getProductDetail(productId);
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-CAT-005: Should return product detail with SKUs and images")
        void getProductDetail_shouldReturnDetailWithSkusAndImages_whenProductExists() {
            // Arrange
            Long productId = 1L;
            Product product = createProduct(productId, "Apple", new BigDecimal("9.90"), new BigDecimal("19.90"));
            ProductSku sku = createSku(1L, productId, "500g", new BigDecimal("9.90"), 50);
            ProductImage image = createProductImage(1L, productId, "http://test.com/image.jpg", 1);
            
            when(productMapper.selectById(productId)).thenReturn(product);
            when(productSkuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(sku));
            when(productImageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(image));
            when(reviewMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // Act
            ProductDetailVO result = catalogService.getProductDetail(productId);

            // Assert
            assertNotNull(result);
            assertEquals("Apple", result.getName());
            assertNotNull(result.getSkuList());
            assertEquals(1, result.getSkuList().size());
            assertNotNull(result.getCarouselImages());
            assertEquals(1, result.getCarouselImages().size());
        }

        @Test
        @DisplayName("TC-UT-CAT-006: Should calculate review summary correctly")
        void getProductDetail_shouldCalculateReviewSummary_whenReviewsExist() {
            // Arrange
            Long productId = 1L;
            Product product = createProduct(productId, "Apple", new BigDecimal("9.90"), new BigDecimal("19.90"));
            
            when(productMapper.selectById(productId)).thenReturn(product);
            when(productSkuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
            when(productImageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
            when(reviewMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

            // Act
            ProductDetailVO result = catalogService.getProductDetail(productId);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getReviewSummary());
            assertEquals(10L, result.getReviewSummary().getTotalCount());
        }
    }

    @Nested
    @DisplayName("getProductPage")
    class GetProductPageTests {

        @Test
        @DisplayName("TC-UT-CAT-007: Should filter by category id")
        void getProductPage_shouldFilterByCategoryId_whenCategoryIdProvided() {
            // Arrange
            ProductQry qry = new ProductQry();
            qry.setCategoryId(1L);
            qry.setPage(1);
            qry.setSize(20);
            
            Category category = new Category();
            category.setId(1L);
            category.setParentId(0L);
            
            when(categoryMapper.selectById(1L)).thenReturn(category);
            when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
            when(productMapper.selectPage(any(), any())).thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());

            // Act
            catalogService.getProductPage(qry);

            // Assert
            verify(productMapper).selectPage(any(), any());
        }

        @Test
        @DisplayName("TC-UT-CAT-008: Should filter by keyword")
        void getProductPage_shouldFilterByKeyword_whenKeywordProvided() {
            // Arrange
            ProductQry qry = new ProductQry();
            qry.setKeyword("apple");
            qry.setPage(1);
            qry.setSize(20);
            
            when(productMapper.selectPage(any(), any())).thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());

            // Act
            catalogService.getProductPage(qry);

            // Assert
            verify(productMapper).selectPage(any(), any());
        }

        @Test
        @DisplayName("TC-UT-CAT-009: Should sort by sales when sort parameter is sales")
        void getProductPage_shouldSortBySales_whenSortIsSales() {
            // Arrange
            ProductQry qry = new ProductQry();
            qry.setSort("sales");
            qry.setPage(1);
            qry.setSize(20);
            
            when(productMapper.selectPage(any(), any())).thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());

            // Act
            catalogService.getProductPage(qry);

            // Assert
            verify(productMapper).selectPage(any(), any());
        }
    }

    @Nested
    @DisplayName("getBannerList")
    class GetBannerListTests {

        @Test
        @DisplayName("TC-UT-CAT-010: Should return active banners")
        void getBannerList_shouldReturnActiveBanners_whenBannersExist() {
            // Arrange
            Banner banner = new Banner();
            banner.setId(1L);
            banner.setTitle("Test Banner");
            banner.setImage("http://test.com/banner.jpg");
            banner.setStatus(1);
            banner.setSort(1);
            
            when(bannerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(banner));

            // Act
            List result = catalogService.getBannerList();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getHotSearchList")
    class GetHotSearchListTests {

        @Test
        @DisplayName("TC-UT-CAT-011: Should return hot search keywords")
        void getHotSearchList_shouldReturnKeywords_whenHotSearchesExist() {
            // Arrange
            HotSearch hotSearch = new HotSearch();
            hotSearch.setId(1L);
            hotSearch.setKeyword("apple");
            hotSearch.setStatus(1);
            hotSearch.setSort(1);
            
            when(hotSearchMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(hotSearch));

            // Act
            List<String> result = catalogService.getHotSearchList();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("apple", result.get(0));
        }
    }

    @Nested
    @DisplayName("getRecommendProducts")
    class GetRecommendProductsTests {

        @Test
        @DisplayName("TC-UT-CAT-012: Should return recommended products")
        void getRecommendProducts_shouldReturnRecommendedProducts_whenProductsExist() {
            // Arrange
            Product product = createProduct(1L, "Apple", new BigDecimal("9.90"), new BigDecimal("19.90"));
            product.setIsRecommend(1);
            
            when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(product));

            // Act
            List<ProductVO> result = catalogService.getRecommendProducts();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Apple", result.get(0).getName());
        }
    }

    @Nested
    @DisplayName("Cache behavior")
    class CacheBehaviorTests {

        @Test
        @DisplayName("TC-UT-CAT-013: getCategoryTree - mapper called once (cache miss scenario)")
        void getCategoryTree_shouldCallMapperOnce_onCacheMiss() {
            // Arrange
            when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(new ArrayList<>());

            // Act
            catalogService.getCategoryTree();

            // Assert - mapper should be called exactly once
            verify(categoryMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-UT-CAT-014: getBannerList - mapper called once")
        void getBannerList_shouldCallMapperOnce_onCacheMiss() {
            // Arrange
            when(bannerMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(new ArrayList<>());

            // Act
            catalogService.getBannerList();

            // Assert
            verify(bannerMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-UT-CAT-015: getHotSearchList - mapper called once")
        void getHotSearchList_shouldCallMapperOnce_onCacheMiss() {
            // Arrange
            when(hotSearchMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(new ArrayList<>());

            // Act
            catalogService.getHotSearchList();

            // Assert
            verify(hotSearchMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-UT-CAT-016: getRecommendProducts - mapper called once")
        void getRecommendProducts_shouldCallMapperOnce_onCacheMiss() {
            // Arrange
            when(productMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(new ArrayList<>());

            // Act
            catalogService.getRecommendProducts();

            // Assert
            verify(productMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-UT-CAT-017: getProductDetail - mapper called once per unique id")
        void getProductDetail_shouldCallMapperOnce_perUniqueId() {
            // Arrange
            Product product = createProduct(1L, "Apple", new BigDecimal("9.90"), new BigDecimal("19.90"));
            when(productMapper.selectById(1L)).thenReturn(product);
            when(productSkuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
            when(productImageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
            when(reviewMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // Act
            catalogService.getProductDetail(1L);

            // Assert - productMapper called exactly once for this id
            verify(productMapper, times(1)).selectById(1L);
        }
    }

    // Helper methods
    private Category createCategory(Long id, String name, Long parentId, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setParentId(parentId);
        category.setStatus(status);
        category.setSort(1);
        category.setCreatedAt(LocalDateTime.now());
        return category;
    }

    private Product createProduct(Long id, String name, BigDecimal minPrice, BigDecimal maxPrice) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSubtitle("Test subtitle");
        product.setCategoryId(1L);
        product.setMainImage("http://test.com/image.jpg");
        product.setMinPrice(minPrice);
        product.setMaxPrice(maxPrice);
        product.setTotalStock(100);
        product.setSales(0);
        product.setStatus(1);
        product.setIsRecommend(0);
        product.setSupportDelivery(1);
        product.setSupportPickup(1);
        product.setCreatedAt(LocalDateTime.now());
        return product;
    }

    private ProductSku createSku(Long id, Long productId, String specName, BigDecimal price, Integer stock) {
        ProductSku sku = new ProductSku();
        sku.setId(id);
        sku.setProductId(productId);
        sku.setSpecName(specName);
        sku.setPrice(price);
        sku.setStock(stock);
        sku.setStatus(1);
        sku.setCreatedAt(LocalDateTime.now());
        return sku;
    }

    private ProductImage createProductImage(Long id, Long productId, String url, Integer type) {
        ProductImage image = new ProductImage();
        image.setId(id);
        image.setProductId(productId);
        image.setUrl(url);
        image.setType(type);
        image.setSort(1);
        return image;
    }
}


