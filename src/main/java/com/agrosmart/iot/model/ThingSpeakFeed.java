package com.agrosmart.iot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ThingSpeakFeed {
    @JsonProperty("field1") private String soilMoisture;
    @JsonProperty("field2") private String waterLevel;
    @JsonProperty("field3") private String rainfall;
    @JsonProperty("field4") private String nodeId; // Maps to Field ID in your DB

    // Environmental fields for random generation
    private String nitrogen;
    private String phosphorus;
    private String potassium;
    private String ph;
    private String temperature;
    private String humidity;
}