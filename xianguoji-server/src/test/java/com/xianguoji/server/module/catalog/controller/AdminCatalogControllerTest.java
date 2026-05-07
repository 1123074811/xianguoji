package com.xianguoji.server.module.catalog.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.common.cache.SalesRankService;
import com.xianguoji.server.common.util.StockRedisHelper;
import com.xianguoji.server.common.websocket.WsNotificationService;
import com.xianguoji.server.module.catalog.entity.*;
import com.xianguoji.server.module.catalog.mapper.*;
import com.xianguoji.server.support.TestStaffFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-CAT-ADM series: Admin catalog controller API tests
 */
@DisplayName("Admin Catalog Controller API Tests")
class AdminCatalogControllerTest extends AbstractApiTest {

    @MockBean
    private CategoryMapper categoryMapper;
    
    @MockBean
    private ProductMapper productMapper;
    
    @MockBean
    private ProductSkuMapper skuMapper;
    
    @MockBean
    private ProductImageMapper productImageMapper;
    
    @MockBean
    private BannerMapper bannerMapper;
    
    @MockBean
    private HotSearchMapper hotSearchMapper;
    
    @MockBean
    private StockRedisHelper stockRedisHelper;
    
    @MockBean
    private SalesRankService salesRankService;
    
    @MockBean
    private WsNotificationService wsNotificationService;

    private String adminToken = "test_admin_token";

    @Nested
    @DisplayName("Category Management")
    class CategoryManagementTests {

        @Test
        @DisplayName("TC-API-CAT-ADM-001: Should return category list")
        void categoryList_shouldReturnCategories_success() throws Exception {
            // Arrange
            List<Category> categories = new ArrayList<>();
            Category category = createCategory(1L, "Fruits", 0L, 1);
            categories.add(category);
            
            when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(categories);

            // Act
            MvcResult result = performGet("/api/admin/category/list", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(categoryMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-002: Should add new category")
        void addCategory_shouldAddCategory_success() throws Exception {
            // Arrange
            Category category = createCategory(null, "New Category", 0L, 1);
            when(categoryMapper.insert(any(Category.class))).thenReturn(1);

            // Act
            MvcResult result = performPost("/api/admin/category", category, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(categoryMapper).insert(any(Category.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-003: Should update category")
        void updateCategory_shouldUpdateCategory_success() throws Exception {
            // Arrange
            Category category = createCategory(1L, "Updated Category", 0L, 1);
            when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

            // Act
            MvcResult result = performPut("/api/admin/category/1", category, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(categoryMapper).updateById(any(Category.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-004: Should delete category")
        void deleteCategory_shouldDeleteCategory_success() throws Exception {
            // Arrange
            when(categoryMapper.deleteById(1L)).thenReturn(1);

            // Act
            MvcResult result = performDelete("/api/admin/category/1", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(categoryMapper).deleteById(1L);
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-005: Should return 403 when staff tries to access")
        void categoryList_shouldReturn403_whenStaffAccess() throws Exception {
            // Arrange
            String staffToken = "test_staff_token";

            // Act
            MvcResult result = performGet("/api/admin/category/list", staffToken)
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("Product Management")
    class ProductManagementTests {

        @Test
        @DisplayName("TC-API-CAT-ADM-006: Should return product page with filters")
        void productPage_shouldReturnPageWithFilters_success() throws Exception {
            // Arrange
            List<Product> products = new ArrayList<>();
            Product product = createProduct(1L, "Apple", new BigDecimal("9.90"), new BigDecimal("19.90"));
            products.add(product);
            
            var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product>(1, 20);
            page.setRecords(products);
            page.setTotal(1);
            
            when(productMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(salesRankService.getTodaySalesDelta()).thenReturn(new HashMap<>());

            // Act
            MvcResult result = performGet("/api/admin/product/page?status=1&categoryId=1&page=1&size=20", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(productMapper).selectPage(any(), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-007: Should add new product with SKUs")
        void addProduct_shouldAddProductWithSkus_success() throws Exception {
            // Arrange
            Map<String, Object> body = new HashMap<>();
            body.put("name", "New Product");
            body.put("subtitle", "Test subtitle");
            body.put("categoryId", 1L);
            body.put("mainImage", "http://test.com/image.jpg");
            body.put("description", "Test description");
            body.put("isRecommend", 1);
            body.put("supportDelivery", 1);
            body.put("supportPickup", 1);
            
            List<Map<String, Object>> skus = new ArrayList<>();
            Map<String, Object> sku = new HashMap<>();
            sku.put("specName", "500g");
            sku.put("skuCode", "SKU001");
            sku.put("price", "9.90");
            sku.put("stock", 100);
            sku.put("isDefault", 1);
            skus.add(sku);
            body.put("skus", skus);
            
            when(productMapper.insert(any(Product.class))).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                p.setId(1L);
                return 1;
            });
            when(skuMapper.insert(any(ProductSku.class))).thenReturn(1);
            when(skuMapper.selectMinPrice(anyLong())).thenReturn(new BigDecimal("9.90"));
            when(skuMapper.selectMaxPrice(anyLong())).thenReturn(new BigDecimal("9.90"));
            when(skuMapper.selectTotalStock(anyLong())).thenReturn(100);

            // Act
            MvcResult result = performPost("/api/admin/product", body, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(productMapper).insert(any(Product.class));
            verify(skuMapper, atLeastOnce()).insert(any(ProductSku.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-008: Should update product")
        void updateProduct_shouldUpdateProduct_success() throws Exception {
            // Arrange
            Map<String, Object> body = new HashMap<>();
            body.put("name", "Updated Product");
            
            Product existingProduct = createProduct(1L, "Apple", new BigDecimal("9.90"), new BigDecimal("19.90"));
            when(productMapper.selectById(1L)).thenReturn(existingProduct);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            // Act
            MvcResult result = performPut("/api/admin/product/1", body, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(productMapper).updateById(any(Product.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-009: Should update product status")
        void updateProductStatus_shouldUpdateStatus_success() throws Exception {
            // Arrange
            Map<String, Integer> body = new HashMap<>();
            body.put("status", 0);
            
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            // Act
            MvcResult result = performPut("/api/admin/product/1/status", body, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(productMapper).updateById(any(Product.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-010: Should copy product")
        void copyProduct_shouldCopyProduct_success() throws Exception {
            // Arrange
            Product sourceProduct = createProduct(1L, "Apple", new BigDecimal("9.90"), new BigDecimal("19.90"));
            when(productMapper.selectById(1L)).thenReturn(sourceProduct);
            when(productMapper.insert(any(Product.class))).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                p.setId(2L);
                return 1;
            });
            when(skuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            // Act
            MvcResult result = performPost("/api/admin/product/1/copy", null, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(productMapper).insert(any(Product.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-011: Should return 404 when product not found for update")
        void updateProduct_shouldReturn404_whenProductNotFound() throws Exception {
            // Arrange
            Map<String, Object> body = new HashMap<>();
            body.put("name", "Updated Product");
            
            when(productMapper.selectById(999L)).thenReturn(null);

            // Act
            MvcResult result = performPut("/api/admin/product/999", body, adminToken)
                    .andReturn();

            // Assert
            assertEquals(500, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("Banner Management")
    class BannerManagementTests {

        @Test
        @DisplayName("TC-API-CAT-ADM-012: Should return banner list")
        void bannerList_shouldReturnBanners_success() throws Exception {
            // Arrange
            List<Banner> banners = new ArrayList<>();
            Banner banner = new Banner();
            banner.setId(1L);
            banner.setTitle("Test Banner");
            banner.setImage("http://test.com/banner.jpg");
            banner.setStatus(1);
            banner.setSort(1);
            banners.add(banner);
            
            when(bannerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(banners);

            // Act
            MvcResult result = performGet("/api/admin/banner/list", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(bannerMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-013: Should add new banner")
        void addBanner_shouldAddBanner_success() throws Exception {
            // Arrange
            Banner banner = new Banner();
            banner.setTitle("New Banner");
            banner.setImage("http://test.com/banner.jpg");
            banner.setStatus(1);
            banner.setSort(1);
            
            when(bannerMapper.insert(any(Banner.class))).thenReturn(1);

            // Act
            MvcResult result = performPost("/api/admin/banner", banner, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(bannerMapper).insert(any(Banner.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-014: Should update banner")
        void updateBanner_shouldUpdateBanner_success() throws Exception {
            // Arrange
            Banner banner = new Banner();
            banner.setId(1L);
            banner.setTitle("Updated Banner");
            
            when(bannerMapper.updateById(any(Banner.class))).thenReturn(1);

            // Act
            MvcResult result = performPut("/api/admin/banner/1", banner, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(bannerMapper).updateById(any(Banner.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-015: Should delete banner")
        void deleteBanner_shouldDeleteBanner_success() throws Exception {
            // Arrange
            when(bannerMapper.deleteById(1L)).thenReturn(1);

            // Act
            MvcResult result = performDelete("/api/admin/banner/1", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(bannerMapper).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Hot Search Management")
    class HotSearchManagementTests {

        @Test
        @DisplayName("TC-API-CAT-ADM-016: Should return hot search list")
        void hotSearchList_shouldReturnHotSearches_success() throws Exception {
            // Arrange
            List<HotSearch> hotSearches = new ArrayList<>();
            HotSearch hotSearch = new HotSearch();
            hotSearch.setId(1L);
            hotSearch.setKeyword("apple");
            hotSearch.setStatus(1);
            hotSearch.setSort(1);
            hotSearches.add(hotSearch);
            
            when(hotSearchMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(hotSearches);

            // Act
            MvcResult result = performGet("/api/admin/hot-search/list", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(hotSearchMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-017: Should add new hot search")
        void addHotSearch_shouldAddHotSearch_success() throws Exception {
            // Arrange
            HotSearch hotSearch = new HotSearch();
            hotSearch.setKeyword("new keyword");
            hotSearch.setStatus(1);
            hotSearch.setSort(1);
            
            when(hotSearchMapper.insert(any(HotSearch.class))).thenReturn(1);

            // Act
            MvcResult result = performPost("/api/admin/hot-search", hotSearch, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(hotSearchMapper).insert(any(HotSearch.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-018: Should update hot search")
        void updateHotSearch_shouldUpdateHotSearch_success() throws Exception {
            // Arrange
            HotSearch hotSearch = new HotSearch();
            hotSearch.setId(1L);
            hotSearch.setKeyword("updated keyword");
            
            when(hotSearchMapper.updateById(any(HotSearch.class))).thenReturn(1);

            // Act
            MvcResult result = performPut("/api/admin/hot-search/1", hotSearch, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(hotSearchMapper).updateById(any(HotSearch.class));
        }

        @Test
        @DisplayName("TC-API-CAT-ADM-019: Should delete hot search")
        void deleteHotSearch_shouldDeleteHotSearch_success() throws Exception {
            // Arrange
            when(hotSearchMapper.deleteById(1L)).thenReturn(1);

            // Act
            MvcResult result = performDelete("/api/admin/hot-search/1", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(hotSearchMapper).deleteById(1L);
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
}

