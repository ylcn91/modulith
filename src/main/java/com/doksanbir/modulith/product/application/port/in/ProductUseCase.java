package com.doksanbir.modulith.product.application.port.in;


import com.doksanbir.modulith.product.web.dto.ProductDTO;

import java.util.List;

public interface ProductUseCase {
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO getProductById(Long id);
    List<ProductDTO> getAllProducts();
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    void deleteProduct(Long id);
}
