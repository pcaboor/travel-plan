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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travelplan.admin.api.dto.ReportCreateRequest;
import com.travelplan.admin.api.dto.ReportResponse;
import com.travelplan.admin.api.dto.ReportStatusUpdateRequest;
import com.travelplan.admin.domain.ReportStatus;
import com.travelplan.admin.service.ReportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ReportResponse create(@Valid @RequestBody ReportCreateRequest request,
                                 @AuthenticationPrincipal Jwt jwt) {
        return service.create(UUID.fromString(jwt.getSubject()), request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<ReportResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return service.listMine(UUID.fromString(jwt.getSubject()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReportResponse> list(@RequestParam(required = false) ReportStatus status) {
        return service.list(status);
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ReportResponse updateStatus(@PathVariable UUID id,
                                       @Valid @RequestBody ReportStatusUpdateRequest request) {
        return service.updateStatus(id, request.status());
    }
}
