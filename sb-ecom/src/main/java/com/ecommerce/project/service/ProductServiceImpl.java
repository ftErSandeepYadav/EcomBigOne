package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired CategoryRepository categoryRepository;
    @Autowired ModelMapper modelMapper;
    @Autowired ProductRepository productRepository;
    @Autowired FileService fileService;
    @Autowired CartRepository cartRepository;
    @Autowired CartService cartService;

    @Value("${project.image}") private String path;


    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isPresent()) {

            boolean isProductAlreadyExists = false ;

            List<Product> products = category.get().getProducts();
            for(Product p : products){
                if(p.getProductName().equalsIgnoreCase(productDTO.getProductName())){
                    isProductAlreadyExists = true ;
                    break;
                }
            }

            if(isProductAlreadyExists){
                throw new APIException("Product with name "+productDTO.getProductName()+" already exists in category "+category.get().getCategoryName());
            }

            Product product = modelMapper.map(productDTO, Product.class) ;
            product.setCategory(category.get());
            product.setImageUrl("default.png");
            double specialPrice = (1 - product.getDiscount()*0.01) * product.getPrice();
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        }
        throw new ResourceNotFoundException("Category", "categoryId", categoryId);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber-1, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findAll(pageable);


        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOListList = new ArrayList<>() ;

        for (Product product : products) {
            productDTOListList.add(modelMapper.map(product, ProductDTO.class)) ;
        }
        return new ProductResponse(productDTOListList,
                pageProducts.getNumber()+1,
                pageProducts.getSize(),
                pageProducts.getTotalElements(),
                pageProducts.getTotalPages(),
                pageProducts.isLast()) ;
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        if(category.isEmpty()){
            throw new ResourceNotFoundException("Category", "categoryId", categoryId);
        }
        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber-1, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByCategory(category.get(), pageable);
        List<Product> products = pageProducts.getContent();

        if(products.isEmpty()){
            throw  new ResourceNotFoundException("Poducts", "categoryId", categoryId);
        }
        List<ProductDTO> productDTOList = new ArrayList<>() ;
        for (Product product : products) {
            productDTOList.add(modelMapper.map(product, ProductDTO.class)) ;
        }
        ProductResponse productResponse = new ProductResponse() ;
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(pageNumber) ;
        productResponse.setPageSize(pageSize);
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse ;
    }

    @Override
    public ProductResponse getProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber-1, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase("%"+keyword+"%", pageable);

        List<Product> products = pageProducts.getContent();
        if(products.isEmpty()){
            throw  new ResourceNotFoundException("Poducts", "keyword", keyword);
        }

        List<ProductDTO> productDTOList = new ArrayList<>() ;
        for (Product product : products) {
            productDTOList.add(modelMapper.map(product, ProductDTO.class)) ;
        }

        ProductResponse productResponse = new ProductResponse() ;
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(pageNumber) ;
        productResponse.setPageSize(pageSize);
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse ;
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            // Update fields from DTO
            product.setProductName(productDTO.getProductName());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            product.setDiscount(productDTO.getDiscount());
            product.setImageUrl(productDTO.getImageUrl());
            product.setQuantity(productDTO.getQuantity());

            double specialPrice = (1 - product.getDiscount() * 0.01) * product.getPrice();
            product.setSpecialPrice(specialPrice);

            Product updatedProduct = productRepository.save(product);

            List<Cart> carts = cartRepository.findByProductId(productId);

            List<CartDTO> cartDTOs = carts.stream()
                    .map(cart -> cartToCartDTO(cart)
                    )
                    .toList();

            cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

            return modelMapper.map(updatedProduct, ProductDTO.class);
        }
        throw new ResourceNotFoundException("Product", "productId", productId);
    }

    @Transactional
    Product helperDeleteProduct(Long productId){
        Optional<Product> product = productRepository.findById(productId);
        if(product.isEmpty()){
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        List<Cart> carts = cartRepository.findByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));
        return product.get();
    }

    @Transactional
    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = helperDeleteProduct(productId);
        productRepository.deleteById(productId);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product productFromDb = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // upload image to file system and get the image name
        String imageName = fileService.uploadImage(path, image) ;

        productFromDb.setImageUrl(imageName);
        Product updatedProduct = productRepository.save(productFromDb);

        return modelMapper.map(updatedProduct, ProductDTO.class) ;

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

}
