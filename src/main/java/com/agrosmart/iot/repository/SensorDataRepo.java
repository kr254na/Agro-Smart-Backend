package com.agrosmart.iot.repository;

import com.agrosmart.iot.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface SensorDataRepo extends JpaRepository<SensorData, Long> {

    // Fetch latest reading for a specific field
    SensorData findFirstByFieldIdOrderByTimestampDesc(@Param("fieldId") Long fieldId);

    @Query(value = "SELECT * FROM (" +
            "  SELECT DATE(timestamp) as date, " +
            "  AVG(soil_moisture) as avgMoisture, " +
            "  AVG(rainfall) as avgRain, " +
            "  AVG(water_level) as avgWater, " +
            "  AVG(soil_ph) as avgPh, " +
            "  AVG(temperature) as avgTemp, " +
            "  AVG(humidity) as avgHumidity, " +
            "  AVG(nitrogen) as avgN, " +
            "  AVG(phosphorus) as avgP, " +
            "  AVG(potassium) as avgK " +
            "  FROM sensor_data " +
            "  WHERE field_id = :fieldId AND timestamp >= :since " +
            "  GROUP BY DATE(timestamp) " +
            "  ORDER BY date DESC " + // Get newest dates first
            "  LIMIT 7" +             // Grab only the latest 7
            ") AS latest_data " +
            "ORDER BY date ASC",       // Re-sort oldest to newest for the chart
            nativeQuery = true)
    List<Object[]> findDailyAverages(@Param("fieldId") Long fieldId, @Param("since") LocalDateTime since);
}