#!/usr/bin/env python3
import argparse
import base64
import socket
import statistics
import struct
import time


SCENARIOS = [
    ("http-open", "http", 31280, None, None),
    ("http-auth", "http", 31281, "proxyuser", "proxy-pass"),
    ("socks-open", "socks5", 31282, None, None),
    ("socks-auth", "socks5", 31283, "proxyuser", "proxy-pass"),
]


def decode_hex(value):
    if not value:
        return b""
    return bytes.fromhex(value.replace(" ", "").replace(":", "").replace("-", ""))


def read_exact(sock, size):
    data = b""
    while len(data) < size:
        chunk = sock.recv(size - len(data))
        if not chunk:
            raise RuntimeError("target closed before the expected response was complete")
        data += chunk
    return data


def verify_payload(data, expected, expected_prefix):
    if expected and data != expected:
        raise RuntimeError(f"response mismatch: got {data.hex()}, expected {expected.hex()}")
    if expected_prefix and not data.startswith(expected_prefix):
        raise RuntimeError(f"response prefix mismatch: got {data.hex()}, expected {expected_prefix.hex()}...")


def exchange(sock, args):
    if args.send_payload:
        sock.sendall(args.send_payload)
    if args.read_bytes:
        response = read_exact(sock, args.read_bytes)
        verify_payload(response, args.expected, args.expected_prefix)


def read_http_response(sock):
    data = b""
    while b"\r\n\r\n" not in data:
        chunk = sock.recv(1)
        if not chunk:
            raise RuntimeError("HTTP proxy closed before headers")
        data += chunk
        if len(data) > 32 * 1024:
            raise RuntimeError("HTTP proxy response is too large")
    return data.split(b"\r\n", 1)[0].decode("ascii", "replace")


def probe_http(port, target_host, target_port, user, password, args):
    with socket.create_connection(("127.0.0.1", port), timeout=5) as sock:
        authority = target_host if ":" not in target_host else f"[{target_host}]"
        request = (
            f"CONNECT {authority}:{target_port} HTTP/1.1\r\n"
            f"Host: {authority}:{target_port}\r\n"
        )
        if user is not None:
            token = base64.b64encode(f"{user}:{password or ''}".encode()).decode()
            request += f"Proxy-Authorization: Basic {token}\r\n"
        sock.sendall((request + "\r\n").encode("ascii"))
        status = read_http_response(sock)
        code = int(status.split()[1])
        if code < 200 or code >= 300:
            return False, status
        exchange(sock, args)
        return True, status


def probe_socks5(port, target_host, target_port, user, password, args):
    with socket.create_connection(("127.0.0.1", port), timeout=5) as sock:
        methods = [0, 2] if user is not None else [0]
        sock.sendall(bytes([5, len(methods), *methods]))
        version, method = read_exact(sock, 2)
        if version != 5:
            raise RuntimeError("invalid SOCKS version")
        if method == 0xFF:
            return False, "no acceptable authentication method"
        if user is not None:
            if method != 2:
                return False, f"unexpected auth method {method}"
            user_bytes = user.encode()
            password_bytes = (password or "").encode()
            sock.sendall(bytes([1, len(user_bytes)]) + user_bytes + bytes([len(password_bytes)]) + password_bytes)
            auth_version, status = read_exact(sock, 2)
            if auth_version != 1 or status != 0:
                return False, "username/password authentication failed"
        elif method != 0:
            return False, f"unexpected auth method {method}"

        if ":" in target_host and "." not in target_host:
            address = bytes([4]) + socket.inet_pton(socket.AF_INET6, target_host)
        elif all(part.isdigit() for part in target_host.split(".")):
            address = bytes([1]) + socket.inet_aton(target_host)
        else:
            host_bytes = target_host.encode()
            address = bytes([3, len(host_bytes)]) + host_bytes
        sock.sendall(bytes([5, 1, 0]) + address + struct.pack("!H", target_port))
        version, reply, _, address_type = read_exact(sock, 4)
        if version != 5 or reply != 0:
            return False, f"SOCKS connect reply {reply}"
        if address_type == 1:
            read_exact(sock, 4)
        elif address_type == 4:
            read_exact(sock, 16)
        elif address_type == 3:
            read_exact(sock, read_exact(sock, 1)[0])
        else:
            raise RuntimeError("invalid SOCKS bind address")
        read_exact(sock, 2)
        exchange(sock, args)
        return True, "SOCKS5 CONNECT succeeded"


def run_once(scenario, args):
    name, protocol, proxy_port, user, password = scenario
    started = time.perf_counter()
    try:
        if protocol == "http":
            ok, detail = probe_http(proxy_port, args.host, args.port, user, password, args)
        else:
            ok, detail = probe_socks5(proxy_port, args.host, args.port, user, password, args)
    except Exception as ex:
        ok, detail = False, f"{type(ex).__name__}: {ex}"
    return name, ok, detail, (time.perf_counter() - started) * 1000


def main():
    parser = argparse.ArgumentParser(description="Protocol-neutral HTTP CONNECT/SOCKS5 TCP proxy test")
    parser.add_argument("--host", required=True)
    parser.add_argument("--port", type=int, required=True)
    parser.add_argument("--send-hex", default="")
    parser.add_argument("--read-bytes", type=int)
    parser.add_argument("--expect-hex", default="")
    parser.add_argument("--expect-prefix-hex", default="")
    parser.add_argument("--benchmark", action="store_true")
    parser.add_argument("--warmup", type=int, default=3)
    parser.add_argument("--iterations", type=int, default=20)
    args = parser.parse_args()
    args.send_payload = decode_hex(args.send_hex)
    args.expected = decode_hex(args.expect_hex)
    args.expected_prefix = decode_hex(args.expect_prefix_hex)
    if args.read_bytes is None:
        args.read_bytes = max(len(args.expected), len(args.expected_prefix))

    failed = False
    for scenario in SCENARIOS:
        name, ok, detail, elapsed = run_once(scenario, args)
        print(f"[{'PASS' if ok else 'FAIL'}] {name}: {detail} ({elapsed:.1f} ms)")
        failed |= not ok

    if args.benchmark:
        scenarios = [("direct", "direct", 0, None, None), *SCENARIOS]
        for scenario in scenarios:
            name = scenario[0]
            for _ in range(args.warmup):
                if scenario[1] == "direct":
                    with socket.create_connection((args.host, args.port), timeout=5) as sock:
                        exchange(sock, args)
                else:
                    run_once(scenario, args)
            samples = []
            for _ in range(args.iterations):
                started = time.perf_counter()
                if scenario[1] == "direct":
                    with socket.create_connection((args.host, args.port), timeout=5) as sock:
                        exchange(sock, args)
                    ok, detail = True, "direct TCP succeeded"
                else:
                    _, ok, detail, _ = run_once(scenario, args)
                if not ok:
                    print(f"[FAIL] benchmark {name}: {detail}")
                    failed = True
                else:
                    samples.append((time.perf_counter() - started) * 1000)
            if samples:
                ordered = sorted(samples)
                p95 = ordered[min(len(ordered) - 1, int(len(ordered) * 0.95))]
                print(
                    f"[BENCH] {name}: min={min(samples):.1f} ms "
                    f"avg={statistics.mean(samples):.1f} ms p95={p95:.1f} ms "
                    f"max={max(samples):.1f} ms"
                )

    raise SystemExit(1 if failed else 0)


if __name__ == "__main__":
    main()
