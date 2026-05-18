# Roadmap & Audit — Travel Management System

Document de référence consulté par Claude pour suivre l'état du projet et les décisions d'architecture. À tenir à jour à chaque phase.

## État au 2026-05-18

### ✅ En place
- Monorepo Maven Java 21 + Spring Boot 3.3.6 (modules `auth`, `admin`, `travel`, `payment`)
- Docker Compose : Postgres 16, Neo4j 5, services x4 (replicas=2), SonarQube + DB, Jenkins
- Réseaux Docker séparés (`travel-internal` interne, `travel-ci`)
- Healthchecks sur tous les conteneurs
- Ansible : `setup-host.yml` + `deploy.yml`
- Jenkinsfile (Checkout → Tests → Sonar → Build)
- Filtre `CorrelationIdFilter` sur chaque service (MDC + header `X-Correlation-Id`)
- `auth-service` : `SecurityConfiguration` minimaliste (HTTP Basic)
- `admin-service` : starter JPA + driver Postgres

### ❌ Manques principaux
1. Aucune logique métier (0 entité, 0 repo, 0 CRUD, 0 migration)
2. Auth incomplète (pas de JWT, pas de rôles, pas de `/login`)
3. Admin Dashboard frontend absent
4. Stripe/PayPal non intégrés (juste flags)
5. Neo4j non câblé côté `travel-service`
6. Tests = `contextLoads()` uniquement
7. Pas de TLS, pas de Vault, pas de RBAC
8. Logging non centralisé (juste MDC local)
9. `replicas: 2` ignoré par `docker compose up` (Swarm only) — il manque LB/gateway
10. Pas de manifestes Kubernetes, pas de tests E2E

## Plan d'attaque

| # | Chantier | Statut |
|---|---|---|
| 1 | **Schéma BDD + entités JPA + migrations Flyway + Neo4j** | ✅ Phase 1 terminée |
| 2 | Auth-service JWT + RBAC | ⏸ |
| 3 | CRUD admin-service (users, travels, payments) + tests | ⏸ |
| 4 | Admin Dashboard (responsive, Chrome/Firefox) | ⏸ |
| 5 | Gateway + reverse proxy TLS | ⏸ |
| 6 | Intégrations Stripe + PayPal | ⏸ |
| 7 | Vault + logging centralisé | ⏸ |
| 8 | Bonus K8s + E2E | ⏸ |

## Décisions d'architecture

### Répartition des données
- **PostgreSQL (admin-service)** : `users`, `roles`, `user_roles`, `payment_methods`, `bookings` — données transactionnelles avec intégrité référentielle stricte
- **Neo4j (travel-service)** : `Travel`, `Destination`, `Activity`, `Accommodation`, `Transportation` — relations entre offres de voyage
- **Référence croisée** : un `booking.travel_ref_id` (UUID) référence un `Travel` Neo4j en intégrité applicative (pas de FK cross-store)

### Cascading
- **Postgres** : `ON DELETE CASCADE` sur `user → payment_methods`, `user → bookings`. `payment_method → bookings` : `ON DELETE SET NULL` (préserve l'historique des réservations même si le moyen de paiement est supprimé). Implémenté à la fois dans Flyway (`V1__init_admin_schema.sql`) ET via `@OnDelete` Hibernate sur les entités pour aligner H2/test
- **Neo4j** : `DETACH DELETE` pour supprimer un `Travel` et toutes ses relations
- **Cross-store** : suppression d'un `Travel` Neo4j → `admin-service` met les bookings concernés en `status=CANCELLED` (event ou appel HTTP synchrone à définir en phase 3)

### Typage des ids Neo4j
- Décision phase 1 : `Travel/Destination/Activity/Accommodation/Transportation.id` sont des `String` au format UUID (généré par `UUIDStringGenerator`)
- `bookings.travel_ref_id` reste `UUID` côté Postgres. Conversion `UUID.toString()` / `UUID.fromString(...)` au point d'appel cross-service
- Raison : éviter d'écrire un converter Neo4j String↔UUID custom et garder le mapping SDN par défaut

### Migrations
- **Flyway** pour Postgres (`admin-service`)
- **Neo4j** : init via script Cypher au démarrage (ou Liquibase Neo4j en phase 2)

### Outillage tests
- Admin-service : `@DataJpaTest` avec H2 (mode PostgreSQL), Flyway désactivé en test, schéma régénéré par Hibernate via `ddl-auto: create-drop`
- Travel-service : `@DataNeo4jTest` avec Testcontainers Neo4j (requiert Docker pour le run)
- Lombok configuré comme annotation processor au niveau du parent pom (`<build><plugins>`, pas `<pluginManagement>` car Spring Boot parent override sinon)

## Conventions

- Branches : `feat/<scope>-<short-name>` → PR vers `develop`
- Commits signés (`git commit -S`)
- Tests : tous les nouveaux composants doivent avoir des tests unitaires
- Code : pas de logique dans les controllers, services isolés, DTO différents des entités JPA
