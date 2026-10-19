# easyshell 标签页模块代码审查文档 — ZooKeeper

> 范围：`cn/oyzh/easyshell/tabs/zk/`（递归，节点 / 查询 / 服务端 / 认证 / 终端）。
> 总览与通用标签页见 [tabs.md](./tabs.md)。说明：仅新增文档，未改动任何 `.java`。

## ZooKeeper 标签页

> 说明：`zk` 包按子域划分为四组。根包负责标签页装配与全局客户端管理；`zk/node` 为节点信息子标签（data/acl/stat/quota/hex），操作“当前树中激活节点”而非独立查询；`zk/query` 为查询类标签（node/data/acl/stat/quota/env/msg/whoami），每个标签只渲染一次查询结果且不可关闭；`zk/server` 为服务端信息标签（cluster/conf/envi/local/srvr/stat/aggregation），由定时任务周期性刷新并绘制折线图；`zk/auth` 为连接级认证配置管理；`zk/terminal` 为交互式命令行面板。

## ShellZKTab
> 文件: cn/oyzh/easyshell/tabs/zk/ShellZKTab.java
- 职责：zk 连接标签页，展示连接名与 OS 类型图标，并委托控制器建立 ZK 客户端。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 原带 `ShellConnect` 参数的构造器与 `onTabClosed` 重写均被注释 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public String getTabTitle()` | 标签标题 | `shellConnect().getName() + "(" + getType().toUpperCase() + ")"` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `ShellOsTypeComboBox.getGlyph(getOsType())` 并设默认光标后 `setGraphic` |
  | `protected String url()` | FXML 路径 | `/tabs/zk/shellZKTab.fxml` |
  | `protected ShellZKTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public void init(ShellConnect connect)` | 初始化连接 | `controller().init(connect)` 建客户端，再 `super.init(connect)` 刷新图标，异常仅打印 |
  | `public ShellBaseClient client()` | 获取客户端 | 委托 `controller().getClient()` |
  | `public static ShellZKTab of(ShellConnect connect)` | 工厂方法 | `new` 后 `init(connect)` 返回 |
- 调用链：`ShellTabPane.connectionOpened → ShellZKTab.of → init → ShellZKTabController.init`

## ShellZKTabController
> 文件: cn/oyzh/easyshell/tabs/zk/ShellZKTabController.java
- 职责：zk 主标签页内容控制器，负责创建/启动 ZK 客户端并装配 node/query/auth/server/terminal 五个子控制器。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | ShellZKClient | 当前 zk 客户端 |
  | nodeController | ShellZKNodeTabController | FXML 注入，节点信息子控制器 |
  | queryController | ShellZKQueryTabController | FXML 注入，查询子控制器 |
  | authController | ShellZKAuthTabController | FXML 注入，认证子控制器 |
  | serverController | ShellZKServerTabController | FXML 注入，服务端信息子控制器 |
  | terminalController | ShellZKTerminalTabController | FXML 注入，命令行子控制器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public List<? extends RichTabController> getSubControllers()` | 返回子控制器列表 | `List.of(nodeController, queryController, authController, serverController, terminalController)` |
  | `public ShellConnect shellConnect()` | 获取连接信息 | `client.getShellConnect()` |
  | `public void init(ShellConnect connect)` | 初始化 | `ShellClientUtil.newClient(connect)`；`addStateListener` 对 `ShellConnState.INTERRUPTED` 弹警告；`StageManager.showMask` 内 `client.start()`，未连接则 `close`/警告/`closeTab`，成功则 `hideLeft()` 并依次 `nodeController.init`、`queryController.init`、`authController.init`、`serverController.init`、`terminalController.init(client.forkClient())`，异常 `MessageBox.exception` 后 `closeTab` |
  | `public ShellZKClient getClient()` | 获取客户端 | 返回 `client` |
  | `public void onTabClosed(Event event)` | 关闭清理 | `super.onTabClosed(event)` 后 `IOUtil.closeAsync(client)` |
- 调用链：`ShellZKTab.init → ShellZKTabController.init → client.start → node/query/auth/server/terminal.init`

## ShellZKAuthTabController
> 文件: cn/oyzh/easyshell/tabs/zk/auth/ShellZKAuthTabController.java
- 职责：zk 认证标签页内容控制器，管理连接的 digest 认证账号列表（查询/新增/删除/复制）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | FXML 注入，根标签节点 |
  | authTable | ShellZKAuthTableView | FXML 注入，认证列表视图 |
  | authSearchKW | ClearableTextField | FXML 注入，认证过滤关键字输入框 |
  | client | ShellZKClient | 当前 zk 客户端 |
  | authStore | ShellZKAuthStore | 认证配置储存（final），取 `ShellZKAuthStore.INSTANCE` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellZKClient client)` | 设置客户端 | `this.client = client` |
  | `public ShellZKClient client()` | 获取客户端 | 返回 `client` |
  | `public ShellConnect shellConnect()` | 获取连接 | `client.getShellConnect()` |
  | `private void initAuthDataList()` | 初始化认证数据 | `authTable.init(client.iid(), authSearchKW.getText())`（旧的分支判断逻辑被注释） |
  | `private void refreshAuth()` | 刷新认证（FXML） | `initAuthDataList()` |
  | `private void addAuth()` | 添加认证（FXML） | `ShellZKViewFactory.zkAuthAdd(shellConnect())` 取弹窗 `auth` 属性，成功后 `authTable.refreshAuths()` |
  | `private void deleteAuth()` | 删除认证（FXML） | 取选中项，`MessageBox.confirm` 后逐个 `authStore.delete(uid)`，再 `refreshAuths()` |
  | `private void copyAuth()` | 复制认证（FXML） | 拼接用户名/密码文本，`ClipboardUtil.setStringAndTip` |
  | `protected void bindListeners()` | 绑定监听器 | `authSearchKW.addTextChangeListener` 变化时 `initAuthDataList()` |
  | `public void onTabInit(FXTab tab)` | 标签页初始化 | `root.selectedProperty().subscribe`：被选中时 `initAuthDataList()` |
- 调用链：`onTabInit → root.selectedProperty → initAuthDataList → authTable.init`

## ShellZKNodeTabController
> 文件: cn/oyzh/easyshell/tabs/zk/node/ShellZKNodeTabController.java
- 职责：节点信息主控制器，左侧节点树 + 右侧 data/hex/stat/acl/quota 子标签，负责节点选择、过滤、刷新与导入导出。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXSplitPane | FXML 注入，左右分割面板 |
  | leftBox | FXVBox | FXML 注入，左侧容器 |
  | tabPane | FXTabPane | FXML 注入，右侧节点信息标签面板 |
  | treeView | ShellZKTreeView | FXML 注入，节点树 |
  | filterType | ShellZKNodeFilterTypeComboBox | FXML 注入，过滤类型选择框 |
  | filterKW | FilterTextField | FXML 注入，过滤关键字输入框 |
  | nodePath | FXLabel | FXML 注入，当前节点路径标签 |
  | activeItem | ShellZKNodeTreeItem | 当前激活的节点（transient） |
  | client | ShellZKClient | zk 客户端 |
  | dataTabController | ShellZKNodeDataTabController | FXML 注入，数据子控制器 |
  | hexTabController | ShellZKNodeHexTabController | FXML 注入，hex 子控制器 |
  | statTabController | ShellZKNodeStatTabController | FXML 注入，状态子控制器 |
  | aclTabController | ShellZKNodeACLTabController | FXML 注入，权限子控制器 |
  | quotaTabController | ShellZKNodeQuotaTabController | FXML 注入，配额子控制器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellZKNodeTreeItem getActiveItem()` | 获取激活节点 | 返回 `activeItem` |
  | `public void init(ShellZKClient client)` | 初始化 | `treeView.client(client)`；`StageManager.showMask` 内 `treeView.loadRoot()`，异常 `closeTab`；`TaskManager.startDelay(...,3000)` 检测 `client.isInvalid()` 则 `closeTab` |
  | `private void initItem(TreeItem<?> treeItem)` | 处理树选中项 | 记录 `activeItem` 并更新 `nodePath`；有激活项则 `StageManager.showMask(initNode)`、`tabPane.enable()`、`FXUtil.runLater(checkStatus,100)`，否则 `tabPane.disable()`；最后 `treeView.refresh()`、`flushTab()` |
  | `private void initNode()` | 按当前标签初始化节点 | 依 `tabPane.getSelectTabId()` 分派 `dataTab/hexTab/statTab/aclTab/quotaTab` 对应控制器的 `initData/initHex/initStat/initACL/initQuota` |
  | `private void refreshItem()` | 刷新节点 | `showMask` 内 `activeItem.refreshNode()` → `initNode()` → `flushTab()` |
  | `private void checkStatus()` | 检查节点状态 | `activeItem.isNeedAuth()` 时 `MessageBox.confirm`，确认后 `authNode()` |
  | `protected void bindListeners()` | 绑定监听器 | `treeView.selectItemChanged(this::initItem)`；`filterType`/`filterKW` 各属性变化触发 `doFilter`；`tabPane.selectedItemChanged` 触发 `initNode`；注册 `KeyHandler`（主修饰键+F 聚焦搜索框） |
  | `protected Window window()` | 获取当前窗口 | `getActiveItem().window()` |
  | `private void doFilter()` | 执行过滤 | 读关键字/大小写/全字/类型；`treeView.setHighlightMatchCase`、`setHighlight`；设置 `getItemFilter()` 的 kw/type/matchCase/wholeWord 后 `ThreadUtil.start(treeView::filter)` |
  | `private void positionNode()` | 定位节点（FXML） | `treeView.positionItem()` |
  | `private void importData()` | 导入数据（FXML） | `ShellZKViewFactory.zkImportData(shellConnect)` |
  | `private void exportData()` | 导出数据（FXML） | `ShellZKViewFactory.zkExportData(shellConnect, null)` |
  | `private void transportData()` | 传输数据（FXML） | `ShellZKViewFactory.zkTransportData(shellConnect)` |
  | `public ShellZKClient getClient()` | 获取客户端 | 返回 `client` |
  | `private void onHistoryRestore(ShellZKHistoryRestoreEvent event)` | 历史恢复事件（`@EventSubscribe`） | 事件客户端与节点路径匹配当前 `activeItem` 时 `refreshItem()` |
  | `public List<? extends RichTabController> getSubControllers()` | 子控制器列表 | `List.of(dataTabController, hexTabController, statTabController, aclTabController, quotaTabController)` |
  | `private void copyNodePath()` | 复制节点路径（FXML） | `ClipboardUtil.setStringAndTip(activeItem.decodeNodePath())` |
- 调用链：`init → treeView.loadRoot`；`selectItemChanged → initItem → initNode → dataTabController.initData/...`；`onHistoryRestore → refreshItem`

## ShellZKNodeDataTabController
> 文件: cn/oyzh/easyshell/tabs/zk/node/ShellZKNodeDataTabController.java
- 职责：节点数据子标签，编辑并保存激活节点的数据，支持编码切换、格式检测、撤销重做、导出文件与转二维码。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | filter | HighlightTextField | FXML 注入，内容过滤/高亮输入框 |
  | charset | CharsetComboBox | FXML 注入，字符集选择框 |
  | dataSize | FXText | FXML 注入，数据大小文本 |
  | loadTime | FXText | FXML 注入，加载耗时文本 |
  | dataSave | SVGGlyph | FXML 注入，保存按钮图标 |
  | dataUndo | SVGGlyph | FXML 注入，撤销按钮图标 |
  | dataRedo | SVGGlyph | FXML 注入，重做按钮图标 |
  | nodeData | ShellDataEditor | FXML 注入，数据编辑器 |
  | format | EditorFormatTypeComboBox | FXML 注入（protected），数据格式选择框 |
  | dataTab | FXTab | FXML 注入，数据面板 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private void copyNode()` | 复制节点路径及数据（FXML） | 拼接 `decodeNodePath()` 与 `new String(getData())`，`ClipboardUtil.setStringAndTip` |
  | `private void saveBinaryFile()` | 保存为二进制文件（FXML） | `FileChooserHelper.save` 选文件后 `FileUtil.writeBytes(activeItem.getNodeData(), file)` |
  | `private void reloadData()` | 刷新节点数据（FXML） | 有未保存改动时 `MessageBox.confirm` 拦截；`showMask` 内 `refreshData()`、`refresh()`、`showData()`、`flushTabGraphicColor()`、`nodeData.forgetHistory()`、`dataSave.disable()` |
  | `private void saveNodeData()` | 保存节点数据（FXML） | `isDataUnsaved()` 时 `RenderService.submit` 内 `activeItem.saveData()`，成功后 `dataSave.disable()`、`flushDataSize()`、`flushTabGraphicColor()` |
  | `private void dataUndo()` | 撤销（FXML） | `nodeData.undo()` 并 `requestFocus()` |
  | `private void dataRedo()` | 重做（FXML） | `nodeData.redo()` 并 `requestFocus()` |
  | `private void pasteData()` | 粘贴（FXML） | `nodeData.paste()` |
  | `private void clearData()` | 清空（FXML） | `nodeData.clear()` |
  | `private void node2QRCode(MouseEvent event)` | 转二维码（FXML） | `PopupManager.parsePopup(ShellZKNodeQRCodePopupController)`，设置 `zkNode`/`nodeData` 属性后展示 |
  | `private void onNodeDataKeyPressed(KeyEvent e)` | 编辑器按键（FXML） | `KeyboardUtil.isCtrlS` 时 `saveNodeData()` 并 `consume` |
  | `protected void showData()` | 显示数据 | 启用 `dataTab` 的 `dataToBig` 分组；`TextUtil.changeCharset` 转码后 `nodeData.showDetectData`，将结果选入 `format` |
  | `public void initData()` | 初始化数据 | `showData()`、`flushDataSize()`、`nodeData.forgetHistory()`、禁用 undo/redo；有激活项时按 `isDataUnsaved()` 设 `dataSave` 禁用态并显示 `loadTime` |
  | `private void flushDataSize()` | 刷新数据大小 | `dataSize.text(size() + " : " + activeItem.dataSizeInfo())` |
  | `protected void bindListeners()` | 绑定监听器 | undo/redo 属性绑定按钮禁用态；`charset` 变化 `showData()`；`EditorUtil.bindHighlight`；`format` 变化 `nodeData.setFormatType`；编辑器内容变化时启用保存、写回 `activeItem.nodeData(bytes)` 并刷新 tab 颜色 |
  | `private void searchNext()` | 搜索下一个（FXML） | `EditorUtil.searchNextHighlight(nodeData, filter)` |
  | `private ShellZKNodeTreeItem activeItem()` | 获取激活节点 | `parent().getActiveItem()` |
  | `public ShellZKNodeTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
- 调用链：`initData → showData → nodeData.showDetectData`；`saveNodeData → activeItem.saveData`；`node2QRCode → ShellZKNodeQRCodePopupController`

## ShellZKNodeHexTabController
> 文件: cn/oyzh/easyshell/tabs/zk/node/ShellZKNodeHexTabController.java
- 职责：节点十六进制子标签，将激活节点数据写入临时文件后用 HexView 展示。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | hexView | HexView | FXML 注入，hex 视图组件 |
  | statusLabel | HexStatusLabel | FXML 注入，hex 状态标签 |
  | file | File | 当前临时文件 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void initHex()` | 初始化数据 | 有激活项时首次创建 `ShellConst.getCachePath()` 下的 `.hex` 临时文件，`FileUtil.writeBytes(getData(), file)` 后 `hexView.enable()`、`hexView.openFile(file)`、`statusLabel.init(hexView)`；无激活项则关闭/禁用视图并 `statusLabel.stop()` |
  | `private ShellZKNodeTreeItem activeItem()` | 获取激活节点 | `parent().getActiveItem()` |
  | `public ShellZKNodeTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
  | `public void destroy()` | 销毁清理 | `FileUtil.del(file)` 后 `super.destroy()`（对视图的销毁被注释） |
- 调用链：`initHex → FileUtil.writeBytes → hexView.openFile`

## ShellZKNodeStatTabController
> 文件: cn/oyzh/easyshell/tabs/zk/node/ShellZKNodeStatTabController.java
- 职责：节点状态子标签，以标签行展示 ZooKeeper Stat 信息，支持友好键名切换与复制。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | statBox | FXVBox | FXML 注入，属性行容器 |
  | statViewSwitch | FXToggleSwitch | FXML 注入，友好/原始视图切换开关 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private void copyStat()` | 复制状态（FXML） | 遍历 `activeItem.statInfos()`，按开关状态取 `FriendlyInfo` 的 name/value 拼接后 `ClipboardUtil.setStringAndTip` |
  | `private void reloadStat()` | 刷新状态（FXML） | `activeItem.refreshStat()` 后 `initStat()` |
  | `public void initStat()` | 初始化状态 | 取 `statInfos` 非空时 `statBox.lookupAll(".statItem")`，逐行写入 `FriendlyInfo` 的 name/value（`FXUtil.runLater`） |
  | `protected void bindListeners()` | 绑定监听器 | `statViewSwitch.selectedChanged` 触发 `initStat()` |
  | `private ShellZKNodeTreeItem activeItem()` | 获取激活节点 | `parent().getActiveItem()` |
  | `public ShellZKNodeTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
- 调用链：`initStat → activeItem.statInfos → statBox.lookupAll`

## ShellZKNodeACLTabController
> 文件: cn/oyzh/easyshell/tabs/zk/node/ShellZKNodeACLTabController.java
- 职责：节点权限子标签，展示并维护激活节点的 ACL，支持友好视图切换与增删改复制。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | aclViewSwitch | FXToggleSwitch | FXML 注入，友好/原始权限视图切换开关 |
  | aclTableView | ShellZKACLTableView | FXML 注入，ACL 表视图 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void reloadACL()` | 重新载入权限（FXML） | `activeItem().refreshACL()` 后 `initACL()`，`MessageBox.okToast` |
  | `private void addACL()` | 添加权限（FXML） | `ShellZKViewFactory.zkAddACL(activeItem(), client())`，`result` 为真时 `reloadACL()` |
  | `private void copyACL()` | 复制权限（FXML） | 取选中项，按开关拼接 id/scheme/perms 的 name 与 value，`ClipboardUtil.setStringAndTip` |
  | `private void updateACL()` | 修改权限（FXML） | `ShellZKViewFactory.zkUpdateACL(...)`，成功时 `reloadACL()` |
  | `private void deleteACL()` | 删除权限（FXML） | 仅剩一条时告警；`MessageBox.confirm` 后 `activeItem().deleteACL(acl)`，成功则 `removeItem` 并移出 `acl()` |
  | `private void renderACLView(List<ShellZKACL> aclList)` | 渲染权限列表 | 逐条构造 `ShellZKACLControl`（id/perms/friendly/authed=`client().isAuthed(idVal())`），`aclTableView.setItem` |
  | `public void initACL()` | 初始化权限 | 无激活项返回；ACL 为空则禁用开关并 `clearItems()`，否则启用开关并 `renderACLView(activeItem().acl())` |
  | `protected void bindListeners()` | 绑定监听器 | `aclViewSwitch.selectedChanged` 触发 `initACL()` |
  | `private ShellZKNodeTreeItem activeItem()` | 获取激活节点 | `parent().getActiveItem()` |
  | `private ShellZKClient client()` | 获取客户端 | `parent().getClient()` |
  | `public ShellZKNodeTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
  | `public void initialize(URL location, ResourceBundle resourceBundle)` | 初始化 | `aclTableView` 注册 add/copy/edit/delete 四个动作回调 |
- 调用链：`initACL → renderACLView → ShellZKACLControl`；`addACL → ShellZKViewFactory.zkAddACL → reloadACL`

## ShellZKNodeQuotaTabController
> 文件: cn/oyzh/easyshell/tabs/zk/node/ShellZKNodeQuotaTabController.java
- 职责：节点配额子标签，展示并保存节点子节点数与数据大小配额（根节点禁用）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | quotaTab | FXTab | FXML 注入（protected），配额面板 |
  | quotaCount | NumberTextField | FXML 注入（protected），子节点数量配额输入 |
  | quotaBytes | NumberTextField | FXML 注入（protected），节点数据大小配额输入 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private void reloadQuota()` | 重载配额（FXML） | `activeItem().refreshQuota()` 后 `initQuota()`，`MessageBox.okToast` |
  | `private void copyQuota()` | 复制配额（FXML） | 取 `activeItem().quota()`，空则输出 -1，否则输出 count/bytes，`ClipboardUtil.setStringAndTip` |
  | `public void initQuota() throws Exception` | 初始化配额 | 根节点则禁用 `quotaTab` 内容；否则启用并回填 `quotaCount`/`quotaBytes`（无配额填 -1） |
  | `private void saveQuota()` | 保存配额（FXML） | `activeItem().saveQuota(quotaBytes.getLongValue(), quotaCount.getIntValue())`，`MessageBox.okToast` |
  | `private void clearQuotaCount()` | 清除数量配额（FXML） | `quotaCount.setValue(-1)` |
  | `private void clearQuotaBytes()` | 清除大小配额（FXML） | `quotaBytes.setValue(-1)` |
  | `private ShellZKNodeTreeItem activeItem()` | 获取激活节点 | `parent().getActiveItem()` |
  | `public ShellZKNodeTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
- 调用链：`initQuota → activeItem.quota`；`saveQuota → activeItem.saveQuota`

## ShellZKQueryTabController
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryTabController.java
- 职责：查询主控制器，提供 zk 四字命令/路径查询编辑区与树形查询历史，运行后按命令类型装配结果标签。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | query | ShellQuery | 当前编辑的查询对象 |
  | zkClient | ShellZKClient | zk 客户端 |
  | content | ShellZKQueryEditor | FXML 注入，查询内容编辑器 |
  | splitPane | FXSplitPane | FXML 注入，分割面板 |
  | resultTabPane | FXTabPane | FXML 注入，结果标签面板 |
  | queryTreeView | ShellQueryTreeView | FXML 注入，查询历史树视图 |
  | queryStore | ShellQueryStore | 查询存储（final），取 `ShellQueryStore.INSTANCE` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellConnect shellConnect()` | 获取连接 | `zkClient.getShellConnect()` |
  | `public void init(ShellZKClient client)` | 初始化 | `content.setClient(client)`，`queryTreeView.setIid(client.iid())` |
  | `private void save()` | 保存查询（FXML） | 无 query 时 `MessageBox.prompt` 取名并 `queryStore.insert` + `queryTreeView.addQuery/selectLast`，否则 `update`；最后 `setUnsaved(false)` |
  | `private void setUnsaved(boolean unsaved)` | 设置未保存状态 | 选中项为 `ShellQueryTreeItem` 时 `setUnsaved` 并 `refresh` |
  | `private void run()` | 运行查询（FXML） | `showMask` 内构造 `ShellZKQueryParam`（内容），`zkClient.query(param)`；展开分割面板并清空结果面板后，按 `param` 类型分派：`isGet/ls/ls2/getEphemerals/whoami/srvr|envi|mntr|conf|stat4/set|setACL/getACL/create|sync|setQuota|rmr|deleteall|delete/stat/listquota` 分别追加 `ShellZKQueryMsgTab/DataTab/NodeTab/StatTab/WhoamiTab/EnvTab/ACLTab/QuotaTab` 等结果页并选中对应索引 |
  | `private void onContentKeyPressed(KeyEvent event)` | 内容按键（FXML） | `Ctrl+S` 保存、`Ctrl+R` 运行 |
  | `protected void bindListeners()` | 绑定监听器 | 内容变化且与 `query.getContent()` 不等时 `setUnsaved(true)`；`queryTreeView` 选中变化 → `doEdit`；注册 `setAddCallback/setEditCallback/setDeleteCallback` |
  | `private void doAdd(ShellQuery query)` | 新增查询 | 记录 `query`，`content.setText(query.getContent())` 并 `selectLast()` |
  | `private void doEdit(ShellQuery query)` | 编辑查询 | 记录 `query`，空则 `content.clear()`，否则写入内容 |
  | `private void doDelete(ShellQuery query)` | 删除查询 | 删除的是当前 query 时清空查询与内容 |
- 调用链：`run → zkClient.query → ShellZKQueryResult → ShellZKQueryMsgTab/DataTab/... of()`

## ShellZKQueryMsgTab
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryMsgTab.java
- 职责：查询消息结果标签页，展示查询语句、返回消息与耗时；所有查询都会生成此标签（结果页第 0 个）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | | 
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellZKQueryParam param, ShellZKQueryResult result)` | 初始化消息数据 | `super.flush()` 后 `controller().init(param, result)` |
  | `protected String url()` | FXML 路径 | `/tabs/zk/query/shellZKQueryMsgTab.fxml` |
  | `protected ShellZKQueryMsgTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.message()` |
  | `public void initNode()` | 初始化节点 | `setClosable(false)` 后 `super.initNode()` |
  | `public static ShellZKQueryMsgTab of(ShellZKQueryParam param, ShellZKQueryResult result)` | 工厂方法 | `new` 后 `init(...)` 返回 |
- 调用链：`ShellZKQueryTabController.run → ShellZKQueryMsgTab.of → init → controller().init`

## ShellZKQueryMsgTabController
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryMsgTabController.java
- 职责：查询消息内容控制器，将查询语句、返回消息与耗时追加到只读文本域，并对纯文本类四字命令兜底输出。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | msg | ReadOnlyTextArea | FXML 注入，消息文本域 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellZKQueryParam param, ShellZKQueryResult result)` | 初始化消息 | 追加 `param.getContent()`、`"> " + result.getMessage()`、耗时；成功且为 `getAllChildrenNumber/ruok/crst/cons/srst/wchc/wchs/wchp/dump/reqs/dirs` 等兜底命令时追加 `result.getResult()` |
- 调用链：`ShellZKQueryMsgTab.init → ShellZKQueryMsgTabController.init → msg.appendLine`

## ShellZKQueryDataTab
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryDataTab.java
- 职责：查询数据结果标签页，展示 get 命令返回的节点数据，可编辑并保存。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 原构造器被注释 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(String path, byte[] data, ShellZKClient zkClient)` | 初始化数据 | `super.flush()` 后 `controller().init(path, data, zkClient)` |
  | `protected String url()` | FXML 路径 | `/tabs/zk/query/shellZKQueryDataTab.fxml` |
  | `protected ShellZKQueryDataTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.data()` |
  | `public void initNode()` | 初始化节点 | `setClosable(false)` 后 `super.initNode()` |
  | `public static ShellZKQueryDataTab of(String path, byte[] data, ShellZKClient zkClient)` | 工厂方法 | `new` 后 `init(...)` 返回 |
- 调用链：`ShellZKQueryTabController.run(isGet) → ShellZKQueryDataTab.of → controller().init`

## ShellZKQueryDataTabController
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryDataTabController.java
- 职责：查询数据内容控制器，编辑 get 结果并可写回 zk（保存时记录历史）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | path | String | zk 节点路径 |
  | zkClient | ShellZKClient | zk 客户端 |
  | save | SVGGlyph | FXML 注入，保存按钮图标 |
  | undo | SVGGlyph | FXML 注入，撤销按钮图标 |
  | redo | SVGGlyph | FXML 注入，重做按钮图标 |
  | filter | HighlightTextField | FXML 注入，高亮过滤输入框 |
  | data | ShellDataEditor | FXML 注入，数据编辑器 |
  | format | EditorFormatTypeComboBox | FXML 注入，数据格式选择框 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(String path, byte[] bytes, ShellZKClient zkClient)` | 初始化数据 | 记录 path/zkClient；`data.showDetectData` 检测格式并选入 `format`，`forgetHistory`；绑定内容变化启用保存、undo/redo 禁用态，并将 `filter` 的文本/regex/matchCase 属性绑定到 `data` |
  | `private void save()` | 保存（FXML） | `save.disable()`；`zkClient.setData(path, bytes)` 后 `ShellZKDataUtil.addHistory(path, bytes, zkClient)` |
  | `private void undo()` | 撤销（FXML） | `data.undo()` 并 `requestFocus()` |
  | `private void redo()` | 重做（FXML） | `data.undo()`（复用 undo，存在笔误）并 `requestFocus()` |
  | `private void onDataKeyPressed(KeyEvent event)` | 数据按键（FXML） | `Ctrl+S` 触发 `save()` |
- 调用链：`save → zkClient.setData → ShellZKDataUtil.addHistory`

## ShellZKQueryNodeTab
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryNodeTab.java
- 职责：查询节点列表结果标签页，展示 ls 命令返回的子节点路径。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(String path, List<String> nodes)` | 初始化节点数据 | `super.flush()` 后 `controller().init(path, nodes)` |
  | `protected String url()` | FXML 路径 | `/tabs/zk/query/shellZKQueryNodeTab.fxml` |
  | `protected ShellZKQueryNodeTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.node()` |
  | `public void initNode()` | 初始化节点 | `setClosable(false)` 后 `super.initNode()` |
  | `public static ShellZKQueryNodeTab of(String path, List<String> nodes)` | 工厂方法 | `new` 后 `init(path, nodes)` 返回 |
- 调用链：`ShellZKQueryTabController.run(ls/getEphemerals) → ShellZKQueryNodeTab.of`

## ShellZKQueryNodeTabController
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryNodeTabController.java
- 职责：查询节点列表内容控制器，将子节点名拼接父路径后填入表格。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | nodeTable | FXTableView<KeyValueProperty<String, String>> | FXML 注入，节点表格（序号→完整路径） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(String path, List<String> nodes)` | 初始化节点数据 | 逐条 `KeyValueProperty.of(序号, ShellZKNodeUtil.concatPath(path, node))` 后 `nodeTable.setItem` |
- 调用链：`ShellZKQueryNodeTab.init → init → ShellZKNodeUtil.concatPath`

## ShellZKQueryACLTab
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryACLTab.java
- 职责：查询 ACL 结果标签页，展示 getAcl 命令返回的权限列表。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 原构造器被注释 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(List<ACL> aclList)` | 初始化 acl 数据 | `super.flush()` 后 `controller().init(aclList)` |
  | `protected String url()` | FXML 路径 | `/tabs/zk/query/shellZKQueryACLTab.fxml` |
  | `protected ShellZKQueryACLTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.acl()` |
  | `public void initNode()` | 初始化节点 | `setClosable(false)` 后 `super.initNode()` |
  | `public static ShellZKQueryACLTab of(List<ACL> aclList)` | 工厂方法 | `new` 后 `init(aclList)` 返回 |
- 调用链：`ShellZKQueryTabController.run(isGetACL) → ShellZKQueryACLTab.of`

## ShellZKQueryACLTabController
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryACLTabController.java
- 职责：查询 ACL 内容控制器，将 ACL 三元组（id/scheme/perms）填入表格。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | aclTable | FXTableView<Param3Property<String, String, String>> | FXML 注入，ACL 表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(List<ACL> aclList)` | 初始化 acl 数据 | 逐条 `Param3Property.of(id.getId(), id.getScheme(), ShellZKACLUtil.toPermStr(perms,","))` 后 `aclTable.setItem` |
- 调用链：`ShellZKQueryACLTab.init → init → ShellZKACLUtil.toPermStr`

## ShellZKQueryStatTab
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryStatTab.java
- 职责：查询状态结果标签页，展示节点 Stat 信息（stat 命令或 get/ls 带 stat 参数时）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 原构造器被注释 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(Stat stat)` | 初始化状态数据 | `super.flush()` 后 `controller().init(stat)` |
  | `protected String url()` | FXML 路径 | `/tabs/zk/query/shellZKQueryStatTab.fxml` |
  | `protected ShellZKQueryStatTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.stat()` |
  | `public void initNode()` | 初始化节点 | `setClosable(false)` 后 `super.initNode()` |
  | `public static ShellZKQueryStatTab of(Stat stat)` | 工厂方法 | `new` 后 `init(stat)` 返回 |
- 调用链：`ShellZKQueryTabController.run(stat/hasParamStat) → ShellZKQueryStatTab.of`

## ShellZKQueryStatTabController
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryStatTabController.java
- 职责：查询状态内容控制器，将 Stat 各字段逐项填入键值表格。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | statTable | FXTableView<KeyValueProperty<String, Object>> | FXML 注入，状态表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(Stat stat)` | 初始化状态数据 | 依次加入 pzxid/czxid/ctime/mzxid/mtime/version/cversion/aversion/dataLength/numChildren/ephemeralOwner 等键值（其中 aversion/numChildren 存在重复键写法）后 `statTable.setItem` |
- 调用链：`ShellZKQueryStatTab.init → init → statTable.setItem`

## ShellZKQueryQuotaTab
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryQuotaTab.java
- 职责：查询配额结果标签页，展示 listquota 命令返回的配额。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 原构造器被注释 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(StatsTrack track)` | 初始化配额数据 | `super.flush()` 后 `controller().init(track)` |
  | `protected String url()` | FXML 路径 | `/tabs/zk/query/shellZKQueryQuotaTab.fxml` |
  | `protected ShellZKQueryQuotaTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.quota()` |
  | `public void initNode()` | 初始化节点 | `setClosable(false)` 后 `super.initNode()` |
  | `public static ShellZKQueryQuotaTab of(StatsTrack track)` | 工厂方法 | `new` 后 `init(track)` 返回 |
- 调用链：`ShellZKQueryTabController.run(isListquota) → ShellZKQueryQuotaTab.of`

## ShellZKQueryQuotaTabController
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryQuotaTabController.java
- 职责：查询配额内容控制器，将 bytes/count 配额填入表格，无配额填 -1。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | quotaTable | FXTableView<KeyValueProperty<String, Object>> | FXML 注入，配额表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(StatsTrack track)` | 初始化配额数据 | track 为空加入 bytes/count 的 -1，否则取 `getBytes()/getCount()`，`quotaTable.setItem` |
- 调用链：`ShellZKQueryQuotaTab.init → init → quotaTable.setItem`

## ShellZKQueryEnvTab
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryEnvTab.java
- 职责：查询环境变量结果标签页，展示 srvr/envi/mntr/conf/stat 等四字命令的环境信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(List<ShellZKEnvNode> envNodes)` | 初始化环境变量数据 | `super.flush()` 后 `controller().init(envNodes)` |
  | `protected String url()` | FXML 路径 | `/tabs/zk/query/shellZKQueryEnvTab.fxml` |
  | `protected ShellZKQueryEnvTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.env()` |
  | `public void initNode()` | 初始化节点 | `setClosable(false)` 后 `super.initNode()` |
  | `public static ShellZKQueryEnvTab of(List<ShellZKEnvNode> envNodes)` | 工厂方法 | `new` 后 `init(envNodes)` 返回 |
- 调用链：`ShellZKQueryTabController.run(srvr/envi/mntr/conf/stat4) → ShellZKQueryEnvTab.of`

## ShellZKQueryEnvTabController
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryEnvTabController.java
- 职责：查询环境变量内容控制器，将 `ShellZKEnvNode` 列表转为键值表格。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | envTable | FXTableView<KeyValueProperty<String, Object>> | FXML 注入，环境变量表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(List<ShellZKEnvNode> envNodes)` | 初始化环境变量数据 | 非空时逐条 `KeyValueProperty.of(envNode.getName(), envNode.getValue())` 后 `envTable.setItem` |
- 调用链：`ShellZKQueryEnvTab.init → init → envTable.setItem`

## ShellZKQueryWhoamiTab
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryWhoamiTab.java
- 职责：查询认证信息结果标签页，展示 whoami 命令返回的客户端认证信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(List<ClientInfo> clientInfos)` | 初始化认证信息数据 | `super.flush()` 后 `controller().init(clientInfos)` |
  | `protected String url()` | FXML 路径 | `/tabs/zk/query/shellZKQueryWhoamiTab.fxml` |
  | `protected ShellZKQueryWhoamiTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.authInfo()` |
  | `public void initNode()` | 初始化节点 | `setClosable(false)` 后 `super.initNode()` |
  | `public static ShellZKQueryWhoamiTab of(List<ClientInfo> clientInfos)` | 工厂方法 | `new` 后 `init(clientInfos)` 返回 |
- 调用链：`ShellZKQueryTabController.run(isWhoami) → ShellZKQueryWhoamiTab.of`

## ShellZKQueryWhoamiTabController
> 文件: cn/oyzh/easyshell/tabs/zk/query/ShellZKQueryWhoamiTabController.java
- 职责：查询认证信息内容控制器，将客户端认证方案与用户填入键值表格。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | whoamiTable | FXTableView<KeyValueProperty<String, Object>> | FXML 注入，认证信息表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(List<ClientInfo> clientInfos)` | 初始化认证信息数据 | 非空时逐条 `KeyValueProperty.of(clientInfo.getAuthScheme(), clientInfo.getUser())` 后 `whoamiTable.setItem` |
- 调用链：`ShellZKQueryWhoamiTab.init → init → whoamiTable.setItem`

## ShellZKServerTabController
> 文件: cn/oyzh/easyshell/tabs/zk/server/ShellZKServerTabController.java
- 职责：服务端信息主控制器，以 3 秒周期任务拉取 srvr 数据并驱动汇总聚合图表与各服务端信息子标签。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | FXML 注入，根标签节点 |
  | client | ShellZKClient | zk 客户端 |
  | serverTable | FXTableView<ShellZKServerInfo> | FXML 注入，服务信息表格（仅一行汇总对象） |
  | latency | FXTableColumn<ShellZKServerInfo, String> | FXML 注入，延迟列 |
  | command | FXTableColumn<ShellZKServerInfo, String> | FXML 注入，命令列 |
  | aggregationController | ShellZKAggregationTabController | FXML 注入，汇总图表子控制器 |
  | srvrController | ShellZKSrvrTabController | FXML 注入，服务信息子控制器 |
  | statController | ShellZKStatTabController | FXML 注入，状态信息子控制器 |
  | localController | ShellZKLocalTabController | FXML 注入，本地信息子控制器 |
  | confController | ShellZKConfTabController | FXML 注入，配置信息子控制器 |
  | enviController | ShellZKEnviTabController | FXML 注入，环境信息子控制器 |
  | clusterController | ShellZKClusterTabController | FXML 注入，集群信息子控制器 |
  | refreshTask | Future<?> | 自动刷新任务句柄 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellZKClient getClient()` | 获取客户端 | 返回 `client` |
  | `public void setClient(ShellZKClient client)` | 设置客户端 | `this.client = client` |
  | `public void init(ShellZKClient client)` | 初始化 | 设置 client；将命令列文本改用 received/sent/outstanding 表头，延迟列改用 min/avg/max 表头 |
  | `private void initRefreshTask()` | 启动刷新任务 | `ExecutorUtil.start(this::renderPane, 0, 3000)` 记录到 `refreshTask` |
  | `public void closeRefreshTask()` | 关闭刷新任务 | `ExecutorUtil.cancel(refreshTask)` |
  | `private void renderPane()` | 渲染主面板 | 无 client 返回；`serverTable` 空则新建 `ShellZKServerInfo`；`client.srvrNodes()` 后 `serverInfo.update(envNodes)`，再 `aggregationController.init(serverInfo)` |
  | `public void onTabInit(FXTab tab)` | 标签页初始化 | `root.selectedProperty().subscribe`：被选中时 `initRefreshTask()`，否则 `closeRefreshTask()` |
  | `public void onTabClosed(Event event)` | 关闭清理 | `super.onTabClosed(event)` 后 `closeRefreshTask()` |
  | `public List<? extends RichTabController> getSubControllers()` | 子控制器列表 | `List.of(aggregationController, localController, srvrController, statController, clusterController, confController, enviController)` |
  | `public void destroy()` | 销毁清理 | 子控制器销毁被注释，仅 `closeRefreshTask()` 后 `super.destroy()` |
- 调用链：`onTabInit → initRefreshTask → renderPane → client.srvrNodes → aggregationController.init`

## ShellZKAggregationTabController
> 文件: cn/oyzh/easyshell/tabs/zk/server/ShellZKAggregationTabController.java
- 职责：服务端汇总子标签，将连接数、节点数、延迟、命令统计绘制为四张折线图。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | connectionsChart | FXLineChart<String, Integer> | FXML 注入，客户端连接数图表 |
  | nodeCountChart | FXLineChart<String, Integer> | FXML 注入，节点数量图表 |
  | latencyChart | FXLineChart<String, Number> | FXML 注入，延迟（min/avg/max）图表 |
  | commandChart | FXLineChart<String, Integer> | FXML 注入，命令（received/sent/outstanding）图表 |
  | dateFormat | SimpleDateFormat | 时间轴格式化（final），`HH:mm:ss` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellZKServerInfo serverInfo)` | 初始化 | 依次调用四个图表初始化方法 |
  | `private void initConnectionsChart(ShellZKServerInfo serverInfo)` | 初始化连接图表 | 首次创建序列（名 `I18nHelper.connections()`），`ChartHelper.addOrUpdateData(series, time, serverInfo.connections(), 10)` |
  | `private void initNodeCountChart(ShellZKServerInfo serverInfo)` | 初始化节点数图表 | 序列名 `I18nHelper.nodeCount()`，追加 `serverInfo.nodeCount()` |
  | `private void initLatencyChart(ShellZKServerInfo serverInfo)` | 初始化延迟图表 | 创建 min/avg/max 三条序列，追加 `latencyMin/latencyAvg/latencyMax` |
  | `private void initCommandChart(ShellZKServerInfo serverInfo)` | 初始化命令图表 | 创建 received/sent/outstanding 三条序列，追加 `commandReceived/commandSent/commandOutstanding` |
- 调用链：`renderPane → aggregationController.init → ChartHelper.addOrUpdateData`

## ShellZKClusterTabController
> 文件: cn/oyzh/easyshell/tabs/zk/server/ShellZKClusterTabController.java
- 职责：服务端集群信息子标签，被选中时刷新集群节点列表。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | FXML 注入，根标签节点 |
  | clusterTable | FXTableView<ShellZKClusterNode> | FXML 注入，集群列表表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellZKServerTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
  | `private void refreshCluster()` | 刷新集群信息（FXML） | `parent().getClient().clusterNodes()` 后 `clusterTable.setItem` |
  | `protected void bindListeners()` | 绑定监听器 | `root.selectedProperty()` 为真时 `refreshCluster()` |
- 调用链：`bindListeners → root.selectedProperty → refreshCluster → client.clusterNodes`

## ShellZKConfTabController
> 文件: cn/oyzh/easyshell/tabs/zk/server/ShellZKConfTabController.java
- 职责：服务端配置信息子标签，被选中时刷新 conf 信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | FXML 注入，根标签节点 |
  | confTable | FXTableView<ShellZKEnvNode> | FXML 注入，配置信息表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellZKServerTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
  | `private void refreshConf()` | 刷新配置信息（FXML） | `parent().getClient().confNodes()` 后 `confTable.setItem` |
  | `protected void bindListeners()` | 绑定监听器 | `root.selectedProperty()` 为真时 `refreshConf()` |
- 调用链：`bindListeners → root.selectedProperty → refreshConf → client.confNodes`

## ShellZKEnviTabController
> 文件: cn/oyzh/easyshell/tabs/zk/server/ShellZKEnviTabController.java
- 职责：服务端环境信息子标签，被选中时刷新服务端环境变量。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | FXML 注入，根标签节点 |
  | enviTable | FXTableView<ShellZKEnvNode> | FXML 注入，服务端环境表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellZKServerTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
  | `private void refreshEnvi()` | 刷新服务端环境信息（FXML） | `parent().getClient().enviNodes()` 后 `enviTable.setItem` |
  | `protected void bindListeners()` | 绑定监听器 | `root.selectedProperty()` 为真时 `refreshEnvi()` |
- 调用链：`bindListeners → root.selectedProperty → refreshEnvi → client.enviNodes`

## ShellZKLocalTabController
> 文件: cn/oyzh/easyshell/tabs/zk/server/ShellZKLocalTabController.java
- 职责：服务端本地（客户端）信息子标签，被选中时刷新客户端本机环境变量。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | FXML 注入，根标签节点 |
  | localEnvTable | FXTableView<ShellZKEnvNode> | FXML 注入，客户端环境表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellZKServerTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
  | `private void refreshLocal()` | 刷新客户端环境信息（FXML） | `parent().getClient().localNodes()` 后 `localEnvTable.setItem` |
  | `protected void bindListeners()` | 绑定监听器 | `root.selectedProperty()` 为真时 `refreshLocal()` |
- 调用链：`bindListeners → root.selectedProperty → refreshLocal → client.localNodes`

## ShellZKSrvrTabController
> 文件: cn/oyzh/easyshell/tabs/zk/server/ShellZKSrvrTabController.java
- 职责：服务端 srvr 信息子标签，被选中时刷新服务信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | FXML 注入，根标签节点 |
  | srvrTable | FXTableView<ShellZKEnvNode> | FXML 注入，服务信息表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellZKServerTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
  | `private void refreshSrvr()` | 刷新服务信息（FXML） | `parent().getClient().srvrNodes()` 后 `srvrTable.setItem` |
  | `protected void bindListeners()` | 绑定监听器 | `root.selectedProperty()` 为真时 `refreshSrvr()` |
- 调用链：`bindListeners → root.selectedProperty → refreshSrvr → client.srvrNodes`

## ShellZKStatTabController
> 文件: cn/oyzh/easyshell/tabs/zk/server/ShellZKStatTabController.java
- 职责：服务端 stat 信息子标签，被选中时刷新状态信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | FXML 注入，根标签节点 |
  | statTable | FXTableView<ShellZKEnvNode> | FXML 注入，状态信息表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellZKServerTabController parent()` | 获取父控制器 | 强转 `super.parent()` |
  | `private void refreshStat()` | 刷新状态信息（FXML） | `parent().getClient().statNodes()` 后 `statTable.setItem` |
  | `protected void bindListeners()` | 绑定监听器 | `root.selectedProperty()` 为真时 `refreshStat()` |
- 调用链：`bindListeners → root.selectedProperty → refreshStat → client.statNodes`

## ShellZKTerminalTabController
> 文件: cn/oyzh/easyshell/tabs/zk/terminal/ShellZKTerminalTabController.java
- 职责：zk 命令行子标签，被选中时在终端面板初始化交互式 ZK 命令行会话。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | FXML 注入，根标签节点 |
  | terminal | ZKTerminalPane | FXML 注入，命令行文本域面板 |
  | client | ShellZKClient | zk 客户端 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellZKClient client)` | 设置客户端 | `this.client = client` |
  | `public ShellZKClient client()` | 获取客户端 | 返回 `client` |
  | `public ShellConnect shellConnect()` | 获取连接 | `client.getShellConnect()` |
  | `public void onTabInit(FXTab tab)` | 标签页初始化 | `root.selectedProperty().subscribe`：被选中且 `terminal.getClient()==null` 时 `terminal.init(client)`（懒加载） |
  | `public void onTabClosed(Event event)` | 关闭清理 | `super.onTabClosed(event)` 后 `IOUtil.closeAsync(client)` |
- 调用链：`onTabInit → root.selectedProperty → terminal.init(client)`；`onTabClosed → IOUtil.closeAsync`
