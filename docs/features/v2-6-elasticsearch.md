# V2-6 — Recherche Elasticsearch (search + autocomplete)

Épic **V2-6** de la [roadmap V2](../roadmap-v2.md). Recherche plein-texte et
autocomplétion sur les voyages, servies par Elasticsearch depuis travel-service.

## Décision d'implémentation

- La recherche est abstraite derrière l'interface **`TravelSearch`**
  (index/delete/search/autocomplete). L'impl réelle
  **`ElasticsearchTravelSearch`** utilise `ElasticsearchOperations`.
- **Best-effort** : si Elasticsearch est indisponible, les échecs d'indexation
  sont loggés et avalés (le CRUD des voyages n'est jamais bloqué) et les
  recherches renvoient une liste vide.
- **Dual-write** : `TravelService` indexe/supprime le document ES à
  chaque create/update/delete.
- Endpoints sous `/api/travels/*` → **pas de nouvelle route gateway**.

## Infrastructure

`elasticsearch:8.13.4` ajouté au `docker-compose` (single-node,
`xpack.security.enabled=false`, heap 512 Mo, port `9200`, volume `es_data`).
travel-service reçoit `ELASTICSEARCH_URIS` et démarre après le conteneur ES.

## Endpoints (`/api/travels`)

| Méthode & chemin | Rôle | Effet |
| --- | --- | --- |
| `GET /api/travels/search?q=` | `USER`+ | recherche plein-texte (titre, description, destinations, activités) |
| `GET /api/travels/autocomplete?q=` | `USER`+ | complétion sur le préfixe du titre |
| `POST /api/travels/reindex` | `ADMIN` | réindexe tous les voyages Neo4j dans ES |

Retour : liste de `TravelHit` (`id`, `title`, `description`, `price`,
`currency`, `status`).

## Fichiers

| Fichier | Rôle |
| --- | --- |
| `pom.xml` | dépendance `spring-boot-starter-data-elasticsearch` |
| `application.yml` | `spring.elasticsearch.uris` |
| `search/TravelDocument.java` | projection ES aplatie d'un `Travel` |
| `search/TravelSearch.java` (+ `ElasticsearchTravelSearch.java`) | port + impl ES best-effort |
| `api/dto/TravelHit.java` | résultat de recherche |
| `service/TravelService.java` | indexation au CRUD + `search`/`autocomplete`/`reindexAll` |
| `api/TravelController.java` | endpoints search/autocomplete/reindex |
| `infra/docker/docker-compose.yml` | service `elasticsearch` + volume |

> Détail : `search`/`autocomplete` sont en `Propagation.NOT_SUPPORTED` pour ne
> pas ouvrir de transaction Neo4j (elles ne touchent qu'Elasticsearch).

## Tests

`SearchControllerIntegrationTest` (6) : recherche par titre / par contenu,
aucun résultat, autocomplétion par préfixe, 401 sans auth, reindex réservé à
l'ADMIN (403 sinon). Utilise un **faux `TravelSearch` en mémoire** (rapide,
sans conteneur ES). travel-service : **23 tests** verts (les tests existants
passent : l'indexation ES échoue en best-effort sans casser le CRUD).

## Limites / suite

- Le comportement **réel** des requêtes ES est validé sur le **stack live**
  (un test Testcontainers-ES est une extension possible, non retenue ici pour
  la vitesse de la CI).
- Autocomplétion via préfixe de titre ; un champ `search_as_you_type` /
  suggester dédié serait une amélioration.
