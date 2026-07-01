package com.travelplan.admin.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.travelplan.admin.domain.Feedback;

public record FeedbackResponse(
        UUID id,
        UUID travelRefId,
        UUID authorUserId,
        int rating,
        String comment,
        OffsetDateTime createdAt) {

    public static FeedbackResponse from(Feedback f) {
        return new FeedbackResponse(f.getId(), f.getTravelRefId(), f.getAuthorUserId(),
                f.getRating(), f.getComment(), f.getCreatedAt());
    }
}
