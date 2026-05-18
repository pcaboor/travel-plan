package com.travelplan.travel.api;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.travelplan.travel.api.dto.TravelCreateRequest;
import com.travelplan.travel.api.dto.TravelResponse;
import com.travelplan.travel.api.dto.TravelUpdateRequest;
import com.travelplan.travel.service.TravelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/travels")
public class TravelController {

    private final TravelService service;

    public TravelController(TravelService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','VIEWER','USER')")
    public Page<TravelResponse> list(@PageableDefault(size = 25) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','VIEWER','USER')")
    public TravelResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<TravelResponse> create(@Valid @RequestBody TravelCreateRequest request) {
        TravelResponse created = service.create(request);
        URI location = UriComponentsBuilder.fromPath("/api/travels/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public TravelResponse update(@PathVariable String id, @Valid @RequestBody TravelUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
