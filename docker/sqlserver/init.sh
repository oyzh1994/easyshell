#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONTAINER="${1:-easyshell-sqlserver}"
PASSWORD="${MSSQL_SA_PASSWORD:-easyshell@2026}"
SQLCMD="/opt/mssql-tools18/bin/sqlcmd"

echo "Waiting for ${CONTAINER}..."
for _ in $(seq 1 60); do
    if docker exec "${CONTAINER}" "${SQLCMD}" \
        -S localhost -U sa -P "${PASSWORD}" -C -Q "SELECT 1" >/dev/null 2>&1; then
        break
    fi
    sleep 2
done

if ! docker exec "${CONTAINER}" "${SQLCMD}" \
    -S localhost -U sa -P "${PASSWORD}" -C -Q "SELECT 1" >/dev/null 2>&1; then
    echo "SQL Server did not become ready in time." >&2
    exit 1
fi

docker cp "${SCRIPT_DIR}/init.sql" "${CONTAINER}:/tmp/easyshell-sqlserver-init.sql"
docker exec "${CONTAINER}" "${SQLCMD}" \
    -S localhost -U sa -P "${PASSWORD}" -C -b \
    -i /tmp/easyshell-sqlserver-init.sql
docker exec -u root "${CONTAINER}" sh -c "rm -f /tmp/easyshell-sqlserver-init.sql"

echo "SQL Server initialized."
echo "Host: 127.0.0.1"
echo "Port: 14333"
echo "User: sa"
echo "Password: ${PASSWORD}"
echo "Database: EASYSHELL_SQLSERVER"
