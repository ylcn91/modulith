package com.doksanbir.modulith.product.application.service;

import com.doksanbir.modulith.product.application.port.in.ProductUseCase;
import com.doksanbir.modulith.product.application.port.out.ProductRepositoryPort;
import com.doksanbir.modulith.product.domain.event.ProductCreatedEvent;
import com.doksanbir.modulith.product.domain.event.ProductDeletedEvent;
import com.doksanbir.modulith.product.domain.event.ProductUpdatedEvent;
import com.doksanbir.modulith.product.domain.model.Product;
import com.doksanbir.modulith.product.web.dto.ProductDTO;
import com.doksanbir.modulith.shared.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService implements ProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating product: {}", productDTO);
        Product product = Product.builder()
                .name(productDTO.name())
                .description(productDTO.description())
                .price(productDTO.price())
                .stockQuantity(productDTO.stockQuantity())
                .build();
        Product savedProduct = productRepositoryPort.save(product);
        eventPublisher.publishEvent(new ProductCreatedEvent(this, savedProduct.getId()));
        log.info("Product created: {}", savedProduct);
        return mapToDTO(savedProduct);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepositoryPort.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        existingProduct.setName(productDTO.name());
        existingProduct.setDescription(productDTO.description());
        existingProduct.setPrice(productDTO.price());
        existingProduct.setStockQuantity(productDTO.stockQuantity());
        Product updatedProduct = productRepositoryPort.save(existingProduct);
        eventPublisher.publishEvent(new ProductUpdatedEvent(this, updatedProduct.getId()));
        return mapToDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        productRepositoryPort.deleteById(id);
        eventPublisher.publishEvent(new ProductDeletedEvent(this, id));
    }

    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity()
        );
    }
}
