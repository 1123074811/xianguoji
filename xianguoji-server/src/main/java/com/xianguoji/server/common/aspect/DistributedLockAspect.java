package com.xianguoji.server.common.aspect;

import com.xianguoji.server.common.annotation.DistributedLock;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "xgj:lock:";
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    @Around("@annotation(lock)")
    public Object around(ProceedingJoinPoint pjp, DistributedLock lock) throws Throwable {
        String lockKey = buildLockKey(pjp, lock);
        RLock rLock = redissonClient.getLock(lockKey);

        boolean acquired;
        if (lock.leaseTime() == -1) {
            acquired = rLock.tryLock(lock.waitTime(), TimeUnit.SECONDS);
        } else {
            acquired = rLock.tryLock(lock.waitTime(), lock.leaseTime(), TimeUnit.SECONDS);
        }

        if (!acquired) {
            log.warn("获取分布式锁失败: key={}", lockKey);
            throw new BizException(ResultCode.CONFLICT, "操作过于频繁，请稍后重试");
        }

        try {
            return pjp.proceed();
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    private String buildLockKey(ProceedingJoinPoint pjp, DistributedLock lock) {
        String prefix = lock.prefix().isBlank()
                ? ((MethodSignature) pjp.getSignature()).getMethod().getName()
                : lock.prefix();

        String keyExpr = lock.key();
        String resolvedKey;

        // 简单 SpEL 解析：支持 #param 形式
        if (keyExpr.startsWith("#")) {
            EvaluationContext context = createEvaluationContext(pjp);
            resolvedKey = PARSER.parseExpression(keyExpr).getValue(context, String.class);
        } else {
            resolvedKey = keyExpr;
        }

        return LOCK_PREFIX + prefix + ":" + resolvedKey;
    }

    private EvaluationContext createEvaluationContext(ProceedingJoinPoint pjp) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String[] paramNames = new DefaultParameterNameDiscoverer().getParameterNames(signature.getMethod());
        Object[] args = pjp.getArgs();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        return context;
    }
}
