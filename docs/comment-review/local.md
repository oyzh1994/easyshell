# easyshell 本地终端（local 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/local/，共 3 个 .java，全部存活。

本包实现本地终端连接：`ShellLocalClient` 维护本地连接的连接状态（本地连接始终视为已连接），`ShellLocalTermWidget` 基于进程终端组件（`ShellProcessTermWidget`）创建本地 TTY 连接器，`ShellLocalTtyConnector` 包装 `PtyProcess` 完成本地终端读写。

## ShellLocalClient

- 职责：本地终端客户端，仅负责本地连接的连接状态维护（无网络握手，`isConnected` 恒为 true）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| shellConnect | `final ShellConnect` | 本地连接配置 |
| state | `final SimpleObjectProperty<ShellConnState>` | 连接状态，初值 NOT_INITIALIZED |
| stateListener | `final ChangeListener<ShellConnState>` | 状态监听器，回调 `onStateChanged` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellLocalClient(ShellConnect shellConnect)` | 构造 | 保存连接，注册 `stateListener` |
| `stateProperty()` | 状态属性 | 返回 `state` |
| `start(int timeout)` | 启动连接 | 已连接则返回；否则置 CONNECTING → CONNECTED；异常置 FAILED 并抛出；finally `SystemUtil.gc()` |
| `close()` | 关闭客户端 | 置 CLOSED 并移除 `stateListener` |
| `getShellConnect()` | 获取连接 | 返回 `shellConnect` |
| `isConnected()` | 是否已连接 | 固定返回 `true` |

- 调用链：`ShellLocalTermWidget.createTtyConnector → ShellLocalClient.getShellConnect / getCharset（ShellBaseClient 默认方法）`

## ShellLocalTermWidget

- 职责：本地终端组件，继承 `ShellProcessTermWidget`，负责创建本地终端 TTY 连接器并设置环境变量。
- 字段：无字段
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `createTtyConnector(ShellLocalClient client)` | 创建本地 TTY 连接器 | `createProcess()` 创建 `PtyProcess`；`getProcessCommand()` 取命令；设置 `TERM`（取连接的 `termType`，为空用 `xterm-256color`）、`LANG`（`en_US.<charset>`）；返回 `new ShellLocalTtyConnector(client, process, List.of(command))` |
| `getTtyConnector()` | 获取连接器 | 将父类连接器强转为 `ShellLocalTtyConnector` |

- 调用链：`ShellLocalTermWidget.createTtyConnector → ShellProcessTermWidget.createProcess / getProcessCommand → new ShellLocalTtyConnector`

## ShellLocalTtyConnector

- 职责：本地终端 TTY 连接器，继承 `TtyProcessTtyConnector`，持有本地客户端并在关闭时一并释放。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| client | `ShellLocalClient` | 关联的本地客户端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellLocalTtyConnector(ShellLocalClient client, PtyProcess process, List<String> commandLines)` | 构造 | 调 `super(process, client.getCharset(), commandLines)` 并保存 client |
| `getClient()` | 获取本地客户端 | 返回 `client` |
| `getName()` | 连接器名称 | 返回 `local-tty` |
| `close()` | 关闭连接器 | `super.close()` 后 `IOUtil.close(this.client)` 并置空 |

- 调用链：`ShellLocalTtyConnector.close → TtyProcessTtyConnector.close（父类）→ ShellLocalClient.close`
