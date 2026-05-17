package com.agrosmart.identity.dto;

import com.agrosmart.identity.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 8)
        private String password;
        private Role role;
        @NotBlank
        private String firstName;
        private String lastName;
        @Pattern(regexp = "^$|^[6-9]\\d{9}$", message = "Invalid Indian mobile number format")
        private String phoneNumber;
        @NotBlank
        private String city;
        @NotBlank
        private String state;
        @NotBlank
        private String district;
        @NotBlank @Pattern(regexp = "^[1-9][0-9]{5}$")
        private String pincode;
        private MultipartFile profilePic;
}