package com.xianguoji.server.base;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * API测试基类，使用Testcontainers管理MySQL和Redis
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractApiTest {

    @LocalServerPort
    protected int port;

    protected TestRestTemplate restTemplate = new TestRestTemplate();

    // MySQL容器
    @Container
    protected static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("xianguoji_test")
            .withUsername("root")
            .withPassword("test")
            .withInitScript("sql/schema.sql")
            .waitingFor(Wait.forLogMessage(".*ready for connections.*", 1))
            .withStartupTimeout(Duration.ofMinutes(5));

    // Redis容器
    @Container
    protected static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofMinutes(2));

    @BeforeAll
    static void setupProperties() {
        // 设置Testcontainers容器的连接信息到系统属性
        System.setProperty("TEST_DB_URL", mysql.getJdbcUrl());
        System.setProperty("TEST_DB_USER", mysql.getUsername());
        System.setProperty("TEST_DB_PASSWORD", mysql.getPassword());
        System.setProperty("TEST_REDIS_HOST", redis.getHost());
        System.setProperty("TEST_REDIS_PORT", redis.getMappedPort(6379).toString());
    }

    /**
     * 构建带认证头的请求
     */
    protected HttpEntity<String> authenticatedRequest(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return new HttpEntity<>(body != null ? toJson(body) : null, headers);
    }

    /**
     * 构建无认证头的请求
     */
    protected HttpEntity<String> anonymousRequest(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body != null ? toJson(body) : null, headers);
    }

    /**
     * 获取完整的API URL
     */
    protected String apiUrl(String path) {
        return "http://localhost:" + port + path;
    }

    /**
     * 简单的JSON转换（实际项目中建议使用ObjectMapper）
     */
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        // 简化处理，实际应使用ObjectMapper
        return obj.toString();
    }
}
