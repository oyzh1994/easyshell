# easyshell Telnet 客户端（telnet 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/telnet/，共 3 个 .java，全部存活。

本包基于 Apache Commons Net 的 `TelnetClient` 实现 Telnet 终端连接：`ShellTelnetClient` 负责连接与流/窗口大小管理，`ShellTelnetTermWidget` 基于流终端组件（`ShellStreamTermWidget`）创建 TTY 连接器，`ShellTelnetTtyConnector` 基于流连接器（`TtyStreamConnector`）实现读写并自动识别登录/密码提示。

## ShellTelnetClient

- 职责：Telnet 客户端，实现 `ShellBaseClient`，负责连接建立、代理、输入输出流暴露及窗口大小设置。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| client | `TelnetClient` | 底层 Telnet 客户端 |
| shellConnect | `final ShellConnect` | 连接配置 |
| state | `final SimpleObjectProperty<ShellConnState>` | 连接状态，初值 NOT_INITIALIZED |
| stateListener | `final ChangeListener<ShellConnState>` | 状态监听器，回调 `onStateChanged` |
| sizeHandler | `WindowSizeOptionHandler` | Telnet 窗口大小选项处理器 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellTelnetClient(ShellConnect shellConnect)` | 构造 | 保存连接，注册 `stateListener` |
| `stateProperty()` | 状态属性 | 返回 `state` |
| `initClient()` | 初始化客户端 | 创建 `TelnetClient`，设置字符集，启用代理时 `setProxy` |
| `start(int timeout)` | 启动连接 | `initClient` → 设置连接超时 → CONNECTING → `connect(hostIp, hostPort)` → 已连接置 CONNECTED + `ShellClientChecker.push`，否则 FAILED；finally `SystemUtil.gc()` |
| `close()` | 关闭客户端 | `client.disconnect()` 并置空，置 CLOSED，`state.removeListener` |
| `setPtySize(int cols, int rows)` | 设置终端大小 | 移除旧 `sizeHandler` 后新建 `WindowSizeOptionHandler(cols, rows, ...)` 并 `addOptionHandler` |
| `isConnected()` | 是否已连接 | `client != null && client.isConnected()` |
| `getShellConnect()` | 获取连接 | 返回 `shellConnect` |
| `getInputStream()` | 获取输入流 | `client.getInputStream()`（client 为空返回 null） |
| `getOutputStream()` | 获取输出流 | `client.getOutputStream()`（client 为空返回 null） |

- 调用链：`ShellTelnetTermWidget.initPtySize → ShellTelnetClient.setPtySize → WindowSizeOptionHandler`

## ShellTelnetTermWidget

- 职责：Telnet 终端组件，继承 `ShellStreamTermWidget`，负责创建 Telnet TTY 连接器并同步终端窗口大小。
- 字段：无字段
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `createTtyConnector(ShellTelnetClient client)` | 创建 Telnet TTY 连接器 | `new ShellTelnetTtyConnector(client)`；监听 `terminalSizeProperty` 变化触发 `initPtySize` |
| `getTtyConnector()` | 获取连接器 | 将父类连接器强转为 `ShellTelnetTtyConnector` |
| `client()` | 获取 Telnet 客户端 | 通过 `getTtyConnector().getClient()` 返回 |
| `initPtySize()` | 初始化终端大小 | 取 `getTermSize()`，调 `client.setPtySize(columns, rows)` |

- 调用链：`ShellTelnetTermWidget.initPtySize → getTermSize → ShellTelnetClient.setPtySize`

## ShellTelnetTtyConnector

- 职责：Telnet TTY 连接器，继承 `TtyStreamConnector`，基于客户端输入输出流读写，并在检测到登录/密码提示时自动填充凭据。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| client | `ShellTelnetClient` | 关联的 Telnet 客户端 |
| inputUser | `boolean` | 是否已输入用户名 |
| inputPasswd | `boolean` | 是否已输入密码 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellTelnetTtyConnector(ShellTelnetClient client)` | 构造 | `super(client.getCharset())`，保存 client |
| `getClient()` | 获取客户端 | 返回 `client` |
| `doRead(char[] buf, int offset, int len)` | 读取并自动登录 | 转字符串后匹配 `login:`/`Username:`/`用户:`/`User:` 自动写用户名，匹配 `Password:`/`密码:` 自动写密码（均带 `\r\n` 并 flush） |
| `isConnected()` | 是否已连接 | `client.isConnected()` |
| `ready()` | 是否就绪 | 延迟初始化 `reader`/`writer`（基于客户端输入输出流与字符集） |
| `getName()` | 连接器名称 | 返回 `telnet-tty` |
| `close()` | 关闭连接器 | `super.close()` 后 `IOUtil.close(this.client)` 并置空 |
| `input()` | 输入流 | `client.getInputStream()` |
| `output()` | 输出流 | `client.getOutputStream()` |

- 调用链：`ShellTelnetTtyConnector.doRead → ShellTelnetClient.getShellConnect → ShellConnect.getUser/getPassword`
