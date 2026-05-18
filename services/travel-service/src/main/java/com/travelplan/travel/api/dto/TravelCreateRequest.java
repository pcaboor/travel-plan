package com.travelplan.travel.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.travelplan.travel.domain.TravelStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TravelCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        LocalDate startDate,
        LocalDate endDate,
        @Positive Integer durationDays,
        @DecimalMin("0.00") BigDecimal price,
        @Size(min = 3, max = 3) String currency,
        TravelStatus status,
        @Valid List<DestinationInput> destinations,
        @Valid List<ActivityInput> activities,
        @Valid List<AccommodationInput> accommodations,
        @Valid List<TransportationInput> transportations) {
}
