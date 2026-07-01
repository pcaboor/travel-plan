package com.travelplan.travel.recommendation;

import java.util.List;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

/**
 * Personalized recommendations from the Neo4j graph. Travelers enter the graph
 * through PARTICIPATED_IN / RATED edges; recommendations rank other published
 * travels by how many characteristics (destination, activity category, manager)
 * they share with what the traveler engaged with — at least 3 fields are used.
 */
@Service
public class GraphRecommender {

    private final Neo4jClient client;

    public GraphRecommender(Neo4jClient client) {
        this.client = client;
    }

    public void recordParticipation(String travelerId, String travelId) {
        client.query("""
                MERGE (t:Traveler {id: $travelerId})
                WITH t
                MATCH (tr:Travel {id: $travelId})
                MERGE (t)-[:PARTICIPATED_IN]->(tr)
                """)
                .bind(travelerId).to("travelerId")
                .bind(travelId).to("travelId")
                .run();
    }

    public void recordRating(String travelerId, String travelId, int score) {
        client.query("""
                MERGE (t:Traveler {id: $travelerId})
                WITH t
                MATCH (tr:Travel {id: $travelId})
                MERGE (t)-[r:RATED]->(tr)
                SET r.score = $score
                """)
                .bind(travelerId).to("travelerId")
                .bind(travelId).to("travelId")
                .bind(score).to("score")
                .run();
    }

    public List<RecommendationHit> recommend(String travelerId) {
        return client.query("""
                MATCH (me:Traveler {id: $travelerId})-[:RATED|PARTICIPATED_IN]->(liked:Travel)
                WITH me, collect(DISTINCT liked) AS likedTravels
                MATCH (rec:Travel)
                WHERE rec.status = 'PUBLISHED'
                  AND NOT rec IN likedTravels
                  AND NOT EXISTS { (me)-[:PARTICIPATED_IN]->(rec) }
                WITH rec,
                  (CASE WHEN EXISTS {
                      MATCH (rec)-[:VISITS]->(rd:Destination), (l:Travel)-[:VISITS]->(ld:Destination)
                      WHERE l IN likedTravels AND rd.name = ld.name
                   } THEN 1 ELSE 0 END) +
                  (CASE WHEN EXISTS {
                      MATCH (rec)-[:INCLUDES_ACTIVITY]->(a:Activity), (l:Travel)-[:INCLUDES_ACTIVITY]->(b:Activity)
                      WHERE l IN likedTravels AND a.category = b.category
                   } THEN 1 ELSE 0 END) +
                  (CASE WHEN any(l IN likedTravels
                      WHERE l.managerId IS NOT NULL AND l.managerId = rec.managerId) THEN 1 ELSE 0 END) AS score
                WHERE score > 0
                RETURN rec.id AS id, rec.title AS title, rec.status AS status, score AS score
                ORDER BY score DESC, rec.title
                LIMIT 10
                """)
                .bind(travelerId).to("travelerId")
                .fetchAs(RecommendationHit.class)
                .mappedBy((ts, record) -> new RecommendationHit(
                        record.get("id").asString(),
                        record.get("title").asString(null),
                        record.get("status").asString(null),
                        record.get("score").asInt()))
                .all().stream().toList();
    }
}
