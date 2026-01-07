package com.tipster.customer.infrastructure.security;

import java.util.UUID;

public interface JwtTokenService {
    String generateToken(UUID userId, String email, String role);
    boolean validateToken(String token);
    UUID getUserIdFromToken(String token);
    String getEmailFromToken(String token);
    String getRoleFromToken(String token);
}
