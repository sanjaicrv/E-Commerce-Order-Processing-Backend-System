package com.example.ecommerce.category;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private Long categoryId;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
}
