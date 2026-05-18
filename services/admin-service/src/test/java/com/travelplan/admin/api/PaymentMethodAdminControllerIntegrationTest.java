package com.travelplan.admin.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.admin.api.dto.PaymentMethodCreateRequest;
import com.travelplan.admin.api.dto.PaymentMethodUpdateRequest;
import com.travelplan.admin.domain.PaymentMethodStatus;
import com.travelplan.admin.domain.PaymentMethodType;
import com.travelplan.admin.domain.PaymentProvider;
import com.travelplan.admin.domain.User;
import com.travelplan.admin.domain.UserStatus;
import com.travelplan.admin.repository.PaymentMethodRepository;
import com.travelplan.admin.repository.UserRepository;
import com.travelplan.admin.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentMethodAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Value("${travelplan.jwt.secret}")
    private String secret;

    @Value("${travelplan.jwt.issuer}")
    private String issuer;

    private JwtTestFactory jwt;
    private String adminAuth;
    private UUID userId;

    @BeforeEach
    void setup() {
        jwt = new JwtTestFactory(secret, issuer);
        adminAuth = jwt.bearer("admin@example.com", List.of("ADMIN"));
        paymentMethodRepository.deleteAll();
        userRepository.deleteAll();
        User user = new User();
        user.setEmail("owner-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("hash");
        user.setStatus(UserStatus.ACTIVE);
        userId = userRepository.save(user).getId();
    }

    @Test
    void admin_creates_payment_method() throws Exception {
        mockMvc.perform(post("/api/admin/users/{userId}/payment-methods", userId)
                        .header("Authorization", adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentMethodCreateRequest(
                                PaymentProvider.STRIPE, PaymentMethodType.CARD,
                                "tok_test", "4242", LocalDate.of(2030, 12, 31), null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.provider").value("STRIPE"))
                .andExpect(jsonPath("$.lastFour").value("4242"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void viewer_cannot_create_payment_method() throws Exception {
        mockMvc.perform(post("/api/admin/users/{userId}/payment-methods", userId)
                        .header("Authorization", jwt.bearer("viewer@example.com", List.of("VIEWER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentMethodCreateRequest(
                                PaymentProvider.STRIPE, PaymentMethodType.CARD,
                                null, "1111", null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_returns_payment_methods_for_user() throws Exception {
        createMethod();

        mockMvc.perform(get("/api/admin/users/{userId}/payment-methods", userId)
                        .header("Authorization", adminAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }

    @Test
    void update_changes_status_and_expiry() throws Exception {
        UUID methodId = createMethod();

        mockMvc.perform(put("/api/admin/users/{userId}/payment-methods/{mid}", userId, methodId)
                        .header("Authorization", adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentMethodUpdateRequest(
                                null, LocalDate.of(2032, 6, 30), PaymentMethodStatus.EXPIRED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"))
                .andExpect(jsonPath("$.expiresAt").value("2032-06-30"));
    }

    @Test
    void delete_then_get_returns_404() throws Exception {
        UUID methodId = createMethod();

        mockMvc.perform(delete("/api/admin/users/{userId}/payment-methods/{mid}", userId, methodId)
                        .header("Authorization", adminAuth))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/users/{userId}/payment-methods/{mid}", userId, methodId)
                        .header("Authorization", adminAuth))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_for_unknown_user_returns_404() throws Exception {
        mockMvc.perform(post("/api/admin/users/{userId}/payment-methods", UUID.randomUUID())
                        .header("Authorization", adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentMethodCreateRequest(
                                PaymentProvider.PAYPAL, PaymentMethodType.WALLET,
                                null, null, null, null))))
                .andExpect(status().isNotFound());
    }

    private UUID createMethod() throws Exception {
        var result = mockMvc.perform(post("/api/admin/users/{userId}/payment-methods", userId)
                        .header("Authorization", adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentMethodCreateRequest(
                                PaymentProvider.STRIPE, PaymentMethodType.CARD,
                                "tok_test", "4242", LocalDate.of(2030, 12, 31), null))))
                .andExpect(status().isCreated())
                .andReturn();
        String id = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
        return UUID.fromString(id);
    }
}
