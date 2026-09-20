FROM ubuntu:20.04
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y rsh-server xinetd python3 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN cat > /etc/xinetd.d/rlogin <<'EOF'
service login
{
    socket_type     = stream
    protocol        = tcp
    wait            = no
    user            = root
    server          = /usr/sbin/in.rlogind
    server_args     = -l
    disable         = no
}
EOF

RUN echo 'ALL: ALL' > /etc/hosts.allow
RUN echo 'rlogin' > /etc/securetty

RUN echo 'localhost' > /etc/hosts.equiv
RUN mkdir -p /root && echo 'localhost' > /root/.rhosts && chmod 600 /root/.rhosts

RUN echo 'root:123456' | chpasswd

# 转发脚本：每个连接从 512-1023 挑一个空闲源端口
RUN cat > /usr/local/bin/rlogin-proxy.py <<'PYEOF'
#!/usr/bin/env python3
import socket, threading, random

LISTEN_PORT = 1513
TARGET_HOST = '127.0.0.1'
TARGET_PORT = 513
SRC_RANGE = list(range(512, 1024))
random.shuffle(SRC_RANGE)

def pick_source_port():
    """尝试从 512-1023 找一个没被占用的端口"""
    for p in SRC_RANGE:
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            s.bind(('127.0.0.1', p))
            return s, p
        except OSError:
            try: s.close()
            except: pass
    return None, None

def pipe(a, b):
    try:
        while True:
            data = a.recv(4096)
            if not data: break
            b.sendall(data)
    except OSError:
        pass
    finally:
        try: a.close()
        except: pass
        try: b.close()
        except: pass

def handle(client):
    src_sock, src_port = pick_source_port()
    if src_sock is None:
        client.close()
        return
    try:
        src_sock.connect((TARGET_HOST, TARGET_PORT))
    except OSError:
        client.close()
        src_sock.close()
        return
    threading.Thread(target=pipe, args=(client, src_sock), daemon=True).start()
    threading.Thread(target=pipe, args=(src_sock, client), daemon=True).start()

def main():
    srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind(('0.0.0.0', LISTEN_PORT))
    srv.listen(16)
    print(f'proxy listening on :{LISTEN_PORT}', flush=True)
    while True:
        client, addr = srv.accept()
        threading.Thread(target=handle, args=(client,), daemon=True).start()

if __name__ == '__main__':
    main()
PYEOF
RUN chmod +x /usr/local/bin/rlogin-proxy.py

EXPOSE 1513

CMD ["sh", "-c", "python3 /usr/local/bin/rlogin-proxy.py & exec xinetd -dontfork"]