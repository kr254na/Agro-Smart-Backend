package com.agrosmart.iot.model;

import lombok.Data;
import java.util.List;

@Data
public class ThingSpeakResponse {
    private List<ThingSpeakFeed> feeds;
}