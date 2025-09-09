package com.ecommerce.project.controller;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    CartService cartService;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    AuthUtil authUtil;

    @PostMapping("/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addToCart(@PathVariable Long productId, @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CartDTO>> getAllCarts() {
        List<CartDTO> cartDTOS = cartService.getAllCarts();
        return new ResponseEntity<>(cartDTOS, HttpStatus.OK);
    }

    @GetMapping("/users/cart")
    public ResponseEntity<CartDTO> getCartsByUser() {

        String email = authUtil.loggedInUserEmail();
        Cart cart = cartRepository.findCartByUserEmail(email);
        Long cartId = cart.getCartId();

        CartDTO cartDTO = cartService.getCartsByUser(email, cartId);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/products/{productId}/quantity/{operation}")
    public  ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long productId, @PathVariable String operation) {
        Integer change = operation.equalsIgnoreCase("increment") ? 1 : operation.equalsIgnoreCase("decrement") ? -1 : 0 ;
        if(change==0){
            throw new APIException("Invalid operation. Use 'increment' or 'decrement'.");
        }
        CartDTO cartDTO = cartService.updateCartProduct(productId, change);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/carts/{cartId}/products/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId, @PathVariable Long productId) {
        String status =  cartService.deleteProductFromCart(cartId, productId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

}
