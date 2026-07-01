# Roadmap V2 — Engagement voyageurs, recommandations & transactions

Deuxième période du projet. Étend le système existant (phases 1→7 terminées, cf.
[roadmap.md](roadmap.md)) avec des fonctionnalités métier par rôle : **Admin**,
**Travel Manager**, **Traveler**. Ce document cadre les **décisions
d'architecture** et le **découpage en épics** — à valider avant tout code.

## 1. Objectif

Rendre le produit « vivant » côté voyageur : recherche performante
(Elasticsearch), recommandations personnalisées (Neo4j), souscription +
paiement, feedback, signalements, et tableaux de bord/statistiques pour Managers
et Admins.

## 2. Modèle de rôles (décision structurante)

Le cahier des charges parle d'**Admin / Travel Manager / Traveler**. On mappe sur
les rôles existants, sans casser l'existant :

| Rôle métier | Rôle technique | Hérite de |
| --- | --- | --- |
| Admin | `ADMIN` | Manager + Traveler |
| Travel Manager | `MANAGER` | Traveler |
| Traveler | `USER` | — |
| *(interne, optionnel)* | `VIEWER` | lecture admin seule (conservé, non exposé aux nouvelles features) |

**Hiérarchie** — décision : implémenter l'héritage à la **construction des
authorities** dans chaque resource server. Le claim JWT `roles` est *étendu* :
`ADMIN → {ADMIN, MANAGER, USER}`, `MANAGER → {MANAGER, USER}`, `USER → {USER}`.
Ainsi un endpoint `@PreAuthorize("hasRole('USER')")` (traveler) est
automatiquement accessible aux managers et admins → « Admin/Manager ont accès à
tout ce que fait un Traveler » sans dupliquer les annotations.

> Alternative écartée : rôles Spring `RoleHierarchy` bean. L'expansion à la
> source (mapper d'authorities) est plus explicite et déjà proche du code actuel.

## 3. Décisions d'architecture par brique

### 3.1 Propriété d'un Travel

- Ajouter `managerId` (`String`/UUID) au nœud `Travel` (travel-service).
- Renseigné depuis le `sub` du JWT à la création. Un manager ne peut
  modifier/supprimer que **ses** travels ; l'admin, tous.
- Backfill Neo4j : script Cypher pour affecter les travels existants (à l'admin
  bootstrap par défaut).

### 3.2 Souscriptions (reuse de `bookings`)

- Un `booking` **est** une souscription (traveler ↔ travel). On ne crée pas de
  nouvelle entité.
- Nouveaux endpoints **côté traveler** :
  - `POST /api/travels/{id}/subscribe` → crée un booking `PENDING` + lance le
    paiement ; passage `CONFIRMED` au succès du paiement.
  - `POST /api/travels/{id}/unsubscribe` → autorisé **uniquement si ≥ 3 jours
    avant `startDate`** (sinon 409).
- **Cutoff J-3 — décision** : dénormaliser `travel_start_date` sur le booking au
  moment du subscribe. Évite un appel cross-service à chaque unsubscribe ; le
  contrôle de date reste local à admin-service.
- Garde-fous : pas de double souscription, travel existant/`PUBLISHED`.

### 3.3 Feedback

- Vérité transactionnelle en **Postgres (admin-service)** : table `feedback`
  (`id`, `travel_ref_id`, `author_user_id`, `rating` 1–5, `comment`,
  `created_at`, unique `(author_user_id, travel_ref_id)`).
- **Règle** : on ne peut noter qu'un travel **participé** (booking `COMPLETED`).
- Alimente : score manager, satisfaction (admin), et le **graphe de reco**
  (cf. 3.6).

### 3.4 Signalements (reports)

- **Postgres (admin-service)** : table `reports` (`id`, `reporter_user_id`,
  `target_type` ∈ {MANAGER, TRAVELER, TRAVEL}, `target_id`, `reason`, `status`
  ∈ {OPEN, REVIEWED, DISMISSED, ACTIONED}, `created_at`).
- Traveler : `POST /api/reports`. Admin : liste + changement de statut.

### 3.5 Recherche Elasticsearch (nouvelle infra)

- Nouveau service `elasticsearch` (single-node) dans `docker-compose`, réseau
  dédié.
- **travel-service** indexe les travels (dual-write : create/update/delete →
  ES). Document ES « aplati » : `title`, `description`, noms/pays des
  `destinations`, catégories d'`activities`, `price`, dates, `status`,
  `managerId`.
- Endpoints : `GET /api/travels/search?q=` (full-text multi-champs) et
  `GET /api/travels/autocomplete?q=` (champ `search_as_you_type` ou
  edge-ngram).
- Backfill initial : réindexation des travels Neo4j existants au démarrage
  (ApplicationRunner ou endpoint `/reindex`).
- **Décision technique** : client **Spring Data Elasticsearch** (cohérent avec
  le style Spring Data déjà utilisé pour JPA/Neo4j).

### 3.6 Recommandations Neo4j (personnalisées, ≥ 3 champs)

Le point le plus structurant. Aujourd'hui les travelers ne sont **qu'en
Postgres** ; la reco graphe exige de les faire entrer dans Neo4j.

- **travel-service** maintient un nœud léger `Traveler {id}` + relations :
  - `(:Traveler)-[:PARTICIPATED_IN {at}]->(:Travel)` (souscription confirmée)
  - `(:Traveler)-[:RATED {score, at}]->(:Travel)` (feedback)
- Ces arêtes sont écrites par **sync cross-store** (même patron que
  `cancel-by-travel` existant) : quand un booking passe `CONFIRMED` ou qu'un
  feedback est créé, admin-service notifie travel-service.
- **Reco (≥ 3 champs)** : suggérer les travels partageant au moins 3
  caractéristiques avec ce que le traveler a aimé — parmi {destination,
  catégorie d'activité, tranche de prix, durée, manager} — plus un signal
  collaboratif (« des voyageurs ayant bien noté les mêmes travels ont participé
  à X »). Endpoint `GET /api/travels/recommendations`.
- **Trade-off assumé** : double écriture Postgres↔Neo4j → cohérence à terme
  (eventual consistency), acceptable ici et aligné sur l'existant.

### 3.7 Score de performance manager

- Calculé dans **admin-service** (a le feedback, l'income via bookings, les
  reports).
- **Formule (pondérations ajustables)** :
  `score = 0.5·note_moyenne_norm + 0.3·income_norm + 0.2·nb_travels_norm − pénalité_reports`
- Exposé via `GET /api/admin/managers/leaderboard` (trié desc).

### 3.8 Statistiques / dashboards

| Rôle | Endpoints (admin-service sauf mention) |
| --- | --- |
| Admin | revenus/N derniers mois, #voyages organisés, top travels, **leaderboard managers**, historique + feedbacks, liste des reports |
| Manager | dashboard perso (income, #trips, #travelers), feedbacks de ses travels, **liste des souscripteurs** (voir profil / désinscrire) |
| Traveler | stats perso (participations, #reports, annulations, moyens de paiement préférés) |

- **Décision « income »** : dérivé de `bookings.amount` (bookings `CONFIRMED`/
  `COMPLETED`) — évite d'agréger dans payment-service.

## 4. Découpage en épics

| # | Épic | Contenu | Dépend de | Statut |
| --- | --- | --- | --- | --- |
| V2-1 | **Socle rôles & ownership** | hiérarchie de rôles, `Travel.managerId`, ownership enforcement | — | ✅ |
| V2-2 | **Souscriptions + paiement** | subscribe/unsubscribe, cutoff J-3, lien payment-service | V2-1 | ✅ (a+b) |
| V2-3 | **Feedback** | entité + règle « participé », endpoints | V2-2 | ✅ |
| V2-4 | **Reports** | entité + endpoints traveler/admin | V2-1 | ✅ |
| V2-5 | **Stats & score manager** | agrégations, leaderboard, dashboards par rôle | V2-2, V2-3, V2-4 | 🚧 |
| V2-6 | **Elasticsearch** | infra + indexation + search/autocomplete | V2-1 | ⏳ |
| V2-7 | **Reco Neo4j** | Traveler dans le graphe + Cypher de suggestion | V2-2, V2-3 | ⏳ |
| V2-8 | **UI traveler & manager** | pages responsive, branchées sur V2-1→7 | V2-1→7 | ⏳ |
| V2-9 | **Tests & E2E** | unit/intégration par feature + Playwright + CI | tous | ⏳ |

## 5. Séquencement conseillé

Fondations d'abord (V2-1 → V2-2 → V2-3/V2-4), puis les agrégations (V2-5), puis
les briques « vitrine » en parallèle (V2-6 ES, V2-7 reco), enfin l'UI (V2-8) et
la qualité en continu (V2-9, à faire **au fil** de chaque épic, pas à la fin).

## 6. Stratégie de tests

- **Unitaire + intégration** pour chaque nouvelle feature (patron existant :
  `@DataJpaTest` H2, `@DataNeo4jTest` Testcontainers, MockMvc).
- **Elasticsearch** : Testcontainers ES pour les tests d'indexation/recherche.
- **E2E** : Playwright contre le stack docker (parcours search → subscribe →
  pay → feedback), branché sur GitHub Actions (et Jenkins).
- **Front** : Vitest pour les composants/hooks.

## 7. Sécurité & conformité

- Chaque nouvel endpoint sous `@PreAuthorize` selon la hiérarchie (§2).
- Un traveler n'accède qu'à **ses** données (souscriptions, stats, paiements) —
  contrôle d'ownership serveur, pas seulement UI.
- Paiements : on garde la vérification de signature webhook existante ; pas de
  donnée carte stockée (tokens provider uniquement).
- TLS déjà en place (nginx). Données perso (feedback/reports) : accès restreint
  par rôle + ownership.

## 8. Décisions arrêtées (validées 2026-07-01)

1. **Feedback/Reports** → dans `admin-service` (pas de nouveau service). ✅
2. **Reco Neo4j** → dual-write Postgres↔Neo4j via endpoint « pull » cross-store,
   même patron que `cancel-by-travel` (pas de bus d'événements). ✅
3. **Elasticsearch** → version 8.x single-node. ✅
4. **Score manager** → `0.5·note_moyenne_norm + 0.3·income_norm +
   0.2·nb_travels_norm − pénalité_reports`. ✅
5. **Income** → dérivé de `bookings.amount` (bookings `CONFIRMED`/`COMPLETED`),
   pas d'agrégation depuis payment-service. ✅
