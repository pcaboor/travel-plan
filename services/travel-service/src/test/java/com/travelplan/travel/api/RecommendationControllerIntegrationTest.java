package com.travelplan.travel.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.travel.api.dto.ActivityInput;
import com.travelplan.travel.api.dto.DestinationInput;
import com.travelplan.travel.api.dto.RatingRequest;
import com.travelplan.travel.api.dto.TravelCreateRequest;
import com.travelplan.travel.domain.TravelStatus;
import com.travelplan.travel.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RecommendationControllerIntegrationTest {

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
    private Neo4jClient neo4jClient;

    @Value("${travelplan.jwt.secret}")
    private String secret;

    @Value("${travelplan.jwt.issuer}")
    private String issuer;

    private String managerAuth;
    private String travelerAuth;

    @BeforeEach
    void setup() {
        JwtTestFactory jwt = new JwtTestFactory(secret, issuer);
        managerAuth = jwt.bearer("manager@example.com", List.of("MANAGER"));
        travelerAuth = jwt.bearer("traveler@example.com", List.of("USER"));
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    void recommends_a_travel_that_shares_destination_activity_and_manager() throws Exception {
        String liked = createTravel("Rome Culture A");
        String candidate = createTravel("Rome Culture B");
        rate(liked, 5);

        mockMvc.perform(get("/api/travels/recommendations").header("Authorization", travelerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(candidate))
                .andExpect(jsonPath("$[0].score").value(3));
    }

    @Test
    void no_recommendation_without_any_history() throws Exception {
        createTravel("Lonely Trip");

        mockMvc.perform(get("/api/travels/recommendations").header("Authorization", travelerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void an_already_participated_travel_is_not_recommended() throws Exception {
        String liked = createTravel("Rome Culture A");
        String candidate = createTravel("Rome Culture B");
        rate(liked, 5);
        mockMvc.perform(post("/api/travels/{id}/participation", candidate).header("Authorization", travelerAuth))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/travels/recommendations").header("Authorization", travelerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void recommendations_require_authentication() throws Exception {
        mockMvc.perform(get("/api/travels/recommendations")).andExpect(status().isUnauthorized());
    }

    @Test
    void rating_out_of_range_is_rejected() throws Exception {
        String travel = createTravel("Rome Culture A");
        mockMvc.perform(post("/api/travels/{id}/rating", travel).header("Authorization", travelerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":9}"))
                .andExpect(status().isBadRequest());
    }

    private String createTravel(String title) throws Exception {
        TravelCreateRequest request = new TravelCreateRequest(
                title, "desc", null, null, 5, new BigDecimal("100.00"), "EUR", TravelStatus.PUBLISHED,
                List.of(new DestinationInput("Rome", "IT", 41.9, 12.5, 1)),
                List.of(new ActivityInput("Museum", "visit", "Culture", 120)),
                List.of(), List.of());
        MvcResult result = mockMvc.perform(post("/api/travels").header("Authorization", managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private void rate(String travelId, int score) throws Exception {
        mockMvc.perform(post("/api/travels/{id}/rating", travelId).header("Authorization", travelerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RatingRequest(score))))
                .andExpect(status().isOk());
    }
}
