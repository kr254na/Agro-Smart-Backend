package com.agrosmart.identity.service;

import com.agrosmart.identity.dto.LoginResponse;
import com.agrosmart.identity.enums.Role;
import com.agrosmart.identity.exception.AuthenticationFailedException;
import com.agrosmart.identity.exception.InvalidOtpException;
import com.agrosmart.identity.model.Address;
import com.agrosmart.identity.model.FarmerProfile;
import com.agrosmart.identity.model.User;
import com.agrosmart.identity.repository.UserRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private JwtService jwtService;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            user = registerNewOAuthUser(oAuth2User, email);
        }
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole().name())),
                oAuth2User.getAttributes(),
                "email"
        );
    }

    public LoginResponse processGoogleUser(String idTokenString) {
        GoogleIdToken idToken =  verifyGoogleToken(idTokenString);
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            user = registerNewOAuthUserFromPayload(payload);
        }
        String jwt = jwtService.generateToken(user.getEmail());

        return LoginResponse.builder()
                .accessToken(jwt)
                .refreshToken("")
                .email(email)
                .role(user.getRole().name())
                .build();
    }

    private GoogleIdToken verifyGoogleToken(String idTokenString) {
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new InvalidOtpException("Invalid Google Token");
            }
            return idToken;
        } catch (Exception e) {
            throw new AuthenticationFailedException("Google verification failed");
        }
    }

    private User registerNewOAuthUserFromPayload(GoogleIdToken.Payload payload) {
        User user = User.builder()
                .email(payload.getEmail())
                .password(null)
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        FarmerProfile profile = FarmerProfile.builder()
                .user(user)
                .firstName((String) payload.get("given_name"))
                .lastName((String) payload.get("family_name"))
                .address(new Address())
                .build();
        user.setProfile(profile);
        return userRepo.save(user);
    }

    private User registerNewOAuthUser(OAuth2User oAuth2User, String email) {
        User user = User.builder()
                .email(email)
                .password(null)
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        FarmerProfile profile = FarmerProfile.builder()
                .user(user)
                .firstName(oAuth2User.getAttribute("given_name"))
                .lastName(oAuth2User.getAttribute("family_name"))
                .address(new Address())
                .build();

        user.setProfile(profile);
        return userRepo.save(user);
    }

}