# V2-8 — UI voyageur & manager

Épic **V2-8** de la [roadmap V2](../roadmap-v2.md). Pages front qui branchent le
backend V2 (recherche, recommandations, souscription, stats), sur la stack
existante (React 18 + Vite + Tailwind + Radix + TanStack Query) — **aucune
nouvelle dépendance**.

## Ce qui a été livré

| Page | Route | Rôle | Contenu |
| --- | --- | --- | --- |
| **Discover** | `/discover` | tous | recherche + **autocomplete** (ES), section **recommandations** (Neo4j), souscription en un clic |
| **My stats** | `/my-stats` | tous | participations, annulations, feedbacks, signalements, moyens de paiement |
| **Dashboard** | `/dashboard` | MANAGER/ADMIN | revenus, voyages, voyageurs, note moyenne |
| **Leaderboard** | `/leaderboard` | ADMIN | managers classés par score |

- **Navigation par rôle** : la sidebar (`Layout`) filtre les entrées via
  `hasRole` ; la home redirige selon le rôle (admin → users, manager →
  dashboard, sinon → discover).
- **Discover** : autocomplétion à la frappe (dropdown), recherche à la validation,
  recommandations toujours affichées, bouton **Subscribe** par carte (crée un
  booking `PENDING`, message de suivi).

## Fichiers

| Fichier | Rôle |
| --- | --- |
| `features/discovery/{api.ts,types.ts}` | hooks search / autocomplete / recommendations / subscribe |
| `features/stats/{api.ts,types.ts}` | hooks my-stats / manager-dashboard / leaderboard |
| `pages/DiscoverPage.tsx` | page phare voyageur |
| `pages/MyStatsPage.tsx`, `ManagerDashboardPage.tsx`, `LeaderboardPage.tsx` | dashboards par rôle |
| `components/StatCard.tsx` | carte de statistique réutilisable |
| `components/Layout.tsx`, `App.tsx` | nav par rôle + routing |

## Validation

`npm run build` (`tsc --noEmit` strict + `vite build`) : **vert**, 0 erreur de
type.

## Limites / suite

- **Non couvert** (extensions UI possibles) : formulaire de **feedback**,
  formulaire de **signalement**, page **manager** de gestion des souscripteurs,
  écran de **paiement** provider (Stripe/PayPal) + confirmation, revue admin des
  reports. Les APIs correspondantes existent déjà (V2-2b/3/4).
- Le bouton *Subscribe* crée la souscription (`PENDING`) ; le parcours
  paiement → `confirm` est prêt côté backend, à brancher dans l'UI.
- Tests **E2E** (Playwright) : épic **V2-9**.
