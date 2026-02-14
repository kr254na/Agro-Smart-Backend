package com.agrosmart.iot.service;

import com.agrosmart.common.exception.NotAllowedException;
import com.agrosmart.farm.exception.FieldNotFoundException;
import com.agrosmart.farm.model.Field;
import com.agrosmart.farm.repository.FieldRepo;
import com.agrosmart.iot.dto.DailyAverage;
import com.agrosmart.iot.dto.TelemetryRequest;
import com.agrosmart.iot.exception.DeviceNotFoundException;
import com.agrosmart.iot.model.SensorData;
import com.agrosmart.iot.model.SensorDevice;
import com.agrosmart.iot.model.ThingSpeakFeed;
import com.agrosmart.iot.model.ThingSpeakResponse;
import com.agrosmart.iot.repository.DeviceRepo;
import com.agrosmart.iot.repository.SensorDataRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelemetryService {
    private final SensorDataRepo dataRepo;
    private final DeviceRepo deviceRepo;
    private final FieldRepo fieldRepo;
    private final RestTemplate restTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    @Value("${agrosmart.iot.api-key}")
    private String hardwareApiKey;
    private static final Logger log =
            LoggerFactory.getLogger(TelemetryService.class);
    private final String THINGSPEAK_BASE_URL="https://api.thingspeak.com/channels/3257222/feeds.json?api_key=";
    @Transactional
    public void processTelemetry(TelemetryRequest request) {

        Field field = fieldRepo.findById(request.getFieldId())
                .orElseThrow(()->new FieldNotFoundException("Field not found"));

        SensorData data = SensorData.builder()
                .field(fieldRepo.getReferenceById(request.getFieldId()))
                .temperature(request.getTemp())
                .humidity(request.getHumidity())
                .rainfall(request.getRainfall())
                .soilMoisture(request.getMoisture())
                .soilPh(request.getPh())
                .nitrogen(request.getN())
                .phosphorus(request.getP())
                .potassium(request.getK())
                .waterLevel(request.getWaterLevel())
                .timestamp(LocalDateTime.now())
                .build();
        dataRepo.save(data);

    }

    @Scheduled(fixedRate = 60000)
    public void fetchFromThingSpeak() {
        String url = "https://api.thingspeak.com/channels/3257222/feeds.json?api_key="
                + hardwareApiKey.trim() + "&results=1";

        try {
            ThingSpeakResponse response = restTemplate.getForObject(url, ThingSpeakResponse.class);
            if (response != null && !response.getFeeds().isEmpty()) {
                ThingSpeakFeed feed = response.getFeeds().get(0);

                Long fieldId = Long.parseLong(feed.getNodeId());
                List<SensorDevice> device = deviceRepo.findByFieldId(fieldId);

                TelemetryRequest request = new TelemetryRequest();
                request.setFieldId(Long.parseLong(feed.getNodeId()));

                request.setMoisture(parseSafe(feed.getSoilMoisture()));
                request.setWaterLevel(feed.getWaterLevel() != null ? Integer.parseInt(feed.getWaterLevel()) : 0);
                request.setRainfall(parseSafe(feed.getRainfall()));

                request.setTemp(22.0 + Math.random() * 8);
                request.setPh(6.0 + Math.random() * 1.5);
                request.setN(Math.random() * 50);
                request.setP(Math.random() * 30);
                request.setK(Math.random() * 40);
                request.setHumidity(19+ Math.random() * 8);

                this.processTelemetry(request);
                messagingTemplate.convertAndSend("/topic/telemetry/" + request.getFieldId(), request);

                log.info("Successfully processed data for Node/Field: {}", fieldId);
                System.out.println(request.toString());
            }
        } catch (Exception e) {
            log.error("Telemetry Sync Error: {}", e.getMessage());
        }
    }

    private double parseSafe(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid numeric value from ThingSpeak: {}", value);
            return 0.0;
        }
    }

    @Transactional
    public SensorData getLatestReadingByField(Long fieldId, String email) {
        // Ownership check via the repository method we fixed earlier
        Field field = fieldRepo.findById(fieldId)
                .orElseThrow(() -> new FieldNotFoundException("Field not found"));

        if (!field.getFarm().getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Unauthorized access to this field's data");
        }

        return dataRepo.findFirstByFieldIdOrderByTimestampDesc(fieldId);
    }

    public List<DailyAverage> getSevenDayHistory(Long fieldId, String email) {
        // 1. Security check as before
        Field field = fieldRepo.findById(fieldId).orElseThrow(() -> new FieldNotFoundException("Field not found"));
        if (!field.getFarm().getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Access Denied");
        }

        LocalDateTime weekAgo = LocalDateTime.now()
                .minusDays(7)
                .withHour(0).withMinute(0).withSecond(0);

        List<Object[]> rows = dataRepo.findDailyAverages(fieldId, weekAgo);

        return rows.stream().map(row -> DailyAverage.builder()
                .date(row[0].toString())
                .moisture(convertToDouble(row[1]))
                .rain(convertToDouble(row[2]))
                .waterLevel(convertToDouble(row[3]))
                .ph(convertToDouble(row[4]))
                .temp(convertToDouble(row[5]))
                .humidity(convertToDouble(row[6]))
                .n(convertToDouble(row[7]))
                .p(convertToDouble(row[8]))
                .k(convertToDouble(row[9]))
                .build()
        ).collect(Collectors.toList());
    }

    private Double convertToDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) {
            return ((Number) val).doubleValue(); // Safely converts BigDecimal to Double
        }
        return 0.0;
    }

    // Helper to keep numbers clean (2 decimal places)
    private Double round(Double val) {
        return val == null ? 0.0 : Math.round(val * 100.0) / 100.0;
    }

}