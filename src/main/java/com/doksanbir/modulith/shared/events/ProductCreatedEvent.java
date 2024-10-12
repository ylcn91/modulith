package com.doksanbir.modulith.shared.events;

import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent(namespace = "product", name = "ProductCreated")
public record ProductCreatedEvent (Long productId){
}
