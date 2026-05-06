package com.xianguoji.server.common.security;

import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录失败次数管理 —— 暴力破解 / 撞库防御
 * - 按账户 + IP 双维度计数
 * - 失败 5 次后锁定 15 分钟
 * - 登录成功重置计数
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginAttemptManager {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String FAIL_ACCOUNT_PREFIX = "xgj:auth:fail:account:";
    private static final String FAIL_IP_PREFIX = "xgj:auth:fail:ip:";
    private static final String LOCK_ACCOUNT_PREFIX = "xgj:auth:lock:account:";
    private static final String LOCK_IP_PREFIX = "xgj:auth:lock:ip:";

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    /**
     * 记录登录失败，达到阈值后自动锁定
     *
     * @param account 账户标识（用户名/手机号/openid）
     * @param ip      客户端 IP
     */
    public void recordFail(String account, String ip) {
        // 检查是否已被锁定
        checkLocked(account, ip);

        // 账户维度计数
        String accountKey = FAIL_ACCOUNT_PREFIX + account;
        Long accountCount = stringRedisTemplate.opsForValue().increment(accountKey);
        if (accountCount != null && accountCount == 1) {
            stringRedisTemplate.expire(accountKey, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }

        // IP 维度计数
        String ipKey = FAIL_IP_PREFIX + ip;
        Long ipCount = stringRedisTemplate.opsForValue().increment(ipKey);
        if (ipCount != null && ipCount == 1) {
            stringRedisTemplate.expire(ipKey, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }

        // 任一维度达到阈值 → 锁定
        if (accountCount != null && accountCount >= MAX_ATTEMPTS) {
            String lockKey = LOCK_ACCOUNT_PREFIX + account;
            stringRedisTemplate.opsForValue().set(lockKey, "1", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            log.warn("[LoginAttempt] 账户锁定: account={}, ip={}, failCount={}", account, ip, accountCount);
        }
        if (ipCount != null && ipCount >= MAX_ATTEMPTS) {
            String lockKey = LOCK_IP_PREFIX + ip;
            stringRedisTemplate.opsForValue().set(lockKey, "1", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            log.warn("[LoginAttempt] IP锁定: ip={}, failCount={}", ip, ipCount);
        }

        // 锁定后抛异常
        checkLocked(account, ip);
    }

    /**
     * 登录成功后重置失败计数
     */
    public void reset(String account, String ip) {
        stringRedisTemplate.delete(FAIL_ACCOUNT_PREFIX + account);
        stringRedisTemplate.delete(FAIL_IP_PREFIX + ip);
        stringRedisTemplate.delete(LOCK_ACCOUNT_PREFIX + account);
        stringRedisTemplate.delete(LOCK_IP_PREFIX + ip);
    }

    /**
     * 检查是否被锁定，锁定则抛异常
     */
    public void checkLocked(String account, String ip) {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOCK_ACCOUNT_PREFIX + account))) {
            throw new BizException(ResultCode.ACCESS_DENIED, "账户已锁定，请15分钟后重试");
        }
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOCK_IP_PREFIX + ip))) {
            throw new BizException(ResultCode.ACCESS_DENIED, "IP已锁定，请15分钟后重试");
        }
    }

    /**
     * 判断账户是否需要强制验证码（失败次数 >= 3 时强制）
     */
    public boolean requireCaptcha(String account, String ip) {
        String accountCount = stringRedisTemplate.opsForValue().get(FAIL_ACCOUNT_PREFIX + account);
        String ipCount = stringRedisTemplate.opsForValue().get(FAIL_IP_PREFIX + ip);
        int ac = accountCount != null ? Integer.parseInt(accountCount) : 0;
        int ic = ipCount != null ? Integer.parseInt(ipCount) : 0;
        return ac >= 3 || ic >= 3;
    }
}
