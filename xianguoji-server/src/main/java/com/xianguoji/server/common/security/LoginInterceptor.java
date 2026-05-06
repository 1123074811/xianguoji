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
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtBlacklistManager jwtBlacklistManager;

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

        String token = extractToken(request);

        // 公开接口：无注解但携带 token 时，仍设置 LoginContext 以便 uidOptional() 可用
        if (loginRequired == null && adminRequired == null) {
            if (token != null && !token.isBlank()) {
                try {
                    Claims claims = jwtUtil.parse(token);
                    if (!jwtBlacklistManager.isBlacklisted(token)) {
                        String role = claims.get("role", String.class);
                        if ("staff".equals(role)) {
                            Long sid = claims.get("sid", Long.class);
                            String staffRole = claims.get("staffRole", String.class);
                            LoginContext.set(LoginUser.builder().sid(sid).role(role).staffRole(staffRole).build());
                        } else {
                            Long uid = claims.get("uid", Long.class);
                            LoginContext.set(LoginUser.builder().uid(uid).role(role).build());
                        }
                    }
                } catch (Exception ignored) {}
            }
            return true;
        }

        if (token == null || token.isBlank()) {
            throw new BizException(ResultCode.TOKEN_INVALID);
        }

        // P2-8: 检查 JWT 黑名单
        if (jwtBlacklistManager.isBlacklisted(token)) {
            throw new BizException(ResultCode.TOKEN_INVALID, "token 已失效");
        }

        Claims claims = jwtUtil.parse(token);
        String role = claims.get("role", String.class);

        // S-7: 仅 access token 可访问受保护接口，refresh token 仅用于 /api/auth/refresh
        String tokenType = claims.get("type", String.class);
        if ("refresh".equals(tokenType)) {
            throw new BizException(ResultCode.TOKEN_INVALID, "refresh token 不可用于业务接口");
        }

        // S-7: 设备指纹校验（仅 access token 含 fp）
        String fp = claims.get("fp", String.class);
        if (fp != null && !fp.isBlank()) {
            String currentFp = JwtUtil.fingerprint(null, request.getHeader("User-Agent"));
            if (!fp.equals(currentFp) && !currentFp.isBlank()) {
                log.warn("[LoginInterceptor] 指纹不匹配: tokenFp={}, currentFp={}, uri={}", fp, currentFp, request.getRequestURI());
                // 指纹不匹配时记录但不阻断（小程序 UA 可能变化），后续可收紧
            }
        }

        // Token 续期：剩余 < 1h 时签发新 access token，通过 response header 下发
        try {
            Date expiry = claims.getExpiration();
            long remainMs = expiry.getTime() - System.currentTimeMillis();
            if (remainMs > 0 && remainMs < 3600_000L) {
                String newToken;
                if ("staff".equals(role)) {
                    Long sid = claims.get("sid", Long.class);
                    String staffRole = claims.get("staffRole", String.class);
                    newToken = jwtUtil.issueAdminAccessToken(sid, staffRole, fp);
                } else {
                    Long uid = claims.get("uid", Long.class);
                    newToken = jwtUtil.issueUserAccessToken(uid, fp);
                }
                response.setHeader("X-Token-Renewal", newToken);
            }
        } catch (Exception ignored) {}

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

    /**
     * S-6: 仅从 Header 提取 token，禁止从 Cookie 读取，天然免疫 CSRF
     */
    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return request.getHeader("token");
    }
}
