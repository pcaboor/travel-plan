# V2-5 — Statistiques & score de performance manager

Épic **V2-5** de la [roadmap V2](../roadmap-v2.md). Dashboards par rôle et
classement des managers, à partir des données consolidées (V2-1→4).

## Décision d'implémentation : dénormalisation de `manager_id`

Pour que les agrégations par manager soient **locales** à admin-service (pas
d'appel cross-service au moment des stats), l'id du manager est **dénormalisé** :

- sur `bookings` (capturé au subscribe depuis le `Travel` de travel-service) ;
- sur `feedback` (repris du booking « participé » à la création du feedback).

Migration `V5__denormalize_manager_id.sql` (+ index). Les données créées avant
la migration ont `manager_id = null` et ne comptent pas dans les stats manager.

## Endpoints (`/api/stats`)

| Méthode & chemin | Rôle | Contenu |
| --- | --- | --- |
| `GET /api/stats/me` | `USER`+ | participations, annulations, #signalements déposés, #feedbacks, moyens de paiement |
| `GET /api/stats/manager/me` | `MANAGER`+ | revenus, #voyages, #voyageurs, note moyenne (ses voyages) |
| `GET /api/stats/managers/leaderboard` | `ADMIN` | managers classés par score décroissant |

## Score de performance manager

Calculé dans admin-service, métriques normalisées sur l'ensemble des managers :

```
score = 0.5·note_moyenne_norm + 0.3·revenus_norm + 0.2·voyages_norm − 0.2·signalements_norm
```

- `note_moyenne_norm` = note/5 (absolue) ; `revenus`/`voyages`/`signalements`
  normalisés min-max sur le panel.
- **Revenus** = somme des `bookings.amount` `CONFIRMED`/`COMPLETED` du manager
  (décision roadmap : dérivé des bookings, pas de payment-service).
- **Signalements** = reports `targetType=MANAGER` visant le manager (malus).

## Fichiers

| Fichier | Rôle |
| --- | --- |
| `db/migration/V5__denormalize_manager_id.sql` | colonnes `manager_id` |
| `domain/Booking.java`, `Feedback.java` | champ `managerId` |
| `service/TravelLookup.java`, `TravelClient.java` | `TravelSnapshot` porte `managerId` |
| `service/SubscriptionService.java`, `FeedbackService.java` | renseignent `managerId` |
| `repository/*Repository.java` | requêtes d'agrégation (sommes, moyennes, group by manager) |
| `service/StatsService.java` | stats voyageur/manager + calcul du leaderboard |
| `api/StatsController.java` + DTOs (`TravelerStats`, `ManagerDashboard`, `ManagerScore`) | endpoints |
| `frontend/vite.config.ts`, `frontend/nginx.conf` | route gateway `/api/stats` |

## Tests

`StatsControllerIntegrationTest` (5) : stats voyageur, dashboard manager
(revenus/voyages/voyageurs/note), leaderboard (le meilleur manager en tête),
403 pour un voyageur sur dashboard manager et leaderboard. admin-service :
**74 tests** (69 → +5).

## Limites / suite

- **`#voyages`** compté = voyages distincts **ayant des souscriptions** (proxy
  local), pas le total de voyages créés par le manager (fait travel-service).
- Non inclus dans cet épic (extensions possibles) : revenus **par mois**, top
  travels, liste des **souscripteurs** d'un voyage, historique global admin.
