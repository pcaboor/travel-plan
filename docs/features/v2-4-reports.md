# V2-4 — Reports (signalements)

Épic **V2-4** de la [roadmap V2](../roadmap-v2.md). Un voyageur signale un
manager, un autre voyageur ou un voyage ; les admins traitent les signalements.

## Ce qui a été livré

- Déposer un signalement (`targetType` ∈ {MANAGER, TRAVELER, TRAVEL}, `targetId`,
  `reason`).
- L'admin liste les signalements (filtrables par statut) et fait évoluer leur
  statut (OPEN → REVIEWED / DISMISSED / ACTIONED).
- Le voyageur consulte ses propres signalements.

## Endpoints (`/api/reports`)

| Méthode & chemin | Rôle | Effet |
| --- | --- | --- |
| `POST /api/reports` (`{ targetType, targetId, reason }`) | `USER`+ | dépose un signalement (`OPEN`) |
| `GET /api/reports/me` | `USER`+ | mes signalements |
| `GET /api/reports?status=` | `ADMIN` | tous les signalements (filtre statut optionnel) |
| `POST /api/reports/{id}/status` (`{ status }`) | `ADMIN` | change le statut |

La liste et la gestion des statuts sont **réservées à l'ADMIN** (la hiérarchie
n'élève pas un MANAGER en ADMIN). Un voyageur ne voit que les siens.

### Codes de retour

| Situation | Code |
| --- | --- |
| Créé | 200 (`OPEN`) |
| `reason` vide / `targetType` manquant | 400 |
| Liste/maj par un non-admin | 403 |
| Signalement inconnu (maj statut) | 404 |

## Fichiers

| Fichier | Rôle |
| --- | --- |
| `db/migration/V4__create_reports.sql` | table `reports` (FK user, CHECK type/statut) |
| `domain/Report.java`, `ReportTargetType.java`, `ReportStatus.java` | entité + enums |
| `repository/ReportRepository.java` | finders (par reporter, par statut) |
| `service/ReportService.java` | création, listes, changement de statut |
| `api/ReportController.java` | endpoints `/api/reports` |
| `api/dto/ReportCreateRequest.java`, `ReportStatusUpdateRequest.java`, `ReportResponse.java` | DTOs |
| `frontend/vite.config.ts`, `frontend/nginx.conf` | route gateway `/api/reports` → admin-service |

## Tests

`ReportControllerIntegrationTest` (8) : dépôt, `reason` obligatoire (400),
liste interdite au voyageur (403), liste admin, liste personnelle, maj statut
par l'admin, maj interdite au voyageur (403), signalement inconnu (404).
admin-service : **69 tests** (61 → +8).

## Limites / suite

- Le **nombre de signalements** par manager/voyageur (affiché sur la page
  manager et les stats perso) est agrégé dans **V2-5 (stats)**.
- Pas de notification automatique à l'admin — la revue est manuelle (pull).
