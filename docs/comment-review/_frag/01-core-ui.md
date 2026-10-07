# 核心与通用标签页

## ShellBaseTabController
> 文件: cn/oyzh/easyshell/tabs/ShellBaseTabController.java
- 职责：shell 标签页基础控制器，为子类提供程序设置读取与左右侧栏收放能力。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | setting | ShellSetting | 程序设置，取 `ShellSettingStore.SETTING` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void hideLeft()` | 连接后按设置收起左侧栏 | `setting.isHiddenLeftAfterConnected()` 为真时调用 `ShellEventUtil.layout1()` |
  | `protected void showLeft()` | 显示左侧栏 | 实现体已被注释，当前为空方法 |
  | `public void onTabClosed(Event event)` | 标签页关闭回调 | 先 `super.onTabClosed(event)`，再调用 `showLeft()` |
- 调用链：`onTabClosed → showLeft`；`hideLeft → ShellEventUtil.layout1`

## ShellConnectTab
> 文件: cn/oyzh/easyshell/tabs/ShellConnectTab.java
- 职责：shell 连接标签页抽象基类，维护连接状态监听、图标状态着色与菜单项（含复制会话）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | stateListener | ChangeListener<ShellConnState> | 连接状态变化监听器，仅在 `init` 中注册一次 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void init(ShellConnect connect)` | 初始化连接标签页 | `this.flush()`；存在 `client()` 且未注册监听时创建 `stateListener` 并 `client().addStateListener(...)` |
  | `public abstract ShellBaseClient client()` | 获取 shell 客户端 | 抽象方法，由子类实现 |
  | `public ShellConnect shellConnect()` | 获取配套连接 | `client()` 为空返回 null，否则 `client().getShellConnect()` |
  | `public List<MenuItem> getMenuItems()` | 构建标签菜单 | 在父类菜单基础上追加 `MenuItemHelper.copyThisSession(this::copySession)` |
  | `private void copySession()` | 复制会话 | `ShellEventUtil.connectionOpened(this.shellConnect())` |
  | `public void flushGraphicColor()` | 按连接状态给图标着色 | 已连接绿、已关闭红、连接中橙，作用于 `SVGGlyph` |
  | `public void destroy()` | 销毁清理 | `client().removeStateListener(this.stateListener)` 后 `super.destroy()` |
- 调用链：`init → flush/flushGraphicColor`；`getMenuItems → copySession → ShellEventUtil.connectionOpened`

## ShellParentTabController
> 文件: cn/oyzh/easyshell/tabs/ShellParentTabController.java
- 职责：shell 父级标签页控制器，为父标签页提供程序设置读取与左右侧栏收放能力。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | setting | ShellSetting | 程序设置，取 `ShellSettingStore.SETTING` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void hideLeft()` | 连接后按设置收起左侧栏 | `setting.isHiddenLeftAfterConnected()` 为真时调用 `ShellEventUtil.layout1()` |
  | `protected void showLeft()` | 显示左侧栏 | 实现体已被注释，当前为空方法 |
  | `public void onTabClosed(Event event)` | 标签页关闭回调 | 先 `super.onTabClosed(event)`，再调用 `showLeft()` |
- 调用链：`onTabClosed → showLeft`；`hideLeft → ShellEventUtil.layout1`

## ShellSnippetAdapter
> 文件: cn/oyzh/easyshell/tabs/ShellSnippetAdapter.java
- 职责：shell 片段适配器接口，规范“片段列表选择 + 片段运行”能力，供终端类控制器复用。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `default void snippetList(Node node)` | 弹出片段列表并运行所选片段 | `ShellViewFactory.snippetList(node, h -> this.runSnippet(h.getContent() + "\r"))`，异常时 `MessageBox.exception(ex)` |
  | `void runSnippet(String content) throws IOException` | 运行片段内容 | 抽象方法，由实现类写入对应终端 |
- 调用链：`snippetList → ShellViewFactory.snippetList → runSnippet`

## ShellTabPane
> 文件: cn/oyzh/easyshell/tabs/ShellTabPane.java
- 职责：shell 主切换面板，按事件为各类连接创建对应标签页，并统一处理快捷键与中键关闭。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | setting | ShellSetting | 程序设置（final），取 `ShellSettingStore.SETTING` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void initNode()` | 初始化并绑定快捷键与鼠标事件 | `addEventFilter(KEY_PRESSED,...)`：主修饰键+W 关闭当前页、主修饰键+数字选中对应页；`addEventFilter(MOUSE_PRESSED,...)`：中键关闭当前页，均委托 `closeTab`/`select` |
  | `private void changelog(ChangelogEvent event)` | 更新日志事件（`@EventSubscribe`） | 无则 `new ShellChangelogTab()` 并 `addTab`，再 `select` |
  | `private ShellSSHTab getConnectTab(ShellSSHClient client)` | 查找指定客户端的 SSH 标签页 | 遍历 `getTabs()` 比较 `client` 引用 |
  | `private void connectionOpened(ShellConnectOpenedEvent event)` | 连接打开事件（`@EventSubscribe`） | 按 `connect` 类型分派 `ShellSSHTab/ShellLocalTab/ShellTelnetTab/...of(connect)`；RDP 类型走 `ShellRDPUtil.isBuiltIn` 判断，内置则 `ShellRdpTab.of`，否则按系统提示或 `ShellClientUtil.newClient(connect).start()`；最后 `addTab`+`select` |
  | `private void connectEdit(ShellConnectEditEvent event)` | 连接编辑事件（`@EventSubscribe`） | 关闭连接被编辑的 `ShellSSHTab`，`removeTab(closeTabs)` |
  | `private void localTerminal(ShellShowTerminalEvent event)` | 本地终端事件（`@EventSubscribe`） | `new ShellTerminalTab()` 并 `addTab`+`select` |
  | `private void keyManager(ShellShowKeyEvent event)` | 密钥管理事件（`@EventSubscribe`） | 复用或 `new ShellKeyTab()`，`addTab`+`select` |
  | `private void termSplit(ShellShowSplitEvent event)` | 终端分屏事件（`@EventSubscribe`） | `ThreadLocalUtil.setVal("type", event.data())`，`ShellSplitTab.of(event.getConnects())` 后选中 |
  | `private void runSnippet(ShellRunSnippetEvent event)` | 运行片段事件（`@EventSubscribe`） | 遍历 `ShellTermTab`，`isRunAll` 时全部执行，否则仅执行选中页并 `break` |
  | `private void connectDeleted(ShellConnectDeletedEvent event)` | 连接删除事件（`@EventSubscribe`） | 关闭匹配 `connect` 的 `ShellConnectTab`，`removeTab(closeTabs)` |
  | `private ShellMessageTab getMessageTab()` | 查找消息标签页 | 遍历 `getTabs()` 匹配 `ShellMessageTab` |
  | `public void showMessage(ShellShowMessageEvent event)` | 显示消息事件（`@EventSubscribe`） | 复用或新建 `ShellMessageTab`，已存在时 `tab.flushGraphic()`，再选中 |
- 调用链：`connectionOpened → ShellXXXTab.of(connect) → addTab/select`；`runSnippet → ShellTermTab.runSnippet`

## ShellTermTab
> 文件: cn/oyzh/easyshell/tabs/ShellTermTab.java
- 职责：shell 终端标签页抽象基类，继承 `ShellConnectTab` 并约定片段运行能力。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected RichTabController controller()` | 获取控制器 | 直接返回 `super.controller()` |
  | `public abstract void runSnippet(String content) throws Exception` | 运行片段 | 抽象方法，由子类实现 |
- 调用链：（无，仅提供抽象约定）

## ShellChangelogTab
> 文件: cn/oyzh/easyshell/tabs/changelog/ShellChangelogTab.java
- 职责：shell 更新日志标签页，负责标题与图标展示。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellChangelogTab()` | 构造标签页 | `super.flush()` 刷新，`ObjectWatcherManager.watch(this)` 注册对象监听 |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new ChangelogSVGGlyph()` 并设为默认光标后 `graphic(glyph)` |
  | `protected String url()` | 返回 FXML 路径 | `/tabs/changelog/shellChangelogTab.fxml` |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.changelogTitle()` |
- 调用链：`ShellTabPane.changelog → new ShellChangelogTab → flush → flushGraphic`

## ShellChangelogTabController
> 文件: cn/oyzh/easyshell/tabs/changelog/ShellChangelogTabController.java
- 职责：更新日志标签页内容控制器，加载并展示更新日志列表。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | changelog | ChangelogListView | FXML 注入，更新日志列表视图 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void initialize(URL url, ResourceBundle resourceBundle)` | 初始化并填充更新日志 | `ChangelogManager.load()` 取列表后 `changelog.init(changelogs.reversed())` 倒序展示 |
- 调用链：`initialize → ChangelogManager.load → changelog.init`

## ShellHomeTab
> 文件: cn/oyzh/easyshell/tabs/home/ShellHomeTab.java
- 职责：shell 主页标签页，展示主页图标与标题，且不可关闭。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellHomeTab()` | 构造标签页 | `super.flush()` |
  | `protected String url()` | 返回 FXML 路径 | `/tabs/home/shellHomeTab.fxml` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new HomeSVGGlyph()` 并设默认光标 |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.homeTitle()` |
  | `public void initNode()` | 初始化节点 | 先 `setClosable(false)` 禁止关闭，再 `super.initNode()` |
- 调用链：`initNode → setClosable(false)`

## ShellHomeTabController
> 文件: cn/oyzh/easyshell/tabs/home/ShellHomeTabController.java
- 职责：主页标签页内容控制器，填充版本/日期/环境信息并提供快捷入口按钮。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | version | FXText | FXML 注入，版本信息文本 |
  | update | FXText | FXML 注入，更新日期文本 |
  | jdkInfo | FXText | FXML 注入，环境（JDK）信息文本 |
  | project | Project | 项目对象（final），`Project.load()` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public void initialize(URL url, ResourceBundle resource)` | 填充主页信息 | 设置 version/update 文本；读取 `java.vm.name`、`java.vm.version` 拼接为 jdkInfo |
  | `private void addConnect()` | 新增连接（FXML 事件） | `ShellViewFactory.addConnectGuid()` |
  | `private void addGroup()` | 添加分组（FXML 事件） | `ShellEventUtil.addGroup()` |
  | `private void openTerminal()` | 打开终端（FXML 事件） | `ShellEventUtil.showTerminal()` |
  | `private void changelog()` | 打开更新日志（FXML 事件） | `ShellEventUtil.changelog()` |
  | `private void splitView()` | 打开分屏视图（FXML 事件） | `ShellViewFactory.splitGuid()` |
  | `public void onTabInit(FXTab tab)` | 标签页初始化 | 监听 `tab.selectedProperty()`，被选中时 `ShellEventUtil.layout2()` 展开左侧栏 |
- 调用链：`openTerminal → ShellEventUtil.showTerminal → ShellTabPane.localTerminal`

## ShellKeyTab
> 文件: cn/oyzh/easyshell/tabs/key/ShellKeyTab.java
- 职责：shell 密钥管理标签页，负责标题与图标展示。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellKeyTab()` | 构造标签页 | `super.flush()`，`ObjectWatcherManager.watch(this)` |
  | `protected String url()` | 返回 FXML 路径 | `/tabs/key/shellKeyTab.fxml` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new KeySVGGlyph()` 并设默认光标 |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.key1Manager()` |
- 调用链：`ShellTabPane.keyManager → new ShellKeyTab`

## ShellKeyTabController
> 文件: cn/oyzh/easyshell/tabs/key/ShellKeyTabController.java
- 职责：密钥管理标签页内容控制器，提供密钥的查询、刷新、新增与导入。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | keyTable | ShellKeyTableView | FXML 注入，密钥列表视图 |
  | filterKW | ClearableTextField | FXML 注入，过滤关键字输入框 |
  | keyStore | ShellKeyStore | 密钥存储（final），取 `ShellKeyStore.INSTANCE` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected void bindListeners()` | 绑定监听器 | 对 `filterKW` 添加文本变化监听，变化时 `refresh()` |
  | `public void onTabInit(FXTab tab)` | 标签页初始化 | 调用 `refresh()` 加载数据 |
  | `private void addKey()` | 添加密钥（FXML 事件） | `ShellViewFactory.addKey()` |
  | `private void importKey()` | 导入密钥（FXML 事件） | `ShellViewFactory.importKey()` |
  | `private void refreshKey()` | 刷新密钥（FXML 事件） | `refresh()` |
  | `private void refresh()` | 按关键字查询并填充表格 | 关键字非空时构造 `QueryParam.of("name", "%kw%", "LIKE")`，`keyStore.selectList(param)` 后 `keyTable.setItem(...)` |
  | `private void onKeyAdded(ShellKeyAddedEvent event)` | 密钥新增事件（`@EventSubscribe`） | `refresh()` |
  | `private void onKeyUpdated(ShellKeyUpdatedEvent event)` | 密钥修改事件（`@EventSubscribe`） | `keyTable.refresh()` |
- 调用链：`onKeyAdded → refresh → keyStore.selectList → keyTable.setItem`

## ShellLocalTab
> 文件: cn/oyzh/easyshell/tabs/local/ShellLocalTab.java
- 职责：本地终端连接标签页，展示 OS 类型图标与标题，并委托控制器运行本地终端。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | 返回 FXML 路径 | `/tabs/local/shellLocalTab.fxml` |
  | `public String getTabTitle()` | 标签标题 | `连接名(类型大写)` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时按 `shellConnect().getOsType()` 生成 `SVGGlyph` |
  | `public void init(ShellConnect connect)` | 初始化连接 | `controller().init(connect)` 建客户端，再 `super.init(connect)` 刷新图标 |
  | `public ShellLocalTabController controller()` | 获取控制器 | 强转 `super.controller()` |
  | `public ShellBaseClient client()` | 获取客户端 | 委托 `controller().getClient()` |
  | `public void runSnippet(String content) throws Exception` | 运行片段 | 委托 `controller().runSnippet(content)` |
  | `public static ShellLocalTab of(ShellConnect connect)` | 工厂方法 | `new` 后 `init(connect)` 返回 |
- 调用链：`ShellTabPane.connectionOpened → ShellLocalTab.of → init → ShellLocalTabController.init`

## ShellLocalTabController
> 文件: cn/oyzh/easyshell/tabs/local/ShellLocalTabController.java
- 职责：本地终端标签页内容控制器，创建本地客户端、打开终端会话并支持片段写入。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | widget | ShellLocalTermWidget | FXML 注入，终端根组件 |
  | termSize | FXText | FXML 注入，终端尺寸显示文本 |
  | client | ShellLocalClient | 当前本地客户端 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellLocalClient getClient()` | 获取客户端 | 返回 `client` |
  | `public ShellConnect shellConnect()` | 获取连接 | `client.getShellConnect()` |
  | `private void initWidget() throws IOException` | 初始化终端组件 | `widget.createTtyConnector(client)`；监听 `terminalSizeProperty` 更新 `termSize`；`widget.initBackspaceCode(...)`、`setAltSendsEscape(...)`、`openSession(connector)` |
  | `private void initBackground()` | 初始化终端背景 | `ShellConnectUtil.initTermBackground(widget.getTerminalPanel())` |
  | `public void init(ShellConnect shellConnect) throws IOException` | 初始化并建立会话 | `ShellClientUtil.newClient(shellConnect)`；`hideLeft()`；`initWidget()`；`ThreadUtil.start(this::initBackground)` 异步加载背景 |
  | `public void onTabClosed(Event event)` | 关闭清理 | `widget.close()` 与 `IOUtil.close(client)` |
  | `private void refesh(MouseEvent event)` | 刷新/重连（FXML 事件） | `ShellEventUtil.connectionOpened(shellConnect())` 后 `closeTab()`，异常 `MessageBox.exception` |
  | `private void snippet(MouseEvent event)` | 片段列表（FXML 事件） | `ShellSnippetAdapter.super.snippetList((Node) event.getSource())` |
  | `public void runSnippet(String content) throws IOException` | 运行片段 | `widget.getTtyConnector().write(content)` |
- 调用链：`ShellLocalTab.init → ShellLocalTabController.init → ShellClientUtil.newClient → initWidget → widget.openSession`

## ShellMessageTab
> 文件: cn/oyzh/easyshell/tabs/message/ShellMessageTab.java
- 职责：shell 消息标签页，负责标题与图标展示。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellMessageTab()` | 构造标签页 | `super.flush()` |
  | `protected String url()` | 返回 FXML 路径 | `/tabs/message/shellMessageTab.fxml` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new MessageSVGGlyph()` 并设默认光标 |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.message()` |
- 调用链：`ShellTabPane.showMessage → new ShellMessageTab`

## ShellMessageTabController
> 文件: cn/oyzh/easyshell/tabs/message/ShellMessageTabController.java
- 职责：消息标签页内容控制器，订阅全局事件消息并追加展示，支持清空与高亮搜索。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | EVENT_MESSAGES | ObservableList<EventFormatter> | 全局事件消息列表（static final） |
  | changeListener | ListChangeListener<EventFormatter> | 消息列表变更监听器，新增项时追加消息 |
  | msgArea | RichMsgTextArea | FXML 注入，消息文本框 |
  | filter | HighlightTextField | FXML 注入，搜索/高亮关键字输入框 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private void clearMsg()` | 清空消息（FXML 事件） | `msgArea.clear()` 与 `EVENT_MESSAGES.clear()` |
  | `public void onTabInit(FXTab tab)` | 标签页初始化 | 列表非空时经 `StageManager.showMask` 同步回填并 `EVENT_MESSAGES.addListener(changeListener)`；并将 `msgArea` 的 highlight/regex/matchCase 属性绑定到 `filter` |
  | `private void appendMsg(List<? extends EventFormatter> formatters)` | 追加消息 | 逐条 `formatter.eventFormat()` 后 `msgArea.appendLines(lines)` |
  | `public void destroy()` | 销毁清理 | `EVENT_MESSAGES.removeListener(changeListener)` 后 `super.destroy()` |
- 调用链：`onTabInit → EVENT_MESSAGES.addListener → changeListener → appendMsg → msgArea.appendLines`

## ShellTerminalTab
> 文件: cn/oyzh/easyshell/tabs/terminal/ShellTerminalTab.java
- 职责：本地终端标签页，负责标题与图标展示（无独立客户端，`client()` 恒返回 null）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `public ShellTerminalTab()` | 构造标签页 | `super.flush()`，`ObjectWatcherManager.watch(this)` |
  | `public ShellBaseClient client()` | 获取客户端 | 恒返回 `null` |
  | `protected String url()` | 返回 FXML 路径 | `/tabs/terminal/shellTerminalTab.fxml` |
  | `public void flushGraphic()` | 刷新标签图标 | 无图标时 `new TerminalSVGGlyph()` 并设默认光标 |
  | `public String getTabTitle()` | 标签标题 | `I18nHelper.localTerminal()` |
- 调用链：`ShellTabPane.localTerminal → new ShellTerminalTab`

## ShellTerminalTabController
> 文件: cn/oyzh/easyshell/tabs/terminal/ShellTerminalTabController.java
- 职责：本地终端标签页内容控制器，打开本地进程终端会话并支持片段写入。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | widget | ShellProcessTermWidget | FXML 注入，终端根组件 |
  | termSize | FXText | FXML 注入，终端尺寸显示文本 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private void initWidget() throws IOException` | 初始化终端组件 | `widget.createTtyConnector()`；监听 `terminalSizeProperty` 更新 `termSize`；`widget.openSession(connector)` |
  | `public void initialize(URL url, ResourceBundle resource)` | 控制器初始化 | 调用 `initWidget()`，异常时打印并 `MessageBox.exception(ex)` |
  | `public void onTabClosed(Event event)` | 关闭清理 | `widget.close()` |
  | `private void snippet(MouseEvent event)` | 片段列表（FXML 事件） | `ShellSnippetAdapter.super.snippetList((Node) event.getSource())` |
  | `public void runSnippet(String content) throws IOException` | 运行片段 | `widget.getTtyConnector().write(content)` |
  | `public void onTabInit(FXTab tab)` | 标签页初始化 | 调用 `super.hideLeft()` 收起左侧栏 |
- 调用链：`initialize → initWidget → widget.openSession`
