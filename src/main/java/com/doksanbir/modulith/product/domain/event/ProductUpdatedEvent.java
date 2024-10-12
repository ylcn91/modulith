package com.doksanbir.modulith.product.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductUpdatedEvent extends ApplicationEvent {
    private final Long productId;

    public ProductUpdatedEvent(Object source, Long productId) {
        super(source);
        this.productId = productId;
    }
}
