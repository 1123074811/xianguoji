package com.xianguoji.server.module.catalog.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.module.catalog.dto.ProductQry;
import com.xianguoji.server.module.catalog.service.CatalogService;
import com.xianguoji.server.module.catalog.vo.*;
import com.xianguoji.server.module.user.entity.SearchHistory;
import com.xianguoji.server.module.user.mapper.SearchHistoryMapper;
import com.xianguoji.server.support.TestUserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-CAT series: Catalog controller API tests
 */
@DisplayName("Catalog Controller API Tests")
class CatalogControllerTest extends AbstractApiTest {

    @MockBean
    private CatalogService catalogService;

    @MockBean
    private SearchHistoryMapper searchHistoryMapper;

    @MockBean
    private TestUserFactory testUserFactory;

    @Nested
    @DisplayName("GET /api/pub/category/tree")
    class CategoryTreeTests {

        @Test
        @DisplayName("TC-API-CAT-TREE-001: Should return category tree")
        void categoryTree_shouldReturnTree_success() throws Exception {
            // Arrange
            List<CategoryTreeVO> tree = new ArrayList<>();
            CategoryTreeVO category = CategoryTreeVO.builder()
                    .id(1L)
                    .name("Fruits")
                    .sort(1)
                    .children(new ArrayList<>())
                    .build();
            tree.add(category);
            
            when(catalogService.getCategoryTree()).thenReturn(tree);

            // Act
            MvcResult result = performGet("/api/pub/category/tree")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(catalogService).getCategoryTree();
        }

        @Test
        @DisplayName("TC-API-CAT-TREE-002: Should return empty list when no categories")
        void categoryTree_shouldReturnEmptyList_whenNoCategories() throws Exception {
            // Arrange
            when(catalogService.getCategoryTree()).thenReturn(new ArrayList<>());

            // Act
            MvcResult result = performGet("/api/pub/category/tree")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/pub/banner/list")
    class BannerListTests {

        @Test
        @DisplayName("TC-API-CAT-BNR-001: Should return active banners")
        void bannerList_shouldReturnBanners_success() throws Exception {
            // Arrange
            List<BannerVO> banners = new ArrayList<>();
            BannerVO banner = BannerVO.builder()
                    .id(1L)
                    .title("Test Banner")
                    .image("http://test.com/banner.jpg")
                    .build();
            banners.add(banner);
            
            when(catalogService.getBannerList()).thenReturn(banners);

            // Act
            MvcResult result = performGet("/api/pub/banner/list")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(catalogService).getBannerList();
        }
    }

    @Nested
    @DisplayName("GET /api/pub/hot-search/list")
    class HotSearchListTests {

        @Test
        @DisplayName("TC-API-CAT-HOT-001: Should return hot search keywords")
        void hotSearchList_shouldReturnKeywords_success() throws Exception {
            // Arrange
            List<String> keywords = List.of("apple", "banana", "orange");
            when(catalogService.getHotSearchList()).thenReturn(keywords);

            // Act
            MvcResult result = performGet("/api/pub/hot-search/list")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(catalogService).getHotSearchList();
        }
    }

    @Nested
    @DisplayName("GET /api/pub/product/page")
    class ProductPageTests {

        @Test
        @DisplayName("TC-API-CAT-PRD-001: Should return product page with category filter")
        void productPage_shouldReturnPageWithCategoryFilter_success() throws Exception {
            // Arrange
            List<ProductVO> products = new ArrayList<>();
            ProductVO product = ProductVO.builder()
                    .id(1L)
                    .name("Apple")
                    .minPrice(new BigDecimal("9.90"))
                    .maxPrice(new BigDecimal("19.90"))
                    .totalStock(100)
                    .sales(0)
                    .build();
            products.add(product);
            
            var pageVO = new com.xianguoji.server.common.result.PageVO<>(1L, products, 1, 20);
            when(catalogService.getProductPage(any(ProductQry.class))).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/pub/product/page?categoryId=1&page=1&size=20")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(catalogService).getProductPage(any(ProductQry.class));
        }

        @Test
        @DisplayName("TC-API-CAT-PRD-002: Should return product page with keyword filter")
        void productPage_shouldReturnPageWithKeywordFilter_success() throws Exception {
            // Arrange
            List<ProductVO> products = new ArrayList<>();
            var pageVO = new com.xianguoji.server.common.result.PageVO<>(0L, products, 1, 20);
            when(catalogService.getProductPage(any(ProductQry.class))).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/pub/product/page?keyword=apple&page=1&size=20")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-CAT-PRD-003: Should return product page with price range filter")
        void productPage_shouldReturnPageWithPriceRangeFilter_success() throws Exception {
            // Arrange
            List<ProductVO> products = new ArrayList<>();
            var pageVO = new com.xianguoji.server.common.result.PageVO<>(0L, products, 1, 20);
            when(catalogService.getProductPage(any(ProductQry.class))).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/pub/product/page?minPrice=10&maxPrice=50&page=1&size=20")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-CAT-PRD-004: Should return product page with sort parameter")
        void productPage_shouldReturnPageWithSort_success() throws Exception {
            // Arrange
            List<ProductVO> products = new ArrayList<>();
            var pageVO = new com.xianguoji.server.common.result.PageVO<>(0L, products, 1, 20);
            when(catalogService.getProductPage(any(ProductQry.class))).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/pub/product/page?sort=sales&page=1&size=20")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/pub/product/recommend")
    class ProductRecommendTests {

        @Test
        @DisplayName("TC-API-CAT-REC-001: Should return recommended products")
        void productRecommend_shouldReturnRecommendedProducts_success() throws Exception {
            // Arrange
            List<ProductVO> products = new ArrayList<>();
            ProductVO product = ProductVO.builder()
                    .id(1L)
                    .name("Apple")
                    .isRecommend(1)
                    .build();
            products.add(product);
            
            when(catalogService.getRecommendProducts()).thenReturn(products);

            // Act
            MvcResult result = performGet("/api/pub/product/recommend")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(catalogService).getRecommendProducts();
        }
    }

    @Nested
    @DisplayName("GET /api/pub/product/{id}")
    class ProductDetailTests {

        @Test
        @DisplayName("TC-API-CAT-DTL-001: Should return product detail")
        void productDetail_shouldReturnDetail_success() throws Exception {
            // Arrange
            ProductDetailVO detail = ProductDetailVO.builder()
                    .id(1L)
                    .name("Apple")
                    .minPrice(new BigDecimal("9.90"))
                    .maxPrice(new BigDecimal("19.90"))
                    .totalStock(100)
                    .skuList(new ArrayList<>())
                    .carouselImages(new ArrayList<>())
                    .detailImages(new ArrayList<>())
                    .build();
            
            when(catalogService.getProductDetail(1L)).thenReturn(detail);

            // Act
            MvcResult result = performGet("/api/pub/product/1")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(catalogService).getProductDetail(1L);
        }

        @Test
        @DisplayName("TC-API-CAT-DTL-002: Should return 404 when product not found")
        void productDetail_shouldReturn404_whenProductNotFound() throws Exception {
            // Arrange
            when(catalogService.getProductDetail(999L))
                    .thenThrow(new RuntimeException("商品不存在"));

            // Act
            MvcResult result = performGet("/api/pub/product/999")
                    .andReturn();

            // Assert
            assertEquals(500, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/u/search/history")
    class SearchHistoryTests {

        @Test
        @DisplayName("TC-API-CAT-SH-001: Should return search history when logged in")
        void searchHistory_shouldReturnHistory_success() throws Exception {
            // Arrange
            String token = "test_token";
            List<SearchHistory> history = new ArrayList<>();
            SearchHistory sh = new SearchHistory();
            sh.setId(1L);
            sh.setUserId(1L);
            sh.setKeyword("apple");
            sh.setCreatedAt(LocalDateTime.now());
            history.add(sh);
            
            when(searchHistoryMapper.selectList(any())).thenReturn(history);

            // Act
            MvcResult result = performGet("/api/u/search/history", token)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-CAT-SH-003: Should return 401 when not logged in")
        void searchHistory_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performGet("/api/u/search/history")
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("DELETE /api/u/search/history")
    class ClearSearchHistoryTests {

        @Test
        @DisplayName("TC-API-CAT-SH-003: Should clear search history when logged in")
        void clearSearchHistory_shouldClearHistory_success() throws Exception {
            // Arrange
            String token = "test_token";
            when(searchHistoryMapper.delete(any())).thenReturn(1);

            // Act
            MvcResult result = performDelete("/api/u/search/history", token)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(searchHistoryMapper).delete(any());
        }
    }

    @Nested
    @DisplayName("POST /api/u/search/record")
    class RecordSearchTests {

        @Test
        @DisplayName("TC-API-CAT-SH-004: Should insert new search keyword")
        void recordSearch_shouldInsertNewKeyword_success() throws Exception {
            // Arrange
            String token = "test_token";
            var body = Map.of("keyword", "apple");
            when(searchHistoryMapper.selectOne(any())).thenReturn(null);
            when(searchHistoryMapper.insert(any(SearchHistory.class))).thenReturn(1);

            // Act
            MvcResult result = performPost("/api/u/search/record", body, token)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(searchHistoryMapper).insert(any(SearchHistory.class));
            verify(searchHistoryMapper, never()).updateById(any(SearchHistory.class));
        }

        @Test
        @DisplayName("TC-API-CAT-SH-005: Should update existing keyword instead of duplicate")
        void recordSearch_shouldUpdateExistingKeyword_success() throws Exception {
            // Arrange
            String token = "test_token";
            var body = Map.of("keyword", "apple");
            SearchHistory existing = new SearchHistory();
            existing.setId(1L);
            existing.setUserId(1L);
            existing.setKeyword("apple");
            existing.setCreatedAt(LocalDateTime.now().minusDays(1));
            when(searchHistoryMapper.selectOne(any())).thenReturn(existing);
            when(searchHistoryMapper.updateById(any(SearchHistory.class))).thenReturn(1);

            // Act
            MvcResult result = performPost("/api/u/search/record", body, token)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(searchHistoryMapper).updateById(any(SearchHistory.class));
            verify(searchHistoryMapper, never()).insert(any(SearchHistory.class));
        }

        @Test
        @DisplayName("TC-API-CAT-SH-006: Should not record empty keyword")
        void recordSearch_shouldNotRecordEmptyKeyword_success() throws Exception {
            // Arrange
            String token = "test_token";
            var body = Map.of("keyword", "");

            // Act
            MvcResult result = performPost("/api/u/search/record", body, token)
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(searchHistoryMapper, never()).insert(any(SearchHistory.class));
            verify(searchHistoryMapper, never()).updateById(any(SearchHistory.class));
        }
    }
}
