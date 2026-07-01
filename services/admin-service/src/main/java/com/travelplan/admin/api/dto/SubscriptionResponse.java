package com.travelplan.admin.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.travelplan.admin.domain.Booking;

public record SubscriptionResponse(
        UUID bookingId,
        UUID travelRefId,
        String status,
        BigDecimal amount,
        String currency,
        LocalDate travelStartDate) {

    public static SubscriptionResponse from(Booking b) {
        return new SubscriptionResponse(b.getId(), b.getTravelRefId(), b.getStatus().name(),
                b.getAmount(), b.getCurrency(), b.getTravelStartDate());
    }
}
