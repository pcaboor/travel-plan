# Travel Management System

Travel Management System is a microservices-based project for managing users, travel offers, destinations, and payment providers.

This repository starts with the infrastructure and backend foundations required before building the complete admin dashboard.

## Target Architecture

- Java 21 and Spring Boot 3 microservices.
- PostgreSQL for relational administration data.
- Neo4j for graph-oriented travel and destination relationships.
- HashiCorp Vault (dev mode) for service secrets; each Spring service is built
  with the Maven `vault` profile and reads `secret/data/travelplan/<service>`
  at boot.
- Loki + Grafana + promtail for centralised JSON logs (per-request
  `correlationId` ships through MDC for cross-service tracing).
- Docker Compose for local provisioning and reproducible infrastructure.
- Jenkins for CI/CD orchestration.
- SonarQube for automated code-quality checks.
- Ansible for repeatable host provisioning and stack deployment.

## Git Workflow

- `main` is the stable branch.
- `develop` integrates validated work.
- Feature work is done on `feat/...` branches.
- Commits should be signed with `git commit -S`.
- Pull requests should be reviewed before merging into `develop`, then promoted to `main` when stable.

## Repository Layout

```text
services/   Spring Boot microservices
frontend/   React + Vite admin dashboard
infra/      Docker, Jenkins, SonarQube, and Ansible assets
docs/       Architecture, security, and operational documentation
```

## Quick Start

Create a local environment file:

```bash
cp .env.example .env
```

Validate the Compose file:

```bash
docker compose -f infra/docker/docker-compose.yml --env-file .env.example config --quiet
```

Start the stack:

```bash
docker compose -f infra/docker/docker-compose.yml --env-file .env up --build
```

The admin dashboard is served at:
- `https://localhost:5443` (TLS, self-signed cert — accept the browser warning in dev)
- `http://localhost:5173` (redirects 301 to HTTPS)

Observability and secrets endpoints:
- `http://localhost:5440` — Grafana (login `admin/admin`), the *TravelPlan logs*
  dashboard streams every backend container with a `correlationId` filter.
- `http://localhost:8200` — Vault UI / API (root token from `VAULT_ROOT_TOKEN`).
  The `vault-init` container seeds `secret/travelplan/*` from the `.env` so the
  services can read their database credentials, JWT secret, Stripe key, etc.

Run tests locally when Maven is installed:

```bash
mvn clean test
```

Run tests without local Maven:

```bash
docker run --rm -v "$PWD:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn clean test
```

## Local Requirements

- Git
- Docker Desktop
- Java 21
- Maven 3.9+
- Node 20+ and npm (for the frontend)

If Maven is not installed locally, the Jenkins pipeline and Docker-based build commands can still run in a Maven container.

## Frontend (Admin Dashboard)

Local development:

```bash
cd frontend
npm install
npm run dev
```

By default the dev server listens on `http://localhost:5173` and proxies
`/api/auth`, `/api/admin` and `/api/travels` to the backend services
(`localhost:8081/8082/8083`). Override those targets with `VITE_AUTH_URL`,
`VITE_ADMIN_URL`, `VITE_TRAVEL_URL`.

Production build:

```bash
cd frontend
npm run build
```

When running the full stack via `docker compose`, the dashboard is built into an
`nginx` image and exposed at `http://localhost:5173`.

Default admin credentials are seeded by `auth-service` on first boot — see
`ADMIN_BOOTSTRAP_EMAIL` and `ADMIN_BOOTSTRAP_PASSWORD` in `.env.example`.

## Documentation

- [Architecture](docs/architecture.md)
- [Database schema](docs/database-schema.md)
- [Security baseline](docs/security.md)
- [API endpoints](docs/api.md)
- [Roadmap](docs/roadmap.md)
- [Ansible deployment](infra/ansible/README.md)
- [Jenkins setup](infra/jenkins/README.md)
