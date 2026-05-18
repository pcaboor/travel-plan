package com.travelplan.payment.api;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.travelplan.payment.api.dto.CreateIntentRequest;
import com.travelplan.payment.api.dto.IntentResponse;
import com.travelplan.payment.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments/intents")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<IntentResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                 @Valid @RequestBody CreateIntentRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        IntentResponse created = service.createIntent(userId, request);
        URI location = UriComponentsBuilder.fromPath("/api/payments/intents/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public IntentResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping("/{id}/refresh")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public IntentResponse refresh(@PathVariable UUID id) {
        return service.refresh(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public IntentResponse cancel(@PathVariable UUID id) {
        return service.cancel(id);
    }
}
