package com.doksanbir.modulith.inventory.web.dto;

public record InventoryDTO(
        Long id,
        Long productId,
        Integer quantity
) {}
