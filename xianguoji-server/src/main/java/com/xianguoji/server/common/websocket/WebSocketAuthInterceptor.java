package com.xianguoji.server.common.websocket;

import com.xianguoji.server.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        String token = extractToken(query);
        if (token == null || token.isBlank()) {
            log.warn("WebSocket握手拒绝: 无token");
            return false;
        }
        try {
            Claims claims = jwtUtil.parse(token);
            String role = claims.get("role", String.class);
            if (!"staff".equals(role)) {
                log.warn("WebSocket握手拒绝: 非staff角色 role={}", role);
                return false;
            }
            attributes.put("role", role);
            attributes.put("sid", claims.get("sid", Long.class));
            attributes.put("staffRole", claims.get("staffRole", String.class));
            return true;
        } catch (Exception e) {
            log.warn("WebSocket握手拒绝: token解析失败", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String extractToken(String query) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if ("token".equals(kv[0]) && kv.length == 2) {
                return kv[1];
            }
        }
        return null;
    }
}
