# easyshell Redis 模块（redis 包）代码审查文档

> 范围：`easyshell/src/main/java/cn/oyzh/easyshell/redis/`（含 key/batch 子包），共 25 个 `.java`，其中存活 23 个、注释死代码 2 个。
> 说明：仅新增文档，未改动任何 `.java`。

---

# 一、连接与客户端

## ShellRedisClient

- 职责：Redis 终端的核心客户端，实现 `ShellBaseClient`，封装 Jedis 连接池/集群连接、生命周期管理以及各类 Redis 命令的调用入口。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| poolManager | `ShellRedisPoolManager` | 连接池管理器（final，构造即创建） |
| commandObjects | `CommandObjects` | Redis 命令对象（cluster 模式下初始化） |
| role | `String` | 当前连接角色（master/slave/sentinel） |
| dbIndex | `int` | 当前 db 索引，默认 0 |
| databases | `Integer` | 数据库数量 |
| infoProp | `ShellRedisInfoProp` | 服务属性信息 |
| jumpForwarder | `SSHJumpForwarder2` | SSH 端口转发器 |
| shellConnect | `ShellConnect` | Redis 连接信息（final） |
| state | `SimpleObjectProperty<ShellConnState>` | 连接状态（final） |
| stateListener | `ChangeListener<ShellConnState>` | 状态监听器（final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisClient(ShellConnect shellConnect)` | 构造方法 | 保存 shellConnect，注册 stateListener |
| `stateProperty()` | 获取状态属性 | 返回 `ObjectProperty<ShellConnState>` |
| `start(int connectTimeout)` | 启动连接 | 委托 `startDatabase(0, connectTimeout)` |
| `startDatabase(int dbIndex)` | 启动并选中 db | 委托 `startDatabase(dbIndex, shellConnect.connectTimeOutMs())` |
| `startDatabase(int dbIndex, int connectTimeout)` | 启动连接主流程 | 状态置 CONNECTING → `initClient` → 非集群/哨兵则 `select` → 置 CONNECTED → `ShellClientChecker.push(this)`；异常置 FAILED 并抛 `ShellException` |
| `initHost()` | 初始化主机地址 | 跳板开启时 `jumpForwarder.forward` 返回 `127.0.0.1:localPort`，否则直连返回 `hostIp:hostPort` |
| `initClient(int connectTimeout)` | 初始化客户端 | `ShellRedisHelper.newConfig` 生成配置 → `initPool` → 读取 `role()` → 集群模式 `initCluster` |
| `initCluster(String host, int port, DefaultJedisClientConfig clientConfig)` | 初始化集群 | 建 `ConnectionPoolConfig`、`JedisCluster`，设置到 poolManager 并建 `CommandObjects` |
| `initPool(String host, int port, DefaultJedisClientConfig clientConfig)` | 初始化连接池 | 建 `JedisPoolConfig`；按需取 SSL/代理配置，构建 `ShellRedisSocketFactory` 与 `JedisPool`，调用 `poolManager.initResource/setConnectName` |
| `intPoolConfig(GenericObjectPoolConfig<?> poolConfig)` | 初始化池参数 | minIdle=3、maxIdle=16、maxTotal=50、测试开关全开、maxWait=10s |
| `close()` | 关闭客户端 | 关 jumpForwarder、`poolManager.destroy`、状态置 CLOSED、清空 role/databases/infoProp/commandObjects、移除监听器 |
| `closeQuiet()` | 静默关闭 | 直接调用 `close()` |
| `reset()` | 重置客户端 | 状态置 NOT_INITIALIZED |
| `isConnected()` | 是否已连接 | `getPool()` 非空且未关闭 |
| `isBroken()` | 连接是否失效 | `getResource()` 后 `jedis.isBroken()`，finally 归还 |
| `isClusterMode()` | 是否集群模式 | `Objects.equals(1, infoProp().getClusterEnabled())` |
| `isMasterMode()` | 是否 master 模式 | `!isClusterMode()` |
| `isSentinelMode()` | 是否哨兵模式 | role 忽略大小写等于 `sentinel` |
| `isStandaloneMode()` | 是否单机模式 | `infoProp().getRedisMode()` 忽略大小写等于 `standalone` |
| `isReadonly()` | 是否只读 | `shellConnect.isReadonly()` |
| `throwClusterException()` | 集群不支持操作时抛异常 | 集群模式抛 `ShellRedisClusterOperationException` |
| `throwSentinelException()` | 哨兵不支持操作时抛异常 | 哨兵模式抛 `ShellRedisSentinelOperationException` |
| `throwReadonlyException()` | 只读不支持操作时抛异常 | 只读抛 `ShellReadonlyOperationException` |
| `throwCommandException(String command)` | 指令不支持时抛异常 | `ShellRedisVersionUtil.checkSupported(getServerVersion(), command)` |
| `getCluster()` | 获取集群对象 | 委托 `poolManager.getCluster()` |
| `getClusterPools()` | 获取集群主节点连接 | 无缓存时 `getCluster().getClusterNodes()` → `poolManager.initClusterPool` |
| `getResource()` / `getResource(Integer dbIndex)` | 获取连接 | 委托 `poolManager.getResource` |
| `returnResource(Jedis jedis)` | 归还连接 | 委托 `poolManager.returnResource` |
| `getPool()` | 获取连接池 | 委托 `poolManager.getJedisPool()` |
| `ping()` | 连通性检测 | 集群走 `cluster.ping()`，否则 Jedis 连接 ping |
| `echo(String string)` | 回显 | `throwSentinelException` 后 Jedis echo |
| `query(ShellRedisQueryParam param)` | 执行终端查询 | `TerminalManager.findHandler` → `RedisTerminalUtil.getCommand` → `execCommand`，返回 `ShellRedisQueryResult` |
| `execCommand(Integer dbIndex, CommandObject<Object> commandObject)` | 执行命令对象 | 由终端解析后执行 |
| `forkClient()` | 派生客户端 | 匿名子类覆写 `isForked()=true` 并 `start()` |
| `getRole()` / `shellConnect()` / `getShellConnect()` | 获取角色/连接 | getter |
| `connectName()` / `iid()` | 连接名/id | 取自 `shellConnect` |
| `getServerVersion()` | 服务端版本 | `infoProp().getRedisVersion()` |
| `databases()` | 数据库数量 | 查询并缓存 databases |
| `infoProp()` / `clearInfoProp()` / `info(String section)` | 服务属性 | 获取/清空/查询 info |
| `getDB()` | 当前 db | 查询当前选中库 |
| `select(Integer dbIndex)` | 切换 db | 按需建连接并 select |
| `dbIndex(Jedis jedis, Integer dbIndex)` | 内部切换 db | 供各命令方法复用 |
| `scan(int dbIndex, String cursor, ScanParams params)` | 扫描键 | 返回 `ScanResult<String>` |
| `keys(Integer dbIndex, String pattern)` / `keys(ConnectionPool pool, String pattern)` / `keys(Integer, String, ShellRedisKeyType)` | 键查询 | 按模式/类型获取键 |
| `fullKeys(String pattern)` / `allKeys(Integer dbIndex, String pattern)` | 全量键 | 全库扫描键 |
| `type(Integer, String)` / `typeMulti(Integer, Collection<String>)` | 键类型 | `TYPE` / 批量类型 |
| `dbSize(Integer)` / `dbSize(ConnectionPool)` | 键数量 | `DBSIZE` |
| `del(Integer, String...)` / `del(Integer, Collection<String>)` | 删除键 | `DEL` |
| `rename(...)` / `move(...)` / `copy(...)` / `persist(...)` / `touch(...)` / `randomKey(...)` | 键操作 | 重命名/移动/复制/持久化/触碰/随机键 |
| `ttl(...)` / `pttl(...)` / `expire(...)` / `pexpire(...)` / `expireAt(...)` / `pexpireAt(...)` | 过期时间 | 秒/毫秒级 TTL 与过期设置 |
| `objectEncoding/objectFreq/objectIdletime/objectRefcount(...)` | OBJECT 子命令 | 对象编码、访问频率、空闲时间、引用计数 |
| `flushDB(Integer)` / `flushAll()` | 清库 | `FLUSHDB` / `FLUSHALL` |
| `set/get/setrange/getrange/setnx/setex/mset/msetnx/mget/append/strlen/getSet(...)` | string 命令族 | 单值读写、区间、批量、追加、长度 |
| `jsonSet/jsonGet(Integer dbIndex, String key, ...)` | JSON 命令族 | 走 `ShellRedisJsonUtil.jsonSet/jsonGet` |
| `decr/decrBy/incr/incrBy/incrByFloat(...)` | 计数命令族 | 自增自减 |
| `setbit/getbit/bitcount/bitpos(...)` | bitmap 命令族 | 位操作 |
| `hget/hset/hsetnx/hmset/hmget/hgetAll/hdel/hlen/hkeys/hvals/hexists/hincrBy/hincrByFloat/hstrlen/hrandfield/hrandfieldWithValues(...)` | hash 命令族 | 字段读写、批量、随机字段、自增、长度 |
| `lpush/rpush/lpushx/rpushx/lset/linsert/lpop/rpop/blpop/brpop/lrem/lrange/lindex/ltrim/llen(...)` | list 命令族 | 推入、弹出、区间、裁剪、插入、阻塞弹出 |
| `sadd/srem/spop/smembers/sismember/scard/srandmember/sdiff/sunion/sinter/sdiffstore/sunionstore/sinterstore(...)` | set 命令族 | 成员增删、集合运算（含 store 变体） |
| `zadd/zincrby/zcard/zcount/zdiff/zdiffWithScores/zdiffStore/zscore/zmscore/zmscore_ext/zrandmember/zrange/zrem/zrank/zrevrank(...)` | zset 命令族 | 成员分数、区间、排名、差集；`zmscore_ext` 兼容老版本 |
| `pfadd/pfcount/pfmerge(...)` | hyperloglog 命令族 | 基数统计 |
| `geoadd/geohash/geodist/geopos(...)` | geo 命令族 | 地理坐标 |
| `xadd/xdel/xinfoStream/xinfoStreamFull/xrange(...)` | stream 命令族 | 流条目增删、信息、区间 |
| `configGet/configSet/configRewrite/configResetStat(...)` | CONFIG 命令族 | 配置读取/设置 |
| `publish/pubsubNumSub/pubsubNumPat/pubsubChannels/subscribe/psubscribe(...)` | pubsub 命令族 | 发布订阅 |
| `save/bgsave/bgrewriteaof/lastsave(...)` | 持久化命令 | 保存/后台重写 |
| `clientList/clientGetname/clientSetname(...)` | CLIENT 命令族 | 客户端信息 |
| `slowlogLen/slowlogReset/slowlogGet(...)` | SLOWLOG 命令族 | 慢日志 |
| `memoryUsage(Integer, String)` / `waitReplicas(int, long)` / `waitAOF(long, int, long)` | 其他服务命令 | 内存占用、复制等待 |
| `commandCount()` / `commandInfo(String...)` / `role()` / `role(ConnectionPool)` | 服务端信息 | 命令数、命令信息、角色 |

- 调用链：`start → startDatabase → initClient → initHost / ShellRedisHelper.newConfig → initPool / initCluster → ShellRedisSocketFactory；命令方法 → getResource(dbIndex) → dbIndex(jedis,dbIndex) → jedis.xxx → returnResource(jedis)`，连接资源统一由 `ShellRedisPoolManager` 管理。

## ShellRedisConn

- 职责：封装一条 Jedis 连接的持有状态（是否使用中），供连接池复用。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| jedis | `Jedis` | jedis 客户端 |
| using | `boolean` | 是否使用中 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisConn(Jedis jedis)` | 构造方法 | 仅设置 jedis |
| `ShellRedisConn(Jedis jedis, boolean using)` | 构造方法 | 设置 jedis 与 using |
| `getDB()` | 获取数据库索引 | 委托 `jedis.getDB()` |
| `getJedis()` / `setJedis(Jedis)` | 读写 jedis | getter/setter |
| `isUsing()` / `setUsing(boolean)` | 读写使用标志 | getter/setter |

- 调用链：`ShellRedisPoolManager.getResource → ShellRedisConn.isUsing/getDB → getJedis`。

## ShellRedisConnectUtil

- 职责：Redis 连接工具类，解析命令行连接串并复制连接到 `ShellConnect`。

- 字段：无字段。

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `parse(String input)` | 解析连接串 | 按空格切分，识别 `-h/-p/-a/-n/-r/-u/-timeout`，生成 `ShellRedisConnectInfo`；异常返回 null |
| `copyConnect(ShellRedisConnectInfo connectInfo, ShellConnect redisConnect)` | 复制连接信息 | 将 user/readonly/password/timeout 及 `host:port` 写回 `ShellConnect` |

- 调用链：`parse → ShellRedisConnectInfo`；`copyConnect → ShellConnect.setHost/setUser/...`。

## ShellRedisHelper

- 职责：Redis 客户端配置与 SSL 上下文构建工具类。

- 字段：无字段。

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `newConfig(ShellConnect connect, int connectTimeout)` | 生成 Jedis 客户端配置 | 通过 `DefaultJedisClientConfig.builder()` 设置 socket/连接超时、user、password；SSL 模式校验 `ShellSSLConfig` 并注入 `buildSSLContext` 产生的 socket 工厂 |
| `buildSSLContext(ShellSSLConfig sslConfig)` | 构建 SSLContext | 读取 CA/客户端证书与 PKCS#8 私钥，装配 TrustStore/KeyStore + `KeyManagerFactory`，`SSLContext.getInstance("TLS").init(...)` |

- 调用链：`ShellRedisClient.initClient → ShellRedisHelper.newConfig → buildSSLContext → ShellRedisSocketFactory`。

## ShellRedisPoolManager

- 职责：Redis 连接池管理器，维护 JedisPool/JedisCluster、连接资源缓存与集群节点连接。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| connectName | `String` | 连接名称 |
| jedisPool | `JedisPool` | 连接池 |
| cluster | `JedisCluster` | redis 集群操作对象 |
| maxPoolSize | `byte` | 最大池上限，默认 16 |
| initPoolSize | `byte` | 初始池大小，默认 3 |
| resources | `List<ShellRedisConn>` | 资源集合（CopyOnWriteArrayList，final） |
| clusterPools | `List<ConnectionPool>` | cluster 集群的主节点连接 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `initResource()` | 初始化资源 | 从 jedisPool 取 `initPoolSize` 个连接，逐个 `select(i)` 并放入 resources |
| `initClusterPool(Map<String, ConnectionPool> poolMap)` | 初始化集群连接 | 缓存集群节点连接集合 |
| `hasClusterPool()` | 是否有集群连接 | `clusterPools` 非空 |
| `getResource(Integer dbIndex)` | 获取连接 | 打乱空闲连接列表，dbIndex 为空取任意空闲、否则匹配同 db 连接；无则新建并标记 using，finally 写入 ThreadLocal 连接名 |
| `returnResource(Jedis jedis)` | 归还连接 | 找到对应 `ShellRedisConn` 标记未使用；若超出 maxPoolSize 则移除并异步 `doReturnResource` |
| `doReturnResource(Jedis jedis)` | 执行归还 | 调用 `jedisPool.returnResource` |
| `destroy()` | 销毁 | 归还全部资源、关闭并置空 jedisPool/cluster/clusterPools |
| `getConnectName/setConnectName`、`getJedisPool/setJedisPool`、`getCluster/setCluster`、`getMaxPoolSize/setMaxPoolSize`、`getInitPoolSize/setInitPoolSize`、`getClusterPools/setClusterPools` | 属性读写 | getter/setter |

- 调用链：`ShellRedisClient.getResource → ShellRedisPoolManager.getResource → ShellRedisConn`；`ShellRedisClient.close → ShellRedisPoolManager.destroy`。

## ShellRedisSocketFactory

- 职责：实现 `JedisSocketFactory`，创建支持代理与 SSL 的 socket。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| port | `int` | 端口（final） |
| host | `String` | 主机（final） |
| proxy | `Proxy` | 代理（final） |
| socketTimeout | `int` | socket 超时时间（final） |
| proxyConfig | `ShellProxyConfig` | 代理配置（final） |
| sslSocketFactory | `SSLSocketFactory` | ssl socket 工厂（final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisSocketFactory(SSLSocketFactory, String, int, ShellProxyConfig, int)` | 构造方法 | 保存参数，`ShellProxyUtil.initProxy1` 初始化代理 |
| `createSocket()` | 创建 socket | 需要代理则 `ShellProxyUtil.createSocket`，否则直连 `socket.connect`；若配置了 sslSocketFactory 则包装为 `SSLSocket` 并 `startHandshake`；IOException 包为 RuntimeException |

- 调用链：`JedisPool → ShellRedisSocketFactory.createSocket → ShellProxyUtil.createSocket / SSLSocketFactory.createSocket`。

---

# 二、工具与枚举

## ShellRedisJsonUtil

- 职责：RedisJSON（JSON.SET/JSON.GET）命令的底层封装工具类。

- 字段：无字段。

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `jsonSet(Jedis jedis, String key, String path, String json)` | 设置 JSON 数据 | `jedis.sendCommand(JsonProtocol.JsonCommand.SET, ...)` 并 `toString()` |
| `jsonGet(Jedis jedis, String key, String path)` | 获取 JSON 数据 | `jedis.sendCommand(JsonProtocol.JsonCommand.GET, ...)` |

- 调用链：`ShellRedisClient.jsonSet/jsonGet → ShellRedisJsonUtil → Jedis.sendCommand`。

## ShellRedisKeyType

- 职责：Redis 键类型枚举，含类型描述与字符串映射。

- 字段：无字段（枚举常量：`STRING`、`SET`、`ZSET`、`LIST`、`HASH`、`STREAM`、`JSON`）。

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `desc()` | 获取类型描述 | `switch` 返回 `I18nHelper` 对应国际化文案 |
| `ShellRedisKeyType()` | 构造方法 | 空实现 |
| `valueOfType(String type)` | 字符串转枚举 | 小写匹配，如 `string/bitmap/hyperloglog/hylog→STRING`、`zset/geo→ZSET`、`rejson-rl/json→JSON`；未知返回 null |
| `equalsString(String type)` | 是否匹配字符串类型 | `valueOfType(type) != null` |
| `length()` | 枚举长度 | `values().length` |

- 调用链：`ShellRedisKeyUtil.keyType/valueOfType → ShellRedisKeyType`。

## ShellRedisKeyUtil

- 职责：Redis 键工具类，负责键的序列化/反序列化、扫描、统计、删除、批量获取与值加载。

- 字段：无字段（全静态方法）。

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `serializeNode(ShellRedisKey redisKey)` | 序列化键 | 按类型产出文本/JSON：string（raw 编码 `0x'...'`）、json、list/set/zset/hash/stream（`JSONUtil.toJson`） |
| `deserializeNode(ShellRedisKeyType type, String value)` | 反序列化键 | 按类型解析文本/JSONArray，调用 `valueOfString/valueOfJson/valueOfList/valueOfSet/valueOfZSet/valueOfHash/valueOfStream` |
| `createKey(ShellRedisKey redisKey, Integer dbIndex, ShellRedisClient client)` | 创建键 | 按类型调用 `client.set/jsonSet/lpush/sadd/zadd/hmset/xadd` |
| `keyValue(ShellRedisKey redisKey, Integer dbIndex, String key, ShellRedisClient client)` | 加载键值 | 按类型调用 `client.get/jsonGet/lrange/hgetAll/smembers/zrange+zmscore_ext/xrange` |
| `keyObject(ShellRedisKey redisKey, Integer dbIndex, String key, ShellRedisClient client)` | 加载对象信息 | `objectRefcount/objectIdletime/objectEncoding` 写回键 |
| `scanKeys(Integer dbIndex, String cursor, ScanParams params, ShellRedisClient client)` | 扫描键 | `client.scan` → 批量 `keyType` → `initKey`，设置 loadTime，返回 `ShellRedisScanResult` |
| `scanKeysSimple(Integer, String, ScanParams, ShellRedisClient)` | 简单扫描 | 仅返回键名列表 `ShellRedisScanSimpleResult` |
| `scanKeys(Integer dbIndex, ShellRedisClient client, String pattern, int limit)` | 限量扫描 | 构造 `ScanParams`（count/match）→ `scanKeysSimple` |
| `countKeys(Integer, String, ScanParams, ShellRedisClient)` | 统计键 | 扫描后返回 `ShellRedisCountResult` |
| `deleteKeys(Integer, String, ScanParams, ShellRedisClient)` | 删除键 | 扫描后 `client.del`，返回 `ShellRedisDeleteResult` |
| `allKeys(Integer dbIndex, String pattern, ShellRedisClient client)` | 获取所有键 | `client.keys` → 批量类型 → `initKey`，返回键列表 |
| `getKey(int dbIndex, String key, boolean ttl, boolean loadValue, ShellRedisClient client)` | 获取键（简版） | 委托 5 参重载 |
| `getKey(int, String, boolean ttl, boolean objectEncoding, boolean loadValue, ShellRedisClient)` | 获取键 | `initKey` + 可选 ttl/objectEncoding/value，记录 loadTime |
| `initKey(int dbIndex, String key, ShellRedisKeyType type)` | 初始化键 | 支持类型则 `new ShellRedisKey` 并设置 type/key/dbIndex |
| `keyType(Integer dbIndex, String key, ShellRedisClient client)` | 单键类型 | `client.type → ShellRedisKeyType.valueOfType` |
| `keyType(Integer dbIndex, Collection<String> keys, ShellRedisClient client)` | 批量键类型 | `client.typeMulti` 后逐个映射 |
| `count(Integer dbIndex, String key, ShellRedisClient client)` | 统计值 | `client.pfcount`（忽略 HyperLogLog 类型错误） |
| `isHylog(Integer dbIndex, String key, ShellRedisClient client)` | 是否统计键 | 尝试 `pfcount` 判断 |
| `getKeys(ShellRedisClient client, int dbIndex, String pattern, List<String> existingKeys, int limit)` | 分页获取键 | 循环 `scanKeys`，过滤 existingKeys 并累加至 limit |

- 调用链：`ShellRedisKeyUtil.scanKeys → ShellRedisClient.scan / typeMulti → ShellRedisKeyUtil.initKey`；`getKey → keyValue → ShellRedisClient` 各数据命令。

---

# 三、键值模型（key 子包）

## ShellRedisKey

- 职责：Redis 键的领域模型，持有键名、类型、TTL、对象信息与键值对象，实现 `Comparable` 与 `ObjectCopier`。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbIndex | `int` | db 索引 |
| loadTime | `short` | 加载耗时 |
| ttl | `Long` | ttl 值 |
| key | `String` | key 名称 |
| type | `ShellRedisKeyType` | 键类型 |
| objectIdletime | `Long` | 空闲时间 |
| objectRefcount | `Long` | 引用数量 |
| objectedEncoding | `String` | 编码值 |
| value | `ShellRedisKeyValue<?>` | 键值 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `type(ShellRedisKeyType)` | 设置键类型 | 赋值 type |
| `isStringKey/isSetKey/isZSetKey/isListKey/isHashKey/isStreamKey/isJsonKey()` | 类型判断 | 与 `ShellRedisKeyType` 枚举比较 |
| `getDbIndex/setDbIndex`、`getLoadTime/setLoadTime`、`getTtl/setTtl`、`getKey/setKey`、`getType/setType`、`getObjectIdletime/setObjectIdletime`、`getObjectRefcount/setObjectRefcount`、`getObjectedEncoding/setObjectedEncoding`、`getValue/setValue` | 属性读写 | getter/setter |
| `compareTo(ShellRedisKey node)` | 键名比较 | 忽略大小写比较 key |
| `objectIdletimeString/objectedEncodingString/objectRefcountString()` | 展示字符串 | 空值显示 `N/A`，引用数为 `Integer.MAX_VALUE` 也显示 `N/A` |
| `isRawEncoding()` | 是否 raw 编码 | `encoding` 忽略大小写等于 `raw` |
| `keyBinary()` | 键的二进制 | `key.getBytes()` |
| `valueOfSet/valueOfZSet/valueOfCoordinates/valueOfHash/valueOfList/valueOfStream/valueOfString(byte[]/String)/valueOfJson/valueOfBytes(...)` | 以各类型设置键值 | 构造对应 `ShellRedisKeyValue` 实现 |
| `asSetValue/asZSetValue/asListValue/asHashValue/asStreamValue` | 转换为值对象 | 强转 value |
| `asStringValue()` | 转换为字符串值 | value 为空时 new `ShellRedisStringValue` |
| `asJsonValue()` | 转换为 json 值 | value 为空时 new `ShellRedisJsonValue` |
| `typeName()` | 类型名称 | `type.name()` |
| `copy(ShellRedisKey t1)` | 拷贝 | 复制 key/ttl/type/value/dbIndex/object* 字段 |

- 调用链：`ShellRedisKeyUtil.initKey → new ShellRedisKey；getKey → keyValue → valueOfXxx → ShellRedisKeyValue 实现`。

## ShellRedisKeyValue

- 职责：键值对象接口，定义值的读写与“未保存值”管理契约。

- 字段：无字段（接口）。

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getValue()` | 获取值 | 泛型 V |
| `setValue(V value)` | 设置值 | 由实现类处理存储 |
| `hasValue()` | 是否包含值 | 由实现类实现 |
| `getUnSavedValue()` | 获取未保存值 | 由实现类实现 |
| `clearUnSavedValue()` | 清除未保存值 | 由实现类实现 |
| `hasUnSavedValue()` | 是否含未保存值 | 由实现类实现 |
| `setUnSavedValue(Object unSavedValue)` | 设置未保存值 | 由实现类实现 |

- 调用链：`ShellRedisKey.value（接口） → 各 ShellRedisXxxValue 实现`。

## ShellRedisKeyRow

- 职责：键行接口，规范行列的取值/存值，继承 `Cloneable`。

- 字段：无字段（接口）。

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getValue()` | 获取值 | 由行实现类实现 |
| `setValue(String value)` | 设置值 | 由行实现类实现 |

- 调用链：`ShellRedisHashValue.RedisHashRow / ShellRedisListValue.RedisListRow / ... implements ShellRedisKeyRow`。

## ShellRedisHashValue

- 职责：hash 键值实现，内部行类 `RedisHashRow` 保存 field/value。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| value | `List<RedisHashRow>` | 值 |
| unSavedRow | `RedisHashRow` | 未保存的行 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisHashValue(List<RedisHashRow> value)` | 构造方法 | 保存 value |
| `valueOf(Map<String, String> value)` | 创建 hash 值 | 遍历 entry 生成 `RedisHashRow` |
| `getValue/setValue`、`getUnSavedRow/setUnSavedRow` | 属性读写 | getter/setter |
| `hasValue()` | 是否含值 | `CollectionUtil.isNotEmpty(value)` |
| `getUnSavedValue/clearUnSavedValue/hasUnSavedValue/setUnSavedValue` | 未保存值管理 | 直接维护 `unSavedRow` |
| `RedisHashRow(String field, String value)` | 行构造 | setField/setValue |
| `RedisHashRow.setField/getField/setValue/getValue/clone()` | 行读写 | 通过 `ShellRedisCacheUtil.cacheValue/loadValue` 按 `hashCode()` 键缓存 field/value |

- 调用链：`ShellRedisKey.valueOfHash → ShellRedisHashValue.valueOf → RedisHashRow`。

## ShellRedisJsonValue

- 职责：json 键值实现，值以 `ShellRedisCacheUtil` 缓存。

- 字段：无字段（值存于 `ShellRedisCacheUtil` 缓存）：

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisJsonValue()` / `ShellRedisJsonValue(String value)` | 构造方法 | 带参构造调用 setValue |
| `valueOf(String value)` | 创建 json 值 | new 实例 |
| `setValue/getValue/hasValue` | 值读写 | `ShellRedisCacheUtil.cacheValue/loadValue/hasValue` |
| `getUnSavedValue/clearUnSavedValue/hasUnSavedValue/setUnSavedValue` | 未保存值管理 | 使用缓存键 `unsaved` |
| `stringValue()` | 获取字符串值 | String/byte[] 兼容返回 |

- 调用链：`ShellRedisKey.valueOfJson → ShellRedisJsonValue.valueOf → ShellRedisCacheUtil`。

## ShellRedisListValue

- 职责：list 键值实现，行类 `RedisListRow` 带索引与值。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| value | `List<RedisListRow>` | 值 |
| unSavedRow | `RedisListRow` | 未保存的行 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisListValue(List<RedisListRow> value)` | 构造方法 | 保存 value |
| `valueOf(List<String> elements)` | 创建 list 值 | 按序生成 `RedisListRow(index++, element)` |
| `getValue/setValue`、`getUnSavedRow/setUnSavedRow` | 属性读写 | getter/setter |
| `hasValue()` | 是否含值 | 非空判断 |
| `getUnSavedValue/clearUnSavedValue/hasUnSavedValue/setUnSavedValue` | 未保存值管理 | 维护 `unSavedRow` |
| `RedisListRow(int index, String value)` / `getIndex()` / `setValue/getValue/clone()` | 行实现 | 值经 `ShellRedisCacheUtil` 缓存；clone 保留 index |

- 调用链：`ShellRedisKey.valueOfList → ShellRedisListValue.valueOf → RedisListRow`。

## ShellRedisSetValue

- 职责：set 键值实现，行类 `RedisSetRow` 带 `byte index`。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| value | `List<RedisSetRow>` | 值 |
| unSavedRow | `RedisSetRow` | 未保存的行 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisSetValue()` / `ShellRedisSetValue(List<RedisSetRow> value)` | 构造方法 | 无参/带值 |
| `valueOf(Set<String> members)` | 创建 set 值 | 遍历成员生成 `RedisSetRow` |
| `getValue/setValue`、`getUnSavedRow/setUnSavedRow` | 属性读写 | getter/setter |
| `hasValue()` | 是否含值 | 非空判断 |
| `getUnSavedValue/clearUnSavedValue/hasUnSavedValue/setUnSavedValue` | 未保存值管理 | 维护 `unSavedRow` |
| `RedisSetRow(String value)` / `getIndex/setIndex` / `setValue/getValue/clone()` | 行实现 | 值经 `ShellRedisCacheUtil` 缓存 |

- 调用链：`ShellRedisKey.valueOfSet → ShellRedisSetValue.valueOf → RedisSetRow`。

## ShellRedisStreamValue

- 职责：stream 键值实现，行类 `RedisStreamRow` 保存 id 与 JSON 化字段。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| value | `List<RedisStreamRow>` | 值 |
| unSavedRow | `RedisStreamRow` | 未保存的行 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisStreamValue(List<RedisStreamRow> value)` | 构造方法 | 保存 value |
| `valueOf(List<StreamEntry> value)` | 创建 stream 值 | 遍历 `StreamEntry` 生成行 |
| `getValue/setValue`、`getUnSavedRow/setUnSavedRow` | 属性读写 | getter/setter |
| `hasValue()` | 是否含值 | 非空判断 |
| `getUnSavedValue/clearUnSavedValue/hasUnSavedValue/setUnSavedValue` | 未保存值管理 | 维护 `unSavedRow` |
| `RedisStreamRow(StreamEntry entry)` | 行构造 | setId(entry id)、setValue(`JSONUtil.toJson(entry.getFields())`) |
| `setId/getId/getValue/setValue/getStreamId()/getFields()` | 行读写 | id/value 经缓存；`getStreamId` 返回 `StreamEntryID`，`getFields` 解析 JSON 为 `Map<String,String>` |

- 调用链：`ShellRedisKey.valueOfStream → ShellRedisStreamValue.valueOf → RedisStreamRow`；行字段 `ShellRedisKeyUtil.createKey → client.xadd(row.getStreamId(), row.getFields())`。

## ShellRedisStringValue

- 职责：string 键值实现，额外承载 HyperLogLog 统计值与标志位。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| count | `Long` | 统计值 |
| hyLog | `Boolean` | 统计值标志位 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisStringValue()` / `ShellRedisStringValue(String value)` / `ShellRedisStringValue(byte[] value)` | 构造方法 | 带参调用 setValue |
| `valueOf(String)` / `valueOf(byte[])` | 创建 string 值 | new 实例 |
| `setValue/getValue/hasValue` | 值读写 | `ShellRedisCacheUtil` 缓存，键 `value` |
| `getUnSavedValue/clearUnSavedValue/hasUnSavedValue/setUnSavedValue` | 未保存值管理 | 缓存键 `unsaved` |
| `getCount/setCount`、`getHyLog/setHyLog` | 统计属性读写 | getter/setter |
| `isHyLog()` | 是否统计值 | `count != null || BooleanUtil.isTrue(hyLog)` |
| `stringValue()` | 获取字符串值 | String/byte[] 兼容 |
| `bytesValue()` | 获取字节数组值 | String/byte[] 兼容 |

- 调用链：`ShellRedisKey.valueOfString/ShellRedisKeyUtil.count → ShellRedisStringValue`。

## ShellRedisZSetValue

- 职责：zset 键值实现，行类 `RedisZSetRow` 支持分数与经纬度。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| value | `List<RedisZSetRow>` | 值 |
| unSavedRow | `RedisZSetRow` | 未保存的行 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRedisZSetValue(List<RedisZSetRow> value)` | 构造方法 | 保存 value |
| `valueOf(List<String> members, List<Double> scores)` | 创建 zset 值 | 按序取分数生成行 |
| `valueOfCoordinates(List<String> members, List<GeoCoordinate> coordinates)` | 以地理坐标创建 | 使用 `coordinate.getLatitude/getLongitude` |
| `getValue/setValue`、`getUnSavedRow/setUnSavedRow` | 属性读写 | getter/setter |
| `hasValue()` | 是否含值 | 非空判断 |
| `getUnSavedValue/clearUnSavedValue/hasUnSavedValue/setUnSavedValue` | 未保存值管理 | 维护 `unSavedRow` |
| `RedisZSetRow(...)` 三个构造 / `getScore/setScore/getLatitude/setLatitude/getLongitude/setLongitude` | 行属性 | 分数、纬度、经度 |
| `RedisZSetRow.setValue/getValue/clone()` | 行读写 | 值经缓存；clone 复制 score/lat/lng/value |

- 调用链：`ShellRedisKey.valueOfZSet/valueOfCoordinates → ShellRedisZSetValue`；`ShellRedisKeyUtil.keyValue → client.zrange + zmscore_ext → valueOfZSet`。

---

# 四、批量结果（batch 子包）

## ShellRedisCountResult

- 职责：键计数结果，携带游标与本次数量。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| cursor | `String` | 光标 |
| count | `Integer` | 数量 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getCursor/setCursor`、`getCount/setCount` | 属性读写 | getter/setter |
| `isFinish()` | 是否完成 | 游标为 `ScanParams.SCAN_POINTER_START` 或 count 为空/为 0 |

- 调用链：`ShellRedisKeyUtil.countKeys → ShellRedisCountResult`；使用方循环判断 `isFinish()`。

## ShellRedisDeleteResult

- 职责：键删除结果，携带游标与本次数量。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| cursor | `String` | 光标 |
| count | `Integer` | 数量 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getCursor/setCursor`、`getCount/setCount` | 属性读写 | getter/setter |
| `isFinish()` | 是否完成 | 游标为起始或 count 空/0 |

- 调用链：`ShellRedisKeyUtil.deleteKeys → ShellRedisDeleteResult`。

## ShellRedisScanResult

- 职责：键扫描结果，携带游标与 `ShellRedisKey` 列表。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| cursor | `String` | 光标 |
| keys | `List<ShellRedisKey>` | 数据 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getCursor/setCursor`、`getKeys/setKeys` | 属性读写 | getter/setter |
| `isFinish()` | 是否完成 | 游标为起始或 keys 为空 |
| `keySize()` | 键数量 | keys 为 null 返回 0 |
| `keys()` | 键名称集合 | `parallelStream().map(ShellRedisKey::getKey)` |

- 调用链：`ShellRedisKeyUtil.scanKeys → ShellRedisScanResult`。

## ShellRedisScanSimpleResult

- 职责：简单键扫描结果，携带游标与键名列表。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| cursor | `String` | 光标 |
| keys | `List<String>` | 数据 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getCursor/setCursor`、`getKeys/setKeys` | 属性读写 | getter/setter |
| `isFinish()` | 是否完成 | 游标为起始或 keys 为空 |
| `keySize()` | 键数量 | keys 为 null 返回 0 |

- 调用链：`ShellRedisKeyUtil.scanKeysSimple → ShellRedisScanSimpleResult`；`scanKeys(limit)` 复用其结果。

---

## 跳过清单

| 文件 | 备注 |
|---|---|
| ShellRedisClient2.java | 整文件注释死代码（文件首行 `//package`，已被 ShellRedisClient 取代） |
| ShellRedisClientWrapper.java | 整文件注释死代码（文件首行 `//package`） |
