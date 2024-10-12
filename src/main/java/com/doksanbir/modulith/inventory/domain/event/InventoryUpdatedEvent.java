package com.doksanbir.modulith.inventory.domain.event;

public record InventoryUpdatedEvent (Long productId, Integer quantity) {

}
