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
import java.util.concurrent.atomic.AtomicInteger;

public final class ShellJSchProxyRegression {

    private static final String PROXY_SCRIPT = """
            import base64
            import socket
            import struct
            import sys
            import threading
            import time
            import os

            base = sys.argv[1]
            auth_user = sys.argv[2]
            auth_pass = sys.argv[3]
            expected_basic = 'Basic ' + base64.b64encode(
                (auth_user + ':' + auth_pass).encode('utf-8')).decode('ascii')
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

            def http_handler(client, require_auth):
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
                    if require_auth:
                        headers = {}
                        for line in head.split(b'\\r\\n')[1:]:
                            if b':' in line:
                                key, value = line.split(b':', 1)
                                headers[key.strip().lower()] = value.strip()
                        actual = headers.get(b'proxy-authorization', b'').decode('ascii', 'replace')
                        if actual != expected_basic:
                            client.sendall(b'HTTP/1.1 407 Proxy Authentication Required\\r\\n'
                                           b'Proxy-Authenticate: Basic realm="EasyShell Test"\\r\\n'
                                           b'Content-Length: 0\\r\\n\\r\\n')
                            log('http-auth-failed')
                            return
                        log('http-auth-ok')
                    authority = parts[1].decode('ascii')
                    if authority.startswith('['):
                        host, port = authority[1:].split(']:', 1)
                    else:
                        host, port = authority.rsplit(':', 1)
                    try:
                        socket.inet_aton(host)
                        log('http-target-ip')
                    except OSError:
                        log('http-target-domain')
                    upstream = socket.create_connection((host, int(port)), timeout=20)
                    upstream.settimeout(120)
                    client.sendall(b'HTTP/1.1 200 Connection Established\\r\\n\\r\\n')
                    log('http-connect-auth' if require_auth else 'http-connect')
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

            def socks_handler(client, require_auth):
                upstream = None
                try:
                    client.settimeout(120)
                    header = read_exact(client, 2)
                    if header[0] != 5:
                        return
                    methods = read_exact(client, header[1])
                    if require_auth:
                        if 2 not in methods:
                            client.sendall(b'\\x05\\xff')
                            log('socks5-auth-method-missing')
                            return
                        client.sendall(b'\\x05\\x02')
                        version = read_exact(client, 1)[0]
                        username = read_exact(client, read_exact(client, 1)[0]).decode('utf-8')
                        password = read_exact(client, read_exact(client, 1)[0]).decode('utf-8')
                        if version != 1 or username != auth_user or password != auth_pass:
                            client.sendall(b'\\x01\\x01')
                            log('socks5-auth-failed')
                            return
                        client.sendall(b'\\x01\\x00')
                        log('socks5-auth-ok')
                    else:
                        if 0 not in methods:
                            client.sendall(b'\\x05\\xff')
                            return
                        client.sendall(b'\\x05\\x00')
                    request = read_exact(client, 4)
                    address_type = request[3]
                    if address_type == 1:
                        host = socket.inet_ntoa(read_exact(client, 4))
                        log('socks5-target-ip')
                    elif address_type == 3:
                        host = read_exact(client, read_exact(client, 1)[0]).decode('utf-8')
                        log('socks5-target-domain')
                    elif address_type == 4:
                        host = socket.inet_ntop(socket.AF_INET6, read_exact(client, 16))
                        log('socks5-target-ip')
                    else:
                        client.sendall(b'\\x05\\x08\\x00\\x01\\x00\\x00\\x00\\x00\\x00\\x00')
                        return
                    port = struct.unpack('!H', read_exact(client, 2))[0]
                    upstream = socket.create_connection((host, port), timeout=20)
                    upstream.settimeout(120)
                    client.sendall(b'\\x05\\x00\\x00\\x01\\x00\\x00\\x00\\x00\\x00\\x00')
                    log('socks5-connect-auth' if require_auth else 'socks5-connect')
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

            def serve(server, handler, require_auth):
                while True:
                    try:
                        client, _ = server.accept()
                    except Exception:
                        return
                    threading.Thread(target=handler, args=(client, require_auth), daemon=True).start()

            def new_server():
                server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
                server.bind(('0.0.0.0', 0))
                server.listen(32)
                return server

            http_server = new_server()
            socks_server = new_server()
            http_auth_server = new_server()
            socks_auth_server = new_server()

            with open(base + '/proxy.ports', 'w', encoding='ascii') as stream:
                stream.write(str(http_server.getsockname()[1]) + '\\n')
                stream.write(str(socks_server.getsockname()[1]) + '\\n')
                stream.write(str(http_auth_server.getsockname()[1]) + '\\n')
                stream.write(str(socks_auth_server.getsockname()[1]) + '\\n')

            threading.Thread(target=serve, args=(http_server, http_handler, False), daemon=True).start()
            threading.Thread(target=serve, args=(socks_server, socks_handler, False), daemon=True).start()
            threading.Thread(target=serve, args=(http_auth_server, http_handler, True), daemon=True).start()
            threading.Thread(target=serve, args=(socks_auth_server, socks_handler, True), daemon=True).start()
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
        String proxyUser = "u" + UUID.randomUUID().toString().replace("-", "");
        String proxyPassword = "p" + UUID.randomUUID().toString().replace("-", "");
        ShellSSHClient admin = new ShellSSHClient(host.newConnect());
        admin.setHostKeyConfirmCallback(message -> true);
        String pid = null;
        try {
            admin.setVerifyFailureCallback(current -> null);
            admin.start(15000);
            String script = Base64.getEncoder().encodeToString(PROXY_SCRIPT.getBytes(StandardCharsets.UTF_8));
            admin.exec("mkdir -p " + quote(directory));
            admin.exec("printf '%s' " + quote(script) + " | base64 -d > " + quote(directory + "/proxy.py"));
            admin.exec("cd " + quote(directory) + "; nohup python3 proxy.py . "
                    + quote(proxyUser) + " " + quote(proxyPassword)
                    + " </dev/null >proxy.log 2>&1 &");

            String ports = awaitPorts(admin, directory);
            pid = requirePid(admin.exec("cat " + quote(directory + "/proxy.pid") + " 2>/dev/null || true"));
            String[] lines = ports.lines().filter(line -> !line.isBlank()).toArray(String[]::new);
            require(lines.length >= 4, "proxy ports file is incomplete");
            int httpPort = Integer.parseInt(lines[0].trim());
            int socksPort = Integer.parseInt(lines[1].trim());
            int httpAuthPort = Integer.parseInt(lines[2].trim());
            int socksAuthPort = Integer.parseInt(lines[3].trim());
            System.out.println("Temporary proxy endpoints ready: http=" + httpPort
                    + ", socks5=" + socksPort + ", httpAuth=" + httpAuthPort
                    + ", socks5Auth=" + socksAuthPort);

            requireProxy(admin, host, "http", httpPort, false, host.host(), null, null);
            requireProxy(admin, host, "socks5", socksPort, false, host.host(), null, null);
            requireProxy(admin, host, "http", httpAuthPort, true, "localhost", proxyUser, proxyPassword);
            requireProxy(admin, host, "socks5", socksAuthPort, true, "localhost", proxyUser, proxyPassword);
            requireProxyAuthFailure(admin, host, "http", httpAuthPort, proxyUser, proxyPassword + "-bad");
            requireProxyAuthFailure(admin, host, "socks5", socksAuthPort, proxyUser, proxyPassword + "-bad");
            require(count(admin, directory, "http-connect") >= 1, "HTTP proxy was not used");
            require(count(admin, directory, "socks5-connect") >= 1, "SOCKS5 proxy was not used");
            require(count(admin, directory, "http-connect-auth") >= 1, "authenticated HTTP proxy was not used");
            require(count(admin, directory, "socks5-connect-auth") >= 1,
                    "authenticated SOCKS5 proxy was not used");
            require(count(admin, directory, "http-auth-failed") >= 1, "HTTP auth failure was not enforced");
            require(count(admin, directory, "socks5-auth-failed") >= 1, "SOCKS5 auth failure was not enforced");
            require(count(admin, directory, "http-target-domain") >= 1, "HTTP proxy did not receive a domain target");
            require(count(admin, directory, "socks5-target-domain") >= 1,
                    "SOCKS5 proxy did not receive a domain target");
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
            if (ports != null && ports.lines().filter(line -> !line.isBlank()).count() >= 4) {
                return ports;
            }
            Thread.sleep(200);
        }
        String log = admin.exec("tail -n 20 " + quote(directory + "/proxy.log") + " 2>/dev/null || true");
        throw new IllegalStateException("temporary proxy did not start: " + log);
    }

    private static void requireProxy(ShellSSHClient admin, ShellJSchTestHosts.Host host, String protocol,
                                     int port, boolean auth, String targetHost,
                                     String proxyUser, String proxyPassword) throws Exception {
        ShellConnect connect = host.newConnect();
        connect.setHost(targetHost + ":" + host.port());
        ShellProxyConfig proxy = new ShellProxyConfig();
        proxy.setProtocol(protocol);
        proxy.setHost(host.host());
        proxy.setPort(port);
        if (auth) {
            proxy.setAuthType("password");
            proxy.setUser(proxyUser);
            proxy.setPassword(proxyPassword);
        }
        connect.setEnableProxy(true);
        connect.setProxyConfig(proxy);

        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> null);
            client.setHostKeyConfirmCallback(message -> true);
            client.start(15000);
            require(client.getSession() != null && client.getSession().isConnected(),
                    protocol + " proxy session is not connected");
            String marker = "proxy-" + protocol + "-" + UUID.randomUUID();
            require(marker.equals(client.exec("printf " + quote(marker))),
                    protocol + " proxy exec output mismatch");
            requireSftp(client, protocol);
            System.out.println(protocol + " proxy SSH/SFTP verified, auth=" + auth);
        }
    }

    private static void requireProxyAuthFailure(ShellSSHClient admin, ShellJSchTestHosts.Host host,
                                                String protocol, int port, String proxyUser,
                                                String proxyPassword) throws Exception {
        ShellConnect connect = host.newConnect();
        connect.setHost("localhost:" + host.port());
        ShellProxyConfig proxy = new ShellProxyConfig();
        proxy.setProtocol(protocol);
        proxy.setHost(host.host());
        proxy.setPort(port);
        proxy.setAuthType("password");
        proxy.setUser(proxyUser);
        proxy.setPassword(proxyPassword);
        connect.setEnableProxy(true);
        connect.setProxyConfig(proxy);

        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            AtomicInteger authCallbacks = new AtomicInteger();
            client.setVerifyFailureCallback(current -> {
                authCallbacks.incrementAndGet();
                return null;
            });
            client.setHostKeyConfirmCallback(message -> true);
            boolean rejected = false;
            try {
                client.start(15000);
            } catch (RuntimeException ex) {
                rejected = true;
            }
            require(rejected, protocol + " proxy accepted invalid credentials");
            require(!client.isConnected(), protocol + " proxy invalid credentials left a connected session");
            require(authCallbacks.get() == 0,
                    protocol + " proxy failure incorrectly triggered SSH authentication callback");
            System.out.println(protocol + " proxy invalid credentials rejected");
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
