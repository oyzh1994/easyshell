FROM kevinwang15/quick-ssh-server:latest

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get -o Acquire::ForceIPv4=true update \
    && apt-get -o Acquire::ForceIPv4=true install -y --no-install-recommends \
        libpam-google-authenticator \
        openssl \
    && rm -rf /var/lib/apt/lists/*

RUN sed -i '/^@include common-auth/a auth required pam_google_authenticator.so' /etc/pam.d/sshd \
    && printf '%s\n' \
        'PermitRootLogin yes' \
        'PasswordAuthentication no' \
        'KbdInteractiveAuthentication yes' \
        'ChallengeResponseAuthentication yes' \
        'PubkeyAuthentication no' \
        'PermitEmptyPasswords no' \
        'UsePAM yes' \
        'AuthenticationMethods keyboard-interactive' \
        'MaxAuthTries 6' \
        'UseDNS no' \
        > /etc/ssh/sshd_config.d/99-easyshell-2fa.conf

RUN mkdir -p /run/sshd && sshd -t

COPY entrypoint.sh /usr/local/bin/easyshell-2fa-entrypoint
RUN chmod 755 /usr/local/bin/easyshell-2fa-entrypoint

EXPOSE 22
ENTRYPOINT ["/usr/local/bin/easyshell-2fa-entrypoint"]
CMD []
