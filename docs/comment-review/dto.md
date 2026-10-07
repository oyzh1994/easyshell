# easyshell 数据传输对象（dto 包）代码审查文档

> 范围：easyshell/src/main/java/cn/oyzh/easyshell/dto/（含 zk/redis/mongo 子包），共 18 个 .java，全部存活。

> 说明：仅新增文档，未改动任何 `.java`。

本包集中存放跨模块复用的数据传输对象：导出载体、扫描结果、以及 zk / redis / mongo 三个连接器的连接信息与运行信息模型。多数类为纯 POJO（字段 + getter/setter），少数类携带解析或格式化逻辑（如 `ShellZKServerInfo.update`、`ShellRedisInfoProp.parse`、`ShellRedisDBInfo.parse`）。

---

## ShellDataExport

- 职责：shell 连接的导出/导入载体，聚合版本、平台与密钥、分组、片段、连接四类数据集合。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| version | `String` | 导出程序版本号 |
| platform | `String` | 平台（取 `System.getProperty("os.name")`） |
| keys | `List<ShellKey>` | 密钥列表 |
| groups | `List<ShellGroup>` | 分组列表 |
| snippets | `List<ShellSnippet>` | 片段列表 |
| connects | `List<ShellConnect>` | 连接列表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static ShellDataExport fromConnects(List<ShellConnect> shellConnects)` | 由连接列表生成导出对象 | `Project.load()` 取版本；设置 `connects`；`System.getProperty("os.name")` 取平台 |
| `static ShellDataExport of()` | 生成空白导出对象 | 同 `fromConnects`，但不设置 `connects` |
| `static ShellDataExport fromJSON(String json)` | 从 json 字符串反序列化 | `JSONUtil.parseObject` 解析；按 `keys`/`groups`/`connects`/`snippets` 键分别 `JSONUtil.toList(..., Xxx.class)` |
| `String toJSONString()` | 序列化为 json 字符串 | `JSONUtil.toJson(this)` |
| `getVersion/setVersion`、`getPlatform/setPlatform`、`getKeys/setKeys`、`getSnippets/setSnippets`、`getGroups/setGroups`、`getConnects/setConnects` | 属性读写 | 直接读写字段 |

- 调用链：`ShellDataExport.fromJSON → JSONUtil.parseObject → JSONUtil.toList(ShellKey/ShellGroup/ShellConnect/ShellSnippet)`；`ShellDataExport.toJSONString → JSONUtil.toJson`；`ShellDataExport.of / fromConnects → Project.load()`

---

## ShellNetworkScanResult

- 职责：网络（主机）扫描结果模型，记录目标地址上各端口协议是否可用，并提供界面状态图标。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| host | `String` | 地址 |
| rdpAvailable | `boolean` | rdp 是否可用 |
| vncAvailable | `boolean` | vnc 是否可用 |
| ftpAvailable | `boolean` | ftp 是否可用 |
| sshAvailable | `boolean` | ssh 是否可用 |
| telnetAvailable | `boolean` | telnet 是否可用 |
| rtspAvailable | `boolean` | rtsp 是否可用 |
| rloginAvailable | `boolean` | rlogin 是否可用 |
| httpAvailable | `boolean` | http 是否可用 |
| httpsAvailable | `boolean` | https 是否可用 |
| mysqlAvailable | `boolean` | mysql 是否可用 |
| redisAvailable | `boolean` | redis 是否可用 |
| zookeeperAvailable | `boolean` | zookeeper 是否可用 |
| oracleAvailable | `boolean` | oracle 是否可用 |
| mongoDBAvailable | `boolean` | mongoDB 是否可用 |
| postgreSQLAvailable | `boolean` | postgreSQL 是否可用 |
| memcachedAvailable | `boolean` | Memcached 是否可用 |
| elasticsearchAvailable | `boolean` | Elasticsearch 是否可用 |
| sqlServerAvailable | `boolean` | SQL Server 是否可用 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getHost/setHost` | 地址读写 | 直接读写字段 |
| `isXxxAvailable/setXxxAvailable` | 各协议可用性读写（rdp/vnc/ftp/ssh/telnet/rtsp/rlogin/http/https/mysql/redis/zookeeper/oracle/mongoDB/postgreSQL/memcached/elasticsearch/sqlServer） | 直接读写对应布尔字段 |
| `getSshStatus()`、`getRdpStatus()`、`getVncStatus()`、`getFtpStatus()`、`getHttpStatus()`、`getTelnetStatus()`、`getHttpsStatus()`、`getRloginStatus()`、`getMysqlStatus()`、`getRedisStatus()`、`getZookeeperStatus()`、`getOracleStatus()`、`getMongoDBStatus()`、`getPostgreSQLStatus()`、`getSqlServerStatus()`、`getRtspStatus()`、`getMemcachedStatus()`、`getElasticsearchStatus()` | 返回对应协议的状态图标 | 均转调 `createSVG(对应 isXxxAvailable())` |
| `private SVGGlyph createSVG(boolean success)` | 根据可用性生成图标 | 可用返回 `SubmitSVGGlyph`（`Color.GREEN`），不可用返回 `CancelSVGGlyph`（`Color.RED`），光标均为 `Cursor.DEFAULT` |

- 调用链：`ShellNetworkScanResult.getSshStatus → createSVG → new SubmitSVGGlyph()/new CancelSVGGlyph()`

---

## ShellPortScanResult

- 职责：端口扫描结果模型，记录单个端口及其描述。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| port | `int` | 端口 |
| desc | `String` | 描述 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getPort/setPort` | 端口读写 | 直接读写字段 |
| `getDesc/setDesc` | 描述读写 | 直接读写字段 |

- 调用链：`ShellPortScanResult.getPort/setPort → port`（纯 POJO，无外部调用）

---

## ShellZKACL

- 职责：ZooKeeper 访问控制项，继承 `org.apache.zookeeper.data.ACL`，补充权限/标识解析与友好对象、常用判定方法。

- 字段：无自有字段（继承 `ACL` 的 `id`（`Id`）、`perms`（`int`）等）。

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKACL()` | 无参构造 | 空实现 |
| `ShellZKACL(ACL acl)` | 由原始 ACL 拷贝构造 | `setId(acl.getId())`、`setPerms(acl.getPerms())` |
| `setPerms(String perms)` | 以权限字符串设置权限 | `super.setPerms(ShellZKACLUtil.toPermInt(perms))`（重载父类 `setPerms(int)`） |
| `FriendlyInfo<ACL> idFriend()` | id 友好对象 | `ShellZKACLUtil.parseId(this.getId())` |
| `FriendlyInfo<ACL> permsFriend()` | 权限友好对象 | `ShellZKACLUtil.parsePerms(this.getPerms())` |
| `FriendlyInfo<ACL> schemeFriend()` | 协议友好对象 | `ShellZKACLUtil.parseScheme(this.schemeVal())` |
| `String idVal()` | id 值 | `this.getId().getId()` |
| `String schemeVal()` | 协议值 | `this.getId().getScheme()` |
| `boolean hasPerm(String perm)` | 是否含某权限字符 | `ShellZKACLUtil.toPermStr(this.getPerms())` 结果 `contains(perm)` |
| `hasReadPerm()` / `hasWritePerm()` / `hasDeletePerm()` / `hasCreatePerm()` / `hasAdminPerm()` | 读/写/删/创建子节点/特殊权限判定 | 分别转调 `hasPerm("r"/"w"/"d"/"c"/"a")` |
| `isIPACL()` / `isDigestACL()` / `isWorldACL()` | 协议类型判定 | `schemeVal()` 与 `"ip"/"digest"/"world"` 忽略大小写比较 |
| `boolean isReadOnly()` | 是否只读权限 | `idVal().endsWith(":x")` |
| `String digestUser()` | 摘要用户名 | 仅 `isDigestACL()` 时取 `idVal().split(":")[0]`，否则 `""` |

- 调用链：`ShellZKACL.hasReadPerm → hasPerm → ShellZKACLUtil.toPermStr`；`ShellZKACL.permsFriend → ShellZKACLUtil.parsePerms`；`ShellZKACL.schemeFriend → schemeVal → getId().getScheme()`

---

## ShellZKClusterNode

- 职责：ZooKeeper 集群节点模型，支持从 `QuorumServer` 或服务器配置文本两种来源构造。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| id | `Long` | 节点 id |
| type | `String` | 类型（选举节点/观察节点，随语言环境本地化） |
| addr | `String` | 交互地址 |
| weight | `Long` | 权重 |
| clientAddr | `String` | 客户端连接地址 |
| electionAddr | `String` | 服务端选举地址 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKClusterNode(QuorumPeer.QuorumServer server)` | 由集群服务器对象构造 | 取 `server.id`；按 `I18nManager.currentLocale()` 与 `server.type`（`PARTICIPANT`/其他）确定 `type`；由 `server.addr/clientAddr/electionAddr` 的 `toString()` 填充地址 |
| `ShellZKClusterNode(String serverTxt)` | 由服务器配置文本构造 | 按 `:` 切分提取 serverName/交互/选举地址；`weight=1L`；`id` 取 `[7, indexOf("="))` 子串；`clientAddr` 取 `";"` 之后；按文本是否含 `participant` 决定 `type` |
| `getId/setId`、`getType/setType`、`getAddr/setAddr`、`getWeight/setWeight`、`getClientAddr/setClientAddr`、`getElectionAddr/setElectionAddr` | 属性读写 | 直接读写字段 |

- 调用链：`ShellZKClusterNode(QuorumServer) → I18nManager.currentLocale()`；`ShellZKClusterNode(String) → String.split(":") / substring / Long.parseLong`

---

## ShellZKConnectInfo

- 职责：ZooKeeper 连接信息模型（地址/端口/超时/只读），字段带默认值。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| input | `String` | 原始输入内容 |
| host | `String` | 地址（默认 `"localhost"`） |
| port | `int` | 端口（默认 `2181`） |
| timeout | `int` | 超时时间，单位毫秒（默认 `5000`） |
| readonly | `boolean` | 只读模式 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getInput/setInput`、`getHost/setHost`、`getPort/setPort`、`getTimeout/setTimeout`、`isReadonly/setReadonly` | 属性读写 | 直接读写字段 |

- 调用链：`ShellZKConnectInfo.getHost/setHost → host`（纯 POJO，无外部调用）

---

## ShellZKEnvNode

- 职责：ZooKeeper 环境（`srvr`/`stat` 等）信息的单个键值对模型。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| name | `String` | 名称 |
| value | `String` | 值 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellZKEnvNode(String name, String value)` | 构造函数 | 赋值 `name`、`value` |
| `getName/setName`、`getValue/setValue` | 属性读写 | 直接读写字段 |

- 调用链：`ShellZKServerInfo.update → ShellZKEnvNode.getName/getValue`（被 `ShellZKServerInfo` 消费）

---

## ShellZKHistoryData

- 职责：ZooKeeper 数据历史记录模型，含内容、数据大小与保存时间，并提供可读的大小/时间格式化。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| data | `byte[]` | 内容（`@Column` 持久化列） |
| dataLength | `long` | 数据大小（`@Column`） |
| saveTime | `long` | 保存时间（`@Column`，默认 `System.currentTimeMillis()`） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String getDataSize()` | 格式化数据大小 | 按 1024 进制返回 `b/Kb/Mb/Gb`；`@JSONField(serialize=false, deserialize=false)` 不参与序列化 |
| `String getSaveTimeFormated()` | 格式化保存时间 | `DateHelper.DATE_TIME_SIMPLE_FORMAT.format(getSaveTime())`；同样排除序列化 |
| `getData/setData` | 内容读写 | `setData` 同时按 `data` 是否为空把 `dataLength` 置为 `data.length` 或 `0` |
| `getDataLength/setDataLength`、`getSaveTime/setSaveTime` | 大小/时间读写 | 直接读写字段 |

- 调用链：`ShellZKHistoryData.setData → dataLength 更新`；`getSaveTimeFormated → DateHelper.DATE_TIME_SIMPLE_FORMAT.format`

---

## ShellZKServerInfo

- 职责：ZooKeeper 服务运行信息模型，基于 JavaFX `SimpleStringProperty` 承载各展示字段，并从环境节点列表解析出命令、连接、节点数、延迟等。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| zxidProperty | `SimpleStringProperty` | 服务 id |
| modeProperty | `SimpleStringProperty` | 服务类型（角色） |
| versionProperty | `SimpleStringProperty` | 服务版本 |
| latencyInfoProperty | `SimpleStringProperty` | 延迟信息（最小/平均/最大） |
| nodeCountProperty | `SimpleStringProperty` | 节点数量 |
| connectionsProperty | `SimpleStringProperty` | 已连接客户端 |
| commandInfoProperty | `SimpleStringProperty` | 命令信息（已接收/已发送/等待中） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void update(List<ShellZKEnvNode> envNodes)` | 从环境节点列表更新服务信息 | 遍历 `envNodes`，按名称匹配（`Zxid`/`Mode`/含 `version`/`Connections`/`Node count`/含 `Latency`/`Received`/`Sent`/`Outstanding`）分别写入；最后拼接 `received/sent/outstanding` 为 `commandInfo` 并 `setCommandInfo` |
| `int commandReceived()` | 已接收命令数 | 取 `getCommandInfo()` 首段 `Integer.parseInt`；空或 `N/A`、`N/N/N` 返回 0；异常返回 0 |
| `int commandSent()` | 已发送命令数 | 取第 2 段，判定同 `commandReceived` |
| `int commandOutstanding()` | 等待中命令数 | 取第 3 段，判定同 `commandReceived` |
| `int connections()` | 已连接客户端数 | 由 `getConnections()` 解析整数，空/`N/A` 返回 0，异常返回 0 |
| `int nodeCount()` | 节点数量 | 由 `getNodeCount()` 解析整数，判定同上 |
| `double latencyMin()` / `latencyAvg()` / `latencyMax()` | 最小/平均/最大延迟 | 由 `getLatencyInfo()` 按 `/` 切分取第 1/2/3 段 `Double.parseDouble`，空/`N/A` 返回 0，异常返回 0 |
| `commandInfoProperty()`、`connectionsProperty()`、`nodeCountProperty()`、`latencyInfoProperty()`、`zxidProperty()`、`modeProperty()`、`versionProperty()` | 获取/懒初始化对应属性 | 属性为 `null` 时 `new SimpleStringProperty()` |
| `setCommandInfo/setConnections/setNodeCount/setLatencyInfo/setZxid/setMode/setVersion` | 设置各字段 | 经对应 `xxxProperty().set(...)`；`setConnections/setNodeCount` 先 `trim()` 且入参非空；`setVersion` 先按 `-` 取首段 |
| `getCommandInfo/getConnections/getNodeCount/getLatencyInfo/getZxid/getMode/getVersion` | 读取各字段 | 属性为 `null` 时返回默认 `"N/A"` |

- 调用链：`ShellZKServerInfo.update → setZxid/setMode/setVersion/... → zxidProperty()/...`；`commandReceived → getCommandInfo → commandInfoProperty`；`connections → getConnections → connectionsProperty`

---

## ShellRedisClientItem

- 职责：Redis 客户端列表项模型，支持从 `CLIENT LIST` 单行文本解析。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| index | `int` | 编号 |
| addr | `String` | 地址 |
| flags | `String` | 标记 |
| db | `String` | 当前 db |
| age | `String` | 存活时间 |
| idle | `String` | 空闲时间 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static ShellRedisClientItem from(String l)` | 从客户端信息字符串解析 | 按空格切分，逐段以 `age/addr/db/flags/idle` 前缀匹配（忽略大小写），取 `=` 后的值 |
| `getIndex/setIndex`、`getAddr/setAddr`、`getFlags/setFlags`、`getDb/setDb`、`getAge/setAge`、`getIdle/setIdle` | 属性读写 | 直接读写字段 |

- 调用链：`ShellRedisClientItem.from → String.split(" ") → s.split("=")`

---

## ShellRedisConnectInfo

- 职责：Redis 连接信息模型（地址/端口/超时/用户/密码/db/只读），字段带默认值。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| input | `String` | 原始输入内容 |
| host | `String` | 地址（默认 `"127.0.0.1"`） |
| port | `int` | 端口（默认 `6379`） |
| timeout | `int` | 超时时间（默认 `3000`） |
| user | `String` | 用户 |
| password | `String` | 密码 |
| db | `int` | db 索引（默认 `0`） |
| readonly | `boolean` | 只读模式 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getInput/setInput`、`getHost/setHost`、`getPort/setPort`、`getTimeout/setTimeout`、`getUser/setUser`、`getPassword/setPassword`、`getDb/setDb`、`isReadonly/setReadonly` | 属性读写 | 直接读写字段（getter/setter 定义在字段声明之前，等价） |

- 调用链：`ShellRedisConnectInfo.getHost/setHost → host`（纯 POJO，无外部调用）

---

## ShellRedisDBInfo

- 职责：Redis 单个数据库信息模型，支持从 `INFO keyspace` 单行文本解析。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| keys | `int` | 键数量 |
| index | `int` | db 索引 |
| expires | `int` | 过期键数量 |
| avgTTL | `double` | 平均存活时间 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static ShellRedisDBInfo parse(String str)` | 解析数据库信息字符串 | 非空时先 `substring(2)` 去掉 `db` 前缀，取 `:` 前为 `index`，`:` 后按 `,` 切分；逐段以 `keys=`/`expires=`/`avg_ttl=` 前缀解析（注意 `avg_ttl` 用 `Integer.parseInt` 赋值给 `double`） |
| `getKeys/setKeys`、`getIndex/setIndex`、`getExpires/setExpires`、`getAvgTTL/setAvgTTL` | 属性读写 | 直接读写字段 |

- 调用链：`ShellRedisDBInfo.parse → StringUtil.isNotBlank → substring/split/Integer.parseInt`

---

## ShellRedisInfoProp

- 职责：Redis `INFO` 文本的解析与访问载体，按分组（`# Section`）组织为 `Map<String, JSONObject>`，并提供大量语义化取值方法。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| props | `Map<String, JSONObject>` | 属性列表，key 为分组名（小写），value 为该组键值对 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `Map<String, JSONObject> getProps()` | 获取全部属性 | 返回 `props` |
| `void parse(String str)` | 解析 INFO 文本 | 非空时按行处理：`#` 开头作为分组（`replace("# ", "")` 并转小写）并 `computeIfAbsent`；含 `:` 的行按首个 `:` 拆为名/值写入当前分组 |
| `JSONObject getProps(String group)` | 取某分组属性 | `group` 非空且 `props` 非空时按小写取 |
| `boolean hasProps(String group)` | 分组是否存在 | `props.containsKey(group.toLowerCase())` |
| `String getProp(String group, String propName)` | 取字符串属性 | 经 `getProps(group)` 后 `getString` |
| `int getIntProp(...)` | 取 int 属性 | 经 `getProps` 后 `getIntValue`，缺省返回 `-1` |
| `Integer getIntegerProp(...)` | 取 Integer 属性 | 经 `getProps` 后 `getIntValue`，缺省返回 `null` |
| `long getLongProp(...)` | 取 long 属性 | 经 `getProps` 后 `getLong`，缺省返回 `-1L` |
| `Double getDoubleProp(...)` | 取 Double 属性 | 经 `getProps` 后 `getDouble`，缺省返回 `-1d` |
| `String getRedisVersion()` / `getUsedMemoryHuman()` / `getReplicationRole()` / `getRedisMode()` | 语义化字符串取值 | 分别取 `server.redis_version`、`memory.used_memory_human`、`replication.role`、`server.redis_mode` |
| `long getUsedMemory()` / `getKeyspaceHits()` / `getKeyspaceMisses()` / `getUptimeInSeconds()` / `getTotalCommandsProcessed()` / `getTotalNetInputBytes()` / `getTotalNetOutputBytes()` / `getInstantaneousOpsPerSec()` | 语义化 long 取值 | 分别映射到 `memory.used_memory`、`stats.keyspace_hits`、`stats.keyspace_misses`、`server.uptime_in_seconds`、`stats.total_commands_processed`、`stats.total_net_input_bytes`、`stats.total_net_output_bytes`、`stats.instantaneous_ops_per_sec` |
| `int getConnectedClients()` / `getUptimeInDays()` | 语义化 int 取值 | `clients.connected_clients`、`server.uptime_in_days` |
| `Integer getClusterEnabled()` | cluster 开启状态 | `cluster.cluster_enabled` |
| `Double getInstantaneousInputKbps()` / `getInstantaneousOutputKbps()` | 每秒入/出网数据 | `stats.instantaneous_input_kbps`、`stats.instantaneous_output_kbps` |
| `Long keyCount()` | 键数量合计 | 取 `keyspace` 分组各值，按 `,` 切分并对 `keys=` 累加；无分组返回 `null` |
| `String masterName()` | 当前 master 名称 | 取 `sentinel.master0`，按 `,` 切分找以 `name` 开头的项并取 `=` 后值 |
| `Set<String> groups()` | 分组名称集合 | `props.keySet()` |
| `boolean isEmpty()` | 是否为空 | `props == null || props.isEmpty()` |

- 调用链：`ShellRedisInfoProp.parse → Stream.lines → props.computeIfAbsent`；`getRedisVersion/getUsedMemory/... → getProp/getLongProp/... → getProps(group) → props.get`；`keyCount → getProps("keyspace")`；`masterName → getProp("sentinel", "master0")`

---

## ShellRedisInfoPropItem

- 职责：Redis 信息属性列表项，用 JavaFX 属性承载名称与值，供表格展示。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| nameProperty | `SimpleStringProperty` | 名称 |
| valueProperty | `SimpleStringProperty` | 值 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisInfoPropItem(String name, String value)` | 构造函数 | 转调 `setName`、`setValue` |
| `nameProperty()` / `valueProperty()` | 获取/懒初始化属性 | 为 `null` 时 `new SimpleStringProperty()` |
| `setName(String)` / `getValue()` / `setValue(String)` / `getName()` | 名称/值读写 | 经对应 `xxxProperty().setValue/get`；属性为 `null` 时 get 返回 `null` |

- 调用链：`ShellRedisInfoPropItem 构造 → setName/setValue → nameProperty()/valueProperty()`

---

## ShellRedisPubsubItem

- 职责：Redis 订阅/发布（pubsub）项目模型，记录通道及其所属客户端。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| index | `int` | 编号 |
| channel | `String` | 通道 |
| client | `ShellRedisClient` | redis 客户端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getIndex/setIndex`、`getChannel/setChannel`、`getClient/setClient` | 属性读写 | 直接读写字段 |

- 调用链：`ShellRedisPubsubItem.getClient/setClient → ShellRedisClient`（纯 POJO，无外部调用）

---

## ShellRedisServerItem

- 职责：Redis 服务概览信息模型（版本/角色/运行时长/命中率/键数量/内存/客户端/命令数），由 `ShellRedisInfoProp` 填充。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| role | `String` | 服务角色（本地化文本） |
| serverVersion | `String` | 服务版本 |
| uptimeProperty | `SimpleStringProperty` | 正常运行时间 |
| hitRateProperty | `SimpleStringProperty` | 命中率 |
| keyCountProperty | `SimpleStringProperty` | 键数量 |
| usedMemoryProperty | `SimpleStringProperty` | 已使用内存 |
| connectedClientsProperty | `SimpleStringProperty` | 已连接客户端 |
| totalCommandsProcessedProperty | `SimpleStringProperty` | 已处理命令 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void init(ShellRedisInfoProp prop)` | 初始化服务信息 | 取 `prop` 的 `getUptimeInDays/getUsedMemoryHuman/getTotalCommandsProcessed/getKeyspaceHits/getKeyspaceMisses/keyCount/getConnectedClients` 后转调 `update(...)` |
| `void update(long uptime, String useMemory, long totalCommandsProcessed, long hits, long misses, Long keyCount, int connectedClients)` | 更新服务信息 | 计算命中率：`hits==0&&misses==0` 为 `N/A`，否则 `100.0*hits/(hits+misses)` 经 `NumberUtil.round(d,4)` 拼 `%`；`setUptime(uptime + I18nHelper.days())`；其余经 `null` 判定后写属性 |
| `uptimeProperty()`、`hitRateProperty()`、`usedMemoryProperty()`、`keyCountProperty()`、`totalCommandsProcessedProperty()`、`connectedClientsProperty()` | 获取/懒初始化属性 | 为 `null` 时 `new SimpleStringProperty()` |
| `setUptime/getUptime`、`setUsedMemory/getUsedMemory`、`setHitRate/getHitRate`、`setKeyCount/getKeyCount`、`setConnectedClients/getConnectedClients`、`setTotalCommandsProcessed/getTotalCommandsProcessed` | 各字段读写 | 经对应 `xxxProperty().setValue/get`；get 时属性为 `null` 返回 `"N/A"` |
| `setRole(String role)` / `getRole()` | 角色读写 | `setRole` 将 `master/slave/sentinel`（忽略大小写）分别映射为 `I18nHelper.master()/slave()/sentinel()`，其余置 `"N/A"` |
| `getServerVersion/setServerVersion` | 服务版本读写 | 直接读写字段 |

- 调用链：`ShellRedisServerItem.init → ShellRedisInfoProp.getXxx → update → setHitRate/setUptime/... → hitRateProperty()/...`；`setRole → I18nHelper.master/slave/sentinel`

---

## ShellRedisSlowlogItem

- 职责：Redis 慢查询日志项模型，支持从 Jedis `Slowlog` 对象转换。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| logId | `long` | 日志 id |
| command | `String` | 指令 |
| timeStamp | `String` | 发生时间 |
| clientHost | `String` | 客户端地址 |
| clientName | `String` | 客户端名称 |
| executionTime | `long` | 耗时 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static ShellRedisSlowlogItem from(Slowlog slowlog)` | 从慢查日志转换 | 取 `getId/getClientName/getExecutionTime/getClientIpPort`；`command` 由 `StringUtil.join(" ", slowlog.getArgs())`；`timeStamp` 由 `Const.DATE_FORMAT.format(getTimeStamp()*1000)` |
| `setClientHost(String)` / `setClientHost(HostAndPort)` | 设置客户端地址（重载） | `HostAndPort` 版在参数为 `null` 时置 `"未知"`，否则取 `toString()` |
| `getLogId/setLogId`、`getCommand/setCommand`、`getTimeStamp/setTimeStamp`、`getClientHost`、`getClientName/setClientName`、`getExecutionTime/setExecutionTime` | 属性读写 | 直接读写字段 |

- 调用链：`ShellRedisSlowlogItem.from → StringUtil.join / Const.DATE_FORMAT.format`；`from → setClientHost(String)`

---

## ShellMongoConnectInfo

- 职责：MongoDB 连接信息模型（地址/端口/超时/只读），字段带默认值。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| input | `String` | 原始输入内容 |
| host | `String` | 地址（默认 `"localhost"`） |
| port | `int` | 端口（默认 `2181`，与 zk 默认值一致，注意与 MongoDB 常用端口 27017 不符） |
| timeout | `int` | 超时时间，单位毫秒（默认 `5000`） |
| readonly | `boolean` | 只读模式 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getInput/setInput`、`getHost/setHost`、`getPort/setPort`、`getTimeout/setTimeout`、`isReadonly/setReadonly` | 属性读写 | 直接读写字段 |

- 调用链：`ShellMongoConnectInfo.getHost/setHost → host`（纯 POJO，无外部调用）

---

- 覆盖类数：**18**
