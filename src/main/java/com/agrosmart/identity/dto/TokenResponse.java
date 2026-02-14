package com.agrosmart.identity.dto;

import lombok.Builder;

@Builder
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String type,
        String email,
        String role
) {
    public TokenResponse {
        if (type == null) type = "Bearer";
    }
}