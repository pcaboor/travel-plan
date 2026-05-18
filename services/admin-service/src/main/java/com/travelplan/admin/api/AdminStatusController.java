package com.travelplan.admin.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminStatusController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "admin-service",
                "status", "UP");
    }
}
