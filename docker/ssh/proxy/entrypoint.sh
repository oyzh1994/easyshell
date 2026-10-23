#!/usr/bin/env bash
set -euo pipefail

PROXY_TYPE="${PROXY_TYPE:-http}"
PROXY_AUTH="${PROXY_AUTH:-0}"
PROXY_USER="${PROXY_USER:-proxyuser}"
PROXY_PASSWORD="${PROXY_PASSWORD:-proxy-pass}"

if [[ "${PROXY_TYPE}" == "http" ]]; then
    if [[ "${PROXY_AUTH}" == "1" ]]; then
        cat >/tmp/easyshell-tinyproxy.conf <<EOF
User root
Group root
Port 3128
Listen 0.0.0.0
Timeout 600
DefaultErrorFile "/usr/share/tinyproxy/default.html"
StatFile "/usr/share/tinyproxy/stats.html"
Logfile "/dev/stdout"
LogLevel Info
PidFile "/tmp/tinyproxy.pid"
MaxClients 100
Allow 0.0.0.0/0
ViaProxyName "easyshell-proxy-http-auth"
BasicAuth ${PROXY_USER} ${PROXY_PASSWORD}
EOF
    else
        cat >/tmp/easyshell-tinyproxy.conf <<EOF
User root
Group root
Port 3128
Listen 0.0.0.0
Timeout 600
DefaultErrorFile "/usr/share/tinyproxy/default.html"
StatFile "/usr/share/tinyproxy/stats.html"
Logfile "/dev/stdout"
LogLevel Info
PidFile "/tmp/tinyproxy.pid"
MaxClients 100
Allow 0.0.0.0/0
ViaProxyName "easyshell-proxy-http-open"
EOF
    fi
    exec tinyproxy -d -c /tmp/easyshell-tinyproxy.conf
fi

if [[ "${PROXY_TYPE}" == "socks" ]]; then
    if [[ "${PROXY_AUTH}" == "1" ]]; then
        if ! id "${PROXY_USER}" >/dev/null 2>&1; then
            adduser -D -s /bin/bash "${PROXY_USER}"
        fi
        echo "${PROXY_USER}:${PROXY_PASSWORD}" | chpasswd
        socks_method="username"
    else
        socks_method="none"
    fi
    cat >/tmp/easyshell-danted.conf <<EOF
logoutput: /dev/stdout
internal: eth0 port = 1080
external: eth0
clientmethod: none
socksmethod: ${socks_method}
user.privileged: root
user.unprivileged: nobody
client pass {
    from: 0.0.0.0/0 to: 0.0.0.0/0
    log: connect disconnect error
}
socks pass {
    from: 0.0.0.0/0 to: 0.0.0.0/0
    command: connect
    method: ${socks_method}
    log: connect disconnect error
}
EOF
    exec sockd -f /tmp/easyshell-danted.conf -N 1
fi

echo "unsupported PROXY_TYPE: ${PROXY_TYPE}" >&2
exit 2
