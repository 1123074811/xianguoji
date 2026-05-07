package com.xianguoji.server.module.help.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.module.help.entity.HelpFaq;
import com.xianguoji.server.module.help.entity.HelpFeedback;
import com.xianguoji.server.module.help.entity.HelpGuide;
import com.xianguoji.server.module.help.mapper.HelpFaqMapper;
import com.xianguoji.server.module.help.mapper.HelpFeedbackMapper;
import com.xianguoji.server.module.help.mapper.HelpGuideMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-HELP series: Help controller API tests
 */
@DisplayName("Help Controller API Tests")
class HelpControllerTest extends AbstractApiTest {

    @MockBean
    private HelpFaqMapper faqMapper;
    
    @MockBean
    private HelpGuideMapper guideMapper;
    
    @MockBean
    private HelpFeedbackMapper feedbackMapper;

    private String adminToken = "test_admin_token";

    @Nested
    @DisplayName("GET /api/admin/help/faq/list")
    class FaqListTests {

        @Test
        @DisplayName("TC-API-HELP-001: Should return FAQ list")
        void faqList_shouldReturnList_success() throws Exception {
            // Arrange
            List<HelpFaq> faqs = new ArrayList<>();
            HelpFaq faq = createHelpFaq(1L, "订单问题", "如何下单？", "点击商品加入购物车即可下单。");
            faqs.add(faq);
            
            when(faqMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(faqs);

            // Act
            MvcResult result = performGet("/api/admin/help/faq/list", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(faqMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-API-HELP-002: Should return FAQ list with section filter")
        void faqList_shouldReturnListWithSection_success() throws Exception {
            // Arrange
            List<HelpFaq> faqs = new ArrayList<>();
            when(faqMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(faqs);

            // Act
            MvcResult result = performGet("/api/admin/help/faq/list?section=订单问题", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-HELP-003: Should return FAQ list with keyword search")
        void faqList_shouldReturnListWithKeyword_success() throws Exception {
            // Arrange
            List<HelpFaq> faqs = new ArrayList<>();
            when(faqMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(faqs);

            // Act
            MvcResult result = performGet("/api/admin/help/faq/list?keyword=下单", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
        }

        @Test
        @DisplayName("TC-API-HELP-004: Should return 403 when not admin")
        void faqList_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/help/faq/list", "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/help/guide/list")
    class GuideListTests {

        @Test
        @DisplayName("TC-API-HELP-005: Should return guide list")
        void guideList_shouldReturnList_success() throws Exception {
            // Arrange
            List<HelpGuide> guides = new ArrayList<>();
            HelpGuide guide = createHelpGuide(1L, "新手指南", "如何注册账号", "点击注册按钮填写信息即可。");
            guides.add(guide);
            
            when(guideMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(guides);

            // Act
            MvcResult result = performGet("/api/admin/help/guide/list", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(guideMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-API-HELP-006: Should return 403 when not admin")
        void guideList_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/help/guide/list", "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/help/feedback")
    class SubmitFeedbackTests {

        @Test
        @DisplayName("TC-API-HELP-007: Should submit feedback successfully")
        void submitFeedback_shouldSubmit_success() throws Exception {
            // Arrange
            HelpController.FeedbackDto dto = new HelpController.FeedbackDto();
            dto.content = "Test feedback content";
            dto.contact = "test@example.com";
            
            when(feedbackMapper.insert(any(HelpFeedback.class))).thenReturn(1);

            // Act
            MvcResult result = performPost("/api/admin/help/feedback", dto, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(feedbackMapper).insert(any(HelpFeedback.class));
        }

        @Test
        @DisplayName("TC-API-HELP-008: Should return error when content is empty")
        void submitFeedback_shouldReturnError_whenContentEmpty() throws Exception {
            // Arrange
            HelpController.FeedbackDto dto = new HelpController.FeedbackDto();
            dto.content = "";
            dto.contact = "test@example.com";

            // Act
            MvcResult result = performPost("/api/admin/help/feedback", dto, adminToken)
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            assertFalse(response.contains("\"code\":200"));
            assertTrue(response.contains("反馈内容不能为空"));
        }

        @Test
        @DisplayName("TC-API-HELP-009: Should return error when content is null")
        void submitFeedback_shouldReturnError_whenContentNull() throws Exception {
            // Arrange
            HelpController.FeedbackDto dto = new HelpController.FeedbackDto();
            dto.contact = "test@example.com";

            // Act
            MvcResult result = performPost("/api/admin/help/feedback", dto, adminToken)
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            assertFalse(response.contains("\"code\":200"));
        }

        @Test
        @DisplayName("TC-API-HELP-010: Should return 403 when not admin")
        void submitFeedback_shouldReturn403_whenNotAdmin() throws Exception {
            // Arrange
            HelpController.FeedbackDto dto = new HelpController.FeedbackDto();
            dto.content = "Test feedback";

            // Act
            MvcResult result = performPost("/api/admin/help/feedback", dto, "user_token")
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    // Helper methods
    private HelpFaq createHelpFaq(Long id, String section, String question, String answer) {
        HelpFaq faq = new HelpFaq();
        faq.setId(id);
        faq.setSection(section);
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setStatus(1);
        faq.setSort(1);
        faq.setCreatedAt(LocalDateTime.now());
        return faq;
    }

    private HelpGuide createHelpGuide(Long id, String category, String title, String content) {
        HelpGuide guide = new HelpGuide();
        guide.setId(id);
        guide.setTitle(title);
        guide.setIcon(category);
        guide.setUrl(content);
        guide.setSort(1);
        guide.setCreatedAt(LocalDateTime.now());
        return guide;
    }
}

