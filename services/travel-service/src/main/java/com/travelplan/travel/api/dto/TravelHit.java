package com.travelplan.travel.api.dto;

import java.math.BigDecimal;

import com.travelplan.travel.search.TravelDocument;

/** Lean search result for a travel. */
public record TravelHit(
        String id,
        String title,
        String description,
        BigDecimal price,
        String currency,
        String status) {

    public static TravelHit from(TravelDocument d) {
        return new TravelHit(d.getId(), d.getTitle(), d.getDescription(),
                d.getPrice(), d.getCurrency(), d.getStatus());
    }
}
