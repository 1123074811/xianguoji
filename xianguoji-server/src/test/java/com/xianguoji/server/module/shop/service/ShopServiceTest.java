package com.xianguoji.server.module.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.AbstractServiceTest;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.shop.entity.DeliverySetting;
import com.xianguoji.server.module.shop.entity.PickupPoint;
import com.xianguoji.server.module.shop.entity.Shop;
import com.xianguoji.server.module.shop.mapper.DeliverySettingMapper;
import com.xianguoji.server.module.shop.mapper.PickupPointMapper;
import com.xianguoji.server.module.shop.mapper.ShopMapper;
import com.xianguoji.server.module.shop.service.impl.ShopServiceImpl;
import com.xianguoji.server.module.shop.vo.DeliverySettingVO;
import com.xianguoji.server.module.shop.vo.PickupPointVO;
import com.xianguoji.server.module.shop.vo.ShopVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TC-UT-SHOP series: Shop service unit tests
 */
@DisplayName("Shop Service Unit Tests")
class ShopServiceTest extends AbstractServiceTest {

    @Mock
    private ShopMapper shopMapper;
    
    @Mock
    private PickupPointMapper pickupPointMapper;
    
    @Mock
    private DeliverySettingMapper deliverySettingMapper;

    @InjectMocks
    private ShopServiceImpl shopService;

    @Nested
    @DisplayName("getShopInfo")
    class GetShopInfoTests {

        @Test
        @DisplayName("TC-UT-SHOP-001: Should return shop info")
        void getShopInfo_shouldReturnShopInfo_success() {
            // Arrange
            Shop shop = createShop(1, "Test Shop", "123 Main St");
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);

            // Act
            ShopVO result = shopService.getShopInfo();

            // Assert
            assertNotNull(result);
            assertEquals("Test Shop", result.getName());
            assertEquals("123 Main St", result.getAddress());
        }

        @Test
        @DisplayName("TC-UT-SHOP-002: Should throw exception when shop not found")
        void getShopInfo_shouldThrowException_whenShopNotFound() {
            // Arrange
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                shopService.getShopInfo();
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
            assertTrue(exception.getMessage().contains("店铺信息不存在"));
        }
    }

    @Nested
    @DisplayName("getPickupPoints")
    class GetPickupPointsTests {

        @Test
        @DisplayName("TC-UT-SHOP-003: Should return pickup points")
        void getPickupPoints_shouldReturnPickupPoints_success() {
            // Arrange
            List<PickupPoint> points = new ArrayList<>();
            PickupPoint point = createPickupPoint(1L, "Point 1", "456 Oak Ave");
            points.add(point);
            
            when(pickupPointMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(points);

            // Act
            List<PickupPointVO> result = shopService.getPickupPoints();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Point 1", result.get(0).getName());
        }

        @Test
        @DisplayName("TC-UT-SHOP-004: Should return empty list when no pickup points")
        void getPickupPoints_shouldReturnEmptyList_whenNoPickupPoints() {
            // Arrange
            when(pickupPointMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            // Act
            List<PickupPointVO> result = shopService.getPickupPoints();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getDeliverySetting")
    class GetDeliverySettingTests {

        @Test
        @DisplayName("TC-UT-SHOP-005: Should return delivery setting")
        void getDeliverySetting_shouldReturnSetting_success() {
            // Arrange
            DeliverySetting setting = createDeliverySetting(1, new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("100"));
            when(deliverySettingMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(setting);

            // Act
            DeliverySettingVO result = shopService.getDeliverySetting();

            // Assert
            assertNotNull(result);
            assertEquals(new BigDecimal("50"), result.getMinOrderAmount());
            assertEquals(new BigDecimal("5"), result.getBaseFee());
        }

        @Test
        @DisplayName("TC-UT-SHOP-006: Should throw exception when delivery setting not found")
        void getDeliverySetting_shouldThrowException_whenSettingNotFound() {
            // Arrange
            when(deliverySettingMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                shopService.getDeliverySetting();
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
            assertTrue(exception.getMessage().contains("配送设置不存在"));
        }
    }

    // Helper methods
    private Shop createShop(Integer id, String name, String address) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setName(name);
        shop.setLogo("http://test.com/logo.jpg");
        shop.setDescription("Test description");
        shop.setPhone("1234567890");
        shop.setAddress(address);
        shop.setBusinessHours("9:00-18:00");
        shop.setIsOpen(1);
        shop.setAutoAccept(0);
        shop.setVoiceNotify(1);
        return shop;
    }

    private PickupPoint createPickupPoint(Long id, String name, String address) {
        PickupPoint point = new PickupPoint();
        point.setId(id);
        point.setName(name);
        point.setAddress(address);
        point.setPhone("1234567890");
        point.setBusinessHours("9:00-18:00");
        point.setLongitude(new BigDecimal("116.404"));
        point.setLatitude(new BigDecimal("39.915"));
        point.setStatus(1);
        point.setSort(1);
        return point;
    }

    private DeliverySetting createDeliverySetting(Integer id, BigDecimal minOrderAmount, BigDecimal baseFee, BigDecimal freeAmount) {
        DeliverySetting setting = new DeliverySetting();
        setting.setId(id);
        setting.setMinOrderAmount(minOrderAmount);
        setting.setBaseFee(baseFee);
        setting.setFreeAmount(freeAmount);
        setting.setTimeSlots("[{\"start\":\"09:00\",\"end\":\"11:00\"}]");
        return setting;
    }
}



