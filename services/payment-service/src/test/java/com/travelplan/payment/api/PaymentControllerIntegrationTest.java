package com.travelplan.payment.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.payment.api.dto.CreateIntentRequest;
import com.travelplan.payment.domain.PaymentProvider;
import com.travelplan.payment.domain.PaymentStatus;
import com.travelplan.payment.provider.PaypalAdapter;
import com.travelplan.payment.provider.ProviderIntent;
import com.travelplan.payment.provider.StripeAdapter;
import com.travelplan.payment.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StripeAdapter stripeAdapter;

    @MockBean
    private PaypalAdapter paypalAdapter;

    @Value("${travelplan.jwt.secret}")
    private String secret;

    @Value("${travelplan.jwt.issuer}")
    private String issuer;

    private JwtTestFactory jwt;

    JwtTestFactory jwt() {
        if (jwt == null) {
            jwt = new JwtTestFactory(secret, issuer);
        }
        return jwt;
    }

    @Test
    void user_creates_stripe_intent() throws Exception {
        UUID userId = UUID.randomUUID();
        when(stripeAdapter.createIntent(any(), eq("EUR"), eq(userId), any()))
                .thenReturn(ProviderIntent.ofStripe("pi_test_123", PaymentStatus.PENDING, "secret_test"));

        mockMvc.perform(post("/api/payments/intents")
                        .header("Authorization", jwt().bearer(userId, "u@example.com", List.of("USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateIntentRequest(
                                PaymentProvider.STRIPE, new BigDecimal("12.50"), "EUR", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.provider").value("STRIPE"))
                .andExpect(jsonPath("$.providerIntentId").value("pi_test_123"))
                .andExpect(jsonPath("$.clientSecret").value("secret_test"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void user_creates_paypal_intent() throws Exception {
        UUID userId = UUID.randomUUID();
        when(paypalAdapter.createIntent(any(), eq("EUR"), eq(userId), any()))
                .thenReturn(ProviderIntent.ofPaypal("ORDER-123", PaymentStatus.PENDING,
                        "https://paypal.com/approve"));

        mockMvc.perform(post("/api/payments/intents")
                        .header("Authorization", jwt().bearer(userId, "u@example.com", List.of("USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateIntentRequest(
                                PaymentProvider.PAYPAL, new BigDecimal("99.00"), "EUR", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.provider").value("PAYPAL"))
                .andExpect(jsonPath("$.providerIntentId").value("ORDER-123"))
                .andExpect(jsonPath("$.approvalUrl").value("https://paypal.com/approve"));
    }

    @Test
    void unauthenticated_request_returns_401() throws Exception {
        mockMvc.perform(post("/api/payments/intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateIntentRequest(
                                PaymentProvider.STRIPE, new BigDecimal("1.00"), "EUR", null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancel_requires_manager_role() throws Exception {
        UUID userId = UUID.randomUUID();
        when(stripeAdapter.createIntent(any(), any(), any(), any()))
                .thenReturn(ProviderIntent.ofStripe("pi_to_cancel", PaymentStatus.PENDING, null));

        var result = mockMvc.perform(post("/api/payments/intents")
                        .header("Authorization", jwt().bearer(userId, "u@example.com", List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateIntentRequest(
                                PaymentProvider.STRIPE, new BigDecimal("5.00"), "EUR", null))))
                .andExpect(status().isCreated())
                .andReturn();
        String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        when(stripeAdapter.cancel("pi_to_cancel"))
                .thenReturn(ProviderIntent.ofStripe("pi_to_cancel", PaymentStatus.CANCELLED, null));

        mockMvc.perform(post("/api/payments/intents/{id}/cancel", id)
                        .header("Authorization", jwt().bearer(userId, "u@example.com", List.of("USER"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/payments/intents/{id}/cancel", id)
                        .header("Authorization", jwt().bearer(userId, "u@example.com", List.of("MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void get_unknown_intent_returns_404() throws Exception {
        mockMvc.perform(get("/api/payments/intents/{id}", UUID.randomUUID())
                        .header("Authorization", jwt().bearer(UUID.randomUUID(), "u@example.com", List.of("USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void validation_rejects_short_currency() throws Exception {
        mockMvc.perform(post("/api/payments/intents")
                        .header("Authorization", jwt().bearer(UUID.randomUUID(), "u@example.com", List.of("USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"provider\":\"STRIPE\",\"amount\":\"1.00\",\"currency\":\"E\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"));
    }
}
