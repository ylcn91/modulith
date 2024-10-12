package com.doksanbir.modulith.product.application.service;

import com.doksanbir.modulith.product.application.port.in.ProductUseCase;
import com.doksanbir.modulith.product.application.port.out.ProductRepositoryPort;
import com.doksanbir.modulith.product.domain.event.*;
import com.doksanbir.modulith.product.domain.model.Product;
import com.doksanbir.modulith.product.domain.model.ProductStatus;
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
                .status(ProductStatus.ACTIVE) // Set default status
                .build();
        Product savedProduct = productRepositoryPort.save(product);
        eventPublisher.publishEvent(new ProductCreatedEvent(this, savedProduct.getId()));
        log.info("Product created: {}", savedProduct);
        return mapToDTO(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
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

        // Capture original state
        ProductStatus oldStatus = existingProduct.getStatus();
        Integer oldStockQuantity = existingProduct.getStockQuantity();

        // Update product fields
        updateProductFields(existingProduct, productDTO);

        // Save updated product
        Product updatedProduct = productRepositoryPort.save(existingProduct);

        // Publish relevant events based on changes
        publishStatusChangeEvents(oldStatus, updatedProduct.getStatus(), updatedProduct.getId());
        publishStockChangeEvent(oldStockQuantity, updatedProduct.getStockQuantity(), updatedProduct.getId());

        return mapToDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        Product existingProduct = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepositoryPort.deleteById(id);
        eventPublisher.publishEvent(new ProductDeletedEvent(this, id));
    }

    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus()
        );
    }

    private void updateProductFields(Product product, ProductDTO productDTO) {
        product.setName(productDTO.name());
        product.setDescription(productDTO.description());
        product.setPrice(productDTO.price());
        product.setStockQuantity(productDTO.stockQuantity());
        product.setStatus(productDTO.status());
    }

    private void publishStatusChangeEvents(ProductStatus oldStatus, ProductStatus newStatus, Long productId) {
        if (!oldStatus.equals(newStatus)) {
            switch (newStatus) {
                case DISCONTINUED:
                    eventPublisher.publishEvent(new ProductDiscontinuedEvent(this, productId));
                    break;
                case ACTIVE:
                    eventPublisher.publishEvent(new ProductReactivatedEvent(this, productId));
                    break;
                default:
                    break;
            }
        }
    }

    private void publishStockChangeEvent(Integer oldStock, Integer newStock, Long productId) {
        if (!oldStock.equals(newStock)) {
            eventPublisher.publishEvent(new ProductStockUpdatedEvent(this, productId, newStock));
        }
    }
}
