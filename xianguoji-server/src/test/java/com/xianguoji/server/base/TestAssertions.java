package com.xianguoji.server.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * 统一断言工具类
 */
public class TestAssertions {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 断言响应结构符合规范
     */
    public static void assertResponseStructure(ResponseEntity<String> response) {
        assert response != null;
        assert response.getBody() != null;
        
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            assert root.has("code");
            assert root.has("msg");
            assert root.has("data");
            assert root.has("traceId");
        } catch (Exception e) {
            throw new AssertionError("Response structure validation failed", e);
        }
    }

    /**
     * 断言业务码
     */
    public static void assertBusinessCode(ResponseEntity<String> response, int expectedCode) {
        assertResponseStructure(response);
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            int actualCode = root.get("code").asInt();
            if (actualCode != expectedCode) {
                throw new AssertionError(String.format(
                    "Expected business code %d but got %d. Message: %s",
                    expectedCode, actualCode, root.get("msg").asText()
                ));
            }
        } catch (Exception e) {
            throw new AssertionError("Business code assertion failed", e);
        }
    }

    /**
     * 断言金额字段格式（应为字符串形式的BigDecimal）
     */
    public static void assertAmountFormat(JsonNode node, String fieldName) {
        assert node.has(fieldName);
        String value = node.get(fieldName).asText();
        try {
            new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new AssertionError(String.format(
                "Field %s should be a valid decimal string but got: %s",
                fieldName, value
            ));
        }
    }

    /**
     * 断言时间字段格式（ISO-8601）
     */
    public static void assertTimeFormat(JsonNode node, String fieldName) {
        assert node.has(fieldName);
        String value = node.get(fieldName).asText();
        try {
            Instant.parse(value);
        } catch (Exception e) {
            throw new AssertionError(String.format(
                "Field %s should be ISO-8601 format but got: %s",
                fieldName, value
            ));
        }
    }

    /**
     * 断言分页响应
     */
    public static void assertPaginationResponse(ResponseEntity<String> response) {
        assertResponseStructure(response);
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");
            assert data.has("records");
            assert data.has("total");
            assert data.has("page");
            assert data.has("size");
        } catch (Exception e) {
            throw new AssertionError("Pagination response structure validation failed", e);
        }
    }

    /**
     * 获取响应中的业务码
     */
    public static int getBusinessCode(ResponseEntity<String> response) {
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get("code").asInt();
        } catch (Exception e) {
            throw new AssertionError("Failed to extract business code", e);
        }
    }

    /**
     * 获取响应中的消息
     */
    public static String getMessage(ResponseEntity<String> response) {
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get("msg").asText();
        } catch (Exception e) {
            throw new AssertionError("Failed to extract message", e);
        }
    }

    /**
     * 获取响应中的数据节点
     */
    public static JsonNode getData(ResponseEntity<String> response) {
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get("data");
        } catch (Exception e) {
            throw new AssertionError("Failed to extract data", e);
        }
    }
}
