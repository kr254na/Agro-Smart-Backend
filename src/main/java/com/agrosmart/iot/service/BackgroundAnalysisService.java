package com.agrosmart.iot.service;

import com.agrosmart.iot.model.SensorData;
import com.agrosmart.iot.model.SensorDevice;
import com.agrosmart.iot.repository.DeviceRepo;
import com.agrosmart.iot.repository.SensorDataRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BackgroundAnalysisService {

    private final DeviceRepo deviceRepo;
    private final SensorDataRepo dataRepo;
    private final JavaMailSender mailSender;

    @Scheduled(fixedRate = 3600000)
    public void performFieldAnalysis() {
        log.info("Executing field condition analysis...");

        List<SensorDevice> allDevices = deviceRepo.findAll();

        for (SensorDevice device : allDevices) {
            // Fetch the latest data point for the field
            SensorData latest = dataRepo.findFirstByFieldIdOrderByTimestampDesc(device.getField().getId());

            if (latest != null) {
                List<String> activeAlerts = evaluateConditions(latest);

                if (!activeAlerts.isEmpty()) {
                    String ownerEmail = device.getField().getFarm().getFarmer().getUser().getEmail();
                    sendEmailAlert(ownerEmail, device.getField().getFieldName(), activeAlerts);
                }
            }
        }
    }

    private List<String> evaluateConditions(SensorData data) {
        List<String> alerts = new ArrayList<>();

        // 1. Irrigation Condition: Soil VWC < 15% (Image Threshold)
        if (data.getSoilMoisture() < 15.0) {
            alerts.add("Irrigation Required: Soil VWC is below 15%. Motor On karein.");
        }

        // 2. Disease Condition: Humidity > 85% AND Temp 18-24°C (Image Threshold)
        if (data.getHumidity() > 85.0 && (data.getTemperature() >= 18.0 && data.getTemperature() <= 24.0)) {
            alerts.add("Disease Warning: High humidity and optimal temp detected. High risk of fungal growth.");
        }

        // 3. Spraying Condition: Rain > 1mm (Image Threshold)
        if (data.getRainfall() > 1.0) {
            alerts.add("Spraying Alert: Significant rain detected (>1mm). Spray cancel karein.");
        }

        // 4. Heat Condition: Temp > 42°C (Image Threshold)
        if (data.getTemperature() > 42.0) {
            alerts.add("Heat Stress: Extreme temperature (>42°C). Protect the crops.");
        }

        return alerts;
    }

    private void sendEmailAlert(String to, String fieldName, List<String> alerts) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>" +
                    "  <div style='background-color: #d32f2f; padding: 20px; color: #ffffff; text-align: center;'>" +
                    "    <h1 style='margin: 0;'>AgroSmart: Critical Field Alert</h1>" +
                    "  </div>" +
                    "  <div style='padding: 20px; color: #333333;'>" +
                    "    <p>Dear Farmer,</p>" +
                    "    <p>Our real-time analysis for <strong>" + fieldName + "</strong> has triggered the following alerts:</p>" +
                    "    <div style='background-color: #fff5f5; padding: 15px; border-left: 5px solid #d32f2f;'>" +
                    "       <ul style='margin: 0; padding-left: 20px;'>" +
                    generateHtmlAlertList(alerts) +
                    "       </ul>" +
                    "    </div>" +
                    "  </div>" +
                    "  <div style='background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #777777;'>" +
                    "    This is an automated alert based on live sensor telemetry.<br/>" +
                    "    © 2026 AgroSmart Inc. | Uttar Pradesh, India" +
                    "  </div>" +
                    "</div>";

            helper.setText(htmlMsg, true);
            helper.setTo(to);
            helper.setSubject("URGENT: Field Conditions Alert for " + fieldName);
            helper.setFrom("alerts@agrosmart.com","Agro Smart Support");
            mailSender.send(mimeMessage);
            log.info("Critical Alert sent to: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send alert to {}: {}", to, e.getMessage());
        }
    }

    private String generateHtmlAlertList(List<String> alerts) {
        StringBuilder listItems = new StringBuilder();
        for (String alert : alerts) {
            listItems.append("<li style='margin-bottom: 8px; color: #b71c1c;'>").append(alert).append("</li>");
        }
        return listItems.toString();
    }
}