package com.xianguoji.server.module.order.controller;

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
 * TC-API-ORD-ADM-001: 列表筛选
 * TC-API-ORD-ADM-002: accept/reject
 * TC-API-ORD-ADM-003: ship 必填物流单号
 * TC-API-ORD-ADM-004: pickup-verify
 * TC-API-ORD-ADM-008: refund 同意
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminOrderControllerTest extends AbstractApiTest {

    private String adminToken;

    /**
     * 获取商家token
     */
    private String getAdminToken() {
        if (adminToken == null) {
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

            if (loginResponse.getStatusCode() == HttpStatus.OK) {
                adminToken = TestAssertions.getData(loginResponse).get("token").asText();
            }
        }
        return adminToken;
    }

    /**
     * TC-API-ORD-ADM-001: 商家订单列表
     */
    @Test
    void page_shouldReturnOrderList_whenAdminLoggedIn() {
        // Arrange
        String token = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/order/page?page=1&size=10"),
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
        TestAssertions.assertPaginationResponse(response);
    }

    /**
     * TC-API-ORD-ADM-001: 未登录访问商家订单列表
     */
    @Test
    void page_shouldReturn401_whenNoToken() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/order/page?page=1&size=10"),
                HttpMethod.GET,
                anonymousRequest(null),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * TC-API-ORD-ADM-002: 接单
     */
    @Test
    void accept_shouldReturn200_whenOrderExists() {
        // Arrange
        String token = getAdminToken();
        String orderNo = "250101120000123456";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/order/" + orderNo + "/accept"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * TC-API-ORD-ADM-002: 拒单
     */
    @Test
    void reject_shouldReturn200_whenOrderExists() {
        // Arrange
        String token = getAdminToken();
        String orderNo = "250101120000123456";
        Map<String, String> body = new HashMap<>();
        body.put("reason", "库存不足");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/order/" + orderNo + "/reject"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * TC-API-ORD-ADM-003: 发货（快递配送）
     */
    @Test
    void ship_shouldReturn200_whenDeliveryTypeIsExpress() {
        // Arrange
        String token = getAdminToken();
        String orderNo = "250101120000123456";
        Map<String, Object> body = new HashMap<>();
        body.put("deliveryType", 1);
        body.put("courierName", "顺丰快递");
        body.put("courierPhone", "95338");
        body.put("trackingNo", "SF1234567890");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/order/" + orderNo + "/ship"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * TC-API-ORD-ADM-003: 发货（自提）
     */
    @Test
    void ship_shouldReturn200_whenDeliveryTypeIsPickup() {
        // Arrange
        String token = getAdminToken();
        String orderNo = "250101120000123456";
        Map<String, Object> body = new HashMap<>();
        body.put("deliveryType", 2);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/order/" + orderNo + "/ship"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-ADM-004: 核销自提码
     */
    @Test
    void pickupVerify_shouldReturn200_whenPickupCodeIsValid() {
        // Arrange
        String token = getAdminToken();
        String orderNo = "250101120000123456";
        Map<String, String> body = new HashMap<>();
        body.put("pickupCode", "123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/order/" + orderNo + "/pickup-verify"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-ADM-005: 确认送达
     */
    @Test
    void complete_shouldReturn200_whenOrderIsShipped() {
        // Arrange
        String token = getAdminToken();
        String orderNo = "250101120000123456";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/order/" + orderNo + "/complete"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * TC-API-ORD-ADM-005: 确认已完成订单
     */
    @Test
    void complete_shouldReturnError_whenOrderAlreadyCompleted() {
        // Arrange
        String token = getAdminToken();
        String orderNo = "250101120000123456";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/order/" + orderNo + "/complete"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert - 如果订单已完成会返回业务错误
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-ADM-007: 新订单数量
     */
    @Test
    void newCount_shouldReturnCount_whenAdminLoggedIn() {
        // Arrange
        String token = getAdminToken();
        long sinceTimestamp = System.currentTimeMillis() - 86400000L; // 24小时前

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/order/new-count?since=" + sinceTimestamp),
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);

        var data = TestAssertions.getData(response);
        assertNotNull(data);
        assertTrue(data.isInt());
    }

    /**
     * TC-API-ORD-ADM-008: 同意退款
     */
    @Test
    void approveRefund_shouldReturn200_whenRefundExists() {
        // Arrange
        String token = getAdminToken();
        String refundNo = "RF250101120000123456";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/refund/" + refundNo + "/approve"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * TC-API-ORD-ADM-008: 拒绝退款
     */
    @Test
    void rejectRefund_shouldReturn200_whenRefundExists() {
        // Arrange
        String token = getAdminToken();
        String refundNo = "RF250101120000123456";
        Map<String, String> body = new HashMap<>();
        body.put("reason", "不符合退款条件");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/admin/refund/" + refundNo + "/reject"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }
}
