package com.agrosmart.agromarket.dto;

import com.agrosmart.agromarket.enums.ProductCategory;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    private String productName;
    private String description;
    private BigDecimal price;
    private Double quantity;
    private String unit;
    private ProductCategory category;
    @Size(max = 2048, message = "Image URL is too long. Please use a shorter URL")
    private String imageUrl;
}