package com.agrosmart.iot.repository;

import com.agrosmart.iot.model.SensorDevice;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepo extends JpaRepository<SensorDevice, Long> {

    @Query("SELECT d FROM SensorDevice d WHERE d.deviceSerialNumber = :serialNumber")
    Optional<SensorDevice> findByDeviceSerialNumber(@Param("serialNumber") String serialNumber);

    @Query("SELECT d FROM SensorDevice d WHERE d.field.id = :fieldId")
    List<SensorDevice> findByFieldId(@Param("fieldId") Long fieldId);

    @Query("SELECT COUNT(d) > 0 FROM SensorDevice d WHERE d.deviceSerialNumber = :serialNumber")
    boolean existsByDeviceSerialNumber(@Param("serialNumber") String serialNumber);
}