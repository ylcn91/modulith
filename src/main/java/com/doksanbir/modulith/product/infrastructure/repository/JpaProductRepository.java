package com.doksanbir.modulith.product.infrastructure.repository;

import com.doksanbir.modulith.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductRepository extends JpaRepository<Product, Long> {
}
