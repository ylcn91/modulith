package com.doksanbir.modulith.inventory.application.service;

import com.doksanbir.modulith.inventory.application.port.in.InventoryUseCase;
import com.doksanbir.modulith.inventory.application.port.out.InventoryRepositoryPort;
import com.doksanbir.modulith.inventory.domain.model.Inventory;
import com.doksanbir.modulith.inventory.web.dto.InventoryDTO;
import com.doksanbir.modulith.product.*;
import com.doksanbir.modulith.product.ProductCreatedEvent;
import com.doksanbir.modulith.shared.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
class InventoryService implements InventoryUseCase {

    private final InventoryRepositoryPort inventoryRepositoryPort;

    //TODO: check if this is necessary later.
    private final ApplicationEventPublisher eventPublisher;

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
    @Transactional
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

    // Event Listeners

    @ApplicationModuleListener
    void handleProductCreated(ProductCreatedEvent event) {
        log.info("Received ProductCreatedEvent for productId: {}", event.productId());
        initializeInventory(event.productId(), 0);
    }

    @ApplicationModuleListener
    public void handleProductDeleted(ProductDeletedEvent event) {
        log.info("Received ProductDeletedEvent for productId: {}", event.productId());
        inventoryRepositoryPort.deleteByProductId(event.productId());

    }

    @ApplicationModuleListener
    public void handleProductDiscontinued(ProductDiscontinuedEvent event) {
        log.info("Received ProductDiscontinuedEvent for productId: {}", event.productId());
        inventoryRepositoryPort.deleteByProductId(event.productId());
    }

    @ApplicationModuleListener
    public void handleProductReactivated(ProductReactivatedEvent event) {
        log.info("Received ProductReactivatedEvent for productId: {}", event.productId());
        initializeInventory(event.productId(), 0);
    }

    /*
    @Async
    @EventListener
    @TransactionalEventListener
     */
    @ApplicationModuleListener
    public void handleProductStockUpdated(ProductStockUpdatedEvent event) {
        log.info("Received ProductStockUpdatedEvent for productId: {}, updatedStockQuantity: {}",
                event.productId(), event.stockChange());
        updateInventory(event.productId(), event.stockChange());
    }

    private InventoryDTO mapToDTO(Inventory inventory) {
        return new InventoryDTO(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity()
        );
    }
}
