package com.travelplan.payment.api;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.travelplan.payment.domain.PaymentProvider;
import com.travelplan.payment.domain.PaymentStatus;
import com.travelplan.payment.domain.PaymentTransaction;
import com.travelplan.payment.provider.PaypalAdapter;
import com.travelplan.payment.provider.StripeAdapter;
import com.travelplan.payment.repository.PaymentTransactionRepository;
import com.travelplan.payment.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
class BookingPaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentTransactionRepository repository;

    @MockBean
    private StripeAdapter stripeAdapter;

    @MockBean
    private PaypalAdapter paypalAdapter;

    @Value("${travelplan.jwt.secret}")
    private String secret;

    @Value("${travelplan.jwt.issuer}")
    private String issuer;

    private String userAuth;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        userAuth = new JwtTestFactory(secret, issuer).bearer(UUID.randomUUID(), "user@example.com", List.of("USER"));
    }

    @Test
    void booking_is_paid_when_a_succeeded_transaction_exists() throws Exception {
        UUID bookingRef = UUID.randomUUID();
        persist(bookingRef, PaymentStatus.SUCCEEDED);

        mockMvc.perform(get("/api/payments/bookings/{ref}", bookingRef).header("Authorization", userAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid").value(true));
    }

    @Test
    void booking_is_not_paid_when_only_a_pending_transaction_exists() throws Exception {
        UUID bookingRef = UUID.randomUUID();
        persist(bookingRef, PaymentStatus.PENDING);

        mockMvc.perform(get("/api/payments/bookings/{ref}", bookingRef).header("Authorization", userAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid").value(false));
    }

    @Test
    void booking_is_not_paid_when_no_transaction_exists() throws Exception {
        mockMvc.perform(get("/api/payments/bookings/{ref}", UUID.randomUUID()).header("Authorization", userAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid").value(false));
    }

    @Test
    void unauthenticated_request_returns_401() throws Exception {
        mockMvc.perform(get("/api/payments/bookings/{ref}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private void persist(UUID bookingRef, PaymentStatus status) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setUserId(UUID.randomUUID());
        tx.setBookingRefId(bookingRef);
        tx.setProvider(PaymentProvider.STRIPE);
        tx.setAmount(new BigDecimal("50.00"));
        tx.setCurrency("EUR");
        tx.setStatus(status);
        repository.save(tx);
    }
}
