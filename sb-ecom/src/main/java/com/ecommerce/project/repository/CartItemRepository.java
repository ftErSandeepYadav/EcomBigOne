package com.ecommerce.project.repository;

import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Integer> {
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = ?1 AND ci.product.id = ?2")
    CartItem findCartItemByCartIdAndProductId(Long cartId, Long productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.product.id = ?1 AND ci.cart.cartId = ?2")
    void deleteCartItemByProductIdAndCartCartId(Long productId, Long cartId);
}
