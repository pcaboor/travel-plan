package com.travelplan.travel.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/travels")
public class TravelStatusController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "travel-service",
                "status", "UP");
    }
}
