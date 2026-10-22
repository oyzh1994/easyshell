# EasyShell SSH Agent Authentication Test

该目录用于验证 EasyShell JSch 的 `ssh-agent` 公钥认证。

测试流程：

1. 启动本机 Docker SSH 主机 `easyshell-ssh-agent-test`，映射到 `127.0.0.1:2222`。
2. 启动独立的本机 `ssh-agent`，生成临时 Ed25519 密钥并加入 Agent。
3. 删除临时私钥文件，确保认证只能通过 Agent 完成。
4. 使用密码会话临时向主机 `~/.ssh/authorized_keys` 授权对应公钥。
5. 使用 `ShellConnect.authMethod=sshAgent` 验证连续连接、连接复用、SFTP、
   并发连接和未授权密钥拒绝。
6. 结束时恢复原 `authorized_keys` 并关闭测试 Agent。

先完成测试编译，再运行：

```bash
/Users/oyzh/.m2/wrapper/dists/apache-maven-3.9.10-bin/53h08a94dg6djh6umvru7q564/apache-maven-3.9.10/bin/mvn -q -DskipTests test-compile
bash docker/ssh/agent/test-agent.sh
```

可通过 `EASYSHELL_AGENT_PORT`、`EASYSHELL_AGENT_USER`、
`EASYSHELL_AGENT_PASSWORD` 修改本地测试主机参数。测试结束后 Docker 主机保留，
独立 Agent 和临时密钥会清理。

最近一次本地验证结果：

- Agent 内 2 把 Ed25519 密钥（1 把未授权干扰密钥、1 把已授权密钥）。
- 私钥文件删除后，连续 8 次连接、单连接复用 5 条命令、SFTP、6 路并发均通过。
- 未授权 Agent 密钥约 98 ms 拒绝，`authorized_keys` 已恢复并清理。
