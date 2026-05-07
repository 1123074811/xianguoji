package com.xianguoji.server.module.shop.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.common.cache.PickupGeoService;
import com.xianguoji.server.common.websocket.WsNotificationService;
import com.xianguoji.server.module.shop.entity.AdminNotification;
import com.xianguoji.server.module.shop.entity.DeliverySetting;
import com.xianguoji.server.module.shop.entity.NotifySetting;
import com.xianguoji.server.module.shop.entity.PickupPoint;
import com.xianguoji.server.module.shop.entity.Shop;
import com.xianguoji.server.module.shop.mapper.AdminNotificationMapper;
import com.xianguoji.server.module.shop.mapper.DeliverySettingMapper;
import com.xianguoji.server.module.shop.mapper.NotifySettingMapper;
import com.xianguoji.server.module.shop.mapper.PickupPointMapper;
import com.xianguoji.server.module.shop.mapper.ShopMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-SHOP-ADM series: Admin shop controller API tests
 */
@DisplayName("Admin Shop Controller API Tests")
class AdminShopControllerTest extends AbstractApiTest {

    @MockBean
    private ShopMapper shopMapper;
    
    @MockBean
    private PickupPointMapper pickupPointMapper;
    
    @MockBean
    private PickupGeoService pickupGeoService;
    
    @MockBean
    private DeliverySettingMapper deliverySettingMapper;
    
    @MockBean
    private NotifySettingMapper notifySettingMapper;
    
    @MockBean
    private AdminNotificationMapper adminNotificationMapper;
    
    @MockBean
    private WsNotificationService wsNotificationService;

    private String adminToken = "test_admin_token";

    @Nested
    @DisplayName("GET /api/admin/shop/info")
    class GetShopInfoTests {

        @Test
        @DisplayName("TC-API-SHOP-ADM-001: Should return shop info")
        void getShopInfo_shouldReturnShopInfo_success() throws Exception {
            // Arrange
            Shop shop = createShop(1, "Test Shop", "123 Main St");
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);

            // Act
            MvcResult result = performGet("/api/admin/shop/info", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(shopMapper).selectOne(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-API-SHOP-ADM-002: Should return 403 when not admin")
        void getShopInfo_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/shop/info", "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/shop/info")
    class UpdateShopInfoTests {

        @Test
        @DisplayName("TC-API-SHOP-ADM-003: Should update shop info successfully")
        void updateShopInfo_shouldUpdate_success() throws Exception {
            // Arrange
            Shop shop = createShop(1, "Test Shop", "123 Main St");
            Shop dto = createShop(null, "Updated Shop", "456 Oak Ave");
            
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(shopMapper.updateById(any(Shop.class))).thenReturn(1);

            // Act
            MvcResult result = performPut("/api/admin/shop/info", dto, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(shopMapper).updateById(any(Shop.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/shop/open-status")
    class ToggleOpenStatusTests {

        @Test
        @DisplayName("TC-API-SHOP-ADM-004: Should toggle open status successfully")
        void toggleOpenStatus_shouldToggle_success() throws Exception {
            // Arrange
            Shop shop = createShop(1, "Test Shop", "123 Main St");
            Map<String, Integer> body = Map.of("isOpen", 0);
            
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(shopMapper.updateById(any(Shop.class))).thenReturn(1);

            // Act
            MvcResult result = performPut("/api/admin/shop/open-status", body, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("Pickup Point Management")
    class PickupPointManagementTests {

        @Test
        @DisplayName("TC-API-SHOP-ADM-005: Should return pickup point list")
        void pickupPointList_shouldReturnList_success() throws Exception {
            // Arrange
            List<PickupPoint> points = new ArrayList<>();
            PickupPoint point = createPickupPoint(1L, "Point 1", "456 Oak Ave");
            points.add(point);
            
            when(pickupPointMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(points);

            // Act
            MvcResult result = performGet("/api/admin/pickup-point/list", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-SHOP-ADM-006: Should add pickup point successfully")
        void addPickupPoint_shouldAdd_success() throws Exception {
            // Arrange
            PickupPoint dto = createPickupPoint(null, "New Point", "789 Pine St");
            when(pickupPointMapper.insert(any(PickupPoint.class))).thenReturn(1);
            doNothing().when(pickupGeoService).rebuildGeoIndex();

            // Act
            MvcResult result = performPost("/api/admin/pickup-point", dto, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(pickupPointMapper).insert(any(PickupPoint.class));
            verify(pickupGeoService).rebuildGeoIndex();
        }

        @Test
        @DisplayName("TC-API-SHOP-ADM-007: Should update pickup point successfully")
        void updatePickupPoint_shouldUpdate_success() throws Exception {
            // Arrange
            PickupPoint dto = createPickupPoint(1L, "Updated Point", "789 Pine St");
            when(pickupPointMapper.updateById(any(PickupPoint.class))).thenReturn(1);
            doNothing().when(pickupGeoService).rebuildGeoIndex();

            // Act
            MvcResult result = performPut("/api/admin/pickup-point/1", dto, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(pickupGeoService).rebuildGeoIndex();
        }

        @Test
        @DisplayName("TC-API-SHOP-ADM-008: Should delete pickup point successfully")
        void deletePickupPoint_shouldDelete_success() throws Exception {
            // Arrange
            when(pickupPointMapper.deleteById(1L)).thenReturn(1);
            doNothing().when(pickupGeoService).rebuildGeoIndex();

            // Act
            MvcResult result = performDelete("/api/admin/pickup-point/1", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(pickupGeoService).rebuildGeoIndex();
        }
    }

    @Nested
    @DisplayName("Delivery Setting Management")
    class DeliverySettingManagementTests {

        @Test
        @DisplayName("TC-API-SHOP-ADM-009: Should return delivery setting")
        void getDeliverySetting_shouldReturnSetting_success() throws Exception {
            // Arrange
            DeliverySetting setting = createDeliverySetting(1, new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("100"));
            when(deliverySettingMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(setting);

            // Act
            MvcResult result = performGet("/api/admin/delivery-setting", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-SHOP-ADM-010: Should update delivery setting successfully")
        void updateDeliverySetting_shouldUpdate_success() throws Exception {
            // Arrange
            DeliverySetting existing = createDeliverySetting(1, new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("100"));
            DeliverySetting dto = createDeliverySetting(null, new BigDecimal("60"), new BigDecimal("6"), new BigDecimal("120"));
            
            when(deliverySettingMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
            when(deliverySettingMapper.updateById(any(DeliverySetting.class))).thenReturn(1);

            // Act
            MvcResult result = performPut("/api/admin/delivery-setting", dto, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-SHOP-ADM-011: Should insert delivery setting when not exists")
        void updateDeliverySetting_shouldInsert_whenNotExists() throws Exception {
            // Arrange
            DeliverySetting dto = createDeliverySetting(null, new BigDecimal("60"), new BigDecimal("6"), new BigDecimal("120"));
            
            when(deliverySettingMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(deliverySettingMapper.insert(any(DeliverySetting.class))).thenReturn(1);

            // Act
            MvcResult result = performPut("/api/admin/delivery-setting", dto, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(deliverySettingMapper).insert(any(DeliverySetting.class));
        }
    }

    @Nested
    @DisplayName("Notification Management")
    class NotificationManagementTests {

        @Test
        @DisplayName("TC-API-SHOP-ADM-012: Should return notification list")
        void notificationList_shouldReturnList_success() throws Exception {
            // Arrange
            List<AdminNotification> notifications = new ArrayList<>();
            AdminNotification notification = new AdminNotification();
            notification.setId(1L);
            notification.setType(1);
            notification.setTitle("Test");
            notifications.add(notification);
            
            when(adminNotificationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(notifications);

            // Act
            MvcResult result = performGet("/api/admin/notification/list", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-SHOP-ADM-013: Should mark notification as read")
        void markRead_shouldMarkRead_success() throws Exception {
            // Arrange
            AdminNotification notification = new AdminNotification();
            notification.setId(1L);
            when(adminNotificationMapper.selectById(1L)).thenReturn(notification);
            when(adminNotificationMapper.updateById(any(AdminNotification.class))).thenReturn(1);

            // Act
            MvcResult result = performPut("/api/admin/notification/1/read", null, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-SHOP-ADM-014: Should mark all notifications as read")
        void markAllRead_shouldMarkAllRead_success() throws Exception {
            // Arrange
            List<AdminNotification> notifications = new ArrayList<>();
            AdminNotification notification = new AdminNotification();
            notification.setId(1L);
            notification.setIsRead(0);
            notifications.add(notification);
            
            when(adminNotificationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(notifications);
            when(adminNotificationMapper.updateById(any(AdminNotification.class))).thenReturn(1);

            // Act
            MvcResult result = performPut("/api/admin/notification/read-all", null, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-SHOP-ADM-015: Should return unread count")
        void unreadCount_shouldReturnCount_success() throws Exception {
            // Arrange
            when(adminNotificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            // Act
            MvcResult result = performGet("/api/admin/notification/unread-count", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-SHOP-ADM-016: Should publish system notification successfully")
        void publishSystemNotification_shouldPublish_success() throws Exception {
            // Arrange
            Map<String, String> body = Map.of(
                    "title", "System Notice",
                    "content", "Test content",
                    "linkUrl", "/test"
            );
            doNothing().when(wsNotificationService).notifySystem(anyString(), anyString(), anyString());

            // Act
            MvcResult result = performPost("/api/admin/notification/system", body, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(wsNotificationService).notifySystem(anyString(), anyString(), anyString());
        }
    }

    // Helper methods
    private Shop createShop(Integer id, String name, String address) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setName(name);
        shop.setAddress(address);
        return shop;
    }

    private PickupPoint createPickupPoint(Long id, String name, String address) {
        PickupPoint point = new PickupPoint();
        point.setId(id);
        point.setName(name);
        point.setAddress(address);
        return point;
    }

    private DeliverySetting createDeliverySetting(Integer id, BigDecimal minOrderAmount, BigDecimal baseFee, BigDecimal freeAmount) {
        DeliverySetting setting = new DeliverySetting();
        setting.setId(id);
        setting.setMinOrderAmount(minOrderAmount);
        setting.setBaseFee(baseFee);
        setting.setFreeAmount(freeAmount);
        return setting;
    }
}



