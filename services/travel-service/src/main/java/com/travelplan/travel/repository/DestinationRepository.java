package com.travelplan.travel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.travelplan.travel.domain.Destination;

@Repository
public interface DestinationRepository extends Neo4jRepository<Destination, String> {

    Optional<Destination> findByNameIgnoreCase(String name);

    List<Destination> findByCountryIgnoreCase(String country);
}
