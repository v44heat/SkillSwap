package com.skillswap.service;

import com.skillswap.dto.AuthDTO;
import com.skillswap.exception.AppException;
import com.skillswap.model.User;
import com.skillswap.repository.UserRepository;
import com.skillswap.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;
    private final AuthenticationManager authManager;

    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw AppException.conflict("An account with this email already exists");
        }

        User user = User.builder()
                .email(req.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .firstName(sanitize(req.getFirstName()))
                .lastName(sanitize(req.getLastName()))
                .department(req.getDepartment())
                .yearOfStudy(req.getYearOfStudy())
                .studentId(req.getStudentId())
                .bio(req.getBio())
                .role(User.UserRole.STUDENT)
                .status(User.UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return buildResponse(user, token);
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getEmail().toLowerCase().trim(),
                            req.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw AppException.badRequest("Invalid email or password");
        }

        User user = userRepository.findByEmail(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> AppException.notFound("User"));

        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw AppException.forbidden("Your account has been suspended");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return buildResponse(user, token);
    }

    private AuthDTO.AuthResponse buildResponse(User user, String token) {
        return AuthDTO.AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    private String sanitize(String input) {
        return input == null ? null : input.trim().replaceAll("<[^>]*>", "");
    }
}