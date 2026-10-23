# TCP 代理 Docker 复测环境

本目录提供与应用协议无关的 HTTP CONNECT / SOCKS5 TCP 代理服务，可供 SSH、Redis、数据库及其他 TCP 客户端复测。四组容器启动后不需要清理，便于人工复测。

## 服务端口

| 服务 | 宿主端口 | 协议 | 认证 |
|---|---:|---|---|
| `http-open` | `31280` | HTTP CONNECT | 无 |
| `http-auth` | `31281` | HTTP CONNECT | Basic：`proxyuser` / `proxy-pass` |
| `socks-open` | `31282` | SOCKS5 | 无 |
| `socks-auth` | `31283` | SOCKS5 | 用户名密码：`proxyuser` / `proxy-pass` |

可通过 `HTTP_OPEN_PORT`、`HTTP_AUTH_PORT`、`SOCKS_OPEN_PORT`、`SOCKS_AUTH_PORT`、`PROXY_USER`、`PROXY_PASSWORD` 覆盖默认值。

## 启动四组容器

```bash
cd docker/ssh/proxy
docker compose up -d --build
docker compose ps
```

## 协议无关测试

测试工具只处理 TCP 字节流；协议请求和响应通过十六进制参数传入，不在本目录保存任何应用协议信息。

```bash
python3 test_tcp_proxy.py --host 127.0.0.1 --port 22
python3 test_tcp_proxy.py --host 127.0.0.1 --port 6379 \
  --send-hex "2a310d0a24340d0a50494e470d0a" \
  --read-bytes 5 --expect-hex "2b504f4e470d"
```

支持 `--send-hex`、`--read-bytes`、`--expect-hex`、`--expect-prefix-hex` 和 `--benchmark`。应用层测试可将协议探针作为运行参数传入。

## 自动测试与基准

```bash
TARGET_HOST=127.0.0.1 TARGET_PORT=22 BENCHMARK=1 ./run-tests.sh
```

需要验证应用数据时，可同时设置 `PROBE_SEND_HEX`、`PROBE_READ_BYTES`、`PROBE_EXPECT_HEX` 或 `PROBE_EXPECT_PREFIX_HEX`。

## 日志与清理

```bash
docker compose logs -f http-open http-auth socks-open socks-auth
docker compose down
```
