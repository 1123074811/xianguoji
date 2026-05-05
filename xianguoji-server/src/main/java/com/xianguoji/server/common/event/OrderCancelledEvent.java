package com.xianguoji.server.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCancelledEvent extends ApplicationEvent {

    private final String orderNo;
    private final Long orderId;
    private final String reason;

    public OrderCancelledEvent(Object source, String orderNo, Long orderId, String reason) {
        super(source);
        this.orderNo = orderNo;
        this.orderId = orderId;
        this.reason = reason;
    }
}
