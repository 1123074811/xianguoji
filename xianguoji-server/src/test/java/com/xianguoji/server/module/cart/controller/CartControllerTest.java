package com.xianguoji.server.module.cart.controller;

import com.xianguoji.server.base.AbstractApiTest;
import com.xianguoji.server.base.TestAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-API-CART-001: GET /cart 空购物车
 * TC-API-CART-002: POST /cart 正常添加
 * TC-API-CART-003: DELETE /cart 删除非己
 * TC-API-CART-004: PUT /cart/items/{id} 改数量超限
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CartControllerTest extends AbstractApiTest {

    private String userToken;

    /**
     * 获取用户token的辅助方法
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
     * TC-API-CART-001: 空购物车
     */
    @Test
    void getCartList_shouldReturnEmptyCart_whenUserHasNoItems() {
        // Arrange
        String token = getUserToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/cart/list"),
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);

        var data = TestAssertions.getData(response);
        assertNotNull(data.get("items"));
        assertEquals(BigDecimal.ZERO, data.get("totalAmount").decimalValue());
    }

    /**
     * TC-API-CART-002: 正常添加到购物车
     */
    @Test
    void addToCart_shouldReturn200_whenRequestValid() {
        // Arrange
        String token = getUserToken();
        Map<String, Object> body = new HashMap<>();
        body.put("skuId", 1L);
        body.put("quantity", 2);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/cart"),
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
     * TC-API-CART-002: 未登录添加到购物车
     */
    @Test
    void addToCart_shouldReturn401_whenNoToken() {
        // Arrange
        Map<String, Object> body = new HashMap<>();
        body.put("skuId", 1L);
        body.put("quantity", 2);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/cart"),
                HttpMethod.POST,
                anonymousRequest(body),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * TC-API-CART-002: SKU不存在
     */
    @Test
    void addToCart_shouldReturnError_whenSkuNotExists() {
        // Arrange
        String token = getUserToken();
        Map<String, Object> body = new HashMap<>();
        body.put("skuId", 99999L);
        body.put("quantity", 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/cart"),
                HttpMethod.POST,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 1); // 业务错误码
    }

    /**
     * 测试更新数量
     */
    @Test
    void updateQuantity_shouldReturn200_whenRequestValid() {
        // Arrange
        String token = getUserToken();
        Long cartId = 1L;
        Map<String, Object> body = new HashMap<>();
        body.put("quantity", 5);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/cart/" + cartId + "/quantity"),
                HttpMethod.PUT,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * 测试更新选中状态
     */
    @Test
    void updateSelected_shouldReturn200_whenRequestValid() {
        // Arrange
        String token = getUserToken();
        Map<String, Object> body = new HashMap<>();
        body.put("ids", java.util.List.of(1L, 2L, 3L));
        body.put("selected", 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/cart/selected"),
                HttpMethod.PUT,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * TC-API-CART-003: 删除购物车项
     */
    @Test
    void deleteItem_shouldReturn200_whenItemBelongsToUser() {
        // Arrange
        String token = getUserToken();
        Long cartId = 1L;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/cart/" + cartId),
                HttpMethod.DELETE,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * 测试清空购物车
     */
    @Test
    void clearCart_shouldReturn200_whenUserHasItems() {
        // Arrange
        String token = getUserToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/cart/clear"),
                HttpMethod.DELETE,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * TC-API-CART-CNT-001: 获取购物车数量
     */
    @Test
    void getCartCount_shouldReturnCount_whenUserHasItems() {
        // Arrange
        String token = getUserToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/cart/count"),
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
     * TC-API-CART-CNT-001: 未登录获取购物车数量
     */
    @Test
    void getCartCount_shouldReturn401_whenNoToken() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/cart/count"),
                HttpMethod.GET,
                anonymousRequest(null),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
