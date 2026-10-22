# EasyShell SSH / SSH2 / SFTP JSch 迁移审查报告

## 1. 审查结论

本次审查确认：EasyShell 活跃的 `ssh`、`sftp` 调用链已经切换为 JSch 2.28.8，MINA sshd 类型不再出现在活跃源码、隔离 classpath 或隔离字节码中。审查中发现并修复了 SFTP 通道池死锁、通道泄漏、Agent/X11 条件错误、跳板配置过滤错误、SFTP 文件对象销毁不完整等确定性问题。Docker 专项审查还修复了资源更新参数、history 输出解析、参数保护、异常输出容错和长操作超时问题，并完成了真实容器与镜像生命周期回归。

构建和回归结论：

- `base-ssh` 与 `easyshell` 的 Maven 编译通过。
- 活跃 JSch 调用链在不包含 MINA sshd/JGit 的隔离 classpath 下编译通过。
- 真实 Linux 主机回归通过，包含连接、认证、终端、Exec、SFTP、端口转发、跳板、Docker 完整生命周期、Process、Server、重连和并发。
- 密钥与 Agent 初始化回归通过。
- Docker 临时容器、镜像、卷和归档清理验证通过，主机无测试资源残留。
- 未修改 `ssh2`、`sftp2`、`ssh1`、`sftp1` 及 `base-ssh` 中的 sshd 版本文件。

## 2. 迁移方案

1. 以 `ShellTerminalApp2` 的连接方式和主机配置为集成入口参考，梳理 `ShellSSHClient -> ShellBaseSSHClient -> ShellSSHShell/Exec -> ShellSFTPClient` 的调用链。
2. 在 `cn.oyzh.ssh.jsch` 中建立 JSch 适配层，隔离密钥、动态转发、认证提示等 JSch 差异，不向业务层暴露 MINA 类型。
3. 按层替换：会话与认证、Shell/PTY、Exec、SFTP、端口转发、跳板、隧道、Agent、X11、Docker/Process/Server。
4. 保留现有公开 API 和配置字段；必须改变参数类型时，仅在 JSch 包内或对应 JSch 实现中完成。
5. 对资源生命周期使用明确归属：会话、通道、流、动态监听端口分别记录并在关闭路径统一释放。
6. 用隔离编译加真实主机回归验证“只保留 JSch 也能运行”，而不是仅验证当前完整依赖下的编译。

## 3. 功能对照表

| 功能 | 原 MINA sshd 实现 | JSch 实现 | 兼容结论 |
| --- | --- | --- | --- |
| SSH 连接 | `ClientSession` | `Session` | 已迁移，支持超时、keep-alive、重连 |
| 密码认证 | sshd password auth | JSch `UserInfo` / password | 已迁移 |
| 密钥认证 | sshd key identity | JSch `addIdentity` | 已迁移，支持路径和内嵌密钥 |
| Agent 认证 | sshd Agent 工厂 | JSch `AgentIdentityRepository` | 已迁移并完成初始化验证 |
| 交互认证 | sshd keyboard-interactive | JSch `UIKeyboardInteractive` | 已迁移 |
| Known hosts | sshd verifier | JSch `known_hosts` | 已迁移，跳板保持旧行为不校验 |
| 压缩 | sshd compression | JSch compression config | 已迁移，未做长时压测 |
| Shell / PTY | `ChannelShell` | JSch `ChannelShell` | 已验证交互、PTY resize、关闭重开 |
| Exec | `ChannelExec` | JSch `ChannelExec` | 已验证并发、超时和超时后恢复 |
| SFTP 文件操作 | `SftpClient` | JSch `ChannelSftp` | 已验证上传、下载、权限、重命名、递归删除 |
| SFTP 流操作 | sshd SFTP streams | JSch SFTP streams | 已验证进度回调和流关闭 |
| SFTP 通道池 | `Pool<SftpClient>` | 独立条件队列池 + JSch 通道 | 已修复原实现死锁，验证并发借还 |
| 本地转发 | `startLocalPortForwarding` | `setPortForwardingL` | 已验证 |
| 远程转发 | `startRemotePortForwarding` | `setPortForwardingR` | 已验证，含动态远端端口解析 |
| 动态转发 | sshd dynamic forwarding | SOCKS4/SOCKS5 + `ChannelDirectTCPIP` | 已验证无认证 SOCKS4/5 和半关闭 |
| 跳板 | 多段转发 | 多段 `Session` + 本地端口 | 已验证单级和多级跳板 |
| Docker | Exec 命令层 | Exec 命令层 + 容器/镜像生命周期 | 已验证 info/version、run、ps、exec、logs、资源更新、pause/unpause、rename、restart、start/stop/kill、port、commit、tag、save、rm/rmi；全局 prune 和 Docker 服务重启未执行 |
| Process | Exec + 解析 | Exec + 解析 | 已验证并发采集和网络速率解析 |
| Server | Exec + 解析 | Exec + 解析 | 已验证信息与监控并发 |
| X11 | sshd X11 channel | JSch X11 session/channel 配置 | 已迁移，未做真实 X11 端到端验证 |
| 异常处理 | sshd exception | `ShellException` / `SSHException` 包装 | 已迁移，增加可恢复通道错误重试 |

## 4. 审查发现与修复

### 4.1 SFTP 通道池确定性死锁（严重）

原实现中 `borrowObject()` 和 `returnObject()` 都持有池对象锁，借出线程在池空时等待，而归还线程无法取得同一把锁，存在确定性死锁。

修复：`ShellSFTPChannelPool` 改为 `ReentrantLock + Condition + 空闲队列 + 创建计数`。创建通道时释放锁，归还或销毁时唤醒等待者，关闭池时唤醒全部等待线程并关闭空闲和借出通道。

### 4.2 SFTP 通道泄漏（严重）

发现两类泄漏：

- `fileInfo()` 通过工具方法创建池外通道并以 `try-with-resources` 直接关闭，绕过池借用和归还。
- `put/get` 直接调用 `newSFTPChannel()`，随后把通道交给 `returnObject()`；该通道不在借出集合中，归还被忽略，造成悬空通道。

修复：`fileInfo()` 和文件传输改为 `takeChannel()/returnChannel()`；流式传输仍保留独立通道，并由延迟资源队列关闭。新增 100 次 `fileInfo` 池稳定性断言和 16 线程借还压力测试。

### 4.3 Agent 转发条件错误

原 JSch 代码以 `isJumpForward()` 判断 Agent 转发，该字段只表示存在跳板配置。修复为 `isForwardAgent()`，与 sshd 版本语义一致。

### 4.4 X11 启动语义不一致

原 JSch 代码只判断 `x11Config.isLocal()`，sshd 版本使用 `ShellX11Manager.isBuilt_in(x11Config)`。修复为相同判断并调用内建 X11 服务启动方法。

### 4.5 SFTP 文件对象销毁不完整

`ShellSFTPFile.destroy()` 原实现未解绑权限属性，也未清理 owner、group、fileName、parentPath。已恢复 sshd 版本的全部释放语义。

### 4.6 SFTP 通道创建缺少有界重试

新增 `newSFTPChannel(int maxRetry)`，仅对会话断开、通道未打开、连接重置等可恢复错误重试，默认 3 次并带退避；认证错误和客户端已关闭错误不重试。

### 4.7 跳板配置过滤错误

`ShellSSHClient` 原使用 `getJumpConfigs()`，会把已禁用的跳板配置也带入多跳链路。修复为 `getEnableJumpConfigs()`。

### 4.8 SFTP 兼容辅助 API 缺失

补充 `ShellSFTPUtil.formatPermissions(SftpATTRS)`、`parsePermissions1(String)`、`getFileType(SftpATTRS)`，保持旧工具方法名称和权限字符串语义。

### 4.9 动态转发 SOCKS 响应字段不准确

SSH `direct-tcpip` 无法提供远端 BND.ADDR/BND.PORT。成功响应改为规范允许的 `0.0.0.0:0` 占位，避免返回客户端本地地址造成误解。

### 4.10 会话可见性

`ShellBaseSSHClient.session` 改为 `volatile`，减少重连线程与其他读取线程之间的可见性风险。

### 4.11 Docker 资源更新参数错误

原 `docker_update()` 使用 `>= 0` 判断字段，默认 `ShellDockerResource` 的零值会生成 `--memory 0m`、`--cpu-shares 0`、`--cpus 0` 等无效参数；同时把 inspect 返回的 `NanoCpus` 纳秒值直接传给 `--cpus`，单位不匹配。修复为：

- 仅对大于零的字段生成更新参数，默认零值不再污染命令。
- `ShellDockerResource.nanoCpus` 统一保持 inspect/API 的纳秒语义，生成 CLI 参数时转换为十进制核数。
- `ShellDockerResourceController` 显示时保留小数核数，保存时转换回纳秒，避免 `1.5` 核被截断。
- Docker 不允许 `--cpus` 与 `--cpu-period/--cpu-quota` 同时设置；两者同时存在时优先使用 `NanoCpus`，否则使用 period/quota。

### 4.12 Docker 输出解析与参数保护

- `docker history` 原来使用 `\r\t` 作为分隔符，但 Docker 会把它按普通文本输出，导致真实历史记录无法解析。现改用不可见 Unit Separator，并保留对旧 `\r\t` 文本格式的兼容。
- `ps`、`images`、`port`、`history` 解析器跳过空行、错误输出和列数不足的行；`resource` 对非法数字回退为 0 并记录告警，避免远端 Docker 异常输出导致数组越界或 `NumberFormatException`。
- `docker commit` 备注、`docker save` 路径，以及 `docker run` 的容器名、镜像、环境变量、标签和卷参数统一做 shell 参数保护；测试中包含空格的环境值、标签值和保存路径已验证通过。
- `docker run` 首次拉取镜像、`docker commit` 和 `docker save` 分别使用 300 秒、120 秒和 300 秒超时，避免大镜像或慢速主机被默认连接超时误中断。

## 5. 兼容性说明

- 公开业务类名、连接配置字段、状态枚举和主要方法保持不变。
- 业务层不再出现 `org.apache.sshd` 或 `org.eclipse.jgit` 类型。
- `ShellSFTPUtil` 的权限方法参数从 sshd 属性类型改为 JSch `SftpATTRS`，这是移除 MINA 类型依赖的必要变化；旧方法名已保留。
- `ShellSFTPChannelPool` 继续继承原 `Pool` 类型以降低外部类型兼容风险，但借还逻辑由自身实现。
- JSch 依赖由 test scope 调整为 compile scope，`base-ssh` 中 JSch 依赖也恢复为编译依赖。
- `ssh2`、`sftp2`、`ssh1`、`sftp1` 未改动，仍保留原 sshd 实现，供后续由项目维护者统一移除。
- Docker 公开方法名、参数对象和返回类型保持不变；`ShellDockerResource.nanoCpus` 明确采用纳秒单位，与 `docker inspect` 和 `docker_resource()` 保持一致。
- Docker history 的新分隔符属于内部命令格式，解析器同时兼容旧 `\r\t` 输出，不影响既有调用方。
- Docker 参数保护只改变远程 shell 的转义方式，不改变 Docker 参数顺序和业务含义；`docker run` 的自由文本 `params` 仍按原始 Docker CLI 选项处理。

## 6. 风险与功能差异

1. JSch 与 MINA sshd 的默认 KEX、MAC、主机密钥算法集合不同，旧服务器或弱算法环境可能需要额外算法配置。
2. 动态转发目前只支持无认证 SOCKS4/SOCKS5；不支持 SOCKS5 用户名密码、UDP ASSOCIATE 和 BIND。
3. JSch 的 X11、压缩、代理连接与 sshd 的实现细节不同，当前未做完整端到端验证。
4. 跳板链路按每个跳板节点自身认证建立，主连接代理配置不会自动套用到每个跳板节点。
5. SFTP 流式读写依赖调用方关闭流，并由客户端关闭延迟资源；长时间未关闭的流仍会占用通道。
6. Docker 自动化已在 Linux 主机完成真实生命周期验证，但未执行全局 `prune` 和 `systemctl restart docker`，避免影响主机上其他容器；Windows/macOS 远端 Docker、Docker Compose 插件模式仍未实机验证。
7. SFTP 通道池现在会关闭借出通道，因此客户端关闭属于强制终止语义，调用方不应在关闭后继续使用借出通道。
8. 客户端连接仍主要依赖服务端命令输出解析；不同发行版、BusyBox、Windows 和 macOS 的输出差异需要继续覆盖。
9. `docker run` 的 `params` 是面向高级用户的原始 CLI 片段，仍可能包含任意 Docker 选项；UI 调用方应将其视为可信输入。
10. `docker_update` 在 `NanoCpus` 和 period/quota 同时存在时优先使用 `NanoCpus`。需要显式 period/quota 时应将该资源对象中的 `nanoCpus` 置为 0。

## 7. 测试计划

### 7.1 编译验证

1. `base-ssh` 全量 Maven 安装编译。
2. `easyshell` Maven test-compile。
3. 使用不包含 MINA sshd/JGit 的 classpath 隔离编译 JSch 相关源码。
4. 检查活跃源码、隔离 classpath、生成字节码均无 sshd/JGit 引用。
5. 检查 sshd 版本目录无 diff。

### 7.2 自动回归

- 连接、认证、系统类型和 PATH 注入。
- Shell 交互、PTY resize、Shell 关闭重开。
- Exec 输出、并发执行、超时中止、超时后恢复。
- SFTP 文件上传下载、流上传下载、进度回调、权限、重命名、递归删除。
- SFTP 通道池复用、失效通道替换、并发借还、关闭唤醒、`fileInfo` 稳定性。
- 本地转发、远程转发、动态 SOCKS4/SOCKS5、SOCKS5 半关闭、畸形请求恢复。
- 单级和多级跳板。
- Docker info/version、容器 run/ps/exec/logs、资源更新、pause/unpause、rename、restart、start/stop/kill、端口、commit/tag/save/rm/rmi、解析器容错和资源清理。
- Process 信息、Server 信息和并发监控。
- 会话断开重连、隧道重建、SFTP 重连。
- 启动失败后的会话、通道和转发清理。
- 密钥生成、类型/长度解析、代理类型映射、Agent 初始化。

### 7.3 命令示例

凭据仅通过本地环境变量传入，不写入文档或源码：

```bash
export EASYSHELL_TEST_HOST='<local-test-host>'
export EASYSHELL_TEST_USER='<local-test-user>'
export EASYSHELL_TEST_PASSWORD='<local-test-password>'
```

```bash
cd /Users/oyzh/IdeaProjects/oyzh/base/base-ssh
mvn -q -DskipTests install

cd /Users/oyzh/IdeaProjects/oyzh/easyshell
mvn -q -DskipTests test-compile

java -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchKeyRegression
java -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchConcurrentRegression
java -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchDockerRegression
```

Docker 回归默认使用 `easyshell-jsch-test:local`。本机没有该镜像且无法访问 Docker Hub 时，测试会从主机已有的静态 `/usr/bin/busybox` 构建临时镜像；也可通过 `EASYSHELL_TEST_DOCKER_IMAGE` 指定已存在镜像。测试资源均带唯一后缀，`finally` 中删除容器、临时镜像、卷和归档。

### 7.4 人工验证项

- JavaFX `ShellTerminalApp2` 终端界面手工交互。
- Windows Pageant、OpenSSH Agent、macOS Agent 实机登录。
- 真实 HTTP/SOCKS4/SOCKS5 代理连接。
- X11 图形应用转发。
- 大文件、长时间空闲、弱网和多小时稳定性。
- Windows/macOS 远端 Docker、Docker Compose 插件模式。
- 全局 Docker prune、Docker 服务重启和宿主机级 Docker 故障恢复。

## 8. 验证结果

验证日期：2026-10-22。

| 验证项 | 结果 |
| --- | --- |
| `base-ssh` Maven `install` | 通过 |
| `easyshell` Maven `test-compile` | 通过 |
| 隔离编译 `base-ssh` JSch 子集（21 个源文件） | 通过 |
| 隔离编译 `easyshell` 活跃源码（2375 个源文件，排除 ssh2/sftp2） | 通过 |
| 隔离编译 JSch 测试入口 | 通过 |
| 活跃源码 sshd/JGit import 检查 | 0 处 |
| 隔离 classpath sshd/JGit 检查 | 0 个 |
| 隔离生成字节码 sshd/JGit 引用检查 | 0 处 |
| 密钥与 Agent 回归 | 通过 |
| 真实主机完整回归 | 通过 |
| Docker 真实容器/镜像生命周期回归 | 通过 |
| Docker 解析器容错与旧 history 格式兼容 | 通过 |
| Docker 带空格参数（env/label/save path） | 通过 |
| Docker 测试资源清理 | 容器、镜像、卷、归档均为 0 |
| 测试日志凭据检查 | 主机、用户名、密码均未写入日志 |
| sshd 版本文件 diff | 无 |
| `git diff --check` | 通过 |

完整回归日志末尾输出：

```text
JSch integration regression passed
```

密钥回归日志末尾输出：

```text
JSch key and agent regression passed
```

Docker 回归日志末尾输出：

```text
JSch Docker regression passed
```

本次验证日志：

- `/tmp/easyshell-jsch-docker-final.log`
- `/tmp/easyshell-jsch-regression-docker-review.log`
- `/tmp/easyshell-jsch-key-docker-review.log`

回归过程中出现的 `PortForwardingL: local port ... cannot be bound` 是“启动失败资源清理”用例的预期异常，该用例随后验证会话已释放，整体回归通过。

## 9. 当前限制

- 自动化运行主机为 Linux，Windows 和 macOS 远端分支未实机运行。
- Unix Agent 只验证了初始化；当前环境 Agent 无身份，未完成 Agent 登录。
- Windows Pageant 和 Windows OpenSSH Agent 未实机验证。
- 动态转发只验证无认证 SOCKS4/SOCKS5。
- X11、压缩、真实 HTTP/SOCKS 代理未端到端验证。
- Docker 自动化在 Linux 主机完成，未覆盖 Windows/macOS 远端 Docker；由于主机无法访问 Docker Hub，测试镜像由本地静态 busybox 构建。
- 未执行全局 prune 和 Docker 服务重启；`docker-compose version` 仍使用旧 standalone 命令，若环境只有 Compose v2 插件会返回命令不存在，这是当前方法语义。
- 大文件、长时间空闲和长时间稳定性仍需专项压测。
- 跳板和旧版弱 KEX 算法仍建议在目标服务器矩阵上补充兼容性测试。
- 本次证明的是活跃 JSch 调用链可在仅 JSch classpath 下独立编译和运行；若要彻底移除项目级 MINA sshd，还需项目维护者自行屏蔽遗留的 `ssh2/sftp2` 目录及其依赖。

## 10. Docker 专项迁移与回归记录

### 10.1 迁移说明

Docker 功能仍沿用原有业务模型：通过 SSH Exec 通道执行远端 Docker CLI，并由 `ShellDockerExec`、`ShellDockerParser` 和参数对象完成命令生成、输出解析和 UI 适配。迁移后的执行传输完全由 JSch `ChannelExec` 承担，未引入 MINA sshd 类型，也未修改 `ssh2/docker` 或任何 sshd 版本代码。

本次涉及的 JSch 活跃文件：

- `src/main/java/cn/oyzh/easyshell/ssh/docker/ShellDockerExec.java`
- `src/main/java/cn/oyzh/easyshell/ssh/docker/ShellDockerParser.java`
- `src/main/java/cn/oyzh/easyshell/controller/docker/ShellDockerResourceController.java`
- `src/test/java/cn/oyzh/easyshell/test/ShellJSchDockerRegression.java`

### 10.2 Docker 功能对照与覆盖

| 功能类别 | 覆盖方法/场景 | 验证结论 |
| --- | --- | --- |
| 系统信息 | `docker_info`、`docker_version`、`docker_compose_version` | version/info 通过；standalone compose 不存在时返回预期 not-found |
| 容器创建 | `docker_run_cmd`、`docker_run` | 验证 `-d -i -t`、hostname、端口、卷、env、label、镜像参数及命令顺序 |
| 容器查询 | `docker_ps`、`docker_ps_a`、`docker_ps_exited` | 运行、暂停、退出状态及 `ShellDockerParser.ps()` 均通过 |
| 容器控制 | `docker_pause`、`docker_unpause`、`docker_restart`、`docker_stop`、`docker_start`、`docker_kill` | 状态转换全部通过 |
| 容器变更 | `docker_rename`、`docker_rm`、`docker_rm_f` | 重命名、普通删除和强制删除均通过 |
| 容器执行 | SSH Exec + `docker exec` | 容器内 echo 输出通过，验证 JSch Exec 与 Docker 组合 |
| 日志 | `docker_logs` | 容器 stdout 日志令牌读取通过 |
| 端口 | `docker_port`、`ShellDockerParser.port()` | 随机宿主机端口映射解析通过 |
| 资源 | `docker_resource`、`docker_update`、`ShellDockerParser.resource()` | 内存、swap、CPU shares、NanoCpus、period/quota 及零值/优先级通过 |
| 镜像查询 | `docker_images`、`docker_history` | 镜像列表和真实 history 注释解析通过 |
| 镜像生成 | `docker_commit`、`docker_tag` | 带空格备注、仓库/标签、tag 引用通过 |
| 镜像导出 | `docker_save` | 带空格路径的非空 tar 归档通过 |
| 镜像删除 | `docker_rmi(ShellDockerRmi)`、`docker_rmi_f` | 强制删除 tag 和 commit 镜像通过 |
| 解析容错 | Docker daemon 错误文本、非法数字、旧 history 格式 | 不抛越界/数字格式异常，旧格式兼容通过 |
| 资源清理 | 容器、镜像、卷、tar、临时 rootfs | 测试结束后全部为 0 残留 |

### 10.3 功能差异与兼容性

- `docker update` 的零值字段不再发送；旧调用方若依赖“零值也更新”的行为需要调整，这是为了避免无效 Docker 参数。
- `ShellDockerResource.nanoCpus` 以纳秒为唯一持久语义；UI 显示为核数并保留小数，CLI 转换为 `--cpus` 十进制值。
- `NanoCpus` 与 period/quota 冲突时采用 `NanoCpus` 优先，避免 Docker daemon 拒绝组合请求。
- Docker history 使用 Unit Separator 生成新输出，解析器兼容旧 `\r\t` 文本，现有调用方无需修改。
- `docker run` 的 `params` 保持原始高级参数语义；结构化字段（名称、镜像、env、label、volume）统一参数保护。
- Docker 方法名称、参数对象、返回类型和既有 UI 调用方式未改变；`ssh2`、`sftp2`、`ssh1`、`sftp1` 保持原样。

### 10.4 验证结果与限制

最终隔离编译包含 2378 个活跃/兼容源码输入，生成 2278 个 class；隔离 classpath 和字节码中 sshd/JGit 引用均为 0。Docker 回归、完整 SSH/SFTP/转发/Process/Server 回归、密钥/Agent 回归均通过，日志末尾分别为：

```text
JSch Docker regression passed
JSch integration regression passed
JSch key and agent regression passed
```

Docker 测试使用唯一资源名并验证了镜像不存在时的本地构建路径。由于测试主机无法直连 Docker Hub，测试镜像由主机已有静态 `/usr/bin/busybox` 导入；测试凭据仅从本地环境读取，未写入文档、日志或源码。全局 `docker prune`、`systemctl restart docker` 及 Windows/macOS 远端 Docker 未执行，保留为后续环境矩阵测试项。
