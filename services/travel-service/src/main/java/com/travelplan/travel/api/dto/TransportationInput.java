package com.travelplan.travel.api.dto;

import java.time.OffsetDateTime;

import com.travelplan.travel.domain.TransportationType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransportationInput(
        @NotNull TransportationType type,
        @Size(max = 255) String provider,
        @Size(max = 255) String departureLocation,
        @Size(max = 255) String arrivalLocation,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime) {
}
