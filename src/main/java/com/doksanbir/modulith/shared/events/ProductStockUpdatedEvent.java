package com.doksanbir.modulith.shared.events;

import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent(namespace = "product", name = "ProductStockUpdated")
public record ProductStockUpdatedEvent(Long productId, Integer stockChange) {
}
