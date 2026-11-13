FROM ubuntu:20.04
ENV DEBIAN_FRONTEND=noninteractive

RUN printf '%s\n' \
    'deb http://mirrors.aliyun.com/ubuntu/ focal main restricted universe multiverse' \
    'deb http://mirrors.aliyun.com/ubuntu/ focal-updates main restricted universe multiverse' \
    'deb http://mirrors.aliyun.com/ubuntu/ focal-backports main restricted universe multiverse' \
    'deb http://mirrors.aliyun.com/ubuntu/ focal-security main restricted universe multiverse' \
    > /etc/apt/sources.list

RUN apt-get update && \
    apt-get install -y rsh-server rsh-client xinetd python3 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN cat > /etc/xinetd.d/rsh <<'EOF'
service shell
{
    socket_type     = stream
    protocol        = tcp
    wait            = no
    user            = root
    server          = /usr/sbin/in.rshd
    disable         = no
}
EOF

RUN echo 'ALL: ALL' > /etc/hosts.allow
RUN echo 'localhost' > /etc/hosts.equiv
RUN mkdir -p /root && echo 'localhost root' > /root/.rhosts && chmod 600 /root/.rhosts

RUN echo 'root:123456' | chpasswd

RUN cat > /usr/local/bin/rsh-proxy.py <<'PYEOF'
#!/usr/bin/env python3
import random
import socket
import threading

LISTEN_PORT = 1514
TARGET_HOST = '127.0.0.1'
TARGET_PORT = 514


def pick_source_socket():
    ports = list(range(512, 1024))
    random.shuffle(ports)
    for port in ports:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        try:
            sock.bind(('127.0.0.1', port))
            return sock
        except OSError:
            sock.close()
    return None


def close_socket(sock):
    try:
        sock.shutdown(socket.SHUT_RDWR)
    except OSError:
        pass
    try:
        sock.close()
    except OSError:
        pass


def pipe(source, target):
    try:
        while True:
            data = source.recv(4096)
            if not data:
                break
            target.sendall(data)
    except OSError:
        pass
    finally:
        close_socket(source)
        close_socket(target)


def handle(client):
    upstream = pick_source_socket()
    if upstream is None:
        client.close()
        return
    try:
        upstream.connect((TARGET_HOST, TARGET_PORT))
    except OSError:
        client.close()
        upstream.close()
        return
    threading.Thread(target=pipe, args=(client, upstream), daemon=True).start()
    threading.Thread(target=pipe, args=(upstream, client), daemon=True).start()


def main():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(('0.0.0.0', LISTEN_PORT))
    server.listen(16)
    print(f'rsh proxy listening on :{LISTEN_PORT}', flush=True)
    while True:
        client, _ = server.accept()
        threading.Thread(target=handle, args=(client,), daemon=True).start()


if __name__ == '__main__':
    main()
PYEOF
RUN chmod +x /usr/local/bin/rsh-proxy.py

EXPOSE 514 1514

CMD ["sh", "-c", "python3 /usr/local/bin/rsh-proxy.py & exec xinetd -dontfork"]
