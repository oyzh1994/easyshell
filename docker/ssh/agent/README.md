# EasyShell SSH Agent Authentication and Forwarding Test

该目录用于验证 EasyShell JSch 的 `ssh-agent` 公钥认证、Agent 转发和 SSH
环境变量注入。

## Agent 认证

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
EASYSHELL_AGENT_PASSWORD='<local-test-password>' bash docker/ssh/agent/test-agent.sh
```

可通过 `EASYSHELL_AGENT_PORT`、`EASYSHELL_AGENT_USER`、
`EASYSHELL_AGENT_PASSWORD` 修改本地测试主机参数。测试密码不设默认值，必须由
调用者通过环境变量提供。测试结束后 Docker 主机保留，独立 Agent 和临时密钥会清理。

最近一次本地验证结果：

- Agent 内 2 把 Ed25519 密钥（1 把未授权干扰密钥、1 把已授权密钥）。
- 私钥文件删除后，连续 8 次连接、单连接复用 5 条命令、SFTP、6 路并发均通过。
- 未授权 Agent 密钥约 101 ms 拒绝，`authorized_keys` 已恢复并清理。

## Agent 转发与 SSH 环境变量

`test-agent-forwarding.sh` 创建双机 Docker 链路验证 `ShellConnect.forwardAgent`
和 `ShellConnect.environment`：

- 目标机 `easyshell-ssh-agent-test` 映射 `127.0.0.1:2222`，跳板机
  `easyshell-ssh-agent-jump` 映射 `127.0.0.1:2223`。
- 两台主机加入同一 Docker 网络，目标机在跳板机侧可通过别名 `target` 访问。
- 本机独立 `ssh-agent` 放入已授权密钥和一把未授权干扰密钥，`ssh-add` 后
  删除私钥文件。
- 先验证“外层密码认证 + `forwardAgent`”：此时 Agent 公钥尚未授权给跳板机，
  跳板机仍通过转发的 `SSH_AUTH_SOCK` 执行内层 `ssh target`。
- 再授权跳板机并验证“外层 Agent 认证 + `forwardAgent`”链路。
- Exec 和 Shell 验证环境变量、转发 socket 与 Agent 身份；内层 SSH 连续执行
  3 次并验证 `SendEnv`/`AcceptEnv` 环境传递。
- 关闭 `forwardAgent` 后再执行内层 SSH，验证 `SSH_AUTH_SOCK=none` 且认证失败。
- 测试结束恢复或删除两台主机的 `authorized_keys`，关闭独立 Agent。

服务端需要开启 `PubkeyAuthentication`、`AllowAgentForwarding`，并配置
`AcceptEnv EASYSHELL_TEST_*`；脚本会自动设置并重启测试容器。测试密码必须通过
`EASYSHELL_FORWARD_PASSWORD` 提供，脚本和文档不保存本地测试凭据。

先编译测试代码，再运行：

```bash
/Users/oyzh/.m2/wrapper/dists/apache-maven-3.9.10-bin/53h08a94dg6djh6umvruv7q564/apache-maven-3.9.10/bin/mvn -q -DskipTests test-compile
EASYSHELL_FORWARD_PASSWORD='<local-test-password>' bash docker/ssh/agent/test-agent-forwarding.sh
```

最近一次验证结果：

- Exec 两个环境变量、转发 `SSH_AUTH_SOCK` 和 Ed25519 身份均通过。
- 密码认证外层和 Agent 认证外层均通过，内层 SSH 各 3/3 成功。
- 密码场景中跳板机未授权 Agent 公钥，证明转发不依赖外层 Agent 认证。
- Shell 环境变量、转发 socket 和 Agent 身份均通过。
- 关闭转发后内层认证被拒绝，`SSH_AUTH_SOCK=none`。
- 两台主机临时 `authorized_keys` 已清理，测试 Agent 进程和临时目录已清理。
