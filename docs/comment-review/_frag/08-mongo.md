# MongoDB 标签页

## ShellMongoBaseTab
> 文件: cn/oyzh/easyshell/tabs/mongo/ShellMongoBaseTab.java
- 职责：MongoDB 数据库基础标签页抽象基类，约定数据库树节点访问方法。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public abstract ShellMongoDatabaseTreeItem dbItem()` | 获取数据库树节点 | 抽象方法，由子类实现，供 `ShellMongoTabPane` 按 `dbItem` 引用匹配标签页 |
- 调用链：（无，仅提供抽象约定）

## ShellMongoTab
> 文件: cn/oyzh/easyshell/tabs/mongo/ShellMongoTab.java
- 职责：MongoDB 数据库连接标签页，展示 OS 类型图标与标题，委托控制器建立连接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public String getTabTitle()` | 标签标题 | `连接名(类型大写)`，取 `shellConnect()` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时按 `shellConnect().getOsType()` 生成 `SVGGlyph` 并设默认光标 |
  | `protected String url()` | 返回 FXML 路径 | `/tabs/mongo/shellMongoTab.fxml` |
  | `protected ShellMongoTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public void init(ShellConnect connect)` | 初始化连接 | `controller().init(connect)` 建客户端，再 `super.init(connect)` 刷新图标，异常打印 |
  | `public ShellBaseClient client()` | 获取客户端 | 委托 `controller().getClient()` |
  | `public static ShellMongoTab of(ShellConnect connect)` | 工厂方法 | `new` 后 `init(connect)` 返回 |
- 调用链：`ShellMongoTabPane/ShellTabPane → ShellMongoTab.of → init → ShellMongoTabController.init`

## ShellMongoTabController
> 文件: cn/oyzh/easyshell/tabs/mongo/ShellMongoTabController.java
- 职责：MongoDB 连接标签页内容控制器，创建客户端、加载数据库树、提供过滤与数据导入导出/脚本运行入口。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | ShellMongoClient | 当前 MongoDB 客户端 |
  | root | FXSplitPane | FXML 注入，根分割面板（用于绑定快捷键） |
  | tabPane | ShellMongoTabPane | FXML 注入，内部标签页容器 |
  | treeView | ShellMongoTreeView | FXML 注入，MongoDB 对象树 |
  | filterKW | FilterTextField | FXML 注入，树过滤关键字输入框 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellConnect shellConnect()` | 获取 shell 连接 | `client.getShellConnect()` |
  | `public void init(ShellConnect connect)` | 初始化并加载根节点 | `ShellClientUtil.newClient(connect)`；`client.addStateListener(...)` 监听 `INTERRUPTED` 弹提示；`StageManager.showMask` 内 `client.start()`，未连接则 `close()`+告警+`closeTab()`，否则 `tabPane.setClient`、`treeView.setClient`、`root().loadChild()`+`expend()`、`hideLeft()` |
  | `public ShellMongoClient getClient()` | 获取客户端 | 返回 `client` |
  | `public void onTabClosed(Event event)` | 关闭清理 | `super.onTabClosed(event)` 后 `IOUtil.close(client)` |
  | `private void doFilter()` | 执行树过滤 | 取 `filterKW` 文本/大小写/全字，设置 `treeView` 高亮与 `getItemFilter()` 参数，`ThreadUtil.start(() -> treeView.filter())` |
  | `private void importData()` | 导入数据（FXML 事件） | `ShellMongoViewFactory.importData(client, null)` |
  | `private void exportData()` | 导出数据（FXML 事件） | `ShellMongoViewFactory.exportData(client, null, null)` |
  | `private void runScriptFile()` | 运行脚本文件（FXML 事件） | `ShellMongoViewFactory.runScriptFile(client, null)` |
  | `private void positionNode()` | 定位节点（FXML 事件） | `treeView.positionItem()` |
  | `private void transportData()` | 传输数据（FXML 事件） | `ShellMongoViewFactory.transportData(client.getShellConnect(), null)` |
  | `public void onTabInit(FXTab tab)` | 标签页初始化 | 仅调用 `super.onTabInit(tab)` |
  | `protected void bindListeners()` | 绑定监听器 | 注册 Ctrl+F 聚焦 `filterKW` 的 `KeyHandler`；对 `filterKW` 文本、全字、大小写属性添加监听触发 `doFilter()` |
  | `public void destroy()` | 销毁清理 | `tabPane.destroy()`、`treeView.destroy()` 后 `super.destroy()` |
- 调用链：`ShellMongoTab.init → init → ShellClientUtil.newClient → client.start → treeView.root().loadChild`；`doFilter → treeView.filter`

## ShellMongoTabPane
> 文件: cn/oyzh/easyshell/tabs/mongo/ShellMongoTabPane.java
- 职责：MongoDB 内部切换面板，按事件为集合/桶/查询/终端/函数/用户创建对应标签页，并统一维护主页标签。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | clientProperty | SimpleObjectProperty\<ShellMongoClient\> | 客户端属性，惰性创建 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void setClient(ShellMongoClient client)` | 设置客户端 | `clientProperty().set(client)` |
  | `public ShellMongoClient getClient()` | 获取客户端 | `clientProperty` 为 null 返回 null，否则 `get()` |
  | `public SimpleObjectProperty<ShellMongoClient> clientProperty()` | 获取客户端属性 | 为 null 时 `new SimpleObjectProperty<>()` |
  | `public void initNode()` | 初始化节点 | `super.initNode()`；`initHomeTab()`；监听 `getTabs()` 变化，增删时 `TaskManager.startDelay(this::flushHomeTab, 100)` |
  | `private void flushHomeTab()` | 刷新主页标签 | `tabsEmpty()` 时 `initHomeTab()`，`tabsSize()>1` 时 `closeHomeTab()` |
  | `public ShellMongoHomeTab getHomeTab()` | 获取主页标签 | 遍历 `getTabs()` 匹配 `ShellMongoHomeTab` |
  | `public void initHomeTab()` | 初始化主页标签 | 无主页标签时 `super.addTab(new ShellMongoHomeTab())` |
  | `public void closeHomeTab()` | 关闭主页标签 | 有主页标签时 `super.removeTab(homeTab)` |
  | `private ShellMongoCollectionRecordTab getMongoCollectionRecordTab(ShellMongoDatabaseTreeItem, String)` | 查找集合记录标签 | 匹配 `dbItem()==dbItem` 且集合名相等 |
  | `private void onMongoCollectionOpen(ShellMongoCollectionOpenEvent)` | 集合打开事件（`@EventSubscribe`） | 复用或 `new ShellMongoCollectionRecordTab()` 并 `init(event.data())`、`addTab`，最后 `select(tab)` |
  | `private ShellMongoBucketRecordTab getMongoBucketRecordTab(ShellMongoDatabaseTreeItem, String)` | 查找桶记录标签 | 匹配 `dbItem()` 与桶名相等 |
  | `private void onMongoBucketOpen(ShellMongoBucketOpenEvent)` | 桶打开事件（`@EventSubscribe`） | 复用或 `new ShellMongoBucketRecordTab()` 并 `init(event.data())`、`addTab`，最后 `select(tab)` |
  | `private ShellMongoQueryMainTab getMongoQueryMainTab(String queryId)` | 查找查询主标签 | 匹配 `queryId()` |
  | `private void onMongoQueryAdd(ShellMongoQueryAddEvent)` | 查询新增事件（`@EventSubscribe`） | `new ShellMongoQueryMainTab()`；`new ShellQuery()` 后 `tab.init(query, event.data())`，`addTab`+`select` |
  | `private void onMongoQueryOpen(ShellMongoQueryOpenEvent)` | 查询打开事件（`@EventSubscribe`） | 复用或 `new ShellMongoQueryMainTab()` 并 `init(event.data(), event.getDbItem())`，`addTab`+`select` |
  | `private ShellMongoTerminalTab getTerminalTab(ShellMongoClient, String)` | 查找终端标签 | 匹配 `client()==client` 且 `dbName()` 相等 |
  | `private void onMongoTerminalOpen(ShellMongoTerminalOpenEvent)` | 终端打开事件（`@EventSubscribe`） | 复用或 `new ShellMongoTerminalTab()` 并 `init(event.data(), event.getDbName())`、`addTab`；复用则 `flushGraphic()`；未选中则 `select` |
  | `private void onMongoTerminalClose(ShellMongoTerminalCloseEvent)` | 终端关闭事件（`@EventSubscribe`） | 找到终端标签后 `closeTab()` |
  | `private ShellMongoFunctionDesignTab getMongoFunctionDesignTab(ShellMongoDatabaseTreeItem, String)` | 查找函数设计标签 | 匹配 `dbItem()` 与函数名相等 |
  | `private void onMongoFunctionRenamed(ShellMongoFunctionRenamedEvent)` | 函数重命名事件（`@EventSubscribe`） | 找到后 `tab.closeTab()` |
  | `private void onMongoFunctionDesign(ShellMongoFunctionDesignEvent)` | 函数设计事件（`@EventSubscribe`） | 复用或 `new ShellMongoFunctionDesignTab()` 并 `init(event.data(), event.getDbItem())`，`addTab`+`select` |
  | `private void onMongoFunctionDropped(ShellMongoFunctionDroppedEvent)` | 函数删除事件（`@EventSubscribe`） | 找到后 `tab.closeTab()` |
  | `private void onMongoCollectionRenamed(ShellMongoCollectionRenamedEvent)` | 集合重命名事件（`@EventSubscribe`） | 按新集合名查找后 `tab.closeTab()` |
  | `private List<ShellMongoBaseTab> getBaseTabs(ShellMongoDatabaseTreeItem)` | 获取同库基础标签列表 | 收集 `dbItem()==dbItem` 的 `ShellMongoBaseTab` |
  | `private void onMongoDatabaseClosed(ShellMongoDatabaseClosedEvent)` | 数据库关闭事件（`@EventSubscribe`） | `removeTab(getBaseTabs(event.data()))` |
  | `private void onMongoQueryRenamed(ShellMongoQueryRenamedEvent)` | 查询重命名事件（`@EventSubscribe`） | 按 `event.data()` 查找后 `tab.closeTab()` |
  | `private ShellMongoUserViewTab getMongoUserViewTab(MongoUser)` | 查找用户视图标签 | 匹配用户名相等 |
  | `private void onMongoUserView(ShellMongoUserViewEvent)` | 用户查看事件（`@EventSubscribe`） | 复用或 `new ShellMongoUserViewTab()` 并 `init(event.data(), event.getDbItem())`，`addTab`+`select` |
  | `private void onMongoUserDelete(ShellMongoUserDeletedEvent)` | 用户删除事件（`@EventSubscribe`） | 找到后 `tab.closeTab()` |
- 调用链：`onMongoCollectionOpen → new ShellMongoCollectionRecordTab → init → addTab/select`；`flushHomeTab → initHomeTab/closeHomeTab`

## ShellMongoBucketRecordTab
> 文件: cn/oyzh/easyshell/tabs/mongo/bucket/ShellMongoBucketRecordTab.java
- 职责：MongoDB 存储桶记录标签页，展示桶图标与标题，委托控制器加载存储桶文件数据。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | 返回 FXML 路径 | `FXConst.TAB_PATH + "mongo/bucket/shellMongoBucketRecordTab.fxml"` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new BucketSVGGlyph()` 并设默认光标 |
  | `public void flushTitle()` | 刷新标签标题 | `桶名@库名(信息名)` |
  | `public boolean init(ShellMongoBucketTreeItem item)` | 初始化 | `controller().init(item)` 后 `flush()`，恒返回 true |
  | `public ShellMongoBucketRecordTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public void reload()` | 重载数据 | 委托 `controller().reload()` |
  | `public ShellMongoClient client()` | 获取客户端 | `item().client()` |
  | `public void setFilters(List<MongoRecordFilter> filters)` | 设置过滤条件 | 委托 `controller().setFilters(filters)` |
  | `public ShellMongoBucketTreeItem item()` | 获取树节点 | `controller().getItem()` |
  | `public String bucketName()` | 获取桶名称 | `item().bucketName()` |
  | `public ShellMongoDatabaseTreeItem dbItem()` | 获取数据库树节点 | `item().dbItem()` |
  | `public String dbName()` | 获取数据库名称 | `item().dbName()` |
- 调用链：`ShellMongoTabPane.onMongoBucketOpen → init → ShellMongoBucketRecordTabController.init`

## ShellMongoBucketRecordTabController
> 文件: cn/oyzh/easyshell/tabs/mongo/bucket/ShellMongoBucketRecordTabController.java
- 职责：存储桶记录标签页内容控制器，分页加载桶内文件并支持过滤、分页、上传下载、查看/编辑/删除。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | FXML 注入，根节点（绑定快捷键） |
  | client | ShellMongoClient | MongoDB 客户端 |
  | itemProperty | ObjectProperty\<ShellMongoBucketTreeItem\> | 存储桶树节点属性 |
  | pageData | Paging\<MongoBucketFile\> | 当前分页数据 |
  | filter | SVGGlyph | FXML 注入，记录过滤按钮 |
  | manage | SVGLabel | FXML 注入，上传/下载管理入口 |
  | pageBox | PageBox\<MongoBucketFile\> | FXML 注入，分页组件 |
  | fileTable | ShellMongoBucketFileTableView | FXML 注入，文件数据表 |
  | filterFile | ClearableTextField | FXML 注入，文件名过滤输入框 |
  | fileInfo | FXLabel | FXML 注入，文件信息显示 |
  | fileName | TableColumn\<ShellFile,?\> | FXML 注入，文件名列 |
  | filters | List\<MongoRecordFilter\> | 过滤条件列表 |
  | columns | MongoColumns | 字段列表，来自 `ShellMongoHelper.bucketColumns()` |
  | setting | ShellSetting | 程序设置（final），取 `ShellSettingStore.SETTING` |
  | taskSizeListener | ListChangeListener\<ShellFileTask\> | 文件任务数量监听器 |
  | taskTypes | List\<ShellFileTaskType\> | 任务类型列表（final），UPLOAD/DOWNLOAD |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellMongoBucketTreeItem item)` | 初始化 | 设置 `client`、`columns`、`fileTable` 客户端/库名/桶名；`itemProperty` 与 `item.parentProperty()` 监听置空时 `closeTab()`；`client.addTaskSizeListener(...)` 更新 `manage` 文本与计数；最后 `reload()` |
  | `public ShellMongoBucketTreeItem getItem()` | 获取树节点 | `itemProperty.get()` |
  | `private void initDataList(long pageNo)` | 加载分页数据 | `getItem().recordPage(...)` 取 `pageData`，`pageBox.setPaging`，`fileTable.setItem(records)` |
  | `private void initDataListByMask(long pageNo)` | 带遮罩加载分页 | `StageManager.showMask(() -> initDataList(pageNo))` |
  | `private List<MongoRecordFilter> enabledFilters()` | 获取已启用过滤条件 | 过滤 `isEnabled()`，空则返回 null |
  | `private void initCount(long count)` | 更新计数 | 以 `fileTable.itemList()`、`pageData.limit()`、`count` 重建 `Paging` 并 `pageBox.setPaging` |
  | `private void editDocument()` | 编辑文档（FXML 事件） | `fileTable.editDocument(fileTable.getSelectedItem())` |
  | `public void viewDocument()` | 查看文档（FXML 事件） | `fileTable.viewFile(fileTable.getSelectedItem())` |
  | `public void reload()` | 刷新记录（FXML 事件） | `StageManager.showMask(this::doReload)` |
  | `private void doReload()` | 刷新业务 | `initDataListByMask(0)`；按 `enabledFilters()` 设置 `filter.setActive(...)` |
  | `private void filter()` | 过滤记录（FXML 事件） | `PopupManager.parsePopup(ShellMongoRecordFilterPopupController.class)`，设置 item/filters/columns，`setSubmitHandler` 内 `setFilters`+`reload` |
  | `private void nextPage()` | 下一页（FXML 事件） | `initDataListByMask(pageData.nextPage())` |
  | `private void prevPage()` | 上一页（FXML 事件） | `initDataListByMask(pageData.prevPage())` |
  | `private void lastPage()` | 尾页（FXML 事件） | `initDataListByMask(pageData.lastPage())` |
  | `private void firstPage()` | 首页（FXML 事件） | `initDataListByMask(0)` |
  | `private void pageJump(PageEvent.PageJumpEvent event)` | 跳页（FXML 事件） | `initDataListByMask(event.getPage())` |
  | `private void pageSetting()` | 页码设置（FXML 事件） | 弹出 `ShellMongoPageSettingPopupController`，提交时若页限变化则 `firstPage()` |
  | `private void deleteRecord()` | 删除记录（FXML 事件） | 取选中项后 `fileTable.deleteFile(records)` |
  | `private void uploadRecord()` | 上传记录（FXML 事件） | `fileTable.uploadFile()` |
  | `private void downloadRecord()` | 下载记录（FXML 事件） | `fileTable.downloadFile(...)`，取过滤后选中项 |
  | `private void manage()` | 管理上传下载（FXML 事件） | `ShellViewFactory.fileManage(client)` |
  | `protected void bindListeners()` | 绑定监听器 | `fileName` 单元格工厂设为 `IconTableCell`；`filterFile` 文本变化设置表格过滤；`root` 监听搜索快捷键聚焦 `filterFile`；监听 `fileTable.itemList()` 更新 `fileInfo` |
  | `private void draggedFile(ShellFileDraggedEvent event)` | 文件拖拽事件（`@EventSubscribe`） | 标签选中时 `fileTable.uploadFile(event.data())` |
  | `public List<MongoRecordFilter> getFilters()` | 获取过滤条件 | 返回 `filters` |
  | `public void setFilters(List<MongoRecordFilter> filters)` | 设置过滤条件 | 赋值 `filters` |
  | `public void destroy()` | 销毁清理 | `client.removeTaskSizeListener(...)`、`fileTable.destroy()` 后 `super.destroy()` |
- 调用链：`init → reload → doReload → initDataListByMask → initDataList → getItem().recordPage → fileTable.setItem`

## ShellMongoCollectionRecordTab
> 文件: cn/oyzh/easyshell/tabs/mongo/collection/ShellMongoCollectionRecordTab.java
- 职责：MongoDB 集合记录标签页，展示集合图标与标题，委托控制器加载集合文档数据。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | 返回 FXML 路径 | `FXConst.TAB_PATH + "mongo/collection/shellMongoCollectionRecordTab.fxml"` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new TableSVGGlyph()` 并设默认光标 |
  | `public void flushTitle()` | 刷新标签标题 | `集合名@库名(信息名)` |
  | `public boolean init(ShellMongoCollectionTreeItem item)` | 初始化 | `controller().init(item)` 后 `flush()`，恒返回 true |
  | `public ShellMongoCollectionRecordTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public void reload()` | 重载数据 | 委托 `controller().reload()` |
  | `public ShellMongoClient client()` | 获取客户端 | `item().client()` |
  | `public void setFilters(List<MongoRecordFilter> filters)` | 设置过滤条件 | 委托 `controller().setFilters(filters)` |
  | `public ShellMongoCollectionTreeItem item()` | 获取树节点 | `controller().getItem()` |
  | `public String collectionName()` | 获取集合名称 | `item().collectionName()` |
  | `public ShellMongoDatabaseTreeItem dbItem()` | 获取数据库树节点 | `item().dbItem()` |
  | `public String dbName()` | 获取数据库名称 | `item().dbName()` |
- 调用链：`ShellMongoTabPane.onMongoCollectionOpen → init → ShellMongoCollectionRecordTabController.init`

## ShellMongoCollectionRecordTabController
> 文件: cn/oyzh/easyshell/tabs/mongo/collection/ShellMongoCollectionRecordTabController.java
- 职责：集合记录标签页内容控制器，分页加载文档、动态维护列，并支持新增/编辑/删除/应用/丢弃变更与导入导出。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | FXML 注入，根节点（绑定快捷键） |
  | itemProperty | ObjectProperty\<ShellMongoCollectionTreeItem\> | 集合树节点属性 |
  | pageData | Paging\<MongoRecord\> | 当前分页数据 |
  | filter | SVGGlyph | FXML 注入，记录过滤按钮 |
  | pageBox | PageBox\<MongoRecord\> | FXML 注入，分页组件 |
  | recordTable | ShellMongoRecordTableView | FXML 注入，记录数据表 |
  | filters | List\<MongoRecordFilter\> | 过滤条件列表 |
  | apply | SVGGlyph | FXML 注入，应用变更按钮 |
  | discard | SVGGlyph | FXML 注入，丢弃变更按钮 |
  | changeListener | DBStatusListener | 记录变更监听器，变更时 `apply.enable()` |
  | columns | MongoColumns | 字段列表，动态维护 |
  | setting | ShellSetting | 程序设置（final），取 `ShellSettingStore.SETTING` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellMongoCollectionTreeItem item)` | 初始化 | 建 `itemProperty` 并监听置空 `closeTab()`；监听 `item.parentProperty()` 置空 `closeTab()`；`reload()`；创建 `changeListener`（键为 `dbName:collectionName`）触发 `apply.enable()` |
  | `public ShellMongoCollectionTreeItem getItem()` | 获取树节点 | `itemProperty.get()` |
  | `private void initDataList(long pageNo)` | 加载分页数据 | `getItem().recordPage(...)` 取 `pageData`，`pageBox.setPaging`，`updateColumns`、`initRecords`、`correctRecords` |
  | `private void initDataListByMask(long pageNo)` | 带遮罩加载分页 | `StageManager.showMask(() -> initDataList(pageNo))` |
  | `private List<MongoRecordFilter> enabledFilters()` | 获取已启用过滤条件 | 过滤 `isEnabled()`，空则 null |
  | `private void updateColumns(List<MongoRecord> records)` | 更新字段 | 汇总记录列名，缺失列 `column.copy` 后 `add`，删除已消失列，有变化时 `initColumns` |
  | `private void initCount(long count)` | 更新计数 | 以 `recordTable.itemList()`、`pageData.limit()`、`count` 重建 `Paging` 并 `pageBox.setPaging` |
  | `private void initColumns(MongoColumns columns)` | 初始化列 | 首列 `DBStatusColumn`，其余按 `ShellMongoRecordColumn` 构建并 `setColumn` |
  | `private void initRecords(List<MongoRecord> records)` | 初始化记录 | `recordTable.setItem(records)` |
  | `private void correctRecords()` | 纠正记录 | 逐条 `record.correctColumns(this.columns)` |
  | `private void addRecord()` | 添加记录（FXML 事件） | 无最后一项则 `addDocument()`；否则基于其列构建新 `MongoRecord`（`setCreated(true)`、填默认值）`addItem`+`selectLast`+`initCount` |
  | `private void addDocument()` | 添加文档（FXML 事件） | 弹出 `ShellMongoViewFactory.collectionDocumentAdd`，转 `ShellMongoDataUtil.toInsertScript` 后 `getItem().eval(script)`，按插入 id 查询记录并追加 |
  | `private void editDocument()` | 编辑文档（FXML 事件） | 弹出更新视图，转 `toUpdateScript` 后 `eval`，查询 `selectCollectionRecord` 并 `record.copy(r)`、`clearStatus`，`updateColumns`+`correctRecords` |
  | `private void insertRecord(MongoRecord record)` | 插入记录 | `getItem().insertRecord(record)`，按 `_idColumn().supportObjectId()` 设置 `_id` |
  | `private void updateRecord(MongoRecord record)` | 更改记录 | `getItem().updateRecord(record)`，成功则更新字段并纠正记录，否则告警 |
  | `private void apply()` | 应用变更（FXML 事件） | 遍历记录，`DBObjectList.isCreated` 走 `insertRecord`，`isChanged` 走 `updateRecord`，各 `clearStatus` 后 `apply.disable()` |
  | `private void discard()` | 丢弃变更（FXML 事件） | 新增记录移除，变更记录 `record.discard()`，`removeItem` 后计数 |
  | `public void reload()` | 刷新记录（FXML 事件） | `StageManager.showMask(this::doReload)` |
  | `private void doReload()` | 刷新业务 | 有未保存变更时确认 `unsavedAndContinue`；`initDataListByMask(0)`、设置 `filter` 激活、`apply.disable()` |
  | `private void filter()` | 过滤记录（FXML 事件） | 弹出 `ShellMongoRecordFilterPopupController`，提交后 `setFilters`+`reload` |
  | `private void nextPage()` / `prevPage()` / `lastPage()` / `firstPage()` | 翻页（FXML 事件） | 分别 `initDataListByMask(pageData.nextPage()/prevPage()/lastPage()/0)` |
  | `private void pageJump(PageEvent.PageJumpEvent event)` | 跳页（FXML 事件） | `initDataListByMask(event.getPage())` |
  | `private void pageSetting()` | 页码设置（FXML 事件） | 弹出 `ShellMongoPageSettingPopupController`，页限变化则 `firstPage()` |
  | `private void deleteRecord()` | 删除记录（FXML 事件） | 确认后 `deleteRecords(选中记录)` |
  | `private void deleteRecords(List<MongoRecord> records)` | 批量删除 | 逐条 `deleteRecord`，成功则 `removeItem`+计数，否则告警 |
  | `private boolean deleteRecord(MongoRecord record)` | 删除单条 | 新增记录直接删；否则 `getItem().deleteRecord(record)==1`，成功 `removeItem` |
  | `public void onTabClosed(Event event)` | 关闭清理 | `super.onTabClosed(event)` 后 `DBStatusListenerManager.removeListener(changeListener)` |
  | `protected void bindListeners()` | 绑定监听器 | `discard` 禁用绑定 `apply`；`apply` 状态变化切换 `NodeGroupUtil` action2；监听 `recordTable.getItems()` 新增时 `apply.enable()`；选中项变化设可编辑；`setCtrlSAction(this::apply)` 与 `NodeUtil.nodeOnCtrlS` |
  | `public List<MongoRecordFilter> getFilters()` | 获取过滤条件 | 返回 `filters` |
  | `public void setFilters(List<MongoRecordFilter> filters)` | 设置过滤条件 | 赋值 `filters` |
  | `private void importData()` | 导入数据（FXML 事件） | `ShellMongoViewFactory.importData(client, dbName)` |
  | `private void exportData()` | 导出数据（FXML 事件） | `ShellMongoViewFactory.exportData(client, dbName, collectionName)` |
- 调用链：`init → reload → doReload → initDataListByMask → initDataList → getItem().recordPage`；`apply → insertRecord/updateRecord`

## ShellMongoFunctionDesignTab
> 文件: cn/oyzh/easyshell/tabs/mongo/function/ShellMongoFunctionDesignTab.java
- 职责：MongoDB 函数设计标签页，展示函数图标与标题，并在未保存时关闭前确认。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | 返回 FXML 路径 | `FXConst.TAB_PATH + "mongo/function/shellMongoFunctionDesignTab.fxml"` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new FunctionSVGGlyph()` 并设默认光标 |
  | `public void flushTitle()` | 刷新标签标题 | 未命名取 `I18nHelper.unnamedFunction()`；未保存时前缀 `* `，格式 `名称@库名(连接名)` |
  | `public String dbName()` | 获取数据库名称 | `dbItem().dbName()` |
  | `public String connectName()` | 获取连接名称 | `dbItem().connectName()` |
  | `public String functionName()` | 获取函数名称 | `controller().getFunction().getName()` |
  | `public ShellMongoDatabaseTreeItem dbItem()` | 获取数据库树节点 | `controller().getDbItem()` |
  | `public void init(MongoFunction function, ShellMongoDatabaseTreeItem item)` | 初始化 | `controller().init(function, item)` 后 `flush()` |
  | `public ShellMongoFunctionDesignTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public boolean isUnsaved()` | 是否未保存 | `controller().isUnsaved()` |
  | `protected void onTabCloseRequest(Event event)` | 关闭请求 | 未保存且确认 `unsavedAndContinue` 为否则 `event.consume()`，否则 `closeTab()` |
- 调用链：`ShellMongoTabPane.onMongoFunctionDesign → init → ShellMongoFunctionDesignTabController.init`

## ShellMongoFunctionDesignTabController
> 文件: cn/oyzh/easyshell/tabs/mongo/function/ShellMongoFunctionDesignTabController.java
- 职责：函数设计标签页内容控制器，编辑函数定义、预览替换脚本并支持新建/保存函数。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | function | MongoFunction | 当前函数对象 |
  | dbItem | ShellMongoDatabaseTreeItem | MongoDB 数据库树节点 |
  | definition | Editor | FXML 注入，函数定义编辑器 |
  | preview | Editor | FXML 注入，替换脚本预览编辑器 |
  | tabPane | FXTabPane | FXML 注入，定义/预览切换面板 |
  | listener | DBStatusListener | 数据变更监听器 |
  | unsaved | boolean | 未保存标志位 |
  | newData | boolean | 新数据标志位 |
  | initiating | boolean | 初始化中标志位 |
  | functionName | String | 函数名称（保存时使用） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public MongoFunction getFunction()` | 获取函数对象 | 返回 `function` |
  | `public void init(MongoFunction function, ShellMongoDatabaseTreeItem dbItem)` | 初始化 | 赋值 `dbItem`/`function`，`newData = function.isNew()`，`StageManager.showMask(this::doInit)` |
  | `private void doInit()` | 执行初始化 | `initDBListener()`；`FXUtil.runWait(this::initInfo)` |
  | `private void initDBListener()` | 初始化数据监听器 | 销毁旧 `listener` 并 `unbindListener`；新建 `DBStatusListener`（键 `dbName:name`）变更时 `initChangedFlag()`，`bindListener` 到 `definition` |
  | `private void initChangedFlag()` | 初始化变更标志 | 非 `initiating` 时置 `unsaved=true` 并 `flushTab()` |
  | `protected void initInfo()` | 初始化信息 | 置 `initiating=true`；新数据写入默认模板并 `unsaved=true`；否则 `dbItem.selectFunction` 取代码 `setText`+`forgetHistory`；`FXUtil.runPulse` 复位 `initiating` |
  | `private void save()` | 保存（FXML 事件） | `StageManager.showMask(this::doSave)` |
  | `private void doSave()` | 执行保存 | `tempData()` 构造临时函数；新数据 `MessageBox.prompt` 取名并 `dbItem.createFunction`，随后 `selectFunction`+`addFunction`+`initDBListener`；否则 `dbItem.alertFunction`；复位标志并 `initInfo`+`initPreview` |
  | `private MongoFunction tempData()` | 获取临时数据 | 以 `function` 名称、库名与 `definition` 文本构建 `MongoFunction` |
  | `public void initialize(URL location, ResourceBundle resourceBundle)` | 控制器初始化 | `NodeUtil.nodeOnCtrlS` 绑定 tab 与编辑器 `save`；`tabPane.selectedIndexChanged` 选到索引 1 时 `initPreview()` |
  | `private void initPreview()` | 初始化预览 | `tempData()` 后空名设 `Unnamed_Function`，`ShellMongoDataUtil.toReplaceScript` 写 `preview` |
  | `public boolean isUnsaved()` | 是否未保存 | 返回 `unsaved` |
  | `public void setUnsaved(boolean unsaved)` | 设置未保存标志 | 赋值 `unsaved` |
  | `public ShellMongoDatabaseTreeItem getDbItem()` | 获取数据库树节点 | 返回 `dbItem` |
  | `public void setDbItem(ShellMongoDatabaseTreeItem dbItem)` | 设置数据库树节点 | 赋值 `dbItem` |
  | `public void destroy()` | 销毁清理 | `preview.destroy()`、`definition.destroy()` 后 `super.destroy()` |
- 调用链：`init → doInit → initInfo`；`save → doSave → dbItem.createFunction/alertFunction → initPreview`

## ShellMongoHomeTab
> 文件: cn/oyzh/easyshell/tabs/mongo/home/ShellMongoHomeTab.java
- 职责：MongoDB 主页标签页，展示主页图标与标题，且不可关闭。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellMongoHomeTab()` | 构造标签页 | `super.flush()` |
  | `protected String url()` | 返回 FXML 路径 | `/tabs/mongo/home/shellMongoHomeTab.fxml` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new HomeSVGGlyph()` 并设默认光标 |
  | `protected String getTabTitle()` | 标签标题 | `I18nResourceBundle.i18nString("base.title.home")` |
  | `public void initNode()` | 初始化节点 | 先 `setClosable(false)` 禁止关闭，再 `super.initNode()` |
- 调用链：`ShellMongoTabPane.initHomeTab → new ShellMongoHomeTab`

## ShellMongoHomeTabController
> 文件: cn/oyzh/easyshell/tabs/mongo/home/ShellMongoHomeTabController.java
- 职责：MongoDB 主页标签页内容控制器，展示系统类型与 MongoDB 版本信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | system | FXLabel | FXML 注入，系统类型文本 |
  | version | FXLabel | FXML 注入，版本信息文本 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void onTabInit(FXTab tab)` | 标签页初始化 | 监听 `tab.tabPaneProperty()`，为 `ShellMongoTabPane` 时若客户端已存在则 `initInfo(client)`，否则监听 `clientProperty()` 就绪后 `initInfo`；最后 `super.flushTab()` |
  | `private void initInfo(ShellMongoClient client)` | 初始化信息 | 客户端已关闭则返回；`client.selectHostInfo()` 取 `os`/`system`，拼接 `type_cpuArch` 写 `system`，`client.selectVersion()` 写 `version` |
- 调用链：`onTabInit → clientProperty 监听 → initInfo → client.selectHostInfo/selectVersion`

## ShellMongoQueryInfoTab
> 文件: cn/oyzh/easyshell/tabs/mongo/query/ShellMongoQueryInfoTab.java
- 职责：MongoDB 查询信息标签页，承载查询执行结果的文本信息，且不可关闭。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | 返回 FXML 路径 | `FXConst.TAB_PATH + "mongo/query/shellMongoQueryInfoTab.fxml"` |
  | `public void init(DBQueryResults<?> results)` | 初始化 | 委托 `controller().init(results)` |
  | `public ShellMongoQueryInfoTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public void initNode()` | 初始化节点 | 先 `setClosable(false)` 再 `super.initNode()` |
  | `public static ShellMongoQueryInfoTab of(DBQueryResults<?> results)` | 工厂方法 | `new` 后 `init(results)` 返回 |
- 调用链：`ShellMongoQueryMainTabController.initInfoTab → infoTab.init → ShellMongoQueryInfoTabController.init`

## ShellMongoQueryInfoTabController
> 文件: cn/oyzh/easyshell/tabs/mongo/query/ShellMongoQueryInfoTabController.java
- 职责：查询信息标签页控制器，将查询结果逐条格式化为文本输出。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | infoArea | FXTextArea | FXML 注入，信息文本域 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(DBQueryResults<?> results)` | 初始化并渲染结果 | 清空后，成功时逐条 `appendLine(result.getContent())`，成功且 `getUpdateCount()>0` 输出受影响行数否则 `> OK`，失败输出 `> 错误信息`，并输出耗时、空行；整体失败输出 `results.getErrMsg()` |
- 调用链：`ShellMongoQueryInfoTab.init → init → infoArea.appendLine`

## ShellMongoQueryMainTab
> 文件: cn/oyzh/easyshell/tabs/mongo/query/ShellMongoQueryMainTab.java
- 职责：MongoDB 查询标签页，展示查询图标与标题（未保存加 `*`），委托控制器执行脚本。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | 返回 FXML 路径 | `FXConst.TAB_PATH + "mongo/query/shellMongoQueryMainTab.fxml"` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new QuerySVGGlyph()` 并设默认光标 |
  | `public void flushTitle()` | 刷新标签标题 | 名称为空取 `I18nHelper.newQuery()`；`controller().isUnsaved()` 时前缀 `* `，格式 `名称@库名(连接名)` |
  | `public ShellQuery query()` | 获取查询对象 | `controller().getQuery()` |
  | `public String queryId()` | 获取查询 id | `query().getUid()` |
  | `public ShellMongoDatabaseTreeItem dbItem()` | 获取数据库树节点 | `controller().getDbItem()` |
  | `public String dbName()` | 获取数据库名称 | `dbItem().dbName()` |
  | `public String connectName()` | 获取连接名称 | `dbItem().connectName()` |
  | `public boolean init(ShellQuery query, ShellMongoDatabaseTreeItem item)` | 初始化 | `controller().init(query, item)` 后 `flush()`，恒返回 true |
  | `public ShellMongoQueryMainTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public static ShellMongoQueryMainTab of(ShellQuery query, ShellMongoDatabaseTreeItem item)` | 工厂方法 | `new` 后 `init(query, item)` 返回 |
- 调用链：`ShellMongoTabPane.onMongoQueryAdd/onMongoQueryOpen → init → ShellMongoQueryMainTabController.init`

## ShellMongoQueryMainTabController
> 文件: cn/oyzh/easyshell/tabs/mongo/query/ShellMongoQueryMainTabController.java
- 职责：查询标签页内容控制器，编辑并执行 MongoDB 脚本，在结果面板中展示信息与结果集标签，并支持保存查询。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | query | ShellQuery | 当前查询对象 |
  | unsaved | boolean | 未保存标志位 |
  | dbItem | ShellMongoDatabaseTreeItem | MongoDB 数据库树节点 |
  | queryArea | ShellMongoQueryEditor | FXML 注入，查询脚本编辑器 |
  | resultTabPane | FXTabPane | FXML 注入，结果标签容器 |
  | root | FXVBox | FXML 注入，根节点 |
  | infoTab | ShellMongoQueryInfoTab | FXML 注入，结果信息标签 |
  | splitPane | FXSplitPane | FXML 注入，编辑/结果分割面板 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellQuery getQuery()` | 获取查询对象 | 返回 `query` |
  | `public ShellMongoDatabaseTreeItem getDbItem()` | 获取数据库树节点 | 返回 `dbItem` |
  | `public void init(ShellQuery query, ShellMongoDatabaseTreeItem dbItem)` | 初始化 | `resultTabPane.setProp("query", query)`；赋值 `query`/`dbItem`；`showNode(0)`；`queryArea.setText(query.getContent())`+`forgetHistory()`；监听文本变化置 `unsaved`；`ShellMongoQueryUtil.updateIndex(...)` |
  | `public void initialize(URL url, ResourceBundle resourceBundle)` | 控制器初始化 | `resultTabPane.selectedItemChanged` 时按 id 切换 `showNode(1/2)`；`queryArea.setRunCallback(this::run)` |
  | `private void clearTabs()` | 清理结果标签 | 保留 `infoTab`，其余 `resultTabPane.removeTab(removes)` |
  | `private void run()` | 运行（FXML 事件） | 取选中文本或全文，`StageManager.showMask(() -> doRun(sql))` |
  | `private void doRun(String sql)` | 执行运行 | `dbItem.executeScript(sql)` 取结果；`clearTabs()`、`initInfoTab(results)`；对成功结果建 `initSelectTab` 并 `addTab`，有结果则 `showType=2`；按类型选中结果或首项并 `showNode` |
  | `private void initInfoTab(DBQueryResults<?> results)` | 初始化信息标签 | `infoTab.init(results)` |
  | `private ShellMongoQuerySelectTab initSelectTab(ShellMongoExecuteResult result, String title)` | 初始化结果标签 | `new ShellMongoQuerySelectTab()` 后 `init(title, result, dbItem)`，设 id 与属性 |
  | `private void save()` | 保存查询（FXML 事件） | 名称为空时 `MessageBox.prompt`；写 `query` 的 content/dbName/iid；`isNew()` 走 `ShellQueryStore.INSTANCE.insert` 并 `addQuery`，否则 `update`；成功复位 `unsaved` |
  | `private void queryKeyPressed(KeyEvent e)` | 快捷键（FXML 事件） | `KeyboardUtil.isCtrlS` 调 `save()`，`isCtrlR` 调 `run()` |
  | `private void showNode(int type)` | 显示/隐藏结果面板 | 0 隐藏 `resultTabPane` 且收起分割条；1/2 显示并按 0.3/0.7 展示分割 |
  | `public boolean isUnsaved()` | 是否未保存 | 返回 `unsaved` |
  | `public void destroy()` | 销毁清理 | `queryArea.destroy()` 后 `super.destroy()` |
- 调用链：`run → doRun → dbItem.executeScript → initInfoTab/initSelectTab → resultTabPane.addTab`；`queryArea.setRunCallback → run`

## ShellMongoQuerySelectTab
> 文件: cn/oyzh/easyshell/tabs/mongo/query/ShellMongoQuerySelectTab.java
- 职责：MongoDB 查询结果集标签页，承载单次执行的结果数据，且不可关闭。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | 返回 FXML 路径 | `FXConst.TAB_PATH + "mongo/query/shellMongoQuerySelectTab.fxml"` |
  | `public void init(String title, ShellMongoExecuteResult result, ShellMongoDatabaseTreeItem dbItem)` | 初始化 | `setTitle(title)` 后 `controller().init(result, dbItem)` |
  | `public ShellMongoQuerySelectTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public void initNode()` | 初始化节点 | 先 `setClosable(false)` 再 `super.initNode()` |
  | `public static ShellMongoQuerySelectTab of(String title, ShellMongoExecuteResult result, ShellMongoDatabaseTreeItem dbItem)` | 工厂方法 | `new` 后 `init(...)` 返回 |
- 调用链：`ShellMongoQueryMainTabController.initSelectTab → init → ShellMongoQuerySelectTabController.init`

## ShellMongoQuerySelectTabController
> 文件: cn/oyzh/easyshell/tabs/mongo/query/ShellMongoQuerySelectTabController.java
- 职责：查询结果集标签页控制器，展示脚本/耗时/数量与结果数据表，并支持结果可更新时的增删改与应用/丢弃。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | FXML 注入，根节点（绑定快捷键） |
  | script | FXText | FXML 注入，脚本显示文本 |
  | used | FXText | FXML 注入，耗时显示文本 |
  | count | FXText | FXML 注入，计数显示文本 |
  | recordTable | ShellMongoRecordTableView | FXML 注入，结果数据表 |
  | dbItem | ShellMongoDatabaseTreeItem | 数据库树节点 |
  | result | ShellMongoExecuteResult | 当前执行结果 |
  | add | SVGGlyph | FXML 注入，新增按钮 |
  | delete | SVGGlyph | FXML 注入，删除按钮 |
  | apply | SVGGlyph | FXML 注入，应用按钮 |
  | discard | SVGGlyph | FXML 注入，丢弃按钮 |
  | changeListener | DBStatusListener | 记录变更监听器，变更时 `apply.enable()` |
  | columns | MongoColumns | 字段列表，动态维护 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellMongoExecuteResult result, ShellMongoDatabaseTreeItem dbItem)` | 初始化 | 赋值 `result`/`dbItem`；监听 `dbItem.parentProperty()` 置空 `closeTab()`；`reload()`；`result.isUpdatable()` 时建 `changeListener` 并 `display` add/apply/delete/discard |
  | `private void initDataList()` | 初始化数据列表 | `updateColumns`、`initRecords`、`correctRecords`；写 `script`（`TextUtil.toSingleLine`）、`used`、`initCount(result.getCount())` |
  | `private void initDataListByMask()` | 带遮罩加载 | `StageManager.showMask(this::initDataList)` |
  | `private void updateColumns(List<MongoRecord> records)` | 更新字段 | 汇总列名，缺失列 `copy`+`add`，删除消失列，有变化时 `initColumns` |
  | `private void initCount(int count)` | 更新计数 | `count.text(总数据: count)` |
  | `private void initColumns(MongoColumns columns)` | 初始化列 | 首列 `DBStatusColumn`，其余 `ShellMongoRecordColumn`（标记 2）并 `setColumn` |
  | `private void initRecords(List<MongoRecord> records)` | 初始化记录 | `recordTable.setItem(records)` |
  | `private void correctRecords()` | 纠正记录 | 逐条 `record.correctColumns(this.columns)` |
  | `private void addRecord()` | 添加记录（FXML 事件） | 无最后一项则 `addDocument()`；否则基于其列构建新记录并追加计数 |
  | `private void addDocument()` | 添加文档（FXML 事件） | 空实现 |
  | `private void insertRecord(MongoRecord record)` | 插入记录 | `dbItem.insertCollectionRecord(record)` 后 `record.set_id(_id)` |
  | `private void updateRecord(MongoRecord record)` | 更改记录 | `dbItem.updateCollectionRecord(record)`，成功更新字段与纠正，否则告警 |
  | `private void apply()` | 应用变更（FXML 事件） | 遍历记录，`isCreated` 走 `insertRecord`、`isChanged` 走 `updateRecord`，各 `clearStatus` 后 `apply.disable()` |
  | `private void discard()` | 丢弃变更（FXML 事件） | 新增记录移除，变更记录 `discard()`，`removeItem` 后计数 |
  | `public void reload()` | 刷新记录（FXML 事件） | `StageManager.showMask(this::doReload)` |
  | `private void doReload()` | 刷新业务 | 未保存变更时确认；`dbItem.executeSingleScript(result.getContent())` 重新执行；`initDataListByMask()`、`apply.disable()` |
  | `private void exportRecord()` | 导出记录（FXML 事件） | 从 `getTabPane().getProp("query")` 取查询，构建 `ShellMongoDataExportCollection` 并 `ShellMongoViewFactory.exportData(...)` |
  | `private void deleteRecord()` | 删除记录（FXML 事件） | 确认后 `deleteRecords(选中记录)` |
  | `private void deleteRecords(List<MongoRecord> records)` | 批量删除 | 逐条 `deleteRecord`，成功则计数，否则告警 |
  | `private boolean deleteRecord(MongoRecord record)` | 删除单条 | 新增记录直接删；否则 `dbItem.deleteCollectionRecord(record)==1`，成功 `removeItem` |
  | `public void onTabClosed(Event event)` | 关闭清理 | `super.onTabClosed(event)` 后 `DBStatusListenerManager.removeListener(changeListener)` |
  | `protected void bindListeners()` | 绑定监听器 | `discard` 禁用绑定 `apply`；`apply` 状态切换 `NodeGroupUtil` action2；监听 `recordTable.getItems()` 新增置 `apply.enable()`；选中项变化设可编辑；`setCtrlSAction(this::apply)` 与 `NodeUtil.nodeOnCtrlS` |
- 调用链：`init → reload → doReload → dbItem.executeSingleScript → initDataListByMask → initDataList`；`apply → insertRecord/updateRecord`

## ShellMongoTerminalTab
> 文件: cn/oyzh/easyshell/tabs/mongo/terminal/ShellMongoTerminalTab.java
- 职责：MongoDB 命令行终端标签页，展示终端图标与连接名，委托控制器初始化终端。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellMongoTerminalTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `protected String url()` | 返回 FXML 路径 | `FXConst.TAB_PATH + "mongo/terminal/shellMongoTerminalTab.fxml"` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new TerminalSVGGlyph()` 并设默认光标 |
  | `protected String getTabTitle()` | 标签标题 | `shellConnect().getName()` |
  | `public void init(ShellMongoClient client, String dbName)` | 初始化 | `client` 为 null 时以 `ShellConnect`（未命名连接）+`new ShellMongoClient(connect)` 调 `controller().init(...)`；否则直接 `controller().init(client, dbName)`；随后 `flushTitle()` |
  | `public ShellConnect shellConnect()` | 获取连接 | `controller().shellConnect()` |
  | `public ShellMongoClient client()` | 获取客户端 | `controller().client()` |
  | `public String dbName()` | 获取数据库名称 | `controller().getDbName()` |
  | `public static ShellMongoTerminalTab of(ShellMongoClient client, String dbName)` | 工厂方法 | `new` 后 `init(client, dbName)` 返回 |
- 调用链：`ShellMongoTabPane.onMongoTerminalOpen → init → ShellMongoTerminalTabController.init → terminal.init`

## ShellMongoTerminalTabController
> 文件: cn/oyzh/easyshell/tabs/mongo/terminal/ShellMongoTerminalTabController.java
- 职责：MongoDB 命令行终端标签页控制器，初始化终端面板并在临时会话关闭时释放客户端。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | terminal | MongoTerminalPane | FXML 注入，命令行终端面板 |
  | dbName | String | 数据库名称 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellMongoClient client, String dbName)` | 初始化 | `terminal.init(client, dbName)` 后记录 `dbName` |
  | `public String getDbName()` | 获取数据库名称 | 返回 `dbName` |
  | `protected ShellConnect shellConnect()` | 获取连接 | `terminal.shellConnect()` |
  | `public ShellMongoClient client()` | 获取客户端 | `terminal.getClient()` |
  | `public void onTabClosed(Event event)` | 关闭清理 | `terminal.isTemporary()` 时 `client().close()`，再 `super.onTabClosed(event)` |
- 调用链：`ShellMongoTerminalTab.init → init → terminal.init`；`onTabClosed → terminal.isTemporary → client().close`

## ShellMongoUserViewTab
> 文件: cn/oyzh/easyshell/tabs/mongo/user/ShellMongoUserViewTab.java
- 职责：MongoDB 用户标签页，展示用户图标与标题，委托控制器展示用户信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | 返回 FXML 路径 | `FXConst.TAB_PATH + "mongo/user/shellMongoUserViewTab.fxml"` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new UserSVGGlyph()` 并设默认光标 |
  | `public void flushTitle()` | 刷新标签标题 | `用户名@库名(连接名)` |
  | `public String dbName()` | 获取数据库名称 | `dbItem().dbName()` |
  | `public String connectName()` | 获取连接名称 | `dbItem().connectName()` |
  | `public String userName()` | 获取用户名 | `controller().getMongoUser().getUser()` |
  | `public ShellMongoDatabaseTreeItem dbItem()` | 获取数据库树节点 | `controller().getDbItem()` |
  | `public void init(MongoUser user, ShellMongoDatabaseTreeItem dbItem)` | 初始化 | `controller().init(user, dbItem)` 后 `flush()` |
  | `public ShellMongoUserViewTabController controller()` | 获取控制器 | 强转 `super.controller()` |
- 调用链：`ShellMongoTabPane.onMongoUserView → init → ShellMongoUserViewTabController.init`

## ShellMongoUserViewTabController
> 文件: cn/oyzh/easyshell/tabs/mongo/user/ShellMongoUserViewTabController.java
- 职责：用户视图标签页控制器，展示用户名、所属库并按库聚合展示角色。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | user | ReadOnlyTextField | FXML 注入，用户名文本 |
  | database | ReadOnlyTextField | FXML 注入，数据库文本 |
  | roleTableView | FXTableView\<MongoUserRoleDb\> | FXML 注入，角色表格 |
  | mongoUser | MongoUser | Mongo 用户对象 |
  | dbItem | ShellMongoDatabaseTreeItem | 数据库树节点 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public MongoUser getMongoUser()` | 获取 Mongo 用户 | 返回 `mongoUser` |
  | `public ShellMongoDatabaseTreeItem getDbItem()` | 获取数据库树节点 | 返回 `dbItem` |
  | `public void init(MongoUser user, ShellMongoDatabaseTreeItem dbItem)` | 初始化并渲染 | 赋值后遍历 `user.getRoles()`，按 `role.getDb()` 聚合为 `MongoUserRoleDb` 并 `roles().add(role.getRole())`；`user`/`database` 设文本，`roleTableView.setItem(dbs.values())` |
- 调用链：`ShellMongoUserViewTab.init → init → MongoUser.getRoles → roleTableView.setItem`
