package com.xianguoji.server.common.security;

import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 短信验证码频控
 * - 同手机号 60s/次
 * - 同手机号 1天 10次
 * - 同 IP 60s 5次
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsRateLimiter {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String PHONE_SEC_PREFIX = "xgj:sms:phone:sec:";
    private static final String PHONE_DAY_PREFIX = "xgj:sms:phone:day:";
    private static final String IP_SEC_PREFIX = "xgj:sms:ip:sec:";

    private static final int PHONE_SEC_LIMIT = 1;
    private static final int PHONE_DAY_LIMIT = 10;
    private static final int IP_SEC_LIMIT = 5;

    /**
     * 检查短信发送频率，超频则抛异常
     *
     * @param phone 手机号
     * @param ip    客户端 IP
     */
    public void check(String phone, String ip) {
        // 同手机号 60s 内 ≤ 1 次
        String phoneSecKey = PHONE_SEC_PREFIX + phone;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(phoneSecKey))) {
            throw new BizException(ResultCode.RATE_LIMITED, "验证码发送过于频繁，请60秒后重试");
        }

        // 同手机号 1 天 ≤ 10 次
        String phoneDayKey = PHONE_DAY_PREFIX + phone;
        Long dayCount = stringRedisTemplate.opsForValue().increment(phoneDayKey);
        if (dayCount != null && dayCount == 1) {
            stringRedisTemplate.expire(phoneDayKey, 1, TimeUnit.DAYS);
        }
        if (dayCount != null && dayCount > PHONE_DAY_LIMIT) {
            throw new BizException(ResultCode.RATE_LIMITED, "今日验证码发送次数已达上限");
        }

        // 同 IP 60s 内 ≤ 5 次
        if (ip != null && !ip.isBlank()) {
            String ipSecKey = IP_SEC_PREFIX + ip;
            Long ipCount = stringRedisTemplate.opsForValue().increment(ipSecKey);
            if (ipCount != null && ipCount == 1) {
                stringRedisTemplate.expire(ipSecKey, 60, TimeUnit.SECONDS);
            }
            if (ipCount != null && ipCount > IP_SEC_LIMIT) {
                throw new BizException(ResultCode.RATE_LIMITED, "IP发送频率超限，请稍后再试");
            }
        }
    }

    /**
     * 发送成功后设置 60s 冷却标记
     */
    public void markSent(String phone) {
        stringRedisTemplate.opsForValue().set(PHONE_SEC_PREFIX + phone, "1", 60, TimeUnit.SECONDS);
    }
}
