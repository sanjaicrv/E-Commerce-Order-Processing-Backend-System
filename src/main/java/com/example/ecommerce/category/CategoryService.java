package com.example.ecommerce.category;

import com.example.ecommerce.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService implements CategoryServiceInterface {

    @Autowired
    private CategoryJpa categoryJpa;

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryJpa.existsByName(categoryDTO.getName())) {
            throw new IllegalArgumentException("Category name already exists: " + categoryDTO.getName());
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .build();

        CategoryEntity saved = categoryJpa.save(category);
        return convertToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryJpa.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        CategoryEntity category = categoryJpa.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        return convertToDTO(category);
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        CategoryEntity category = categoryJpa.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        CategoryEntity updated = categoryJpa.save(category);
        return convertToDTO(updated);
    }

    @Override
    public void deleteCategory(Long id) {
        CategoryEntity category = categoryJpa.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        categoryJpa.delete(category);
    }

    private CategoryDTO convertToDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .categoryId(entity.getCategoryId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
