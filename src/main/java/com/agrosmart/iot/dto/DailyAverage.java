package com.agrosmart.iot.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyAverage {
    private String date;
    private Double moisture;
    private Double rain;
    private Double waterLevel;
    private Double ph;
    private Double temp;
    private Double humidity;
    private Double n;
    private Double p;
    private Double k;
}