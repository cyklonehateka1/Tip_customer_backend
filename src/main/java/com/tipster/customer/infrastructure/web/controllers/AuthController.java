package com.tipster.customer.infrastructure.web.controllers;

import com.tipster.customer.application.service.AuthService;
import com.tipster.customer.domain.models.ApiResponse;
import com.tipster.customer.domain.models.dto.AuthResponse;
import com.tipster.customer.domain.models.dto.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/session")
    public ResponseEntity<ApiResponse<AuthResponse>> getSession(Authentication authentication) {
        AuthResponse response = authService.getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success("Session retrieved successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(Authentication authentication) {
        authService.logout(authentication);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
