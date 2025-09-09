package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long cartItemId ;
    private ProductDTO product ;
    private Integer quantity ;
    private Double productPrice ;
    private ProductDTO productDTO ;
    private Double discount;

}
