package com.agrosmart.identity.repository;

import com.agrosmart.identity.model.PasswordResetToken;
import com.agrosmart.identity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUserEmail(String token, String email);
    void deleteByUser(User user);
}