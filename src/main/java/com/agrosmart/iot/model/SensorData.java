package com.agrosmart.iot.model;

import com.agrosmart.farm.model.Field;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Environmental Data
    private Double temperature;
    private Double humidity;
    private Double rainfall;

    // Soil Health Data
    private Double soilMoisture;
    private Double soilPh;

    // Soil Nutrients (NPK)
    private Double nitrogen;
    private Double phosphorus;
    private Double potassium;

    private int waterLevel;
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    @JsonIgnore
    private Field field;

}