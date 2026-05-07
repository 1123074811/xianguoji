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
 * §2.2 Auth 安全组件单元测试 - SmsRateLimiter
 *
 * TC-SEC-AUTH-002: 验证码频控/重放
 * TC-SEC-RATE-001: 短信限流
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SmsRateLimiter 单元测试")
class SmsRateLimiterTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private SmsRateLimiter smsRateLimiter;

    @Nested
    @DisplayName("check 短信发送频率检查")
    class CheckTests {

        @Test
        @DisplayName("首次发送应通过")
        void check_shouldPass_onFirstSend() {
            when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.increment(anyString())).thenReturn(1L);

            assertDoesNotThrow(() -> smsRateLimiter.check("13800138001", "192.168.1.1"));
        }

        @Test
        @DisplayName("60s内同号重复发送应拒绝")
        void check_shouldReject_whenSamePhoneWithin60s() {
            when(stringRedisTemplate.hasKey(startsWith("xgj:sms:phone:sec:"))).thenReturn(true);

            BizException ex = assertThrows(BizException.class,
                    () -> smsRateLimiter.check("13800138001", "192.168.1.1"));
            assertEquals(ResultCode.RATE_LIMITED, ex.getResultCode());
            assertTrue(ex.getMessage().contains("60秒"));
        }

        @Test
        @DisplayName("同号1天超10次应拒绝")
        void check_shouldReject_whenDailyLimitExceeded() {
            when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.increment(contains("phone:day:"))).thenReturn(11L);

            BizException ex = assertThrows(BizException.class,
                    () -> smsRateLimiter.check("13800138001", "192.168.1.1"));
            assertEquals(ResultCode.RATE_LIMITED, ex.getResultCode());
            assertTrue(ex.getMessage().contains("上限"));
        }

        @Test
        @DisplayName("同IP 60s内超5次应拒绝")
        void check_shouldReject_whenIpRateExceeded() {
            when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.increment(contains("phone:day:"))).thenReturn(1L);
            when(valueOps.increment(contains("ip:sec:"))).thenReturn(6L);

            BizException ex = assertThrows(BizException.class,
                    () -> smsRateLimiter.check("13800138001", "192.168.1.1"));
            assertEquals(ResultCode.RATE_LIMITED, ex.getResultCode());
            assertTrue(ex.getMessage().contains("IP"));
        }

        @Test
        @DisplayName("IP为null时应跳过IP检查")
        void check_shouldSkipIpCheck_whenIpIsNull() {
            when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.increment(contains("phone:day:"))).thenReturn(1L);

            assertDoesNotThrow(() -> smsRateLimiter.check("13800138001", null));
        }
    }

    @Nested
    @DisplayName("markSent 设置60s冷却标记")
    class MarkSentTests {

        @Test
        @DisplayName("发送成功应设置60s冷却")
        void markSent_shouldSet60sCooldown() {
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

            smsRateLimiter.markSent("13800138001");

            verify(valueOps).set(startsWith("xgj:sms:phone:sec:"), eq("1"), eq(60L), eq(TimeUnit.SECONDS));
        }
    }
}
