#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../../.." && pwd)"
NETWORK="${EASYSHELL_AGENT_NETWORK:-easyshell-agent-forward}"
TARGET_CONTAINER="${EASYSHELL_FORWARD_TARGET_CONTAINER:-easyshell-ssh-agent-test}"
JUMP_CONTAINER="${EASYSHELL_FORWARD_JUMP_CONTAINER:-easyshell-ssh-agent-jump}"
TARGET_PORT="${EASYSHELL_FORWARD_TARGET_PORT:-2222}"
JUMP_PORT="${EASYSHELL_FORWARD_JUMP_PORT:-2223}"
SSH_USER="${EASYSHELL_FORWARD_USER:-root}"
SSH_PASSWORD="${EASYSHELL_FORWARD_PASSWORD:-}"
ENV_NAME="${EASYSHELL_FORWARD_ENV_NAME:-EASYSHELL_TEST_ENV}"
ENV_VALUE="${EASYSHELL_FORWARD_ENV_VALUE:-agent-forward-env}"
IMAGE="kevinwang15/quick-ssh-server:latest"

if [ -z "$SSH_PASSWORD" ]; then
    echo "EASYSHELL_FORWARD_PASSWORD is required for the local test hosts" >&2
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

"$DOCKER_BIN" network inspect "$NETWORK" >/dev/null 2>&1 \
    || "$DOCKER_BIN" network create "$NETWORK" >/dev/null

if "$DOCKER_BIN" ps -a --format '{{.Names}}' | grep -qx "$TARGET_CONTAINER"; then
    "$DOCKER_BIN" start "$TARGET_CONTAINER" >/dev/null
else
    "$DOCKER_BIN" run -d --name "$TARGET_CONTAINER" -p "${TARGET_PORT}:22" \
        -e "PASSWORD=${SSH_PASSWORD}" "$IMAGE" >/dev/null
fi

"$DOCKER_BIN" rm -f "$JUMP_CONTAINER" >/dev/null 2>&1 || true
"$DOCKER_BIN" run -d --name "$JUMP_CONTAINER" --network "$NETWORK" --network-alias jump \
    -p "${JUMP_PORT}:22" -e "PASSWORD=${SSH_PASSWORD}" "$IMAGE" >/dev/null

"$DOCKER_BIN" network disconnect "$NETWORK" "$TARGET_CONTAINER" >/dev/null 2>&1 || true
"$DOCKER_BIN" network connect --alias target "$NETWORK" "$TARGET_CONTAINER"

for container in "$TARGET_CONTAINER" "$JUMP_CONTAINER"; do
    "$DOCKER_BIN" exec "$container" sh -c \
        'grep -q "^AcceptEnv EASYSHELL_TEST_" /etc/ssh/sshd_config \
         || echo "AcceptEnv EASYSHELL_TEST_*" >> /etc/ssh/sshd_config'
done
"$DOCKER_BIN" restart "$TARGET_CONTAINER" "$JUMP_CONTAINER" >/dev/null

for _ in $(seq 1 60); do
    target_ready=false
    jump_ready=false
    if "$DOCKER_BIN" exec "$TARGET_CONTAINER" sh -c 'sshd -T' >/dev/null 2>&1; then
        target_ready=true
    fi
    if "$DOCKER_BIN" exec "$JUMP_CONTAINER" sh -c 'sshd -T' >/dev/null 2>&1; then
        jump_ready=true
    fi
    if [ "$target_ready" = true ] && [ "$jump_ready" = true ]; then
        break
    fi
    sleep 1
done

for container in "$TARGET_CONTAINER" "$JUMP_CONTAINER"; do
    if ! "$DOCKER_BIN" exec "$container" sh -c 'sshd -T | grep -x "pubkeyauthentication yes" >/dev/null'; then
        echo "public key authentication is not enabled on $container" >&2
        exit 1
    fi
    if ! "$DOCKER_BIN" exec "$container" sh -c 'sshd -T | grep -x "allowagentforwarding yes" >/dev/null'; then
        echo "agent forwarding is not enabled on $container" >&2
        exit 1
    fi
done

if ! "$DOCKER_BIN" exec "$JUMP_CONTAINER" sh -c 'getent hosts target' >/dev/null 2>&1; then
    echo "the jump host cannot resolve the Docker target alias" >&2
    exit 1
fi

known_hosts="$HOME/.easyshell_dev/known_hosts"
mkdir -p "$(dirname "$known_hosts")"
touch "$known_hosts"
ssh-keygen -R "[127.0.0.1]:$TARGET_PORT" -f "$known_hosts" >/dev/null 2>&1 || true
ssh-keygen -R "[127.0.0.1]:$JUMP_PORT" -f "$known_hosts" >/dev/null 2>&1 || true
ssh-keyscan -p "$TARGET_PORT" 127.0.0.1 2>/dev/null >> "$known_hosts"
ssh-keyscan -p "$JUMP_PORT" 127.0.0.1 2>/dev/null >> "$known_hosts"

agent_dir="$(mktemp -d /tmp/easyshell-agent-forward.XXXXXX)"
agent_socket="$agent_dir/agent.sock"
real_key="$agent_dir/id_ed25519"
real_public_key="$real_key.pub"
decoy_key="$agent_dir/id_ed25519_decoy"

cleanup() {
    SSH_AUTH_SOCK="$agent_socket" ssh-agent -k >/dev/null 2>&1 || true
    rm -rf "$agent_dir"
}
trap cleanup EXIT

eval "$(ssh-agent -a "$agent_socket")" >/dev/null
ssh-keygen -q -t ed25519 -N '' -C "easyshell-forward-decoy-$(date +%s)" -f "$decoy_key"
SSH_AUTH_SOCK="$agent_socket" ssh-add "$decoy_key" >/dev/null
ssh-keygen -q -t ed25519 -N '' -C "easyshell-forward-$(date +%s)" -f "$real_key"
SSH_AUTH_SOCK="$agent_socket" ssh-add "$real_key" >/dev/null
rm -f "$decoy_key" "$real_key"

classpath="$ROOT_DIR/target/classes:$ROOT_DIR/target/test-classes:$(cat "$ROOT_DIR/target/jsch-isolation/easyshell-full.cp")"
SSH_AUTH_SOCK="$agent_socket" \
EASYSHELL_AGENT_PUBLIC_KEY="$real_public_key" \
EASYSHELL_AGENT_PRIVATE_KEY="$real_key" \
EASYSHELL_FORWARD_JUMP_HOST=127.0.0.1 \
EASYSHELL_FORWARD_JUMP_PORT="$JUMP_PORT" \
EASYSHELL_FORWARD_TARGET_HOST=127.0.0.1 \
EASYSHELL_FORWARD_TARGET_PORT="$TARGET_PORT" \
EASYSHELL_FORWARD_TARGET_SSH_HOST=target \
EASYSHELL_FORWARD_TARGET_SSH_PORT=22 \
EASYSHELL_FORWARD_USER="$SSH_USER" \
EASYSHELL_FORWARD_PASSWORD="$SSH_PASSWORD" \
EASYSHELL_FORWARD_ENV_NAME="$ENV_NAME" \
EASYSHELL_FORWARD_ENV_VALUE="$ENV_VALUE" \
java -cp "$classpath" cn.oyzh.easyshell.test.ShellJSchAgentForwardingRegression
