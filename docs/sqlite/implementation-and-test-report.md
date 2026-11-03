# SQLite 数据库管理实现与测试报告

## 1. 实施结论

- 实施日期：2026-11-02。
- 结论：SQLite 文件型数据库管理功能已按需求与设计文档实现，并通过 SQLite 方言、连接生命周期、元数据、SQL、事务、错误路径、FXML 和入口架构测试。
- 设计依据：`docs/sqlite/requirements-and-design.md`、`docs/sqlite/pre-development-review.md`。
- 复用基线：MySQL、MariaDB、达梦的连接模型、客户端接口、连接树、查询编辑器、结果表、标签页、终端框架、FXML 和国际化机制。

## 2. 参考实现分析

已检索并对照以下目录及其关联公共组件：

- `cn.oyzh.easyshell.mysql`、`cn.oyzh.easyshell.dameng`。
- `cn.oyzh.easyshell.controller.mysql`、`cn.oyzh.easyshell.controller.dameng`。
- `cn.oyzh.easyshell.controller.connect.mysql`、`cn.oyzh.easyshell.controller.connect.dameng`。
- `cn.oyzh.easyshell.fx.mysql`、`cn.oyzh.easyshell.fx.dameng`。
- `cn.oyzh.easyshell.query.mysql`、`cn.oyzh.easyshell.query.dameng`。
- `cn.oyzh.easyshell.terminal.mysql`、`cn.oyzh.easyshell.terminal.dameng`。
- `cn.oyzh.easyshell.tabs.mysql`、`cn.oyzh.easyshell.tabs/dameng`。
- `cn.oyzh.easyshell.util.mysql`、`cn.oyzh.easyshell.util/dameng`。
- `src/main/resources/fxml/dameng`、`src/main/resources/fxml/connect/dameng`。
- `src/main/resources/tabs/dameng`、`src/main/resources/tabs/mysql`。

分析结果：

- 连接管理统一通过 `ShellConnect`、`ShellClientUtil`、`ShellBaseClient`、`DBClient`、`DBConnManager` 和 `ShellConnectStore` 接入。
- 数据库对象采用客户端/模型/树节点/标签页分层；查询采用 `DBQueryEditor`、查询提示、`DBQueryResults` 和结果表格。
- 终端采用 `TerminalPane`、命令处理器、补全、帮助和历史机制。
- MySQL/达梦的多库、多模式、存储过程、函数、事件、序列和设计器能力不直接迁移到 SQLite。
- SQLite 只增加文件型数据库所需适配，不向 `fx-db` 泄漏 EasyShell 业务类型。

## 3. 实现范围

### 3.1 `fx-base/fx-db`

- 新增 `DBDialect.SQLITE`。
- 仅扩展 `fx-db` 的方言、SQL 拆分、标识符/字符串转义和查询识别能力。
- 继续复用 `DBConnConfig`、`DBConnManager`、`DBClient`、`DBQueryResult` 等现有数据库组件，不向 `fx-db` 增加 SQLite 业务 UI。
- SQLite SQL 拆分支持字符串、反引号、双引号、方括号标识符、行注释和块注释中的分号。
- SQLite 标识符使用双引号并转义内部双引号；字符串使用单引号并转义内部单引号。

### 3.2 EasyShell 核心

- `cn.oyzh.easyshell.sqlite`：继承 `DBConnConfig` 的 `ShellSqliteConnConfig`、`ShellSqliteClient`、`ShellSqliteConnManager`、`ShellSqliteHelper`、数据库/表/视图/字段模型。
- 文件路径保存在 `ShellConnect.host`；连接确认时才创建缺失文件，支持只读和 `createIfMissing`。
- 读取 `sqlite_master`、`PRAGMA table_xinfo`、`PRAGMA page_size/page_count/encoding/journal_mode/foreign_keys/synchronous`。
- SQL 执行返回每条语句的独立结果，包括列、记录、更新数量、耗时和错误；默认批次单事务提交/回滚。
- 显式 `BEGIN/COMMIT/END/ROLLBACK/SAVEPOINT/RELEASE/ROLLBACK TO` 通过 JDBC 事务边界适配；失败路径回滚并恢复连接自动提交状态。
- 连接关闭释放连接管理器、状态监听器和检查器引用，支持关闭后重新打开。

### 3.3 JavaFX 页面与入口

- 新增 SQLite 连接新建/编辑控制器及 FXML；控制器结构、`StageController` 继承关系、`insert`/`replace` 保存方式与 MySQL 保持一致。
- SQLite 连接文件统一由文件选择框选择，不再提供独立打开/创建按钮；`只读` 与 `文件不存在时创建` 作为同一选项区域，布局与 MySQL 连接表单一致。
- SQLite 专用 `SQLiteDatabaseFileField` 放在 `cn.oyzh.easyshell.fx.sqlite`，允许任意扩展名或无扩展名文件。
- 新增 SQLite 连接树、数据库首页、对象浏览、查询主标签和 SQL 终端标签。
- SQLite 树按 MySQL 数据库节点模式重构：根节点直接表示 `main` 数据库，下挂表、视图、函数、查询、终端，不再额外创建 Database 动作节点；树节点和值对象按 `root`、`table`、`view`、`function`、`query`、`terminal` 子包拆分，每种类型拥有独立的 `TreeItem` 和 `TreeItemValue`，不再使用公共对象节点/公共值对象。
- SQLite 树视图补齐 `FXEventListener` 和 `ShellSqliteTreeItemFilter` 初始化，过滤前保证 `getItemFilter()` 非空；搜索框初始化或输入时不再触发空指针异常。
- 表、视图和查询新增改为对应分组内增量插入节点并刷新数量，查询保存、重命名、删除分别增量更新；不再通过根节点重建清空已加载分组。SQLite 函数分组表示 `PRAGMA function_list` 的内置/扩展函数，不伪造持久化函数节点；函数设计产生的表函数视图会增量加入视图分组。
- SQLite 连接页补齐与 MySQL 一致的底部导入、导出、运行 SQL、传输和定位工具栏，以及 `Ctrl/Cmd+F` 搜索聚焦快捷键；表/视图/函数设计器统一为顶部 SVG 保存/预览工具栏和分页式布局，视图定义与预览编辑区均支持 `Ctrl/Cmd+S` 保存，切换预览页时自动重新生成 SQL。
- SQLite 表设计器按 MySQL `DBStatusTableView` 模式重做：字段名称/类型/长度/主键/非空/唯一/默认值使用可编辑网格，自动递增、排序规则、检查表达式、生成列属性置于字段区下方。
- 表设计器移除 Options 页常驻表名输入框，新表首次保存时弹窗设置名称，已有表沿用原名；索引、触发器草稿与预览使用 `Unnamed_Table` 占位，保存时统一替换为目标表名。
- 字段扩展属性按 MySQL/达梦模式拆为独立 `shellSqliteTableColumnExtra.fxml` 和子控制器，通过 `fx:include`/`ParentTabController` 与当前选中字段联动；自动递增、排序规则、字段检查、生成表达式、存储方式各占独立行；排序规则使用连接级 `PRAGMA collation_list` 下拉选项并保留自定义已有值；自动递增仅对精确 `INTEGER` 且为主键的字段显示，类型或主键变化时实时刷新并清理不支持的值。
- 表设计增加索引、触发器、表级约束、表选项和 SQL 预览分页；索引/触发器可直接编辑定义，保存时处理新增、修改、删除并兼容表重命名。
- 表设计新增外键和表检查分页：外键支持本地字段、引用表、引用字段、删除/更新策略；表检查支持名称和 CHECK 表达式。
- 外键和表检查可从 `CREATE TABLE` 解析回填，并在保存时重新组合到表 DDL；保留主键、唯一等高级原样约束。
- 表数据从通用对象标签拆分到 `tabs/sqlite/table` 独立记录页，提供刷新、导入、导出和分页，表双击不再打开混合的字段/数据/定义页面。
- 视图数据从通用对象页拆分到 `tabs/sqlite/view` 独立记录页，视图双击打开分页数据；视图数据支持刷新、导入、导出，视图变更事件同步刷新或重建标签。
- 表/视图数据页过滤对齐 MySQL/达梦：使用结构化过滤弹窗维护启用状态、字段、条件、值和连接符，支持包含/比较/NULL/空串/IN/BETWEEN/前后缀等条件及条件增删；同时保留显式 NULL/空串操作、默认值插入和 BLOB 原值保护，空字符串不再被误归一化为 NULL。
- 表/视图记录单元格编辑器接入 `TableViewUtil.rowOnCtrlS`，控制器在根容器上绑定 `Ctrl/Cmd+S`；新增或修改记录时在单元格内保存可直接触发应用逻辑。
- 表/视图记录单元格复用 MySQL/达梦的 `DBNodeUtil.getNodeBackground` 类型色映射，文本、整数、浮点、BLOB、布尔、日期时间、JSON 等字段显示不同背景色。
- 表节点补齐重命名、清空、重置 AUTOINCREMENT、删除和“结构/结构+数据”克隆；视图节点补齐重命名、删除和克隆。
- 视图重命名会先保存依赖视图和 INSTEAD OF 触发器，再重建并替换引用；表/视图删除事件会关闭对应数据页。新增视图只向当前视图分组增量追加节点并刷新数量，修改、重命名和删除不再整树重建，避免清空已加载的视图树状态。
- 表设计器补齐字段/索引/触发器/外键/检查上下移动和刷新动作；视图设计器改用 SQL 查询编辑器并支持从数据库刷新，定义内容使用独立 Definition 页签，移除常驻视图名字段，新视图首次保存时弹窗设置名称，已有视图保存沿用原名。
- 查询结果改为 Info 加多 Result 标签，每条返回列的结果语句独立展示和导出；Explain 同样支持多结果。
- 查询页对齐 MySQL 结构，增加 Explain 操作、Info/Result 双结果分页；`EXPLAIN QUERY PLAN` 结果可表格显示；查询保存改为新查询提示名称、`insert/update` 分流。
- 函数设计器移除重复且不参与生成的内置函数 Tab，内置/扩展函数继续由函数树和函数信息页展示；保留未保存标题刷新和保存时名称弹窗，按能力类型条件展示字段，SQL 表达式模式仅显示表达式，表函数视图模式仅显示视图定义，生成列模式显示目标表、返回类型、表达式和存储方式；目标表从当前 SQLite 表列表加载为可搜索 `SQLiteTableSelectTextField`，输入时实时过滤候选项，返回类型使用 `DBFiledTypeComboBox` 的 SQLite 类型列表，输入框和下拉框高度统一为字段扩展属性同款 `25`；类型说明改为随模式变化的组件 Tooltip，预览和编辑区支持 `Ctrl/Cmd+S`；视图/表通用混合对象页已移除。
- SQLite 终端新增 `.databases`、`.functions`、`.version`，保留 `.database`、`.tables`、`.views`、`.indexes`、`.schema`、`.read`。
- 新增 SQLite 字段表、结果表、查询编辑器、查询提示和终端命令处理器。
- 新增表、视图、函数信息控制器和 FXML，展示 SQLite 对象属性、字段明细、视图 SELECT 定义、函数能力标志和 DDL；表/视图/函数树节点接入对应信息菜单。
- 新增 `cn.oyzh.easyshell.event.sqlite` 事件模型和工具；表/视图打开、设计器打开、结构变更、函数设计、查询保存/打开和终端打开统一通过事件总线分发，由 `ShellSqliteTabPane` 按客户端隔离处理。
- 新增 SQLite 查询分组与查询节点，支持新建、重新打开、重命名、删除已保存查询，并通过查询事件同步树和已打开标签页。
- `ShellSqliteTab` 接入 `LLMTabCapability`，新增绑定 SQLite 文件的 LLM SQL/原始结果集工具；SQLite SELECT 结果可在 LLM 工具中按表格文本返回。
- 补齐 `cn.oyzh.easyshell.util.sqlite` 字段能力、数据/DML、节点、记录、通用辅助和页面工厂工具；注册 SQLite `DBColumnFieldManager` 与终端处理器，支持预配置导出、dump 参数和临时传输客户端生命周期。
- 新增表、视图、函数设计器标签页和 FXML；表设计器支持约束、生成列、`WITHOUT ROWID`、`STRICT` 和事务内表重建，函数设计器按 SQLite 能力边界提供表达式、表函数视图和生成列。
- 新增 SQLite 导入、导出、dump、SQL 文件执行和数据库间传输处理器、控制器、FXML、页面工厂与树菜单入口。
- 导出、导入、dump、SQL 文件和传输窗口统一为 MySQL 风格分步向导；传输支持源/目标路径、版本、数据选项、表和视图勾选及执行消息。
- 修正 SQL 文件导入被目标表/列预检查拦截的问题；SQL 导入直接执行脚本并可创建新表。
- 接入连接引导、连接类型选择、连接树编辑、客户端工厂、标签页路由、OS 图标和国际化。
- 终端元命令：`.tables`、`.views`、`.indexes`、`.schema`、`.database`、`.read`；同时复用 `.help` 和 `.clear`。

## 4. 最终审查修正

- 修正显式事务跨调用提交后自动提交状态未恢复的问题，并覆盖事务失败后的回滚与状态恢复。
- 修正带前导注释的事务命令识别，避免把 `BEGIN` 误当普通 SQL；增加 `ROLLBACK TO` 保存点回滚识别。
- 修正连接关闭后的状态监听器和检查器清理，同时保留重新连接时的监听器注册。
- 将 SQLite 专用配置和文件控件移出 `fx-db`，并按 MySQL 模式拆分新增/修改连接控制器。
- 修正文档中的终端命令清单、`table_xinfo`、连接超时单位、文件扩展名策略和 JDBC URL 描述，使其与实现一致。
- 将 SQLite 数据工具和设计器从达梦/MySQL 的服务端能力映射调整为 SQLite 能力适配，避免伪造持久化函数。
- 修正 SQL 导出对生成列的处理，导出文件可保留生成值，但 SQL INSERT 自动排除生成列；保留字段的唯一、COLLATE、CHECK 和生成表达式元数据。
- 表结构变更后恢复连接原 `foreign_keys` 状态，传输页面拒绝源文件与目标文件相同。
- 视图重命名或重定义时重建依赖视图并替换旧视图引用，避免遗留失效定义。
- 兼容 `PRAGMA function_list` 的不同结果列形态，从 `flags` 解析 deterministic/direct-only 标志，并对缺少的 final 信息采用保守值。
- 补齐首轮遗漏的 SQLite 事件层，避免树节点和设计器直接耦合标签页容器；结构变更事件同时刷新对象标签和数据库树。
- 修正打开已有表设计器时未加载字段和表级约束的问题；字段名统一引用，复合主键和表级主键不再重复生成，并校验 `WITHOUT ROWID + AUTOINCREMENT`。
- 修正全库 dump 重复输出索引/触发器的问题，按“表及数据 → 索引 → 视图 → 触发器”顺序输出；补全视图触发器传输，传输表时保留原始完整 DDL。
- 修正 SQLite SQL 拆分器对 `CREATE TRIGGER ... BEGIN ...; END;` 的错误切分，含 `CASE ... END` 的触发器可被 dump/run SQL file 正确恢复。
- 查询联想索引改为线程安全后台刷新，并在结构变更后自动更新；补齐树、首页、终端、字段表、dump 和连接文件的国际化，根资源包提供完整英文回退。
- 补齐查询树变更闭环和 LLM 结果格式化，避免查询保存后无法重新打开、LLM 读取 SQLite SELECT 时误报为空结果。
- 按 MySQL 表单规范收敛 SQLite 连接 UI：移除独立打开/创建动作，将只读和缺失时创建作为并列选项；新增 FXML 结构回归测试。
- 将 SQLite 主树与设计器页面统一到 MySQL 的数据库节点、分组菜单和 SVG 工具栏风格，移除独立 Database 节点。
- 将表设计从文本域表单替换为字段控制网格和分页设计器，并将表数据独立为 MySQL 风格分页标签。
- 补齐 SQLite 外键与表检查的结构化设计、DDL 生成、元数据解析和保存回填。
- 对齐视图数据、查询 Explain/Info、函数刷新和 SQLite 常用终端命令，并移除旧通用对象页。
- 补齐表/视图对象生命周期、数据过滤、NULL/空串/BLOB/默认值语义、多结果查询标签和向导式数据工具。
- 修正视图重命名后依赖视图/触发器未同步、SQL 导入误删目标表、传输视图早于数据创建等边界问题。
- `git diff --check` 检查通过，无空白错误。

## 5. 测试执行

| 命令 | 结果 |
| --- | --- |
| `mvn -pl fx-db install -DskipTests` | 通过，生成并安装 `fx-db-1.3.8.jar` |
| `mvn -pl fx-db -Dtest=SqliteDialectTest test` | 通过，1 test |
| `mvn -Dtest=ShellSqliteUtilTest,ShellSqliteArchitectureTest,ShellSqliteClientTest,SQLiteDesignAndDataTest test` | 通过，38 tests |
| `mvn -DskipTests compile` | 通过，EasyShell 2601 个源文件编译成功 |

SQLite 测试覆盖：

- 方言、标识符/字符串转义、SQL 拆分、文件配置归一化和任意/无扩展名路径。
- 文件创建、打开、关闭、重新打开、只读写入拒绝。
- 表/视图/字段/行数/文件信息元数据，保留字和引号对象名；表、视图和 `PRAGMA function_list` 信息模型。
- DDL、DML、多语句提交、失败回滚、显式事务、事务状态恢复。
- NULL、BLOB、中文、结果集、原始 JDBC 结果集、SQL 错误、丢失文件、损坏文件和环境参数。
- FXML XML 完整性、真实 `FXMLLoader` 加载、信息控制器资源和连接类型分派。
- SQLite 事件监听接口和标签页事件订阅处理器完整性。
- SQLite 字段能力注册、生成列排除、数值归一化、INSERT/UPDATE SQL、标识符和事务命令工具。
- 表设计器完整元数据、表级约束、复合主键、多词类型和带空格字段名解析。
- 表 DDL、生成列、表重建保数据、视图保存、表函数视图、生成列函数和错误能力边界。
- SQL/CSV 导出、CSV 导入、dump、含视图触发器的 dump 恢复到新库、SQL 文件执行和 SQLite 文件间表/视图/触发器/数据传输。
- SQLite 触发器语句拆分、根语言包/英文/中文国际化键一致性。
- 已保存查询树、查询事件订阅、LLM 标签页能力和 SQLite 查询结果格式化。
- 连接表单结构、选项分组和已移除独立打开/创建动作的资源约束。
- `main` 数据库根节点结构、五类功能子节点顺序和设计器 FXML 加载。
- 表设计页字段网格/索引/触发器/约束/选项/预览结构、表数据类型长度、索引与触发器定义规范化、独立分页数据页 FXML。
- 独立字段扩展 FXML、自动递增/排序规则/检查/生成列属性控件和主设计器 `fx:include` 结构；主字段网格不再包含自动递增列。
- 外键/表检查 FXML 结构、CREATE TABLE 解析、约束 SQL 生成和实际建表执行。
- 视图分页数据 FXML、查询 Explain FXML、EXPLAIN QUERY PLAN 执行、视图数据导出和终端命令注册。
- 表重命名/清空/重置自增/删除/克隆、视图重命名/删除/克隆、依赖视图与视图触发器保留。
- 数据过滤与计数、默认值插入、NULL/空串区分、BLOB 原值保留、SQL 文件导入建表。
- 数据工具向导 FXML 完整加载、查询多结果结构和设计器排序/刷新入口。
- 表、视图、函数、查询分组节点连接打开即显示数量、双击加载入口及非空子节点绿色图标状态。
- 字段控件首次渲染时不触发变更状态，只有用户实际修改类型等属性后才标记为已修改。
- SQLite 排序规则下拉：内置 `BINARY`、`NOCASE`、`RTRIM`，同时合并当前连接 `PRAGMA collation_list` 发现的扩展规则，保留未知但已存在的字段值。

## 6. 差异、边界与风险

- 不支持 SQLCipher、远程 SQLite 文件、网络代理/SSH 跳板、多文件可视化管理、独立于 SQL 定义的索引/触发器可视化向导和持久化存储函数。
- SQLite 为动态类型、单写多读文件数据库；不映射 MySQL/达梦的多库、多模式、存储过程、事件和序列。
- 文件选择器不限制扩展名，任意扩展名或无扩展名均可；SQLite 格式由 sqlite-jdbc 判断，损坏文件返回可读错误。
- 当前 Maven 输出存在既有的 `maven-resources-plugin` 版本提示、JavaFX unnamed module/native access 警告和测试存储层初始化警告，均未导致构建或 SQLite 测试失败。
- 未运行与 SQLite 无关的完整历史测试套件；共享代码已通过 EasyShell 全量编译，MySQL/达梦回归仍建议在发布前按项目常规流程执行。

## 7. 验收核对

1. 连接引导可新建/编辑/测试 SQLite 连接：已完成。
2. 可创建或打开 SQLite 文件，取消选择不产生空文件：已完成。
3. 可浏览文件、表、视图、函数、字段、定义和行数，并打开对应信息页：已完成。
4. SQL 查询、更新数量、耗时、错误和事务行为：已完成。
5. 查询与终端入口可用，连接关闭释放资源：已完成。
6. 只读、丢失、损坏和 SQL 错误有明确提示：已完成。
7. MySQL、MariaDB、MongoDB、达梦共享入口无编译回归；完整业务回归待常规发布测试执行。
8. 新增 SQLite 与 `fx-db` 相关测试全部通过：已完成。
9. 表、视图、函数设计器及 SQLite 能力边界：已完成。
10. 导入、导出、dump、SQL 文件执行和 SQLite 间传输：已完成。
