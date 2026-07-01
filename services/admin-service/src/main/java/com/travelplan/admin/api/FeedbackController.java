package com.travelplan.admin.api;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplan.admin.api.dto.FeedbackCreateRequest;
import com.travelplan.admin.api.dto.FeedbackResponse;
import com.travelplan.admin.service.FeedbackService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public FeedbackResponse create(@Valid @RequestBody FeedbackCreateRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.create(UUID.fromString(jwt.getSubject()), request, "Bearer " + jwt.getTokenValue());
    }

    @GetMapping("/travels/{travelId}")
    @PreAuthorize("hasRole('USER')")
    public List<FeedbackResponse> forTravel(@PathVariable UUID travelId) {
        return service.listForTravel(travelId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<FeedbackResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return service.listMine(UUID.fromString(jwt.getSubject()));
    }
}
