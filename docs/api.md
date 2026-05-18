# API endpoints

Tous les endpoints (sauf `/health`) requièrent un JWT Bearer valide. RBAC appliqué par `@PreAuthorize` sur les méthodes du controller.

## auth-service (port 8081)

| Méthode | Chemin | Rôle requis | Description |
|---|---|---|---|
| `GET`  | `/api/auth/health` | public | Health check |
| `POST` | `/api/auth/login`  | public | Renvoie `{ accessToken, tokenType, expiresInSeconds, user }` |
| `GET`  | `/api/auth/me`     | authenticated | Profil de l'utilisateur connecté |

Payload login : `{ "email": "...", "password": "..." }`

## admin-service (port 8082)

### Users

| Méthode | Chemin | Rôles |
|---|---|---|
| `GET`    | `/api/admin/users` (paginé) | ADMIN/MANAGER/VIEWER |
| `GET`    | `/api/admin/users/:id` | ADMIN/MANAGER/VIEWER |
| `POST`   | `/api/admin/users` | ADMIN |
| `PUT`    | `/api/admin/users/:id` | ADMIN |
| `PUT`    | `/api/admin/users/:id/password` | ADMIN |
| `PUT`    | `/api/admin/users/:id/roles` | ADMIN |
| `DELETE` | `/api/admin/users/:id/roles/:role` | ADMIN |
| `DELETE` | `/api/admin/users/:id` | ADMIN |

### Payment methods

| Méthode | Chemin | Rôles |
|---|---|---|
| `GET`    | `/api/admin/users/:userId/payment-methods` | ADMIN/MANAGER/VIEWER |
| `GET`    | `/api/admin/users/:userId/payment-methods/:methodId` | ADMIN/MANAGER/VIEWER |
| `POST`   | `/api/admin/users/:userId/payment-methods` | ADMIN |
| `PUT`    | `/api/admin/users/:userId/payment-methods/:methodId` | ADMIN |
| `DELETE` | `/api/admin/users/:userId/payment-methods/:methodId` | ADMIN |

### Bookings

| Méthode | Chemin | Rôles |
|---|---|---|
| `GET`  | `/api/admin/bookings` (paginé) | ADMIN/MANAGER/VIEWER |
| `GET`  | `/api/admin/bookings/:id` | ADMIN/MANAGER/VIEWER |
| `GET`  | `/api/admin/users/:userId/bookings` | ADMIN/MANAGER/VIEWER |
| `POST` | `/api/admin/bookings/cancel-by-travel/:travelRefId` | ADMIN/MANAGER |

## travel-service (port 8083)

| Méthode | Chemin | Rôles |
|---|---|---|
| `GET`    | `/api/travels/health` | public |
| `GET`    | `/api/travels` (paginé) | ADMIN/MANAGER/VIEWER/USER |
| `GET`    | `/api/travels/:id` | ADMIN/MANAGER/VIEWER/USER |
| `POST`   | `/api/travels` | ADMIN/MANAGER |
| `PUT`    | `/api/travels/:id` | ADMIN/MANAGER |
| `DELETE` | `/api/travels/:id` | ADMIN |

Le `POST`/`PUT` accepte un payload imbriqué pour `destinations`, `activities`, `accommodations`, `transportations`. Les sous-collections sont remplacées au `PUT` si elles sont non-null dans le payload.

## Codes d'erreur communs

| Code | `error` | Cas |
|---|---|---|
| 400 | `validation_error` | Payload invalide (champ trop court, type incorrect…) |
| 401 | — | Token absent ou invalide |
| 403 | `forbidden` | Rôle insuffisant |
| 404 | `not_found` | Ressource inexistante |
| 409 | `conflict` | Conflit (ex: email déjà utilisé) |
