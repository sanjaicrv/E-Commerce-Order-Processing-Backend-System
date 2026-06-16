package com.example.ecommerce.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewJpa extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findByProductProductId(Long productId);
}
