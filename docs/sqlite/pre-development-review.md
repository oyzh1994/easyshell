# SQLite 功能开发前文档审核

- 审核日期：2026-11-02
- 审核对象：`docs/sqlite/requirements-and-design.md`
- 审核结论：修改后批准进入开发

## 1. 完整性检查

- 已覆盖用户、范围、流程、文件数据库边界、UI、数据结构、模块、接口、异常、兼容性、测试与验收。
- 已明确服务端数据库与 SQLite 的差异，不把多库、多模式、存储过程、事件等能力错误映射到 SQLite。
- 已覆盖文件创建、打开、关闭、只读、丢失、损坏和锁冲突场景。
- 已补充表/视图/函数设计器、数据导入/导出、dump、SQL 文件执行和 SQLite 文件间传输的范围与能力边界。

## 2. 架构一致性

- 继续使用 `ShellConnect`、`ShellBaseClient`、`DBClient`、`DBConnManager`、`DBQueryResult`、`RichTab`、`RichTreeView`、`TerminalPane` 和 FXML/国际化机制。
- SQLite 文件连接参数通过继承 `fx-db` 的 `DBConnConfig` 的 `ShellSqliteConnConfig` 承载；SQLite 专用 UI 控件放在 EasyShell，保持 `fx-db` 只提供数据库抽象和方言能力。
- 新增/修改连接控制器与 MySQL 保持相同结构，分别继承 `StageController`，使用 `insert`/`replace` 完成新增和更新。
- EasyShell 层只负责业务对象和页面，SQL 标识符、字符串与语句解析留在通用 `fx-db` 方言层。
- 数据处理继续复用 `fx-db` 的 `DBData*Handler`、reader/writer 和配置模型；SQLite SQL 字面量、生成列和 schema 重建只留在 EasyShell SQLite 层。

## 3. 组件复用与职责边界

- 复用连接存储、连接树、标签页框架、查询编辑器、查询结果模型、终端框架和消息框。
- 不复用 MySQL 专用 `information_schema` 查询、存储过程/事件/触发器模型和多数据库树层级。
- 不在 `fx-db` 引入 EasyShell 的 `ShellConnect` 或业务事件，保持依赖方向从 EasyShell 指向 `fx-db`。

## 4. 发现的问题与修正

| 问题 | 风险 | 修正 |
| --- | --- | --- |
| 文件创建若在选择阶段执行，会留下用户取消后的空文件 | 文件污染 | 增加 `createIfMissing`，只在连接确认时创建 |
| SQLite 标识符若沿用 MySQL 反引号会执行失败 | SQL 错误 | `DBUtil` 增加 SQLITE 双引号转义 |
| 多语句默认 JDBC 事务未明确提交和自动提交恢复 | 隐式提交或连接污染 | 明确事务提交、回滚和恢复 autoCommit |
| 单一结果模型会丢失多语句更新数量 | 结果不完整 | 保留每条 SQL 的独立执行结果 |
| 文件数据库可能被其他进程写锁 | 打开或写入卡死 | 设置 busy timeout，并转换为用户可读错误 |
| FXML 资源与 Java 包可能漏集成 | 运行时加载失败 | 增加架构测试检查入口、FXML 和包归属 |
| SQLite 专用配置和文件控件放在 `fx-db` 根/UI 包会破坏组件边界 | 通用层被业务 UI 污染 | `ShellSqliteConnConfig` 移至 EasyShell SQLite 层，文件控件移至 EasyShell SQLite FX 包 |
| 新增/修改控制器若共用非 MySQL 基类会形成两套连接表单模式 | 维护和行为不一致 | 拆分为两个直接继承 `StageController` 的控制器，保存动作分别使用 `insert`/`replace` |
| 直接照搬达梦存储函数设计器会伪造 SQLite 不具备的能力 | 功能误导或保存失败 | 函数设计器限定为 SQL 表达式、表函数视图、生成列和内置函数浏览 |
| 直接 ALTER TABLE 修改 SQLite 表可能丢失数据或依赖对象 | 数据丢失 | 使用事务内临时表重建、复制兼容列并重建索引/触发器/依赖视图 |
| 数据处理器中断或异常可能遗留 writer、reader、JDBC 语句或目标连接 | 文件句柄泄漏 | handler 使用 try-with-resources/finally，并在 UI 关闭时中断后台任务 |
| 传输目标与源为同一 SQLite 文件时可能先 DROP 再复制 | 源数据损坏 | UI 显式比较规范化路径并拒绝同文件传输 |

## 5. 测试可执行性

- 通用方言与路径测试不依赖 JavaFX，可在 Maven 单元测试中直接执行。
- 数据库集成测试使用 JUnit 临时目录和真实 sqlite-jdbc 文件，不依赖外部服务。
- UI 架构测试检查类型分派和资源存在性，不在无头环境启动完整 Stage。
- 集成测试覆盖事务、元数据、只读、设计器 DDL/重建、数据格式、dump、SQL 文件、传输和边界条件，测试结束关闭临时数据库客户端。

## 6. 与 MySQL、达梦实现的差异

- MySQL/达梦有主机、端口、用户、密码、代理、跳板和多模式；SQLite 仅管理本地文件，不启用代理和跳板。
- MySQL/达梦有存储过程、持久化函数、事件、序列和触发器设计器；SQLite 提供表、视图和能力适配后的函数设计器，不支持持久化存储函数。
- MySQL/达梦按库或模式切换连接；SQLite 始终指向 main 文件，`ATTACH` 由 SQL 层支持。
- SQLite 支持动态类型、rowid 和事务型 DDL，元数据来源及错误语义不同。

## 7. 最终批准

上述问题已写入设计约束，需求、架构、复用、边界、风险和测试均可执行。批准按 `requirements-and-design.md` 进入开发阶段。
