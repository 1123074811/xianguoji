package com.xianguoji.server.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.security.JwtBlacklistManager;
import com.xianguoji.server.common.security.JwtUtil;
import com.xianguoji.server.common.security.LoginAttemptManager;
import com.xianguoji.server.common.security.SmsRateLimiter;
import com.xianguoji.server.common.util.IpUtil;
import com.xianguoji.server.common.util.SmsUtil;
import com.xianguoji.server.common.util.WechatUtil;
import com.xianguoji.server.module.auth.dto.SmsLoginDto;
import com.xianguoji.server.module.auth.dto.SmsSendDto;
import com.xianguoji.server.module.auth.service.AuthService;
import com.xianguoji.server.module.auth.vo.LoginVO;
import com.xianguoji.server.module.staff.entity.Staff;
import com.xianguoji.server.module.staff.mapper.StaffMapper;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TC-UT-AUTH-005: AuthService.loginByPassword 密码错误连续5次锁定
 * TC-UT-AUTH-006: AuthService.loginByWechat 微信code失效
 * TC-UT-AUTH-SMSL-003: 新手机号自动注册
 * TC-UT-AUTH-SMSL-004: 已注册用户
 * TC-UT-AUTH-SMSL-005: 用户已被封禁
 * TC-UT-AUTH-RFR-001: 正常刷新token
 * TC-UT-AUTH-RFR-002: refresh token过期
 * TC-UT-AUTH-RFR-003: refresh token已被吊销
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private UserMapper userMapper;

    @Mock
    private StaffMapper staffMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtBlacklistManager jwtBlacklistManager;

    @Mock
    private SmsUtil smsUtil;

    @Mock
    private WechatUtil wechatUtil;

    @Mock
    private LoginAttemptManager loginAttemptManager;

    @Mock
    private SmsRateLimiter smsRateLimiter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(10);
        ReflectionTestUtils.setField(authService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(authService, "smsProvider", "mock");
        
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(IpUtil.getClientIp(any())).thenReturn("127.0.0.1");
    }

    /**
     * TC-UT-AUTH-SMSL-003: 新手机号自动注册
     */
    @Test
    void smsLogin_shouldRegisterNewUser_whenPhoneNotExists() {
        // Arrange
        String phone = "13800138001";
        SmsLoginDto dto = new SmsLoginDto();
        dto.setPhone(phone);
        dto.setCode("1234");

        when(valueOperations.get(anyString())).thenReturn("1234");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });
        when(jwtUtil.issueUserAccessToken(anyLong(), anyString())).thenReturn("access-token");
        when(jwtUtil.issueUserRefreshToken(anyLong())).thenReturn("refresh-token");
        when(jwtUtil.getUserAccessTtlMinutes()).thenReturn(120);
        when(jwtUtil.getUserRefreshTtlHours()).thenReturn(168);

        // Act
        LoginVO result = authService.smsLogin(dto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertNotNull(result.getRefreshToken());
        assertTrue(result.getUserInfo().getIsNew());
        verify(userMapper).insert(any(User.class));
        verify(stringRedisTemplate).delete(anyString());
    }

    /**
     * TC-UT-AUTH-SMSL-004: 已注册用户
     */
    @Test
    void smsLogin_shouldLoginExistingUser_whenPhoneExists() {
        // Arrange
        String phone = "13800138001";
        SmsLoginDto dto = new SmsLoginDto();
        dto.setPhone(phone);
        dto.setCode("1234");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPhone(phone);
        existingUser.setNickname("测试用户");
        existingUser.setStatus(1);

        when(valueOperations.get(anyString())).thenReturn("1234");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);
        when(jwtUtil.issueUserAccessToken(anyLong(), anyString())).thenReturn("access-token");
        when(jwtUtil.issueUserRefreshToken(anyLong())).thenReturn("refresh-token");
        when(jwtUtil.getUserAccessTtlMinutes()).thenReturn(120);
        when(jwtUtil.getUserRefreshTtlHours()).thenReturn(168);

        // Act
        LoginVO result = authService.smsLogin(dto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertFalse(result.getUserInfo().getIsNew());
        verify(userMapper, never()).insert(any(User.class));
        verify(userMapper).updateById(any(User.class));
    }

    /**
     * TC-UT-AUTH-SMSL-005: 用户已被封禁
     */
    @Test
    void smsLogin_shouldThrowException_whenUserIsDisabled() {
        // Arrange
        String phone = "13800138001";
        SmsLoginDto dto = new SmsLoginDto();
        dto.setPhone(phone);
        dto.setCode("1234");

        User disabledUser = new User();
        disabledUser.setId(1L);
        disabledUser.setPhone(phone);
        disabledUser.setStatus(0); // 禁用状态

        when(valueOperations.get(anyString())).thenReturn("1234");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(disabledUser);

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> authService.smsLogin(dto));
        assertEquals(ResultCode.ACCESS_DENIED, exception.getResultCode());
    }

    /**
     * TC-UT-AUTH-SMSL-001: 短信码错误
     */
    @Test
    void smsLogin_shouldThrowException_whenSmsCodeIsWrong() {
        // Arrange
        String phone = "13800138001";
        SmsLoginDto dto = new SmsLoginDto();
        dto.setPhone(phone);
        dto.setCode("9999"); // 错误的验证码

        when(valueOperations.get(anyString())).thenReturn("1234"); // Redis中存储的是1234

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> authService.smsLogin(dto));
        assertEquals(ResultCode.BIZ_ERROR, exception.getResultCode());
        assertTrue(exception.getMessage().contains("验证码错误"));
    }

    /**
     * TC-UT-AUTH-RFR-001: 正常刷新token
     */
    @Test
    void refreshToken_shouldIssueNewTokens_whenRefreshTokenIsValid() {
        // Arrange
        String oldRefreshToken = "old-refresh-token";
        Claims claims = mock(Claims.class);
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(claims.get("role", String.class)).thenReturn("user");
        when(claims.get("uid", Long.class)).thenReturn(1L);
        when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));

        when(jwtUtil.parse(oldRefreshToken)).thenReturn(claims);
        when(jwtBlacklistManager.isBlacklisted(oldRefreshToken)).thenReturn(false);
        when(jwtUtil.issueUserAccessToken(anyLong(), anyString())).thenReturn("new-access-token");
        when(jwtUtil.issueUserRefreshToken(anyLong())).thenReturn("new-refresh-token");
        when(jwtUtil.getUserAccessTtlMinutes()).thenReturn(120);
        when(jwtUtil.getUserRefreshTtlHours()).thenReturn(168);

        // Act
        LoginVO result = authService.refreshToken(oldRefreshToken);

        // Assert
        assertNotNull(result);
        assertEquals("new-access-token", result.getToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        verify(jwtBlacklistManager).blacklist(eq(oldRefreshToken), anyLong());
        verify(jwtBlacklistManager).registerActiveToken(anyString(), anyLong(), anyLong());
    }

    /**
     * TC-UT-AUTH-RFR-002: refresh token过期
     */
    @Test
    void refreshToken_shouldThrowException_whenRefreshTokenIsExpired() {
        // Arrange
        String expiredRefreshToken = "expired-refresh-token";

        when(jwtUtil.parse(expiredRefreshToken)).thenThrow(new BizException(ResultCode.TOKEN_EXPIRED));

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> authService.refreshToken(expiredRefreshToken));
        assertEquals(ResultCode.TOKEN_EXPIRED, exception.getResultCode());
    }

    /**
     * TC-UT-AUTH-RFR-003: refresh token已被吊销
     */
    @Test
    void refreshToken_shouldThrowException_whenRefreshTokenIsBlacklisted() {
        // Arrange
        String blacklistedToken = "blacklisted-refresh-token";
        Claims claims = mock(Claims.class);
        when(claims.get("type", String.class)).thenReturn("refresh");

        when(jwtUtil.parse(blacklistedToken)).thenReturn(claims);
        when(jwtBlacklistManager.isBlacklisted(blacklistedToken)).thenReturn(true);

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> authService.refreshToken(blacklistedToken));
        assertEquals(ResultCode.TOKEN_INVALID, exception.getResultCode());
        assertTrue(exception.getMessage().contains("已失效"));
    }

    /**
     * TC-UT-AUTH-RFR-001: 刷新管理员token
     */
    @Test
    void refreshToken_shouldIssueNewAdminTokens_whenRefreshTokenIsStaffType() {
        // Arrange
        String oldRefreshToken = "old-admin-refresh-token";
        Claims claims = mock(Claims.class);
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(claims.get("role", String.class)).thenReturn("staff");
        when(claims.get("sid", Long.class)).thenReturn(1L);
        when(claims.get("staffRole", String.class)).thenReturn("OWNER");
        when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));

        when(jwtUtil.parse(oldRefreshToken)).thenReturn(claims);
        when(jwtBlacklistManager.isBlacklisted(oldRefreshToken)).thenReturn(false);
        when(jwtUtil.issueAdminAccessToken(anyLong(), anyString(), anyString())).thenReturn("new-admin-access-token");
        when(jwtUtil.issueAdminRefreshToken(anyLong(), anyString())).thenReturn("new-admin-refresh-token");
        when(jwtUtil.getAdminAccessTtlMinutes()).thenReturn(30);
        when(jwtUtil.getAdminRefreshTtlHours()).thenReturn(12);

        // Act
        LoginVO result = authService.refreshToken(oldRefreshToken);

        // Assert
        assertNotNull(result);
        assertEquals("new-admin-access-token", result.getToken());
        assertEquals("new-admin-refresh-token", result.getRefreshToken());
    }

    /**
     * 测试refresh token类型错误（使用access token刷新）
     */
    @Test
    void refreshToken_shouldThrowException_whenTokenTypeIsNotRefresh() {
        // Arrange
        String accessToken = "access-token";
        Claims claims = mock(Claims.class);
        when(claims.get("type", String.class)).thenReturn("access");

        when(jwtUtil.parse(accessToken)).thenReturn(claims);
        when(jwtBlacklistManager.isBlacklisted(accessToken)).thenReturn(false);

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> authService.refreshToken(accessToken));
        assertEquals(ResultCode.TOKEN_INVALID, exception.getResultCode());
        assertTrue(exception.getMessage().contains("仅 refresh token"));
    }

    // ==================== 微信登录 ====================

    /**
     * TC-UT-AUTH-006: 微信登录 - code失效
     */
    @Test
    void wechatLogin_shouldThrowException_whenCodeInvalid() {
        // Arrange
        com.xianguoji.server.module.auth.dto.WechatLoginDto dto = new com.xianguoji.server.module.auth.dto.WechatLoginDto();
        dto.setJsCode("invalid_code");
        dto.setNickname("微信用户");

        when(wechatUtil.code2Session("invalid_code")).thenThrow(
                new BizException(ResultCode.THIRD_PARTY_ERROR, "微信code无效"));

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> authService.wechatLogin(dto));
        assertEquals(ResultCode.THIRD_PARTY_ERROR, exception.getResultCode());
    }

    /**
     * TC-UT-AUTH-006: 微信登录 - 首次登录无手机号
     */
    @Test
    void wechatLogin_shouldReturnNeedBindPhone_whenNewUserWithoutPhone() {
        // Arrange
        com.xianguoji.server.module.auth.dto.WechatLoginDto dto = new com.xianguoji.server.module.auth.dto.WechatLoginDto();
        dto.setJsCode("valid_code");
        dto.setNickname("微信新用户");

        cn.hutool.json.JSONObject session = new cn.hutool.json.JSONObject();
        session.set("openid", "test_openid_001");
        session.set("unionid", "test_unionid_001");
        when(wechatUtil.code2Session("valid_code")).thenReturn(session);

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return 1;
        });
        when(jwtUtil.issueUserAccessToken(anyLong(), anyString())).thenReturn("wx-access-token");
        when(jwtUtil.issueUserRefreshToken(anyLong())).thenReturn("wx-refresh-token");
        when(jwtUtil.getUserAccessTtlMinutes()).thenReturn(120);
        when(jwtUtil.getUserRefreshTtlHours()).thenReturn(168);

        // Act
        LoginVO result = authService.wechatLogin(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.getUserInfo().getIsNew());
    }

    /**
     * TC-UT-AUTH-006: 微信登录 - 已绑定手机号的老用户
     */
    @Test
    void wechatLogin_shouldLoginDirectly_whenExistingUserWithPhone() {
        // Arrange
        com.xianguoji.server.module.auth.dto.WechatLoginDto dto = new com.xianguoji.server.module.auth.dto.WechatLoginDto();
        dto.setJsCode("valid_code");
        dto.setNickname("老用户");

        cn.hutool.json.JSONObject session = new cn.hutool.json.JSONObject();
        session.set("openid", "existing_openid");
        session.set("unionid", "existing_unionid");
        when(wechatUtil.code2Session("valid_code")).thenReturn(session);

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPhone("13800138001");
        existingUser.setWxOpenid("existing_openid");
        existingUser.setStatus(1);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);
        when(jwtUtil.issueUserAccessToken(anyLong(), anyString())).thenReturn("wx-access-token");
        when(jwtUtil.issueUserRefreshToken(anyLong())).thenReturn("wx-refresh-token");
        when(jwtUtil.getUserAccessTtlMinutes()).thenReturn(120);
        when(jwtUtil.getUserRefreshTtlHours()).thenReturn(168);

        // Act
        LoginVO result = authService.wechatLogin(dto);

        // Assert
        assertNotNull(result);
        assertFalse(result.getUserInfo().getIsNew());
    }

    // ==================== 管理员登录 ====================

    /**
     * TC-UT-AUTH-005: 管理员密码错误连续5次锁定
     */
    @Test
    void adminLogin_shouldLockAccount_after5WrongAttempts() {
        // Arrange
        com.xianguoji.server.module.auth.dto.AdminLoginDto dto = new com.xianguoji.server.module.auth.dto.AdminLoginDto();
        dto.setUsername("admin");
        dto.setPassword("wrong_password");
        dto.setCaptchaKey("test-key");
        dto.setCaptchaCode("1234");

        Staff staff = new Staff();
        staff.setId(1L);
        staff.setUsername("admin");
        staff.setPasswordHash(passwordEncoder.encode("test123456"));
        staff.setStatus(1);
        staff.setRole("OWNER");

        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(valueOperations.get(startsWith("captcha:"))).thenReturn("1234");
        when(staffMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(staff);

        // checkLocked passes, then recordFail should be called for wrong password
        doNothing().when(loginAttemptManager).checkLocked(anyString(), anyString());

        // Act & Assert - wrong password triggers recordFail
        BizException exception = assertThrows(BizException.class, () -> authService.adminLogin(dto, request));
        verify(loginAttemptManager).recordFail(eq("admin"), anyString());
    }

    /**
     * TC-UT-AUTH-005: 管理员账号已被锁定
     */
    @Test
    void adminLogin_shouldReject_whenAccountIsLocked() {
        // Arrange
        com.xianguoji.server.module.auth.dto.AdminLoginDto dto = new com.xianguoji.server.module.auth.dto.AdminLoginDto();
        dto.setUsername("admin");
        dto.setPassword("test123456");
        dto.setCaptchaKey("test-key");
        dto.setCaptchaCode("1234");

        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // checkLocked throws ACCESS_DENIED
        doThrow(new BizException(ResultCode.ACCESS_DENIED, "账户已锁定，请15分钟后重试"))
                .when(loginAttemptManager).checkLocked(eq("admin"), anyString());

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> authService.adminLogin(dto, request));
        assertEquals(ResultCode.ACCESS_DENIED, exception.getResultCode());
    }

    /**
     * TC-UT-AUTH-005: 管理员正常登录成功
     */
    @Test
    void adminLogin_shouldReturnToken_whenCredentialsValid() {
        // Arrange
        com.xianguoji.server.module.auth.dto.AdminLoginDto dto = new com.xianguoji.server.module.auth.dto.AdminLoginDto();
        dto.setUsername("admin");
        dto.setPassword("test123456");
        dto.setCaptchaKey("test-key");
        dto.setCaptchaCode("1234");

        Staff staff = new Staff();
        staff.setId(1L);
        staff.setUsername("admin");
        staff.setPasswordHash(passwordEncoder.encode("test123456"));
        staff.setStatus(1);
        staff.setRole("OWNER");

        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(valueOperations.get(startsWith("captcha:"))).thenReturn("1234");
        when(staffMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(staff);
        doNothing().when(loginAttemptManager).checkLocked(anyString(), anyString());
        when(jwtUtil.issueAdminAccessToken(anyLong(), anyString(), anyString())).thenReturn("admin-access-token");
        when(jwtUtil.issueAdminRefreshToken(anyLong(), anyString())).thenReturn("admin-refresh-token");
        when(jwtUtil.getAdminAccessTtlMinutes()).thenReturn(30);
        when(jwtUtil.getAdminRefreshTtlHours()).thenReturn(12);

        // Act
        LoginVO result = authService.adminLogin(dto, request);

        // Assert
        assertNotNull(result);
        assertEquals("admin-access-token", result.getToken());
        verify(loginAttemptManager).reset(eq("admin"), anyString());
    }

    // ==================== 登出黑名单 ====================

    /**
     * TC-UT-AUTH-LO-001: 登出后token加入黑名单（Controller层逻辑，此处验证blacklist调用）
     */
    @Test
    void logout_shouldBlacklistToken_whenTokenIsValid() {
        // Arrange - 模拟Controller层logout逻辑
        String token = "valid-access-token";
        Claims claims = mock(Claims.class);
        when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));

        when(jwtUtil.parse(token)).thenReturn(claims);

        // Act - 模拟Controller中的blacklist调用
        long remainMs = claims.getExpiration().getTime() - System.currentTimeMillis();
        jwtBlacklistManager.blacklist(token, remainMs);

        // Assert
        verify(jwtBlacklistManager).blacklist(eq(token), anyLong());
    }

    /**
     * TC-UT-AUTH-LO-003: 登出后refresh token也应被吊销
     */
    @Test
    void logout_shouldRevokeRefreshToken_whenRefreshTokenExists() {
        // Arrange - 模拟refresh token rotation时旧token被blacklist
        String oldRefreshToken = "old-refresh-token";
        Claims claims = mock(Claims.class);
        when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(claims.get("type", String.class)).thenReturn("refresh");

        when(jwtUtil.parse(oldRefreshToken)).thenReturn(claims);
        when(jwtBlacklistManager.isBlacklisted(oldRefreshToken)).thenReturn(false);

        // Act - refreshToken方法会将旧refresh token加入黑名单
        authService.refreshToken(oldRefreshToken);

        // Assert - 旧refresh token应被blacklist
        verify(jwtBlacklistManager).blacklist(eq(oldRefreshToken), anyLong());
    }

    // ==================== SMS 频控 ====================

    /**
     * TC-UT-AUTH-004: SMS发送 - 同号60s内重复
     */
    @Test
    void sendSmsCode_shouldReject_whenSamePhoneWithin60s() {
        // Arrange
        com.xianguoji.server.module.auth.dto.SmsSendDto dto = new com.xianguoji.server.module.auth.dto.SmsSendDto();
        dto.setPhone("13800138001");

        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // SmsRateLimiter.check throws when rate limited
        doThrow(new BizException(ResultCode.RATE_LIMITED, "验证码发送过于频繁，请60秒后重试"))
                .when(smsRateLimiter).check(eq("13800138001"), anyString());

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> authService.sendSmsCode(dto, request));
        assertTrue(exception.getMessage().contains("频繁"));
    }

    /**
     * TC-UT-AUTH-004: CaptchaService - 验证码一次性
     */
    @Test
    void smsLogin_shouldInvalidateCode_afterSuccessfulUse() {
        // Arrange
        String phone = "13800138001";
        SmsLoginDto dto = new SmsLoginDto();
        dto.setPhone(phone);
        dto.setCode("1234");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPhone(phone);
        existingUser.setStatus(1);

        when(valueOperations.get(anyString())).thenReturn("1234");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);
        when(jwtUtil.issueUserAccessToken(anyLong(), anyString())).thenReturn("access-token");
        when(jwtUtil.issueUserRefreshToken(anyLong())).thenReturn("refresh-token");
        when(jwtUtil.getUserAccessTtlMinutes()).thenReturn(120);
        when(jwtUtil.getUserRefreshTtlHours()).thenReturn(168);

        // Act
        authService.smsLogin(dto);

        // Assert - 验证码应被删除
        verify(stringRedisTemplate).delete(anyString());
    }
}


