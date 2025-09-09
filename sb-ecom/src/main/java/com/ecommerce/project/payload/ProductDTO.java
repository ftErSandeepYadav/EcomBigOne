package com.ecommerce.project.payload;

import com.ecommerce.project.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long productId ;

    @NotBlank
    @Size(min = 3, message = "Product name must be at least 3 characters long")
    private String productName ;

    private String imageUrl ;

    @NotBlank
    @Size(min = 6, message = "Product name must be at least 6 characters long")
    private String description ;
    private Integer quantity ;
    private Double price ;
    private Double discount ;
    private Double specialPrice ;
    private CategoryDTO category;
}
