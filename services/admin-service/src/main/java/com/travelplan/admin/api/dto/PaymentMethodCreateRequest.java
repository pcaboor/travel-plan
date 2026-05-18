package com.travelplan.admin.api.dto;

import java.time.LocalDate;

import com.travelplan.admin.domain.PaymentMethodStatus;
import com.travelplan.admin.domain.PaymentMethodType;
import com.travelplan.admin.domain.PaymentProvider;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentMethodCreateRequest(
        @NotNull PaymentProvider provider,
        @NotNull PaymentMethodType type,
        @Size(max = 255) String providerToken,
        @Size(min = 4, max = 4) String lastFour,
        LocalDate expiresAt,
        PaymentMethodStatus status) {
}
