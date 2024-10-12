package com.doksanbir.modulith.order.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductStockDecreasedEvent extends ApplicationEvent {
    private final Long productId;
    private final Integer quantity;

    public ProductStockDecreasedEvent(Object source, Long productId, Integer quantity) {
        super(source);
        this.productId = productId;
        this.quantity = quantity;
    }
}
