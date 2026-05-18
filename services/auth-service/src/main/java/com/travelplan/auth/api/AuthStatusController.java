package com.travelplan.auth.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthStatusController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "auth-service",
                "status", "UP");
    }
}
