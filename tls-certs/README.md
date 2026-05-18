# TLS certificates

This directory is mounted into the `admin-dashboard` container at
`/etc/nginx/certs/external` (read-only).

For local development the dashboard image generates a self-signed
certificate at build time, so this directory can stay empty.

For production deployments, drop a real PEM cert and key here (or point
`TLS_CERT_DIR` to wherever you keep them) and update `frontend/nginx.conf`
to read from `external/server.crt` and `external/server.key`.

Private keys must never be committed. The `.gitignore` rules in the
repository root keep this directory empty except for documentation.
