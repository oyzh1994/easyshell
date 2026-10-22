#!/usr/bin/env bash
set -euo pipefail

CREDENTIALS_FILE=/root/.easyshell-2fa-credentials
GOOGLE_AUTH_FILE=/root/.google_authenticator
GOOGLE_AUTH_SETUP=/root/.google_authenticator.setup
SSH_PASSWORD="${SSH_PASSWORD:-123456}"

mkdir -p /run/sshd /root

if [ ! -s "$GOOGLE_AUTH_FILE" ]; then
    google-authenticator -t -D -f -C -q -u -w 1 \
        -l root@easyshell-2fa -i EasyShell > "$GOOGLE_AUTH_SETUP" 2>&1
    chmod 600 "$GOOGLE_AUTH_FILE" "$GOOGLE_AUTH_SETUP"
    echo "Generated a new TOTP secret at $GOOGLE_AUTH_FILE"
fi

if [ ! -s "$CREDENTIALS_FILE" ] || ! grep -qxF "password=$SSH_PASSWORD" "$CREDENTIALS_FILE"; then
    secret="$(head -n 1 "$GOOGLE_AUTH_FILE")"
    otpauth_uri="otpauth://totp/EasyShell:root?secret=${secret}&issuer=EasyShell"
    umask 077
    printf 'username=root\npassword=%s\ntotp_secret=%s\notpauth_uri=%s\n' \
        "$SSH_PASSWORD" "$secret" "$otpauth_uri" > "$CREDENTIALS_FILE"
    chmod 600 "$CREDENTIALS_FILE"
fi

password="$(awk -F= '$1 == "password" { sub(/^password=/, ""); print; exit }' "$CREDENTIALS_FILE")"
if [ -z "$password" ]; then
    echo "Unable to read the SSH password from $CREDENTIALS_FILE" >&2
    exit 1
fi

echo "root:${password}" | chpasswd
chown root:root "$GOOGLE_AUTH_FILE" "$CREDENTIALS_FILE"
chmod 600 "$GOOGLE_AUTH_FILE" "$CREDENTIALS_FILE"
ssh-keygen -A >/dev/null

echo "EasyShell SSH 2FA is starting on container port 22"
echo "Local test credentials are stored in $CREDENTIALS_FILE"

exec /usr/sbin/sshd -D -e
