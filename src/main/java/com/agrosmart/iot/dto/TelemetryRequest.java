package com.agrosmart.iot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TelemetryRequest {
    @NotBlank
    private Long fieldId;
    private Double temp;
    private Double humidity;
    private Double rainfall;
    private Double moisture;
    private Double ph;
    private Double n;
    private Double p;
    private Double k;
    private int waterLevel;
}