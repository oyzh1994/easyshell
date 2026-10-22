#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../../.." && pwd)"
CONTAINER="${EASYSHELL_LEGACY_CONTAINER:-easyshell-ssh-legacy-test}"
HOST_PORT="${EASYSHELL_LEGACY_PORT:-2226}"
SSH_USER="${EASYSHELL_LEGACY_USER:-root}"
SSH_PASSWORD="${EASYSHELL_LEGACY_PASSWORD:-}"
IMAGE="${EASYSHELL_LEGACY_IMAGE:-kevinwang15/quick-ssh-server:latest}"

if [ -z "$SSH_PASSWORD" ]; then
    echo "EASYSHELL_LEGACY_PASSWORD is required for the local test host" >&2
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

known_hosts="$(mktemp /tmp/easyshell-legacy-known_hosts.XXXXXX)"
cleanup() {
    "$DOCKER_BIN" rm -f "$CONTAINER" >/dev/null 2>&1 || true
    rm -f "$known_hosts"
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
    echo "the legacy algorithm test container did not become ready" >&2
    return 1
}

wait_for_sshd
"$DOCKER_BIN" exec "$CONTAINER" sh -c 'ssh-keygen -A >/dev/null 2>&1'
"$DOCKER_BIN" exec "$CONTAINER" sh -c 'cat >>/etc/ssh/sshd_config <<"EOF"
KexAlgorithms diffie-hellman-group14-sha1
HostKeyAlgorithms ssh-rsa
PubkeyAcceptedAlgorithms ssh-rsa
EOF'
"$DOCKER_BIN" restart "$CONTAINER" >/dev/null
wait_for_sshd

"$DOCKER_BIN" exec "$CONTAINER" sh -c \
    "sshd -T | grep -qx 'kexalgorithms diffie-hellman-group14-sha1'"
"$DOCKER_BIN" exec "$CONTAINER" sh -c \
    "sshd -T | grep -qx 'hostkeyalgorithms ssh-rsa'"

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

prepare_isolated_base
: > "$known_hosts"
dependencies="$(cat "$ROOT_DIR/target/jsch-isolation/easyshell-full.cp")"
if [ -f "$ROOT_DIR/target/jsch-isolation/easyshell-jsch.cp" ]; then
    dependencies="$(tr ':' '\n' < "$ROOT_DIR/target/jsch-isolation/easyshell-jsch.cp" \
        | grep -viE '(sshd|sshj|jgit)' | paste -sd: -)"
fi
classpath="$ROOT_DIR/target/classes:$ROOT_DIR/target/test-classes"
classpath="$classpath:$ROOT_DIR/target/jsch-isolation/base-classes-isolated:$dependencies"

run_case() {
    local jump="$1"
    local session_only="$2"
    EASYSHELL_LEGACY_USER="$SSH_USER" EASYSHELL_LEGACY_PASSWORD="$SSH_PASSWORD" \
    java -Deasyshell.known_hosts.path="$known_hosts" \
        -Dtest.legacy.host=127.0.0.1 \
        -Dtest.legacy.port="$HOST_PORT" \
        -Dtest.legacy.jump="$jump" \
        -Dtest.legacy.jump.sessionOnly="$session_only" \
        -cp "$classpath" cn.oyzh.easyshell.test.ShellJSchLegacyAlgorithmRegression
}

run_case false false
run_case true true
echo "JSch legacy algorithm regression passed"
