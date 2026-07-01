package com.travelplan.travel.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplan.travel.api.dto.RatingRequest;
import com.travelplan.travel.recommendation.GraphRecommender;
import com.travelplan.travel.recommendation.RecommendationHit;

import jakarta.validation.Valid;

/**
 * Graph-based recommendations. The record endpoints let a traveler (or
 * admin-service on their behalf, forwarding their token) enter the graph.
 */
@RestController
@RequestMapping("/api/travels")
public class RecommendationController {

    private final GraphRecommender recommender;

    public RecommendationController(GraphRecommender recommender) {
        this.recommender = recommender;
    }

    @PostMapping("/{travelId}/participation")
    @PreAuthorize("hasRole('USER')")
    public void recordParticipation(@PathVariable String travelId, @AuthenticationPrincipal Jwt jwt) {
        recommender.recordParticipation(jwt.getSubject(), travelId);
    }

    @PostMapping("/{travelId}/rating")
    @PreAuthorize("hasRole('USER')")
    public void recordRating(@PathVariable String travelId, @Valid @RequestBody RatingRequest request,
                             @AuthenticationPrincipal Jwt jwt) {
        recommender.recordRating(jwt.getSubject(), travelId, request.score());
    }

    @GetMapping("/recommendations")
    @PreAuthorize("hasRole('USER')")
    public List<RecommendationHit> recommendations(@AuthenticationPrincipal Jwt jwt) {
        return recommender.recommend(jwt.getSubject());
    }
}
