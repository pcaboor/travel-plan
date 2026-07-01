# V2-3 — Feedback (notes sur les voyages)

Épic **V2-3** de la [roadmap V2](../roadmap-v2.md). Un voyageur note un voyage
auquel il a **participé**. Alimentera le score manager ([V2-5]) et les
recommandations Neo4j ([V2-7]).

## Ce qui a été livré

- Laisser un feedback (`rating` 1–5 + commentaire) sur un voyage **participé**.
- Un voyageur ne peut noter un voyage **qu'une fois**.
- Lister les feedbacks d'un voyage ; lister ses propres feedbacks.

## Règles

- **Participation** : l'auteur doit avoir un booking `CONFIRMED` ou `COMPLETED`
  pour ce voyage (vérifié localement via `bookings`, pas d'appel cross-service).
- **Unicité** : contrainte `(author_user_id, travel_ref_id)` unique (DB + entité).
- **Note** : entier `1..5` (validation `@Min/@Max` + `CHECK` SQL).

## Endpoints (`/api/feedback`, rôle `USER`+)

| Méthode & chemin | Effet |
| --- | --- |
| `POST /api/feedback` (`{ travelId, rating, comment }`) | crée un feedback |
| `GET /api/feedback/travels/{travelId}` | feedbacks d'un voyage |
| `GET /api/feedback/me` | mes feedbacks |

### Codes de retour de `POST`

| Situation | Code |
| --- | --- |
| Créé | 200 |
| Voyage non participé | 409 |
| Déjà noté | 409 |
| `rating` hors 1..5 | 400 |

## Fichiers

| Fichier | Rôle |
| --- | --- |
| `db/migration/V3__create_feedback.sql` | table `feedback` (FK user, CHECK rating, unique auteur+voyage) |
| `domain/Feedback.java` | entité (`@UniqueConstraint`, `@Min/@Max`) |
| `repository/FeedbackRepository.java` | finders + `existsByAuthorUserIdAndTravelRefId` |
| `service/FeedbackService.java` | participation + unicité + création/listes |
| `api/FeedbackController.java` | endpoints `/api/feedback` |
| `api/dto/FeedbackCreateRequest.java`, `FeedbackResponse.java` | DTOs |
| `frontend/vite.config.ts`, `frontend/nginx.conf` | route gateway `/api/feedback` → admin-service |

## Tests

`FeedbackControllerIntegrationTest` (6) : participant peut noter, non-participant
→ 409, double note → 409, note hors bornes → 400, liste par voyage, liste
personnelle. admin-service : **61 tests** (55 → +6).

## Limites / suite

- **Sync Neo4j (V2-7)** : à la création d'un feedback, écrire une arête
  `(:Traveler)-[:RATED {score}]->(:Travel)` dans le graphe pour les
  recommandations — déféré.
- **Score manager (V2-5)** : l'agrégation des notes par manager (via
  `Travel.managerId`) se fait dans l'épic stats.
- **« Participé »** = booking `CONFIRMED`/`COMPLETED`. Le passage automatique
  `CONFIRMED → COMPLETED` (après la date de fin) n'est pas encore implémenté.
