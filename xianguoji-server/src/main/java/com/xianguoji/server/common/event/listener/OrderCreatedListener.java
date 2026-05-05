package com.xianguoji.server.common.event.listener;

import com.xianguoji.server.common.cache.SalesRankService;
import com.xianguoji.server.common.event.OrderCreatedEvent;
import com.xianguoji.server.common.websocket.WsNotificationService;
import com.xianguoji.server.module.order.entity.OrderItem;
import com.xianguoji.server.module.order.mapper.OrderItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final OrderItemMapper orderItemMapper;
    private final SalesRankService salesRankService;

    @Async
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("[Event] 订单创建: orderNo={}", event.getOrderNo());
        try {
            // P1-6: 记录销量到 ZSet
            List<OrderItem> items = orderItemMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                            .eq(OrderItem::getOrderId, event.getOrderId()));
            for (OrderItem item : items) {
                salesRankService.incrementSales(item.getProductId(), item.getQuantity());
            }
        } catch (Exception e) {
            log.error("[Event] 处理订单创建事件失败: orderNo={}", event.getOrderNo(), e);
        }
    }
}
