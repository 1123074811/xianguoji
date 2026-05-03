package com.xianguoji.server.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    String key();

    int limit() default 5;

    int period() default 60;
}
