package com.agrosmart.community.enums;

import lombok.Getter;

@Getter
public enum PostCategory {

    GENERAL_DISCUSSION("General Discussion"),
    DISEASE_OUTBREAK("Disease Outbreak"),
    WEATHER_ALERT("Weather Alert"),
    SEED_EXCHANGE("Seed Exchange"),
    EQUIPMENT_RENTAL("Equipment Rental"),
    FERTILIZER_SALE("Fertilizer Sale"),
    CROP_MARKET("Crop Marketplace");

    private final String displayName;

    PostCategory(String displayName) {
        this.displayName = displayName;
    }
}