package com.xianguoji.server.module.shop.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.common.cache.PickupGeoService;
import com.xianguoji.server.module.shop.service.ShopService;
import com.xianguoji.server.module.shop.vo.DeliverySettingVO;
import com.xianguoji.server.module.shop.vo.PickupPointVO;
import com.xianguoji.server.module.shop.vo.ShopVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-SHOP series: Shop controller API tests
 */
@DisplayName("Shop Controller API Tests")
class ShopControllerTest extends AbstractApiTest {

    @MockBean
    private ShopService shopService;
    
    @MockBean
    private PickupGeoService pickupGeoService;

    @Nested
    @DisplayName("GET /api/pub/shop/info")
    class ShopInfoTests {

        @Test
        @DisplayName("TC-API-SHOP-001: Should return shop info")
        void shopInfo_shouldReturnShopInfo_success() throws Exception {
            // Arrange
            ShopVO shop = ShopVO.builder()
                    .id(1)
                    .name("Test Shop")
                    .address("123 Main St")
                    .phone("1234567890")
                    .isOpen(1)
                    .build();
            
            when(shopService.getShopInfo()).thenReturn(shop);

            // Act
            MvcResult result = performGet("/api/pub/shop/info")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(shopService).getShopInfo();
        }
    }

    @Nested
    @DisplayName("GET /api/pub/pickup-point/list")
    class PickupPointsTests {

        @Test
        @DisplayName("TC-API-SHOP-002: Should return pickup points")
        void pickupPoints_shouldReturnPickupPoints_success() throws Exception {
            // Arrange
            List<PickupPointVO> points = new ArrayList<>();
            PickupPointVO point = PickupPointVO.builder()
                    .id(1L)
                    .name("Point 1")
                    .address("456 Oak Ave")
                    .build();
            points.add(point);
            
            when(shopService.getPickupPoints()).thenReturn(points);

            // Act
            MvcResult result = performGet("/api/pub/pickup-point/list")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(shopService).getPickupPoints();
        }
    }

    @Nested
    @DisplayName("GET /api/pub/pickup-point/geo")
    class PickupPointsGeoTests {

        @Test
        @DisplayName("TC-API-SHOP-003: Should return sorted pickup points by geo")
        void pickupPointsGeo_shouldReturnSortedPoints_success() throws Exception {
            // Arrange
            List<PickupPointVO> points = new ArrayList<>();
            PickupPointVO point = PickupPointVO.builder()
                    .id(1L)
                    .name("Point 1")
                    .build();
            points.add(point);
            
            when(pickupGeoService.sortByDistance(anyDouble(), anyDouble(), anyDouble())).thenReturn(points);

            // Act
            MvcResult result = performGet("/api/pub/pickup-point/geo?lng=116.404&lat=39.915&radiusKm=50")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(pickupGeoService).sortByDistance(anyDouble(), anyDouble(), anyDouble());
        }
    }

    @Nested
    @DisplayName("GET /api/pub/delivery-setting")
    class DeliverySettingTests {

        @Test
        @DisplayName("TC-API-SHOP-004: Should return delivery setting")
        void deliverySetting_shouldReturnSetting_success() throws Exception {
            // Arrange
            DeliverySettingVO setting = DeliverySettingVO.builder()
                    .minOrderAmount(new BigDecimal("50"))
                    .baseFee(new BigDecimal("5"))
                    .freeAmount(new BigDecimal("100"))
                    .build();
            
            when(shopService.getDeliverySetting()).thenReturn(setting);

            // Act
            MvcResult result = performGet("/api/pub/delivery-setting")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(shopService).getDeliverySetting();
        }
    }
}

