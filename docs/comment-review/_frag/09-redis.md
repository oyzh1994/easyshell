# Redis 标签页

## ShellRedisTab
> 文件: cn/oyzh/easyshell/tabs/redis/ShellRedisTab.java
- 职责：Redis 连接标签页，负责图标、标题与内容控制器的装配与启动。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 构造器与 `onTabClosed` 均被注释，当前无自有字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public String getTabTitle()` | 生成标签标题 | `连接名 + "(" + 连接类型大写 + ")"` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时按 `shellConnect().getOsType()` 取 `ShellOsTypeComboBox.getGlyph`，设为 `Cursor.DEFAULT` |
  | `protected String url()` | FXML 路径 | `/tabs/redis/shellRedisTab.fxml` |
  | `protected ShellRedisTabController controller()` | 获取内容控制器 | 强转 `super.controller()` |
  | `public void init(ShellConnect connect)` | 初始化标签页 | 先 `controller().init(connect)`，再 `super.init(connect)`，异常仅打印 |
  | `public ShellBaseClient client()` | 获取客户端 | 委托 `controller().getClient()` |
  | `public static ShellRedisTab of(ShellConnect connect)` | 工厂方法 | `new ShellRedisTab()` 后 `init(connect)` |
- 调用链：`of → init → controller().init`；`getTabTitle/flushGraphic/client → controller()`

## ShellRedisTabController
> 文件: cn/oyzh/easyshell/tabs/redis/ShellRedisTabController.java
- 职责：Redis 标签页内容组件，创建客户端、连接并按状态初始化各子标签页。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | ShellRedisClient | Redis 客户端 |
  | root | FXTabPane | 根 tab 面板（`@FXML`） |
  | keys | FXTab | 键标签页节点（`@FXML`） |
  | keysController | ShellRedisKeysTabController | 键子控制器（`@FXML`） |
  | queryController | ShellRedisQueryTabController | 查询子控制器（`@FXML`） |
  | serverController | ShellRedisServerTabController | 服务子控制器（`@FXML`） |
  | terminalController | ShellRedisTerminalTabController | 终端子控制器（`@FXML`） |
  | publishController | ShellRedisPublishTabController | 发布子控制器（`@FXML`） |
  | subscribeController | ShellRedisSubscribeTabController | 订阅子控制器（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public List<? extends RichTabController> getSubControllers()` | 返回子控制器集合 | 返回 keys/query/server/terminal/publish/subscribe 六个控制器 |
  | `public ShellConnect shellConnect()` | 获取连接 | `client.shellConnect()` |
  | `public void init(ShellConnect connect)` | 初始化并启动连接 | `ShellClientUtil.newClient` 创建客户端；注册状态监听（INTERRUPTED 时告警）；`StageManager.showMask` 中 `client.start()`，未连接则告警并 `closeTab`；否则 `hideLeft()`，哨兵模式移除 keys 标签否则 `keysController.init`，再依次 `queryController/serverController/terminalController.init`（终端用 `forkClient()`） |
  | `public ShellRedisClient getClient()` | 获取客户端 | 返回 `client` |
  | `public void onTabClosed(Event event)` | 标签关闭回调 | `super` 后 `subscribeController.unsubscribe()`，`IOUtil.closeAsync(client)` |
- 调用链：`init → ShellClientUtil.newClient/start → keysController.init/queryController.init/serverController.init/terminalController.init`；`onTabClosed → subscribeController.unsubscribe → IOUtil.closeAsync`

## ShellRedisKeysTabController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisKeysTabController.java
- 职责：Redis 键树标签页，负责键树加载、过滤、选中项数据初始化以及键数据/hex/信息三个子视图装配。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXSplitPane | 根分割面板（`@FXML`） |
  | tabPane | FXTabPane | 数据/hex/信息 tab 面板（`@FXML`） |
  | keyDataController | ShellRedisKeyDataController | 键数据控制器（`@FXML`） |
  | keyHexController | ShellRedisKeyHexController | 键 hex 控制器（`@FXML`） |
  | keyInfoController | ShellRedisKeyInfoController | 键信息控制器（`@FXML`） |
  | leftBox | FXVBox | 左侧节点容器（`@FXML`） |
  | client | ShellRedisClient | Redis 客户端 |
  | activeItem | ShellRedisKeyTreeItem | 当前激活的键节点 |
  | treeView | ShellRedisTreeView | 键树组件（`@FXML`） |
  | filterKW | FilterTextField | 过滤关键字输入（`@FXML`） |
  | filterType | ShellRedisKeyFilterTypeComboBox | 过滤类型下拉（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellRedisClient getClient()` / `public void setClient(ShellRedisClient client)` | 客户端读写 | 直接返回/赋值 `client` |
  | `public void init(ShellRedisClient client)` | 初始化键树 | `treeView.setClient(client)` 后 `treeView.loadItems()` |
  | `private void doFilter()` | 执行键过滤 | 读取 `filterKW`/`filterType`，设置高亮与 `treeView.getItemFilter()` 参数，`ThreadUtil.start(treeView::filter)` |
  | `private void positionNode()` | 定位当前节点 | `treeView.positionItem()` |
  | `protected void bindListeners()` | 绑定交互 | 监听 `treeView.selectItemChanged(this::initItem)`、过滤类型变化、`Ctrl+F`（`KeyListener.addHandler`）、过滤文本/全字/大小写变化均触发 `doFilter` |
  | `private void initItem(TreeItem<?> treeItem)` | 键节点选中处理 | 非键节点禁用 `tabPane`；否则设 `activeItem`、启用 `tabPane`，`StageManager.showMask` 中 `CostUtil.record` → `initData` → `flushTab` → `treeView.focusNode` |
  | `public void initData()` | 初始化键数据 | `keyDataController.init`、`keyInfoController.init`、`keyHexController.init` 依次以 `activeItem` 调用 |
  | `public void flushTTL()` | 刷新 TTL | `keyDataController.flushTTL()` |
  | `public List<? extends RichTabController> getSubControllers()` | 子控制器集合 | 返回 keyData/keyHex/keyInfo |
  | `public void importData()` / `private void exportData()` / `private void transportData()` | 导入/导出/传输 | 均委托 `ShellRedisViewFactory.redisImportData/redisExportData/redisTransportData(this.shellConnect(), null)` |
  | `public ShellConnect shellConnect()` | 获取连接 | `client.shellConnect()` |
  | `private void onTTLUpdate(ShellRedisKeyTTLUpdatedEvent event)` | TTL 更新事件（`@EventSubscribe`） | 校验连接、dbIndex、key 与 `activeItem` 一致后 `flushTTL()` |
  | `private void onReverseView(ShellRedisZSetReverseViewEvent event)` | 反转视图事件（`@EventSubscribe`） | `activeItem == event.data()` 时 `initItem(activeItem)` |
- 调用链：`init → treeView.loadItems`；`initItem → initData → keyDataController.init/keyInfoController.init/keyHexController.init`

## ShellRedisKeyDataController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisKeyDataController.java
- 职责：键数据分发中枢，按键树节点类型切换到对应的键类型控制器并显示其面板。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dataRoot | FXTab | 数据根 tab（`@FXML`） |
  | setKeyController | ShellRedisSetKeyController | set 键控制器（`@FXML`） |
  | zsetKeyController | ShellRedisZSetKeyController | zset 键控制器（`@FXML`） |
  | listKeyController | ShellRedisListKeyController | list 键控制器（`@FXML`） |
  | hylogKeyController | ShellRedisHylogKeyController | hylog 键控制器（`@FXML`） |
  | hashKeyController | ShellRedisHashKeyController | hash 键控制器（`@FXML`） |
  | jsonKeyController | ShellRedisJsonKeyController | json 键控制器（`@FXML`） |
  | stringKeyController | ShellRedisStringKeyController | string 键控制器（`@FXML`） |
  | streamKeyController | ShellRedisStreamKeyController | stream 键控制器（`@FXML`） |
  | coordinateKeyController | ShellRedisCoordinateKeyController | 坐标键控制器（`@FXML`） |
  | treeItem | ShellRedisKeyTreeItem | 当前键节点 |
  | keyExtraController | ShellRedisKeyExtraController | 键扩展信息控制器（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellRedisKeyTreeItem treeItem)` | 按类型分发初始化 | `NodeGroupUtil.disappear(dataRoot,"key-data")` 隐藏旧内容；按 `instanceof` 分派：String 且 `isHyLog()`→hylog、否则 string；Json→json；ZSet 且 `isCoordinateView()`→coordinate、否则 zset；Hash/List/Set/Stream 各自；随后 `lookup` 并 `NodeUtil.display` 对应 `#xxxKey` 节点；再 `keyExtraController.init(treeItem)`、`flushTab()`；`isExpire()` 时确认后 `deleteByExpired()` |
  | `private ShellRedisKeyController<?> getKeyController()` | 按类型返回键控制器 | 逻辑与 `init` 基本对应，但 String 分支返回相反（`isHyLog()` 返回 `stringKeyController`，否则 `hylogKeyController`），与 `init` 中的展示分支不一致 |
  | `public void reloadKey()` | 重载当前键 | `getKeyController().reloadKey()` |
  | `public void flushTTL()` | 刷新 TTL | 直接 `keyExtraController.flushTTL()`（旧逻辑已注释） |
  | `public List<? extends SubTabController> getSubControllers()` | 子控制器集合 | 返回各键控制器与 `keyExtraController` |
- 调用链：`init → string/hylog/json/zset/coordinate/hash/list/set/streamKeyController.init → keyExtraController.init → flushTab`

## ShellRedisKeyController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisKeyController.java
- 职责：各键类型控制器的抽象基类，提供树节点绑定、复制/重命名、保存与快捷键等通用能力。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | treeItem | T extends ShellRedisKeyTreeItem | 当前键树节点（泛型） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public boolean init(T treeItem)` | 初始化键控制器 | 保存 `treeItem`；`isExpire()` 已过期返回 false；否则 `initKey()` 并返回 true |
  | `protected void initKey()` | 初始化键（空实现） | 由子类覆写 |
  | `protected void copyKey()` | 复制键信息 | 拼接数据库/键类型/键名，`ClipboardUtil.setStringAndTip` |
  | `protected void renameKey()` | 重命名键 | `treeItem.rename()` |
  | `protected void onKeyDataKeyPressed(KeyEvent e)` | 数据区按键 | `Ctrl+S` 时 `saveKeyValue()` 并消费事件 |
  | `protected void saveKeyValue()` | 保存键数据 | `treeItem.isDataUnsaved()` 时 `StageManager.showMask(treeItem::saveKeyValue)` |
  | `public void reloadKey()` | 重载键（空实现） | 子类覆写 |
  | `protected abstract void firstShowData()` | 首次显示数据 | 抽象方法 |
- 调用链：`init → initKey`；`onKeyDataKeyPressed → saveKeyValue → treeItem.saveKeyValue`

## ShellRedisRowKeyController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisRowKeyController.java
- 职责：支持行/分页显示的键控制器抽象基类，统一分页、过滤、表格及行操作骨架。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | pageData | Paging<R> | 分页数据 |
  | pagePane | PageBox<R> | 分页面板（`@FXML`） |
  | filter | ClearableTextField | 数据过滤输入（`@FXML`） |
  | listTable | ShellRedisKeyRowTableView<R> | 数据列表表格（`@FXML`） |
  | dataAction | FXHBox | 数据操作面板（`@FXML`） |
  | setting | ShellSetting | 程序设置（final，取 `ShellSettingStore.SETTING`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public boolean init(T treeItem)` | 初始化行控制器 | `super.init` 成功后绑定过滤监听（延迟 50ms 调 `firstPage`）并设置表格增/复制/删回调 |
  | `protected void prevPage()` / `protected void nextPage()` | 上/下一页 | `initPage(currentPage ± 1)` |
  | `public void firstPage()` / `protected void lastPage()` | 首/尾页 | `initPage(0)` / `initPage(Integer.MAX_VALUE)` |
  | `public void reloadKey()` | 重载键 | 未保存时确认放弃；`treeItem.refreshKeyValue()` → `firstPage()` → `flushMemoryUsage()` |
  | `protected abstract void addRow()` / `deleteRow()` / `getRows()` / `copyRow()` / `clearRow()` | 行操作抽象 | 由子类实现 |
  | `protected void initPage(long pageNo)` | 初始化分页 | `getRows()` 取数据，`new Paging<>(rows, setting.getRowPageLimit())`，`page(pageNo)` 后设置表格与分页面板 |
  | `protected void initTable()` | 初始化表格 | `listTable.selectedItemChanged → initRow` |
  | `protected void initRow(R row)` | 初始化行 | `disableTab`；`FXUtil.runLater` 中 `currentRow(row)`、`clearData()`，行空则 `clearRow()` 否则 `firstShowData()`，最后 `enableTab` |
  | `private void pageSetting()` | 页码设置弹窗 | `ShellRedisPageSettingPopupController`，提交且页码限制变化时 `firstPage()` |
  | `private void pageJump(PageEvent.PageJumpEvent event)` | 跳页 | `initPage(event.getPage())` |
- 调用链：`init → firstPage → initPage → getRows → listTable.setItem`；`initPage → initRow → firstShowData`

## ShellRedisStringKeyController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisStringKeyController.java
- 职责：string 键标签内容组件，基于数据编辑器提供内容编辑、格式识别、二进制保存、二维码与高亮搜索。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | filter | HighlightTextField | 内容过滤/高亮输入（`@FXML`） |
  | dataUndo | SVGGlyph | 撤销按钮（`@FXML`） |
  | dataRedo | SVGGlyph | 重做按钮（`@FXML`） |
  | binary | FXText | 二进制提示文本（`@FXML`） |
  | saveNodeData | SVGGlyph | 保存按钮（`@FXML`） |
  | format | EditorFormatTypeComboBox | 数据格式下拉（`@FXML`） |
  | nodeData | ShellDataEditor | 数据编辑器（`@FXML`） |
  | ignoreDataChange | boolean | 忽略数据变化标志 |
  | dataListener | ChangeListener<String> | 数据变化监听器（final） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void initKey()` | 初始化键 | `flushBinary`；同步保存按钮状态；`isDataTooBig()` 时禁用编辑器并延迟弹窗确认 `saveBinaryFile`；否则启用、`firstShowData`、按 `showDetectData` 选择格式（过大则 `RAW`） |
  | `private void saveBinaryFile()` | 保存为二进制文件 | `FileChooserHelper.save` 后按 `rawValue()`（String/byte[]）写文件 |
  | `private void flushBinary()` | 刷新二进制提示 | `isRawEncoding()` 时 `binary.display()`，否则 `disappear()` |
  | `private void reloadData()` | 重载数据 | 未保存时确认；`refreshKeyValue` → `initKey` → `flushMemoryUsage` |
  | `protected void saveKeyValue()` | 保存键值 | 数据过大告警；未保存时 `treeItem.saveKeyValue()` 后 `flushBinary`、禁用保存按钮、刷新内存 |
  | `protected void dataUndo()` / `dataRedo()` / `pasteData()` / `clearData()` | 编辑器操作 | 分别调用 `nodeData.undo/redo/paste/clear` 并请求焦点 |
  | `protected void firstShowData()` | 首次显示数据 | `nodeData.showData(treeItem.data())` 后 `forgetHistory()` |
  | `protected void bindListeners()` | 绑定监听 | 格式变化设置编辑器格式；`nodeData` 文本/撤销/重做监听；`EditorUtil.bindHighlight` |
  | `private void key2QRCode(MouseEvent event)` | 键值转二维码 | 弹出 `ShellRedisKeyQRCodePopupController` 并传 key/keyData |
  | `private void searchNext()` | 搜索下一个 | `EditorUtil.searchNextHighlight(nodeData, filter)` |
- 调用链：`initKey → flushBinary/firstShowData`；`saveKeyValue → treeItem.saveKeyValue → flushBinary`

## ShellRedisHashKeyController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisHashKeyController.java
- 职责：hash 键内容组件，字段-值成行显示，支持字段名与值分别编辑、格式识别及字段列表展开。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dataUndo/dataRedo | SVGGlyph | 数据撤销/重做按钮（`@FXML`） |
  | fieldUndo/fieldRedo | SVGGlyph | 字段撤销/重做按钮（`@FXML`） |
  | saveNodeData | SVGGlyph | 数据保存按钮（`@FXML`） |
  | hashField | ShellDataEditor | 字段名编辑器（`@FXML`） |
  | nodeData | ShellDataEditor | 值编辑器（`@FXML`） |
  | splitPane | FXSplitPane | 分割面板（`@FXML`） |
  | format | EditorFormatTypeComboBox | 值格式下拉（`@FXML`） |
  | fieldFormat | EditorFormatTypeComboBox | 字段格式下拉（`@FXML`） |
  | fieldAction | FXHBox | 字段操作面板（`@FXML`） |
  | expandPane | ExpandListSVGPane | 展开列表面板（`@FXML`） |
  | ignoreDataChange | boolean | 忽略数据变化标志 |
  | dataListener | ChangeListener<String> | 值变化监听器（final） |
  | fieldValListener | ChangeListener<String> | 字段变化监听器（final） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void initKey()` | 初始化键 | `initTable()` + `firstPage()` |
  | `protected void initRow(RedisHashRow row)` | 初始化行 | `super`；行空清空 `hashField`，否则回填字段名并 `forgetHistory` |
  | `protected List<RedisHashRow> getRows()` | 取行 | `treeItem.rows()`，有过滤词时按字段名或值忽略大小写过滤（并行流） |
  | `protected void addRow()` | 新增字段 | `ShellRedisViewFactory.redisHashFieldAdd(treeItem)`，成功后 `firstPage` + `flushMemoryUsage` |
  | `private void reloadRow()` | 重载当前行 | 未保存确认；`treeItem.reloadRow()` 成功后 `initRow(currentRow)` |
  | `protected void copyRow()` | 复制行 | 拼接键名/字段名/字段值至剪贴板 |
  | `protected void saveKeyValue()` | 保存键值 | 字段重复、数据过大告警；未保存时 `saveKeyValue` → `listTable.refresh` → 禁用保存 → 刷新内存 |
  | `private void dataUndo/dataRedo/pasteData/clearData()` | 值编辑操作 | `nodeData` 对应操作并请求焦点 |
  | `private void fieldUndo/fieldRedo/pasteField/clearFiled()` | 字段编辑操作 | `hashField` 对应操作并请求焦点 |
  | `protected void firstShowData()` | 首次显示数据 | 行空返回；过大则禁用并告警；否则 `showDetectData` 选择值/字段格式、`forgetHistory`、禁用保存 |
  | `protected void deleteRow()` | 删除字段 | 确认后 `treeItem.deleteRow()`，多行移除项否则 `firstPage`，刷新内存 |
  | `protected void clearRow()` | 清除行 | `nodeData.clear()` 并禁用 |
  | `protected void bindListeners()` | 绑定监听 | 值/字段格式监听、值/字段文本监听、撤销重做绑定、`hashField`/`fieldAction`/`dataAction` 禁用绑定 |
  | `private void expendList()` | 展开/收起字段列表 | 切换 `hash_list` 显示与 `splitPane` 高度 |
- 调用链：`initKey → initTable/firstPage → getRows`；`addRow → ShellRedisViewFactory.redisHashFieldAdd → firstPage`

## ShellRedisListKeyController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisListKeyController.java
- 职责：list 键内容组件，按索引-元素成行显示，支持元素编辑、二进制导出与元素列表展开。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dataUndo | SVGGlyph | 数据撤销按钮（`@FXML`） |
  | dataRedo | SVGGlyph | 数据重做按钮（`@FXML`） |
  | saveNodeData | SVGGlyph | 数据保存按钮（`@FXML`） |
  | format | EditorFormatTypeComboBox | 格式下拉（`@FXML`） |
  | nodeData | ShellDataEditor | 数据编辑器（`@FXML`） |
  | expandPane | ExpandListSVGPane | 展开列表面板（`@FXML`） |
  | ignoreDataChange | boolean | 忽略数据变化标志 |
  | dataListener | ChangeListener<String> | 数据变化监听器（final） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void initKey()` | 初始化键 | `initTable()` + `firstPage()` |
  | `private void reloadRow()` | 重载当前行 | 未保存确认；`treeItem.reloadRow()` 成功后 `initRow(currentRow)` |
  | `protected List<RedisListRow> getRows()` | 取行 | 按元素值忽略大小写过滤 |
  | `protected void addRow()` | 新增元素 | `ShellRedisViewFactory.redisListElementAdd`，成功后 `firstPage` + 刷新内存 |
  | `protected void saveKeyValue()` | 保存键值 | 过大告警；未保存时保存并刷新表格/内存 |
  | `protected void copyRow()` | 复制行 | 拼接键名与元素值至剪贴板 |
  | `private void dataUndo/dataRedo/pasteData/clearData()` | 编辑操作 | `nodeData` 对应操作并请求焦点 |
  | `protected void firstShowData()` | 首次显示数据 | 过大则禁用并异步确认 `saveBinaryFile`；否则识别格式、`forgetHistory`、禁用保存 |
  | `private void saveBinaryFile()` | 保存元素的二进制 | `FileChooserHelper.save` 后写 `rawValue().getValue().getBytes()` |
  | `protected void deleteRow()` | 删除元素 | 确认后 `deleteRow`，多行移除否则 `firstPage` |
  | `protected void clearRow()` | 清除行 | `nodeData.clear()` 并禁用 |
  | `protected void bindListeners()` | 绑定监听 | 格式监听、数据文本/撤销/重做监听、`dataAction` 禁用绑定 |
  | `private void expendList()` | 展开/收起元素列表 | 切换 `list_list` 显示与编辑器高度 |
- 调用链：`initKey → initTable/firstPage`；`addRow → ShellRedisViewFactory.redisListElementAdd → firstPage`

## ShellRedisSetKeyController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisSetKeyController.java
- 职责：set 键内容组件，成员成行显示，支持成员编辑、二进制导出与成员列表展开。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dataUndo | SVGGlyph | 数据撤销按钮（`@FXML`） |
  | dataRedo | SVGGlyph | 数据重做按钮（`@FXML`） |
  | saveNodeData | SVGGlyph | 数据保存按钮（`@FXML`） |
  | format | EditorFormatTypeComboBox | 格式下拉（`@FXML`） |
  | nodeData | ShellDataEditor | 数据编辑器（`@FXML`） |
  | expandPane | ExpandListSVGPane | 展开列表面板（`@FXML`） |
  | ignoreDataChange | boolean | 忽略数据变化标志 |
  | dataListener | ChangeListener<String> | 数据变化监听器（final） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void initKey()` | 初始化键 | `initTable()` + `firstPage()` |
  | `protected List<RedisSetRow> getRows()` | 取行 | 按成员值忽略大小写过滤 |
  | `protected void addRow()` | 新增成员 | `ShellRedisViewFactory.redisSetMemberAdd`，成功后 `firstPage` + 刷新内存 |
  | `protected void saveKeyValue()` | 保存键值 | 成员重复、过大告警；未保存时保存并刷新表格/内存 |
  | `protected void copyRow()` | 复制行 | 拼接键名与成员值至剪贴板 |
  | `private void dataUndo/dataRedo/pasteData/clearData()` | 编辑操作 | `nodeData` 对应操作并请求焦点 |
  | `protected void firstShowData()` | 首次显示数据 | 过大则禁用并异步确认 `saveBinaryFile`；否则识别格式、`forgetHistory`、禁用保存 |
  | `private void saveBinaryFile()` | 保存成员的二进制 | 写 `rawValue().getValue().getBytes()` |
  | `protected void deleteRow()` | 删除成员 | 确认后 `deleteRow`，多行移除否则 `firstPage` |
  | `protected void clearRow()` | 清除行 | `nodeData.clear()` 并禁用 |
  | `protected void bindListeners()` | 绑定监听 | 格式监听、数据监听、`dataAction` 禁用绑定 |
  | `private void expendList()` | 展开/收起成员列表 | 切换 `set_list` 显示与编辑器高度 |
- 调用链：`initKey → initTable/firstPage`；`addRow → ShellRedisViewFactory.redisSetMemberAdd → firstPage`

## ShellRedisZSetKeyController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisZSetKeyController.java
- 职责：zset（有序集合）键内容组件，成员带分数，支持分数编辑、反转视图及坐标视图切换。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dataUndo | SVGGlyph | 数据撤销按钮（`@FXML`） |
  | dataRedo | SVGGlyph | 数据重做按钮（`@FXML`） |
  | saveNodeData | SVGGlyph | 数据保存按钮（`@FXML`） |
  | reverseView | SVGGlyph | 反转视图按钮（`@FXML`） |
  | scoreVal | DecimalTextField | 分数字段（`@FXML`） |
  | nodeData | ShellDataEditor | 数据编辑器（`@FXML`） |
  | format | EditorFormatTypeComboBox | 格式下拉（`@FXML`） |
  | expandPane | ExpandListSVGPane | 展开列表面板（`@FXML`） |
  | ignoreDataChange | boolean | 忽略数据变化标志 |
  | dataListener | ChangeListener<String> | 数据变化监听器（final） |
  | scoreValListener | ChangeListener<String> | 分数变化监听器（final） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void initKey()` | 初始化键 | `initTable()` + `firstPage()`；`reverseView` 可见性取决于 `isSupportCoordinate()` |
  | `protected List<RedisZSetRow> getRows()` | 取行 | 按成员值或分数忽略大小写过滤 |
  | `protected void addRow()` | 新增成员 | `ShellRedisViewFactory.redisZSetMemberAdd`，成功后 `firstPage` + 刷新内存 |
  | `protected void initRow(RedisZSetRow row)` | 初始化行 | `super`；回填分数并 `clearData` |
  | `protected void saveKeyValue()` | 保存键值 | 成员重复、过大告警；未保存时保存并刷新表格/内存 |
  | `protected void copyRow()` | 复制行 | 拼接键名/成员值/分数至剪贴板 |
  | `private boolean isSupportCoordinate()` | 是否支持地理坐标 | `treeItem.isSupportCoordinate()` |
  | `private void reverseView()` | 反转视图 | `treeItem.reverseView()` |
  | `private void dataUndo/dataRedo/pasteData/clearData()` | 编辑操作 | `nodeData` 对应操作并请求焦点 |
  | `protected void firstShowData()` | 首次显示数据 | 过大则禁用并告警；否则识别格式、`forgetHistory`、禁用保存 |
  | `protected void deleteRow()` | 删除成员 | 确认后 `deleteRow`，多行移除否则 `firstPage` |
  | `protected void clearRow()` | 清除行 | `nodeData.clear()` 并禁用 |
  | `protected void bindListeners()` | 绑定监听 | 分数文本监听与禁用/可编辑绑定、格式监听、数据监听、`dataAction` 绑定 |
  | `private void expendList()` | 展开/收起成员列表 | 切换 `zset_list` 显示与编辑器高度 |
- 调用链：`initKey → initTable/firstPage`；`reverseView → treeItem.reverseView → ShellRedisKeysTabController.onReverseView`

## ShellRedisCoordinateKeyController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisCoordinateKeyController.java
- 职责：zset 地理坐标视图组件，以经度/纬度方式展示与编辑 GEO 成员。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dataUndo/dataRedo | SVGGlyph | 数据撤销/重做按钮（`@FXML`） |
  | saveNodeData | SVGGlyph | 数据保存按钮（`@FXML`） |
  | longitudeVal | DecimalTextField | 经度值（`@FXML`） |
  | latitudeVal | DecimalTextField | 纬度值（`@FXML`） |
  | nodeData | ShellDataEditor | 数据编辑器（`@FXML`） |
  | format | EditorFormatTypeComboBox | 格式下拉（`@FXML`） |
  | expandPane | ExpandListSVGPane | 展开列表面板（`@FXML`） |
  | ignoreDataChange | boolean | 忽略数据变化标志 |
  | dataListener | ChangeListener<String> | 数据变化监听器（final） |
  | longitudeValListener | ChangeListener<String> | 经度变化监听器（final） |
  | latitudeValListener | ChangeListener<String> | 纬度变化监听器（final） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void initKey()` | 初始化键 | `initTable()` + `firstPage()` |
  | `protected List<RedisZSetRow> getRows()` | 取行 | 按成员值或经/纬度忽略大小写过滤 |
  | `protected void addRow()` | 新增坐标 | `ShellRedisViewFactory.redisZSetCoordinateAdd`，成功后 `firstPage` + 刷新内存 |
  | `protected void initRow(RedisZSetRow row)` | 初始化行 | `super`；行空清空经纬度，否则回填经纬度并 `clearData` |
  | `protected void saveKeyValue()` | 保存键值 | 成员重复、过大告警；未保存时保存并刷新表格/内存 |
  | `protected void copyRow()` | 复制行 | 拼接键名/成员/经度/纬度至剪贴板 |
  | `private void reverseView()` | 反转视图 | `treeItem.reverseView()` |
  | `private void dataUndo/dataRedo/pasteData/clearData()` | 编辑操作 | `nodeData` 对应操作并请求焦点 |
  | `protected void firstShowData()` | 首次显示数据 | 过大则禁用并告警；否则识别格式、`forgetHistory`、禁用保存 |
  | `protected void deleteRow()` | 删除坐标 | 确认后 `deleteRow`，多行移除否则 `firstPage` |
  | `protected void clearRow()` | 清除行 | `nodeData.clear()` 并禁用 |
  | `protected void bindListeners()` | 绑定监听 | 经纬度文本监听与禁用/可编辑绑定、格式监听、数据监听、`dataAction` 绑定 |
  | `private void expendList()` | 展开/收起坐标列表 | 切换 `coordinate_list` 显示与编辑器高度 |
- 调用链：`initKey → initTable/firstPage`；`addRow → ShellRedisViewFactory.redisZSetCoordinateAdd → firstPage`

## ShellRedisStreamKeyController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisStreamKeyController.java
- 职责：stream 键内容组件，消息以 id-内容成行显示，支持消息新增/删除与内容展示。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | streamID | ReadOnlyTextField | 消息 id 只读框（`@FXML`） |
  | nodeData | ShellDataEditor | 消息内容编辑器（`@FXML`） |
  | expandPane | ExpandListSVGPane | 展开列表面板（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void initKey()` | 初始化键 | `initTable()` + `firstPage()` |
  | `protected List<RedisStreamRow> getRows()` | 取行 | 按内容或 id 忽略大小写过滤 |
  | `protected void addRow()` | 新增消息 | `ShellRedisViewFactory.redisStreamMessageAdd`，成功后 `firstPage` + 刷新内存 |
  | `protected void initRow(RedisStreamRow row)` | 初始化行 | `super`；行空禁用并清空，否则回填 id 并启用 |
  | `protected void copyRow()` | 复制行 | 拼接键名/消息 id/内容至剪贴板 |
  | `protected void firstShowData()` | 首次显示数据 | `nodeData.showData(rawValue().getValue())` 后 `forgetHistory` |
  | `protected void deleteRow()` | 删除消息 | 确认后 `deleteRow`，多行移除否则 `firstPage` |
  | `protected void clearRow()` | 清除行 | `nodeData.clear()` 并禁用 |
  | `private void expendList()` | 展开/收起消息列表 | 切换 `stream_list` 显示与编辑器高度 |
- 调用链：`initKey → initTable/firstPage`；`addRow → ShellRedisViewFactory.redisStreamMessageAdd → firstPage`

## ShellRedisJsonKeyController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisJsonKeyController.java
- 职责：json 键内容组件，基于数据编辑器提供 JSON 内容编辑、二进制保存、二维码与高亮搜索，无格式下拉。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | filter | HighlightTextField | 内容过滤/高亮输入（`@FXML`） |
  | dataUndo | SVGGlyph | 数据撤销按钮（`@FXML`） |
  | dataRedo | SVGGlyph | 数据重做按钮（`@FXML`） |
  | saveNodeData | SVGGlyph | 数据保存按钮（`@FXML`） |
  | nodeData | ShellDataEditor | 数据编辑器（`@FXML`） |
  | ignoreDataChange | boolean | 忽略数据变化标志 |
  | dataListener | ChangeListener<String> | 数据变化监听器（final） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void initKey()` | 初始化键 | 同步保存按钮状态；`isDataTooBig()` 时禁用并异步确认 `saveBinaryFile`；否则启用 + `firstShowData` |
  | `private void saveBinaryFile()` | 保存为二进制文件 | `FileChooserHelper.save` 后按 `rawValue()`（String/byte[]）写文件 |
  | `private void reloadData()` | 重载数据 | 未保存确认；`refreshKeyValue` → `initKey` → `flushMemoryUsage` |
  | `protected void saveKeyValue()` | 保存键值 | 过大告警；未保存时保存、禁用保存、刷新内存 |
  | `protected void dataUndo/dataRedo/pasteData/clearData()` | 编辑操作 | `nodeData` 对应操作并请求焦点 |
  | `protected void firstShowData()` | 首次显示数据 | `nodeData.showData(treeItem.data())` 后 `forgetHistory` |
  | `protected void bindListeners()` | 绑定监听 | 数据文本/撤销/重做监听；`EditorUtil.bindHighlight` |
  | `private void key2QRCode(MouseEvent event)` | 键值转二维码 | 弹出 `ShellRedisKeyQRCodePopupController` |
  | `private void searchNext()` | 搜索下一个 | `EditorUtil.searchNextHighlight(nodeData, filter)` |
- 调用链：`initKey → firstShowData`；`saveKeyValue → treeItem.saveKeyValue`；`key2QRCode → ShellRedisKeyQRCodePopupController`

## ShellRedisHylogKeyController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisHylogKeyController.java
- 职责：hylog 键内容组件，针对标记为 HyLog 的 string 键，展示原始值、二进制提示与统计值，并支持追加元素。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | binary | FXText | 二进制提示文本（`@FXML`） |
  | count | FXText | 统计值文本（`@FXML`） |
  | nodeData | ShellDataEditor | 数据编辑器（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void initKey()` | 初始化键 | `firstShowData()`、`flushBinary()`、`count.setText(count: treeItem.count())` |
  | `private void flushBinary()` | 刷新二进制提示 | `isRawEncoding()` 时 `binary.display()`，否则 `disappear()` |
  | `private void reloadData()` | 重载数据 | 未保存确认；`refreshKeyValue` → `initKey` → `flushMemoryUsage` |
  | `private void addRow()` | 添加元素 | `ShellRedisViewFactory.redisHylogElementsAdd`，成功后 `refreshKeyValue`、`flushCount`、`flushMemoryUsage`、`initKey` |
  | `protected void firstShowData()` | 首次显示数据 | `nodeData.showData(treeItem.rawValue())` 后 `forgetHistory` |
- 调用链：`initKey → firstShowData/flushBinary`；`addRow → ShellRedisViewFactory.redisHylogElementsAdd → flushCount/initKey`

## ShellRedisKeyExtraController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisKeyExtraController.java
- 职责：键扩展信息组件，展示并更新 TTL、加载耗时与内存占用。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | ttl | FXLabel | TTL 标签（`@FXML`） |
  | loadTime | FXText | 加载耗时文本（`@FXML`） |
  | memoryUsage | FXText | 内存占用文本（`@FXML`） |
  | treeItem | ShellRedisKeyTreeItem | redis 键节点 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellRedisKeyTreeItem treeItem)` | 初始化组件 | `memoryUsage.textProperty().bind(treeItem.memoryUsageInfoProperty())`；`flushTTL()`；`treeItem.flushMemoryUsage()`；`loadTime` 显示耗时 |
  | `public void flushTTL()` | 刷新 TTL | `ttl.text("TTL : " + treeItem.ttl())` |
  | `protected void ttlUpdate()` | TTL 设置 | `ShellRedisViewFactory.redisTtlKey(treeItem)` |
- 调用链：`init → flushTTL/treeItem.flushMemoryUsage`；`ttlUpdate → ShellRedisViewFactory.redisTtlKey`

## ShellRedisKeyHexController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisKeyHexController.java
- 职责：键 hex 视图组件，将 string 键数据写入临时文件并用 HexView 展示。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | hexRoot | FXTab | hex 根 tab（`@FXML`） |
  | hexView | HexView | hex 视图组件（`@FXML`） |
  | statusLabel | HexStatusLabel | hex 状态组件（`@FXML`） |
  | file | File | 当前临时文件 |
  | keyItem | ShellRedisKeyTreeItem | 键节点 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellRedisKeyTreeItem keyItem)` | 初始化数据 | 保存 `keyItem`；`hexRoot.isSelected()` 时 `initObject()` |
  | `private void initObject()` | 初始化对象 | String 键：懒创建缓存 `.hex` 文件，按 `data()`（String/byte[]）写入并 `hexView.openFile`、`statusLabel.init`；非 String 键：`hexView.close/disable`、`statusLabel.stop` |
  | `public void initialize(URL, ResourceBundle)` | 初始化 | `hexRoot` 选中变化时 `initObject()` |
  | `public void destroy()` | 销毁 | `hexView/statusLabel.destroy()`，删除临时文件，`super.destroy()` |
- 调用链：`init/initialize → initObject → hexView.openFile`

## ShellRedisKeyInfoController
> 文件: cn/oyzh/easyshell/tabs/redis/key/ShellRedisKeyInfoController.java
- 职责：键信息组件，展示并刷新键的编码、空闲时间与引用数量。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | infoRoot | FXTab | 信息根 tab（`@FXML`） |
  | client | ShellRedisClient | redis 客户端 |
  | redisKey | ShellRedisKey | redis 键对象 |
  | treeItem | ShellRedisKeyTreeItem | redis 树节点 |
  | objectEncoding | FXLabel | 编码标签（`@FXML`） |
  | objectIdletime | FXLabel | 空闲时间标签（`@FXML`） |
  | objectRefcount | FXLabel | 引用数量标签（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private void copy()` | 复制信息 | 拼接键名/数据库/编码/空闲/引用至剪贴板 |
  | `private void refresh()` | 刷新信息 | `initObject()` |
  | `public void init(ShellRedisKeyTreeItem treeItem)` | 初始化组件 | 节点或树为空直接返回；保存引用；`infoRoot.isSelected()` 时 `initObject()` |
  | `protected void initObject()` | 初始化对象 | `ShellRedisKeyUtil.keyObject(redisKey, dbIndex, key, client)` 后刷新编码/空闲/引用三个标签 |
  | `public void initialize(URL, ResourceBundle)` | 初始化 | `infoRoot` 选中变化时 `initObject()` |
- 调用链：`init/initialize/refresh → initObject → ShellRedisKeyUtil.keyObject`

## ShellRedisPublishTabController
> 文件: cn/oyzh/easyshell/tabs/redis/publish/ShellRedisPublishTabController.java
- 职责：Redis 发布（publish）内容组件，向指定通道发送消息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | msg | ShellDataEditor | 消息编辑框（`@FXML`） |
  | channel | ClearableTextField | 通道输入框（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellRedisTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
  | `public ShellRedisClient getClient()` | 获取客户端 | `parent().getClient()` |
  | `private void send()` | 发送信息 | 通道为空则告警并聚焦；否则 `getClient().publish(channel, msg.getTextTrim())` 并提示成功 |
- 调用链：`send → getClient → ShellRedisClient.publish`

## ShellRedisSubscribeTabController
> 文件: cn/oyzh/easyshell/tabs/redis/subscribe/ShellRedisSubscribeTabController.java
- 职责：Redis 订阅（psubscribe）内容组件，按通道模式异步订阅并输出收到的消息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | pubSub | JedisPubSub | 订阅回调对象 |
  | subscribe | FXToggleSwitch | 订阅开关（`@FXML`） |
  | msg | ReadOnlyTextArea | 消息输出区（`@FXML`） |
  | channel | ClearableTextField | 通道输入框（`@FXML`） |
  | pubsubThread | Thread | 订阅线程 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void subscribe()` | 订阅 | 通道为空则告警并复位开关；否则禁用输入并 `ThreadUtil.start` 中 `getClient().psubscribe(pubSub, channel)` |
  | `public void unsubscribe()` | 取消订阅 | `ThreadUtil.interrupt(pubsubThread)`；`pubSub.isSubscribed()` 时 `unsubscribe()`；启用通道输入 |
  | `public void onTabClosed(Event event)` | 标签关闭 | `unsubscribe()` 后 `super` |
  | `public void initialize(URL, ResourceBundle)` | 初始化 | 绑定开关变化（subscribe/unsubscribe）；创建匿名 `JedisPubSub` 覆写 `onPMessage/onPSubscribe/onPUnsubscribe` 输出到 `msg` |
  | `public ShellRedisTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
  | `public ShellRedisClient getClient()` | 获取客户端 | `parent().getClient()` |
  | `private void clearMsg()` | 清除消息 | `msg.clear()` |
  | `public void destroy()` | 销毁 | `unsubscribe()` 后 `super.destroy()` |
- 调用链：`initialize → subscribe/unsubscribe`；`subscribe → ShellRedisClient.psubscribe → JedisPubSub.onPMessage → msg`

## ShellRedisTerminalTabController
> 文件: cn/oyzh/easyshell/tabs/redis/terminal/ShellRedisTerminalTabController.java
- 职责：Redis 命令行标签内容组件，首次选中时延迟初始化命令行文本域。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根 tab（`@FXML`） |
  | terminal | RedisTerminalPane | redis 命令行文本域（`@FXML`） |
  | client | ShellRedisClient | redis 客户端 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellRedisClient client)` | 初始化 | 保存 `client`（真实初始化在 tab 选中时进行） |
  | `protected ShellConnect shellConnect()` | redis 信息 | `terminal.shellConnect()` |
  | `public Integer dbIndex()` | 数据库索引 | `terminal.getDbIndex()` |
  | `public ShellRedisClient getClient()` | 获取客户端 | `terminal.getClient()` |
  | `public void onTabInit(FXTab tab)` | tab 初始化 | `super`；`root` 选中且 `terminal.getClient()==null` 时 `terminal.init(client, null)` |
- 调用链：`init → onTabInit → terminal.init`

## ShellRedisQueryTabController
> 文件: cn/oyzh/easyshell/tabs/redis/query/ShellRedisQueryTabController.java
- 职责：Redis 查询标签内容组件，负责查询编辑、运行、结果展示与查询记录的增删改保存。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | query | ShellQuery | 当前查询对象 |
  | client | ShellRedisClient | redis 客户端 |
  | content | ShellRedisQueryEditor | 查询编辑器（`@FXML`） |
  | splitPane | FXSplitPane | 分割面板（`@FXML`） |
  | database | ShellRedisDatabaseComboBox | 数据库下拉（`@FXML`） |
  | resultTabPane | FXTabPane | 结果面板（`@FXML`） |
  | queryTreeView | ShellQueryTreeView | 查询列表树（`@FXML`） |
  | queryStore | ShellQueryStore | 查询存储（final，`ShellQueryStore.INSTANCE`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellConnect shellConnect()` | 获取连接 | `client.shellConnect()` |
  | `public void init(ShellRedisClient client)` | 初始化 | 设置编辑器 client、按 `client.databases()` 初始化数据库下拉、`queryTreeView.setIid(client.iid())` |
  | `private void save()` | 保存查询 | 新建时提示输入名称并 `queryStore.insert` + `addQuery`；否则更新内容/dbIndex 后 `queryStore.update`；置未保存为 false |
  | `private void setUnsaved(boolean unsaved)` | 设置未保存状态 | 选中查询树项时 `setUnsaved` 并 `refresh` |
  | `private void run()` | 运行查询 | 禁用 tab；构造 `ShellRedisQueryParam`；`client.query(param)` 得结果；展开分割面板并展示 `ShellRedisQueryMsgTab.of`，有数据时 `ShellRedisQueryDataTab.of` 并选中 |
  | `private void onContentKeyPressed(KeyEvent event)` | 内容按键 | `Ctrl+S` 保存、`Ctrl+R` 运行 |
  | `protected void bindListeners()` | 绑定监听 | 数据库变化（dbIndex 变更标记未保存并 `content.setDbIndex`）、内容变化标记未保存、查询树选择/新增/编辑/删除回调 |
  | `private void doAdd(ShellQuery query)` | 新增查询 | 载入内容与 dbIndex，选中末项 |
  | `private void doEdit(ShellQuery query)` | 编辑查询 | 载入或清空内容 |
  | `private void doDelete(ShellQuery query)` | 删除查询 | 删除当前查询时清空编辑器 |
- 调用链：`run → client.query → ShellRedisQueryMsgTab.of/ShellRedisQueryDataTab.of`；`save → ShellQueryStore.insert/update`

## ShellRedisQueryDataTab
> 文件: cn/oyzh/easyshell/tabs/redis/query/ShellRedisQueryDataTab.java
- 职责：查询数据结果标签页，封装结果数据到内容控制器的初始化。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 仅继承 RichTab |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(Object object)` | 初始化查询数据 | `super.flush()`；集合调用 `controller().init(collection)`，否则 `controller().init(object)` |
  | `protected String url()` | FXML 路径 | `/tabs/redis/query/shellRedisQueryDataTab.fxml` |
  | `protected ShellRedisQueryDataTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public String getTabTitle()` | 标题 | `I18nHelper.data()` |
  | `public void initNode()` | 初始化节点 | `setClosable(false)` 后 `super` |
  | `public static ShellRedisQueryDataTab of(Object object)` | 工厂方法 | 创建后 `init(object)` |
- 调用链：`of → init → controller().init → ShellRedisQueryDataTabController`

## ShellRedisQueryDataTabController
> 文件: cn/oyzh/easyshell/tabs/redis/query/ShellRedisQueryDataTabController.java
- 职责：查询数据结果内容组件，将 Redis 返回对象解析为序号-值表格。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dataTable | FXTableView<KeyValueProperty<Integer,Object>> | 数据表格（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(Collection<?> list)` | 初始化集合数据 | 逐项 `parseObject`（序号从 1 递增）后 `dataTable.setItem` |
  | `public void init(Object o)` | 初始化单对象数据 | `parseObject(o, 1, data)` 后 `dataTable.setItem` |
  | `private void parseObject(Object o, int index, List<...> data)` | 解析对象为表格行 | switch 分派：byte[]→`SafeEncoder.encode`；Collection/KeyValue→`SafeEncoder.encodeObject`；null→空串；其它→`toString` |
- 调用链：`init → parseObject → dataTable.setItem`

## ShellRedisQueryMsgTab
> 文件: cn/oyzh/easyshell/tabs/redis/query/ShellRedisQueryMsgTab.java
- 职责：查询消息结果标签页，封装查询参数与结果到内容控制器。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 仅继承 RichTab |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellRedisQueryParam param, ShellRedisQueryResult result)` | 初始化查询消息 | `super.flush()` 后 `controller().init(param, result)` |
  | `protected String url()` | FXML 路径 | `/tabs/redis/query/shellRedisQueryMsgTab.fxml` |
  | `protected ShellRedisQueryMsgTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public String getTabTitle()` | 标题 | `I18nHelper.message()` |
  | `public void initNode()` | 初始化节点 | `setClosable(false)` 后 `super` |
  | `public static ShellRedisQueryMsgTab of(ShellRedisQueryParam param, ShellRedisQueryResult result)` | 工厂方法 | 创建后 `init(param, result)` |
- 调用链：`of → init → controller().init → ShellRedisQueryMsgTabController`

## ShellRedisQueryMsgTabController
> 文件: cn/oyzh/easyshell/tabs/redis/query/ShellRedisQueryMsgTabController.java
- 职责：查询消息结果内容组件，输出查询语句、结果消息与耗时。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | msg | ReadOnlyTextArea | 消息内容文本域（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellRedisQueryParam param, ShellRedisQueryResult result)` | 初始化查询消息 | `msg.appendLine` 依次追加 `param.getContent()`、`result.getMessage()`、耗时 `result.costSeconds()` |
- 调用链：`init → msg.appendLine`

## ShellRedisServerTabController
> 文件: cn/oyzh/easyshell/tabs/redis/server/ShellRedisServerTabController.java
- 职责：Redis 服务标签内容组件，汇总服务信息、客户端信息、订阅发布、慢查日志与图表，并管理自动刷新任务。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根 tab（`@FXML`） |
  | client | ShellRedisClient | redis 客户端 |
  | pubsub | FXTab | 发布及订阅 tab（`@FXML`） |
  | slowlog | FXTab | 慢查日志 tab（`@FXML`） |
  | clientInfo | FXTab | 客户端信息 tab（`@FXML`） |
  | tabPane | FXTabPane | tab 面板（`@FXML`） |
  | pubsubController | ShellRedisPubsubTabController | 订阅发布控制器（`@FXML`） |
  | slowlogController | ShellRedisSlowlogTabController | 慢查日志控制器（`@FXML`） |
  | serverInfoController | ShellRedisServerInfoTabController | 服务信息控制器（`@FXML`） |
  | clientInfoController | ShellRedisClientInfoTabController | 客户端信息控制器（`@FXML`） |
  | aggregationController | ShellRedisAggregationTabController | 汇总图表控制器（`@FXML`） |
  | propTable | FXTableView<ShellRedisServerItem> | 属性表格（`@FXML`） |
  | refreshTask | Future<?> | 刷新任务句柄 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellRedisClient getClient()` | 获取客户端 | 返回 `client` |
  | `public void init(ShellRedisClient client)` | 初始化 | 非哨兵模式初始化 pubsub/slowlog/clientInfo 控制器；哨兵模式移除对应三个 tab |
  | `private void initRefreshTask()` | 初始化自动刷新任务 | `ExecutorUtil.start(this::renderPane, 0, 3000)` 每 3 秒刷新 |
  | `public void closeRefreshTask()` | 关闭刷新任务 | `ExecutorUtil.cancel(refreshTask)` |
  | `private synchronized void renderPane()` | 渲染主面板 | 解析 `client.info(null)` 为 `ShellRedisInfoProp`；表格空则新建 `ShellRedisServerItem`（版本、role），否则复用；`serverItem.init`，并 `serverInfoController.init`/`aggregationController.init` |
  | `public void onTabInit(FXTab tab)` | tab 初始化 | `root` 选中时 `initRefreshTask`，取消选中时 `closeRefreshTask` |
  | `public void onTabClosed(Event event)` | tab 关闭 | `super` 后 `closeRefreshTask` |
  | `public List<? extends RichTabController> getSubControllers()` | 子控制器集合 | 返回 aggregation/pubsub/slowlog/serverInfo/clientInfo |
  | `public void destroy()` | 销毁 | `closeRefreshTask()` 后 `super.destroy()` |
- 调用链：`initRefreshTask → ExecutorUtil.start → renderPane → client.info → serverInfoController.init/aggregationController.init`

## ShellRedisServerInfoTabController
> 文件: cn/oyzh/easyshell/tabs/redis/server/ShellRedisServerInfoTabController.java
- 职责：服务信息内容组件，将信息属性按分组渲染为多个名称-值表格 tab。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tabPane | FXTabPane | tab 面板（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellRedisInfoProp prop)` | 初始化 | `initPropPane(prop)` |
  | `private void initPropPane(ShellRedisInfoProp prop)` | 初始化属性面板 | 对排序后的分组 `prop.groups()` 逐一 `initPropTab` |
  | `private void initPropTab(ShellRedisInfoProp prop, String group)` | 初始化属性 tab | 取 `prop.getProps(group)`；按 id `prop-group` 查找或新建 `FXTab` 与两列表格（名称/值，`PropertyValueFactory`，绑定双击复制）；再遍历 key `initPropItem` |
  | `private void initPropItem(TableView<...> tableView, String name, String value)` | 初始化属性行 | 存在同名则不一致时更新值，否则新增 `ShellRedisInfoPropItem` |
- 调用链：`init → initPropPane → initPropTab → initPropItem`

## ShellRedisAggregationTabController
> 文件: cn/oyzh/easyshell/tabs/redis/server/ShellRedisAggregationTabController.java
- 职责：服务聚合信息内容组件，将内存、客户端、网络、指令的实时指标追加到折线图。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | clientChart | FXLineChart<String,Number> | 客户端图表（`@FXML`） |
  | memoryChart | FXLineChart<String,Number> | 内存图表（`@FXML`） |
  | commandChart | FXLineChart<String,Number> | 指令图表（`@FXML`） |
  | networkChart | FXLineChart<String,Number> | 网络图表（`@FXML`） |
  | DATE_FORMAT | SimpleDateFormat | 时间格式化（static final，`HH:mm:ss`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellRedisInfoProp prop)` | 初始化 | 依次初始化客户端/内存/指令/网络图表 |
  | `private void initMemoryChart(ShellRedisInfoProp prop)` | 内存图表 | 序列不存在则创建（名称“已用内存”）；`usedMemory/1024/1024` 后 `ChartHelper.addOrUpdateData(...,10)` |
  | `private void initClientChart(ShellRedisInfoProp prop)` | 客户端图表 | 使用 `getConnectedClients()` 追加数据点 |
  | `private void initNetworkChart(ShellRedisInfoProp prop)` | 网络图表 | 维护入/出两条序列，使用 `getInstantaneousInputKbps/OutputKbps` 追加数据点 |
  | `private void initCommandChart(ShellRedisInfoProp prop)` | 指令图表 | 使用 `getInstantaneousOpsPerSec()` 追加数据点 |
- 调用链：`init → initClientChart/initMemoryChart/initCommandChart/initNetworkChart → ChartHelper.addOrUpdateData`

## ShellRedisClientInfoTabController
> 文件: cn/oyzh/easyshell/tabs/redis/server/ShellRedisClientInfoTabController.java
- 职责：客户端信息内容组件，解析 `clientList` 输出为表格并支持刷新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | ShellRedisClient | redis 客户端 |
  | listTable | FXTableView<ShellRedisClientItem> | 表格组件（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellRedisClient getClient()` | 获取客户端 | 返回 `client` |
  | `public void init(ShellRedisClient client)` | 初始化 | 保存 client 后 `initClientList()` |
  | `private void refresh()` | 刷新 | `initClientList()` |
  | `private void initClientList()` | 初始化客户端信息 | `client.clientList()` 按行 `ShellRedisClientItem.from(l)` 并设置递增序号，`listTable.getItems().setAll` |
- 调用链：`init/refresh → initClientList → ShellRedisClient.clientList`

## ShellRedisPubsubTabController
> 文件: cn/oyzh/easyshell/tabs/redis/server/ShellRedisPubsubTabController.java
- 职责：订阅发布内容组件，列出当前所有活跃通道并支持刷新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | ShellRedisClient | redis 客户端 |
  | listTable | FXTableView<ShellRedisPubsubItem> | 表格组件（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellRedisClient getClient()` | 获取客户端 | 返回 `client` |
  | `public void init(ShellRedisClient client)` | 初始化 | 保存 client 后 `initPubsub()` |
  | `private void refresh()` | 刷新 | `initPubsub()` |
  | `private void initPubsub()` | 初始化订阅发布 | `client.pubsubChannels("*")` 结果逐个封装 `ShellRedisPubsubItem`（序号、通道）并 `setAll` |
- 调用链：`init/refresh → initPubsub → ShellRedisClient.pubsubChannels`

## ShellRedisSlowlogTabController
> 文件: cn/oyzh/easyshell/tabs/redis/server/ShellRedisSlowlogTabController.java
- 职责：慢查日志内容组件，拉取慢查询日志并倒序展示。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | ShellRedisClient | redis 客户端 |
  | listTable | FXTableView<ShellRedisSlowlogItem> | 表格组件（`@FXML`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellRedisClient getClient()` | 获取客户端 | 返回 `client` |
  | `public void init(ShellRedisClient client)` | 初始化 | 保存 client 后 `initSlowlog()` |
  | `private void refresh()` | 刷新 | `initSlowlog()` |
  | `private void initSlowlog()` | 初始化慢查日志 | `client.slowlogGet(1024)` 逐条 `ShellRedisSlowlogItem.from`，`Collections.reverse` 后 `listTable.setItem` |
- 调用链：`init/refresh → initSlowlog → ShellRedisClient.slowlogGet`
