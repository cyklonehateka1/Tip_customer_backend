package com.tipster.customer.infrastructure.web.controllers;

import com.tipster.customer.domain.models.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("health")
@RequiredArgsConstructor
public class HealthController {

    private static final Logger _logger = LoggerFactory.getLogger(HealthController.class);

    @GetMapping("check")
    public ResponseEntity<ApiResponse<Map<String, String>>> check() {
        Map<String, String> healthData = new LinkedHashMap<>();
        try {
            healthData.put("Status", "Ok");
            healthData.put("AppName", "tipster-customer-api");
            healthData.put("Message", "I'm alive");

            _logger.info("information");
            _logger.warn("warning");
            _logger.error("error");
            return ResponseEntity.ok(ApiResponse.success("Service is healthy", healthData));
        } catch (Exception ex) {
            _logger.error("Health check failed", ex);
            healthData.put("Status", "Error");
            healthData.put("Message", "Health check failed");
            return ResponseEntity.status(500).body(ApiResponse.failure("Health check failed"));
        }
    }
}
