package com.travelplan.admin.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
        import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.admin.api.dto.SubscriptionResponse;
import com.travelplan.admin.domain.Booking;
import com.travelplan.admin.domain.BookingStatus;
import com.travelplan.admin.domain.User;
import com.travelplan.admin.repository.BookingRepository;
import com.travelplan.admin.repository.UserRepository;
import com.travelplan.admin.service.TravelLookup.TravelSnapshot;

/**
 * Traveler-facing subscription lifecycle. A subscription is a {@link Booking}:
 * subscribing creates a PENDING booking (payment confirms it later, V2-2b);
 * unsubscribing is only allowed up to {@value #CUTOFF_DAYS} days before departure.
 */
@Service
@Transactional
public class SubscriptionService {

    static final int CUTOFF_DAYS = 3;
    private static final Set<BookingStatus> ACTIVE = EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookings;
    private final UserRepository users;
    private final TravelLookup travelLookup;
    private final PaymentLookup paymentLookup;
    private final RecommendationSync recommendationSync;

    public SubscriptionService(BookingRepository bookings, UserRepository users,
                               TravelLookup travelLookup, PaymentLookup paymentLookup,
                               RecommendationSync recommendationSync) {
        this.bookings = bookings;
        this.users = users;
        this.travelLookup = travelLookup;
        this.paymentLookup = paymentLookup;
        this.recommendationSync = recommendationSync;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> listMine(UUID userId) {
        return bookings.findByUserId(userId).stream().map(SubscriptionResponse::from).toList();
    }

    public SubscriptionResponse subscribe(UUID userId, UUID travelId, String authorization) {
        User user = users.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        TravelSnapshot travel = travelLookup.fetch(travelId, authorization)
                .orElseThrow(() -> new NotFoundException("Travel not found: " + travelId));
        if (!"PUBLISHED".equals(travel.status())) {
            throw new ConflictException("Travel is not open for subscription");
        }
        boolean alreadySubscribed = bookings.findByUserIdAndTravelRefId(userId, travelId).stream()
                .anyMatch(b -> ACTIVE.contains(b.getStatus()));
        if (alreadySubscribed) {
            throw new ConflictException("Already subscribed to this travel");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTravelRefId(travelId);
        booking.setAmount(travel.price() != null ? travel.price() : BigDecimal.ZERO);
        booking.setCurrency(travel.currency() != null ? travel.currency() : "EUR");
        booking.setTravelStartDate(travel.startDate());
        booking.setManagerId(travel.managerId() != null ? UUID.fromString(travel.managerId()) : null);
        booking.setStatus(BookingStatus.PENDING);
        return SubscriptionResponse.from(bookings.save(booking));
    }

    public SubscriptionResponse unsubscribe(UUID userId, UUID travelId) {
        Booking booking = bookings.findByUserIdAndTravelRefId(userId, travelId).stream()
                .filter(b -> ACTIVE.contains(b.getStatus()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No active subscription for this travel"));

        LocalDate start = booking.getTravelStartDate();
        if (start != null && ChronoUnit.DAYS.between(LocalDate.now(), start) < CUTOFF_DAYS) {
            throw new ConflictException(
                    "Unsubscription closed: must be at least " + CUTOFF_DAYS + " days before departure");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return SubscriptionResponse.from(bookings.save(booking));
    }

    /**
     * Confirms a subscription once its payment has succeeded (V2-2b). The traveler
     * pays via payment-service; this transitions the booking PENDING -> CONFIRMED
     * after checking payment-service reports the booking as paid.
     */
    public SubscriptionResponse confirm(UUID userId, UUID travelId, String authorization) {
        Booking booking = bookings.findByUserIdAndTravelRefId(userId, travelId).stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No pending subscription for this travel"));
        if (!paymentLookup.isBookingPaid(booking.getId(), authorization)) {
            throw new ConflictException("Payment not completed for this subscription");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        SubscriptionResponse response = SubscriptionResponse.from(bookings.save(booking));
        recommendationSync.recordParticipation(travelId, authorization);
        return response;
    }
}
