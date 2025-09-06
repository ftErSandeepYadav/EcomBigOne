package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
public class User {

    public User(String userName, String email, String password) {
        this.userName = userName;
        this.userEmail = email;
        this.userPassword = password;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId ;

    @NotBlank
    @Size(max = 20)
    private String userName ;

    @NotBlank
    @Size(max = 50)
    @Email
    private String userEmail ;

    @NotBlank
    @Size(max = 120)
    private String userPassword ;

    @Getter
    @Setter
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

}
