package com.doksanbir.modulith.order.application;

import com.doksanbir.modulith.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderDTO(
        Long id,
        Long customerId,
        List<OrderItemDTO> items,
        OrderStatus status
) {
    public record OrderItemDTO(
            Long productId,
            Integer quantity,
            BigDecimal price
    ) {}
}
