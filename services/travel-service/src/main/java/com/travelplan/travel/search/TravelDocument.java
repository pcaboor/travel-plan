package com.travelplan.travel.search;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.travelplan.travel.domain.Travel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Flattened, searchable projection of a {@link Travel}, indexed in Elasticsearch. */
@Getter
@Setter
@NoArgsConstructor
@Document(indexName = "travels")
public class TravelDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String currency;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Keyword)
    private String managerId;

    @Field(type = FieldType.Text)
    private List<String> destinations;

    @Field(type = FieldType.Text)
    private List<String> activities;

    public static TravelDocument from(Travel t) {
        TravelDocument d = new TravelDocument();
        d.id = t.getId();
        d.title = t.getTitle();
        d.description = t.getDescription();
        d.status = t.getStatus() != null ? t.getStatus().name() : null;
        d.currency = t.getCurrency();
        d.price = t.getPrice();
        d.managerId = t.getManagerId();
        d.destinations = t.getDestinations().stream()
                .map(v -> join(v.getDestination().getName(), v.getDestination().getCountry()))
                .toList();
        d.activities = t.getActivities().stream()
                .map(a -> join(a.getName(), a.getCategory()))
                .toList();
        return d;
    }

    private static String join(String a, String b) {
        if (a == null) {
            return b;
        }
        return b == null ? a : a + " " + b;
    }
}
