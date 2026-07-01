package com.travelplan.travel.search;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;

import com.travelplan.travel.api.dto.TravelHit;

/**
 * Elasticsearch-backed search. All operations are best-effort: if Elasticsearch
 * is unavailable, indexing failures are logged and swallowed so travel CRUD is
 * never blocked, and searches return an empty list.
 */
@Component
class ElasticsearchTravelSearch implements TravelSearch {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchTravelSearch.class);

    private final ElasticsearchOperations operations;

    ElasticsearchTravelSearch(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public void index(TravelDocument document) {
        try {
            operations.save(document);
        } catch (RuntimeException e) {
            log.warn("Elasticsearch index failed for travel {}: {}", document.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(String travelId) {
        try {
            operations.delete(travelId, TravelDocument.class);
        } catch (RuntimeException e) {
            log.warn("Elasticsearch delete failed for travel {}: {}", travelId, e.getMessage());
        }
    }

    @Override
    public List<TravelHit> search(String query) {
        Criteria criteria = new Criteria("title").matches(query)
                .or(new Criteria("description").matches(query))
                .or(new Criteria("destinations").matches(query))
                .or(new Criteria("activities").matches(query));
        return run(new CriteriaQuery(criteria));
    }

    @Override
    public List<TravelHit> autocomplete(String query) {
        return run(new CriteriaQuery(new Criteria("title").startsWith(query)));
    }

    private List<TravelHit> run(CriteriaQuery query) {
        try {
            return operations.search(query, TravelDocument.class).stream()
                    .map(SearchHit::getContent)
                    .map(TravelHit::from)
                    .toList();
        } catch (RuntimeException e) {
            log.warn("Elasticsearch search failed: {}", e.getMessage());
            return List.of();
        }
    }
}
