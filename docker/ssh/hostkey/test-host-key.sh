#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../../.." && pwd)"
CONTAINER="${EASYSHELL_HOSTKEY_CONTAINER:-easyshell-ssh-hostkey-test}"
HOST_PORT="${EASYSHELL_HOSTKEY_PORT:-2225}"
SSH_USER="${EASYSHELL_HOSTKEY_USER:-root}"
SSH_PASSWORD="${EASYSHELL_HOSTKEY_PASSWORD:-}"
IMAGE="${EASYSHELL_HOSTKEY_IMAGE:-kevinwang15/quick-ssh-server:latest}"

if [ -z "$SSH_PASSWORD" ]; then
    echo "EASYSHELL_HOSTKEY_PASSWORD is required for the local test host" >&2
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

known_hosts="$(mktemp /tmp/easyshell-hostkey-known_hosts.XXXXXX)"
cleanup() {
    "$DOCKER_BIN" rm -f "$CONTAINER" >/dev/null 2>&1 || true
    rm -f "$known_hosts"
    rm -f "$known_hosts.old"
}
trap cleanup EXIT

"$DOCKER_BIN" rm -f "$CONTAINER" >/dev/null 2>&1 || true
"$DOCKER_BIN" run -d --name "$CONTAINER" -p "${HOST_PORT}:22" \
    -e "PASSWORD=${SSH_PASSWORD}" "$IMAGE" >/dev/null

wait_for_sshd() {
    for _ in $(seq 1 60); do
        if "$DOCKER_BIN" exec "$CONTAINER" sh -c 'sshd -T' >/dev/null 2>&1; then
            return 0
        fi
        sleep 1
    done
    echo "the host key test container did not become ready" >&2
    return 1
}

prepare_isolated_base() {
    if [ ! -f "$ROOT_DIR/target/jsch-isolation/base-jsch.cp" ] \
        || [ ! -f "$ROOT_DIR/target/jsch-isolation/base-sources.txt" ]; then
        return 0
    fi
    local base_dir="$ROOT_DIR/target/jsch-isolation/base-classes-isolated"
    mkdir -p "$base_dir"
    local base_cp
    base_cp="$(paste -sd: "$ROOT_DIR/target/jsch-isolation/base-jsch.cp")"
    javac -encoding UTF-8 -cp "$base_cp" -d "$base_dir" \
        @"$ROOT_DIR/target/jsch-isolation/base-sources.txt"
}

run_stage() {
    local mode="$1"
    local dependencies="$ROOT_DIR/target/jsch-isolation/easyshell-full.cp"
    if [ -f "$ROOT_DIR/target/jsch-isolation/easyshell-jsch.cp" ]; then
        dependencies="$(tr ':' '\n' < "$ROOT_DIR/target/jsch-isolation/easyshell-jsch.cp" \
            | grep -viE '(sshd|sshj|jgit)' | paste -sd: -)"
    else
        dependencies="$(cat "$dependencies")"
    fi
    local classpath="$ROOT_DIR/target/classes:$ROOT_DIR/target/test-classes"
    classpath="$classpath:$ROOT_DIR/target/jsch-isolation/base-classes-isolated:$dependencies"
    EASYSHELL_HOSTKEY_PASSWORD="$SSH_PASSWORD" \
    java -Deasyshell.known_hosts.path="$known_hosts" \
        -Dtest.hostkey.mode="$mode" \
        -Dtest.hostkey.host=127.0.0.1 \
        -Dtest.hostkey.port="$HOST_PORT" \
        -Dtest.hostkey.user="$SSH_USER" \
        -cp "$classpath" cn.oyzh.easyshell.test.ShellJSchHostKeyRegression
}

wait_for_sshd
prepare_isolated_base
: > "$known_hosts"

run_stage reject-new
run_stage accept-new
ssh-keygen -H -f "$known_hosts" >/dev/null 2>&1
printf '\n# hashed known_hosts compatibility test\n\n' >> "$known_hosts"
run_stage persist

"$DOCKER_BIN" exec "$CONTAINER" sh -c \
    'rm -f /etc/ssh/ssh_host_* && ssh-keygen -A >/dev/null 2>&1'
"$DOCKER_BIN" restart "$CONTAINER" >/dev/null
wait_for_sshd

run_stage reject-changed
run_stage accept-changed
run_stage persist-changed

echo "JSch host key regression passed"
