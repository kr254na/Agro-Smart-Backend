package com.agrosmart.iot.controller;

import com.agrosmart.common.dto.ApiResponse;
import com.agrosmart.iot.dto.DeviceRegistrationRequest;
import com.agrosmart.iot.model.SensorDevice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.agrosmart.iot.service.DeviceService;
import java.util.List;

@RestController
@RequestMapping("/api/iot/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SensorDevice>> registerDevice(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DeviceRegistrationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Device linked successfully",
                deviceService.registerDevice(userDetails.getUsername(), request)));
    }

    @GetMapping("/field/{fieldId}")
    public ResponseEntity<ApiResponse<List<SensorDevice>>> getDevicesByField(
            @PathVariable("fieldId") Long fieldId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                "Devices fetched",
                deviceService.getDevicesByField(fieldId, userDetails.getUsername())
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SensorDevice>> updateDevice(
            @PathVariable("id") Long id,
            @RequestParam String newName,
            @RequestParam boolean isActive,
            @AuthenticationPrincipal UserDetails userDetails) {
        SensorDevice updated = deviceService.updateDevice(id, newName, isActive, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Device renamed successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        deviceService.deleteDevice(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Device removed successfully", null));
    }
}