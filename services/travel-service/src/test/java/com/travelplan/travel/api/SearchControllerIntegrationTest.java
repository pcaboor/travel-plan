package com.travelplan.travel.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import com.travelplan.travel.api.dto.TravelHit;
import com.travelplan.travel.search.TravelDocument;
import com.travelplan.travel.search.TravelSearch;
import com.travelplan.travel.support.JwtTestFactory;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerIntegrationTest {

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        TravelSearch fakeTravelSearch() {
            return new InMemoryTravelSearch();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TravelSearch travelSearch;

    @Value("${travelplan.jwt.secret}")
    private String secret;

    @Value("${travelplan.jwt.issuer}")
    private String issuer;

    private String userAuth;

    @BeforeEach
    void setup() {
        JwtTestFactory jwt = new JwtTestFactory(secret, issuer);
        ((InMemoryTravelSearch) travelSearch).clear();
        userAuth = jwt.bearer("traveler@example.com", List.of("USER"));

        TravelDocument doc = new TravelDocument();
        doc.setId(UUID.randomUUID().toString());
        doc.setTitle("Rome Adventure");
        doc.setDescription("A cultural trip to Rome");
        doc.setStatus("PUBLISHED");
        doc.setDestinations(List.of("Rome IT"));
        doc.setActivities(List.of("Colosseum Culture"));
        travelSearch.index(doc);
    }

    @Test
    void search_finds_a_travel_by_title() throws Exception {
        mockMvc.perform(get("/api/travels/search").param("q", "Rome").header("Authorization", userAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Rome Adventure"));
    }

    @Test
    void search_finds_a_travel_by_destination() throws Exception {
        mockMvc.perform(get("/api/travels/search").param("q", "Colosseum").header("Authorization", userAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_returns_empty_when_nothing_matches() throws Exception {
        mockMvc.perform(get("/api/travels/search").param("q", "Tokyo").header("Authorization", userAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void autocomplete_matches_a_title_prefix() throws Exception {
        mockMvc.perform(get("/api/travels/autocomplete").param("q", "Rom").header("Authorization", userAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_requires_authentication() throws Exception {
        mockMvc.perform(get("/api/travels/search").param("q", "Rome"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reindex_requires_admin() throws Exception {
        mockMvc.perform(post("/api/travels/reindex").header("Authorization", userAuth))
                .andExpect(status().isForbidden());
    }

    /** In-memory substring search, standing in for Elasticsearch in tests. */
    static class InMemoryTravelSearch implements TravelSearch {

        private final Map<String, TravelDocument> docs = new ConcurrentHashMap<>();

        void clear() {
            docs.clear();
        }

        @Override
        public void index(TravelDocument document) {
            docs.put(document.getId(), document);
        }

        @Override
        public void delete(String travelId) {
            docs.remove(travelId);
        }

        @Override
        public List<TravelHit> search(String query) {
            String needle = query.toLowerCase();
            return docs.values().stream().filter(d -> matches(d, needle)).map(TravelHit::from).toList();
        }

        @Override
        public List<TravelHit> autocomplete(String query) {
            String prefix = query.toLowerCase();
            return docs.values().stream()
                    .filter(d -> d.getTitle() != null && d.getTitle().toLowerCase().startsWith(prefix))
                    .map(TravelHit::from).toList();
        }

        private boolean matches(TravelDocument d, String needle) {
            return has(d.getTitle(), needle) || has(d.getDescription(), needle)
                    || inList(d.getDestinations(), needle) || inList(d.getActivities(), needle);
        }

        private boolean has(String s, String needle) {
            return s != null && s.toLowerCase().contains(needle);
        }

        private boolean inList(List<String> list, String needle) {
            return list != null && list.stream().anyMatch(s -> has(s, needle));
        }
    }
}
