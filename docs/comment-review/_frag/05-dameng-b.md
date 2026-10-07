# 达梦数据库标签页（首页/存储过程/表/视图）

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
