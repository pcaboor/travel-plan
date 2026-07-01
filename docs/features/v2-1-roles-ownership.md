# V2-1 — Hiérarchie de rôles & propriété des voyages

Épic **V2-1** de la [roadmap V2](../roadmap-v2.md). Fondation de la phase :
sans elle, les dashboards manager, le score de performance et les
recommandations n'ont rien sur quoi s'appuyer.

## Ce qui a été livré

1. Une **hiérarchie de rôles** (Admin ⊇ Manager ⊇ Traveler).
2. La **propriété d'un voyage** : chaque `Travel` connaît le manager qui l'a créé.
3. Le **contrôle d'ownership** : un manager ne gère que ses propres voyages.

## 1. Hiérarchie de rôles

`ADMIN > MANAGER > USER`. Un rôle « supérieur » hérite des droits des rôles
« inférieurs ».

- **Mécanisme** : un bean `RoleHierarchy` + un `MethodSecurityExpressionHandler`
  qui l'utilise, ajoutés dans les 3 resource servers
  (`travel`, `admin`, `payment` — `SecurityConfiguration`).
- **Effet** : `@PreAuthorize("hasRole('USER')")` passe désormais pour un manager
  ou un admin ; `hasRole('MANAGER')` passe pour un admin. Plus besoin de lister
  tous les rôles à chaque endpoint.
- **`VIEWER`** reste hors hiérarchie (rôle admin lecture-seule historique, non
  concerné par les nouvelles features).

```java
@Bean
static RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.fromHierarchy(
            "ROLE_ADMIN > ROLE_MANAGER\nROLE_MANAGER > ROLE_USER");
}
```

## 2. Propriété d'un voyage (`Travel.managerId`)

- Nouveau champ `managerId` (String/UUID) sur le nœud Neo4j `Travel`.
- Renseigné **à la création** depuis le `sub` du JWT (`jwt.getSubject()`), donc
  l'utilisateur authentifié qui crée le voyage en devient propriétaire.
- Exposé dans `TravelResponse` (le front pourra afficher/filtrer par manager).

## 3. Contrôle d'ownership

- `create` / `update` / `delete` exigent `hasRole('MANAGER')` (l'admin l'obtient
  via la hiérarchie).
- Sur `update` et `delete`, `TravelService.ensureOwnerOrAdmin(...)` vérifie que
  l'appelant est **le propriétaire** ou un **admin** ; sinon
  `AccessDeniedException` → **HTTP 403**.

### Matrice de permissions sur `/api/travels`

| Action | ADMIN | MANAGER (propriétaire) | MANAGER (autre) | VIEWER | USER |
| --- | :---: | :---: | :---: | :---: | :---: |
| `GET` (list/get) | ✅ | ✅ | ✅ | ✅ | ✅ |
| `POST` (create) | ✅ | ✅ | ✅ | ❌ | ❌ |
| `PUT` (update) | ✅ | ✅ | ❌ 403 | ❌ | ❌ |
| `DELETE` | ✅ | ✅ | ❌ 403 | ❌ | ❌ |

> Changement de comportement vs phase 1 : `DELETE` n'est plus réservé à l'admin —
> un manager peut supprimer **ses** voyages.

## Fichiers touchés

| Fichier | Changement |
| --- | --- |
| `travel/domain/Travel.java` | champ `managerId` |
| `travel/api/dto/TravelResponse.java` | expose `managerId` |
| `travel/service/TravelService.java` | `create(req, managerId)`, ownership sur `update`/`delete` |
| `travel/api/TravelController.java` | injecte le `Jwt`, passe `sub` + `isAdmin`, `@PreAuthorize` sur la hiérarchie |
| `travel` / `admin` / `payment` `config/SecurityConfiguration.java` | beans `RoleHierarchy` + `MethodSecurityExpressionHandler` |

## Tests

`TravelControllerIntegrationTest` (Testcontainers Neo4j) — 6 tests ajoutés :

- `manager_creating_a_travel_becomes_its_owner` → `managerId` == subject du token
- `admin_can_create_travel_via_role_hierarchy` → valide l'héritage (ADMIN via `hasRole('MANAGER')`)
- `manager_cannot_update_a_travel_they_do_not_own` → 403
- `manager_can_update_their_own_travel` → 200
- `manager_cannot_delete_a_travel_they_do_not_own` → 403
- `admin_can_delete_any_managers_travel` → 204

`JwtTestFactory` gagne une surcharge `bearer(subject, email, roles)` pour émettre
un token avec un `sub` fixe (nécessaire pour asserter l'ownership).

Suite complète : **BUILD SUCCESS** (auth 12, admin 45, travel 17, payment 12).

## Données existantes (backfill)

Les voyages créés avant cette feature ont `managerId = null` → seul un admin peut
les modifier/supprimer (aucun manager n'en est propriétaire). Pour les rattacher
à un manager, script Cypher à jouer dans le Neo4j Browser :

```cypher
MATCH (t:Travel) WHERE t.managerId IS NULL
SET t.managerId = $managerUserId;
```

## Limites / suite

- Un **Traveler (USER)** ne peut pas créer de voyage — c'est voulu (ce sont les
  managers qui créent l'offre). Les capacités traveler arrivent dans V2-2+.
- L'ownership est vérifié **côté serveur** (pas seulement dans l'UI).
