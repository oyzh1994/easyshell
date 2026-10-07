# easyshell RLogin 客户端（rlogin 包）代码审查文档

> 范围：easyshell/src/main/java/cn/oyzh/easyshell/rlogin/，共 4 个 .java，全部存活。
> 说明：仅新增文档，未改动任何 `.java`。

## PatchedRLoginClient

- 职责：修正版 rlogin 客户端，覆写建连逻辑，在本地端口绑定失败（`BindException`）时自动换端口重试。
- 字段：无字段（`_socket_`、`_socketFactory_` 等为父类 `org.apache.commons.net.bsd.RLoginClient` 的受保护字段，本类未新增字段）。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `connect(InetAddress host, int port, InetAddress localAddr)` | 覆写父类建连方法，带本地端口重试 | 死循环内 `SSHUtil.findAvailablePort(excludes)` 取可用本地端口 `localPort`；`super._socketFactory_.createSocket(host, port, localAddr, localPort)` 建连；捕获 `BindException` 时把 `localPort` 加入 `excludes` 重试；成功后调用 `super._connectAction_()` 完成 rlogin 协议动作 |

- 调用链：`ShellRLoginClient.start → PatchedRLoginClient.connect → SSHUtil.findAvailablePort → _socketFactory_.createSocket → super._connectAction_()`

## ShellRLoginClient

- 职责：rlogin 客户端，实现 `ShellBaseClient`，管理 rlogin 连接生命周期、状态属性与输入输出流。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| client | `RLoginClient` | 底层 rlogin 客户端实例，可能为 `PatchedRLoginClient` 或原生 `RLoginClient` |
| shellConnect | `ShellConnect`（final） | 连接配置（主机、端口、用户、终端类型、密码、字符集、代理等） |
| state | `SimpleObjectProperty<ShellConnState>`（final） | 连接状态，初值 `ShellConnState.NOT_INITIALIZED` |
| stateListener | `ChangeListener<ShellConnState>`（final） | 状态监听器，把状态变更转发给 `ShellBaseClient.super.onStateChanged(state3)` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ObjectProperty<ShellConnState> stateProperty()` | 返回状态属性 | 返回 `this.state` |
| `ShellRLoginClient(ShellConnect shellConnect)` | 构造 rlogin 客户端 | 保存 `shellConnect`，`this.addStateListener(this.stateListener)` |
| `void initClient()`（private） | 初始化底层客户端 | `shellConnect.hostPort() >= 1024` 时用 `new PatchedRLoginClient()`，否则 `new RLoginClient()`；`client.setCharset(ShellBaseClient.super.getCharset())`；`shellConnect.isEnableProxy()` 时 `client.setProxy(ShellProxyUtil.initProxy1(getProxyConfig()))` |
| `void start(int timeout)` | 发起连接并认证 | 已连接直接返回；`initClient()`、`client.setConnectTimeout(timeout)`、`state=CONNECTING`；用 `ThreadUtil.start` 异步执行 `client.connect(hostIp, hostPort)`，`DownLatch.await(timeout)` 等待；超时置 `FAILED`；有异常则抛出；`isConnected()` 为真时 `client.rlogin(user, user, termType)` 并置 `CONNECTED` 且 `ShellClientChecker.push(this)`；否则 `FAILED`；finally `SystemUtil.gc()` |
| `void close()` | 关闭连接 | `client.disconnect()`、`client=null`、`state=CLOSED`、`removeStateListener(this.stateListener)` |
| `boolean isConnected()` | 是否已连接 | `this.client != null && this.client.isConnected()` |
| `ShellConnect getShellConnect()` | 取连接配置 | 返回 `shellConnect` |
| `InputStream getInputStream()` | 取输入流 | `client != null` 时返回 `client.getInputStream()`，否则 `null` |
| `OutputStream getOutputStream()` | 取输出流 | `client != null` 时返回 `client.getOutputStream()`，否则 `null` |

- 调用链：`ShellRLoginClient.start → initClient → client.connect → client.rlogin(user, user, termType) → ShellClientChecker.push`

## ShellRLoginTermWidget

- 职责：rlogin 终端组件，继承 `ShellStreamTermWidget`，负责为 rlogin 客户端创建 tty 连接器。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRLoginTtyConnector createTtyConnector(ShellRLoginClient client)` | 创建 rlogin tty 连接器 | `return new ShellRLoginTtyConnector(client)` |
| `ShellRLoginTtyConnector getTtyConnector()` | 覆写取连接器并窄化返回类型 | 把 `super.getTtyConnector()` 强转为 `ShellRLoginTtyConnector` |

- 调用链：`ShellRLoginTermWidget.createTtyConnector → new ShellRLoginTtyConnector(client) → TtyStreamConnector(client.getCharset())`

## ShellRLoginTtyConnector

- 职责：rlogin 终端 tty 连接器，继承 `TtyStreamConnector`，桥接终端与 rlogin 输入输出流，并在检测到密码提示时自动输入密码。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| client | `ShellRLoginClient` | 关联的 rlogin 客户端 |
| inputPasswd | `int` | 已自动输入密码的次数；读到 `#` 或累计大于等于 3 次后停止尝试 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRLoginTtyConnector(ShellRLoginClient client)` | 构造连接器 | `super(client.getCharset())`，保存 `this.client = client` |
| `int doRead(char[] buf, int offset, int len)` | 读取后钩子，自动输入密码 | `super.doRead(buf, offset, len)`；用缓冲区构造 `line`，若含 `#` 则 `inputPasswd = Integer.MAX_VALUE` 并返回 `len`；否则取 `client.getShellConnect().getPassword()`，密码非空且 `inputPasswd < 3` 且 `line` 含 `"Password:"`/`"密码:"` 时 `inputPasswd++` 并 `writer.write(password + "\r")`、`writer.flush()`；返回 `len` |
| `boolean isConnected()` | 是否已连接 | `this.client.isConnected()` |
| `boolean ready()` | 延迟初始化读写器 | `reader == null` 时用 `client.getInputStream()/getOutputStream()` 按 `charset()` 创建 `InputStreamReader`/`OutputStreamWriter`，再 `super.ready()` |
| `String getName()` | 连接器名称 | 返回 `"rlogin-tty"` |
| `void close()` | 关闭 | `super.close()`；`IOUtil.close(this.client)`；`client = null` |
| `InputStream input()` | 输入流 | `this.client.getInputStream()` |
| `OutputStream output()` | 输出流 | `this.client.getOutputStream()` |

- 调用链：`终端读取 → TtyStreamConnector.read → ShellRLoginTtyConnector.doRead →（命中密码提示）writer.write → client.getOutputStream()`
