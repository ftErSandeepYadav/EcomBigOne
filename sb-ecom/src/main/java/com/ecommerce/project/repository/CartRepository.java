package com.ecommerce.project.repository;

import com.ecommerce.project.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {

    @Query("SELECT c FROM Cart c WHERE c.user.userEmail = ?1")
    Cart findCartByUserEmail(String userEmail);

//    @Query("SELECT c FROM Cart c WHERE c.user.userEmail = ?1 AND c.cartId = ?2")
    Cart findCartByUserUserEmailAndCartId(String email, Long cartId);

    @Query("SELECT c FROM Cart c JOIN c.cartItems ci WHERE ci.product.id = ?1")
    List<Cart> findByProductId(Long productId);
}
