package com.agrosmart.agromarket.dto;

import com.agrosmart.agromarket.enums.ProductCategory;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String productName;
    private String description;
    private BigDecimal price;
    private Double quantity;
    private String unit;
    private ProductCategory category;
    private String imageUrl;
    private String sellerName;
    private String sellerEmail;
    private String sellerContact;
    private boolean isSold;
    private LocalDateTime createdAt;
}