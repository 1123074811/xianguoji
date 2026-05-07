package com.xianguoji.server.common.security;

import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * §2.2 Auth 安全组件单元测试
 *
 * TC-UT-AUTH-005: LoginAttemptManager - 密码错误连续5次锁定15分钟
 * TC-SEC-AUTH-002: 验证码频控/重放
 * TC-SEC-RATE-001: 登录/短信限流
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginAttemptManager 单元测试")
class LoginAttemptManagerTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private LoginAttemptManager loginAttemptManager;

    @Nested
    @DisplayName("TC-UT-AUTH-005: recordFail 连续失败锁定")
    class RecordFailTests {

        @Test
        @DisplayName("首次失败应不锁定")
        void recordFail_shouldNotLock_onFirstFail() {
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.increment(anyString())).thenReturn(1L);
            when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);

            // 首次失败不抛异常
            assertDoesNotThrow(() -> loginAttemptManager.recordFail("admin", "192.168.1.1"));
        }

        @Test
        @DisplayName("连续5次失败应锁定账户")
        void recordFail_shouldLockAccount_after5Failures() {
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.increment(contains("account:admin"))).thenReturn(5L);
            when(valueOps.increment(contains("ip:"))).thenReturn(1L);
            when(stringRedisTemplate.hasKey(startsWith("xgj:auth:lock:account:"))).thenReturn(true);
            when(stringRedisTemplate.hasKey(startsWith("xgj:auth:lock:ip:"))).thenReturn(false);

            // 第5次失败后 checkLocked 应检测到锁定
            BizException ex = assertThrows(BizException.class,
                    () -> loginAttemptManager.recordFail("admin", "192.168.1.1"));
            assertEquals(ResultCode.ACCESS_DENIED, ex.getResultCode());
        }

        @Test
        @DisplayName("同一IP连续5次失败应锁定IP")
        void recordFail_shouldLockIP_after5FailuresFromSameIP() {
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.increment(contains("account:"))).thenReturn(1L);
            when(valueOps.increment(contains("ip:192.168.1.1"))).thenReturn(5L);
            when(stringRedisTemplate.hasKey(startsWith("xgj:auth:lock:account:"))).thenReturn(false);
            when(stringRedisTemplate.hasKey(startsWith("xgj:auth:lock:ip:"))).thenReturn(true);

            BizException ex = assertThrows(BizException.class,
                    () -> loginAttemptManager.recordFail("user1", "192.168.1.1"));
            assertEquals(ResultCode.ACCESS_DENIED, ex.getResultCode());
        }
    }

    @Nested
    @DisplayName("checkLocked 检查锁定状态")
    class CheckLockedTests {

        @Test
        @DisplayName("未锁定应通过")
        void checkLocked_shouldPass_whenNotLocked() {
            when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);

            assertDoesNotThrow(() -> loginAttemptManager.checkLocked("admin", "192.168.1.1"));
        }

        @Test
        @DisplayName("账户已锁定应抛异常")
        void checkLocked_shouldThrow_whenAccountLocked() {
            when(stringRedisTemplate.hasKey(startsWith("xgj:auth:lock:account:"))).thenReturn(true);

            BizException ex = assertThrows(BizException.class,
                    () -> loginAttemptManager.checkLocked("admin", "192.168.1.1"));
            assertTrue(ex.getMessage().contains("账户已锁定"));
        }

        @Test
        @DisplayName("IP已锁定应抛异常")
        void checkLocked_shouldThrow_whenIpLocked() {
            when(stringRedisTemplate.hasKey(startsWith("xgj:auth:lock:account:"))).thenReturn(false);
            when(stringRedisTemplate.hasKey(startsWith("xgj:auth:lock:ip:"))).thenReturn(true);

            BizException ex = assertThrows(BizException.class,
                    () -> loginAttemptManager.checkLocked("admin", "192.168.1.1"));
            assertTrue(ex.getMessage().contains("IP已锁定"));
        }
    }

    @Nested
    @DisplayName("reset 重置失败计数")
    class ResetTests {

        @Test
        @DisplayName("登录成功应清除所有计数和锁定")
        void reset_shouldClearAllKeys() {
            loginAttemptManager.reset("admin", "192.168.1.1");

            verify(stringRedisTemplate).delete("xgj:auth:fail:account:admin");
            verify(stringRedisTemplate).delete("xgj:auth:fail:ip:192.168.1.1");
            verify(stringRedisTemplate).delete("xgj:auth:lock:account:admin");
            verify(stringRedisTemplate).delete("xgj:auth:lock:ip:192.168.1.1");
        }
    }

    @Nested
    @DisplayName("requireCaptcha 判断是否需要验证码")
    class RequireCaptchaTests {

        @Test
        @DisplayName("失败次数<3 不需要验证码")
        void requireCaptcha_shouldReturnFalse_whenFailCountBelow3() {
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.get(contains("account:admin"))).thenReturn("2");
            when(valueOps.get(contains("ip:"))).thenReturn("1");

            assertFalse(loginAttemptManager.requireCaptcha("admin", "192.168.1.1"));
        }

        @Test
        @DisplayName("账户失败次数≥3 需要验证码")
        void requireCaptcha_shouldReturnTrue_whenAccountFailCountAt3() {
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.get(contains("account:admin"))).thenReturn("3");
            when(valueOps.get(contains("ip:"))).thenReturn("0");

            assertTrue(loginAttemptManager.requireCaptcha("admin", "192.168.1.1"));
        }

        @Test
        @DisplayName("IP失败次数≥3 需要验证码")
        void requireCaptcha_shouldReturnTrue_whenIpFailCountAt3() {
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.get(contains("account:"))).thenReturn(null);
            when(valueOps.get(contains("ip:192.168.1.1"))).thenReturn("4");

            assertTrue(loginAttemptManager.requireCaptcha("admin", "192.168.1.1"));
        }

        @Test
        @DisplayName("无失败记录不需要验证码")
        void requireCaptcha_shouldReturnFalse_whenNoFailures() {
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.get(anyString())).thenReturn(null);

            assertFalse(loginAttemptManager.requireCaptcha("admin", "192.168.1.1"));
        }
    }
}
