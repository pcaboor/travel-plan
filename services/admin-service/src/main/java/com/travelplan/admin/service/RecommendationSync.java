package com.travelplan.admin.service;

import java.util.UUID;

/**
 * Pushes traveler activity into travel-service's recommendation graph. Best-effort:
 * a failure never blocks the subscription confirmation or the feedback.
 */
public interface RecommendationSync {

    void recordParticipation(UUID travelId, String authorization);

    void recordRating(UUID travelId, int score, String authorization);
}
