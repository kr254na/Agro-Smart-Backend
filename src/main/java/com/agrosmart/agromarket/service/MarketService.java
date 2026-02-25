package com.agrosmart.agromarket.service;

import com.agrosmart.agromarket.dto.ProductRequest;
import com.agrosmart.agromarket.dto.ProductResponse;
import com.agrosmart.agromarket.enums.ProductCategory;
import com.agrosmart.agromarket.model.Product;
import com.agrosmart.agromarket.repository.MarketRepo;
import com.agrosmart.common.exception.NotAllowedException;
import com.agrosmart.agromarket.exception.ProductNotFoundException;
import com.agrosmart.identity.exception.UserNotFoundException;
import com.agrosmart.identity.model.User;
import com.agrosmart.identity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final MarketRepo productRepo;
    private final UserRepo userRepo;

    @Transactional
    public ProductResponse addProduct(String email, ProductRequest request)
            throws BadRequestException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (request.getQuantity() < 0) {
            throw new BadRequestException("Quantity cannot be negative. Please enter 0 or more.");
        }
        Product product = Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .seller(user)
                .isSold(false)
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(productRepo.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAvailableProducts(ProductCategory category, String search) {
        List<Product> products;
        if (category != null && search != null) {
            products = productRepo.findByCategoryAndSearch(category, search);
        } else if (category != null) {
            products = productRepo.findByCategoryAndIsSoldFalse(category);
        } else if (search != null) {
            products = productRepo.searchMarket(search);
        } else {
            products = productRepo.findAllByIsSoldFalse();
        }

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getUserProducts(String email) {
        List<Product> myProducts = productRepo.findBySellerEmailOrderByCreatedAtDesc(email);
        return myProducts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, String email, ProductRequest request)
            throws BadRequestException {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getQuantity() < 0) {
            throw new BadRequestException("Quantity cannot be negative. Please enter 0 or more.");
        }

        if (!product.getSeller().getEmail().equals(email)) {
            throw new NotAllowedException("You are not authorized to update this listing");
        }

        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());

        if(product.getQuantity() == 0) {
            product.setSold(true);
        }

        return mapToResponse(productRepo.save(product));
    }

    @Transactional
    public void deleteProduct(Long productId, String email) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!product.getSeller().getEmail().equals(email)) {
            throw new NotAllowedException("Not authorized to delete this product");
        }

        productRepo.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .unit(product.getUnit())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .isSold(product.isSold())
                .createdAt(product.getCreatedAt())
                .sellerName(product.getSeller().getProfile().getFirstName() + " " +
                        product.getSeller().getProfile().getLastName())
                .sellerContact(product.getSeller().getProfile().getPhoneNumber())
                .build();
    }
}