package com.travelplan.admin.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Calls payment-service GET /api/payments/bookings/{ref}, forwarding the
 * caller's bearer token.
 */
@Component
class PaymentClient implements PaymentLookup {

    private final RestClient restClient;

    PaymentClient(@Value("${travelplan.payment-service.base-url:http://payment-service:8084}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public boolean isBookingPaid(UUID bookingRefId, String authorization) {
        try {
            BookingPaymentDto dto = restClient.get()
                    .uri("/api/payments/bookings/{id}", bookingRefId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(BookingPaymentDto.class);
            return dto != null && dto.paid();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return false;
            }
            throw ex;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record BookingPaymentDto(boolean paid) {
    }
}
