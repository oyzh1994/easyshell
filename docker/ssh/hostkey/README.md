# JSch 主机密钥回归

本目录提供隔离的 known_hosts 回归入口，验证 JSch 版 EasyShell 在未知主机、
已保存主机和服务端换钥场景下的行为。测试只使用临时容器和临时 known_hosts，
不会修改用户正式配置。

## 前置条件

- 本机 Docker 可用。
- 已执行 `mvn -q -DskipTests test-compile`。
- 优先使用 `target/jsch-isolation/easyshell-jsch.cp` 和
  `target/jsch-isolation/base-jsch.cp` 作为隔离依赖；脚本会过滤 sshd/sshj/JGit
  并在需要时按 `base-sources.txt` 编译隔离的 `base-ssh` JSch 类。若隔离依赖文件
  不存在，才回退到 `easyshell-full.cp`。
- 测试密码仅通过环境变量传入，不要写入脚本、文档或提交记录。

## 运行

```bash
export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
EASYSHELL_HOSTKEY_PASSWORD='<local-test-password>' \
  bash docker/ssh/hostkey/test-host-key.sh
```

默认容器名为 `easyshell-ssh-hostkey-test`，宿主机端口为 `2225`。可通过
`EASYSHELL_HOSTKEY_CONTAINER`、`EASYSHELL_HOSTKEY_PORT`、
`EASYSHELL_HOSTKEY_USER` 和 `EASYSHELL_HOSTKEY_IMAGE` 覆盖。

## 测试矩阵

| 阶段 | 服务端状态 | 期望行为 |
| --- | --- | --- |
| `reject-new` | 未知主机 | 拒绝，known_hosts 不变 |
| `accept-new` | 未知主机 | 接受一次，写入 known_hosts |
| `persist` | 已保存主机 | 不提示，Exec 正常 |
| `reject-changed` | host key 变化 | 拒绝，known_hosts 不变 |
| `accept-changed` | host key 变化 | 接受，更新 known_hosts |
| `persist-changed` | 已更新主机 | 不提示，重连正常 |

脚本在换钥阶段删除容器内 `/etc/ssh/ssh_host_*` 并重新生成 key，随后重启
sshd。退出时无论成功或失败都会删除测试容器和临时 known_hosts。

成功输出现象：

```text
JSch host key regression passed
```
