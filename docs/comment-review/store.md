# easyshell 持久化存储层（store 包）代码审查文档

> 范围：easyshell/src/main/java/cn/oyzh/easyshell/store/（含 zk/redis 子包），共 19 个 .java，全部存活。

> 说明：仅新增文档，未改动任何 `.java`。

## 总体结构

- `store` 包下 14 个类，`store/zk` 子包 3 个类，`store/redis` 子包 2 个类，共 19 个类。
- 除 `ShellStoreUtil` 外，全部继承 `cn.oyzh.store.jdbc` 下的基类：`JdbcStandardStore<T>`（标准单表存储）或 `JdbcKeyValueStore<T>`（键值存储）。
- 单例模式：几乎每个存储类都以 `public static final XXX INSTANCE = new XXX();` 暴露唯一实例，依赖方通过 `INSTANCE` 复用。
- `replace` 为统一写入口：内部按主键/业务键判断存在性，存在走 `update`、不存在走 `insert`（或直接 `insert`）。
- `deleteByIid` / `loadByIid` / `getByIid` 组成"按所属连接 id 级联"的一组约定方法，主连接 `ShellConnectStore` 负责聚合调度。
- 说明：源文件中的部分注释代码块（被 `//` 注释的旧实现）保留未删，属正常历史残留，非死代码（编译不参与）。

## ShellConnectStore

- 职责：shell 连接主表 `ShellConnect` 的持久化存储，并聚合调度其下挂的代理、跳板、X11、隧道、SSL、ZK-SASL 等关联配置。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellConnectStore` | 当前实例（单例） |
| x11ConfigStore | `ShellX11ConfigStore` | X11 配置存储，取 `ShellX11ConfigStore.INSTANCE` |
| jumpConfigStore | `ShellJumpConfigStore` | 跳板配置存储，取 `INSTANCE` |
| proxyConfigStore | `ShellProxyConfigStore` | 代理配置存储，取 `INSTANCE` |
| fileCollectStore | `ShellFileCollectStore` | shell 文件收藏存储，取 `INSTANCE` |
| sslConfigStore | `ShellSSLConfigStore` | SSL 配置存储，取 `INSTANCE` |
| zkAuthStore | `ShellZKAuthStore` | ZK 认证配置存储，取 `INSTANCE` |
| zkSaslConfigStore | `ShellZKSASLConfigStore` | ZK SASL 配置存储，取 `INSTANCE` |
| tunnelingConfigStore | `ShellTunnelingConfigStore` | 隧道配置存储，取 `INSTANCE` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `load()` | 加载全部连接 | `synchronized`，委托 `super.selectList()` |
| `loadSSHType()` | 加载 SSH 类型连接 | `selectList(new SelectParam())` 后 `filter(ShellConnect::isSSHType)` |
| `loadRedisType()` | 加载 Redis 类型连接 | 同上，`filter(ShellConnect::isRedisType)` |
| `loadZKType()` | 加载 ZK 类型连接 | 同上，`filter(ShellConnect::isZKType)` |
| `loadMysqlType()` | 加载 MySQL 类型连接 | 同上，`filter(ShellConnect::isMysqlType)` |
| `loadMongoType()` | 加载 Mongo 类型连接 | 同上，`filter(ShellConnect::isMongoType)` |
| `loadDamengType()` | 加载达梦类型连接 | 同上，`filter(ShellConnect::isDamengType)` |
| `loadTermType()` | 加载终端类型连接 | 同上，`filter(ShellConnect::isTermType)` |
| `loadFileType()` | 加载文件类型连接 | 同上，`filter(ShellConnect::isFileType)` |
| `modelClass()` | 声明模型类型 | 返回 `ShellConnect.class` |
| `loadFull()` | 加载完整连接（含关联配置） | 对每个连接：`proxyConfigStore.getByIid`、`jumpConfigStore.loadByIid`；SSH 再取 `x11ConfigStore.getByIid`、`tunnelingConfigStore.loadByIid`；Redis/Mongo 取 `sslConfigStore.getByIid`；ZK 取 `zkSaslConfigStore.getByIid` |
| `delete(ShellConnect model)` | 删除连接并级联删除关联配置 | `super.delete` 成功后：`jumpConfigStore/proxyConfigStore/fileCollectStore.deleteByIid`，再按类型删除 `x11ConfigStore`+`tunnelingConfigStore` / `sslConfigStore` / `zkAuthStore`+`zkSaslConfigStore` |
| `replace(ShellConnect model)` | 存在则更新、否则插入，并整体重写关联配置 | `exist(id)` 判存在→`update`/`insert`；跳板/隧道用 `replace(List)` 循环，代理/X11/SSL/ZK-SASL 为空时 `deleteByIid` 清理 |
| `sync(ShellConnect model)` | 数据同步专用，仅更新不删除 | 与 `replace` 类似，但关联配置为空时**不做删除**，避免覆盖式同步误删 |

- 调用链：`ShellConnectStore.loadFull() → load() → proxyConfigStore.getByIid()/jumpConfigStore.loadByIid()/x11ConfigStore.getByIid()/tunnelingConfigStore.loadByIid()/sslConfigStore.getByIid()/zkSaslConfigStore.getByIid()`
- 调用链：`ShellConnectStore.delete() → super.delete() → {jumpConfigStore|proxyConfigStore|fileCollectStore|x11ConfigStore|tunnelingConfigStore|sslConfigStore|zkAuthStore|zkSaslConfigStore}.deleteByIid()`

## ShellFileCollectStore

- 职责：shell 文件收藏 `ShellFileCollect` 的持久化存储，带最大数量上限的滚动淘汰。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| Max_Size | `int` | 最大收藏数量，默认 20（`public static`，可外部调整） |
| INSTANCE | `ShellFileCollectStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `modelClass()` | 声明模型类型 | 返回 `ShellFileCollect.class` |
| `exist(String iid, String path)` | 按连接 id + 路径判断是否存在 | 构造 `HashMap{iid,content}` 调 `super.exist(map)` |
| `delete(String iid, String path)` | 按连接 id + 路径删除 | 构造 `DeleteParam`，加入 `iid`、`content` 查询条件，调 `super.delete(param)` |
| `replace(ShellFileCollect model)` | 收藏（不存在则插入），并淘汰超限数据 | `exist(iid,content) \|\| insert`；再查 `saveTime` 升序第 `Max_Size` 条，存在则 `super.delete(data.getId())` |
| `deleteByIid(String iid)` | 按连接 id 删除 | iid 空返回 false，否则 `DeleteParam` 按 `iid` 删除 |
| `loadByIid(String iid)` | 按连接 id 加载，按保存时间倒序 | `selectList(QueryParam.of("iid",iid))` 后按 `getSaveTime` 排序并 `reversed()` |

- 调用链：`ShellFileCollectStore.replace() → exist() → super.exist() / insert() → selectOne(超限查询) → super.delete(id)`
- 调用链：`ShellConnectStore.loadFull()/delete() → fileCollectStore.loadByIid()/deleteByIid()`

## ShellGroupStore

- 职责：shell 分组 `ShellGroup` 的持久化存储。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellGroupStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `load()` | 加载全部分组 | 委托 `super.selectList()` |
| `replace(ShellGroup model)` | 存在（按 `gid`）则更新，否则插入 | `exist(model.getGid())` → `update` / `insert`；model 为 null 返回 false |
| `modelClass()` | 声明模型类型 | 返回 `ShellGroup.class` |

- 调用链：`ShellGroupStore.replace() → super.exist(gid) → super.update()/insert()`
- 说明：源文件保留了两段按名称删除/存在判断的注释代码，未参与编译。

## ShellJumpConfigStore

- 职责：shell 跳板配置 `ShellJumpConfig` 的持久化存储。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellJumpConfigStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `replace(List<ShellJumpConfig> models)` | 批量替换 | 循环调用单条 `replace`，异常 `printStackTrace` 并返回 false，全部成功返回 true |
| `replace(ShellJumpConfig model)` | 存在（按 `id`）则更新，否则插入 | `super.exist(model.getId())` → `update` / `insert` |
| `modelClass()` | 声明模型类型 | 返回 `ShellJumpConfig.class` |
| `deleteByIid(String iid)` | 按连接 id 删除 | iid 空返回 false，否则按 `iid` 删除 |
| `loadByIid(String iid)` | 按连接 id 加载，过滤无效数据并按 `order` 排序 | `selectList(QueryParam.of("iid",iid))`→过滤 `user`/`host` 均非空→`Comparator.comparingInt(getOrder)` |

- 调用链：`ShellJumpConfigStore.replace(List) → replace(单条) → super.exist(id) → super.update()/insert()`
- 调用链：`ShellConnectStore.loadFull() → jumpConfigStore.loadByIid() → 过滤 → 排序`

## ShellKeyStore

- 职责：shell 密钥 `ShellKey` 的持久化存储。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellKeyStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `replace(ShellKey model)` | 存在（按 `id`）则更新，否则插入 | `super.exist(id)` → `super.update(model)` / `insert(model)` |
| `modelClass()` | 声明模型类型 | 返回 `ShellKey.class` |

- 调用链：`ShellKeyStore.replace() → super.exist(id) → super.update()/insert()`

## ShellProxyConfigStore

- 职责：代理配置 `ShellProxyConfig` 的持久化存储。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellProxyConfigStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `replace(ShellProxyConfig model)` | 依次按 `id`、`iid` 判断存在性后写库 | `id` 非空且 `exist(id)`→`update`；否则 `iid` 非空且 `exist(map{iid})`→`update`；都不命中则 `insert` |
| `getByIid(String iid)` | 按连接 id 获取代理配置 | iid 空返回 null，否则 `selectOne(QueryParam.of("iid",iid))`（源码 javadoc 误写为 sasl 配置） |
| `deleteByIid(String iid)` | 按连接 id 删除 | iid 空返回 false，否则按 `iid` 删除 |
| `modelClass()` | 声明模型类型 | 返回 `ShellProxyConfig.class` |

- 调用链：`ShellProxyConfigStore.replace() → super.exist(id)/super.exist(map) → super.update()/insert()`
- 调用链：`ShellConnectStore.loadFull() → proxyConfigStore.getByIid()`

## ShellQueryStore

- 职责：shell 查询 `ShellQuery` 的持久化存储（收藏的 SQL/查询语句）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellQueryStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `list(String iid)` | 按连接 id 加载查询列表 | 构造 `QueryParam`（`iid`）后 `selectList` |
| `list(String iid, String dbName)` | 按连接 id + 数据库名加载 | `SelectParam` 加入 `iid`、`dbName` 两个查询条件 |
| `replace(ShellQuery model)` | 不存在（按 `uid`）则插入，否则更新 | `exist(model.getUid())` 分支；model 为 null 返回 false |
| `deleteByIid(String iid)` | 按连接 id 删除 | 条件为 `StringUtil.isEmpty(iid)` 时执行删除（源码判断与语义相反，iid 为空才会走删除，疑似缺陷） |
| `modelClass()` | 声明模型类型 | 返回 `ShellQuery.class` |

- 调用链：`ShellQueryStore.replace() → exist(uid) → insert()/update()`

## ShellSSLConfigStore

- 职责：SSL 配置 `ShellSSLConfig` 的持久化存储。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellSSLConfigStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `replace(ShellSSLConfig model)` | 按 `id`、`iid` 判断后写库 | `if (StringUtil.isNotBlank(id) \|\| super.exist(id))` 直接 `update`（用 `\|\|`，与 `ShellProxyConfigStore` 的 `&&` 不一致，id 非空即 update 可能命中 0 行）；否则 `iid` 命中则 `update`，都不命中 `insert` |
| `getByIid(String iid)` | 按连接 id 获取 SSL 配置 | iid 空返回 null，否则 `selectOne(QueryParam.of("iid",iid))`（源码 javadoc 误写为 sasl 配置） |
| `deleteByIid(String iid)` | 按连接 id 删除 | iid 空返回 false，否则按 `iid` 删除 |
| `modelClass()` | 声明模型类型 | 返回 `ShellSSLConfig.class` |

- 调用链：`ShellConnectStore.loadFull() → sslConfigStore.getByIid()`
- 调用链：`ShellConnectStore.replace() → sslConfigStore.replace()/deleteByIid()`

## ShellSettingStore

- 职责：shell 全局设置 `ShellSetting` 的键值存储（继承 `JdbcKeyValueStore`）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellSettingStore` | 当前实例（单例） |
| SETTING | `ShellSetting` | 当前设置，类加载时 = `INSTANCE.load()` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `load()` | 加载设置，异常/空时回落默认值 | `super.select()`，捕获异常 `printStackTrace`，null 时 `new ShellSetting()` |
| `replace(ShellSetting model)` | 更新设置 | model 非空则 `update`，否则 false（键值表单条，无 insert） |
| `modelClass()` | 声明模型类型 | 返回 `ShellSetting.class` |

- 调用链：`ShellSettingStore.SETTING（类初始化） → INSTANCE.load() → super.select()`

## ShellSnippetStore

- 职责：shell 代码片段 `ShellSnippet` 的持久化存储。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellSnippetStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `replace(ShellSnippet model)` | 存在（按 `id`）则更新，否则插入 | `super.exist(id)` → `super.update` / `insert` |
| `listByName(String name)` | 按名称模糊查询 | `SelectParam` 加入 `QueryParam.of("name","%"+name+"%","LIKE")` |
| `modelClass()` | 声明模型类型 | 返回 `ShellSnippet.class` |

- 调用链：`ShellSnippetStore.replace() → super.exist(id) → super.update()/insert()`

## ShellStoreUtil

- 职责：shell 存储层初始化/销毁工具类（非存储实体，直接调用 `cn.oyzh.store.jdbc` 门面）。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `init()` | 初始化 JDBC 存储引擎 | `JdbcConst.dbCacheSize(1024)`、`dbDialect(JdbcDialect.H2)`、`dbFile(ShellConst.getStorePath()+"db")`，再 `JdbcManager.takeoff()`；捕获异常且消息含 `Database may be already in use` 时 `MessageBox.warn(I18nHelper.programTip1())` |
| `destroy()` | 销毁存储引擎 | `JdbcManager.destroy()` |

- 调用链：`ShellStoreUtil.init() → JdbcConst.* → JdbcManager.takeoff() → （异常）MessageBox.warn()`
- 调用链：`ShellStoreUtil.destroy() → JdbcManager.destroy()`

## ShellTerminalHistoryStore

- 职责：shell 终端历史 `ShellTerminalHistory` 的持久化存储，仅追加。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellTerminalHistoryStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `replace(ShellTerminalHistory model)` | 追加写入（不做存在性判断） | 直接 `this.insert(model)` |
| `modelClass()` | 声明模型类型 | 返回 `ShellTerminalHistory.class` |

- 调用链：`ShellTerminalHistoryStore.replace() → insert()`

## ShellTunnelingConfigStore

- 职责：shell 隧道（端口转发）配置 `ShellTunnelingConfig` 的持久化存储。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellTunnelingConfigStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `replace(List<ShellTunnelingConfig> models)` | 批量替换 | 循环单条 `replace`，异常 `printStackTrace` 返回 false |
| `replace(ShellTunnelingConfig model)` | 存在（按 `id`）则更新，否则插入 | `super.exist(model.getId())` → `update` / `insert` |
| `modelClass()` | 声明模型类型 | 返回 `ShellTunnelingConfig.class` |
| `deleteByIid(String iid)` | 按连接 id 删除 | iid 空返回 false，否则按 `iid` 删除 |
| `loadByIid(String iid)` | 按连接 id 加载 | iid 空返回 null，否则 `selectList(QueryParam.of("iid",iid))` |

- 调用链：`ShellConnectStore.loadFull() → tunnelingConfigStore.loadByIid()`
- 调用链：`ShellConnectStore.replace() → tunnelingConfigStore.replace(List) → replace(单条)`

## ShellX11ConfigStore

- 职责：X11 转发配置 `ShellX11Config` 的持久化存储。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellX11ConfigStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `replace(ShellX11Config model)` | 按 `iid` 判断存在性后写库 | `super.exist(model.getIid())` → `update` / `insert` |
| `getByIid(String iid)` | 按连接 id 获取 X11 配置 | iid 空返回 null，否则 `selectOne(QueryParam.of("iid",iid))`（源码 javadoc 误写为 sasl 配置） |
| `deleteByIid(String iid)` | 按连接 id 删除 | iid 空返回 false，否则按 `iid` 删除 |
| `modelClass()` | 声明模型类型 | 返回 `ShellX11Config.class` |

- 调用链：`ShellConnectStore.loadFull() → x11ConfigStore.getByIid()`

## ShellZKAuthStore

- 职责：ZK 认证 `ShellZKAuth` 的持久化存储（按 `iid+user+password` 判重）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellZKAuthStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `loadByIid(String iid)` | 按 zk 连接 id 加载 | `selectList(QueryParam.of("iid",iid))` |
| `loadEnableByIid(String iid)` | 加载已启用认证 | `loadByIid(iid)` 后 `filter(ShellZKAuth::isEnable).toList()` |
| `replace(ShellZKAuth model)` | 命中已有认证则更新，否则插入 | `select(user,password,iid)` 命中→回填字段后 `update(auth)`；否则 `insert(model)` |
| `deleteByIid(String iid)` | 按 zk 连接 id 删除 | iid 非空时按 `iid` 删除 |
| `exist(String user, String password, String iid)` | 判断认证是否已存在 | 三参数均非空时 `super.exist(map{iid,user,password})` |
| `select(String user, String password, String iid)` | 查询单条认证 | 三参数均非空时 `selectOne(SelectParam{iid,user,password})`，否则 null |
| `modelClass()` | 声明模型类型 | 返回 `ShellZKAuth.class` |

- 调用链：`ShellZKAuthStore.replace() → select(user,password,iid) → update()/insert()`
- 调用链：`ShellZKAuthStore.loadEnableByIid() → loadByIid() → filter(isEnable)`

## ShellZKCollectStore

- 职责：ZK 路径收藏 `ShellZKCollect` 的持久化存储（按 `iid+path` 判重）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellZKCollectStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `loadByIid(String iid)` | 按 zk 连接 id 加载 | 构造 `QueryParam{name=iid,data=iid}` 后 `selectList` |
| `replace(String iid, String path)` | 便捷重载 | 委托 `replace(new ShellZKCollect(iid,path))` |
| `replace(ShellZKCollect model)` | 不存在（按 `iid+path`）才插入 | `!exist(iid,path)` 时 `insert(model)`，否则返回 false（不更新） |
| `deleteByIid(String iid)` | 按 zk 连接 id 删除 | 条件为 `StringUtil.isEmpty(iid)` 时执行删除（源码判断与语义相反，iid 为空才删除，疑似缺陷） |
| `delete(String iid, String path)` | 按 `iid+path` 删除 | 条件为 `isEmpty(iid) && isEmpty(path)` 时构造 `DeleteParam`，但实际执行 `this.delete(path)`（源码逻辑与注释语义不符，疑似缺陷） |
| `exist(String iid, String path)` | 判断收藏是否存在 | 两参数均非空时 `super.exist(map{iid,path})` |
| `modelClass()` | 声明模型类型 | 返回 `ShellZKCollect.class` |

- 调用链：`ShellZKCollectStore.replace(iid,path) → replace(model) → exist(iid,path) → insert()`

## ShellZKSASLConfigStore

- 职责：ZK SASL 配置 `ShellZKSASLConfig` 的持久化存储。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellZKSASLConfigStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `replace(ShellZKSASLConfig model)` | 存在（按 `id`）则更新，否则插入 | `super.exist(model.getId())` → `update` / `insert` |
| `modelClass()` | 声明模型类型 | 返回 `ShellZKSASLConfig.class` |
| `getByIid(String iid)` | 按 zk 连接 id 获取 SASL 配置 | iid 空返回 null，否则 `selectOne(QueryParam.of("iid",iid))` |
| `deleteByIid(String iid)` | 按 zk 连接 id 删除配置 | 构造 `DeleteParam{iid}` 后 `super.delete`（无 iid 非空校验） |

- 调用链：`ShellConnectStore.loadFull() → zkSaslConfigStore.getByIid()`

## RedisCollectStore

- 职责：Redis 键收藏 `ShellRedisCollect` 的持久化存储（按 `iid+dbIndex+key` 判重）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `RedisCollectStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `loadByIid(String iid)` | 按连接 id 加载收藏列表 | 构造 `QueryParam{name=iid,data=iid}` 后 `selectList`（源文件保留旧实现注释） |
| `replace(String iid, int dbIndex, String key)` | 便捷重载 | 委托 `replace(new ShellRedisCollect(iid,dbIndex,key))` |
| `replace(ShellRedisCollect model)` | 不存在（按 `iid+dbIndex+key`）才插入 | `!exist(iid,dbIndex,key)` 时 `insert(model)`，否则 false（不更新） |
| `delete(String iid, Integer dbIndex, String key)` | 按 `key+iid+dbIndex` 删除 | `iid`、`key` 非空时构造 `DeleteParam` 三个条件后 `super.delete` |
| `exist(String iid, int dbIndex, String key)` | 判断收藏是否存在 | `iid`、`key` 非空时 `super.exist(map{iid,key,dbIndex})` |
| `modelClass()` | 声明模型类型 | 返回 `ShellRedisCollect.class` |
| `deleteByIid(String iid)` | 按连接 id 删除 | 构造 `DeleteParam{iid}` 后 `super.delete`，返回 `void`（与同类 `boolean` 返回值约定不一致） |

- 调用链：`RedisCollectStore.replace(iid,dbIndex,key) → replace(model) → exist() → insert()`

## RedisKeyFilterHistoryStore

- 职责：Redis 键过滤历史 `ShellRedisKeyFilterHistory` 的持久化存储，带最大数量上限与分页查询。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| Max_Size | `int` | 最大历史数量，默认 50（`public static`，可外部调整） |
| INSTANCE | `RedisKeyFilterHistoryStore` | 当前实例（单例） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `load()` | 加载全部历史 | 委托 `super.selectList()` |
| `replace(ShellRedisKeyFilterHistory model)` | 插入历史并淘汰超限数据 | model 为 null 返回 false；`insert` 成功后按 `saveTime` 升序查第 `Max_Size` 条（额外按 `iid`、`pattern` 过滤），存在则 `this.delete(data.getUid())` |
| `getPage(long pageNo, int limit, String kw)` | 分页查询历史（按 `pattern` 模糊） | `selectPage(kw, ["pattern"], PageParam)`；非空时 `selectCount` 组装 `Paging` 并 `currentPage(pageNo)`，空则 `new Paging<>(limit)` |
| `exist(String kw)` | 判断模式是否已存在 | kw 非空时 `super.exist(map{pattern=kw})` |
| `modelClass()` | 声明模型类型 | 返回 `ShellRedisKeyFilterHistory.class` |
| `getPatterns()` | 获取全部模式 | `load()` 后 `parallelStream().map(getPattern)` 收集 |

- 调用链：`RedisKeyFilterHistoryStore.getPage() → selectPage(kw,["pattern"],PageParam) → selectCount(kw,["pattern"]) → new Paging`
- 调用链：`RedisKeyFilterHistoryStore.replace() → insert() → selectOne(超限查询) → delete(uid)`
- 说明：源文件保留了一段按 `iid+pattern` 删除的注释方法，未参与编译。
