package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.ssh.ShellSSHClient;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

public final class ShellJSchProxyRegression {

    private static final String PROXY_SCRIPT = """
            import socket
            import struct
            import sys
            import threading
            import time
            import os

            base = sys.argv[1]
            log_lock = threading.Lock()
            with open(base + '/proxy.pid', 'w', encoding='ascii') as stream:
                stream.write(str(os.getpid()))

            def log(message):
                with log_lock:
                    with open(base + '/proxy.log', 'a', encoding='utf-8') as stream:
                        stream.write(message + '\\n')

            def read_exact(stream, length):
                data = b''
                while len(data) < length:
                    chunk = stream.recv(length - len(data))
                    if not chunk:
                        raise ConnectionError('unexpected eof')
                    data += chunk
                return data

            def relay(left, right):
                def forward(source, target):
                    try:
                        while True:
                            data = source.recv(65536)
                            if not data:
                                break
                            target.sendall(data)
                    except Exception:
                        pass
                    try:
                        target.shutdown(socket.SHUT_WR)
                    except Exception:
                        pass

                first = threading.Thread(target=forward, args=(left, right), daemon=True)
                second = threading.Thread(target=forward, args=(right, left), daemon=True)
                first.start()
                second.start()
                first.join()
                second.join()
                try:
                    left.close()
                except Exception:
                    pass
                try:
                    right.close()
                except Exception:
                    pass

            def http_handler(client):
                upstream = None
                try:
                    client.settimeout(120)
                    head = b''
                    while b'\\r\\n\\r\\n' not in head:
                        chunk = client.recv(4096)
                        if not chunk:
                            return
                        head += chunk
                    parts = head.split(b'\\r\\n', 1)[0].split()
                    if len(parts) < 2 or parts[0].upper() != b'CONNECT':
                        client.sendall(b'HTTP/1.1 405 Method Not Allowed\\r\\n\\r\\n')
                        return
                    authority = parts[1].decode('ascii')
                    if authority.startswith('['):
                        host, port = authority[1:].split(']:', 1)
                    else:
                        host, port = authority.rsplit(':', 1)
                    upstream = socket.create_connection((host, int(port)), timeout=20)
                    upstream.settimeout(120)
                    client.sendall(b'HTTP/1.1 200 Connection Established\\r\\n\\r\\n')
                    log('http-connect')
                    relay(client, upstream)
                except Exception as ex:
                    log('http-error-' + type(ex).__name__)
                finally:
                    try:
                        client.close()
                    except Exception:
                        pass
                    if upstream is not None:
                        try:
                            upstream.close()
                        except Exception:
                            pass

            def socks_handler(client):
                upstream = None
                try:
                    client.settimeout(120)
                    header = read_exact(client, 2)
                    if header[0] != 5:
                        return
                    methods = read_exact(client, header[1])
                    if 0 not in methods:
                        client.sendall(b'\\x05\\xff')
                        return
                    client.sendall(b'\\x05\\x00')
                    request = read_exact(client, 4)
                    address_type = request[3]
                    if address_type == 1:
                        host = socket.inet_ntoa(read_exact(client, 4))
                    elif address_type == 3:
                        host = read_exact(client, read_exact(client, 1)[0]).decode('utf-8')
                    elif address_type == 4:
                        host = socket.inet_ntop(socket.AF_INET6, read_exact(client, 16))
                    else:
                        client.sendall(b'\\x05\\x08\\x00\\x01\\x00\\x00\\x00\\x00\\x00\\x00')
                        return
                    port = struct.unpack('!H', read_exact(client, 2))[0]
                    upstream = socket.create_connection((host, port), timeout=20)
                    upstream.settimeout(120)
                    client.sendall(b'\\x05\\x00\\x00\\x01\\x00\\x00\\x00\\x00\\x00\\x00')
                    log('socks5-connect')
                    relay(client, upstream)
                except Exception as ex:
                    log('socks5-error-' + type(ex).__name__)
                finally:
                    try:
                        client.close()
                    except Exception:
                        pass
                    if upstream is not None:
                        try:
                            upstream.close()
                        except Exception:
                            pass

            def serve(server, handler):
                while True:
                    try:
                        client, _ = server.accept()
                    except Exception:
                        return
                    threading.Thread(target=handler, args=(client,), daemon=True).start()

            http_server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            http_server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            http_server.bind(('0.0.0.0', 0))
            http_server.listen(32)
            socks_server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            socks_server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            socks_server.bind(('0.0.0.0', 0))
            socks_server.listen(32)

            with open(base + '/proxy.ports', 'w', encoding='ascii') as stream:
                stream.write(str(http_server.getsockname()[1]) + '\\n')
                stream.write(str(socks_server.getsockname()[1]) + '\\n')

            threading.Thread(target=serve, args=(http_server, http_handler), daemon=True).start()
            threading.Thread(target=serve, args=(socks_server, socks_handler), daemon=True).start()
            log('proxy-ready')
            while True:
                time.sleep(3600)
            """;

    private ShellJSchProxyRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch proxy regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.host.index", 1);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        String token = UUID.randomUUID().toString();
        String directory = "/tmp/easyshell-jsch-proxy-" + token;
        ShellSSHClient admin = new ShellSSHClient(host.newConnect());
        String pid = null;
        try {
            admin.setVerifyFailureCallback(current -> null);
            admin.start(15000);
            String script = Base64.getEncoder().encodeToString(PROXY_SCRIPT.getBytes(StandardCharsets.UTF_8));
            admin.exec("mkdir -p " + quote(directory));
            admin.exec("printf '%s' " + quote(script) + " | base64 -d > " + quote(directory + "/proxy.py"));
            admin.exec("cd " + quote(directory) + "; nohup python3 proxy.py . </dev/null >proxy.log 2>&1 &");

            String ports = awaitPorts(admin, directory);
            pid = requirePid(admin.exec("cat " + quote(directory + "/proxy.pid") + " 2>/dev/null || true"));
            String[] lines = ports.lines().filter(line -> !line.isBlank()).toArray(String[]::new);
            require(lines.length >= 2, "proxy ports file is incomplete");
            int httpPort = Integer.parseInt(lines[0].trim());
            int socksPort = Integer.parseInt(lines[1].trim());
            System.out.println("Temporary proxy endpoints ready: http=" + httpPort + ", socks5=" + socksPort);

            requireProxy(admin, host, "http", httpPort);
            requireProxy(admin, host, "socks5", socksPort);
            require(count(admin, directory, "http-connect") >= 1, "HTTP proxy was not used");
            require(count(admin, directory, "socks5-connect") >= 1, "SOCKS5 proxy was not used");
            require(count(admin, directory, "http-error") == 0, "HTTP proxy reported errors");
            require(count(admin, directory, "socks5-error") == 0, "SOCKS5 proxy reported errors");
        } finally {
            try {
                stopProxy(admin, directory, pid);
                admin.exec("rm -rf " + quote(directory));
            } finally {
                admin.close();
            }
        }
    }

    private static void stopProxy(ShellSSHClient admin, String directory, String pid) {
        String actualPid = pid;
        if (actualPid == null) {
            actualPid = admin.exec("cat " + quote(directory + "/proxy.pid") + " 2>/dev/null || true");
        }
        if (actualPid != null && actualPid.trim().matches("\\d+")) {
            String value = actualPid.trim();
            admin.exec("kill " + value + " 2>/dev/null || true");
            admin.exec("sleep 1; kill -9 " + value + " 2>/dev/null || true");
        }
    }

    private static String awaitPorts(ShellSSHClient admin, String directory) throws Exception {
        long deadline = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < deadline) {
            String ports = admin.exec("test -s " + quote(directory + "/proxy.ports")
                    + " && cat " + quote(directory + "/proxy.ports") + " || true");
            if (ports != null && ports.lines().filter(line -> !line.isBlank()).count() >= 2) {
                return ports;
            }
            Thread.sleep(200);
        }
        String log = admin.exec("tail -n 20 " + quote(directory + "/proxy.log") + " 2>/dev/null || true");
        throw new IllegalStateException("temporary proxy did not start: " + log);
    }

    private static void requireProxy(ShellSSHClient admin, ShellJSchTestHosts.Host host, String protocol, int port)
            throws Exception {
        ShellConnect connect = host.newConnect();
        ShellProxyConfig proxy = new ShellProxyConfig();
        proxy.setProtocol(protocol);
        proxy.setHost(host.host());
        proxy.setPort(port);
        connect.setEnableProxy(true);
        connect.setProxyConfig(proxy);

        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> null);
            client.start(15000);
            require(client.getSession() != null && client.getSession().isConnected(),
                    protocol + " proxy session is not connected");
            String marker = "proxy-" + protocol + "-" + UUID.randomUUID();
            require(marker.equals(client.exec("printf " + quote(marker))),
                    protocol + " proxy exec output mismatch");
            requireSftp(client, protocol);
            System.out.println(protocol + " proxy SSH/SFTP verified");
        }
    }

    private static void requireSftp(ShellSSHClient client, String protocol) throws Exception {
        String remote = "/tmp/easyshell-jsch-proxy-" + UUID.randomUUID() + ".txt";
        String content = "proxy-sftp-" + protocol + "-" + UUID.randomUUID();
        Path downloaded = Files.createTempFile("easyshell-proxy-", ".txt");
        try {
            client.sftpClient().put(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), remote, null);
            ShellSFTPFile file = client.sftpClient().fileInfo(remote);
            require(file != null, protocol + " proxy sftp metadata is missing");
            client.sftpClient().get(file, downloaded.toString());
            require(content.equals(Files.readString(downloaded, StandardCharsets.UTF_8)),
                    protocol + " proxy sftp content mismatch");
        } finally {
            client.exec("rm -f " + quote(remote));
            Files.deleteIfExists(downloaded);
        }
    }

    private static int count(ShellSSHClient admin, String directory, String marker) {
        String output = admin.exec("grep -c " + quote("^" + marker) + " " + quote(directory + "/proxy.log") + " || true");
        try {
            return Integer.parseInt(output == null ? "0" : output.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static String requirePid(String output) {
        require(output != null && output.trim().matches("\\d+"), "temporary proxy pid is invalid");
        return output.trim();
    }

    private static String quote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
