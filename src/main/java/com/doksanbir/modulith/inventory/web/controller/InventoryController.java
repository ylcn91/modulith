package com.doksanbir.modulith.inventory.web.controller;

import com.doksanbir.modulith.inventory.application.port.in.InventoryUseCase;
import com.doksanbir.modulith.inventory.web.dto.InventoryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryUseCase inventoryUseCase;

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDTO> getInventory(@PathVariable Long productId) {
        log.info("Fetching inventory for productId: {}", productId);
        InventoryDTO inventory = inventoryUseCase.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Void> updateInventory(@PathVariable Long productId, @RequestBody InventoryDTO inventoryDTO) {
        log.info("Updating inventory for productId: {}", productId);
        inventoryUseCase.updateInventory(productId, inventoryDTO.quantity());
        return ResponseEntity.noContent().build();
    }
}
