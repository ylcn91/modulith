package com.doksanbir.modulith.shared.events;

import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent(namespace = "product", name = "ProductReactivated")
public record ProductReactivatedEvent(Long productId) {
}
