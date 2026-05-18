package com.travelplan.travel.repository;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.travelplan.travel.domain.Activity;

@Repository
public interface ActivityRepository extends Neo4jRepository<Activity, String> {

    List<Activity> findByCategoryIgnoreCase(String category);
}
