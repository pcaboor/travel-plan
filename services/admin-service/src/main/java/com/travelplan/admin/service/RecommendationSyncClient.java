package com.travelplan.admin.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Calls travel-service's recommendation endpoints, forwarding the traveler's token. */
@Component
class RecommendationSyncClient implements RecommendationSync {

    private static final Logger log = LoggerFactory.getLogger(RecommendationSyncClient.class);

    private final RestClient restClient;

    RecommendationSyncClient(@Value("${travelplan.travel-service.base-url:http://travel-service:8083}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void recordParticipation(UUID travelId, String authorization) {
        try {
            restClient.post().uri("/api/travels/{id}/participation", travelId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().toBodilessEntity();
        } catch (RuntimeException e) {
            log.warn("Recommendation sync (participation) failed for travel {}: {}", travelId, e.getMessage());
        }
    }

    @Override
    public void recordRating(UUID travelId, int score, String authorization) {
        try {
            restClient.post().uri("/api/travels/{id}/rating", travelId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("score", score))
                    .retrieve().toBodilessEntity();
        } catch (RuntimeException e) {
            log.warn("Recommendation sync (rating) failed for travel {}: {}", travelId, e.getMessage());
        }
    }
}
