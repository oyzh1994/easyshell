#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../../.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker/ssh/2fa/docker-compose.yml"
CONTAINER="${EASYSHELL_2FA_CONTAINER:-easyshell-ssh-2fa}"
DOCKER_BIN="${DOCKER_BIN:-}"

if [ -z "$DOCKER_BIN" ]; then
    if command -v docker >/dev/null 2>&1; then
        DOCKER_BIN="$(command -v docker)"
    elif [ -x /Applications/Docker.app/Contents/Resources/bin/docker ]; then
        DOCKER_BIN=/Applications/Docker.app/Contents/Resources/bin/docker
    else
        echo "docker executable was not found" >&2
        exit 1
    fi
fi

"$DOCKER_BIN" compose -f "$COMPOSE_FILE" up -d --build

for _ in $(seq 1 60); do
    if "$DOCKER_BIN" exec "$CONTAINER" test -s /root/.easyshell-2fa-credentials >/dev/null 2>&1; then
        break
    fi
    sleep 1
done

credentials="$("$DOCKER_BIN" exec "$CONTAINER" cat /root/.easyshell-2fa-credentials)"
password="$(printf '%s\n' "$credentials" | sed -n 's/^password=//p')"
secret="$(printf '%s\n' "$credentials" | sed -n 's/^totp_secret=//p')"
if [ -z "$password" ] || [ -z "$secret" ]; then
    echo "2FA credentials are not ready" >&2
    exit 1
fi

port="$("$DOCKER_BIN" port "$CONTAINER" 22/tcp | awk -F: 'NR == 1 { print $NF }')"
if [ -z "$port" ]; then
    echo "unable to resolve the mapped SSH port" >&2
    exit 1
fi

known_hosts="$HOME/.easyshell_dev/known_hosts"
mkdir -p "$(dirname "$known_hosts")"
touch "$known_hosts"
ssh-keygen -R "[127.0.0.1]:$port" -f "$known_hosts" >/dev/null 2>&1 || true
host_key="$("$DOCKER_BIN" exec "$CONTAINER" cat /etc/ssh/ssh_host_ed25519_key.pub)"
printf '[127.0.0.1]:%s %s\n' "$port" "$host_key" >> "$known_hosts"

classpath="$ROOT_DIR/target/classes:$ROOT_DIR/target/test-classes:$(cat "$ROOT_DIR/target/jsch-isolation/easyshell-full.cp")"
EASYSHELL_2FA_PASSWORD="$password" \
EASYSHELL_2FA_SECRET="$secret" \
EASYSHELL_2FA_PORT="$port" \
java -cp "$classpath" cn.oyzh.easyshell.test.ShellJSchTwoFactorAuthRegression
