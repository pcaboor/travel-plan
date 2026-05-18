package com.travelplan.travel.domain;

import java.time.OffsetDateTime;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Node("Transportation")
public class Transportation {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property("type")
    private TransportationType type;

    @Property("provider")
    private String provider;

    @Property("departureLocation")
    private String departureLocation;

    @Property("arrivalLocation")
    private String arrivalLocation;

    @Property("departureTime")
    private OffsetDateTime departureTime;

    @Property("arrivalTime")
    private OffsetDateTime arrivalTime;
}
