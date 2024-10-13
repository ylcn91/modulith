package com.doksanbir.modulith.inventory.application.service;

import com.doksanbir.modulith.inventory.application.port.in.InventoryUseCase;
import com.doksanbir.modulith.inventory.application.port.out.InventoryRepositoryPort;
import com.doksanbir.modulith.inventory.domain.model.Inventory;
import com.doksanbir.modulith.inventory.web.dto.InventoryDTO;
import com.doksanbir.modulith.shared.ProductNotFoundException;
import com.doksanbir.modulith.shared.events.*;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
class InventoryService implements InventoryUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Override
    public void initializeInventory(Long productId, Integer quantity) {
        log.info("Initializing inventory for productId: {}, quantity: {}", productId, quantity);
        Inventory inventory = Inventory.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
        inventoryRepositoryPort.save(inventory);
    }

    @Override
    @Retryable(
            retryFor = OptimisticLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void updateInventory(Long productId, Integer quantity) {
        log.info("Updating inventory for productId: {}, quantity: {}", productId, quantity);
        Inventory inventory = inventoryRepositoryPort.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        inventory.setQuantity(quantity);
        inventoryRepositoryPort.save(inventory);

    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDTO getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepositoryPort.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return mapToDTO(inventory);
    }


    @ApplicationModuleListener
    void handleProductEvent(Object event) {
        log.info("Received event: {}", event.getClass().getSimpleName());
        switch (event) {
            case ProductCreatedEvent e -> initializeInventory(e.productId(), 0);
            case ProductDeletedEvent e -> inventoryRepositoryPort.deleteByProductId(e.productId());
            case ProductDiscontinuedEvent e -> inventoryRepositoryPort.deleteByProductId(e.productId());
            case ProductReactivatedEvent e -> initializeInventory(e.productId(), 0);
            case ProductStockUpdatedEvent e -> updateInventory(e.productId(), e.stockChange());
            default -> log.warn("Unhandled event type: {}", event.getClass().getSimpleName());
        }
    }


    private InventoryDTO mapToDTO(Inventory inventory) {
        return new InventoryDTO(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity()
        );
    }
}
