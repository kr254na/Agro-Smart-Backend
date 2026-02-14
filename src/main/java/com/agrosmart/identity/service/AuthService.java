package com.agrosmart.identity.service;

import com.agrosmart.identity.dto.LoginRequest;
import com.agrosmart.identity.dto.LoginResponse;
import com.agrosmart.identity.dto.RegistrationRequest;
import com.agrosmart.identity.dto.ResetPasswordRequest;
import com.agrosmart.identity.exception.AuthenticationFailedException;
import com.agrosmart.identity.exception.UserAlreadyExistsException;
import com.agrosmart.identity.exception.UserNotFoundException;
import com.agrosmart.identity.model.Address;
import com.agrosmart.identity.model.FarmerProfile;
import com.agrosmart.identity.model.PasswordResetToken;
import com.agrosmart.identity.model.User;
import com.agrosmart.identity.repository.FarmerProfileRepo;
import com.agrosmart.identity.repository.PasswordResetTokenRepo;
import com.agrosmart.identity.repository.UserRepo;
import com.agrosmart.identity.utility.UserPrincipal;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import com.agrosmart.identity.exception.EmailSendingException;
import com.agrosmart.identity.exception.InvalidOtpException;

@Service
public class AuthService implements UserDetailsService
{
    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private FarmerProfileRepo profileRepo;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordResetTokenRepo tokenRepo;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JavaMailSender mailSender;
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepo.findByEmail(email).orElse(null);
        if(user == null)
        {
            throw new UserNotFoundException("Invalid email or password");
        }
        return new UserPrincipal(user);
    }

    @Transactional
    public String registerUser(RegistrationRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }
        if (profileRepo.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Account with the given phone number already exists");
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();

        Address address = Address.builder()
                .city(request.getCity())
                .state(request.getState())
                .district(request.getDistrict())
                .pincode(request.getPincode())
                .build();

        FarmerProfile profile = FarmerProfile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .address(address)
                .build();

        user.setProfile(profile);
        userRepo.save(user);
        return "Farmer registered successfully with ID: " + user.getEmail();
    }

    @Transactional
    public LoginResponse authenticate(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );
            User user = userRepo.findByEmail(loginRequest.email()).orElse(null);
            System.out.println(user);
            String token = jwtService.generateToken(user.getEmail());
            System.out.println(token);
            return LoginResponse.builder()
                    .email(user.getEmail())
                    .accessToken(token)
                    .refreshToken(refreshTokenService.createRefreshToken(user.getEmail()).getToken())
                    .role(user.getRole().name())
                    .build();

        } catch (AuthenticationException ex) {
            throw new AuthenticationFailedException(
                    "Invalid username or password"
            );
        }
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            throw new UserNotFoundException("User with this email does not exist.");
        }

        tokenRepo.deleteByUser(user);
        tokenRepo.flush();

        SecureRandom secureRandom = new SecureRandom();
        String otp = String.format("%06d", secureRandom.nextInt(1000000));

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(otp);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(2)); // Valid for 2 mins
        tokenRepo.save(resetToken);

        sendOtpEmail(email, otp);
    }

    @Async
    private void sendOtpEmail(String email, String otp) {
        try {
            User user = userRepo.findByEmail(email).orElse(null);
            if(user == null){
                throw new UserNotFoundException("User with this email id does not exists");
            }
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(new InternetAddress("no-reply@agrosmart.com", "Agro Smart Support"));
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("Password Reset OTP - Agro Smart");
            String htmlContent = "<h3>Hello "+user.getProfile().getFirstName()+",</h3>" +
                    "<p>Your OTP for password reset is: <b>" + otp + "</b></p>" +
                    "<p>This code is valid for 2 minutes.</p>" +
                    "<br><i>Regards,<br>Agro Smart Team</i>";
            message.setContent(htmlContent, "text/html; charset=utf-8");
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendingException("Failed to send email! Please try again later");
        }
    }

    public void verifyPasswordResetOtp(String email, String otp) {
        PasswordResetToken token = tokenRepo.findByTokenAndUserEmail(otp, email)
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP! Please check your mail"));
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepo.delete(token);
            throw new InvalidOtpException("Your OTP has expired! Please request a new one");
        }
        if (token.isUsed()) {
            throw new InvalidOtpException("This OTP has already been used");
        }
    }

    @Transactional
    public void completePasswordReset(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepo.findByTokenAndUserEmail(request.otp(), request.email())
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP or Email"));
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepo.delete(token);
            throw new InvalidOtpException("Your OTP has expired! Please request a new one");
        }
        if (token.isUsed()) {
            throw new InvalidOtpException("This OTP has already been used");
        }
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepo.save(user);
        token.setUsed(true);
        tokenRepo.save(token);
        tokenRepo.delete(token);
    }

}