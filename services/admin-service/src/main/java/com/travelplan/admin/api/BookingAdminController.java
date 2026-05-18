package com.travelplan.admin.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplan.admin.api.dto.BookingResponse;
import com.travelplan.admin.service.BookingAdminService;

@RestController
@RequestMapping("/api/admin")
public class BookingAdminController {

    private final BookingAdminService service;

    public BookingAdminController(BookingAdminService service) {
        this.service = service;
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','VIEWER')")
    public Page<BookingResponse> list(@PageableDefault(size = 25) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/bookings/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','VIEWER')")
    public BookingResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping("/users/{userId}/bookings")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','VIEWER')")
    public List<BookingResponse> listByUser(@PathVariable UUID userId) {
        return service.listByUser(userId);
    }

    @PostMapping("/bookings/cancel-by-travel/{travelRefId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Map<String, Integer> cancelByTravel(@PathVariable UUID travelRefId) {
        int cancelled = service.cancelByTravel(travelRefId);
        return Map.of("cancelled", cancelled);
    }
}
