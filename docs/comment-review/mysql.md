# easyshell MySQL 模块代码审查文档

> 范围：`easyshell/src/main/java/cn/oyzh/easyshell/mysql/` 递归全部 `.java`，共 **78 个存活类**。
> 已跳过 9 个整文件被注释的死代码：`ShellMysqlConnConfig`、`check/MysqlChecks`、`event/MysqlEvents`、`foreignKey/MysqlForeignKeys`、`generator/routine/MysqlFunctionSqlGenerator`、`generator/routine/MysqlProcedureSqlGenerator`、`index/MysqlIndexes`、`record/MysqlRecordData`、`trigger/MysqlTriggers`。
> 说明：仅新增本文档，未改动任何 `.java` 文件。术语用中文，标识符保留原文。多数 DTO/Param 类为纯数据载体（getter/setter），方法表已作概括；核心 SQL 生成/执行逻辑逐条列出。

---

## ShellMysqlClient

- 职责：MySQL 数据库客户端封装，实现 `ShellBaseClient`/`DBClient`，提供连接管理，以及库/表/视图/字段/索引/外键/检查/触发器/事件/函数/存储过程/记录的 CRUD、SQL 执行与批量导入、对象克隆等能力。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| jumpForwarder | `SSHJumpForwarder2` | SSH 端口转发器，启用跳板机时使用 |
| shellConnect | `ShellConnect` | 连接信息（主机、端口、账号、代理、跳板等） |
| connManager | `DBConnManager` | 数据库连接管理器（实际为 `ShellMysqlConnManager`） |
| properties | `Map<String,Object>` | 附加属性表 |
| state | `SimpleObjectProperty<ShellConnState>` | 连接状态属性 |
| stateListener | `ChangeListener<ShellConnState>` | 状态变更监听器，转发 `onStateChanged` |
| TABLE_TYPES | `String[]`（static final） | 表类型集合（TABLE/SYSTEM TABLE/…） |
| VIEW_TYPES | `String[]`（static final） | 视图类型集合（VIEW） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlClient(ShellConnect)` | 构造 | 保存连接信息并注册 `stateListener` |
| `DBConnManager getConnManager()` | 取连接管理器 | 懒加载 `new ShellMysqlConnManager()` |
| `boolean isReadonly()` | 是否只读 | 透传 `shellConnect.isReadonly()` |
| `void start(int timeout)` | 启动连接 | 已连接/连接中直接返回；`initClient()` → 设超时 → 状态 CONNECTING → `connection().isValid()` 成功置 CONNECTED 并 `ShellClientChecker.push(this)`，失败置 FAILED；异常包 `ShellException` |
| `String initHost()` | 计算主机地址 | 启用跳板时 `SSHJumpForwarder2.forward(jumpConfigs,target)` 得 `127.0.0.1:localPort`；否则 `hostIp:hostPort` |
| `void initClient()` | 初始化客户端 | 组装 `DBConnConfig`（host/port/user/password/env）；启用代理时写入 proxy 字段并设 `socketFactory=ShellMysqlProxySocketFactory` |
| `void close()` | 关闭 | 关闭 connManager 与 jumpForwarder，状态置 CLOSED，移除监听器，字段置空 |
| `boolean isConnected()` | 是否已连接 | `connection()` 非空且 `!isClosed() && isValid(1000)` |
| `int tableSize(String)` / `int viewSize(String)` | 表/视图数量 | `DatabaseMetaData.getTables(null,dbName,"%",TABLE_TYPES\|VIEW_TYPES)` + `ShellMysqlUtil.checkTableType/checkViewType` 计数 |
| `int procedureSize/functionSize/eventSize(String)` | 过程/函数/事件数量 | 查询 information_schema / `SHOW` 系列统计 |
| `DBQueryResults<ShellMysqlExecuteResult> executeSql(String,String)` | 执行多语句 SQL | `DBSqlParser.parseSql()` 拆分 → 关闭自动提交 → 逐条 `statement.execute`，查询走 `result.parseResult`，更新取 `getUpdateCount`，单条计时；异常回滚并 `parseError` |
| `ShellMysqlExecuteResult executeSingleSql(String,String)` | 执行单条 SQL | `parseSingleSql()`；查询解析结果，更新则提交并返回 updateCount |
| `void executeSqlSimple(String,String)` | 简单执行 | 关自动提交 → `execute` → commit；异常回滚抛 `ShellException` |
| `DBQueryResults<ShellMysqlExplainResult> explainSql(String,String)` | 执行计划 | 每条 SQL 前拼 `EXPLAIN ` 后 `executeQuery` 并 `parseResult` |
| `int insertBatch(String,List<String>)` / `insertBatch(String,List<String>,boolean)` | 批量插入 | 可选新连接 → `addBatch`/`executeBatch` → commit，累加影响行数 |
| `int tableSize/viewSize/procedureSize/functionSize` | 见上 | 元数据计数 |
| `List<MysqlDatabase> databases()` / `List<String> databaseNames()` / `MysqlDatabase database(String)` | 库列表/查询 | information_schema 查询，装配 `MysqlDatabase` |
| `boolean existDatabase/createDatabase(MysqlDatabase)/alterDatabase/dropDatabase` | 库 DDL | 生成 `CREATE/ALTER/DROP DATABASE` 并执行 |
| `String databaseCollation(String)` | 库排序规则 | 查询 information_schema.SCHEMATA |
| `List<String> engines()` / `charSets()` / `collation(String)` | 引擎/字符集/排序规则 | `SHOW ENGINES` / 字符集查询 |
| `List<MysqlTable> selectTables(MysqlSelectTableParam)` | 查询表 | 由 information_schema.TABLES 读取引擎/行格式/排序/注释/自增；`full` 时再调 `showCreateTable` |
| `MysqlTable selectTable(String,String)` / `selectTableSimple` / `selectTable(param)` | 查询单表 | 同表列表按名过滤，`full` 含建表语句 |
| `List<MysqlColumn> selectColumns(MysqlSelectColumnParam)` | 查询字段 | `SHOW FULL COLUMNS` 解析 Key/Type/Extra/Null/Collation，`parseKey/parseExtra/parseCollation` 装配后按 position 排序 |
| `List<MysqlRecord> selectRecords(MysqlSelectRecordParam)` | 查询记录 | 构造 `SELECT *`，`MysqlConditionUtil.buildCondition` 拼 WHERE，分页 `LIMIT`；几何值 `getGeometryString` |
| `long selectRecordCount(MysqlSelectRecordParam)` | 记录数 | `SELECT COUNT(*)` + 过滤条件 |
| `int insertRecord(MysqlInsertRecordParam)` | 插入记录 | 拼 `INSERT INTO (...)`；几何字段用 `ST_GeomFromText(?)`；`RETURN_GENERATED_KEYS` 取自增主键回填 |
| `int deleteRecord(MysqlDeleteRecordParam)` | 删除记录 | 有主键按主键删；否则按全字段 `= ?`/`IS NULL` 拼 WHERE 并 `LIMIT 1` |
| `int updateRecord(MysqlUpdateRecordParam)` | 更新记录 | `UPDATE ... SET`（几何 `ST_GeomFromText(?)`）+ 主键或原始值定位 |
| `String showCreateTable/View/Function/Procedure/Trigger/Event(String,String)` | 取建对象语句 | `SHOW CREATE XXX` 返回第 2 列（或命名列） |
| `List<MysqlIndex> selectIndexes(String,String)` | 查询索引 | information_schema.STATISTICS 装配 `MysqlIndex` |
| `List<MysqlCheck> selectChecks(String,String)` | 查询检查约束 | information_schema.CHECK_CONSTRAINTS/TABLE_CONSTRAINTS |
| `List<MysqlForeignKey> selectForeignKeys(String,String)` | 查询外键 | information_schema 装配外键列与引用策略 |
| `List<MysqlColumn> viewColumns(String,String)` / `List<MysqlRecord> viewRecords(...)` | 视图字段/数据 | 对视图做字段与记录查询 |
| `void createTable(MysqlCreateTableParam)` / `alertTable(MysqlAlertTableParam)` | 建表/改表 | 调 `MysqlTableCreateSqlGenerator` / `MysqlTableAlertSqlGenerator`，逐条执行 SQL |
| `void renameTable/Event/Function/Procedure(...)` | 重命名 | 生成 `RENAME` / 重建对象 |
| `void clearTable/truncateTable/dropTable(...)` | 清空/截断/删表 | `TRUNCATE` / `DROP TABLE` |
| `void dropView/createView(MysqlCreateViewParam)/alertView(MysqlAlertViewParam)` | 视图 DDL | 调对应视图 SQL 生成器 |
| `List<MysqlEvent> selectEvents(...)` / `MysqlEvent selectEvent(...)` | 查询事件 | information_schema.EVENTS / `SHOW` |
| `void dropEvent/createEvent/alertEvent(...)` | 事件 DDL | `DROP EVENT` / 事件 SQL 生成器 |
| `MysqlFunction selectFunction(...)` / `List<MysqlFunction> selectFunctions(...)` | 查询函数 | 装配 `MysqlRoutineSchema` 子类 |
| `void dropFunction/createFunction(MysqlCreateFunctionParam)/alertFunction(MysqlAlertFunctionParam)` | 函数 DDL | 调函数 SQL 生成器 |
| `MysqlProcedure selectProcedure(...)` / `List<MysqlProcedure> selectProcedures(...)` | 查询过程 | 装配过程定义 |
| `void dropProcedure/createProcedure/alertProcedure(...)` | 过程 DDL | 调过程 SQL 生成器 |
| `List<MysqlTrigger> selectTriggers(...)` | 查询触发器 | information_schema.TRIGGERS |
| `List<MysqlRoutineParam> listRoutineParam(String,String,String)` | 例程参数 | 查 information_schema.PARAMETERS，按 PARAMETER_NAME/MODE/COLLATION/CHARSET/DTD_IDENTIFIER 装配 |
| `List<MysqlRoutineParam> listFunctionParam/listProcedureParam(...)` | 函数/过程参数 | 转调 `listRoutineParam(...,"FUNCTION"/"PROCEDURE")` |
| `void cloneTable(String,String,String,boolean)` | 克隆表 | `CREATE TABLE new LIKE old` → `alertTable` 复制检查/外键/触发器 → 可选 `INSERT ... SELECT` 复制数据 |
| `void cloneView/Function/Procedure/Event(...)` | 克隆对象 | 取建对象语句后替换对象名再执行 |
| `boolean existView(String,String)` / `boolean existPrimaryKey(String,String)` | 存在性判断 | 查询元数据 |
| `boolean isSupportFeature(DBFeature)` / `isSupportCheckFeature/isSupportEventFeature()` | 特性支持 | EVENT 恒 true；CHECK 依赖版本：mariadb 或 MySQL>=8.0.16 |
| `Long getGeneratedKeys(Statement)` | 自增键 | 优先 `getGeneratedKeys()`，为空则 `SELECT LAST_INSERT_ID()` |
| `DBDialect dialect()` | 方言 | 返回 `DBDialect.MYSQL` |
| `String selectVersion/selectProduct/selectClientCharacter()` | 版本/产品/字符集 | `SELECT VERSION()` 等 |
| `void printSql(String)` | 打印 SQL | `DBUtil.printSql` + `ShellEventUtil.printSql(压缩后SQL, shellConnect)` |

- 调用链：`ShellMysqlClient.start → initClient → initHost → SSHJumpForwarder2.forward`；`executeSql → DBSqlParser.parseSql → Statement.execute → ShellMysqlExecuteResult.parseResult`；`selectRecords → MysqlConditionUtil.buildCondition → ShellMysqlHelper.parseColumns`；`createTable → MysqlTableCreateSqlGenerator.generate → buildSql`；`cloneTable → selectChecks/selectTriggers/selectForeignKeys → alertTable`。

---

## ShellMysqlConnManager

- 职责：MySQL 连接管理器，继承 `DBConnManager`，负责加载 JDBC 驱动并创建 MySQL 连接。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无自持字段；继承父类 config/connectTimeout 等） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `Connection initConnection(String dbName,String user,String password)` | 建立连接 | `Class.forName("com.mysql.cj.jdbc.Driver")`；拼 host（含库名）；`Properties` 写 user/password；有代理时写 `_proxyHost/_proxyType/...` 并设 socketFactory；写 `DEFAULT_ENVIRONMENT` 与自定义 env；`DriverManager.getConnection` |
| `String getConnectionString()` | 连接串 | 返回 `jdbc:mysql://host:port/` |

- 调用链：`ShellMysqlClient.start → getConnManager → ShellMysqlConnManager.initConnection → DriverManager.getConnection`。

---

## ShellMysqlHelper

- 职责：MySQL 工具类，提供视图可更新性判断、几何值转换、结果集字段解析、建视图语句与默认连接环境参数。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| DEFAULT_ENVIRONMENT | `Map<String,Object>`（static） | 默认连接参数（tcpNoDelay/tcpKeepAlive/autoReconnect/characterEncoding/allowPublicKeyRetrieval/zeroDateTimeBehavior/connectionTimeZone/maxReconnects） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `boolean isViewUpdatable(Connection,String,String)` | 视图是否可更新 | 查 information_schema.VIEWS 的 IS_UPDATABLE 是否为 YES |
| `String getGeometryString(Connection,Object)` | 几何值转文本 | `SELECT ST_AsText(?)` |
| `MysqlColumns parseColumns(ResultSet)` / `parseColumns(ResultSet,List<String>)` | 解析结果集字段 | 遍历 `ResultSetMetaData`，按列标签/类型/长度/自增/可空/小数位装配 `MysqlColumn`，支持排除列 |
| `String showCreateView(Connection,String)` | 建视图语句 | `SHOW CREATE VIEW` 取 Create View 列 |
| `String defaultEnvironment()` | 默认环境字符串 | 遍历 `DEFAULT_ENVIRONMENT` 拼 `key=value\n` |

- 调用链：`ShellMysqlClient.selectRecords → ShellMysqlHelper.parseColumns`；`ShellMysqlClient.selectView → ShellMysqlHelper.isViewUpdatable`。

---

## ShellMysqlProxySocketFactory

- 职责：MySQL 代理连接工厂，继承 `StandardSocketFactory`，通过 `ShellProxyUtil` 建立经代理的 Socket。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| connectTimeout | `int` | 连接超时（毫秒） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `Socket connect(String,int,PropertySet,int)` | 建连 | 记录 host/port，超时（登录超时<=0 用 5000ms）；`createSocket` 后 `configureSocket`，赋值 rawSocket/sslSocket |
| `Socket createSocket(PropertySet)` | 创建 Socket | 从属性读取 `_proxyHost/_proxyType/_proxyUser/_proxyPort/_proxyPassword` 组装 `ShellProxyConfig`，调 `ShellProxyUtil.createSocket` |

- 调用链：`ShellMysqlClient.initClient（设 socketFactory）→ ShellMysqlConnManager.initConnection → ShellMysqlProxySocketFactory.connect → ShellProxyUtil.createSocket`。

---

## MysqlCheck

- 职责：MySQL 检查约束模型，实现 `DBCheck`/`ObjectCopier`，承载库/表/名称/子语句及变更追踪。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| tableName | `String` | 表名称 |
| name | `String` | 约束名称 |
| clause | `String` | 检查子语句 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void setName(String)` / `String getName()` | 名称读写 | set 时记录原始数据 |
| `boolean isNameChanged()` / `String originalName()` | 名称变更 | `checkOriginalData`/`getOriginalData` |
| `void setClause(String)` / `String getClause()` | 子语句读写 | set 记录原始数据 |
| `boolean isClauseChanged()` | 子语句变更 | `checkOriginalData("clause",…)` |
| `void copy(MysqlCheck)` | 复制 | 复制 name/dbName/clause/tableName |
| `boolean isInvalid()` | 是否无效 | 父类无效或 clause 为空 |

- 调用链：`ShellMysqlClient.selectChecks → MysqlCheck`；`MysqlTableCreateSqlGenerator.checkHandle → MysqlCheck.getName/getClause`。

---

## MysqlCheckControl

- 职责：检查约束 UI 组件，继承 `MysqlCheck`，为表格行生成名称/子语句输入框。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无新增字段） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ClearableTextField getNameControl()` | 名称组件 | 名称为空时 `DBUtil.genCheckName()` 生成默认名；文本变更回写 `setName` |
| `ClearableTextField getClauseControl()` | 子语句组件 | 文本变更回写 `setClause` |
| `static MysqlCheckControl of(MysqlCheck)` / `of(List<MysqlCheck>)` | 工厂 | `copy` 后返回控件 |

- 调用链：`表结构界面 → MysqlCheckControl.of → getNameControl/getClauseControl`。

---

## MysqlColumn

- 职责：MySQL 字段模型，实现 `DBColumn`/`ObjectCopier`，承载字段全部属性、类型解析与变更追踪、按方言的能力判定。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| schema | `String` | 模式名称 |
| tableName | `String` | 表名称 |
| size | `Integer` | 字段大小（长度） |
| typeProperty | `StringProperty` | 字段类型属性 |
| valueProperty | `StringProperty` | 字段值（枚举/集合值）属性 |
| comment | `String` | 注释 |
| nullable | `Boolean` | 是否可空 |
| unsigned | `Boolean` | 是否无符号 |
| zeroFill | `Boolean` | 是否填充零 |
| updateOnCurrentTimestamp | `Boolean` | 是否 ON UPDATE CURRENT_TIMESTAMP |
| position | `Integer` | 字段位置 |
| primaryKeyProperty | `SimpleBooleanProperty` | 主键属性 |
| primaryKeySize | `Integer` | 主键键长度 |
| defaultValue | `Object` | 默认值 |
| digits | `Integer` | 小数位 |
| autoIncrement | `Boolean` | 是否自增 |
| name | `String` | 字段名称 |
| charset | `String` | 字符集 |
| collation | `String` | 排序规则 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlColumn()` / `MysqlColumn(String)` | 构造 | 名称构造 |
| `boolean isNameChanged()` / `String originalName()` | 名称变更 | `checkOriginalData`/`getOriginalData` |
| `List<String> getValueList()` | 值列表 | 按逗号拆分 value，去引号 |
| `void setDefaultValue(Object)` / `Object getDefaultValue()` | 默认值 | set 记录原始数据 |
| `String getDefaultValueString()` / `Object getDefaultValueFix()` | 默认值字符串/修正 | 按整型/小数/枚举/时间戳类型转换（数字转 Long/Double，空白或 null 转 null） |
| `void setAutoIncrement(Boolean)` / `boolean isAutoIncrement()` | 自增 | set 记录原始数据 |
| `boolean hasComment()` | 是否有注释 | `getComment()!=null` |
| `void setCharset/setCollation(...)` | 字符集/排序 | 记录原始数据 |
| `void setValue(String)` / `String getValue()` | 值 | 记录原始数据 |
| `void setUnsigned/boolean isUnsigned()`、`setZeroFill/isZeroFill`、`setUpdateOnCurrentTimestamp/isUpdateOnCurrentTimestamp` | 无符号/填充零/时间戳更新 | 记录原始数据 |
| `boolean supportSize/supportGeometry/supportCharset/supportUnsigned/supportDigits/supportInteger/supportAutoIncrement/supportDefaultValue/supportTimestamp/supportValue/supportText/supportZeroFill/supportBit/supportJson/supportKeySize/supportString/supportBinary/supportEnum` | 类型能力判定 | 全部委托 `DBColumnFieldManager.*(DBDialect.MYSQL, getType())` |
| `Integer suggestSize()` / `Long minValue()/maxValue()` / `Object exampleValue()` | 推荐长度/极值/示例值 | 委托 `DBColumnFieldManager` |
| `SimpleBooleanProperty primaryKeyProperty()` / `isPrimaryKey()/setPrimaryKey(Boolean)` | 主键 | set 记录原始数据 |
| `boolean isColumnChanged()` | 字段是否变更 | 原始数据除 primaryKey/primaryKeySize 外存在即变更 |
| `boolean isPrimaryKeyChanged()` | 主键是否变更 | 5 项判断：primaryKey/primaryKeySize 变更、新建且主键、改名且主键、删除且主键 |
| `void initStatus()` | 初始化状态 | 将 size/value/digits/unsigned/zeroFill/autoIncrement/updateOnCurrentTimestamp 记录为原始数据 |
| `void parseKey(String)` | 解析主键标识 | key 为 "pri" 置主键 |
| `String parseType(String)` | 解析类型 | 解析 unsigned/zerofill；拆 `type(x,y)`：枚举置 value、小数置 size+digits、整数置 size |
| `void parseExtra(String)` | 解析额外信息 | 含 auto_increment/on update CURRENT_TIMESTAMP 置标志 |
| `void parseCollation(String)` | 解析排序规则 | 设 collation 并截取下划线前为 charset |
| `void initColumn(String,String)` | 初始化字段 | 解析 columnType（类型+长度/值+unsigned/zerofill）与 columnExtra |
| `boolean hasDefaultValue()` | 是否有默认值 | `defaultValue!=null` |
| `void copy(MysqlColumn)` | 复制 | 逐属性复制 |
| `boolean isYearType/isDateType/isDateTimeType/isGeometryType/isTimeType()` | 类型判断 | 委托 `ShellMysqlColumnUtil` |
| `void setType(String)` / `String getType()` | 类型读写 | set 走 `parseType` 并转大写，记录原始数据 |
| `void destroy()` | 销毁 | 解绑各属性并 `super.destroy()` |

- 调用链：`ShellMysqlClient.selectColumns → MysqlColumn.parseKey/parseExtra/parseCollation`；`MysqlTableCreateSqlGenerator.columnHandle → MysqlColumn.supportXxx/getXxx`。

---

## MysqlColumnControl

- 职责：字段 UI 组件，继承 `MysqlColumn`，为表结构表格行生成各属性编辑控件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无新增字段） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ClearableTextField getNameControl()` | 名称控件 | 变更回写 `setName` |
| `ClearableTextField getCommentControl()` | 注释控件 | 变更回写 `setComment` |
| `NumberTextField getSizeControl()` | 长度控件 | 绑定 `setSize` |
| `NumberTextField getDigitsControl()` | 小数位控件 | 绑定 `setDigits` |
| `DBFiledTypeComboBox getTypeControl()` | 类型控件 | `setDialect(MYSQL)`，选择回写 `setType` |
| `FXCheckBox getNullableControl()` | 可空控件 | 变更回写 `setNullable`；主键变更时取消可空 |
| `FXCheckBox getPrimaryKeyControl()` | 主键控件 | 变更回写 `setPrimaryKey` |
| `static MysqlColumnControl of(MysqlColumn)` / `of(List<MysqlColumn>)` | 工厂 | `copy` 后返回控件 |

- 调用链：`表结构界面 → MysqlColumnControl.of → 各 getXxxControl`。

---

## MysqlColumns

- 职责：字段列表集合，继承 `DBObjectList<MysqlColumn>`，提供主键/名称/位置等聚合查询。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无新增字段） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlColumns()` / `MysqlColumns(List<MysqlColumn>)` | 构造 | 列表构造 `addAll` |
| `List<MysqlColumn> primaryKeys()` | 主键字段 | 过滤未删除且主键，自增优先排序 |
| `boolean primaryKeyChanged()` | 主键是否变更 | 恒返回 false（占位） |
| `MysqlColumn column(String)` | 按名取字段 | 忽略大小写匹配 |
| `int index(String)` | 字段位置 | 遍历计位 |
| `List<MysqlColumn> sortOfPosition()` | 按位置排序 | 按 getPosition 排序 |
| `String tableName()/dbName()` | 表名/库名 | 取首个字段的对应属性 |
| `List<String> columnNames()` | 字段名列表 | 全字段名 |
| `boolean hasPrimaryKey()` | 是否含主键 | 存在主键或自增字段 |

- 调用链：`ShellMysqlClient.selectColumns 返回 → MysqlColumns.sortOfPosition`；`MysqlTableCreateSqlGenerator.primaryKeyHandle → MysqlCreateTableParam.primaryKeys`。

---

## MysqlSelectColumnParam

- 职责：查询字段参数 DTO（库/模式/表名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| schema | `String` | 模式名称 |
| tableName | `String` | 表名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| 3 个构造：`()`、`(dbName,tableName)`、`(dbName,schema,tableName)` | 构造 | 组装查询条件 |
| `getDbName/setDbName`、`getSchema/setSchema`、`getTableName/setTableName` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.selectColumns(param)`。

---

## MysqlBetweenCondition

- 职责：介于（BETWEEN）条件，继承 `MysqlCondition`，支持数组/集合两值区间。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlBetweenCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlBetweenCondition()` | 构造 | `super(I18nHelper.between(),"BETWEEN")` |
| `MysqlBetweenCondition(String,String)` | 构造 | 自定义名称/值 |
| `String wrapCondition(Object)` | 包装条件 | Object[]/Collection 取两值拼 `值 BETWEEN a AND b`（`DBUtil.wrapData(...,MYSQL)`） |

- 调用链：`MysqlConditionUtil.generateNode → isBetweenCondition`；记录过滤界面 → `wrapCondition`。

---

## MysqlCondition

- 职责：MySQL 查询条件抽象基类，继承 `DBCondition`，统一按 MYSQL 方言包装条件值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 DBCondition：name 条件名称、value 条件值/SQL 运算符、requireCondition 是否需输入值） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| 3 个构造：`()`、`(name,value)`、`(name,value,requireCondition)` | 构造 | 透传父类 |
| `String wrapCondition(Object)` | 包装条件值 | `DBUtil.wrapData(condition,MYSQL)` 转字符串 |
| `String wrapCondition(String columnName,Object condition)` | 包装完整条件 | 需输入值时 `value + ' ' + wrap`，否则仅 `wrap` |

- 调用链：所有具体条件类继承本类；`MysqlRecordFilter.condition() → wrapCondition`。

---

## MysqlConditionUtil

- 职责：条件工具类，注册全部 MySQL 条件并负责条件 SQL 构建与 UI 节点生成。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无字段，全静态方法） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static void init()` | 注册条件 | 将 20 个条件单例 `DBConditionManager.putCondition(DBDialect.MYSQL, …)` |
| `static String buildCondition(List<MysqlRecordFilter>)` | 构建条件 SQL | 逐条 `DBUtil.wrap(column)` + 条件串，用 `getJoinSymbol` 连接 |
| `static boolean isInCondition/isBetweenCondition(MysqlCondition)` | 条件类型判断 | 与 IN/NOT IN、BETWEEN/NOT BETWEEN 单例比较 |
| `static List<Node> generateNode(MysqlColumn,MysqlCondition)` | 生成输入控件 | IN 类生成单输入框；BETWEEN 类生成两节点；否则生成单节点，按 isRequireCondition 决定禁用 |
| `static void setNodeVal(List<Node>,Object)` | 设置控件值 | 逐控件 setNodeVal（值为 List 时按位取） |
| `static Object getNodeVal(List<Node>)` | 取控件值 | 单控件取单值，多控件取列表 |

- 调用链：`ShellMysqlClient.selectRecords → MysqlConditionUtil.buildCondition`；记录过滤界面 → `generateNode/setNodeVal/getNodeVal`。

---

## MysqlContainsCondition

- 职责：包含（LIKE %x%）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlContainsCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlContainsCondition()` / `(String,String)` | 构造 | `super(I18nHelper.contains(),"LIKE")` |
| `String wrapCondition(Object)` | 包装 | 非空时值两侧加 `%` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlEmptyCondition

- 职责：为空（`=''`）条件，无需输入值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlEmptyCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlEmptyCondition()` | 构造 | `super(I18nHelper.isEmpty(),"=''",false)`（requireCondition=false） |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlEndWithCondition

- 职责：以指定值结尾（LIKE %x）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlEndWithCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlEndWithCondition()` / `(String,String)` | 构造 | `super(I18nHelper.endWith(),"LIKE")` |
| `String wrapCondition(Object)` | 包装 | 值前加 `%` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlEqCondition

- 职责：等于（`=`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlEqCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlEqCondition()` | 构造 | `super(I18nHelper.eq(),"=")` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlGtCondition

- 职责：大于（`>`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlGtCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlGtCondition()` | 构造 | `super(I18nHelper.gt(),">")` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlGtEqCondition

- 职责：大于等于（`>=`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlGtEqCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlGtEqCondition()` | 构造 | `super(I18nHelper.gtEq(),">=")` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlInListCondition

- 职责：在列表（IN）条件，支持逗号分隔字符串拆分。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlInListCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlInListCondition()` / `(String,String)` | 构造 | `super(I18nHelper.inList(),"IN")` |
| `String wrapCondition(Object)` | 包装 | 字符串按 `,` 拆分，各值 `DBUtil.wrapData` 拼 `IN (a,b,...)` |

- 调用链：`MysqlConditionUtil.init`；记录过滤界面 → `wrapCondition`。

---

## MysqlLtCondition

- 职责：小于（`<`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlLtCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlLtCondition()` | 构造 | `super(I18nHelper.lt(),"<")` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlLtEqCondition

- 职责：小于等于（`<=`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlLtEqCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlLtEqCondition()` | 构造 | `super(I18nHelper.ltEq(),"<=")` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlNotBetweenCondition

- 职责：不介于（NOT BETWEEN）条件，继承 `MysqlBetweenCondition`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlNotBetweenCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlNotBetweenCondition()` | 构造 | `super(I18nHelper.notBetween(),"NOT BETWEEN")`（复用父类区间包装逻辑） |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlNotContainsCondition

- 职责：不包含（NOT LIKE %x%）条件，继承 `MysqlContainsCondition`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlNotContainsCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlNotContainsCondition()` | 构造 | `super(I18nHelper.notContains(),"NOT LIKE")` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlNotEmptyCondition

- 职责：不为空（`!=''`）条件，无需输入值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlNotEmptyCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlNotEmptyCondition()` | 构造 | `super(I18nHelper.notIsEmpty(),"!=''",false)` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlNotEndWithCondition

- 职责：不以指定值结尾（NOT LIKE %x）条件，继承 `MysqlEndWithCondition`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlNotEndWithCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlNotEndWithCondition()` | 构造 | `super(I18nHelper.notEndWith(),"NOT LIKE")` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlNotEqCondition

- 职责：不等于（`!=`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlNotEqCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlNotEqCondition()` | 构造 | `super(I18nHelper.notEq(),"!=")` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlNotInListCondition

- 职责：不在列表（NOT IN）条件，继承 `MysqlInListCondition`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlNotInListCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlNotInListCondition()` | 构造 | `super(I18nHelper.notInList(),"NOT IN")` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlNotNullCondition

- 职责：非空（IS NOT NULL）条件，无需输入值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlNotNullCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlNotNullCondition()` | 构造 | `super(I18nHelper.notIsNull(),"IS NOT NULL",false)` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlNotStartWithCondition

- 职责：不以指定值开头（NOT LIKE x%）条件，继承 `MysqlStartWithCondition`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlNotStartWithCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlNotStartWithCondition()` | 构造 | `super(I18nHelper.notStartWith(),"NOT LIKE")` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlNullCondition

- 职责：为空（IS NULL）条件，无需输入值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlNullCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlNullCondition()` | 构造 | `super(I18nHelper.isNull(),"IS NULL",false)` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlStartWithCondition

- 职责：以指定值开头（LIKE x%）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `MysqlStartWithCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlStartWithCondition()` / `(String,String)` | 构造 | `super(I18nHelper.startWith(),"LIKE")` |
| `String wrapCondition(Object)` | 包装 | 值后加 `%` |

- 调用链：`MysqlConditionUtil.init`。

---

## MysqlDatabase

- 职责：MySQL 数据库模型，实现 `DBDatabse`，承载库名、字符集与排序规则。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| name | `String` | 库名称 |
| charsetProperty | `SimpleStringProperty` | 字符集属性 |
| collationProperty | `SimpleStringProperty` | 排序规则属性 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `SimpleStringProperty charsetProperty()` / `setCharset/getCharset` | 字符集 | 懒加载属性 |
| `SimpleStringProperty collationProperty()` / `setCollation/getCollation` | 排序规则 | 懒加载属性 |
| `void setCharsetAndCollation(String)` | 由排序规则推字符集 | 取 `_` 前为 charset，含 `_` 则设 collation |
| `String getName()` / `setName(String)` | 名称 | 读写 |

- 调用链：`ShellMysqlClient.databases/database → MysqlDatabase`；`createDatabase/alterDatabase → MysqlDatabase`。

---

## MysqlEvent

- 职责：MySQL 事件模型，实现 `DBName`/`ObjectCopier`/`ObjectComparator`，承载单次/循环事件的调度、状态、定义等。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| name | `String` | 事件名称 |
| type | `String` | 类型（ONE TIME 单次 / RECURRING 循环） |
| intervalValue | `Integer` | 定期-循环值 |
| intervalField | `String` | 定期-循环类型（DAY/HOUR…） |
| eventStatus | `String` | 事件状态 |
| definer | `String` | 定义者 |
| executeAt | `Object` | 单次-执行时间 |
| starts | `Object` | 定期-开始时间 |
| startIntervalValue | `Integer` | 定期-开始循环值 |
| startIntervalField | `String` | 定期-开始循环类型 |
| ends | `Object` | 定期-结束时间 |
| endIntervalValue | `Integer` | 定期-结束循环值 |
| endIntervalField | `String` | 定期-结束循环类型 |
| dbName | `String` | 库名称 |
| comment | `String` | 注释 |
| definition | `String` | 事件体定义 |
| onCompletion | `String` | 完成时行为 |
| createDefinition | `String` | 建事件语句缓存 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void copy(MysqlEvent)` / `boolean compare(MysqlEvent)` | 复制/比较 | 逐属性复制；比较名称等 |
| `void setCreateDefinition(String)` | 设建事件语句 | 记录原始数据 |
| `boolean isOnTimeType()` / `isRecurringType()` | 类型判断 | ONE TIME / RECURRING |
| `Object executeAt()/starts()/ends()` | 调度时间 | 按类型返回格式化时间 |
| `boolean isEnable()` / `isPreserve()` | 状态判断 | 依据 eventStatus/onCompletion |
| 其余 `getXxx/setXxx` | 属性访问器 | 纯读写（set 记录原始数据） |

- 调用链：`ShellMysqlClient.selectEvent/selectEvents → MysqlEvent`；`MysqlEventCreateSqlGenerator.generate → MysqlEvent.getXxx`。

---

## MysqlSelectEventParam

- 职责：查询事件参数 DTO（是否完整/库名/事件名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| full | `boolean` | 是否查询完整信息 |
| dbName | `String` | 库名称 |
| eventName | `String` | 事件名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFull/setFull`、`getDbName/setDbName`、`getEventName/setEventName` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.selectEvent/selectEvents(param)`。

---

## MysqlForeignKey

- 职责：MySQL 外键模型，实现 `ObjectCopier`，承载外键列、引用库表列与更新/删除策略。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| name | `String` | 外键名称 |
| columns | `Set<String>` | 本表外键列 |
| primaryKeyDatabaseProperty | `SimpleStringProperty` | 引用库属性 |
| primaryKeyTableProperty | `SimpleStringProperty` | 引用表属性 |
| primaryKeyColumns | `Set<String>` | 引用列 |
| deletePolicy | `String` | 删除策略 |
| updatePolicy | `String` | 更新策略 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String originalName()` / `setName(String)` | 名称变更 | 原始名/记录原始数据 |
| `SimpleStringProperty primaryKeyDatabaseProperty()/primaryKeyTableProperty()` | 引用库/表属性 | 懒加载属性 |
| `void setDeletePolicy/setUpdatePolicy(String)` | 策略 | 记录原始数据 |
| `void setColumns(Set<String>)` / `addColumn(String)` | 外键列 | 记录原始数据/追加 |
| `void setPrimaryKeyDatabase/setPrimaryKeyTable(...)` / `getPrimaryKeyDatabase/getPrimaryKeyTable` | 引用库/表 | 记录原始数据 |
| `void setPrimaryKeyColumns(Set<String>)` / `addPrimaryKeyColumn(String)` | 引用列 | 记录原始数据/追加 |
| `void copy(MysqlForeignKey)` | 复制 | 逐属性复制 |
| `boolean isInvalid()` | 是否无效 | 无外键列或引用列时无效 |
| `void destroy()` | 销毁 | 解绑属性 |

- 调用链：`ShellMysqlClient.selectForeignKeys → MysqlForeignKey`；`MysqlTableCreateSqlGenerator.foreignKeyHandle → getColumns/getPrimaryKeyColumns`。

---

## MysqlForeignKeyControl

- 职责：外键 UI 组件，继承 `MysqlForeignKey`，生成名称/列/引用库表/策略等编辑控件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| dbClient | `ShellMysqlClient` | 数据库客户端（用于联级下拉） |
| columnList | `List<MysqlColumn>` | 候选字段列表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `FXTextField getNameControl()` | 名称控件 | 变更回写 setName |
| `ShellMysqlFieldTextFiled getColumnControl()` | 外键列控件 | 从 columnList 选列 |
| `ShellMysqlDatabaseComboBox getPrimaryKeyDatabaseControl()` | 引用库控件 | dbClient.databases() 填充 |
| `ShellMysqlTableComboBox getPrimaryKeyTableControl()` | 引用表控件 | 按库加载表 |
| `ShellMysqlForeignKeyPolicyComboBox getDeletePolicyControl()/getUpdatePolicyControl()` | 策略控件 | 变更回写策略 |
| `ShellMysqlFieldTextFiled getPrimaryKeyColumnControl()` | 引用列控件 | 按引用表加载列 |
| `String getPrimaryKeyDatabase()` | 取引用库 | 重写 |
| `static MysqlForeignKeyControl of(MysqlForeignKey)` / `of(List<MysqlForeignKey>)` | 工厂 | copy 后返回控件 |

- 调用链：`表结构界面 → MysqlForeignKeyControl.of → 各 getXxxControl → dbClient.selectTables/selectColumns`。

---

## MysqlAlertFunctionParam

- 职责：修改函数参数 DTO（库名/函数/函数名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| function | `MysqlFunction` | 函数对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getDbName/setDbName`、`getFunction/setFunction` | 属性访问器 | 纯读写 |
| `String getFunctionName()` / `setFunctionName(String)` | 函数名 | 代理 function.getName/setName |

- 调用链：`ShellMysqlClient.alertFunction(param) → MysqlFunctionAlertSqlGenerator`。

---

## MysqlCreateFunctionParam

- 职责：创建函数参数 DTO（库名/函数/函数名），结构与 `MysqlAlertFunctionParam` 相同。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| function | `MysqlFunction` | 函数对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getDbName/setDbName`、`getFunction/setFunction`、`getFunctionName/setFunctionName` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.createFunction(param) → MysqlFunctionCreateSqlGenerator`。

---

## MysqlFunction

- 职责：MySQL 函数模型，继承 `MysqlRoutineSchema`，在例程基础上增加返回值参数。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| returnParam | `MysqlRoutineParam` | 返回值参数 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void setParams(List<MysqlRoutineParam>)` | 设参数 | 重写，过滤表函数/过程 |
| `String getReturnType()` | 返回类型 | 取 returnParam 类型 |
| `void copy(MysqlFunction)` | 复制 | 复制基类属性与返回参数 |
| `MysqlRoutineParam getReturnParam()` / `setReturnParam(...)` | 返回参数 | 读写 |

- 调用链：`ShellMysqlClient.selectFunction(s) → MysqlFunction`；`MysqlFunctionCreateSqlGenerator._generate → getParams/getReturnParam`。

---

## MysqlSelectFunctionParam

- 职责：查询函数参数 DTO（是否完整/库名/函数名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| full | `boolean` | 是否完整信息 |
| dbName | `String` | 库名称 |
| functionName | `String` | 函数名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFull/setFull`、`getDbName/setDbName`、`getFunctionName/setFunctionName` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.selectFunction(param)`。

---

## MysqlEventAlertSqlGenerator

- 职责：MySQL 修改事件 SQL 生成器，生成 `ALTER EVENT` 语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无字段） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String generate(MysqlEvent)` | 生成 SQL | 拼 `ALTER [DEFINER=] EVENT 库.事件`；`ON SCHEDULE`：单次 `AT`（可加 INTERVAL），循环 `EVERY ... [STARTS ...][ENDS ...]`；追加 ON COMPLETION/状态/COMMENT/DO 定义 |
| `static String generateSql(MysqlEvent)` | 静态入口 | new 后调 generate |

- 调用链：`ShellMysqlClient.alertEvent → MysqlEventAlertSqlGenerator.generateSql`。

---

## MysqlEventCreateSqlGenerator

- 职责：MySQL 创建事件 SQL 生成器，生成 `CREATE EVENT` 语句（逻辑与修改版一致，仅前缀为 CREATE）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无字段） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String generate(MysqlEvent)` | 生成 SQL | 同 `MysqlEventAlertSqlGenerator`，首关键词为 `CREATE` |
| `static String generateSql(MysqlEvent)` | 静态入口 | new 后调 generate |

- 调用链：`ShellMysqlClient.createEvent → MysqlEventCreateSqlGenerator.generateSql`。

---

## MysqlFunctionAlertSqlGenerator

- 职责：MySQL 修改函数 SQL 生成器，生成删除+重建函数两条语句（继承 `DBSqlGenerator`）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlList/sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(MysqlAlertFunctionParam)` | 生成片段 | 先 `DROP FUNCTION IF EXISTS 库.名;`，再 `CREATE [DEFINER=] FUNCTION`；参数由 `MysqlRoutineParam.getDefinition(false)` 拼接；`RETURNS` 返回类型；追加 COMMENT/SQL SECURITY/特征/函数体 |
| `List<String> generate(param)` / `String generateSingle(param)` | 生成列表/单条 | `_generate` 后 `buildSql`/`buildSqlSingle` |
| `static List<String> generateSql/generateSqlSingle(param)` | 静态入口 | new 后调用 |

- 调用链：`ShellMysqlClient.alertFunction → MysqlFunctionAlertSqlGenerator.generateSql → MysqlRoutineParam.getDefinition`。

---

## MysqlFunctionCreateSqlGenerator

- 职责：MySQL 创建函数 SQL 生成器，生成 `CREATE FUNCTION` 单条语句（无 DROP）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(MysqlCreateFunctionParam)` | 生成片段 | `CREATE [DEFINER=] FUNCTION 库.名`；参数 `getDefinition(false)`、`RETURNS`、COMMENT/SQL SECURITY/特征/函数体 |
| `String generateSingle(param)` | 生成单条 | `_generate` 后 `buildSqlSingle` |
| `static String generateSqlSingle(param)` | 静态入口 | new 后调用 |

- 调用链：`ShellMysqlClient.createFunction → MysqlFunctionCreateSqlGenerator.generateSqlSingle`。

---

## MysqlProcedureAlertSqlGenerator

- 职责：MySQL 修改存储过程 SQL 生成器，生成删除+重建过程语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlList/sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(MysqlAlertProcedureParam)` | 生成片段 | 先 `DROP PROCEDURE IF EXISTS 库.名;`，再 `CREATE [DEFINER=] PROCEDURE`；参数 `getDefinition(true)`（含 IN/OUT/INOUT 模式）；追加 COMMENT/SQL SECURITY/特征/过程体 |
| `List<String> generate/generateSingle` | 生成列表/单条 | buildSql/buildSqlSingle |
| `static List<String> generateSql/generateSqlSingle` | 静态入口 | new 后调用 |

- 调用链：`ShellMysqlClient.alertProcedure → MysqlProcedureAlertSqlGenerator.generateSql`。

---

## MysqlProcedureCreateSqlGenerator

- 职责：MySQL 创建存储过程 SQL 生成器，生成 `CREATE PROCEDURE` 单条语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(MysqlCreateProcedureParam)` | 生成片段 | `CREATE [DEFINER=] PROCEDURE 库.名`；参数 `getDefinition(true)`；COMMENT/SQL SECURITY/特征/过程体 |
| `String generateSingle/generateSqlSingle` | 生成单条 | buildSqlSingle |

- 调用链：`ShellMysqlClient.createProcedure → MysqlProcedureCreateSqlGenerator.generateSqlSingle`。

---

## MysqlTableAlertSqlGenerator

- 职责：MySQL 修改表 SQL 生成器，按变更差异生成 `ALTER TABLE` 及触发器语句（继承 `DBSqlGenerator`）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| changeFlag | `boolean` | 变更标志位，用于删除末尾逗号 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(MysqlAlertTableParam)` | 生成片段 | 有外键删除时先 `foreignKeyHandle2`；`ALTER TABLE 库.表`；字段/主键/索引/外键/检查依次处理；追加字符集/排序/引擎/注释/行格式/自增；有触发器走 `triggerHandle` |
| `List<String> generate/generateSingle` | 生成列表/单条 | buildSql/buildSqlSingle |
| `void triggerHandle(param)` | 触发器 | 先 DROP（删除或变更的），再 CREATE（新建或变更的） |
| `void columnHandle(builder,param)` | 字段 | 先统一 DROP COLUMN（删除的），再对新增/变更字段用 ADD COLUMN/CHANGE COLUMN（改名）/MODIFY COLUMN；拼类型/长度/小数位/unsigned/zerofill/字符集/默认值/可空/时间戳更新/自增/注释 |
| `void primaryKeyHandle(builder,param)` | 主键 | 存在原主键先 `DROP PRIMARY KEY`；`ADD PRIMARY KEY(...)` 支持 keySize（默认截断 100）`USING BTREE` |
| `void indexHandle(builder,param)` | 索引 | 先 `DROP INDEX`（删除/变更），再 `ADD [类型] INDEX (...)`（新建/变更），含 subPart/USING/COMMENT |
| `void foreignKeyHandle1(builder,param)` | 新增外键 | 对新增/变更外键 `ADD CONSTRAINT ... FOREIGN KEY ... REFERENCES ... ON DELETE/UPDATE` |
| `void foreignKeyHandle2(param)` | 删除外键 | 单独 `ALTER TABLE ... DROP FOREIGN KEY ...`（临时数据名空则跳过） |
| `void checkHandle(builder,param)` | 检查约束 | 先 `DROP CONSTRAINT`（删除/变更），再 `ADD CONSTRAINT ... CHECK (...)` |
| `static List<String> generateSql/generateSqlSingle(param)` | 静态入口 | new 后调用 |

- 调用链：`ShellMysqlClient.alertTable → MysqlTableAlertSqlGenerator.generateSql → 各 handle`。

---

## MysqlTableCreateSqlGenerator

- 职责：MySQL 创建表 SQL 生成器，生成 `CREATE TABLE` 与触发器语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| changeFlag | `boolean` | 变更标志位 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(MysqlCreateTableParam)` | 生成片段 | `CREATE TABLE 库.表 ( ... )`；字段/主键/索引/外键/检查依次拼入；追加表字符集/排序/引擎/注释/行格式/自增；有触发器走 `triggerHandle` |
| `List<String> generate/generateSingle` | 生成列表/单条 | buildSql/buildSqlSingle |
| `void triggerHandle(param)` | 触发器 | 逐触发器 `CREATE TRIGGER ... ON 表 FOR EACH ROW 定义;` |
| `void columnHandle(builder,param)` | 字段 | 拼字段名/类型/长度(,小数位)/值/unsigned/zerofill/字符集/排序/默认值/可空/时间戳更新/自增/注释 |
| `void primaryKeyHandle(builder,param)` | 主键 | `PRIMARY KEY (列...)` |
| `void indexHandle(builder,param)` | 索引 | `[UNIQUE] INDEX 名 (列[(subPart)]...) [USING 方法] [COMMENT ...]` |
| `void foreignKeyHandle(builder,param)` | 外键 | `CONSTRAINT 名 FOREIGN KEY (...) REFERENCES 库.表 (...) ON DELETE/UPDATE` |
| `void checkHandle(builder,param)` | 检查约束 | `CONSTRAINT 名 CHECK (clause)` |
| `static List<String> generateSql/generateSqlSingle(param)` | 静态入口 | new 后调用 |

- 调用链：`ShellMysqlClient.createTable → MysqlTableCreateSqlGenerator.generateSql → 各 handle`。

---

## MysqlViewAlertSqlGenerator

- 职责：MySQL 修改视图 SQL 生成器，生成 `CREATE OR REPLACE VIEW` 单条语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(MysqlAlertViewParam)` | 生成片段 | `CREATE OR REPLACE [ALGORITHM=][DEFINER=][SQL SECURITY] VIEW 库.名 AS 定义`；有检查选项追加 `WITH xxx CHECK OPTION` |
| `String generateSingle/generateSqlSingle` | 生成单条 | buildSqlSingle |

- 调用链：`ShellMysqlClient.alertView → MysqlViewAlertSqlGenerator.generateSqlSingle`。

---

## MysqlViewCreateSqlGenerator

- 职责：MySQL 创建视图 SQL 生成器，生成 `CREATE VIEW` 单条语句（与修改版逻辑一致，去掉 OR REPLACE）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(MysqlCreateViewParam)` | 生成片段 | `CREATE [ALGORITHM=][DEFINER=][SQL SECURITY] VIEW 库.名 AS 定义`；可选 CHECK OPTION |
| `String generateSingle/generateSqlSingle` | 生成单条 | buildSqlSingle |

- 调用链：`ShellMysqlClient.createView → MysqlViewCreateSqlGenerator.generateSqlSingle`。

---

## MysqlIndex

- 职责：MySQL 索引模型，承载索引名/类型/方法/注释/列（含子长度），实现 `ObjectCopier`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| seqIndex | `int` | 序号 |
| type | `String` | 索引类型（NORMAL/UNIQUE 等） |
| method | `String` | 索引方法（BTREE/HASH） |
| comment | `String` | 注释 |
| name | `String` | 索引名称 |
| columns | `List<IndexColumn>` | 索引列列表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String originalName()` / `setName(String)` | 名称变更 | 原始名/记录原始数据 |
| `void addColumn(String,int)` | 添加列 | 追加 `IndexColumn` |
| `boolean isUnique()` | 是否唯一 | 按 type 判定 |
| `void setColumns/setType/setMethod/setComment(...)` | 属性 | 记录原始数据 |
| `void type(String,int)` | 由 TYPE 字段解析 | 依据 noneUnique 值归一化类型 |
| `String typeName()` / `String methodName()` | 类型/方法名（展示用） | 归一化文本 |
| `void copy(MysqlIndex)` | 复制 | 逐属性复制 |
| `boolean isInvalid()` | 是否无效 | 无索引列时无效 |
| `void destroy()` | 销毁 | 清理 |
| `static class IndexColumn` | 内部类 | 索引列（columnName + subPart） |

- 调用链：`ShellMysqlClient.selectIndexes → MysqlIndex`；`MysqlTableCreateSqlGenerator.indexHandle → getColumns/isUnique`。

---

## MysqlIndexControl

- 职责：索引 UI 组件，继承 `MysqlIndex`，生成名称/列/类型/方法/注释编辑控件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| columnList | `List<MysqlColumn>` | 候选字段列表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ClearableTextField getNameControl()` | 名称控件 | 回写 setName |
| `ShellMysqlIndexFieldTextFiled getColumnControl()` | 列控件 | 从 columnList 选列 |
| `ShellMysqlIndexTypeComboBox getTypeControl()` | 类型控件 | 回写 setType |
| `ShellMysqlIndexMethodComboBox getMethodControl()` | 方法控件 | 回写 setMethod |
| `ClearableTextField getCommentControl()` | 注释控件 | 回写 setComment |
| `static MysqlIndexControl of(MysqlIndex)` / `of(List<MysqlIndex>)` | 工厂 | copy 后返回控件 |

- 调用链：`表结构界面 → MysqlIndexControl.of → 各 getXxxControl`。

---

## MysqlAlertProcedureParam

- 职责：修改存储过程参数 DTO（库名/过程/过程名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| procedure | `MysqlProcedure` | 过程对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getDbName/setDbName`、`getProcedure/setProcedure` | 属性访问器 | 纯读写 |
| `getProcedureName/setProcedureName` | 过程名 | 代理 procedure 名称 |

- 调用链：`ShellMysqlClient.alertProcedure(param) → MysqlProcedureAlertSqlGenerator`。

---

## MysqlCreateProcedureParam

- 职责：创建存储过程参数 DTO，结构与 `MysqlAlertProcedureParam` 相同。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| procedure | `MysqlProcedure` | 过程对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getDbName/setDbName`、`getProcedure/setProcedure`、`getProcedureName/setProcedureName` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.createProcedure(param) → MysqlProcedureCreateSqlGenerator`。

---

## MysqlProcedure

- 职责：MySQL 存储过程模型，继承 `MysqlRoutineSchema`，实现 `ObjectCopier`（无额外字段）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 MysqlRoutineSchema：params/dbName/comment/definer/securityType/characteristic 等） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void copy(MysqlProcedure)` | 复制 | 复制基类属性 |

- 调用链：`ShellMysqlClient.selectProcedure(s) → MysqlProcedure`。

---

## MysqlSelectProcedureParam

- 职责：查询存储过程参数 DTO（是否完整/库名/过程名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| full | `boolean` | 是否完整信息 |
| dbName | `String` | 库名称 |
| procedureName | `String` | 过程名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFull/setFull`、`getDbName/setDbName`、`getProcedureName/setProcedureName` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.selectProcedure(param)`。

---

## MysqlDeleteRecordParam

- 职责：删除记录参数 DTO（库/表/记录数据/主键）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| tableName | `String` | 表名称 |
| record | `DBRecordData` | 记录数据（无主键时按全字段定位） |
| primaryKey | `MysqlRecordPrimaryKey` | 主键 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getDbName/setDbName`、`getTableName/setTableName`、`getRecord/setRecord`、`getPrimaryKey/setPrimaryKey` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.deleteRecord(param)`。

---

## MysqlInsertRecordParam

- 职责：插入记录参数 DTO（库/表/记录数据/主键）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| tableName | `String` | 表名称 |
| record | `DBRecordData` | 待插入记录 |
| primaryKey | `MysqlRecordPrimaryKey` | 主键（用于回填自增） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getDbName/setDbName`、`getTableName/setTableName`、`getRecord/setRecord`、`getPrimaryKey/setPrimaryKey` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.insertRecord(param) → getGeneratedKeys → primaryKey.setReturnData`。

---

## MysqlRecord

- 职责：MySQL 记录模型，继承 `DBObject`，以属性表存储字段值并跟踪变更。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| readonly | `boolean`（final） | 是否只读 |
| editable | `boolean` | 是否可编辑 |
| columns | `MysqlColumns` | 字段列表 |
| properties | `HashMap<String,MysqlRecordProperty>` | 字段值属性表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| 4 个构造：`(columns)`/`(columns,readonly)` × `List`/`MysqlColumns` | 构造 | 保存字段与只读标志 |
| `MysqlRecordProperty putValue(String,Object)` / `putValue(MysqlColumn,Object)` | 设值 | 已存在则 setValue，否则新建属性并监听变更 |
| `Object getValue(String)` / `getOriginal(String)` | 取值/原值 | 按列取属性 |
| `Set<String> columns()` / `getProperty(String)` / `hasProperty(...)` | 字段/属性访问 | 属性表查询 |
| `void clear()` / `update(Map<String,Object>)` | 清空/更新 | 更新为逐列 putValue |
| `boolean isChanged()` | 是否变更 | 自身或任一属性变更 |
| `void clearStatus()` / `discard()` | 清除/抛弃变更 | 属性 updateOriginal/discard |
| `void copy(MysqlRecord)` | 复制 | 逐列复制非空值 |
| `DBRecordData getRecordData/getChangedRecordData/getOriginalRecordData()` | 记录数据 | 全量/变更/原始（时间戳 CURRENT_TIMESTAMP 跳过） |
| `boolean isColumnChanged(String)` | 列是否变更 | 属性 isChanged |
| `Map<String,Object> toMap()` | 转 Map | 列→值 |
| `boolean isEditable()/setEditable(boolean)` | 可编辑 | 读写 |
| `void destroy()` | 销毁 | 清空字段与属性 |

- 调用链：`ShellMysqlClient.selectRecords → new MysqlRecord(columns) → putValue`；`updateRecord → getChangedRecordData`；`deleteRecord → getRecordData`。

---

## MysqlRecordFilter

- 职责：MySQL 记录过滤条件模型，继承 `DBRecordFilter`，组合字段与条件并生成输入控件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| condition | `MysqlCondition` | 条件对象 |
| valueBox | `FXHBox` | 值输入容器 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `Object value()` | 取值 | 取控件值（`MysqlConditionUtil.getNodeVal`） |
| `Node getValueControl()` | 值控件 | 按条件生成 |
| `void updateValueControl()` | 刷新值控件 | 条件变更时重建 |
| `DBColumnComboBox getColumnControl()` | 字段控件 | 字段下拉 |
| `DBConditionComboBox getConditionControl()` | 条件控件 | 条件下拉 |
| `String column()` | 字段名 | 当前字段 |
| `String condition()` | 条件 SQL | `condition.wrapCondition(column, value)` |
| `boolean isRequireCondition()` | 是否需值 | 透传条件 |
| `MysqlCondition getCondition()/setCondition(...)` | 条件 | 读写 |

- 调用链：记录过滤界面 → `MysqlRecordFilter.condition() → MysqlCondition.wrapCondition`；`MysqlConditionUtil.buildCondition(filters)`。

---

## MysqlRecordPrimaryKey

- 职责：记录主键模型，承载主键值、原始值、自增回填值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| data | `Object` | 主键值 |
| columnName | `String` | 主键列名 |
| column | `MysqlColumn` | 主键字段 |
| returnData | `Object` | 自增返回值 |
| originalData | `Object` | 原始值 |
| autoIncrement | `boolean` | 是否自增 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void init(MysqlColumn,MysqlRecord)` | 初始化 | 由字段与记录初始化主键值 |
| `Object data()` / `originalData()` | 取当前/原始值 | 优先返回 returnData/data |
| `boolean shouldReturnData()` / `isChanged()` | 是否需回填/变更 | 自增且需返回；与原始值比较 |
| `getData/setData`、`getColumnName/setColumnName`、`getReturnData/setReturnData`、`getOriginalData/setOriginalData`、`isAutoIncrement/setAutoIncrement`、`getColumn` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.insertRecord → getGeneratedKeys → setReturnData`；`deleteRecord/updateRecord → primaryKey.originalData()`。

---

## MysqlRecordProperty

- 职责：记录字段属性，继承 `DBRecordProperty`，绑定字段与记录并跟踪单值变更。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| column | `MysqlColumn` | 字段 |
| record | `MysqlRecord` | 所属记录 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `MysqlRecordProperty(MysqlRecord,MysqlColumn,Object,boolean)` | 构造 | 初始化值与原值 |
| `Object get()` / `void set(Object)` | 取值/设值 | set 触发变更 |
| `Object getValue()` | 取值 | 当前值 |
| `void discard()` / `setChanged(boolean)` / `void updateOriginal()` | 变更管理 | 抛弃/标记/更新原值 |
| `void vCopyAsInsertSql()` / `vCopyAsUpdateSql()` | 复制 SQL 片段 | 生成插入/更新片段（界面复制） |
| `MysqlColumn getColumn()/setColumn(...)` | 字段 | 读写 |
| `void destroy()` | 销毁 | 解绑 |

- 调用链：`MysqlRecord.putValue → new MysqlRecordProperty`。

---

## MysqlSelectRecordParam

- 职责：查询记录参数 DTO（分页/库表/只读/字段/过滤/主键）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| start | `Long` | 起始行 |
| limit | `Long` | 限制行数 |
| dbName | `String` | 库名称 |
| tableName | `String` | 表名称 |
| readonly | `boolean` | 是否只读 |
| columns | `List<MysqlColumn>` | 指定字段（可选） |
| filters | `List<MysqlRecordFilter>` | 过滤条件 |
| primaryKey | `MysqlRecordPrimaryKey` | 主键 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `boolean hasPageControl()` | 是否分页 | start 与 limit 均非空 |
| 其余 `getXxx/setXxx` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.selectRecords/selectRecordCount → hasPageControl/getFilters`。

---

## MysqlUpdateRecordParam

- 职责：更新记录参数 DTO（库/表/原始记录/更新记录/主键）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| tableName | `String` | 表名称 |
| record | `DBRecordData` | 原始记录（无主键时定位） |
| updateRecord | `DBRecordData` | 待更新记录 |
| primaryKey | `MysqlRecordPrimaryKey` | 主键 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getDbName/setDbName`、`getTableName/setTableName`、`getRecord/setRecord`、`getUpdateRecord/setUpdateRecord`、`getPrimaryKey/setPrimaryKey` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.updateRecord(param)`。

---

## MysqlRoutineParam

- 职责：MySQL 例程（函数/过程）参数模型，继承 `DBObject`，承载参数名/类型/模式/长度/值/字符集，并提供 UI 控件与定义拼装。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| name | `String` | 参数名 |
| typeProperty | `StringProperty` | 参数类型属性 |
| mode | `String` | 模式（IN/OUT/INOUT） |
| size | `Integer` | 长度 |
| digits | `Integer` | 小数位 |
| value | `String` | 值/枚举定义 |
| charsetProperty | `StringProperty` | 字符集属性 |
| collation | `String` | 排序规则 |
| dbClient | `ShellMysqlClient` | 数据库客户端（下拉数据源） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String getType()/setType(String)`、`getCharset/setCharset` | 类型/字符集 | 属性读写，记录原始数据 |
| `ClearableTextField getNameControl()` / `DBFiledTypeComboBox getTypeControl()` | UI 控件 | 名称/类型编辑 |
| `ShellMysqlCharsetComboBox getCharsetControl()` / `NumberTextField getDigitsControl()` / `getSizeControl()` | UI 控件 | 字符集/小数位/长度 |
| `List<String> getValueList()` | 值列表 | 按逗号拆分 value |
| `ShellDBEnumTextFiled getValueControl()` / `ShellMysqlCollationComboBox getCollationControl()` / `ShellMysqlParamModeComboBox getModeControl()` | UI 控件 | 值/排序规则/模式 |
| `boolean isReturnParam()` | 是否返回参数 | name 与 mode 均为空 |
| `String getDefinition(boolean mode)` | 拼参数定义 | `[模式] [名] 类型(长度[,小数位]/值) [CHARSET][COLLATE]`，去掉空括号 |
| `void setDtdIdentifier(String)` | 解析定义标识 | 拆类型/长度/小数位/枚举值 |
| 其余 `getXxx/setXxx` | 属性访问器 | 读写（记录原始数据） |
| `void destroy()` | 销毁 | 解绑属性 |

- 调用链：`MysqlFunction/ProcedureCreateSqlGenerator._generate → MysqlRoutineParam.getDefinition`；`ShellMysqlClient.listRoutineParam → setDtdIdentifier`。

---

## MysqlRoutineSchema

- 职责：MySQL 例程基类，实现 `DBRoutineSchema`/`ObjectComparator`，承载参数、库、注释、定义者、安全类型、特征与定义。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| params | `List<MysqlRoutineParam>` | 参数列表 |
| dbName | `String` | 库名称 |
| comment | `String` | 注释 |
| definer | `String` | 定义者 |
| securityType | `String` | 安全类型（DEFINER/INVOKER） |
| characteristic | `String` | 特征（DETERMINISTIC 等） |
| nameProperty | `SimpleStringProperty` | 名称属性 |
| definitionProperty | `SimpleStringProperty` | 定义属性 |
| createDefinitionProperty | `SimpleStringProperty` | 建对象语句属性 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `SimpleStringProperty nameProperty()/setName/getName` | 名称 | 懒加载属性 |
| `SimpleStringProperty definitionProperty()/setDefinition/getDefinition` | 定义 | 懒加载属性 |
| `SimpleStringProperty createDefinitionProperty()/setCreateDefinition/getCreateDefinition` | 建对象语句 | 懒加载属性 |
| `boolean compare(MysqlRoutineSchema)` | 比较 | 名称等比较 |
| `List<MysqlRoutineParam> getParams()/setParams(...)` | 参数 | 读写 |
| `getDbName/setDbName`、`getComment/setComment`、`getDefiner/setDefiner`、`getSecurityType/setSecurityType`、`getCharacteristic/setCharacteristic` | 属性访问器 | 读写 |

- 调用链：`MysqlFunction/MysqlProcedure extends MysqlRoutineSchema`；SQL 生成器读取 `getParams/getDefinition/getSecurityType/getCharacteristic`。

---

## MysqlAlertTableParam

- 职责：修改表参数 DTO，聚合表、字段、索引、外键、检查、触发器及变更判定。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| table | `MysqlTable` | 表对象 |
| checks | `DBObjects<MysqlCheck>` | 检查约束 |
| columns | `MysqlColumns` | 字段列表 |
| indexes | `DBObjects<MysqlIndex>` | 索引 |
| triggers | `DBObjects<MysqlTrigger>` | 触发器 |
| foreignKeys | `DBObjects<MysqlForeignKey>` | 外键 |
| existPrimaryKey | `boolean` | 原表是否已有主键 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String dbName()` / `String tableName()` | 库/表名 | 代理 table |
| `boolean hasColumns/hasIndex/hasForeignKey/hasCheck/hasTrigger()` | 是否存在子项 | 集合非空判断 |
| `List<MysqlColumn> primaryKeys()` | 主键字段 | `columns.primaryKeys()` |
| `boolean primaryKeyChanged()` | 主键是否变更 | 逐字段判断主键变更 |
| `boolean columnChanged()` | 字段是否变更 | columns 存在变更/新增/删除 |
| 其余 `getXxx/setXxx` | 属性访问器 | 读写 |

- 调用链：`ShellMysqlClient.alertTable → MysqlTableAlertSqlGenerator._generate → hasForeignKey/columnChanged/primaryKeyChanged`。

---

## MysqlCreateTableParam

- 职责：创建表参数 DTO，聚合表、字段、索引、外键、检查、触发器（无变更判定）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| table | `MysqlTable` | 表对象 |
| checks | `DBObjects<MysqlCheck>` | 检查约束 |
| columns | `MysqlColumns` | 字段列表 |
| indexes | `DBObjects<MysqlIndex>` | 索引 |
| triggers | `DBObjects<MysqlTrigger>` | 触发器 |
| foreignKeys | `DBObjects<MysqlForeignKey>` | 外键 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String dbName()/tableName()`、`hasColumns/hasIndex/hasForeignKey/hasCheck/hasTrigger()`、`primaryKeys()` | 聚合查询 | 同 `MysqlAlertTableParam` |
| 其余 `getXxx/setXxx` | 属性访问器 | 读写 |

- 调用链：`ShellMysqlClient.createTable → MysqlTableCreateSqlGenerator._generate`。

---

## MysqlSelectTableParam

- 职责：查询表参数 DTO（是否完整/库名/表名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| full | `boolean` | 是否完整信息（含建表语句） |
| dbName | `String` | 库名称 |
| tableName | `String` | 表名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFull/setFull`、`getDbName/setDbName`、`getTableName/setTableName` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.selectTable/selectTables(param)`。

---

## MysqlTable

- 职责：MySQL 表模型，实现 `DBTable`/`ObjectCopier`/`ObjectComparator`，承载表名/注释/引擎/字符集/排序/行格式/自增/建表语句及变更追踪。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| hasPrimaryKey | `boolean` | 是否有主键 |
| rowFormat | `String` | 行格式 |
| autoIncrement | `Long` | 自增当前值 |
| createDefinition | `String` | 建表语句 |
| engine | `String` | 存储引擎 |
| charset | `String` | 字符集 |
| collation | `String` | 排序规则 |
| dbName | `String` | 库名称 |
| schema | `String` | 模式名称 |
| nameProperty | `SimpleStringProperty` | 名称属性 |
| commentProperty | `SimpleStringProperty` | 注释属性 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `setEngine/setCharset/setCollation/setRowFormat/setAutoIncrement(...)` | 属性设置 | 记录原始数据 |
| `boolean isEngineChanged/isCharsetChanged/isCollationChanged/isRowFormatChanged/isAutoIncrementChanged()` | 属性变更 | `checkOriginalData` |
| `hasCharset/hasCollation/hasEngine/hasAutoIncrement/hasRowFormat/hasComment` | 是否存在 | 非空判断 |
| `void setCharsetAndCollation(String)` | 由排序规则推字符集 | 同 MysqlDatabase |
| `void copy(MysqlTable)` / `boolean compare(MysqlTable)` | 复制/比较 | 逐属性 |
| `boolean isInnoDB()` | 是否 InnoDB | engine 判断 |
| `SimpleStringProperty nameProperty()/setName/getName`、`commentProperty()/setComment/getComment` | 名称/注释 | 懒加载属性 |
| `getRowFormat/getAutoIncrement/getCreateDefinition/setCreateDefinition/getEngine/getCharset/getCollation/getDbName/setDbName/getSchema/setSchema` | 属性访问器 | 读写 |
| `boolean isHasPrimaryKey()/setHasPrimaryKey(boolean)` | 是否有主键 | 读写 |
| `void destroy()` | 销毁 | 解绑属性 |

- 调用链：`ShellMysqlClient.selectTable(s) → MysqlTable`；`MysqlTableCreateSqlGenerator._generate → hasCharset/hasEngine/...`。

---

## MysqlSelectTriggerParam

- 职责：查询触发器参数 DTO（是否完整/库/表/触发器名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| full | `boolean` | 是否完整信息 |
| dbName | `String` | 库名称 |
| tableName | `String` | 表名称 |
| triggerName | `String` | 触发器名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFull/setFull`、`getDbName/setDbName`、`getTableName/setTableName`、`getTriggerName/setTriggerName` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.selectTriggers(param)`。

---

## MysqlTrigger

- 职责：MySQL 触发器模型，实现 `DBTrigger`/`ObjectCopier`，承载名称/触发策略/定义/所属表/建触发器语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| name | `String` | 触发器名称 |
| policy | `String` | 触发策略（BEFORE/AFTER INSERT/UPDATE/DELETE） |
| definition | `String` | 触发器体 |
| tableName | `String` | 所属表 |
| createDefinition | `String` | 建触发器语句 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String originalName()` / `setName(String)` | 名称变更 | 原始名/记录原始数据 |
| `void setPolicy(String)` / `void setPolicy(String timing,String manipulation)` | 设策略 | 单参直接设；双参拼 `timing manipulation` |
| `void setDefinition/setTableName(String)` | 定义/表名 | 记录原始数据 |
| `void copy(MysqlTrigger)` | 复制 | 逐属性 |
| `getName/getPolicy/getDefinition/getTableName/getCreateDefinition` / `setCreateDefinition` | 属性访问器 | 读写 |

- 调用链：`ShellMysqlClient.selectTriggers → MysqlTrigger`；`MysqlTableCreateSqlGenerator.triggerHandle → getPolicy/getDefinition`。

---

## MysqlTriggerControl

- 职责：触发器 UI 组件，继承 `MysqlTrigger`，生成名称/策略/定义编辑控件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无新增字段） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ClearableTextField getNameControl()` | 名称控件 | 回写 setName |
| `ShellMysqlTriggerPolicyComboBox getPolicyControl()` | 策略控件 | 回写 setPolicy |
| `EditorEnlargeTextFiled getDefinitionControl()` | 定义控件 | 回写 setDefinition |
| `static MysqlTriggerControl of(MysqlTrigger)` / `of(List<MysqlTrigger>)` | 工厂 | copy 后返回控件 |

- 调用链：`表结构界面 → MysqlTriggerControl.of → 各 getXxxControl`。

---

## MysqlAlertViewParam

- 职责：修改视图参数 DTO（库名/视图）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| view | `MysqlView` | 视图对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getDbName/setDbName`、`getView/setView` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.alertView(param) → MysqlViewAlertSqlGenerator`。

---

## MysqlCreateViewParam

- 职责：创建视图参数 DTO（库名/视图/视图名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbName | `String` | 库名称 |
| view | `MysqlView` | 视图对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getDbName/setDbName`、`getView/setView` | 属性访问器 | 纯读写 |
| `getViewName/setViewName` | 视图名 | 代理 view 名称 |

- 调用链：`ShellMysqlClient.createView(param) → MysqlViewCreateSqlGenerator`。

---

## MysqlSelectViewParam

- 职责：查询视图参数 DTO（是否完整/库名/视图名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| full | `boolean` | 是否完整信息 |
| dbName | `String` | 库名称 |
| viewName | `String` | 视图名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFull/setFull`、`getDbName/setDbName`、`getViewName/setViewName` | 属性访问器 | 纯读写 |

- 调用链：`ShellMysqlClient.selectView(s)(param)`。

---

## MysqlView

- 职责：MySQL 视图模型，实现 `DBView`/`ObjectCopier`/`ObjectComparator`，承载定义者/算法/可更新/检查选项/安全类型/定义/字段。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| definer | `String` | 定义者 |
| algorithm | `String` | 算法（UNDEFINED/MERGE/TEMPTABLE） |
| updatable | `boolean` | 是否可更新 |
| checkOption | `String` | 检查选项 |
| securityType | `String` | 安全类型 |
| definitionProperty | `SimpleStringProperty` | 定义属性 |
| createDefinition | `String` | 建视图语句 |
| dbName | `String` | 库名称 |
| schema | `String` | 模式名称 |
| columns | `MysqlColumns` | 字段列表 |
| nameProperty | `SimpleStringProperty` | 名称属性 |
| commentProperty | `SimpleStringProperty` | 注释属性 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `SimpleStringProperty definitionProperty()/setDefinition/getDefinition` | 定义 | 懒加载属性 |
| `void setCreateDefinition(String)/getCreateDefinition()` | 建视图语句 | 读写 |
| `void copy(MysqlView)` | 复制 | 逐属性 |
| `boolean hasCheckOption()` | 是否有检查选项 | 非空判断 |
| `SimpleStringProperty nameProperty()/setName/getName`、`commentProperty()/setComment/getComment` | 名称/注释 | 懒加载属性 |
| `boolean primaryKeyChanged()` / `List<MysqlColumn> primaryKeys()` / `hasPrimaryKey()` | 主键 | 视图主键判定 |
| `boolean hasColumns()` / `MysqlColumns columns()` / `getColumns/setColumns` | 字段 | 读写 |
| `boolean compare(MysqlView)` | 比较 | 名称等 |
| `void removeColumn(MysqlColumn)` | 移除字段 | columns 移除 |
| `getDefiner/setDefiner`、`getAlgorithm/setAlgorithm`、`isUpdatable/setUpdatable`、`getCheckOption/setCheckOption`、`getSecurityType/setSecurityType` | 属性访问器 | 读写 |
| `getDbName/setDbName`、`getSchema/setSchema` | 库/模式 | 读写 |
| `void destroy()` | 销毁 | 解绑属性 |

- 调用链：`ShellMysqlClient.selectView(s) → MysqlView`；`MysqlViewCreateSqlGenerator._generate → getAlgorithm/getDefiner/getSecurityType/getDefinition`。
