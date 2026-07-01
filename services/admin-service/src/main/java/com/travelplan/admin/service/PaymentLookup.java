package com.travelplan.admin.service;

import java.util.UUID;

/**
 * Asks payment-service whether a booking has been paid. Abstracted behind an
 * interface so it can be mocked in tests.
 */
public interface PaymentLookup {

    boolean isBookingPaid(UUID bookingRefId, String authorization);
}
