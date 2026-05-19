#!/bin/sh
# Seeds dev Vault with the secrets each microservice expects.
# Reads cleartext values from environment variables (already present in
# the compose env) so .env stays the single source of truth in dev.

set -e

: "${VAULT_ADDR:?VAULT_ADDR is required}"
: "${VAULT_TOKEN:?VAULT_TOKEN is required}"

until vault status -format=json >/dev/null 2>&1; do
  echo "vault-init: waiting for Vault at ${VAULT_ADDR}..."
  sleep 2
done

echo "vault-init: enabling KV v2 (idempotent)"
vault secrets enable -path=secret -version=2 kv 2>/dev/null || true

echo "vault-init: writing shared/jwt"
vault kv put secret/travelplan/shared \
  jwt.secret="${JWT_SECRET}" \
  jwt.issuer="${JWT_ISSUER}"

echo "vault-init: writing auth-service"
vault kv put secret/travelplan/auth-service \
  spring.datasource.username="${AUTH_DATABASE_USERNAME}" \
  spring.datasource.password="${AUTH_DATABASE_PASSWORD}" \
  travelplan.bootstrap.admin-email="${ADMIN_BOOTSTRAP_EMAIL}" \
  travelplan.bootstrap.admin-password="${ADMIN_BOOTSTRAP_PASSWORD}"

echo "vault-init: writing admin-service"
vault kv put secret/travelplan/admin-service \
  spring.datasource.username="${ADMIN_DATABASE_USERNAME}" \
  spring.datasource.password="${ADMIN_DATABASE_PASSWORD}"

echo "vault-init: writing travel-service"
vault kv put secret/travelplan/travel-service \
  spring.neo4j.authentication.username="${NEO4J_USER}" \
  spring.neo4j.authentication.password="${NEO4J_PASSWORD}"

echo "vault-init: writing payment-service"
vault kv put secret/travelplan/payment-service \
  spring.datasource.username="${ADMIN_DATABASE_USERNAME}" \
  spring.datasource.password="${ADMIN_DATABASE_PASSWORD}" \
  payment.providers.stripe.api-key="${STRIPE_API_KEY:-}" \
  payment.providers.stripe.webhook-secret="${STRIPE_WEBHOOK_SECRET:-}" \
  payment.providers.paypal.client-id="${PAYPAL_CLIENT_ID:-}" \
  payment.providers.paypal.client-secret="${PAYPAL_CLIENT_SECRET:-}" \
  payment.providers.paypal.webhook-id="${PAYPAL_WEBHOOK_ID:-}"

echo "vault-init: done"
