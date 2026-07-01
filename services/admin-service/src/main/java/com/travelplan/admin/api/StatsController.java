package com.travelplan.admin.api;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplan.admin.api.dto.ManagerDashboard;
import com.travelplan.admin.api.dto.ManagerScore;
import com.travelplan.admin.api.dto.TravelerStats;
import com.travelplan.admin.service.StatsService;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    /** Personal stats for the authenticated traveler. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public TravelerStats me(@AuthenticationPrincipal Jwt jwt) {
        return service.travelerStats(UUID.fromString(jwt.getSubject()));
    }

    /** Dashboard for the authenticated manager (their own travels). */
    @GetMapping("/manager/me")
    @PreAuthorize("hasRole('MANAGER')")
    public ManagerDashboard managerDashboard(@AuthenticationPrincipal Jwt jwt) {
        return service.managerDashboard(UUID.fromString(jwt.getSubject()));
    }

    /** Managers ranked by performance score. Admin only. */
    @GetMapping("/managers/leaderboard")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ManagerScore> leaderboard() {
        return service.leaderboard();
    }
}
