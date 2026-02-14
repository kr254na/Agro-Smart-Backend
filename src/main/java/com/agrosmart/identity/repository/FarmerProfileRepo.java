package com.agrosmart.identity.repository;

import com.agrosmart.identity.model.FarmerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FarmerProfileRepo extends JpaRepository<FarmerProfile, Long> {
    List<FarmerProfile> findByAddressDistrict(String district);
    Optional<FarmerProfile> findByUserEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
}