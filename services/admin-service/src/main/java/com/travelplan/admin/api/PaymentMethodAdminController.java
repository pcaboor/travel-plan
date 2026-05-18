package com.travelplan.admin.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.travelplan.admin.api.dto.PaymentMethodCreateRequest;
import com.travelplan.admin.api.dto.PaymentMethodResponse;
import com.travelplan.admin.api.dto.PaymentMethodUpdateRequest;
import com.travelplan.admin.service.PaymentMethodAdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users/{userId}/payment-methods")
public class PaymentMethodAdminController {

    private final PaymentMethodAdminService service;

    public PaymentMethodAdminController(PaymentMethodAdminService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','VIEWER')")
    public List<PaymentMethodResponse> list(@PathVariable UUID userId) {
        return service.list(userId);
    }

    @GetMapping("/{methodId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','VIEWER')")
    public PaymentMethodResponse get(@PathVariable UUID userId, @PathVariable UUID methodId) {
        return service.get(userId, methodId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentMethodResponse> create(@PathVariable UUID userId,
                                                        @Valid @RequestBody PaymentMethodCreateRequest request) {
        PaymentMethodResponse created = service.create(userId, request);
        URI location = UriComponentsBuilder
                .fromPath("/api/admin/users/{userId}/payment-methods/{methodId}")
                .buildAndExpand(userId, created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{methodId}")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentMethodResponse update(@PathVariable UUID userId,
                                        @PathVariable UUID methodId,
                                        @Valid @RequestBody PaymentMethodUpdateRequest request) {
        return service.update(userId, methodId, request);
    }

    @DeleteMapping("/{methodId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID methodId) {
        service.delete(userId, methodId);
        return ResponseEntity.noContent().build();
    }
}
