# easyshell 领域模型（domain 包）代码审查文档

> 范围：easyshell/src/main/java/cn/oyzh/easyshell/domain/（含 zk/redis 子包），共 18 个 .java，全部存活。

> 说明：仅新增文档，未改动任何 `.java`。

本包均为配置 / 实体模型，主要以普通字段 + getter/setter 表达，并通过 `@Table` / `@Column` / `@PrimaryKey`（`cn.oyzh.store.jdbc`）标注持久化映射；部分控件的“可编辑属性”由 `FXToggleSwitch` 之类的控件工厂方法（如 `getEnabledStatus()`）在读取时动态构造，而非 JavaFX 属性字段。多个模型继承自通用基类（`SSHConnect`、`SSHProxyConfig`、`SSHTunneling`、`TerminalHistory`、`AppGroup`、`AppSetting`），继承字段在下表中以“（继承自 X）”标注。

常用辅助类：
- `StringUtil` / `CollectionUtil` / `BooleanUtil`（`cn.oyzh.common.util`）
- `JSONUtil`（`cn.oyzh.common.json`）、`JSONObject` / `@JSONField`（fastjson2）
- 各模型均有对应存储类 `cn.oyzh.easyshell.store.*`（如 `ShellConnectStore`、`ShellQueryStore`），负责 `select` / `replace` / `update` 等持久化动作。

---

## ShellConnect

- 职责：SSH / FTP / SFTP / Redis / ZK / MongoDB 等全部连接类型的统一连接配置实体，是连接树、各协议客户端与持久化存储的核心模型。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| id | `String` | 数据 id（主键） |
| host | `String` | 连接地址（形如 `ip:port`） |
| name | `String` | 名称 |
| remark | `String` | 备注信息 |
| groupId | `String` | 分组 id |
| user | `String` | 认证用户 |
| password | `String` | 认证密码 |
| charset | `String` | 字符集（默认 utf-8） |
| termType | `String` | 终端类型（默认 xterm） |
| backspaceType | `Integer` | 终端退格类型 |
| altSendsEscape | `Boolean` | 终端 alt 修饰 |
| connectTimeOut | `Integer` | 连接超时时间（秒） |
| jumpConfigs | `List<ShellJumpConfig>` | 跳板信息列表 |
| forwardAgent | `Boolean` | 客户端转发（Agent 转发） |
| x11forwarding | `Boolean` | x11 转发开关 |
| x11Config | `ShellX11Config` | x11 配置 |
| authMethod | `String` | 认证方式（password/certificate/manager/sshAgent…） |
| certificate | `String` | 证书路径 |
| certificatePwd | `String` | 证书密码 |
| keyId | `String` | 密钥 id |
| osType | `String` | 系统类型 |
| enableProxy | `Boolean` | 是否开启代理转发 |
| proxyConfig | `ShellProxyConfig` | 代理配置 |
| tunnelingConfigs | `List<ShellTunnelingConfig>` | 隧道信息列表 |
| type | `String` | 连接类型（ssh/ftp/sftp/local/serial/telnet/vnc/smb/s3/redis/zk/rdp/webdav/mysql/dameng/mongodb/mosh…） |
| serialBaudRate | `int` | 波特率（串口） |
| serialPortName | `String` | 端口名（串口） |
| serialParityBits | `int` | 校验位（串口） |
| serialNumDataBits | `int` | 数据位（串口） |
| serialNumStopBits | `int` | 停止位（串口） |
| serialFlowControl | `int` | 流控（串口） |
| sslMode | `Boolean` | ssl 模式（ftp/vnc/redis/mysql/rdp 使用） |
| ftpPassiveMode | `Boolean` | ftp 被动模式 |
| environment | `String` | 环境信息（多行 `KEY=VALUE`） |
| enableCompress | `Boolean` | 启用压缩 |
| enableZModem | `Boolean` | ssh 协议，启用 ZModem |
| showFile | `Boolean` | ssh 协议，显示文件 |
| serverMonitor | `Boolean` | ssh 协议，显示服务监控 |
| followTerminalDir | `Boolean` | ssh 协议，跟随终端目录 |
| showHiddenFile | `Boolean` | ftp/sftp 是否显示隐藏文件 |
| region | `String` | s3 协议，区域 |
| s3Type | `String` | s3 协议，类型（alibaba/huawei/tencent/minio/s3） |
| smbShareName | `String` | smb 协议，共享名称 |
| domain | `String` | 域（smb/rdp 协议） |
| readonly | `Boolean` | 只读（zk/redis/vnc 协议） |
| executeTimeOut | `Integer` | 执行超时（redis 协议，秒） |
| sslConfig | `ShellSSLConfig` | ssl 配置（redis） |
| saslAuth | `Boolean` | 是否开启 sasl 认证（zk 协议） |
| sessionTimeOut | `Integer` | 会话超时时间（zk 协议，分钟） |
| compatibility | `Integer` | 兼容模式（zk 协议，`1` 表示兼容 3.4.x） |
| saslConfig | `ShellZKSASLConfig` | sasl 配置（zk） |
| mongoAuthDatabase | `String` | MongoDB 认证数据库 |
| mongoSpecifiedDatabase | `String` | MongoDB 指定数据库（逗号分隔） |
| moshKey | `String` | Mosh 认证 key |
| collects | `List<String>` | 收藏列表（路径集合） |
| extras | `String` | 扩展内容（JSON 字符串） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `copy(ShellConnect)` | 从另一连接复制全部配置 | 逐字段拷贝；跳板/隧道/x11/代理/ssl/sasl 调用各自的 `clone()` 深拷贝 |
| `compareTo(ShellConnect)` | 按名称不区分大小写排序 | `name.compareToIgnoreCase` |
| `compare(ShellConnect)` | 相等判定 | 比较 `id` |
| `hostIp()` | 返回连接 ip | 取 `host.split(":")[0]`，空则返回 "" |
| `hostPort()` | 返回连接端口 | `host.split(":")[1]` 转 int，异常返回 -1 |
| `connectTimeOutMs()` | 连接超时毫秒值 | `getConnectTimeOut() * 1000`；`getConnectTimeOut()` 空或 <3 时返回 5 |
| `executeTimeOutMs()` | 执行超时毫秒值 | `getExecuteTimeOut() * 1000`；`getExecuteTimeOut()` 空时返回 3 |
| `sessionTimeOutMs()` | 会话超时毫秒值 | `getSessionTimeOut() * 60 * 1000`；`getSessionTimeOut()` 空或 <1 时返回 30 |
| `environments()` | 解析环境变量 | 按行 `split("=")`，仅 `length==2` 的行入 `Map` |
| `mongoSpecifiedDatabases()` | 解析指定的 MongoDB 库集合 | 逗号 `split`，非空项入 `Set` |
| `getEnableJumpConfigs()` | 取启用的跳板配置 | `jumpConfigs.parallelStream().filter(ShellJumpConfig::isEnabled).toList()` |
| `getEnableTunnelingConfigs()` | 取启用的隧道配置 | `tunnelingConfigs.parallelStream().filter(ShellTunnelingConfig::isEnabled).toList()` |
| `isEnableJump()` / `isJumpForward()` / `isEnableTunneling()` | 是否开启跳板 / 隧道 | 基于上者可空/非空判定 |
| `isSSHType()` / `isLocalType()` / `isSFTPType()` / `isFTPType()` / `isS3Type()` / `isSMBType()` / `isRedisType()` / `isZKType()` / `isRDPType()` / `isWebdavType()` / `isMysqlType()` / `isDamengType()` / `isMongoType()` / `isMoshType()` 等 | 连接类型判定 | 按 `type` 字符串比较；`isZKType()` 支持 `zookeeper`/`zk`，`isWebdavType()`/`isMysqlType()`/`isDamengType()`/`isMongoType()`/`isMoshType()` 使用 `ShellPrototype` 常量 |
| `isTermType()` | 是否终端类连接 | `isSSHType()\|\|isLocalType()\|\|isTelnetType()\|\|isSerialType()\|\|isRloginType()\|\|isMoshType()` |
| `isFileType()` | 是否文件类连接 | ssh/sftp/ftp/s3/smb/webdav |
| `isPasswordAuth()` / `isCertificateAuth()` / `isManagerAuth()` / `isSSHAgentAuth()` | 认证方式判定 | 基于 `authMethod` |
| `isEnableCompress()` / `isEnableZModem()` / `isShowFile()` | 默认开启的开关 | 字段为 null 视作开启 |
| `isEnableProxy()` / `isX11forwarding()` / `isServerMonitor()` / `isFollowTerminalDir()` / `isShowHiddenFile()` / `isForwardAgent()` / `isReadonly()` / `isSASLAuth()` | 开关读取 | 借助 `BooleanUtil.isTrue`（null 视作 false） |
| `isAlibabaS3Type()` / `isHuaweiS3Type()` / `isTencentS3Type()` / `isMinioS3Type()` / `isStandardS3Type()` | 云厂商 S3 判定 | 依据 `s3Type` 与 `host` 域名后缀 |
| `compatibility34()` | 是否兼容 zk 3.4.x | `Objects.equals(1, compatibility)` |
| `isCollect(String)` / `addCollect(String)` / `removeCollect(String)` | 收藏集合操作 | 基于 `collects` 列表 |
| `extrasJson()`（private） | 解析 `extras` 为 `JSONObject` | `JSONUtil.parseObject`，异常返回空对象 |
| `putExtra(String,Object)` / `containsExtra(String)` / `getExtra(String)` | 扩展内容读写 | 通过 `extrasJson()` 序列化回 `extras` |
| `setHost(String)` | 设置连接地址 | 写入前 `trim()` |
| `getCharset()` / `getTermType()` / `getType()` | 带默认值的读取 | 分别默认 `utf-8` / `xterm` / `ssh` |

- 调用链：`ShellConnectStore.loadByIid → ShellConnect`；`ShellBaseSSHClient.connect → shellConnect.getEnableJumpConfigs() → ShellJumpConfig.isEnabled()`；`ShellZKClient.init → shellConnect.hostIp()/hostPort()`；`ShellConnect.getEnableTunnelingConfigs → ShellTunnelingConfig.isEnabled()`。

---

## ShellFileCollect

- 职责：文件（文件管理器）收藏记录，记录某连接下收藏的文件内容。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| id | `String` | 数据 id（主键） |
| iid | `String` | 所属连接 id |
| saveTime | `long` | 保存时间 |
| content | `String` | 内容 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getId()` / `setId(String)` | 读取/设置数据 id | 纯访问器 |
| `getIid()` / `setIid(String)` | 读取/设置所属连接 id | 纯访问器 |
| `getSaveTime()` / `setSaveTime(long)` | 读取/设置保存时间 | 纯访问器 |
| `getContent()` / `setContent(String)` | 读取/设置内容 | 纯访问器 |

- 调用链：`ShellFileCollectStore → ShellFileCollect`（收藏的新增/查询/删除）。

---

## ShellGroup

- 职责：连接分组（目录）模型，直接继承通用 `AppGroup`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| gid | `String` | 分组 id（继承自 `AppGroup`，主键） |
| pid | `String` | 父 id（继承自 `AppGroup`） |
| name | `String` | 分组名称（继承自 `AppGroup`） |
| expand | `Boolean` | 是否展开分组（继承自 `AppGroup`） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellGroup()` | 无参构造 | 调用 `super()` |
| `ShellGroup(String, String, boolean)` | 构造分组 | 调用 `super(gid, name, expand)` |
| `isExpand()` / `getGid()` / `setGid(String)` / `getName()` / `setName(String)` / `getPid()` / `setPid(String)` / `setExpand(boolean)` | 继承自 `AppGroup` 的访问器 | 见 `AppGroup` |
| `compareTo(AppGroup)` | 按名称排序（继承） | `name.compareToIgnoreCase` |
| `copy(Object)` | 复制（继承） | 复制 `gid`/`name`/`expand` |

- 调用链：`ShellGroupStore → ShellGroup`（分组树加载/保存）。

---

## ShellJumpConfig

- 职责：SSH 跳板机配置，继承 `SSHConnect` 以复用跳板机自身的连接参数。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| id | `String` | id（主键） |
| iid | `String` | 所属连接 id（指向 `ShellConnect`） |
| enabled | `Boolean` | 是否启用 |
| name / order / port / host / user / password / timeout / authMethod / forwardAgent / certificatePath / certificatePwd / certificatePubKey / certificatePriKey | 见 `SSHConnect` | 跳板机连接参数（继承自 `SSHConnect`） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getId()` / `setId(String)` | 读取/设置 id | 纯访问器 |
| `getIid()` / `setIid(String)` | 读取/设置所属连接 id | 纯访问器 |
| `isEnabled()` | 是否启用 | `enabled == null \|\| enabled` |
| `setEnabled(boolean)` | 设置启用状态 | 纯访问器 |
| `getEnabledStatus()` | 构造启用状态开关控件 | 新建 `FXToggleSwitch`，`selectedChanged` 回写 `setEnabled` |
| `copy(SSHConnect)` | 复制 | 先 `super.copy(t1)`，再复制 `enabled` |
| `clone(List<ShellJumpConfig>)`（static） | 深拷贝跳板列表 | 逐个 `new ShellJumpConfig()` 并 `copy`；空集合返回 `Collections.emptyList()` |

- 调用链：`ShellConnect.getEnableJumpConfigs → ShellJumpConfig.isEnabled()`；`ShellConnect.copy → ShellJumpConfig.clone()`；`ShellJumpConfigStore → ShellJumpConfig`。

---

## ShellKey

- 职责：SSH 密钥实体，管理公私钥与密钥口令。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| id | `String` | 数据 id（主键） |
| name | `String` | 名称 |
| type | `String` | 类型 |
| length | `long` | 长度 |
| password | `String` | 密码（口令） |
| publicKey | `String` | 公钥 |
| privateKey | `String` | 密钥（私钥） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `copy(ShellKey)` | 复制 | 复制 name/type/length/publicKey/privateKey（不含 id/password） |
| `compare(ShellKey)` | 相等判定 | 同时比较 `privateKey` 与 `publicKey` |
| `getPrivateKey()` | 获取私钥 | 若私钥非空且不以 `\n` 结尾则补 `\n` |
| `setPrivateKey(String)` | 设置私钥 | 同上，非空且不以 `\n` 结尾则补 `\n` |
| `getPublicKeyBytes()` | 公钥字节数组 | `publicKey.getBytes()` |
| `getPasswordBytes()` | 口令字节数组 | `password.getBytes()` |
| `getPrivateKeyBytes()` | 私钥字节数组 | `privateKey.getBytes()` |
| `getId()` / `getName()` / `getType()` / `getLength()` / `getPublicKey()` / `getPassword()` 及对应 setter | 访问器 | 纯读写 |

- 调用链：`ShellKeyStore → ShellKey`；`ShellKeyUtil → ShellKey.getPrivateKeyBytes()/getPublicKeyBytes()/getPasswordBytes()`。

---

## ShellProxyConfig

- 职责：SSH 代理配置，继承通用 `SSHProxyConfig`，补充主键与所属连接 id。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| iid | `String` | 所属连接 id |
| id | `String` | 数据 id（主键） |
| protocol / host / port / authType / user / password | 见 `SSHProxyConfig` | 代理协议/地址/端口/认证类型/用户名/密码（继承自 `SSHProxyConfig`） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getIid()` / `setIid(String)` | 读取/设置所属连接 id | 纯访问器 |
| `getId()` / `setId(String)` | 读取/设置数据 id | 纯访问器 |
| `clone(ShellProxyConfig)`（static） | 深拷贝 | `new` 后 `copy`；入参为 null 返回 null |
| `isHttpProxy()` / `isSocksProxy()` / `isSocks4Proxy()` / `isSocks5Proxy()` / `isNoneProxy()` / `isPasswordAuth()` | 代理类型判定（继承） | 基于 `protocol`/`authType` |

- 调用链：`ShellConnect.copy → ShellProxyConfig.clone()`；`ShellConnectStore → ShellProxyConfig`。

---

## ShellQuery

- 职责：SQL / 查询书签记录，关联连接与数据库。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| iid | `String` | 连接 id（指向 `ShellConnect`） |
| uid | `String` | 主键 |
| name | `String` | 名称 |
| content | `String` | 内容（查询语句） |
| dbIndex | `int` | db 索引 |
| dbName | `String` | 数据库名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getIid()` / `setIid(String)` | 读取/设置连接 id | 纯访问器 |
| `getUid()` / `setUid(String)` | 读取/设置主键 | 纯访问器 |
| `getName()` / `setName(String)` | 读取/设置名称 | 纯访问器 |
| `getContent()` / `setContent(String)` | 读取/设置内容 | 纯访问器 |
| `getDbIndex()` / `setDbIndex(int)` | 读取/设置 db 索引 | 纯访问器 |
| `getDbName()` / `setDbName(String)` | 读取/设置数据库名 | 纯访问器 |
| `isNew()` | 是否新查询 | `StringUtil.isBlank(uid)` |

- 调用链：`ShellQueryStore → ShellQuery`；`ShellQuery.isNew → uid` 判空。

---

## ShellSSLConfig

- 职责：SSL/TLS 双向认证配置（客户端密钥/证书与 CA 证书），供 redis 等协议使用。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| id | `String` | 数据 id（主键） |
| iid | `String` | 所属连接 id |
| clientKey | `String` | 客户端密钥 |
| clientPwd | `String` | 客户端密码 |
| clientCrt | `String` | 客户端证书 |
| caCrt | `String` | ca 证书 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getId()` / `setId(String)` | 读取/设置数据 id | 纯访问器 |
| `getIid()` / `setIid(String)` | 读取/设置所属连接 id | 纯访问器 |
| `getClientKey()` / `getClientPwd()` / `getClientCrt()` / `getCaCrt()` 及对应 setter | 访问器 | 纯读写 |
| `copy(ShellSSLConfig)` | 复制 | 复制 caCrt/clientCrt/clientKey/clientPwd（不含 id/iid） |
| `isInvalid()` | 是否无效 | `caCrt`/`clientCrt`/`clientKey` 任一为空白即无效 |
| `clone(ShellSSLConfig)`（static） | 深拷贝 | `new` 后 `copy`；入参 null 返回 null |

- 调用链：`ShellConnect.copy → ShellSSLConfig.clone()`；`ShellSSLConfigStore → ShellSSLConfig`。

---

## ShellSetting

- 职责：应用级设置实体（终端、连接树、redis、zookeeper、同步、扩展等），继承通用 `AppSetting`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| x11Path | `String` | x11 目录（`@Deprecated`） |
| hiddenLeftAfterConnected | `Boolean` | 连接后收起左侧 |
| termType | `String` | 终端类型 |
| termBeep | `Boolean` | 终端蜂鸣声 |
| termRefreshRate | `Integer` | 终端刷新率 |
| termCursorStyle | `int` | 终端光标样式 |
| termCursorBlinks | `Integer` | 终端光标闪烁（毫秒） |
| termMaxLineCount | `Integer` | 终端最大行数 |
| termCopyOnSelected | `Boolean` | 终端选中时复制 |
| termUseAntialiasing | `Boolean` | 终端使用抗锯齿 |
| termParseHyperlink | `Boolean` | 终端解析超链接 |
| termPasteByMiddle | `Boolean` | 终端鼠标中键粘贴（`@Deprecated`） |
| termBackgroundImage | `String` | 终端背景图片 |
| connectShowType | `Boolean` | 连接显示类型 |
| connectShowMoreInfo | `Boolean` | 连接显示更多信息 |
| keyLoadLimit | `Integer` | redis 键加载上限 |
| rowPageLimit | `Integer` | redis 行页码限制 |
| syncToken | `String` | 同步令牌 |
| syncId | `String` | 同步 id |
| syncTime | `Long` | 更新时间 |
| syncType | `String` | 同步类型（gitee/github） |
| syncKey | `Boolean` | 同步密钥 |
| syncGroup | `Boolean` | 同步分组 |
| syncSnippet | `Boolean` | 同步片段 |
| syncConnect | `Boolean` | 同步连接 |
| enableShortcutKey | `Boolean` | 启用快捷键 |
| loadMode | `Byte` | zk 节点加载模式（0/null 一级，1 全部，2 仅根） |
| viewport | `Byte` | zk 节点视图（0/null 名称，1 路径） |
| zkContentViewport | `Byte` | zk 内容视图（0/null list，1 tree） |
| nodeLoadLimit | `Integer` | zk 节点加载限制（0 无限制） |
| recordPageLimit | `Integer` | 数据库记录加载限制 |
| mongoRecordPageLimit | `Integer` | mongo 记录每页限制 |
| opacity / titleBarOpacity / theme / fgColor / bgColor / accentColor / fontSize / fontFamily / fontWeight / editorFontSize / editorFontFamily / editorFontWeight / terminalFontSize / terminalFontFamily / terminalFontWeight / locale / exitMode / rememberPageSize / rememberPageResize / rememberPageLocation / pageWidth / pageHeight / pageScreenX / pageScreenY / pageMaximized / pageLeftWidth | 见 `AppSetting` | 主题、字体、页面等通用设置（继承自 `AppSetting`） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `copy(Object)` | 复制设置 | 先 `super.copy(o)`；再复制自身字段（含 zk 的 loadMode/viewport/zkContentViewport/nodeLoadLimit） |
| `getKeyLoadLimit()` | redis 键加载上限 | 默认 1000 |
| `getRowPageLimit()` | redis 行页码限制 | 默认 100 |
| `getRecordPageLimit()` | 数据库记录加载限制 | 默认 100 |
| `getMongoRecordPageLimit()` | mongo 每页限制 | 空或 ≤0 时返回 100 |
| `nodeLoadLimit()` | zk 节点加载限制 | 空时返回 1000 |
| `isLoadAll()` / `isLoadFirst()` / `isLoadRoot()` | zk 加载模式判定 | 依据 `loadMode`（1 / null或0 / 2） |
| `isShowNodeName()` / `isShowNodePath()` | zk 节点显示判定 | 依据 `viewport` |
| `isZkContentListViewport()` / `isZkContentTreeViewport()` | zk 内容视图判定 | 依据 `zkContentViewport` |
| `isHiddenLeftAfterConnected()` / `isTermBeep()` / `isTermUseAntialiasing()` / `isTermParseHyperlink()` / `isConnectShowType()` / `isSyncKey()` / `isSyncGroup()` / `isSyncSnippet()` / `isSyncConnect()` / `isEnableShortcutKey()` / `isTermPasteByMiddle()` | 默认开启的开关 | null 视作开启 |
| `isTermCopyOnSelected()` / `isConnectShowMoreInfo()` | 开关读取 | 借助 `BooleanUtil.isTrue` |
| `getTermRefreshRate()` | 终端刷新率 | 空或 ≤0 返回 -1 |
| `getTermCursorBlinks()` / `getTermMaxLineCount()` | 终端参数 | 默认 500 / 5000 |
| `isGiteeType()` / `isGithubType()` | 同步类型判定 | 依据 `syncType` |
| `x11Path()` / `x11Binary()` / `x11WorkDir()` / `getX11Path()` / `setX11Path(String)` | x11 目录与可执行文件（`@Deprecated`） | 按 `OSUtil` 返回各平台默认路径/文件名 |
| `isTermBackgroundImageInvalid()` | 终端背景图是否失效 | http(s) 或本地文件存在视为有效，否则失效 |
| `getTermBackgroundImageUrl()` | 终端背景图 url | 本地路径经 `ResourceUtil.getLocalFileUrl` 转换 |
| `getLoadMode()` / `getViewport()` / `getZkContentViewportViewport()` / `setZkContentViewport(Byte)` 及同步相关 getter/setter | 访问器 | 纯读写 |

- 调用链：`ShellSettingStore.SETTING → ShellSetting`；`SettingController → settingStore.replace(setting)`；`AppSetting.copy ← ShellSetting.copy`。

---

## ShellSnippet

- 职责：命令片段（snippet）实体，保存可复用的命令内容。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| id | `String` | 数据 id（主键） |
| name | `String` | 名称 |
| content | `String` | 内容 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getId()` / `setId(String)` | 读取/设置数据 id | 纯访问器 |
| `getName()` / `setName(String)` | 读取/设置名称 | 纯访问器 |
| `getContent()` / `setContent(String)` | 读取/设置内容 | 纯访问器 |

- 调用链：`ShellSnippetStore → ShellSnippet`（片段树加载/保存）。

---

## ShellTerminalHistory

- 职责：终端命令行历史记录，继承通用 `TerminalHistory`，补充自身主键 `tid`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| tid | `String` | 数据 id（主键） |
| line | `String` | 命令行（继承自 `TerminalHistory`） |
| saveTime | `long` | 保存时间（继承自 `TerminalHistory`） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getTid()` / `setTid(String)` | 读取/设置数据 id | 纯访问器 |
| `getLine()` / `setLine(String)` | 读取/设置命令行（继承） | 见 `TerminalHistory` |
| `getSaveTime()` / `setSaveTime(long)` | 读取/设置保存时间（继承） | 见 `TerminalHistory` |
| `compare(TerminalHistory)` / `equals(Object)` | 相等判定（继承） | 比较 `line` 与 `saveTime` |

- 调用链：`ShellTerminalHistoryStore → ShellTerminalHistory`；`TerminalHistory.equals ← ShellTerminalHistory`。

---

## ShellTunnelingConfig

- 职责：SSH 隧道（端口转发）配置，继承通用 `SSHTunneling`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| id | `String` | id（主键） |
| iid | `String` | 所属连接 id（指向 `ShellConnect`） |
| enabled | `Boolean` | 是否启用 |
| name / type / localPort / localHost / remotePort / remoteHost | 见 `SSHTunneling` | 隧道名称/类型/本地与远程地址端口（继承自 `SSHTunneling`） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getId()` / `setId(String)` | 读取/设置 id | 纯访问器 |
| `getIid()` / `setIid(String)` | 读取/设置所属连接 id | 纯访问器 |
| `isEnabled()` | 是否启用 | `enabled == null \|\| enabled` |
| `setEnabled(boolean)` | 设置启用状态 | 纯访问器 |
| `getEnabledStatus()` | 构造启用状态开关控件 | 新建 `FXToggleSwitch`，`selectedChanged` 回写 `setEnabled` |
| `getTypeName()` | 隧道类型名称（本地/远程/动态） | 依据 `isLocalType()`/`isRemoteType()` 调用 `I18nHelper.local()/remote()/dynamic()` |
| `copy(SSHTunneling)` | 复制 | 先 `super.copy(t1)`，再复制 `enabled` |
| `clone(List<ShellTunnelingConfig>)`（static） | 深拷贝隧道列表 | 逐个 `new` 并 `copy`；空集合返回 `Collections.emptyList()` |

- 调用链：`ShellConnect.getEnableTunnelingConfigs → ShellTunnelingConfig.isEnabled()`；`ShellConnect.copy → ShellTunnelingConfig.clone()`；`ShellTunnelingConfigStore → ShellTunnelingConfig`。

---

## ShellX11Config

- 职责：X11 转发配置（地址、端口、cookie 与屏幕号换算）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| port | `int` | 端口 |
| id | `String` | 数据 id（主键） |
| iid | `String` | 连接 id（指向 `ShellConnect`） |
| host | `String` | 地址 |
| cookie | `String` | cookie |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isLocal()` | 是否本地转发 | `host` 等于 `localhost`/`127.0.0.1`（忽略大小写） |
| `screen()` | 屏幕编号 | `port - 6000` |
| `getPort()` / `setPort(int)` | 读取/设置端口 | 纯访问器 |
| `getId()` / `setId(String)` | 读取/设置数据 id | 纯访问器 |
| `getIid()` / `setIid(String)` | 读取/设置连接 id | 纯访问器 |
| `getHost()` / `setHost(String)` | 读取/设置地址 | 纯访问器 |
| `getCookie()` / `setCookie(String)` | 读取/设置 cookie | 纯访问器 |
| `copy(ShellX11Config)` | 复制 | 复制 port/host/cookie（不含 id/iid） |
| `clone(ShellX11Config)`（static） | 深拷贝 | `new` 后 `copy`；入参 null 返回 null |

- 调用链：`ShellConnect.copy → ShellX11Config.clone()`；`ShellSSHClient.reqX11Forwarding → x11Config.screen()`；`ShellX11ConfigStore → ShellX11Config`。

---

## zk/ShellZKAuth

- 职责：Zookeeper 认证信息（用户名/密码/启用状态），并生成 digest 摘要。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| uid | `String` | 数据 id（主键） |
| iid | `String` | 连接 id（指向 `ShellConnect`） |
| user | `String` | 用户名 |
| password | `String` | 密码 |
| enable | `Boolean` | 是否启用 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKAuth()` / `ShellZKAuth(String, String, String)` | 构造 | 后者设置 iid/user/password |
| `digest()` | 生成摘要 | 用户或密码为空返回 ""，否则 `ShellZKAuthUtil.digest(user, password)` |
| `compare(ShellZKAuth)` | 相等判定 | 同一对象或 user、password 均相等 |
| `copy(ShellZKAuth)` | 复制 | 复制 iid/user/enable/password（不含 uid） |
| `isEnable()` | 是否启用 | `enable == null \|\| enable` |
| `setEnable(Boolean)` | 设置启用状态 | 纯访问器 |
| `getStatusControl()` | 构造状态开关控件 | `EnabledToggleSwitch`，`selectedChanged` 回写 `setEnable` 并 `ShellZKAuthStore.INSTANCE.replace(this)` |
| `clone(List<ShellZKAuth>)`（static） | 深拷贝列表 | 逐个 `new` 并 `copy`；空集合返回 `Collections.emptyList()` |
| `getUid()` / `getIid()` / `getUser()` / `getPassword()` 及对应 setter | 访问器 | 纯读写 |

- 调用链：`ShellZKAuth.getStatusControl → ShellZKAuthStore.INSTANCE.replace → ShellZKAuth`；`ShellZKAuthUtil.toAuthInfo → ShellZKAuth.getUser()/getPassword()`；`ShellZKAuth.digest → ShellZKAuthUtil.digest`。

---

## zk/ShellZKCollect

- 职责：Zookeeper 节点路径收藏记录。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| iid | `String` | 连接 id（指向 `ShellConnect`） |
| path | `String` | 路径 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKCollect()` / `ShellZKCollect(String, String)` | 构造 | 后者设置 iid/path |
| `getIid()` / `setIid(String)` | 读取/设置连接 id | 纯访问器 |
| `getPath()` / `setPath(String)` | 读取/设置路径 | 纯访问器 |
| `copy(ShellZKCollect)` | 复制 | 仅复制 `path` |
| `clone(List<ShellZKCollect>)`（static） | 深拷贝列表 | 逐个 `new` 并 `copy`；空集合返回 `Collections.emptyList()` |

- 调用链：`ShellZKCollectStore → ShellZKCollect`。

---

## zk/ShellZKSASLConfig

- 职责：Zookeeper 连接的 SASL 配置（类型/用户名/密码）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| id | `String` | 数据 id（主键） |
| iid | `String` | zk 连接 id（指向 `ShellConnect`） |
| type | `String` | sasl 类型 |
| userName | `String` | 用户名 |
| password | `String` | 密码 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getId()` / `setId(String)` | 读取/设置数据 id | 纯访问器 |
| `getIid()` / `setIid(String)` | 读取/设置 zk 连接 id | 纯访问器 |
| `getType()` / `getUserName()` / `getPassword()` 及对应 setter | 访问器 | 纯读写 |
| `checkInvalid()` | 校验是否无效 | `iid==null` 无效；`type` 为 `Digest` 时用户名或密码为空则无效 |
| `copy(ShellZKSASLConfig)` | 复制 | 复制 type/userName/password（不含 id/iid） |
| `clone(ShellZKSASLConfig)`（static） | 深拷贝 | `new` 后 `copy`；入参 null 返回 null |

- 调用链：`ShellConnect.copy → ShellZKSASLConfig.clone()`；`ShellZKSASLConfigStore → ShellZKSASLConfig`。

---

## redis/ShellRedisCollect

- 职责：Redis 键收藏记录（连接 + db 索引 + key）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| uid | `String` | 数据 id（主键） |
| iid | `String` | 连接 id（指向 `ShellConnect`） |
| dbIndex | `int` | db 索引 |
| key | `String` | 键 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisCollect()` / `ShellRedisCollect(String, int, String)` | 构造 | 后者设置 iid/dbIndex/key |
| `getIid()` / `setIid(String)` | 读取/设置连接 id | 纯访问器 |
| `getDbIndex()` / `setDbIndex(int)` | 读取/设置 db 索引 | 纯访问器 |
| `getKey()` / `setKey(String)` | 读取/设置键 | 纯访问器 |
| `getUid()` / `setUid(String)` | 读取/设置数据 id | 纯访问器 |
| `copy(ShellRedisCollect)` | 复制 | 复制 key/iid/dbIndex（不含 uid） |
| `clone(List<ShellRedisCollect>)`（static） | 深拷贝列表 | 逐个 `new` 并 `copy`；空集合返回 `Collections.emptyList()` |

- 调用链：`RedisCollectStore.replace(new ShellRedisCollect(iid, dbIndex, key)) → ShellRedisCollect`。

---

## redis/ShellRedisKeyFilterHistory

- 职责：Redis 键过滤模式历史记录。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| uid | `String` | 数据 id（主键） |
| iid | `String` | 连接 id（指向 `ShellConnect`） |
| pattern | `String` | 模式（过滤表达式） |
| saveTime | `long` | 保存时间（默认 `System.currentTimeMillis()`） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `compare(ShellRedisKeyFilterHistory)` | 相等判定 | 同一对象或 `pattern` 相等 |
| `getUid()` / `setUid(String)` | 读取/设置数据 id | 纯访问器 |
| `getIid()` / `setIid(String)` | 读取/设置连接 id | 纯访问器 |
| `getPattern()` / `setPattern(String)` | 读取/设置模式 | 纯访问器 |
| `getSaveTime()` / `setSaveTime(long)` | 读取/设置保存时间 | 纯访问器 |

- 调用链：`RedisKeyFilterHistoryStore → ShellRedisKeyFilterHistory`。
