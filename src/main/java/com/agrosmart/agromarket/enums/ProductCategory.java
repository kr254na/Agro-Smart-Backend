package com.agrosmart.agromarket.enums;

import lombok.Getter;

@Getter
public enum ProductCategory {
    SEEDS("Seeds"),
    FERTILIZERS("Fertilizers"),
    EQUIPMENT("Equipment"),
    TOOLS("Tools"),
    CEREALS("Cereals"),
    VEGETABLES("Vegetables"),
    OTHERS("Others");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }
}