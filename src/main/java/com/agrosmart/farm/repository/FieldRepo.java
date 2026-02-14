package com.agrosmart.farm.repository;

import com.agrosmart.farm.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepo extends JpaRepository<Field, Long> {

    // ❌ REMOVE @Param here
    List<Field> findByFarmId(Long farmId);
}
