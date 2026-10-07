# 三、util 包代码审查（下）：mysql / redis / zk

本片段覆盖 `cn.oyzh.easyshell.util` 下的 `mysql`、`redis`、`zk` 三个子包，共 24 个 `.java` 文件，其中正式类 22 个、整文件死代码 2 个（清单见文末）。util 根包、dameng、db、mongo 由其他片段负责，此处不涉及。

## 3.1 mysql

### ShellMysqlColumnUtil

- 职责：初始化并注册 MySQL 各字段类型（`DBColumnField`）的元数据定义，并提供类型判断与默认值生成等工具方法。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 全为静态方法，无实例字段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init()` | 构建并注册全部 MySQL 字段类型定义 | 逐个 `new DBColumnField(类型名)` 并设置 `suggestSize/supportXxx` 等开关，最后分类调用 `putFiled` 注册；覆盖字符/数字/时间/文本/特殊/集合/二进制/空间八大类 |
  | `void putFiled(DBColumnField)` | private，注册单个字段定义 | `DBColumnFieldManager.putFiled(DBDialect.MYSQL, columnField)` |
  | `boolean isYearType(String)` 等 12 个 `isXxxType` | 判断字段类型字符串是否为某几何/时间类型 | 统一 `"XXX".equalsIgnoreCase(type)`；注意几何类型仅 `Polygon/MultiPolygon` 用纯大写，其余如 `Point/LineString/Geometry` 使用首字母大写的字符串比较 |
  | `Object defaultValue(String)` | 按类型返回默认值 | 先 `DBColumnFieldManager.supportDefaultValue(MYSQL,type)`，再依次判 `supportDigits→0.0`、`supportInteger→0`、`supportString→""`、`supportJson→"{'a':1}"`、`supportBinary→new byte[]{}`，否则 `null` |

- 调用链：`init → putFiled → DBColumnFieldManager.putFiled`；`defaultValue → DBColumnFieldManager.supportDefaultValue/supportDigits/supportInteger/supportString/supportJson/supportBinary`

### ShellMysqlDataUtil

- 职责：把 `MysqlRecord` 转换为插入 / 更新 SQL 语句。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 静态工具类，另有 6 组 `parameterizedForXxx` 方法整块被注释 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String toInsertSql(MysqlColumns, MysqlRecord, boolean)` | 单条记录转插入 SQL | 委托三参批量重载后取第一个 |
  | `List<String> toInsertSql(MysqlColumns, List<MysqlRecord>)` | 批量插入 SQL（不含字段名） | 委托三参重载，`includeFields=false` |
  | `List<String> toInsertSql(MysqlColumns, List<MysqlRecord>, boolean)` | 批量插入 SQL 核心实现 | 字段用 `sortOfPosition()` 排序，拼接 `INSERT INTO <表> (列...) VALUES (...)`；值经 `DBDataUtil.parameterizedForSql(col,val,MYSQL)`；末尾统一去除多余 `", "` |
  | `String toUpdateSql(MysqlColumns, MysqlRecord)` | 单条记录转更新 SQL | 先 `ShellMysqlUtil.initPrimaryKey`；有主键则 `WHERE 主键=值`，无主键则全字段 `AND` 连接并附加 `LIMIT 1`；几何类型用 `ST_GeomFromText(...)` 包装 |
  | 6 个 `parameterizedForXxx`（整块注释） | 已废弃的参数化实现 | 死代码，属类内注释，不展开 |

  > 审查提示：`toUpdateSql` 中 SET 子句的参数化调用误用了 `DBDialect.DAMENG`（第 456 行），与同方法内其它调用使用的 `MYSQL` 不一致，疑似方言串用缺陷；另 `builder.deleteCharAt(builder.length()-2)` 在无任何 SET 字段时会越界。

- 调用链：`toInsertSql → DBDataUtil.parameterizedForSql`；`toUpdateSql → ShellMysqlUtil.initPrimaryKey → DBDataUtil.parameterizedForSql`

### ShellMysqlNodeUtil

- 职责：生成 MySQL 字段对应的 JavaFX 录入节点，以及节点值的读写与附加处理（小数位、注释、默认值、示例值）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 静态工具类 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `Object getNodeVal(Node)` | 读取节点值 | 直接委托 `DBNodeUtil.getNodeVal`（原内联实现已注释） |
  | `void setNodeVal(Node, Object)` | 设置节点值 | 直接委托 `DBNodeUtil.setNodeVal` |
  | `Node generateNode(MysqlColumn)` | 生成录入节点（带默认值） | 委托 `generateNode(column, true)` |
  | `Node generateNode(MysqlColumn, boolean)` | 生成录入节点核心 | 枚举类型用 `SelectTextFiled` 并设 `getValueList()`，其它委托 `DBNodeUtil.generateNode`；随后设 `id="value"`，并调用 `handlerDigits/handlerComment/handlerDefaultValue` |
  | `List<FXLabel> generateTags(MysqlColumn)` | 生成字段标签（nullable/自增/时间戳/主键/无符号/零填充） | 依次 `new FXLabel(I18nHelper.xxx())` 并 `addClass("tag_xxx")` |
  | `void handlerDigits(Node, Integer)` | 设置小数位 | 节点为 `DecimalTextField` 且位数 >0 时 `setScaleLen` |
  | `void handlerComment(Node, String)` | 把注释设为提示文本 | `TextInputControl.setPromptText(comment)` |
  | `void handlerDefaultValue(Node, Object)` | 回填默认值 | 分 `DigitalTextField/ComboBox/TextInputControl` 三支处理 |
  | `void handlerExampleValue(Node, Object)` | 回填示例值 | 分 `DigitalTextField/ChooseFileTextField/TextInputControl` 三支处理 |

- 调用链：`generateNode → DBNodeUtil.generateNode / SelectTextFiled`；`generateTags → I18nHelper.*`

### ShellMysqlRecordUtil

- 职责：为记录单元格构造编辑/展示节点、按列类型格式化值、提供单元格右键菜单。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 静态工具类；`getNode` 内原大段内联实现已注释 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `Node getNode(MysqlRecordProperty, Object, MysqlColumn)` | 构造单元格节点 | 枚举用 `SelectTextFiled`（只读、设值、定制背景），其它委托 `DBNodeUtil.getNode`；对 `FXTextField` 补 null 提示文本、背景色、右键菜单、文本变更监听 `property.setChanged(true)` |
  | `String formatValue(Object, MysqlColumn)` | 按列类型格式化值 | 空类型按 CharSequence/byte[]/Date 兜底；否则依次判 `supportJson/Binary/Enum/Integer/Digits/Bit`、`isDate/Time/Year`、`supportTimestamp||isDateTime`、`supportText`、`supportGeometry`，分派到对应 `XxxTextField.format` |
  | `List<FXMenuItem> getColumnMenuItem(MysqlRecordProperty)` | 生成单元格右键菜单项 | 复制、粘贴、置空、置空字符串、复制为插入语句、复制为更新语句，均由 `MenuItemHelper.xxx_no_graphic(property::vXxx)` 构造 |

- 调用链：`getNode → DBNodeUtil.getNode → getColumnMenuItem → MenuItemHelper.*`；`formatValue → JsonTextFiled/BinaryTextFiled/NumberTextField/...`

### ShellMysqlUtil

- 职责：MySQL 元数据 `ResultSet` 行的过滤判断与记录主键初始化。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 静态工具类；多个打印/包装/常量方法已注释 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isInternalDatabase(String)` | 判断是否 MySQL 内部库 | `StringUtil.equalsAnyIgnoreCase(dbName,"mysql","information_schema","performance_schema")` |
  | `boolean checkTableType(ResultSet, String)` | 判断行是否为指定库的表 | `TABLE_CAT==dbName` 且 `TABLE_TYPE!=VIEW` |
  | `boolean checkViewType(ResultSet, String)` | 判断行是否为指定库的视图 | `TABLE_CAT==dbName` 且 `TABLE_TYPE==VIEW` |
  | `boolean checkTableCat(ResultSet, String, String)` | 判断库名+表名是否匹配 | `Objects.equals` 双字段比较 |
  | `boolean checkProcedureType(ResultSet, String)` | @Deprecated，判断存储过程行 | `PROCEDURE_CAT==dbName` 且 `PROCEDURE_TYPE=="1"` |
  | `boolean checkFunctionType(ResultSet, String)` | @Deprecated，判断函数行 | `FUNCTION_CAT==dbName` 且 `FUNCTION_TYPE=="1"` |
  | `boolean checkFunctionType(ResultSet, String, String)` | 判断函数行（带 schema） | `FUNCTION_CAT==dbName`；schema 为 null 直接 true，否则比较 `FUNCTION_SCHEM` |
  | `MysqlRecordPrimaryKey initPrimaryKey(MysqlColumns, MysqlRecord)` | 初始化记录主键 | 取 `columns.primaryKeys()` 首个列，构造 `MysqlRecordPrimaryKey` 并 `init(column,record)`，无主键返回 null |

- 调用链：`initPrimaryKey → MysqlRecordPrimaryKey.init`

### ShellMysqlViewFactory

- 职责：MySQL 各类功能窗口 / 对话框的统一创建入口。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 全部方法通过 `StageManager` 拉起对应 Controller |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void exportData(ShellMysqlClient, String, String)` | 导出数据（简版） | 委托五参重载，`exportMode=0`、`exportTable=null` |
  | `void exportData(ShellMysqlClient, String, String, int, ShellMysqlDataExportTable)` | 导出数据核心 | `parseStage(ShellMysqlDataExportController)`，setProp(dbName/dbClient/tableName/exportMode/exportTable)，`display()` |
  | `void importData(ShellMysqlClient, String)` | 导入数据 | `ShellMysqlDataImportController` |
  | `void dumpData(ShellMysqlClient, String, String, int)` | 转储数据 | `ShellMysqlDataDumpController`，setProp(dumpType/dbName/dbClient/tableName) |
  | `void runSqlFile(ShellMysqlClient, String)` | 运行 SQL 文件 | `ShellMysqlDataRunSqlFileController` |
  | `void databaseUpdate(MysqlDatabase, ShellMysqlRootTreeItem)` | 编辑数据库 | `ShellMysqlDatabaseUpdateController`（基于 `getFrontWindow()`） |
  | `void transportData(ShellConnect, String)` | 传输数据 | `ShellMysqlDataTransportController` |
  | `StageAdapter addDatabase(ShellMysqlRootTreeItem)` | 添加数据库（模态） | `ShellMysqlDatabaseAddController`，`showAndWait()` 并返回适配器 |
  | `void tableInfo(ShellMysqlTableTreeItem)` | 表信息 | `ShellMysqlTableInfoController` |
  | `void viewInfo(ShellMysqlViewTreeItem)` | 视图信息 | `ShellMysqlViewInfoController` |
  | `void functionInfo(ShellMysqlFunctionTreeItem)` | 函数信息 | `ShellMysqlFunctionInfoController` |
  | `void procedureInfo(ShellMysqlProcedureTreeItem)` | 过程信息 | `ShellMysqlProcedureInfoController` |
  | `void eventInfo(ShellMysqlEventTreeItem)` | 事件信息 | `ShellMysqlEventInfoController` |

- 调用链：`Xxx 方法 → StageManager.parseStage(XxxController) → adapter.setProp(...) → adapter.display()/showAndWait()`

## 3.2 redis

### ShellRedisCacheUtil

- 职责：把 Redis 键值缓存到本地文件（带 1 字节类型标记）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 静态工具类 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String baseDir(int)` | private，拼缓存路径 | `ShellConst.getKeyCachePath() + hashCode` |
  | `boolean cacheValue(int, Object, String)` | 缓存值 | String 记 type=1、byte[] 记 type=2；`bytes+type` 合并后 `FileUtil.writeBytes`；value 为 null 时转 `deleteValue` |
  | `Object loadValue(int, String)` | 读取缓存 | 读文件取末字节 type，type==1 还原 `String`、type==2 还原 `byte[]` |
  | `boolean deleteValue(int, String)` | 删除缓存 | 拼文件名后 `FileUtil.del` |
  | `boolean hasValue(int, String)` | 是否存在缓存 | `FileUtil.exists` |

- 调用链：`cacheValue → FileUtil.touch/writeBytes → deleteValue`；`loadValue → FileUtil.readBytes`；`hasValue/deleteValue → FileUtil.exists/del`

### ShellRedisCommand

- 职责：Redis 命令元数据 POJO。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `desc` | `String` | 命令描述 |
  | `args` | `String` | 参数说明 |
  | `command` | `String` | 命令名 |
  | `available` | `String` | 起始可用版本 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getXxx/setXxx` 8 个 | 各字段标准访问器 | 无附加逻辑 |

- 调用链：`ShellRedisCommandUtil 反序列化 → setter`

### ShellRedisCommandUtil

- 职责：加载并提供 Redis 命令元数据（来自类路径 `redis_commands.json`）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `COMMANDS` | `static final List<ShellRedisCommand>` | 命令缓存列表，静态块初始化 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 静态块 | 加载命令清单 | `ShellRedisCommand.class.getResource("/redis_commands.json")` → `FileUtil.readString` → `JSONUtil.toList(json, ShellRedisCommand.class)` 填入 `COMMANDS` |
  | `List<ShellRedisCommand> getCommands()` | 取全部命令 | 直接返回 `COMMANDS` |
  | `ShellRedisCommand getCommand(String)` | 按命令名查找 | 遍历 `COMMANDS`，`equalsIgnoreCase` 匹配 `getCommand()` |
  | `String getCommandDesc(String)` | 取命令描述 | `getCommand` 后取 `desc`，空补 `""` |
  | `String getCommandArgs(String)` | 取命令参数说明 | `getCommand` 后取 `args`，空补 `""` |
  | `String getCommandAvailable(String)` | 取命令可用版本 | `getCommand` 后取 `available`，空补 `""` |

- 调用链：`static 初始化 → FileUtil.readString → JSONUtil.toList`；`getCommandDesc/Args/Available → getCommand`

### ShellRedisVersionUtil

- 职责：按 `x.y.z` 版本号判断 Redis 命令在服务端版本是否可用。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 静态工具类 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getSupportedVersion(String)` | 取命令所需最低版本 | `ShellRedisCommandUtil.getCommand`，无匹配默认 `"1.0.0"` |
  | `void checkSupported(String, String)` | 校验，不支持则抛异常 | `isSupported` 为 false 时抛 `ShellRedisUnsupportedCommandException` |
  | `boolean isCommandSupported(String, String)` | 判断命令是否支持 | `getSupportedVersion` 后 `isSupported` |
  | `boolean isSupported(String, String)` | 版本比较核心 | 按 `.` 拆分，逐段 `Double.parseDouble` 比较大/小/等三段；任一方为 null 返回 false |

  > 审查提示：`isSupported` 中判断条件 `str2.length != 3 && str1.length != str2.length` 逻辑可疑（应为长度不等即返回 false），且首段为大版本时未做“大于则直接 true、小于则直接 false、相等才继续”的短路，存在误判风险。

- 调用链：`checkSupported → getSupportedVersion → ShellRedisCommandUtil.getCommand → isSupported → 抛 ShellRedisUnsupportedCommandException`

### ShellRedisViewFactory

- 职责：Redis 各功能窗口 / 对话框的统一创建入口（键的增删改、数据导入导出与传输、各类元素添加）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 静态入口类 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `StageAdapter addRedisKey(ShellRedisClient, Integer, ShellRedisKeyType)` | 添加键 | `ShellRedisKeyAddController`，`showAndWait()` |
  | `void redisBatchOperation(ShellRedisClient, Integer)` | 批量操作 | `ShellRedisKeyBatchOperationController` |
  | `StageAdapter redisMoveKey(ShellRedisKeyTreeItem)` | 移动键 | `ShellRedisKeyMoveController` |
  | `StageAdapter redisCopyKey(ShellRedisKeyTreeItem)` | 复制键 | `ShellRedisKeyCopyController` |
  | `void redisTtlKey(ShellRedisKeyTreeItem)` | 设置键 TTL | `ShellRedisKeyTTLController` |
  | `StageAdapter redisZSetCoordinateAdd(ShellRedisZSetKeyTreeItem)` | 添加 zset 坐标 | `ShellRedisZSetCoordinateAddController` |
  | `StageAdapter redisZSetMemberAdd(ShellRedisZSetKeyTreeItem)` | 添加 zset 成员 | `ShellRedisZSetMemberAddController` |
  | `StageAdapter redisSetMemberAdd(ShellRedisSetKeyTreeItem)` | 添加 set 成员 | `ShellRedisSetMemberAddController` |
  | `StageAdapter redisHashFieldAdd(ShellRedisHashKeyTreeItem)` | 添加 hash 字段 | `ShellRedisHashFieldAddController` |
  | `StageAdapter redisListElementAdd(ShellRedisListKeyTreeItem)` | 添加 list 元素 | `ShellRedisListElementAddController` |
  | `StageAdapter redisStreamMessageAdd(ShellRedisStreamKeyTreeItem)` | 添加 stream 消息 | `ShellRedisStreamMessageAddController` |
  | `StageAdapter redisHylogElementsAdd(ShellRedisStringKeyTreeItem)` | 添加 hylog 元素 | `ShellRedisHylogElementsAddController` |
  | `void redisImportData(ShellConnect, Integer)` | 导入数据 | `ShellRedisImportDataController` |
  | `void redisExportData(ShellConnect, Integer)` | 导出数据 | `ShellRedisExportDataController` |
  | `void redisTransportData(ShellConnect, Integer)` | 传输数据 | `ShellRedisTransportDataController` |

- 调用链：`Xxx 方法 → StageManager.parseStage(XxxController) → adapter.setProp(...) → adapter.showAndWait()/display()`

## 3.3 zk

### ShellZKACLUtil

- 职责：ZooKeeper ACL 的解析、权限位（int）与权限字符串（rwcda）互转、友好展示信息构造及 IP 校验。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `OPEN_ACL` | `static ACL` | 开放 ACL，取自 `ZooDefs.Ids.OPEN_ACL_UNSAFE.getFirst()` |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isOpenACL(ACL)` | 判断是否开放 ACL | 比较 `perms` 与 `Id`（先引用相等，再比较 `getId().getId()` 与 `getScheme()`） |
  | `FriendlyInfo<ACL> parseId(Id)` | 解析 Id 为友好信息 | 按当前 Locale 设 friendlyName/Value；scheme 为 WORLD 时值显示“任何人/anyone” |
  | `FriendlyInfo<ACL> parseScheme(String)` | 解析认证方式 | switch：IP/WORLD/DIGEST，未知用 `I18nHelper.unknown()` |
  | `FriendlyInfo<ACL> parsePerms(int)` | 解析权限位为友好信息 | `toPermStr(perms)` 后逐字符 a/w/r/d/c 拼接并映射 i18n 文案 |
  | `List<ACL> parseAcl(String)` | 解析 ACL 字符串为 `List<ACL>` | 空则返回 `OPEN_ACL_UNSAFE`；按 `,` 拆项、`:` 拆段；digest scheme 特殊处理（`user:pwd:perms`） |
  | `int toPermInt(String)` | 权限字符串转 int | 逐字符累加 `ZooDefs.Perms.ADMIN/READ/WRITE/CREATE/DELETE`，非法字符抛 `ShellException` |
  | `String toPermStr(int)` | 权限 int 转字符串 | 委托 `toPermStr(permInt, null)` |
  | `String toPermStr(int, String)` | 权限 int 转字符串（可拼接分隔符） | `NumberUtil.getBinaryStr` 转二进制左补 0 到 5 位，按位映射 a/d/c/w/r |
  | `void checkIP(String)` | 校验 ACL IP 合法性 | 按 `/` 拆段，`RegexUtil.isIPV4` 校验首段；有掩码时要求首段以 0 结尾 |
  | `boolean existDigest(List<ShellZKACL>, String)` | 是否存在指定 digest 用户 | 遍历 `acl.isDigestACL()` 且用户名 `equalsIgnoreCase` |
  | `String toAclStr(List<? extends ACL>)` | ACL 列表转字符串 | 每项拼 `scheme:id:perms`，逗号连接 |

- 调用链：`parseAcl → toPermInt`；`toAclStr → toPermStr`；`parsePerms → toPermStr → I18nHelper.*`

### ShellZKAuthUtil

- 职责：ZooKeeper 节点 digest 认证（构造 AuthInfo、执行认证并回填节点）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 原 `AUTHED_INFOS` 已注释为死字段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `List<AuthInfo> toAuthInfo(List<? extends ShellZKAuth>)` | 构造认证信息列表 | 每个 auth 生成 `new AuthInfo("digest", (user+":"+password).getBytes())`，空则 `emptyList()` |
  | `int authNode(String, String, ShellZKClient, ShellZKNode)` | 对节点执行认证 | `client.addAuth` 后经 `ShellZKNodeUtil.getNode` 重新取节点，比较 acl/增删读写权限是否“由无变有”，或命中 `getDigestACLs()`；成功则 `zkNode.copy(node)` + `client.setAuthed`；返回 0 失败/1 成功/2 异常 |
  | `String digest(String, String)` | 生成 digest 摘要 | `DigestAuthenticationProvider.generateDigest(user+":"+password)`，失败返回 null |

- 调用链：`authNode → client.addAuth → ShellZKNodeUtil.getNode → digest → client.setAuthed`；`toAuthInfo → AuthInfo`

### ShellZKCacheUtil

- 职责：把 ZK 节点数据（byte[]）缓存到本地文件。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 静态工具类 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String baseDir(int)` | private，拼缓存路径 | `ShellConst.getNodeCachePath() + hashCode + "_"` |
  | `boolean cacheData(int, byte[], String)` | 写缓存 | data 非空时 `FileUtil.touch` + `writeBytes`；否则转 `deleteData` |
  | `byte[] loadData(int, String)` | 读缓存 | 文件存在则 `FileUtil.readBytes` |
  | `boolean deleteData(int, String)` | 删除缓存 | `FileUtil.del` |
  | `boolean hasData(int, String)` | 是否有缓存 | `dataSize(...) > 0` |
  | `long dataSize(int, String)` | 缓存大小 | `new File(fileName).length()`，异常返回 -1 |

- 调用链：`cacheData → FileUtil.touch/writeBytes → deleteData`；`hasData → dataSize → File`

### ShellZKClientActionArgument

- 职责：zkCli 风格动作参数的 POJO（一个参数名 + 一个值）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `Object` | 参数值 |
  | `argument` | `String` | 参数名/开关（如 `-s`、`-v`、路径） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getValue/setValue/getArgument/setArgument` | 标准访问器 | 无附加逻辑 |
  | `ShellZKClientActionArgument(String, Object)` | 构造（名+值） | 赋值 argument/value |
  | `ShellZKClientActionArgument(String)` | 构造（仅名） | 赋值 argument |
  | `ShellZKClientActionArgument(Object)` | 构造（仅值） | 赋值 value |
  | `ofArgument(Object)/ofArgument(String)/ofArgument(String,Object)` | 静态工厂 | 分别委托上述三个构造器 |

- 调用链：`ofArgument → 构造器`

### ShellZKClientActionUtil

- 职责：把 ZK 客户端各类操作（create/set/get/ls/ACL/quota 等）转换为 zkCli 风格参数并派发客户端动作事件。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 静态工具类；顶部原 `forAction(Record)` 实现已注释 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void forAction(String, String)` | 派发简单动作 | `ShellZKEventUtil.zkClientAction(connectName, action)` |
  | `void forCreateAction(String, String, byte[], CreateMode, List<? extends ACL>, Long)` | 构造 create 参数 | 按 `createMode` 追加 `-s/-e/-c`、ttl 追加 `-t`，再拼 path/data/`ShellZKACLUtil.toAclStr(aclList)` |
  | `void forSetAction(String, String, byte[], Integer, boolean)` | 构造 set 参数 | `-s`（stat）、`-v`（version）、path、data |
  | `void forLsAction(String, String, boolean, boolean, boolean)` | 构造 ls 参数 | `-s/-w/-R` + path |
  | `void forGetAction(String, String, boolean, boolean)` | 构造 get 参数 | `-s/-w` + path |
  | `void forSetAclAction(String, String, boolean, boolean, Integer, List<? extends ACL>)` | 构造 setAcl 参数 | `-s/-v/-R` + path + acl 串 |
  | `void forGetAclAction(String, String, boolean)` | 构造 getAcl 参数 | `-w` + path |
  | `void forStatAction(String, String, boolean)` | 构造 stat 参数 | `-w` + path |
  | `void forGetEphemeralsAction(String, String)` | 构造 getEphemerals 参数 | path |
  | `void forGetAllChildrenNumberAction(String, String)` | 构造 getAllChildrenNumber 参数 | path |
  | `void forSyncAction(String, String)` | 构造 sync 参数 | path |
  | `void forDeleteAction(String, String, Integer)` | 构造 delete 参数 | `-v` + path；**注意此处动作名误写为 `"sync"`（第 169 行），疑似复制粘贴缺陷** |
  | `void forListQuotaAction(String, String)` | 构造 listquota 参数 | path |
  | `void forDelQuotaAction(String, String, boolean, boolean)` | 构造 delquota 参数 | `-b/-n` + path |
  | `void forSetQuotaAction(String, String, long, long)` | 构造 setquota 参数 | bytes/count ≥0 时追加 `-b/-n` + path |
  | `void forAddAuthAction(String, String, String)` | 构造 addauth 参数 | scheme + auth |

- 调用链：`forXxxAction → ShellZKEventUtil.zkClientAction(connectName, action, arguments)`；`forCreateAction/forSetAclAction → ShellZKACLUtil.toAclStr`

### ShellZKConnectUtil

- 职责：解析 zkCli 风格连接串并拷贝到 `ShellConnect`。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 静态工具类；`testSSHConnect/testConnect/close` 已注释为死方法 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellZKConnectInfo parse(String)` | 解析连接串 | 按空格分词，识别 `-server`（拆 host:port）、`-timeout`（毫秒转秒）、`-r`（只读），构造并返回 `ShellZKConnectInfo` |
  | `void copyConnect(ShellZKConnectInfo, ShellConnect)` | 拷贝连接属性 | 回填 `readonly/connectTimeOut/host`（host 拼 `host:port`） |

- 调用链：`parse → ShellZKConnectInfo`；`copyConnect → ShellConnect.set*`

### ShellZKDataUtil

- 职责：ZK 节点数据的历史版本管理（保留在服务端 `/_data_history/` 下）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `SERVER_PATH` | `static final String` | 历史数据根路径 `"/_data_history/"` |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `List<ShellZKHistoryData> listHistory(String, ShellZKClient)` | 列出某路径的历史 | 目录名取 `MD5Util.md5Hex(path)`；遍历子节点取其 `stat.getDataLength()` 与 saveTime（节点名解析失败时用 `getMtime`），按 saveTime 排序后 `reversed()` |
  | `void addHistory(String, byte[], ShellZKClient)` | 新增一条历史 | 节点名用当前毫秒；存在则 `setData`，否则用 `toPermInt("rd")` + `ANYONE_ID_UNSAFE` 的 PERSISTENT ACL `create` |
  | `boolean deleteHistory(String, long, ShellZKClient)` | 删除指定时间的历史 | 路径含 saveTime，存在则 `client.delete` |
  | `boolean deleteHistory(String, ShellZKClient)` | 删除某路径全部历史 | 存在则 `client.delete(path, null, true)`（递归） |
  | `byte[] getHistory(String, long, ShellZKClient)` | 读取指定时间历史数据 | 存在则 `client.getData` |

- 调用链：`addHistory → ShellZKACLUtil.toPermInt → client.create`；`listHistory → client.getChildren/checkExists`；`getHistory/deleteHistory → client.exists/getData/delete`

### ShellZKNodeTask

- 职责：按属性位异步拉取 ZK 节点的状态 / ACL / 数据并返回异常。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 以实例方法 `doWorker` + 静态工厂 `of` 组合 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `Exception doWorker(ShellZKNode, ShellZKClient, String, String)` | 执行拉取任务 | 依 `properties` 含 `s/a/d` 分别追加任务：`client.checkExists→node.stat`、`client.getACL→node.acl`、`client.getData→node.setNodeData`；`KeeperException.NoAuthException` 忽略，其它异常存入 `AtomicReference`；最后 `ThreadUtil.submit(tasks)` 并返回异常 |
  | `Exception of(ShellZKNode, ShellZKClient, String, String)` | 静态工厂 | `new ShellZKNodeTask().doWorker(...)` |

- 调用链：`of → doWorker → ThreadUtil.submit → client.checkExists/getACL/getData`

  > 审查提示：`doWorker` 先 `ThreadUtil.submit(tasks)` 后立即 `return exceptionReference.get()`，异步任务尚未完成即读取异常，异常捕获存在时序缺陷。

### ShellZKNodeUtil

- 职责：ZK 节点的构造、刷新、路径工具与递归遍历。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `ACL_PROPERTIES` | `static final String` | 权限属性 `"a"` |
  | `DATA_PROPERTIES` | `static final String` | 数据属性 `"d"` |
  | `STAT_PROPERTIES` | `static final String` | 状态属性 `"s"` |
  | `FULL_PROPERTIES` | `static final String` | 全部属性 `"ads"` |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellZKNode getNode(ShellZKClient, String)` | 取完整节点 | 委托三参重载，用 `FULL_PROPERTIES` |
  | `ShellZKNode getNode(ShellZKClient, String, String)` | 取节点核心 | 校验 path 含 `/`；`new ShellZKNode` 设 `nodePath`；委托 `ShellZKNodeTask.of` 拉取属性并抛异常；记录 `loadTime`（原内联实现已注释） |
  | `void refreshData(ShellZKClient, ShellZKNode)` | 刷新节点数据 | `client.getData` → `setNodeData` 并更新 `loadTime` |
  | `void refreshAcl(ShellZKClient, ShellZKNode)` | 刷新节点 ACL | `client.getACL` → `node.acl` |
  | `void refreshQuota(ShellZKClient, ShellZKNode)` | 刷新配额 | `client.listQuota` → `node.quota` |
  | `void refreshStat(ShellZKClient, ShellZKNode)` | 刷新状态 | `client.checkExists` → `node.stat` |
  | `void refreshNode(ShellZKClient, ShellZKNode)` | 刷新整个节点 | `getNode(...)` 后 `node.copy(n)` |
  | `String getParentPath(String)` | 取父路径 | 处理空/根/无斜杠等边界，`lastIndexOf("/")` 截取 |
  | `String concatPath(String, String)` | 拼接路径 | 按两边是否已含 `/` 避免重复分隔符 |
  | `String getName(String)` | 取路径末段名 | 空返回 `""`，根返回 `/`，否则 `ArrayUtil.last(path.split("/"))` |
  | `List<ShellZKNode> getChildNode(ShellZKClient, String, List<String>, int)` | 取子节点（带去重与数量限制） | `client.getChildren` 后逐个子路径跳过 `existingNodes`，任务加入 `list`（`CopyOnWriteArrayList`），`ThreadUtil.submit` 并发执行；limit>0 时限制任务数 |
  | `String decodePath(String)` | URL 解码路径 | 含 `%`/`+` 时 `URLDecoder.decode`，否则原样返回 |
  | `void loopNode(ShellZKClient, String, Predicate<String>, Consumer<ShellZKNode>, BiConsumer<String,Exception>, boolean)` | 递归遍历节点 | 过滤命中则取 data（可选 acl）回调 `success`，异常回调 `error`；再对子节点递归（原 `getChildNode` 多参重载已注释） |

- 调用链：`getNode → ShellZKNodeTask.of → client.*`；`refreshNode → getNode → node.copy`；`loopNode → client.getData/getACL/getChildren`（递归）

### ShellZKViewFactory

- 职责：zk 各功能窗口 / 对话框的统一创建入口（认证、节点、ACL、导入导出传输、历史）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 静态入口类；`zkHistoryView` 已注释为死方法 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `StageAdapter zkAuthNode(ShellZKNodeTreeItem, ShellZKClient)` | 认证节点 | `ShellZKAuthNodeController`，`showAndWait()` |
  | `StageAdapter zkAddNode(ShellZKNodeTreeItem, ShellZKClient)` | 添加子节点 | `ShellZKAddNodeController` |
  | `StageAdapter zkAddACL(ShellZKNodeTreeItem, ShellZKClient)` | 添加权限 | `ShellZKAddACLController` |
  | `StageAdapter zkUpdateACL(ShellZKNodeTreeItem, ShellZKClient, ShellZKACL)` | 修改权限 | `ShellZKUpdateACLController` |
  | `void zkImportData(ShellConnect)` | 导入数据 | `ShellZKImportDataController` |
  | `void zkExportData(ShellConnect, String)` | 导出数据 | `ShellZKExportDataController` |
  | `void zkTransportData(ShellConnect)` | 传输数据 | `ShellZKTransportDataController` |
  | `StageAdapter zkAuthAdd(ShellConnect)` | 新增认证 | `ShellZKAddAuthController` |
  | `void zkHistoryData(ShellZKClient, String)` | 数据历史 | `ShellZKHistoryDataController` |

- 调用链：`Xxx → StageManager.parseStage(XxxController) → adapter.setProp(...) → adapter.showAndWait()/display()`

## 死代码清单（整文件被注释、无真实代码）

- `cn/oyzh/easyshell/util/mysql/ShellMysqlColumnField.java`：整文件注释，原为 mysql 字段域 POJO。
- `cn/oyzh/easyshell/util/mysql/ShellMysqlI18nHelper.java`：整文件注释，原为 mysql 国际化辅助方法。
