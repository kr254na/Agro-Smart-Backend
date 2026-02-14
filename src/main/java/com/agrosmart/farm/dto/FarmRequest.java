package com.agrosmart.farm.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class FarmRequest {
    @NotBlank(message = "Farm name is required")
    private String farmName;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @Positive(message = "Total area must be greater than zero")
    private Double totalArea;
}
