package com.travelplan.travel.domain;

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
@Node("Accommodation")
public class Accommodation {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property("name")
    private String name;

    @Property("type")
    private AccommodationType type;

    @Property("address")
    private String address;
}
