package com.travelplan.admin.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;

import com.travelplan.admin.domain.Booking;
import com.travelplan.admin.domain.BookingStatus;
import com.travelplan.admin.domain.Feedback;
import com.travelplan.admin.domain.PaymentMethod;
import com.travelplan.admin.domain.PaymentMethodType;
import com.travelplan.admin.domain.PaymentProvider;
import com.travelplan.admin.domain.Report;
import com.travelplan.admin.domain.ReportTargetType;
import com.travelplan.admin.domain.User;
import com.travelplan.admin.domain.UserStatus;
import com.travelplan.admin.repository.BookingRepository;
import com.travelplan.admin.repository.FeedbackRepository;
import com.travelplan.admin.repository.PaymentMethodRepository;
import com.travelplan.admin.repository.ReportRepository;
import com.travelplan.admin.repository.UserRepository;
import com.travelplan.admin.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
class StatsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Value("${travelplan.jwt.secret}")
    private String secret;

    @Value("${travelplan.jwt.issuer}")
    private String issuer;

    private JwtTestFactory jwt;
    private User traveler;

    @BeforeEach
    void setup() {
        jwt = new JwtTestFactory(secret, issuer);
        feedbackRepository.deleteAll();
        reportRepository.deleteAll();
        bookingRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        userRepository.deleteAll();
        traveler = seedUser();
    }

    @Test
    void traveler_sees_their_personal_stats() throws Exception {
        UUID manager = UUID.randomUUID();
        booking(manager, BookingStatus.CONFIRMED, "100");
        booking(manager, BookingStatus.COMPLETED, "100");
        booking(manager, BookingStatus.CANCELLED, "100");
        report(traveler.getId(), ReportTargetType.MANAGER, UUID.randomUUID());
        feedback(traveler.getId(), UUID.randomUUID(), manager, 5);
        paymentMethod(PaymentProvider.STRIPE);

        mockMvc.perform(get("/api/stats/me").header("Authorization", userToken(traveler.getId(), "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participations").value(2))
                .andExpect(jsonPath("$.cancellations").value(1))
                .andExpect(jsonPath("$.reportsFiled").value(1))
                .andExpect(jsonPath("$.feedbackGiven").value(1))
                .andExpect(jsonPath("$.paymentProviders[0]").value("STRIPE"));
    }

    @Test
    void manager_sees_their_dashboard() throws Exception {
        UUID manager = UUID.randomUUID();
        booking(manager, BookingStatus.CONFIRMED, "100");
        booking(manager, BookingStatus.CONFIRMED, "200");
        feedback(UUID.randomUUID(), UUID.randomUUID(), manager, 4);
        feedback(UUID.randomUUID(), UUID.randomUUID(), manager, 5);

        mockMvc.perform(get("/api/stats/manager/me").header("Authorization", userToken(manager, "MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").value(2))
                .andExpect(jsonPath("$.travelers").value(2))
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }

    @Test
    void leaderboard_ranks_the_better_manager_first() throws Exception {
        UUID good = UUID.randomUUID();
        UUID bad = UUID.randomUUID();
        booking(good, BookingStatus.CONFIRMED, "1000");
        feedback(UUID.randomUUID(), UUID.randomUUID(), good, 5);
        booking(bad, BookingStatus.CONFIRMED, "10");
        feedback(UUID.randomUUID(), UUID.randomUUID(), bad, 1);
        report(UUID.randomUUID(), ReportTargetType.MANAGER, bad);

        mockMvc.perform(get("/api/stats/managers/leaderboard").header("Authorization", jwt.bearer("admin@example.com", List.of("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].managerId").value(good.toString()));
    }

    @Test
    void a_traveler_cannot_view_the_manager_dashboard() throws Exception {
        mockMvc.perform(get("/api/stats/manager/me").header("Authorization", userToken(traveler.getId(), "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void a_traveler_cannot_view_the_leaderboard() throws Exception {
        mockMvc.perform(get("/api/stats/managers/leaderboard").header("Authorization", userToken(traveler.getId(), "USER")))
                .andExpect(status().isForbidden());
    }

    private User seedUser() {
        User u = new User();
        u.setEmail("user-" + UUID.randomUUID() + "@example.com");
        u.setPasswordHash("hash");
        u.setStatus(UserStatus.ACTIVE);
        return userRepository.save(u);
    }

    private void booking(UUID managerId, BookingStatus status, String amount) {
        Booking b = new Booking();
        b.setUser(traveler);
        b.setTravelRefId(UUID.randomUUID());
        b.setManagerId(managerId);
        b.setAmount(new BigDecimal(amount));
        b.setCurrency("EUR");
        b.setStatus(status);
        bookingRepository.save(b);
    }

    private void feedback(UUID authorId, UUID travelRef, UUID managerId, int rating) {
        Feedback f = new Feedback();
        f.setAuthorUserId(authorId);
        f.setTravelRefId(travelRef);
        f.setManagerId(managerId);
        f.setRating(rating);
        feedbackRepository.save(f);
    }

    private void report(UUID reporterId, ReportTargetType type, UUID targetId) {
        Report r = new Report();
        r.setReporterUserId(reporterId);
        r.setTargetType(type);
        r.setTargetId(targetId);
        r.setReason("reason");
        reportRepository.save(r);
    }

    private void paymentMethod(PaymentProvider provider) {
        PaymentMethod pm = new PaymentMethod();
        pm.setUser(traveler);
        pm.setProvider(provider);
        pm.setType(PaymentMethodType.CARD);
        paymentMethodRepository.save(pm);
    }

    private String userToken(UUID userId, String role) {
        return "Bearer " + jwt.token(userId.toString(), "u@example.com", List.of(role));
    }
}
