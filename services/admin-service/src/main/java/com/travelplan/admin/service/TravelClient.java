package com.travelplan.admin.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Calls travel-service GET /api/travels/{id}, forwarding the caller's bearer
 * token so the traveler's own authorization applies to the read.
 */
@Component
class TravelClient implements TravelLookup {

    private final RestClient restClient;

    TravelClient(@Value("${travelplan.travel-service.base-url:http://travel-service:8083}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public Optional<TravelSnapshot> fetch(UUID travelId, String authorization) {
        try {
            TravelDto dto = restClient.get()
                    .uri("/api/travels/{id}", travelId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(TravelDto.class);
            return Optional.ofNullable(dto)
                    .map(d -> new TravelSnapshot(d.startDate(), d.price(), d.currency(), d.status(), d.managerId()));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TravelDto(LocalDate startDate, BigDecimal price, String currency, String status, String managerId) {
    }
}
