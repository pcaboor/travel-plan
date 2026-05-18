package com.travelplan.admin.api.dto;

import java.time.LocalDate;

import com.travelplan.admin.domain.PaymentMethodStatus;

import jakarta.validation.constraints.Size;

public record PaymentMethodUpdateRequest(
        @Size(min = 4, max = 4) String lastFour,
        LocalDate expiresAt,
        PaymentMethodStatus status) {
}
