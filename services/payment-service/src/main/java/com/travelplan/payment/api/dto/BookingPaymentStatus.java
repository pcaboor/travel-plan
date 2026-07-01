package com.travelplan.payment.api.dto;

import java.util.UUID;

/** Whether a booking has a successful payment. Consumed by admin-service to confirm a subscription. */
public record BookingPaymentStatus(UUID bookingRefId, boolean paid) {
}
