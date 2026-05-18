package com.travelplan.admin.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.travelplan.admin.domain.Booking;
import com.travelplan.admin.domain.BookingStatus;

public record BookingResponse(
        UUID id,
        UUID userId,
        UUID travelRefId,
        UUID paymentMethodId,
        BigDecimal amount,
        String currency,
        BookingStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getTravelRefId(),
                booking.getPaymentMethod() == null ? null : booking.getPaymentMethod().getId(),
                booking.getAmount(),
                booking.getCurrency(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getUpdatedAt());
    }
}
