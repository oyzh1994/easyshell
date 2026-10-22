#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../../.." && pwd)"
CONTAINER="${EASYSHELL_AGENT_CONTAINER:-easyshell-ssh-agent-test}"
HOST_PORT="${EASYSHELL_AGENT_PORT:-2222}"
SSH_USER="${EASYSHELL_AGENT_USER:-root}"
SSH_PASSWORD="${EASYSHELL_AGENT_PASSWORD:-}"

if [ -z "$SSH_PASSWORD" ]; then
    echo "EASYSHELL_AGENT_PASSWORD is required for the local test host" >&2
    exit 1
fi

if [ -z "${DOCKER_BIN:-}" ]; then
    if command -v docker >/dev/null 2>&1; then
        DOCKER_BIN="$(command -v docker)"
    elif [ -x /Applications/Docker.app/Contents/Resources/bin/docker ]; then
        DOCKER_BIN=/Applications/Docker.app/Contents/Resources/bin/docker
    else
        echo "docker executable was not found" >&2
        exit 1
    fi
fi

if "$DOCKER_BIN" ps -a --format '{{.Names}}' | grep -qx "$CONTAINER"; then
    "$DOCKER_BIN" start "$CONTAINER" >/dev/null
else
    "$DOCKER_BIN" run -d --name "$CONTAINER" -p "${HOST_PORT}:22" \
        -e "PASSWORD=${SSH_PASSWORD}" kevinwang15/quick-ssh-server:latest >/dev/null
fi

for _ in $(seq 1 60); do
    if "$DOCKER_BIN" exec "$CONTAINER" sh -c 'sshd -T' >/dev/null 2>&1; then
        break
    fi
    sleep 1
done

if ! "$DOCKER_BIN" exec "$CONTAINER" sh -c 'sshd -T | grep -x "pubkeyauthentication yes" >/dev/null'; then
    echo "the agent test host does not enable public key authentication" >&2
    exit 1
fi

known_hosts="$HOME/.easyshell_dev/known_hosts"
mkdir -p "$(dirname "$known_hosts")"
touch "$known_hosts"
ssh-keygen -R "[127.0.0.1]:$HOST_PORT" -f "$known_hosts" >/dev/null 2>&1 || true
ssh-keyscan -p "$HOST_PORT" 127.0.0.1 2>/dev/null >> "$known_hosts"

agent_dir="$(mktemp -d /tmp/easyshell-agent.XXXXXX)"
agent_socket="$agent_dir/agent.sock"
key="$agent_dir/id_ed25519"
public_key="$key.pub"
private_key="$key"
decoy_key="$agent_dir/id_ed25519_decoy"

cleanup() {
    SSH_AUTH_SOCK="$agent_socket" ssh-agent -k >/dev/null 2>&1 || true
    rm -rf "$agent_dir"
}
trap cleanup EXIT

eval "$(ssh-agent -a "$agent_socket")" >/dev/null
ssh-keygen -q -t ed25519 -N '' -C "easyshell-agent-decoy-$(date +%s)" -f "$decoy_key"
SSH_AUTH_SOCK="$agent_socket" ssh-add "$decoy_key" >/dev/null
ssh-keygen -q -t ed25519 -N '' -C "easyshell-agent-$(date +%s)" -f "$key"
SSH_AUTH_SOCK="$agent_socket" ssh-add "$private_key" >/dev/null
rm -f "$decoy_key" "$private_key"

classpath="$ROOT_DIR/target/classes:$ROOT_DIR/target/test-classes:$(cat "$ROOT_DIR/target/jsch-isolation/easyshell-full.cp")"
SSH_AUTH_SOCK="$agent_socket" \
EASYSHELL_AGENT_PUBLIC_KEY="$public_key" \
EASYSHELL_AGENT_PRIVATE_KEY="$private_key" \
EASYSHELL_AGENT_HOST=127.0.0.1 \
EASYSHELL_AGENT_PORT="$HOST_PORT" \
EASYSHELL_AGENT_USER="$SSH_USER" \
EASYSHELL_AGENT_PASSWORD="$SSH_PASSWORD" \
java -cp "$classpath" cn.oyzh.easyshell.test.ShellJSchAgentAuthRegression
