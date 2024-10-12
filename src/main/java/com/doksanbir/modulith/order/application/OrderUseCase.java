package com.doksanbir.modulith.order.application;

import java.util.List;

public interface OrderUseCase {
    OrderDTO placeOrder(OrderDTO orderDTO);
    OrderDTO getOrderById(Long id);
    List<OrderDTO> getAllOrders();
    OrderDTO updateOrder(Long id, OrderDTO orderDTO);
    void deleteOrder(Long id);
}
