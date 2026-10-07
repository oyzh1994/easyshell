# easyshell 达梦（Dameng）模块代码审查文档

> 范围：`easyshell/src/main/java/cn/oyzh/easyshell/dameng/` 递归全部 `.java`，共 **73 个存活类**。
> 已跳过 15 个整文件被注释的死代码：`dto/DamengConnect`、`dto/ShellDamengDataTransport{Function,Procedure,Table,Trigger,View}`、`database/DamengDatabase`、`check/DamengChecks`、`foreignKey/DamengForeignKeys`、`index/DamengIndexes`、`record/DamengRecordData`、`trigger/DamengTriggers`、`generator/routine/DamengFunctionSqlGenerator`、`generator/routine/DamengProcedureSqlGenerator`。
> 说明：仅新增本文档，未改动任何 `.java` 文件。术语用中文，标识符保留原文。达梦用「模式（schema）」代替 MySQL 的「库」，SQL 生成多用 `DBMS_METADATA.GET_DDL` 与 `COMMENT ON` 独立语句。

---

## ShellDamengClient

- 职责：达梦数据库客户端封装，实现 `ShellBaseClient`/`DBClient`，提供连接管理与模式/表/视图/字段/索引/外键/检查/触发器/函数/存储过程/记录的 CRUD、SQL 执行与批量导入、对象克隆等能力。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| jumpForwarder | `SSHJumpForwarder2` | SSH 端口转发器，启用跳板机时使用 |
| shellConnect | `ShellConnect` | 连接信息 |
| connManager | `DBConnManager` | 连接管理器（实际为 `ShellDamengConnManager`） |
| properties | `Map<String,Object>` | 附加属性表 |
| state | `SimpleObjectProperty<ShellConnState>` | 连接状态属性 |
| stateListener | `ChangeListener<ShellConnState>` | 状态变更监听器 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellDamengClient(ShellConnect)` | 构造 | 保存连接信息并注册 `stateListener` |
| `DBConnManager getConnManager()` | 取连接管理器 | 懒加载 `new ShellDamengConnManager()` |
| `boolean isReadonly()` | 是否只读 | 透传 shellConnect |
| `void start(int timeout)` | 启动连接 | 同 MySQL：initClient → 状态 CONNECTING → `isValid` 判定 → CONNECTED/FAILED |
| `String initHost()` | 计算主机地址 | 启用跳板时 `SSHJumpForwarder2.forward` 得 `127.0.0.1:port`；否则直连 |
| `void initClient()` | 初始化客户端 | 组装 `DBConnConfig`（host/port/user/password/env、代理配置） |
| `void close()/boolean isConnected()` | 关闭/连通判断 | 关闭 connManager/jumpForwarder；`!isClosed()&&isValid(1000)` |
| `int tableSize(String schema)` | 表数量 | `SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER = ?` |
| `int viewSize/procedureSize/functionSize(String schema)` | 视图/过程/函数数量 | ALL_VIEWS/ALL_PROCEDURES/ALL_OBJECTS 统计 |
| `DBQueryResults<DamengExecuteResult> executeSql(String,String)` | 执行 SQL | `DBSqlParser.parseSql` 拆分后逐条 execute，查询解析结果/更新取 updateCount；异常回滚 |
| `DamengExecuteResult executeSingleSql(String,String)` / `void executeSqlSimple(String,String)` | 单条/简单执行 | 单条解析执行；简单执行提交或回滚 |
| `DBQueryResults<DamengExplainResult> explainSql(String,String)` | 执行计划 | 每条前拼 `EXPLAIN ` |
| `int insertBatch(String,List<String>)` / `insertBatch(String,List<String>,boolean)` | 批量插入 | `addBatch`/`executeBatch` 后 commit，可选并行所用新连接 |
| `List<DamengSchema> selectSchemas()` | 模式列表 | `SELECT DISTINCT OBJECT_NAME FROM ALL_OBJECTS WHERE OBJECT_TYPE='SCH'` |
| `DamengSchema schema(String)` | 模式对象 | 包装 `DamengSchema` |
| `List<String> tableSpaces()` | 表空间列表 | 查 `V$TABLESPACE` |
| `boolean existSchema/createSchema/alterSchema/dropSchema(...)` | 模式 DDL | 生成对应 SQL 执行 |
| `List<DamengTable> selectTables(DamengSelectTableParam)` / `selectTables(String)` | 查询表 | 查 ALL_TABLES，`full` 时用 `showCreateTable` |
| `DamengTable selectTable(String,String)/selectTableSimple/selectTable(param)` | 查询单表 | 按模式+表名过滤 |
| `List<DamengColumn> selectColumns(DamengSelectColumnParam)` | 查询字段 | 查 ALL_TAB_COLUMNS + ALL_COL_COMMENTS + ALL_CONS_COLUMNS（LEFT JOIN 判定主键）；再用 `DatabaseMetaData.getColumns` 的 IS_AUTOINCREMENT 判定自增，按 position 排序 |
| `List<DamengRecord> selectRecords(DamengSelectRecordParam)` | 查询记录 | `SELECT *` + 过滤条件 + 分页 |
| `long selectRecordCount(...)` | 记录数 | `SELECT COUNT(*)` |
| `int insertRecord(DamengInsertRecordParam)` | 插入记录 | `INSERT INTO (...)`，`RETURN_GENERATED_KEYS` 回填自增主键 |
| `int deleteRecord(DamengDeleteRecordParam)` | 删除记录 | 有主键按主键删；否则全字段 `= ?`/`IS NULL` 定位 |
| `int updateRecord(DamengUpdateRecordParam)` | 更新记录 | `UPDATE ... SET` + 主键或用原始值（空值 `IS NULL`）定位 |
| `String showCreateTable/View/Function/Procedure/Trigger(String,String)` | 取建对象语句 | `DBMS_METADATA.GET_DDL(...)` / USER_VIEWS |
| `List<DamengIndex> indexes(String,String)` | 查询索引 | ALL_INDEXES/ALL_IND_COLUMNS 装配 |
| `List<DamengCheck> selectChecks(String,String)` | 查询检查约束 | ALL_CONSTRAINTS/ALL_CONS_COLUMNS |
| `List<String> selectForeignKeyTables(String)` | 外键引用表 | 查询具备外键的表名 |
| `List<DamengForeignKey> selectForeignKeys(String,String)` | 查询外键 | ALL_CONSTRAINTS 装配外键及引用策略 |
| `List<DamengColumn> viewColumns(...)` / `List<DamengRecord> viewRecords(...)` | 视图字段/数据 | 视图元数据与数据查询 |
| `void createTable(DamengCreateTableParam)/alertTable(DamengAlertTableParam)` | 建表/改表 | 调 `DamengTableCreateSqlGenerator`/`DamengTableAlertSqlGenerator` |
| `void renameTable/Function/Procedure(...)` | 重命名 | 生成 RENAME/重建语句 |
| `void clearTable/truncateTable/dropTable(...)` | 清空/截断/删表 | DML/DDL 执行 |
| `boolean existView/createView/alertView/dropView(...)` | 视图 DDL | 调视图 SQL 生成器 |
| `List<DamengView> selectViews(...)` / `DamengView selectView(...)` | 查询视图 | 装配视图定义 |
| `List<DamengFunction> selectFunctions(DamengSelectFunctionParam)` | 查询函数 | 查 ALL_OBJECTS+ALL_PROCEDURES，`DBMS_METADATA.GET_DDL` 取定义；解析 PARALLEL_ENABLE/RESULT_CACHE/AGGREGATE/PIPELINED/DETERMINISTIC 为 characteristic，`full` 时 `listFunctionParam` |
| `DamengFunction selectFunction/selectFunctionSimple(...)` | 查询单函数 | 按名过滤 |
| `void createFunction/alertFunction/dropFunction(...)` | 函数 DDL | 调函数 SQL 生成器 |
| `List<DamengProcedure> selectProcedures(...)` / `DamengProcedure selectProcedure(...)` | 查询过程 | 装配过程定义 |
| `void createProcedure/alertProcedure/dropProcedure(...)` | 过程 DDL | 调过程 SQL 生成器 |
| `List<DamengTrigger> selectTriggers(...)` / `selectTriggers(String,String)` | 查询触发器 | ALL_TRIGGERS 装配，`fixTiggerDefinition` 修整触发器体 |
| `List<DamengRoutineParam> listRoutineParam(String,String,String)` | 例程参数 | 查 ALL_ARGUMENTS（PACKAGE_NAME IS NULL），装配 POSITION/DATA_TYPE/SIZE/DIGITS/IN_OUT/ARGUMENT_NAME/CHARSET |
| `List<DamengRoutineParam> listFunctionParam/listProcedureParam(...)` | 函数/过程参数 | 转调 `listRoutineParam` |
| `String cloneTable(String,String,String,boolean)` | 克隆表 | 取建表 DDL 替换新名执行 → `alertTable` 复制检查/外键/触发器 → 可选复制数据，返回新表名 |
| `void cloneView/Function/Procedure(...)` | 克隆对象 | 取 DDL 替换名后执行 |
| `boolean existAutoIncrement(String,String)` | 是否存在自增列 | `SELECT * FROM 表 WHERE 1=0` 后遍历元数据 `isAutoIncrement` |
| `List<String> selectePrimaryKeys(String,String)` | 主键约束名 | ALL_CONSTRAINTS 中 CONSTRAINT_TYPE='P' |
| `boolean isSupportFeature(DBFeature)` / `isSupportCheckFeature()` | 特性支持 | EVENT/CHECK 恒 true |
| `Long getGeneratedKeys(Statement)` | 自增键 | 优先 `getGeneratedKeys()`，否则 `SELECT @@IDENTITY FROM DUAL` |
| `DBDialect dialect()` | 方言 | `DBDialect.DAMENG` |
| `String selectVersion/selectProduct/selectClientCharacter()` | 版本/产品/字符集 | 系统查询 |
| `void printSql(String)` | 打印 SQL | `DBUtil.printSql` + `ShellEventUtil.printSql` |

- 调用链：`ShellDamengClient.start → initClient → initHost`；`executeSql → DBSqlParser.parseSql → Statement.execute`；`selectColumns → ALL_TAB_COLUMNS 查询 + DatabaseMetaData.getColumns`；`createTable → DamengTableCreateSqlGenerator.generate`；`cloneTable → showCreateTable → alertTable`。

---

## ShellDamengConnManager

- 职责：达梦连接管理器，继承 `DBConnManager`，加载达梦 JDBC 驱动并创建连接。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无自持字段；继承父类 config 等） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `Connection initConnection(String dbName,String user,String password)` | 建立连接 | `Class.forName("dm.jdbc.driver.DmDriver")`；拼 host（含库名）；`Configuration.user/password` 写账号；写自定义 env；`DriverManager.getConnection` |
| `String getConnectionString()` | 连接串 | `jdbc:dm://host:port/` |

- 调用链：`ShellDamengClient.start → ShellDamengConnManager.initConnection → DriverManager.getConnection`。

---

## ShellDamengHelper

- 职责：达梦工具类，提供视图可更新性判断、结果集字段解析、触发器定义修整与默认连接环境。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| DEFAULT_ENVIRONMENT | `Map<String,String>`（static） | 默认连接环境参数（默认空，静态块留空） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `boolean isViewUpdatable(Connection,String,String)` | 视图是否可更新 | 查 `USER_VIEWS.READ_ONLY`，为 N 或空即可更新 |
| `DamengColumns parseColumns(ResultSet)` / `parseColumns(ResultSet,List<String>)` | 解析结果集字段 | 遍历 `ResultSetMetaData` 装配 `DamengColumn`（列标签/类型/长度/自增/可空/小数位），支持排除列 |
| `String fixTiggerDefinition(String)` | 修整触发器体 | 截取 ` ROW BEGIN ` 与末尾 `END;` 之间的主体 |
| `String defaultEnvironment()` | 默认环境字符串 | 遍历 `DEFAULT_ENVIRONMENT` 拼 `key=value\n` |

- 调用链：`ShellDamengClient.viewRecords → ShellDamengHelper.parseColumns`；触发器装配 → `fixTiggerDefinition`。

---

## DamengCheck

- 职责：达梦检查约束模型，实现 `DBCheck`/`ObjectCopier`，承载模式/表/名称/子语句及变更追踪。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| tableName | `String` | 表名称 |
| name | `String` | 约束名称 |
| clause | `String` | 检查子语句 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengCheck()` / `DamengCheck(String)` | 构造 | 名称构造 |
| `void setName(String)` / `String getName()` | 名称读写 | set 记录原始数据 |
| `boolean isNameChanged()` / `String originalName()` | 名称变更 | checkOriginalData/getOriginalData |
| `void setClause(String)` / `String getClause()` | 子语句读写 | set 记录原始数据 |
| `boolean isClauseChanged()` | 子语句变更 | checkOriginalData("clause",…) |
| `void copy(DamengCheck)` | 复制 | 复制 name/schema/clause/tableName |
| `boolean isInvalid()` | 是否无效 | 父类无效或 clause 为空 |
| `String getSchema()/setSchema`、`getTableName/setTableName` | 模式/表 | 读写 |

- 调用链：`ShellDamengClient.selectChecks → DamengCheck`；`DamengTableCreateSqlGenerator.checkHandle → getName/getClause`。

---

## DamengCheckControl

- 职责：检查约束 UI 组件，继承 `DamengCheck`，生成名称/子语句输入框。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无新增字段） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ClearableTextField getNameControl()` | 名称控件 | 变更回写 setName |
| `ClearableTextField getClauseControl()` | 子语句控件 | 变更回写 setClause |
| `static DamengCheckControl of(DamengCheck)` / `of(List<DamengCheck>)` | 工厂 | copy 后返回控件 |

- 调用链：`表结构界面 → DamengCheckControl.of → getNameControl/getClauseControl`。

---

## DamengColumn

- 职责：达梦字段模型，实现 `DBColumn`/`ObjectCopier`，承载字段属性、类型/长度/默认值/注释变更追踪与按 DAMENG 方言的能力判定。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| tableName | `String` | 表名称 |
| size | `Integer` | 字段长度 |
| typeProperty | `StringProperty` | 字段类型属性 |
| valueProperty | `StringProperty` | 字段值属性 |
| comment | `String` | 注释 |
| nullable | `Boolean` | 是否可空 |
| unsigned | `Boolean` | 是否无符号 |
| zeroFill | `Boolean` | 是否填充零 |
| updateOnCurrentTimestamp | `Boolean` | 是否按当前时间戳更新 |
| position | `Integer` | 字段位置 |
| primaryKeyProperty | `SimpleBooleanProperty` | 主键属性 |
| primaryKeySize | `Integer` | 主键键长度 |
| defaultValue | `Object` | 默认值 |
| digits | `Integer` | 小数位 |
| autoIncrement | `Boolean` | 是否自增（IDENTITY） |
| name | `String` | 字段名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengColumn()` / `DamengColumn(String)` | 构造 | 名称构造 |
| `void setType(String)` / `String getType()` | 类型 | set 转大写并记录原始数据 |
| `boolean isTypeChanged/isSizeChanged/isDefaultValueChanged/isCommentChanged/isAutoIncrementChanged()` | 属性变更 | 各自 checkOriginalData |
| `boolean isNameChanged()` / `String originalName()` | 名称变更 | checkOriginalData |
| `List<String> getValueList()` | 值列表 | 按逗号拆分 value 去引号 |
| `void setDefaultValue(Object)` / `Object getDefaultValueFix()` | 默认值 | set 记录原始数据；fix 按类型转换（空/null 处理） |
| `void setAutoIncrement(Boolean)` / `boolean isAutoIncrement()` | 自增 | set 记录原始数据 |
| `boolean hasComment()` | 是否有注释 | 非空判断 |
| `void setValue/setUnsigned/isUnsigned/setUpdateOnCurrentTimestamp/isUpdateOnCurrentTimestamp(...)` | 属性 | 记录原始数据 |
| `boolean supportSize/supportUnsigned/supportDigits/supportInteger/supportAutoIncrement/supportDefaultValue/supportTimestamp/supportValue/supportBit/supportBoolean/supportJson/supportJsonArray/supportText/supportKeySize/supportString/supportBinary()` | 类型能力判定 | 委托 `DBColumnFieldManager.*(DBDialect.DAMENG,...)` |
| `Integer suggestSize()` / `Long minValue()/maxValue()` / `Object exampleValue()` | 推荐长度/极值/示例 | 委托 `DBColumnFieldManager` |
| `boolean isYearType/isDateType/isDateTimeType/isTimeType()` | 时间类型判断 | 类型文本判断 |
| `SimpleBooleanProperty primaryKeyProperty()` / `isPrimaryKey/setPrimaryKey` | 主键 | set 记录原始数据 |
| `boolean isColumnChanged()` / `isPrimaryKeyChanged()` | 变更判定 | 字段/主键变更 |
| `void initStatus()` / `void parseKey(String)` | 初始化/解析主键 | 记录原始数据；key 为 pri 置主键 |
| `boolean hasDefaultValue()` | 是否有默认值 | 非空判断 |
| `void copy(DamengColumn)` | 复制 | 逐属性复制 |
| 其余 `getXxx/setXxx` | 属性访问器 | 读写 |
| `void destroy()` | 销毁 | 解绑属性 |

- 调用链：`ShellDamengClient.selectColumns → DamengColumn.setType/parseKey/setAutoIncrement`；`DamengTableCreateSqlGenerator.columnHandle → supportXxx/getXxx`。

---

## DamengColumnControl

- 职责：达梦字段 UI 组件，继承 `DamengColumn`，生成各属性编辑控件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无新增字段） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ClearableTextField getNameControl()/getCommentControl()` | 名称/注释控件 | 变更回写 |
| `NumberTextField getSizeControl()/getDigitsControl()` | 长度/小数位控件 | 变更回写 |
| `DBFiledTypeComboBox getTypeControl()` | 类型控件 | `setDialect(DAMENG)`，选择回写 |
| `FXCheckBox getNullableControl()/getPrimaryKeyControl()` | 可空/主键控件 | 变更回写；主键变更取消可空 |
| `static DamengColumnControl of(DamengColumn)` / `of(List<DamengColumn>)` | 工厂 | copy 后返回控件 |

- 调用链：`表结构界面 → DamengColumnControl.of → 各 getXxxControl`。

---

## DamengColumns

- 职责：字段列表集合，继承 `DBObjectList<DamengColumn>`，提供主键/名称/位置/自增等聚合查询。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无新增字段） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengColumns()` / `DamengColumns(List<DamengColumn>)` | 构造 | addAll |
| `List<DamengColumn> primaryKeys()` | 主键字段 | 过滤未删除主键，自增优先 |
| `boolean primaryKeyChanged()` | 主键变更 | 判定 |
| `DamengColumn column(String)` | 按名取字段 | 忽略大小写 |
| `int index(String)` | 字段位置 | 遍历计位 |
| `List<DamengColumn> sortOfPosition()` | 按位置排序 | 按 getPosition |
| `String tableName()/schema()` | 表名/模式 | 取首个字段属性 |
| `List<String> columnNames()` | 字段名列表 | 全字段名 |
| `boolean hasPrimaryKey()/hasAutoIncrement()` | 是否含主键/自增 | 存在判断 |

- 调用链：`ShellDamengClient.selectColumns 返回 → DamengColumns.sortOfPosition`。

---

## DamengSelectColumnParam

- 职责：查询字段参数 DTO（模式/表名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| tableName | `String` | 表名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengSelectColumnParam()` / `(String schema,String tableName)` | 构造 | 组装查询条件 |
| `getSchema/setSchema`、`getTableName/setTableName` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.selectColumns(param)`。

---

## DamengBetweenCondition

- 职责：介于（BETWEEN）条件，继承 `DamengCondition`，支持数组/集合两值区间（按 DAMENG 方言）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengBetweenCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengBetweenCondition()` | 构造 | `super(I18nHelper.between(),"BETWEEN")` |
| `String wrapCondition(Object)` | 包装条件 | Object[]/Collection 取两值拼 `值 BETWEEN a AND b`（`DBUtil.wrapData(...,DAMENG)`） |

- 调用链：`DamengConditionUtil.generateNode → isBetweenCondition`。

---

## DamengCondition

- 职责：达梦查询条件抽象基类，继承 `DBCondition`，统一按 DAMENG 方言包装条件值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 DBCondition：name 条件名称、value 条件值/SQL 运算符、requireCondition 是否需输入值） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| 3 个构造：`()`、`(name,value)`、`(name,value,requireCondition)` | 构造 | 透传父类 |
| `String wrapCondition(Object)` | 包装条件值 | `DBUtil.wrapData(condition,DAMENG)` |
| `String wrapCondition(String columnName,Object condition)` | 包装完整条件 | 需输入值时 `value + ' ' + wrap`，否则仅 wrap |

- 调用链：具体条件类继承本类；`DamengRecordFilter.condition() → wrapCondition`。

---

## DamengConditionUtil

- 职责：条件工具类，注册全部达梦条件并负责条件 SQL 构建与 UI 节点生成。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无字段，全静态方法） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static void init()` | 注册条件 | 将 20 个条件单例 `DBConditionManager.putCondition(DBDialect.DAMENG, …)` |
| `static String buildCondition(List<DamengRecordFilter>)` | 构建条件 SQL | 逐条 `DBUtil.wrap(column,DAMENG)` + 条件串，用 `getJoinSymbol` 连接 |
| `static boolean isInCondition/isBetweenCondition(DamengCondition)` | 条件类型判断 | 与单例比较 |
| `static List<Node> generateNode(DamengColumn,DamengCondition)` | 生成控件 | IN 类单输入框、BETWEEN 类两节点、其余单节点，按 isRequireCondition 决定禁用 |
| `static void setNodeVal(List<Node>,Object)` / `static Object getNodeVal(List<Node>)` | 控件值 | 设置/读取（值为 List 按位） |

- 调用链：`ShellDamengClient.selectRecords → DamengConditionUtil.buildCondition`；记录过滤界面 → `generateNode`。

---

## DamengContainsCondition

- 职责：包含（LIKE %x%）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengContainsCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengContainsCondition()` / `(String,String)` | 构造 | `super(I18nHelper.contains(),"LIKE")` |
| `String wrapCondition(Object)` | 包装 | 非空时值两侧加 `%` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengEmptyCondition

- 职责：为空（`=''`）条件，无需输入值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengEmptyCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengEmptyCondition()` | 构造 | `super(I18nHelper.isEmpty(),"=''",false)` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengEndWithCondition

- 职责：以指定值结尾（LIKE %x）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengEndWithCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengEndWithCondition()` / `(String,String)` | 构造 | `super(I18nHelper.endWith(),"LIKE")` |
| `String wrapCondition(Object)` | 包装 | 值前加 `%` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengEqCondition

- 职责：等于（`=`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengEqCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengEqCondition()` | 构造 | `super(I18nHelper.eq(),"=")` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengGtCondition

- 职责：大于（`>`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengGtCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengGtCondition()` | 构造 | `super(I18nHelper.gt(),">")` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengGtEqCondition

- 职责：大于等于（`>=`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengGtEqCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengGtEqCondition()` | 构造 | `super(I18nHelper.gtEq(),">=")` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengInListCondition

- 职责：在列表（IN）条件，按 DAMENG 方言将整体值包入括号（不做逗号拆分）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengInListCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengInListCondition()` | 构造 | `super(I18nHelper.inList(),"IN")` |
| `String wrapCondition(Object)` | 包装 | 非空时 `值 IN (wrapData(condition))`（与 MySQL 的逗号拆分不同） |

- 调用链：`DamengConditionUtil.init`。

---

## DamengLtCondition

- 职责：小于（`<`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengLtCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengLtCondition()` | 构造 | `super(I18nHelper.lt(),"<")` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengLtEqCondition

- 职责：小于等于（`<=`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengLtEqCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengLtEqCondition()` | 构造 | `super(I18nHelper.ltEq(),"<=")` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengNotBetweenCondition

- 职责：不介于（NOT BETWEEN）条件，继承 `DamengBetweenCondition`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengNotBetweenCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengNotBetweenCondition()` | 构造 | `super(I18nHelper.notBetween(),"NOT BETWEEN")` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengNotContainsCondition

- 职责：不包含（NOT LIKE %x%）条件，继承 `DamengContainsCondition`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengNotContainsCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengNotContainsCondition()` | 构造 | `super(I18nHelper.notContains(),"NOT LIKE")` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengNotEmptyCondition

- 职责：不为空（`!=''`）条件，无需输入值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengNotEmptyCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengNotEmptyCondition()` | 构造 | `super(I18nHelper.notIsEmpty(),"!=''",false)` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengNotEndWithCondition

- 职责：不以指定值结尾（NOT LIKE %x）条件，继承 `DamengEndWithCondition`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengNotEndWithCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengNotEndWithCondition()` | 构造 | `super(I18nHelper.notEndWith(),"NOT LIKE")` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengNotEqCondition

- 职责：不等于（`!=`）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengNotEqCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengNotEqCondition()` | 构造 | `super(I18nHelper.notEq(),"!=")` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengNotInListCondition

- 职责：不在列表（NOT IN）条件，继承 `DamengInListCondition`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengNotInListCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengNotInListCondition()` | 构造 | `super(I18nHelper.notInList(),"NOT IN")` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengNotNullCondition

- 职责：非空（IS NOT NULL）条件，无需输入值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengNotNullCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengNotNullCondition()` | 构造 | `super(I18nHelper.notIsNull(),"IS NOT NULL",false)` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengNotStartWithCondition

- 职责：不以指定值开头（NOT LIKE x%）条件，继承 `DamengStartWithCondition`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengNotStartWithCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengNotStartWithCondition()` | 构造 | `super(I18nHelper.notStartWith(),"NOT LIKE")` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengNullCondition

- 职责：为空（IS NULL）条件，无需输入值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengNullCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengNullCondition()` | 构造 | `super(I18nHelper.isNull(),"IS NULL",false)` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengStartWithCondition

- 职责：以指定值开头（LIKE x%）条件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `DamengStartWithCondition`（static final） | 单例 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengStartWithCondition()` / `(String,String)` | 构造 | `super(I18nHelper.startWith(),"LIKE")` |
| `String wrapCondition(Object)` | 包装 | 值后加 `%` |

- 调用链：`DamengConditionUtil.init`。

---

## DamengSchema

- 职责：达梦模式模型，实现 `DBSchema`，仅承载模式名称。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| name | `String` | 模式名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String getName()` / `void setName(String)` | 名称 | 读写 |

- 调用链：`ShellDamengClient.selectSchemas/schema → DamengSchema`；`createSchema/alterSchema → DamengSchema`。

---

## DamengForeignKey

- 职责：达梦外键模型，承载外键列、引用模式表列与更新/删除策略。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| name | `String` | 外键名称 |
| columns | `List<String>` | 本表外键列 |
| primaryKeyDatabaseProperty | `SimpleStringProperty` | 引用模式属性 |
| primaryKeyTableProperty | `SimpleStringProperty` | 引用表属性 |
| primaryKeyColumns | `List<String>` | 引用列 |
| deletePolicy | `String` | 删除策略 |
| updatePolicy | `String` | 更新策略 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String originalName()` / `setName(String)` | 名称变更 | 原始名/记录原始数据 |
| `SimpleStringProperty primaryKeyDatabaseProperty()/primaryKeyTableProperty()` | 引用模式/表属性 | 懒加载属性 |
| `void setDeletePolicy/setUpdatePolicy(String)` | 策略 | 记录原始数据 |
| `void setColumns(List<String>)` / `addColumn(String)` | 外键列 | 记录原始数据/追加 |
| `void setPrimaryKeyDatabase/setPrimaryKeyTable(...)` / `getPrimaryKeyDatabase/getPrimaryKeyTable` | 引用模式/表 | 记录原始数据 |
| `void setPrimaryKeyColumns(List<String>)` / `addPrimaryKeyColumn(String)` | 引用列 | 记录原始数据/追加 |
| `void copy(DamengForeignKey)` | 复制 | 逐属性复制 |
| `boolean isInvalid()` | 是否无效 | 无外键列或引用列时无效 |

- 调用链：`ShellDamengClient.selectForeignKeys → DamengForeignKey`；`DamengTableCreateSqlGenerator.foreignKeyHandle → getColumns/getPrimaryKeyColumns`。

---

## DamengForeignKeyControl

- 职责：外键 UI 组件，继承 `DamengForeignKey`，生成名称/列/引用模式表/策略等编辑控件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| dbClient | `ShellDamengClient` | 数据库客户端（联级下拉数据源） |
| columnList | `List<DamengColumn>` | 候选字段列表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `FXTextField getNameControl()` | 名称控件 | 回写 setName |
| `DamengFieldTextFiled getColumnControl()` | 外键列控件 | 从 columnList 选列 |
| `ShellDamengSchemaComboBox getPrimaryKeyDatabaseControl()` | 引用模式控件 | dbClient.selectSchemas 填充 |
| `DamengTableComboBox getPrimaryKeyTableControl()` | 引用表控件 | 按模式加载表 |
| `DamengForeignKeyPolicyComboBox getDeletePolicyControl()/getUpdatePolicyControl()` | 策略控件 | 回写策略 |
| `DamengFieldTextFiled getPrimaryKeyColumnControl()` | 引用列控件 | 按引用表加载列 |
| `String getPrimaryKeyDatabase()` | 取引用模式 | 重写 |
| `static DamengForeignKeyControl of(DamengForeignKey)` / `of(List<DamengForeignKey>)` | 工厂 | copy 后返回控件 |

- 调用链：`表结构界面 → DamengForeignKeyControl.of → dbClient.selectTables/selectColumns`。

---

## DamengAlertFunctionParam

- 职责：修改函数参数 DTO（模式/函数/函数名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| function | `DamengFunction` | 函数对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getSchema/setSchema`、`getFunction/setFunction` | 属性访问器 | 纯读写 |
| `getFunctionName/setFunctionName` | 函数名 | 代理 function 名称 |

- 调用链：`ShellDamengClient.alertFunction(param) → DamengFunctionAlertSqlGenerator`。

---

## DamengCreateFunctionParam

- 职责：创建函数参数 DTO（模式/函数/函数名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| function | `DamengFunction` | 函数对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getSchema/setSchema`、`getFunction/setFunction`、`getFunctionName/setFunctionName` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.createFunction(param) → DamengFunctionCreateSqlGenerator`。

---

## DamengFunction

- 职责：达梦函数模型，继承 `DamengRoutineSchema`，在例程基础上增加返回值参数。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| returnParam | `DamengRoutineParam` | 返回值参数 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void setParams(List<DamengRoutineParam>)` | 设参数 | 重写 |
| `String getReturnType()` | 返回类型 | 取 returnParam 类型 |
| `void copy(DamengFunction)` | 复制 | 复制基类属性与返回参数 |
| `DamengRoutineParam getReturnParam()/setReturnParam(...)` | 返回参数 | 读写 |

- 调用链：`ShellDamengClient.selectFunction(s) → DamengFunction`；`DamengFunctionCreateSqlGenerator._generate → getParams/getReturnParam`。

---

## DamengSelectFunctionParam

- 职责：查询函数参数 DTO（是否完整/模式/函数名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| full | `boolean` | 是否完整信息 |
| schema | `String` | 模式名称 |
| functionName | `String` | 函数名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFull/setFull`、`getSchema/setSchema`、`getFunctionName/setFunctionName` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.selectFunction(param)`。

---

## DamengFunctionAlertSqlGenerator

- 职责：达梦修改函数 SQL 生成器，生成 `CREATE OR REPLACE FUNCTION` 单条语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(DamengAlertFunctionParam)` | 生成片段 | `CREATE OR REPLACE FUNCTION 模式.名 (参数...)`；`RETURN 返回参数`；按 characteristic 追加 PIPELINED/PARALLEL_ENABLE/DETERMINISTIC/RESULT_CACHE/AGGREGATE；`AUTHID 安全类型`；`AS` + 函数体 |
| `String generateSingle/generateSqlSingle(param)` | 生成单条 | buildSqlSingle |

- 调用链：`ShellDamengClient.alertFunction → DamengFunctionAlertSqlGenerator.generateSqlSingle → DamengRoutineParam.getDefinition`。

---

## DamengFunctionCreateSqlGenerator

- 职责：达梦创建函数 SQL 生成器，生成 `CREATE FUNCTION` 单条语句（逻辑与修改版一致，去掉 OR REPLACE）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(DamengCreateFunctionParam)` | 生成片段 | `CREATE FUNCTION 模式.名 (参数...)`；`RETURN`、characteristic、`AUTHID`、`AS` 函数体 |
| `String generateSingle/generateSqlSingle(param)` | 生成单条 | buildSqlSingle |

- 调用链：`ShellDamengClient.createFunction → DamengFunctionCreateSqlGenerator.generateSqlSingle`。

---

## DamengProcedureAlertSqlGenerator

- 职责：达梦修改存储过程 SQL 生成器，生成 `CREATE OR REPLACE PROCEDURE` 单条语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(DamengAlertProcedureParam)` | 生成片段 | `CREATE OR REPLACE PROCEDURE 名 (参数...)`（注意此处只用过程名，未带模式）；characteristic 含 PARALLEL_ENABLE 则追加；`AUTHID 安全类型`；`AS` + 过程体 |
| `List<String> generate/generateSingle` | 生成列表/单条 | buildSql/buildSqlSingle |
| `static List<String> generateSql/generateSqlSingle` | 静态入口 | new 后调用 |

- 调用链：`ShellDamengClient.alertProcedure → DamengProcedureAlertSqlGenerator.generateSql`。

---

## DamengProcedureCreateSqlGenerator

- 职责：达梦创建存储过程 SQL 生成器，生成 `CREATE PROCEDURE` 单条语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(DamengCreateProcedureParam)` | 生成片段 | `CREATE PROCEDURE 名 (参数...)`；characteristic/AUTHID；`AS` 过程体 |
| `String generateSingle/generateSqlSingle(param)` | 生成单条 | buildSqlSingle |

- 调用链：`ShellDamengClient.createProcedure → DamengProcedureCreateSqlGenerator.generateSqlSingle`。

---

## DamengTableAlertSqlGenerator

- 职责：达梦表结构修改 SQL 生成器，按变更差异生成多条 ALTER/COMMENT 语句（继承 `DBSqlGenerator`）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlList/sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(DamengAlertTableParam)` | 主流程 | 顺序：主键变更 → 字段变更 → 索引 → 外键 → 检查 → 触发器；最后表空间（`ALTER TABLE ... MOVE TABLESPACE`）与表注释（`COMMENT ON TABLE`） |
| `void triggerHandle(param)` | 触发器 | 删除/变更的 `DROP TRIGGER`，变更/新增的 `CREATE OR REPLACE TRIGGER ... FOR EACH ROW BEGIN 体; END;` |
| `void columnHandle(param)` | 字段 | 依次：重命名字段（`RENAME COLUMN old TO new`）、删除字段（`DROP COLUMN`）、新增字段（`ADD COLUMN (...)`）、修改字段（存在自增时先 `DROP IDENTITY`，再 `MODIFY`，自增列再 `ADD COLUMN IDENTITY(1,1)`）、列注释（`COMMENT ON COLUMN`） |
| `void primaryKeyHandle(param)` | 主键 | 存在原主键按约束名 `DROP CONSTRAINT ... CASCADE`；`ADD PRIMARY KEY (...)` 支持 keySize |
| `void indexHandle(param)` | 索引 | 删除用独立 `DROP INDEX`（达梦不支持 ALTER DROP INDEX）；新增用独立 `CREATE [类型] INDEX ... ON 表 (...)`（过滤 FULLTEXT/SPATIAL） |
| `void foreignKeyHandle(param)` | 外键 | 删除 `DROP CONSTRAINT`；新增 `ADD CONSTRAINT ... FOREIGN KEY ... REFERENCES ... ON DELETE/UPDATE` |
| `void checkHandle(param)` | 检查约束 | 删除 `DROP CONSTRAINT`；新增 `ADD CONSTRAINT ... CHECK (...)` |
| `void appendColumnType(builder,column,created)` | 拼列类型 | 类型/长度/小数位/值/默认值（自增列跳过）/NULL/IDENTITY（仅新增列） |
| `List<String> generate/generateSingle` / `static generateSql/generateSqlSingle(param)` | 生成入口 | buildSql/buildSqlSingle |

- 调用链：`ShellDamengClient.alertTable → DamengTableAlertSqlGenerator.generateSql → 各 handle`。

---

## DamengTableCreateSqlGenerator

- 职责：达梦建表 SQL 生成器，生成 `CREATE TABLE` 及触发器/注释独立语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| changeFlag | `boolean` | 变更标志位 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(DamengCreateTableParam)` | 主流程 | `CREATE TABLE 模式.表 ( ... )`；字段/主键/索引/外键/检查拼入；追加表空间 `STORAGE (ON xxx)`；触发器与表注释（`COMMENT ON TABLE`）为独立语句 |
| `void triggerHandle(param)` | 触发器 | `CREATE OR REPLACE TRIGGER ... FOR EACH ROW BEGIN 体; END;`（体末尾补 `;`） |
| `void columnHandle(builder,param)` | 字段 | 拼名/类型/长度/值；自增列用 `IDENTITY(1,1)`（且不加 DEFAULT）；NULL/NOT NULL；列注释生成独立 `COMMENT ON COLUMN "模式"."表"."列" IS ...;` |
| `void primaryKeyHandle(builder,param)` | 主键 | `PRIMARY KEY (列...)` |
| `void indexHandle(builder,param)` | 索引 | `[UNIQUE] INDEX 名 (列...) [USING 方法]` |
| `void foreignKeyHandle(builder,param)` | 外键 | `CONSTRAINT 名 FOREIGN KEY (...) REFERENCES 模式.表 (...) ON DELETE/UPDATE` |
| `void checkHandle(builder,param)` | 检查约束 | `CONSTRAINT 名 CHECK (clause)` |
| `List<String> generate/generateSingle` / `static generateSql/generateSqlSingle` | 生成入口 | buildSql/buildSqlSingle |

- 调用链：`ShellDamengClient.createTable → DamengTableCreateSqlGenerator.generateSql → 各 handle`。

---

## DamengViewAlertSqlGenerator

- 职责：达梦修改视图 SQL 生成器，生成 `CREATE OR REPLACE VIEW` 及视图注释语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlList/sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(DamengAlertViewParam)` | 生成片段 | `CREATE OR REPLACE VIEW 模式.视图 AS 定义`；有安全类型追加 `BEQUEATH xxx`；不可更新追加 `WITH READ ONLY`；视图注释生成独立 `COMMENT ON VIEW` |
| `List<String> generate/generateSingle` / `static generateSql/generateSqlSingle` | 生成入口 | buildSql/buildSqlSingle |

- 调用链：`ShellDamengClient.alertView → DamengViewAlertSqlGenerator.generateSql`。

---

## DamengViewCreateSqlGenerator

- 职责：达梦创建视图 SQL 生成器，生成 `CREATE VIEW` 及视图注释语句（与修改版逻辑一致，去掉 OR REPLACE）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 sqlList/sqlBuilder） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void _generate(DamengCreateViewParam)` | 生成片段 | `CREATE VIEW 模式.视图 AS 定义`；`BEQUEATH`、`WITH READ ONLY`；视图注释独立 `COMMENT ON VIEW` |
| `List<String> generate/generateSingle` / `static generateSql/generateSqlSingle` | 生成入口 | buildSql/buildSqlSingle |

- 调用链：`ShellDamengClient.createView → DamengViewCreateSqlGenerator.generateSql`。

---

## DamengIndex

- 职责：达梦索引模型，承载索引名/类型/方法/列（含子长度）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| seqIndex | `int` | 序号 |
| type | `String` | 索引类型 |
| method | `String` | 索引方法 |
| name | `String` | 索引名称 |
| columns | `List<IndexColumn>` | 索引列列表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String originalName()` / `setName(String)` | 名称变更 | 原始名/记录原始数据 |
| `void addColumn(String)` | 添加列 | 追加 `IndexColumn` |
| `boolean isUnique()` | 是否唯一 | 按 type 判定 |
| `void setColumns/setType/setMethod(...)` | 属性 | 记录原始数据 |
| `void type(String,int)` | 由 TYPE 字段解析 | 依据 noneUnique 归一化类型 |
| `String typeName()/methodName()` | 类型/方法名 | 展示文本 |
| `void copy(DamengIndex)` | 复制 | 逐属性 |
| `boolean isInvalid()` | 是否无效 | 无索引列时无效 |
| `static class IndexColumn` | 内部类 | 索引列（columnName + subPart） |

- 调用链：`ShellDamengClient.indexes → DamengIndex`；`DamengTableCreateSqlGenerator.indexHandle → getColumns/isUnique`。

---

## DamengIndexControl

- 职责：索引 UI 组件，继承 `DamengIndex`，生成名称/列/类型/方法编辑控件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| columnList | `List<DamengColumn>` | 候选字段列表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ClearableTextField getNameControl()` | 名称控件 | 回写 setName |
| `DamengIndexFieldTextFiled getColumnControl()` | 列控件 | 从 columnList 选列 |
| `DamengIndexTypeComboBox getTypeControl()` | 类型控件 | 回写 setType |
| `DamengIndexMethodComboBox getMethodControl()` | 方法控件 | 回写 setMethod |
| `static DamengIndexControl of(DamengIndex)` / `of(List<DamengIndex>)` | 工厂 | copy 后返回控件 |

- 调用链：`表结构界面 → DamengIndexControl.of → 各 getXxxControl`。

---

## DamengAlertProcedureParam

- 职责：修改存储过程参数 DTO（模式/过程/过程名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| procedure | `DamengProcedure` | 过程对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getSchema/setSchema`、`getProcedure/setProcedure` | 属性访问器 | 纯读写 |
| `getProcedureName/setProcedureName` | 过程名 | 代理 procedure 名称 |

- 调用链：`ShellDamengClient.alertProcedure(param) → DamengProcedureAlertSqlGenerator`。

---

## DamengCreateProcedureParam

- 职责：创建存储过程参数 DTO（模式/过程/过程名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| procedure | `DamengProcedure` | 过程对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getSchema/setSchema`、`getProcedure/setProcedure`、`getProcedureName/setProcedureName` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.createProcedure(param) → DamengProcedureCreateSqlGenerator`。

---

## DamengProcedure

- 职责：达梦存储过程模型，继承 `DamengRoutineSchema`（无额外字段）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （继承 DamengRoutineSchema：params/schema/securityType/characteristic 等） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void copy(DamengProcedure)` | 复制 | 复制基类属性 |

- 调用链：`ShellDamengClient.selectProcedure(s) → DamengProcedure`。

---

## DamengSelectProcedureParam

- 职责：查询存储过程参数 DTO（是否完整/模式/过程名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| full | `boolean` | 是否完整信息 |
| schema | `String` | 模式名称 |
| procedureName | `String` | 过程名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFull/setFull`、`getSchema/setSchema`、`getProcedureName/setProcedureName` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.selectProcedure(param)`。

---

## DamengDeleteRecordParam

- 职责：删除记录参数 DTO（模式/表/记录数据/主键）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| tableName | `String` | 表名称 |
| record | `DBRecordData` | 记录数据（无主键时定位） |
| primaryKey | `DamengRecordPrimaryKey` | 主键 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getSchema/setSchema`、`getTableName/setTableName`、`getRecord/setRecord`、`getPrimaryKey/setPrimaryKey` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.deleteRecord(param)`。

---

## DamengInsertRecordParam

- 职责：插入记录参数 DTO（模式/表/记录数据/主键）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| tableName | `String` | 表名称 |
| record | `DBRecordData` | 待插入记录 |
| primaryKey | `DamengRecordPrimaryKey` | 主键（自增回填） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getSchema/setSchema`、`getTableName/setTableName`、`getRecord/setRecord`、`getPrimaryKey/setPrimaryKey` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.insertRecord(param) → getGeneratedKeys → primaryKey.setReturnData`。

---

## DamengRecord

- 职责：达梦记录模型，继承 `DBObject`，以属性表存储字段值并跟踪变更。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| readonly | `boolean`（final） | 是否只读 |
| editable | `boolean` | 是否可编辑 |
| columns | `DamengColumns` | 字段列表 |
| properties | `HashMap<String,DamengRecordProperty>` | 字段值属性表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| 4 个构造：`(columns)`/`(columns,readonly)` × `List`/`DamengColumns` | 构造 | 保存字段与只读标志 |
| `DamengRecordProperty putValue(String,Object)/putValue(DamengColumn,Object)` | 设值 | 已存在则 setValue，否则新建属性并监听变更 |
| `Object getValue(String)/getOriginal(String)` | 取值/原值 | 按列取属性 |
| `Set<String> columns()` / `getProperty(String)` / `hasProperty(...)` | 字段/属性访问 | 属性表查询 |
| `void clear()/update(Map<String,Object>)` | 清空/更新 | 更新为逐列 putValue |
| `boolean isChanged()` | 是否变更 | 自身或任一属性变更 |
| `void clearStatus()/discard()` | 清除/抛弃变更 | 属性 updateOriginal/discard |
| `void copy(DamengRecord)` | 复制 | 逐列复制非空值 |
| `DBRecordData getRecordData/getChangedRecordData/getOriginalRecordData()` | 记录数据 | 全量/变更/原始（时间戳 CURRENT_TIMESTAMP 跳过） |
| `boolean isColumnChanged(String)` | 列变更 | 属性 isChanged |
| `Map<String,Object> toMap()` | 转 Map | 列→值 |
| `boolean isEditable()/setEditable(boolean)` | 可编辑 | 读写 |
| `void destroy()` | 销毁 | 清空字段与属性 |

- 调用链：`ShellDamengClient.selectRecords → new DamengRecord(columns) → putValue`；`updateRecord → getChangedRecordData`。

---

## DamengRecordFilter

- 职责：达梦记录过滤条件模型，继承 `DBRecordFilter`，组合字段与条件并生成输入控件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| condition | `DamengCondition` | 条件对象 |
| valueBox | `FXHBox` | 值输入容器 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `Object value()` | 取值 | 取控件值（`DamengConditionUtil.getNodeVal`） |
| `Node getValueControl()` / `void updateValueControl()` | 值控件/刷新 | 按条件生成，条件变更时重建 |
| `DBColumnComboBox getColumnControl()` / `DBConditionComboBox getConditionControl()` | 字段/条件控件 | 下拉 |
| `String column()` / `String condition()` | 字段名/条件 SQL | `condition.wrapCondition(column,value)` |
| `boolean isRequireCondition()` | 是否需值 | 透传条件 |
| `DamengCondition getCondition()/setCondition(...)` | 条件 | 读写 |

- 调用链：记录过滤界面 → `DamengRecordFilter.condition() → DamengCondition.wrapCondition`。

---

## DamengRecordPrimaryKey

- 职责：记录主键模型，承载主键值、原始值、自增回填值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| data | `Object` | 主键值 |
| columnName | `String` | 主键列名 |
| column | `DamengColumn` | 主键字段 |
| returnData | `Object` | 自增返回值 |
| originalData | `Object` | 原始值 |
| autoIncrement | `boolean` | 是否自增 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void init(DamengColumn,DamengRecord)` | 初始化 | 由字段与记录初始化主键值 |
| `Object data()/originalData()` | 取当前/原始值 | 优先返回 returnData/data |
| `boolean shouldReturnData()/isChanged()` | 是否需回填/变更 | 自增且需返回；与原始值比较 |
| `getData/setData`、`getColumnName/setColumnName`、`getReturnData/setReturnData`、`getOriginalData/setOriginalData`、`isAutoIncrement/setAutoIncrement`、`getColumn` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.insertRecord → getGeneratedKeys → setReturnData`。

---

## DamengRecordProperty

- 职责：记录字段属性，继承 `DBRecordProperty`，绑定字段与记录并跟踪单值变更。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| column | `DamengColumn` | 字段 |
| record | `DamengRecord` | 所属记录 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `DamengRecordProperty(DamengRecord,DamengColumn,Object,boolean)` | 构造 | 初始化值与原值 |
| `Object get()/set(Object)` | 取值/设值 | set 触发变更 |
| `Object getValue()` | 取值 | 当前值 |
| `void discard()/setChanged(boolean)/updateOriginal()` | 变更管理 | 抛弃/标记/更新原值 |
| `void vCopyAsInsertSql()/vCopyAsUpdateSql()` | 复制 SQL 片段 | 生成插入/更新片段 |
| `DamengColumn getColumn()/setColumn(...)` | 字段 | 读写 |
| `void destroy()` | 销毁 | 解绑 |

- 调用链：`DamengRecord.putValue → new DamengRecordProperty`。

---

## DamengSelectRecordParam

- 职责：查询记录参数 DTO（分页/模式表/只读/字段/过滤/主键）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| start | `Long` | 起始行 |
| limit | `Long` | 限制行数 |
| schema | `String` | 模式名称 |
| tableName | `String` | 表名称 |
| readonly | `boolean` | 是否只读 |
| columns | `List<DamengColumn>` | 指定字段 |
| filters | `List<DamengRecordFilter>` | 过滤条件 |
| primaryKey | `DamengRecordPrimaryKey` | 主键 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `boolean hasPageControl()` | 是否分页 | start 与 limit 均非空 |
| 其余 `getXxx/setXxx` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.selectRecords/selectRecordCount → hasPageControl/getFilters`。

---

## DamengUpdateRecordParam

- 职责：更新记录参数 DTO（模式/表/原始记录/更新记录/主键）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| tableName | `String` | 表名称 |
| record | `DBRecordData` | 原始记录（无主键时定位） |
| updateRecord | `DBRecordData` | 待更新记录 |
| primaryKey | `DamengRecordPrimaryKey` | 主键 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getSchema/setSchema`、`getTableName/setTableName`、`getRecord/setRecord`、`getUpdateRecord/setUpdateRecord`、`getPrimaryKey/setPrimaryKey` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.updateRecord(param)`。

---

## DamengRoutineParam

- 职责：达梦例程（函数/过程）参数模型，继承 `DBObject`，承载参数名/类型/模式/长度/值/字符集，并提供 UI 控件与定义拼装。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| dbClient | `ShellDamengClient` | 数据库客户端（下拉数据源） |
| name | `String` | 参数名 |
| typeProperty | `StringProperty`（final） | 参数类型属性 |
| mode | `String` | 模式（IN/OUT/IN OUT） |
| size | `Integer` | 长度 |
| digits | `Integer` | 小数位 |
| position | `Integer` | 参数位置 |
| value | `String` | 值/枚举定义 |
| charsetProperty | `StringProperty`（final） | 字符集属性 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String getType()/setType(String)`、`getCharset/setCharset` | 类型/字符集 | 属性读写，记录原始数据 |
| `Integer getPosition()/setPosition(...)` | 位置 | 读写 |
| `ClearableTextField getNameControl()` / `DBFiledTypeComboBox getTypeControl()` | UI 控件 | 名称/类型编辑 |
| `NumberTextField getDigitsControl()/getSizeControl()` | UI 控件 | 小数位/长度 |
| `List<String> getValueList()` | 值列表 | 按逗号拆分 value |
| `ShellDBEnumTextFiled getValueControl()` / `DamengParamModeComboBox getModeControl()` | UI 控件 | 值/模式 |
| `boolean isReturnParam()` | 是否返回参数 | name 为 `V_RET`、mode 为 OUT、position 为 0 |
| `String getDefinition()` | 拼参数定义 | `名 [IN/OUT/IN OUT] 类型(长度[,小数位]/值)`，去掉空括号 |
| `void setDtdIdentifier(String)` | 解析定义标识 | 拆类型/长度/小数位 |
| 其余 `getXxx/setXxx` | 属性访问器 | 读写（记录原始数据） |

- 调用链：`DamengFunction/ProcedureCreateSqlGenerator._generate → DamengRoutineParam.getDefinition`；`ShellDamengClient.listRoutineParam → setDtdIdentifier`。

---

## DamengRoutineSchema

- 职责：达梦例程基类，承载参数、模式、安全类型、特征与定义。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| params | `List<DamengRoutineParam>` | 参数列表 |
| schema | `String` | 模式名称 |
| securityType | `String` | 安全类型（AUTHID） |
| characteristic | `String` | 特征（PIPELINED/PARALLEL_ENABLE/DETERMINISTIC/RESULT_CACHE/AGGREGATE） |
| nameProperty | `SimpleStringProperty` | 名称属性 |
| definitionProperty | `SimpleStringProperty` | 定义属性 |
| createDefinitionProperty | `SimpleStringProperty` | 建对象语句属性 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `SimpleStringProperty nameProperty()/setName/getName` | 名称 | 懒加载属性 |
| `SimpleStringProperty definitionProperty()/setDefinition/getDefinition` | 定义 | 懒加载属性 |
| `SimpleStringProperty createDefinitionProperty()/setCreateDefinition/getCreateDefinition` | 建对象语句 | 懒加载属性 |
| `boolean compare(DamengRoutineSchema)` | 比较 | 名称等比较 |
| `List<DamengRoutineParam> getParams()/setParams(...)` | 参数 | 读写 |
| `getSchema/setSchema`、`getSecurityType/setSecurityType`、`getCharacteristic/setCharacteristic` | 属性访问器 | 读写 |

- 调用链：`DamengFunction/DamengProcedure extends DamengRoutineSchema`；SQL 生成器读取 `getParams/getDefinition/getSecurityType/getCharacteristic`。

---

## DamengAlertTableParam

- 职责：修改表参数 DTO，聚合表、字段、索引、外键、检查、触发器及变更判定，并携带自增/主键约束信息。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| table | `DamengTable` | 表对象 |
| checks | `DBObjects<DamengCheck>` | 检查约束 |
| columns | `DamengColumns` | 字段列表 |
| indexes | `DBObjects<DamengIndex>` | 索引 |
| triggers | `DBObjects<DamengTrigger>` | 触发器 |
| foreignKeys | `DBObjects<DamengForeignKey>` | 外键 |
| existAutoIncrement | `boolean` | 原表是否存在自增列 |
| primaryKeys | `List<String>` | 原主键约束名列表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `boolean hasColumns/hasIndex/hasForeignKey/hasCheck/hasTrigger()` | 是否存在子项 | 集合非空判断 |
| `List<DamengColumn> primaryKeys()` | 主键字段 | columns.primaryKeys() |
| `boolean primaryKeyChanged()/columnChanged()` | 变更判定 | 主键/字段是否变更 |
| `String tableName()/getSchema()` | 表名/模式 | 代理 table |
| `boolean isExistAutoIncrement()/setExistAutoIncrement(...)` | 是否存在自增 | 读写 |
| `boolean isExistPrimaryKey()` / `setPrimaryKeys(List<String>)/getPrimaryKeys()` | 主键约束 | 是否已有主键；约束名列表 |
| 其余 `getXxx/setXxx` | 属性访问器 | 读写 |

- 调用链：`ShellDamengClient.alertTable → DamengTableAlertSqlGenerator._generate → hasForeignKey/columnChanged/primaryKeyChanged/isExistAutoIncrement`。

---

## DamengCreateTableParam

- 职责：创建表参数 DTO，聚合表、字段、索引、外键、检查、触发器。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| table | `DamengTable` | 表对象 |
| checks | `DBObjects<DamengCheck>` | 检查约束 |
| columns | `DamengColumns` | 字段列表 |
| indexes | `DBObjects<DamengIndex>` | 索引 |
| triggers | `DBObjects<DamengTrigger>` | 触发器 |
| foreignKeys | `DBObjects<DamengForeignKey>` | 外键 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String schema()/tableName()`、`hasColumns/hasIndex/hasForeignKey/hasCheck/hasTrigger()`、`primaryKeys()` | 聚合查询 | 同 `DamengAlertTableParam` |
| 其余 `getXxx/setXxx` | 属性访问器 | 读写 |

- 调用链：`ShellDamengClient.createTable → DamengTableCreateSqlGenerator._generate`。

---

## DamengSelectTableParam

- 职责：查询表参数 DTO（是否完整/模式/表名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| full | `boolean` | 是否完整信息（含建表语句） |
| schema | `String` | 模式名称 |
| tableName | `String` | 表名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFull/setFull`、`getSchema/setSchema`、`getTableName/setTableName` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.selectTable/selectTables(param)`。

---

## DamengTable

- 职责：达梦表模型，实现 `DBTable`/`ObjectCopier`/`ObjectComparator`，承载表名/注释/模式/表空间/建表语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| hasPrimaryKey | `boolean` | 是否有主键 |
| createDefinition | `String` | 建表语句 |
| tableSpace | `String` | 表空间 |
| schema | `String` | 模式名称 |
| nameProperty | `SimpleStringProperty` | 名称属性 |
| commentProperty | `SimpleStringProperty` | 注释属性 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void setTableSpace(String)` / `String getTableSpace()` | 表空间 | set 记录原始数据 |
| `boolean isTableSpaceChanged()` / `hasTableSpace()` | 表空间变更/是否存在 | checkOriginalData/非空 |
| `void copy(DamengTable)` / `boolean compare(DamengTable)` | 复制/比较 | 逐属性 |
| `SimpleStringProperty nameProperty()/setName/getName`、`commentProperty()/setComment/getComment` | 名称/注释 | 懒加载属性 |
| `boolean hasComment()` | 是否有注释 | 非空判断 |
| `boolean isHasPrimaryKey()/setHasPrimaryKey(boolean)` | 是否有主键 | 读写 |
| `getCreateDefinition/setCreateDefinition`、`getSchema/setSchema` | 建表语句/模式 | 读写 |

- 调用链：`ShellDamengClient.selectTable(s) → DamengTable`；`DamengTableCreateSqlGenerator._generate → hasTableSpace/getTableSpace/hasComment`。

---

## DamengSelectTriggerParam

- 职责：查询触发器参数 DTO（是否完整/模式/表/触发器名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| full | `boolean` | 是否完整信息 |
| schema | `String` | 模式名称 |
| tableName | `String` | 表名称 |
| triggerName | `String` | 触发器名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFull/setFull`、`getSchema/setSchema`、`getTableName/setTableName`、`getTriggerName/setTriggerName` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.selectTriggers(param)`。

---

## DamengTrigger

- 职责：达梦触发器模型，实现 `DBTrigger`/`ObjectCopier`，承载名称/模式/触发策略/定义/所属表/建触发器语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| name | `String` | 触发器名称 |
| schema | `String` | 模式名称 |
| policy | `String` | 触发策略（BEFORE/AFTER INSERT/UPDATE/DELETE） |
| definition | `String` | 触发器体 |
| tableName | `String` | 所属表 |
| createDefinition | `String` | 建触发器语句 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `String originalName()` / `setName(String)` | 名称变更 | 原始名/记录原始数据 |
| `void setPolicy(String)` | 设策略 | 记录原始数据 |
| `void setPolicy(String timing,String manipulation)` | 由时机与操作设策略 | 拼 `timing.toUpperCase() + ' ' + manipulation.toUpperCase()` |
| `void setDefinition/setTableName(String)` | 定义/表名 | 记录原始数据 |
| `void copy(DamengTrigger)` | 复制 | 逐属性 |
| `getName/getPolicy/getDefinition/getTableName/getCreateDefinition` / `getSchema/setSchema` / `setCreateDefinition` | 属性访问器 | 读写 |

- 调用链：`ShellDamengClient.selectTriggers → DamengTrigger`；`DamengTableCreateSqlGenerator.triggerHandle → getPolicy/getDefinition`。

---

## DamengTriggerControl

- 职责：触发器 UI 组件，继承 `DamengTrigger`，生成名称/策略/定义编辑控件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无新增字段） | | |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ClearableTextField getNameControl()` | 名称控件 | 回写 setName |
| `DamengTriggerPolicyComboBox getPolicyControl()` | 策略控件 | 回写 setPolicy |
| `EditorEnlargeTextFiled getDefinitionControl()` | 定义控件 | 回写 setDefinition |
| `static DamengTriggerControl of(DamengTrigger)` / `of(List<DamengTrigger>)` | 工厂 | copy 后返回控件 |

- 调用链：`表结构界面 → DamengTriggerControl.of → 各 getXxxControl`。

---

## DamengAlertViewParam

- 职责：修改视图参数 DTO（模式/视图/视图名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| view | `DamengView` | 视图对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getSchema/setSchema`、`getView/setView` | 属性访问器 | 纯读写 |
| `getViewName/setViewName` | 视图名 | 代理 view 名称 |

- 调用链：`ShellDamengClient.alertView(param) → DamengViewAlertSqlGenerator`。

---

## DamengCreateViewParam

- 职责：创建视图参数 DTO（模式/视图/视图名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| schema | `String` | 模式名称 |
| view | `DamengView` | 视图对象 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getSchema/setSchema`、`getView/setView` | 属性访问器 | 纯读写 |
| `getViewName/setViewName` | 视图名 | 代理 view 名称 |

- 调用链：`ShellDamengClient.createView(param) → DamengViewCreateSqlGenerator`。

---

## DamengSelectViewParam

- 职责：查询视图参数 DTO（是否完整/模式/视图名）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| full | `boolean` | 是否完整信息 |
| schema | `String` | 模式名称 |
| viewName | `String` | 视图名称 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFull/setFull`、`getSchema/setSchema`、`getViewName/setViewName` | 属性访问器 | 纯读写 |

- 调用链：`ShellDamengClient.selectView(s)(param)`。

---

## DamengView

- 职责：达梦视图模型，实现 `DBView`/`ObjectCopier`/`ObjectComparator`，承载可更新/安全类型/定义/字段/注释。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| updatable | `boolean` | 是否可更新 |
| securityType | `String` | 安全类型（BEQUEATH） |
| definitionProperty | `SimpleStringProperty` | 定义属性 |
| createDefinition | `String` | 建视图语句 |
| schema | `String` | 模式名称 |
| columns | `DamengColumns` | 字段列表 |
| nameProperty | `SimpleStringProperty` | 名称属性 |
| commentProperty | `SimpleStringProperty` | 注释属性 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `SimpleStringProperty definitionProperty()/setDefinition/getDefinition` | 定义 | 懒加载属性 |
| `void setCreateDefinition(String)/getCreateDefinition()` | 建视图语句 | 读写 |
| `void copy(DamengView)` / `boolean compare(DamengView)` | 复制/比较 | 逐属性 |
| `SimpleStringProperty nameProperty()/setName/getName`、`commentProperty()/setComment/getComment` | 名称/注释 | 懒加载属性 |
| `boolean hasComment()` | 是否有注释 | 非空判断 |
| `boolean primaryKeyChanged()` / `List<DamengColumn> primaryKeys()` / `hasPrimaryKey()` | 主键 | 视图主键判定 |
| `boolean hasColumns()` / `DamengColumns columns()` / `getColumns/setColumns` | 字段 | 读写 |
| `void removeColumn(DamengColumn)` | 移除字段 | columns 移除 |
| `boolean isUpdatable()/setUpdatable(boolean)`、`getSecurityType/setSecurityType` | 可更新/安全类型 | 读写 |
| `getDefinitionProperty/setDefinitionProperty`、`definitionPropertyProperty()` | 定义属性 | 兼容访问器 |
| `getSchema/setSchema` | 模式 | 读写 |

- 调用链：`ShellDamengClient.selectView(s) → DamengView`；`DamengViewCreateSqlGenerator._generate → getDefinition/getSecurityType/isUpdatable/getComment`。
