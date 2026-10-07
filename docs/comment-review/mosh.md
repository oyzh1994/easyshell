# easyshell Mosh 客户端（mosh 包）代码审查文档

> 范围：easyshell/src/main/java/cn/oyzh/easyshell/mosh/，共 4 个 .java，全部存活。
> 说明：仅新增文档，未改动任何 `.java`。

## ShellMoshClient

- 职责：mosh 客户端，实现 `ShellBaseClient`，封装 `MoshTerminalFrontend` 的连接、状态管理、数据收发与终端尺寸调整。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| state | `SimpleObjectProperty<ShellConnState>`（protected final） | 连接状态，初值 `ShellConnState.NOT_INITIALIZED` |
| stateListener | `ChangeListener<ShellConnState>`（protected final） | 状态监听器，转发给 `ShellBaseClient.super.onStateChanged(state3)` |
| shellConnect | `ShellConnect` | 连接配置（主机、端口、moshKey 等） |
| frontend | `MoshTerminalFrontend` | mosh 终端前端（org.mosh4j.core），连接建立后非空 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ObjectProperty<ShellConnState> stateProperty()` | 返回状态属性 | 返回 `this.state` |
| `ShellMoshClient(ShellConnect shellConnect)` | 构造 mosh 客户端 | 保存 `shellConnect`，`ShellBaseClient.super.addStateListener(this.stateListener)` |
| `MoshTerminalFrontend getFrontend()` / `void setFrontend(MoshTerminalFrontend)` | 前端读写 | 直接读写 `frontend` |
| `void initClient(int timeout)`（private） | 初始化客户端 | `shellConnect.getMoshKey()` 非空时走 `ShellMoshHelper.connectWithMoshKey(shellConnect, moshKey)`；否则 `shellConnect.setEnableCompress(true)` 后走 `ShellMoshHelper.connectWithSSH(shellConnect, timeout)` |
| `void start(int timeout)` | 发起连接 | 已连接或连接中直接返回；记录 `starTime`；`frontend == null` 时 `state=CONNECTING` → `initClient(timeout)` → `state=CONNECTED`；`isConnected()` 时 `ShellClientChecker.push(this)`；打印耗时日志；异常时 `state=FAILED` 并抛 `ShellException`；finally `SystemUtil.gc()` |
| `ShellConnect getShellConnect()` | 取连接配置 | 返回 `shellConnect` |
| `boolean isConnected()` | 是否已连接 | `this.frontend != null && this.frontend.isRunning()` |
| `void close()` | 关闭连接 | `frontend.close()`、`frontend=null`、`state=CLOSED`、`removeStateListener(this.stateListener)` |
| `void setPtySize(int columns, int rows, int sizeW, int sizeH)` | 调整终端大小 | 已连接时 `frontend.sendResize(columns, rows)` |
| `void sendUserInput(byte[] bytes)` | 发送用户输入 | 已连接时 `frontend.sendUserInput(bytes)` |
| `byte[] pollHostBytes()` | 非阻塞拉取数据 | 已连接时返回 `frontend.pollHostBytes()`，否则 `null` |
| `byte[] takeHostBytes(long timeoutMs)` | 阻塞拉取数据（带超时） | 已连接时 `frontend.takeHostBytes(timeoutMs)`，否则 `null`；抛 `InterruptedException` |
| `void sendHeartbeat()` | 发送心跳 | 已连接时 `frontend.sendHeartbeat()` |
| `String takeRenderedOutput(long timeoutMs)` | 阻塞拉取渲染数据（带超时） | 已连接时 `frontend.takeRenderedOutput(timeoutMs)`，否则 `null`；抛 `InterruptedException` |

- 调用链：`ShellMoshClient.start → initClient → ShellMoshHelper.connectWithSSH / connectWithMoshKey → MoshTerminalFrontend.start → ShellClientChecker.push`

## ShellMoshHelper

- 职责：mosh 客户端辅助类，负责建立 mosh 连接（经 SSH 或直接使用 moshKey）以及 JavaFX 按键到 ANSI 转义序列的转换。
- 字段：无字段（仅静态方法）。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static MoshTerminalFrontend connectWithSSH(ShellConnect connect, int timeout)` | 经 SSH 登录并拉起 mosh-server | try-with-resources `new ShellSSHClient(connect)` → `client.start(timeout)` → `client.exec("mosh-server new -s -c 256")`；遍历输出行，以 `"MOSH CONNECT"` 开头的行按空格 `split(" ")` 取 `parts[3]` 为 key、`parts[2]` 为 moshPort；无 key 抛 `ShellException("mosh-server start fail!")`；按 `connect.hostIp()` 与 moshPort 构造 `InetSocketAddress`，`MoshKey.fromBase64(key)`，`new MoshClientSession(addr, key, 80, 24)`，`new MoshTerminalFrontend(session)`，`sendInitialWakeUp()` 后 `start()`；异常包成 `ShellException` |
| `static MoshTerminalFrontend connectWithMoshKey(ShellConnect connect, String moshKey)` | 直接用 moshKey 连接 | `host=connect.hostIp()`、`moshPort=connect.hostPort()` 构造 `InetSocketAddress`；`MoshKey.fromBase64(moshKey)`；`new MoshClientSession(addr, key, 80, 24)`；`new MoshTerminalFrontend(session)`；`sendInitialWakeUp()` 后 `start()` |
| `static byte[] mapKeyToAnsiSequence(KeyEvent event)` | 按键转 ANSI 序列 | `switch (event.getCode())`：`BACK_SPACE→{0x7f}`、`TAB→{'\t'}`、`ESCAPE→{0x1b}`；`UP/DOWN/RIGHT/LEFT/HOME/END` 用 application cursor mode（SS3，`ESC O x`）；`PAGE_UP/PAGE_DOWN/DELETE/INSERT` 用普通模式（CSI）；`default→null`。注释说明因 mosh 的 `StatefulAnsiRenderer` 不转发 DECCKM，故统一用 SS3 序列 |

- 调用链：`ShellMoshClient.initClient → ShellMoshHelper.connectWithSSH → ShellSSHClient.exec("mosh-server new -s -c 256") → MoshClientSession → MoshTerminalFrontend.start`

> 审查提示：`connectWithSSH` 的 MOSH CONNECT 解析判断 `parts.length >= 3`，但随后访问 `parts[3]`，当某行恰好只有 3 段时会抛 `ArrayIndexOutOfBoundsException`；实际格式 `"MOSH CONNECT 60001 key"` 为 4 段，条件应为 `parts.length >= 4` 更稳妥。

## ShellMoshTermWidget

- 职责：mosh 终端组件，继承 `ShellStreamTermWidget`，负责为 mosh 客户端创建 tty 连接器、转发按键序列并同步终端尺寸。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMoshTtyConnector createTtyConnector(ShellMoshClient client)` | 创建 mosh tty 连接器 | `new ShellMoshTtyConnector(client)`；对 `connector.terminalSizeProperty()` 注册监听器，尺寸变化时调 `this.initPtySize()` |
| `ShellMoshTtyConnector getTtyConnector()` | 取连接器并窄化返回类型 | 把 `super.getTtyConnector()` 强转为 `ShellMoshTtyConnector` |
| `ShellMoshClient client()` | 取 mosh 客户端 | 从 `getTtyConnector().getClient()` 获取，连接器为 `null` 时返回 `null` |
| `void initPtySize()` | 初始化终端大小 | `client()` 与 `getTermSize()` 均非空时，调 `client.setPtySize(termSize.getColumns(), termSize.getRows(), 终端面板宽, 终端面板高)` |
| `void initNode()` | 初始化节点并注册按键过滤器 | `addEventFilter(KeyEvent.KEY_PRESSED, ...)`：`ShellMoshHelper.mapKeyToAnsiSequence(event)` 得到序列，非空且 `client()` 非空时 `client().sendUserInput(seq)` 并 `event.consume()`；最后 `super.initNode()` |

- 调用链：`initNode 按键 → ShellMoshHelper.mapKeyToAnsiSequence → client().sendUserInput`；`连接器尺寸变化 → initPtySize → ShellMoshClient.setPtySize → frontend.sendResize`

## ShellMoshTtyConnector

- 职责：mosh 终端 tty 连接器，继承 `TtyStreamConnector`，用管道把 mosh 前端渲染数据桥接到终端，并定时发送心跳。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| client | `ShellMoshClient` | 关联的 mosh 客户端 |
| input | `InputStream` | 管道输入流，供终端读取（render 线程写入） |
| output | `OutputStream` | 管道输出流 |
| heartbeat | `Future<?>` | 心跳定时任务句柄 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMoshTtyConnector(ShellMoshClient client)` | 构造连接器 | `super(client.getCharset())`，保存 `this.client = client` |
| `ShellMoshClient getClient()` | 取客户端 | 返回 `client` |
| `void write(String str)` | 写字符串 | 调试日志；`client != null` 时 `client.sendUserInput(str.getBytes(charset()))` |
| `void write(byte[] bytes)` | 写字节 | `client != null` 时 `client.sendUserInput(bytes)` |
| `boolean isConnected()` | 是否已连接 | `this.client.isConnected()` |
| `boolean ready()` | 延迟初始化管道、渲染线程与心跳 | `reader == null` 时：建 `PipedOutputStream`/`PipedInputStream(65536)`；启动守护线程 `"mosh-ouput"` 循环 `client.takeHostBytes(250)` 写入管道；`input/output` 赋值，`reader=InputStreamReader(hostInputPipe)`；`heartbeat=TaskManager.startInterval(client::sendHeartbeat, 15_000)`；最后 `super.ready()` |
| `String getName()` | 连接器名称 | 返回 `"mosh-tty"` |
| `void close()` | 关闭 | `super.close()`；`IOUtil.close(this.client)`；`client=null`；`TaskManager.cancel(this.heartbeat)`；`heartbeat=null` |
| `InputStream input()` | 输入流 | 返回 `this.input` |
| `OutputStream output()` | 输出流 | 返回 `this.output` |

- 调用链：`终端 ready → 建 PipedInput/OutputStream → 守护线程 client.takeHostBytes(250) → 写 hostOutputPipe → reader → 终端显示`；`每 15s → TaskManager → client.sendHeartbeat → frontend.sendHeartbeat`
