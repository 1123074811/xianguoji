package com.xianguoji.server.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Utility class for common response assertions.
 * Standard response format: {code, msg, data, traceId}
 */
public class ResponseAssertions {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Assert response is successful (code = 0).
     */
    public static void assertSuccess(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        int code = root.get("code").asInt();
        assertEquals(0, code, "Expected success code 0, got: " + code);
    }

    /**
     * Assert response has specific business code.
     */
    public static void assertCode(String responseBody, int expectedCode) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        int code = root.get("code").asInt();
        assertEquals(expectedCode, code, "Expected code " + expectedCode + ", got: " + code);
    }

    /**
     * Assert HTTP status code.
     */
    public static void assertHttpStatus(int actualStatus, HttpStatus expectedStatus) {
        assertEquals(expectedStatus.value(), actualStatus,
                "Expected HTTP " + expectedStatus + ", got: " + HttpStatus.valueOf(actualStatus));
    }

    /**
     * Assert response contains specific message.
     */
    public static void assertMessageContains(String responseBody, String expectedMessage) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String msg = root.get("msg").asText();
        assertTrue(msg.contains(expectedMessage),
                "Expected message to contain '" + expectedMessage + "', got: " + msg);
    }

    /**
     * Assert data field is not null.
     */
    public static void assertDataNotNull(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode data = root.get("data");
        assertNotNull(data, "Expected data field to be non-null");
    }

    /**
     * Assert data field is null.
     */
    public static void assertDataNull(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode data = root.get("data");
        assertTrue(data == null || data.isNull(), "Expected data field to be null");
    }
}
