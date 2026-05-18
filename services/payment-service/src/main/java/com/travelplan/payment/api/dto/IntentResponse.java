package com.travelplan.payment.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.travelplan.payment.domain.PaymentProvider;
import com.travelplan.payment.domain.PaymentStatus;
import com.travelplan.payment.domain.PaymentTransaction;

public record IntentResponse(
        UUID id,
        UUID userId,
        UUID bookingRefId,
        PaymentProvider provider,
        String providerIntentId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        String clientSecret,
        String approvalUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static IntentResponse from(PaymentTransaction tx, String clientSecret, String approvalUrl) {
        return new IntentResponse(
                tx.getId(),
                tx.getUserId(),
                tx.getBookingRefId(),
                tx.getProvider(),
                tx.getProviderIntentId(),
                tx.getStatus(),
                tx.getAmount(),
                tx.getCurrency(),
                clientSecret,
                approvalUrl,
                tx.getCreatedAt(),
                tx.getUpdatedAt());
    }
}
