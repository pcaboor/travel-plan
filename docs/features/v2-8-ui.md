# V2-8 — UI voyageur & manager

Épic **V2-8** de la [roadmap V2](../roadmap-v2.md). Pages front qui branchent le
backend V2 (recherche, recommandations, souscription, stats), sur la stack
existante (React 18 + Vite + Tailwind + Radix + TanStack Query) — **aucune
nouvelle dépendance**.

## Ce qui a été livré

| Page | Route | Rôle | Contenu |
| --- | --- | --- | --- |
| **Discover** | `/discover` | tous | recherche + **autocomplete** (ES), section **recommandations** (Neo4j), souscription en un clic |
| **My trips** | `/my-trips` | tous | mes souscriptions ; actions **Confirm** (paiement), **Unsubscribe**, **Feedback**, **Report** |
| **My stats** | `/my-stats` | tous | participations, annulations, feedbacks, signalements, moyens de paiement |
| **Dashboard** | `/dashboard` | MANAGER/ADMIN | revenus, voyages, voyageurs, note moyenne |
| **Leaderboard** | `/leaderboard` | ADMIN | managers classés par score |
| **Reports** | `/reports` | ADMIN | revue des signalements + filtre par statut + changement de statut |

- **Navigation par rôle** : la sidebar (`Layout`) filtre les entrées via
  `hasRole` ; la home redirige selon le rôle (admin → users, manager →
  dashboard, sinon → discover).
- **Discover** : autocomplétion à la frappe (dropdown), recherche à la validation,
  recommandations toujours affichées, bouton **Subscribe** par carte (crée un
  booking `PENDING`, message de suivi).
- **My trips** : liste des souscriptions du voyageur (`GET /api/subscriptions/me`).
  Selon le statut : **Confirm** (`PENDING` → paiement puis `CONFIRMED`),
  **Unsubscribe** (souscriptions actives, règle cutoff J-3 côté backend),
  **Feedback** (voyages `CONFIRMED`/`COMPLETED`) et **Report** (dialog pré-rempli
  sur le voyage). Messages d'erreur backend remontés en clair (ex. paiement non
  finalisé, fenêtre d'annulation fermée).
- **Feedback / Report** : dialogs `react-hook-form` + `zod` réutilisables
  (note 1–5 + commentaire ; type de cible + id + motif).
- **Reports (admin)** : tableau filtrable par statut, dialog de revue pour passer
  un signalement à `OPEN`/`REVIEWED`/`ACTIONED`/`DISMISSED`.

## Fichiers

| Fichier | Rôle |
| --- | --- |
| `features/discovery/{api.ts,types.ts}` | hooks search / autocomplete / recommendations / subscribe |
| `features/subscriptions/{api.ts,types.ts}` | hooks my-subscriptions / unsubscribe / confirm |
| `features/feedback/{api.ts,types.ts}` + `FeedbackDialog.tsx` | hooks feedback + dialog note/commentaire |
| `features/reports/{api.ts,types.ts}` + `ReportDialog.tsx`, `ReportStatusDialog.tsx` | hooks reports + dialogs création/revue |
| `features/stats/{api.ts,types.ts}` | hooks my-stats / manager-dashboard / leaderboard |
| `pages/DiscoverPage.tsx` | page phare voyageur |
| `pages/MyTripsPage.tsx` | souscriptions du voyageur + actions |
| `pages/ReportsPage.tsx` | revue admin des signalements |
| `pages/MyStatsPage.tsx`, `ManagerDashboardPage.tsx`, `LeaderboardPage.tsx` | dashboards par rôle |
| `components/StatCard.tsx` | carte de statistique réutilisable |
| `components/ui/textarea.tsx` | champ multi-lignes (feedback / motif de report) |
| `components/Layout.tsx`, `App.tsx` | nav par rôle + routing |

Côté backend, ajout de `GET /api/subscriptions/me` (liste des souscriptions du
voyageur connecté, `hasRole('USER')`) pour alimenter **My trips**.

## Validation

`npm run build` (`tsc --noEmit` strict + `vite build`) : **vert**, 0 erreur de
type.

## Limites / suite

- **Non couvert** : écran de **paiement** provider (Stripe/PayPal) — les
  providers sont désactivés par défaut, donc pas d'UI de saisie carte. Le bouton
  **Confirm** appelle `POST /api/subscriptions/{id}/confirm` : tant que le
  paiement n'a pas réussi côté payment-service, l'appel renvoie « paiement non
  finalisé » (message remonté à l'utilisateur). Le parcours complet nécessite
  d'activer un provider.
- Page **manager** de gestion fine des souscripteurs : non prioritaire (le
  dashboard agrégé couvre le besoin d'audit).
- Tests **E2E** (Playwright) : épic **V2-9**.
