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
import org.springframework.test.web.servlet.MockMvc;

import com.travelplan.admin.domain.Booking;
import com.travelplan.admin.domain.BookingStatus;
import com.travelplan.admin.domain.User;
import com.travelplan.admin.domain.UserStatus;
import com.travelplan.admin.repository.BookingRepository;
import com.travelplan.admin.repository.UserRepository;
import com.travelplan.admin.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
class BookingAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Value("${travelplan.jwt.secret}")
    private String secret;

    @Value("${travelplan.jwt.issuer}")
    private String issuer;

    private JwtTestFactory jwt;
    private User user;

    @BeforeEach
    void setup() {
        jwt = new JwtTestFactory(secret, issuer);
        bookingRepository.deleteAll();
        userRepository.deleteAll();
        user = new User();
        user.setEmail("booker-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("hash");
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);
    }

    @Test
    void viewer_can_list_bookings() throws Exception {
        persistBooking(UUID.randomUUID());

        mockMvc.perform(get("/api/admin/bookings")
                        .header("Authorization", jwt.bearer("viewer@example.com", List.of("VIEWER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void get_unknown_booking_returns_404() throws Exception {
        mockMvc.perform(get("/api/admin/bookings/" + UUID.randomUUID())
                        .header("Authorization", jwt.bearer("admin@example.com", List.of("ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_bookings_by_user() throws Exception {
        persistBooking(UUID.randomUUID());
        persistBooking(UUID.randomUUID());

        mockMvc.perform(get("/api/admin/users/{uid}/bookings", user.getId())
                        .header("Authorization", jwt.bearer("viewer@example.com", List.of("VIEWER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void cancel_by_travel_updates_all_matching_bookings() throws Exception {
        UUID travelRef = UUID.randomUUID();
        persistBooking(travelRef);
        persistBooking(travelRef);
        persistBooking(UUID.randomUUID());

        mockMvc.perform(post("/api/admin/bookings/cancel-by-travel/{tr}", travelRef)
                        .header("Authorization", jwt.bearer("admin@example.com", List.of("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelled").value(2));

        long cancelled = bookingRepository.findByStatus(BookingStatus.CANCELLED).size();
        org.assertj.core.api.Assertions.assertThat(cancelled).isEqualTo(2);
    }

    @Test
    void viewer_cannot_cancel_by_travel() throws Exception {
        mockMvc.perform(post("/api/admin/bookings/cancel-by-travel/{tr}", UUID.randomUUID())
                        .header("Authorization", jwt.bearer("viewer@example.com", List.of("VIEWER"))))
                .andExpect(status().isForbidden());
    }

    private void persistBooking(UUID travelRef) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTravelRefId(travelRef);
        booking.setAmount(new BigDecimal("99.00"));
        booking.setCurrency("EUR");
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);
    }
}
