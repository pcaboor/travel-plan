package com.travelplan.travel.recommendation;

/** A recommended travel with the number of shared characteristics that scored it. */
public record RecommendationHit(String id, String title, String status, int score) {
}
