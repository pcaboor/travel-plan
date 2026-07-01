package com.travelplan.admin.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.admin.api.dto.FeedbackCreateRequest;
import com.travelplan.admin.domain.Booking;
import com.travelplan.admin.domain.BookingStatus;
import com.travelplan.admin.domain.User;
import com.travelplan.admin.domain.UserStatus;
import com.travelplan.admin.repository.BookingRepository;
import com.travelplan.admin.repository.FeedbackRepository;
import com.travelplan.admin.repository.UserRepository;
import com.travelplan.admin.service.RecommendationSync;
import com.travelplan.admin.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
class FeedbackControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @MockBean
    private RecommendationSync recommendationSync;

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
        feedbackRepository.deleteAll();
        bookingRepository.deleteAll();
        userRepository.deleteAll();

        traveler = new User();
        traveler.setEmail("traveler-" + UUID.randomUUID() + "@example.com");
        traveler.setPasswordHash("hash");
        traveler.setStatus(UserStatus.ACTIVE);
        traveler = userRepository.save(traveler);
        travelerAuth = "Bearer " + jwt.token(traveler.getId().toString(), traveler.getEmail(), List.of("USER"));

        travelId = UUID.randomUUID();
        persistBooking(travelId, BookingStatus.CONFIRMED); // participation
    }

    @Test
    void participant_can_leave_feedback() throws Exception {
        mockMvc.perform(post("/api/feedback").header("Authorization", travelerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(travelId, 5, "Great trip")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.travelRefId").value(travelId.toString()));
    }

    @Test
    void cannot_review_a_travel_not_participated_in() throws Exception {
        mockMvc.perform(post("/api/feedback").header("Authorization", travelerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(UUID.randomUUID(), 4, "Never went")))
                .andExpect(status().isConflict());
    }

    @Test
    void cannot_review_the_same_travel_twice() throws Exception {
        mockMvc.perform(post("/api/feedback").header("Authorization", travelerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(travelId, 5, "First")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/feedback").header("Authorization", travelerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(travelId, 3, "Second")))
                .andExpect(status().isConflict());
    }

    @Test
    void rating_out_of_range_is_rejected() throws Exception {
        mockMvc.perform(post("/api/feedback").header("Authorization", travelerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(travelId, 6, "Too high")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void feedback_can_be_listed_for_a_travel() throws Exception {
        mockMvc.perform(post("/api/feedback").header("Authorization", travelerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(travelId, 4, "Nice")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/feedback/travels/{id}", travelId).header("Authorization", travelerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void traveler_can_list_their_own_feedback() throws Exception {
        mockMvc.perform(post("/api/feedback").header("Authorization", travelerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(travelId, 4, "Mine")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/feedback/me").header("Authorization", travelerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    private String body(UUID travel, int rating, String comment) throws Exception {
        return objectMapper.writeValueAsString(new FeedbackCreateRequest(travel, rating, comment));
    }

    private void persistBooking(UUID travelRef, BookingStatus status) {
        Booking booking = new Booking();
        booking.setUser(traveler);
        booking.setTravelRefId(travelRef);
        booking.setAmount(new BigDecimal("100.00"));
        booking.setCurrency("EUR");
        booking.setStatus(status);
        bookingRepository.save(booking);
    }
}
