package com.agrosmart.iot.service;

import com.agrosmart.common.exception.NotAllowedException;
import com.agrosmart.farm.repository.FieldRepo;
import com.agrosmart.farm.service.FarmService;
import com.agrosmart.iot.dto.DeviceRegistrationRequest;
import com.agrosmart.iot.exception.DeviceAlreadyRegisteredException;
import com.agrosmart.iot.exception.DeviceNotFoundException;
import com.agrosmart.iot.model.SensorDevice;
import com.agrosmart.iot.repository.DeviceRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepo deviceRepo;
    private final FarmService farmService;
    private final FieldRepo fieldRepo;

    @Transactional
    public SensorDevice registerDevice(String email, DeviceRegistrationRequest request) {
        // Ownership checks
        farmService.validateAndGetFieldOwnership(request.getFieldId(),email);
        
        if(deviceRepo.existsByDeviceSerialNumber(request.getDeviceSerialNumber())) {
            throw new DeviceAlreadyRegisteredException("Device already registered to a field");
        }

        SensorDevice device = SensorDevice.builder()
                .deviceSerialNumber(request.getDeviceSerialNumber())
                .deviceName(request.getDeviceName())
                .deviceType(request.getDeviceType())
                .isActive(true)
                .lastSeen(LocalDateTime.now())
                .field(fieldRepo.getReferenceById(request.getFieldId()))
                .build();

        return deviceRepo.save(device);
    }

    public List<SensorDevice> getDevicesByField(Long fieldId, String email) {
        // Ownership Checks
        farmService.validateAndGetFieldOwnership(fieldId,email);
        return deviceRepo.findByFieldId(fieldId);
    }

    @Transactional
    public void deleteDevice(Long deviceId, String email) {
        SensorDevice device = deviceRepo.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));

        // Check ownership through the hierarchy (Device -> Field -> Farm -> User)
        if (!device.getField().getFarm().getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Unauthorized to delete this device");
        }

        deviceRepo.delete(device);
    }

    @Transactional
    public SensorDevice updateDevice(Long deviceId, String newName, boolean isActive, String email) {
        SensorDevice device = deviceRepo.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));

        if (!device.getField().getFarm().getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Unauthorized to update this device");
        }

        device.setDeviceName(newName);
        device.setActive(isActive);
        return deviceRepo.save(device);
    }

}