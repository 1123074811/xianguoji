package com.xianguoji.server.base;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试认证辅助类 - 提供登录夹具
 */
public class TestAuthHelper {

    /**
     * 模拟用户短信登录并返回token
     */
    public static String loginBySms(TestRestTemplate restTemplate, String baseUrl, String phone, String smsCode) {
        String url = baseUrl + "/api/pub/auth/login/sms";
        
        Map<String, String> body = new HashMap<>();
        body.put("phone", phone);
        body.put("smsCode", smsCode);
        body.put("captchaId", "test-captcha-id");
        body.put("captchaCode", "1234");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode data = TestAssertions.getData(response);
                if (data.has("token")) {
                    return data.get("token").asText();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract token from response", e);
            }
        }
        
        throw new RuntimeException("Login failed with status: " + response.getStatusCode());
    }

    /**
     * 模拟商家登录并返回token
     */
    public static String loginAsAdmin(TestRestTemplate restTemplate, String baseUrl, String username, String password) {
        String url = baseUrl + "/api/pub/admin/login";
        
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode data = TestAssertions.getData(response);
                if (data.has("token")) {
                    return data.get("token").asText();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract token from response", e);
            }
        }
        
        throw new RuntimeException("Admin login failed with status: " + response.getStatusCode());
    }

    /**
     * 创建测试用户token（简化版，实际应调用真实接口）
     */
    public static String createTestUserToken(Long userId) {
        // 这里应该调用真实的JWT工具生成token
        // 暂时返回一个模拟token
        return "Bearer mock_user_token_" + userId;
    }

    /**
     * 创建测试商家token（简化版，实际应调用真实接口）
     */
    public static String createTestAdminToken(Long staffId) {
        // 这里应该调用真实的JWT工具生成token
        // 暂时返回一个模拟token
        return "Bearer mock_admin_token_" + staffId;
    }
}
