package com.doksanbir.modulith.product.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductStockUpdatedEvent extends ApplicationEvent {
    private final Long productId;
    private final Integer updatedStockQuantity;

    public ProductStockUpdatedEvent(Object source, Long productId, Integer updatedStockQuantity) {
        super(source);
        this.productId = productId;
        this.updatedStockQuantity = updatedStockQuantity;
    }
}
