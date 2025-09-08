package com.ecommerce.project.security.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class SignupRequest {

    @NotBlank
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank
    @Size(max=50, message = "Email must not exceed 50 characters")
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 40, message = "Password must be between 8 and 40 characters")
    private String password;

    private Set<String> roles;



}
