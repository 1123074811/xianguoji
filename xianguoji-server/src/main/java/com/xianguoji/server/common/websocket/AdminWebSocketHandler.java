package com.xianguoji.server.common.websocket;

import com.xianguoji.server.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;

    private static final ConcurrentHashMap<String, WebSocketSession> ADMIN_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String role = (String) session.getAttributes().get("role");
        if (!"staff".equals(role)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            log.warn("WebSocket连接拒绝: 非staff角色");
            return;
        }
        ADMIN_SESSIONS.put(session.getId(), session);
        log.info("商家端WebSocket连接: sessionId={}, staffRole={}", session.getId(), session.getAttributes().get("staffRole"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        ADMIN_SESSIONS.remove(session.getId());
        log.info("商家端WebSocket断开: sessionId={}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);
        ADMIN_SESSIONS.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端发来的心跳/pong，忽略
    }

    public void broadcastToAdmin(String payload) {
        TextMessage msg = new TextMessage(payload);
        for (WebSocketSession s : ADMIN_SESSIONS.values()) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(msg);
                } catch (IOException e) {
                    log.error("WebSocket发送失败: sessionId={}", s.getId(), e);
                }
            }
        }
    }

    public int onlineCount() {
        return ADMIN_SESSIONS.size();
    }
}
