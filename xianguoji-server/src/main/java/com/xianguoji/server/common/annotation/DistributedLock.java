package com.xianguoji.server.common.annotation;

import java.lang.annotation.*;

/**
 * 分布式锁注解：基于 Redisson，在方法执行前获取锁，执行后释放
 * key 支持 SpEL 表达式，如 "#id"、"#dto.skuId"
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁的 key，支持 SpEL 表达式
     * 最终 Redis key = xgj:lock:{prefix}:{key}
     */
    String key();

    /**
     * key 前缀，默认方法名
     */
    String prefix() default "";

    /**
     * 等待时间（秒），0 = 不等待（tryLock 立即返回）
     */
    long waitTime() default 0;

    /**
     * 锁持有时间（秒），-1 = 看门狗自动续期
     */
    long leaseTime() default -1;
}
