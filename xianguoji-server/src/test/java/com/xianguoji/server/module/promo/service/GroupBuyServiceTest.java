package com.xianguoji.server.module.promo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xianguoji.server.AbstractServiceTest;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.util.StockRedisHelper;
import com.xianguoji.server.common.websocket.WsNotificationService;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.entity.ProductSku;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.catalog.mapper.ProductSkuMapper;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import com.xianguoji.server.module.promo.dto.GroupBuyLaunchDto;
import com.xianguoji.server.module.promo.dto.GroupBuyJoinDto;
import com.xianguoji.server.module.promo.entity.GroupBuyActivity;
import com.xianguoji.server.module.promo.entity.GroupBuyInstance;
import com.xianguoji.server.module.promo.entity.GroupBuyParticipant;
import com.xianguoji.server.module.promo.mapper.GroupBuyActivityMapper;
import com.xianguoji.server.module.promo.mapper.GroupBuyInstanceMapper;
import com.xianguoji.server.module.promo.mapper.GroupBuyParticipantMapper;
import com.xianguoji.server.module.promo.service.impl.GroupBuyServiceImpl;
import com.xianguoji.server.module.promo.vo.GroupBuyActivityVO;
import com.xianguoji.server.module.promo.vo.GroupBuyInstanceVO;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * TC-UT-PROMO-GB series: GroupBuy service unit tests
 */
@DisplayName("GroupBuy Service Unit Tests")
class GroupBuyServiceTest extends AbstractServiceTest {

    @Mock
    private GroupBuyActivityMapper activityMapper;
    
    @Mock
    private GroupBuyInstanceMapper instanceMapper;
    
    @Mock
    private GroupBuyParticipantMapper participantMapper;
    
    @Mock
    private OrderMapper orderMapper;
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private ProductSkuMapper skuMapper;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private StockRedisHelper stockRedisHelper;
    
    @Mock
    private WsNotificationService wsNotificationService;

    @InjectMocks
    private GroupBuyServiceImpl groupBuyService;

    @Nested
    @DisplayName("getGroupBuyPage")
    class GetGroupBuyPageTests {

        @Test
        @DisplayName("TC-UT-PROMO-GB-001: Should return group buy activities page")
        void getGroupBuyPage_shouldReturnPage_success() {
            // Arrange
            GroupBuyActivity activity = createGroupBuyActivity(1L, 1L, 1L, 2, new BigDecimal("9.90"));
            var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<GroupBuyActivity>(1, 20);
            page.setRecords(List.of(activity));
            page.setTotal(1);
            
            when(activityMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            PageVO<GroupBuyActivityVO> result = groupBuyService.getGroupBuyPage(1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getList().size());
        }

        @Test
        @DisplayName("TC-UT-PROMO-GB-002: Should return empty page when no activities")
        void getGroupBuyPage_shouldReturnEmptyPage_whenNoActivities() {
            // Arrange
            var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<GroupBuyActivity>(1, 20);
            page.setRecords(new ArrayList<>());
            page.setTotal(0);
            
            when(activityMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            PageVO<GroupBuyActivityVO> result = groupBuyService.getGroupBuyPage(1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotal());
            assertTrue(result.getList().isEmpty());
        }
    }

    @Nested
    @DisplayName("launch")
    class LaunchTests {

        @Test
        @DisplayName("TC-UT-PROMO-GB-003: Should throw exception when activity not found")
        void launch_shouldThrowException_whenActivityNotFound() {
            // Arrange
            Long uid = 1L;
            GroupBuyLaunchDto dto = new GroupBuyLaunchDto();
            dto.setActivityId(999L);
            
            when(activityMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                groupBuyService.launch(uid, dto);
            });
            assertEquals(ResultCode.GROUP_BUY_ENDED, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-PROMO-GB-004: Should throw exception when activity ended")
        void launch_shouldThrowException_whenActivityEnded() {
            // Arrange
            Long uid = 1L;
            GroupBuyLaunchDto dto = new GroupBuyLaunchDto();
            dto.setActivityId(1L);
            
            GroupBuyActivity activity = createGroupBuyActivity(1L, 1L, 1L, 2, new BigDecimal("9.90"));
            activity.setStatus(0); // Inactive
            activity.setEndTime(LocalDateTime.now().minusDays(1));
            
            when(activityMapper.selectById(1L)).thenReturn(activity);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                groupBuyService.launch(uid, dto);
            });
            assertEquals(ResultCode.GROUP_BUY_ENDED, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-PROMO-GB-005: Should throw exception when stock not enough")
        void launch_shouldThrowException_whenStockNotEnough() {
            // Arrange
            Long uid = 1L;
            GroupBuyLaunchDto dto = new GroupBuyLaunchDto();
            dto.setActivityId(1L);
            
            GroupBuyActivity activity = createGroupBuyActivity(1L, 1L, 1L, 2, new BigDecimal("9.90"));
            activity.setStatus(1);
            activity.setEndTime(LocalDateTime.now().plusDays(1));
            
            ProductSku sku = createProductSku(1L, 1L, "500g", new BigDecimal("9.90"), 0);
            
            when(activityMapper.selectById(1L)).thenReturn(activity);
            when(skuMapper.selectById(1L)).thenReturn(sku);
            when(stockRedisHelper.getStock(1L)).thenReturn(0);
            when(skuMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(0);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                groupBuyService.launch(uid, dto);
            });
            assertEquals(ResultCode.STOCK_NOT_ENOUGH, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-PROMO-GB-006: Should successfully launch group buy")
        void launch_shouldLaunch_success() {
            // Arrange
            Long uid = 1L;
            GroupBuyLaunchDto dto = new GroupBuyLaunchDto();
            dto.setActivityId(1L);
            dto.setAddressId(1L);
            dto.setPayMethod("wechat");
            
            GroupBuyActivity activity = createGroupBuyActivity(1L, 1L, 1L, 2, new BigDecimal("9.90"));
            activity.setStatus(1);
            activity.setEndTime(LocalDateTime.now().plusDays(1));
            activity.setValidHours(24);
            
            ProductSku sku = createProductSku(1L, 1L, "500g", new BigDecimal("9.90"), 100);
            Product product = createProduct(1L, "Apple");
            
            when(activityMapper.selectById(1L)).thenReturn(activity);
            when(skuMapper.selectById(1L)).thenReturn(sku);
            when(productMapper.selectById(1L)).thenReturn(product);
            when(stockRedisHelper.getStock(1L)).thenReturn(100);
            when(stockRedisHelper.deduct(1L, 1)).thenReturn(true);
            when(skuMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(instanceMapper.insert(any(GroupBuyInstance.class))).thenAnswer(inv -> {
                GroupBuyInstance inst = inv.getArgument(0);
                inst.setId(1L);
                return 1;
            });
            when(participantMapper.insert(any(GroupBuyParticipant.class))).thenReturn(1);
            when(activityMapper.updateById(any(GroupBuyActivity.class))).thenReturn(1);

            // Act
            Long instanceId = groupBuyService.launch(uid, dto);

            // Assert
            assertNotNull(instanceId);
            verify(instanceMapper).insert(any(GroupBuyInstance.class));
            verify(participantMapper).insert(any(GroupBuyParticipant.class));
        }
    }

    @Nested
    @DisplayName("join")
    class JoinTests {

        @Test
        @DisplayName("TC-UT-PROMO-GB-007: Should throw exception when instance not found")
        void join_shouldThrowException_whenInstanceNotFound() {
            // Arrange
            Long uid = 1L;
            Long instanceId = 999L;
            GroupBuyJoinDto dto = new GroupBuyJoinDto();
            
            when(instanceMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                groupBuyService.join(uid, instanceId, dto);
            });
            assertEquals(ResultCode.GROUP_BUY_ENDED, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-PROMO-GB-008: Should throw exception when already joined")
        void join_shouldThrowException_whenAlreadyJoined() {
            // Arrange
            Long uid = 1L;
            Long instanceId = 1L;
            GroupBuyJoinDto dto = new GroupBuyJoinDto();
            
            GroupBuyInstance instance = createGroupBuyInstance(1L, 1L, 1L, 2, 1);
            instance.setStatus(1);
            instance.setExpireAt(LocalDateTime.now().plusHours(1));
            
            when(instanceMapper.selectById(1L)).thenReturn(instance);
            when(participantMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                groupBuyService.join(uid, instanceId, dto);
            });
            assertEquals(ResultCode.CONFLICT, exception.getResultCode());
            assertTrue(exception.getMessage().contains("已参团"));
        }

        @Test
        @DisplayName("TC-UT-PROMO-GB-009: Should successfully join group buy")
        void join_shouldJoin_success() {
            // Arrange
            Long uid = 2L;
            Long instanceId = 1L;
            GroupBuyJoinDto dto = new GroupBuyJoinDto();
            dto.setAddressId(1L);
            dto.setPayMethod("wechat");
            
            GroupBuyInstance instance = createGroupBuyInstance(1L, 1L, 1L, 2, 1);
            instance.setStatus(1);
            instance.setExpireAt(LocalDateTime.now().plusHours(1));
            instance.setCurrentSize(1);
            
            GroupBuyActivity activity = createGroupBuyActivity(1L, 1L, 1L, 2, new BigDecimal("9.90"));
            ProductSku sku = createProductSku(1L, 1L, "500g", new BigDecimal("9.90"), 100);
            Product product = createProduct(1L, "Apple");
            
            when(instanceMapper.selectById(1L)).thenReturn(instance);
            when(participantMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(activityMapper.selectById(1L)).thenReturn(activity);
            when(skuMapper.selectById(1L)).thenReturn(sku);
            when(productMapper.selectById(1L)).thenReturn(product);
            when(stockRedisHelper.getStock(1L)).thenReturn(100);
            when(stockRedisHelper.deduct(1L, 1)).thenReturn(true);
            when(skuMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(participantMapper.insert(any(GroupBuyParticipant.class))).thenReturn(1);
            when(instanceMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(instanceMapper.selectById(1L)).thenReturn(instance);
            when(activityMapper.updateById(any(GroupBuyActivity.class))).thenReturn(1);

            // Act
            Long result = groupBuyService.join(uid, instanceId, dto);

            // Assert
            assertEquals(instanceId, result);
            verify(participantMapper).insert(any(GroupBuyParticipant.class));
        }
    }

    @Nested
    @DisplayName("getInstanceDetail")
    class GetInstanceDetailTests {

        @Test
        @DisplayName("TC-UT-PROMO-GB-010: Should return instance detail")
        void getInstanceDetail_shouldReturnDetail_success() {
            // Arrange
            Long instanceId = 1L;
            GroupBuyInstance instance = createGroupBuyInstance(1L, 1L, 1L, 2, 1);
            User user = createUser(1L, "Test User");
            GroupBuyActivity activity = createGroupBuyActivity(1L, 1L, 1L, 2, new BigDecimal("9.90"));
            Product product = createProduct(1L, "Apple");
            
            when(instanceMapper.selectById(1L)).thenReturn(instance);
            when(userMapper.selectById(1L)).thenReturn(user);
            when(participantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
            when(activityMapper.selectById(1L)).thenReturn(activity);
            when(productMapper.selectById(1L)).thenReturn(product);

            // Act
            GroupBuyInstanceVO result = groupBuyService.getInstanceDetail(instanceId);

            // Assert
            assertNotNull(result);
            assertEquals(instanceId, result.getId());
        }

        @Test
        @DisplayName("TC-UT-PROMO-GB-011: Should throw exception when instance not found")
        void getInstanceDetail_shouldThrowException_whenInstanceNotFound() {
            // Arrange
            Long instanceId = 999L;
            when(instanceMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                groupBuyService.getInstanceDetail(instanceId);
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }
    }

    @Nested
    @DisplayName("getInstanceByShareCode")
    class GetInstanceByShareCodeTests {

        @Test
        @DisplayName("TC-UT-PROMO-GB-012: Should return instance by share code")
        void getInstanceByShareCode_shouldReturnInstance_success() {
            // Arrange
            String shareCode = "ABC12345";
            GroupBuyInstance instance = createGroupBuyInstance(1L, 1L, 1L, 2, 1);
            instance.setShareCode(shareCode);
            User user = createUser(1L, "Test User");
            GroupBuyActivity activity = createGroupBuyActivity(1L, 1L, 1L, 2, new BigDecimal("9.90"));
            Product product = createProduct(1L, "Apple");
            
            when(instanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(instance);
            when(userMapper.selectById(1L)).thenReturn(user);
            when(participantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
            when(activityMapper.selectById(1L)).thenReturn(activity);
            when(productMapper.selectById(1L)).thenReturn(product);

            // Act
            GroupBuyInstanceVO result = groupBuyService.getInstanceByShareCode(shareCode);

            // Assert
            assertNotNull(result);
            assertEquals(shareCode, result.getShareCode());
        }

        @Test
        @DisplayName("TC-UT-PROMO-GB-013: Should throw exception when share code is empty")
        void getInstanceByShareCode_shouldThrowException_whenShareCodeEmpty() {
            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                groupBuyService.getInstanceByShareCode("");
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-PROMO-GB-014: Should throw exception when instance not found by share code")
        void getInstanceByShareCode_shouldThrowException_whenInstanceNotFound() {
            // Arrange
            String shareCode = "INVALID";
            when(instanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                groupBuyService.getInstanceByShareCode(shareCode);
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }
    }

    @Nested
    @DisplayName("getActivityByProduct")
    class GetActivityByProductTests {

        @Test
        @DisplayName("TC-UT-PROMO-GB-015: Should return activity by product")
        void getActivityByProduct_shouldReturnActivity_success() {
            // Arrange
            Long productId = 1L;
            GroupBuyActivity activity = createGroupBuyActivity(1L, 1L, 1L, 2, new BigDecimal("9.90"));
            Product product = createProduct(1L, "Apple");
            ProductSku sku = createProductSku(1L, 1L, "500g", new BigDecimal("9.90"), 100);
            
            when(activityMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(activity);
            when(productMapper.selectById(1L)).thenReturn(product);
            when(skuMapper.selectById(1L)).thenReturn(sku);

            // Act
            GroupBuyActivityVO result = groupBuyService.getActivityByProduct(productId);

            // Assert
            assertNotNull(result);
            assertEquals(productId, result.getProductId());
        }

        @Test
        @DisplayName("TC-UT-PROMO-GB-016: Should return null when no activity found")
        void getActivityByProduct_shouldReturnNull_whenNoActivity() {
            // Arrange
            Long productId = 999L;
            when(activityMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act
            GroupBuyActivityVO result = groupBuyService.getActivityByProduct(productId);

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("getStats")
    class GetStatsTests {

        @Test
        @DisplayName("TC-UT-PROMO-GB-017: Should return statistics")
        void getStats_shouldReturnStats_success() {
            // Arrange
            GroupBuyActivity activity = createGroupBuyActivity(1L, 1L, 1L, 2, new BigDecimal("9.90"));
            activity.setStatus(1);
            activity.setTotalJoinCount(10);
            activity.setSuccessCount(5);
            
            GroupBuyInstance instance1 = createGroupBuyInstance(1L, 1L, 1L, 2, 2);
            instance1.setStatus(2); // Success
            GroupBuyInstance instance2 = createGroupBuyInstance(2L, 1L, 1L, 2, 1);
            instance2.setStatus(1); // Ongoing
            GroupBuyInstance instance3 = createGroupBuyInstance(3L, 1L, 1L, 2, 0);
            instance3.setStatus(3); // Failed
            
            when(activityMapper.selectList(null)).thenReturn(List.of(activity));
            when(instanceMapper.selectCount(null)).thenReturn(3L);
            when(instanceMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L, 1L, 1L);
            when(instanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(instance1));

            // Act
            Map<String, Object> result = groupBuyService.getStats();

            // Assert
            assertNotNull(result);
            assertTrue(result.containsKey("activeActivityCount"));
            assertTrue(result.containsKey("instanceTotal"));
            assertTrue(result.containsKey("successRate"));
        }
    }

    // Helper methods
    private GroupBuyActivity createGroupBuyActivity(Long id, Long productId, Long skuId, Integer groupSize, BigDecimal groupPrice) {
        GroupBuyActivity activity = new GroupBuyActivity();
        activity.setId(id);
        activity.setProductId(productId);
        activity.setSkuId(skuId);
        activity.setGroupSize(groupSize);
        activity.setGroupPrice(groupPrice);
        activity.setValidHours(24);
        activity.setEndTime(LocalDateTime.now().plusDays(1));
        activity.setStatus(1);
        activity.setTotalJoinCount(0);
        activity.setSuccessCount(0);
        activity.setCreatedAt(LocalDateTime.now());
        return activity;
    }

    private GroupBuyInstance createGroupBuyInstance(Long id, Long activityId, Long leaderId, Integer targetSize, Integer currentSize) {
        GroupBuyInstance instance = new GroupBuyInstance();
        instance.setId(id);
        instance.setActivityId(activityId);
        instance.setLeaderId(leaderId);
        instance.setTargetSize(targetSize);
        instance.setCurrentSize(currentSize);
        instance.setStatus(1);
        instance.setExpireAt(LocalDateTime.now().plusHours(24));
        instance.setShareCode("ABC12345");
        instance.setCreatedAt(LocalDateTime.now());
        return instance;
    }

    private Product createProduct(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setMainImage("http://test.com/image.jpg");
        product.setCreatedAt(LocalDateTime.now());
        return product;
    }

    private ProductSku createProductSku(Long id, Long productId, String specName, BigDecimal price, Integer stock) {
        ProductSku sku = new ProductSku();
        sku.setId(id);
        sku.setProductId(productId);
        sku.setSpecName(specName);
        sku.setPrice(price);
        sku.setOriginalPrice(new BigDecimal("19.90"));
        sku.setStock(stock);
        sku.setSales(0);
        sku.setStatus(1);
        sku.setCreatedAt(LocalDateTime.now());
        return sku;
    }

    private User createUser(Long id, String nickname) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setAvatar("http://test.com/avatar.jpg");
        return user;
    }
}

