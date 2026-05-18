package com.travelplan.payment.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.travelplan.payment.domain.PaymentProvider;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIntentRequest(
        @NotNull PaymentProvider provider,
        @NotNull @DecimalMin("0.50") BigDecimal amount,
        @NotNull @Size(min = 3, max = 3) String currency,
        UUID bookingRefId) {
}
