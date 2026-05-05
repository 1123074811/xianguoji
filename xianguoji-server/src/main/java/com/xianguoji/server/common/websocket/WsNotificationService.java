package com.xianguoji.server.common.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WsNotificationService {

    private final AdminWebSocketHandler adminWebSocketHandler;
    private final ObjectMapper objectMapper;

    public void notifyNewOrder(String orderNo, String payAmount, String deliveryType) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "NEW_ORDER");
        msg.put("orderNo", orderNo);
        msg.put("payAmount", payAmount);
        msg.put("deliveryType", deliveryType);
        msg.put("title", "新订单");
        msg.put("content", "您有新的订单 " + orderNo + "，金额 ¥" + payAmount);
        broadcast(msg);
    }

    public void notifyRefundApply(String orderNo, String refundAmount) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "REFUND_APPLY");
        msg.put("orderNo", orderNo);
        msg.put("refundAmount", refundAmount);
        msg.put("title", "退款申请");
        msg.put("content", "订单 " + orderNo + " 申请退款 ¥" + refundAmount);
        broadcast(msg);
    }

    public void notifyRemindShip(String orderNo) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "REMIND_SHIP");
        msg.put("orderNo", orderNo);
        msg.put("title", "催单提醒");
        msg.put("content", "用户催促发货，订单 " + orderNo);
        broadcast(msg);
    }

    private void broadcast(Map<String, Object> msg) {
        try {
            String json = objectMapper.writeValueAsString(msg);
            adminWebSocketHandler.broadcastToAdmin(json);
            log.info("WebSocket推送: {}", json);
        } catch (JsonProcessingException e) {
            log.error("WebSocket消息序列化失败", e);
        }
    }
}
