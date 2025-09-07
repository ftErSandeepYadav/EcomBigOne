package com.ecommerce.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
public class Role {

    public Role(AppRole appRole) {
        this.roleName = appRole;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId ;

    @ToString.Exclude
    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "role_name")
    private AppRole roleName ;

}
