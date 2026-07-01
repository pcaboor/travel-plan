package com.travelplan.travel.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.travel.api.dto.AccommodationInput;
import com.travelplan.travel.api.dto.ActivityInput;
import com.travelplan.travel.api.dto.DestinationInput;
import com.travelplan.travel.api.dto.TravelCreateRequest;
import com.travelplan.travel.api.dto.TravelUpdateRequest;
import com.travelplan.travel.domain.AccommodationType;
import com.travelplan.travel.domain.TravelStatus;
import com.travelplan.travel.repository.TravelRepository;
import com.travelplan.travel.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TravelControllerIntegrationTest {

    @Container
    static final Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5-community")
            .withoutAuthentication()
            .withReuse(true);

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TravelRepository travelRepository;

    @Value("${travelplan.jwt.secret}")
    private String secret;

    @Value("${travelplan.jwt.issuer}")
    private String issuer;

    private JwtTestFactory jwt;
    private String adminAuth;
    private String viewerAuth;
    private String managerAId;
    private String managerAAuth;
    private String managerBAuth;

    @BeforeEach
    void setup() {
        jwt = new JwtTestFactory(secret, issuer);
        adminAuth = jwt.bearer("admin@example.com", List.of("ADMIN"));
        viewerAuth = jwt.bearer("viewer@example.com", List.of("VIEWER"));
        managerAId = UUID.randomUUID().toString();
        managerAAuth = jwt.bearer(managerAId, "managerA@example.com", List.of("MANAGER"));
        managerBAuth = jwt.bearer(UUID.randomUUID().toString(), "managerB@example.com", List.of("MANAGER"));
        travelRepository.deleteAll();
    }

    @Test
    void admin_creates_travel_with_subresources() throws Exception {
        TravelCreateRequest request = new TravelCreateRequest(
                "Tour de France", "10-day cycling trip",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 10),
                10, new BigDecimal("1200.00"), "EUR", TravelStatus.PUBLISHED,
                List.of(new DestinationInput("Paris", "France", 48.85, 2.35, 1),
                        new DestinationInput("Lyon", "France", 45.75, 4.85, 2)),
                List.of(new ActivityInput("Louvre", "Visit museum", "Culture", 180)),
                List.of(new AccommodationInput("Hotel Lux", AccommodationType.HOTEL, "Paris")),
                List.of());

        mockMvc.perform(post("/api/travels")
                        .header("Authorization", adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Tour de France"))
                .andExpect(jsonPath("$.destinations.length()").value(2))
                .andExpect(jsonPath("$.activities[0].name").value("Louvre"))
                .andExpect(jsonPath("$.accommodations[0].type").value("HOTEL"));
    }

    @Test
    void viewer_can_list_travels() throws Exception {
        mockMvc.perform(get("/api/travels").header("Authorization", viewerAuth))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticated_request_returns_401() throws Exception {
        mockMvc.perform(get("/api/travels")).andExpect(status().isUnauthorized());
    }

    @Test
    void viewer_cannot_create_travel() throws Exception {
        TravelCreateRequest request = new TravelCreateRequest(
                "X", null, null, null, null, null, null, null, null, null, null, null);
        mockMvc.perform(post("/api/travels")
                        .header("Authorization", viewerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewer_cannot_delete_travel() throws Exception {
        String id = createSampleTravel();
        mockMvc.perform(delete("/api/travels/" + id).header("Authorization", viewerAuth))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_then_get_reflects_changes() throws Exception {
        String id = createSampleTravel();

        mockMvc.perform(put("/api/travels/" + id)
                        .header("Authorization", adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TravelUpdateRequest(
                                "Updated title", null, null, null, null, null, null,
                                TravelStatus.ARCHIVED, null, null, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(get("/api/travels/" + id).header("Authorization", adminAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    void delete_then_get_returns_404() throws Exception {
        String id = createSampleTravel();

        mockMvc.perform(delete("/api/travels/" + id).header("Authorization", adminAuth))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/travels/" + id).header("Authorization", adminAuth))
                .andExpect(status().isNotFound());
    }

    @Test
    void health_endpoint_is_public() throws Exception {
        mockMvc.perform(get("/api/travels/health"))
                .andExpect(status().isOk());
    }

    @Test
    void manager_creating_a_travel_becomes_its_owner() throws Exception {
        mockMvc.perform(post("/api/travels")
                        .header("Authorization", managerAAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TravelCreateRequest(
                                "Owned", "desc", null, null, 5, new BigDecimal("100.00"), "EUR",
                                TravelStatus.DRAFT, null, null, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.managerId").value(managerAId));
    }

    @Test
    void admin_can_create_travel_via_role_hierarchy() throws Exception {
        // create requires hasRole('MANAGER'); ADMIN must inherit it through the hierarchy
        mockMvc.perform(post("/api/travels")
                        .header("Authorization", adminAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TravelCreateRequest(
                                "By admin", null, null, null, 1, null, "EUR",
                                TravelStatus.DRAFT, null, null, null, null))))
                .andExpect(status().isCreated());
    }

    @Test
    void manager_cannot_update_a_travel_they_do_not_own() throws Exception {
        String id = createTravelAs(managerAAuth);
        mockMvc.perform(put("/api/travels/" + id)
                        .header("Authorization", managerBAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TravelUpdateRequest(
                                "Hijacked", null, null, null, null, null, null, null, null, null, null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void manager_can_update_their_own_travel() throws Exception {
        String id = createTravelAs(managerAAuth);
        mockMvc.perform(put("/api/travels/" + id)
                        .header("Authorization", managerAAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TravelUpdateRequest(
                                "Mine", null, null, null, null, null, null, null, null, null, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Mine"));
    }

    @Test
    void manager_cannot_delete_a_travel_they_do_not_own() throws Exception {
        String id = createTravelAs(managerAAuth);
        mockMvc.perform(delete("/api/travels/" + id).header("Authorization", managerBAuth))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_delete_any_managers_travel() throws Exception {
        String id = createTravelAs(managerAAuth);
        mockMvc.perform(delete("/api/travels/" + id).header("Authorization", adminAuth))
                .andExpect(status().isNoContent());
    }

    private String createSampleTravel() throws Exception {
        return createTravelAs(adminAuth);
    }

    private String createTravelAs(String auth) throws Exception {
        TravelCreateRequest request = new TravelCreateRequest(
                "Sample", "desc", null, null, 5, new BigDecimal("100.00"), "EUR",
                TravelStatus.DRAFT, null, null, null, null);
        MvcResult result = mockMvc.perform(post("/api/travels")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}
