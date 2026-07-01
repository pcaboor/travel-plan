package com.travelplan.admin.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.travelplan.admin.domain.Booking;
import com.travelplan.admin.domain.BookingStatus;
import com.travelplan.admin.domain.User;
import com.travelplan.admin.domain.UserStatus;
import com.travelplan.admin.repository.BookingRepository;
import com.travelplan.admin.repository.UserRepository;
import com.travelplan.admin.service.PaymentLookup;
import com.travelplan.admin.service.TravelLookup;
import com.travelplan.admin.service.TravelLookup.TravelSnapshot;
import com.travelplan.admin.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
class SubscriptionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private TravelLookup travelLookup;

    @MockBean
    private PaymentLookup paymentLookup;

    @Value("${travelplan.jwt.secret}")
    private String secret;

    @Value("${travelplan.jwt.issuer}")
    private String issuer;

    private JwtTestFactory jwt;
    private User traveler;
    private String travelerAuth;
    private UUID travelId;

    @BeforeEach
    void setup() {
        jwt = new JwtTestFactory(secret, issuer);
        bookingRepository.deleteAll();
        userRepository.deleteAll();

        traveler = new User();
        traveler.setEmail("traveler-" + UUID.randomUUID() + "@example.com");
        traveler.setPasswordHash("hash");
        traveler.setStatus(UserStatus.ACTIVE);
        traveler = userRepository.save(traveler);
        travelerAuth = "Bearer " + jwt.token(traveler.getId().toString(), traveler.getEmail(), List.of("USER"));

        travelId = UUID.randomUUID();
        // Default: a published travel starting well beyond the cutoff.
        when(travelLookup.fetch(any(), any())).thenReturn(Optional.of(
                new TravelSnapshot(LocalDate.now().plusDays(30), new BigDecimal("500.00"), "EUR", "PUBLISHED",
                        UUID.randomUUID().toString())));
    }

    @Test
    void traveler_subscribes_and_gets_a_pending_booking() throws Exception {
        mockMvc.perform(post("/api/subscriptions/{id}", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.travelRefId").value(travelId.toString()));

        org.assertj.core.api.Assertions.assertThat(bookingRepository.findByUserId(traveler.getId())).hasSize(1);
    }

    @Test
    void subscribing_twice_to_the_same_travel_conflicts() throws Exception {
        mockMvc.perform(post("/api/subscriptions/{id}", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/subscriptions/{id}", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isConflict());
    }

    @Test
    void subscribing_to_an_unpublished_travel_conflicts() throws Exception {
        when(travelLookup.fetch(any(), any())).thenReturn(Optional.of(
                new TravelSnapshot(LocalDate.now().plusDays(30), new BigDecimal("500.00"), "EUR", "DRAFT",
                        UUID.randomUUID().toString())));
        mockMvc.perform(post("/api/subscriptions/{id}", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isConflict());
    }

    @Test
    void subscribing_to_an_unknown_travel_returns_404() throws Exception {
        when(travelLookup.fetch(any(), any())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/subscriptions/{id}", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isNotFound());
    }

    @Test
    void unsubscribe_before_the_cutoff_cancels_the_booking() throws Exception {
        mockMvc.perform(post("/api/subscriptions/{id}", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/subscriptions/{id}/unsubscribe", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void unsubscribe_after_the_cutoff_conflicts() throws Exception {
        // Booking whose travel departs tomorrow: inside the 3-day cutoff.
        Booking booking = new Booking();
        booking.setUser(traveler);
        booking.setTravelRefId(travelId);
        booking.setAmount(new BigDecimal("500.00"));
        booking.setCurrency("EUR");
        booking.setStatus(BookingStatus.PENDING);
        booking.setTravelStartDate(LocalDate.now().plusDays(1));
        bookingRepository.save(booking);

        mockMvc.perform(post("/api/subscriptions/{id}/unsubscribe", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isConflict());
    }

    @Test
    void unsubscribe_without_an_active_subscription_returns_404() throws Exception {
        mockMvc.perform(post("/api/subscriptions/{id}/unsubscribe", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirm_marks_the_booking_confirmed_when_paid() throws Exception {
        mockMvc.perform(post("/api/subscriptions/{id}", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isOk());
        when(paymentLookup.isBookingPaid(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/subscriptions/{id}/confirm", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void confirm_conflicts_when_payment_is_not_completed() throws Exception {
        mockMvc.perform(post("/api/subscriptions/{id}", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isOk());
        when(paymentLookup.isBookingPaid(any(), any())).thenReturn(false);

        mockMvc.perform(post("/api/subscriptions/{id}/confirm", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isConflict());
    }

    @Test
    void confirm_without_a_pending_subscription_returns_404() throws Exception {
        mockMvc.perform(post("/api/subscriptions/{id}/confirm", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isNotFound());
    }
}
