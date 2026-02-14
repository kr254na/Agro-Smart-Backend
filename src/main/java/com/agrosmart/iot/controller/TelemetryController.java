package com.agrosmart.iot.controller;

import com.agrosmart.common.dto.ApiResponse;
import com.agrosmart.iot.dto.DailyAverage;
import com.agrosmart.iot.model.SensorData;
import com.agrosmart.iot.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/iot/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    @GetMapping("/latest/{fieldId}")
    public ResponseEntity<ApiResponse<SensorData>> getLatestFieldData(
            @PathVariable Long fieldId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Current field status fetched",
                telemetryService.getLatestReadingByField(fieldId, userDetails.getUsername())));
    }

    @GetMapping("/history/{fieldId}")
    public ResponseEntity<ApiResponse<List<DailyAverage>>> getSevenDayTrend(
            @PathVariable Long fieldId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("7-day trend data fetched",
                telemetryService.getSevenDayHistory(fieldId, userDetails.getUsername())));
    }
}