package com.xianguoji.server.common.aspect;

import com.xianguoji.server.common.annotation.RateLimit;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String LUA_SCRIPT =
            "local current = redis.call('INCR', KEYS[1]) " +
            "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[2]) end " +
            "if current > tonumber(ARGV[1]) then return 0 end " +
            "return 1";

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getRemoteAddr();
        String redisKey = "rate_limit:" + rateLimit.key() + ":" + ip;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
        Long result = stringRedisTemplate.execute(script,
                Collections.singletonList(redisKey),
                String.valueOf(rateLimit.limit()),
                String.valueOf(rateLimit.period()));

        if (result == null || result == 0L) {
            throw new BizException(ResultCode.RATE_LIMITED);
        }

        return pjp.proceed();
    }
}
