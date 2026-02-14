package com.agrosmart.iot.model;

import com.agrosmart.farm.model.Field;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String deviceSerialNumber; // Unique hardware ID (MAC address or UUID)
    private String deviceName; // e.g., "Soil Moisture Sensor 1"
    private String deviceType; // e.g., "MOISTURE", "TEMPERATURE", "ALL_IN_ONE
    private boolean isActive;
    private LocalDateTime lastSeen;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    @JsonIgnore
    private Field field;

}