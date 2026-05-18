package com.travelplan.travel.repository;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.travelplan.travel.domain.Travel;
import com.travelplan.travel.domain.TravelStatus;

@Repository
public interface TravelRepository extends Neo4jRepository<Travel, String> {

    List<Travel> findByStatus(TravelStatus status);

    List<Travel> findByTitleContainingIgnoreCase(String title);
}
