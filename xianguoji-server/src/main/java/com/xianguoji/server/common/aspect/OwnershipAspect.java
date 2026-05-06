package com.xianguoji.server.common.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianguoji.server.common.annotation.OwnedBy;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.common.security.LoginUser;
import com.xianguoji.server.common.service.SecurityEventService;
import com.xianguoji.server.common.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * S-12: 越权防护切面
 * 拦截 @OwnedBy 注解的方法，校验资源归属
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OwnershipAspect {

    private final ApplicationContext applicationContext;
    private final SecurityEventService securityEventService;

    @Around("@annotation(ownedBy)")
    public Object checkOwnership(ProceedingJoinPoint pjp, OwnedBy ownedBy) throws Throwable {
        LoginUser loginUser = LoginContext.get();
        if (loginUser == null) {
            throw new BizException(ResultCode.TOKEN_INVALID);
        }

        // 管理员跳过归属校验
        if ("staff".equals(loginUser.getRole())) {
            return pjp.proceed();
        }

        // 获取资源 ID
        Long resourceId = extractResourceId(ownedBy);
        if (resourceId == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "资源ID不能为空");
        }

        // 查询资源实体
        Object entity = loadEntity(ownedBy.entity(), resourceId);
        if (entity == null) {
            throw new BizException(ResultCode.BIZ_ERROR, "资源不存在");
        }

        // 获取归属字段值
        Long ownerId = getFieldValue(entity, ownedBy.field());
        Long currentUid = loginUser.getUid();

        if (ownerId != null && !ownerId.equals(currentUid)) {
            // S-12 + S-17: 记录越权事件
            String ip = getCurrentIp();
            securityEventService.log("IDOR_ATTEMPT", ip,
                    "uid=" + currentUid + " tried to access " + ownedBy.entity().getSimpleName()
                            + " id=" + resourceId + " ownedBy=" + ownerId);
            log.warn("[S-12] IDOR: uid={} tried {} id={} ownedBy={}", currentUid,
                    ownedBy.entity().getSimpleName(), resourceId, ownerId);
            throw new BizException(ResultCode.ACCESS_DENIED, "无权访问该资源");
        }

        return pjp.proceed();
    }

    private Long extractResourceId(OwnedBy ownedBy) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();

        // 从 path variable 取
        String idStr = request.getParameter(ownedBy.idParam());
        if (idStr == null) {
            // 尝试从 URI path 中提取最后一个数字段
            String uri = request.getRequestURI();
            String[] parts = uri.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                try {
                    return Long.parseLong(parts[i]);
                } catch (NumberFormatException ignored) {}
            }
        }
        if (idStr != null) {
            try {
                return Long.parseLong(idStr);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Object loadEntity(Class<?> entityClass, Long id) {
        // 从 Spring 容器中找对应的 Mapper
        String mapperName = entityClass.getSimpleName().replace("Entity", "").replace("PO", "")
                + "Mapper";
        // 首字母小写
        mapperName = Character.toLowerCase(mapperName.charAt(0)) + mapperName.substring(1);
        try {
            BaseMapper<Object> mapper = (BaseMapper<Object>) applicationContext.getBean(mapperName);
            return mapper.selectById(id);
        } catch (Exception e) {
            log.warn("[S-12] 无法加载 Mapper: name={}, err={}", mapperName, e.getMessage());
            return null;
        }
    }

    private Long getFieldValue(Object entity, String fieldName) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(entity);
            if (value instanceof Long l) return l;
            if (value instanceof Integer i) return i.longValue();
            return null;
        } catch (Exception e) {
            log.warn("[S-12] 无法读取字段: field={}, err={}", fieldName, e.getMessage());
            return null;
        }
    }

    private String getCurrentIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        return IpUtil.getClientIp(attrs.getRequest());
    }
}
