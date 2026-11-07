# Valkey Docker 测试环境

镜像：`valkey/valkey:latest`
容器内命令：`valkey-server`、`valkey-sentinel`、`valkey-cli`

###### 启动单个 Valkey

```bash
docker run -itd --name valkey -p 6379:6379 valkey/valkey:latest
```

带密码：

```bash
docker run -itd --name valkey -p 6379:6379 valkey/valkey:latest valkey-server --requirepass valkey@2026
```

###### 示例目录

- `example/`：单节点，带密码
- `auth/`：ACL 用户认证
- `master/`：主从 + 哨兵
- `cluster/`：6 节点集群 + 3 个哨兵
- `ssl/`：TLS/SSL 加密连接

###### 启动方式

```bash
# 进入对应示例目录后启动，例如主从示例
cd docker/valkey/master

# macOS / Linux
docker compose -f valkey-master-compose.yml up -d

# Windows
docker-compose -f valkey-master-compose.yml up -d
```

###### 说明

- 配置文件中的 `192.168.3.132`、`192.168.3.4` 需要替换为宿主机实际 IP。
- 主从配置使用 Valkey 的 `replicaof`，不要写 `127.0.0.1`，否则从节点会连接到容器自身。
- `ssl/` 中的 CA、服务端证书和私钥由 `openssl` 生成，测试 CA 为 `ssl/ca.crt`，服务端证书为 `ssl/valkey.crt`，私钥为 `ssl/valkey.key`，DH 参数为 `ssl/valkey.dh`。
- `master/`、`cluster/`、`auth/`、`ssl/` 示例默认端口与 Redis 示例相同，不要同时启动 Redis 和 Valkey 的同一组示例。
