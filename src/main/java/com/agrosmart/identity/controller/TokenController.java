package com.agrosmart.identity.controller;

import com.agrosmart.common.dto.ApiResponse;
import com.agrosmart.identity.dto.RefreshTokenRequest;
import com.agrosmart.identity.dto.TokenResponse;
import com.agrosmart.identity.model.RefreshToken;
import com.agrosmart.identity.service.JwtService;
import com.agrosmart.identity.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/token")
@RequiredArgsConstructor
public class TokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {

        TokenResponse response = refreshTokenService.findByToken(request.token())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtService.generateToken(user.getEmail());
                    return TokenResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(request.token())
                            .email(user.getEmail())
                            .role(user.getRole().name())
                            .type("Bearer")
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }
}