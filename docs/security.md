# Security Baseline

This foundation prepares the project for secure deployment without committing production secrets.

## Transport Security

- Public traffic terminates TLS at the `admin-dashboard` nginx (port 5443). The nginx container runs `listen 443 ssl` with TLSv1.2/1.3, modern ciphers, and HSTS (`max-age=31536000`) on every response.
- Port 80 (5173 on the host) returns a 301 to `https://$host:5443$request_uri`. No application traffic is served over plain HTTP.
- A self-signed certificate is generated at image build time via `openssl req -x509` for local development. In production, drop a real cert/key pair into the path mounted at `/etc/nginx/certs/external` (configured via `TLS_CERT_DIR`) and either symlink them over the generated ones or update `nginx.conf` to read from `external/`.
- Recommended production path: terminate Let's Encrypt via an external ingress (Traefik, Caddy, or AWS ALB) and forward to the dashboard over the internal network, or use cert-manager with Kubernetes (phase 8 bonus).
- Internal service traffic stays on the `travel-internal` Docker network (`internal: true`). No backend service is published on the host. Production deployment should layer firewall rules and optionally mTLS between services.
- The browser will warn about the self-signed certificate on first visit — accept the warning in dev or trust the cert in your OS keychain.

## Network Isolation

- `travel-internal` is an internal Docker network for databases and microservices.
- `travel-ci` is a separate network for Jenkins and SonarQube.
- Databases are reachable only by containers attached to their network.

## Secret Management

- `.env.example` contains placeholders only. Real `.env` files are git-ignored.
- **HashiCorp Vault** is wired in the stack (dev mode in `infra/docker/docker-compose.yml`). At boot, the `vault-init` container seeds `secret/data/travelplan/{shared,auth-service,admin-service,travel-service,payment-service}` from the values that the `.env` already exposes. Each Spring service is built with the Maven `vault` profile so `spring-cloud-starter-vault-config` is on the classpath; in Docker it activates the `vault` Spring profile and reads `secret/travelplan/<service>` over the token-authenticated KV v2 backend.
- For production: replace dev mode with `storage "file"` (or raft), real seal/unseal handlers, an AppRole or Kubernetes auth method per service, and remove the bootstrap script — secrets are then written through a CI/CD pipeline (Ansible or a Vault-aware admin job).
- The Maven `vault` profile keeps Vault out of the unit-test classpath, so `mvn test` runs without needing a Vault instance. Tests still rely on `.env` defaults injected by Spring Boot's standard property resolution.

## Authentication & Authorization

- `auth-service` issues HS256 JWT access tokens after BCrypt password verification.
- `JWT_SECRET` must be at least 32 bytes (validated at boot).
- `admin-service` validates JWTs as a Spring Security OAuth2 resource server using the same shared secret.
- Roles claim is mapped to Spring authorities with `ROLE_` prefix; method-level access uses `@PreAuthorize("hasRole('ADMIN')")`.
- Endpoints are stateless (no session), CSRF disabled (API-only).
- Default bootstrap admin must be replaced or rotated in any non-dev deployment.

## Least Privilege

- Application database credentials should be service-specific before production.
- Admin dashboard roles start with `ADMIN`, `MANAGER`, and `VIEWER` permissions (seeded by Flyway V1).
- Payment provider credentials should be scoped to the minimum API permissions required for Stripe and PayPal operations.

## Audit & Observability

- Each microservice writes structured JSON logs through `LogstashEncoder` when the `docker` Spring profile is active. The `correlationId` (already injected by `CorrelationIdFilter` since phase 1) ships as a top-level JSON field, so the same request can be followed across all four services.
- `promtail` scrapes Docker container logs through the host socket, ships them to **Loki**. **Grafana** exposes the "TravelPlan logs" dashboard on `http://localhost:5440` with a `correlationId` text filter. Use it to debug a failed request end-to-end.
- Logs are ephemeral by default (Loki uses a local volume). In production point Loki at object storage (S3-compatible) and configure retention.

## Dependency Hygiene

- Jenkins runs unit tests and SonarQube analysis on pull requests.
- Components should be updated regularly to receive security patches.
- Container base images should be rebuilt often and scanned before production release.
