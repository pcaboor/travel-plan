# Security Baseline

This foundation prepares the project for secure deployment without committing production secrets.

## Transport Security

- Public traffic should terminate TLS at a reverse proxy or ingress in front of the services.
- Internal service traffic currently stays on Docker networks. Production deployment should use private networking and firewall rules.
- HTTP services are not directly exposed from the internal application network in the base Compose stack.

## Network Isolation

- `travel-internal` is an internal Docker network for databases and microservices.
- `travel-ci` is a separate network for Jenkins and SonarQube.
- Databases are reachable only by containers attached to their network.

## Secret Management

- `.env.example` contains placeholders only.
- Real `.env` files are ignored by Git and must not be committed.
- Production secrets should be supplied by a secret manager such as HashiCorp Vault, injected at deploy time by CI/CD or Ansible.

## Least Privilege

- Application database credentials should be service-specific before production.
- Admin dashboard roles should start with `ADMIN`, `MANAGER`, and `VIEWER` permissions.
- Payment provider credentials should be scoped to the minimum API permissions required for Stripe and PayPal operations.

## Dependency Hygiene

- Jenkins runs unit tests and SonarQube analysis on pull requests.
- Components should be updated regularly to receive security patches.
- Container base images should be rebuilt often and scanned before production release.
