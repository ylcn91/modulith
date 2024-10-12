package com.doksanbir.modulith.inventory.application.port.in;


import com.doksanbir.modulith.inventory.web.dto.InventoryDTO;

public interface InventoryUseCase {
    void initializeInventory(Long productId, Integer quantity);
    void updateInventory(Long productId, Integer quantity);
    InventoryDTO getInventoryByProductId(Long productId);
}
