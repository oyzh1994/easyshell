# MySQL 标签页（主框架/查询/终端/事件）

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
