package com.ecommerce.project.service;

import com.ecommerce.project.payload.CartDTO;
import jakarta.transaction.Transactional;

import java.util.List;


public interface CartService {
    public CartDTO addProductToCart(Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getCartsByUser(String email, Long cartId);

    @Transactional
    CartDTO updateCartProduct(Long productId, Integer change);

    String deleteProductFromCart(Long cartId, Long productId);
}
