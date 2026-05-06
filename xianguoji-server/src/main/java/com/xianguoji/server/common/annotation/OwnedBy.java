package com.xianguoji.server.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * S-12: 资源归属校验注解
 * 标注在 Controller 方法上，切面在执行前校验资源是否属于当前登录用户
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OwnedBy {

    /** 资源所属字段，如 "userId" */
    String field() default "userId";

    /** 资源实体类，用于反射加载 service 查询主体 */
    Class<?> entity();

    /** 主键参数名，默认从第一个 path variable 取 */
    String idParam() default "id";
}
