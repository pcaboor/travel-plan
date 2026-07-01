package com.travelplan.payment.api;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplan.payment.api.dto.BookingPaymentStatus;
import com.travelplan.payment.service.PaymentService;

/**
 * Exposes whether a booking has been paid, so admin-service can confirm the
 * corresponding subscription (V2-2b).
 */
@RestController
@RequestMapping("/api/payments/bookings")
public class BookingPaymentController {

    private final PaymentService service;

    public BookingPaymentController(PaymentService service) {
        this.service = service;
    }

    @GetMapping("/{bookingRefId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public BookingPaymentStatus status(@PathVariable UUID bookingRefId) {
        return service.statusForBooking(bookingRefId);
    }
}
