package com.travelplan.travel.api.dto;

import com.travelplan.travel.domain.AccommodationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccommodationInput(
        @NotBlank @Size(max = 255) String name,
        @NotNull AccommodationType type,
        @Size(max = 500) String address) {
}
