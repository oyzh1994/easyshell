# easyshell mongo 包代码审查

范围：`easyshell/src/main/java/cn/oyzh/easyshell/mongo/`（递归全部）。
共覆盖 52 个正式类（另有 0 个整文件被注释掉的死代码，文末清单为空，故不展开）。

本包是 easyshell 对 MongoDB 的本地封装，整体分为四层：

- **客户端入口**：`ShellMongoClient` 基于官方 `com.mongodb.client.*` 驱动，负责连接（含 SSH 跳板 / 代理 / SSL）、库表桶的元数据与数据 CRUD、函数与用户管理，并实现 `ShellFileClient<MongoBucketFile>` 以复用 GridFS 文件传输界面。
- **领域模型**：`MongoDatabase`/`MongoCollection`/`MongoBucket`/`MongoColumn(s)`/`MongoRecord(Property)`/`MongoFunction`/`MongoUser*` 等纯数据对象，普遍实现 `ObjectCopier`/`ObjectComparator` 以支持复制与比较。
- **查询条件体系**：`condition/` 下以 `MongoCondition`（抽象基类）派生出一批条件类，每个用单例 `INSTANCE` 注册到 `DBConditionManager`，`wrapCondition` 把界面值翻译成 Bson 过滤器；`MongoConditionUtil` 负责注册、拼装 `$and`/`$or` 与生成输入控件。
- **脚本引擎**：`script/` 下用 Nashorn 执行类 MongoDB Shell 语法，`MongoScriptCollection`/`MongoScriptDatabase` 是对驱动集合/库的薄包装（供 JS 调用），`script/function/` 提供 `ObjectId`、`ISODate`、`Code` 等内建构造函数。

全包统一约定：对 `_id` 字段的比较/正则条件不走普通字段路径，而是用 `$expr` + `$toString: "$_id"` 把 ObjectId 转成字符串后再比较。

---

# 一、客户端入口（cn.oyzh.easyshell.mongo）

## ShellMongoClient
- 职责：MongoDB 连接客户端封装，负责连接建立、库/集合/桶/函数/用户的元数据与数据操作，并实现文件客户端接口。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | shellConnect | ShellConnect | 连接信息 |
  | jumpForwarder | SSHJumpForwarder2 | SSH 端口转发器（跳板） |
  | sslConfigStore | ShellSSLConfigStore | SSL 配置存储（常量 INSTANCE） |
  | proxyConfigStore | ShellProxyConfigStore | 代理配置存储（常量 INSTANCE） |
  | state | SimpleObjectProperty\<ShellConnState\> | 连接状态属性 |
  | stateListener | ChangeListener\<ShellConnState\> | 状态变更监听器 |
  | mongoClient | MongoClient | 官方驱动客户端 |
  | engine | MongoScriptEngine | 脚本引擎（懒加载） |
  | version | String | 服务端版本号缓存 |
  | deleteCompetitor / uploadCompetitor / downloadCompetitor | Competitor | 删除/上传/下载并发控制（容量 5/2/2） |
  | deleteTasks / uploadTasks / downloadTasks | ObservableList\<...\> | 文件任务列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoClient(ShellConnect)` | 构造 | 保存连接、注册状态监听器 |
  | `boolean isConnected()` | 是否已连接 | 列举库名后逐个 `ping`，异常返回 false |
  | `void close()` | 关闭 | `mongoClient.close()` → 移除监听器 → 状态置 `CLOSED` |
  | `boolean ping(String dbName)` | 心跳 | `runCommand({ping:1})`，校验 `ok==1.0` |
  | `String initHost()` | 解析主机 | 启用跳板则 `SSHJumpForwarder2.forward` 得到 `127.0.0.1:port`，否则直连 host:port |
  | `MongoScriptEngine shellEngine()` | 取脚本引擎 | 懒加载 `new MongoScriptEngine(mongoClient)` |
  | `void initProxy(ProxySettings.Builder)` | 初始化代理 | 读 `ShellProxyConfig`，设置 host/port/用户名密码 |
  | `void initSSL(SslSettings.Builder)` | 初始化 SSL | 读 `ShellSSLConfig`，`ShellMongoHelper.buildSSLContext` 构建上下文 |
  | `void initClient(int timeoutMs)` | 初始化客户端 | 组装 `MongoClientSettings`（集群/套接字/SSL/代理/认证）→ `MongoClients.create` |
  | `void start(int timeout)` | 启动连接 | `initClient` → 状态 `CONNECTING` → `listDatabases().first()` 触发实连 → `CONNECTED` → `ShellClientChecker.push` |
  | `MongoDatabase database(String dbName)` | 构造库对象 | new `MongoDatabase` 并 set 名称 |
  | `List<MongoDatabase> listDatabases()` | 列举库 | 优先 `mongoSpecifiedDatabases`，否则 `listDatabases()`；无权限时回退认证库 |
  | `List<String> listDatabaseNames()` | 列举库名 | 同上（字符串版） |
  | `void createDatabase(String dbName)` | 建库 | 建立集合 `_empty_` 触发库创建 |
  | `boolean existDatabase(String dbName)` | 库是否存在 | `listCollectionNames().first() != null` |
  | `boolean dropDatabase(String dbName)` | 删库 | `database.drop()` |
  | `void dropCollection(String, String)` | 删集合 | `collection.drop()` |
  | `List<MongoCollection> listCollections(String)` | 列举集合 | 过滤 `ShellMongoRecordUtil.isCollection`，按名称排序 |
  | `List<String> listCollectionNames(String)` | 列举集合名 | 同上 |
  | `boolean existCollection(String, String)` | 集合是否存在 | 名称列表 contains |
  | `List<MongoRecord> selectCollectionRecords(MongoSelectRecordParam)` | 查记录 | `MongoConditionUtil.buildCondition(filters)` → `find().skip().limit()` → `ShellMongoRecordUtil.docToRecord` |
  | `MongoRecord selectCollectionRecord(String, String, Object)` | 查单条 | `Filters.eq(_id,id)` + `first()` |
  | `long selectCollectionRecordCount(MongoSelectRecordParam)` | 计数 | `countDocuments(filters)` |
  | `BsonValue insertCollectionRecord(MongoRecord)` | 插单条 | 逐列 append 成 Document（`_id` 为空则跳过）→ `insertOne` |
  | `List<BsonValue> insertCollectionRecord(List<MongoRecord>)` | 插多条 | 批量转换 → `insertMany` → `getInsertedIds` |
  | `long deleteCollectionRecord(MongoRecord)` | 删记录 | `_idColumn` 非空校验 → `Filters.eq(_id)` → `deleteOne` |
  | `void createCollection(MongoCollection)` / `void createCollection(String, String)` | 建集合 | `database.createCollection` |
  | `long clearCollection(String, String)` | 清空集合 | `deleteMany(new Document())` |
  | `void renameCollection(String, String, String)` | 重命名集合 | `MongoNamespace` + `renameCollection` |
  | `long updateCollectionRecord(MongoRecord)` | 更新记录 | 逐非 `_id` 列 `Updates.set`，旧文档多余列 `Updates.unset`，`updateOne` |
  | `List<MongoBucket> listBuckets(String)` | 列举桶 | 过滤 `isBucket`，桶名取 `.files` 之前部分，排序 |
  | `List<String> listBucketNames(String)` | 列举桶名 | 收集 `*.files` 集合名 |
  | `boolean existBucket(String, String)` | 桶是否存在 | 桶名列表 contains `bucketName + ".files"` |
  | `GridFSBucket bucket(String, String)` | 取 GridFS 桶 | `GridFSBuckets.create` |
  | `void createBucket(MongoBucket)` | 建桶 | 上传空流 `_empty_` 后再删除，使桶出现 |
  | `void dropBucket(String, String)` | 删桶 | `dropCollection(bucketName + ".files")` |
  | `void clearBucket(String, String)` | 清空桶 | `find().forEach(delete)` |
  | `List<MongoBucketFile> selectBucketRecords(MongoSelectRecordParam)` | 查桶文件 | `bucket.find(filters).limit().skip()` → `MongoBucketFile.of` |
  | `MongoBucketFile selectBucketRecord(String, String, Object)` | 查单文件 | 参数校验 + `Filters.eq(_id)` + `first()` |
  | `long selectBucketRecordCount(MongoSelectRecordParam)` | 桶计数 | 对 `collectionName + ".files"` 计数 |
  | `ObjectId uploadBucketRecord(String, String, File)` / `(..., String, InputStream)` | 上传桶文件 | `bucket.uploadFromStream` |
  | `void reuploadBucketRecord(...)` | 重新上传 | 先删旧记录再 `uploadFromStream(bsonValue, filename, stream)` |
  | `void downloadBucketRecord(String, String, Object, String)` / `(..., OutputStream)` | 下载 | `bucket.downloadToStream` |
  | `long deleteBucketRecord(String, String, Object)` | 删桶记录 | `bucket.delete(bsonValue/objectId)` |
  | `long updateBucketRecord(MongoBucketFile)` | 更新桶文件 | 对 `*.files` 集合 `Updates.set(filename/metadata)` |
  | `List<? extends MongoColumn> selectColumns(MongoSelectRecordParam)` | 查字段 | `selectCollectionRecords` → `ShellMongoRecordUtil.columns` |
  | `String selectVersion()` | 版本 | `admin.runCommand(buildInfo)` 取 version（缓存） |
  | `Map<?,?> selectHostInfo()` | 主机信息 | `admin.runCommand(hostInfo)` |
  | `ShellMongoExecuteResult executeSingleScript(String, String)` | 执行单段脚本 | `shellEngine().db()` → `eval` → `parseResult` 归类结果类型 |
  | `void parseResult(..., Object, ..., ...)` | 解析脚本结果 | 分派 FindCursor/Cursor/List/Delete/Insert/Update 结果，转 `MongoRecord` |
  | `MongoRecord toMongoRecord(Object, String, String)` | 转记录 | Document → `docToRecord`；其他对象包成单列 JSON |
  | `DBQueryResults<ShellMongoExecuteResult> executeScript(String, String)` | 执行脚本 | `MongoScriptParser.parseScript` 分段后逐段执行 |
  | `List<MongoFunction> listFunctions(String)` | 列举函数 | 查 `system.js` 集合，`value` 为 `Code` 才收录 |
  | `BsonValue createFunction(String, String, String)` | 建函数 | 写 `system.js`（`_id`+`value=Code`） |
  | `boolean alertFunction(String, String, String)` | 改函数 | `Updates.set("value", Code)` |
  | `MongoFunction selectFunction(String, String)` | 查函数 | `Filters.eq("_id")` + `first()` |
  | `boolean dropFunction(String, String)` | 删函数 | `findOneAndDelete` |
  | `boolean renameFunction(String, String, String)` | 重命名函数 | 复制文档为新 `_id` 后删旧 |
  | `long functionSize(String)` | 函数数 | `countDocuments()` |
  | `List<MongoUser> listUsers(String)` | 列举用户 | `ShellMongoUserUtil.getUsers` → 解析 users/roles |
  | `boolean createUser(String, MongoUser)` | 建用户 | 组装 roles 文档 → `ShellMongoUserUtil.createUser` |
  | `boolean dropUser(String, String)` | 删用户 | `ShellMongoUserUtil.dropUser` |
  | `long userSize(String)` | 用户数 | `getUsers` 后取 users 集合大小 |
  | `Object eval(String, String)` | 执行脚本 | `shellEngine().db()` + `eval` |
  | `void get(MongoBucketFile, String, Function<Long,Boolean>)` | 下载（文件接口） | 记录操作 → `ShellFileProgressMonitor.of` 包装后 `downloadBucketRecord` |
  | `void put(InputStream, String, Function<Long,Boolean>)` | 上传（文件接口） | 解析路径 → `uploadBucketRecord`，结果写入 `ThreadLocalUtil` |
  | `boolean rename(MongoBucketFile, String)` | 重命名 | 构造新 `MongoBucketFile` → `updateBucketRecord` |
  | `void delete(MongoBucketFile)` | 删除 | `deleteBucketRecord` |
  | `Competitor deleteCompetitor()/uploadCompetitor()/downloadCompetitor()` | 并发器 | 返回对应 `Competitor` |
  | `ObservableList<...> deleteTasks()/uploadTasks()/downloadTasks()` | 任务列表 | 返回对应可观察列表 |
  | 其余 `lsFileDynamic/delete(String)/deleteDir/.../chmod/fileInfo` | 文件接口占位 | 多数为空实现或返回 false/null |

- 调用链：`界面/业务 → ShellMongoClient.xxx → MongoConditionUtil/ShellMongoRecordUtil/ShellMongoUserUtil → com.mongodb.client.* → 服务端`

## ShellMongoHelper
- 职责：MongoDB 静态辅助类，提供角色常量、SSL 上下文构建、桶字段列表与远端路径解析。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | ROLES | List\<String\> | 内置角色常量（read/readWrite/dbAdmin 等 10 个） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static SSLContext buildSSLContext(ShellSSLConfig)` | 构建 SSL 上下文 | CA 证书 → TrustManager；客户端证书+私钥 → KeyManager；无 CA 时用 `TrustAllX509TrustManager` |
  | `static MongoColumns bucketColumns()` | 桶字段列表 | 构造 `_id/filename/length/chunkSize/uploadDate/metadata` 列 |
  | `static String getDbName(String)` | 解析库名 | 取 `@` 之前 |
  | `static String getBucketName(String)` | 解析桶名 | 取 `@` 与 `/` 之间 |
  | `static String getFileName(String)` | 解析文件名 | 取 `/` 之后 |

- 调用链：`ShellMongoClient.initSSL → ShellMongoHelper.buildSSLContext → PemUtil/TrustAllX509TrustManager`

## ShellMongoUserUtil
- 职责：MongoDB 用户管理命令的静态封装（用户查询/删除/创建）。
- 字段：无（纯静态工具类）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static Document getUsers(MongoDatabase)` | 查询所有用户 | `runCommand({usersInfo:1})` |
  | `static Document getUser(MongoDatabase, String)` | 查询指定用户 | `runCommand({usersInfo:{user,db}})` |
  | `static Document dropUser(MongoDatabase, String)` | 删除用户 | `runCommand({dropUser:username})` |
  | `static Document createUser(MongoDatabase, String, String, List<Document>)` | 创建用户 | `runCommand({createUser,pwd,roles})` |

- 调用链：`ShellMongoClient.listUsers/createUser/dropUser → ShellMongoUserUtil → MongoDatabase.runCommand`

---

# 二、桶（cn.oyzh.easyshell.mongo.bucket）

## MongoBucket
- 职责：GridFS 存储桶的数据对象（名称 + 所属库）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | String | 桶名称 |
  | dbName | String | 所属数据库名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getName()/void setName(String)` | 桶名存取 | 简单存取 |
  | `String getDbName()/void setDbName(String)` | 库名存取 | 简单存取 |
  | `boolean compare(MongoBucket)` | 比较 | name 与 dbName 均相等 |
  | `void copy(MongoBucket)` | 复制 | 复制 name/dbName |

- 调用链：`ShellMongoClient.listBuckets → new MongoBucket`

## MongoBucketFile
- 职责：GridFS 文件的数据对象，实现 `ShellFile` 以适配通用文件界面。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbName | String | 所属数据库 |
  | bucketName | String | 所属桶 |
  | length | long | 文件长度 |
  | id | BsonValue | 文件 `_id` |
  | chunkSize | int | 分块大小 |
  | fileName | String | 文件名 |
  | uploadDate | Date | 上传时间 |
  | metadata | Document | 元数据 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 各 `getXxx/setXxx` | 属性存取 | 简单存取 |
  | `String getMetadataJson()` | 元数据 JSON | `JSONUtil.toJson(metadata)` |
  | `String getChunkSizeText()` | 分块大小文本 | `NumberUtil.formatSize` |
  | `String getAddTime()` | 上传时间文本 | `ShellMongoUtil.DATE_FORMAT.format` |
  | `String getIdText()` | id 文本 | `ShellMongoRecordUtil.idValue(id).toString()` |
  | `String getParentPath()` | 父路径 | `dbName + "@" + bucketName` |
  | `boolean isDirectory()/isFile()/isLink()` | 类型判断 | 分别 false/true/false |
  | `String getModifyTime()/void setModifyTime(String)` | 修改时间 | 格式化/解析 uploadDate |
  | `long getFileSize()/void setFileSize(long)` | 文件大小 | 映射 length |
  | `String getLengthText()` | 长度文本 | `NumberUtil.formatSize` |
  | `static MongoBucketFile of(GridFSFile)` | 由驱动文件构造 | 拷贝 id/length/filename/metadata/chunkSize/uploadDate |
  | `void copy(ShellFile)` | 复制 | 从另一个 `MongoBucketFile` 拷贝核心字段 |

- 调用链：`ShellMongoClient.selectBucketRecords → MongoBucketFile.of(GridFSFile)`

---

# 三、集合（cn.oyzh.easyshell.mongo.collection）

## MongoCollection
- 职责：MongoDB 集合的数据对象（名称 + 所属库），实现 `DBName`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | String | 集合名称 |
  | dbName | String | 所属数据库名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getName()/void setName(String)` | 集合名存取 | 简单存取 |
  | `String getDbName()/void setDbName(String)` | 库名存取 | 简单存取 |
  | `boolean compare(MongoCollection)` | 比较 | name 与 dbName 均相等 |
  | `void copy(MongoCollection)` | 复制 | 复制 name/dbName |

- 调用链：`ShellMongoClient.listCollections → new MongoCollection`

---

# 四、字段（cn.oyzh.easyshell.mongo.column）

## MongoColumn
- 职责：MongoDB 字段（列）对象，实现 `DBColumn`，承载名称、类型、值与类型能力判定。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbName | String | 所属库 |
  | collectionName | String | 所属集合 |
  | typeProperty | StringProperty | 字段类型属性 |
  | value | String | 字段值 |
  | name | String | 字段名 |
  | aliasName | String | 别名（优先显示） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 三个构造器 | 构造 | 名称、别名可选 |
  | `boolean isNameChanged()` / `String originalName()` | 原名变更判断 | 基于 `DBObject` 原始数据 |
  | `void setType(String)/String getType()` | 类型存取 | 置大写并记录原始类型 |
  | `void setValue(String)/String getValue()` | 值存取 | 记录原始值 |
  | `supportDigits/supportInteger/supportBigInteger/supportString/supportBoolean/supportJson/supportJsonArray/supportBinary` | 类型能力判定 | 均委托 `DBColumnFieldManager.supportXxx(DBDialect.MONGODB, type)` |
  | `boolean supportObjectId()` | 是否对象 id | type 忽略大小写等于 `"obejectid"`（原文拼写） |
  | `boolean supportCode()` | 是否代码 | type 等于 `code` |
  | `boolean supportTimestamp()` | 是否时间戳 | type 等于 `date` |
  | `void setName/setDbName/setCollectionName/setAliasName` | 各属性存取 | 名称写入原始数据 |
  | `void initStatus()` | 初始化状态 | 值为空则置空 |
  | `void copy(MongoColumn)` | 复制 | 复制名/类型/值/库/别名/集合 |
  | `void setSize/getSize` | 大小占位 | 空实现 / 返回 0 |
  | `boolean is_id()` | 是否 `_id` | 名称忽略大小写等于 `_id` |
  | `String displayName()` | 显示名 | 有别名用别名否则用名 |
  | `Object defaultValue()` | 默认值 | 按类型返回 ObjectId/0/0L/0d/Document/Date/byte[]/false/Code/空串 |

- 调用链：`ShellMongoRecordUtil ↔ MongoColumn（类型能力判定）`

## MongoColumns
- 职责：MongoDB 字段集合，继承 `DBObjectList<MongoColumn>`，提供按名查找与批量取名。
- 字段：无（继承 `DBObjectList` 的列表存储）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 两个构造器 | 构造 | 空构造 / 从 List 初始化 |
  | `boolean exists(String)` | 是否存在 | `column(name) != null` |
  | `MongoColumn column(String)` | 按名取列 | 忽略大小写匹配（`StringUtil.equalsAnyIgnoreCase`） |
  | `int index(String)` | 索引 | 遍历找到匹配下标 |
  | `String collectionName()` | 集合名 | 返回首个元素的集合名 |
  | `String dbName()` | 库名 | 返回首个元素的库名 |
  | `List<String> columnNames()` | 全部列名 | 收集各列名 |

- 调用链：`ShellMongoRecordUtil/ShellMongoClient → MongoColumns`

---

# 五、条件（cn.oyzh.easyshell.mongo.condition）

> 说明：本组统一模式为——每个条件实现 `MongoCondition` 抽象类，持有 `public final static` 单例 `INSTANCE`，构造时向父类传（国际化名、符号、是否必需值），并重写 `wrapCondition(columnName, condition)` 把界面值翻译为 Bson。对 `_id` 字段一律走 `$expr` + `$toString: "$_id"` 路径，其他字段走 `Filters.and(Filters.exists(col), ...)`。

## MongoCondition
- 职责：MongoDB 条件抽象基类，定义条件翻译接口。
- 字段：无（继承 `DBCondition`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 三个构造器 | 构造 | 委托 `DBCondition` 的 name/value/requireCondition |
  | `Bson wrapCondition(String, Object)` | 条件翻译 | 抽象行为，默认返回 null，由子类实现 |

- 调用链：`MongoRecordFilter.condition → MongoCondition.wrapCondition`

## MongoConditionUtil
- 职责：条件注册与 Bson 过滤器拼装、条件输入控件生成、`_id` 正则过滤器构建。
- 字段：无（纯静态工具类）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static void init()` | 注册全部条件 | 逐个 `DBConditionManager.putCondition(DBDialect.MONGODB, XxxCondition.INSTANCE)`（共 20 个） |
  | `static Bson buildCondition(List<MongoRecordFilter>)` | 拼装过滤器 | 逐个启用条件 `filter.condition()`，按上一条件 `joinSymbol` 用 `Filters.and`/`Filters.or` 连接 |
  | `static boolean isInCondition(MongoCondition)` | 是否 IN 类 | `== MongoInListCondition.INSTANCE || == MongoNotInListCondition.INSTANCE` |
  | `static boolean isBetweenCondition(MongoCondition)` | 是否 BETWEEN 类 | 介于/不介于判断 |
  | `static List<Node> generateNode(MongoColumn, MongoCondition)` | 生成输入控件 | IN 类给 1 个文本框；BETWEEN 类给 2 个；其他给 1 个 `ShellMongoNodeUtil.generateNode` |
  | `static void setNodeVal(List<Node>, Object)` | 回填值 | 值本身为 List 时逐控件回填 |
  | `static Object getNodeVal(List<Node>)` | 取值 | 单控件直接取值，多控件聚合成 List |
  | `static Bson idFilterRegex(Pattern)` | `_id` 正则匹配 | `Filters.expr($regexMatch(input=$toString($_id), regex))` |
  | `static Bson idFilterRegexNot(Pattern)` | `_id` 正则不匹配 | 在 `$regexMatch` 外包 `$not` |

- 调用链：`ShellMongoClient.selectCollectionRecords → MongoConditionUtil.buildCondition → MongoRecordFilter.condition → MongoCondition.wrapCondition`

## MongoEqCondition
- 职责：等于条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoEqCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoEqCondition()` | 构造 | name=eq 国际化值，符号 `=` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `$expr($eq[$toString($_id), 值])`；否则 `Filters.and(exists, eq)` |

- 调用链：`MongoConditionUtil.init → MongoEqCondition.INSTANCE`

## MongoNotEqCondition
- 职责：不等于条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoNotEqCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoNotEqCondition()` | 构造 | 符号 `!=` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `$expr($ne...)`；否则 `Filters.and(exists, ne)` |

- 调用链：`MongoConditionUtil.init → MongoNotEqCondition.INSTANCE`

## MongoGtCondition
- 职责：大于条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoGtCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoGtCondition()` | 构造 | 符号 `>` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `$expr($gt...)`；否则 `Filters.and(exists, gt)` |

- 调用链：`MongoConditionUtil.init → MongoGtCondition.INSTANCE`

## MongoGtEqCondition
- 职责：大于等于条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoGtEqCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoGtEqCondition()` | 构造 | 符号 `>=` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `$expr($gte...)`；否则 `Filters.and(exists, gte)` |

- 调用链：`MongoConditionUtil.init → MongoGtEqCondition.INSTANCE`

## MongoLtCondition
- 职责：小于条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoLtCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoLtCondition()` | 构造 | 符号 `<` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `$expr($lt...)`；否则 `Filters.and(exists, lt)` |

- 调用链：`MongoConditionUtil.init → MongoLtCondition.INSTANCE`

## MongoLtEqCondition
- 职责：小于等于条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoLtEqCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoLtEqCondition()` | 构造 | 符号 `<=` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `$expr($lte...)`；否则 `Filters.and(exists, lte)` |

- 调用链：`MongoConditionUtil.init → MongoLtEqCondition.INSTANCE`

## MongoBetweenCondition
- 职责：介于条件（区间 `[f,l]`）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoBetweenCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoBetweenCondition()` / `(String, String)` | 构造 | 符号 `BETWEEN` |
  | `Bson wrapCondition(String, Object)` | 翻译 | 条件为 List，取首尾；`_id` → `$expr($and[$gte,$lte])`；否则 `and(exists, gte(f), lte(l))` |

- 调用链：`MongoConditionUtil.init → MongoBetweenCondition.INSTANCE`

## MongoNotBetweenCondition
- 职责：不介于条件，继承 `MongoBetweenCondition`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoNotBetweenCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoNotBetweenCondition()` | 构造 | 符号 `NOT BETWEEN` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `$expr($or[$lt,$gt])`；否则 `and(exists, or(lt(f), gt(l)))` |

- 调用链：`MongoConditionUtil.init → MongoNotBetweenCondition.INSTANCE`

## MongoContainsCondition
- 职责：包含条件（忽略大小写正则）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoContainsCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoContainsCondition()` / `(String, String)` | 构造 | 符号 `LIKE` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `Pattern.quote` + 忽略大小写；`_id` → `idFilterRegex`；否则 `and(exists, regex)` |

- 调用链：`MongoConditionUtil.init → MongoContainsCondition.INSTANCE → MongoConditionUtil.idFilterRegex`

## MongoNotContainsCondition
- 职责：不包含条件，继承 `MongoContainsCondition`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoNotContainsCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoNotContainsCondition()` | 构造 | 符号 `NOT LIKE` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `idFilterRegexNot`；否则 `and(exists, not(regex))` |

- 调用链：`MongoConditionUtil.init → MongoNotContainsCondition.INSTANCE → MongoConditionUtil.idFilterRegexNot`

## MongoStartWithCondition
- 职责：以……开始条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoStartWithCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoStartWithCondition()` / `(String, String)` | 构造 | 符号 `LIKE` |
  | `Bson wrapCondition(String, Object)` | 翻译 | 正则加 `^` 前缀；`_id` → `idFilterRegex`；否则 `and(exists, regex)` |

- 调用链：`MongoConditionUtil.init → MongoStartWithCondition.INSTANCE`

## MongoNotStartWithCondition
- 职责：不以……开始条件，继承 `MongoStartWithCondition`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoNotStartWithCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoNotStartWithCondition()` | 构造 | 符号 `NOT LIKE` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `idFilterRegexNot`；否则 `and(exists, not(regex))` |

- 调用链：`MongoConditionUtil.init → MongoNotStartWithCondition.INSTANCE`

## MongoEndWithCondition
- 职责：以……结束条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoEndWithCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoEndWithCondition()` / `(String, String)` | 构造 | 符号 `LIKE` |
  | `Bson wrapCondition(String, Object)` | 翻译 | 正则加 `$` 后缀；`_id` → `idFilterRegex`；否则 `and(exists, regex)` |

- 调用链：`MongoConditionUtil.init → MongoEndWithCondition.INSTANCE`

## MongoNotEndWithCondition
- 职责：不以……结束条件，继承 `MongoEndWithCondition`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoNotEndWithCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoNotEndWithCondition()` | 构造 | 符号 `NOT LIKE` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `idFilterRegexNot`；否则 `and(exists, not(regex))` |

- 调用链：`MongoConditionUtil.init → MongoNotEndWithCondition.INSTANCE`

## MongoNullCondition
- 职责：为 NULL 条件（无需输入值）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoNullCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoNullCondition()` | 构造 | 符号 `IS NULL`，`requireCondition=false` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `Filters.or(exists(col,false), eq(col,null))` |

- 调用链：`MongoConditionUtil.init → MongoNullCondition.INSTANCE`

## MongoNotNullCondition
- 职责：不为 NULL 条件（无需输入值）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoNotNullCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoNotNullCondition()` | 构造 | 符号 `IS NOT NULL`，`requireCondition=false` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `$expr($ne[$toString($_id), null])`；否则 `and(exists, ne(col,null))` |

- 调用链：`MongoConditionUtil.init → MongoNotNullCondition.INSTANCE`

## MongoEmptyCondition
- 职责：为空串条件（无需输入值）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoEmptyCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoEmptyCondition()` | 构造 | 符号 `=''`，`requireCondition=false` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `Filters.eq(col, "")` |

- 调用链：`MongoConditionUtil.init → MongoEmptyCondition.INSTANCE`

## MongoNotEmptyCondition
- 职责：不为空串条件（无需输入值）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoNotEmptyCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoNotEmptyCondition()` | 构造 | 符号 `!=''`，`requireCondition=false` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `$expr($ne[$toString($_id), ""])`；否则 `Filters.ne(col,"")` |

- 调用链：`MongoConditionUtil.init → MongoNotEmptyCondition.INSTANCE`

## MongoInListCondition
- 职责：在列表条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoInListCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoInListCondition()` / `(String, String)` | 构造 | 符号 `IN` |
  | `Bson wrapCondition(String, Object)` | 翻译 | 字符串按逗号切分或直接用 List；`_id` → `$expr($in[$toString($_id), list])`；否则 `and(exists, in)` |

- 调用链：`MongoConditionUtil.init → MongoInListCondition.INSTANCE`

## MongoNotInListCondition
- 职责：不在列表条件，继承 `MongoInListCondition`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | MongoNotInListCondition | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoNotInListCondition()` | 构造 | 符号 `NOT IN` |
  | `Bson wrapCondition(String, Object)` | 翻译 | `_id` → `$expr($not($in[...]))`；否则 `and(exists, nin)` |

- 调用链：`MongoConditionUtil.init → MongoNotInListCondition.INSTANCE`

---

# 六、数据库（cn.oyzh.easyshell.mongo.database）

## MongoDatabase
- 职责：MongoDB 数据库的数据对象。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | String | 数据库名称 |
  | sizeOnDisk | Double | 占用磁盘大小 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getName()/void setName(String)` | 库名存取 | 简单存取 |
  | `Double getSizeOnDisk()/void setSizeOnDisk(Double)` | 大小存取 | 简单存取 |

- 调用链：`ShellMongoClient.listDatabases → new MongoDatabase`

---

# 七、函数（cn.oyzh.easyshell.mongo.function）

## MongoFunction
- 职责：MongoDB 存储函数（`system.js`）对象，实现 `DBRoutineSchema`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | String | 函数名称 |
  | code | String | 函数代码 |
  | dbName | String | 所属数据库 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getName()/setName` | 名称存取 | 简单存取 |
  | `String getCode()/setCode` | 代码存取 | 简单存取 |
  | `String getDbName()/setDbName` | 库名存取 | 简单存取 |
  | `boolean isNew()` | 是否新建 | 代码为空视为新函数 |
  | `void copy(MongoFunction)` | 复制 | 复制 name/code/dbName |
  | `boolean compare(MongoFunction)` | 比较 | name 与 dbName 均相等 |

- 调用链：`ShellMongoClient.listFunctions/selectFunction → new MongoFunction`

---

# 八、记录（cn.oyzh.easyshell.mongo.record）

## MongoRecord
- 职责：MongoDB 文档记录对象，维护字段属性映射、变更追踪与复制/纠正逻辑。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | readonly | boolean | 是否只读（final） |
  | editable | boolean | 是否可编辑 |
  | columns | MongoColumns | 字段列表 |
  | properties | HashMap\<String, MongoRecordProperty\> | 字段名到属性映射 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 四个构造器 | 构造 | 由 `MongoColumns` 或 `List<MongoColumn>` + readonly 构造 |
  | `MongoColumns getColumns()` | 取字段列表 | 简单返回 |
  | `MongoRecordProperty putValue(String, Object)` | 按列名写值 | 无属性时用新 `MongoColumn` 建属性 |
  | `MongoRecordProperty putValue(MongoColumn, Object)` | 按列写值 | 新建 `MongoRecordProperty`、注册 changed 监听、放入 map |
  | `Object getValue(String)` / `Object getOriginal(String)` | 取值/原值 | 委托属性 |
  | `Set<String> columns()` | 全部列名 | `properties.keySet()` |
  | `MongoRecordProperty getProperty(String)` | 取属性 | map 查找 |
  | `boolean hasProperty(MongoRecordProperty)` | 是否含属性 | `containsValue` |
  | `MongoRecordProperty removeProperty(String)` / `void clearProperty()` | 删/清属性 | map 操作 |
  | `void update(Map<String, Object>)` | 批量更新 | 逐 entry `putValue` |
  | `boolean isChanged()` | 是否变更 | 自身或任一属性变更 |
  | `void clearStatus()` | 清状态 | 各属性清变更并更新原始值 |
  | `void discard()` | 抛弃变更 | 各属性 `discard` |
  | `void copy(MongoRecord)` | 复制 | 比对列集合，新增列 `addAll`、缺少列 `removeAll`，再逐列复制值 |
  | `void correctColumns(MongoColumns)` | 纠正字段 | 补齐记录缺失的列（`_id` 除外）并置空值 |
  | `Map<String, Object> toMap()` | 转 Map | 收集属性值 |
  | `void destroy()` | 销毁 | 销毁属性并置空 |
  | `boolean isEditable()/setEditable` | 可编辑标志 | 简单存取 |
  | `void set_id(Object)` | 设置 `_id` | `putValue(ShellMongoUtil.ID, ...)` |
  | `MongoColumn column(String)` / `MongoColumn _idColumn()` | 取列 | 从 columns 按名取 |
  | `Object _idValue()` | 取 `_id` 原值 | 属性原值 |

- 调用链：`ShellMongoClient.toMongoRecord → ShellMongoRecordUtil.docToRecord → MongoRecord.putValue`

## MongoRecordFilter
- 职责：界面记录过滤条件行，管理列/条件/值三类控件并产出 Bson 条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | condition | MongoCondition | 当前条件 |
  | valueBox | FXHBox | 值输入组件容器 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `Object value()` | 取值 | 从 `valueBox` 子控件经 `MongoConditionUtil.getNodeVal` 取 |
  | `Node getValueControl()` | 取值组件 | 刷新后返回 `valueBox` |
  | `void updateValueControl()` | 刷新值控件 | `MongoConditionUtil.generateNode` 生成并按数量设置 flex 宽度 |
  | `DBColumnComboBox getColumnControl()` | 字段下拉框 | 选中变化更新 `column` 并刷新值控件 |
  | `DBConditionComboBox getConditionControl()` | 条件下拉框 | 从 `DBConditionManager` 取 MONGODB 条件，变化刷新值控件 |
  | `String column()` | 字段名 | `column.getName()` |
  | `Bson condition()` | 生成条件 | `this.condition.wrapCondition(column(), value())` |
  | `boolean isRequireCondition()` | 是否需值 | 委托条件 |
  | `getCondition()/setCondition(...)` | 条件存取 | 简单存取（文件末尾类体闭合花括号缩进异常，但括号配平） |

- 调用链：`MongoConditionUtil.buildCondition → MongoRecordFilter.condition → MongoCondition.wrapCondition`

## MongoRecordProperty
- 职责：记录中单元格属性，绑定字段类型与编辑控件节点，负责取值/设值/节点刷新与脚本复制。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | column | MongoColumn | 关联字段 |
  | record | MongoRecord | 关联记录 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoRecordProperty(MongoRecord, MongoColumn, Object, boolean)` | 构造 | `_id` 走 `ShellMongoRecordUtil.idValue`；按 readonly 记录 original |
  | `Object get()` | 取值 | 非只读且已变更时从编辑节点取值（`setToNullFlag` 返回 null） |
  | `void set(Object)` | 设值 | 计算类型差异，必要时 `column.setType` + `refreshNode`；更新节点值 |
  | `Object getValue()` | 取显示值 | 只读时格式化；否则懒加载节点后返回节点 |
  | `void refreshNode()/void initNode()` | 节点刷新 | 置空后重建并绑定 Ctrl+S/鼠标点击 |
  | `void discard()` | 抛弃变更 | 变更时用原值回写节点 |
  | `void setChanged(boolean)` | 设变更 | 通知 `DBStatusListenerManager` 监听器；二进制节点重格式化 |
  | `void updateOriginal()` | 更新原值 | 由节点值回写并更新 original |
  | `void vCopyAsInsertSql()` | 复制插入语句 | `ShellMongoDataUtil.toInsertScript` → 剪贴板 |
  | `void vCopyAsUpdateSql()` | 复制更新语句 | `ShellMongoDataUtil.toUpdateScript` → 剪贴板 |
  | `getColumn()/setColumn(...)` | 字段存取 | 简单存取 |
  | `void destroy()` | 销毁 | 解绑字段与记录 |

- 调用链：`MongoRecord.putValue → new MongoRecordProperty → ShellMongoRecordUtil.getNode/ShellMongoNodeUtil`

## MongoSelectRecordParam
- 职责：集合/桶记录查询参数对象（分页、库表、字段、过滤条件）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | start | Long | 起始行 |
  | limit | Long | 数量上限 |
  | dbName | String | 数据库名 |
  | collectionName | String | 集合/桶名 |
  | readonly | boolean | 是否只读 |
  | columns | List\<MongoColumn\> | 字段列表 |
  | filters | List\<MongoRecordFilter\> | 过滤条件 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 两个构造器 | 构造 | 空构造 / (dbName, collectionName) |
  | `boolean hasPageControl()` | 是否有分页 | start 与 limit 均非空 |
  | 各 `getXxx/setXxx` | 属性存取 | 简单存取 |

- 调用链：`界面 → new MongoSelectRecordParam → ShellMongoClient.selectCollectionRecords/selectBucketRecords`

---

# 九、脚本引擎（cn.oyzh.easyshell.mongo.script）

## MongoScriptEngine
- 职责：基于 Nashorn 的 MongoDB Shell 脚本引擎，注入 db 对象与内建构造函数。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | mongoClient | MongoClient | 驱动客户端（final） |
  | engine | ScriptEngine | Nashorn 引擎 |
  | bindings | Bindings | 引擎作用域绑定 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoScriptEngine(MongoClient)` | 构造 | 保存客户端并 `initEngine` |
  | `void initEngine()` | 初始化引擎 | `NashornScriptEngineFactory.getScriptEngine("--language=es6","-scripting")`，注入 `Code/Long/Int32/Binary/ISODate/ObjectId` |
  | `ScriptEngine getEngine()` | 取引擎 | 简单返回 |
  | `void db(String)` | 切换库 | 绑定 `db=MongoScriptDatabase`，`engine.put("dbName")` |
  | `Object eval(String)` | 执行脚本 | `engine.eval` |
  | `void put(String, Object)` | 注入变量 | `engine.put` |

- 调用链：`ShellMongoClient.shellEngine().db().eval → MongoScriptEngine → Nashorn → MongoScriptDatabase/Collection`

## MongoScriptCursor
- 职责：脚本层游标包装，提供美观输出与数组化。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | cursor | MongoIterable\<?\> | 底层可迭代游标（final） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoScriptCursor(MongoIterable<?>)` | 构造 | 保存游标 |
  | `String pretty()` | 美化输出 | 收集元素 → `JSONUtil.toPretty` |
  | `List<?> toArray()` | 转数组 | 遍历游标收集为 List |
  | `String toString()` | 字符串 | 返回 `pretty()` |

- 调用链：`MongoScriptDatabase/Collection.xxx → new MongoScriptCursor`

## MongoScriptFindCursor
- 职责：`find` 结果游标，继承 `MongoScriptCursor`，携带库/集合名并支持 limit/skip/explain。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbName | String | 库名（final） |
  | collectionName | String | 集合名（final） |
  | cursor | FindIterable\<Document\> | 底层 find 游标（final） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoScriptFindCursor(String, String, FindIterable<Document>)` | 构造 | 保存库/集合与游标 |
  | `String getDbName()/getCollectionName()` | 名字存取 | 简单返回 |
  | `MongoScriptFindCursor limit(int)` | 限制数量 | 包装 `cursor.limit(n)` |
  | `MongoScriptFindCursor skip(int)` | 跳过 | 包装 `cursor.skip(n)` |
  | `Document explain()` | 执行计划 | `cursor.explain()` |

- 调用链：`MongoScriptCollection.find → MongoScriptFindCursor`

## MongoScriptParser
- 职责：脚本分段解析器，按 `db.` 起、`;` 止切分为多条语句。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | scriptContent | String | 脚本内容（final） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoScriptParser(String)` | 构造 | 保存脚本 |
  | `String removeComment()` | 去注释 | `ShellMongoUtil.removeComment` |
  | `List<String> parseScript()` | 分段 | `show dbs/collections` 直接整段返回；否则逐行，遇 `db.` 起、行尾 `;` 止，切分语句 |
  | `static MongoScriptParser getParser(String)` | 工厂 | `new MongoScriptParser` |

- 调用链：`ShellMongoClient.executeScript → MongoScriptParser.getParser().parseScript`

## MongoScriptDatabase
- 职责：脚本层库对象，包装 `MongoDatabase` 暴露集合获取、建库、聚合、视图、用户等能力供 JS 调用。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | database | MongoDatabase | 驱动库对象（final） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoScriptDatabase(MongoDatabase)` | 构造 | 保存库 |
  | `String getName()` | 库名 | `database.getName()` |
  | `MongoScriptCollection getCollection(String)` | 取集合 | 包装为 `MongoScriptCollection` |
  | `MongoScriptDatabase createCollection(String)` / `(String, Object)` | 建集合 | 第二版解析 capped/sizeInBytes/maxDocuments 选项 |
  | `void drop()` | 删库 | `database.drop()` |
  | `Document runCommand(Object)` | 执行命令 | Map → `new Document(map)` → `runCommand` |
  | `void createView(String, String, Object)` / `(..., Object)` | 建视图 | `MongoScriptUtil.toDocumentList` 解析 pipeline |
  | `MongoScriptCursor aggregate(Object)` | 聚合 | 允许磁盘使用后返回游标 |
  | `MongoScriptCursor watch()` / `watch(Object)` | 变更流 | 返回 `MongoScriptCursor` |
  | `MongoScriptCursor listCollectionNames()` / `listCollections()` | 列举集合 | 返回游标 |
  | `Document getUsers()/getUser(String)/dropUser(String)/createUser(...)` | 用户管理 | 委托 `ShellMongoUserUtil` |

- 调用链：`MongoScriptEngine.db() → MongoScriptDatabase → MongoScriptCollection / ShellMongoUserUtil`

## MongoScriptCollection
- 职责：脚本层集合对象，包装驱动 `MongoCollection`，暴露增删改查、聚合、索引、重命名等 Mongo Shell 方法。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbName | String | 库名（final，取自命名空间） |
  | collectionName | String | 集合名（final，取自命名空间） |
  | collection | MongoCollection\<Document\> | 驱动集合（final） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoScriptCollection(MongoCollection<Document>)` | 构造 | 由命名空间解析库/集合名 |
  | `MongoScriptFindCursor find()` / `find(Object)` | 查询 | 转换 filter → `find` → `MongoScriptFindCursor` |
  | `Object insert(Object)` | 插入（自动单/多） | 数组或 Collection 走 `insertMany`，否则 `insertOne` |
  | `InsertOneResult insertOne(Object)` / `InsertManyResult insertMany(Object)` | 插入 | `MongoScriptUtil.toDocument` 转换后写库 |
  | `DeleteResult delete(Object)` / `deleteOne(Object)` / `deleteMany()` / `deleteMany(Object)` | 删除 | 默认 filter 为空 Document |
  | `UpdateResult update(Object, Object)` / `updateOne(...)` / `updateMany(...)` | 更新 | filter/update 均转 Document |
  | `void drop()` | 删集合 | `collection.drop()` |
  | `Document findOne()` / `findOne(Object)` | 查单条 | `find().first()` |
  | `Document findOneAndDelete(Object)` / `findOneAndReplace(...)` / `findOneAndUpdate(...)` | 原子操作 | 转换后调用驱动同名方法 |
  | `UpdateResult replaceOne(Object, Object)` / `(..., Object)` | 替换 | 带选项版用 `JSONUtil.toBean(ReplaceOptions)` |
  | `long countDocuments()` / `countDocuments(Object)` | 计数 | 空 filter 计数 |
  | `long estimatedDocumentCount()` | 估算计数 | `estimatedDocumentCount` |
  | `MongoScriptCursor distinct(String)` / `(String, Object)` | 去重 | `distinct(field, String.class)` 包装游标 |
  | `MongoScriptCursor aggregate(Object)` | 聚合 | 允许磁盘使用 |
  | `String createIndex(Object)` / `(Object, Object)` | 建索引 | 解析 name/unique/background/sparse/expireAfterSeconds |
  | `MongoScriptCursor listIndexes()` / `dropIndex(Object)` / `dropIndexByName(String)` / `dropIndexes()` | 索引管理 | 包装驱动方法 |
  | `void rename(String)` | 重命名 | `MongoNamespace` + `renameCollection` |

- 调用链：`MongoScriptDatabase.getCollection → MongoScriptCollection → com.mongodb.client.MongoCollection`

## MongoScriptUtil
- 职责：脚本层类型转换工具，负责 JS 对象/Mirror 与 Bson Document 互转及反射收集可用函数名。
- 字段：无（纯静态工具类）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static Set<String> databaseFuncions()` | 库函数名 | `functions(MongoScriptDatabase.class)` |
  | `static Set<String> collectionFuncions()` | 集合函数名 | `functions(MongoScriptCollection.class)` |
  | `static Set<String> functions(Class<?>)` | 反射收集方法名 | 过滤 static/native/protected/private 与 Object 方法 |
  | `static List<Document> toDocumentList(Object)` | 转 Document 列表 | 支持 `ScriptObjectMirror`/`Collection` |
  | `static Document toDocument(Object)` | 转 Document | Mirror 数组返回 null，Map 递归转换 |
  | `static Document toDocumentOrDefault(Object)` | 带默认的转换 | 转换失败返回空 Document |
  | `static Object convertValue(Object)` | 递归转换值 | Mirror 数组→List、对象→Document、Collection→List |
  | `static Document toDocument(Map<String, Object>)` | Map 转 Document | 逐 entry 递归 `convertValue` |

- 调用链：`MongoScriptCollection/Database 各方法 → MongoScriptUtil.toDocument*`

---

# 十、脚本内建函数（cn.oyzh.easyshell.mongo.script.function）

> 说明：本组是注入 Nashorn 引擎的内建构造函数，模拟 Mongo Shell 的全局函数。除 `MongoScriptBinaryFcuntion` 外均继承 `AbstractJSObject` 并重写 `call`。

## MongoScriptBinaryFcuntion
- 职责：模拟 `Binary` 构造函数，从 Base64 生成 BSON 二进制。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `Binary createFromBase64()` | 默认构造 | 等价 `createFromBase64("", 0)` |
  | `Binary createFromBase64(String, int)` | 构造 | `Base64Util.decode` → `new Binary((byte) type, data)` |

- 调用链：`MongoScriptEngine.initEngine 注入 Binary → MongoScriptBinaryFcuntion`

## MongoScriptCodeFunction
- 职责：模拟 `Code` 构造函数。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isFunction()` | 标记为函数 | 返回 true |
  | `Object call(Object, Object...)` | 调用 | 无参/undefined 返回 `new Date()`；否则 `new Code(args[0].toString())` |

- 调用链：`MongoScriptEngine.initEngine 注入 Code → MongoScriptCodeFunction.call`

## MongoScriptISODateFunction
- 职责：模拟 `ISODate` 构造函数。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isFunction()` | 标记为函数 | 返回 true |
  | `Object call(Object, Object...)` | 调用 | 无参返回当前时间；有参按 `ShellMongoUtil.DATE_FORMAT` 解析，失败回退当前时间 |

- 调用链：`MongoScriptEngine.initEngine 注入 ISODate → MongoScriptISODateFunction.call`

## MongoScriptInit32Function
- 职责：模拟 `Int32` 构造函数（注意类名拼写为 `Init32`）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isFunction()` | 标记为函数 | 返回 true |
  | `Object call(Object, Object...)` | 调用 | 无参返回 0；Number 取 intValue，否则 `Integer.parseInt` |

- 调用链：`MongoScriptEngine.initEngine 注入 Int32 → MongoScriptInit32Function.call`

## MongoScriptLongFunction
- 职责：模拟 `Long` 构造函数。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isFunction()` | 标记为函数 | 返回 true |
  | `Object call(Object, Object...)` | 调用 | 无参返回 0L；Number 取 longValue，否则 `Long.parseLong` |

- 调用链：`MongoScriptEngine.initEngine 注入 Long → MongoScriptLongFunction.call`

## MongoScriptObjectIdFunction
- 职责：模拟 `ObjectId` 构造函数。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isFunction()` | 标记为函数 | 返回 true |
  | `Object call(Object, Object...)` | 调用 | 无参返回新 `ObjectId`；入参为字符串（CharSequence）则按十六进制构造，否则新 `ObjectId` |

- 调用链：`MongoScriptEngine.initEngine 注入 ObjectId → MongoScriptObjectIdFunction.call`

---

# 十一、用户（cn.oyzh.easyshell.mongo.user）

## MongoUser
- 职责：MongoDB 用户数据对象（库、用户名、密码、角色）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | db | String | 所属库 |
  | user | String | 用户名 |
  | password | String | 密码 |
  | roles | List\<MongoUserRole\> | 角色列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 各 `getXxx/setXxx` | 属性存取 | 简单存取 |
  | `void copy(MongoUser)` | 复制 | 复制 roles/password |
  | `boolean compare(MongoUser)` | 比较 | db 与 user 均相等 |

- 调用链：`ShellMongoClient.listUsers → new MongoUser`

## MongoUserRole
- 职责：用户角色数据对象（角色所属库 + 角色名）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | db | String | 角色所属库 |
  | role | String | 角色名 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getDb()/setDb` | 库存取 | 简单存取 |
  | `String getRole()/setRole` | 角色存取 | 简单存取 |

- 调用链：`ShellMongoClient.listUsers → new MongoUserRole`

## MongoUserRoleDb
- 职责：按库组织用户角色集合，并生成角色勾选控件（用于用户编辑界面）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | db | String | 库名 |
  | roles | Set\<String\> | 已选角色集合 |
  | rolesControl | FXHBox | 角色勾选控件（懒加载） |
  | ROLES_MAIGIN | Insets | 复选框外边距常量 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getDb()/setDb` | 库名存取 | 简单存取 |
  | `Set<String> getRoles()/setRoles` | 角色集存取 | 简单存取 |
  | `Set<String> roles()` | 懒加载角色集 | 为空时初始化 HashSet |
  | `boolean isEmpty()` | 是否无角色 | `CollectionUtil.isEmpty` |
  | `FXHBox getRolesControl()` | 生成勾选控件 | 遍历 `ShellMongoHelper.ROLES` 建复选，勾选变化增删角色 |
  | `String getRolesText()` | 角色文本 | `StringUtil.join(",", roles)` |

- 调用链：`用户编辑界面 → MongoUserRoleDb.getRolesControl → ShellMongoHelper.ROLES`

---

# 附：整文件被注释掉的死代码（跳过，未展开）

经逐一文件完整通读，本目录下 52 个 `.java` 文件均含可执行正式代码，**无整文件被注释掉的死代码**，故本清单为空。

（个别文件内部含注释掉的历史方法，例如 `ShellMongoHelper.initProxySocketFactory`、`ShellMongoClient` 中多处旧代理/桶记录方法、`MongoScriptParser` 中旧解析方法，但这些文件本身有正式代码，已按正式类展开。）

---

# 统计

- 目录文件总数：52（全部为正式类）
- 死代码文件：0
- 本次覆盖正式类：52
  - 客户端入口：3
  - 桶 bucket：2
  - 集合 collection：1
  - 字段 column：2
  - 条件 condition：22
  - 数据库 database：1
  - 函数 function：1
  - 记录 record：4
  - 脚本引擎 script：7
  - 脚本内建函数 script/function：6
  - 用户 user：3
