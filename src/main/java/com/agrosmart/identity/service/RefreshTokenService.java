package com.agrosmart.identity.service;

import com.agrosmart.identity.exception.UserNotFoundException;
import com.agrosmart.identity.model.RefreshToken;
import com.agrosmart.identity.model.User;
import com.agrosmart.identity.repository.RefreshTokenRepo;
import com.agrosmart.identity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepo refreshTokenRepo;
    private final UserRepo userRepo;

    @Transactional
    public RefreshToken createRefreshToken(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        refreshTokenRepo.deleteByUser(user);
        refreshTokenRepo.flush();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        return refreshTokenRepo.save(refreshToken);
    }


    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepo.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new sign in request");
        }
        return token;
    }

    public java.util.Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepo.deleteByUser(userRepo.findById(userId).get());
    }
}
