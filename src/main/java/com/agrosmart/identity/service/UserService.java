package com.agrosmart.identity.service;

import com.agrosmart.identity.dto.ChangePasswordRequest;
import com.agrosmart.identity.dto.ProfileUpdateRequest;
import com.agrosmart.identity.exception.AuthenticationFailedException;
import com.agrosmart.identity.model.Address;
import com.agrosmart.identity.model.FarmerProfile;
import com.agrosmart.identity.model.User;
import com.agrosmart.identity.repository.FarmerProfileRepo;
import com.agrosmart.identity.exception.UserNotFoundException;
import com.agrosmart.identity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final FarmerProfileRepo farmerProfileRepo;
    private final PasswordEncoder passwordEncoder;

    public FarmerProfile getProfileByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Profile not found for: " + email));
        return user.getProfile();
    }

    @Transactional
    public FarmerProfile updateProfile(String email, ProfileUpdateRequest dto) {
        FarmerProfile profile = getProfileByEmail(email);

        if (dto.getFirstName() != null) profile.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) profile.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) profile.setPhoneNumber(dto.getPhoneNumber());

        if (profile.getAddress() == null) {
            profile.setAddress(new Address());
        }

        Address addr = profile.getAddress();
        if (dto.getCity() != null) addr.setCity(dto.getCity());
        if (dto.getState() != null) addr.setState(dto.getState());
        if (dto.getDistrict() != null) addr.setDistrict(dto.getDistrict());
        if (dto.getPincode() != null) addr.setPincode(dto.getPincode());

        return farmerProfileRepo.save(profile);
    }

    public List<FarmerProfile> getAllFarmers() {
        return farmerProfileRepo.findAll();
    }

    @Transactional
    public void deleteUser(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepo.delete(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthenticationFailedException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
    }
}