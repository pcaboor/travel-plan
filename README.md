# Travel Management System

Travel Management System is a microservices-based project for managing users, travel offers, destinations, and payment providers.

This repository starts with the infrastructure and backend foundations required before building the complete admin dashboard.

## Target Architecture

- Java 21 and Spring Boot 3 microservices.
- PostgreSQL for relational administration data.
- Neo4j for graph-oriented travel and destination relationships.
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
infra/      Docker, Jenkins, SonarQube, and Ansible assets
docs/       Architecture, security, and operational documentation
```

## Local Requirements

- Git
- Docker Desktop
- Java 21
- Maven 3.9+

If Maven is not installed locally, the Jenkins pipeline and Docker-based build commands can still run in a Maven container.
