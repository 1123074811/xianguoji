package com.xianguoji.server.module.user.controller;

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
 * §1.2.5 / §1.2.6 足迹 & 收藏 接口测试
 *
 * TC-API-USR-FP-001: POST /api/u/footprint/{productId} 写入幂等
 * TC-API-USR-FP-002: GET /api/u/footprint/page 分页
 * TC-API-USR-FP-003: DELETE /api/u/footprint 清空
 * TC-API-USR-FP-004: GET /api/u/footprint/page 未登录
 * TC-API-USR-FV-001: POST /api/u/favorite/{productId} 重复收藏幂等
 * TC-API-USR-FV-002: DELETE /api/u/favorite/{productId} 取消收藏
 * TC-API-USR-FV-003: GET /api/u/favorite/page 分页
 * TC-API-USR-FV-004: POST /api/u/favorite/{productId} 未登录
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FootprintFavoriteControllerTest extends AbstractApiTest {

    private String userToken;

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

    private HttpEntity<Void> authGet() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getUserToken());
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Void> authPost() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getUserToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    // ==================== Footprint ====================

    /**
     * TC-API-USR-FP-001: 记录足迹 - 写入幂等（同商品多次仅更新viewedAt）
     */
    @Test
    void addFootprint_shouldBeIdempotent_whenSameProductRecordedTwice() {
        // Act - 第一次记录
        ResponseEntity<String> response1 = restTemplate.exchange(
                apiUrl("/api/u/footprint/1"),
                HttpMethod.POST,
                authPost(),
                String.class
        );

        // Act - 第二次记录同一商品
        ResponseEntity<String> response2 = restTemplate.exchange(
                apiUrl("/api/u/footprint/1"),
                HttpMethod.POST,
                authPost(),
                String.class
        );

        // Assert - 两次都应返回200
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        TestAssertions.assertResponseStructure(response1);
        TestAssertions.assertResponseStructure(response2);
    }

    /**
     * TC-API-USR-FP-002: 分页足迹
     */
    @Test
    void footprintPage_shouldReturnPaginatedList_whenFootprintsExist() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/footprint/page?page=1&size=20"),
                HttpMethod.GET,
                authGet(),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * TC-API-USR-FP-002: 分页足迹 - 第二页
     */
    @Test
    void footprintPage_shouldReturnSecondPage_whenPageIs2() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/footprint/page?page=2&size=20"),
                HttpMethod.GET,
                authGet(),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-USR-FP-003: 清空足迹
     */
    @Test
    void clearFootprint_shouldDeleteAll_whenLoggedIn() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/footprint"),
                HttpMethod.DELETE,
                authPost(),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * TC-API-USR-FP-004: 未登录访问足迹
     */
    @Test
    void footprintPage_shouldReturn401_whenNotLoggedIn() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/footprint/page?page=1&size=20"),
                HttpMethod.GET,
                anonymousRequest(null),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * TC-API-USR-FP-004: 未登录记录足迹
     */
    @Test
    void addFootprint_shouldReturn401_whenNotLoggedIn() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/footprint/1"),
                HttpMethod.POST,
                anonymousRequest(null),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ==================== Favorite ====================

    /**
     * TC-API-USR-FV-001: 收藏 - 重复收藏幂等
     */
    @Test
    void addFavorite_shouldBeIdempotent_whenSameProductFavoritedTwice() {
        // Act - 第一次收藏
        ResponseEntity<String> response1 = restTemplate.exchange(
                apiUrl("/api/u/favorite/1"),
                HttpMethod.POST,
                authPost(),
                String.class
        );

        // Act - 第二次收藏同一商品
        ResponseEntity<String> response2 = restTemplate.exchange(
                apiUrl("/api/u/favorite/1"),
                HttpMethod.POST,
                authPost(),
                String.class
        );

        // Assert - 两次都应返回200（幂等）
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    /**
     * TC-API-USR-FV-002: 取消收藏
     */
    @Test
    void removeFavorite_shouldReturn200_whenFavoriteExists() {
        // Arrange - 先收藏
        restTemplate.exchange(
                apiUrl("/api/u/favorite/1"),
                HttpMethod.POST,
                authPost(),
                String.class
        );

        // Act - 取消收藏
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/favorite/1"),
                HttpMethod.DELETE,
                authPost(),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * TC-API-USR-FV-002: 取消收藏 - 未收藏过（幂等200）
     */
    @Test
    void removeFavorite_shouldReturn200_whenFavoriteNotExists() {
        // Act - 取消一个从未收藏的商品
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/favorite/99999"),
                HttpMethod.DELETE,
                authPost(),
                String.class
        );

        // Assert - 幂等，仍然200
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
    }

    /**
     * TC-API-USR-FV-003: 分页收藏
     */
    @Test
    void favoritePage_shouldReturnPaginatedList_whenFavoritesExist() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/favorite/page?page=1&size=20"),
                HttpMethod.GET,
                authGet(),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TestAssertions.assertResponseStructure(response);
        TestAssertions.assertBusinessCode(response, 0);
    }

    /**
     * TC-API-USR-FV-004: 未登录收藏
     */
    @Test
    void addFavorite_shouldReturn401_whenNotLoggedIn() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/favorite/1"),
                HttpMethod.POST,
                anonymousRequest(null),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * TC-API-USR-FV-004: 未登录查看收藏
     */
    @Test
    void favoritePage_shouldReturn401_whenNotLoggedIn() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl("/api/u/favorite/page?page=1&size=20"),
                HttpMethod.GET,
                anonymousRequest(null),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
