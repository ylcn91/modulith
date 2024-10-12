package com.doksanbir.modulith.product.web.dto;

import com.doksanbir.modulith.product.domain.model.ProductStatus;

import java.math.BigDecimal;

public record ProductDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        ProductStatus status
) {}
