# easyshell SSH 模块（ssh2 包）代码审查文档
> 范围：`easyshell/src/main/java/cn/oyzh/easyshell/ssh2/`（含 docker/server/exec/process 子包），共 34 个 `.java`，全部存活。
> 说明：仅新增文档，未改动任何 `.java`。

---

# 一、核心 SSH 客户端（ssh2 包根）

## ShellBaseSSHClient
- 职责：抽象 SSH 客户端基类，封装 ClientSession 建立、认证方式选择、代理/跳板/心跳等连接配置以及通用命令执行，供 `ShellSSHClient` 继承。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `session` | `ClientSession` | 当前 SSH 会话 |
| `sshClient` | `ShellSSHJGitClient` | 底层 SSHD/JGit 客户端 |
| `osType` | `String` | 缓存的系统类型（Windows/Linux/Darwin/FreeBSD/Aix） |
| `userHome` | `String` | 缓存的用户目录 |
| `remoteCharset` | `String` | 缓存的远程字符集 |
| `shellConnect` | `ShellConnect` | 连接配置 |
| `environment` | `List<String>` | 环境变量 PATH 目录列表 |
| `keyStore` | `ShellKeyStore` | 密钥存储（final，单例） |
| `proxyConfigStore` | `ShellProxyConfigStore` | 代理配置存储（final，单例） |
| `authInteractive` | `ShellSSHAuthInteractive` | 交互式认证处理器 |
| `connectHost` | `String` | 缓存的连接地址 `host:port` |
| `jumpForwarder` | `SSHJumpForwarder2` | 跳板转发器 |
| `verifyFailureCallback` | `Function<ShellConnect, ShellConnect>` | 认证失败回调，默认 `ShellSSHUtil::onVerifyFailure` |
| `sessionLock` | `Object` | 会话创建锁（final） |
| `state` | `SimpleObjectProperty<ShellConnState>` | 连接状态属性（final） |
| `stateListener` | `ChangeListener<ShellConnState>` | 状态监听器（final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellBaseSSHClient(ShellConnect connect)` | 构造 | 保存 `shellConnect` |
| `getShellConnect()` | 获取连接配置 | 返回 `shellConnect` |
| `getSession()` | 获取会话 | 返回 `session` |
| `osType()` | 获取系统类型（synchronized） | `exec("which")` 判断 Windows，否则 `exec("uname")` |
| `exec(String command, int timeout)` | 执行命令并收集输出 | `newExecChannel` → 轮询 `isOpen` + 超时 → 按 `remoteCharset` 解码 |
| `exec(String command)` | 无超时执行 | 转调 `exec(command, -1)` |
| `getExportPath()` | 拼装 PATH 变量 | `initEnvironment()` 后按 Windows `;` / 其它 `:` 拼接并加 `$PATH` |
| `initEnvironment()` | 初始化环境目录（synchronized） | 各平台默认目录 + macos Docker 目录 |
| `isMacos()/isLinux()/isUnix()/isFreeBSD()/isWindows()` | 平台判断 | 基于 `osType()` |
| `getRemoteCharset()` | 获取远程字符集（synchronized） | Windows `chcp`、其它 `echo $LANG` |
| `getFileSeparator()` | 文件分隔符 | Windows `\` 其它 `/` |
| `getUserHome()` | 获取用户目录（synchronized） | Windows `echo %HOME%`，其它 `echo $HOME` |
| `newExecChannel(String command)` | 创建 exec 通道 | `takeSession` → 修正 PATH → `session.createExecChannel` → `open().verify` |
| `initHost()` | 初始化连接地址 | 启用跳板时 `jumpForwarder.forward` 得本地端口，否则直连 |
| `initProxy()` | 初始化代理连接器 | Http/Socks5 `ClientProxyConnector` |
| `initClient(int timeout)` | 初始化并启动客户端 | 构建 `ClientBuilder`、kex/压缩/签名/通道工厂、认证工厂、心跳、超时 |
| `setVerifyFailureCallback(...)/getVerifyFailureCallback()` | 认证失败回调读写 | - |
| `takeSession(int timeout)` | 获取/创建会话（含认证） | `connect` → 按认证方式 addIdentity → `auth().verify`；认证失败回调递归 |
| `initEnvironments()` | 初始化环境变量 Map | PATH、LANG |
| `close()` | 关闭资源 | 关闭 jumpForwarder/session/sshClient |
| `isConnected()` | 是否已连接 | `session != null && session.isOpen()` |
| `stateProperty()` | 状态属性 | 返回 `state` |

- 调用链：`ShellSSHClient.start → initClient → ShellSSHJGitClient(ClientBuilder.build) → takeSession → sshClient.connect/session.auth`；命令执行 `ShellSSHClient.exec → newExecChannel → takeSession`

## ShellSSHAuthInteractive
- 职责：SSH 交互式认证（UserInteraction 实现），通过弹窗让用户输入密码或验证码。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `interactive(ClientSession session, String name, String instruction, String lang, String[] prompt, boolean[] echo)` | 处理服务端交互提示 | 提示含 Password/密码 → `MessageBox.prompt(pleaseInputPassword)`；含 Verification code/验证码 → 弹验证码框；空则抛 `SSHException` |
| `getUpdatedPassword(ClientSession session, String prompt, String lang)` | 更新密码 | 抛 `UnsupportedOperationException` |

- 调用链：`ShellBaseSSHClient.authInteractive ← sshClient.setUserInteraction / jumpForwarder.setUserInteraction`；认证过程中由 SSHD 回调 `interactive → MessageBox.prompt`

## ShellSSHClient
- 职责：具体 SSH 客户端，管理 shell 通道（PTY）、隧道转发、X11 转发，并作为 docker/server/exec/process 各执行器与 sftp 客户端的入口。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `shellType` | `String` | shell 类型缓存 |
| `lastOutput` | `String` | 最后一次终端输出（用于解析工作目录） |
| `resolveWorkerDir` | `boolean` | 是否解析工作目录 |
| `workDirProperty` | `StringProperty` | 工作目录属性 |
| `tunnelForwarder` | `SSHTunnelingForwarder2` | SSH 隧道转发器 |
| `x11ConfigStore` | `ShellX11ConfigStore` | X11 配置存储（final 单例） |
| `sftpClient` | `ShellSFTPClient` | 与 SSH 共用会话的 SFTP 客户端（volatile） |
| `shell` | `ChannelShell` | shell 通道 |
| `dockerExec` | `ShellDockerExec` | docker 执行器 |
| `serverExec` | `ShellServerExec` | 服务器执行器（volatile） |
| `sshExec` | `ShellSSHExec` | ssh 执行器（volatile） |
| `processExec` | `ShellProcessExec` | 进程执行器（volatile） |
| `whoami` | `String` | 当前用户名缓存 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSSHClient(ShellConnect shellConnect)` | 构造 | `super(connect)` + `addStateListener(stateListener)` |
| `isResolveWorkerDir()/setResolveWorkerDir(boolean)` | 工作目录解析开关 | set 时触发 `doResolveWorkerDir(lastOutput)` |
| `resolveWorkerDir(String output)` | 解析工作目录 | `ShellSSHUtil.resolveWorkerDir` → `workDirProperty().set` |
| `doResolveWorkerDir(String output)` | 私有解析 | 见上 |
| `workDirProperty()` | 懒加载属性 | `SimpleStringProperty` |
| `sftpClient()` | 获取 SFTP 客户端 | 复用 `sshClient`、`session` 创建 `ShellSFTPClient` |
| `initTunneling(ClientSession session)` | 初始化隧道转发 | `SSHTunnelingForwarder2.forward` |
| `initChannel(ChannelShell channel)` | 初始化通道 | X11 转发配置 + agent 转发 |
| `close()` | 关闭 | 关闭 shell/各执行器/隧道/sftp，`super.close()`，state=CLOSED |
| `start(int timeout)` | 启动连接 | `initClient` → `takeSession` → state=CONNECTED/FAILED |
| `getShell()` | 获取 shell 通道 | 返回 `shell` |
| `openShell()` | 打开 shell 通道 | `takeSession` → PTY 配置 → `createShellChannel` → `initChannel` → `initTunneling` → open |
| `waitShellReady(int maxWait)` | 等待 shell 就绪 | 轮询 `shell.getInvertedOut() != null`，超时抛 IOException |
| `dockerExec()/serverExec()/sshExec()/processExec()` | 各执行器懒加载 | 分别 new 对应执行器 |
| `whoami()` | 当前用户 | `exec("whoami")`，Windows 去域前缀 |
| `getShellType()/getShellName()` | shell 类型/名称 | 委托 `serverExec().getShellType()` |
| `isZshType()/isBashType()` | shell 类型判断 | 后缀匹配 |
| `setPtySize(int columns, int rows, int sizeW, int sizeH)` | 设置 PTY 大小（synchronized） | `shell.sendWindowChange` |

- 调用链：`ShellSSHTtyConnector(构造函数) → client.openShell → takeSession/createShellChannel`；`ShellSSHTermWidget.createTtyConnector → ShellSSHTtyConnector`；`ShellSSHClient.sftpClient → ShellSFTPClient`；`ShellProcessExec/ShellDockerExec/ShellServerExec/ShellSSHExec ← ShellSSHClient.xxxExec()`

## ShellSSHIoConnector
- 职责：IoConnector 装饰器，在建立底层 IO 连接时把目标地址改写为代理地址。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `ioConnector` | `IoConnector` | 被装饰的真实连接器（final） |
| `sshClient` | `ShellSSHJGitClient` | 持有代理配置的客户端（final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSSHIoConnector(ShellSSHJGitClient sshClient, IoConnector ioConnector)` | 构造 | - |
| `connect(SocketAddress targetAddress, AttributeRepository context, SocketAddress localAddress)` | 建立连接 | 若 `sshClient.getProxyHost() != null` 则目标改写为 `InetSocketAddress(proxyHost, proxyPort)` |
| `close/isClosed/isClosing/addCloseFutureListener/removeCloseFutureListener/getManagedSessions/getIoServiceEventListener/setIoServiceEventListener` | 委托方法 | 直接转发到 `ioConnector` |

- 调用链：`ShellSSHJGitClient.createConnector → new ShellSSHIoConnector(this, super.createConnector())`；`ShellBaseSSHClient.initProxy/setClientProxyConnector` 设定 proxyHost/Port 后生效

## ShellSSHJGitClient
- 职责：继承 JGit 的 `JGitSshClient`，增加代理地址/端口字段并在创建连接器时套上 `ShellSSHIoConnector`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `proxyPort` | `int` | 代理端口 |
| `proxyHost` | `String` | 代理地址 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getProxyPort()/setProxyPort(int)` | 代理端口读写 | - |
| `getProxyHost()/setProxyHost(String)` | 代理地址读写 | - |
| `createConnector()` | 创建 IO 连接器 | 返回 `new ShellSSHIoConnector(this, super.createConnector())` |

- 调用链：`ShellBaseSSHClient.initClient → builder.factory(ShellSSHJGitClient::new) / setProxyHost/setProxyPort`；`ShellSSHIoConnector ← ShellSSHJGitClient.createConnector`

## ShellSSHKnownHostsServerKeyVerifier
- 职责：基于 known_hosts 文件的服务端密钥校验器，当前实现直接放行（`verifyServerKey` 恒真）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `INSTANCE` | `ShellSSHKnownHostsServerKeyVerifier` | 静态默认实例（委托 `ShellSSHServerKeyVerifier` + `ShellSSHUtil.getKnownHostsPath()`） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSSHKnownHostsServerKeyVerifier(ServerKeyVerifier delegate, Path file)` | 构造 | 转调父类 |
| `updateKnownHostsFile(...)` | 更新 known_hosts | 调用 `super.updateKnownHostsFile` |
| `verifyServerKey(ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey)` | 校验密钥 | 直接 `return true`（放行） |

- 调用链：`ShellBaseSSHClient.initClient → sshClient.setServerKeyVerifier(ShellSSHKnownHostsServerKeyVerifier.INSTANCE)`

## ShellSSHServerKeyVerifier
- 职责：通过弹窗让用户确认服务端密钥（含密钥变更确认）。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `verifyServerKey(ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey)` | 确认服务端密钥 | `MessageBox.confirm(ShellI18nHelper.sshTip2())` |
| `acceptModifiedServerKey(ClientSession clientSession, SocketAddress remoteAddress, KnownHostEntry entry, PublicKey expected, PublicKey actual)` | 确认变更后的密钥 | `MessageBox.confirm(ShellI18nHelper.sshTip1())` |

- 调用链：`ShellSSHKnownHostsServerKeyVerifier.INSTANCE(构造) → new ShellSSHServerKeyVerifier()`；校验时 JSch/SSHD 回调 `verifyServerKey/acceptModifiedServerKey → MessageBox.confirm`

## ShellSSHTermWidget
- 职责：SSH 终端组件，负责创建 SSH 终端 TTY 连接器并同步 PTY 尺寸。
- 字段：无字段（继承 `ShellStreamTermWidget`）。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `createTtyConnector(ShellSSHClient client)` | 创建 TTY 连接器 | `new ShellSSHTtyConnector(client)` + 监听 `terminalSizeProperty` 触发 `initPtySize` |
| `getTtyConnector()` | 获取连接器 | 转 `super.getTtyConnector()` 强转 |
| `client()` | 获取 SSH 客户端 | `getTtyConnector().getClient()` |
| `initPtySize()` | 初始化终端尺寸 | 取 `TermSize` 与面板宽高 → `client.setPtySize` |

- 调用链：`ShellSSHTermWidget.createTtyConnector → ShellSSHTtyConnector(client)`；`initPtySize → ShellSSHClient.setPtySize → shell.sendWindowChange`

## ShellSSHTtyConnector
- 职责：JediTerm 的 TtyConnector 实现，把 SSH shell 通道的输入输出流桥接给终端，并顺带解析工作目录。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `client` | `ShellSSHClient` | 关联的 SSH 客户端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSSHTtyConnector(ShellSSHClient client)` | 构造 | `super(client.getCharset())` + `client.openShell()` |
| `getClient()` | 获取客户端 | 返回 `client` |
| `isConnected()` | 是否连接 | `client.isConnected()` |
| `ready()` | 就绪并初始化流 | `waitShellReady(1000)` → `shell.getInvertedOut/In` 建 reader/writer |
| `getName()` | 名称 | 返回 `"ssh-tty"` |
| `close()` | 关闭 | `super.close()` + `IOUtil.close(client)` |
| `doRead(char[] buf, int offset, int len)` | 读取回调 | 转字符串后线程内 `client.resolveWorkerDir(str)` |
| `input()/output()` | 输入/输出流 | `client.getShell().getInvertedOut/In` |

- 调用链：`ShellSSHTermWidget.createTtyConnector → ShellSSHTtyConnector → ShellSSHClient.openShell`；`doRead → ShellSSHClient.resolveWorkerDir → ShellSSHUtil.resolveWorkerDir`

## ShellSSHUtil
- 职责：SSH 模块工具类，提供工作目录解析、认证失败处理、连接对象互转、命令历史、已知主机路径等。
- 字段：无字段（纯静态工具类）。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `resolveWorkerDir(String output, String homeDir)` | 解析终端输出中的工作目录 | 去 ANSI/控制字符，按 linux/unix/macos/windows 提示符格式截取，`~` 与 `homeDir` 拼接 |
| `onVerifyFailure(ShellConnect connect)` | 认证失败处理 | `FXUtil.runWait → ShellViewFactory.sshAuth` 弹出认证窗口取回 connect |
| `getKnownHostsPath()` | 已知主机文件路径 | `Path.of(ShellConst.getStorePath(), "known_hosts")` |
| `convert(ShellConnect connect)` | ShellConnect → SSHConnect | 按管理/证书/密码/agent 认证方式填充 |
| `convert(SSHConnect connect)` | SSHConnect → ShellConnect | key 认证时写入 `ShellKeyStore` |
| `histories(ShellSSHClient client, String kw, int limit)` | 命令历史 | `client.serverExec().persistentCommand()` → `history(limit, kw)` |
| `isSamePath(String path1, String path2)` | 路径比较 | `correctPath` 后比较 |
| `correctPath(String path1)` | 纠正路径 | 去尾部 `/` |

- 调用链：`ShellSSHTtyConnector.doRead → ShellSSHClient.resolveWorkerDir → ShellSSHUtil.resolveWorkerDir`；`ShellBaseSSHClient.verifyFailureCallback(默认) = ShellSSHUtil::onVerifyFailure`

---

# 二、docker 子包

## ShellDockerCommit
- 职责：docker commit 命令参数载体（POJO）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `tag` | `String` | 标签 |
| `comment` | `String` | 备注 |
| `repository` | `String` | 仓库 |
| `containerId` | `String` | 容器 id |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getTag()/setTag(String)` | 标签读写 | - |
| `getComment()/setComment(String)` | 备注读写 | - |
| `getRepository()/setRepository(String)` | 仓库读写 | - |
| `getContainerId()/setContainerId(String)` | 容器 id 读写 | - |

- 调用链：`ShellDockerExec.docker_commit(ShellDockerCommit) → docker_commit_cmd → client.exec`

## ShellDockerContainer
- 职责：docker 容器信息载体（`docker ps` 单行解析结果）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `containerId` | `String` | 容器 id |
| `image` | `String` | 镜像 |
| `command` | `String` | 命令 |
| `created` | `String` | 创建时间 |
| `status` | `String` | 状态 |
| `ports` | `String` | 端口 |
| `names` | `String` | 名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getContainerId()/setContainerId(String)` | 容器 id 读写 | - |
| `getImage()/setImage(String)` | 镜像读写 | - |
| `getCommand()/setCommand(String)` | 命令读写 | - |
| `getCreated()/setCreated(String)` | 创建时间读写 | - |
| `getStatus()/setStatus(String)` | 状态读写 | - |
| `getPorts()/setPorts(String)` | 端口读写 | - |
| `getNames()/setNames(String)` | 名称读写 | - |
| `isExited()` | 是否已退出 | `status` 含 `Exited` |
| `isPaused()` | 是否已暂停 | `status` 含 `Paused` |
| `isRunning()` | 是否运行中 | `!isExited()` |

- 调用链：`ShellDockerParser.ps(output) → new ShellDockerContainer().setXxx`

## ShellDockerExec
- 职责：docker 命令执行器，提供 ps/images/rm/run/save/tag/commit/inspect/update 等命令封装（AutoCloseable）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `client` | `ShellSSHClient` | 底层 SSH 客户端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellDockerExec(ShellSSHClient client)` | 构造 | 保存 client |
| `getClient()` | 获取客户端 | - |
| `getContainerFormat()/getImageFormat()/getHistoryFormat()` | 获取 `--format` 模板 | 按 Windows/其它返回不同引号 |
| `docker_ps()/docker_ps_a()/docker_ps_exited()` | 容器列表 | `client.exec("docker ps ...")` |
| `docker_rm/docker_rm_f/docker_start/docker_restart/docker_pause/docker_unpause/docker_stop/docker_kill/docker_logs/docker_rename/docker_port(String)` | 容器操作 | 拼接命令后 `client.exec` |
| `docker_rmi(String)`（@Deprecated） | 删除镜像 | `client.exec("docker rmi ...")` |
| `docker_rmi_f(String)`（@Deprecated） | 强制删除镜像 | `client.exec("docker rmi -f ...")` |
| `docker_rmi(ShellDockerRmi rmi)` | 删除镜像（新） | 拼接 `-f` 与 id/name |
| `docker_inspect(String id)` / `docker_inspect(String id, String format)` | 检查 | 带 format 时 `exec(cmd, 3000)` |
| `docker_images()` | 镜像列表 | `exec("docker images --format ...", 3000)` |
| `docker_resource(String id)` | 获取资源 | 6 次 `docker_inspect` 拼接 |
| `docker_update(ShellDockerResource resource, String id)` | 修改资源 | 按字段拼 `--memory/--cpu-shares/--cpus/...` |
| `docker_run(ShellDockerRun run)` / `docker_run_cmd(ShellDockerRun run)` | 运行容器 | 生成 `docker run` 命令并执行 |
| `docker_save(ShellDockerSave save)` / `docker_save_cmd(...)` | 保存镜像 | 生成 `docker save -o ...` |
| `docker_tag(ShellDockerTag tag)` / `docker_tag_cmd(...)` | 打标签 | 生成 `docker tag` |
| `docker_commit(ShellDockerCommit commit)` / `docker_commit_cmd(...)` | 提交容器 | 生成 `docker commit` |
| `docker_info()/docker_version()/docker_compose_version()/docker_restart()` | 信息/版本/重启 | `docker info`、`docker version`、`systemctl restart docker` |
| `docker_history(String imageId)` | 镜像历史 | 带 `getHistoryFormat` |
| `docker_container_prune_f/image_prune_f/network_prune_f/volume_prune_f()` | 清理 | `docker xxx prune -f` |
| `close()` | 关闭 | `client = null` |
| `getDaemonFilePath()` | daemon.json 路径 | macos/windows/linux 分平台返回 |

- 调用链：`ShellSSHClient.dockerExec() → new ShellDockerExec(this)`；各 `docker_xxx → client.exec(command) → ShellBaseSSHClient.exec → newExecChannel`

## ShellDockerImage
- 职责：docker 镜像信息载体（`docker images` 单行解析结果）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `imageId` | `String` | 镜像 id |
| `repository` | `String` | 仓库 |
| `tag` | `String` | 标签 |
| `created` | `String` | 创建时间 |
| `size` | `String` | 大小 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getImageId()/setImageId(String)` | 镜像 id 读写 | - |
| `getRepository()/setRepository(String)` | 仓库读写 | - |
| `getTag()/setTag(String)` | 标签读写 | - |
| `getCreated()/setCreated(String)` | 创建时间读写 | - |
| `getSize()/setSize(String)` | 大小读写 | - |
| `getImageName()` | 镜像名 | `repository + ":" + tag` |

- 调用链：`ShellDockerParser.images(output) → new ShellDockerImage().setXxx`

## ShellDockerImageHistory
- 职责：docker 镜像历史信息载体（`docker history` 单行解析结果）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `imageId` | `String` | 镜像 id |
| `created` | `String` | 创建事件 |
| `createdBy` | `String` | 创建人 |
| `size` | `String` | 大小 |
| `comment` | `String` | 命令 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getImageId()/setImageId(String)` | 镜像 id 读写 | - |
| `getCreated()/setCreated(String)` | 创建时间读写 | - |
| `getCreatedBy()/setCreatedBy(String)` | 创建人读写 | - |
| `getSize()/setSize(String)` | 大小读写 | - |
| `getComment()/setComment(String)` | 命令读写 | - |

- 调用链：`ShellDockerParser.history(output) → new ShellDockerImageHistory().setXxx`

## ShellDockerParser
- 职责：docker 命令文本输出解析器，全部为静态方法（私有构造禁止实例化）。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ps(String output)` | 解析容器列表 | 按 `\n`/`\t` 拆分 → `ShellDockerContainer` |
| `images(String output)` | 解析镜像列表 | 按 `\n`/`\t` 拆分 → `ShellDockerImage` |
| `resource(String output)` | 解析资源 | 按 `\t` 拆 6 列 → `ShellDockerResource` |
| `port(String output)` | 解析端口映射 | 按 `->` 拆分 → `ShellDockerPort` |
| `history(String output)` | 解析镜像历史 | 按 `\r\t` 拆列 → `ShellDockerImageHistory` |

- 调用链：`ShellDockerExec.docker_ps/images/... 输出 → UI 层调用 ShellDockerParser.ps/images/...`（解析结果与 `ShellDockerContainer`/`ShellDockerImage` 等一一对应）

## ShellDockerPort
- 职责：docker 端口映射信息载体（内外端口）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `innerPort` | `String` | 内部端口 |
| `outerPort` | `String` | 外部端口 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getInnerPort()/setInnerPort(String)` | 内部端口读写 | - |
| `getOuterPort()/setOuterPort(String)` | 外部端口读写 | - |

- 调用链：`ShellDockerParser.port(output) → new ShellDockerPort().setXxx`

## ShellDockerResource
- 职责：docker 容器资源限制载体（内存、cpu 等长整型字段）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `memory` | `long` | 内存 |
| `memorySwap` | `long` | 内存交换区 |
| `cpuShares` | `long` | cpu 份额 |
| `nanoCpus` | `long` | cpus 核心 |
| `cpuPeriod` | `long` | cpu 时间 |
| `cpuQuota` | `long` | cpu 配额 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getMemory()/setMemory(long)` | 内存读写 | - |
| `getMemorySwap()/setMemorySwap(long)` | 交换区读写 | - |
| `getCpuShares()/setCpuShares(long)` | cpu 份额读写 | - |
| `getNanoCpus()/setNanoCpus(long)` | cpus 核心读写 | - |
| `getCpuPeriod()/setCpuPeriod(long)` | cpu 时间读写 | - |
| `getCpuQuota()/setCpuQuota(long)` | cpu 配额读写 | - |

- 调用链：`ShellDockerParser.resource(output) → new ShellDockerResource().setXxx`；`ShellDockerExec.docker_update(resource, id) → docker update` 命令

## ShellDockerRmi
- 职责：docker 删除镜像参数载体。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `force` | `boolean` | 是否强制删除 |
| `imageId` | `String` | 镜像 id |
| `imageName` | `String` | 镜像名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isForce()/setForce(boolean)` | 是否强制删除读写 | - |
| `getImageId()/setImageId(String)` | 镜像 id 读写 | - |
| `getImageName()/setImageName(String)` | 镜像名称读写 | - |

- 调用链：`ShellDockerExec.docker_rmi(ShellDockerRmi) → docker_rmi 命令`

## ShellDockerRun
- 职责：docker run 参数载体，并内置 DockerPort/DockerVolume/DockerEnv/DockerLabel 四个静态内部类及其表格控件工厂方法。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `imageId` | `String` | 镜像 id |
| `imageName` | `String` | 镜像名称 |
| `containerName` | `String` | 容器名称 |
| `i` | `boolean` | -i 参数 |
| `t` | `boolean` | -t 参数 |
| `d` | `boolean` | -d 参数 |
| `rm` | `boolean` | --rm |
| `privileged` | `boolean` | --privileged 参数 |
| `restart` | `String` | 重启策略 |
| `params` | `String` | 附加参数 |
| `ports` | `List<DockerPort>` | 端口 |
| `envs` | `List<DockerEnv>` | 环境变量 |
| `labels` | `List<DockerLabel>` | 标签 |
| `volumes` | `List<DockerVolume>` | 卷 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getContainerName()/setContainerName(String)` | 容器名称 | - |
| `isI()/setI(boolean)`、`isT()/setT`、`isD()/setD` | i/t/d 参数 | - |
| `isRm()/setRm(boolean)` | --rm 参数 | - |
| `isPrivileged()/setPrivileged(boolean)` | 特权模式 | - |
| `getRestart()/setRestart(String)` | 重启策略 | - |
| `getParams()/setParams(String)` | 附加参数 | - |
| `getPorts()/setPorts(List<DockerPort>)` | 端口 | - |
| `getEnvs()/setEnvs(List<DockerEnv>)` | 环境变量 | - |
| `getLabels()/setLabels(List<DockerLabel>)` | 标签 | - |
| `getVolumes()/setVolumes(List<DockerVolume>)` | 卷 | - |
| `getImageId()/setImageId`、`getImageName()/setImageName` | 镜像 | - |
| `isIgnoreRestart()` | 是否忽略重启策略 | restart 为空或 `no` |

- 内部类 `DockerPort`：字段 `type`(默认 `tcp`)、`outerPort`、`innerPort`；方法 `getType/setType`、`getOuterPort/setOuterPort`、`getInnerPort/setInnerPort`、`isTcp()`，以及 UI 控件工厂 `getTypeControl()`、`getOuterPortControl()`、`getInnerPortControl()`（`@JSONField(serialize=false,deserialize=false)`）。
- 内部类 `DockerVolume`：字段 `outerVolume`、`innerVolume`；控件 `getOuterVolumeControl()`、`getInnerVolumeControl()`。
- 内部类 `DockerEnv`：字段 `name`、`value`；控件 `getNameControl()`、`getValueControl()`。
- 内部类 `DockerLabel`：字段 `name`、`value`；控件 `getNameControl()`、`getValueControl()`。
- 调用链：`ShellDockerExec.docker_run(ShellDockerRun) → docker_run_cmd(run) → client.exec`；内部类控件用于 UI 表格编辑（`TableViewUtil.selectRowOnMouseClicked`）

## ShellDockerSave
- 职责：docker save 参数载体。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `imageId` | `String` | 镜像 id |
| `imageName` | `String` | 镜像名称 |
| `filePath` | `String` | 文件路径 |
| `quiet` | `boolean` | 是否静默 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getImageId()/setImageId(String)` | 镜像 id 读写 | - |
| `getFilePath()/setFilePath(String)` | 文件路径读写 | - |
| `isQuiet()/setQuiet(boolean)` | 是否静默读写 | - |
| `getImageName()/setImageName(String)` | 镜像名称读写 | - |

- 调用链：`ShellDockerExec.docker_save(ShellDockerSave) → docker_save_cmd(save) → client.exec`

## ShellDockerTag
- 职责：docker tag 参数载体。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `imageName` | `String` | 镜像名称 |
| `newImageName` | `String` | 新镜像名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getImageName()/setImageName(String)` | 镜像名称读写 | - |
| `getNewImageName()/setNewImageName(String)` | 新镜像名称读写 | - |

- 调用链：`ShellDockerExec.docker_tag(ShellDockerTag) → docker_tag_cmd(tag) → client.exec`

---

# 三、server 子包

## ShellServerDisk
- 职责：服务器磁盘读写速度计算器（基于两次采样差值）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `lastUpdateTime` | `long` | 最后更新时间 |
| `lastRead` | `double` | 最后读取值（初值 -1） |
| `lastWrite` | `double` | 最后写入值（初值 -1） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `calcSpeed(double[] data)` | 计算速度 | 首次或含 -1 返回 `{-1,-1}`，否则按时间差换算 MB/s（`*512/1024/1024/cost`） |

- 调用链：`ShellServerExec.monitor → this.disk.calcSpeed(data) → ShellServerMonitor.setDiskReadSpeed/setDiskWriteSpeed`

## ShellServerExec
- 职责：服务器执行器，采集服务器信息与监控指标（CPU/内存/磁盘/网络/时间等），并执行文件移动/复制/删除/压缩等操作（AutoCloseable）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `client` | `ShellSSHClient` | 底层 SSH 客户端 |
| `disk` | `ShellServerDisk` | 磁盘速度计算器 |
| `network` | `ShellServerNetwork` | 网络速度计算器 |
| `lastMonitorTime` | `long` | 上一次监控缓存时间 |
| `lastMonitor` | `ShellServerMonitor` | 监控缓存记录 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellServerExec(ShellSSHClient client)` | 构造 | new `ShellServerDisk`/`ShellServerNetwork` |
| `getClient()` | 获取客户端 | - |
| `info()` | 获取服务信息 | 8 个虚拟线程并发采集 arch/uname/ulimit/uptime/locale/timezone/memory/shellName，`DownLatch` 汇总 |
| `monitor()` | 获取监控信息（3 秒缓存） | 4 线程并发 cpuUsage/memoryUsage/disk/network，`DownLatch` 汇总 |
| `cpuUsage()` | CPU 使用率 | 按 macos/windows/unix/linux 分别执行 `top`/`wmic`/`vmstat` 等 |
| `memoryUsage()` | 内存使用率 | macos `vm_stat`、windows `wmic OS`、unix `sysctl`、linux `free` |
| `ulimit()` | 文件限制 | `ulimit -n` |
| `uname()` | 系统名称 | `uname -rs` / windows `hostname` |
| `arch()` | 系统架构 | `uname -m` / windows `wmic os get osarchitecture` |
| `totalMemory()` | 内存大小(MB) | macos/windows/unix/linux 分支 |
| `disk()` | 磁盘读写 | macos `top`、windows `typeperf`、unix `gstat`、linux `/proc/diskstats` |
| `network()` | 网络收发 | macos `top`、windows `typeperf`、unix `netstat`、linux `/proc/net/dev` |
| `uptime()` | 启动时间 | windows `wmic` / 其它 `uptime` |
| `move(String src, String dst)` | 移动文件 | linux/macos/freebsd `mv -f`，windows `move /Y` |
| `copy(String src, String dst)` | 复制文件 | `cp -rf` / windows `copy /Y` |
| `forceDel(List<String> files)` / `forceDel(String file, boolean isFile)` | 强制删除 | `rm -rf` / windows `del`/`rmdir` |
| `compress(String file, String type)` | 压缩 | 支持 tgz/xz/bz2/lz/lzo/zst/rar/7z/zip |
| `uncompress(String file)` | 解压 | 7z/rar/zip 用 `7z x`，其它 `tar -axof` |
| `timezone()` | 时区 | windows `tzutil`、linux `/etc/timezone`、unix `/var/db/zoneinfo` 等 |
| `locale()` | 区域 | Windows `chcp` 转换，其它 `echo $LANG` |
| `persistentCommand()` | 持久化历史 | linux/unix `history -a` |
| `history(Integer limit, String kw)` | 命令历史 | 读取 `~/.<shell>_history`，过滤/截断后倒序 |
| `getShellType()` | 终端类型 | `echo $SHELL` / `echo %ComSpec%` |
| `close()` | 关闭 | 置空 disk/client/network |

- 调用链：`ShellSSHClient.serverExec() → new ShellServerExec(this)`；`ShellSSHClient.getShellType → serverExec().getShellType()`；`ShellSSHUtil.histories → client.serverExec().history`；`ShellProcessExec.ps(windows) → serverExec.totalMemory()`

## ShellServerInfo
- 职责：服务器基础信息载体（架构、系统、内存、时区、shell 名等）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `ulimit` | `String` | 文件限制 |
| `arch` | `String` | 系统架构 |
| `uname` | `String` | 系统名称 |
| `uptime` | `String` | 启动时间 |
| `locale` | `String` | 本地化信息 |
| `timezone` | `String` | 时区信息 |
| `shellName` | `String` | shell 名称 |
| `totalMemory` | `double` | 总内存 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getTimezone()/setTimezone(String)` | 时区读写 | - |
| `getLocale()/setLocale(String)` | 本地化读写 | - |
| `getUptime()/setUptime(String)` | 启动时间读写 | - |
| `getUlimit()/setUlimit(String)` | 文件限制读写 | - |
| `getArch()/setArch(String)` | 架构读写 | - |
| `getUname()/setUname(String)` | 系统名读写 | - |
| `getTotalMemory()/setTotalMemory(double)` | 总内存读写 | - |
| `getTotalMemoryInfo()` | 总内存字符串 | `totalMemory + "MB"` |
| `getShellName()/setShellName(String)` | shell 名读写 | - |

- 调用链：`ShellServerExec.info() → new ShellServerInfo().setXxx`

## ShellServerMonitor
- 职责：服务器监控信息载体（CPU/内存/磁盘/网络速率）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `cpuUsage` | `double` | cpu 使用率 |
| `memoryUsage` | `double` | 内存使用率 |
| `diskReadSpeed` | `double` | 磁盘读取速度 |
| `diskWriteSpeed` | `double` | 磁盘写入速度 |
| `networkSendSpeed` | `double` | 网络发送速度 |
| `networkReceiveSpeed` | `double` | 网络接收速度 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getDiskReadSpeed()/setDiskReadSpeed(double)` | 磁盘读速读写 | - |
| `getDiskWriteSpeed()/setDiskWriteSpeed(double)` | 磁盘写速读写 | - |
| `getNetworkSendSpeed()/setNetworkSendSpeed(double)` | 网络发送读写 | - |
| `getNetworkReceiveSpeed()/setNetworkReceiveSpeed(double)` | 网络接收读写 | - |
| `getCpuUsage()/setCpuUsage(double)` | CPU 使用率读写 | - |
| `getMemoryUsage()/setMemoryUsage(double)` | 内存使用率读写 | - |

- 调用链：`ShellServerExec.monitor() → new ShellServerMonitor().setXxx`

## ShellServerNetwork
- 职责：服务器网络速率计算器（基于两次采样差值）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `lastUpdateTime` | `long` | 最后更新时间 |
| `lastSend` | `double` | 最后发送值（初值 -1） |
| `lastReceive` | `double` | 最后接收值（初值 -1） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `calcSpeed(double[] data)` | 计算速度 | 首次/含 -1/回绕时返回 `{-1,-1}`，否则换算 KB/s |

- 调用链：`ShellServerExec.monitor → this.network.calcSpeed(data)`；`ShellProcessExec.calcNetworkUplinkAndDownlink_* → new ShellServerNetwork().calcSpeed`

---

# 四、exec 子包

## ShellSSHDiskInfo
- 职责：SSH 磁盘信息载体（`df -h` 或 `wmic logicaldisk` 解析结果）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `fileSystem` | `String` | 文件系统 |
| `size` | `String` | 大小 |
| `used` | `String` | 已用 |
| `avail` | `String` | 可用 |
| `use` | `String` | 使用率 |
| `mountedOn` | `String` | 挂载点 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getFileSystem()/setFileSystem(String)` | 文件系统读写 | - |
| `getSize()/setSize(String)` | 大小读写 | - |
| `getUsed()/setUsed(String)` | 已用读写 | - |
| `getAvail()/setAvail(String)` | 可用读写 | - |
| `getUse()/setUse(String)` | 使用率读写 | - |
| `getMountedOn()/setMountedOn(String)` | 挂载点读写 | - |

- 调用链：`ShellSSHExec.disk_info() → ShellSSHExecParser.diskForWindows/Macos/Linux → new ShellSSHDiskInfo().setXxx`

## ShellSSHExec
- 职责：SSH 执行器，采集系统硬件信息（cpu/磁盘/网卡/内存/gpu）并执行文件内容查看、echo、cat、append 等命令（AutoCloseable）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `client` | `ShellSSHClient` | 底层 SSH 客户端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSSHExec(ShellSSHClient client)` | 构造 | 保存 client |
| `cpu_info()` | CPU 信息 | macos `sysctl machdep.cpu`、windows `wmic cpu`、unix `sysctl`、linux `lscpu` |
| `disk_info()` | 磁盘信息 | windows `wmic logicaldisk`、macos/linux `df -h` → `ShellSSHExecParser` |
| `network_interface_info()` | 网卡信息 | windows `ipconfig /all`、其它 `ifconfig`/`ip addr` |
| `memory_info()` | 内存信息 | macos `system_profiler`、windows `wmic memorychip`、unix `dmesg`、linux `lshw`/`dmidecode` |
| `gpu_info()` | GPU 信息 | macos `system_profiler`、windows `nvidia-smi`/`wmic`、unix `pciconf`、linux `nvidia-smi`/`lspci`/`lshw`/`lsmod` |
| `cat_profile()/cat_environment()/cat_resolv()/cat_hosts()/cat_sshd_config()/cat_bash_bashrc()/cat_user_profile()/cat_user_bash_profile()/cat_user_bashrc()/cat_user_zshrc()` | 查看各类配置文件 | 均转调 `cat_file(...)` |
| `source(String file)` | source 命令 | `exec("source " + file)` |
| `cat_file(String filePath)` | 查看文件内容 | windows `type`，其它 `cat` |
| `echo(String text)` / `echo(String text, String file)` | echo 命令 | `echo text` / `echo "text" > file` |
| `cat_file(String sourceFile, String targetFile)` | 复制文件内容 | macos/linux `echo "$(cat ...)"`，windows `type ... > ...` |
| `append(String text, String file)` | 追加内容 | `echo "text" >> file` |
| `append_file(String sourceFile, String targetFile)` | 追加文件 | 平台分支 |
| `whoami()` | 当前用户 | `exec("whoami")` |
| `close()` | 关闭 | `client = null` |

- 调用链：`ShellSSHClient.sshExec() → new ShellSSHExec(this)`；`disk_info → ShellSSHExecParser.diskFor*`；所有命令 → `ShellSSHClient.exec → ShellBaseSSHClient.exec`

## ShellSSHExecParser
- 职责：SSH 执行结果解析器（磁盘信息），全静态方法。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `diskForLinux(String output)` | 解析 linux `df -h` | 按空白拆 6 列 → `ShellSSHDiskInfo` |
| `diskForMacos(String output)` | 解析 macos `df -h` | 使用率/挂载点取第 7、8 列 |
| `diskForWindows(String output)` | 解析 windows `wmic logicaldisk` | free/size 换算并 `NumberUtil.formatSize`/`scale` |

- 调用链：`ShellSSHExec.disk_info() → ShellSSHExecParser.diskForWindows/Macos/Linux`

---

# 五、process 子包

## ShellProcessAttr
- 职责：进程属性（pid/状态/用户）及 CPU 使用率计算，主要供 Windows 分支使用。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `pid` | `String` | pid |
| `stat` | `String` | 状态 |
| `user` | `String` | 用户名 |
| `lastTime` | `long` | 上一次更新时间 |
| `lastCpuUsage` | `double` | 上一次 cpu 使用率 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getPid()/setPid(String)` | pid 读写 | - |
| `getStat()/setStat(String)` | 状态读写 | - |
| `getUser()/setUser(String)` | 用户名读写 | - |
| `calcCpuUsage(double cpuUsage)` | 计算 cpu 使用率 | 首次返回 0，否则按时间差估算并取绝对值 |

- 调用链：`ShellProcessExec.ps(windows) → processAttr.get(pid)/new ShellProcessAttr`；`ShellProcessParser.psForWindows → attr.calcCpuUsage(usage)`

## ShellProcessExec
- 职责：进程执行器，按平台采集进程列表（含网络上下行速度）并支持 kill/forceKill（AutoCloseable）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `client` | `ShellSSHClient` | 底层 SSH 客户端 |
| `totalMemory` | `long` | 总内存（仅 windows，初值 -1） |
| `processAttr` | `Map<String, ShellProcessAttr>` | 进程属性（仅 windows） |
| `networksSpeed` | `Map<String, ShellServerNetwork>` | 网络上下行速度计算器 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellProcessExec(ShellSSHClient client)` | 构造 | 保存 client |
| `close()` | 关闭 | 清空并置空各 Map 与 client |
| `ps()` | 获取进程信息 | windows/linux/macos/unix 分支，`DownLatch` 并发解析进程与网络速度 |
| `kill(int pid)` | 杀死进程 | windows `taskkill /PID`，其它 `kill` |
| `forceKill(int pid)` | 强制杀死 | windows `taskkill /F /PID`，其它 `kill -9` |
| `calcNetworkUplinkAndDownlink_linux()` | 计算网络上下行(linux) | 复用 `networksSpeed` 中 `ShellServerNetwork.calcSpeed` |
| `getNetworkUplinkAndDownlink_linux()` | 获取网络上下行(linux) | `/proc/*/net/dev` 脚本解析 |
| `calcNetworkUplinkAndDownlink_macos()` | 计算网络上下行(macos) | 同上 |
| `getNetworkUplinkAndDownlink_macos()` | 获取网络上下行(macos) | `nettop` 解析 |
| `getNetworkUplinkAndDownlink_windows()` | 获取网络上下行(windows) | PowerShell `Get-Counter` 解析 |

- 调用链：`ShellSSHClient.processExec() → new ShellProcessExec(this)`；`ps → ShellProcessParser.psForWindows/psForLinux/psForUnix + parseNetworkSpeed_*`；windows 分支 `client.serverExec().totalMemory()`

## ShellProcessInfo
- 职责：进程信息载体（ObjectCopier 实现），含 CPU/内存/网络等使用数据及展示格式化方法。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `user` | `String` | 用户 |
| `pid` | `int` | 进程 id |
| `stat` | `String` | 状态 |
| `start` | `String` | 开始时间 |
| `time` | `String` | cpu 总使用时间 |
| `cpuUsage` | `double` | cpu 使用率 |
| `memUsage` | `double` | 内存使用率 |
| `command` | `String` | 启动命令 |
| `rss` | `double` | rss |
| `networkSend` | `double` | 网络发送（初值 -1） |
| `networkRecv` | `double` | 网络接收（初值 -1） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getRss()/setRss(double)` | rss 读写 | - |
| `getUser()/setUser(String)` | 用户读写 | - |
| `getStat()/setStat(String)` | 状态读写 | - |
| `getCommand()/setCommand(String)` | 启动命令读写 | - |
| `getPid()/setPid(int)` | pid 读写 | - |
| `getCpuUsage()/setCpuUsage(double)` | cpu 使用率读写 | - |
| `getMemUsage()/setMemUsage(double)` | 内存使用率读写 | - |
| `getStart()/setStart(String)` | 开始时间读写 | - |
| `getTime()/setTime(String)` | cpu 总时间读写 | - |
| `getTimeData()` | 展示用时间 | 为空返回 `-` |
| `getNetworkSend()/setNetworkSend(double)` | 网络发送读写 | - |
| `getNetworkSendData()` | 展示用发送 | -1 返回 `-`，否则 `ShellProcessParser.formatSpeed(...,2)` |
| `getNetworkRecv()/setNetworkRecv(double)` | 网络接收读写 | - |
| `getNetworkRecvData()` | 展示用接收 | 同上 |
| `copy(ShellProcessInfo t1)` | 拷贝属性 | 复制各展示字段 |

- 调用链：`ShellProcessParser.psFor* → new ShellProcessInfo().setXxx`；`getNetworkSendData/RecvData → ShellProcessParser.formatSpeed`

## ShellProcessParser
- 职责：进程命令输出解析器，按平台解析进程列表与网络速度映射，全静态方法。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `psForLinux(String output)` | 解析 linux `ps -auxe` | 按空白拆列 → `ShellProcessInfo`（rss 转 MB） |
| `psForUnix(String output)` | 解析 unix 进程 | 同 linux |
| `psForMacos(String output)` | 解析 macos 进程 | 同 linux |
| `psForWindows(String output, Map<String, ShellProcessAttr> attrs, long totalMemory)` | 解析 windows 进程 | CSV 拆分 + attr 计算 cpu/内存使用率 |
| `parseNetworkSpeed_linux(List<ShellProcessInfo> infos, Map<String, double[]> speed)` | 关联网络速度(linux) | 按 pid 匹配设置 send/recv |
| `parseNetworkSpeed_macos(...)` | 关联网络速度(macos) | key 形如 `name.pid` 取 pid |
| `parseNetworkSpeed_windows(...)` | 关联网络速度(windows) | 按 pid 匹配 |
| `formatSpeed(double size, Integer scale)` | 格式化速度 | 保留 2 位并加 `KB` |

- 调用链：`ShellProcessExec.ps → ShellProcessParser.psForWindows/psForLinux/psForUnix + parseNetworkSpeed_*`；`ShellProcessInfo.getNetworkSendData → ShellProcessParser.formatSpeed`

---

> 覆盖类清单（34）：ShellBaseSSHClient、ShellSSHAuthInteractive、ShellSSHClient、ShellSSHIoConnector、ShellSSHJGitClient、ShellSSHKnownHostsServerKeyVerifier、ShellSSHServerKeyVerifier、ShellSSHTermWidget、ShellSSHTtyConnector、ShellSSHUtil、ShellDockerCommit、ShellDockerContainer、ShellDockerExec、ShellDockerImage、ShellDockerImageHistory、ShellDockerParser、ShellDockerPort、ShellDockerResource、ShellDockerRmi、ShellDockerRun、ShellDockerSave、ShellDockerTag、ShellServerDisk、ShellServerExec、ShellServerInfo、ShellServerMonitor、ShellServerNetwork、ShellSSHDiskInfo、ShellSSHExec、ShellSSHExecParser、ShellProcessAttr、ShellProcessExec、ShellProcessInfo、ShellProcessParser。
