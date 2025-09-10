package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    CartRepository cartRepository;

    @Autowired AuthUtil authUtil;
    @Autowired ModelMapper modelMapper;
    @Autowired ProductRepository productRepository;
    @Autowired CartItemRepository cartItemRepository;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart = createCart() ;

        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);

        validateProductQuantity(product, cartItem, quantity);

        CartItem newCartItem = new CartItem();
        newCartItem.setCart(cart);
        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setProductPrice(product.getSpecialPrice());
        newCartItem.setDiscount(product.getDiscount());

        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice()+ (product.getSpecialPrice()*quantity));

        cart.getCartItems().add(newCartItem);


        cartRepository.save(cart);


        CartDTO cartDTO = cartToCartDTO(cart);

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if(carts.isEmpty()){ throw new APIException("No carts found"); }
        return carts.stream()
                .map(cart -> cartToCartDTO(cart)
                )
                .toList();
    }

    @Override
    public CartDTO getCartsByUser(String email, Long cartId) {
        Cart cart = cartRepository.findCartByUserUserEmailAndCartId(email, cartId);

        if(cart==null) throw new ResourceNotFoundException("Cart","cartId",cartId);

        CartDTO cartDTO = cartToCartDTO(cart);
        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateCartProduct(Long productId, Integer change) {

        String emailId = authUtil.loggedInUserEmail();
        Cart userCart = cartRepository.findCartByUserEmail(emailId);
        if(userCart==null) throw new APIException("Cart doesn't exists for user "+emailId);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId).orElseThrow(()-> new ResourceNotFoundException("Cart","cartId",cartId)) ;
        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId)) ;

        if(product.getQuantity()==0) throw new APIException("Product "+product.getProductName()+" is not available");

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);

        if(cartItem==null) throw new ResourceNotFoundException("CartItem","product",productId);

        Integer newQuantity = cartItem.getQuantity()+change;

        Cart savedCart ;

        if(newQuantity>product.getQuantity()) throw new APIException("Only "+product.getQuantity()+" items are available in stock");
        if(newQuantity==0) {
            deleteProductFromCart(cartId, productId);
            savedCart = cartRepository.findById(cartId).orElseThrow(()-> new ResourceNotFoundException("Cart","cartId",cartId)) ;
        }
        else if (newQuantity<0) {
            throw new APIException("Cart Item quantity cannot be less than 0");
        } else{
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
            cart.setTotalPrice(cart.getTotalPrice() + (change * cartItem.getProductPrice()));
            savedCart = cartRepository.save(cart);
        }
        CartDTO cartDTO = cartToCartDTO(savedCart);
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId);
        if (cartItem == null) throw new ResourceNotFoundException("CartItem", "productId", productId);

        String productName = cartItem.getProduct().getProductName();
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

        // remove by id to avoid equals/proxy issues and keep both sides consistent
        Long removeId = cartItem.getCartItemId();
        cart.getCartItems().removeIf(ci -> ci.getCartItemId().equals(removeId));

        // break association (keeps JPA state consistent)
        cartItem.setCart(null);

        // rely on orphanRemoval = true; saving the cart will remove the orphaned child
        cartRepository.save(cart);

        return "Product with id " + productName + " removed from cart successfully";
    }


    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice()
                - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice
                + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.save(cartItem);
    }

    CartDTO cartToCartDTO(Cart cart){
        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        List<ProductDTO> products = cartItems.stream().map(item -> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
            productDTO.setQuantity(item.getQuantity());
            return productDTO;
        }).toList();
        cartDTO.setProducts(products);
        return cartDTO;
    }

    private Cart createCart(){
        Cart userCart = cartRepository.findCartByUserEmail((authUtil.loggedInUserEmail()));

        if(userCart!=null) return userCart;
        Cart cart = new Cart(authUtil.loggedInUser(),0.0);
        return cartRepository.save(cart);
    }

    void validateProductQuantity(Product product, CartItem cartItem, Integer quantity){
        if(cartItem!=null) throw new APIException("Product "+product.getProductName()+" already exists in the cart");
        if(product.getQuantity()==0) throw new APIException("Product "+product.getProductName()+" is not available");
        if(quantity>product.getQuantity()) throw new APIException("Only "+product.getQuantity()+" items are available in stock");;
    }

}
