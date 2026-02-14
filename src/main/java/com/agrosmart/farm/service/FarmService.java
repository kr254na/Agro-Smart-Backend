package com.agrosmart.farm.service;

import com.agrosmart.common.exception.NotAllowedException;
import com.agrosmart.farm.dto.FarmRequest;
import com.agrosmart.farm.dto.FieldRequest;
import com.agrosmart.farm.exception.FarmNotFoundException;
import com.agrosmart.farm.exception.FieldNotFoundException;
import com.agrosmart.farm.model.Farm;
import com.agrosmart.farm.model.Field;
import com.agrosmart.farm.repository.FarmRepo;
import com.agrosmart.farm.repository.FieldRepo;
import com.agrosmart.identity.model.FarmerProfile;
import com.agrosmart.identity.service.UserService;
import com.agrosmart.iot.model.SensorDevice;
import com.agrosmart.iot.repository.DeviceRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmService {
    private final FarmRepo farmRepo;
    private final FieldRepo fieldRepo;
    private final UserService userService;
    private final DeviceRepo deviceRepo;
    @Transactional
    public Farm createFarm(String email, FarmRequest request) {
        FarmerProfile farmer = userService.getProfileByEmail(email);
        Farm farm = Farm.builder()
                .farmName(request.getFarmName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .totalArea(request.getTotalArea())
                .farmer(farmer)
                .build();
        return farmRepo.save(farm);
    }

    @Transactional
    public Field addFieldToFarm(Long farmId, FieldRequest request, String email) {
        Farm farm = farmRepo.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found"));
        if (!farm.getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Unauthorized to add field to this farm");
        }
        Field field = Field.builder()
                .fieldName(request.getFieldName())
                .cropType(request.getCropType())
                .fieldArea(request.getFieldArea())
                .soilType(request.getSoilType())
                .farm(farm)
                .build();
        return fieldRepo.save(field);
    }

    @Transactional
    public List<Farm> getAllMyFarms(String email) {
        return farmRepo.findByFarmerUserEmail(email);
    }



    @Transactional
    public Farm getFarmById(String email, Long farmId) {
        // USE the custom repository method you created with @Param
        Farm farm = farmRepo.findByFarmerUserEmailAndId(email, farmId)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found or unauthorized access"));
        return farm;
    }

    @Transactional
    public Farm updateFarm(Long farmId, FarmRequest request, String email) {
        Farm farm = farmRepo.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found"));

        if (!farm.getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Unauthorized to update this farm");
        }

        farm.setFarmName(request.getFarmName());
        farm.setLatitude(request.getLatitude());
        farm.setLongitude(request.getLongitude());
        farm.setTotalArea(request.getTotalArea());

        return farmRepo.save(farm);
    }

    @Transactional
    public void deleteFarm(Long farmId, String email) {
        Farm farm = farmRepo.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found"));

        if (!farm.getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Unauthorized to delete this farm");
        }

        for (Field field : farm.getFields()) {
            List<SensorDevice> devices = deviceRepo.findByFieldId(field.getId());
            deviceRepo.deleteAll(devices); // This clears the foreign key constraint
        }

        farmRepo.delete(farm);
    }

    @Transactional
    public List<Field> getFieldsByFarm(Long farmId, String email) {
        Farm farm = farmRepo.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found"));

        if (!farm.getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Unauthorized access to this farm's fields");
        }
        return fieldRepo.findByFarmId(farmId);
    }

    @Transactional
    public Field getField(Long farmId, Long fieldId, String email) {
        Farm farm = farmRepo.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException("Farm not found"));
        Field field = fieldRepo.findById(fieldId)
                .orElseThrow(() -> new FieldNotFoundException("Field not found"));

        // Check if the field belongs to the farm and if the farm belongs to the user
        if (!field.getFarm().getId().equals(farmId) ||
                !field.getFarm().getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Unauthorized access to this field");
        }
        return field;
    }

    @Transactional
    public Field updateField(Long fieldId, FieldRequest request, String email) {
        Field field = fieldRepo.findById(fieldId)
                .orElseThrow(() -> new FieldNotFoundException("Field not found"));

        if (!field.getFarm().getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Unauthorized to update this field");
        }

        field.setFieldName(request.getFieldName());
        field.setCropType(request.getCropType());
        field.setFieldArea(request.getFieldArea());
        field.setSoilType(request.getSoilType());

        return fieldRepo.save(field);
    }

    @Transactional
    public void deleteField(Long fieldId, String email) {
        Field field = fieldRepo.findById(fieldId)
                .orElseThrow(() -> new FieldNotFoundException("Field not found"));

        if (!field.getFarm().getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("Unauthorized to delete this field");
        }

        fieldRepo.delete(field);
    }

    public Field validateAndGetFieldOwnership(Long fieldId, String email) {
        Field field = fieldRepo.findById(fieldId)
                .orElseThrow(() -> new FieldNotFoundException("Field not found with ID: " + fieldId));
        if (!field.getFarm().getFarmer().getUser().getEmail().equals(email)) {
            throw new NotAllowedException("You do not have permission for this field.");
        }

        return field;
    }
}