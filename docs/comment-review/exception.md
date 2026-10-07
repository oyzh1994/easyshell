# easyshell 异常体系（exception 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/exception/（含 zk/redis 子包），共 14 个 .java，全部存活。

## 异常继承结构总览

```
RuntimeException
└── ShellException
    ├── ShellDataTooBigException
    ├── ShellReadonlyOperationException
    ├── ShellRedisClusterOperationException
    ├── ShellRedisSentinelOperationException
    └── ShellRedisUnsupportedCommandException

org.apache.zookeeper.KeeperException.NoAuthException
└── ShellZKNoAuthException
    ├── ShellZKNoAdminPermException
    ├── ShellZKNoChildPermException
    ├── ShellZKNoCreatePermException
    ├── ShellZKNoDeletePermException
    ├── ShellZKNoReadPermException
    └── ShellZKNoWritePermException

ShellExceptionParser implements java.util.function.Function<Throwable, String>（独立的异常信息解析器，不属于上述继承树）
```

设计要点：Shell 自身异常分两条主线——通用运行时异常继承 `ShellException`（`RuntimeException` 子类，免检）；ZooKeeper 权限类异常继承 ZK 客户端库的 `KeeperException.NoAuthException`，从而可被现有 `catch (KeeperException.NoAuthException)` 逻辑统一捕获。Redis 系列异常同时提供了无参（国际化默认文案）与带参（自定义信息）两类构造函数。

## ShellException

- 职责：easyshell 通用运行时异常基类，承载业务异常信息与原因。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellException()` | 无参构造 | 调用 `super()` |
| `ShellException(String message)` | 指定异常信息构造 | 调用 `super(message)` |
| `ShellException(Throwable ex)` | 指定异常原因构造 | 调用 `super(ex)` |

- 调用链：`业务代码 throw new ShellException(...)` → `RuntimeException` 体系

## ShellDataTooBigException

- 职责：数据过大异常，表示待处理数据超出允许上限。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellDataTooBigException()` | 无参构造 | 调用 `this(I18nHelper.dataTooLarge())`，使用国际化默认文案 |
| `ShellDataTooBigException(String msg)` | 指定异常信息构造 | 调用 `super(msg)` → `ShellException(String)` |

- 调用链：`new ShellDataTooBigException()` → `I18nHelper.dataTooLarge()`（国际化文案）。当前仓库内未见对本类的引用点。

## ShellReadonlyOperationException

- 职责：只读模式不支持操作异常。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellReadonlyOperationException()` | 无参构造 | 调用 `this(I18nResourceBundle.i18nString("base.readonlyMode", "base.notSupport", "base.current", "base.operation"))` 组合为文案“只读模式不支持当前操作” |
| `ShellReadonlyOperationException(String msg)` | 指定异常信息构造 | 调用 `super(msg)` → `ShellException(String)` |

- 调用链：`ShellZKClient.throwReadonlyException()` → `new ShellReadonlyOperationException()`；`ShellRedisClient.throwReadonlyException()` → `new ShellReadonlyOperationException()`；ZK CLI 各命令处理器（如 `ZKCreateTerminalCommandHandler`、`ZKDeleteTerminalCommandHandler`、`ZKSetAclTerminalCommandHandler` 等）→ `TerminalExecuteResult.fail(new ShellReadonlyOperationException())`
- 备注：`ShellRedisClient2`（旧实现）中对应逻辑为注释状态。

## ShellExceptionParser

- 职责：SSH/终端异常信息解析器，将 `Throwable` 归一化为可展示的消息字符串。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `public final static ShellExceptionParser` | 全局单例实例，供各处复用 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `apply(Throwable e)` | 解析异常为消息字符串（实现 `Function.apply`） | 1) `e == null` 返回 `null`；2) 沿 `getCause()` 链循环向上寻找首个非空 `getMessage()`；3) `UnsupportedOperationException` / `IllegalArgumentException` 直接返回消息；4) 其余情况调用 `e.printStackTrace()` 后返回消息 |

- 调用链：`EasyShellApp.init()` → `MessageBox.registerExceptionParser(ShellExceptionParser.INSTANCE)`（全局注册）；`ZKTerminalPane` / `RedisTerminalPane` → `this.onError(ShellExceptionParser.INSTANCE.apply(ex))`
- 备注：代码中保留了针对 `RuntimeException.getCause()` 的注释逻辑，尚未启用。

## ShellZKNoAuthException

- 职责：ZooKeeper 无权限异常基类，继承 ZK 库的 `KeeperException.NoAuthException`，携带节点路径。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| path | `protected String` | 无权限对应的 ZK 节点路径 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKNoAuthException(String path)` | 构造并记录节点路径 | 赋值 `this.path = path` |
| `getPath()` | 获取节点路径（覆写父类） | 返回 `this.path` |

- 调用链：`ShellZKClient` 各操作捕获 `KeeperException.NoAuthException` → `throw new ShellZKNoXxxException(path)` → 上层 `ShellZKNodeTreeItem` 以 `catch (KeeperException.NoAuthException)` 捕获并 `setNeedAuth(true)`
- 备注：构造函数未调用 `super(...)`，因此异常自身的 `getMessage()` 为 `null`，其语义完全依赖 `path`。

## ShellZKNoAdminPermException

- 职责：ZK 节点无管理（ACL/Admin）权限异常。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKNoAdminPermException(String path)` | 构造 | 调用 `super(path)` → `ShellZKNoAuthException(String)` |

- 调用链：`ShellZKClient.setACL(String, List<ACL>, Integer)`、`ShellZKClient.setACL(List<String>, List<ACL>)`、`ShellZKClient.getACL(String)`、`ShellZKClient.getACL(String, BackgroundCallback)` 捕获 `KeeperException.NoAuthException` → `throw new ShellZKNoAdminPermException(path)`

## ShellZKNoChildPermException

- 职责：ZK 节点无子节点（子列表读取）权限异常。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKNoChildPermException(String path)` | 构造 | 调用 `super(path)` → `ShellZKNoAuthException(String)` |

- 调用链：`ShellZKClient.getChildren(String)` 捕获 `KeeperException.NoAuthException` → `throw new ShellZKNoChildPermException(path)`

## ShellZKNoCreatePermException

- 职责：ZK 节点无创建（子节点创建）权限异常。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKNoCreatePermException(String path)` | 构造 | 调用 `super(path)` → `ShellZKNoAuthException(String)` |

- 调用链：`ShellZKClient.create(String, byte[], List<ACL>, Long, CreateMode, boolean)` 捕获 `KeeperException.NoAuthException` → `throw new ShellZKNoCreatePermException(path)`

## ShellZKNoDeletePermException

- 职责：ZK 节点无删除权限异常。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKNoDeletePermException(String path)` | 构造 | 调用 `super(path)` → `ShellZKNoAuthException(String)` |

- 调用链：`ShellZKClient.delete(String, Integer, boolean)` 捕获 `KeeperException.NoAuthException` → `throw new ShellZKNoDeletePermException(path)`

## ShellZKNoReadPermException

- 职责：ZK 节点无数据读取权限异常。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKNoReadPermException(String path)` | 构造 | 调用 `super(path)` → `ShellZKNoAuthException(String)` |

- 调用链：`ShellZKClient.getData(String)`、`ShellZKClient.getData(String, BackgroundCallback)` 捕获 `KeeperException.NoAuthException` → `throw new ShellZKNoReadPermException(path)`

## ShellZKNoWritePermException

- 职责：ZK 节点无数据写入权限异常。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKNoWritePermException(String path)` | 构造 | 调用 `super(path)` → `ShellZKNoAuthException(String)` |

- 调用链：`ShellZKClient.setData(String, byte[], Integer)` 捕获 `KeeperException.NoAuthException` → `throw new ShellZKNoWritePermException(path)`

## ShellRedisClusterOperationException

- 职责：Redis 集群连接不支持当前操作异常。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisClusterOperationException()` | 无参构造 | 调用 `this(I18nResourceBundle.i18nString("base.cluster", "base.notSupport", "base.current", "base.operation"))`，组合为“集群不支持当前操作” |
| `ShellRedisClusterOperationException(String msg)` | 指定异常信息构造 | 调用 `super(msg)` → `ShellException(String)` |

- 调用链：`ShellRedisClient.throwClusterException()`（`isClusterMode()` 为真时）→ `new ShellRedisClusterOperationException()`；另在 `ShellRedisClient` 的集群无关命令分支（如 3606、4405 行附近）抛出

## ShellRedisSentinelOperationException

- 职责：Redis 哨兵连接不支持当前操作异常。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisSentinelOperationException()` | 无参构造 | 调用 `this(I18nResourceBundle.i18nString("base.sentinel", "base.notSupport", "base.current", "base.operation"))`，组合为“哨兵不支持当前操作” |
| `ShellRedisSentinelOperationException(String msg)` | 指定异常信息构造 | 调用 `super(msg)` → `ShellException(String)` |

- 调用链：`ShellRedisClient.throwSentinelException()`（`isSentinelMode()` 为真时）→ `new ShellRedisSentinelOperationException()`

## ShellRedisUnsupportedCommandException

- 职责：Redis 服务版本过低、不支持指定命令时抛出的异常。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisUnsupportedCommandException(String serverVersion, String supportedVersion, String command)` | 构造 | 调用 `super(I18nHelper.cmd() + " [" + command + "] " + I18nHelper.notSupport())`，拼出“命令 [xxx] 不支持”；`serverVersion`、`supportedVersion` 仅存在于签名与注释文案中，未参与实际消息拼接（源码保留原中文拼接语句为注释） |

- 调用链：`ShellRedisVersionUtil.checkSupported(String serverVersion, String command)` → `ShellRedisVersionUtil.getSupportedVersion(command)` 取出最低支持版本 → 版本不满足时 `throw new ShellRedisUnsupportedCommandException(serverVersion, version, command)`
