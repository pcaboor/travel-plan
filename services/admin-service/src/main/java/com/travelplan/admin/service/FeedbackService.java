package com.travelplan.admin.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.admin.api.dto.FeedbackCreateRequest;
import com.travelplan.admin.api.dto.FeedbackResponse;
import com.travelplan.admin.domain.BookingStatus;
import com.travelplan.admin.domain.Feedback;
import com.travelplan.admin.repository.BookingRepository;
import com.travelplan.admin.repository.FeedbackRepository;

/**
 * Traveler feedback on travels. A traveler may review a travel only once and
 * only if they participated in it (a CONFIRMED or COMPLETED booking).
 */
@Service
@Transactional
public class FeedbackService {

    private static final Set<BookingStatus> PARTICIPATED =
            EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);

    private final FeedbackRepository feedback;
    private final BookingRepository bookings;

    public FeedbackService(FeedbackRepository feedback, BookingRepository bookings) {
        this.feedback = feedback;
        this.bookings = bookings;
    }

    public FeedbackResponse create(UUID authorUserId, FeedbackCreateRequest request) {
        boolean participated = bookings.findByUserIdAndTravelRefId(authorUserId, request.travelId()).stream()
                .anyMatch(b -> PARTICIPATED.contains(b.getStatus()));
        if (!participated) {
            throw new ConflictException("You can only review a travel you participated in");
        }
        if (feedback.existsByAuthorUserIdAndTravelRefId(authorUserId, request.travelId())) {
            throw new ConflictException("You already reviewed this travel");
        }

        Feedback entity = new Feedback();
        entity.setAuthorUserId(authorUserId);
        entity.setTravelRefId(request.travelId());
        entity.setRating(request.rating());
        entity.setComment(request.comment());
        return FeedbackResponse.from(feedback.save(entity));
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> listForTravel(UUID travelId) {
        return feedback.findByTravelRefId(travelId).stream().map(FeedbackResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> listMine(UUID authorUserId) {
        return feedback.findByAuthorUserId(authorUserId).stream().map(FeedbackResponse::from).toList();
    }
}
