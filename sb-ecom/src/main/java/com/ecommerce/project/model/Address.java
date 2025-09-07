package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.stream.XMLInputFactory;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId ;

    @NotBlank
    @Size(min = 5, message = "Street must be at least 5 characters long")
    private String street ;
    private String city ;
    private String state ;

    @NotBlank
    @Size(min = 6, max = 6, message = "Zip code must be 6 characters long")
    private String pinCode ;

    @ToString.Exclude
    @ManyToMany(mappedBy = "addresses")
    private List<User> users ;

}
