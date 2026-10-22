# JSch 旧算法兼容回归

本目录使用临时 OpenSSH 容器验证 JSch 版 EasyShell 对旧服务端算法的最小兼容性。
服务端只开放：

- KEX：`diffie-hellman-group14-sha1`
- 主机密钥：`ssh-rsa`
- 公钥认证：`ssh-rsa`

密码认证下执行直连 SSH/Exec/SFTP，并单独验证跳板会话的算法降级回退和本地
转发监听。另可用真实现代跳板主机连接到该旧算法目标，验证主会话降级。测试
密码仅通过环境变量传入。

```bash
export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
EASYSHELL_LEGACY_PASSWORD='<local-test-password>' \
  bash docker/ssh/legacy/test-legacy-algorithms.sh
```

默认宿主机端口为 `2226`，容器名为 `easyshell-ssh-legacy-test`。可通过
`EASYSHELL_LEGACY_PORT`、`EASYSHELL_LEGACY_CONTAINER`、
`EASYSHELL_LEGACY_USER` 和 `EASYSHELL_LEGACY_IMAGE` 覆盖。退出时会删除容器和
临时 known_hosts。

成功输出现象：

```text
JSch legacy algorithm regression passed
```
