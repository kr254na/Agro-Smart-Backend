package com.agrosmart.agromarket.service;

import com.agrosmart.agromarket.dto.ProductRequest;
import com.agrosmart.agromarket.dto.ProductResponse;
import com.agrosmart.agromarket.enums.ProductCategory;
import com.agrosmart.agromarket.model.Product;
import com.agrosmart.agromarket.repository.MarketRepo;
import com.agrosmart.common.exception.NotAllowedException;
import com.agrosmart.agromarket.exception.ProductNotFoundException;
import com.agrosmart.common.service.CloudinaryService;
import com.agrosmart.identity.exception.UserNotFoundException;
import com.agrosmart.identity.model.User;
import com.agrosmart.identity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final MarketRepo productRepo;
    private final UserRepo userRepo;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public ProductResponse addProduct(String email, ProductRequest request, MultipartFile imageFile)
            throws BadRequestException {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getQuantity() < 0) {
            throw new BadRequestException("Quantity cannot be negative. Please enter 0 or more.");
        }

        String uploadedImageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String contentType = imageFile.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("Only image files are allowed.");
            }
                uploadedImageUrl = cloudinaryService.uploadImage(imageFile,"agromarket/products","agromarket","product");
        }

        Product product = Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .category(request.getCategory())
                .imageUrl(uploadedImageUrl)
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
    public ProductResponse updateProduct(Long productId, String email, ProductRequest request, MultipartFile imageFile)
            throws BadRequestException {

        if (request == null) {
            throw new BadRequestException("Required request body 'product' is missing.");
        }

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
        product.setUnit(request.getUnit());

        if (request.isRemoveImage()) {
            cloudinaryService.deleteImage(product.getImageUrl());
            product.setImageUrl(null);
        }
        else if (imageFile != null && !imageFile.isEmpty()) {
            cloudinaryService.deleteImage(product.getImageUrl());
                String newImageUrl = cloudinaryService.uploadImage(imageFile,"agromarket/products","agromarket","product");
                product.setImageUrl(newImageUrl);
        }

        if(product.getQuantity() == 0) {
            product.setSold(true);
        }
        else{
            product.setSold(false);
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

        if (product.getImageUrl() != null) {
            cloudinaryService.deleteImage(product.getImageUrl());
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