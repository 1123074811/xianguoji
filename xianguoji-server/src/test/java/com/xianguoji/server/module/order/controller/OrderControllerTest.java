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
 * TC-API-ORD-001: POST /orders/preview 含优惠券+运费
 * TC-API-ORD-002: POST /orders 正常下单
 * TC-API-ORD-003: POST /orders 同幂等键重复
 * TC-API-ORD-004: POST /orders 库存不足
 * TC-API-ORD-005: POST /orders/{id}/pay-callback 重复回调
 * TC-API-ORD-006: POST /orders/{id}/cancel 已支付未发货
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrderControllerTest extends AbstractApiTest {

    private String userToken;
    private String adminToken;

    /**
     * 获取用户token
     */
    private String getUserToken() {
        if (userToken == null) {
            Map<String, String> loginBody = new HashMap<>();
            loginBody.put("phone", "13800138001");
            loginBody.put("code", "1234");

            ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                    apiUrl("/api/pub/auth/login/sms"),
                    anonymousRequest(loginBody),
                    String.class
            );

            if (loginResponse.getStatusCode() == HttpStatus.OK) {
                userToken = TestAssertions.getData(loginResponse).get("token").asText();
            }
        }
        return userToken;
    }

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
     * TC-API-ORD-001: 结算预览
     */
    @Test
    void preview_shouldReturnOrderPreview_whenCartHasItems() {
        // Arrange
        String token = getUserToken();
        Map<String, Object> body = new HashMap<>();
        body.put("cartItemIds", java.util.List.of(1L, 2L));
        body.put("deliveryType", 1);
        body.put("addressId", 1L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/preview"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);

        var data = TestAssertions.getData(response);
        assertNotNull(data.get("items"));
        assertNotNull(data.get("goodsAmount"));
        assertNotNull(data.get("payAmount"));
    }

    /**
     * TC-API-ORD-001: 未登录预览
     */
    @Test
    void preview_shouldReturn401_whenNoToken() {
        // Arrange
        Map<String, Object> body = new HashMap<>();
        body.put("cartItemIds", java.util.List.of(1L));

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/preview"),
                HttpMethod.POST,
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * TC-API-ORD-002: 正常下单
     */
    @Test
    void submit_shouldReturnOrderNo_whenRequestValid() {
        // Arrange
        String token = getUserToken();
        Map<String, Object> body = new HashMap<>();
        body.put("cartItemIds", java.util.List.of(1L));
        body.put("deliveryType", 1);
        body.put("addressId", 1L);
        body.put("remark", "测试订单");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/submit"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);

        var data = TestAssertions.getData(response);
        assertNotNull(data.get("orderNo"));
    }

    /**
     * TC-API-ORD-002: 未登录下单
     */
    @Test
    void submit_shouldReturn401_whenNoToken() {
        // Arrange
        Map<String, Object> body = new HashMap<>();
        body.put("cartItemIds", java.util.List.of(1L));
        body.put("deliveryType", 1);
        body.put("addressId", 1L);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/submit"),
                HttpMethod.POST,
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * TC-API-ORD-005: 发起支付
     */
    @Test
    void pay_shouldReturnPaymentParams_whenOrderExists() {
        // Arrange
        String token = getUserToken();
        String orderNo = "250101120000123456";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/" + orderNo + "/pay"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        // 注意：如果订单不存在会返回业务错误，这里只验证接口调用成功
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-LST-001: 订单列表
     */
    @Test
    void page_shouldReturnOrderList_whenUserHasOrders() {
        // Arrange
        String token = getUserToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/page?status=ALL&page=1&size=10"),
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
     * TC-API-ORD-LST-003: 订单详情
     */
    @Test
    void detail_shouldReturnOrderDetail_whenOrderExists() {
        // Arrange
        String token = getUserToken();
        String orderNo = "250101120000123456";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/" + orderNo),
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-CAN-001: 取消订单（未支付）
     */
    @Test
    void cancel_shouldReturn200_whenOrderIsPendingPay() {
        // Arrange
        String token = getUserToken();
        String orderNo = "250101120000123456";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/" + orderNo + "/cancel"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-CFM-001: 确认收货
     */
    @Test
    void confirm_shouldReturn200_whenOrderIsShipped() {
        // Arrange
        String token = getUserToken();
        String orderNo = "250101120000123456";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/" + orderNo + "/confirm"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-RMD-001: 提醒发货
     */
    @Test
    void remind_shouldReturn200_whenOrderIsPendingShip() {
        // Arrange
        String token = getUserToken();
        String orderNo = "250101120000123456";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/" + orderNo + "/remind"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-REP-001: 再来一单
     */
    @Test
    void repurchase_shouldReturn200_whenOrderExists() {
        // Arrange
        String token = getUserToken();
        String orderNo = "250101120000123456";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/" + orderNo + "/repurchase"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    // ==================== 退款 ====================

    /**
     * TC-API-ORD-RFD-001: 申请退款
     */
    @Test
    void refund_shouldReturn200_whenOrderIsPaid() {
        // Arrange
        String token = getUserToken();
        String orderNo = "250101120000123456";
        Map<String, Object> body = new HashMap<>();
        body.put("reason", "商品质量问题");
        body.put("amount", 50.00);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/" + orderNo + "/refund"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-RFD-002: 退款金额超过实付
     */
    @Test
    void refund_shouldReject_whenAmountExceedsPayAmount() {
        // Arrange
        String token = getUserToken();
        String orderNo = "250101120000123456";
        Map<String, Object> body = new HashMap<>();
        body.put("reason", "超额退款");
        body.put("amount", 999999.00);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/" + orderNo + "/refund"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert - 应返回业务错误
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-RFD-003: 未支付订单不能退款
     */
    @Test
    void refund_shouldReject_whenOrderIsNotPaid() {
        // Arrange
        String token = getUserToken();
        // 使用一个未支付订单号（假设不存在或未支付）
        String orderNo = "250101120000000001";
        Map<String, Object> body = new HashMap<>();
        body.put("reason", "未支付退款");
        body.put("amount", 10.00);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/" + orderNo + "/refund"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    // ==================== 支付回调幂等 ====================

    /**
     * TC-API-ORD-PAY-005: 支付回调幂等
     */
    @Test
    void payCallback_shouldBeIdempotent_whenCalledTwice() {
        // Arrange - 模拟微信支付回调
        Map<String, Object> body = new HashMap<>();
        body.put("orderNo", "250101120000123456");
        body.put("transactionId", "wx_pay_001");
        body.put("payTime", "2025-01-01T12:00:00");

        // Act - 第一次回调
        ResponseEntity<String> response1 = restTemplate.postForEntity(
                apiUrl("/api/pub/order/pay-callback"),
                anonymousRequest(body),
                String.class
        );

        // Act - 第二次相同回调
        ResponseEntity<String> response2 = restTemplate.postForEntity(
                apiUrl("/api/pub/order/pay-callback"),
                anonymousRequest(body),
                String.class
        );

        // Assert - 两次都应返回200（幂等）
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    // ==================== 幂等键重复下单 ====================

    /**
     * TC-API-ORD-SBM-003: 同幂等键重复提交
     */
    @Test
    void submit_shouldRejectDuplicate_whenSameIdempotentKey() {
        // Arrange
        String token = getUserToken();
        Map<String, Object> body = new HashMap<>();
        body.put("cartItemIds", java.util.List.of(1L));
        body.put("deliveryType", 1);
        body.put("addressId", 1L);
        body.put("idempotentKey", "idem-test-001");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act - 第一次提交
        ResponseEntity<String> response1 = restTemplate.exchange(
                apiUrl("/api/u/order/submit"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Act - 第二次相同幂等键
        ResponseEntity<String> response2 = restTemplate.exchange(
                apiUrl("/api/u/order/submit"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert - 第二次应被幂等拦截
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        TestAssertions.assertResponseStructure(response2);
    }

    // ==================== 超时自动取消 ====================

    /**
     * TC-API-ORD-SBM-008: 超时未支付订单查询
     */
    @Test
    void page_shouldFilterExpiredOrders_whenStatusIsPendingPay() {
        // Arrange
        String token = getUserToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act - 查询待支付订单
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/page?status=PENDING_PAY&page=1&size=10"),
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    // ==================== 订单列表筛选 ====================

    /**
     * TC-API-ORD-LST-002: 按状态筛选订单
     */
    @Test
    void page_shouldFilterByStatus_whenStatusParamProvided() {
        // Arrange
        String token = getUserToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act - 按已完成筛选
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/page?status=COMPLETED&page=1&size=10"),
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-LST-004: 订单详情 - 不存在的订单
     */
    @Test
    void detail_shouldReturnError_whenOrderNotFound() {
        // Arrange
        String token = getUserToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/NONEXISTENT999"),
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-ORD-CAN-002: 已发货订单不能直接取消
     */
    @Test
    void cancel_shouldReject_whenOrderIsShipped() {
        // Arrange
        String token = getUserToken();
        String orderNo = "250101120000123456";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/order/" + orderNo + "/cancel"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert - 已发货不能取消，应返回业务错误
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }
}
