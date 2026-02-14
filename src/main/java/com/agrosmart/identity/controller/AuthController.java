package com.agrosmart.identity.controller;

import com.agrosmart.common.dto.ApiResponse;
import com.agrosmart.identity.dto.*;
import com.agrosmart.identity.service.AuthService;
import com.agrosmart.identity.service.CustomOAuth2UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CustomOAuth2UserService oauth2Service;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegistrationRequest request) {
        String message = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true,"Registration Successful", message, LocalDateTime.now()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login Successfull",  authService.authenticate(request)));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateGoogleUser(
            @Valid @RequestBody GoogleLoginRequest request) {
        LoginResponse response = oauth2Service.processGoogleUser(request.idToken());
        return ResponseEntity.ok(ApiResponse.success("Google login successful", response));
    }
}