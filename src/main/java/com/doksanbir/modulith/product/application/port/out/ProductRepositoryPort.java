package com.doksanbir.modulith.product.application.port.out;

import com.doksanbir.modulith.product.domain.model.Product;

import java.util.Optional;
import java.util.List;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
    void deleteById(Long id);
}
