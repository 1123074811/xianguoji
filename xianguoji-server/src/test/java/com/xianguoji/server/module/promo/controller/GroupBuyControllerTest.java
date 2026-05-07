package com.xianguoji.server.module.promo.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.promo.dto.GroupBuyLaunchDto;
import com.xianguoji.server.module.promo.dto.GroupBuyJoinDto;
import com.xianguoji.server.module.promo.service.GroupBuyService;
import com.xianguoji.server.module.promo.vo.GroupBuyActivityVO;
import com.xianguoji.server.module.promo.vo.GroupBuyInstanceVO;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-PROMO-GB series: GroupBuy controller API tests
 */
@DisplayName("GroupBuy Controller API Tests")
class GroupBuyControllerTest extends AbstractApiTest {

    @MockBean
    private GroupBuyService groupBuyService;

    private String userToken = "test_user_token";

    @Nested
    @DisplayName("GET /api/pub/group-buy/page")
    class GroupBuyPageTests {

        @Test
        @DisplayName("TC-API-PROMO-GB-001: Should return group buy activities page")
        void page_shouldReturnPage_success() throws Exception {
            // Arrange
            List<GroupBuyActivityVO> activities = new ArrayList<>();
            GroupBuyActivityVO activity = GroupBuyActivityVO.builder()
                    .id(1L)
                    .productId(1L)
                    .productName("Apple")
                    .groupPrice(new BigDecimal("9.90"))
                    .groupSize(2)
                    .status(1)
                    .build();
            activities.add(activity);
            
            PageVO<GroupBuyActivityVO> pageVO = new PageVO<>(1L, activities, 1, 20);
            when(groupBuyService.getGroupBuyPage(1, 20)).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/pub/group-buy/page?page=1&size=20")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(groupBuyService).getGroupBuyPage(1, 20);
        }

        @Test
        @DisplayName("TC-API-PROMO-GB-002: Should return empty page when no activities")
        void page_shouldReturnEmptyPage_whenNoActivities() throws Exception {
            // Arrange
            PageVO<GroupBuyActivityVO> pageVO = new PageVO<>(0L, new ArrayList<>(), 1, 20);
            when(groupBuyService.getGroupBuyPage(1, 20)).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/pub/group-buy/page?page=1&size=20")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/pub/group-buy/by-product/{productId}")
    class ByProductTests {

        @Test
        @DisplayName("TC-API-PROMO-GB-003: Should return activity by product")
        void byProduct_shouldReturnActivity_success() throws Exception {
            // Arrange
            Long productId = 1L;
            GroupBuyActivityVO activity = GroupBuyActivityVO.builder()
                    .id(1L)
                    .productId(productId)
                    .productName("Apple")
                    .groupPrice(new BigDecimal("9.90"))
                    .groupSize(2)
                    .status(1)
                    .build();
            
            when(groupBuyService.getActivityByProduct(productId)).thenReturn(activity);

            // Act
            MvcResult result = performGet("/api/pub/group-buy/by-product/" + productId)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(groupBuyService).getActivityByProduct(productId);
        }

        @Test
        @DisplayName("TC-API-PROMO-GB-004: Should return null when no activity found")
        void byProduct_shouldReturnNull_whenNoActivity() throws Exception {
            // Arrange
            Long productId = 999L;
            when(groupBuyService.getActivityByProduct(productId)).thenReturn(null);

            // Act
            MvcResult result = performGet("/api/pub/group-buy/by-product/" + productId)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }
    }

    @Nested
    @DisplayName("GET /api/pub/group-buy/share/{shareCode}")
    class ByShareCodeTests {

        @Test
        @DisplayName("TC-API-PROMO-GB-005: Should return instance by share code")
        void byShareCode_shouldReturnInstance_success() throws Exception {
            // Arrange
            String shareCode = "ABC12345";
            GroupBuyInstanceVO instance = GroupBuyInstanceVO.builder()
                    .id(1L)
                    .shareCode(shareCode)
                    .currentSize(1)
                    .targetSize(2)
                    .status(1)
                    .build();
            
            when(groupBuyService.getInstanceByShareCode(shareCode)).thenReturn(instance);

            // Act
            MvcResult result = performGet("/api/pub/group-buy/share/" + shareCode)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(groupBuyService).getInstanceByShareCode(shareCode);
        }

        @Test
        @DisplayName("TC-API-PROMO-GB-006: Should return error when share code invalid")
        void byShareCode_shouldReturnError_whenShareCodeInvalid() throws Exception {
            // Arrange
            String shareCode = "INVALID";
            when(groupBuyService.getInstanceByShareCode(shareCode))
                    .thenThrow(new RuntimeException("拼团不存在"));

            // Act
            MvcResult result = performGet("/api/pub/group-buy/share/" + shareCode)
                    .andReturn();

            // Assert
            assertEquals(500, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("POST /api/u/group-buy/launch")
    class LaunchTests {

        @Test
        @DisplayName("TC-API-PROMO-GB-007: Should launch group buy successfully")
        void launch_shouldLaunch_success() throws Exception {
            // Arrange
            GroupBuyLaunchDto dto = new GroupBuyLaunchDto();
            dto.setActivityId(1L);
            dto.setAddressId(1L);
            dto.setPayMethod("wechat");
            
            Long instanceId = 1L;
            GroupBuyInstanceVO instance = GroupBuyInstanceVO.builder()
                    .id(instanceId)
                    .shareCode("ABC12345")
                    .build();
            
            when(groupBuyService.launch(anyLong(), any(GroupBuyLaunchDto.class))).thenReturn(instanceId);
            when(groupBuyService.getInstanceDetail(instanceId)).thenReturn(instance);

            // Act
            MvcResult result = performPost("/api/u/group-buy/launch", dto, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(groupBuyService).launch(anyLong(), any(GroupBuyLaunchDto.class));
        }

        @Test
        @DisplayName("TC-API-PROMO-GB-008: Should return 401 when not logged in")
        void launch_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Arrange
            GroupBuyLaunchDto dto = new GroupBuyLaunchDto();

            // Act
            MvcResult result = performPost("/api/u/group-buy/launch", dto)
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("POST /api/u/group-buy/{instanceId}/join")
    class JoinTests {

        @Test
        @DisplayName("TC-API-PROMO-GB-009: Should join group buy successfully")
        void join_shouldJoin_success() throws Exception {
            // Arrange
            Long instanceId = 1L;
            GroupBuyJoinDto dto = new GroupBuyJoinDto();
            dto.setAddressId(1L);
            dto.setPayMethod("wechat");
            
            doNothing().when(groupBuyService).join(anyLong(), eq(instanceId), any(GroupBuyJoinDto.class));

            // Act
            MvcResult result = performPost("/api/u/group-buy/" + instanceId + "/join", dto, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(groupBuyService).join(anyLong(), eq(instanceId), any(GroupBuyJoinDto.class));
        }

        @Test
        @DisplayName("TC-API-PROMO-GB-010: Should return 401 when not logged in")
        void join_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Arrange
            Long instanceId = 1L;
            GroupBuyJoinDto dto = new GroupBuyJoinDto();

            // Act
            MvcResult result = performPost("/api/u/group-buy/" + instanceId + "/join", dto)
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/u/group-buy/{instanceId}")
    class DetailTests {

        @Test
        @DisplayName("TC-API-PROMO-GB-011: Should return instance detail")
        void detail_shouldReturnDetail_success() throws Exception {
            // Arrange
            Long instanceId = 1L;
            GroupBuyInstanceVO instance = GroupBuyInstanceVO.builder()
                    .id(instanceId)
                    .currentSize(1)
                    .targetSize(2)
                    .status(1)
                    .shareCode("ABC12345")
                    .expireAt(LocalDateTime.now().plusHours(24))
                    .build();
            
            when(groupBuyService.getInstanceDetail(instanceId)).thenReturn(instance);

            // Act
            MvcResult result = performGet("/api/u/group-buy/" + instanceId, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(groupBuyService).getInstanceDetail(instanceId);
        }

        @Test
        @DisplayName("TC-API-PROMO-GB-012: Should return 401 when not logged in")
        void detail_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Arrange
            Long instanceId = 1L;

            // Act
            MvcResult result = performGet("/api/u/group-buy/" + instanceId)
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }
}

