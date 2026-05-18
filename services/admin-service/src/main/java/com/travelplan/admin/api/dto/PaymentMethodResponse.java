package com.travelplan.admin.api.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.travelplan.admin.domain.PaymentMethod;
import com.travelplan.admin.domain.PaymentMethodStatus;
import com.travelplan.admin.domain.PaymentMethodType;
import com.travelplan.admin.domain.PaymentProvider;

public record PaymentMethodResponse(
        UUID id,
        UUID userId,
        PaymentProvider provider,
        PaymentMethodType type,
        String lastFour,
        LocalDate expiresAt,
        PaymentMethodStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static PaymentMethodResponse from(PaymentMethod method) {
        return new PaymentMethodResponse(
                method.getId(),
                method.getUser().getId(),
                method.getProvider(),
                method.getType(),
                method.getLastFour(),
                method.getExpiresAt(),
                method.getStatus(),
                method.getCreatedAt(),
                method.getUpdatedAt());
    }
}
