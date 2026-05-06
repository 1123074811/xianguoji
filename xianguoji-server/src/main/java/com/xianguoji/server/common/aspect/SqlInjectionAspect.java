package com.xianguoji.server.common.aspect;

import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.validator.SqlInjectionValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;

// SQL 注入防御切面
// - 拦截 Controller 层的查询方法(get*/list*/page*/search*/query*)
// - 遍历 String 参数及 DTO 内的 String 字段做注入检测
// - 命中抛 BizException(PARAM_ERROR) 并记录安全日志
@Slf4j
@Aspect
@Component
public class SqlInjectionAspect {

    @Before("execution(* com.xianguoji.server.module..controller..*.*(..))")
    public void checkSqlInjection(JoinPoint jp) {
        String methodName = jp.getSignature().getName();
        // 仅对查询类方法检测
        if (!isQueryMethod(methodName)) {
            return;
        }

        String uri = getCurrentUri();
        String ip = getCurrentIp();

        for (Object arg : jp.getArgs()) {
            if (arg == null) continue;

            if (arg instanceof String str) {
                checkString(str, uri, ip);
            } else if (isDto(arg)) {
                checkDtoFields(arg, uri, ip);
            }
        }
    }

    private boolean isQueryMethod(String name) {
        return name.startsWith("get") || name.startsWith("list") || name.startsWith("page")
                || name.startsWith("search") || name.startsWith("query") || name.startsWith("find");
    }

    private void checkString(String value, String uri, String ip) {
        if (SqlInjectionValidator.containsSqlInjection(value)) {
            log.warn("[SQLI] uri={} ip={} value={}", uri, ip, truncate(value, 100));
            throw new BizException(ResultCode.PARAM_ERROR, "参数包含非法字符");
        }
    }

    private void checkDtoFields(Object dto, String uri, String ip) {
        Class<?> clazz = dto.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() != String.class) continue;
            field.setAccessible(true);
            try {
                String value = (String) field.get(dto);
                if (value != null && SqlInjectionValidator.containsSqlInjection(value)) {
                    log.warn("[SQLI] uri={} ip={} field={} value={}", uri, ip, field.getName(), truncate(value, 100));
                    throw new BizException(ResultCode.PARAM_ERROR, "参数包含非法字符");
                }
            } catch (IllegalAccessException ignored) {
                // 无法访问字段，跳过
            } catch (BizException e) {
                throw e;
            } catch (Exception ignored) {
                // 其他异常跳过
            }
        }
    }

    private boolean isDto(Object obj) {
        // 简单判断：非 JDK 类型、非基本类型、非 Map/Collection
        String className = obj.getClass().getName();
        return !className.startsWith("java.") && !(obj instanceof java.util.Map) && !(obj instanceof java.util.Collection);
    }

    private String getCurrentUri() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) return attrs.getRequest().getRequestURI();
        } catch (Exception ignored) {}
        return "unknown";
    }

    private String getCurrentIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) return attrs.getRequest().getRemoteAddr();
        } catch (Exception ignored) {}
        return "unknown";
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
