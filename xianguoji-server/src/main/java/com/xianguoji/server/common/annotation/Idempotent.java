package com.xianguoji.server.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口幂等防重放注解
 * 基于 Redis setnx + TTL 实现，同一请求在 TTL 窗口内只执行一次
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /** 幂等 key 前缀，默认取方法名 */
    String key() default "";

    /** TTL 秒数，默认 5 秒 */
    int ttl() default 5;

    /** 是否以用户维度隔离（默认 true，按 uid 区分） */
    boolean userScope() default true;

    /** 提示信息 */
    String message() default "操作过于频繁，请稍后再试";
}
