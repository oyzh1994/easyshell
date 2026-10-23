#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
export PATH="/Applications/Docker.app/Contents/Resources/bin:${PATH}"

: "${TARGET_HOST:?set TARGET_HOST to the TCP target host}"
: "${TARGET_PORT:?set TARGET_PORT to the TCP target port}"

docker compose up -d --build
for _ in $(seq 1 30); do
    if docker compose ps --status running | grep -q 'easyshell-proxy-http-open'; then
        break
    fi
    sleep 1
done

args=(--host "${TARGET_HOST}" --port "${TARGET_PORT}")
if [[ -n "${PROBE_SEND_HEX:-}" ]]; then
    args+=(--send-hex "${PROBE_SEND_HEX}")
fi
if [[ -n "${PROBE_READ_BYTES:-}" ]]; then
    args+=(--read-bytes "${PROBE_READ_BYTES}")
fi
if [[ -n "${PROBE_EXPECT_HEX:-}" ]]; then
    args+=(--expect-hex "${PROBE_EXPECT_HEX}")
fi
if [[ -n "${PROBE_EXPECT_PREFIX_HEX:-}" ]]; then
    args+=(--expect-prefix-hex "${PROBE_EXPECT_PREFIX_HEX}")
fi

python3 test_tcp_proxy.py "${args[@]}"
if [[ "${BENCHMARK:-0}" == "1" ]]; then
    python3 test_tcp_proxy.py "${args[@]}" --benchmark
fi
docker compose ps
echo "Proxy containers are left running for manual review."
