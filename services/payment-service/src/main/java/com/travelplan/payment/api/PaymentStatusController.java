package com.travelplan.payment.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentStatusController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "service", "payment-service",
                "status", "UP",
                "supportedProviders", List.of("stripe", "paypal"));
    }
}
