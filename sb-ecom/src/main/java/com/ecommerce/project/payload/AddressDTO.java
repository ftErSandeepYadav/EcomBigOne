package com.ecommerce.project.payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private Long addressId ;

    @NotBlank
    @Size(min = 5, message = "Street must be at least 5 characters long")
    private String street ;
    private String city ;
    private String state ;

    @NotBlank
    @Size(min = 6, max = 6, message = "Zip code must be 6 characters long")
    private String pinCode ;

}
