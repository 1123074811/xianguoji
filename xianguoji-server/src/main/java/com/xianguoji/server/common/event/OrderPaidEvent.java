package com.xianguoji.server.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderPaidEvent extends ApplicationEvent {

    private final String orderNo;
    private final Long orderId;
    private final String payAmount;
    private final Integer deliveryType;

    public OrderPaidEvent(Object source, String orderNo, Long orderId, String payAmount, Integer deliveryType) {
        super(source);
        this.orderNo = orderNo;
        this.orderId = orderId;
        this.payAmount = payAmount;
        this.deliveryType = deliveryType;
    }
}
