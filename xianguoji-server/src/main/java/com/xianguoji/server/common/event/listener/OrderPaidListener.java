package com.xianguoji.server.common.event.listener;

import com.xianguoji.server.common.event.OrderPaidEvent;
import com.xianguoji.server.common.websocket.WsNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidListener {

    private final WsNotificationService wsNotificationService;

    @Async
    @EventListener
    public void onOrderPaid(OrderPaidEvent event) {
        log.info("[Event] 订单支付: orderNo={}", event.getOrderNo());
        try {
            // WebSocket 推送新订单通知给商家
            wsNotificationService.notifyNewOrder(
                    event.getOrderNo(),
                    event.getPayAmount(),
                    event.getDeliveryType() != null ? String.valueOf(event.getDeliveryType()) : "1");
        } catch (Exception e) {
            log.error("[Event] 处理订单支付事件失败: orderNo={}", event.getOrderNo(), e);
        }
    }
}
