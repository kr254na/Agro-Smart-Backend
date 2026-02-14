package com.agrosmart.identity.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ProfileUpdateRequest {
    private String firstName;
    private String lastName;
    @Pattern(regexp = "^[6-9]\\d{9}$")
    private String phoneNumber;
    private String city;
    private String state;
    private String district;
    @Pattern(regexp = "^[1-9][0-9]{5}$")
    private String pincode;
}