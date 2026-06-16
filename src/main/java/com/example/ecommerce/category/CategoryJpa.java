package com.example.ecommerce.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryJpa extends JpaRepository<CategoryEntity, Long> {
    boolean existsByName(String name);
}
