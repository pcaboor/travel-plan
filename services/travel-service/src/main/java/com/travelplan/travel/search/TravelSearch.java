package com.travelplan.travel.search;

import java.util.List;

import com.travelplan.travel.api.dto.TravelHit;

/**
 * Search index for travels. Abstracted behind an interface so it can be faked in
 * tests and so the real (Elasticsearch) implementation stays best-effort.
 */
public interface TravelSearch {

    void index(TravelDocument document);

    void delete(String travelId);

    List<TravelHit> search(String query);

    List<TravelHit> autocomplete(String query);
}
