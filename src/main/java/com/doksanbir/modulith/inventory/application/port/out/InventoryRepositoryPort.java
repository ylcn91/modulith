package com.doksanbir.modulith.inventory.application.port.out;

import com.doksanbir.modulith.inventory.domain.model.Inventory;

import java.util.Optional;

public interface InventoryRepositoryPort {
    void save(Inventory inventory);
    Optional<Inventory> findByProductId(Long productId);
    void deleteByProductId(Long productId);
}
