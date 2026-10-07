# EasyShell `fx` 包代码审查文档（非图标类）

> 范围：`src/main/java/cn/oyzh/easyshell/fx/` 递归全部 Java 类。
> 本文件覆盖除 SVG 图标类以外的全部类；SVG 图标类见 `fx-icons.md`。
> 基于真实源码整理，标识符保留原文，说明为中文。
> 已跳过的整文件注释死代码（16 个）：`WelcomeTitle`、`s3/ShellS3RegionCombobox`、`s3/ShellS3RegionTextFieldSkin`、`file/ShellFileConnectComboBox`、`mysql/ShellMysqlStatusTableView`、`smb/ShellSMBUserTextFieldSkin`、`mongo/ShellMongoColumnComboBox`、`mongo/ShellMongoConditionComboBox`、`mongo/ShellMongoCodeTextFiled`、`dameng/table/DBEnumTextFiled`、`dameng/view/DamengViewAlgorithmComboBox`、`dameng/view/DamengViewCheckOptionComboBox`、`mysql/table/ShellMysqlFiledTypeComboBox`、`mysql/table/ShellMysqlJoinSymbolComboBox`、`mysql/table/ShellMysqlColumnComboBox`、`mysql/table/ShellMysqlConditionComboBox`。

---

## 一、`cn.oyzh.easyshell.fx`（根包）

### ShellDataEditor
- 职责：Shell 数据编辑器，提供编辑器字体来源。
- 字段：无。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `Font getEditorFont()` | 返回编辑器字体 | `FontManager.toFont(ShellSettingStore.SETTING.editorFontConfig())` |
- 调用链：`ShellDataEditor.getEditorFont → ShellSettingStore.SETTING.editorFontConfig() → FontManager.toFont`

### ShellDiskInfoTableView
- 职责：磁盘信息表格视图（展示 `ShellSSHDiskInfo`）。
- 字段：无。
- 方法：`init` 代码块调用 `TableViewUtil.copyCellDataOnDoubleClicked(this)`，双击复制单元格内容。
- 调用链：`ShellDiskInfoTableView → FXTableView<ShellSSHDiskInfo>`

### ShellOsTypeComboBox
- 职责：Shell 系统、协议、应用类型选择框，带图标显示。
- 字段：无。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initNode()` | 初始化选项与单元格工厂 | 依次 `addItem` 系统名（Ubuntu/Centos/.../Windows）、`ShellPrototype` 各协议常量（SFTP/FTP/S3/.../DAMENG）、云厂商（Alibaba/Tencent/Huawei Cloud）；设置 `cellFactory`/`buttonCell`，`updateItem` 中调用 `getGlyph(item)` 作为 `graphic` |
  | `void selectType(String type)` | 按类型选中对应项 | 对各 `ShellPrototype` 常量 `equalsIgnoreCase` 后 `super.select(...)`，否则 `super.select(type)` |
  | `static SVGGlyph getGlyph(String name)` | 名称→图标 | 空名返回 `LinuxSVGGlyph`；`switch(name.toLowerCase())` 映射到 OS/协议/云/redis/zk/mysql/mongo/mosh/dameng 图标，默认 `LinuxSVGGlyph` |
- 调用链：`ShellOsTypeComboBox.initNode → getGlyph → 各 xxxSVGGlyph`；被外部按名称取图标：`ShellOsTypeComboBox.getGlyph(name) → xxxSVGGlyph`

### ShellShortcutKeyTableView
- 职责：展示终端快捷键列表（`KeyValueProperty<String,Object>`）。
- 字段：无。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initNode()` | 构建快捷键说明数据 | 依据 `OSUtil.isMacOS()` 分别加入 Meta/⌘ 或 Ctrl+Shift 组合，文案取自 `ShellI18nHelper.termTip1..15`、`I18nHelper.*`；`setItem(data)`、`setFixedCellSize(22)` |

---

## 二、`fx.connect`

### ShellConnectTextField
- 职责：连接输入框，支持搜索过滤（继承 `SelectTextFiled<ShellConnect>`）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `connects` | `List<ShellConnect>` | 当前连接列表 |
  | `filterMode` | `String` | 过滤模式（ssh/file/term/zk/redis/mysql/mongo/dameng/all），默认 `"ssh"` |
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected boolean onTextChanged(String newValue)` | 文本变化时过滤 | 空串时恢复全部并 `skin().showPopup()` 返回 false；否则按 `ShellConnect.getName` 忽略大小写过滤，命中为空 `hidePopup`，否则 `showPopup` |
  | `void removeItem(ShellConnect connect)` | 移除某连接 | 按 id 移除后刷新 `skin().setItemList` |
  | `String getFilterMode()/void setFilterMode(String)` | 读写过滤模式 | 设置后若 `connects!=null` 触发 `loadConnects()` |
  | `protected void loadConnects()` | 按模式加载连接 | 走 `ShellConnectStore.INSTANCE` 的 `loadSSHType/loadFileType/loadTermType/loadRedisType/loadZKType/loadMysqlType/loadMongoType/loadDamengType/load` |
  | `void initNode()` | 初始化 | `loadConnects()`→提示文本→skin 转换器取 `getName` |
- 调用链：`setFilterMode → loadConnects → ShellConnectStore.INSTANCE.loadXxxType`；`输入 → onTextChanged → skin.show/hidePopup`

---

## 三、`fx.db`

### ShellDBEnumTextFiled
- 职责：数据库字段枚举值输入框（点击弹窗编辑枚举列表）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `values` | `List<String>` | 枚举值列表 |
  | `popup` | `PopupAdapter` | 枚举值选择弹窗 |
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDBEnumTextFiled()` / `ShellDBEnumTextFiled(List<String> values)` | 构造器 | 后者保存 values |
  | `protected void initPopup()` | 打开弹窗 | `PopupManager.parsePopup(ShellDBColumnEnumPopupController.class)`，设 `values` 与 `onSubmit`（回读 listView 文本，`initText()`）后 `showPopup` |
  | `void initText()` | 按枚举列表生成文本 | 拼接 `,'v'` 形式并去掉首个逗号，`setText` |
  | `void setValues(List<String>)` | 设置枚举列表 | 更新列表、listView 与文本 |
  | `protected FXListView<ClearableTextField> listView()` | 取弹窗内列表 | `popup.content().lookup("#listView")` |
  | `void initNode()` | 初始化 | `setAction(this::initPopup)`，设置 prompt |
- 调用链：`点击 → initPopup → 弹窗 onSubmit → listView() → initText`

---

## 四、`fx.dameng`

### ShellDamengSchemaComboBox
- 职责：达梦数据库模式（schema）选择框。
- 字段：无。
- 方法：`void init(ShellDamengClient)` / `void init(ShellDamengClient,String schema)`：`clearItems`→`client.selectSchemas()`→`setItem(names)`；有 schema 则 `select`。

### ShellDamengSecurityTypeComboBox
- 职责：达梦安全类型下拉框。
- 方法：`initNode()` 加入 `DEFINER`/`CURRENT_USER`；重写 `select` 大写化。

### DamengRecordColumn
- 职责：达梦记录表格列，列头展示字段名/类型/注释，带右键菜单。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `column` | `final DamengColumn` | 字段元信息 |
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DamengRecordColumn(DamengColumn)` / `(DamengColumn,boolean showComment)` | 构造器 | `setCellValueFactory(p->p.getValue().getProperty(name))`；`initContent` 构建 `FXVBox` 列头 |
  | `private FXVBox initContent(boolean)` | 构建列头 | 名称(粗体)、类型(绿)、注释(灰)；右键 `showContextMenu(getMenuItems())`；`heightProperty` 监听实时更新表头高度 |
  | `List<? extends MenuItem> getMenuItems()` | 右键菜单 | `columnInfo`（弹 `ShellDamengFieldInfoPopupController`）、`copyColumnName` |
  | `private void showColumnInfo()/copyColumnName()` | 显示信息/复制名 | 弹窗 / `ClipboardUtil.copy(getName())` |
  | `String getName()/getType()/Integer getSize()/boolean supportSize()` | 元信息 | 委托 `column` |
  | `void initNode()` | 初始化 | `showGraphicOnlyLater()` |
- 调用链：`列头右键 → getMenuItems → columnInfo → ShellDamengFieldInfoPopupController`

### DamengRecordTableRow
- 职责：达梦记录表格行（`FXTableRow<DamengRecord>` 空实现）。

### DamengRecordTableView
- 职责：达梦记录表格视图。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean hasProperty(DamengRecordProperty)` | 是否含某属性 | 遍历 `getItems()` 调 `record.hasProperty` |
  | `boolean hasRecord(DamengRecord)` | 是否含记录 | `getItems().contains(record)` |
  | `void initNode()` | 初始化 | 多选、`setRowFactory(DamengRecordTableRow)`、`destroyItemsOnRemoved()` |

### DamengParamModeComboBox
- 职责：达梦存储过程参数模式下拉框。方法：`initNode()` 加 `IN`/`OUT`/`IN/OUT`。

### DamengColumnListView
- 职责：达梦字段多选列表（`FXListView<FXCheckBox>`）。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(List<DamengColumn>)` / `init(List<DamengColumn>,List<String> selectedColumns)` | 初始化勾选项 | 每字段生成 `FXCheckBox`，`setProp("column",column)`，`ListViewUtil.selectRowOnMouseClicked` |
  | `List<DamengColumn> getSelectedColumns()` | 取已勾选字段 | 过滤 `isSelected` 后取 prop |
  | `List<String> getSelectedColumnNames()` | 取已勾选字段名 | 映射 `getName` |
  | `void select(Collection<String>)` | 勾选指定名 | 忽略大小写匹配后 `setSelected(true)` |

### DamengDefaultValueTextFiled
- 职责：达梦字段默认值输入框（`SelectTextFiled<String>`）。
- 字段：`boolean editableFlag` 是否允许编辑。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(DamengColumn)` / `init(DamengColumn,String defaultValue)` | 初始化 | 加 `""`/`EMPTY STRING`/`NULL`；有默认值可编辑并 setText，否则不可编辑选中 `NULL` |
  | `String getValue()` | 取默认值 | 可编辑取文本；`NULL`→null，`EMPTY STRING`→"" |
- 调用链：`init → getValue`

### DamengFieldTextFiled
- 职责：达梦字段选择输入框（弹窗多选列）。
- 字段：`columns`(`List<DamengColumn>`)、`selectedColumns`(`List<String>`)、`popup`(`PopupAdapter`)。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void initPopup()` | 弹窗选择 | `ShellDamengColumnFieldPopupController`，onSubmit 回读 `getSelectedColumnNames` |
  | `void setColumns/setSelectedColumns` | 设置数据 | 更新 listView 与文本 |
  | `List<String> getSelectedColumns()` | 取已选 | `Objects.requireNonNullElse(...)` |
  | `protected void initText()` | 刷新显示 | `CollectionUtil.join(selectedColumns,",")` |
  | `protected DamengColumnListView listView()` | 取弹窗列表 | `lookup("#listView")` |

### DamengForeignKeyPolicyComboBox
- 职责：达梦外键删除策略下拉框。方法：`init` 块加 `CASCADE`/`NO ACTION`/`RESTRICT`/`SET NULL`。

### DamengIndexColumnListView
- 职责：达梦索引字段选择列表（`FXListView<FXHBox>`）。
- 字段：`List<String> columnNames`。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(DamengIndex dbIndex,List<DamengColumn> columnList)` | 初始化 | 记录字段名集合，遍历 `dbIndex.getColumns()` 逐列 `addColumn` |
  | `void addColumn(DamengIndex.IndexColumn column)` | 添加一行 | 生成 `FXComboBox<String>`（300×25），选中列名，`addItem(comboBox)` |
  | `List<DamengIndex.IndexColumn> getColumns()` | 读取结果 | 遍历 items，取 comboBox 值构造 `IndexColumn` |

### DamengIndexFieldTextFiled
- 职责：达梦索引字段选择框。
- 字段：`dbIndex`(`DamengIndex`)、`columnList`(`List<DamengColumn>`)、`columns`(`List<DamengIndex.IndexColumn>`)、`popup`(`PopupAdapter`)。
- 方法：`initPopup()`（`ShellDamengIndexFieldPopupController`，弹窗隐藏时 enable 并复位按钮颜色）、`setColumns`、`initText`（列名拼接）、`listView()`、`getColumns()`、构造器 `(DamengIndex,List<DamengColumn>,List<DamengIndex.IndexColumn>)`。

### DamengIndexMethodComboBox
- 职责：达梦索引方法下拉框。方法：`init` 块加 `""`/`BTREE`/`HASH`。

### DamengIndexTypeComboBox
- 职责：达梦索引类型下拉框。方法：`initNode()` 加 `NORMAL`/`UNIQUE`。

### DamengTableComboBox
- 职责：达梦数据表下拉框。方法：`init(String schema,ShellDamengClient)` / `init(String,String tableName,ShellDamengClient)`：`client.selectTables(schema)`→setItem→可选 select。

### DamengTableSpaceComboBox
- 职责：达梦表空间下拉框。方法：`init(ShellDamengClient)`：`client.tableSpaces()` 逐项 addItem。

### DamengTriggerPolicyComboBox
- 职责：达梦触发器策略下拉框。方法：`init` 块加 BEFORE/AFTER INSERT/UPDATE/DELETE。

---

## 五、`fx.docker`

### ShellDockerContainerStatusComboBox
- 职责：docker 状态选择框。方法：`init` 块加 `running`/`all`/`stopped`（I18n）。

### ShellDockerContainerTableView
- 职责：Docker 容器表格视图，带容器全生命周期操作（启停/杀/暂停/重命名/日志/资源/端口/审查）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `exec` | `ShellDockerExec` | docker 执行器 |
  | `status` | `byte` | 容器状态（0 运行 / 1 全部 / 2 已退出） |
  | `containers` | `List<ShellDockerContainer>` | 容器全量列表 |
  | `filterText` | `String` | 过滤文本 |
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initEvenListener()` | 事件 | 右键菜单 + 快捷键（run/delete/stop/rename/pause） |
  | `void setStatus(byte)` | 切状态 | 状态变化后 `StageManager.showMask(loadContainer)` |
  | `void loadContainer()` | 加载容器 | 按 status 调 `exec.docker_ps/_a/_exited` → `ShellDockerParser.ps` → `setItem(doFilter)` |
  | `void setFilterText/refreshContainer/doFilter` | 过滤 | 按 containerId/image/names 忽略大小写过滤 |
  | `void deleteContainer(ShellDockerContainer,boolean force)` | 删除 | `docker_rm_f`/`docker_rm`，移除并刷新 |
  | `void startContainer/stopContainer/killContainer/restartContainer/pauseContainer/unpauseContainer` | 生命周期 | 各自 `exec.docker_xxx`，成功后 `loadContainer` |
  | `void containerInspect(container)` | 审查 | `docker_inspect` → `ShellViewFactory.dockerInspect` |
  | `void containerResource()` | 资源 | `docker_resource` → `ShellDockerParser.resource` → `ShellViewFactory.dockerResource` |
  | `void containerLogs()` | 日志 | 子线程 `docker_logs`，`DownLatch` 最多等 30s，超 512KB 提示过大，`ShellViewFactory.dockerLogs` |
  | `void renameContainer(container)` | 重命名 | `docker_rename` 后刷新 |
  | `void containerPorts()` | 端口 | `docker_port` → `ShellDockerParser.port` → `ShellViewFactory.dockerPort` |
  | `List<? extends MenuItem> getMenuItems()` | 右键菜单 | 依容器状态动态组装（启动/停止/杀/重启/暂停/恢复/日志/重命名/删除/强删/保存） |
  | `void saveContainer(container)` | 保存 | `ShellViewFactory.commitContainer` |
- 调用链：`右键 → getMenuItems → startContainer → exec.docker_start → loadContainer → ShellDockerParser.ps`
- 调用链：`containerLogs → exec.docker_logs → ShellViewFactory.dockerLogs`

### ShellDockerImageHistoryTableView
- 职责：Docker 镜像历史表格视图。代码块 `copyCellDataOnDoubleClicked`。

### ShellDockerImageTableView
- 职责：Docker 镜像表格视图，含运行/删除/保存/打标签/审查/历史。
- 字段：`exec`(`ShellDockerExec`)、`images`(`List<ShellDockerImage>`)、`filterText`(`String`)。
- 方法：`loadImage()`（`docker_images`→`ShellDockerParser.images`→`doFilter`）、`setFilterText/refreshImage/doFilter`、`deleteImage(image,force)`（`ShellDockerRmi`，失败提示）、`runImage/saveImage/tagImage`（`ShellViewFactory.xxx`）、`imageInspect`、`imageHistory`（`ShellDockerParser.history`→`ShellViewFactory.dockerHistory`）、`getMenuItems()`、`initEvenListener()`。

### ShellDockerPortTableView / ShellDockerRunEnvTableView / ShellDockerRunLabelTableView / ShellDockerRunPortTableView / ShellDockerRunVolumeTableView
- 职责：分别为 Docker 端口映射、运行环境变量、运行标签、运行端口、运行卷挂载的表格视图（前两者带双击复制，其余为空实现）。

---

## 六、`fx.file`

### ShellFileDownloadTaskTableView
- 职责：文件下载任务表格视图。
- 方法：`initNode()`（多选）、`initEvenListener()`（右键 + `TableViewMouseSelectHelper.install`）、`getMenuItems()`（取消/重试/错误，错误项仅单条失败时可用）、`cancel/retry/errorInfo`。

### ShellFileLocationTextField
- 职责：文件路径输入框（可跳转、带历史与收藏下拉）。
- 字段：`fileCollectSupplier`(`Supplier<List<ShellFileCollect>>`)、`tempLocations`(`final List<String>`，历史)。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void text(String)` | 设置路径 | 去空白、`ShellFileUtil.fixFilePath`，去尾 `/`；更新 `tempLocations`（先删后加保序） |
  | `ShellFileLocationTextFieldSkin skin()/createDefaultSkin()` | 皮肤 | 注入 `itemList` 供应器 |
  | `setOnJumpLocation/getOnJumpLocation` | 跳转回调 | 委托 skin |
  | `private List<String> itemList()` | 下拉数据 | 保留最近 20 条 + 收藏项去重 |
- 调用链：`text → ShellFileUtil.fixFilePath`；`皮肤 → itemListSupplier`

### ShellFileLocationTextFieldSkin
- 职责：文件路径输入框皮肤（继承 `SelectTextFiledSkin<String>`）。
- 字段：`onJumpLocation`(`Consumer<String>`)、`itemListSupplier`(`Supplier<List<String>>`)。
- 方法：`set/getOnJumpLocation`、`protected void onJumpLocation(String)`、构造器（选中项变化与 ENTER 键触发跳转）、`getItemList()`（用 supplier 刷新）。

### ShellFileSizeTableColumn
- 职责：文件大小表格列（`FXTableColumn<ShellFile,Long>`）。
- 方法：`cellFactory()` 自定义 `FXTableCell`，`updateItem` 里用 `file.getFileSizeDisplay()` 显示；`initNode()` 设置 cellFactory。

### ShellFileTableView
- 职责：文件表格视图**抽象基类**（泛型 `C extends ShellFileClient<E>, E extends ShellFile`），负责加载/过滤/排序/进入目录/上传下载/删除/重命名等通用文件操作。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `enabledLoading` | `boolean` | 是否显示加载遮罩（默认 true） |
  | `mouseSelectHelperRef` | `WeakReference<TableViewMouseSelectHelper>` | 鼠标多选辅助类弱引用 |
  | `filterText` | `protected String` | 过滤内容 |
  | `showHiddenFile` | `protected boolean` | 是否显示隐藏文件 |
  | `client` | `protected C` | 文件客户端 |
  | `locationProperty` | `private StringProperty` | 当前位置属性 |
  | `files` | `protected List<E>` | 真实文件全量列表 |
  | `loading` | `final AtomicBoolean` | 加载中标志 |
  | `interrupt` | `final AtomicBoolean` | 加载中断标志 |
- 方法（关键）：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initNode()` | 初始化 | 多选 |
  | `void initEvenListener()` | 事件 | 右键菜单；`TableViewMouseSelectHelper.install`；`MOUSE_CLICKED`→`onMouseClicked`；快捷键 delete/rename/refresh |
  | `void loadFile()` | 加载文件 | 防重入；`clearItems`→`loadFileInnerBatch`→`refresh`；`enabledLoading` 决定遮罩或子线程 |
  | `protected synchronized void loadFileInnerBatch()` | 批量加载 | 校正工作目录，`client.lsFileBatch(path, this::addFile, 10)` |
  | `private void addFile(E)/addFile(List<E>)` | 追加文件 | 中断检查，`files.addAll`→`sortFile`→`setItem(doFilter)` |
  | `void refreshFile()/reloadFile()` | 刷新/重载 | 依 `files` 是否为空决定 loadFile 或重过滤 |
  | `protected List<E> doFilter(List<E>)` | 过滤+排序 | `filterFile` 过滤；`getFileOrder` 目录优先，再按 `getFileName` 排序 |
  | `protected boolean filterFile(E)` | 单项保留判定 | 排除当前文件、根目录下的返回目录、隐藏文件、不匹配过滤文本 |
  | `void onMouseClicked(MouseEvent)` | 鼠标 | 后退键 back；前进键 forward；双击目录 `intoDir`，文件 `viewFile`（SFTP 链接先 `realpath`） |
  | `void intoDir(E)/intoDir(String)` | 进入目录 | 取消删除任务、中断加载、`setLocation`+`loadFile` |
  | `void back()/forward()/returnDir()/intoHome()` | 导航 | 父目录/选中目录/上一级/home |
  | `void deleteFile(List<E>)` | 删除 | 确认后 `client.doDelete` 并取消收藏，`refreshFile` |
  | `void renameFile(List<E>)` | 重命名 | 输入新名，`client.rename` 后刷新 |
  | `void viewFile/editFile/touch/createDir` | 查看/编辑/建文件/建目录 | 委托 `ShellViewFactory` 或 `client` |
  | `void onFileAdded(String)/onFileSaved/onFileDeleted(String)` | 文件事件 | 同目录判断后增删，`client.fileInfo` |
  | `void uploadFile(...)` 多重载 / `uploadFolder()` | 上传 | 冲突确认后 `client.doUpload` |
  | `void downloadFile(List<E>)` | 下载 | 选择目录、冲突确认后 `client.doDownload` |
  | `List<? extends MenuItem> getMenuItems()` | 右键菜单 | 依 `isSupportXxxAction` 组装（touch/mkdir/edit/view/collect/fileInfo/copyPath/rename/permission/refresh/delete） |
  | `protected Menu initUploadMenu()` | 上传菜单 | 文件/文件夹子菜单 |
  | `long totalSize()/String fileInfo()` | 汇总 | 统计总大小与条数 |
  | `void destroy()` | 销毁 | 解绑 locationProperty |
- 子类通过覆写 `isSupportTouchAction/isSupportMkdirAction/isSupportFileInfoAction/isSupportDownloadAction/isSupportUploadAction/isSupportRenameAction/isSupportRenameDirAction/isSupportDeleteAction/isSupportPermissionAction` 裁剪菜单。
- 调用链：`loadFile → loadFileInnerBatch → client.lsFileBatch → addFile → doFilter → setItem`
- 调用链：`onMouseClicked → intoDir/viewFile → loadFile/ShellViewFactory.fileView`

### ShellFileTransportFileTableView
- 职责：文件传输文件表格视图（继承 `ShellFileTableView<ShellFileClient<ShellFile>,ShellFile>`）。
- 字段：`transportCallback`(`Consumer<List<ShellFile>>`)。
- 方法：`setClient()`（监听 `deleteTasks`，成功删除后 `onFileDeleted`）、`getMenuItems()`（在父菜单前加“传输文件”）、`private void transportFile(List<ShellFile>)`（过滤无效文件后回调）。

### ShellFileTransportTaskTableView
- 职责：文件传输任务表格视图。
- 方法：`initNode()`（多选）、`initEvenListener()`、`getMenuItems()`（取消/重试/错误）、`cancel(List)/retry(List)/errorInfo(task)`、`cancel()`。

### ShellFileUploadTaskTableView
- 职责：文件上传任务表格视图。方法与下载任务表一致：`initNode/initEvenListener/getMenuItems/cancel/retry/errorInfo`。

---

## 七、`fx.ftp`

### ShellFTPFileTableView
- 职责：FTP 文件表格视图（继承 `ShellFileTableView<ShellFTPClient,ShellFTPFile>`）。
- 字段：`uploadTaskListener`、`deleteTaskListener`（`ListChangeListener`，任务成功移除后 `onFileAdded`/`onFileDeleted`）。
- 方法：`setClient()`（注册上传/删除任务监听）、`getMenuItems()`（父菜单 + 上传菜单 + 下载）、`destroy()`（移除监听并置空）。
- 调用链：`setClient → client.uploadTasks().addListener → onFileAdded`

---

## 八、`fx.jump`

### ShellJumpTableView
- 职责：跳转配置表格视图（`FXTableView<ShellJumpConfig>`）。
- 方法：`void updateOrder()`：按行序 `config.setOrder(i)`；代码块双击复制。

---

## 九、`fx.key`

### ShellKeyComboBox
- 职责：Shell 密钥选择框（`FXComboBox<ShellKey>`）。
- 方法：`getKeyId()`（选中项 id）、`selectById(String)`（按 id 匹配选中）、`initNode()`（转换器取 name，`ShellKeyStore.INSTANCE.selectList()`）。

### ShellKeyLengthComboBox
- 职责：Shell 密钥长度选择框（`FXComboBox<Integer>`）。
- 方法：`void init(String keyType)`：按 RSA/ED25519/ECDSA/DSA 填充长度并默认选中。

### ShellKeyTableView
- 职责：密钥表格视图（多选），支持删除/重命名/导出/复制到主机。
- 字段：`keyStore`(`final ShellKeyStore`)。
- 方法：`initNode()`（多选）、`initEvenListener()`（右键 + 鼠标多选）、`getMenuItems()`（复制到主机/删除/编辑/重命名/导出）、`deleteKey(List)`、`renameKey(ShellKey)`（改名后 `keyStore.update`，失败回滚）、`exportKey(ShellKey)`（写私钥/公钥文件）。

### ShellKeyTypeComboBox
- 职责：Shell 密钥类型选择框。
- 方法：`init` 块加 RSA/ED25519/ECDSA/DSA；`isRsaType/isEd25519Type/isEcdsaType/isDsaType` 按序号判断；`static String getTypeName(String type)` 由 `KeyPairProvider` 常量映射类型名。

---

## 十、`fx.mongo`

### ShellMongoBucketFileTableView
- 职责：MongoDB 桶（GridFS）文件表格视图，管理文档的查看/编辑/重命名/删除/上传/下载。
- 字段：`uploadTaskListener`、`deleteTaskListener`、`dbName`(`String`)、`bucketName`(`String`)。
- 方法：`setClient()`（注册任务监听）、`setDbName/setBucketName`、`initEvenListener()`、`getMenuItems()`（上传/下载/查看/编辑/编辑文档/重命名/删除）、`editDocument(MongoBucketFile)`（弹窗改文档，`client.updateBucketRecord`）、`uploadFile`、`getLocation()`（`dbName@bucketName`）、重写 `setItem/addItem/removeItem/refreshFile` 维护内部 `files`、`initNode/destroy`。

### ShellMongoCodeTextFiledSkin
- 职责：MongoDB 代码文本输入框皮肤（继承 `LongTextFiledSkin`），格式类型 `SQL`。

### ShellMongoCollectionComboBox
- 职责：MongoDB 集合选择框。方法：`init(String dbName,ShellMongoClient)` / `init(dbName,tableName,client)`（`client.listCollections`→setItem→可选 select/clearChild）。

### ShellMongoDatabaseComboBox
- 职责：MongoDB 数据库选择框。方法：`init(ShellMongoClient)` / `init(client,dbName)`（`client.listDatabaseNames`→setItem→可选 select）。

### ShellMongoRecordColumn
- 职责：MongoDB 记录表格列，列头展示字段名/类型，带复制字段名菜单。
- 字段：`column`(`final MongoColumn`)。
- 方法：构造器 `(MongoColumn)`/`(MongoColumn,int mode)`（mode 0 仅文本，否则构建 VBox 列头）、`private FXVBox initContent()`、`getMenuItems()`（复制字段名）、`getName/getType/getFont`。

### ShellMongoRecordTableRow
- 职责：MongoDB 记录表格行（空实现）。

### ShellMongoRecordTableView
- 职责：MongoDB 记录表格视图。方法：`hasProperty(MongoRecordProperty)`、`hasRecord(MongoRecord)`、`initNode()`（多选/行工厂/`destroyItemsOnRemoved`）。

---

## 十一、`fx.mysql`

### ShellMysqlCharsetComboBox
- 职责：MySQL 字符集下拉框。方法：`init(ShellMysqlClient)`（先加空项，再 `client.charsets()` 大写）；`select` 大写化。

### ShellMysqlCollationComboBox
- 职责：MySQL 排序规则下拉框。方法：`init(charset,client)`（charset 变化时才用 `client.collation(charset)` 重载）；`select` 大写化。

### ShellMysqlDatabaseComboBox
- 职责：MySQL 数据库下拉框。方法：`init(client)` / `init(client,dbName)`（`client.databases()`→setItem→可选 select）。

### ShellMysqlSecurityTypeComboBox
- 职责：MySQL 安全类型下拉框。方法：`init` 块加 `DEFINER`/`INVOKER`；`select` 大写化。

### ShellMysqlRecordColumn
- 职责：MySQL 记录表格列，列头展示字段名/类型(带长度)/注释，带字段信息与复制名菜单。
- 字段：`column`(`final MysqlColumn`)。
- 方法：构造器 `(MysqlColumn)`/`(MysqlColumn,boolean showComment)`、`initContent(showComment)`、`getMenuItems()`（`columnInfo` 弹 `ShellMysqlFieldInfoPopupController` / `copyColumnName`）、`getName/getType/supportSize/getSize`、`initNode()`。

### ShellMysqlRecordTableRow
- 职责：MySQL 记录表格行（空实现）。

### ShellMysqlRecordTableView
- 职责：MySQL 记录表格视图。方法：`hasProperty(MysqlRecordProperty)`、`hasRecord(MysqlRecord)`、`initNode()`。

### ShellMysqlCharacteristicCombobox
- 职责：MySQL 存储程序特性下拉框。方法：`init` 块加 LANGUAGE SQL/CONTAINS SQL/DETERMINISTIC/NO SQL/READS|MODIFIES SQL DATA/SQL SECURITY DEFINER|INVOKER；`select` 大写化。

### ShellMysqlParamModeComboBox
- 职责：MySQL 存储过程参数模式下拉框。方法：`init` 块加 `IN`/`OUT`/`INOUT`。

### ShellMysqlColumnListView
- 职责：MySQL 字段选择列表（`FXListView<FXCheckBox>`）。
- 方法：`init(List<MysqlColumn>)` / `init(columns,selectedColumns)`（每字段 FXCheckBox，`setProp("column")`）、`getSelectedColumns()`、`Set<String> getSelectedColumnNames()`、`select(Collection<String>)`。

### ShellMysqlDefaultValueTextFiled
- 职责：MySQL 字段默认值输入框。
- 字段：`editableFlag`(`boolean`)。
- 方法：`init(MysqlColumn)` / `init(column,defaultValue)`：若 `supportEnum` 用枚举值列表（含 NULL）并可监听列值变化刷新；否则加 `""`/`EMPTY STRING`/`NULL`。`getValue()` 处理 NULL/EMPTY STRING 语义。`initNode()` 注册选中项变化回调控制可编辑状态。

### ShellMysqlEngineComboBox
- 职责：MySQL 存储引擎下拉框。方法：`init(client)`（`client.engines()` 大写）、`select` 大写化、`isInnoDB()`。

### ShellMysqlFieldTextFiled
- 职责：MySQL 字段选择输入框（弹窗多选，`Set<String>` 承载已选）。
- 字段：`columns`(`List<MysqlColumn>`)、`selectedColumns`(`Set<String>`)、`popup`(`PopupAdapter`)。
- 方法：`initPopup()`（`ShellMysqlColumnFieldPopupController`）、`setColumns/setSelectedColumns/getSelectedColumns/initText/listView/initNode`。

### ShellMysqlForeignKeyPolicyComboBox
- 职责：外键策略选择框。方法：`initNode()` 加 CASCADE/NO ACTION/RESTRICT/SET NULL。

### ShellMysqlIndexColumnListView
- 职责：MySQL 索引字段选择列表（`FXListView<FXHBox>`，每行 列下拉 + 子部分长度数字框）。
- 字段：`columnNames`(`List<String>`)。
- 方法：`init(MysqlIndex,List<MysqlColumn>)`、`addColumn(MysqlIndex.IndexColumn)`（FXComboBox + NumberTextField 组成 FXHBox）、`getColumns()`。

### ShellMysqlIndexFieldTextFiled
- 职责：索引字段文本框。
- 字段：`dbIndex`(`MysqlIndex`)、`columnList`(`List<MysqlColumn>`)、`columns`(`List<MysqlIndex.IndexColumn>`)、`popup`(`PopupAdapter`)。
- 方法：`initPopup()`（`ShellMysqlIndexFieldPopupController`，含隐藏复位）、`setColumns`、`initText`（列名+子部分）、`listView()`、`getColumns()`、`initNode()`。

### ShellMysqlIndexMethodComboBox
- 职责：索引方法选择框。方法：`initNode()` 加 `""`/`BTREE`/`HASH`。

### ShellMysqlIndexTypeComboBox
- 职责：索引类型选择框。方法：`initNode()` 加 NORMAL/UNIQUE/FULLTEXT/SPATIAL。

### ShellMysqlRowFormatComboBox
- 职责：行格式下拉框。方法：`initNode()` 加 COMPACT/COMPRESSED/DEFAULT/DYNAMIC/FIXED/REDUNDANT；`select` 大写化。

### ShellMysqlTableComboBox
- 职责：数据表选择框。方法：`init(dbName,client)` / `init(dbName,tableName,client)`（`client.selectTables(dbName)`）。

### ShellMysqlTriggerPolicyComboBox
- 职责：触发器策略选择框。方法：`initNode()` 加 BEFORE/AFTER INSERT/UPDATE/DELETE。

### ShellMysqlViewAlgorithmComboBox
- 职责：视图算法下拉框。方法：`init` 块加 UNDEFINED/MERGE/TEMPTABLE；`select` 大写化。

### ShellMysqlViewCheckOptionComboBox
- 职责：视图检查选项下拉框。方法：`init` 块加 NONE/CASCADED/LOCAL；`select` 大写化。

### ShellMysqlEventIntervalTypeCombobox
- 职责：MySQL 事件间隔类型下拉框。方法：`init` 块加 YEAR/QUARTER/.../MINUTE_SECOND；`select` 大写化。

### ShellMysqlEventOnCompletionCombobox
- 职责：MySQL 事件完成后保留策略下拉框。方法：`init` 块加 PRESERVE/NOT PRESERVE；`select` 大写化。

### ShellMysqlEventStatusCombobox
- 职责：MySQL 事件状态下拉框。方法：`init` 块加 ENABLE/DISABLE/DISABLE ON SLAVE；`select` 兼容 ENABLED/DISABLED/SLAVESIDE_DISABLED 等别名；`isSameStatus(String)` 判断一致。

---

## 十二、`fx.process`

### ShellProcessInfoTableView
- 职责：进程信息表格，支持按用户/关键字过滤、增删对比更新、杀/强制杀进程。
- 字段：`user`(`String`)、`filterText`(`String`)、`dataList`(`List<ShellProcessInfo>`)、`exec`(`ShellProcessExec`)。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initEvenListener()` | 事件 | 右键 + 快捷键 stop→`killProcess` |
  | `void setUser/setFilterText` | 设置过滤 | 触发 `refreshData()` |
  | `void refreshData()` | 刷新 | `setItem(doFilter(dataList))`→`sort`→`refresh` |
  | `getMenuItems()` | 菜单 | killProcess / forceKillProcess |
  | `void killProcess/forceKillProcess(ShellProcessInfo)` | 杀进程 | `exec.kill/forceKill(pid)`，成功后移除并刷新 |
  | `protected List<ShellProcessInfo> doFilter(List)` | 过滤 | 按用户与 command/pid/stat 关键字 |
  | `void updateData(List<ShellProcessInfo>)` | 增量更新 | 对比 pid 求新增/删除，已存在则 `copy` |
- 调用链：`updateData → doFilter → sort → refresh`

### ShellProcessTypeComboBox
- 职责：进程类型选择框。方法：`init` 块加 全部用户/当前用户。

---

## 十三、`fx.proxy`

### ShellProxyAuthTypeComboBox
- 职责：代理认证类型选择框。方法：`isPasswordAuth()`（序号 1）、`getAuthType()`（none/password）、`initNode()` 加 无/密码。

### ShellProxyProtocolComboBox
- 职责：代理协议选择框。方法：`init` 块加 HTTP/SOCKS；`select` 将 socks4/socks5 归一到 SOCKS。

---

## 十四、`fx.rdp`

### ShellRdpColorComboBox
- 职责：RDP 颜色深度选择框。方法：`getColor()`（8/15/16/24/32）、`selectColor(int)`、`initNode()`（`"n"+bit`）。

### ShellRdpMethodComboBox
- 职责：RDP 连接方式选择框。方法：`getMethod()`、`selectMethod(int)`、`initNode()`（内置 + Windows 下 mstsc.exe / macOS 下 Windows.app）。

---

## 十五、`fx.redis`

### ShellRedisDatabaseComboBox
- 职责：Redis 数据库选择框。
- 字段：`dbCount`(`Integer`)。
- 方法：`getDbCount/setDbCount`（null 加“全部数据库”，否则 `addDB(i)`）、`addDB(int)`（`"db"+i`）、`getDB()`（由值解析索引，非法返回 -1）。

### ShellRedisKeyFilterHistoryPopup
- 职责：Redis 键过滤历史弹窗。字段：`historyStore`(`final RedisKeyFilterHistoryStore`)。方法：`getHistories()`（倒序返回 `getPatterns()`）。

### ShellRedisKeyRowTableView
- 职责：Redis 键行表格（泛型 `R extends ShellRedisKeyRow`），承载新增/复制/删除动作。
- 字段：`addAction`、`copyAction`、`deleteAction`（均为 `Runnable`）。
- 方法：`get/setAddAction`、`get/setCopyAction`、`get/setDeleteAction`、`initEvenListener()`（右键）、`getMenuItems()`（新增/复制/删除，复制删除无选中时禁用）。

### ShellRedisKeyTypeComboBox
- 职责：Redis 键类型下拉框（实现 `I18nSelectAdapter`）。方法：`getType()`（由值截取 `(` 前解析 `ShellRedisKeyType`）、`select(ShellRedisKeyType)`（按 ordinal）、`values(Locale)`（枚举名 + HYPERLOGLOG/COORDINATE/BITMAP）。

### ShellRedisLatitudeField
- 职责：Redis 纬度输入框。方法：`initNode()`（必填，范围 ±85.05112878）。

### ShellRedisLongitudeField
- 职责：Redis 经度输入框。方法：`initNode()`（必填，范围 ±180）。

---

## 十六、`fx.s3`

### ShellS3BucketTableView
- 职责：S3 桶列表视图，支持增/刷新/改/删/强删。
- 字段：`client`(`ShellS3Client`)。
- 方法：`setClient`、`initEvenListener()`、`getMenuItems()`、`loadBucket()`（`client.listBuckets`）、`deleteBucket(bucket,force)`、`addBucket()`（`ShellViewFactory.addS3Bucket`）、`updateBucket(bucket)`。

### ShellS3EffectiveTimeCombobox
- 职责：S3 有效期时间单位下拉框。方法：`init` 块加 天/小时/分钟/秒并首选项；`isDays/isHours/isMinutes/isSeconds`。

### ShellS3FileTableView
- 职责：S3 文件列表视图（继承 `ShellFileTableView<ShellS3Client,ShellS3File>`），带分享文件与根路径能力裁剪。
- 字段：`uploadTaskListener`、`deleteTaskListener`。
- 方法：`setClient`、`getMenuItems()`（上传/下载/分享 + 父菜单）、`isRootLocation()`、覆写各 `isSupportXxxAction`（根路径下多为 false，mkdir/permission/renameDir 恒 false）、`destroy()`。

### ShellS3RegionTextField
- 职责：S3 区域输入框，可搜索（`SelectTextFiled<Region>`）。
- 字段：`regions`(`List<Region>`)。
- 方法：`onTextChanged`（按 `toString` 过滤）、`select(String)`/`select(Region)`、`loadRegions()`（`Region.regions()`）、`initNode()`。

### ShellS3RetentionModeComboBox
- 职责：S3 对象保留模式下拉框。方法：`init` 块加 合规/治理。

### ShellS3RetentionValidityTypeComboBox
- 职责：S3 对象保留期限类型下拉框。方法：`init` 块加 天/年。

### ShellS3TypeCombobox
- 职责：S3 协议类型选择框。方法：`getType()`（Minio/Alibaba/Tencent/Huawei/S3）、`selectType(String)`、`initNode()`（Minio/阿里 oss/腾讯 cos/华为 obs/标准 s3）。

---

## 十七、`fx.serial`

### ShellSerialBaudRateTextFiled
- 职责：串口波特率输入框。方法：`init` 块加 9600..115200 并默认 9600；`getBaudRate()`。

### ShellSerialFlowControlComboBox
- 职责：串口流控下拉框。方法：`getFlowControl()`（映射 `SerialPort.FLOW_CONTROL_*`）、`init(int)`。

### ShellSerialNumDataBitsComboBox
- 职责：串口数据位下拉框。方法：`getNumDataBits()`、`init(int)`（5/6/7/8）。

### ShellSerialNumStopBitsComboBox
- 职责：串口停止位下拉框。方法：`getNumStopBits()`、`init(int)`（`SerialPort.ONE_STOP_BIT` 等）。

### ShellSerialParityBitsComboBox
- 职责：串口校验位下拉框。方法：`getParityBits()`、`init(int)`（NO/EVEN/ODD/MARK/SPACE）。

### ShellSerialPortNameTextFiled
- 职责：串口名称输入框。方法：`init` 块遍历 `SerialPort.getCommPorts()` 加系统端口名并默认首个。

---

## 十八、`fx.sftp`

### ShellSFTPFileTableView
- 职责：SFTP 文件列表视图（实现 `FXEventListener, Destroyable`）。
- 字段：`uploadTaskListener`、`deleteTaskListener`。
- 方法：`setClient`（注册任务监听）、`getMenuItems()`（父菜单 + 上传菜单 + 下载）、`destroy()`。

### ShellSSHSFTPFileTableView
- 职责：SSH SFTP 文件列表视图，扩展剪切/复制/粘贴、强制删除、压缩/解压、打包上传。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `pkgTransfer` | `boolean` | 是否打包传输 |
  | `tempFileType` | `byte` | 临时文件类型（1 剪切 / 2 复制） |
  | `tempFiles` | `List<ShellSFTPFile>` | 剪切/复制的临时文件 |
  | `sshClient` | `ShellSSHClient` | SSH 客户端 |
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isPkgTransfer()` | 打包传输状态 | 返回字段 |
  | `void setSSHClient(ShellSSHClient)` | 设置客户端 | `setClient(sshClient.sftpClient())` |
  | `List<? extends MenuItem> getMenuItems()` | 右键菜单 | 复制/剪切/粘贴、强制删除、按扩展名判定压缩/解压、打包传输勾选项 + 父菜单 |
  | `private void packageTransfer()` | 切换打包 | 取反 pkgTransfer |
  | `protected void forceDel(List<ShellSFTPFile>)` | 强制删除 | Linux/macOS/Unix 批量 `serverExec().forceDel(list)`；Windows 逐个 |
  | `protected void compress(List,type)` | 压缩 | 子线程逐个 `serverExec().compress`，完成后 `loadFileInnerBatch` |
  | `protected void uncompress(List)` | 解压 | 子线程 `serverExec().uncompress` |
  | `protected void cutFile/copyFile(List)` | 剪切/复制 | 记录 tempFiles 与 tempFileType |
  | `protected void pasteFile()` | 粘贴 | 存在则确认，按类型 `move`/`copy`，成功后 `onFileAdded` |
  | `public void uploadByPkg(List<File>,ShellSSHClient)` | 打包上传 | `ShellViewFactory.filePkgUpload` → 上传压缩包 → 解压 → 删除 → `reloadFile` |
- 调用链：`getMenuItems → 压缩/解压 → sshClient.serverExec() → loadFileInnerBatch`
- 调用链：`pasteFile → serverExec().move/copy → onFileAdded`

---

## 十九、`fx.smb`

### ShellSMBFileTableView
- 职责：SMB 文件列表视图。字段：`uploadTaskListener`、`deleteTaskListener`。方法：`setClient`、`getMenuItems()`（父菜单 + 上传 + 下载）、`isSupportPermissionAction()`（false）、`destroy()`。

### ShellSMBUserTextField
- 职责：SMB 用户输入框，可搜索。方法：`initNode()`（提示文本，默认项 Guest/Anonymous）。

---

## 二十、`fx.snippet`

### ShellSnippetEditor
- 职责：Shell 片段编辑器（继承 `Editor`）。方法：`getEditorFont()`（同 `ShellDataEditor`）、`getPrompts()`（首次懒加载 Linux/macOS/Windows/脚本命令提示集合）。

### ShellSnippetListView
- 职责：Shell 片段列表（`FXListView<FXHBox>`），双击/右键运行片段。
- 字段：`onItemPicked`(`Runnable`)。
- 方法：`get/setOnItemPicked`、`private onItemPicked()`、`select(int)`（越界纠正）、`getPickedItem()`（取 hBox 的 prop "item"）、`init(List<ShellSnippet>)`、`private initBox(ShellSnippet)`、`initNode()`、`getMenuItems()`（运行）。

---

## 二十一、`fx.split`

### ShellSplitListView
- 职责：Shell 连接拆分列表，多选连接进行分屏。
- 字段：`maxSelected`(`int`，默认 2)。
- 方法：`init` 块加载 `ShellConnectStore.INSTANCE.loadTermType()` 生成带复选框的项；`get/setMaxSelected`、`checkMaxSelected()`（达上限禁用未选项）、`unSelectAll()`、`getSelectedConnects()`。

---

## 二十二、`fx.ssh`

### ShellGpuEditor
- 职责：GPU 信息编辑器（只读、无行号）。方法：`getEditorFont()`（依设置构造字体）、`initNode()`（不可编辑、关闭行号）。

### ShellSSHAuthTypeComboBox
- 职责：SSH 认证类型下拉框（继承 `SSHAuthTypeCombobox`）。方法：`init` 块加密钥管理项；`isManagerAuth()`（序号 3）、`getAuthType()`（manager 或父类）。

### ShellSSHAuthTypeComboBox2
- 职责：SSH 认证类型下拉框（精简版）。方法：`init` 块重置为 密码/证书/密钥管理；覆写 `isManagerAuth()`（序号 2）、`isSSHAgentAuth()`（false）。

---

## 二十三、`fx.sync`

### ShellSyncTypeCombobox
- 职责：Shell 同步类型选择框。方法：`init` 块加 Gitee/Github 并首选项；`isGitee()`、`isGithub()`。

---

## 二十四、`fx.term`

### ShellTemShellComboBox
- 职责：Shell 类型（解释器）选择框。方法：`select(String)`（空则首项）、`initNode()`（Windows 固定列表；Linux/macOS 读 `/etc/shells`，否则回退）。

### ShellTermBackspaceTypeCombobox
- 职责：Shell 退格类型选择框。方法：`init` 块加三种退格并默认类型二；`isType1/2/3`、`selectType(Integer)`。

### ShellTermCursorBlinkComboBox
- 职责：Shell 光标闪烁选择框。方法：`init` 块加关闭 及 2000/1000/500/250/125ms；`getCursorBlinks()`（关闭返回 -1）、`selectCursorBlinks(int)`。

### ShellTermCursorStyleComboBox
- 职责：Shell 光标样式选择框（带 SVG 图标）。方法：`init` 块加 BLOCK/UNDERLINE/VERTICAL BAR 并设置单元格工厂；`private SVGGlyph getGlyph(String)`（按名称选图标并按主题设色）；`getCursorStyle()`、`selectCursorStyle(int)`。字段无（图标动态生成）。

### ShellTermFpsComboBox
- 职责：Shell 刷新率选择框。方法：`init` 块加 自动/120/90/75/60/30/24；`getFps()`（自动返回 -1）、`selectFps(int)`。

### ShellTermHistoryListView
- 职责：Shell 终端历史列表（`FXListView<FXHBox>`），双击/右键运行或复制历史命令。
- 字段：`onItemPicked`(`Runnable`)。
- 方法：`get/setOnItemPicked`、`private onItemPicked()`、`select(int)`、`getPickedItem()`、`init(List<String>)`、`private initBox(String)`、`initNode()`、`private onCopy()`、`getMenuItems()`（运行/复制）。

### ShellTermTypeComboBox
- 职责：Shell 终端类型选择框。方法：`init` 块加 xterm-256color/xterm-color/xterm/linux/vt*/ansi/dump 及空项。

---

## 二十五、`fx.tool`

### ShellNetworkScanResultTableView
- 职责：网络扫描结果表。代码块双击复制。

### ShellPortScanResultTableView
- 职责：端口扫描结果表。方法：`doSort()`：按端口号排序；代码块双击复制。

---

## 二十六、`fx.tunneling`

### ShellTunnelingTableView
- 职责：隧道配置表。代码块双击复制。

### ShellTunnelingTypeComboBox
- 职责：隧道类型下拉框。方法：`isLocalAuth/isRemoteAuth/isDynamicAuth`、`getTunnelingType()`（local/remote/dynamic）、`setType(String)`、`initNode()`（本地/远程/动态）。

---

## 二十七、`fx.vnc`

### ShellVNCCursorComboBox
- 职责：VNC 光标形状下拉框。方法：`initNode()` 遍历 `LocalMouseCursorShape.values()`。

### ShellVNCEncodingComboBox
- 职责：VNC 编码类型下拉框。方法：`initNode()` 遍历 `EncodingType.ordinaryEncodings`。

---

## 二十八、`fx.webdav`

### ShellWebdavFileTableView
- 职责：WebDAV 文件表（继承 `ShellFileTableView<ShellWebdavClient,ShellWebdavFile>`）。字段：`uploadTaskListener`、`deleteTaskListener`。方法：`setClient`、`getMenuItems()`（父菜单 + 上传 + 下载）、`destroy()`。

---

## 二十九、`fx.zk`

### ShellZKACLControl
- 职责：ZooKeeper 权限控件（继承 `ShellZKACL`），承载认证状态与“友好显示”。
- 字段：`authed`(`boolean`)、`friendly`(`boolean`)。
- 方法：`isAuthed/setAuthed`、`isFriendly/setFriendly`、`getIdControl/getPermsControl/getSchemaControl`（`idFriend/permsFriend/schemeFriend` 按 friendly 取值）、`getStatusControl()`（authed 时返回绿色“已认证”文本，否则 null）。

### ShellZKACLTableView
- 职责：ZooKeeper ACL 权限表，承载增/编辑/复制/删除动作。
- 字段：`addAction`、`editAction`、`copyAction`、`deleteAction`（`Runnable`）。
- 方法：各自 `get/set`、`initEvenListener()`（右键）、`getMenuItems()`（增/编辑/复制/删除，后三者无选中禁用）。

### ShellZKACLType2ComboBox
- 职责：ZooKeeper ACL 类型下拉框（简化版，实现 `I18nSelectAdapter`）。方法：`values(Locale)` 按语言返回 WORLD/DIGEST/IP（简体、繁体、英文三套文案）。

### ShellZKACLTypeComboBox
- 职责：ZooKeeper ACL 类型下拉框（实现 `I18nSelectAdapter`）。方法：`values(Locale)` 返回 WORLD、DIGEST（明文/摘要/已有）、IP（单/多）三类本地化项。

### ShellZKAuthComboBox
- 职责：ZooKeeper 认证下拉框（`FXComboBox<ShellZKAuth>`）。方法：`init` 块 `NodeManager.init(this)`；`init(String iid)`（`ShellZKAuthStore.INSTANCE.loadByIid` 后首选项）、`initNode()`（转换器显示“用户名:x 密码:y”）。

### ShellZKAuthTableView
- 职责：ZooKeeper 认证表，支持刷新/过滤/删除/复制。
- 字段：`kw`(`String` 关键字)、`iid`(`String` 连接 id)。
- 方法：`init(String iid,String kw)`、`refreshAuths()`（按 kw 过滤 user/password）、`initNode()`（右键）、`getMenuItems()`（删除/复制）、`deleteData(ShellZKAuth)`（`ShellZKAuthStore.INSTANCE.delete`）、`copyData(ShellZKAuth)`（复制用户名密码）。

### ShellZKAuthTypeComboBox
- 职责：ZooKeeper 认证类型下拉框（实现 `I18nSelectAdapter`）。方法：`init` 块 `NodeManager.init(this)`；`values(Locale)` 返回 用户密码明文/已有认证信息。

### ShellZKCreateModeComboBox
- 职责：ZooKeeper 节点创建模式下拉框（实现 `I18nSelectAdapter`）。方法：`values(Locale)` 返回 持久/临时/持久顺序/临时顺序/容器 节点。

### ShellZKHistoryDataTableView
- 职责：ZooKeeper 历史数据表，支持刷新/删除/还原历史。
- 字段：`nodePath`(`String`)、`client`(`ShellZKClient`)。
- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(ShellZKClient,String nodePath)` | 初始化 | 保存客户端与路径后 `refreshData()` |
  | `String getNodePath()/ShellZKClient getClient()` | 读取 | 返回字段 |
  | `void initNode()` | 右键菜单 | — |
  | `List<? extends MenuItem> getMenuItems()` | 菜单 | refreshHistory / deleteHistory / restoreHistory |
  | `void refreshData()` | 刷新 | `ShellZKDataUtil.listHistory(nodePath,client)` |
  | `void deleteData(ShellZKHistoryData)` | 删除历史 | `ShellZKDataUtil.deleteHistory(...)` 后移除 |
  | `void restoreData(ShellZKHistoryData)` | 还原历史 | `ShellZKDataUtil.getHistory` → `client.setData` → `ShellZKEventUtil.zkHistoryRestoreUpdated` |
- 调用链：`restoreData → ShellZKDataUtil.getHistory → client.setData → ShellZKEventUtil.zkHistoryRestoreUpdated`

### ShellZKSASLTypeComboBox
- 职责：ZooKeeper SASL 类型下拉框。方法：`initNode()` 加 `Digest`（Kerberos 已注释）。

---

## 附：包覆盖统计

| 包 | 类数（非图标） |
|---|---|
| fx（根） | 4 |
| connect | 1 |
| dameng | 17 |
| db | 1 |
| docker | 9 |
| file | 8 |
| ftp | 1 |
| jump | 1 |
| key | 4 |
| mongo | 7 |
| mysql | 26 |
| process | 2 |
| proxy | 2 |
| rdp | 2 |
| redis | 6 |
| s3 | 7 |
| serial | 6 |
| sftp | 2 |
| smb | 2 |
| snippet | 2 |
| split | 1 |
| ssh | 3 |
| sync | 1 |
| term | 7 |
| tool | 2 |
| tunneling | 2 |
| vnc | 2 |
| webdav | 1 |
| zk | 10 |
| **合计** | **139** |

> SVG 图标类（50 个 glyph + 1 个 pane = 51）见 `fx-icons.md`。
