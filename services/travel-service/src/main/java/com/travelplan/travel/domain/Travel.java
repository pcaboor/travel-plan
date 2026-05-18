package com.travelplan.travel.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Node("Travel")
public class Travel {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property("title")
    private String title;

    @Property("description")
    private String description;

    @Property("startDate")
    private LocalDate startDate;

    @Property("endDate")
    private LocalDate endDate;

    @Property("durationDays")
    private Integer durationDays;

    @Property("price")
    private BigDecimal price;

    @Property("currency")
    private String currency;

    @Property("status")
    private TravelStatus status = TravelStatus.DRAFT;

    @Property("createdAt")
    private OffsetDateTime createdAt;

    @Property("updatedAt")
    private OffsetDateTime updatedAt;

    @Relationship(type = "VISITS", direction = Relationship.Direction.OUTGOING)
    private List<DestinationVisit> destinations = new ArrayList<>();

    @Relationship(type = "INCLUDES_ACTIVITY", direction = Relationship.Direction.OUTGOING)
    private List<Activity> activities = new ArrayList<>();

    @Relationship(type = "STAYS_AT", direction = Relationship.Direction.OUTGOING)
    private List<Accommodation> accommodations = new ArrayList<>();

    @Relationship(type = "USES_TRANSPORT", direction = Relationship.Direction.OUTGOING)
    private List<Transportation> transportations = new ArrayList<>();
}
