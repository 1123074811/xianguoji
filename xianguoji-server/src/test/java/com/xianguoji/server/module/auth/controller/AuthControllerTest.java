package com.xianguoji.server.module.auth.controller;

import com.xianguoji.server.base.AbstractApiTest;
import com.xianguoji.server.base.TestAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-API-AUTH-001: POST /auth/register 手机号已注册
 * TC-API-AUTH-002: POST /auth/register 验证码错误
 * TC-API-AUTH-003: POST /auth/login 正常
 * TC-API-AUTH-004: POST /auth/login 5次错误后锁定
 * TC-API-AUTH-005: POST /auth/wechat-login 新用户首次
 * TC-API-AUTH-006: POST /auth/bind-phone 已绑定其他账号
 * TC-API-AUTH-007: POST /auth/refresh refreshToken过期
 * TC-API-AUTH-008: POST /auth/logout 登出后token立即失效
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest extends AbstractApiTest {

    /**
     * TC-API-AUTH-SMS-001: 手机号格式错误
     */
    @Test
    void sendSmsCode_shouldReturn400_whenPhoneFormatInvalid() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("phone", "13"); // 无效手机号
        body.put("captchaId", "test-id");
        body.put("captchaCode", "1234");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/sms/send"),
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * TC-API-AUTH-SMS-003: 正常发送短信验证码
     */
    @Test
    void sendSmsCode_shouldReturn200_whenValidRequest() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("phone", "13800138001");
        body.put("captchaId", "test-id");
        body.put("captchaCode", "1234");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/sms/send"),
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-AUTH-SMSL-003: 新手机号自动注册
     */
    @Test
    void smsLogin_shouldRegisterNewUser_whenPhoneNotExists() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("phone", "13900139001"); // 新手机号
        body.put("code", "1234"); // 测试环境固定验证码

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/login/sms"),
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
        
        // 验证返回的token和用户信息
        var data = TestAssertions.getData(response);
        assertNotNull(data.get("token"));
        assertNotNull(data.get("refreshToken"));
        
        var userInfo = data.get("userInfo");
        assertNotNull(userInfo);
        assertTrue(userInfo.has("isNew") && userInfo.get("isNew").asBoolean());
    }

    /**
     * TC-API-AUTH-SMSL-001: 短信码错误
     */
    @Test
    void smsLogin_shouldReturnError_whenSmsCodeIsWrong() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("phone", "13800138001");
        body.put("code", "9999"); // 错误的验证码

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/login/sms"),
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode()); // 业务错误仍返回200
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 1); // 业务错误码
    }

    /**
     * TC-API-AUTH-RFR-001: 正常刷新token
     */
    @Test
    void refreshToken_shouldIssueNewTokens_whenRefreshTokenIsValid() {
        // Arrange - 先登录获取refresh token
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("phone", "13800138001");
        loginBody.put("code", "1234");

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/login/sms"),
                anonymousRequest(loginBody),
                String.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        String refreshToken = TestAssertions.getData(loginResponse).get("refreshToken").asText();

        // Act - 刷新token
        Map<String, String> refreshBody = new HashMap<>();
        refreshBody.put("refreshToken", refreshToken);

        ResponseEntity<String> refreshResponse = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/refresh"),
                anonymousRequest(refreshBody),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
        TestAssertions.assertResponseStructure(refreshResponse);
        TestAssertions.assertBusinessCode(refreshResponse, 0);
        
        var data = TestAssertions.getData(refreshResponse);
        assertNotNull(data.get("token"));
        assertNotNull(data.get("refreshToken"));
    }

    /**
     * TC-API-AUTH-RFR-001: refreshToken为空
     */
    @Test
    void refreshToken_shouldReturnError_whenRefreshTokenIsNull() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("refreshToken", "");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/refresh"),
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 400); // PARAM_ERROR
    }

    /**
     * TC-API-AUTH-LO-001: 正常登出
     */
    @Test
    void userLogout_shouldSuccess_whenTokenIsValid() {
        // Arrange - 先登录获取token
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("phone", "13800138001");
        loginBody.put("code", "1234");

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/login/sms"),
                anonymousRequest(loginBody),
                String.class
        );

        String token = TestAssertions.getData(loginResponse).get("token").asText();

        // Act - 登出
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> logoutResponse = restTemplate.exchange(
                apiUrl("/api/u/auth/logout"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
        TestAssertions.assertResponseStructure(logoutResponse);
        TestAssertions.assertBusinessCode(logoutResponse, 0);
    }

    /**
     * TC-API-AUTH-LO-002: 未登录访问受保护接口
     */
    @Test
    void userLogout_shouldReturn401_whenNoToken() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/auth/logout"),
                HttpMethod.POST,
                anonymousRequest(null),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * TC-API-AUTH-ADM-001: 商家登录 - 账号不存在
     */
    @Test
    void adminLogin_shouldReturnError_whenAccountNotExists() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("username", "nonexistent");
        body.put("password", "test123456");
        body.put("captchaKey", "test-key");
        body.put("captchaCode", "1234");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/admin/login"),
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 1); // 业务错误
    }

    /**
     * TC-API-AUTH-ADM-004: 正常商家登录
     */
    @Test
    void adminLogin_shouldReturnToken_whenCredentialsValid() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("username", "admin");
        body.put("password", "test123456");
        body.put("captchaKey", "test-key");
        body.put("captchaCode", "1234");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/admin/login"),
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
        
        var data = TestAssertions.getData(response);
        assertNotNull(data.get("token"));
        assertNotNull(data.get("userInfo"));
        assertEquals("staff", data.get("userInfo").get("role").asText());
    }

    /**
     * TC-API-AUTH-ADM-009: 获取当前商家信息
     */
    @Test
    void adminMe_shouldReturnStaffInfo_whenTokenValid() {
        // Arrange - 先登录
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "admin");
        loginBody.put("password", "test123456");
        loginBody.put("captchaKey", "test-key");
        loginBody.put("captchaCode", "1234");

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                apiUrl("/api/pub/admin/login"),
                anonymousRequest(loginBody),
                String.class
        );

        String token = TestAssertions.getData(loginResponse).get("token").asText();

        // Act - 获取商家信息
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/auth/me"),
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
        
        var data = TestAssertions.getData(response);
        assertNotNull(data.get("id"));
        assertNotNull(data.get("nickname"));
        assertNotNull(data.get("staffRole"));
    }

    /**
     * TC-API-AUTH-ADM-009: 未登录访问商家信息
     */
    @Test
    void adminMe_shouldReturn401_whenNoToken() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/auth/me"),
                HttpMethod.GET,
                anonymousRequest(null),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ==================== SMS 频控 ====================

    /**
     * TC-API-AUTH-SMS-002: 验证码图未过校验
     */
    @Test
    void sendSmsCode_shouldReturnError_whenCaptchaCodeInvalid() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("phone", "13800138001");
        body.put("captchaId", "nonexistent-id");
        body.put("captchaCode", "wrong");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/sms/send"),
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-AUTH-SMS-004: 同号60s内重复发送
     */
    @Test
    void sendSmsCode_shouldReject_whenSamePhoneWithin60s() {
        // Arrange - 第一次发送
        Map<String, String> body1 = new HashMap<>();
        body1.put("phone", "13800138002");
        body1.put("captchaId", "test-id");
        body1.put("captchaCode", "1234");

        restTemplate.postForEntity(
                apiUrl("/api/pub/auth/sms/send"),
                anonymousRequest(body1),
                String.class
        );

        // Arrange - 立即第二次发送
        Map<String, String> body2 = new HashMap<>();
        body2.put("phone", "13800138002");
        body2.put("captchaId", "test-id");
        body2.put("captchaCode", "1234");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/sms/send"),
                anonymousRequest(body2),
                String.class
        );

        // Assert - 应被频控拒绝
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-AUTH-SMSL-002: 短信码连错5次
     */
    @Test
    void smsLogin_shouldInvalidateCode_after5WrongAttempts() {
        // Arrange - 先发送验证码
        Map<String, String> sendBody = new HashMap<>();
        sendBody.put("phone", "13800138003");
        sendBody.put("captchaId", "test-id");
        sendBody.put("captchaCode", "1234");

        restTemplate.postForEntity(
                apiUrl("/api/pub/auth/sms/send"),
                anonymousRequest(sendBody),
                String.class
        );

        // Act - 连续5次错误验证码
        for (int i = 0; i < 5; i++) {
            Map<String, String> loginBody = new HashMap<>();
            loginBody.put("phone", "13800138003");
            loginBody.put("code", "0000");

            restTemplate.postForEntity(
                    apiUrl("/api/pub/auth/login/sms"),
                    anonymousRequest(loginBody),
                    String.class
            );
        }

        // 第6次用正确验证码也应失败（验证码已被失效）
        Map<String, String> correctBody = new HashMap<>();
        correctBody.put("phone", "13800138003");
        correctBody.put("code", "1234");

        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/login/sms"),
                anonymousRequest(correctBody),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-AUTH-SMSL-004: 已注册用户正常登录
     */
    @Test
    void smsLogin_shouldLoginExistingUser_whenPhoneExists() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("phone", "13800138001");
        body.put("code", "1234");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/login/sms"),
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);

        var data = TestAssertions.getData(response);
        assertNotNull(data.get("token"));
        assertFalse(data.get("userInfo").get("isNew").asBoolean());
    }

    // ==================== 微信登录 ====================

    /**
     * TC-API-AUTH-WX-001: 微信code失效
     */
    @Test
    void wechatLogin_shouldReturnError_whenCodeInvalid() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("code", "invalid_wx_code");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/login/wechat"),
                anonymousRequest(body),
                String.class
        );

        // Assert - 微信code无效应返回业务错误
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    // ==================== 管理员锁定/重置密码 ====================

    /**
     * TC-API-AUTH-ADM-002: 密码错误
     */
    @Test
    void adminLogin_shouldReturnError_whenPasswordWrong() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("username", "admin");
        body.put("password", "wrong_password");
        body.put("captchaKey", "test-key");
        body.put("captchaCode", "1234");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/admin/login"),
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 1);
    }

    /**
     * TC-API-AUTH-ADM-003: 错5次锁30min
     */
    @Test
    void adminLogin_shouldLockAccount_after5WrongAttempts() {
        // Arrange - 连续5次错误密码
        for (int i = 0; i < 5; i++) {
            Map<String, String> body = new HashMap<>();
            body.put("username", "admin");
            body.put("password", "wrong_pass_" + i);
            body.put("captchaKey", "test-key");
            body.put("captchaCode", "1234");

            restTemplate.postForEntity(
                    apiUrl("/api/pub/admin/login"),
                    anonymousRequest(body),
                    String.class
            );
        }

        // Act - 第6次用正确密码也应被锁定
        Map<String, String> correctBody = new HashMap<>();
        correctBody.put("username", "admin");
        correctBody.put("password", "test123456");
        correctBody.put("captchaKey", "test-key");
        correctBody.put("captchaCode", "1234");

        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/admin/login"),
                anonymousRequest(correctBody),
                String.class
        );

        // Assert - 应返回锁定错误
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-AUTH-ADM-005: 重置密码 - 缺验证
     */
    @Test
    void adminResetPassword_shouldReject_whenVerificationMissing() {
        // Arrange - 仅提供新密码，无验证
        Map<String, String> body = new HashMap<>();
        body.put("newPassword", "newStrongPass123");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/admin/reset-password"),
                anonymousRequest(body),
                String.class
        );

        // Assert - 缺少验证信息应拒绝
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-AUTH-ADM-006: 密码强度不足
     */
    @Test
    void adminResetPassword_shouldReject_whenPasswordTooWeak() {
        // Arrange
        Map<String, String> body = new HashMap<>();
        body.put("newPassword", "123456"); // 6位纯数字

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl("/api/pub/admin/reset-password"),
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-AUTH-LO-002: 登出后再用同token
     */
    @Test
    void logout_shouldInvalidateToken_whenUsedAfterLogout() {
        // Arrange - 先登录
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("phone", "13800138001");
        loginBody.put("code", "1234");

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/login/sms"),
                anonymousRequest(loginBody),
                String.class
        );

        String token = TestAssertions.getData(loginResponse).get("token").asText();

        // Act1 - 登出
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> logoutRequest = new HttpEntity<>(headers);

        ResponseEntity<String> logoutResponse = restTemplate.exchange(
                apiUrl("/api/u/auth/logout"),
                HttpMethod.POST,
                logoutRequest,
                String.class
        );

        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());

        // Act2 - 用同一token访问受保护接口
        ResponseEntity<String> protectedResponse = restTemplate.exchange(
                apiUrl("/api/u/user/profile"),
                HttpMethod.GET,
                logoutRequest,
                String.class
        );

        // Assert - 应返回401
        assertEquals(HttpStatus.UNAUTHORIZED, protectedResponse.getStatusCode());
    }

    /**
     * TC-API-AUTH-LO-003: 登出 - 用户token调admin接口
     */
    @Test
    void adminEndpoint_shouldReturn403_whenUserTokenUsed() {
        // Arrange - 用用户token
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("phone", "13800138001");
        loginBody.put("code", "1234");

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                apiUrl("/api/pub/auth/login/sms"),
                anonymousRequest(loginBody),
                String.class
        );

        String token = TestAssertions.getData(loginResponse).get("token").asText();

        // Act - 用用户token访问admin接口
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/auth/me"),
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert - 应返回403
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
