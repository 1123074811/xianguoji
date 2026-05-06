package com.xianguoji.server.common.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xianguoji.server.module.shop.entity.AdminNotification;
import com.xianguoji.server.module.shop.mapper.AdminNotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WsNotificationService {

    private final AdminWebSocketHandler adminWebSocketHandler;
    private final ObjectMapper objectMapper;
    private final AdminNotificationMapper adminNotificationMapper;

    public void notifyNewOrder(String orderNo, String payAmount, String deliveryType) {
        String title = "新订单待处理";
        String content = "您有新的订单 " + orderNo + "，金额 ¥" + payAmount + "，配送方式：" + deliveryType;
        AdminNotification notification = createNotification(1, title, content, "/orders");
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "NEW_ORDER");
        msg.put("orderNo", orderNo);
        msg.put("payAmount", payAmount);
        msg.put("deliveryType", deliveryType);
        msg.put("title", title);
        msg.put("content", content);
        fillNotificationPayload(msg, notification);
        broadcast(msg);
    }

    public void notifyRefundApply(String orderNo, String refundAmount) {
        String title = "退款申请待处理";
        String content = "订单 " + orderNo + " 申请退款 ¥" + refundAmount;
        AdminNotification notification = createNotification(1, title, content, "/orders");
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "REFUND_APPLY");
        msg.put("orderNo", orderNo);
        msg.put("refundAmount", refundAmount);
        msg.put("title", title);
        msg.put("content", content);
        fillNotificationPayload(msg, notification);
        broadcast(msg);
    }

    public void notifyRemindShip(String orderNo) {
        String title = "催单提醒";
        String content = "用户催促发货，订单 " + orderNo;
        AdminNotification notification = createNotification(1, title, content, "/orders");
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "REMIND_SHIP");
        msg.put("orderNo", orderNo);
        msg.put("title", title);
        msg.put("content", content);
        fillNotificationPayload(msg, notification);
        broadcast(msg);
    }

    public void notifyReviewSubmitted(Long reviewId, String productName, Integer rating) {
        String title = rating != null && rating <= 2 ? "收到差评待回复" : "收到新评价";
        String content = "客户评价了「" + productName + "」，评分 " + rating + " 星";
        AdminNotification notification = createNotification(3, title, content, "/reviews");
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "REVIEW_SUBMITTED");
        msg.put("reviewId", reviewId);
        msg.put("title", title);
        msg.put("content", content);
        fillNotificationPayload(msg, notification);
        broadcast(msg);
    }

    public void notifyStockWarn(Long productId, String productName, Integer totalStock, Integer threshold) {
        String title = "库存预警";
        String content = "「" + productName + "」当前库存 " + totalStock + " 件，已低于预警阈值 " + threshold + " 件";
        AdminNotification notification = createNotification(2, title, content, "/goods");
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "STOCK_WARN");
        msg.put("productId", productId);
        msg.put("title", title);
        msg.put("content", content);
        fillNotificationPayload(msg, notification);
        broadcast(msg);
    }

    public void notifyMarketing(String title, String content, String linkUrl) {
        AdminNotification notification = createNotification(4, title, content, linkUrl);
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "MARKETING");
        msg.put("title", title);
        msg.put("content", content);
        fillNotificationPayload(msg, notification);
        broadcast(msg);
    }

    public void notifySystem(String title, String content, String linkUrl) {
        AdminNotification notification = createNotification(5, title, content, linkUrl);
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "SYSTEM");
        msg.put("title", title);
        msg.put("content", content);
        fillNotificationPayload(msg, notification);
        broadcast(msg);
    }

    private AdminNotification createNotification(Integer type, String title, String content, String linkUrl) {
        try {
            AdminNotification notification = new AdminNotification();
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setLinkUrl(linkUrl);
            notification.setIsRead(0);
            notification.setCreatedAt(LocalDateTime.now());
            adminNotificationMapper.insert(notification);
            return notification;
        } catch (RuntimeException e) {
            log.error("商家通知写入失败: title={}", title, e);
            return null;
        }
    }

    private void fillNotificationPayload(Map<String, Object> msg, AdminNotification notification) {
        if (notification == null) return;
        msg.put("notificationId", notification.getId());
        msg.put("notificationType", notification.getType());
        msg.put("linkUrl", notification.getLinkUrl());
        msg.put("isRead", notification.getIsRead());
        msg.put("createdAt", notification.getCreatedAt());
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
