package com.skillswap.controller;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.AuthDTO;
import com.skillswap.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse.Success<AuthDTO.AuthResponse>> register(
            @Valid @RequestBody AuthDTO.RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.Success.of(authService.register(req)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse.Success<AuthDTO.AuthResponse>> login(
            @Valid @RequestBody AuthDTO.LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.Success.of(authService.login(req)));
    }
}