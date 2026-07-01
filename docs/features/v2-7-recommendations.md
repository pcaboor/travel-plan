# V2-7 — Recommandations personnalisées (Neo4j)

Épic **V2-7** de la [roadmap V2](../roadmap-v2.md). Suggestions de voyages
personnalisées à partir du comportement du voyageur, calculées dans le graphe.

## Modèle de graphe

Les voyageurs entrent dans le graphe Neo4j via des arêtes légères :

```
(:Traveler {id})-[:PARTICIPATED_IN]->(:Travel)
(:Traveler {id})-[:RATED {score}]->(:Travel)
```

## Algorithme de recommandation (≥ 3 champs)

Pour un voyageur, on part des voyages qu'il a **notés / auxquels il a
participé**, puis on classe les autres voyages `PUBLISHED` (non déjà participés)
par nombre de **caractéristiques partagées** — au moins 3 champs sont utilisés :

1. **destination** (même `name`),
2. **catégorie d'activité** (même `category`),
3. **manager** (même `managerId`).

Le score = nombre de dimensions partagées ; tri décroissant, top 10. Écrit en
Cypher (`EXISTS { … }` + prédicats de liste) via `Neo4jClient`.

## Endpoints

| Méthode & chemin | Service | Rôle | Effet |
| --- | --- | --- | --- |
| `POST /api/travels/{id}/participation` | travel-service | `USER`+ | arête `PARTICIPATED_IN` |
| `POST /api/travels/{id}/rating` (`{score}`) | travel-service | `USER`+ | arête `RATED` |
| `GET /api/travels/recommendations` | travel-service | `USER`+ | voyages recommandés (`id`, `title`, `status`, `score`) |

## Synchronisation (admin-service → travel-service)

Le graphe est peuplé **automatiquement**, best-effort (une panne ne bloque ni
la confirmation ni le feedback) :

- souscription **confirmée** (`V2-2b`) → `recordParticipation` ;
- **feedback** créé (`V2-3`) → `recordRating`.

`RecommendationSync` (interface + `RecommendationSyncClient` en `RestClient`,
JWT du voyageur propagé) appelle les endpoints ci-dessus.

## Fichiers

**travel-service**
| Fichier | Rôle |
| --- | --- |
| `recommendation/GraphRecommender.java` | Cypher record/reco via `Neo4jClient` |
| `recommendation/RecommendationHit.java` | résultat |
| `api/RecommendationController.java`, `api/dto/RatingRequest.java` | endpoints |

**admin-service**
| Fichier | Rôle |
| --- | --- |
| `service/RecommendationSync.java` (+ `RecommendationSyncClient.java`) | synchro best-effort |
| `service/SubscriptionService.java` | `confirm` → participation |
| `service/FeedbackService.java` (+ `FeedbackController.java`) | feedback → rating |

## Tests

- travel-service : `RecommendationControllerIntegrationTest` (5, **Testcontainers
  Neo4j**) — recommande un voyage partageant destination+activité+manager
  (score 3), exclut les voyages déjà participés, rien sans historique, 401 sans
  auth, note hors bornes → 400. **28 tests** verts.
- admin-service : synchro mockée (`@MockBean RecommendationSync`) ; **74 tests**
  verts, confirm/feedback inchangés côté contrat.

## Limites / suite

- Reco par **identité de valeur** (nom de destination, catégorie, manager) —
  les nœuds `Destination`/`Activity` ne sont pas dédupliqués entre voyages.
- Signal purement **contenu** ; un signal **collaboratif** (« des voyageurs qui
  ont aimé les mêmes voyages ont aussi participé à X ») serait une extension.
