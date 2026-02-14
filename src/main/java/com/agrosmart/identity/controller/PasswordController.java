package com.agrosmart.identity.controller;

import com.agrosmart.common.dto.ApiResponse;
import com.agrosmart.identity.dto.ResetPasswordRequest;
import com.agrosmart.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordController {

    @Autowired
    private AuthService passwordService;

    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam("email") String email) {
        passwordService.initiatePasswordReset(email);
        return ResponseEntity.ok(ApiResponse.success("OTP sent to your registered email"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestParam("email") String email, @RequestParam("otp") String otp) {
        passwordService.verifyPasswordResetOtp(email, otp);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully"));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.completePasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully."));
    }
}