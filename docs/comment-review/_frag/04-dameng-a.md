# 达梦数据库标签页（主框架/查询/终端/函数）

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
