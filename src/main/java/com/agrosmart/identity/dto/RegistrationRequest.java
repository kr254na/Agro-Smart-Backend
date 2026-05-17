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
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
        )
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