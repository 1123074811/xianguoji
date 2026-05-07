package com.xianguoji.server.common.security;

import com.xianguoji.server.common.exception.BizException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-UT-AUTH-001: JwtUtil正常签发并解析
 * TC-UT-AUTH-002: JwtUtil篡改签名
 * TC-UT-AUTH-003: JwtUtil已过期token
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "activeKid", "v1");
        ReflectionTestUtils.setField(jwtUtil, "secretV1", "test_secret_key_32_bytes_long_for_testing");
        ReflectionTestUtils.setField(jwtUtil, "userAccessTtlMinutes", 120);
        ReflectionTestUtils.setField(jwtUtil, "adminAccessTtlMinutes", 30);
        ReflectionTestUtils.setField(jwtUtil, "userRefreshTtlHours", 168);
        ReflectionTestUtils.setField(jwtUtil, "adminRefreshTtlHours", 12);
    }

    /**
     * TC-UT-AUTH-001: 正常签发并解析用户access token
     */
    @Test
    void issueUserAccessToken_shouldGenerateValidToken_whenCalledWithValidParameters() {
        Long uid = 12345L;
        String fingerprint = "test-fingerprint";

        String token = jwtUtil.issueUserAccessToken(uid, fingerprint);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtUtil.parse(token);
        assertEquals(String.valueOf(uid), claims.getSubject());
        assertEquals(uid, claims.get("uid"));
        assertEquals("user", claims.get("role"));
        assertEquals("access", claims.get("type"));
        assertEquals(fingerprint, claims.get("fp"));
        assertNotNull(claims.getId()); // jti should be present
    }

    /**
     * TC-UT-AUTH-001: 正常签发并解析管理员access token
     */
    @Test
    void issueAdminAccessToken_shouldGenerateValidToken_whenCalledWithValidParameters() {
        Long sid = 100L;
        String staffRole = "OWNER";
        String fingerprint = "admin-fp";

        String token = jwtUtil.issueAdminAccessToken(sid, staffRole, fingerprint);

        assertNotNull(token);

        Claims claims = jwtUtil.parse(token);
        assertEquals(String.valueOf(sid), claims.getSubject());
        assertEquals(sid, claims.get("sid"));
        assertEquals("staff", claims.get("role"));
        assertEquals(staffRole, claims.get("staffRole"));
        assertEquals("access", claims.get("type"));
    }

    /**
     * TC-UT-AUTH-001: 正常签发并解析用户refresh token
     */
    @Test
    void issueUserRefreshToken_shouldGenerateValidToken_whenCalledWithValidUid() {
        Long uid = 12345L;

        String token = jwtUtil.issueUserRefreshToken(uid);

        assertNotNull(token);

        Claims claims = jwtUtil.parse(token);
        assertEquals(String.valueOf(uid), claims.getSubject());
        assertEquals(uid, claims.get("uid"));
        assertEquals("user", claims.get("role"));
        assertEquals("refresh", claims.get("type"));
    }

    /**
     * TC-UT-AUTH-001: 正常签发并解析管理员refresh token
     */
    @Test
    void issueAdminRefreshToken_shouldGenerateValidToken_whenCalledWithValidParameters() {
        Long sid = 100L;
        String staffRole = "MANAGER";

        String token = jwtUtil.issueAdminRefreshToken(sid, staffRole);

        assertNotNull(token);

        Claims claims = jwtUtil.parse(token);
        assertEquals(String.valueOf(sid), claims.getSubject());
        assertEquals(sid, claims.get("sid"));
        assertEquals("staff", claims.get("role"));
        assertEquals(staffRole, claims.get("staffRole"));
        assertEquals("refresh", claims.get("type"));
    }

    /**
     * TC-UT-AUTH-002: 篡改签名应抛出异常
     */
    @Test
    void parse_shouldThrowException_whenTokenSignatureIsTampered() {
        String token = jwtUtil.issueUserAccessToken(12345L, "fp");

        // 篡改token（修改最后一个字符）
        String tamperedToken = token.substring(0, token.length() - 1) + "X";

        BizException exception = assertThrows(BizException.class, () -> jwtUtil.parse(tamperedToken));
        // TOKEN_INVALID or similar error code
        assertNotNull(exception);
    }

    /**
     * TC-UT-AUTH-003: 过期token应抛出异常
     */
    @Test
    void parse_shouldThrowException_whenTokenIsExpired() {
        JwtUtil shortTtlJwt = new JwtUtil();
        ReflectionTestUtils.setField(shortTtlJwt, "activeKid", "v1");
        ReflectionTestUtils.setField(shortTtlJwt, "secretV1", "test_secret_key_32_bytes_long_for_testing");
        ReflectionTestUtils.setField(shortTtlJwt, "userAccessTtlMinutes", -1); // 负数表示已过期

        String token = shortTtlJwt.issueUserAccessToken(12345L, "fp");

        BizException exception = assertThrows(BizException.class, () -> shortTtlJwt.parse(token));
        // TOKEN_EXPIRED
        assertNotNull(exception);
    }

    /**
     * 测试设备指纹生成
     */
    @Test
    void fingerprint_shouldGenerateHash_whenCalledWithValidInputs() {
        String openidPrefix = "wx_abc123";
        String userAgent = "Mozilla/5.0";

        String fp = JwtUtil.fingerprint(openidPrefix, userAgent);

        assertNotNull(fp);
        assertFalse(fp.isEmpty());
        assertEquals(16, fp.length()); // 8 bytes * 2 hex chars
    }

    /**
     * 测试设备指纹生成（空输入）
     */
    @Test
    void fingerprint_shouldHandleEmptyInputs() {
        String fp1 = JwtUtil.fingerprint(null, null);
        String fp2 = JwtUtil.fingerprint("", "");

        assertNotNull(fp1);
        assertNotNull(fp2);
    }

    /**
     * 测试token唯一性（jti）
     */
    @Test
    void issueUserAccessToken_shouldGenerateUniqueJti_forEachCall() {
        Long uid = 12345L;

        String token1 = jwtUtil.issueUserAccessToken(uid, "fp");
        String token2 = jwtUtil.issueUserAccessToken(uid, "fp");

        Claims claims1 = jwtUtil.parse(token1);
        Claims claims2 = jwtUtil.parse(token2);

        assertNotEquals(claims1.getId(), claims2.getId());
    }
}
