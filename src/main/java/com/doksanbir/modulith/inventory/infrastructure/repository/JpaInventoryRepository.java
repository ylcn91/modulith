package com.doksanbir.modulith.inventory.infrastructure.repository;

import com.doksanbir.modulith.inventory.domain.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaInventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);
    void deleteByProductId(Long productId);
}
