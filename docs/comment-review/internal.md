# easyshell 客户端内部基础设施（internal 包）代码审查文档

> 范围：easyshell/src/main/java/cn/oyzh/easyshell/internal/，共 5 个 .java，全部存活。

> 说明：仅新增文档，未改动任何 `.java`。

## 总体结构

- `internal` 包定义所有协议客户端（SSH/Redis/ZK/终端等）共享的基础契约与基础设施，共 5 个类/接口：
  - `ShellBaseClient`：客户端根接口，定义连接生命周期、状态、字符集、fork 等默认行为。
  - `ShellClientActionUtil`：客户端动作事件触发工具。
  - `ShellClientChecker`：客户端连接状态轮询监测器。
  - `ShellConnState`：连接状态枚举。
  - `ShellPrototype`：连接原型（连接类型）字符串常量。

## ShellBaseClient

- 职责：所有协议客户端的根接口（继承 `AutoCloseable`），以 `default` 方法提供状态、超时、字符集、监听、fork、事件等通用能力。
- 字段：无字段（接口，无实例字段）。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `start()` | 以默认超时时间启动连接（默认实现） | 委托 `this.start(this.connectTimeout())` |
| `start(int timeout)` | 启动连接（抽象，子类实现） | 由具体客户端实现，抛 `Throwable` |
| `getShellConnect()` | 获取连接实体（抽象） | 返回 `ShellConnect` |
| `connectName()` | 连接名称（默认实现） | `getShellConnect().getName()` |
| `connectTimeout()` | 连接超时毫秒（默认实现） | `getShellConnect().connectTimeOutMs()` |
| `getCharset()` | 获取字符集（默认实现） | 连接 `charset` 空则 `Charset.defaultCharset()`，否则 `Charset.forName(charset)` |
| `isClosed()` | 是否已关闭（默认实现） | `!this.isConnected()` |
| `isConnecting()` | 是否连接中（默认实现） | `isClosed()` 为真返回 false，否则 `getState() == ShellConnState.CONNECTING` |
| `isConnected()` | 是否已连接（抽象） | 由子类实现 |
| `stateProperty()` | 连接状态属性（抽象） | 返回 `ObjectProperty<ShellConnState>` |
| `addStateListener(ChangeListener<ShellConnState> stateListener)` | 添加状态监听（默认实现） | listener 与 `stateProperty()` 均非空时 `addListener` |
| `removeStateListener(ChangeListener<ShellConnState> stateListener)` | 移除状态监听（默认实现） | 同上，调 `removeListener` |
| `getState()` | 获取当前状态（默认实现） | `stateProperty()` 为 null 返回 null，否则 `get()` |
| `onStateChanged(ShellConnState state)` | 状态变更事件分发（默认实现） | `CLOSED` → `ShellEventUtil.connectionClosed(this)`；`CONNECTED` → `ShellEventUtil.connectionConnected(this)` |
| `checkState()` | 校验状态一致性（默认实现） | 状态为 `CONNECTED` 但 `!isConnected()` 时，`synchronized(this)` 内将 `stateProperty` 置为 `INTERRUPTED` |
| `forkClient()` | 派生（fork）子客户端（默认实现） | 默认返回 `this`（不 fork）；上传/下载/传输等占用客户端的场景可重写 |
| `isForked()` | 是否子客户端（默认实现） | 返回 false |
| `iid()` | 获取连接 id（默认实现） | 连接为 null 返回 null，否则 `getShellConnect().getId()` |

- 调用链：`ShellBaseClient.start() → start(connectTimeout()) → getShellConnect().connectTimeOutMs()`
- 调用链：`ShellBaseClient.onStateChanged(CLOSED/CONNECTED) → ShellEventUtil.connectionClosed()/connectionConnected()`
- 调用链：`ShellBaseClient.checkState() → getState()/isConnected() → stateProperty().set(INTERRUPTED)`

## ShellClientActionUtil

- 职责：客户端动作事件的静态触发入口，解耦客户端与事件系统。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `forAction(String connectName, String action)` | 触发指定连接的动作事件（静态） | `ShellEventUtil.clientAction(connectName, action)` |

- 调用链：`ShellClientActionUtil.forAction() → ShellEventUtil.clientAction()`

## ShellClientChecker

- 职责：客户端连接状态轮询监测器，定时检测活跃客户端的连接状态并对超时者标记中断。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| taskFuture | `Future<?>` | 监测任务句柄，`private static`，为空表示未启动 |
| CLIENTS | `List<WeakReference<ShellBaseClient>>` | 受监测客户端列表，`private static final`，实现为 `CopyOnWriteArrayList`，弱引用避免内存泄漏 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `push(ShellBaseClient client)` | 注册客户端并确保监测任务启动（静态） | client 非空时加入 `CLIENTS`（`WeakReference` 包装），随后调 `doCheck()` |
| `remove(ShellBaseClient client)` | 移除指定客户端（静态） | `CLIENTS.removeIf(ref -> ref.get() == client)` |
| `doCheck()` | 启动间隔监测任务（私有 `synchronized static`） | `taskFuture == null` 时 `TaskManager.startInterval(...)`，间隔 1500ms、初始延迟 0 |
| `stop()` | 停止监测（静态） | `CLIENTS.clear()` + `TaskManager.cancel(taskFuture)`，异常 `printStackTrace` |

- 关键逻辑：监测任务遍历 `CLIENTS`，弱引用已失效或 `client.isClosed()` 的引用收集到 `closedList` 待移除；否则起线程执行 `client.checkState()` 并用 `DownLatch` 限时 5000ms，超时则 `client.stateProperty().set(ShellConnState.INTERRUPTED)`；遍历结束 `CLIENTS.removeAll(closedList)`。
- 调用链：`ShellClientChecker.push() → doCheck() → TaskManager.startInterval() → client.checkState() → (超时) stateProperty().set(INTERRUPTED)`
- 调用链：`ShellClientChecker.stop() → CLIENTS.clear() + TaskManager.cancel(taskFuture)`

## ShellConnState

- 职责：连接状态枚举，描述客户端从初始化到关闭的完整生命周期，并提供从 ZK 连接状态到本枚举的映射。
- 字段（枚举常量，各自重写 `isConnected()`）：

| 字段 | 类型 | 含义 |
|---|---|---|
| NOT_INITIALIZED | `ShellConnState` | 未初始化，`isConnected()` 返回 false |
| CONNECTED | `ShellConnState` | 已连接，`isConnected()` 返回 true |
| CONNECTING | `ShellConnState` | 连接中，`isConnected()` 返回 false |
| CLOSED | `ShellConnState` | 已关闭，`isConnected()` 返回 false |
| FAILED | `ShellConnState` | 失败，`isConnected()` 返回 false |
| INTERRUPTED | `ShellConnState` | 中断，`isConnected()` 返回 false |
| RECONNECTED | `ShellConnState` | 重连（成功），`isConnected()` 返回 true |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isConnected()` | 是否已连接（抽象，各常量重写） | `CONNECTED`、`RECONNECTED` 返回 true，其余返回 false |
| `valueOfZK(ConnectionState state)` | ZK 连接状态映射（静态） | `switch`：`CONNECTED/READ_ONLY`→`CONNECTED`；`RECONNECTED`→`RECONNECTED`；`LOST`→`CLOSED`；`SUSPENDED`→`INTERRUPTED` |

- 调用链：`ShellConnState.valueOfZK(ConnectionState) → 返回对应 ShellConnState → 供 stateProperty 使用（如 ShellClientChecker 判定 INTERRUPTED）`

## ShellPrototype

- 职责：连接原型（连接类型）字符串常量定义类，供连接类型判定与工厂分发使用。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| S3 | `String` | S3 连接类型，值 `"S3"` |
| SSH | `String` | SSH 连接类型，值 `"SSH"` |
| RDP | `String` | RDP 连接类型，值 `"RDP"` |
| SMB | `String` | SMB 连接类型，值 `"SMB"` |
| FTP | `String` | FTP 连接类型，值 `"FTP"` |
| VNC | `String` | VNC 连接类型，值 `"VNC"` |
| SFTP | `String` | SFTP 连接类型，值 `"SFTP"` |
| MINIO | `String` | Minio 连接类型，值 `"Minio"` |
| REDIS | `String` | Redis 连接类型，值 `"Redis"` |
| TELNET | `String` | Telnet 连接类型，值 `"Telnet"` |
| RLOGIN | `String` | RLogin 连接类型，值 `"RLogin"` |
| WEBDAV | `String` | Webdav 连接类型，值 `"Webdav"` |
| SERIAL | `String` | 串口连接类型，值 `"Serial"` |
| ZOOKEEPER | `String` | Zookeeper 连接类型，值 `"Zookeeper"` |
| LOCAL | `String` | 本地连接类型，值 `"Local"` |
| MYSQL | `String` | Mysql 连接类型，值 `"Mysql"` |
| DAMENG | `String` | 达梦连接类型，值 `"Dameng"` |
| MONGO | `String` | MongoDB 连接类型，值 `"MongoDB"` |
| MOSH | `String` | Mosh 连接类型，值 `"Mosh"` |

- 方法：无方法（仅 `public static final` 常量）。
- 调用链：无（常量类，被各连接工厂/类型判定引用）。
