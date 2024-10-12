package com.doksanbir.modulith.product;

public record ProductStockUpdatedEvent(Long productId, Integer stockChange) {
}
