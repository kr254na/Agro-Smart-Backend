package com.agrosmart.farm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FieldRequest {
    @NotBlank
    private String fieldName;
    private String cropType;
    @Positive
    private Double fieldArea;
    private String soilType;
}