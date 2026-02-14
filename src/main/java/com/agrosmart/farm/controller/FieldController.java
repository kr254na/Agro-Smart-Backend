package com.agrosmart.farm.controller;

import com.agrosmart.common.dto.ApiResponse;
import com.agrosmart.farm.dto.FieldRequest;
import com.agrosmart.farm.model.Field;
import com.agrosmart.farm.service.FarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fields")
@RequiredArgsConstructor
public class FieldController {

    private final FarmService farmService;


    @GetMapping("/farm/{farmId}")
    public ResponseEntity<ApiResponse<List<Field>>> getFieldsByFarm(
            @PathVariable("farmId") Long farmId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                "Fields retrieved",
                farmService.getFieldsByFarm(farmId, userDetails.getUsername())
        ));
    }


    @GetMapping("/farm/{farmId}/{fieldId}")
    public ResponseEntity<ApiResponse<Field>> getField(
            @PathVariable Long farmId,
            @PathVariable Long fieldId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Field retrieved",
                farmService.getField(farmId, fieldId, userDetails.getUsername())));
    }

    @PostMapping("/farm/{farmId}")
    public ResponseEntity<ApiResponse<Field>> createField(
            @PathVariable Long farmId,
            @Valid @RequestBody FieldRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Field created = farmService.addFieldToFarm(farmId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Field created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Field>> updateField(
            @PathVariable Long id,
            @Valid @RequestBody FieldRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Field updated = farmService.updateField(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Field updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteField(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        farmService.deleteField(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Field deleted successfully", null));
    }
}