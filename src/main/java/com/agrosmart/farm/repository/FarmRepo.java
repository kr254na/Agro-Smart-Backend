package com.agrosmart.farm.repository;

import com.agrosmart.farm.model.Farm;
import com.agrosmart.farm.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Import this
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
// FarmRepo.java
@Repository
public interface FarmRepo extends JpaRepository<Farm, Long> {

    @Query("SELECT f FROM Farm f WHERE f.farmer.user.email = :email AND f.id = :id")
    Optional<Farm> findByFarmerUserEmailAndId(
            @Param("email") String email,
            @Param("id") Long id
    );

    List<Farm> findByFarmerUserEmail(String email);
}
