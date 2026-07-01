package com.travelplan.admin.api;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplan.admin.api.dto.SubscriptionResponse;
import com.travelplan.admin.service.SubscriptionService;

/**
 * Traveler-facing subscription endpoints. {@code hasRole('USER')} — through the
 * role hierarchy (V2-1), managers and admins inherit the traveler capability.
 */
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @PostMapping("/{travelId}")
    @PreAuthorize("hasRole('USER')")
    public SubscriptionResponse subscribe(@PathVariable UUID travelId, @AuthenticationPrincipal Jwt jwt) {
        return service.subscribe(UUID.fromString(jwt.getSubject()), travelId, "Bearer " + jwt.getTokenValue());
    }

    @PostMapping("/{travelId}/unsubscribe")
    @PreAuthorize("hasRole('USER')")
    public SubscriptionResponse unsubscribe(@PathVariable UUID travelId, @AuthenticationPrincipal Jwt jwt) {
        return service.unsubscribe(UUID.fromString(jwt.getSubject()), travelId);
    }
}
