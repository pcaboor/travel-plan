package com.travelplan.travel.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DestinationInput(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 100) String country,
        Double latitude,
        Double longitude,
        @NotNull @Positive Integer order) {
}
