package com.agrosmart.identity.controller;

import com.agrosmart.common.dto.ApiResponse;
import com.agrosmart.identity.dto.ChangePasswordRequest;
import com.agrosmart.identity.dto.ProfileUpdateRequest;
import com.agrosmart.identity.model.FarmerProfile;
import com.agrosmart.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<FarmerProfile>> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        FarmerProfile profile = userService.getProfileByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", profile));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FarmerProfile>>> getAllFarmers() {
        return ResponseEntity.ok(ApiResponse.success("All farmers fetched", userService.getAllFarmers()));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<FarmerProfile>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        FarmerProfile updated = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", updated));
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}