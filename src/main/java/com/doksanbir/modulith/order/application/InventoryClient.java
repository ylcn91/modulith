package com.doksanbir.modulith.order.application;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventoryClient", url = "http://localhost:8080/api/inventories")
public interface InventoryClient {

    @GetMapping("/{productId}")
    int getInventory(@PathVariable Long productId);

    @PutMapping("/{productId}")
    ResponseEntity<Void> updateInventory(@PathVariable Long productId, @RequestBody int quantity);
}

