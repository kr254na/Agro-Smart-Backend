package com.agrosmart.agromarket.controller;

import com.agrosmart.agromarket.enums.ProductCategory;
import com.agrosmart.common.dto.ApiResponse;
import com.agrosmart.agromarket.dto.ProductRequest;
import com.agrosmart.agromarket.dto.ProductResponse;
import com.agrosmart.agromarket.service.MarketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/market/products")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute @Valid ProductRequest request,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws BadRequestException {
        return ResponseEntity.ok(ApiResponse.success(
                "Product added successfully",
                marketService.addProduct(userDetails.getUsername(), request, imageFile)
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMarketplace(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(
                "Marketplace data fetched",
                marketService.getAvailableProducts(category, search)
        ));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getMarketplaceItem(
            @PathVariable("productId") Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product data fetched",
                marketService.getProductById(productId)
        ));
    }

    @GetMapping("/my-listings")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyListings(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                "Your listings fetched successfully",
                marketService.getUserProducts(userDetails.getUsername())
        ));
    }

    @PutMapping(value = "/{productId}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("productId") Long productId,
            @ModelAttribute @Valid ProductRequest request,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws BadRequestException {
        ProductResponse updatedProductResponse =
                marketService.updateProduct(productId, userDetails.getUsername(), request, imageFile);
        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully", updatedProductResponse));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("productId") Long productId
    ){
        marketService.deleteProduct(productId,userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }
}