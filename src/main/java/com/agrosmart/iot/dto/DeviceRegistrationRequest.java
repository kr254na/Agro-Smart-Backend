package com.agrosmart.iot.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class DeviceRegistrationRequest {
    @NotBlank private String deviceSerialNumber;
    @NotBlank private String deviceName;
    @NotBlank private String deviceType;
    @NotNull private Long fieldId;
}