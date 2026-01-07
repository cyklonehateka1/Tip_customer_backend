package com.tipster.customer.application.service.impl;

import com.tipster.customer.application.service.AuthService;
import com.tipster.customer.domain.entities.User;
import com.tipster.customer.domain.entities.UserRole;
import com.tipster.customer.domain.exceptions.ValidateException;
import com.tipster.customer.domain.models.dto.AuthResponse;
import com.tipster.customer.domain.models.dto.LoginRequest;
import com.tipster.customer.domain.repository.UserRepository;
import com.tipster.customer.domain.repository.UserRoleRepository;
import com.tipster.customer.infrastructure.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidateException("Invalid email or password", List.of("Invalid credentials")));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ValidateException("Invalid email or password", List.of("Invalid credentials"));
        }

        if (!user.getIsActive()) {
            throw new ValidateException("Account is inactive", List.of("Account has been deactivated"));
        }

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        // Get user's primary role (first role, typically CUSTOMER or TIPSTER)
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        String primaryRole = userRoles.isEmpty() ? "CUSTOMER" : userRoles.get(0).getRole().name();

        String token = jwtTokenService.generateToken(
                user.getId(),
                user.getEmail(),
                primaryRole
        );

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setRole(primaryRole);

        return response;
    }

    @Override
    public AuthResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ValidateException("User not authenticated", List.of("Authentication required"));
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) {
            throw new ValidateException("Invalid principal type", List.of("User details not found"));
        }

        // Get user's primary role
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        String primaryRole = userRoles.isEmpty() ? "CUSTOMER" : userRoles.get(0).getRole().name();

        AuthResponse response = new AuthResponse();
        response.setToken(null);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setRole(primaryRole);
        return response;
    }
}
