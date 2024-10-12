package com.doksanbir.modulith.order.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderPlacedEvent extends ApplicationEvent {
    private final Long orderId;

    public OrderPlacedEvent(Object source, Long orderId) {
        super(source);
        this.orderId = orderId;
    }
}
