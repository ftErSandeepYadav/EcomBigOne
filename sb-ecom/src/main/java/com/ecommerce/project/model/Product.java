package com.ecommerce.project.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

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

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category category ;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user ;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private List<CartItem> cartItems = new ArrayList<>();

}
