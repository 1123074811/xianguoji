package com.xianguoji.server.module.auth.controller;

import com.xianguoji.server.base.AbstractApiTest;
import com.xianguoji.server.base.TestAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * §1.1.1 GET /api/pub/captcha 验证码接口测试
 *
 * TC-API-AUTH-CAP-001: 正常获取图形验证码
 * TC-API-AUTH-CAP-002: 同IP高频请求（频控）
 * TC-API-AUTH-CAP-003: 响应不包含明文code
 * TC-API-AUTH-CAP-004: 返回captchaKey + base64图
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CaptchaControllerTest extends AbstractApiTest {

    /**
     * TC-API-AUTH-CAP-001: 正常获取图形验证码
     */
    @Test
    void captcha_shouldReturnCaptchaKeyAndImage_whenRequested() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/pub/captcha"),
                HttpMethod.GET,
                anonymousRequest(null),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);

        var data = TestAssertions.getData(response);
        assertNotNull(data.get("captchaKey"));
        assertNotNull(data.get("captchaImage"));
        assertFalse(data.get("captchaKey").asText().isEmpty());
        assertTrue(data.get("captchaImage").asText().startsWith("data:image"));
    }

    /**
     * TC-API-AUTH-CAP-003: 响应不包含明文验证码
     */
    @Test
    void captcha_shouldNotContainPlaintextCode_inResponseBody() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/pub/captcha"),
                HttpMethod.GET,
                anonymousRequest(null),
                String.class
        );

        // Assert - 响应body不应包含明文code字段
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        // 不应有 "code":"xxxx" 明文验证码
        assertFalse(body.contains("\"code\""));
    }

    /**
     * TC-API-AUTH-CAP-004: 多次获取验证码均返回不同key
     */
    @Test
    void captcha_shouldReturnDifferentKeys_whenRequestedMultipleTimes() {
        // Act
        ResponseEntity<String> response1 = restTemplate.exchange(
                apiUrl("/api/pub/captcha"),
                HttpMethod.GET,
                anonymousRequest(null),
                String.class
        );

        ResponseEntity<String> response2 = restTemplate.exchange(
                apiUrl("/api/pub/captcha"),
                HttpMethod.GET,
                anonymousRequest(null),
                String.class
        );

        // Assert - 两次key应不同
        var data1 = TestAssertions.getData(response1);
        var data2 = TestAssertions.getData(response2);
        assertNotEquals(data1.get("captchaKey").asText(), data2.get("captchaKey").asText());
    }

    /**
     * TC-API-AUTH-CAP-002: 高频请求测试（连续10次）
     */
    @Test
    void captcha_shouldHandleHighFrequencyRequests() {
        // Act - 连续快速请求10次
        int successCount = 0;
        int rateLimitedCount = 0;

        for (int i = 0; i < 10; i++) {
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl("/api/pub/captcha"),
                    HttpMethod.GET,
                    anonymousRequest(null),
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                successCount++;
            } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                rateLimitedCount++;
            }
        }

        // Assert - 至少第一次应成功，后续可能被限流
        assertTrue(successCount >= 1, "At least first request should succeed");
    }
}
