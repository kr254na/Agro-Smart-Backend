package com.agrosmart.agromarket.model;

import com.agrosmart.agromarket.enums.ProductCategory;
import com.agrosmart.identity.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(value = 0, message = "Quantity cannot be less than zero")
    private Double quantity;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;

    private String unit; // e.g., "KG", "Unit", "Bag"

    @Enumerated(EnumType.STRING)
    private ProductCategory category;
    @Column(length = 2048)
    private String imageUrl;

    private LocalDateTime createdAt;
    private boolean isSold = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;
}