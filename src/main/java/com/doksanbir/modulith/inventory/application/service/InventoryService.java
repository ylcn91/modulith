package com.doksanbir.modulith.inventory.application.service;

import com.doksanbir.modulith.inventory.application.port.in.InventoryUseCase;
import com.doksanbir.modulith.inventory.application.port.out.InventoryRepositoryPort;
import com.doksanbir.modulith.inventory.domain.model.Inventory;
import com.doksanbir.modulith.inventory.web.dto.InventoryDTO;
import com.doksanbir.modulith.product.domain.event.*;
import com.doksanbir.modulith.shared.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService implements InventoryUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void initializeInventory(Long productId, Integer quantity) {
        log.info("Initializing inventory for productId: {}, quantity: {}", productId, quantity);
        Inventory inventory = Inventory.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
        inventoryRepositoryPort.save(inventory);
        // Decide if InventoryUpdatedEvent is necessary here
        // eventPublisher.publishEvent(new InventoryUpdatedEvent(this, productId, quantity));
    }

    @Override
    @Transactional
    public void updateInventory(Long productId, Integer quantity) {
        log.info("Updating inventory for productId: {}, quantity: {}", productId, quantity);
        Inventory inventory = inventoryRepositoryPort.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        inventory.setQuantity(quantity);
        inventoryRepositoryPort.save(inventory);
        // Decide if InventoryUpdatedEvent is necessary here
        // eventPublisher.publishEvent(new InventoryUpdatedEvent(this, productId, quantity));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDTO getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepositoryPort.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return mapToDTO(inventory);
    }

    // Event Listeners

    @EventListener
    @Transactional
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("Received ProductCreatedEvent for productId: {}", event.getProductId());
        initializeInventory(event.getProductId(), 0);
    }

    @EventListener
    @Transactional
    public void handleProductDeleted(ProductDeletedEvent event) {
        log.info("Received ProductDeletedEvent for productId: {}", event.getProductId());
        inventoryRepositoryPort.deleteByProductId(event.getProductId());
        // Decide if InventoryUpdatedEvent is necessary here
        // eventPublisher.publishEvent(new InventoryUpdatedEvent(this, event.getProductId(), null));
    }

    @EventListener
    @Transactional
    public void handleProductDiscontinued(ProductDiscontinuedEvent event) {
        log.info("Received ProductDiscontinuedEvent for productId: {}", event.getProductId());
        inventoryRepositoryPort.deleteByProductId(event.getProductId());
        // Decide if InventoryUpdatedEvent is necessary here
        // eventPublisher.publishEvent(new InventoryUpdatedEvent(this, event.getProductId(), null));
    }

    @EventListener
    @Transactional
    public void handleProductReactivated(ProductReactivatedEvent event) {
        log.info("Received ProductReactivatedEvent for productId: {}", event.getProductId());
        initializeInventory(event.getProductId(), 0);
    }

    @EventListener
    @Transactional
    public void handleProductStockUpdated(ProductStockUpdatedEvent event) {
        log.info("Received ProductStockUpdatedEvent for productId: {}, updatedStockQuantity: {}",
                event.getProductId(), event.getUpdatedStockQuantity());
        updateInventory(event.getProductId(), event.getUpdatedStockQuantity());
    }

    private InventoryDTO mapToDTO(Inventory inventory) {
        return new InventoryDTO(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity()
        );
    }
}
