package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.payload.PaymentDTO;
import com.ecommerce.project.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private CartRepository cartRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private ModelMapper modelMapper;


//    @Override
//    @Transactional
//    public OrderDTO placeOrder(String email, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
//        Cart cart = cartRepository.findCartByUserEmail(email);
//        if(cart==null) throw new ResourceNotFoundException("Cart","User Email",email);
//        if(cart.getCartItems().isEmpty()) throw new APIException("Cart is empty");
//
//        Address address = addressRepository.findById(addressId)
//                .orElseThrow(()->new ResourceNotFoundException("Address","Id",addressId));
//
//        Order order = new Order();
//        order.setEmail(email);
//        order.setOrderDate(LocalDate.now());
//        order.setOrderStatus("Order Accepted");
//        order.setAddress(address);
//
//        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
//        payment.setOrder(order);
//        payment = paymentRepository.save(payment);
//
//        order.setPayment(payment);
//
//        Order savedOrder = orderRepository.save(order);
//
//        List<CartItem> cartItems = cart.getCartItems();
//        List<OrderItem> orderItems = new ArrayList<>();
//        for(var cartItem : cartItems){
//            OrderItem orderItem = new OrderItem();
//            orderItem.setOrderItemPrice(cartItem.getProductPrice());
//            orderItem.setDiscount(cartItem.getDiscount());
//            orderItem.setProduct(cartItem.getProduct());
//            orderItem.setQuantity(cartItem.getQuantity());
//
//            orderItem.setOrder(savedOrder);
//            orderItems.add(orderItem);
//        }
//
//        orderItems = orderItemRepository.saveAll(orderItems);
//
//        Double totalAmount = cart.getTotalPrice() ;
//
//        for(CartItem cartItem : cartItems){
//            int orderQty = cartItem.getQuantity() ;
//            Product product = cartItem.getProduct() ;
//            int prodcutQty = product.getQuantity() ;
//            if(prodcutQty<orderQty) throw new APIException("Product "+ product.getProductName()+" is out of stock");
//            product.setQuantity(prodcutQty - orderQty);
//
//            productRepository.save(product);
//
//            cartService.deleteProductFromCart(cart.getCartId(), product.getId());
//        }
//
//        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
//        orderDTO.setOrderItems(new ArrayList<>());
//        orderItems.forEach(item-> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));
//        orderDTO.setPayment(modelMapper.map(payment, PaymentDTO.class));
//        orderDTO.setTotalAmount(totalAmount);
//
//        return orderDTO;
//
//    }

    // java
    @Override
    @Transactional
    public OrderDTO placeOrder(String email, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        Cart cart = cartRepository.findCartByUserEmail(email);
        if (cart == null) throw new ResourceNotFoundException("Cart", "User Email", email);

        // take a snapshot to avoid ConcurrentModificationException when removing items later
        List<CartItem> cartItems = new ArrayList<>(cart.getCartItems());
        if (cartItems.isEmpty()) throw new APIException("Cart is empty");

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "Id", addressId));

        Order order = new Order();
        order.setEmail(email);
        order.setOrderDate(LocalDate.now());
        order.setOrderStatus("Order Accepted");
        order.setAddress(address);

        // persist order first so OrderItem and Payment can reference it
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItemPrice(cartItem.getProductPrice());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setProduct(cartItem.getProduct());
// java
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        // persist all order items
        orderItems = orderItemRepository.saveAll(orderItems);

        // compute total from cart
        Double totalAmount = cart.getTotalPrice();

        // create and persist payment, attach to order
        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
        payment.setOrder(savedOrder);
        payment = paymentRepository.save(payment);

        savedOrder.setPayment(payment);
        savedOrder.setTotalAmount(totalAmount);
        savedOrder = orderRepository.save(savedOrder);

        // update product quantities and remove items from cart using the snapshot
        for (CartItem cartItem : cartItems) {
            int orderQty = cartItem.getQuantity();
            Product product = cartItem.getProduct();
            int productQty = product.getQuantity();
            if (productQty < orderQty) {
                throw new APIException("Product " + product.getProductName() + " is out of stock");
            }
            product.setQuantity(productQty - orderQty);
            productRepository.save(product);

            cartService.deleteProductFromCart(cart.getCartId(), product.getId());
        }

        // build and return DTO
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderDTO.setOrderItems(new ArrayList<>());
        orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));
        orderDTO.setPayment(modelMapper.map(payment, PaymentDTO.class));
        orderDTO.setTotalAmount(totalAmount);

        return orderDTO;
    }
}

