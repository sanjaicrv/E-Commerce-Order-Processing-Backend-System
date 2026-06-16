package com.example.ecommerce.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductServiceInterface {
    ProductDTO createProduct(ProductDTO productDTO);
    Page<ProductDTO> getAllProducts(Pageable pageable);
    ProductDTO getProductById(Long id);
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    void deleteProduct(Long id);
    List<ProductDTO> getProductsByCategoryId(Long categoryId);
    List<ProductDTO> searchProducts(String keyword);
    List<ProductDTO> getSortedProductsByPrice(String direction);
    List<ProductDTO> getSortedProductsByRating();
    List<ProductDTO> getSortedProductsByPopularity();
}
