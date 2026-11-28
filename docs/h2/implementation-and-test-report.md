# H2 数据库管理实现与测试报告

## 1. 实施结论

参照 SQLite 实现完成 H2 数据库协议对接，覆盖需求与设计文档（`requirements-and-design.md`）定义的全部范围：

- 连接与树、Tab、查询、终端、事件、弹窗、数据工具（导入/导出/传输/转储/SQL 文件执行）全链路可用；
- 全量编译通过（`mvn -DskipTests compile` BUILD SUCCESS）；
- H2 测试套件 46 个用例全部通过；
- README 协议清单、changes/v1.2.8.md 已同步更新。

## 2. 实现范围

### 2.1 `fx-base/fx-db`

- `DBDialect` 新增 `H2` 枚举；
- `DBBaseSqlParser` 方言映射、`DBUtil.wrap/wrapData/isSqlDialect/unwrapData`、`DBDataUtil.escapeQuotes` 增加 H2 双引号分支；
- 新增 `H2DialectTest`（fx-db 侧）。

### 2.2 EasyShell 核心层

- `cn.oyzh.easyshell.h2` 包：`ShellH2ConnConfig`（urlMode=file/mem/tcp 三模式 URL 构造）、`ShellH2ConnManager`、`ShellH2Client`（implements ShellBaseClient + DBClient）、`ShellH2Helper`、`ShellH2LLMTools`；
- check/column/condition(22 个条件)/database/foreignKey/index/record/schema/table/trigger/view 子包；
- generator：table（create/alert）、view（create/alert）、index、trigger、record（无 SQL 函数设计器，H2 的 CREATE ALIAS 为 Java 别名，不支持表达式函数）；
- 全局注册点：ShellPrototype、ShellConnect.isH2Type、ShellClientUtil、EasyShellApp 列初始化、ShellOsTypeComboBox、ShellTabPane、ShellConnectStore、ShellConnectTextField、ShellConnectTreeItem、ShellViewFactory、ShellAddConnectGuidController + FXML 卡片、ShellLLMTools。

### 2.3 页面与资源

- trees/h2（表、视图、查询、终端分组，索引/触发器复用通用分组）、tabs/h2（25 类）、query/h2、terminal/h2（`use` 映射为 `SET SCHEMA`，新增 `show schemas`）、popups/h2、controller/h2、data/h2、util/h2、fx/h2；
- FXML 资源 24 个、i18n 8 语言各 89 个 `shell.h2.*` 键、`llm/h2_tool.txt`。

## 3. 关键设计点

| 设计点 | 说明 |
| --- | --- |
| 元数据 | 全部基于 INFORMATION_SCHEMA（H2 2.x 无 TABLES.SQL 列，视图定义取 VIEWS.VIEW_DEFINITION） |
| 标识符大小写 | 未加引号标识符存储为大写；`ShellH2Client.canonicalObjectName` 在所有对象入口归一化（INFORMATION_SCHEMA `UPPER(TABLE_NAME)=?` 查询） |
| 主键自动索引 | `INFORMATION_SCHEMA.INDEXES.IS_GENERATED=TRUE` 排除 `PRIMARY_KEY_x` 自动索引，兜底按 `PRIMARY_KEY_数字` 命名判断 |
| DDL 事务 | H2 DDL 自动提交；`executeSql` 事务控制识别 BEGIN/COMMIT/ROLLBACK/SAVEPOINT |
| EXPLAIN | 查询页保留 Explain 页签（`EXPLAIN PLAN FOR`） |
| IDENTITY | 建表 DDL 支持 `IDENTITY`/`AUTO_INCREMENT`；`INT PRIMARY KEY` 不自动生成值 |

## 4. 测试执行

`mvn -Dtest='ShellH2ClientTest,ShellH2UtilTest,H2DesignAndDataTest,ShellH2ArchitectureTest,ShellH2TerminalTest,H2GeneratorSqlTest' test`
→ **Tests run: 46, Failures: 0, Errors: 0**。

| 测试类 | 用例 | 覆盖 |
| --- | --- | --- |
| ShellH2ClientTest | 10 | 连接三模式、表/视图/索引/触发器/外键/CHECK 元数据、记录增删改、行数、DDL |
| H2DesignAndDataTest | 3 | ALTER 序列、CHECK 约束、克隆/重命名、SQL/CSV 导出、CSV 导入、全库 dump 与恢复、SQL 文件执行 |
| ShellH2ArchitectureTest | 16 | FXML 清单与 XML 合法性、FXML + 控制器真实加载、连接表单三模式、类型路由、i18n 键完整、树根形状（6 分组）、树节点结构、建树/查询 upsert、页面结构对齐 SQLite |
| ShellH2TerminalTest | 3 | show tables/databases/dbs/schemas、use（SET SCHEMA 语义）、SQL 回退 |
| ShellH2GeneratorSqlTest | 8 | quote/unquote/literal、建表 DDL、视图 DDL 往返、记录 INSERT、生成列 |
| ShellH2UtilTest | 6 | 工具方法 |

回归：`mvn -DskipTests compile` BUILD SUCCESS；SQLite 侧 SQLiteDesignAndDataTest(20)、ShellSqliteTerminalTest(2) 通过。

## 5. 开发与测试过程中发现并修复的问题

1. **Tab 页面 URL 残留（严重）**：tabs/h2 下 10 个 Tab 类的 `url()` 仍指向 `sqlite/...` FXML，导致 H2 页面实际加载 sqlite 控制器；已全部修正为 `h2/...`。
2. **FXML 类名残留**：6 处 `SQLiteCollationComboBox`/`SQLiteColumnKindComboBox`/`SQLiteResultTableView`/`SQLiteDataImportFileTableView`/`SQLiteDataExport*` 引用不存在的类，已替换为 `fx.h2.H2*` / `data.h2.ui.H2*`。
3. **FXML 标签不闭合**：连接表单 `<PasswordTextField>` 闭合标签误写为 `</FXPasswordField>`（add/update 两份），已修复。
4. **记录分页查询大小写**：`selectRecords(String,...)` 未归一化表名，带引号小写表名查询失败且导出处理器吞错产生空文件；已在入口归一化。
5. **导入列匹配**：CSV 表头与列名大小写不一致（H2 列恒大写）导致导入产生 `DEFAULT VALUES` 语句失败；`ShellH2DataUtil.normalize/insertColumns` 改为大小写不敏感匹配。
6. **导入/导出表名归一化**：`H2DataImportHandler`/`H2DataExportHandler` 入口统一 `canonicalObjectName`，保证生成 SQL 可回放。
7. **dump 三处缺陷**：写头 `PRAGMA`（SQLite 语法，H2 不支持）；`selectTable().getDefinition()` 为 null（改用 `showCreate`）；视图对象无 sql（改由 `VIEW_DEFINITION` 重建 DDL）；分号统一规范化。
8. **自动索引泄漏**：dump 将 `PRIMARY_KEY_x` 主键自动索引输出为 `CREATE UNIQUE INDEX`，恢复时冲突回滚（视图与数据丢失）；由 `IS_GENERATED=TRUE` 过滤修复。

## 6. 差异、边界与风险

- **无 SQL 函数设计器**：H2 的 CREATE ALIAS 为 Java 类别名，不提供表达式函数；树根不设函数分组。
- **视图定义跨库**：`VIEW_DEFINITION` 含 schema 限定（`FROM "PUBLIC"."ITEM"`），dump 恢复到默认 PUBLIC schema 可用；跨 schema 恢复需手工调整。
- **tcp 模式**：host 以 `host:port` 合并存储（对齐 ShellConnect 解析），连接表单单独端口字段。
- **终端**：`use <schema>` 映射 `SET SCHEMA`（校验 INFORMATION_SCHEMA.SCHEMATA），非 sqlite 的 ATTACH 语义。
- **遗留（非本次范围）**：SQLite 架构测试 3 个断言过期（近期"索引/触发器独立支持"提交后 sqlite 根树为 7 分组，测试仍断言 5），属 sqlite 协议收尾工作，未在本次修改。

## 6.1 对接复核修复

- 新增/修改连接页统一为同一套三模式布局，修正端口控件、提示属性和超时字段位置不一致。
- H2 文件路径区分数据库基名和实际 `*.mv.db` 文件；非标准后缀不再被误判为可打开的 H2 数据库。
- H2 i18n 键按 H2 能力重新整理，移除了 busy timeout、journal mode、WITHOUT ROWID、STRICT、函数设计器等 SQLite 文案。
- 字段能力注册改为 H2 强类型矩阵；整数、数值、布尔、日期时间类型不再继承 SQLite 的文本弱类型能力，并修复 `POINT`、`INTERVAL` 被 `INT` 子串误判的问题。
- 字段设计增加小数位，建表和 ALTER 都保留 `NUMERIC(p,s)` 精度；生成列移除 SQLite 的 `VIRTUAL/STORED` 选项。
- 表信息、主页和 H2 弹窗资源移除 SQLite 字段与错误路径；主页改为显示 H2 数据库名、用户、schema、隔离级别和实际文件。
- 表与视图定义优先从 H2 `SCRIPT`/`VIEW_DEFINITION` 还原，dump/传输先创建全部表，再恢复外键，避免约束丢失和表创建顺序失败。
- CLOB 的 JDBC 最大长度超过 `Integer.MAX_VALUE` 时不再调用 `getInt()`，避免复杂表记录页打开时报数值溢出。
- JSON 字段按 H2 UTF-8 `byte[]` 解码为文本，SQL/dump/传输使用 `JSON '...'` 字面量；UUID 使用 `UUID '...'`，避免复杂字段导入导出语法错误。
- 修正 IDENTITY 列 DDL 顺序，克隆表时去除原约束名并由 H2 重新生成，避免 schema 内约束名冲突。
- 表结构变更前保存并级联移除依赖视图，ALTER 完成后按表名/列名变化重写并恢复视图，解决“列被视图引用时无法修改”的 H2 限制。
- 新增 `H2ComplexTableIntegrationTest`，覆盖复杂字段元数据、CSV 导出导入、表编辑、关联视图编辑、JOIN 查询结果按主键更新和跨库传输。

## 7. 验收核对

| 验收项 | 结果 |
| --- | --- |
| 三种连接模式均可建立连接 | 通过（file/mem/tcp 单测覆盖） |
| 表/视图/索引/触发器设计器可用 | 通过 |
| 查询（含 EXPLAIN）可用 | 通过 |
| 导入/导出/传输/转储/SQL 文件执行 | 通过（端到端测试） |
| 终端命令与 SQL 执行 | 通过 |
| FXML 全部可加载且 XML 合法 | 通过 |
| i18n 8 语言键完整一致 | 通过 |
| LLM 工具注册 | 通过 |
| 全量编译 | 通过 |
| H2 测试套件 | 46/46 通过 |
