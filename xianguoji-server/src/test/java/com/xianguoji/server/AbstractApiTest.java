package com.xianguoji.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xianguoji.server.common.security.LoginContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base class for API integration tests.
 * Uses @SpringBootTest with RANDOM_PORT and MockMvc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractApiTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    public void clearLoginContext() {
        LoginContext.clear();
    }

    /**
     * Helper method to perform GET request with JWT token.
     */
    protected ResultActions performGet(String url, String token) throws Exception {
        MockHttpServletRequestBuilder request = get(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);
        return mockMvc.perform(request);
    }

    /**
     * Helper method to perform GET request without token (public endpoint).
     */
    protected ResultActions performGet(String url) throws Exception {
        MockHttpServletRequestBuilder request = get(url)
                .contentType(MediaType.APPLICATION_JSON);
        return mockMvc.perform(request);
    }

    /**
     * Helper method to perform POST request with JWT token.
     */
    protected ResultActions performPost(String url, Object body, String token) throws Exception {
        MockHttpServletRequestBuilder request = post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        return mockMvc.perform(request);
    }

    /**
     * Helper method to perform POST request without token (public endpoint).
     */
    protected ResultActions performPost(String url, Object body) throws Exception {
        MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        return mockMvc.perform(request);
    }

    /**
     * Helper method to perform PUT request with JWT token.
     */
    protected ResultActions performPut(String url, Object body, String token) throws Exception {
        MockHttpServletRequestBuilder request = put(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        return mockMvc.perform(request);
    }

    protected ResultActions performPut(String url, Object body) throws Exception {
        MockHttpServletRequestBuilder request = put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        return mockMvc.perform(request);
    }

    /**
     * Helper method to perform DELETE request with JWT token.
     */
    protected ResultActions performDelete(String url, String token) throws Exception {
        MockHttpServletRequestBuilder request = delete(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);
        return mockMvc.perform(request);
    }

    /**
     * Extract response body as string.
     */
    protected String getResponseBody(MvcResult result) throws Exception {
        MockHttpServletResponse response = result.getResponse();
        return response.getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Extract response body as specified type.
     */
    protected <T> T getResponseBody(MvcResult result, Class<T> clazz) throws Exception {
        String body = getResponseBody(result);
        return objectMapper.readValue(body, clazz);
    }
}
