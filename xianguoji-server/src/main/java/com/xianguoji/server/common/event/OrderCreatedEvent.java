package com.xianguoji.server.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCreatedEvent extends ApplicationEvent {

    private final String orderNo;
    private final Long orderId;
    private final Long userId;

    public OrderCreatedEvent(Object source, String orderNo, Long orderId, Long userId) {
        super(source);
        this.orderNo = orderNo;
        this.orderId = orderId;
        this.userId = userId;
    }
}
