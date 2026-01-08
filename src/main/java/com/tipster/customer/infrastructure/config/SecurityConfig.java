package com.tipster.customer.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tipster.customer.domain.models.ApiResponse;
import com.tipster.customer.infrastructure.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(ObjectMapper objectMapper, Environment environment, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.objectMapper = objectMapper;
        this.environment = environment;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint jsonEntryPoint = (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiResponse<Object> body = ApiResponse.failure("Unauthorized: " + authException.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(body));
        };

        boolean isDevelopment = isDevelopmentEnvironment();

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/health/check").permitAll();
                    auth.requestMatchers("/auth/login").permitAll();
                    auth.requestMatchers("/auth/session").permitAll();
                    auth.requestMatchers("/auth/logout").authenticated();

                    if (isDevelopment) {
                        auth.requestMatchers(
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll();
                    }

                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jsonEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.headers(headers ->
                headers.contentSecurityPolicy(csp -> csp
                        .policyDirectives("frame-ancestors 'self'")
                )
        );

        return http.build();
    }

    private boolean isDevelopmentEnvironment() {
        boolean hasDevProfile = environment.acceptsProfiles("dev", "development", "local");
        boolean isLocalhost = false;
        try {
            String serverName = environment.getProperty("server.address", "localhost");
            isLocalhost = "localhost".equals(serverName) || "127.0.0.1".equals(serverName);
        } catch (Exception e) {
            isLocalhost = false;
        }
        return hasDevProfile || isLocalhost;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Origin", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
