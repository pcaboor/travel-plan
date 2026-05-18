package com.travelplan.travel.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.travelplan.travel.domain.Transportation;

@Repository
public interface TransportationRepository extends Neo4jRepository<Transportation, String> {
}
