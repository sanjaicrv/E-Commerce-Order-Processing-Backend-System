package com.example.ecommerce.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJpa extends JpaRepository<ProductEntity, Long> {
    List<ProductEntity> findByCategoryCategoryId(Long categoryId);
    List<ProductEntity> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(String keyword, String brandKeyword);
    Page<ProductEntity> findByIsActiveTrue(Pageable pageable);
}
