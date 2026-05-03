package com.xianguoji.server.common.security;

import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        LoginRequired loginRequired = hm.getMethodAnnotation(LoginRequired.class);
        if (loginRequired == null) {
            loginRequired = hm.getBeanType().getAnnotation(LoginRequired.class);
        }
        AdminRequired adminRequired = hm.getMethodAnnotation(AdminRequired.class);
        if (adminRequired == null) {
            adminRequired = hm.getBeanType().getAnnotation(AdminRequired.class);
        }

        if (loginRequired == null && adminRequired == null) {
            return true;
        }

        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            throw new BizException(ResultCode.TOKEN_INVALID);
        }

        Claims claims = jwtUtil.parse(token);
        String role = claims.get("role", String.class);

        if (adminRequired != null) {
            if (!"staff".equals(role)) {
                throw new BizException(ResultCode.ACCESS_DENIED);
            }
            String staffRole = claims.get("staffRole", String.class);
            String[] requiredRoles = adminRequired.roles();
            if (requiredRoles.length > 0) {
                boolean match = Arrays.asList(requiredRoles).contains(staffRole);
                if (!match) {
                    throw new BizException(ResultCode.ACCESS_DENIED);
                }
            }
            Long sid = claims.get("sid", Long.class);
            LoginUser loginUser = LoginUser.builder()
                    .sid(sid)
                    .role(role)
                    .staffRole(staffRole)
                    .build();
            LoginContext.set(loginUser);
        } else {
            Long uid = claims.get("uid", Long.class);
            LoginUser loginUser = LoginUser.builder()
                    .uid(uid)
                    .role(role)
                    .build();
            LoginContext.set(loginUser);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return request.getHeader("token");
    }
}
