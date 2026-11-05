package com.example.CoolShopWebsite.service;

import com.example.CoolShopWebsite.config.JwtService;
import com.example.CoolShopWebsite.dto.request.LoginRequest;
import com.example.CoolShopWebsite.dto.request.RegisterRequest;
import com.example.CoolShopWebsite.dto.response.AuthResponse;
import com.example.CoolShopWebsite.dto.response.UserResponse;
import com.example.CoolShopWebsite.exception.AppException;
import com.example.CoolShopWebsite.model.User;
import com.example.CoolShopWebsite.model.enums.UserRole;
import com.example.CoolShopWebsite.model.enums.UserStatus;
import com.example.CoolShopWebsite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw AppException.conflict("Email already exists");
        }
        User user = User.builder()
                .fullname(req.getFullname())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.CUSTOMER)   // không cho tự đăng ký admin
                .status(UserStatus.ACTIVE)
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (BadCredentialsException e) {
            throw AppException.badRequest("Invalid email or password");
        }
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> AppException.notFound("User not found"));
        UserDetails details = userDetailsService.loadUserByUsername(req.getEmail());
        String token = jwtService.generateToken(details);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(UserResponse.from(user))
                .build();
    }

    public UserResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("User not found"));
        return UserResponse.from(user);
    }
}
