package com.doksanbir.modulith.inventory.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InventoryUpdatedEvent extends ApplicationEvent {
    private final Long productId;
    private final Integer quantity;

    public InventoryUpdatedEvent(Object source, Long productId, Integer quantity) {
        super(source);
        this.productId = productId;
        this.quantity = quantity;
    }
}
