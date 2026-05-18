package com.travelplan.travel.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ActivityInput(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 2000) String description,
        @Size(max = 100) String category,
        @PositiveOrZero Integer durationMinutes) {
}
