# VNC / RDP / MOSH / 串口 / 分屏 标签页

## ShellVNCTab（tabs.vnc1）
> 文件: cn/oyzh/easyshell/tabs/vnc1/ShellVNCTab.java
- 职责：VNC 连接标签页，负责图标/标题展示并委托控制器建立 VNC 连接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | 返回 FXML 路径 | 返回 `/tabs/vnc1/shellVNCTab.fxml` |
  | `void flushGraphic()` | 刷新标签图标 | 无图标时用 `ShellOsTypeComboBox.getGlyph(osType)` 生成 `SVGGlyph` |
  | `void init(ShellConnect connect)` | 初始化连接 | 先 `controller().init(connect)`，再 `super.init(connect)` 刷新图标 |
  | `protected String getTabTitle()` | 标签标题 | `名称(类型大写)` |
  | `ShellVNCTabController controller()` | 获取控制器 | 强转父类控制器 |
  | `ShellVNCClient client()` | 获取客户端 | 委托 `controller().client()` |
  | `static ShellVNCTab of(ShellConnect)` | 工厂方法 | new 后 `init(connect)` 返回 |
- 调用链：`of → init → ShellVNCTabController.init → ShellVNCClient.start`

## ShellVNCTabController（tabs.vnc1）
> 文件: cn/oyzh/easyshell/tabs/vnc1/ShellVNCTabController.java
- 职责：VNC 标签页内容控制器，创建客户端、建立连接并驱动帧缓冲视图缩放。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | ScrollPane | FXML 注入，根节点/滚动容器 |
  | vncView | VncFramebufferView | FXML 注入，VNC 画面视图 |
  | client | ShellVNCClient | VNC 客户端 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellVNCClient client()` | 获取客户端 | 返回 client |
  | `ShellConnect shellConnect()` | 获取连接 | `client.getShellConnect()` |
  | `void init(ShellConnect)` | 初始化并连接 | `ShellClientUtil.newClient`→注册状态监听（INTERRUPTED 告警）→`StageManager.showMask` 内 `client.start()`、失败则 `closeTab`，成功 `initVncView`+`hideLeft` |
  | `private void initVncView()` | 初始化视图 | `client.initVncView(vncView)`、`initScale` |
  | `private void initScale()` | 缩放适配 | 按 root 宽高调用 `client.zoomToFit` |
  | `void onTabClosed(Event)` | 关闭清理 | `IOUtil.close(client)` |
  | `protected void bindListeners()` | 绑定监听 | root 宽高变化时重算缩放 |
- 调用链：`init → ShellClientUtil.newClient → ShellVNCClient.start → initVncView → initScale`

## ShellRdpTab（tabs.rdp）
> 文件: cn/oyzh/easyshell/tabs/rdp/ShellRdpTab.java
- 职责：RDP 连接标签页，图标/标题展示并委托控制器建立 RDP 连接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML 路径 | `/tabs/rdp/shellRdpTab.fxml` |
  | `void flushGraphic()` | 刷新图标 | `ShellOsTypeComboBox.getGlyph(osType)` |
  | `void init(ShellConnect)` | 初始化 | `controller().init(connect)` + `super.init` |
  | `protected String getTabTitle()` | 标题 | `名称(类型大写)` |
  | `ShellRdpTabController controller()` | 控制器 | 强转 |
  | `ShellRDPClient client()` | 客户端 | 委托 controller |
  | `static ShellRdpTab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellRdpTabController.init`

## ShellRdpTabController（tabs.rdp）
> 文件: cn/oyzh/easyshell/tabs/rdp/ShellRdpTabController.java
- 职责：RDP 标签页内容控制器，建立 RDP 会话并展示视图。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | rdpView | RdpView | FXML 注入，RDP 视图 |
  | client | ShellRDPClient | RDP 客户端 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRDPClient client()` | 获取客户端 | 返回 client |
  | `ShellConnect shellConnect()` | 获取连接 | `client.getShellConnect()` |
  | `void init(ShellConnect)` | 初始化并连接 | `newClient`→状态监听→`showMask`：`client.initRdpView(rdpView)`、`client.start()`、`setScaleToFit(true)`、`hideLeft` |
  | `void onTabClosed(Event)` | 关闭清理 | `IOUtil.close(client)` |
- 调用链：`init → ShellClientUtil.newClient → initRdpView → ShellRDPClient.start`

## ShellMoshTab（tabs.mosh）
> 文件: cn/oyzh/easyshell/tabs/mosh/ShellMoshTab.java
- 职责：Mosh 连接标签页，继承 `ShellTermTab`，图标/标题展示并委托控制器连接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML 路径 | `/tabs/mosh/shellMoshTab.fxml` |
  | `void flushGraphic()` | 刷新图标 | `ShellOsTypeComboBox.getGlyph(osType)` |
  | `void init(ShellConnect)` | 初始化 | `controller().init` + `super.init` |
  | `protected String getTabTitle()` | 标题 | `名称(类型大写)` |
  | `ShellMoshTabController controller()` | 控制器 | 强转 |
  | `ShellMoshClient client()` | 客户端 | `controller().getClient()` |
  | `void runSnippet(String)` | 执行片段 | 委托 `controller().runSnippet(content)` |
  | `static ShellMoshTab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellMoshTabController.init`

## ShellMoshTabController（tabs.mosh）
> 文件: cn/oyzh/easyshell/tabs/mosh/ShellMoshTabController.java
- 职责：Mosh 终端标签页内容控制器，创建终端 widget、建立 mosh 会话并支持代码片段执行。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | widget | ShellMoshTermWidget | FXML 注入，终端组件 |
  | termSize | FXText | FXML 注入，终端行列大小显示 |
  | client | ShellMoshClient | mosh 客户端 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMoshClient getClient()` | 获取客户端 | 返回 client |
  | `ShellConnect shellConnect()` | 获取连接 | `client.getShellConnect()` |
  | `private void initWidget()` | 初始化终端 | `widget.createTtyConnector(client)`，监听 terminalSize 刷新 termSize，`initBackspaceCode`、`setAltSendsEscape`、`openSession`、`initPtySize` |
  | `private void initBackground()` | 初始化背景 | `ShellConnectUtil.initTermBackground(terminalPanel)` |
  | `void init(ShellConnect)` | 初始化并连接 | `newClient`→状态监听→`showMask`：`client.start()`、失败 `closeTab`、成功 `hideLeft`+`initWidget`，异步 `initBackground` |
  | `void onTabClosed(Event)` | 关闭清理 | `widget.close()` + `IOUtil.close(client)` |
  | `private void refesh(MouseEvent)` | FXML 重连 | `ShellEventUtil.connectionOpened` 后 `closeTab` |
  | `private void snippet(MouseEvent)` | FXML 片段列表 | `ShellSnippetAdapter.super.snippetList` |
  | `void runSnippet(String)` | 执行片段 | 向 `widget.getTtyConnector().write(content)` |
  | `void destroy()` | 销毁 | `widget.destroy()` + super |
- 调用链：`init → initWidget → widget.createTtyConnector → openSession`；`runSnippet → TtyConnector.write`

## ShellSplitTab（tabs.split）
> 文件: cn/oyzh/easyshell/tabs/split/ShellSplitTab.java
- 职责：终端分屏标签页，继承 `ShellTermTab`，按连接数选择不同分屏布局 FXML。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INDEX | AtomicInteger | 静态分屏序号，用于标题递增 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(List<ShellConnect>)` | 初始化 | `flush()` 后 `controller().init(connects)` |
  | `protected String url()` | 根据类型选 FXML | 读取 `ThreadLocalUtil.getVal("type")`，映射到 `shellSplitTab1..8.fxml` |
  | `void flushGraphic()` | 刷新图标 | `SplitViewSVGGlyph` |
  | `protected String getTabTitle()` | 标题 | `termSplitView + INDEX.incrementAndGet()` |
  | `ShellSplitTabController controller()` | 控制器 | 强转 |
  | `void runSnippet(String)` | 执行片段 | 委托 controller |
  | `ShellBaseClient client()` | 客户端 | 返回 null（分屏无单一客户端） |
  | `static ShellSplitTab of(List<ShellConnect>)` | 工厂 | new+init |
- 调用链：`of → init → ShellSplitTabController.init → doConnect`

## ShellSplitTabController（tabs.split）
> 文件: cn/oyzh/easyshell/tabs/split/ShellSplitTabController.java
- 职责：分屏标签页内容控制器，管理最多 9 个子终端控制器并批量建立连接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | term1Controller..term9Controller | ShellSplitTermController | FXML 注入，各分屏终端控制器（3~9 视布局可能为 null） |
  | setting | ShellSetting | 全局设置 `ShellSettingStore.SETTING` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(List<ShellConnect>)` | 批量初始化 | 取 `getSubControllers()`，逐个匹配连接构造 `controller.doConnect(connect)` 任务，`StageManager.showMask` 内 `ThreadUtil.submit(tasks)` 异步执行 |
  | `void onTabInit(FXTab)` | tab 初始化 | super + `hideLeft()` |
  | `List<ShellSplitTermController> getSubControllers()` | 子控制器列表 | 依次添加非空的 term1~term9 |
  | `void runSnippet(String)` | 执行片段 | 遍历子控制器调用 `runSnippet` |
- 调用链：`init → getSubControllers → ShellSplitTermController.doConnect`

## ShellSplitTermController（tabs.split）
> 文件: cn/oyzh/easyshell/tabs/split/ShellSplitTermController.java
- 职责：分屏中单个终端子控制器，按客户端类型创建对应终端 widget 并管理连接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | ShellBaseClient | 当前终端客户端 |
  | widget | TtyTermWidget | 终端组件 |
  | termBox | FXHBox | FXML 注入，终端容器 |
  | connect | ShellConnectTextField | FXML 注入，连接选择组件 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private void initWidget()` | 建终端 | 按 client 类型（SSH/RLogin/Telnet/Serial/Mosh/Local）new 对应 `*TermWidget` 并 `createTtyConnector`，SSH 且启 zmodem 时包 `TtyTerminalUtil.createZModemTtyConnector`；设置退格/alt，`openSession` |
  | `private void init()` | 装配 | `initWidget()` 后 `termBox.addChild(widget)` |
  | `void onTabClosed(Event)` | 关闭 | `destroyTerm()` + super |
  | `private void destroyTerm()` | 销毁终端 | `widget.close()`、`IOUtil.close(client)`、`termBox.clearChild()` |
  | `private void doConnect()` | FXML 连接 | 校验 connect，`destroyTerm`，取选中连接后 mask 内 `doConnect(connect)` |
  | `void doConnect(ShellConnect)` | 执行连接 | 选中连接→`ShellClientUtil.newClient`→`client.start()`→成功 `init()`，失败 `destroyTerm()` |
  | `void runSnippet(String)` | 执行片段 | `widget.getTtyConnector().write` |
  | `private ShellConnect shellConnect()` | 连接 | `client.getShellConnect()` |
- 调用链：`doConnect → ShellClientUtil.newClient → start → init → initWidget → openSession`

## ShellSerialTab（tabs.serial）
> 文件: cn/oyzh/easyshell/tabs/serial/ShellSerialTab.java
- 职责：串口连接标签页，继承 `ShellTermTab`，图标/标题展示并委托控制器连接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML 路径 | `/tabs/serial/shellSerialTab.fxml` |
  | `void flushGraphic()` | 刷新图标 | `ShellOsTypeComboBox.getGlyph(osType)` |
  | `void init(ShellConnect)` | 初始化 | `controller().init` + `super.init` |
  | `protected String getTabTitle()` | 标题 | `名称(类型大写)` |
  | `ShellSerialTabController controller()` | 控制器 | 强转 |
  | `ShellSerialClient client()` | 客户端 | `controller().getClient()` |
  | `void runSnippet(String)` | 执行片段 | 委托 controller |
  | `static ShellSerialTab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellSerialTabController.init`

## ShellSerialTabController（tabs.serial）
> 文件: cn/oyzh/easyshell/tabs/serial/ShellSerialTabController.java
- 职责：串口终端内容控制器，创建串口终端 widget、建立会话并支持片段执行。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | widget | ShellSerialTermWidget | FXML 注入，终端组件 |
  | termSize | FXText | FXML 注入，终端行列大小显示 |
  | client | ShellSerialClient | 串口客户端 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellSerialClient getClient()` | 获取客户端 | 返回 client |
  | `ShellConnect shellConnect()` | 获取连接 | `client.getShellConnect()` |
  | `private void initWidget()` | 初始化终端 | `widget.createTtyConnector(client)`，监听 terminalSize，设置退格/alt，`openSession` |
  | `private void initBackground()` | 背景 | `ShellConnectUtil.initTermBackground` |
  | `void init(ShellConnect)` | 初始化并连接 | `newClient`→状态监听→`showMask`：`client.start()`、失败时告警含 errCode/errLocation 并 `closeTab`、成功后写 `\r`、`hideLeft`、`initWidget`、异步 `initBackground` |
  | `void onTabClosed(Event)` | 关闭清理 | `widget.close()` + `IOUtil.close(client)` |
  | `private void refesh(MouseEvent)` | FXML 重连 | `ShellEventUtil.connectionOpened` + `closeTab` |
  | `private void snippet(MouseEvent)` | FXML 片段列表 | `ShellSnippetAdapter.super.snippetList` |
  | `void runSnippet(String)` | 执行片段 | `widget.getTtyConnector().write` |
- 调用链：`init → initWidget → createTtyConnector → openSession`
