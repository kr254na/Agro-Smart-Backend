package com.agrosmart.identity.dto;
import jakarta.validation.constraints.NotBlank;
public record GoogleLoginRequest(@NotBlank String idToken)
{}