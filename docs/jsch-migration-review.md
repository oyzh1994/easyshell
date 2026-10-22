# EasyShell SSH / SSH2 / SFTP JSch 迁移审查报告

## 1. 审查结论

本次审查确认：EasyShell 活跃的 `ssh`、`sftp` 调用链已经切换为 JSch 2.28.8，MINA sshd 类型不再出现在活跃源码、隔离 classpath 或隔离字节码中。审查中发现并修复了 SFTP 通道池死锁、通道泄漏、Agent/X11 条件错误、跳板配置过滤错误、SFTP 文件对象销毁不完整等确定性问题。Docker 专项审查还修复了资源更新参数、history 输出解析、参数保护、异常输出容错和长操作超时问题，并完成了真实容器与镜像生命周期回归。本轮继续完成 SSH 传输压缩、文件压缩、单跳/双跳/三层跳板、多主机端口转发、真实 HTTP CONNECT 代理、真实 SOCKS5 代理及 Windows/macOS/Linux 主机矩阵的只读回归，修复了 SFTP 目录列举阶段逐链接预解析导致的严重性能问题，并修复了 JSch Shell 首屏 MOTD/登录信息、提示符丢失以及认证失败后无法及时弹出认证页的问题。

构建和回归结论：

- `base-ssh` 与 `easyshell` 的 Maven 编译通过。
- 活跃 JSch 调用链在不包含 MINA sshd/JGit 的隔离 classpath 下编译通过。
- 虚拟机 Linux 主机回归通过，包含连接、认证、终端、Exec、SFTP、端口转发、跳板、Docker 完整生命周期、Process、Server、重连和并发。
- SSH 双向压缩协商、4 MiB 传输校验、512 KiB SFTP 传输以及 `tgz/xz/bz2` 文件压缩/解压通过。
- 单跳、双跳和三层跳板通过（1/2/3 个跳板会话），目标主机身份、断线重连、Exec、SFTP 和转发资源释放均完成断言。
- 多主机本地、远程、动态转发通过；两跳和三层跳板转发后的目标身份、并发连接和监听端口释放均完成断言。
- HTTP CONNECT 与 SOCKS5 无认证代理通过，代理日志确认 SSH 和 SFTP 流量均由代理转发。
- Windows 虚拟机、Windows 真实机器、macOS 真实机器、UOS 虚拟机和云端 Linux 的只读平台/压缩协商探测通过。
- SFTP 目录列举只判断链接类型，点击时才解析目标；阿里云 `/bin` 从 16.9 秒降至 0.54–0.58 秒，虚拟机为毫秒级。
- JSch Shell 在连接前安装并缓存输入输出流，首屏 MOTD、登录信息和提示符恢复；Ubuntu 虚拟机首屏从 60 字节恢复到 652 字节/19 行，阿里云为 1466 字节/41 行。
- 错误密码或账户名现在只提交一次凭证即进入交互认证回调，不再等待 JSch 用同一凭证重试到服务端 `Too many authentication failures`；专项测试约 2.6 秒触发回调。
- 公钥认证已在 Ubuntu 和 UOS 虚拟机完成矩阵回归：RSA（普通/带口令）、ECDSA 256/384/521、Ed25519 的密钥管理器认证、Exec 和 SFTP 全部通过；证书文件路径覆盖普通 RSA、带口令 RSA 和 Ed25519，未授权密钥约 150 ms 内拒绝。
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
| 密码认证 | sshd password auth | JSch `UserInfo` / password | 已迁移；认证失败只提交一次凭证并进入交互认证 |
| 密钥认证 | sshd key identity | JSch `addIdentity` | 已迁移并完成实机矩阵；支持路径和内嵌密钥，普通/带口令私钥均通过 |
| Agent 认证 | sshd Agent 工厂 | JSch `AgentIdentityRepository` | 已迁移并完成初始化验证 |
| 交互认证 | sshd keyboard-interactive | JSch `UIKeyboardInteractive` | 已迁移 |
| Known hosts | sshd verifier | JSch `known_hosts` | 已迁移，跳板保持旧行为不校验 |
| 压缩 | sshd compression | JSch `compression.c2s/s2c` | 已验证关闭/开启协商、4 MiB Exec 传输、SFTP 传输和 `tgz/xz/bz2` 文件压缩/解压 |
| Shell / PTY | `ChannelShell` | JSch `ChannelShell` | 已验证交互、PTY resize、关闭重开 |
| Exec | `ChannelExec` | JSch `ChannelExec` | 已验证并发、超时和超时后恢复 |
| SFTP 文件操作 | `SftpClient` | JSch `ChannelSftp` | 已验证上传、下载、权限、重命名、递归删除 |
| SFTP 流操作 | sshd SFTP streams | JSch SFTP streams | 已验证进度回调和流关闭 |
| SFTP 通道池 | `Pool<SftpClient>` | 独立条件队列池 + JSch 通道 | 已修复原实现死锁，验证并发借还 |
| 本地转发 | `startLocalPortForwarding` | `setPortForwardingL` | 已验证单主机、跨主机、并发和跳板链转发 |
| 远程转发 | `startRemotePortForwarding` | `setPortForwardingR` | 已验证跨主机回环、动态远端端口解析和清理 |
| 动态转发 | sshd dynamic forwarding | SOCKS4/SOCKS5 + `ChannelDirectTCPIP` | 已验证跨主机 SOCKS4/5、半关闭和畸形请求恢复 |
| 跳板 | 多段转发 | 多段 `Session` + 本地端口 | 已验证三个不同跳板节点的三层链路、重连、Exec、SFTP、会话数及关闭清理 |
| HTTP 代理 | sshd/JGit HTTP 代理连接器 | JSch `ProxyHTTP` | 已通过真实 HTTP CONNECT 代理完成 SSH/SFTP 回归 |
| SOCKS5 代理 | sshd/JGit SOCKS 连接器 | JSch `ProxySOCKS5` | 已通过真实 SOCKS5 无认证代理完成 SSH/SFTP 回归；`socks` 历史配置映射到 SOCKS5 |
| Docker | Exec 命令层 | Exec 命令层 + 容器/镜像生命周期 | 已在 Ubuntu 虚拟机验证 info/version、run、ps、exec、logs、资源更新、pause/unpause、rename、restart、start/stop/kill、port、commit、tag、save、rm/rmi；全局 prune 和 Docker 服务重启未执行 |
| Process | Exec + 解析 | Exec + 解析 | 已验证并发采集和网络速率解析 |
| Server | Exec + 解析 | Exec + 解析 | 已验证信息与监控并发 |
| X11 | sshd X11 channel | JSch X11 session/channel 配置 | 已迁移，未做真实 X11 端到端验证 |
| 异常处理 | sshd exception | `ShellException` / `SSHException` 包装 | 已迁移，增加可恢复通道错误重试和认证失败分类 |

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

### 4.13 文件压缩路径缺少 shell 参数保护

`ShellServerExec.compress()` 和 `uncompress()` 原实现把父目录、文件名直接拼接到远端 shell 命令中。路径包含空格时会拆分成多个参数，包含单引号时还可能改变命令语义。

修复：父目录、源文件名和归档文件名统一做 POSIX shell 单引号转义；无父目录时使用当前目录；未知压缩类型返回 `null` 并记录告警，不再执行空命令。`tgz`、`xz`、`bz2` 已用包含空格和单引号的真实目录/文件名验证。

### 4.14 密钥认证失败后的认证方式回退

密钥管理器、证书文件路径和 Agent 认证只准备 `publickey` 身份，但 JSch 在公钥认证失败后仍可能继续尝试服务端提供的 `password` 方法。此时界面没有可用的登录密码，会触发额外的密码输入框，掩盖真正的公钥失败原因。

修复：`ShellBaseSSHClient.initSession()` 按连接配置限制 `PreferredAuthentications`。密码认证使用 `password,keyboard-interactive`；证书、密钥管理器和 Agent 认证只使用 `publickey`。认证失败仍进入既有 `verifyFailureCallback` 流程，不会静默切换认证方式。

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
- `enableCompress=true` 时优先协商 `zlib@openssh.com`，其次 `zlib`；`false` 时强制 `none`。配置字段和默认值保持不变。
- 代理协议映射保持历史语义：`http -> ProxyHTTP`、`socks4 -> ProxySOCKS4`、`socks5/socks -> ProxySOCKS5`；本轮真实代理验证覆盖 HTTP 和 SOCKS5 无认证场景。
- 文件压缩方法名、参数和返回类型不变；仅修正远端 shell 参数转义和未知类型处理，不改变归档命名规则。
- 密钥认证方式保持 `manager`、`certificate`、`sshAgent` 配置值不变；密钥管理器仍使用内嵌公私钥和可选口令，证书认证仍使用私钥文件路径和可选口令。仅收紧了每种认证方式允许的底层 SSH 认证方法，避免跨方式回退。
- DSA 是 OpenSSH 已弃用算法。密钥生成 UI 的长度选项已从不可用的 2048 位纠正为标准 1024 位，与 `JSchKeyUtil`、JSch 和 OpenSSH DSA key 格式保持一致。
- 新增测试统一支持 `-Dtest.hosts.file=<本地主机文件>` 与 `-Dtest.host.index=<序号>`，凭据只在测试进程运行时读取，不进入源码、文档或日志。

## 6. 风险与功能差异

1. JSch 与 MINA sshd 的默认 KEX、MAC、主机密钥算法集合不同，旧服务器或弱算法环境可能需要额外算法配置。
2. 动态转发目前只支持无认证 SOCKS4/SOCKS5；不支持 SOCKS5 用户名密码、UDP ASSOCIATE 和 BIND。
3. JSch 的 X11 实现与 sshd 不同，当前仍未完成真实图形应用端到端验证。
4. SSH 压缩已验证双向协商和 4 MiB 数据传输，但尚未执行多小时空闲、弱网和超大文件稳定性压测。
5. 真实代理本轮只验证 HTTP CONNECT 与 SOCKS5 无认证；HTTP Basic/Digest、SOCKS5 用户名密码、UDP ASSOCIATE 和 BIND 未纳入本轮测试。
6. 跳板链路按每个跳板节点自身认证建立，主连接代理配置不会自动套用到每个跳板节点；弱 KEX/旧算法服务器仍需单独做兼容矩阵。
7. SFTP 流式读写依赖调用方关闭流，并由客户端关闭延迟资源；长时间未关闭的流仍会占用通道。
8. Docker 自动化已在 Ubuntu 虚拟机完成真实生命周期验证，但未执行全局 `prune` 和 `systemctl restart docker`，避免影响主机上其他容器；Windows/macOS 远端 Docker 只完成了 CLI/平台探测，未执行完整容器生命周期。
9. SFTP 通道池现在会关闭借出通道，因此客户端关闭属于强制终止语义，调用方不应在关闭后继续使用借出通道。
10. 客户端连接仍主要依赖服务端命令输出解析；Windows 本地化输出已通过 `isWindows()` + `cmd.exe /c ver` 矩阵验证，但更多发行版差异仍需持续覆盖。
11. `docker run` 的 `params` 是面向高级用户的原始 CLI 片段，仍可能包含任意 Docker 选项；UI 调用方应将其视为可信输入。
12. `docker_update` 在 `NanoCpus` 和 period/quota 同时存在时优先使用 `NanoCpus`。需要显式 period/quota 时应将该资源对象中的 `nanoCpus` 置为 0。
13. DSA 已从现代 OpenSSH 默认服务端算法中移除。本轮测试主机的 `sshd -T` 均未启用 `ssh-dss`，因此 DSA 只验证了 1024 位密钥生成和格式识别，未作为实机认证通过项；生产环境应优先使用 Ed25519 或 RSA/ECDSA。
14. 公钥认证矩阵覆盖 Linux 虚拟机。Windows OpenSSH 的 `administrators_authorized_keys` 路径、权限继承和 Pageant/Agent 行为仍需在 Windows 实机补充。

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
- 跨主机本地转发、远程转发、动态 SOCKS4/SOCKS5、并发转发、SOCKS5 半关闭、畸形请求恢复和监听端口释放。
- 单跳、双跳和三层跳板，包含目标身份、断线重连、SFTP 复验、跳板会话数和监听端口释放。
- SSH 压缩关闭/开启协商、重复数据 4 MiB Exec 校验、压缩会话下 512 KiB SFTP 传输。
- 文件压缩 `tgz/xz/bz2`、含空格和单引号路径、解压内容校验、未知类型处理。
- 真实 HTTP CONNECT 代理和 SOCKS5 无认证代理下的 SSH、Exec、SFTP。
- Docker info/version、容器 run/ps/exec/logs、资源更新、pause/unpause、rename、restart、start/stop/kill、端口、commit/tag/save/rm/rmi、解析器容错和资源清理。
- Process 信息、Server 信息和并发监控。
- 会话断开重连、隧道重建、SFTP 重连。
- 启动失败后的会话、通道和转发清理。
- 密钥生成、类型/长度解析、代理类型映射、Agent 初始化。
- 公钥认证矩阵：RSA 普通/带口令、ECDSA 256/384/521、Ed25519、DSA 生成；密钥管理器和私钥文件两种认证路径；密码会话上传、Exec、SFTP、未授权密钥拒绝、`authorized_keys` 恢复。
- Windows 虚拟机/真实机器、macOS 真实机器、UOS 虚拟机、云端 Linux 的只读平台、架构、Docker CLI 和压缩协商矩阵。

### 7.3 命令示例

凭据仅由测试运行时从本地主机文件读取，不写入文档或源码：

```bash
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.host.index=1 \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchCompressionRegression
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.jump.index=1 -Dtest.target.index=3 \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchJumpRegression
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.host.index=1 \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchProxyRegression
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.host.indices=1,2,3,4,5,6 \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchPlatformRegression
```

```bash
cd /Users/oyzh/IdeaProjects/oyzh/base/base-ssh
mvn -q -DskipTests install

cd /Users/oyzh/IdeaProjects/oyzh/easyshell
mvn -q -DskipTests test-compile

java -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchKeyRegression
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.host.index=1 -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchConcurrentRegression
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.host.index=1 -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchDockerRegression
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.pubkey.host.index=1 \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchPublicKeyAuthRegression
```

Docker 回归默认使用 `easyshell-jsch-test:local`。本机没有该镜像且无法访问 Docker Hub 时，测试会从主机已有的静态 `/usr/bin/busybox` 构建临时镜像；也可通过 `EASYSHELL_TEST_DOCKER_IMAGE` 指定已存在镜像。测试资源均带唯一后缀，`finally` 中删除容器、临时镜像、卷和归档。

### 7.4 人工验证项

- JavaFX `ShellTerminalApp2` 终端界面手工交互。
- Windows Pageant、OpenSSH Agent、macOS Agent 实机登录。
- 带认证的 HTTP/SOCKS5 代理、SOCKS4 外部代理连接。
- X11 图形应用转发。
- 大文件、长时间空闲、弱网和多小时稳定性。
- Windows/macOS 远端 Docker 完整生命周期、Docker Compose 插件模式。
- `7z/zip/rar/lzo/zst/lz` 等可选压缩工具环境。
- 全局 Docker prune、Docker 服务重启和宿主机级 Docker 故障恢复。

## 8. 验证结果

验证日期：2026-10-22。

| 验证项 | 结果 |
| --- | --- |
| `base-ssh` Maven `install` | 通过 |
| `easyshell` Maven `test-compile` | 通过 |
| 隔离编译 `base-ssh` JSch 子集（21 个源文件） | 通过 |
| 隔离编译 `easyshell` 活跃源码（2375 个源文件，排除 ssh2/sftp2） | 通过 |
| 隔离生成的 `base-ssh` / `easyshell` class | 21 / 2278 |
| 隔离编译 JSch 测试入口 | 通过 |
| 活跃源码 sshd/JGit import 检查 | 0 处 |
| 隔离 classpath sshd/JGit 检查 | 0 个 |
| 隔离生成字节码 sshd/JGit 引用检查 | 0 处 |
| 密钥与 Agent 回归 | 通过 |
| 公钥认证生成矩阵（RSA/ECDSA/Ed25519/DSA） | 7 种密钥生成与格式识别通过；测试主机服务端启用 6 种 |
| 公钥认证管理器路径（Ubuntu/UOS 虚拟机） | 6/6 通过，Exec 与 SFTP 均通过 |
| 公钥认证文件路径（普通/带口令私钥） | 3/3 通过 |
| 未授权公钥拒绝 | 通过（Ubuntu 145 ms；UOS 154 ms） |
| 公钥授权清理 | 通过（`authorized_keys` 按原内容恢复） |
| 真实主机完整回归 | 通过 |
| SSH 压缩关闭/开启协商 | 通过（`none/none` 与 `zlib@openssh.com/zlib@openssh.com`） |
| 压缩会话 4 MiB Exec 校验 | 通过 |
| 压缩会话 512 KiB SFTP 上传/下载校验 | 通过 |
| 文件压缩/解压 `tgz/xz/bz2` | 通过（含空格和单引号路径） |
| 单跳跳板 Exec/SFTP/资源释放 | 通过（1 个跳板会话） |
| 双跳跳板 Exec/SFTP/资源释放 | 通过（2 个跳板会话） |
| 三层跳板 Exec/SFTP/重连/资源释放 | 通过（3 个跳板会话） |
| 多主机本地/远程/动态端口转发 | 通过（含并发、半关闭和监听端口释放） |
| HTTP CONNECT 真实代理 | 通过 |
| SOCKS5 无认证真实代理 | 通过 |
| 6 主机平台/架构/压缩协商矩阵 | 6/6 通过 |
| SFTP 链接列表性能（阿里云 `/bin`） | 通过（修复前 16893 ms，修复后 538–579 ms） |
| SFTP 链接列表性能（`/sbin`） | 通过（阿里云 259–282 ms；Ubuntu/UOS 虚拟机 7–54 ms） |
| SFTP 链接懒解析（列表不解析、点击解析） | 通过 |
| Shell 首屏 MOTD/登录信息回显 | 通过（Ubuntu/UOS 虚拟机、阿里云） |
| Shell 首次提示符与交互命令 | 通过（初始打开和关闭后重开） |
| TTY `ready()` 复用已连接 Shell | 通过（通道 ID 不变，不再无条件重开） |
| 错误密码/用户名触发交互认证 | 通过（回调 1 次，错误密码约 2.6 秒） |
| 认证页返回正确凭证后重试 | 通过（错误密码、错误用户名均可恢复连接） |
| `Too many authentication failures` 异常分类 | 通过 |
| Docker 真实容器/镜像生命周期回归 | 通过 |
| Docker 解析器容错与旧 history 格式兼容 | 通过 |
| Docker 带空格参数（env/label/save path） | 通过 |
| Docker 测试资源清理 | 容器、镜像、卷、归档均为 0 |
| 测试凭据检查 | 文档、改动源码、最终日志中完整凭据组合为 0；最终日志无密码字段 |
| 虚拟机测试资源残留检查 | 临时文件、容器、镜像、卷、代理进程均为 0 |
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

压缩、跳板、代理和平台矩阵日志末尾输出：

```text
JSch jump regression passed
JSch port forwarding regression passed
JSch compression regression passed
JSch proxy regression passed
JSch platform regression passed
```

SFTP 链接性能与修复后完整回归日志末尾输出：

```text
JSch SFTP link performance regression passed
JSch integration regression passed
```

Shell 首屏与完整回归日志末尾输出：

```text
JSch shell banner regression passed
JSch integration regression passed
```

公钥认证回归日志末尾输出：

```text
public key auth matrix completed: manager=6, certificate=3, unauthorized=rejected
JSch public key authentication regression passed
```

本次验证日志：

- `/tmp/easyshell-jsch-docker-final.log`
- `/tmp/easyshell-jsch-docker-vm-final.log`
- `/tmp/easyshell-jsch-integration-final.log`
- `/tmp/easyshell-jsch-key-final.log`
- `/tmp/easyshell-jsch-compression-final.log`
- `/tmp/easyshell-jsch-jump-final.log`
- `/tmp/easyshell-jsch-proxy-final.log`
- `/tmp/easyshell-jsch-platform-final.log`
- `/tmp/easyshell-sftp-link-baseline-aliyun.log`
- `/tmp/easyshell-sftp-link-fixed-aliyun.log`
- `/tmp/easyshell-sftp-link-profile-aliyun.log`
- `/tmp/easyshell-sftp-link-fixed-vm1.log`
- `/tmp/easyshell-sftp-link-fixed-vm3.log`
- `/tmp/easyshell-jsch-integration-sftp-link-fix.log`
- `/tmp/easyshell-shell-banner-baseline-vm1.log`
- `/tmp/easyshell-shell-banner-connector-vm1.log`
- `/tmp/easyshell-shell-banner-connector-aliyun.log`
- `/tmp/easyshell-shell-banner-fixed-uos.log`
- `/tmp/easyshell-jsch-integration-shell-banner-fix.log`
- `/tmp/easyshell-shell-banner-final-vm1.log`
- `/tmp/easyshell-shell-banner-final-aliyun.log`
- `/tmp/easyshell-jsch-integration-shell-banner-final.log`
- `/tmp/easyshell-jsch-jump-multihost-final.log`
- `/tmp/easyshell-jsch-port-forwarding-multihost-final.log`
- `/tmp/easyshell-jsch-integration-forward-jump-final.log`
- `/tmp/easyshell-jsch-auth-failure-final.log`
- `/tmp/easyshell-jsch-integration-auth-fix-final.log`
- `/tmp/easyshell-jsch-pubkey-ubuntu.log`
- `/tmp/easyshell-jsch-pubkey-uos.log`
- `/tmp/easyshell-jsch-auth-after-pubkey.log`
- `/tmp/easyshell-jsch-key-after-pubkey.log`
- `/tmp/easyshell-cleanup-check.log`

回归过程中出现的 `PortForwardingL: local port ... cannot be bound` 是“启动失败资源清理”用例的预期异常，该用例随后验证会话已释放，整体回归通过。

## 9. 当前限制

- Windows 虚拟机/真实机器、macOS 真实机器、UOS 虚拟机和云端 Linux 已完成只读平台/压缩协商矩阵；完整 Shell/SFTP 端到端仍以 Linux 虚拟机为主。
- Unix Agent 只验证了初始化；当前环境 Agent 无身份，未完成 Agent 登录。
- Windows Pageant 和 Windows OpenSSH Agent 未实机验证。
- 公钥认证矩阵覆盖 Ubuntu/UOS 虚拟机；Windows OpenSSH 授权文件路径、ACL 和 Pageant 未完成公钥实机验证。
- DSA 在测试主机服务端均被禁用，只验证生成和格式识别，未纳入认证通过项。
- 内置动态转发验证无认证 SOCKS4/SOCKS5；外部代理本轮验证 HTTP CONNECT 与 SOCKS5 无认证。
- X11 仍未完成真实图形应用端到端验证。
- SSH 压缩已验证协商与 4 MiB 传输，但未做长时间/超大文件稳定性压测；`7z/zip/rar/lzo/zst/lz` 因工具或环境未覆盖，未纳入本轮通过项。
- Docker 完整生命周期在 Ubuntu 虚拟机完成；Windows/macOS 远端 Docker 仅完成 CLI/平台探测。由于测试主机无法访问 Docker Hub，测试镜像由本地静态 busybox 构建。
- 未执行全局 prune 和 Docker 服务重启；`docker-compose version` 仍使用旧 standalone 命令，若环境只有 Compose v2 插件会返回命令不存在，这是当前方法语义。
- 大文件、长时间空闲和长时间稳定性仍需专项压测。
- 跳板已完成虚拟机单跳/双跳；旧版弱 KEX 算法仍建议在目标服务器矩阵上补充兼容性测试。
- 本次证明的是活跃 JSch 调用链可在仅 JSch classpath 下独立编译和运行；若要彻底移除项目级 MINA sshd，还需项目维护者自行屏蔽遗留的 `ssh2/sftp2` 目录及其依赖。
- 阿里云 `/bin` 的 0.54–0.58 秒主要受 1369 条目录数据的网络传输限制；原始 `ChannelSftp.ls` 耗时与完整 `lsFile` 基本一致，本地对象构造不是瓶颈。跨地域高延迟环境不保证稳定低于 0.5 秒，但已验证低于 1 秒。
- Shell 首屏已恢复完整回显；MOTD 内容、长度和是否存在仍由远端 sshd、PAM、发行版配置决定，不同主机不保证相同。

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

最终隔离编译包含 `base-ssh` 21 个源文件和 EasyShell 2375 个活跃/兼容源文件，共生成 2278 个 EasyShell class；隔离 classpath 和字节码中 sshd/JGit 引用均为 0。Docker 回归、完整 SSH/SFTP/转发/Process/Server 回归、密钥/Agent 回归均通过，日志末尾分别为：

```text
JSch Docker regression passed
JSch integration regression passed
JSch key and agent regression passed
```

Docker 测试使用唯一资源名并验证了镜像不存在时的本地构建路径。由于测试主机无法直连 Docker Hub，测试镜像由主机已有静态 `/usr/bin/busybox` 导入；测试凭据仅由测试进程从本地主机文件读取，未写入文档、日志或源码。全局 `docker prune`、`systemctl restart docker` 及 Windows/macOS 远端 Docker 完整生命周期未执行，保留为后续环境矩阵测试项。

## 11. 压缩、跳板与代理专项迁移记录

### 11.1 实现位置与调用链

本专项没有引入新的 MINA sshd 类型，也没有修改 sshd 版本代码。活跃调用链如下：

- 传输压缩：`ShellSSHClient.start()` -> `ShellBaseSSHClient.initClient()` -> JSch `Session.setConfig("compression.c2s/s2c")` -> `Session.connect()`。
- 文件压缩：`ShellSSHClient.serverExec()` -> `ShellServerExec.compress()/uncompress()` -> JSch `ChannelExec`。
- 跳板：`ShellSSHClient.initHost()` -> `cn.oyzh.ssh.jump.SSHJumpForwarder` -> 多段 JSch `Session` + `setPortForwardingL`。
- HTTP 代理：`ShellSSHClient.initProxy()` -> `JschUtil.newProxy()` -> JSch `ProxyHTTP`。
- SOCKS5 代理：同一调用链 -> JSch `ProxySOCKS5`。

涉及的活跃实现文件：

- `src/main/java/cn/oyzh/easyshell/ssh/ShellBaseSSHClient.java`
- `src/main/java/cn/oyzh/easyshell/ssh/ShellSSHClient.java`
- `src/main/java/cn/oyzh/easyshell/ssh/ShellSSHUtil.java`
- `src/main/java/cn/oyzh/easyshell/ssh/server/ShellServerExec.java`
- `base-ssh/src/main/java/cn/oyzh/ssh/jump/SSHJumpForwarder.java`
- `base-ssh/src/main/java/cn/oyzh/ssh/util/JschUtil.java`

### 11.2 SSH 传输压缩与文件压缩

| 场景 | 验证内容 | 结果 |
| --- | --- | --- |
| 压缩关闭 | `enableCompress=false`，双向算法均为 `none` | 通过 |
| 压缩开启 | `enableCompress=true`，双向协商为 `zlib@openssh.com` | 通过 |
| 大数据传输 | 压缩会话下 Exec 输出 4 MiB 重复数据，长度和 SHA-256 一致 | 通过 |
| SFTP 传输 | 压缩会话下上传/下载 512 KiB 二进制数据，SHA-256 一致 | 通过 |
| `tgz` | 目录含空格、文件名含单引号，压缩、删除源、解压、内容校验 | 通过 |
| `xz` | 同上 | 通过 |
| `bz2` | 同上 | 通过 |
| 未知类型 | 返回 `null` 并记录告警，不执行空命令 | 通过 |

JSch 的 `zlib@openssh.com` 是延迟压缩，只有认证完成后才启用压缩数据处理；这与 sshd 的压缩工厂实现方式不同，但对业务层表现为同一个 `enableCompress` 配置。服务端未提供压缩算法时，配置中的 `none` 兜底仍可建立连接。

文件压缩仍保持原有业务规则：`tgz/tar.gz/tar` 使用 `tar -z`，`xz` 使用 `tar -J`，`bz2` 使用 `tar -j`，归档命名规则不变。本轮只修复路径 shell 转义和未知类型处理；`7z/zip/rar/lzo/zst/lz` 依赖目标机工具，未纳入本轮通过项。

### 11.3 单跳、双跳与三层跳板

测试链路使用四台不同主机，第二跳和第三跳均为独立节点：

- 单跳：Ubuntu 虚拟机 -> 阿里云 Ubuntu。
- 双跳：Ubuntu 虚拟机 -> UOS 虚拟机 -> 阿里云 Ubuntu。
- 三层跳板：Ubuntu 虚拟机 -> UOS 虚拟机 -> Windows 虚拟机 -> 阿里云 Ubuntu。

| 验证项 | 单跳 | 双跳 | 三层跳板 |
| --- | --- | --- | --- |
| JSch 目标会话建立 | 通过 | 通过 | 通过 |
| 目标会话地址为本地转发地址 | 通过 | 通过 | 通过 |
| 目标 `hostname` 与直连一致 | 通过 | 通过 | 通过 |
| JSch 跳板会话数量 | 1 | 2 | 3 |
| Exec 输出 | 通过 | 通过 | 通过 |
| SFTP 上传/下载内容 | 通过 | 通过 | 通过 |
| 目标会话断开后经原链路重连 | 通过 | 通过 | 通过 |
| 重连后跳板会话数量 | 1 | 2 | 3 |
| 重连后 SFTP 复验 | 通过 | 通过 | 通过 |
| 关闭后转发会话和本地端口释放 | 通过 | 通过 | 通过 |

跳板仍按每个节点自己的 `host`、`port`、`user`、认证方式和超时建立连接；主连接代理不会自动传播到跳板节点。跳板启用时保持旧行为不校验临时本地转发目标的 known_hosts，非跳板连接继续使用 known_hosts 校验配置。

### 11.4 HTTP CONNECT 与 SOCKS5 代理

代理测试在 Ubuntu 虚拟机内启动临时、高位随机端口的无认证 HTTP CONNECT 和 SOCKS5 服务，客户端从本机通过代理连接同一虚拟机的 SSH 服务。验证完成后按 PID 终止代理并删除临时目录。

| 代理类型 | SSH 连接 | Exec | SFTP 上传/下载 | 代理确实被使用 | 结果 |
| --- | --- | --- | --- | --- | --- |
| HTTP CONNECT | 通过 | 通过 | 通过 | 通过代理日志计数断言 | 通过 |
| SOCKS5 无认证 | 通过 | 通过 | 通过 | 通过代理日志计数断言 | 通过 |

本轮按需求只测试 SOCKS5，不测试 SOCKS4 外部代理。历史配置 `protocol=socks` 仍映射到 SOCKS5，`protocol=socks4` 仍映射到 `ProxySOCKS4`。代理用户名密码认证、UDP ASSOCIATE 和 BIND 不在本轮范围内。

### 11.5 主机平台矩阵

所有平台探测均为只读命令，不写入远端文件，不创建容器。压缩算法列为开启 `enableCompress=true` 后的双向协商结果。

| 主机角色 | 系统/架构 | Docker CLI | 压缩协商 | 结果 |
| --- | --- | --- | --- | --- |
| Ubuntu 虚拟机 | Linux / aarch64 | 可用 | `zlib@openssh.com` 双向 | 通过 |
| Windows 虚拟机 | Windows 10 / ARM64 | 可用 | `zlib@openssh.com` 双向 | 通过 |
| UOS 虚拟机 | Linux / aarch64 | 可用 | `zlib@openssh.com` 双向 | 通过 |
| Windows 真实机器 | Windows 10 / AMD64 | 可用 | `zlib@openssh.com` 双向 | 通过 |
| macOS 真实机器 | Darwin / arm64 | 可用 | `zlib@openssh.com` 双向 | 通过 |
| 云端 Ubuntu | Linux / x86_64 | 可用 | `zlib@openssh.com` 双向 | 通过 |

Windows 分支通过 `isWindows()` 和 `cmd.exe /c ver` 识别，避免依赖本地化“命令不存在”文案；Docker 完整生命周期仍只在 Ubuntu 虚拟机执行。

### 11.6 资源释放、安全与凭据检查

最终清理检查结果：

```text
temp_files=0
containers=0
images=0
volumes=0
proxy_processes=0
```

测试使用的主机文件只在运行进程内解析。按“主机 + 用户名 + 密码”完整组合扫描文档、改动源码和最终日志，命中文件数为 0；最终日志不包含密码字段。短密码与通用测试数据、公开 Maven groupId 等子串的巧合匹配已单独复核，不构成凭据记录。所有临时资源名义上均带 UUID/时间戳后缀：

- 文件压缩：`/tmp/easyshell-jsch-compress-*`
- 跳板 SFTP：`/tmp/easyshell-jsch-jump-*`
- 代理临时脚本和日志：`/tmp/easyshell-jsch-proxy-*`
- Docker 容器/镜像/卷/归档：`easyshell-jsch-*`

### 11.7 SFTP 链接判定性能修复

#### 根因

JSch 版 `ShellSFTPChannel.lsFile()` 在目录列举时对每个 `isLink()` 条目立即执行：

1. `SSH_FXP_REALPATH`：解析链接目标。
2. `SSH_FXP_STAT`：读取目标属性并填充 `linkAttrs`。

这两次操作都是串行 SFTP 往返。阿里云 `/bin` 有 1369 个条目、225 个链接，修复前需要额外约 450 次请求，实测冷启动列表耗时 `16893 ms`。该行为也与 sshd 版本不一致：sshd 版本目录列举阶段只使用 READDIR 返回的属性判断 `isSymbolicLink()`，不解析目标。

#### 修复

- `ShellSFTPChannel.lsFile()` 只根据 `SftpATTRS.isLink()` 生成链接类型，不再调用 `ShellSFTPCache.realpath()`，列表阶段 `linkAttrs` 保持为空。
- 保留现有双击行为：`ShellFileTableView.onMouseClicked()` 在链接 `linkAttrs == null` 时按需调用 `ShellSFTPUtil.realpath()`，随后执行 `realpath + stat` 并完成目录跳转或文件查看。
- `ShellSFTPFile`、`ShellSFTPCache` 和 UI 调用 API 未改变；`fileInfo()` 等单文件入口仍按原逻辑解析目标。
- 列表阶段未解析的链接与 sshd 版本一致：`isFile()`、`isDirectory()` 返回 `false`，图标显示为通用链接，点击后再确定目标类型。

#### 性能结果

测试目录均为真实远端目录，数值为 3 次列表的 warm best/worst；`raw` 为直接 JSch `ChannelSftp.ls()` 的对照耗时，用于确认本地解析开销。

| 主机角色 | 目录 | 条目/链接 | cold | best/worst | raw | 结果 |
| --- | --- | --- | --- | --- | --- | --- |
| Ubuntu 虚拟机 | `/bin` | 1619 / 289 | 270 ms | 20 / 24 ms | 28 ms | 通过 |
| Ubuntu 虚拟机 | `/sbin` | 539 / 89 | - | 7 / 8 ms | 30 ms | 通过 |
| UOS 虚拟机 | `/bin` | 1993 / 319 | 371 ms | 23 / 30 ms | 34 ms | 通过 |
| UOS 虚拟机 | `/sbin` | 444 / 99 | - | 20 / 54 ms | 10 ms | 通过 |
| 阿里云 Ubuntu | `/bin` | 1369 / 225 | 1427 ms | 538 / 579 ms | 545 ms | 通过（<1 s） |
| 阿里云 Ubuntu | `/sbin` | 447 / 150 | - | 259 / 282 ms | 258 ms | 通过（<0.5 s） |

阿里云 `/usr/bin` 与 `/bin` 指向同一目录，实测 best/worst 为 `535/628 ms`；`/usr/sbin` 与 `/sbin` 为 `257/284 ms`。原始 `ChannelSftp.ls()` 与完整 `lsFile()` 基本同值，说明剩余耗时来自目录数据传输而不是链接判定或文件对象构造。

#### 回归断言

- 列表返回的所有链接条目 `getLinkAttrs() == null`，证明目录列举没有预解析。
- `/bin`、`/sbin` 均存在链接条目，避免空列表误判通过。
- 选取 `/bin` 中一个未解析链接执行点击路径，`realpath + stat` 后 `linkAttrs != null`，证明跳转能力保留。
- 修复后 `ShellJSchConcurrentRegression` 完整通过，覆盖 SFTP 上传下载、权限、重命名、删除、端口转发、Shell、Exec、隧道重连和并发。

测试入口：

- `src/test/java/cn/oyzh/easyshell/test/ShellJSchSftpLinkPerformanceRegression.java`

测试命令示例：

```bash
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.host.index=6 \
  -Dtest.sftp.link.iterations=3 \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchSftpLinkPerformanceRegression
```

### 11.8 Shell 首屏 MOTD 与提示符修复

#### 根因

JSch Shell 与 MINA sshd 的流创建时序不同：

1. 原实现先执行 `channel.setOutputStream(null)`，随后才 `channel.connect()`。
2. JSch 的服务端输出只在调用 `channel.getInputStream()` 时安装管道；原实现连接完成后才获取输入流。
3. 在连接建立到输入流安装之间到达的 MOTD、登录信息和首批提示符被写入空输出流并丢弃。
4. `ShellSSHTtyConnector.ready()` 首次执行时会关闭刚打开的 Shell 并重新 `reopenShell()`，进一步扩大首屏数据丢失和提示符延迟的竞态。

#### 修复

- `ShellSSHClient.openShell()` 在 `connect()` 前调用 `channel.getInputStream()` 和 `channel.getOutputStream()`，并把流缓存到 `ShellSSHShell`。
- 保留 `channel.setInputStream(null)`，仅关闭不再需要的本地输入线程语义；移除会清空服务端回显管道的 `setOutputStream(null)`。
- `ShellSSHShell.getInputStream()/getOutputStream()` 始终返回连接前创建的同一对流，避免后续再次调用 JSch 时替换管道。
- `ShellSSHTtyConnector.ready()` 对已连接的 Shell 直接绑定已有流，不再关闭并重开；仅在 Shell 确实断开时执行 `reopenShell()`。
- 每次重新打开 Shell 都创建新的流缓存，关闭时清理，避免复用已关闭通道。

#### 回归结果

| 主机角色 | 场景 | 修复前首屏 | 修复后首屏 | 提示符 | 交互命令 | 重开 |
| --- | --- | --- | --- | --- | --- | --- |
| Ubuntu 虚拟机 | 初始连接 | 60 字节 / 1 行，无 banner | 652 字节 / 19 行 | 通过 | 通过 | 通过 |
| Ubuntu 虚拟机 | 关闭后重开 | - | 117 字节 / 3 行 | 通过 | 通过 | 通过 |
| UOS 虚拟机 | 初始连接 | - | 304 字节 / 11 行 | 通过 | 通过 | 通过 |
| 阿里云 Ubuntu | 初始连接 | - | 1466 字节 / 41 行 | 通过 | 通过 | 通过 |
| 阿里云 Ubuntu | 关闭后重开 | - | 135 字节 / 3 行 | 通过 | 通过 | 通过 |

测试同时断言：

- `ShellSSHTtyConnector.ready()` 前后 Shell 通道 ID 不变，确认不会重开已连接会话。
- 首屏输出包含 MOTD/banner 标记和提示符。
- 写入唯一命令标记并能在 Shell 输出中读回，确认交互输入输出正常。
- 修复后 `ShellJSchConcurrentRegression` 完整通过，覆盖 Shell、Exec、SFTP、端口转发、隧道重连和并发。

测试入口：

- `src/test/java/cn/oyzh/easyshell/test/ShellJSchShellBannerRegression.java`

测试命令示例：

```bash
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.host.index=1 \
  -Dtest.shell.require.banner=true \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchShellBannerRegression
```

### 11.9 多主机端口转发专项

端口转发专项使用四台不同主机组成
`Ubuntu 虚拟机 -> UOS 虚拟机 -> Windows 虚拟机 -> 阿里云 Ubuntu` 的可达链路，分别验证单跳和跨主机目标：

| 场景 | 验证内容 | 结果 |
| --- | --- | --- |
| 配置化本地转发 | Ubuntu 虚拟机上的 SSH 会话将本地随机端口转发到跨主机 SSH 服务；校验 SSH banner、Exec 和关闭后端口释放 | 通过 |
| 并发本地转发 | 同一跳板会话同时建立到 UOS 虚拟机与阿里云目标的转发；多线程并发握手均成功 | 通过 |
| 远程转发 | Ubuntu 虚拟机远端随机端口回连本机 echo 服务；在远端通过 `/dev/tcp` 完成双向往返校验 | 通过 |
| 动态转发 SOCKS5 | 经 Ubuntu 跳板分别连接 UOS 虚拟机和阿里云 SSH；覆盖半关闭和远端动态端口 | 通过 |
| 动态转发 SOCKS4 | 经 Ubuntu 跳板连接 UOS 虚拟机 SSH；畸形请求后仍可继续建立转发 | 通过 |
| 双跳转发身份 | 经 Ubuntu -> UOS 转发到阿里云 SSH，使用目标凭据校验 `hostname` 与直连一致，并并发建立连接 | 通过 |
| 三层跳板转发身份 | 经 Ubuntu -> UOS -> Windows 转发到阿里云 SSH，使用目标凭据校验 `hostname` 与直连一致，并并发建立连接 | 通过 |
| 三层跳板上的配置化转发 | 目标会话经三个跳板建立后，再按隧道配置建立本地转发；校验 SSH banner、Exec 和三层会话数 | 通过 |
| 转发资源释放 | 本地监听端口、动态代理端口、跳板会话和远端转发均在关闭后释放 | 通过 |

测试入口：

- `src/test/java/cn/oyzh/easyshell/test/ShellJSchJumpRegression.java`
- `src/test/java/cn/oyzh/easyshell/test/ShellJSchPortForwardingRegression.java`

测试命令示例：

```bash
java -Dtest.hosts.file='<local-hosts-file>' \
  -Dtest.jump.index=1 -Dtest.second.jump.index=3 \
  -Dtest.third.jump.index=2 -Dtest.target.index=6 \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchJumpRegression

java -Dtest.hosts.file='<local-hosts-file>' \
  -Dtest.forward.gateway.index=1 -Dtest.forward.relay.index=3 \
  -Dtest.forward.windows.relay.index=2 -Dtest.forward.target.index=6 \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchPortForwardingRegression
```

### 11.10 认证失败与交互认证修复

#### 根因

JSch 的 `UserAuthPassword` 在服务端返回认证失败后会循环使用同一密码继续尝试，直到本地 `MaxAuthTries` 或服务端返回 `Too many authentication failures`。原实现中的 `ShellSSHAuthInteractive.promptPassword()` 只要已有密码就一直返回成功，因此错误密码会被重复提交多次。同时 `ShellBaseSSHClient.isAuthenticationFailure()` 只识别 `auth fail` 等少数文本，未识别 `Auth cancel`、`authentication failure(s)` 和 `Too many authentication failures`，导致 `verifyFailureCallback` 未执行，认证页无法及时弹出。

#### 修复

- `ShellSSHAuthInteractive` 对密码提示只允许提交一次；JSch 第二次请求同一密码时返回失败，立即结束密码认证循环。
- 认证异常分类同时检查 `JSchSessionDisconnectException.getDescription()` 和异常消息，识别 `Auth cancel`、`authentication failure(s)`、`Too many authentication failures`、`no more auth methods` 等认证失败。
- 认证失败后保留原有 `verifyFailureCallback` 交互页流程；用户提交正确凭证后，客户端重新初始化会话并重试连接。
- 非认证错误（例如连接拒绝）不会误触发认证页。

#### 回归结果

| 场景 | 结果 |
| --- | --- |
| 错误密码且关闭认证页 | 回调 1 次，约 2.6 秒结束，不再等待多次重复认证 |
| 错误密码后在认证页提交正确凭证 | 回调 1 次，连接成功，Exec 正常 |
| 错误用户名后在认证页提交正确凭证 | 回调 1 次，连接成功，Exec 正常 |
| `Too many authentication failures` 异常识别 | 通过 |
| 非认证异常不触发认证页 | 通过 |
| 修复后完整 SSH/SFTP/转发/Server 回归 | 通过 |

测试入口：

- `src/test/java/cn/oyzh/easyshell/test/ShellJSchAuthFailureRegression.java`

测试命令示例：

```bash
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.auth.host.index=1 \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchAuthFailureRegression
```

### 11.11 公钥认证实机回归

#### 调用链

- 密钥生成：`ShellAddKeyController.generateKey()` -> `JSchKeyUtil.generateRsa/generateEcdsa/generateEd25519/generateDsa()`。
- 密码会话上传：密码认证 `ShellSSHClient` -> `ShellKeyUtil.sshCopyId()` -> `ShellSFTPClient` / `ShellSSHExec.append()` 写入 `~/.ssh/authorized_keys`。
- 密钥管理器认证：`ShellConnect.authMethod=manager` -> `ShellKeyStore.selectOne(keyId)` -> JSch `addIdentity(name, privateKey, publicKey, passphrase)` -> `publickey` 认证。
- 私钥文件认证：`ShellConnect.authMethod=certificate` -> `ShellConnect.certificate` 指定私钥文件 -> JSch `addIdentity(path, passphrase)` -> `publickey` 认证。
- 测试在密码会话中先备份原 `authorized_keys`，仅追加带唯一注释的公钥；结束时按原字节恢复，未修改已有授权内容。

#### 回归矩阵

测试日期：2026-10-22。主机为 Ubuntu 虚拟机（OpenSSH 9.6p1）和 UOS 虚拟机；测试凭据仅由本地主机清单在运行时读取。

| 认证/算法 | 密钥管理器 | 私钥文件 | Exec | SFTP | 说明 |
| --- | --- | --- | --- | --- | --- |
| RSA 2048 | 通过 | 通过 | 通过 | 通过 | 普通私钥 |
| RSA 2048 + 口令 | 通过 | 通过 | 通过 | 通过 | OpenSSH v1 加密私钥 |
| ECDSA 256 | 通过 | 未重复执行 | 通过 | 通过 | `ecdsa-sha2-nistp256` |
| ECDSA 384 | 通过 | 未重复执行 | 通过 | 通过 | `ecdsa-sha2-nistp384` |
| ECDSA 521 | 通过 | 未重复执行 | 通过 | 通过 | `ecdsa-sha2-nistp521` |
| Ed25519 256 | 通过 | 通过 | 通过 | 通过 | 普通私钥 |
| DSA 1024 | 策略跳过 | 策略跳过 | - | - | 服务端有效配置未启用 `ssh-dss` |

补充验证：

| 场景 | Ubuntu 虚拟机 | UOS 虚拟机 |
| --- | --- | --- |
| 密码认证上传 6 个服务端可用公钥 | 通过 | 通过 |
| 管理器认证、Exec、SFTP | 6/6 通过 | 6/6 通过 |
| 私钥文件认证（RSA、带口令 RSA、Ed25519） | 3/3 通过 | 3/3 通过 |
| 未授权公钥拒绝 | 145 ms | 154 ms |
| 授权文件恢复并逐字节校验 | 通过 | 通过 |

单次管理器认证包含连接、认证、Exec 和小文件 SFTP 往返，耗时约 431–647 ms。测试日志末尾输出：

```text
public key auth matrix completed: manager=6, certificate=3, unauthorized=rejected
JSch public key authentication regression passed
```

#### 功能差异与兼容性

- 密钥管理器和证书文件认证现在只允许 `publickey`，不会在公钥失败后弹出无关的密码框；失败仍由既有认证回调处理。
- 密码认证保持 `password,keyboard-interactive`，原有错误密码/用户名认证页回归继续通过。
- DSA 生成 UI 的长度选项纠正为 1024 位。现代 OpenSSH 默认禁用 DSA，生产环境建议使用 Ed25519 或 RSA/ECDSA。
- 未改变 `ShellKey`、`ShellConnect` 字段和 `manager/certificate/sshAgent` 取值，也未修改任何 sshd 版本代码。

测试入口：

- `src/test/java/cn/oyzh/easyshell/test/ShellJSchPublicKeyAuthRegression.java`

测试命令：

```bash
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.pubkey.host.index=1 \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchPublicKeyAuthRegression
java -Dtest.hosts.file='<local-hosts-file>' -Dtest.pubkey.host.index=3 \
  -cp '<isolated-classpath>' cn.oyzh.easyshell.test.ShellJSchPublicKeyAuthRegression
```

### 11.12 双因素认证（2FA）实机回归

#### 测试环境

- 环境目录：`docker/ssh/2fa`，基础镜像与既有 SSH 测试环境保持一致。
- 容器：`easyshell-ssh-2fa`，镜像 `easyshell/ssh-2fa:local`，宿主机端口 `2224`。
- 持久卷：`easyshell-ssh-2fa-root`。
- 服务端：OpenSSH + PAM `pam_google_authenticator.so`，仅允许
  `AuthenticationMethods keyboard-interactive`；密码认证、公钥认证均关闭。
- 认证因子：Unix 账户密码 + 6 位 TOTP（30 秒周期）。
- 本地测试容器使用 `WINDOW_SIZE 1`、允许 TOTP 重复使用并关闭 PAM 速率限制，
  便于连续执行回归和手工复测；该设置仅用于本地测试环境，生产环境建议开启
  `DISALLOW_REUSE`。
- 本地测试凭据仅保存在容器内 `/root/.easyshell-2fa-credentials`，不写入镜像、
  仓库、文档或测试日志。
- 凭据文件为明文：`password` 是账户密码，`totp_secret` 是 Base32 共享密钥而不是
  验证码；登录时需输入由该密钥生成的当前 6 位 TOTP，验证码每 30 秒变化。
  `otpauth_uri` 可直接导入认证器，文件中的应急码不是当前 TOTP。

#### 调用链与实现

- `ShellConnect.authMethod=password` -> `ShellBaseSSHClient.initClient()` ->
  JSch `PreferredAuthentications=password,keyboard-interactive` ->
  服务端拒绝独立密码认证 -> JSch `UserAuthKeyboardInteractive` ->
  `ShellSSHAuthInteractive.promptKeyboardInteractive()`。
- 交互提示按内容分类：`password/密码` 使用连接密码；`verification/one-time/otp/
  authenticator/验证码/动态码/动态口令/一次性密码` 使用验证码供应器，未注入供应器时
  仍通过 `MessageBox.prompt()` 弹出输入框，保持桌面端行为。
- `ShellBaseSSHClient` 新增 `setVerificationCodeSupplier()/getVerificationCodeSupplier()`，
  所有密码、私钥、密钥管理器和 Agent 认证统一通过 `newAuthInteractive()` 注入供应器。
- JSch 在 keyboard-interactive 失败后会重复调用同一交互对象。交互对象现在只允许提交
  一次验证码，后续调用直接取消认证，避免同一 TOTP 被重复提交。
- 取消认证产生的 `JSchAuthCancelException` 由既有异常分类识别为认证失败，继续触发
  `verifyFailureCallback`，桌面端可以重新打开认证页并重建会话。

#### 回归结果

测试日期：2026-10-22。

| 场景 | 结果 |
| --- | --- |
| 正确密码 + 当前 TOTP | 通过，验证码供应器调用 1 次 |
| 2FA 会话 Exec 命令 | 通过 |
| 2FA 会话 SFTP 上传、元数据读取、下载内容校验 | 通过 |
| 正确认证总耗时（含连接、Exec、SFTP） | 约 529 ms |
| 错误 TOTP | 拒绝，验证码供应器调用 1 次，认证回调 1 次 |
| 错误 TOTP 失败耗时 | 约 4.4 秒；修复前约 13.2 秒 |
| 认证失败异常映射 | `JSchAuthCancelException` -> `ShellException` |
| 既有错误密码/错误用户名认证页回归 | 通过，均只回调 1 次 |
| 公钥认证回归（管理器 6 种、私钥文件 3 种） | 通过，授权文件按原内容恢复 |

测试日志末尾输出：

```text
2FA authentication verified: supplierCalls=1, exec=PASS, sftp=PASS, elapsed=529ms
wrong TOTP rejected: supplierCalls=1, authCallbacks=1, elapsed=4361ms, failure=ShellException
JSch 2FA authentication regression passed
```

#### 手工复测

容器和命名卷已保留，可直接使用：

```bash
export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
docker ps --filter name=easyshell-ssh-2fa
docker exec easyshell-ssh-2fa cat /root/.easyshell-2fa-credentials
ssh -p 2224 root@127.0.0.1
```

手工 SSH 会依次提示密码和 `Verification code:`。也可重新执行自动回归：

```bash
bash docker/ssh/2fa/test-2fa.sh
```

#### 测试入口

- `src/test/java/cn/oyzh/easyshell/test/ShellJSchTwoFactorAuthRegression.java`
- `docker/ssh/2fa/Dockerfile`
- `docker/ssh/2fa/entrypoint.sh`
- `docker/ssh/2fa/docker-compose.yml`
- `docker/ssh/2fa/test-2fa.sh`

#### 兼容性说明

- 未修改 `ShellConnect`、`ShellKey`、认证方式枚举及配置文件格式。
- 未引入任何 MINA sshd 类型；2FA 仅使用 JSch 的
  `UIKeyboardInteractive`/`UserInfo` 扩展点。
- 未注入验证码供应器时保持原有 GUI 输入方式，不影响既有密码、公钥和 Agent 认证。
- 未修改 `ssh2`、`sftp2`、`ssh1`、`sftp1` 及 `base-ssh` 中的 sshd 版本代码。

### 11.13 SSH Agent 认证实机回归

#### 测试环境

- 目标主机：本机 Docker 容器 `easyshell-ssh-agent-test`，镜像
  `kevinwang15/quick-ssh-server:latest`，地址 `127.0.0.1:2222`。
- 服务端 OpenSSH 开启 `PubkeyAuthentication`；密码认证仅用于临时安装测试公钥。
- 本机启动独立 `ssh-agent`，通过 Unix socket 提供身份；测试结束后自动关闭。
- Agent 中放入两把 Ed25519 密钥：一把未授权干扰密钥，一把已授权测试密钥。
- 两把私钥文件在 `ssh-add` 后立即删除，认证只能由 Agent 完成。
- 目标主机 `~/.ssh/authorized_keys` 仅临时追加测试公钥，测试结束恢复原内容或删除临时文件。

#### 调用链

- `ShellConnect.authMethod=sshAgent` -> `ShellBaseSSHClient.initClient()` ->
  `JschUtil.initAgentIdentityRepository()` -> `SSHAgentConnector`（Unix socket）->
  JSch `AgentIdentityRepository` -> `JSch.setIdentityRepository()` ->
  `UserAuthPublicKey` -> 服务端公钥认证。
- `initSession()` 对 Agent 认证保持 `PreferredAuthentications=publickey`，
  不会在 Agent 失败后回退到密码认证。
- 未引入 MINA sshd 类型；实现仅使用 JSch `AgentIdentityRepository`、
  `SSHAgentConnector` 和既有 `UIKeyboardInteractive` 适配层。

#### 回归结果

测试日期：2026-10-22。

| 场景 | 结果 |
| --- | --- |
| 本机 Agent 身份枚举 | 通过，识别 2 把密钥 |
| Agent 含未授权干扰密钥时的认证 | 通过，自动选择已授权密钥 |
| 私钥文件删除后认证 | 通过，证明签名由 Agent 完成 |
| 连续新建 8 个 Agent 会话并执行命令 | 8/8 通过，约 2080 ms |
| 单连接复用执行 5 条命令 | 5/5 通过，约 373 ms |
| Agent 会话 SFTP 上传、元数据、下载校验 | 通过 |
| 6 路并发 Agent 会话 | 6/6 通过，约 327 ms |
| 移除授权后未授权 Agent 密钥 | 拒绝，约 98 ms |
| `authorized_keys` 恢复 | 通过，临时文件已删除 |

测试日志末尾输出：

```text
ssh-agent identities=2
agent public key authorized: existed=false
sequential agent connections= 8, elapsed=2080ms
agent connection reuse=5, elapsed=373ms
agent SFTP upload/download=PASS
concurrent agent connections=6, elapsed=327ms
unauthorized agent key rejected: elapsed=98ms
temporary authorized_keys removed
agent auth verified: sequential=8, reuse=5, concurrent=6, sftp=PASS, unauthorized=rejected
JSch agent authentication regression passed
```

#### 测试入口

- `src/test/java/cn/oyzh/easyshell/test/ShellJSchAgentAuthRegression.java`
- `docker/ssh/agent/test-agent.sh`
- `docker/ssh/agent/README.md`

测试命令：

```bash
bash docker/ssh/agent/test-agent.sh
```

#### 可靠性结论与兼容性

- Agent 认证不读取或依赖私钥文件；删除私钥后连续、复用、SFTP 和并发连接均正常。
- Agent 持有多把密钥时 JSch 能遍历身份并选中服务端授权的那一把。
- 单会话多命令复用正常，未出现会话断开、通道泄漏或并发认证串扰。
- 未授权 Agent 密钥快速失败，未回退到密码认证。
- 未修改 `ShellConnect`、`ShellKey`、认证方式枚举、配置格式或任何 sshd 版本代码。

## 12. 最终结论

本轮验证覆盖了连接、密码认证、公钥认证、SSH Agent 认证、双因素认证、认证失败交互、Shell 首屏 MOTD/提示符、Windows/macOS/Linux 平台识别、双向压缩协商、4 MiB Exec 传输、512 KiB SFTP 传输、SFTP 链接列表性能与懒解析、`tgz/xz/bz2` 文件压缩、单跳/双跳/三层跳板、多主机本地/远程/动态端口转发、HTTP CONNECT、SOCKS5、Docker 完整生命周期、Process、Server、并发和重连。2FA 使用本机 Docker 的 OpenSSH + `pam_google_authenticator` 环境验证密码 + TOTP，正确认证完成 Exec 和 SFTP，错误 TOTP 只提交一次并触发认证回调。Agent 认证使用本机独立 `ssh-agent` 和 Docker SSH 主机，私钥文件删除后仍完成连续、复用、SFTP 和 6 路并发认证，Agent 含未授权干扰密钥时可正确选择已授权密钥，未授权密钥快速拒绝。公钥认证在 Ubuntu 与 UOS 虚拟机使用工程内建生成器覆盖 RSA 普通/带口令、ECDSA 256/384/521、Ed25519，并分别验证密钥管理器和私钥文件路径；所有服务端启用算法均完成 Exec 和 SFTP，未授权密钥约 150 ms 拒绝，临时授权按原内容恢复。三层跳板使用三个不同节点，重连后跳板会话数和 SFTP 均再次校验；端口转发同时覆盖跨主机目标、并发连接、半关闭、畸形 SOCKS 请求恢复和监听端口释放。错误密码或用户名只提交一次凭证即进入交互认证，认证页返回正确凭证后可以恢复连接；`Too many authentication failures` 已完成异常映射。活跃 JSch 路径在无 MINA sshd/JGit 的隔离 classpath 下完成全量编译和字节码审计，主代码与测试代码中 sshd/JGit 引用均为 0。SFTP 目录列举行为已恢复为与 sshd 版本一致的“只判断链接类型、点击时解析目标”，阿里云 `/bin` 性能从 16.9 秒降至 0.54–0.58 秒。Shell 流已在连接前安装并复用，首屏 MOTD、登录信息和提示符恢复稳定回显。

未修改 `ssh2`、`sftp2`、`ssh1`、`sftp1` 及 `base-ssh` 中已有的 sshd 版本文件；后续移除项目级 MINA sshd 依赖时，只需由维护者屏蔽这些遗留目录和依赖，不需要回退本轮 JSch 实现。
