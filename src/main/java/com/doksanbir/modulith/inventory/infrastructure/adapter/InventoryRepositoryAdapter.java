package com.doksanbir.modulith.inventory.infrastructure.adapter;

import com.doksanbir.modulith.inventory.application.port.out.InventoryRepositoryPort;
import com.doksanbir.modulith.inventory.domain.model.Inventory;
import com.doksanbir.modulith.inventory.infrastructure.repository.JpaInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepositoryPort {

    private final JpaInventoryRepository jpaInventoryRepository;

    @Override
    public void save(Inventory inventory) {
        jpaInventoryRepository.save(inventory);
    }

    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        return jpaInventoryRepository.findByProductId(productId);
    }

    @Override
    public void deleteByProductId(Long productId) {
        jpaInventoryRepository.deleteByProductId(productId);
    }
}
