# SQLite 数据库管理功能需求与设计

## 1. 文档目的

本文档定义 EasyShell SQLite 数据库管理功能的范围、架构、接口、异常策略、测试与验收标准。设计以现有 MySQL、MariaDB、达梦实现为参照，但不复制服务端数据库的多库、多模式、存储过程、事件和调度器能力。

## 2. 目标用户

- 需要在本机创建、打开和维护 SQLite 文件的开发与测试人员。
- 需要浏览表、视图、字段和数据，并执行 SQL 的数据分析人员。
- 需要通过统一 JavaFX 客户端管理 SQLite、MySQL 和达梦连接的桌面用户。

## 3. 功能范围

### 3.1 本期范围

- SQLite 连接的新建、编辑、测试、打开和关闭。
- SQLite 文件的新建、选择、路径校验、丢失提示和只读打开。
- 数据库版本、文件路径、文件大小、页大小、编码、事务模式和自动外键等信息展示。
- 表和视图浏览，包含对象名称、类型、SQL 定义、字段名、声明类型、非空、主键、默认值和行数。
- SQL 编辑、格式化、多语句执行、查询结果展示、更新数量与耗时展示。
- 表设计器支持字段、主键、非空、唯一、默认值、CHECK、生成列、`WITHOUT ROWID`、`STRICT` 和表级约束；表更新采用事务内临时表重建并保留数据、索引、触发器和依赖视图。
- 表设计器支持字段、索引和触发器定义编辑、字段/对象排序、外键、表检查、表约束、表选项及 DDL 预览；索引和触发器使用 SQL 定义编辑，不额外复制 MySQL 的服务端元数据表单。
- 视图设计器与 MySQL/达梦一致，SQL 定义使用独立的 Definition 页签，Preview 页签单独展示生成 DDL；页面不提供常驻视图名字段，已有视图使用原名称保存，新视图首次保存时弹窗设置名称。
- 表数据和视图数据支持分页、结构化条件过滤、新增、删除、应用、丢弃、导入和导出；过滤条件与 MySQL/达梦一致，包含启用状态、字段、条件、值、连接符和条件增删；空字符串、`NULL` 与 BLOB 保持独立语义，新增行未填写的列可使用数据库默认值。
- 表/视图记录编辑支持在单元格内使用 `Ctrl/Cmd+S` 触发应用保存，与 MySQL/达梦记录页行为一致。
- 表节点支持打开、设计、重命名、清空、重置自增、删除、导出、dump、克隆和表信息；视图节点支持打开、设计、重命名、删除、克隆、导出和视图信息。
- 函数设计器按 SQLite 能力边界支持 SQL 表达式测试、表函数视图和生成列设计；三种模式只显示各自需要的字段，视图定义仅用于表函数视图，目标表/返回类型/存储方式仅用于生成列；目标表使用可搜索下拉框，返回类型使用 SQLite 类型下拉框，控件高度与表字段扩展属性一致；函数名不常驻表单，新建时保存弹窗设置；模式说明通过组件 Tooltip 展示；通过 `PRAGMA function_list` 浏览内置/扩展函数，不伪造服务端持久化存储函数。
- 数据工具支持 SQL/JSON/XML/CSV/HTML/XLS/XLSX/TXT 导出、SQL/JSON/XML/CSV/XLS/XLSX/TXT 导入、库或表 dump、SQL 文件执行，以及 SQLite 文件之间的表/视图/数据传输。
- SQL 终端与 MySQL/达梦保持一致，支持 SQL 及 `show tables;`、`show dbs;`、`show databases;`、`use`、`help`、`clear`；SQLite 单库场景下 `use` 仅接受当前库 `main`，其余输入直接按 SQL 执行。
- 连接状态、执行状态、SQL 错误、文件错误、只读错误和资源关闭失败提示。
- 连接引导、连接树、标签页、FXML 页面、国际化键和 SQLite 驱动依赖集成。

### 3.2 明确不在本期范围

- SQLCipher 加密、密码管理和密钥迁移。
- 多数据库文件同时附加的可视化管理；用户仍可通过 `ATTACH DATABASE` SQL 使用。
- 远程 SQLite 文件传输、网络代理和 SSH 跳板。
- 独立于 SQL 定义的索引/触发器可视化向导、存储过程/持久化函数设计器。
- SQLite CLI 的全部点命令、脚本控制台和输出格式配置。
- 在线备份、WAL 恢复、损坏修复和数据库压缩工具。

## 4. 核心业务流程

1. 用户从连接引导选择 SQLite。
2. 用户选择已有文件或指定新文件；新文件仅在确认连接时创建，避免取消选择后产生空文件。
3. 控制器校验路径、父目录、只读选项和创建策略，文件选择器允许任意扩展名或无扩展名，并保存到 `ShellConnect.host`。
4. `ShellSqliteClient` 将路径转换为继承 `DBConnConfig` 的 `ShellSqliteConnConfig`，由 `ShellSqliteConnManager` 创建 JDBC 连接。
5. 连接成功后打开 `ShellSqliteTab`，左侧加载表和视图，右侧提供查询与终端标签。
6. 双击表或视图打开分页数据标签，读取 `sqlite_master`、`PRAGMA table_xinfo`、过滤条件和行数并展示。
7. 查询标签通过 SQLite SQL 解析器拆分语句；无显式事务时在单个 JDBC 事务中执行，成功提交、失败回滚，显式事务命令由 JDBC 事务边界适配。
8. 关闭标签或连接时释放结果集、语句、连接和状态监听器。
9. 从对象树或根菜单打开表、视图或函数设计器，保存前生成 SQLite DDL 预览；保存后刷新树和对象标签。
10. 从根菜单或表节点打开导入、导出、dump、SQL 文件执行或传输页面；后台任务支持进度、错误继续策略和中断。
11. 传输前校验源文件与目标文件不同，并按所选表/视图决定结构与数据迁移范围。

## 5. 文件型数据库能力边界

- 一个连接对应一个本地 SQLite 主文件；`host` 保存规范化绝对路径。
- SQLite 无独立数据库或模式层级；界面使用 main 文件名作为逻辑数据库名。
- SQLite 为动态类型；UI 显示声明类型与实际 JDBC 值，不做服务端数据库式强类型推断。
- 表结构以 `sqlite_master.sql`、`PRAGMA table_info`、`PRAGMA index_list` 为准。
- `INTEGER PRIMARY KEY` 可作为 rowid 自增列；其他类型不承诺自动递增。
- DDL 在 SQLite 中可事务回滚；多语句批次统一提交或回滚。
- SQLite 默认单写多读；连接层设置 busy timeout，写锁冲突转为可读错误。
- 只读模式使用 `open_mode=1`（SQLITE_OPEN_READONLY）；创建策略不会覆盖已有文件内容，目标已存在时按已有文件校验并打开。
- WAL、journal、foreign_keys 和 synchronous 属于连接级 PRAGMA 设置，可由环境参数覆盖。

## 6. 数据结构

### 6.1 通用 `fx-db` 层

- `DBDialect.SQLITE`：统一方言标识。
- `DBBaseSqlParser`：使用 `SqlDatabase.SQLITE` 进行拆分、格式化、查询识别和注释处理。
- `DBUtil` / `DBDataUtil`：SQLite 标识符使用双引号并转义内部双引号，字符串使用单引号并转义内部单引号。
- `DBConnConfig`、`DBConnManager`、`DBClient`、`DBQueryResult`：继续复用现有连接、执行和结果抽象。

### 6.2 EasyShell SQLite 层

- `ShellSqliteConnConfig`：继承 `DBConnConfig`，保存文件路径、只读、按需创建、busy timeout、journal mode、foreign keys、synchronous。
- `SQLiteDatabaseFileField`：EasyShell SQLite 专用文件路径选择组件，允许任意扩展名或无扩展名。
- `SQLiteTable`：名称、注释、SQL、对象类型、是否无 rowid、是否 strict。
- `SQLiteView`：名称、注释、定义、是否可更新。
- `SQLiteColumn`：名称、声明类型、长度提示、非空、主键、默认值、rowid 自增、隐藏列。
- `SQLiteExecuteResult`：SQL、列、只读行、更新数量、耗时、成功状态和错误消息。
- `SQLiteDatabase`：名称、版本、文件、大小、页大小、页数、编码、journal mode、自动外键和只读状态。
- `SQLiteSchemaObject`、`SQLiteFunction`：`sqlite_master`/`PRAGMA function_list` 对象和 SQLite 函数能力模型。
- `SQLiteSqlGenerator`：表、视图、生成列、SQL 表达式和 INSERT 字面量生成。
- `SQLiteSchemaManager`：创建表、事务内重建表、保存视图并重建依赖视图、按能力边界保存函数。
- `data.sqlite.*`：导出/导入 DTO、文件 writer、SQL/CSV/JSON 转换和 dump/import/export/run-file/transport 处理器。

## 7. 模块划分与职责

| 模块 | 职责 | 复用来源 |
| --- | --- | --- |
| `cn.oyzh.fx.db` | SQLite 方言、SQL 拆分、标识符/字符串转义 | 现有 `DBConnConfig`、`DBConnManager`、`DBClient`、`DBQueryResult` |
| `cn.oyzh.easyshell.sqlite` | `ShellSqliteConnConfig`、客户端、连接管理、元数据、SQL 执行和辅助工具 | `ShellBaseClient`、`ShellConnState`、`DBClient` |
| `cn.oyzh.easyshell.controller.connect.sqlite` | 按 MySQL 新增/修改连接模式分别承载表单和保存动作 | `StageController`、`ShellConnectStore` |
| `cn.oyzh.easyshell.fx.sqlite` | SQLite 专用文件路径选择组件 | `ChooseTextField`、`FileChooserHelper` |
| `cn.oyzh.easyshell.util.sqlite` | 字段能力、数据值/DML、节点/记录、通用辅助和页面工厂 | `DBColumnFieldManager`、`DBNodeUtil`、`DBUtil`、MySQL/达梦工具分层 |
| `cn.oyzh.easyshell.trees.sqlite` | SQLite 树通用基类、树视图和节点过滤器 | `RichTreeView`、`RichTreeItem` |
| `cn.oyzh.easyshell.trees.sqlite.root/table/view/function/query/terminal` | 按对象类型拆分的独立分组/对象节点及 `TreeItemValue` | `RichTreeItem`、`RichTreeItemValue`、`RichTreeItemFilter` |
| `cn.oyzh.easyshell.tabs.sqlite` | 连接、对象、查询、终端标签 | `RichTab`、`RichTabPane`、FXML |
| `cn.oyzh.easyshell.query.sqlite` | SQL 编辑、提示、结果模型 | `DBQueryEditor`、`DBQueryPromptPopup` |
| `cn.oyzh.easyshell.terminal.sqlite` | SQL 终端与格式化输出 | `TerminalPane`、`TerminalManager` |
| `cn.oyzh.easyshell.event.sqlite` | 表、视图、函数、查询、终端和结构变更事件 | `Event`、`EventUtil`、`EventSubscribe`、`FXEventListener` |
| `cn.oyzh.easyshell.tabs.sqlite.*.design` | 表、视图、函数设计器标签页 | `RichTab`、`RichTabController`、FXML |
| `cn.oyzh.easyshell.data.sqlite` | SQLite 导入、导出、dump、SQL 文件和传输 | `DBData*Handler`、`DBData*Reader/Writer`、base-store 文件组件 |
| `cn.oyzh.easyshell.controller.sqlite.data` | 数据工具窗口和任务生命周期 | `StageController`、`ThreadUtil`、`Counter` |

## 8. 接口设计

### 8.1 `ShellSqliteClient`

- `start(int timeout)`：验证路径并打开连接，更新 `ShellConnState`。
- `close()`：幂等关闭连接管理器和监听器。
- `isConnected()`：校验 JDBC 连接有效性。
- `selectTables()` / `selectViews()`：读取用户对象并排除内部表。
- `selectColumns(tableName)`：读取列定义。
- `selectRecords(..., filter)` / `selectRowCount(tableName, filter)`：在分页查询和计数中应用 WHERE 过滤条件。
- `database()`：读取文件与 PRAGMA 元数据。
- `executeSql(String sql)`：返回 `DBQueryResults<SQLiteExecuteResult>`。
- `executeSqlRaw(...)`：提供 JDBC 原始结果并保证事务与资源恢复。
- `selectSchemaObjects(...)` / `selectFunctions()`：读取表、视图、索引、触发器和函数元数据。
- `renameTable/clearTable/truncateTable/dropTable/cloneTable`：表重命名、清空、重置 AUTOINCREMENT、删除和结构/数据克隆。
- `renameView/dropView/cloneView`：视图重命名（保留依赖视图和 INSTEAD OF 触发器）、删除和克隆。
- `SQLiteSchemaManager`：通过事务脚本创建/重建对象，并在完成后恢复原 `foreign_keys` 状态。

### 8.2 连接配置映射

- `ShellConnect.type = ShellPrototype.SQLITE`
- `ShellConnect.host` = SQLite 文件绝对路径
- `ShellConnect.connectTimeOut` = 连接/锁等待秒数，经 `connectTimeOutMs()` 映射为 busy timeout 毫秒
- `ShellConnect.environment` = JDBC/PRAGMA 键值，每行 `key=value`
- `ShellConnect.user/password` 保留但不用于 xerial SQLite 驱动

## 9. UI 交互

- 连接窗口提供名称、文件、只读、创建策略、busy timeout、环境参数、备注与测试连接。
- 文件控件通过统一文件选择动作选择数据库路径，允许任意扩展名或无扩展名；“只读”和“文件不存在时创建”合并到选项区域，创建动作只在连接确认时写入空文件。
- SQLite 没有多数据库层级，树根节点直接表示 `main` 数据库；其下直接展示表、视图、函数、查询和终端，不再设置单独的数据库动作节点。
- 连接打开后，表、视图、函数和查询分组节点立即显示各自数量；双击时按需加载子节点，存在子节点时分组图标显示为绿色，并在加载后同步数量。新增表、视图或查询时增量插入对应已加载分组并更新数量，不重建整棵对象树；SQLite 不持久化函数，函数设计器不会伪造函数节点，表函数视图产物会增量进入视图分组。
- 对象标签展示字段表与数据表，工具栏支持刷新。
- 查询标签支持保存查询、格式化、执行、快捷键和多结果集。
- 终端使用当前文件名和连接状态作为提示符，结果使用文本表格输出。
- 根节点菜单提供新建表、新建视图、函数设计器、导入、导出、dump、运行 SQL 文件和传输；表节点提供设计、表信息、导出和 dump，视图节点提供设计和视图信息，函数分组展示内置/扩展函数并提供函数设计与函数信息。
- 查询分组支持新建、打开已保存查询、重命名、删除和刷新；查询变更事件同步树节点与已打开标签。
- 连接页采用 MySQL 同款左侧过滤、底部数据工具/定位工具栏和 `Ctrl/Cmd+F` 搜索聚焦交互；表、视图、函数设计器采用顶部 SVG 操作栏和分页布局。
- 表设计采用 MySQL 风格字段网格，覆盖名称、类型、长度、主键、非空、唯一、默认值，并提供字段扩展属性、索引、触发器、外键、表检查、表约束、表选项和 SQL 预览；Options 页不提供常驻表名字段，新表首次保存时弹窗设置表名，已有表沿用原名。
- 字段扩展属性使用独立子 FXML/子控制器，自动递增、排序规则、字段检查、生成列表达式和存储方式各占独立行；排序规则通过 `PRAGMA collation_list` 从当前连接加载为下拉选项，并保留默认及已有扩展排序规则；由父设计器随字段选择及类型/主键变化联动，自动递增仅对 `INTEGER PRIMARY KEY` 显示。
- 外键编辑本地字段、引用表/字段和删除/更新策略；表检查编辑名称与 CHECK 表达式，保存时合并进 SQLite 建表 DDL。
- 表数据使用独立分页标签，支持 MySQL/达梦风格的结构化过滤、新增、删除、应用、丢弃、刷新、导入、导出和分页；不再与字段定义混在通用对象页中。
- 表/视图记录单元格复用 `DBNodeUtil.getNodeBackground` 按字段能力着色：文本、数值、BLOB、布尔、日期时间、JSON 等类型使用不同背景色，便于快速区分字段类型。
- 视图数据使用独立分页标签，支持过滤、新增、删除、应用、丢弃、刷新、导入、导出；视图设计变更后自动刷新或重建数据标签。
- 数据网格使用 NULL 提示和显式 NULL/空串菜单；BLOB 只读并按十六进制展示，未编辑时保留原始字节。
- 查询页包含保存、美化、执行和 Explain 操作，Info 展示全部语句信息，每条有列的结果使用独立 Result 标签页并可单独导出；Explain 使用 `EXPLAIN QUERY PLAN`。
- 终端提示符使用当前单库名 `main` 和连接状态；基础命令由终端处理器直接查询当前连接的表与库信息，其余输入按 SQL 执行。
- 设计器保存前显示 SQL 预览，未保存标签关闭时要求确认；SQLite 函数页面明确展示函数能力边界。
- 导出、导入、dump、SQL 文件和传输窗口统一为 MySQL 风格的分步向导，提供格式/对象/字段/选项/执行步骤。
- 传输窗口按源文件、目标文件、数据选项、表和视图选择、执行消息分步展示，并在目标连接成功后显示版本和路径。
- 数据页面将 JDBC/文件 I/O 放入后台线程，提供开始、停止、状态、消息、批量进度和出错后继续选项。
- 页面工厂支持普通导出、预配置查询导出、数据库/表 dump 和由连接配置临时创建源客户端的传输入口。
- SQLite 连接标签实现 LLM 标签能力，提供 SQL 执行和原始 JDBC 结果集工具。
- 非 UI 线程只执行 JDBC；JavaFX 控件更新统一回到 FX 线程。

## 10. 异常与资源管理

- 文件不存在、不是普通文件、父目录不可写和损坏文件分别显示明确错误；文件选择器不限制扩展名。
- 创建策略不会覆盖已有文件内容；打开模式不会隐式创建文件。
- 只读连接上的写 SQL 由 SQLite 返回错误，并保留语句上下文。
- SQL 批次任一语句失败时回滚整个批次，并在结果对象中记录失败语句。
- `Connection`、`Statement`、`ResultSet` 使用 try-with-resources 或 `IOUtil.close` 成对释放。
- 关闭流程幂等，状态监听器移除后不重复发送关闭事件。
- UI 任务捕获 `Throwable`，显示错误但不得使 JavaFX Application Thread 崩溃。
- dump、导入、导出、SQL 文件和传输处理器保证 writer/reader/语句/结果集关闭；中断路径不继续写入。
- 传输源文件与目标文件相同、目标文件损坏、表不存在、列不兼容和只读目标均返回明确错误。

## 11. 兼容性策略

- 使用 xerial `sqlite-jdbc`，支持 Windows、macOS、Linux 的标准 SQLite 文件格式。
- JDBC URL 默认 `jdbc:sqlite:<file>`，使用规范化绝对路径构造 URL；文件路径不作为 SQL 值拼接。
- SQL 解析使用 `SqlDatabase.SQLITE`；支持字符串/注释中的分号和 `CREATE TRIGGER ... BEGIN ...; END;` 块，无法解析时回退到原始语句，不让格式化影响执行。
- 旧连接数据无需迁移；未知类型继续按现有默认 SSH 行为处理。
- Java 版本沿用工程 Java 25，FXML、国际化和 Maven 配置不引入额外 UI 框架。
- SQLite 表更新使用临时表复制兼容列；生成列不参与复制，索引、触发器和依赖视图按 `sqlite_master` 定义重建。

## 12. 测试范围

- `fx-db`：SQLITE 方言、标识符与字符串转义、SQL 拆分/查询识别、文件连接配置归一化。
- EasyShell：连接类型分派、文件创建/打开/只读、连接生命周期、表/视图/字段/文件元数据。
- SQL：DDL、DML、多语句提交、失败回滚、更新数量、结果集、空结果、错误语句。
- 边界：路径为空、任意/无扩展名、文件丢失、非 SQLite 文件、空数据库、保留字对象名、包含引号名称、中文数据、BLOB、NULL。
- SQLite 语法边界：触发器体内分号与 `CASE ... END`、复合主键、表级约束、多词类型、带空格字段名、生成列和 `WITHOUT ROWID`。
- UI 架构：FXML 可加载、入口路由、树节点与标签资源完整、JavaFX 线程规则。
- 设计器：字段控制网格、索引/触发器定义、字段/对象排序、外键/检查、表约束/选项、DDL 预览、生成列/表重建、视图保存和刷新、函数 SQL 生成和能力边界。
- 对象生命周期：表重命名/清空/重置自增/删除/克隆，视图重命名/删除/克隆，依赖视图和视图触发器保留。
- 数据：SQL/CSV/JSON/XML 导出，CSV/JSON/XML/SQL 导入，dump、run SQL file、SQLite 间传输，过滤、分页、默认值、NULL/空串/BLOB、批量、中断和错误继续。

## 13. 验收标准

1. 可从连接引导新建和编辑 SQLite 连接，并成功测试连接。
2. 可创建新 SQLite 文件或打开已有文件；取消创建不遗留文件。
3. 打开连接后能读取并展示数据库文件、表、视图、字段和行数。
4. SQL 查询可显示列、行、更新数量、耗时和错误；多语句事务行为符合设计。
5. 查询与终端入口均可工作，关闭后无 JDBC 文件句柄泄漏。
6. 只读连接、文件丢失、损坏文件和 SQL 错误均有可理解提示。
7. MySQL、MariaDB、MongoDB、达梦入口和方言行为无回归。
8. 新增 SQLite 测试全部通过，相关 `fx-db` 与 EasyShell 测试通过。
9. 可加载并保存已有表、视图和函数设计器，字段/约束不丢失，SQLite 不支持的持久化函数能力有明确边界提示。
10. 可完成导入、导出、dump、SQL 文件执行和 SQLite 文件间传输，错误与资源关闭行为符合设计。
11. 表/视图数据可过滤、分页和编辑，NULL、空字符串、BLOB 和默认值行为符合 SQLite 语义。
12. 表/视图重命名、清空、重置自增、删除和克隆动作可用，事件会刷新树和已打开数据页。
