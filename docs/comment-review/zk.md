# easyshell ZooKeeper 客户端（zk 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/zk/，共 9 个 .java，全部存活。

---

## ShellZKClient

- 职责：基于 Curator 的 ZooKeeper 客户端封装（实现 `ShellBaseClient`），提供连接管理、节点 CRUD、ACL、配额、四字命令、集群信息与命令分发等能力。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| lastCreate | `String` | 最后的创建节点 |
| lastUpdate | `String` | 最后的修改节点 |
| lastDelete | `String` | 最后的删除节点 |
| shellConnect | `ShellConnect` | zk 连接信息（final） |
| jumpForwarder | `SSHJumpForwarder2` | SSH 端口转发器（跳板） |
| retryPolicy | `RetryPolicy` | 重试策略 |
| zooKeeper | `ZooKeeper` | 原生 ZooKeeper 对象 |
| framework | `CuratorFramework` | Curator 客户端 |
| state | `SimpleObjectProperty<ShellConnState>` | 连接状态（final，初值 `NOT_INITIALIZED`） |
| stateListener | `ChangeListener<ShellConnState>` | 状态监听器（final，转发 `ShellBaseClient.super.onStateChanged`） |
| autheds | `List<String>` | 已认证摘要列表（final） |

- 方法（静态块注册 SASL 处理器 `ShellZKSASLUtil.registerConfiguration()`）：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKClient(ShellConnect)` | 构造函数 | 保存连接并 `addStateListener(stateListener)` |
| `isReadonly()` / `throwReadonlyException()` | 只读判定/抛异常 | 只读时抛 `ShellReadonlyOperationException` |
| `start(int timeout)` | 建立连接 | `initClient()`；注册 `ConnectionStateListener`；`framework.start()`；`blockUntilConnected` 成功则置 `CONNECTED` 并 `ShellClientChecker.push(this)`，失败则 `closeInner` 置 `FAILED` |
| `initHost()`（私有） | 计算连接地址 | 启用跳板时 `SSHJumpForwarder2.forward(...)` 返回 `127.0.0.1:localPort`，否则直连 `hostIp:hostPort` |
| `initClient()`（私有） | 初始化客户端 | `getEnabledAuths → ShellZKAuthUtil.toAuthInfo`；`ShellZKClientUtil.build(host, connect, retryPolicy, authInfos, zoo->this.zooKeeper=zoo)`；对每个 auth 调 `setAuthed` |
| `close()` / `closeInner()` | 关闭连接 | `closeInner` 关闭跳板转发、`TaskManager.startTimeout(framework::close, 500)`、关闭 SASL 客户端与 zooKeeper |
| `isConnected()` / `isConnecting()` | 连接状态判定 | 结合 `ShellConnState` 与 `framework.getState()`（`STARTED`/`LATENT`） |
| `isLastCreate` / `clearLastCreate` / `isLastUpdate` / `clearLastUpdate` / `isLastDelete` / `clearLastDelete` | 最近操作节点标记 | 比较/清空 `lastCreate/lastUpdate/lastDelete` |
| `setACL(String, List<ACL>)` / `setACL(String, List<ACL>, Integer)` / `setACL(List<String>, List<ACL>)` | 设置节点 ACL | `ShellZKClientActionUtil.forSetAclAction` + `framework.setACL().withVersion(...).withACL(...).forPath(...)`；`NoAuthException` 转 `ShellZKNoAdminPermException` |
| `addACL(String, ACL)` / `addACL(String, List)` / `deleteACL(String, ACL)` | 增删 ACL | 先 `getACL`，再增删后 `setACL` |
| `addAuth(String, String)` | 添加认证 | `zooKeeper.addAuthInfo("digest", ...)`，`ShellZKClientActionUtil.forAddAuthAction`，再 `setAuthed` |
| `exists(String)` | 节点是否存在 | `checkExists(path) != null` |
| `createIncludeParents(String, byte[])` / `createIncludeParents(String, byte[], CreateMode)` | 创建节点并补父节点 | 调 `create(..., true)` |
| `create(...)`（5 个重载） | 创建节点 | `framework.create().creatingParentsIfNeeded()/withTtl/withMode/withACL`；记录 `lastCreate`，`ShellZKClientActionUtil.forCreateAction`；`NoAuthException` 转 `ShellZKNoCreatePermException` |
| `getChildren(String)` | 获取子节点 | `framework.getChildren().forPath`；`NoAuthException` 转 `ShellZKNoChildPermException` |
| `getData(String)` / `getData(String, BackgroundCallback)` / `getDataString(String)` | 获取节点数据 | 同步/异步；`NoAuthException` 转 `ShellZKNoReadPermException` |
| `getACL(String)` / `getACL(String, BackgroundCallback)` | 获取节点权限 | 同步/异步；`NoAuthException` 转 `ShellZKNoAdminPermException` |
| `setData(...)`（4 个重载） | 设置节点数据 | 记录 `lastUpdate`；`framework.setData().withVersion(...)`；`NoAuthException` 转 `ShellZKNoWritePermException` |
| `sync(String)` | 同步节点 | `ShellZKClientActionUtil.forSyncAction` + `framework.sync().forPath` |
| `delete(String)` / `delete(String, Integer, boolean)` | 删除节点 | `DeleteBuilder.guaranteed().withVersion(...)`，可选 `deletingChildrenIfNeeded`；`NoAuthException` 转 `ShellZKNoDeletePermException` |
| `checkExists(String)` | 获取 Stat | `framework.checkExists().forPath` |
| `createQuota(String, long, int)` / `delQuota(String, boolean, boolean)` / `listQuota(String)` | 配额管理 | 分别调 `SetQuotaCommand.createQuota` / `DelQuotaCommand.delQuota` / 读取 `Quotas.quotaZookeeper + path + limitNode` |
| `getEphemerals()` / `getEphemerals(String)` / `getAllChildrenNumber(String)` | 临时节点/子节点统计 | 直接委托 `zooKeeper` |
| `getCurrentConfig()` / `clusterNodes()` | 集群配置/服务列表 | 老版本读 `/zookeeper/config` 解析 `server.` 行，新版本用 `QuorumVerifier.getVotingMembers()` 构造 `ShellZKClusterNode` |
| `connectName()` / `iid()` | 连接名/连接 id | 取 `shellConnect.getName()` / `getId()` |
| `getZooKeeper()` | 获取 ZooKeeper | 优先返回字段，否则从 `framework.getZookeeperClient().getZooKeeper()` |
| `whoami()` / `localNodes()` | 当前用户/本地环境 | `zooKeeper.whoAmI()`；本地构造 `ShellZKEnvNode` 列表（host/connection/sdkVersion/jdkVersion） |
| `envi()`/`enviNodes()`、`srvr()`/`srvrNodes()`、`mntr()`/`mntrNodes()`、`stat()`/`statNodes()`、`conf()`/`confNodes()` | 四字命令及解析 | `FourLetterWordMain.send4LetterWord(hostIp, hostPort, cmd)` 后按分隔符（`=`/`:`/`\t`/`(`）解析为 `ShellZKEnvNode` |
| `cons()` / `ruok()` / `crst()` / `srst()` / `wchc()` / `wchs()` / `wchp()` / `dump()` / `reqs()` / `dirs()` | 其它四字命令 | 均发送四字命令并返回原始文本 |
| `setAuthed(String, String)` / `isAuthed(String)` / `isAnyAuthed(List<ShellZKACL>)` / `isNeedAuth(ShellZKNode)` | 认证状态 | 以 `ShellZKAuthUtil.digest` 生成摘要存入 `autheds`；`isNeedAuth` 结合 `aclEmpty/lackPerm/hasWorldACL/hasIPACL` |
| `isInvalid()` | 客户端是否无效 | `framework == null || framework.getState() == STOPPED` |
| `query(ShellZKQueryParam)` | 命令分发执行 | 依 `param.isXxx()` 分派到 `getChildren/getData/setData/create/delete/setACL/...`，记录耗时与成功标记到 `ShellZKQueryResult` |
| `forkClient()` | 派生子客户端 | 匿名子类覆写 `isForked()` 返回 `true`，`start()` 后返回 |
| `getEnabledAuths()` | 获取启用认证 | `ShellZKAuthStore.INSTANCE.loadEnableByIid(iid())` |

- 调用链：`start → initClient → initHost(SSHJumpForwarder2.forward) / ShellZKAuthUtil.toAuthInfo → ShellZKClientUtil.build → ShellZKFactory.newZooKeeper → framework.start → blockUntilConnected → ShellClientChecker.push`；`create → framework.create → ShellZKClientActionUtil.forCreateAction`；`query → getChildren/getData/create/delete/setACL/...`；`forkClient → new ShellZKClient{isForked=true} → start`

---

## ShellZKClientCnxnSocket

- 职责：自定义 `ClientCnxnSocketNIO`，在建立连接时经由 SOCKS5 代理（供 zk 代理连接使用）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| PROXY_HOST | `String` | 代理地址（`"proxyHost"`，static final） |
| PROXY_PORT | `String` | 代理端口（`"proxyPort"`） |
| PROXY_USER | `String` | 代理用户（`"proxyUser"`） |
| PROXY_PASSWORD | `String` | 代理密码（`"proxyPassword"`） |
| PROXY_PROTOCOL | `String` | 代理协议（`"proxyProtocol"`） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKClientCnxnSocket(ZKClientConfig)` | 构造函数 | 委托父类 |
| `createSock()` | 创建 SocketChannel | 有代理配置时 `ShellProxyUtil.initProxy1` 生成 `Proxy`，`SocketChannel.open()` 后 `configureBlocking(false)` 连到代理并 `finishConnect`；配置 `SoLinger`/`TcpNoDelay`；否则 `super.createSock()` |
| `registerAndConnect(SocketChannel, InetSocketAddress)` | 注册并连接 | 有代理时 `ProxyUtil.socks5Handshake(...)`，`sock.register(selector, OP_READ|OP_WRITE)`，`sendThread.primeConnection()`；否则 `super.registerAndConnect` |

- 调用链：`createSock → ShellZKClientUtil.getProxyConfig → ShellProxyUtil.initProxy1 → SocketChannel.open`；`registerAndConnect → ProxyUtil.socks5Handshake → sock.register → sendThread.primeConnection`；由 `ShellZKFactory` 通过 `ZOOKEEPER_CLIENT_CNXN_SOCKET` 指定

---

## ShellZKClientUtil

- 职责：zk 客户端构建与代理配置读写的工具类。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `build(String, ShellConnect, RetryPolicy, List<AuthInfo>, Consumer<ZooKeeper>)` | 构建 Curator 客户端 | `CuratorFrameworkFactory.builder()` 配置 `connectString/authorization/runSafeService/retryPolicy/zk34CompatibilityMode/threadFactory(ShellZKThread::new)/zookeeperFactory(new ShellZKFactory(...))/sessionTimeoutMs/connectionTimeoutMs` 后 `build()` |
| `newClient(ShellConnect)` | 创建 `ShellZKClient` | `new ShellZKClient(zkConnect)` |
| `setProxyConfig(ZKClientConfig, ShellProxyConfig)` | 写入代理配置 | 将 host/port/protocol 写入配置；密码认证时写入 user/password |
| `getProxyConfig(ZKClientConfig)` | 读取代理配置 | 无 `proxyHost` 返回 `null`，否则构造 `ShellProxyConfig` |

- 调用链：`ShellZKClientUtil.build → CuratorFrameworkFactory.builder().zookeeperFactory(new ShellZKFactory(connect, zooKeeperCallback))`；`ShellZKClient.initClient → ShellZKClientUtil.build`

---

## ShellZKFactory

- 职责：`ZookeeperFactory` 实现，按连接配置创建 `ZooKeeper`（含 SASL、自定义 socket、代理）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| connect | `ShellConnect` | 连接（final） |
| callback | `Consumer<ZooKeeper>` | ZooKeeper 对象回调 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKFactory(ShellConnect, Consumer<ZooKeeper>)` | 构造函数 | 保存连接与回调 |
| `newZooKeeper(String, int, Watcher, boolean)` | 创建 ZooKeeper | 新建 `ZKClientConfig`；按 `ShellZKSASLUtil.isNeedSasl(iid, saslConfig)` 开关 SASL（`ENABLE_CLIENT_SASL_KEY`/`LOGIN_CONTEXT_NAME_KEY`）；设置 `ZOOKEEPER_CLIENT_CNXN_SOCKET` 为 `ShellZKClientCnxnSocket`；启用代理时 `ShellZKClientUtil.setProxyConfig`；`new ZooKeeper(...)`；回调一次后置空 |

- 调用链：`ShellZKClientUtil.build → ShellZKFactory.newZooKeeper → ShellZKSASLUtil.isNeedSasl → ShellZKClientUtil.setProxyConfig → new ZooKeeper(..., clientConfig) → callback.accept(zooKeeper) → ShellZKClient.zooKeeper 赋值`

---

## ShellZKNode

- 职责：zk 节点信息模型（实现 `Comparable<ShellZKNode>`），承载路径、Stat、ACL、配额、加载耗时与缓存数据。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| quota | `StatsTrack` | 配额属性 |
| acl | `List<ShellZKACL>` | acl 权限属性 |
| stat | `Stat` | 状态属性 |
| loadTime | `short` | 加载耗时 |
| nodePath | `String` | 节点路径 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `acl(List<? extends ACL>)` | 设置 ACL 列表 | 空则置空列表，否则逐项 `ShellZKACLUtil.isOpenACL` 判定包装为 `ShellZKACL` |
| `setNodeData(byte[])` / `getNodeData()` / `hasNodeData()` / `getNodeDataSize()` / `clearNodeData()` | 节点数据缓存 | 基于 `hashCode` 与键 `"data"` 委托 `ShellZKCacheUtil` |
| `setUnsavedData(byte[])` / `getUnsavedData()` / `hasUnsavedData()` / `getUnsavedDataSize()` / `clearUnsavedData()` | 未保存数据缓存 | 键 `"unsaved"` 委托 `ShellZKCacheUtil` |
| `copy(ShellZKNode)` | 复制节点 | 复制 acl/stat/quota/nodePath 及节点数据 |
| `decodeNodePath()` / `decodeNodeName()` / `nodeName()` | 路径/名称解码 | `ShellZKNodeUtil.decodePath` / `getName` |
| `isPersistent()` / `isEphemeral()` | 持久/临时节点 | `isEphemeral` 判 `stat().getEphemeralOwner() > 0` |
| `isDubbo()` | 是否 dubbo 节点 | 路径以 `/dubbo` 开头 |
| `isParent()` / `isChildren()` | 父/子节点 | `stat().getNumChildren() > 0` |
| `isRoot()` | 是否根节点 | 路径为 `/` |
| `statInfos()` | 友好状态信息 | `stat()` 非空时用 `ShellZKStatParser.INSTANCE.apply` |
| `compareTo(ShellZKNode)` | 节点比较 | 按 `nodePath` 忽略大小写 |
| `hasPerm(String)` / `hasReadPerm()` / `hasWritePerm()` / `hasDeletePerm()` / `hasCreatePerm()` / `hasAdminPerm()` / `lackPerm()` | 权限判定 | `hasPerm` 遍历 `acl`，`lackPerm` 为四种权限任一缺失 |
| `aclEmpty()` / `hasACL(String)` / `getACLByType(String)` / `hasWorldACL()` / `hasIPACL()` / `hasDigestACL()` / `existIPACL(String)` / `existDigestACL(String)` / `getDigestACLs()` | ACL 查询 | 按 scheme（`world`/`ip`/`digest`）过滤 `ShellZKACL` |
| `getNumChildren()` / `hasChildren()` | 子节点数量/判定 | 取 `stat().getNumChildren()` |
| `nodeEquals(ShellZKNode)` | 节点相等 | 按 `nodePath` 比较 |
| `nodePath()` / `nodePath(String)` / `stat()` / `stat(Stat)` / `acl()` / `loadTime()` / `loadTime(short)` / `quota()` / `quota(StatsTrack)` | 访问器 | 读写对应字段 |

- 调用链：`ShellZKNode.getNodeData → ShellZKCacheUtil.loadData(hashCode, "data")`；`setNodeData → ShellZKCacheUtil.cacheData`；`statInfos → ShellZKStatParser.INSTANCE.apply`；`decodeNodePath → ShellZKNodeUtil.decodePath`

---

## ShellZKSASLConfiguration

- 职责：SASL 登录配置容器（继承 JAAS `Configuration`），以名称为键管理 `AppConfigurationEntry`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| config | `Map<String, AppConfigurationEntry>` | 配置映射 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getAppConfigurationEntry(String)` | 获取配置项 | `config == null` 返回 `null`，否则返回单元素数组 |
| `putAppConfigurationEntry(String, AppConfigurationEntry)` | 添加配置 | 懒初始化 `HashMap` 后 `put` |
| `removeAppConfigurationEntry(String)` | 移除配置 | 非空时 `remove` |
| `containsAppConfigurationEntry(String)` | 是否包含配置 | `config.containsKey(name)` |

- 调用链：`ShellZKSASLUtil.registerConfiguration → Security.setProperty("login.configuration.provider", ShellZKSASLConfiguration.class.getName())`；`ShellZKSASLUtil.addSaslEntry → putAppConfigurationEntry`

---

## ShellZKSASLUtil

- 职责：SASL 注册与登录上下文管理的工具类。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `registerConfiguration()` | 注册配置类 | `Security.setProperty("login.configuration.provider", ShellZKSASLConfiguration.class.getName())` |
| `removeSasl(String)` | 移除 sasl 配置 | `Configuration.getConfiguration()` 为 `ShellZKSASLConfiguration` 时移除该 iid 项 |
| `isNeedSasl(String, ShellZKSASLConfig)` | 是否需要 sasl | 配置已含 iid 直接 true；`saslConfig` 为空或 `checkInvalid()` 返回 false；否则 `addSaslEntry` 后 true |
| `addSaslEntry(ShellZKSASLConfig)`（私有） | 添加 sasl 项 | `Digest` 类型时构造 `DigestLoginModule` 的 `AppConfigurationEntry` 并写入配置 |

- 调用链：`ShellZKClient 静态块 → ShellZKSASLUtil.registerConfiguration`；`ShellZKFactory.newZooKeeper → ShellZKSASLUtil.isNeedSasl → addSaslEntry → ShellZKSASLConfiguration.putAppConfigurationEntry`

---

## ShellZKStatParser

- 职责：实现 `Function<Stat, List<FriendlyInfo<Stat>>>`，把 zk `Stat` 各字段解析为带多语言名称的友好信息列表。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellZKStatParser` | 单例实例（public static final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `apply(Stat)` | 解析 Stat | 逐项构建 `FriendlyInfo`：czxid/mzxid/pzxid/ctime/mtime/version/aversion/cversion/dataLength/numChildren/ephemeralOwner；按 `I18nManager.currentLocale()` 设置简体/繁体/英文名；`ctime/mtime` 为 0 时友好值为空，否则 `Const.DATE_FORMAT.format` |

- 调用链：`ShellZKNode.statInfos → ShellZKStatParser.INSTANCE.apply(stat)`（`I18nManager.currentLocale` / `Const.DATE_FORMAT`）

---

## ShellZKThread

- 职责：zk 任务线程，将任务提交到后台服务执行。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| task | `Runnable` | 执行业务（final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKThread(Runnable)` | 构造函数 | 保存任务 |
| `run()` | 线程执行体 | `BackgroundService.submit(this.task)` |

- 调用链：`ShellZKClientUtil.build → threadFactory(ShellZKThread::new) → ShellZKThread.run → BackgroundService.submit(task)`
