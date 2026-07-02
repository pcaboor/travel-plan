# V2-9 — Tests & E2E

Épic **V2-9** de la [roadmap V2](../roadmap-v2.md). Ferme la boucle qualité :
tests unitaires front (Vitest) branchés sur le gate PR, et parcours **E2E
Playwright** contre le stack docker.

## Ce qui a été livré

### Front — Vitest (unitaire / composant)

| Fichier | Couvre |
| --- | --- |
| `src/lib/api.test.ts` | `apiFetch` : bearer + query string, body JSON, mapping `ApiError`, purge du token sur 401, `204 → undefined` |
| `src/lib/auth.test.ts` | `hasRole` (anonyme, match, non-match) |
| `src/pages/MyTripsPage.test.tsx` | états vide / `PENDING` (Confirm+Unsubscribe) / `CONFIRMED` (Feedback+Report) |
| `src/pages/ReportsPage.test.tsx` | état vide + ligne de signalement (statut + action Review) |

- Config : `vitest.config.ts` (jsdom, alias `@`, setup `src/test/setup.ts` qui
  charge `@testing-library/jest-dom`), helper `src/test/utils.tsx`
  (`renderWithClient` = `QueryClientProvider`).
- Les hooks API sont mockés (`vi.mock`) pour isoler le rendu des composants.
- Les fichiers de test sont **exclus du build de prod** (`tsconfig.json`) : `npm
  run build` (`tsc --noEmit` strict) reste propre.
- Scripts : `npm test` (watch), `npm run test:run` (CI), `npm run coverage`.

### E2E — Playwright (stack complet)

- `frontend/playwright.config.ts` : cible `E2E_BASE_URL` (défaut
  `https://localhost:5443`), `ignoreHTTPSErrors` (TLS auto-signé), retries en CI,
  trace/screenshot on failure.
- `frontend/e2e/journey.spec.ts` :
  1. **UI** — login (admin bootstrap) → atterrissage `/users` → **My trips**
     (état vide) → **Reports** (page admin) → **Discover** (recherche via la
     gateway → Elasticsearch) → **Logout** → retour `/login`.
  2. **API** — login puis vérification que `GET /api/subscriptions/me`,
     `/api/reports` et `/api/stats/me` répondent à travers la gateway.
- L'admin exerce à la fois les pages voyageur et la page admin grâce à la
  hiérarchie de rôles (V2-1) ; parcours **déterministe**, sans données seedées
  (les endpoints renvoient des listes vides sur un stack neuf).

## CI

| Workflow | Déclencheur | Rôle |
| --- | --- | --- |
| `.github/workflows/ci.yml` (job `frontend`) | PR + push `develop`/`main` | `npm ci` → `build` → **Vitest** (gate rapide) |
| `.github/workflows/e2e.yml` | PR vers `develop`/`main` + `workflow_dispatch` | démarre le stack app et lance **Playwright** |

- L'E2E ne démarre que les services applicatifs (`up admin-dashboard` tire
  auth/admin/travel + postgres/neo4j/ES/vault via `depends_on`) — **pas** de
  Grafana/Loki/Jenkins/SonarQube.
- `infra/docker/docker-compose.e2e.yml` réduit chaque service à **1 replica** et
  plafonne les heaps (JVM `-Xmx384m`, ES 512m, Neo4j 512m) pour tenir sur un
  runner GitHub (~7 Go).
- Rapport Playwright et logs du stack (sur échec) sont uploadés en artefacts ;
  `down -v` en `always`.

## Lancer en local

```bash
# unitaires
cd frontend && npm run test:run

# E2E : stack up puis Playwright
docker compose -f infra/docker/docker-compose.yml --env-file .env.example up -d --build
cd frontend && npm run e2e:install && npm run e2e
```

## Limites / suite

- Le parcours **paiement complet** (`subscribe → pay → confirm`) n'est pas
  couvert en E2E : les providers sont désactivés (cf. `v2-8-ui.md`). La logique
  associée reste testée côté back (unit/intégration).
- Jenkins : l'E2E est câblé sur GitHub Actions ; un stage Jenkins équivalent
  (`docker compose … up` + `npm run e2e`) est ajoutable si besoin, mais alourdit
  le build local — laissé optionnel.
