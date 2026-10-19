# easyshell 终端模块 — redis 子系统

> 范围：`terminal/redis/`，共 **370** 个类。
> 其中根目录 11 个为框架类（下方展开），其余按命令族归并为同构简表（无自有字段的处理器一行一类）。
> 全部命令处理器经由 `RedisTerminalManager.registerHandlers()` 注册到 `TerminalManager`，终端名 `redis`。

## 一、框架类（展开）

### RedisTerminalCommandHandler（抽象）

- 职责：redis 命令处理器抽象基类，统一「构造命令 → 执行 → 格式化输出」。
- 字段：无自有字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `TerminalExecuteResult execute(C, RedisTerminalPane)` | 执行命令 | `RedisTerminalUtil.getCommand(getCommandType(), command)` → `terminal.getClient().execCommand(dbIndex, object)` → `RedisTerminalUtil.formatOut(...)`；异常写入 result |
| `String commandName()` | 命令名 | `JsonProtocol.JsonCommand` 用 `SafeEncoder.encode(getRaw())`；`Enum` 用 `name()`；否则空串 |
| `String commandHelp(RedisTerminalPane)` | 帮助 | 返回 `commandArg()`（若有） |
| `String commandArg()` | 参数说明 | `ShellRedisCommandUtil.getCommandArgs(commandFullName())` |
| `String commandDesc()` | 描述 | `ShellRedisCommandUtil.getCommandDesc(commandFullName())` |
| `String commandSupportedVersion()` | 支持版本 | `ShellRedisCommandUtil.getCommandAvailable(commandFullName())` |
| `ProtocolCommand getCommandType()`（抽象） | 命令类型 | 子类实现 |

- 调用链：`TerminalPane → BaseTerminalCommandHandler → RedisTerminalCommandHandler.execute → ShellRedisClient.execCommand`

### RedisKeyTerminalCommandHandler（抽象・单键补全）

- 职责：带单个 key 参数的命令基类，提供键名补全。
- 字段：无。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `boolean completion(String, RedisTerminalPane)` | 键补全 | 取 key 片段拼 `pattern`（空→`*`，不以 `*` 结尾→加 `*`）；`client.keys(null, pattern, getKeyType())`；唯一则 `coverInput`，多个则 `TextUtil.beautifyFormat` 输出并回填输入 |
| `ShellRedisKeyType getKeyType()` | 键类型 | 默认 null，子类覆写（STRING/HASH/LIST/SET/ZSET/STREAM/JSON） |
| `boolean checkArgs(String[])` | 参数校验 | `words.length >= 2` |

- 调用链：`RedisTerminalKeyHandler → RedisKeyTerminalCommandHandler.completion → ShellRedisClient.keys`

### RedisNKeysTerminalCommandHandler（抽象・多键补全）

- 职责：支持多 key 命令的基类，补全逻辑与单键类似但不含结尾 `*` 判断。
- 字段：无。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `boolean completion(String, RedisTerminalPane)` | 键补全 | `pattern = 空?"*":key+"*"`；`client.keys(null, pattern, getKeyType())`；唯一 → `coverInput`，否则 `outputByPrompt` 后回填 |
| `ShellRedisKeyType getKeyType()` | 键类型 | 默认 null |

### RedisTerminalPane

- 职责：redis 终端面板（连接/提示符/字体/状态监听/处理器装配）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| client | `ShellRedisClient` | redis 客户端 |
| connectInfo | `ShellRedisConnectInfo` | 临时连接解析出的连接信息 |
| stateChangeListener | `ChangeListener<ShellConnState>` | 连接状态监听器 |
| dbIndex | `Integer` | 当前 db 索引 |
| TERMINAL_NAME | `String`（static final） | 终端名 `"redis"` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `Font getEditorFont()` | 终端字体 | `FontManager.toFont(setting.terminalFontConfig())` |
| `ShellRedisClient getClient()` / `Integer getDbIndex()` | 取客户端/db 索引 | - |
| `void flushPrompt()` | 生成提示符 | 临时连接 `redis 连接`，否则 `client.connectName()`；拼 `@host`、`(连接中/已连接[/只读]@dbN)>` |
| `String terminalName()` | 终端名 | `TERMINAL_NAME` |
| `String getDbName()` | db 名 | `@db{dbIndex}` |
| `void init(ShellRedisClient, Integer)` | 初始化 | `FXUtil.runPulse` 中禁用输入、打印欢迎，临时→`initByTemporary` 否则 `initByPermanent` |
| `boolean isTemporary()` | 是否临时连接 | `client.iid() == null` |
| `void outputPrompt()` | 输出提示符 | 连接中不输出 |
| `boolean isConnected()/isConnecting()/isClosed()` | 状态查询 | 委托 `client` |
| `void connect(String)` | 执行连接 | `ShellRedisConnectUtil.parse` → `copyConnect` → `start(db)` |
| `void initByTemporary()` | 临时连接引导 | 输出 connect 用法并 `appendByPrompt("connect -timeout 3000 -h 127.0.0.1 -p 6379 -n 0")` |
| `void initByPermanent()` | 常驻连接引导 | 刷新提示符 + 启用输入 |
| `void start(int)` | 异步连接 | `TaskManager.startSync`：`initStatListener` + `client.startDatabase(db)`，finally 启用 |
| `void initStatListener()` | 状态监听 | 依 `ShellConnState`（CONNECTED/CLOSED/CONNECTING/INTERRUPTED/FAILED）输出对应文案并启停输入 |
| `void enableInput()` | 启用输入 | 连接中不启用；已连接或临时连接才启用 |
| `ShellConnect shellConnect()` | 连接信息 | `client.shellConnect()` |
| `void fontSizeIncr()/fontSizeDecr()` | 字号增减 | 调 super 后 `saveFontSize()` |
| `void initNode()` | 装配处理器 | Key/Help/Mouse=`Redis*Handler.INSTANCE`，History=`ShellTerminalHistoryHandler.INSTANCE`，Complete=`RedisTerminalCompleteHandler.INSTANCE` |
| `void destroy()` | 销毁 | 移除状态监听 |

- 调用链：`RedisTerminalPane.init → start → client.startDatabase`；`initNode → 各 XxxHandler.INSTANCE`

### RedisTerminalManager

- 职责：注册全部 redis 命令处理器（无实例，纯静态）。
- 字段：无。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static void registerHandlers()` | 注册 | 对每个处理器调 `TerminalManager.registerHandler("redis", XxxHandler.class)`；含标准命令、acl、base、bit、client、cluster、command、geo、hash、hylog、json、key、latency、list、memory、module、other、program(function/script)、pubsub、server(config)、set、slowlog、stream(xgroup/xinfo)、string、zset 全族 |

- 调用链：`应用启动 → RedisTerminalManager.registerHandlers → TerminalManager.registerHandler`

### RedisTerminalUtil

- 职责：redis 结果格式化与命令对象构造工具。
- 字段：无。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static String formatOut(Object, String)` | 任意值格式化 | `SafeEncoder.encodeObject`；集合→重载 |
| `static String formatOut(Collection<?>, String)` | 集合格式化 | 逐项 `n) "v"` 拼接 |
| `static String formatOut(Map<?,?>, String)` | Map 格式化 | 展开为 key/value 列表后走集合版 |
| `static String formatOut(List<GeoCoordinate>, String)` | 坐标格式化 | 输出经纬度两行 |
| `static CommandObject<Object> getCommand(ProtocolCommand, TerminalCommand)` | 构造命令 | `CommandArguments` + `argsList()`，`BuilderFactory.RAW_OBJECT` |
| `static CommandObject<Object> getCommand(ProtocolCommand, String[])` | 构造命令 | 逐参数 `add` |
| `static CommandObject<Object> getCommand(ProtocolCommand, String)` | 构造命令 | 单参数 `add` |

### RedisTerminalCompleteHandler

- 职责：redis 终端补全处理器（继承 `BaseTerminalCompleteHandler<RedisTerminalPane>`）。
- 字段：`INSTANCE`（static final）。
- 方法：无自有覆写（复用基类）。
- 调用链：`RedisTerminalPane.initNode → INSTANCE → 基类 completion → 匹配 CommandHandler`

### RedisTerminalHelpHandler

- 职责：redis 帮助处理器（`extends BaseTerminalHelpHandler`）。
- 字段：`INSTANCE`（static final）。
- 方法：无自有覆写。

### RedisTerminalHistoryHandler

- 状态：**整体注释禁用**；历史功能实际使用 `ShellTerminalHistoryHandler`（见 terminal.md）。
- 字段（注释）：`INSTANCE`、`cecheList`、`historyStore`；方法：`clearHistory/listHistory/addHistory`。

### RedisTerminalKeyHandler

- 职责：redis 按键处理器（`implements TerminalKeyHandler<RedisTerminalPane>`）。
- 字段：`INSTANCE`（static final）。
- 方法：`onEnterKeyPressed` **已注释**，行为完全走 `TerminalKeyHandler` 默认（回车提交由面板处理）。
- 调用链：`RedisTerminalPane.initNode → INSTANCE`

### RedisTerminalMouseHandler

- 职责：redis 鼠标处理器（`implements TerminalMouseHandler<RedisTerminalPane>`）。
- 字段：`INSTANCE`（static final）。
- 方法：无自有覆写。

## 二、同构命令处理器简表

> 说明：下表均为「无自有字段」的等价处理器，通常只覆写 `getCommandType()`（返回 `Protocol.Command.X`）或子命令类的 `commandSubName()`。
> 「键类型」列非空者继承 `RedisKeyTerminalCommandHandler`，其补全按该键类型过滤；「备注」标注少量额外逻辑（覆写 `commandHelp`、自定义 `execute` 等）。
> `RedisClientXxx`、`RedisClusterXxx`、`RedisConfigXxx`、`RedisXgroup/Xinfo`、`RedisFunction/Script` 等子命令类的基类为对应「总处理器」，该总处理器本身继承 `RedisTerminalCommandHandler` 并覆写 `commandHelp` 转发 `<命令> HELP`。

<!--REDIS_TABLES-->

## 三、命令调用样例

- GET：`RedisGetTerminalCommandHandler(Protocol.Command.GET, RedisKeyTerminalCommandHandler) → execute → RedisTerminalUtil.getCommand → ShellRedisClient.execCommand`
- 子命令 SET：`RedisConfigSetTerminalCommandHandler(commandSubName="SET", extends RedisConfigTerminalCommandHandler) → commandName=CONFIG/SET`
- JSON：`RedisJsonGetCommandHandler(getCommandType=JsonProtocol.JsonCommand.GET, keyType=JSON) → commandName 由 SafeEncoder 编码 raw`
