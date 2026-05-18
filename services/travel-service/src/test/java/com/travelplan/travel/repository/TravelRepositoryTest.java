package com.travelplan.travel.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.travelplan.travel.domain.Accommodation;
import com.travelplan.travel.domain.AccommodationType;
import com.travelplan.travel.domain.Activity;
import com.travelplan.travel.domain.Destination;
import com.travelplan.travel.domain.DestinationVisit;
import com.travelplan.travel.domain.Travel;
import com.travelplan.travel.domain.TravelStatus;

@Testcontainers
@DataNeo4jTest
class TravelRepositoryTest {

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
    private TravelRepository travelRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    @Test
    void saves_travel_with_destinations_and_activities() {
        Destination paris = destination("Paris", "France");
        Destination lyon = destination("Lyon", "France");

        Activity museum = new Activity();
        museum.setName("Louvre");
        museum.setCategory("Culture");

        Accommodation hotel = new Accommodation();
        hotel.setName("Hotel de Ville");
        hotel.setType(AccommodationType.HOTEL);

        Travel travel = new Travel();
        travel.setTitle("Tour de France");
        travel.setStartDate(LocalDate.of(2026, 7, 1));
        travel.setEndDate(LocalDate.of(2026, 7, 10));
        travel.setDurationDays(10);
        travel.setPrice(new BigDecimal("1200.00"));
        travel.setCurrency("EUR");
        travel.setStatus(TravelStatus.PUBLISHED);
        travel.getDestinations().add(new DestinationVisit(paris, 1));
        travel.getDestinations().add(new DestinationVisit(lyon, 2));
        travel.getActivities().add(museum);
        travel.getAccommodations().add(hotel);

        Travel saved = travelRepository.save(travel);

        Travel reloaded = travelRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getDestinations()).hasSize(2);
        assertThat(reloaded.getActivities()).extracting(Activity::getName).containsExactly("Louvre");
        assertThat(reloaded.getAccommodations()).extracting(Accommodation::getName).containsExactly("Hotel de Ville");
    }

    @Test
    void finds_travels_by_status() {
        Travel draft = new Travel();
        draft.setTitle("Draft trip");
        draft.setStatus(TravelStatus.DRAFT);
        travelRepository.save(draft);

        Travel published = new Travel();
        published.setTitle("Published trip");
        published.setStatus(TravelStatus.PUBLISHED);
        travelRepository.save(published);

        List<Travel> drafts = travelRepository.findByStatus(TravelStatus.DRAFT);
        assertThat(drafts).extracting(Travel::getTitle).contains("Draft trip");
    }

    private Destination destination(String name, String country) {
        Destination destination = new Destination();
        destination.setName(name);
        destination.setCountry(country);
        return destinationRepository.save(destination);
    }
}
