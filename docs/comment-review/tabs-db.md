# easyshell 标签页模块代码审查文档 — 达梦 / MySQL

> 范围：`cn/oyzh/easyshell/tabs/` 下 `dameng/` 与 `mysql/`（递归）。
> 总览与通用标签页见 [tabs.md](./tabs.md)。说明：仅新增文档，未改动任何 `.java`。
> 注：`mysql/ShellMysqlTabEventListener.java` 为整文件注释的死代码，未纳入。

## 达梦数据库标签页（主框架/查询/终端/函数）

## ShellDamengBaseTab（tabs.dameng）
> 文件: cn/oyzh/easyshell/tabs/dameng/ShellDamengBaseTab.java
- 职责：达梦数据库所有标签页的抽象基类，提供数据库树节点与模式/连接名称的通用能力。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `abstract ShellDamengSchemaTreeItem dbItem()` | 子类实现 | 返回数据库树节点 |
  | `String schema()` | 模式名称 | `dbItem()==null?null:dbItem().schema()` |
  | `String connectName()` | 连接名称 | `dbItem()==null?null:dbItem().connectName()` |
- 调用链：`schema/connectName → dbItem()`

## ShellDamengTab（tabs.dameng）
> 文件: cn/oyzh/easyshell/tabs/dameng/ShellDamengTab.java
- 职责：达梦数据库连接标签页，承载主界面与客户端。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getTabTitle()` | 标题 | `名称(类型大写)` |
  | `void flushGraphic()` | 图标 | `ShellOsTypeComboBox.getGlyph` |
  | `protected String url()` | FXML | `/tabs/dameng/shellDamengTab.fxml` |
  | `protected ShellDamengTabController controller()` | 控制器 | 强转 |
  | `void init(ShellConnect)` | 初始化 | controller.init + super.init |
  | `ShellBaseClient client()` | 客户端 | `controller().getClient()` |
  | `static ShellDamengTab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellDamengTabController.init`

## ShellDamengTabController（tabs.dameng）
> 文件: cn/oyzh/easyshell/tabs/dameng/ShellDamengTabController.java
- 职责：达梦数据库标签页控制器，负责连接初始化、数据库树加载、过滤与数据导入导出。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | ShellDamengClient | 达梦客户端 |
  | root | FXSplitPane | FXML 注入，根分割面板 |
  | tabPane | ShellDamengTabPane | FXML 注入，标签页容器 |
  | treeView | ShellDamengTreeView | FXML 注入，数据库树 |
  | filterKW | FilterTextField | FXML 注入，过滤输入框 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellConnect shellConnect()` | 连接 | `client.getShellConnect()` |
  | `void init(ShellConnect)` | 初始化并连接 | `newClient`→状态监听→`showMask`：`client.start()`、失败 closeTab；成功 `tabPane.setClient`、`treeView.setClient`、`root().loadChild()/expend()`、`ShellDamengQueryUtil.updateIndex`、`hideLeft` |
  | `ShellDamengClient getClient()` | 客户端 | 返回 client |
  | `void onTabClosed(Event)` | 关闭清理 | `IOUtil.closeAsync(client)` |
  | `private void doFilter()` | 执行过滤 | 取关键字/大小写/全字，设置树高亮与过滤条件，`ThreadUtil.start(treeView.filter)` |
  | `@FXML importData()` | 导入数据 | `ShellDamengViewFactory.importData(client,null)` |
  | `@FXML exportData()` | 导出数据 | `ShellDamengViewFactory.exportData(client,null,null)` |
  | `@FXML runSqlFile()` | 执行 SQL 文件 | `ShellDamengViewFactory.runSqlFile(client,null)` |
  | `@FXML positionNode()` | 定位节点 | `treeView.positionItem()` |
  | `@FXML transportData()` | 数据传输 | `ShellDamengViewFactory.transportData(shellConnect,null)` |
  | `protected void bindListeners()` | 绑定监听 | Ctrl+F 聚焦过滤框；过滤文本/全字/大小写变化触发 `doFilter` |
- 调用链：`init → client.start → treeView.root().loadChild`；`filterKW → doFilter → treeView.filter`

## ShellDamengTabPane（tabs.dameng）
> 文件: cn/oyzh/easyshell/tabs/dameng/ShellDamengTabPane.java
- 职责：达梦标签页容器，负责各类设计/查询/终端标签页的创建、切换与关闭，并作为事件订阅中心。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | clientProperty | SimpleObjectProperty<ShellDamengClient> | 客户端属性（懒初始化） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void setClient/getClient/clientProperty()` | 客户端访问器 | 读写 clientProperty |
  | `void initNode()` | 节点初始化 | super + `initHomeTab`；监听 tabs 增删，`TaskManager.startDelay(flushHomeTab,100)` |
  | `private void flushHomeTab()` | 主页 tab 维护 | tab 空则建主页，>1 则关主页 |
  | `ShellDamengHomeTab getHomeTab()` / `initHomeTab()` / `closeHomeTab()` | 主页 tab | 增删主页 tab |
  | `private getFunctionDesignTab/getProcedureDesignTab/getTableRecordTab/getTableDesignTab/getViewRecordTab/getViewDesignTab/getTerminalTab(...)` | 查找已开 tab | 遍历 `getTabs()` 按类型 + dbItem + 名称匹配 |
  | `private List<ShellDamengBaseTab> getBaseTabs(dbItem)` | 收集 tab | 匹配同一 dbItem 的所有 `ShellDamengBaseTab` |
  | `private ShellDamengQueryMainTab getDamengQueryMainTab(queryId)` | 查找查询 tab | 按 queryId 匹配 |
  | `@EventSubscribe onTableOpen/onTableRenamed/onTableCleared/onTableTruncated/onTableDropped/onTableAlerted(...)` | 表事件 | 创建/选中/关闭/重载表相关 tab |
  | `@EventSubscribe onViewOpen/viewAlerted/onViewRenamed/onViewDropped(...)` | 视图事件 | 打开/重载/关闭视图 tab |
  | `@EventSubscribe onQueryAdd/onQueryDeleted/onQueryOpen/onQueryRenamed(...)` | 查询事件 | 新增/移除/打开/关闭查询 tab |
  | `@EventSubscribe onFunctionDesign/onFunctionRenamed/onFunctionDropped(...)` | 函数事件 | 创建设计 tab / 关闭 |
  | `@EventSubscribe onProcedureDesign/onProcedureRenamed/onProcedureDropped(...)` | 存储过程事件 | 创建设计 tab / 关闭 |
  | `@EventSubscribe onViewDesign/onTableDesign(...)` | 设计事件 | 创建设计 tab |
  | `@EventSubscribe onSchemaClosed/onSchemaDropped(...)` | 模式事件 | `removeTab(getBaseTabs(...))` |
  | `@EventSubscribe onTerminalOpen(...)` | 终端事件 | 创建/选中终端 tab |
- 调用链：`事件 → onXxx → getXxxTab（存在复用）→ addTab/select/removeTab`

## ShellDamengQueryExplainTab（tabs.dameng.query）
> 文件: cn/oyzh/easyshell/tabs/dameng/query/ShellDamengQueryExplainTab.java
- 职责：达梦 SQL 执行计划（解释）结果标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `dameng/query/shellDamengQueryExplainTab.fxml` |
  | `void init(String title, DamengExplainResult result)` | 初始化 | `setTitle(title)` + `controller().init(result)` |
  | `ShellDamengQueryExplainTabController controller()` | 控制器 | 强转 |
  | `void initNode()` | 节点初始化 | `setClosable(false)` + super |
  | `static ShellDamengQueryExplainTab of(String,DamengExplainResult)` | 工厂 | new+init |
- 调用链：`of → init → ShellDamengQueryExplainTabController.init`

## ShellDamengQueryExplainTabController（tabs.dameng.query）
> 文件: cn/oyzh/easyshell/tabs/dameng/query/ShellDamengQueryExplainTabController.java
- 职责：达梦解释结果控制器，展示解释计划的列、记录与 SQL/耗时/总数信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | sql | FXText | FXML 注入，SQL 文本 |
  | used | FXText | FXML 注入，耗时 |
  | count | FXText | FXML 注入，计数 |
  | recordTable | DamengRecordTableView | FXML 注入，结果表格 |
  | result | DamengExplainResult | 执行结果 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(DamengExplainResult)` | 初始化 | 保存结果并 `initDataList()` |
  | `private void initDataList()` | 初始化列表 | `initColumns`、`initRecords`、设置 sql/used/count 文本 |
  | `private void initColumns(List<DamengColumn>)` | 初始化列 | 首列 `DBStatusColumn`，其余 `DamengRecordColumn` 并按 `DBUtil.suitableColumnWidth` 设宽 |
  | `private void initRecords(List<DamengRecord>)` | 初始化记录 | `recordTable.setItem(records)` |
- 调用链：`init → initDataList → initColumns/initRecords`

## ShellDamengQueryInfoTab（tabs.dameng.query）
> 文件: cn/oyzh/easyshell/tabs/dameng/query/ShellDamengQueryInfoTab.java
- 职责：达梦查询信息标签页，展示 SQL 执行结果信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `dameng/query/shellDamengQueryInfoTab.fxml` |
  | `void init(DBQueryResults<?>)` | 初始化 | `controller().init(results)` |
  | `ShellDamengQueryInfoTabController controller()` | 控制器 | 强转 |
  | `void initNode()` | 节点初始化 | `setClosable(false)` + super |
  | `static ShellDamengQueryInfoTab of(DBQueryResults<?>)` | 工厂 | new+init |
- 调用链：`of → init → ShellDamengQueryInfoTabController.init`

## ShellDamengQueryInfoTabController（tabs.dameng.query）
> 文件: cn/oyzh/easyshell/tabs/dameng/query/ShellDamengQueryInfoTabController.java
- 职责：达梦查询信息控制器，将执行结果信息输出到信息区域。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | infoArea | Editor | FXML 注入，信息展示区 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(DBQueryResults<?>)` | 初始化 | 清空后逐条输出：成功输出内容+（Affected rows/OK）；失败输出消息；均追加耗时行 |
- 调用链：`init → infoArea.clear/appendLine`

## ShellDamengQueryMainTab（tabs.dameng.query）
> 文件: cn/oyzh/easyshell/tabs/dameng/query/ShellDamengQueryMainTab.java
- 职责：达梦查询主标签页，负责 SQL 的编辑与执行。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `dameng/query/shellDamengQueryMainTab.fxml` |
  | `void flushGraphic()` | 图标 | `QuerySVGGlyph` |
  | `void flushTitle()` | 标题 | 未保存加 `* ` 前缀；`名称@模式(连接)` |
  | `ShellQuery query()` | 查询对象 | `controller().getQuery()` |
  | `String queryId()` | 查询 id | `query().getUid()` |
  | `ShellDamengSchemaTreeItem dbItem()` | 树节点 | `controller().getDbItem()` |
  | `void init(ShellQuery, ShellDamengSchemaTreeItem)` | 初始化 | controller.init + flush |
  | `ShellDamengQueryMainTabController controller()` | 控制器 | 强转 |
  | `static ShellDamengQueryMainTab of(...)` | 工厂 | new+init |
- 调用链：`of → init → ShellDamengQueryMainTabController.init`

## ShellDamengQueryMainTabController（tabs.dameng.query）
> 文件: cn/oyzh/easyshell/tabs/dameng/query/ShellDamengQueryMainTabController.java
- 职责：达梦查询主内容控制器，管理 SQL 编辑器、运行/解释/保存与结果标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | query | ShellQuery | 查询对象 |
  | unsaved | boolean | 未保存标志 |
  | dbItem | ShellDamengSchemaTreeItem | 数据库树节点 |
  | queryArea | ShellDamengQueryEditor | FXML 注入，查询编辑区 |
  | resultTabPane | FXTabPane | FXML 注入，结果标签容器 |
  | root | FXVBox | FXML 注入，根节点 |
  | infoTab | ShellDamengQueryInfoTab | FXML 注入，信息 tab |
  | splitPane | FXSplitPane | FXML 注入，分割面板 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellQuery getQuery()` / `getDbItem()` | 访问器 | 返回 query/dbItem |
  | `void init(ShellQuery, ShellDamengSchemaTreeItem)` | 初始化 | 设 query/dbItem、`showNode(0)`、编辑器设置内容/方言，文本变化置 unsaved |
  | `protected void bindListeners()` | 绑定 | 结果 tab 刷新监听、选中切换 `showNode`、编辑器运行回调 `run` |
  | `private void clearTabs()` | 清理结果 tab | 移除除 infoTab 外的 tab |
  | `@FXML pretty()` | 美化 | `queryArea.pretty()` |
  | `@FXML run()` | 运行 | 取选中/全部 SQL，mask 内 `doRun` |
  | `private void doRun(String)` | 执行 | `dbItem.executeSql`、清 tab、建 infoTab 与各 selectTab、选择显示、`showNode` |
  | `@FXML explain()` | 解释 | `dbItem.explainSql`、建 infoTab 与 explainTab、选择显示 |
  | `private void initInfoTab(DBQueryResults<?>)` | 初始化信息 tab | `infoTab.init` |
  | `private ShellDamengQuerySelectTab initSelectTab(...)` | 建结果 tab | new + init + 设置 id/prop |
  | `private ShellDamengQueryExplainTab initExplainTab(...)` | 建解释 tab | new + init + 设置 id/prop |
  | `@FXML save()` | 保存查询 | 补名称、写内容/dbName/iid，新增 `ShellQueryStore.insert`+树追加，否则 update；成功清 unsaved |
  | `@FXML queryKeyPressed(KeyEvent)` | 快捷键 | Ctrl+S 保存、Ctrl+R 运行 |
  | `private void showNode(int)` | 布局切换 | 0 隐藏结果面板，1/2 显示并设分隔比例 |
  | `boolean isUnsaved()` | 是否未保存 | 返回 unsaved |
- 调用链：`run → doRun → dbItem.executeSql → initSelectTab`；`explain → dbItem.explainSql → initExplainTab`

## ShellDamengQuerySelectTab（tabs.dameng.query）
> 文件: cn/oyzh/easyshell/tabs/dameng/query/ShellDamengQuerySelectTab.java
- 职责：达梦查询结果标签页，展示查询执行结果。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `dameng/query/shellDamengQuerySelectTab.fxml` |
  | `void init(String, DamengExecuteResult, ShellDamengSchemaTreeItem)` | 初始化 | setTitle + controller.init |
  | `ShellDamengQuerySelectTabController controller()` | 控制器 | 强转 |
  | `void initNode()` | 节点初始化 | `setClosable(false)` + super |
  | `static ShellDamengQuerySelectTab of(...)` | 工厂 | new+init |
- 调用链：`of → init → ShellDamengQuerySelectTabController.init`

## ShellDamengQuerySelectTabController（tabs.dameng.query）
> 文件: cn/oyzh/easyshell/tabs/dameng/query/ShellDamengQuerySelectTabController.java
- 职责：达梦查询结果控制器，负责结果展示与记录的增/改/删、应用/丢弃、导出、刷新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | FXML 注入，根节点 |
  | sql / used / count | FXText | FXML 注入，SQL/耗时/计数 |
  | recordTable | DamengRecordTableView | FXML 注入，记录表格 |
  | dbItem | ShellDamengSchemaTreeItem | 数据库树节点 |
  | result | DamengExecuteResult | 执行结果 |
  | add / delete / apply / discard | SVGGlyph | FXML 注入，增/删/应用/丢弃按钮 |
  | changeListener | DBStatusListener | 记录变更监听器 |
  | columns | List<DamengColumn> | 字段列表 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(DamengExecuteResult, ShellDamengSchemaTreeItem)` | 初始化 | 若可更新则建 `DBStatusListener` 并显示增/删/应用/丢弃按钮，`initDataList` |
  | `private void initDataList()` | 初始化列表 | initColumns/initRecords/sql/used/initCount |
  | `private void initCount(int)` | 计数 | 设置 count 文本 |
  | `private void initColumns(List<DamengColumn>)` | 列 | 首列 `DBStatusColumn`，其余 `DamengRecordColumn` |
  | `private void initRecords(List<DamengRecord>)` | 记录 | `recordTable.setItem` |
  | `@FXML addRecord()` | 新增行 | 建 `DamengRecord`（created），按默认值填充，加行并选中末行 |
  | `private void insertRecord(DamengRecord)` | 插入 | 建主键/参数，`dbItem.client().insertRecord`，有主键则回显 |
  | `private void updateRecord(DamengRecord)` | 更新 | 有主键按主键更新并回显；无主键按全部字段更新 |
  | `private DamengRecordPrimaryKey initPrimaryKey(DamengRecord)` | 主键 | 依 `result.getPrimaryKey()` 构造 |
  | `@FXML apply()` | 应变更 | 遍历记录，created→insert、changed→update，清状态、禁用 apply |
  | `@FXML discard()` | 丢弃 | 还原 changed、移除 created，禁用 apply、更新计数 |
  | `@FXML reload()` | 刷新 | 有未保存先确认，`dbItem.executeSingleSql` 重查并重建 |
  | `@FXML exportRecord()` | 导出 | 由 tabPane 取 query，构造 `ShellDamengDataExportTable`，`ShellDamengViewFactory.exportData` |
  | `@FXML deleteRecord()` | 删除 | 确认后 mask 内批量 `deleteRecords` |
  | `private void deleteRecords(List<DamengRecord>)` | 批量删除 | 逐条 `deleteRecord`，成功移除并更新计数 |
  | `private boolean deleteRecord(DamengRecord)` | 单条删除 | created 直接成功，否则按主键 `dbItem.deleteRecord`，成功 `record.destroy()` |
  | `void onTabClosed(Event)` | 关闭 | 移除 `DBStatusListenerManager` 监听 |
  | `protected void bindListeners()` | 绑定 | 选中可编辑、discard 绑定 apply、apply 状态切换按钮组、行变化启用 apply、Ctrl+S 应用 |
- 调用链：`apply → insertRecord/updateRecord → dbItem.client()/dbItem.deleteRecord`；`reload → dbItem.executeSingleSql`

## ShellDamengTerminalTab（tabs.dameng.terminal）
> 文件: cn/oyzh/easyshell/tabs/dameng/terminal/ShellDamengTerminalTab.java
- 职责：达梦命令行终端标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengTerminalTabController controller()` | 控制器 | 强转 |
  | `protected String url()` | FXML | `dameng/terminal/shellDamengTerminalTab.fxml` |
  | `void flushGraphic()` | 图标 | `TerminalSVGGlyph` |
  | `void flushTitle()` | 标题 | `schema(连接名)` |
  | `void init(ShellDamengSchemaTreeItem)` | 初始化 | controller.init + flush |
  | `ShellConnect shellConnect()` | 连接 | `controller().shellConnect()` |
  | `ShellDamengClient client()` | 客户端 | `controller().client()` |
  | `ShellDamengSchemaTreeItem dbItem()` | 树节点 | `controller().getDbItem()` |
- 调用链：`init → ShellDamengTerminalTabController.init`

## ShellDamengTerminalTabController（tabs.dameng.terminal）
> 文件: cn/oyzh/easyshell/tabs/dameng/terminal/ShellDamengTerminalTabController.java
- 职责：达梦命令行内容控制器，托管命令行面板。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | terminal | DamengTerminalPane | FXML 注入，命令行文本域 |
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(ShellDamengSchemaTreeItem)` | 初始化 | 保存 dbItem，`terminal.init(dbItem.client(), dbItem.schema())` |
  | `ShellDamengSchemaTreeItem getDbItem()` | 树节点 | 返回 dbItem |
  | `protected ShellConnect shellConnect()` | 连接 | `terminal.shellConnect()` |
  | `ShellDamengClient client()` | 客户端 | `terminal.getClient()` |
  | `void onTabClosed(Event)` | 关闭 | 临时终端则 `client().close()`，super |
- 调用链：`init → terminal.init`；`onTabClosed → client().close`

## ShellDamengFunctionDesignTab（tabs.dameng.function）
> 文件: cn/oyzh/easyshell/tabs/dameng/function/ShellDamengFunctionDesignTab.java
- 职责：达梦函数设计标签页，负责函数信息展示与设计维护。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `dameng/function/shellDamengFunctionDesignTab.fxml` |
  | `void flushGraphic()` | 图标 | `FunctionSVGGlyph` |
  | `void flushTitle()` | 标题 | 未保存加 `* `；`函数名@模式(连接)` |
  | `String functionName()` | 函数名 | `controller().getFunction().getName()` |
  | `ShellDamengSchemaTreeItem dbItem()` | 树节点 | `controller().getDbItem()` |
  | `void init(DamengFunction, ShellDamengSchemaTreeItem)` | 初始化 | controller.init + flush |
  | `ShellDamengFunctionDesignTabController controller()` | 控制器 | 强转 |
  | `boolean isUnsaved()` | 是否未保存 | `controller().isUnsaved()` |
  | `protected void onTabCloseRequest(Event)` | 关闭请求 | 未保存则确认，否则 closeTab |
- 调用链：`init → ShellDamengFunctionDesignTabController.init`

## ShellDamengFunctionDesignTabController（tabs.dameng.function）
> 文件: cn/oyzh/easyshell/tabs/dameng/function/ShellDamengFunctionDesignTabController.java
- 职责：达梦函数设计内容控制器，管理函数定义、特征、参数、返回值与保存/SQL 预览。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | function | DamengFunction | 当前函数对象 |
  | dbItem | ShellDamengSchemaTreeItem | 数据库树节点 |
  | definition / preview | Editor | FXML 注入，定义/预览编辑器 |
  | tabPane | FXTabPane | FXML 注入，切换面板 |
  | securityType | ShellDamengSecurityTypeComboBox | FXML 注入，安全性 |
  | parallelEnable/deterministic/resultCache/pipelined/aggregate | FXCheckBox | FXML 注入，各特征开关 |
  | paramTable | DBStatusTableView<DamengRoutineParam> | FXML 注入，参数表格 |
  | returnType | DBFiledTypeComboBox | FXML 注入，返回值类型 |
  | returnValues | ShellDBEnumTextFiled | FXML 注入，返回值列表 |
  | returnDigits / returnSize | NumberTextField | FXML 注入，返回小数/长度 |
  | listener | DBStatusListener | 数据变更监听器 |
  | unsaved / newData / initiating | boolean | 未保存/新数据/初始化中标志 |
  | functionName | String | 保存时的函数名 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DamengFunction getFunction()` | 函数对象 | 返回 function |
  | `void init(DamengFunction, ShellDamengSchemaTreeItem)` | 初始化 | 存 dbItem/function、`newData=function.isNew()`、mask 内 `doInit` |
  | `private void doInit()` | 执行初始化 | `initDBListener` + `FXUtil.runWait(initInfo)` |
  | `private void initDBListener()` | 数据监听 | 解绑旧监听，重建 `DBStatusListener` 绑定到各控件与 paramTable |
  | `private void initChangedFlag()` | 变更标志 | 非初始化中则置 unsaved + flushTab |
  | `protected void initInfo()` | 填充信息 | newData 设默认模板正文；否则查询函数信息填充定义/参数/安全性/特征/返回值；结束清 initiating |
  | `@FXML refresh()` | 刷新 | 确认后重新 init + flushTab |
  | `@FXML save()` | 保存 | mask 内 `doSave` |
  | `private void doSave()` | 执行保存 | `tempData` 构造临时函数，新增则 `dbItem.createFunction` 并追加树节点，否则 `dbItem.alertFunction`；重置标志、刷新、重置表格、`initPreview` |
  | `private DamengFunction tempData()` | 临时数据 | 汇集名称/schema/参数/定义/安全性/特征/返回值 |
  | `private void initPreview()` | 预览 | newData 用 `DamengFunctionCreateSqlGenerator`，否则 `DamengFunctionAlertSqlGenerator` 生成 SQL |
  | `@FXML addParam()` | 加参数 | 新增 created 参数并选中末行 |
  | `@FXML deleteParam()` | 删参数 | 非新增确认；created 直接移除，否则标记 deleted |
  | `@FXML moveParamUp/Down()` | 参数排序 | `TableViewUtil.moveUp/moveDown` |
  | `String schema()` | 模式 | `dbItem.schema()` |
  | `boolean isUnsaved()` / `setUnsaved(boolean)` | 未保存 | 读写 unsaved |
  | `ShellDamengSchemaTreeItem getDbItem()` / `setDbItem(...)` | 树节点 | 读写 dbItem |
  | `protected void bindListeners()` | 绑定 | 参数列表变化 `initParamTable`；大量 Ctrl+S 绑定；返回值类型变化启停 size/digits/value；切换面板显示 param 组、预览页 `initPreview` |
  | `private void initParamTable()` | 参数表 | 为每个参数设 dbClient |
- 调用链：`save → doSave → tempData → dbItem.createFunction/alertFunction → initPreview`；`initPreview → DamengFunction*SqlGenerator.generateSqlSingle`

## 达梦数据库标签页（首页/存储过程/表/视图）

## ShellDamengHomeTab
> 文件: cn/oyzh/easyshell/tabs/dameng/home/ShellDamengHomeTab.java
- 职责：达梦连接首页（信息概览）的标签页骨架，继承 `RichTab`，负责加载 FXML、标题与图标。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） |  |  |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | protected String url() | 返回首页 FXML 路径 | 拼接 `FXConst.TAB_PATH + "dameng/home/shellDamengHomeTab.fxml"` |
  | public String getTabTitle() | 返回标签标题 | 调用 `I18nHelper.info()` |
  | public void flushGraphic() | 刷新图标 | 无图标时创建 `DamengSVGGlyph` 并设为 `Cursor.DEFAULT` |
  | public ShellDamengHomeTabController controller() | 获取控制器 | 强转 `super.controller()` |
- 调用链：`ShellDamengHomeTab.url → FXML加载 → ShellDamengHomeTabController`

## ShellDamengHomeTabController
> 文件: cn/oyzh/easyshell/tabs/dameng/home/ShellDamengHomeTabController.java
- 职责：首页内容控制器，展示达梦连接的产品类型与版本信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | type | FXLabel | 类型标签（FXML 注入） |
  | version | FXLabel | 版本标签（FXML 注入） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | public void onTabInit(FXTab tab) | 标签初始化 | 监听 `tabPaneProperty`，取 `ShellDamengTabPane` 的 `client`；已有 client 直接 `initInfo`，否则监听 `clientProperty`；末尾调用 `super.flushTab()` |
  | private void initInfo(ShellDamengClient client) | 加载类型/版本 | client 未关闭时分别设置 `type.text(client.selectProduct())`、`version.text(client.selectVersion())`，异常打印堆栈 |
- 调用链：`onTabInit → ShellDamengTabPane.getClient → initInfo → ShellDamengClient.selectProduct/selectVersion`

## ShellDamengProcedureDesignTab
> 文件: cn/oyzh/easyshell/tabs/dameng/procedure/ShellDamengProcedureDesignTab.java
- 职责：达梦存储过程设计标签页，负责过程信息展示与设计维护，继承 `ShellDamengBaseTab`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） |  |  |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | protected String url() | 过程设计 FXML 路径 | `FXConst.TAB_PATH + "dameng/procedure/shellDamengProcedureDesignTab.fxml"` |
  | public void flushGraphic() | 刷新图标 | 无图标时创建 `ProcedureSVGGlyph` |
  | public void flushTitle() | 刷新标题 | 取 `procedureName()`（空则 `I18nHelper.unnamedProcedure()`），未保存加 `*` 前缀，格式 `name@schema(connectName)` |
  | public DamengProcedure procedure() | 获取过程对象 | `controller().getProcedure()` |
  | public String procedureName() | 获取过程名 | `procedure().getName()` |
  | public ShellDamengSchemaTreeItem dbItem() | 获取库树节点 | `controller().getDbItem()` |
  | public void init(DamengProcedure procedure, ShellDamengSchemaTreeItem item) | 初始化 | 委托 `controller().init(...)` 后 `flush()` |
  | public ShellDamengProcedureDesignTabController controller() | 获取控制器 | 强转 `super.controller()` |
  | public boolean isUnsaved() | 是否未保存 | `controller().isUnsaved()` |
  | protected void onTabCloseRequest(Event event) | 关闭请求 | 未保存时弹窗确认，否则 `closeTab()` |
- 调用链：`init → Controller.init → doInit → initInfo`

## ShellDamengProcedureDesignTabController
> 文件: cn/oyzh/easyshell/tabs/dameng/procedure/ShellDamengProcedureDesignTabController.java
- 职责：存储过程设计内容控制器，管理过程定义、参数表、安全性与并行特征，并生成创建/修改 SQL 预览。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | procedure | DamengProcedure | 当前过程对象 |
  | dbItem | ShellDamengSchemaTreeItem | 数据库树节点 |
  | definition | Editor | 过程定义编辑器（FXML 注入） |
  | preview | Editor | SQL 预览编辑器（FXML 注入） |
  | tabPane | FXTabPane | 切换面板（FXML 注入） |
  | securityType | ShellDamengSecurityTypeComboBox | 安全性选择（FXML 注入） |
  | parallelEnable | FXCheckBox | 特征 parallelEnable（FXML 注入） |
  | paramTable | DBStatusTableView<DamengRoutineParam> | 参数表单（FXML 注入） |
  | listener | DBStatusListener | 数据变更监听器 |
  | unsaved | boolean | 未保存标志 |
  | newData | boolean | 新数据标志 |
  | initiating | boolean | 初始化中标志 |
  | procedureName | String | 过程名称（保存时缓存） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | public DamengProcedure getProcedure() | 获取过程对象 | 返回 `procedure` |
  | public void init(DamengProcedure procedure, ShellDamengSchemaTreeItem dbItem) | 初始化 | 保存参数，`newData = procedure.isNew()`，`StageManager.showMask(this::doInit)` |
  | private void doInit() | 执行初始化 | `initDBListener()` 后 `FXUtil.runWait(this::initInfo)` |
  | private void initDBListener() | 初始化监听器 | 解绑旧 listener；新建以 `schema:name` 命名的 `DBStatusListener`，绑定 `definition/securityType/parallelEnable`，并 `paramTable.setStatusListener` |
  | private void initChangedFlag() | 置未保存 | 非初始化中时 `unsaved = true` 并 `flushTab()` |
  | protected void initInfo() | 加载信息 | 新数据填默认过程体并隐藏 action3；否则 `dbItem.selectProcedure` 重查，填充定义/参数/安全性/`PARALLEL_ENABLE`；结束置 `initiating=false` |
  | private void refresh() | 刷新（@FXML） | 确认后重调 `init` 并 `flushTab()` |
  | private void save() | 保存（@FXML） | `StageManager.showMask(this::doSave)` |
  | private void doSave() | 执行保存 | 组装临时对象；新数据提示过程名后 `dbItem.createProcedure` 并加入树；否则 `dbItem.alertProcedure`；复位标志，`initInfo`/`paramTable.reset`/`initPreview` |
  | private DamengProcedure tempData() | 组装临时数据 | 从表单读取定义、参数、安全性并按 `parallelEnable` 拼装 `characteristic` |
  | private void initPreview() | 生成 SQL 预览 | 新数据用 `DamengProcedureCreateSqlGenerator`，否则 `DamengProcedureAlertSqlGenerator` |
  | private void addParam() | 新增参数（@FXML） | 新建 `DamengRoutineParam`（created=true）加入 paramTable |
  | private void deleteParam() | 删除参数（@FXML） | 非新建先确认；新建直接移除，已存在则标记 `deleted` |
  | private void moveParamUp() / moveParamDown() | 参数上/下移（@FXML） | `TableViewUtil.moveUp/moveDown(this.paramTable)` |
  | public ShellDamengSchemaTreeItem getDbItem() / setDbItem(...) | 树节点读写 | getter/setter |
  | public String schema() | 模式名 | `dbItem.schema()` |
  | public boolean isUnsaved() / setUnsaved(boolean) | 未保存标志读写 | getter/setter |
  | protected void bindListeners() | 绑定监听 | paramTable 列表变化触发 `initParamTable`；`NodeUtil.nodeOnCtrlS` 为各控件绑定 Ctrl+S 保存；tabPane 切换显示/隐藏 param 组并切换预览 |
  | private void initParamTable() | 初始化参数表 | 为每个 `DamengRoutineParam` 设置 `dbClient` |
- 调用链：`init → doInit → initDBListener/initInfo → tempData → Generator.generateSqlSingle → preview`

## ShellDamengTableColumnExtraController
> 文件: cn/oyzh/easyshell/tabs/dameng/table/ShellDamengTableColumnExtraController.java
- 职责：表字段配置弹窗控制器（子标签），按字段类型能力动态显示默认值、值、主键长度、自增、无符号、时间戳更新等项。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | defaultValueBox | FXHBox | 默认值组件容器（FXML 注入） |
  | defaultValue | DamengDefaultValueTextFiled | 默认值输入（FXML 注入） |
  | valueBox | FXHBox | 字段值组件容器（FXML 注入） |
  | value | ShellDBEnumTextFiled | 字段枚举值输入（FXML 注入） |
  | primaryKeySizeBox | FXHBox | 主键长度组件容器（FXML 注入） |
  | primaryKeySize | NumberTextField | 主键长度（FXML 注入） |
  | autoIncrementBox | FXHBox | 自动递增组件容器（FXML 注入） |
  | autoIncrement | FXCheckBox | 自动递增（FXML 注入） |
  | unsignedBox | FXHBox | 无符号组件容器（FXML 注入） |
  | unsigned | FXCheckBox | 无符号（FXML 注入） |
  | currentTimestampBox | FXHBox | 根据时间戳更新容器（FXML 注入） |
  | currentTimestamp | FXCheckBox | 根据时间戳更新（FXML 注入） |
  | column | DamengColumn | 当前 db 字段 |
  | dbClient | ShellDamengClient | db 客户端 |
  | ignoreChanged | boolean | 忽略变更标志 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | private void apply() | 回写字段 | ignoreChanged 时跳过；按各 Box 可见性回写值/无符号/默认值/自增/主键长度/时间戳 |
  | protected void bindListeners() | 绑定监听 | 各类控件变化事件均调用 `apply()` |
  | public void init(DamengColumn column, ShellDamengColumn, ShellDamengClient) | 初始化 | 移除旧监听；设置 column/dbClient；`doInit()`；监听 `typeProperty` 变化 |
  | private void listenColumnTypeChanged(ObservableValue, String, String) | 类型变更回调 | 调用 `doInit()` 重新按类型刷新 |
  | public void doInit() | 按能力刷新 | 依据 `column.supportValue/supportDefaultValue/supportAutoIncrement/supportUnsigned/supportKeySize/supportTimestamp` 显示/隐藏各 Box 并回填值 |
  | public void initialize(URL, ResourceBundle) | 初始化（@Override） | 对六个 Box 调 `managedBindVisible()` |
- 调用链：`ShellDamengTableDesignTabController.bindListeners → tableColumnExtraController.init → doInit`

## ShellDamengTableDesignTab
> 文件: cn/oyzh/easyshell/tabs/dameng/table/ShellDamengTableDesignTab.java
- 职责：达梦表设计标签页，承载表结构（字段/索引/外键/触发器/检查）设计。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） |  |  |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | protected String url() | FXML 路径 | `FXConst.TAB_PATH + "dameng/table/shellDamengTableDesignTab.fxml"` |
  | public void flushGraphic() | 刷新图标 | 无图标时创建 `TableSVGGlyph` |
  | public void flushTitle() | 刷新标题 | 取 `tableName()`（空则 `I18nHelper.unnamedTable()`），未保存加 `*`，格式 `schema-name` |
  | public String tableName() | 表名 | `controller().tableName()` |
  | public void init(DamengTable table, ShellDamengSchemaTreeItem dbItem) throws Exception | 初始化 | `StageManager.showMask` 内调 `controller().init(...)` 并 `flush()` |
  | public ShellDamengTableDesignTabController controller() | 获取控制器 | 强转 `super.controller()` |
  | public boolean isUnsaved() | 是否未保存 | `controller().isUnsaved()` |
  | protected void onTabCloseRequest(Event event) | 关闭请求 | 未保存时确认，否则 `closeTab()` |
  | public ShellDamengSchemaTreeItem dbItem() | 库树节点 | `controller().getDbItem()` |
- 调用链：`init → Controller.init → doInit → initInfo`

## ShellDamengTableDesignTabController
> 文件: cn/oyzh/easyshell/tabs/dameng/table/ShellDamengTableDesignTabController.java
- 职责：表设计核心业务控制器，管理表空间/注释、字段/索引/外键/触发器/检查五个子表，负责校验、保存与 SQL 预览，并内嵌字段配置子控制器。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tabPane | FXTabPane | 切换面板（FXML 注入） |
  | tableSpace | DamengTableSpaceComboBox | 表空间选择（FXML 注入） |
  | tableComment | FXTextArea | 注释（FXML 注入） |
  | preview | Editor | SQL 预览（FXML 注入） |
  | columnTable | DBStatusTableView<DamengColumnControl> | 字段表（FXML 注入） |
  | indexTable | DBStatusTableView<DamengIndexControl> | 索引表（FXML 注入） |
  | foreignKeyTable | DBStatusTableView<DamengForeignKeyControl> | 外键表（FXML 注入） |
  | triggerTable | DBStatusTableView<DamengTriggerControl> | 触发器表（FXML 注入） |
  | checkTable | DBStatusTableView<DamengCheckControl> | 检查器表（FXML 注入） |
  | tableColumnExtraController | ShellDamengTableColumnExtraController | 字段额外信息子控制器（FXML 注入） |
  | table | DamengTable | 当前表对象 |
  | dbItem | ShellDamengSchemaTreeItem | 数据库树节点 |
  | listener | DBStatusListener | 数据变更监听器 |
  | unsaved | boolean | 未保存标志 |
  | newData | boolean | 新数据标志 |
  | initiating | boolean | 初始化中标志 |
  | tableName | String | 表名（保存时缓存） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | private DamengCreateTableParam initCreateParam() | 建表参数 | `(DamengCreateTableParam) initParam(true)` |
  | private DamengAlertTableParam initAlertParam() | 改表参数 | `(DamengAlertTableParam) initParam(false)` |
  | private Object initParam(boolean isCreate) | 组装参数 | 汇集注释/表空间/字段/索引/外键/触发器/检查（含删除项），调用 `dbItem.createTableParam` 或 `dbItem.alterTableParam` |
  | private void refresh() | 刷新（@FXML） | 确认后 `initTable/resetTable/init/initPreview` |
  | private void save() | 保存（@FXML） | `StageManager.showMask(this::doSave)` |
  | private void doSave() | 执行保存 | 逐表校验 invalid 并定位对应 tab；新数据提示表名后 `dbItem.createTable` 加入树；否则 `dbItem.alterTable` 并 `ShellDamengEventUtil.tableAlerted`；复位后 `initInfo/resetTable/initPreview` |
  | private void initChangedFlag() | 置未保存 | 非初始化中时 `unsaved=true` 并 `flushTab()` |
  | protected void resetTable() throws Exception | 重置五表 | 依次 `reset()` index/check/column/trigger/foreignKey 表 |
  | protected void initInfo() | 加载信息 | 新数据 `initNew()`，否则重查表并 `initNormal()`；结束置 `initiating=false` |
  | protected void initNew() | 新建表初始化 | 显示 action2、隐藏 action3 |
  | protected void initNormal() | 已有表初始化 | 填充表空间/注释并加载检查、索引、字段、触发器、外键子表数据 |
  | private void addColumn() / deleteColumn() | 字段增删 | add 新建 `DamengColumnControl`（created、nullable）；delete 确认后移除并标记 `deleted` |
  | private void addIndex() / deleteIndex() | 索引增删 | 同字段增删模式，操作 `indexTable` |
  | private void addForeignKey() / deleteForeignKey() | 外键增删 | 同上，操作 `foreignKeyTable` |
  | private void addTrigger() / deleteTrigger() | 触发器增删 | 同上，操作 `triggerTable` |
  | private void addCheck() / deleteCheck() | 检查增删 | 同上，操作 `checkTable` |
  | private void initTable() | 初始化子表控件 | 为五表设置 Ctrl+S 保存、绑定 `NodeUtil.nodeOnCtrlS`，监听字段列表变化刷新索引/外键 |
  | protected void bindListeners() | 绑定监听 | tabPane 切换控制 action1/action2 显隐与预览；字段选中驱动 `tableColumnExtraController.init`；索引/外键列表变化触发初始化；`initTable()` |
  | private void initIndexTable() | 初始化索引表 | 将字段列表注入各 `DamengIndexControl` |
  | private void initForeignKeyTable() | 初始化外键表 | 注入字段列表并设置 schema、dbClient |
  | private void initPreview() | SQL 预览 | 新数据 `DamengTableCreateSqlGenerator`，否则 `DamengTableAlertSqlGenerator` |
  | public void init(DamengTable table, ShellDamengSchemaTreeItem dbItem) throws Exception | 初始化 | 保存参数、置 `newData`、设置 schema，`StageManager.showMask(this::doInit)` |
  | private void doInit() | 执行初始化 | `initDBListener`、`tableSpace.init`、`initInfo`，无检查特性时移除 checkTab |
  | private void initDBListener() | 初始化监听器 | 单例 listener 绑定五表及 tableSpace/tableComment |
  | private void doAdd() | 新增（@FXML） | 按当前选中 tab 分派 addColumn/addIndex/addForeignKey/addTrigger/addCheck |
  | private void doDelete() | 删除（@FXML） | 按当前选中 tab 分派 deleteXxx |
  | private void doMoveUp() | 上移（@FXML） | 按选中 tab 对对应表 `TableViewUtil.moveUp` |
  | private void doMoveDown() | 下移（@FXML） | 按选中 tab 对对应表 `TableViewUtil.moveDown` |
  | public String tableName() | 表名 | `table.getName()` |
  | public String schema() | 模式名 | `table.getSchema()` |
  | public List<? extends SubTabController> getSubControllers() | 子控制器 | 返回 `List.of(tableColumnExtraController)` |
  | public ShellDamengSchemaTreeItem getDbItem() | 库树节点 | 返回 `dbItem` |
  | public boolean isUnsaved() | 是否未保存 | 返回 `unsaved` |
- 调用链：`init → doInit → initInfo → initNormal → dbItem.columns/indexes/foreignKeys/triggers/checks → initPreview → Generator.generateSqlSingle`

## ShellDamengTableRecordTab
> 文件: cn/oyzh/easyshell/tabs/dameng/table/ShellDamengTableRecordTab.java
- 职责：达梦表数据记录标签页，用于查看与维护表数据（含过滤、分页）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） |  |  |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | protected String url() | FXML 路径 | `FXConst.TAB_PATH + "dameng/table/shellDamengTableRecordTab.fxml"` |
  | public void flushGraphic() | 刷新图标 | 无图标时创建 `TableSVGGlyph` |
  | public void flushTitle() | 刷新标题 | 格式 `tableName@schema(infoName)` |
  | public boolean init(ShellDamengTableTreeItem item) | 初始化 | 委托 `controller().init(item)` 后 `flush()`，返回 true |
  | public ShellDamengTableRecordTabController controller() | 获取控制器 | 强转 `super.controller()` |
  | public void reload() | 重新加载 | `controller().reload()` |
  | public ShellDamengClient client() | 客户端 | `item().client()` |
  | public void setFilters(List<DamengRecordFilter> filters) | 设置过滤 | `controller().setFilters(filters)` |
  | public ShellDamengTableTreeItem item() | 树节点 | `controller().getItem()` |
  | public String tableName() | 表名 | `item().tableName()` |
  | public ShellDamengSchemaTreeItem dbItem() | 库树节点 | `item().dbItem()` |
- 调用链：`init → Controller.init → reload → doReload → recordPage`

## ShellDamengTableRecordTabController
> 文件: cn/oyzh/easyshell/tabs/dameng/table/ShellDamengTableRecordTabController.java
- 职责：表记录内容控制器，负责记录分页加载、列渲染、增删改、过滤与导入导出。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | 根节点（FXML 注入） |
  | itemProperty | ObjectProperty<ShellDamengTableTreeItem> | 表树节点属性 |
  | pageData | Paging<DamengRecord> | 分页数据 |
  | filter | SVGGlyph | 记录过滤按钮（FXML 注入） |
  | missPrimaryKey | SVGGlyph | 缺少主键警告（FXML 注入） |
  | pageBox | PageBox<DamengRecord> | 分页组件（FXML 注入） |
  | recordTable | DamengRecordTableView | 数据表单（FXML 注入） |
  | filters | List<DamengRecordFilter> | 过滤列表 |
  | apply | SVGGlyph | 应用按钮（FXML 注入） |
  | discard | SVGGlyph | 抛弃按钮（FXML 注入） |
  | changeListener | DBStatusListener | 记录变更监听器 |
  | columns | DamengColumns | 字段列表 |
  | setting | ShellSetting | 全局设置（ShellSettingStore.SETTING） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | public void init(ShellDamengTableTreeItem item) | 初始化 | 建立 itemProperty 及父节点监听（为 null 时 `closeTab`），`reload()`，创建 changeListener（变更时 `apply.enable()`） |
  | public ShellDamengTableTreeItem getItem() | 获取树节点 | `itemProperty.get()` |
  | private void initDataList(long pageNo) | 加载数据列表 | `getItem().recordPage(...)` 后 `pageBox.setPaging` 与 `initRecords` |
  | private void initDataListByMask(long pageNo) | 带遮罩加载 | `StageManager.showMask(this::initDataList)` |
  | private List<DamengRecordFilter> enabledFilters() | 启用过滤 | 过滤 `isEnabled` 的过滤器，否则返回 null |
  | private void initCount(long count) | 更新计数 | 以 recordTable.itemList 重建 `Paging` 并设置到 pageBox |
  | private void initColumns(DamengColumns columns) | 初始化列 | 组合状态列与 `DamengRecordColumn`（设合适宽度）后 `recordTable.setColumn` |
  | private void initRecords(List<DamengRecord> records) | 填充记录 | `recordTable.setItem(records)` |
  | private void addRecord() | 新增记录（@FXML） | 新建 `DamengRecord`，按默认值填列，加入表并选中，计数 +1 |
  | private void insertRecord(DamengRecord record) | 插入记录 | 有主键走 `insertRecord(data, pk)` 并回显，否则 `insertRecord(data)` |
  | private void updateRecord(DamengRecord record) | 更新记录 | 有主键按主键更新（主键未变则移除主键数据）并回显，否则按变更数据+原始数据更新 |
  | private DamengRecordPrimaryKey initPrimaryKey(DamengRecord record) | 组装主键 | `getItem().getPrimaryKey()` 存在时初始化主键对象 |
  | private void apply() | 应用变更（@FXML） | 遍历记录，新建走 insert、变更走 update，完成后 `apply.disable()` |
  | private void discard() | 丢弃变更（@FXML） | 撤销变更记录、移除新建记录，计数 -1 |
  | public void reload() | 刷新（@FXML） | `StageManager.showMask(this::doReload)` |
  | private void doReload() | 刷新业务 | 未保存时确认；`initColumns`、`initDataList(0)`、主键缺失提示、过滤激活、禁用 apply |
  | private void filter() | 过滤（@FXML） | 打开 `ShellDamengRecordFilterPopupController`，提交后设置过滤并 `reload()` |
  | private void nextPage()/prevPage()/lastPage()/firstPage() | 翻页（@FXML） | 委托 `pageData` 对应方法与 `initDataListByMask` |
  | private void pageJump(PageEvent.PageJumpEvent event) | 跳页（@FXML） | `initDataListByMask(event.getPage())` |
  | private void pageSetting() | 页码设置（@FXML） | 打开 `ShellDBPageSettingPopupController`，limit 变化时回首页 |
  | private void deleteRecord() | 删除记录（@FXML） | 取选中记录，确认后 `StageManager.showMask(this::deleteRecords)` |
  | private void deleteRecords(List<DamengRecord> records) | 批量删除 | 逐条 `deleteRecord`，成功则移除并更新计数，失败提示 |
  | private boolean deleteRecord(DamengRecord record) | 单条删除 | 新建直接成功；否则按主键或原始数据删除，成功后 `record.destroy()` |
  | public void onTabClosed(Event event) | 关闭标签（@Override） | `DBStatusListenerManager.removeListener(changeListener)` |
  | protected void bindListeners() | 绑定监听 | 绑定 discard/apply 状态与 action2 启停；监听表数据新增启用 apply；选中行设可编辑；Ctrl+S 触发 apply |
  | public List<DamengRecordFilter> getFilters() / setFilters(...) | 过滤读写 | getter/setter |
  | private void importData() | 导入数据（@FXML） | `ShellDamengViewFactory.importData(client, schema)` |
  | private void exportData() | 导出数据（@FXML） | `ShellDamengViewFactory.exportData(client, schema, tableName)` |
- 调用链：`init → reload → doReload → initColumns/initDataList → ShellDamengTableTreeItem.recordPage → initRecords`；`apply → insertRecord/updateRecord → ShellDamengTableTreeItem.insertRecord/updateRecord`

## ShellDamengViewDesignTab
> 文件: cn/oyzh/easyshell/tabs/dameng/view/ShellDamengViewDesignTab.java
- 职责：达梦视图设计标签页，负责视图定义展示与设计维护。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） |  |  |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | protected String url() | FXML 路径 | `FXConst.TAB_PATH + "dameng/view/shellDamengViewDesignTab.fxml"` |
  | public void flushGraphic() | 刷新图标 | 无图标时创建 `ViewSVGGlyph` |
  | public void flushTitle() | 刷新标题 | 取 `viewName()`（空则 `I18nHelper.unnamedView()`），未保存加 `*`，格式 `schema-name` |
  | public String viewName() | 视图名 | `controller().viewName()` |
  | public ShellDamengSchemaTreeItem dbItem() | 库树节点 | `controller().getDbItem()` |
  | public void init(DamengView view, ShellDamengSchemaTreeItem item) | 初始化 | 委托 `controller().init(...)` 后 `flush()` |
  | public ShellDamengViewDesignTabController controller() | 获取控制器 | 强转 `super.controller()` |
  | public boolean isUnsaved() | 是否未保存 | `controller().isUnsaved()` |
  | protected void onTabCloseRequest(Event event) | 关闭请求 | 未保存时确认，否则 `closeTab()` |
- 调用链：`init → Controller.init → doInit → initInfo`

## ShellDamengViewDesignTabController
> 文件: cn/oyzh/easyshell/tabs/dameng/view/ShellDamengViewDesignTabController.java
- 职责：视图设计内容控制器，管理只读模式、注释、安全性、定义，并生成创建/修改视图 SQL 预览。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | view | DamengView | 当前视图对象 |
  | dbItem | ShellDamengSchemaTreeItem | 数据库树节点 |
  | readonly | FXCheckBox | 只读模式（FXML 注入） |
  | comment | FXTextArea | 注释（FXML 注入） |
  | securityType | ShellDamengSecurityTypeComboBox | 安全性（FXML 注入） |
  | definition | Editor | 视图定义（FXML 注入） |
  | preview | Editor | SQL 预览（FXML 注入） |
  | tabPane | FXTabPane | 切换面板（FXML 注入） |
  | listener | DBStatusListener | 数据变更监听器 |
  | unsaved | boolean | 未保存标志 |
  | newData | boolean | 新数据标志 |
  | initiating | boolean | 初始化中标志 |
  | viewName | String | 视图名称（保存时缓存） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | protected void initInfo() | 加载信息 | 新数据隐藏 action3；否则 `dbItem.selectView` 重查并填充注释/定义/只读/安全性，显示 action3；结束置 `initiating=false` |
  | public void init(DamengView view, ShellDamengSchemaTreeItem dbItem) | 初始化 | 保存参数、置 `newData`，`StageManager.showMask(this::doInit)` |
  | private void doInit() | 执行初始化 | `initDBListener()` 后 `FXUtil.runWait(this::initInfo)`，再绑定各组件监听 |
  | private void initDBListener() | 初始化监听器 | 解绑旧 listener，新建 `schema:name` listener，绑定 comment/readonly/definition/securityType |
  | private void initChangedFlag() | 置未保存 | 非初始化中时 `unsaved=true` 并 `flushTab()` |
  | private void refresh() | 刷新（@FXML） | 确认后 `init` 并 `flushTab()` |
  | private void save() | 保存（@FXML） | `StageManager.showMask(this::doSave)` |
  | private void doSave() | 执行保存 | 组装临时对象；新数据提示视图名后 `dbItem.createView` 并加入树、重建监听；否则 `dbItem.alertView` 并 `ShellDamengEventUtil.viewAlerted`；复位后 `initInfo/initPreview` |
  | private DamengView tempData() | 组装临时数据 | 从表单读取注释/可更新/定义/安全性 |
  | protected void bindListeners() | 绑定监听 | Ctrl+S 保存绑定；tabPane 切到索引 2 时 `initPreview()` |
  | public String schema() | 模式名 | `dbItem.schema()` |
  | public String viewName() | 视图名 | `view.getName()` |
  | public ShellDamengSchemaTreeItem getDbItem() | 库树节点 | 返回 `dbItem` |
  | public boolean isUnsaved() | 是否未保存 | 返回 `unsaved` |
  | private void initPreview() | SQL 预览 | 新数据 `DamengViewCreateSqlGenerator`，否则 `DamengViewAlertSqlGenerator` |
- 调用链：`init → doInit → initDBListener/initInfo → tempData → Generator.generateSqlSingle → preview`

## ShellDamengViewRecordTab
> 文件: cn/oyzh/easyshell/tabs/dameng/view/ShellDamengViewRecordTab.java
- 职责：达梦视图记录标签页，用于查看与维护视图数据。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | openedTime | long | 标签打开时间（`System.currentTimeMillis()`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | protected String url() | FXML 路径 | `FXConst.TAB_PATH + "dameng/view/shellDamengViewRecordTab.fxml"` |
  | public void flushGraphic() | 刷新图标 | 无图标时创建 `ViewSVGGlyph` |
  | public void flushTitle() | 刷新标题 | 格式 `viewName@schema(infoName)` |
  | public boolean init(ShellDamengViewTreeItem item) | 初始化 | 委托 `controller().init(item)` 后 `flush()`，返回 true |
  | public ShellDamengViewRecordTabController controller() | 获取控制器 | 强转 `super.controller()` |
  | public void reload() | 重新加载 | `controller().reload()` |
  | public ShellDamengViewTreeItem item() | 树节点 | `controller().getItem()` |
  | public ShellDamengClient client() | 客户端 | `item().client()` |
  | public String viewName() | 视图名 | `item().viewName()` |
  | public ShellDamengSchemaTreeItem dbItem() | 库树节点 | `item().dbItem()` |
  | public void setFilters(List<DamengRecordFilter> filters) | 设置过滤 | `controller().setFilters(filters)` |
- 调用链：`init → Controller.init → reload → doReload → recordPage`

## ShellDamengViewRecordTabController
> 文件: cn/oyzh/easyshell/tabs/dameng/view/ShellDamengViewRecordTabController.java
- 职责：视图记录内容控制器，功能与表记录控制器一致，另按视图可更新性控制编辑能力。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | 根节点（FXML 注入） |
  | itemProperty | ObjectProperty<ShellDamengViewTreeItem> | 视图树节点属性 |
  | pageData | Paging<DamengRecord> | 分页数据 |
  | filter | SVGGlyph | 记录过滤按钮（FXML 注入） |
  | missPrimaryKey | SVGGlyph | 缺少主键警告（FXML 注入） |
  | pageBox | PageBox<DamengRecord> | 分页组件（FXML 注入） |
  | recordTable | DamengRecordTableView | 数据表单（FXML 注入） |
  | filters | List<DamengRecordFilter> | 过滤列表 |
  | apply | SVGGlyph | 应用按钮（FXML 注入） |
  | discard | SVGGlyph | 抛弃按钮（FXML 注入） |
  | changeListener | DBStatusListener | 记录变更监听器 |
  | columns | DamengColumns | 字段列表 |
  | setting | ShellSetting | 全局设置（ShellSettingStore.SETTING） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | public void init(ShellDamengViewTreeItem item) | 初始化 | 建立 itemProperty 及父节点监听（null 时 `closeTab`），`reload()`；视图可更新时创建 changeListener 并显示 apply/action2 |
  | public ShellDamengViewTreeItem getItem() | 获取树节点 | `itemProperty.get()` |
  | private void initDataList(long pageNo) | 加载数据列表 | `getItem().recordPage(...)` 后 `pageBox.setPaging` 与 `initRecords` |
  | private void initDataListByMask(long pageNo) | 带遮罩加载 | `StageManager.showMask(this::initDataList)` |
  | private List<DamengRecordFilter> enabledFilters() | 启用过滤 | 过滤 `isEnabled` 的过滤器，否则返回 null |
  | private void initCount(long count) | 更新计数 | 重建 `Paging` 并设置到 pageBox |
  | private void initColumns(DamengColumns columns) | 初始化列 | 组合状态列与 `DamengRecordColumn`（设合适宽度）后 `recordTable.setColumn` |
  | private void initRecords(List<DamengRecord> records) | 填充记录 | `recordTable.setItem(records)` |
  | private void addRecord() | 新增记录（@FXML） | 新建 `DamengRecord`，按默认值填列，加入表并选中，计数 +1 |
  | private void insertRecord(DamengRecord record) | 插入记录 | 有主键走 `insertRecord(data, pk)` 并回显，否则 `insertRecord(data)` |
  | private void updateRecord(DamengRecord record) | 更新记录 | 有主键按主键更新（主键未变则移除主键数据）并回显，否则按变更数据+原始数据更新 |
  | private DamengRecordPrimaryKey initPrimaryKey(DamengRecord record) | 组装主键 | `getItem().getPrimaryKey()` 存在时初始化主键对象 |
  | private void apply() | 应用变更（@FXML） | 遍历记录，新建走 insert、变更走 update，完成后 `apply.disable()` |
  | private void discard() | 丢弃变更（@FXML） | 撤销变更记录、移除新建记录，计数 -1 |
  | public void reload() | 刷新（@FXML） | `StageManager.showMask(this::doReload)` |
  | private void doReload() | 刷新业务 | 未保存时确认；`initColumns`、`initDataList(0)`、主键缺失提示、过滤激活、禁用 apply |
  | private void filter() | 过滤（@FXML） | 打开 `ShellDamengRecordFilterPopupController`，提交后设置过滤并 `reload()` |
  | private void nextPage()/prevPage()/lastPage()/firstPage() | 翻页（@FXML） | 委托 `pageData` 对应方法与 `initDataListByMask` |
  | private void pageJump(PageEvent.PageJumpEvent event) | 跳页（@FXML） | `initDataListByMask(event.getPage())` |
  | private void pageSetting() | 页码设置（@FXML） | 打开 `ShellDBPageSettingPopupController`，limit 变化时回首页 |
  | private void deleteRecord() | 删除记录（@FXML） | 取选中记录，确认后 `StageManager.showMask(this::deleteRecords)` |
  | private void deleteRecords(List<DamengRecord> records) | 批量删除 | 逐条 `deleteRecord`，成功则移除并更新计数，失败提示 |
  | private boolean deleteRecord(DamengRecord record) | 单条删除 | 新建直接成功；否则按主键或原始数据删除，成功后 `record.destroy()` |
  | public void onTabClosed(Event event) | 关闭标签（@Override） | `DBStatusListenerManager.removeListener(changeListener)` |
  | protected void bindListeners() | 绑定监听 | 绑定 discard/apply 状态与 action2 启停；监听表数据新增启用 apply；选中行设可编辑；Ctrl+S 触发 apply |
  | public List<DamengRecordFilter> getFilters() / setFilters(...) | 过滤读写 | getter/setter |
- 调用链：`init → reload → doReload → initColumns/initDataList → ShellDamengViewTreeItem.recordPage → initRecords`；`apply → insertRecord/updateRecord → ShellDamengViewTreeItem.insertRecord/updateRecord`

## MySQL 标签页（主框架/查询/终端/事件）

## ShellMysqlBaseTab（tabs.mysql）
> 文件: cn/oyzh/easyshell/tabs/mysql/ShellMysqlBaseTab.java
- 职责：MySQL 操作标签页抽象基类，提供数据库树节点与库/连接名称的通用能力。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `abstract ShellMysqlDatabaseTreeItem dbItem()` | 子类实现 | 返回数据库树节点 |
  | `String dbName()` | 库名 | `dbItem()==null?null:dbItem().dbName()` |
  | `String connectName()` | 连接名 | `dbItem()==null?null:dbItem().connectName()` |
- 调用链：`dbName/connectName → dbItem()`

## ShellMysqlTab（tabs.mysql）
> 文件: cn/oyzh/easyshell/tabs/mysql/ShellMysqlTab.java
- 职责：MySQL 连接标签页，承载主界面与客户端。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getTabTitle()` | 标题 | `名称(类型大写)` |
  | `void flushGraphic()` | 图标 | `ShellOsTypeComboBox.getGlyph` |
  | `protected String url()` | FXML | `/tabs/mysql/shellMysqlTab.fxml` |
  | `protected ShellMysqlTabController controller()` | 控制器 | 强转 |
  | `void init(ShellConnect)` | 初始化 | controller.init + super.init |
  | `ShellBaseClient client()` | 客户端 | `controller().getClient()` |
  | `static ShellMysqlTab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellMysqlTabController.init`

## ShellMysqlTabController（tabs.mysql）
> 文件: cn/oyzh/easyshell/tabs/mysql/ShellMysqlTabController.java
- 职责：MySQL 标签页控制器，负责连接初始化、数据库树加载、过滤与数据导入导出。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | ShellMysqlClient | MySQL 客户端 |
  | root | FXSplitPane | FXML 注入，根分割面板 |
  | tabPane | ShellMysqlTabPane | FXML 注入，标签页容器 |
  | treeView | ShellMysqlTreeView | FXML 注入，数据库树 |
  | filterKW | FilterTextField | FXML 注入，过滤输入框 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellConnect shellConnect()` | 连接 | `client.getShellConnect()` |
  | `void init(ShellConnect)` | 初始化并连接 | newClient→状态监听→showMask：`client.start()`、失败 closeTab；成功 `tabPane.setClient`、`treeView.setClient`、`root().loadChild()/expend()`、`ShellMysqlQueryUtil.updateIndex`、`hideLeft` |
  | `ShellMysqlClient getClient()` | 客户端 | 返回 client |
  | `void onTabClosed(Event)` | 关闭 | `IOUtil.closeAsync(client)` |
  | `private void doFilter()` | 过滤 | 设置树高亮与过滤条件，`ThreadUtil.start(treeView.filter)` |
  | `@FXML importData()/exportData()/runSqlFile()/transportData()` | 数据操作 | `ShellMysqlViewFactory` 对应方法 |
  | `@FXML positionNode()` | 定位节点 | `treeView.positionItem()` |
  | `protected void bindListeners()` | 绑定 | Ctrl+F 聚焦过滤框；过滤文本/全字/大小写变化触发 `doFilter` |
- 调用链：`init → client.start → treeView.root().loadChild`；`filterKW → doFilter`

## ShellMysqlTabPane（tabs.mysql）
> 文件: cn/oyzh/easyshell/tabs/mysql/ShellMysqlTabPane.java
- 职责：MySQL 标签页容器，负责各类功能标签的打开/查找/关闭，并作为事件订阅中心。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | clientProperty | SimpleObjectProperty<ShellMysqlClient> | 客户端属性（懒初始化） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void setClient/getClient/clientProperty()` | 客户端访问器 | 读写 clientProperty |
  | `void initNode()` | 节点初始化 | super + `initHomeTab`；监听 tabs 增删延迟 `flushHomeTab` |
  | `private void flushHomeTab()` | 主页 tab 维护 | 空则建，>1 则关 |
  | `ShellMysqlHomeTab getHomeTab()` / `initHomeTab()` / `closeHomeTab()` | 主页 tab | 增删主页 |
  | `private getEventDesignTab/getFunctionDesignTab/getProcedureDesignTab/getTableRecordTab/getTableDesignTab/getViewRecordTab/getViewDesignTab/getMysqlQueryMainTab/getTerminalTab(...)` | 查找已开 tab | 遍历按类型+dbItem+名称匹配 |
  | `private List<ShellMysqlBaseTab> getBaseTabs(dbItem)` | 收集 tab | 同 dbItem 的基类 tab |
  | `@EventSubscribe onTableOpen/Renamed/Cleared/Truncated/Dropped/Alerted(...)` | 表事件 | 打开/重载/关闭表相关 tab |
  | `@EventSubscribe onViewOpen/viewAlerted/onViewRenamed/onViewDropped(...)` | 视图事件 | 打开/重载/关闭视图 tab |
  | `@EventSubscribe onQueryAdd/Deleted/Open/Renamed(...)` | 查询事件 | 新增/移除/打开/关闭查询 tab |
  | `@EventSubscribe onEventDesign/Renamed/Dropped(...)` | 事件事件 | 创建/关闭事件设计 tab |
  | `@EventSubscribe onFunctionDesign/Renamed/Dropped(...)` | 函数事件 | 创建/关闭函数 tab |
  | `@EventSubscribe onProcedureDesign/Renamed/Dropped(...)` | 存储过程事件 | 创建/关闭过程 tab |
  | `@EventSubscribe onViewDesign/onTableDesign(...)` | 设计事件 | 创建设计 tab |
  | `@EventSubscribe onDatabaseClosed/onDatabaseDropped(...)` | 库事件 | `removeTab(getBaseTabs(...))` |
  | `@EventSubscribe onTerminalOpen(...)` | 终端事件 | 创建/选中终端 tab |
- 调用链：`事件 → onXxx → getXxxTab（复用）→ addTab/select/removeTab`

## ShellMysqlQueryExplainTab（tabs.mysql.query）
> 文件: cn/oyzh/easyshell/tabs/mysql/query/ShellMysqlQueryExplainTab.java
- 职责：MySQL 查询执行计划标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/query/shellMysqlQueryExplainTab.fxml` |
  | `void init(String, ShellMysqlExplainResult)` | 初始化 | setTitle + controller.init |
  | `ShellMysqlQueryExplainTabController controller()` | 控制器 | 强转 |
  | `void initNode()` | 节点初始化 | `setClosable(false)` |
  | `static ... of(String, ShellMysqlExplainResult)` | 工厂 | new+init |
- 调用链：`of → init → ShellMysqlQueryExplainTabController.init`

## ShellMysqlQueryExplainTabController（tabs.mysql.query）
> 文件: cn/oyzh/easyshell/tabs/mysql/query/ShellMysqlQueryExplainTabController.java
- 职责：MySQL 解释结果控制器，展示执行计划的列/记录与 SQL/耗时/计数。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | sql / used / count | FXText | FXML 注入，SQL/耗时/计数 |
  | recordTable | ShellMysqlRecordTableView | FXML 注入，结果表格 |
  | result | ShellMysqlExplainResult | 执行结果 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(ShellMysqlExplainResult)` | 初始化 | 存结果 + `initDataList` |
  | `private void initDataList()` | 初始化列表 | initColumns/initRecords/sql/used/count |
  | `private void initColumns(List<MysqlColumn>)` | 列 | 首列 `DBStatusColumn`，其余 `ShellMysqlRecordColumn(column,false)` |
  | `private void initRecords(List<MysqlRecord>)` | 记录 | `recordTable.setItem` |
- 调用链：`init → initDataList → initColumns/initRecords`

## ShellMysqlQueryInfoTab（tabs.mysql.query）
> 文件: cn/oyzh/easyshell/tabs/mysql/query/ShellMysqlQueryInfoTab.java
- 职责：MySQL 查询信息标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/query/shellMysqlQueryInfoTab.fxml` |
  | `void init(DBQueryResults<?>)` | 初始化 | controller.init |
  | `ShellMysqlQueryInfoTabController controller()` | 控制器 | 强转 |
  | `void initNode()` | 节点初始化 | `setClosable(false)` |
  | `static ShellMysqlQueryInfoTab of(DBQueryResults<?>)` | 工厂 | new+init |
- 调用链：`of → init → ShellMysqlQueryInfoTabController.init`

## ShellMysqlQueryInfoTabController（tabs.mysql.query）
> 文件: cn/oyzh/easyshell/tabs/mysql/query/ShellMysqlQueryInfoTabController.java
- 职责：MySQL 查询信息控制器，将执行结果信息输出到信息区。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | infoArea | Editor | FXML 注入，信息编辑区 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(DBQueryResults<?>)` | 初始化 | 清空后逐条输出内容 +（Affected rows/OK 或错误消息）+ 耗时行 |
- 调用链：`init → infoArea.clear/appendLine`

## ShellMysqlQueryMainTab（tabs.mysql.query）
> 文件: cn/oyzh/easyshell/tabs/mysql/query/ShellMysqlQueryMainTab.java
- 职责：MySQL 查询主标签页，负责 SQL 编辑与执行。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/query/shellMysqlQueryMainTab.fxml` |
  | `void flushGraphic()` | 图标 | `QuerySVGGlyph` |
  | `void flushTitle()` | 标题 | 未保存加 `* `；`名称@库(连接)` |
  | `ShellQuery query()` / `String queryId()` | 查询 | controller.getQuery / uid |
  | `ShellMysqlDatabaseTreeItem dbItem()` | 树节点 | controller.getDbItem |
  | `boolean init(ShellQuery, ShellMysqlDatabaseTreeItem)` | 初始化 | controller.init + flush，返回 true |
  | `ShellMysqlQueryMainTabController controller()` | 控制器 | 强转 |
  | `boolean isUnsaved()` | 未保存 | controller.isUnsaved |
  | `protected void onTabCloseRequest(Event)` | 关闭请求 | 未保存确认 |
  | `static ShellMysqlQueryMainTab of(...)` | 工厂 | new+init |
- 调用链：`of → init → ShellMysqlQueryMainTabController.init`

## ShellMysqlQueryMainTabController（tabs.mysql.query）
> 文件: cn/oyzh/easyshell/tabs/mysql/query/ShellMysqlQueryMainTabController.java
- 职责：MySQL 查询主内容控制器，管理 SQL 编辑器、运行/解释/保存与结果标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | query / dbItem | ShellQuery / ShellMysqlDatabaseTreeItem | 查询对象/数据库树节点 |
  | unsaved | boolean | 未保存标志 |
  | queryArea | ShellMysqlQueryEditor | FXML 注入，查询编辑区 |
  | resultTabPane | FXTabPane | FXML 注入，结果标签容器 |
  | root | FXVBox | FXML 注入，根节点 |
  | infoTab | ShellMysqlQueryInfoTab | FXML 注入，信息 tab |
  | splitPane | FXSplitPane | FXML 注入，分割面板 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellQuery getQuery()` / `ShellMysqlDatabaseTreeItem getDbItem()` | 访问器 | 返回字段 |
  | `void init(ShellQuery, ShellMysqlDatabaseTreeItem)` | 初始化 | 设 query/dbItem、showNode(0)、编辑器设内容/方言、文本变化置 unsaved |
  | `protected void bindListeners()` | 绑定 | 结果 tab 刷新、选中切换 showNode、编辑器运行回调 run |
  | `private void clearTabs()` | 清理 tab | 移除除 infoTab 外 |
  | `@FXML pretty()` | 美化 | `queryArea.pretty()` |
  | `@FXML run()` | 运行 | 取选中/全部 SQL，mask 内 `doRun` |
  | `private void doRun(String)` | 执行 | `dbItem.executeSql`、清 tab、建 infoTab 与 selectTab（`initSelectTab`）、选择显示 |
  | `@FXML explain()` | 解释 | `dbItem.explainSql`、建 infoTab 与 explainTab（`initExplainTab`）、选择显示 |
  | `private void initInfoTab(DBQueryResults<?>)` | 信息 tab | `infoTab.init` |
  | `private ShellMysqlQuerySelectTab initSelectTab(ShellMysqlExecuteResult,String)` | 建结果 tab | new+init+id/prop |
  | `private ShellMysqlQueryExplainTab initExplainTab(ShellMysqlExplainResult,String)` | 建解释 tab | new+init+id/prop |
  | `@FXML save()` | 保存 | 补名称、写内容/dbName/iid，新增 insert+树追加，否则 update |
  | `@FXML queryKeyPressed(KeyEvent)` | 快捷键 | Ctrl+S 保存、Ctrl+R 运行 |
  | `private void showNode(int)` | 布局 | 0 隐藏结果面板，1/2 显示并设分隔 |
  | `boolean isUnsaved()` | 未保存 | 返回 unsaved |
- 调用链：`run → doRun → dbItem.executeSql → initSelectTab`；`explain → dbItem.explainSql → initExplainTab`

## ShellMysqlQuerySelectTab（tabs.mysql.query）
> 文件: cn/oyzh/easyshell/tabs/mysql/query/ShellMysqlQuerySelectTab.java
- 职责：MySQL 查询结果标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/query/shellMysqlQuerySelectTab.fxml` |
  | `void init(String, ShellMysqlExecuteResult, ShellMysqlDatabaseTreeItem)` | 初始化 | setTitle + controller.init |
  | `ShellMysqlQuerySelectTabController controller()` | 控制器 | 强转 |
  | `void initNode()` | 节点初始化 | `setClosable(false)` |
  | `static ShellMysqlQuerySelectTab of(...)` | 工厂 | new+init |
- 调用链：`of → init → ShellMysqlQuerySelectTabController.init`

## ShellMysqlQuerySelectTabController（tabs.mysql.query）
> 文件: cn/oyzh/easyshell/tabs/mysql/query/ShellMysqlQuerySelectTabController.java
- 职责：MySQL 查询结果控制器，负责结果展示与记录的增/改/删、应用/丢弃、导出、刷新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | FXML 注入，根节点 |
  | sql / used / count | FXText | FXML 注入，SQL/耗时/计数 |
  | recordTable | ShellMysqlRecordTableView | FXML 注入，记录表格 |
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库树节点 |
  | result | ShellMysqlExecuteResult | 执行结果 |
  | add / delete / apply / discard | SVGGlyph | FXML 注入，增/删/应用/丢弃按钮 |
  | changeListener | DBStatusListener | 记录变更监听器 |
  | columns | List<MysqlColumn> | 字段列表 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(ShellMysqlExecuteResult, ShellMysqlDatabaseTreeItem)` | 初始化 | 可更新则建 `DBStatusListener(dbName:tableName)` 并显示增/删/应用/丢弃，`initDataList` |
  | `private void initDataList()` | 列表 | initColumns/initRecords/sql/used/initCount |
  | `private void initCount(int)` | 计数 | 设 count |
  | `private void initColumns(List<MysqlColumn>)` | 列 | 首列 `DBStatusColumn`，其余 `ShellMysqlRecordColumn(column,false)` |
  | `private void initRecords(List<MysqlRecord>)` | 记录 | setItem |
  | `@FXML addRecord()` | 新增行 | 建 created `MysqlRecord`，默认值填充，加行选中 |
  | `private void insertRecord(MysqlRecord)` | 插入 | `MysqlInsertRecordParam` → `dbItem.client().insertRecord`，有主键回显 |
  | `private void updateRecord(MysqlRecord)` | 更新 | 有主键按主键更新并回显；否则按所有字段更新 |
  | `private MysqlRecordPrimaryKey initPrimaryKey(MysqlRecord)` | 主键 | 依 `result.getPrimaryKey()` |
  | `@FXML apply()` | 应变更 | created→insert、changed→update，清状态、禁用 apply |
  | `@FXML discard()` | 丢弃 | 还原/移除、禁用 apply、更新计数 |
  | `@FXML reload()` | 刷新 | 未保存先确认，`dbItem.executeSingleSql` 重查 |
  | `@FXML exportRecord()` | 导出 | 取 query，构造 `ShellMysqlDataExportTable`，`ShellMysqlViewFactory.exportData` |
  | `@FXML deleteRecord()` | 删除 | 确认后 mask 内批量删除 |
  | `private void deleteRecords(List<MysqlRecord>)` | 批量删除 | 逐条删除，成功移除更新计数 |
  | `private boolean deleteRecord(MysqlRecord)` | 单条删除 | created 直接成功；否则 `MysqlDeleteRecordParam` → `dbItem.deleteRecord` |
  | `void onTabClosed(Event)` | 关闭 | 移除 `DBStatusListenerManager` 监听 |
  | `protected void bindListeners()` | 绑定 | 选中可编辑、discard 绑定 apply、apply 切换按钮组、行变化启用 apply、Ctrl+S 应用 |
- 调用链：`apply → insertRecord/updateRecord → dbItem.client()/dbItem.deleteRecord`

## ShellMysqlTerminalTab（tabs.mysql.terminal）
> 文件: cn/oyzh/easyshell/tabs/mysql/terminal/ShellMysqlTerminalTab.java
- 职责：MySQL 终端标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/terminal/shellMysqlTerminalTab.fxml` |
  | `void flushGraphic()` | 图标 | `TerminalSVGGlyph` |
  | `void flushTitle()` | 标题 | `库名(连接名)` |
  | `void init(ShellMysqlDatabaseTreeItem)` | 初始化 | controller.init + flush |
  | `ShellMysqlTerminalTabController controller()` | 控制器 | 强转 |
  | `ShellMysqlClient client()` | 客户端 | controller.client |
  | `ShellMysqlDatabaseTreeItem dbItem()` | 树节点 | controller.getDbItem |
- 调用链：`init → ShellMysqlTerminalTabController.init`

## ShellMysqlTerminalTabController（tabs.mysql.terminal）
> 文件: cn/oyzh/easyshell/tabs/mysql/terminal/ShellMysqlTerminalTabController.java
- 职责：MySQL 命令行标签页控制器，托管命令行面板。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | terminal | MysqlTerminalPane | FXML 注入，命令行文本域 |
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(ShellMysqlDatabaseTreeItem)` | 初始化 | 保存 dbItem，`terminal.init(dbItem.client(), dbItem.dbName())` |
  | `ShellMysqlDatabaseTreeItem getDbItem()` | 树节点 | 返回 dbItem |
  | `String getDbName()` | 库名 | `dbItem.dbName()` |
  | `protected ShellConnect shellConnect()` | 连接 | `terminal.shellConnect()` |
  | `ShellMysqlClient client()` | 客户端 | `terminal.getClient()` |
  | `void destroy()` | 销毁 | 临时终端则 `client().close()` |
- 调用链：`init → terminal.init`；`destroy → client().close`

## ShellMysqlEventDesignTab（tabs.mysql.event）
> 文件: cn/oyzh/easyshell/tabs/mysql/event/ShellMysqlEventDesignTab.java
- 职责：MySQL 事件设计标签页，负责事件信息展示与设计维护。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/event/shellMysqlEventDesignTab.fxml` |
  | `void flushGraphic()` | 图标 | `EventSVGGlyph` |
  | `void flushTitle()` | 标题 | 未保存加 `* `；`事件名@库(连接)` |
  | `MysqlEvent event()` / `String eventName()` | 事件 | controller.getEvent / name |
  | `ShellMysqlDatabaseTreeItem dbItem()` | 树节点 | controller.getDbItem |
  | `void init(MysqlEvent, ShellMysqlDatabaseTreeItem)` | 初始化 | controller.init + flush |
  | `ShellMysqlEventDesignTabController controller()` | 控制器 | 强转 |
  | `boolean isUnsaved()` | 未保存 | controller.isUnsaved |
  | `protected void onTabCloseRequest(Event)` | 关闭请求 | 未保存确认 |
- 调用链：`init → ShellMysqlEventDesignTabController.init`

## ShellMysqlEventDesignTabController（tabs.mysql.event）
> 文件: cn/oyzh/easyshell/tabs/mysql/event/ShellMysqlEventDesignTabController.java
- 职责：MySQL 事件设计内容控制器，管理计划类型（单次/周期）、时间、定义/状态/定义者等与保存/SQL 预览。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | event | MysqlEvent | 当前事件对象 |
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库树节点 |
  | definition | ShellMysqlQueryEditor | FXML 注入，定义编辑区 |
  | preview | Editor | FXML 注入，预览 |
  | planType | FXToggleGroup | FXML 注入，计划类型 |
  | onetimeType | FXRadioButton | FXML 注入，单次类型 |
  | onetime | DateTimeTextField | FXML 注入，单次执行时间 |
  | onetimeInterval | FXCheckBox / onetimeIntervalValue | 单次循环开关/值（FXML 注入） |
  | onetimeIntervalType | ShellMysqlEventIntervalTypeCombobox | FXML 注入，单次循环类型 |
  | loopType | FXRadioButton | FXML 注入，周期类型 |
  | loopIntervalValue | NumberTextField / loopIntervalType | 周期循环值/类型（FXML 注入） |
  | loopStart / loopStartTime / loopStartInterval / loopStartIntervalValue / loopStartIntervalType | 各控件 | FXML 注入，周期开始相关 |
  | loopEnd / loopEndTime / loopEndInterval / loopEndIntervalValue / loopEndIntervalType | 各控件 | FXML 注入，周期结束相关 |
  | tabPane | FXTabPane | FXML 注入，切换面板 |
  | comment | FXTextArea / definer | FXTextField | FXML 注入，注释/定义者 |
  | status | ShellMysqlEventStatusCombobox | FXML 注入，状态 |
  | onCompletion | ShellMysqlEventOnCompletionCombobox | FXML 注入，完成时 |
  | listener | DBStatusListener | 数据变更监听器 |
  | unsaved / newData / initiating | boolean | 未保存/新数据/初始化中标志 |
  | eventName | String | 保存时事件名 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MysqlEvent getEvent()` / `ShellMysqlDatabaseTreeItem getDbItem()` | 访问器 | 返回字段 |
  | `void init(MysqlEvent, ShellMysqlDatabaseTreeItem)` | 初始化 | 存 dbItem/event、`newData=event.isNew()`、mask 内 `doInit` |
  | `private void doInit()` | 执行初始化 | `initDBListener` + `FXUtil.runWait(initInfo)` |
  | `private void initDBListener()` | 数据监听 | 解绑旧监听，重建 `DBStatusListener(dbName:name)` 绑定到状态/定义者/注释/定义/完成时及各时间控件 |
  | `private void initChangedFlag()` | 变更标志 | 非初始化中则置 unsaved + flushTab |
  | `protected void initInfo()` | 填充信息 | 清理旧设置；newData 设默认定义者；否则查询事件信息填充；按单次/周期类型填充时间字段；结束清 initiating |
  | `@FXML refresh()` | 刷新 | 确认后重新 init |
  | `@FXML save()` | 保存 | mask 内 `doSave` |
  | `private void doSave()` | 执行保存 | `tempData`；新增 `dbItem.createEvent` 并追加树节点，否则 `dbItem.alertEvent`；重置标志、刷新、`initPreview` |
  | `private MysqlEvent tempData()` | 临时数据 | 汇集名称/库/定义者/注释/定义/状态/完成时/类型/时间（单次或周期开始结束） |
  | `protected void bindListeners()` | 绑定 | 各开关级联 enable/disable；大量 Ctrl+S；切换面板第 3 页 `initPreview` |
  | `private void initPreview()` | 预览 | newData 用 `MysqlEventCreateSqlGenerator`，否则 `MysqlEventAlertSqlGenerator` |
  | `boolean isUnsaved()` / `setUnsaved(boolean)` | 未保存 | 读写 unsaved |
- 调用链：`save → doSave → tempData → dbItem.createEvent/alertEvent → initPreview`

## MySQL 标签页（首页/函数/存储过程/表/视图）

## ShellMysqlFunctionDesignTab（tabs.mysql.function）
> 文件: cn/oyzh/easyshell/tabs/mysql/function/ShellMysqlFunctionDesignTab.java
- 职责：MySQL 函数设计标签页，负责函数信息展示与设计维护。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/function/shellMysqlFunctionDesignTab.fxml` |
  | `void flushGraphic()` | 图标 | `FunctionSVGGlyph` |
  | `void flushTitle()` | 标题 | 未保存加 `* `；`函数名@库(连接)` |
  | `String functionName()` | 函数名 | controller.getFunction().getName() |
  | `ShellMysqlDatabaseTreeItem dbItem()` | 树节点 | controller.getDbItem() |
  | `void init(MysqlFunction, ShellMysqlDatabaseTreeItem)` | 初始化 | controller.init + flush |
  | `ShellMysqlFunctionDesignTabController controller()` | 控制器 | 强转 |
  | `boolean isUnsaved()` | 未保存 | controller.isUnsaved() |
  | `protected void onTabCloseRequest(Event)` | 关闭请求 | 未保存确认 |
- 调用链：`init → ShellMysqlFunctionDesignTabController.init`

## ShellMysqlFunctionDesignTabController（tabs.mysql.function）
> 文件: cn/oyzh/easyshell/tabs/mysql/function/ShellMysqlFunctionDesignTabController.java
- 职责：MySQL 函数设计内容控制器，管理定义/注释/定义者/安全性/特征/参数/返回值与保存/SQL 预览。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | function | MysqlFunction | 当前函数 |
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库树节点 |
  | definition / preview | Editor | FXML 注入，定义/预览 |
  | tabPane | FXTabPane | FXML 注入，切换面板 |
  | comment | FXTextArea / definer | FXTextField | FXML 注入，注释/定义者 |
  | securityType | ShellMysqlSecurityTypeComboBox | FXML 注入，安全性 |
  | characteristic | ShellMysqlCharacteristicCombobox | FXML 注入，特征 |
  | paramTable | DBStatusTableView<MysqlRoutineParam> | FXML 注入，参数表格 |
  | returnType | DBFiledTypeComboBox | FXML 注入，返回类型 |
  | returnValues | ShellDBEnumTextFiled | FXML 注入，返回值列表 |
  | returnDigits / returnSize | NumberTextField | FXML 注入，返回小数/长度 |
  | returnCharset | ShellMysqlCharsetComboBox | FXML 注入，返回字符集 |
  | listener | DBStatusListener | 数据变更监听器 |
  | unsaved / newData / initiating | boolean | 未保存/新数据/初始化中 |
  | functionName | String | 保存时函数名 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MysqlFunction getFunction()` | 函数 | 返回 function |
  | `void init(MysqlFunction, ShellMysqlDatabaseTreeItem)` | 初始化 | 存字段、`newData=isNew()`、mask 内 `doInit` |
  | `private void doInit()` | 执行初始化 | `returnCharset.init(client)`、`initDBListener`、`FXUtil.runWait(initInfo)` |
  | `private void initDBListener()` | 数据监听 | 重建 `DBStatusListener(dbName:name)` 绑定各控件与 paramTable |
  | `private void initChangedFlag()` | 变更标志 | 非初始化中置 unsaved + flushTab |
  | `protected void initInfo()` | 填充信息 | newData 设默认定义者与模板正文；否则查询函数填充；填充返回值；结束清 initiating |
  | `@FXML refresh()` / `@FXML save()` | 刷新/保存 | 保存 mask 内 `doSave` |
  | `private void doSave()` | 执行保存 | `tempData`；新增 `dbItem.createFunction` 追加树节点，否则 `dbItem.alertFunction`；刷新、重置表格、`initPreview` |
  | `private MysqlFunction tempData()` | 临时数据 | 汇集名称/库/参数/定义者/注释/定义/安全性/特征/返回值 |
  | `private void initPreview()` | 预览 | newData 用 `MysqlFunctionCreateSqlGenerator`，否则 `MysqlFunctionAlertSqlGenerator` |
  | `@FXML addParam()/deleteParam()/moveParamUp()/moveParamDown()` | 参数维护 | 增/删/上下移（`TableViewUtil`） |
  | `String dbName()` / `boolean isUnsaved()` / `setUnsaved` / `getDbItem` / `setDbItem` | 访问器 | 读写字段 |
  | `protected void bindListeners()` | 绑定 | 参数列表变化；大量 Ctrl+S；返回值类型变化启停 charset/size/digits/value；切换面板显示 param 组、预览页 `initPreview` |
  | `private void initParamTable()` | 参数表 | 为每个参数设 dbClient |
- 调用链：`save → doSave → tempData → dbItem.createFunction/alertFunction → initPreview`

## ShellMysqlHomeTab（tabs.mysql.home）
> 文件: cn/oyzh/easyshell/tabs/mysql/home/ShellMysqlHomeTab.java
- 职责：MySQL 主页标签页（展示库信息）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/home/shellMysqlHomeTab.fxml` |
  | `String getTabTitle()` | 标题 | `I18nHelper.info()` |
  | `void flushGraphic()` | 图标 | `MysqlSVGGlyph` |
  | `ShellMysqlHomeTabController controller()` | 控制器 | 强转 |
- 调用链：`getTabTitle/flushGraphic`

## ShellMysqlHomeTabController（tabs.mysql.home）
> 文件: cn/oyzh/easyshell/tabs/mysql/home/ShellMysqlHomeTabController.java
- 职责：MySQL 主页内容控制器，展示产品类型与版本信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | type | FXLabel | FXML 注入，产品类型 |
  | version | FXLabel | FXML 注入，版本 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onTabInit(FXTab)` | tab 初始化 | 监听 tabPane 属性，取到 `ShellMysqlTabPane` 后（或等待其 client 就绪）调用 `initInfo` |
  | `private void initInfo(ShellMysqlClient)` | 展示信息 | `client.selectProduct()` → type，`client.selectVersion()` → version |
- 调用链：`onTabInit → initInfo → client.selectProduct/selectVersion`

## ShellMysqlProcedureDesignTab（tabs.mysql.procedure）
> 文件: cn/oyzh/easyshell/tabs/mysql/procedure/ShellMysqlProcedureDesignTab.java
- 职责：MySQL 存储过程设计标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/procedure/shellMysqlProcedureDesignTab.fxml` |
  | `void flushGraphic()` | 图标 | `ProcedureSVGGlyph` |
  | `void flushTitle()` | 标题 | 未保存加 `* `；`过程名@库(连接)` |
  | `MysqlProcedure procedure()` / `String procedureName()` | 过程 | controller.getProcedure / name |
  | `ShellMysqlDatabaseTreeItem dbItem()` | 树节点 | controller.getDbItem |
  | `void init(MysqlProcedure, ShellMysqlDatabaseTreeItem)` | 初始化 | controller.init + flush |
  | `ShellMysqlProcedureDesignTabController controller()` | 控制器 | 强转 |
  | `boolean isUnsaved()` | 未保存 | controller.isUnsaved |
  | `protected void onTabCloseRequest(Event)` | 关闭请求 | 未保存确认 |
- 调用链：`init → ShellMysqlProcedureDesignTabController.init`

## ShellMysqlProcedureDesignTabController（tabs.mysql.procedure）
> 文件: cn/oyzh/easyshell/tabs/mysql/procedure/ShellMysqlProcedureDesignTabController.java
- 职责：MySQL 存储过程设计内容控制器，管理定义/注释/定义者/安全性/特征/参数与保存/SQL 预览。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | procedure | MysqlProcedure | 当前过程 |
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库树节点 |
  | definition / preview | Editor | FXML 注入，定义/预览 |
  | tabPane | FXTabPane | FXML 注入，切换面板 |
  | comment | FXTextArea / definer | FXTextField | FXML 注入，注释/定义者 |
  | securityType | ShellMysqlSecurityTypeComboBox | FXML 注入，安全性 |
  | characteristic | ShellMysqlCharacteristicCombobox | FXML 注入，特征 |
  | paramTable | DBStatusTableView<MysqlRoutineParam> | FXML 注入，参数表格 |
  | listener | DBStatusListener | 数据变更监听器 |
  | unsaved / newData / initiating | boolean | 未保存/新数据/初始化中 |
  | procedureName | String | 保存时过程名 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MysqlProcedure getProcedure()` | 过程 | 返回 procedure |
  | `void init(MysqlProcedure, ShellMysqlDatabaseTreeItem)` | 初始化 | 存字段、`newData=isNew()`、mask 内 `doInit` |
  | `private void doInit()` | 执行初始化 | `initDBListener` + `FXUtil.runWait(initInfo)` |
  | `private void initDBListener()` | 数据监听 | 重建 `DBStatusListener(dbName:name)` 绑定各控件与 paramTable |
  | `private void initChangedFlag()` | 变更标志 | 非初始化中置 unsaved + flushTab |
  | `protected void initInfo()` | 填充信息 | newData 设默认定义者与模板正文；否则查询过程填充；结束清 initiating |
  | `@FXML refresh()` / `@FXML save()` | 刷新/保存 | 保存 mask 内 `doSave` |
  | `private void doSave()` | 执行保存 | `tempData`；新增 `dbItem.createProcedure` 追加树节点，否则 `dbItem.alertProcedure`；刷新、重置表格、`initPreview` |
  | `private MysqlProcedure tempData()` | 临时数据 | 汇集名称/库/参数/定义者/注释/定义/安全性/特征 |
  | `private void initPreview()` | 预览 | newData 用 `MysqlProcedureCreateSqlGenerator`，否则 `MysqlProcedureAlertSqlGenerator` |
  | `@FXML addParam()/deleteParam()/moveParamUp()/moveParamDown()` | 参数维护 | 增/删/上下移 |
  | `ShellMysqlDatabaseTreeItem getDbItem()` / `setDbItem` / `String dbName()` / `boolean isUnsaved()` / `setUnsaved` | 访问器 | 读写字段 |
  | `protected void bindListeners()` | 绑定 | 参数列表变化；Ctrl+S；切换面板显示 param 组、预览页 `initPreview` |
  | `private void initParamTable()` | 参数表 | 为每个参数设 dbClient |
- 调用链：`save → doSave → tempData → dbItem.createProcedure/alertProcedure → initPreview`

## ShellMysqlTableColumnExtraController（tabs.mysql.table）
> 文件: cn/oyzh/easyshell/tabs/mysql/table/ShellMysqlTableColumnExtraController.java
- 职责：字段配置弹窗控制器，按当前字段类型动态展示默认值/值/主键长度/填充零/自动递增/无符号/字符集/排序/时间戳等额外配置并回写字段。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | defaultValueBox / defaultValue | FXHBox / ShellMysqlDefaultValueTextFiled | FXML 注入，默认值容器/控件 |
  | valueBox / value | FXHBox / ShellDBEnumTextFiled | FXML 注入，值容器/控件 |
  | primaryKeySizeBox / primaryKeySize | FXHBox / NumberTextField | FXML 注入，主键长度 |
  | zeroFillBox / zeroFill | FXHBox / FXCheckBox | FXML 注入，填充零 |
  | autoIncrementBox / autoIncrement | FXHBox / FXCheckBox | FXML 注入，自动递增 |
  | unsignedBox / unsigned | FXHBox / FXCheckBox | FXML 注入，无符号 |
  | currentTimestampBox / currentTimestamp | FXHBox / FXCheckBox | FXML 注入，按时间戳更新 |
  | charsetBox / charset | FXHBox / ShellMysqlCharsetComboBox | FXML 注入，字符集 |
  | collationBox / collation | FXHBox / ShellMysqlCollationComboBox | FXML 注入，排序 |
  | column | MysqlColumn | 当前字段 |
  | dbClient | ShellMysqlClient | 客户端 |
  | ignoreChanged | boolean | 忽略变更标志（初始化期间） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private void apply()` | 回写字段 | ignoreChanged 时跳过；按各 box 可见性把控件值写回 `column` |
  | `protected void bindListeners()` | 绑定 | 各控件变化均调用 `apply`；字符集变化初始化排序 |
  | `void init(MysqlColumn, ShellMysqlClient)` | 初始化 | 移除旧类型监听、存字段、`doInit`、注册类型变更监听 |
  | `private void listenColumnTypeChanged(...)` | 类型变更 | 类型变化时 `doInit` |
  | `void doInit()` | 重建 UI | 依 `column.supportXxx()` 显示/隐藏各 box 并回填值；`ignoreChanged` 包裹 |
  | `void initialize(URL, ResourceBundle)` | FXML 初始化 | 各 box `managedBindVisible` |
- 调用链：`init → doInit`；`控件变化 → apply`；`列类型变化 → listenColumnTypeChanged → doInit`

## ShellMysqlTableDesignTab（tabs.mysql.table）
> 文件: cn/oyzh/easyshell/tabs/mysql/table/ShellMysqlTableDesignTab.java
- 职责：MySQL 表设计标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/table/shellMysqlTableDesignTab.fxml` |
  | `void flushGraphic()` | 图标 | `EditSVGGlyph` |
  | `void flushTitle()` | 标题 | 未保存加 `* `；`表名@库(连接)` |
  | `String tableName()` | 表名 | controller.tableName() |
  | `void init(MysqlTable, ShellMysqlDatabaseTreeItem)` | 初始化 | controller.init + flush（捕获异常） |
  | `ShellMysqlTableDesignTabController controller()` | 控制器 | 强转 |
  | `boolean isUnsaved()` | 未保存 | controller.isUnsaved |
  | `protected void onTabCloseRequest(Event)` | 关闭请求 | 未保存确认 |
  | `ShellMysqlDatabaseTreeItem dbItem()` | 树节点 | controller.getDbItem |
- 调用链：`init → ShellMysqlTableDesignTabController.init`

## ShellMysqlTableDesignTabController（tabs.mysql.table）
> 文件: cn/oyzh/easyshell/tabs/mysql/table/ShellMysqlTableDesignTabController.java
- 职责：MySQL 表设计内容控制器，管理表属性（引擎/字符集/排序/行格式/注释/自增）与字段/索引/外键/触发器/检查器，生成建表/改表 SQL 并执行保存。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tabPane | FXTabPane | FXML 注入，切换面板 |
  | tableEngine | ShellMysqlEngineComboBox | FXML 注入，引擎 |
  | tableCharset / tableCollation | ShellMysqlCharsetComboBox / ShellMysqlCollationComboBox | FXML 注入，字符集/排序 |
  | tableRowFormatBox / tableRowFormat | FXHBox / ShellMysqlRowFormatComboBox | FXML 注入，行格式 |
  | tableAutoIncrementBox / tableAutoIncrement | FXHBox / NumberTextField | FXML 注入，自增起始 |
  | tableComment | FXTextArea | FXML 注入，注释 |
  | preview | Editor | FXML 注入，SQL 预览 |
  | columnTable | DBStatusTableView<MysqlColumnControl> | FXML 注入，字段表 |
  | indexTable | DBStatusTableView<MysqlIndexControl> | FXML 注入，索引表 |
  | foreignKeyTable | DBStatusTableView<MysqlForeignKeyControl> | FXML 注入，外键表 |
  | triggerTable | DBStatusTableView<MysqlTriggerControl> | FXML 注入，触发器表 |
  | checkTable | DBStatusTableView<MysqlCheckControl> | FXML 注入，检查器表 |
  | tableColumnExtraController | ShellMysqlTableColumnExtraController | FXML 注入，字段额外配置子控制器 |
  | table | MysqlTable | 当前表 |
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库树节点 |
  | listener | DBStatusListener | 数据变更监听器 |
  | unsaved / newData / initiating | boolean | 未保存/新数据/初始化中 |
  | tableName | String | 保存时表名 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private MysqlCreateTableParam initCreateParam()` / `initAlertParam()` | 建参数 | 走 `initParam(true/false)` |
  | `private Object initParam(boolean)` | 汇集参数 | 对比表属性生成 `MysqlTable`；收集字段/索引/外键/触发器/检查（含 deleteItems）→ `dbItem.createTableParam/alterTableParam` |
  | `@FXML refresh()` | 刷新 | 确认后 `initTable/resetTable/init/initPreview` |
  | `@FXML save()` | 保存 | mask 内 `doSave` |
  | `private void doSave()` | 执行保存 | 校验字段/索引/外键/触发器/检查是否 invalid（定位对应 tab）；新增 `dbItem.createTable` 追加树节点，否则 `dbItem.alterTable` + `ShellMysqlEventUtil.tableAlerted`；刷新、重置、预览 |
  | `private void initChangedFlag()` | 变更标志 | 非初始化中置 unsaved |
  | `protected void resetTable()` | 重置表格 | 各 DBStatusTableView `reset` |
  | `protected void initInfo()` | 信息 | newData→`initNew`，否则重查表→`initNormal`，结束清 initiating |
  | `protected void initNew()` | 新表 | 显示 action2/隐藏 action3，默认引擎 innoDB，初始化字符集 |
  | `protected void initNormal()` | 已有表 | 隐藏 action2/显示 action3，填充引擎/注释/字符集/排序，加载检查器/索引/字段/触发器/外键（`*Control.of(...)`），行格式/自增 |
  | `private void addColumn/deleteColumn/addIndex/.../addCheck/deleteCheck()` | 子项增删 | new 对应 `*Control` 标记 created 或确认后 `removeItem`+deleted |
  | `private void initTable()` | 初始化表格 | 设置各表 Ctrl+S；字段列表变化刷新索引/外键 |
  | `protected void bindListeners()` | 绑定 | 字符集→排序、引擎→行格式显示、tab 切换显示按钮组与预览、字段选中→`tableColumnExtraController.init`、索引/外键列表变化、`initTable` |
  | `private void initIndexTable()` / `initForeignKeyTable()` | 刷新列引用 | 设索引/外键的列列表与 dbName/dbClient |
  | `private void initPreview()` | 预览 | newData 用 `MysqlTableCreateSqlGenerator`，否则 `MysqlTableAlertSqlGenerator` |
  | `void init(MysqlTable, ShellMysqlDatabaseTreeItem)` | 初始化 | 存字段、`newData=table.isNew()`、设 dbName、mask 内 `doInit` |
  | `private void doInit()` | 执行初始化 | `initDBListener`、`tableEngine.init`、`initInfo`、不支持 check 时移除 checkTab |
  | `private void initDBListener()` | 数据监听 | 建 `DBStatusListener` 绑定 5 个表与表属性控件 |
  | `@FXML doAdd()/doDelete()/doMoveUp()/doMoveDown()` | 列表操作 | 按当前选中 tab 分派到对应增/删/上下移 |
  | `String tableName()` / `dbName()` | 名称 | 返回表名/库名 |
  | `List<? extends SubTabController> getSubControllers()` | 子控制器 | 返回 tableColumnExtraController |
  | `ShellMysqlDatabaseTreeItem getDbItem()` / `boolean isUnsaved()` | 访问器 | 读字段 |
- 调用链：`save → doSave → initCreateParam/initAlertParam → dbItem.createTable/alterTable`；`initPreview → MysqlTable*SqlGenerator`

## ShellMysqlTableRecordTab（tabs.mysql.table）
> 文件: cn/oyzh/easyshell/tabs/mysql/table/ShellMysqlTableRecordTab.java
- 职责：MySQL 表记录标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/table/shellMysqlTableRecordTab.fxml` |
  | `void flushGraphic()` | 图标 | `TableSVGGlyph` |
  | `void flushTitle()` | 标题 | `表名@库(连接名)` |
  | `boolean init(ShellMysqlTableTreeItem)` | 初始化 | controller.init + flush，返回 true |
  | `ShellMysqlTableRecordTabController controller()` | 控制器 | 强转 |
  | `void reload()` | 刷新 | controller.reload() |
  | `ShellMysqlClient client()` | 客户端 | `item().client()` |
  | `void setFilters(List<MysqlRecordFilter>)` | 过滤 | controller.setFilters |
  | `ShellMysqlTableTreeItem item()` | 树节点 | controller.getItem() |
  | `String tableName()` | 表名 | `item().tableName()` |
  | `ShellMysqlDatabaseTreeItem dbItem()` | 库节点 | `item().dbItem()` |
- 调用链：`init → ShellMysqlTableRecordTabController.init → reload`

## ShellMysqlTableRecordTabController（tabs.mysql.table）
> 文件: cn/oyzh/easyshell/tabs/mysql/table/ShellMysqlTableRecordTabController.java
- 职责：MySQL 表记录内容控制器，分页展示记录并支持增/改/删、应用/丢弃、过滤、翻页与导入导出。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | FXML 注入，根节点 |
  | itemProperty | ObjectProperty<ShellMysqlTableTreeItem> | 表树节点属性 |
  | pageData | Paging<MysqlRecord> | 分页数据 |
  | filter | SVGGlyph | FXML 注入，过滤按钮 |
  | missPrimaryKey | SVGGlyph | FXML 注入，缺主键警告 |
  | pageBox | PageBox<MysqlRecord> | FXML 注入，分页组件 |
  | recordTable | ShellMysqlRecordTableView | FXML 注入，记录表格 |
  | filters | List<MysqlRecordFilter> | 过滤列表 |
  | apply / discard | SVGGlyph | FXML 注入，应用/丢弃按钮 |
  | changeListener | DBStatusListener | 记录变更监听器 |
  | columns | MysqlColumns | 字段列表 |
  | setting | ShellSetting | 全局设置 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(ShellMysqlTableTreeItem)` | 初始化 | 建 itemProperty（空则 closeTab）、监听父属性、`reload`、建 `DBStatusListener(dbName:tableName)` |
  | `ShellMysqlTableTreeItem getItem()` | 树节点 | itemProperty.get() |
  | `private void initDataList(long)` | 加载数据 | `getItem().recordPage(pageNo, limit, filters, columns)` → pageBox、表格 |
  | `private void initDataListByMask(long)` | 带遮罩加载 | mask 内 `initDataList` |
  | `private List<MysqlRecordFilter> enabledFilters()` | 启用过滤 | 过滤 `isEnabled` |
  | `private void initCount(long)` | 计数 | 重建 Paging |
  | `private void initColumns(List<MysqlColumn>)` | 列 | 首列 `DBStatusColumn`，其余 `ShellMysqlRecordColumn` |
  | `private void initRecords(List<MysqlRecord>)` | 记录 | setItem |
  | `@FXML addRecord()` | 新增行 | 建 created 记录、默认值填充、加行选中、计数+1 |
  | `private void insertRecord(MysqlRecord)` | 插入 | 有主键 `getItem().insertRecord(data,pk)` 并回显，否则无主键插入 |
  | `private void updateRecord(MysqlRecord)` | 更新 | 有主键按主键更新回显；否则按变更+原始数据更新 |
  | `private MysqlRecordPrimaryKey initPrimaryKey(MysqlRecord)` | 主键 | 依 `getItem().getPrimaryKey()` |
  | `@FXML apply()` | 应变更 | created→insert、changed→update，清状态、禁用 apply |
  | `@FXML discard()` | 丢弃 | 还原/移除、更新计数 |
  | `@FXML reload()` | 刷新 | mask 内 `doReload` |
  | `private void doReload()` | 刷新业务 | 未保存确认→`initColumns(getItem().columns())`、`initDataList(0)`、缺主键提示、过滤激活、禁用 apply |
  | `@FXML filter()` | 过滤弹窗 | `ShellMysqlRecordFilterPopupController`，提交后 setFilters + reload |
  | `@FXML nextPage/prevPage/lastPage/firstPage/pageJump(PageEvent)` | 翻页 | 以 pageData 目标页调用 `initDataListByMask` |
  | `@FXML pageSetting()` | 每页设置 | `ShellDBPageSettingPopupController`，限制变化则 firstPage |
  | `@FXML deleteRecord()` | 删除 | 确认后 mask 内批量 `deleteRecords` |
  | `private void deleteRecords(List<MysqlRecord>)` | 批量删除 | 逐条删除，成功移除并计数- size |
  | `private boolean deleteRecord(MysqlRecord)` | 单条删除 | created 直接成功；否则有主键按主键 `getItem().deleteRecord`，无主键按原始数据删除 |
  | `void onTabClosed(Event)` | 关闭 | 移除 `DBStatusListenerManager` 监听 |
  | `protected void bindListeners()` | 绑定 | 缺主键主题、discard 绑定 apply、apply 切换按钮组、行变化启用 apply、选中可编辑、Ctrl+S 应用 |
  | `List<MysqlRecordFilter> getFilters()` / `setFilters(...)` | 过滤 | 读写 filters |
  | `@FXML importData()/exportData()` | 导入导出 | `ShellMysqlViewFactory.importData/exportData` |
- 调用链：`reload → doReload → initColumns/initDataList → getItem().recordPage`；`apply → insertRecord/updateRecord → getItem().insertRecord/updateRecord`

## ShellMysqlViewDesignTab（tabs.mysql.view）
> 文件: cn/oyzh/easyshell/tabs/mysql/view/ShellMysqlViewDesignTab.java
- 职责：MySQL 视图设计标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/view/shellMysqlViewDesignTab.fxml` |
  | `void flushGraphic()` | 图标 | `EditSVGGlyph` |
  | `void flushTitle()` | 标题 | 未保存加 `* `；`视图名@库(连接)` |
  | `String viewName()` | 视图名 | controller.viewName() |
  | `ShellMysqlDatabaseTreeItem dbItem()` | 树节点 | controller.getDbItem |
  | `void init(MysqlView, ShellMysqlDatabaseTreeItem)` | 初始化 | controller.init + flush |
  | `ShellMysqlViewDesignTabController controller()` | 控制器 | 强转 |
  | `boolean isUnsaved()` | 未保存 | controller.isUnsaved |
  | `protected void onTabCloseRequest(Event)` | 关闭请求 | 未保存确认 |
- 调用链：`init → ShellMysqlViewDesignTabController.init`

## ShellMysqlViewDesignTabController（tabs.mysql.view）
> 文件: cn/oyzh/easyshell/tabs/mysql/view/ShellMysqlViewDesignTabController.java
- 职责：MySQL 视图设计内容控制器，管理定义者/算法/安全性/检查选项/定义与保存/SQL 预览。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | view | MysqlView | 当前视图 |
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库树节点 |
  | definer | FXTextField | FXML 注入，定义者 |
  | algorithm | ShellMysqlViewAlgorithmComboBox | FXML 注入，算法 |
  | securityType | ShellMysqlSecurityTypeComboBox | FXML 注入，安全性 |
  | checkOption | ShellMysqlViewCheckOptionComboBox | FXML 注入，检查选项 |
  | definition | ShellMysqlQueryEditor | FXML 注入，定义 |
  | preview | Editor | FXML 注入，预览 |
  | tabPane | FXTabPane | FXML 注入，切换面板 |
  | listener | DBStatusListener | 数据变更监听器 |
  | unsaved / newData / initiating | boolean | 未保存/新数据/初始化中 |
  | viewName | String | 保存时视图名 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void initInfo()` | 填充信息 | newData 设默认值；否则 `dbItem.selectView` 填充各控件；结束清 initiating |
  | `void init(MysqlView, ShellMysqlDatabaseTreeItem)` | 初始化 | 存字段、`newData=view.isNew()`、mask 内 `doInit` |
  | `private void doInit()` | 执行初始化 | `initDBListener`、`initInfo`、绑定控件监听 |
  | `private void initDBListener()` | 数据监听 | 重建 `DBStatusListener(dbName:name)` 绑定各控件 |
  | `private void initChangedFlag()` | 变更标志 | 非初始化中置 unsaved |
  | `@FXML refresh()` / `@FXML save()` | 刷新/保存 | 保存 mask 内 `doSave` |
  | `private void doSave()` | 执行保存 | `tempData`；新增 `dbItem.createView` 追加树节点，否则 `dbItem.alertView` + `ShellMysqlEventUtil.viewAlerted`；刷新、预览 |
  | `private MysqlView tempData()` | 临时数据 | 汇集名称/库/定义者/定义/算法/检查选项/安全性 |
  | `protected void bindListeners()` | 绑定 | Ctrl+S；切换面板第 2 页 `initPreview` |
  | `String dbName()` / `viewName()` | 名称 | 返回库名/视图名 |
  | `ShellMysqlDatabaseTreeItem getDbItem()` / `boolean isUnsaved()` | 访问器 | 读字段 |
  | `private void initPreview()` | 预览 | newData 用 `MysqlViewCreateSqlGenerator`，否则 `MysqlViewAlertSqlGenerator` |
- 调用链：`save → doSave → tempData → dbItem.createView/alertView → initPreview`

## ShellMysqlViewRecordTab（tabs.mysql.view）
> 文件: cn/oyzh/easyshell/tabs/mysql/view/ShellMysqlViewRecordTab.java
- 职责：MySQL 视图记录标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | openedTime | long | 标签打开时间（System.currentTimeMillis） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML | `mysql/view/shellMysqlViewRecordTab.fxml` |
  | `void flushGraphic()` | 图标 | `ViewSVGGlyph` |
  | `void flushTitle()` | 标题 | `视图名@库(连接名)` |
  | `boolean init(ShellMysqlViewTreeItem)` | 初始化 | controller.init + flush |
  | `ShellMysqlViewRecordTabController controller()` | 控制器 | 强转 |
  | `void reload()` | 刷新 | controller.reload() |
  | `ShellMysqlViewTreeItem item()` | 树节点 | controller.getItem() |
  | `ShellMysqlClient client()` | 客户端 | `item().client()` |
  | `String viewName()` | 视图名 | `item().viewName()` |
  | `ShellMysqlDatabaseTreeItem dbItem()` | 库节点 | `item().dbItem()` |
  | `void setFilters(List<MysqlRecordFilter>)` | 过滤 | controller.setFilters |
- 调用链：`init → ShellMysqlViewRecordTabController.init → reload`

## ShellMysqlViewRecordTabController（tabs.mysql.view）
> 文件: cn/oyzh/easyshell/tabs/mysql/view/ShellMysqlViewRecordTabController.java
- 职责：MySQL 视图记录内容控制器，分页展示记录并支持增/改/删、应用/丢弃、过滤、翻页；仅当视图可更新时开放编辑。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | FXML 注入，根节点 |
  | itemProperty | ObjectProperty<ShellMysqlViewTreeItem> | 视图树节点属性 |
  | pageData | Paging<MysqlRecord> | 分页数据 |
  | filter | SVGGlyph | FXML 注入，过滤按钮 |
  | missPrimaryKey | SVGGlyph | FXML 注入，缺主键警告 |
  | pageBox | PageBox<MysqlRecord> | FXML 注入，分页组件 |
  | recordTable | ShellMysqlRecordTableView | FXML 注入，记录表格 |
  | filters | List<MysqlRecordFilter> | 过滤列表 |
  | apply / discard | SVGGlyph | FXML 注入，应用/丢弃按钮 |
  | changeListener | DBStatusListener | 记录变更监听器 |
  | columns | MysqlColumns | 字段列表 |
  | setting | ShellSetting | 全局设置 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(ShellMysqlViewTreeItem)` | 初始化 | 建 itemProperty、监听父属性、`reload`；`item.isUpdatable()` 时建 `DBStatusListener`、显示 apply 与 action2 |
  | `ShellMysqlViewTreeItem getItem()` | 树节点 | itemProperty.get() |
  | `private void initDataList(long)` / `initDataListByMask(long)` | 加载数据 | 同表记录控制器（走 `getItem().recordPage`） |
  | `private List<MysqlRecordFilter> enabledFilters()` | 启用过滤 | 过滤 isEnabled |
  | `private void initCount(long)` | 计数 | 重建 Paging |
  | `private void initColumns(MysqlColumns)` | 列 | 首列 `DBStatusColumn` + `ShellMysqlRecordColumn` |
  | `private void initRecords(List<MysqlRecord>)` | 记录 | setItem |
  | `@FXML addRecord()` | 新增行 | created 记录默认值填充并加行 |
  | `private void insertRecord/updateRecord(MysqlRecord)` | 增改 | 与表记录控制器同逻辑（`getItem().insertRecord/updateRecord` + 回显） |
  | `private MysqlRecordPrimaryKey initPrimaryKey(MysqlRecord)` | 主键 | 依 `getItem().getPrimaryKey()` |
  | `@FXML apply()` / `discard()` | 应用/丢弃 | 遍历记录提交或还原 |
  | `@FXML reload()` `public` / `private doReload()` | 刷新 | 未保存确认→重建列/数据、缺主键提示、过滤激活、禁用 apply |
  | `@FXML filter()` | 过滤弹窗 | `ShellMysqlRecordFilterPopupController` |
  | `@FXML nextPage/prevPage/lastPage/firstPage/pageJump/pageSetting` | 翻页/设置 | 同表记录控制器 |
  | `@FXML deleteRecord()` / `private deleteRecords/deleteRecord` | 删除 | 同表记录控制器 |
  | `void onTabClosed(Event)` | 关闭 | 移除 DBStatusListenerManager 监听 |
  | `protected void bindListeners()` | 绑定 | 同表记录控制器 |
  | `List<MysqlRecordFilter> getFilters()` / `setFilters(...)` | 过滤 | 读写 filters |
- 调用链：`reload → doReload → initColumns/initDataList`；`apply → insertRecord/updateRecord → getItem().insertRecord/updateRecord`
