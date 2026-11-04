# SQLite 全功能与 UI 像素对齐测试报告

## 1. 结论

- 测试日期：2026-11-04。
- 测试环境：JDK 27.0.1、JavaFX 27.0.1、macOS aarch64。
- 功能回归：`87` 个 SQLite 相关测试全部通过，`0` failures、`0` errors、`0` skipped。
- 页面像素回归：`25` 个页面、`23` 个页签完成 SQLite / MySQL / 达梦三方截图比较，门禁通过。
- 查询提示弹窗：SQLite 与 MySQL、达梦均为 `0 / 120000` 像素差异。
- 坏视图：依赖表不存在的视图可继续读取定义、进入设计器并修正 SQL；双击记录入口会自动回退到设计器，不再直接报错退出。
- 视图设计预览：名称或定义为空时静默使用 `Unnamed_View` / `SELECT 1` 生成预览，不再弹异常框；保存时仍执行严格校验。
- 函数设计器：函数类型、目标表、返回类型、生成列存储方式和表达式统一为“标签在上、控件在下”的纵向布局，不再混用横向字段行。
- 对齐口径：非方言差异的组件结构、尺寸、间距、主题、字体和颜色按像素门禁对齐；SQLite 不存在的数据库概念保留必要差异，不做伪字段或伪功能。

## 2. 测试范围

### 2.1 功能测试

执行类：

- `ShellSqliteClientTest`
- `SQLiteDesignAndDataTest`
- `SQLiteGeneratorSqlTest`
- `SQLiteIndexControlTest`
- `SQLiteForeignKeyControlTest`
- `SQLiteTriggerControlTest`
- `SQLiteRecordColumnTest`
- `SQLiteDefaultValueTest`
- `ShellSqliteArchitectureTest`
- `ShellSqliteUtilTest`
- `ShellSqliteTerminalTest`
- `ShellSqliteBrokenViewNavigationTest`
- `ShellSqliteFullUiParityTest`
- `ShellSqliteQueryPromptUiParityTest`

覆盖范围：

- 连接创建、只读、文件不存在时创建、关闭、重新打开和损坏文件错误路径。
- 表、视图、函数、字段、索引、外键、触发器、检查约束、DDL 和对象信息页。
- 查询执行、结果集编辑、Explain、事务提交/回滚、Savepoint 和错误恢复。
- 表/视图设计器、函数设计器、表克隆、重命名、删除、刷新和预览。
- 数据过滤、分页、默认值、NULL/空串、BLOB、中文和原始 JDBC 结果。
- SQL/CSV 导出、CSV 导入、Dump、运行 SQL 文件、SQLite 文件间表和视图传输。
- 连接树、首页、对象树、查询页、终端页、国际化资源和 FXML 加载完整性。
- SQLite 与 MySQL、达梦的查询提示类型、图标、宽度、扩展信息、颜色和弹窗尺寸。
- 坏视图元数据读取、设计器修正、保存后重新查询字段。

### 2.2 坏视图导航行为测试

新增 `ShellSqliteBrokenViewNavigationTest`，场景为：

1. 创建依赖不存在表的视图 `broken_view`。
2. 从数据库树查找该视图节点。
3. 调用记录页入口 `openViewRecord("broken_view")`。
4. 断言不会创建 `ShellSqliteViewRecordTab`。
5. 断言自动进入 `openViewDesign("broken_view")`。

该测试直接覆盖用户反馈的双击坏视图进入记录页报错路径，而不仅是源码结构断言。

## 3. 像素测试方法

### 3.1 页面比较

- 对 SQLite、MySQL、达梦分别加载各自原始 FXML。
- 使用同一主题、同一字体环境、同一 Scene 尺寸和同一背景。
- 强制 CSS 与布局完成后截取完整页面。
- 逐像素比较 ARGB，输出差异像素数、差异图和对照截图。
- `ShellSqliteFullUiParityTest` 对页面的非白名单差异默认预算为 `0`，超过预算直接失败。

### 3.2 页签比较

- 选择同名或同 ID 页签。
- 仅截取页签内容区，排除 Tab 标题栏本身的方言文案差异。
- 对 SQLite 专属页签设置明确预算；通用页签默认预算为 `0`。

### 3.3 查询提示弹窗比较

- 使用同一组 database/keyword/table/column/view/function/procedure 提示项。
- 比较列表尺寸、SVG 图标、标签宽度、扩展信息、文字颜色和 Tooltip。
- 三方截图逐像素比较，结果写入独立报告。

## 4. 页面像素结果

自动报告：`target/ui-test-report/sqlite-full-ui/report.md`

| 页面 | SQLite vs MySQL | SQLite vs 达梦 |
| --- | ---: | ---: |
| 连接新增 | 8634 / 912000 | 8790 / 912000 |
| 连接修改 | 8634 / 912000 | 8790 / 912000 |
| Dump | 0 / 912000 | 0 / 912000 |
| 导出 | 0 / 912000 | 0 / 912000 |
| 导入 | 0 / 912000 | 0 / 912000 |
| 运行 SQL 文件 | 0 / 912000 | 0 / 912000 |
| 数据传输 | 0 / 912000 | 0 / 912000 |
| 函数信息 | 359 / 912000 | 359 / 912000 |
| 表信息 | 2687 / 912000 | 202683 / 912000 |
| 视图信息 | 0 / 912000 | 0 / 912000 |
| 索引字段弹窗 | 262 / 912000 | 0 / 912000 |
| 记录过滤弹窗 | 0 / 912000 | 0 / 912000 |
| 函数设计器 | 18923 / 912000 | 18923 / 912000 |
| 首页 | 2530 / 912000 | 2530 / 912000 |
| 查询 Explain | 0 / 912000 | 0 / 912000 |
| 查询信息 | 0 / 912000 | 0 / 912000 |
| 查询主页面 | 0 / 912000 | 0 / 912000 |
| 查询结果 | 0 / 912000 | 0 / 912000 |
| 主 Tab | 3047 / 912000 | 3132 / 912000 |
| 字段扩展 | 0 / 912000 | 0 / 912000 |
| 表设计器 | 1687 / 912000 | 1687 / 912000 |
| 表记录 | 0 / 912000 | 0 / 912000 |
| 终端 | 0 / 912000 | 0 / 912000 |
| 视图设计器 | 493 / 912000 | 493 / 912000 |
| 视图记录 | 0 / 912000 | 0 / 912000 |

非零页面均已分类到第 7 节白名单差异；其余页面为零差异。

## 5. 页签像素结果

自动报告：`target/ui-test-report/sqlite-full-ui/tab-report.md`

| 页面 | 页签 | SQLite vs MySQL | SQLite vs 达梦 |
| --- | --- | ---: | ---: |
| 连接新增 | 基础 | 8113 / 762000 | 8269 / 762000 |
| 连接新增 | 扩展 | 12648 / 762000 | 12648 / 762000 |
| 连接修改 | 基础 | 8113 / 762000 | 8269 / 762000 |
| 连接修改 | 扩展 | 12648 / 762000 | 12648 / 762000 |
| 数据传输 | 表 | 0 / 562600 | 0 / 562600 |
| 数据传输 | 视图 | 0 / 562600 | 0 / 562600 |
| 函数信息 | 基础 | 0 / 762000 | 0 / 762000 |
| 函数信息 | DDL | 0 / 762000 | 0 / 762000 |
| 表信息 | 基础 | 2687 / 762000 | 202683 / 762000 |
| 表信息 | DDL | 0 / 762000 | 0 / 762000 |
| 视图信息 | 基础 | 0 / 762000 | 0 / 762000 |
| 视图信息 | DDL | 0 / 762000 | 0 / 762000 |
| 函数设计器 | 定义 | 17871 / 840000 | 17871 / 840000 |
| 函数设计器 | 预览 | 0 / 840000 | 0 / 840000 |
| 表设计器 | 字段 | 782 / 840000 | 782 / 840000 |
| 表设计器 | 索引 | 309 / 840000 | 2886 / 840000 |
| 表设计器 | 外键 | 4310 / 840000 | 4310 / 840000 |
| 表设计器 | 触发器 | 0 / 840000 | 0 / 840000 |
| 表设计器 | 检查 | 0 / 840000 | 0 / 840000 |
| 表设计器 | 选项 | 9309 / 840000 | 3720 / 840000 |
| 表设计器 | 预览 | 0 / 840000 | 0 / 840000 |
| 视图设计器 | 定义 | 0 / 840000 | 0 / 840000 |
| 视图设计器 | 预览 | 0 / 840000 | 0 / 840000 |

## 6. 本轮修复与对齐结果

### 6.1 坏视图可设计修复

- `ShellSqliteClient.loadViewColumns` 在字段加载失败时保留视图名和定义，返回可用于设计器修正的 `SQLiteView`。
- `ShellSqliteTabPane.openViewRecord` 在打开记录页前验证字段；失败时自动转入视图设计页。
- 保存修正后的定义后可重新查询字段并恢复视图记录入口。

### 6.2 数据工具数据库选择

- 新增 `ShellSqliteDatabaseComboBox`。
- 运行 SQL 文件页由固定只读 `main` 改为数据库下拉框。
- 数据传输页来源/目标数据库由静态标签改为数据库下拉框，并同步数据库名。
- 两个页面的视觉结构已与 MySQL、达梦归零。

### 6.3 信息页

- 表信息移除“记录数”，保留类型、WITHOUT ROWID、STRICT，DDL 页签统一为 `%base.ddl`。
- 函数信息调整为“基础 / SQLite / DDL”三页签；基础、DDL 内容区与 MySQL 像素差异为 `0`。
- 视图信息基础、DDL 内容区像素差异均为 `0`。

### 6.4 设计器

- 视图设计器移除额外内边距；刷新按钮按 MySQL 规则显隐；定义和预览内容区均为 `0` 差异。
- 表设计器统一 `RichTabPane`、高度、列宽和通用页签顺序；页签顺序为“字段、索引、外键、触发器、检查、选项、表级约束、预览”。
- 表设计器字段区差异从 `2127` 降到 `1687`，主要剩余差异为 SQLite 必要列差异。
- 函数设计器统一为纵向字段布局：所有字段使用 `InfoLabel` 在上、控件在下，不再出现“部分字段横向、部分字段纵向”的割裂结构。该布局是用户明确要求的 SQLite 设计，不跟随 MySQL/达梦的横向字段行，因此函数定义页差异为 `17871`，整页差异为 `18923`。

### 6.5 其他

- `SQLiteIndexControl.getColumns()` 按索引定义顺序返回字段。
- `SQLiteRecordProperty.updateOriginal()` 即使没有 UI 节点也更新原始值，修复状态刷新语义。
- 字段扩展区域仅在需要时显示排序规则、检查、生成列等区块，空表不显示无效输入框。
- 首页标题由“数据库”改为“信息”，与 MySQL/达梦首页标题语义一致。

## 7. 必要方言差异白名单

以下差异由 SQLite 文件型数据库模型决定，禁止为了像素归零伪造 MySQL/达梦功能：

1. 连接页：SQLite 使用“数据库文件”；MySQL/达梦使用主机、端口、用户名、密码。SQLite 使用锁等待、只读、文件不存在时创建；MySQL/达梦使用连接级选项。SQLite 无代理/跳板机页。
2. 首页/主 Tab：SQLite 根库显示真实名称 `main`；MySQL/达梦显示固定“数据库”语义。
3. 函数信息：SQLite 保留专属页签，展示内置/表函数类型、参数数量、返回类型、目标表和属性。
4. 表信息：SQLite 展示 WITHOUT ROWID、STRICT；达梦展示表空间等概念。该差异导致表信息对达梦的差异像素较大。
5. 索引字段弹窗：SQLite 不支持 MySQL 的“子部分”（前缀索引），因此少一列。
6. 表设计器字段：SQLite 使用“唯一”列；MySQL/达梦使用“小数位/注释”等列。
7. 表设计器索引：SQLite 使用“条件”列表达部分索引；MySQL/达梦使用“注释”等列。
8. 表设计器外键：SQLite 无 MySQL 的“被引用模式”列。
9. 表设计器页签：SQLite 增加“表级约束”，不提供仅 MySQL/达梦存在的专属选项。
10. 函数设计器：SQLite 使用函数类型/表达式/表函数视图/生成列表达式，并统一采用标签在上、控件在下的纵向布局；MySQL/达梦使用参数、返回值、高级选项等模型和横向字段行。
11. 视图设计器：SQLite 无 MySQL“高级”页签；视图定义直接由目标 SQL 表达。

## 8. 修复前/后指标

| 项目 | 修复前 | 修复后 |
| --- | ---: | ---: |
| 函数设计器布局 | 横向字段行与纵向编辑器混用 | 全部标签在上、控件在下 |
| 表设计器 vs MySQL | 2127 | 1687 |
| 运行 SQL 文件页 vs MySQL | 非零 | 0 |
| 数据传输页 vs MySQL | 非零 | 0 |
| 函数信息基础页 vs MySQL | 非零 | 0 |
| 视图信息基础/DDL vs MySQL | 非零 | 0 |
| 查询提示弹窗三方比较 | 非零/未覆盖 | 0 / 120000 |

## 9. 剩余风险与修复方案

1. 剩余非零像素大多位于方言专属列、页签和连接字段；继续压缩只能通过伪造不存在的能力实现，因此不纳入归零目标。
2. 像素结果依赖字体和主题。当前使用同一运行环境的三方实时截图作为基准，避免固定基线漂移；发布前应在目标操作系统 CI 再执行。
3. 函数设计器采用用户指定的纵向布局，与 MySQL/达梦横向字段行存在有意差异；后续若新增函数能力，应继续使用标签在上、控件在下的统一结构，并同步补充内容区截图测试。
4. 坏视图当前覆盖双击记录入口和设计器保存。后续可补充从上下文菜单、查询入口和树刷新后再次打开的端到端测试。
5. 像素门禁已对非白名单页面和页签默认使用 `0` 预算；新增页面若未登记预算，会直接暴露差异，防止回归被静默放过。
6. 建议在 CI 中固定 JavaFX 字体包与显示缩放；无桌面环境使用 Xvfb 或 Monocle，避免 headless 渲染阻塞。

## 10. 复现命令

```bash
cd /Users/oyzh/IdeaProjects/oyzh/easyshell

# 全量 SQLite 功能、架构、行为与 UI 回归
mvn -q -Dtest='SQLite*Test,ShellSqlite*Test' test

# 25 页面 / 23 页签像素门禁
mvn -q -Dtest=ShellSqliteFullUiParityTest test

# 查询提示弹窗三方像素比较
mvn -q -Dtest=ShellSqliteQueryPromptUiParityTest test

# 坏视图导航行为
mvn -q -Dtest=ShellSqliteBrokenViewNavigationTest test
```

## 11. 测试产物

- `target/ui-test-report/sqlite-full-ui/report.md`
- `target/ui-test-report/sqlite-full-ui/tab-report.md`
- `target/ui-test-report/sqlite-full-ui/*-mysql.png`
- `target/ui-test-report/sqlite-full-ui/*-dameng.png`
- `target/ui-test-report/sqlite-full-ui/*-sqlite.png`
- `target/ui-test-report/sqlite-full-ui/*-diff.png`
- `target/ui-test-report/sqlite-full-ui/tabs/`
- `target/ui-test-report/sqlite-query-prompt/report.md`
- `target/ui-test-report/sqlite-query-prompt/*.png`
- `docs/sqlite/query-prompt-ui-parity-test-report.md`
