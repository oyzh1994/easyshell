# MySQL 标签页（首页/函数/存储过程/表/视图）

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

## ShellMysqlRecordSelectTabController（tabs.mysql.query）
> 说明：MySQL 查询结果标签页控制器见上文「ShellMysqlQuerySelectTabController」；本组无此独立类。
