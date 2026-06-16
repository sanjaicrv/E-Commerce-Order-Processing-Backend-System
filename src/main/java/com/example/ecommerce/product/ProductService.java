package com.example.ecommerce.product;

import com.example.ecommerce.category.CategoryEntity;
import com.example.ecommerce.category.CategoryJpa;
import com.example.ecommerce.exception.CategoryNotFoundException;
import com.example.ecommerce.exception.ProductNotFoundException;
import com.example.ecommerce.review.ReviewEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService implements ProductServiceInterface {

    @Autowired
    private ProductJpa productJpa;

    @Autowired
    private CategoryJpa categoryJpa;

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        CategoryEntity category = categoryJpa.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + productDTO.getCategoryId()));

        ProductEntity product = ProductEntity.builder()
                .category(category)
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .brand(productDTO.getBrand())
                .price(productDTO.getPrice())
                .isActive(productDTO.getIsActive() != null ? productDTO.getIsActive() : true)
                .build();

        ProductEntity saved = productJpa.save(product);
        return convertToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        Page<ProductEntity> page = productJpa.findAll(pageable);
        List<ProductDTO> dtos = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        ProductEntity product = productJpa.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return convertToDTO(product);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        ProductEntity product = productJpa.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        CategoryEntity category = categoryJpa.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + productDTO.getCategoryId()));

        product.setCategory(category);
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBrand(productDTO.getBrand());
        product.setPrice(productDTO.getPrice());
        if (productDTO.getIsActive() != null) {
            product.setIsActive(productDTO.getIsActive());
        }

        ProductEntity updated = productJpa.save(product);
        return convertToDTO(updated);
    }

    @Override
    public void deleteProduct(Long id) {
        ProductEntity product = productJpa.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        productJpa.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        if (!categoryJpa.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found with id: " + categoryId);
        }
        return productJpa.findByCategoryCategoryId(categoryId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword) {
        return productJpa.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(keyword, keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getSortedProductsByPrice(String direction) {
        List<ProductDTO> list = productJpa.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toCollection(ArrayList::new));

        Comparator<ProductDTO> comparator = Comparator.comparing(ProductDTO::getPrice);
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        // Custom Merge Sort DSA implementation
        mergeSort(list, comparator);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getSortedProductsByRating() {
        List<ProductDTO> list = productJpa.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toCollection(ArrayList::new));

        Comparator<ProductDTO> comparator = Comparator.comparing(ProductDTO::getAverageRating).reversed();
        mergeSort(list, comparator);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getSortedProductsByPopularity() {
        List<ProductDTO> list = productJpa.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toCollection(ArrayList::new));

        Comparator<ProductDTO> comparator = Comparator.comparing(ProductDTO::getPopularity).reversed();
        mergeSort(list, comparator);
        return list;
    }

    // Merge Sort Helper
    private void mergeSort(List<ProductDTO> list, Comparator<ProductDTO> comparator) {
        if (list.size() < 2) return;
        int mid = list.size() / 2;
        List<ProductDTO> left = new ArrayList<>(list.subList(0, mid));
        List<ProductDTO> right = new ArrayList<>(list.subList(mid, list.size()));
        
        mergeSort(left, comparator);
        mergeSort(right, comparator);
        
        merge(list, left, right, comparator);
    }

    private void merge(List<ProductDTO> list, List<ProductDTO> left, List<ProductDTO> right, Comparator<ProductDTO> comparator) {
        int i = 0, j = 0, k = 0;
        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                list.set(k++, left.get(i++));
            } else {
                list.set(k++, right.get(j++));
            }
        }
        while (i < left.size()) {
            list.set(k++, left.get(i++));
        }
        while (j < right.size()) {
            list.set(k++, right.get(j++));
        }
    }

    private ProductDTO convertToDTO(ProductEntity entity) {
        double avgRating = 0.0;
        int pop = 0;
        if (entity.getReviews() != null && !entity.getReviews().isEmpty()) {
            avgRating = entity.getReviews().stream()
                    .mapToInt(ReviewEntity::getRating)
                    .average()
                    .orElse(0.0);
            pop = entity.getReviews().size();
        }

        return ProductDTO.builder()
                .productId(entity.getProductId())
                .categoryId(entity.getCategory().getCategoryId())
                .name(entity.getName())
                .description(entity.getDescription())
                .brand(entity.getBrand())
                .price(entity.getPrice())
                .isActive(entity.getIsActive())
                .averageRating(avgRating)
                .popularity(pop)
                .build();
    }
}
