package com.travelplan.travel.domain;

import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RelationshipProperties
public class DestinationVisit {

    @RelationshipId
    private Long id;

    private Integer order;

    @TargetNode
    private Destination destination;

    public DestinationVisit(Destination destination, Integer order) {
        this.destination = destination;
        this.order = order;
    }
}
