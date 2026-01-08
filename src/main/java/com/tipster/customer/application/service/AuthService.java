package com.tipster.customer.application.service;

import com.tipster.customer.domain.models.dto.AuthResponse;
import com.tipster.customer.domain.models.dto.LoginRequest;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse getCurrentUser(Authentication authentication);
    void logout(Authentication authentication);
}
