package com.xianguoji.server.common.util;

import com.xianguoji.server.common.exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * §2.1 通用/工具单元测试 - UrlSecurityUtil
 *
 * TC-UT-COMMON-URL-001 ~ 010: SSRF 防御与 URL 白名单校验
 */
@DisplayName("UrlSecurityUtil Unit Tests")
class UrlSecurityUtilTest {

    @Nested
    @DisplayName("validatePublicHttpUrl")
    class ValidatePublicHttpUrlTests {

        @Test
        @DisplayName("TC-UT-COMMON-URL-001: 合法公网 HTTPS URL 应通过")
        void shouldAccept_validPublicHttpsUrl() {
            assertDoesNotThrow(() ->
                    UrlSecurityUtil.validatePublicHttpUrl("https://img.xianguoji.com/product/1.jpg"));
        }

        @Test
        @DisplayName("TC-UT-COMMON-URL-002: 合法公网 HTTP URL 应通过")
        void shouldAccept_validPublicHttpUrl() {
            assertDoesNotThrow(() ->
                    UrlSecurityUtil.validatePublicHttpUrl("http://example.com/image.png"));
        }

        @Test
        @DisplayName("TC-UT-COMMON-URL-003: localhost 应被拒绝")
        void shouldReject_localhost() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl("http://localhost/admin"));
        }

        @Test
        @DisplayName("TC-UT-COMMON-URL-004: 127.0.0.1 应被拒绝")
        void shouldReject_loopbackAddress() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl("http://127.0.0.1/secret"));
        }

        @Test
        @DisplayName("TC-UT-COMMON-URL-005: 192.168.x.x 内网应被拒绝")
        void shouldReject_privateNetwork() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl("http://192.168.1.1/config"));
        }

        @Test
        @DisplayName("TC-UT-COMMON-URL-006: 10.x.x.x 内网应被拒绝")
        void shouldReject_classAPrivateNetwork() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl("http://10.0.0.1/internal"));
        }

        @Test
        @DisplayName("TC-UT-COMMON-URL-007: 敏感端口 Redis(6379) 应被拒绝")
        void shouldReject_sensitivePortRedis() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl("http://example.com:6379/info"));
        }

        @Test
        @DisplayName("TC-UT-COMMON-URL-008: 敏感端口 MySQL(3306) 应被拒绝")
        void shouldReject_sensitivePortMysql() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl("http://example.com:3306/query"));
        }

        @Test
        @DisplayName("TC-UT-COMMON-URL-009: ftp scheme 应被拒绝")
        void shouldReject_nonHttpScheme() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl("ftp://example.com/file"));
        }

        @Test
        @DisplayName("TC-UT-COMMON-URL-010: file scheme 应被拒绝")
        void shouldReject_fileScheme() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl("file:///etc/passwd"));
        }

        @Test
        @DisplayName("含 userinfo 的 URL 应被拒绝")
        void shouldReject_userInfoInUrl() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl("http://admin:pass@example.com/secret"));
        }

        @Test
        @DisplayName(".local 域名应被拒绝")
        void shouldReject_localDomain() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl("http://myserver.local/api"));
        }

        @Test
        @DisplayName("空 URL 应被拒绝")
        void shouldReject_blankUrl() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl(""));
        }

        @Test
        @DisplayName("null URL 应被拒绝")
        void shouldReject_nullUrl() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl(null));
        }

        @Test
        @DisplayName("非法 URL 格式应被拒绝")
        void shouldReject_malformedUrl() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validatePublicHttpUrl("not a valid url"));
        }
    }

    @Nested
    @DisplayName("validateUrlPrefix")
    class ValidateUrlPrefixTests {

        private final Set<String> allowedPrefixes = Set.of(
                "https://img.xianguoji.com/",
                "https://cdn.xianguoji.com/"
        );

        @Test
        @DisplayName("匹配白名单前缀应通过")
        void shouldAccept_whenPrefixMatches() {
            assertDoesNotThrow(() ->
                    UrlSecurityUtil.validateUrlPrefix("https://img.xianguoji.com/avatar.png", allowedPrefixes));
        }

        @Test
        @DisplayName("不匹配白名单前缀应拒绝")
        void shouldReject_whenPrefixNotMatches() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validateUrlPrefix("https://evil.com/malware.jpg", allowedPrefixes));
        }

        @Test
        @DisplayName("空 URL 应拒绝")
        void shouldReject_blankUrl() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validateUrlPrefix("", allowedPrefixes));
        }

        @Test
        @DisplayName("null URL 应拒绝")
        void shouldReject_nullUrl() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validateUrlPrefix(null, allowedPrefixes));
        }

        @Test
        @DisplayName("相似但不同的域名应拒绝")
        void shouldReject_similarButDifferentDomain() {
            assertThrows(BizException.class, () ->
                    UrlSecurityUtil.validateUrlPrefix("https://img.xianguoji.com.evil.com/fake.png", allowedPrefixes));
        }
    }
}
