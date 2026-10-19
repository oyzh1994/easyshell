# 数据模块 · redis / zk 包

> 范围：`cn.oyzh.easyshell.data.redis.handler`（3 个类）与 `cn.oyzh.easyshell.data.zk.handler`（3 个类），共 **6** 个处理器类。
> 本模块基于 `cn.oyzh.fx.db.data.handler` 的 `DataExportHandler` / `DataImportHandler` / `DataTransportHandler` 三套基类，分别实现 redis 与 zk 的「导出/导入/传输」能力；通用文件读写经由 `cn.oyzh.store.file` 的 `FileHelper`/`TypeFileWriter`/`TypeFileReader` 完成。
> 全部字段均有 Javadoc，未发现整文件注释掉的死代码文件。

## 一、redis 部分（`data/redis/handler`）

## ShellRedisDataExportHandler

- 职责：将 redis 一个或全部库中的键，按查询模式批量序列化导出到文件（csv/excel/json 等）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | `ShellRedisClient` | redis 客户端 |
  | database | `Integer` | 数据库索引，`null` 表示全部库，其他值表示指定库 |
  | keyTypes | `List<String>` | 需要导出的键类型白名单（list/set/zset/hash/stream/json/string） |
  | retainTTL | `boolean` | 是否保留 ttl |
  | pattern | `String` | 键查询模式，默认 `*` |
  | batchSize | `int` | 批量处理大小，默认 10 |
  | config | `FileWriteConfig` | 导出（文件写入）配置 |
  | fileType | `String` | 文件格式 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doExport()` | 导出主流程（重写基类） | 构造 `FileColumns`（key/value/dbIndex/type/ttl）→ `FileHelper.initWriter(fileType, config, columns)` 取写入器 → 定义 `writeBatch` Runnable（`writer.writeRecords(batchList)` + `processedIncr`）→ 定义节点过滤 `BiPredicate`（空键/被 `isExclude` 排除则 `processedDecr`/`processedSkip` 并返回 false）→ 定义 `success`（`checkInterrupt` 后 `ShellRedisKeyUtil.serializeNode(redisKey)` 生成 `FileRecord`，满 `batchSize` 触发 `writeBatch`）→ 定义 `error`（剥离 RuntimeException 包装的 cause、忽略中断、`processedDecr`）→ `doExport(success,error,filter)`；`finally` 中冲刷 `writeBatch`、`writer.writeTrial()`、`writer.close()` |
  | `void doExport(Consumer<ShellRedisKey>, BiConsumer<String,Exception>, BiPredicate<String,ShellRedisKey>)`（私有） | 按库遍历键并处理 | 定义 `export` 消费者：对每个 key 调 `ShellRedisKeyUtil.getKey(dbIndex,key,retainTTL,true,client)`，`filter.test` 通过后若为 string 键则 `client.objectEncoding` 回填编码再 `success.accept`；`database==null` 时遍历 `client.databases()` 全部库 `allKeys`，否则仅查询 `database` 指定库 |
  | `boolean isExclude(ShellRedisKey)`（私有） | 判断键是否被类型白名单排除 | `keyTypes` 为空直接排除；逐一比对 list/set/zset/hash/stream/json/string，白名单不含且类型匹配即排除 |
  | `String getFileType()` / `setFileType(String)` | 文件格式读写 | 普通 getter/setter |
  | `ShellRedisClient getClient()` / `setClient(...)` | 客户端读写 | 普通 getter/setter |
  | `Integer getDatabase()` / `setDatabase(Integer)` | 数据库读写 | 普通 getter/setter |
  | `List<String> getKeyTypes()` / `setKeyTypes(...)` | 键类型读写 | 普通 getter/setter |
  | `boolean isRetainTTL()` / `setRetainTTL(boolean)` | ttl 保留标志读写 | 普通 getter/setter |
  | `String getPattern()` / `setPattern(String)` | 查询模式读写 | 普通 getter/setter |
  | `int getBatchSize()` / `setBatchSize(int)` | 批量大小读写 | 普通 getter/setter |
  | `FileWriteConfig getConfig()` / `setConfig(...)` | 导出配置读写 | 普通 getter/setter |
  | `void prefix(String)` | 设置前缀 | 空白 → `config.prefix(null)`，否则 `config.prefix(prefix+" ")` |
  | `void charset(String)` | 设置字符集 | 空白 → UTF-8，否则用传入值 |
  | `void filePath(String)` / `txtIdentifier(Character)` / `includeTitle(boolean)` / `compress(boolean)` | 委派到 `config` | 直接转发到 `FileWriteConfig` 对应方法 |

- 调用链：`DataExportHandler → ShellRedisDataExportHandler.doExport → doExport(success,error,filter) → ShellRedisKeyUtil.getKey / ShellRedisKeyUtil.serializeNode → ShellRedisClient.allKeys/objectEncoding`

## ShellRedisDataImportHandler

- 职责：从文件读取记录，把键值反序列化重建到 redis 指定库（支持不存在时创建、存在时跳过或备份覆盖）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | fileType | `String` | 文件格式 |
  | dbIndex | `Integer` | 数据库索引；`null` 时使用记录中的 dbIndex |
  | client | `ShellRedisClient` | redis 客户端 |
  | retainTTL | `boolean` | 是否保留 ttl |
  | batchSize | `int` | 批量处理大小，默认 50 |
  | ignoreExist | `boolean` | 键已存在时是否忽略（跳过） |
  | config | `FileReadConfig` | 导入（文件读取）配置 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doImport()` | 导入主流程（重写基类） | 构造 `FileColumns`（key=0/value=1/dbIndex=2/type=3/ttl=4）→ `FileHelper.initReader(...)` 取读取器 → 循环 `reader.readRecords(batchSize)` 直到空；逐条：`checkInterrupt`、取 key（空则 `processedSkip`）、确定 dbIndex（`dbIndex==null` 时从记录第 2 列取）、取 value/type/ttl、`ShellRedisKeyType.valueOfType(type)`；`!client.exists` 则 `createKey` + `processedIncr`；已存在且 `ignoreExist` 则 `processedSkip`；否则 `client.rename(key,key+"_backup")` → `createKey` → `client.del(key+"_backup")` 完成覆盖；异常时 `processedDecr`；`finally` 中 `reader.close()` |
  | `void createKey(String,int,ShellRedisKeyType,String,Long)`（私有） | 按类型重建键并处理 ttl | `ShellRedisKeyUtil.deserializeNode(type,value)` 得到键；string 键按 `String`/`byte[]` 分别 `client.set`；json 键 `client.jsonSet`；list 键 `client.lpush`；set 键 `client.sadd`；zset 键组装 `Map<String,Double>` 后 `client.zadd`；hash 键组装 `Map<String,String>` 后 `client.hmset`；stream 键逐行 `client.xadd`；其他类型告警；`ttl!=null && retainTTL` 时 `ttl==-1` 走 `client.persist`，否则 `client.expire` |
  | `String getFileType()` / `setFileType(String)` | 文件格式读写 | 普通 getter/setter |
  | `ShellRedisClient getClient()` / `setClient(...)` | 客户端读写 | 普通 getter/setter |
  | `boolean isRetainTTL()` / `setRetainTTL(boolean)` | ttl 保留标志读写 | 普通 getter/setter |
  | `int getBatchSize()` / `setBatchSize(int)` | 批量大小读写 | 普通 getter/setter |
  | `boolean isIgnoreExist()` / `setIgnoreExist(boolean)` | 忽略已存在标志读写 | 普通 getter/setter |
  | `FileReadConfig getConfig()` / `setConfig(...)` | 导入配置读写 | 普通 getter/setter |
  | `void dbIndex(Integer)` | 设置数据库索引 | 直接赋值 `dbIndex` |
  | `void charset(String)` | 设置字符集 | 空白 → UTF-8，否则用传入值 |
  | `void filePath(String)` / `txtIdentifier(Character)` / `dataRowStarts(Integer)` | 委派到 `config` | 直接转发到 `FileReadConfig` 对应方法 |

- 调用链：`DataImportHandler → ShellRedisDataImportHandler.doImport → createKey → ShellRedisKeyUtil.deserializeNode → ShellRedisClient.set/jsonSet/lpush/sadd/zadd/hmset/xadd`

## ShellRedisDataTransportHandler

- 职责：在源/目标 redis 客户端之间按模式复制键（目标不存在则创建，已存在按策略跳过或删除重建），可保留 ttl。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | sourceClient | `ShellRedisClient` | 来源客户端（protected） |
  | targetClient | `ShellRedisClient` | 目标客户端（protected） |
  | existsPolicy | `String` | 键已存在时处理策略（"0" 跳过 / "1" 更新） |
  | sourceDatabase | `int` | 来源数据库索引 |
  | targetDatabase | `int` | 目标数据库索引 |
  | keyTypes | `List<String>` | 键类型白名单 |
  | retainTTL | `boolean` | 是否保留 ttl |
  | pattern | `String` | 键查询模式，默认 `*` |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doTransport()` | 传输主流程（重写基类） | `sourceClient.allKeys(sourceDatabase, pattern)` 取全部键 → `doTransport(sourceDatabase, targetDatabase, allKeys)` |
  | `void doTransport(int,int,Set<String>)`（私有） | 逐键传输 | 对每个 key：`checkInterrupt` → `ShellRedisKeyUtil.getKey(fromDBIndex,key,retainTTL,true,sourceClient)` 取键（null 则 `processedIncr` 跳过）→ `isExclude` 排除则 `processedSkip` → 目标 `!exists` 则 `createKey(redisKey,targetDBIndex)` + `processedIncr` → 已存在且 `existsPolicy=="0"` 则 `processedSkip` → 否则 `targetClient.del(targetDBIndex,key)` 后 `createKey` 重建（更新语义） |
  | `boolean isExclude(ShellRedisKey)`（私有） | 判断键是否被类型白名单排除 | 逻辑同导出处理器：白名单空即排除，逐一比对 list/set/zset/hash/stream/string |
  | `void createKey(ShellRedisKey,int)`（私有） | 在目标库重建键 | `ShellRedisKeyUtil.createKey(redisKey,targetDBIndex,targetClient)`；`ttl!=null && retainTTL` 时 `ttl>=0` 走 `targetClient.expire`，`ttl==-1` 走 `targetClient.persist` |
  | `ShellRedisClient getSourceClient()` / `setSourceClient(...)` | 来源客户端读写 | 普通 getter/setter |
  | `ShellRedisClient getTargetClient()` / `setTargetClient(...)` | 目标客户端读写 | 普通 getter/setter |
  | `String getExistsPolicy()` / `setExistsPolicy(String)` | 存在策略读写 | 普通 getter/setter |
  | `int getSourceDatabase()` / `setSourceDatabase(int)` | 来源库索引读写 | 普通 getter/setter |
  | `int getTargetDatabase()` / `setTargetDatabase(int)` | 目标库索引读写 | 普通 getter/setter |
  | `List<String> getKeyTypes()` / `setKeyTypes(...)` | 键类型读写 | 普通 getter/setter |
  | `boolean isRetainTTL()` / `setRetainTTL(boolean)` | ttl 保留标志读写 | 普通 getter/setter |
  | `String getPattern()` / `setPattern(String)` | 查询模式读写 | 普通 getter/setter |

- 调用链：`DataTransportHandler → ShellRedisDataTransportHandler.doTransport → doTransport(from,to,keys) → ShellRedisKeyUtil.getKey / ShellRedisKeyUtil.createKey → ShellRedisClient.del/expire/persist`

## 二、zk 部分（`data/zk/handler`）

## ShellZKDataExportHandler

- 职责：从 zk 指定节点起递归导出节点路径与数据（可选包含 ACL）到文件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | fileType | `String` | 文件格式 |
  | client | `ShellZKClient` | zk 客户端 |
  | nodePath | `String` | 起始节点路径 |
  | batchSize | `int` | 批量处理大小，默认 10 |
  | includeACL | `boolean` | 是否包含 acl |
  | config | `FileWriteConfig` | 导出（文件写入）配置 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doExport()` | 导出主流程（重写基类） | 构造 `FileColumns`（path/data，`includeACL` 时追加 acl）→ `FileHelper.initWriter(...)` 取写入器 → 定义 `writeBatch`（`writeRecords` + `processedIncr`）→ 定义 `success`（`checkInterrupt` 后写 `FileRecord`：`node.nodePath()`、`new String(node.getNodeData(),UTF_8)`，`includeACL` 时 `ShellZKACLUtil.toAclStr(node.acl())`，满 `batchSize` 触发 `writeBatch`）→ 定义 `error`（同 redis 处理中断与 `processedDecr`）→ `ShellZKNodeUtil.loopNode(client,nodePath,null,success,error,includeACL)` 递归获取节点；`finally` 冲刷批、`writeTrial`、`close` |
  | `String getFileType()` / `setFileType(String)` | 文件格式读写 | 普通 getter/setter |
  | `ShellZKClient getClient()` / `setClient(...)` | 客户端读写 | 普通 getter/setter |
  | `String getNodePath()` / `setNodePath(String)` | 节点路径读写 | 普通 getter/setter |
  | `int getBatchSize()` / `setBatchSize(int)` | 批量大小读写 | 普通 getter/setter |
  | `boolean isIncludeACL()` / `setIncludeACL(boolean)` | acl 包含标志读写 | 普通 getter/setter |
  | `FileWriteConfig getConfig()` / `setConfig(...)` | 导出配置读写 | 普通 getter/setter |
  | `void prefix(String)` | 设置前缀 | 空白 → `config.prefix(null)`，否则 `config.prefix(prefix+" ")` |
  | `void charset(String)` | 设置字符集 | 空白 → UTF-8，否则用传入值 |
  | `void filePath(String)` / `txtIdentifier(Character)` / `includeTitle(boolean)` / `compress(boolean)` | 委派到 `config` | 直接转发到 `FileWriteConfig` 对应方法 |

- 调用链：`DataExportHandler → ShellZKDataExportHandler.doExport → ShellZKNodeUtil.loopNode → ShellZKACLUtil.toAclStr`

## ShellZKDataImportHandler

- 职责：从文件读取节点记录，反序列化到 zk（不存在则创建含父节点、存在则更新数据，可选解析 ACL）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | fileType | `String` | 文件格式 |
  | client | `ShellZKClient` | zk 客户端 |
  | batchSize | `int` | 批量处理大小，默认 50 |
  | includeACL | `boolean` | 是否包含 acl |
  | ignoreExist | `boolean` | 节点已存在时是否忽略 |
  | config | `FileReadConfig` | 导入（文件读取）配置 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doImport()` | 导入主流程（重写基类） | 构造 `FileColumns`（path=0/data=1/acl=2）→ `FileHelper.initReader(...)` 取读取器 → 循环 `reader.readRecords(batchSize)`；逐条：`checkInterrupt`、取 path（空则 `processedSkip`）、`client.exists(path)` 判断；`ignoreExist && exists` 则 `processedSkip`；取 data 并 `TextUtil.changeCharset(data, UTF-8, config.charset())`；存在则 `client.setData(path,dataStr)`，否则 `includeACL` 时用 `ShellZKACLUtil.parseAcl(acl)` 覆盖默认 `ZooDefs.Ids.OPEN_ACL_UNSAFE`，再 `client.create(path,dataStr,aclList,CreateMode.PERSISTENT,true)`；`processedIncr`；异常 `processedDecr`；`finally` 中 `reader.close()` |
  | `String getFileType()` / `setFileType(String)` | 文件格式读写 | 普通 getter/setter |
  | `ShellZKClient getClient()` / `setClient(...)` | 客户端读写 | 普通 getter/setter |
  | `int getBatchSize()` / `setBatchSize(int)` | 批量大小读写 | 普通 getter/setter |
  | `boolean isIncludeACL()` / `setIncludeACL(boolean)` | acl 包含标志读写 | 普通 getter/setter |
  | `boolean isIgnoreExist()` / `setIgnoreExist(boolean)` | 忽略已存在标志读写 | 普通 getter/setter |
  | `FileReadConfig getConfig()` / `setConfig(...)` | 导入配置读写 | 普通 getter/setter |
  | `void charset(String)` | 设置字符集 | 空白 → UTF-8，否则用传入值 |
  | `void filePath(String)` / `txtIdentifier(Character)` / `dataRowStarts(Integer)` | 委派到 `config` | 直接转发到 `FileReadConfig` 对应方法 |

- 调用链：`DataImportHandler → ShellZKDataImportHandler.doImport → ShellZKACLUtil.parseAcl / TextUtil.changeCharset → ShellZKClient.exists/setData/create`

## ShellZKDataTransportHandler

- 职责：从源 zk 根节点 `/` 起递归将节点数据复制到目标 zk（跳过临时节点、按策略跳过/更新、支持源到目标字符集转码）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | sourceClient | `ShellZKClient` | 来源客户端（protected） |
  | targetClient | `ShellZKClient` | 目标客户端（protected） |
  | existsPolicy | `String` | 节点已存在时处理策略（"0" 跳过 / "1" 更新） |
  | sourceCharset | `Charset` | 来源字符集 |
  | targetCharset | `Charset` | 目标字符集 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doTransport()` | 传输主流程（重写基类） | 从根路径调用 `doTransport("/")` |
  | `void doTransport(String)`（私有，递归） | 递归传输单个节点及其子节点 | `ShellZKNodeUtil.decodePath(path)` 解码用于日志；`checkInterrupt` → `sourceClient.checkExists(path)` 与 `getData(path)`；`stat==null || bytes==null` 则 `processedDecr` 返回；`stat.getEphemeralOwner()>0`（临时节点）则 `processedSkip` 返回；目标存在时：`existsPolicy=="0"` → `processedSkip`，`=="1"` → `TextUtil.changeCharset(bytes,sourceCharset,targetCharset)` 后 `targetClient.setData` 并 `processedIncr`；目标不存在则转码后 `targetClient.createIncludeParents(path,bytes,CreateMode.PERSISTENT)` 并 `processedIncr`；最后 `sourceClient.getChildren(path)` 逐子节点 `ShellZKNodeUtil.concatPath` 后递归；`InterruptedException` 直接抛出，其余异常 `processedDecr` |
  | `ShellZKClient getSourceClient()` / `setSourceClient(...)` | 来源客户端读写 | 普通 getter/setter |
  | `ShellZKClient getTargetClient()` / `setTargetClient(...)` | 目标客户端读写 | 普通 getter/setter |
  | `String getExistsPolicy()` / `setExistsPolicy(String)` | 存在策略读写 | 普通 getter/setter |
  | `Charset getSourceCharset()` / `setSourceCharset(...)` | 来源字符集读写 | 普通 getter/setter |
  | `Charset getTargetCharset()` / `setTargetCharset(...)` | 目标字符集读写 | 普通 getter/setter |

- 调用链：`DataTransportHandler → ShellZKDataTransportHandler.doTransport("/") → doTransport(path) 递归 → ShellZKNodeUtil.decodePath/concatPath / TextUtil.changeCharset → ShellZKClient.getData/setData/createIncludeParents/getChildren`

## 跳过清单

- 无整文件被注释掉的死代码文件。6 个类均为活跃代码，全部覆盖。
- 各文件内存在零散的注释代码块（如 redis/zk 导出、传输处理器中被注释的 `filters` 过滤逻辑，`ShellRedisDataExportHandler` 中被注释的 `getFilters/setFilters`），按规则不单独列出。
