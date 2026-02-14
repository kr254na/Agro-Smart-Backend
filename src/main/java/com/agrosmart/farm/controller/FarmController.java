package com.agrosmart.farm.controller;

import com.agrosmart.common.dto.ApiResponse;
import com.agrosmart.farm.dto.FarmRequest;
import com.agrosmart.farm.model.Farm;
import com.agrosmart.farm.service.FarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Farm>>> getMyFarms(@AuthenticationPrincipal UserDetails userDetails) {
        List<Farm> farms = farmService.getAllMyFarms(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Farms retrieved", farms));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Farm>> getFarmById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Farm details fetched", farmService.getFarmById(userDetails.getUsername(),id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Farm>> createFarm(
            @Valid @RequestBody FarmRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Farm created = farmService.createFarm(userDetails.getUsername(),request);
        return ResponseEntity.ok(ApiResponse.success("Farm created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Farm>> updateFarm(
            @PathVariable Long id,
            @Valid @RequestBody FarmRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Farm updated = farmService.updateFarm(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Farm updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFarm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        farmService.deleteFarm(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Farm deleted successfully", null));
    }
}