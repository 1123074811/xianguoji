package com.xianguoji.server.common.aspect;

import com.xianguoji.server.common.annotation.Idempotent;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.security.LoginContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "xgj:idempotent:";

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint pjp, Idempotent idempotent) throws Throwable {
        String key = buildKey(pjp, idempotent);
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", idempotent.ttl(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("[Idempotent] 重复请求被拦截: key={}", key);
            throw new BizException(ResultCode.RATE_LIMITED, idempotent.message());
        }
        try {
            return pjp.proceed();
        } catch (Exception e) {
            // 业务异常时删除幂等标记，允许重试
            stringRedisTemplate.delete(key);
            throw e;
        }
    }

    private String buildKey(ProceedingJoinPoint pjp, Idempotent idempotent) {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String prefix = idempotent.key().isEmpty()
                ? sig.getDeclaringType().getSimpleName() + ":" + sig.getName()
                : idempotent.key();
        StringBuilder sb = new StringBuilder(KEY_PREFIX).append(prefix);

        // S-13: 从请求参数提取唯一标识
        if (!idempotent.requestParam().isEmpty()) {
            String paramValue = getRequestParam(idempotent.requestParam());
            if (paramValue != null && !paramValue.isBlank()) {
                sb.append(":").append(idempotent.requestParam()).append("=").append(paramValue);
            }
        }

        if (idempotent.userScope()) {
            Long uid = LoginContext.uid();
            if (uid != null) {
                sb.append(":uid=").append(uid);
            }
        }
        return sb.toString();
    }

    private String getRequestParam(String paramName) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();
        String value = request.getParameter(paramName);
        if (value == null) {
            // 尝试从 header 取
            value = request.getHeader(paramName);
        }
        return value;
    }
}
