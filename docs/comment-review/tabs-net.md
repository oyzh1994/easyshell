# easyshell 标签页模块代码审查文档 — 远程与文件协议

> 范围：`cn/oyzh/easyshell/tabs/` 下 `vnc1/`、`rdp/`、`mosh/`、`split/`、`serial/`、`ftp/`、`sftp/`、`smb/`、`webdav/`、`s3/`、`telnet/`、`rlogin/`。
> 总览与通用标签页见 [tabs.md](./tabs.md)。说明：仅新增文档，未改动任何 `.java`。
> 注：`vnc/` 目录为整文件注释的死代码，未纳入（使用中的为 `vnc1/`）。

## VNC / RDP / MOSH / 串口 / 分屏 标签页

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

## 文件传输/远程协议标签页（FTP/SFTP/SMB/WebDAV/S3/Telnet/RLogin）

## ShellFTPTab（tabs.ftp）
> 文件: cn/oyzh/easyshell/tabs/ftp/ShellFTPTab.java
- 职责：FTP 连接标签页，继承 `ShellConnectTab` 并实现 `NodeLifeCycle`，负责图标/标题与内容刷新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML 路径 | `/tabs/ftp/shellFTPTab.fxml` |
  | `void flushGraphic()` | 刷新图标 | `ShellOsTypeComboBox.getGlyph(osType)` |
  | `void init(ShellConnect)` | 初始化 | `controller().init(connect)` + `super.init` |
  | `protected String getTabTitle()` | 标题 | `名称(类型大写)` |
  | `ShellFTPTabController controller()` | 控制器 | 强转 |
  | `ShellFTPClient client()` | 客户端 | 委托 controller |
  | `void onNodeInitialize()` | 节点初始化回调 | `StageManager.getAdapter(window()).updateContentLater()` 延迟刷新内容 |
  | `static ShellFTPTab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellFTPTabController.init`

## ShellFTPTabController（tabs.ftp）
> 文件: cn/oyzh/easyshell/tabs/ftp/ShellFTPTabController.java
- 职责：FTP 文件浏览内容控制器，管理文件表格、上传下载任务、隐藏文件与拖拽上传。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | FXML 注入，根节点（快捷键过滤） |
  | location | ShellFileLocationTextField | FXML 注入，当前位置/路径跳转 |
  | manage | SVGLabel | FXML 注入，上传下载数量显示 |
  | refreshFile | SVGGlyph | FXML 注入，刷新按钮 |
  | deleteFile | SVGGlyph | FXML 注入，删除按钮 |
  | hiddenPane | HiddenSVGPane | FXML 注入，隐藏文件切换 |
  | fileTable | ShellFTPFileTableView | FXML 注入，文件表格 |
  | filterFile | ClearableTextField | FXML 注入，文件过滤 |
  | fileInfo | FXLabel | FXML 注入，文件信息 |
  | fileName | TableColumn<ShellFile,?> | FXML 注入，文件名列 |
  | connectStore | ShellConnectStore | 连接存储 `ShellConnectStore.INSTANCE` |
  | client | ShellFTPClient | FTP 客户端 |
  | taskSizeListener | ListChangeListener<ShellFileTask> | 任务数量变更监听器 |
  | taskTypes | List<ShellFileTaskType> | 关注的任务类型（UPLOAD/DOWNLOAD） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellFTPClient client()` / `ShellConnect shellConnect()` | 访问器 | 返回客户端/连接 |
  | `void init(ShellConnect)` | 初始化并连接 | `newClient`→状态监听→`showMask`：`client.start()`、`fileTable.setClient`、`hiddenFile(...)`、注册任务数量监听更新 manage |
  | `void onTabClosed(Event)` | 关闭清理 | `IOUtil.close(client)`、`connectStore.update(shellConnect())` |
  | `protected void bindListeners()` | 绑定监听 | 位置/过滤/路径跳转/快捷键/信息/图标 cellFactory |
  | `private void refreshFile()` `@FXML` | 刷新 | `fileTable.loadFile()` |
  | `private void deleteFile()` `@FXML` | 删除 | `fileTable.deleteFile(selectedItems)` |
  | `private void returnDir()` `@FXML` | 上级目录 | `fileTable.returnDir()` |
  | `private void intoHome()` `@FXML` | home 目录 | `fileTable.intoHome()` |
  | `private void mkdir()` `@FXML` | 新建目录 | `fileTable.createDir()` |
  | `private void touchFile()` `@FXML` | 新建文件 | `fileTable.touch()` |
  | `private void uploadFile()` / `uploadFolder()` `@FXML` | 上传 | `fileTable.uploadFile()` / `uploadFolder()` |
  | `private void draggedFile(ShellFileDraggedEvent)` `@EventSubscribe` | 拖拽上传 | tab 选中时 `fileTable.uploadFile(files)` |
  | `private void hiddenFile()` `@FXML` / `hiddenFile(boolean)` | 隐藏文件切换 | 切换 hiddenPane 与 `fileTable.setShowHiddenFile`，写回 `shellConnect` |
  | `private void manage()` `@FXML` | 传输管理 | `ShellViewFactory.fileManage(client)` |
  | `void destroy()` | 销毁 | `client.removeTaskSizeListener(...)` |
- 调用链：`init → fileTable.setClient → addTaskSizeListener`；`draggedFile → fileTable.uploadFile`

## ShellSFTPTab（tabs.sftp）
> 文件: cn/oyzh/easyshell/tabs/sftp/ShellSFTPTab.java
- 职责：SFTP 连接标签页（继承 `ShellConnectTab`），图标/标题与内容刷新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML 路径 | `/tabs/sftp/shellSFTPTab.fxml` |
  | `void flushGraphic()` | 刷新图标 | `ShellOsTypeComboBox.getGlyph` |
  | `void init(ShellConnect)` | 初始化 | controller.init + super.init |
  | `protected String getTabTitle()` / `controller()` / `client()` | 标题/控制器/客户端 | client 委托 `controller().client()` |
  | `static ShellSFTPTab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellSFTPTabController.init`

## ShellSFTPTabController（tabs.sftp）
> 文件: cn/oyzh/easyshell/tabs/sftp/ShellSFTPTabController.java
- 职责：SFTP 文件浏览内容控制器，功能与 `ShellFTPTabController` 一致，客户端为 `ShellSFTPClient`、表格为 `ShellSFTPFileTableView`，并额外支持收藏处理。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | FXML 注入，根节点 |
  | location | ShellFileLocationTextField | FXML 注入，当前位置/收藏 |
  | manage | SVGLabel | FXML 注入，传输数量 |
  | refreshFile / deleteFile | SVGGlyph | FXML 注入，刷新/删除按钮 |
  | hiddenPane | HiddenSVGPane | FXML 注入，隐藏文件切换 |
  | fileTable | ShellSFTPFileTableView | FXML 注入，文件表格 |
  | filterFile | ClearableTextField | FXML 注入，过滤 |
  | fileInfo | FXLabel | FXML 注入，文件信息 |
  | fileName | TableColumn<ShellFile,?> | FXML 注入，文件名列 |
  | connectStore | ShellConnectStore | 连接存储 |
  | client | ShellSFTPClient | SFTP 客户端 |
  | taskSizeListener / taskTypes | ListChangeListener / List | 任务数量监听与类型 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(ShellConnect)` | 初始化并连接 | 同 FTP，另注册 `location.setFileCollectSupplier(() -> ShellFileUtil.fileCollect(client))` 收藏 |
  | `void onTabClosed(Event)` | 关闭清理 | close client + update 连接 |
  | `protected void bindListeners()` | 绑定监听 | 位置/过滤/跳转/快捷键/信息/图标 |
  | `@FXML refreshFile/deleteFile/returnDir/intoHome/mkdir/touchFile/uploadFile/uploadFolder/manage` | 文件操作 | 均委托 `fileTable` 对应方法 / `ShellViewFactory.fileManage` |
  | `@EventSubscribe draggedFile(ShellFileDraggedEvent)` | 拖拽上传 | tab 选中时上传 |
  | `hiddenFile()` / `hiddenFile(boolean)` | 隐藏文件切换 | 同步表格与连接设置 |
  | `void destroy()` | 销毁 | 移除任务监听 |
- 调用链：`init → fileTable.setClient → setFileCollectSupplier`；`draggedFile → fileTable.uploadFile`

## ShellSMBTab（tabs.smb）
> 文件: cn/oyzh/easyshell/tabs/smb/ShellSMBTab.java
- 职责：SMB 连接标签页（继承 `ShellConnectTab`），图标/标题与内容刷新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML 路径 | `/tabs/smb/shellSMBTab.fxml` |
  | `void flushGraphic()` / `init(ShellConnect)` / `getTabTitle()` / `controller()` / `client()` | 图标/初始化/标题/控制器/客户端 | client 委托 `controller().client()` |
  | `static ShellSMBTab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellSMBTabController.init`

## ShellSMBTabController（tabs.smb）
> 文件: cn/oyzh/easyshell/tabs/smb/ShellSMBTabController.java
- 职责：SMB 文件浏览内容控制器，功能同 SFTP 版本，客户端 `ShellSMBClient`、表格 `ShellSMBFileTableView`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root / location / manage / refreshFile / deleteFile / hiddenPane / filterFile / fileInfo / fileName | 见上（FXML 注入） | 同 FTP 控制器布局，表格类型 `ShellSMBFileTableView` |
  | connectStore | ShellConnectStore | 连接存储 |
  | client | ShellSMBClient | SMB 客户端 |
  | taskSizeListener / taskTypes | ListChangeListener / List | 任务数量监听与类型 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(ShellConnect)` | 初始化并连接 | newClient→showMask→`fileTable.setClient`、隐藏文件、任务监听、收藏 supplier |
  | `void onTabClosed(Event)` | 关闭清理 | close client + update 连接 |
  | `protected void bindListeners()` | 绑定监听 | 位置/过滤/跳转/快捷键/信息/图标 |
  | `@FXML refreshFile/deleteFile/returnDir/intoHome/mkdir/touchFile/uploadFile/uploadFolder/manage` | 文件操作 | 委托 fileTable |
  | `@EventSubscribe draggedFile(...)` | 拖拽上传 | 选中时上传 |
  | `hiddenFile()` / `hiddenFile(boolean)` | 隐藏文件切换 | 同步表格与连接 |
  | `void destroy()` | 销毁 | 移除任务监听 |
- 调用链：`init → fileTable.setClient → addTaskSizeListener`

## ShellWebdavTab（tabs.webdav）
> 文件: cn/oyzh/easyshell/tabs/webdav/ShellWebdavTab.java
- 职责：WebDAV 连接标签页（继承 `ShellConnectTab`），图标/标题与内容刷新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML 路径 | `/tabs/webdav/shellWebdavTab.fxml` |
  | `void flushGraphic()` / `init(ShellConnect)` / `getTabTitle()` / `controller()` / `client()` | 图标/初始化/标题/控制器/客户端 | client 委托 controller |
  | `static ShellWebdavTab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellWebdavTabController.init`

## ShellWebdavTabController（tabs.webdav）
> 文件: cn/oyzh/easyshell/tabs/webdav/ShellWebdavTabController.java
- 职责：WebDAV 文件浏览内容控制器，功能同 SFTP 版本，客户端 `ShellWebdavClient`、表格 `ShellWebdavFileTableView`（文件名列为 `FXTableColumn`）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root / location / manage / refreshFile / deleteFile / hiddenPane / filterFile / fileInfo | 见上（FXML 注入） | 同 FTP 控制器布局 |
  | fileName | FXTableColumn<ShellFile,?> | FXML 注入，文件名列 |
  | fileTable | ShellWebdavFileTableView | FXML 注入，文件表格 |
  | connectStore | ShellConnectStore | 连接存储 |
  | client | ShellWebdavClient | WebDAV 客户端 |
  | taskSizeListener / taskTypes | ListChangeListener / List | 任务数量监听与类型 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(ShellConnect)` | 初始化并连接 | newClient→showMask→`fileTable.setClient`、隐藏文件、任务监听、收藏 supplier |
  | `void onTabClosed(Event)` | 关闭清理 | close client + update 连接 |
  | `protected void bindListeners()` | 绑定监听 | 位置/过滤/跳转/快捷键/信息/图标 |
  | `@FXML refreshFile/deleteFile/returnDir/intoHome/mkdir/touchFile/uploadFile/uploadFolder/manage` | 文件操作 | 委托 fileTable |
  | `@EventSubscribe draggedFile(...)` | 拖拽上传 | 选中时上传 |
  | `hiddenFile()` / `hiddenFile(boolean)` | 隐藏文件切换 | 同步表格与连接 |
  | `void destroy()` | 销毁 | 移除任务监听 |
- 调用链：`init → fileTable.setClient → setFileCollectSupplier`

## ShellS3Tab（tabs.s3）
> 文件: cn/oyzh/easyshell/tabs/s3/ShellS3Tab.java
- 职责：S3 连接标签页，继承 `ShellConnectTab` 并实现 `NodeLifeCycle`，图标/标题与内容刷新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML 路径 | `/tabs/s3/shellS3Tab.fxml` |
  | `void flushGraphic()` | 刷新图标 | `ShellOsTypeComboBox.getGlyph` |
  | `void init(ShellConnect)` | 初始化 | controller.init + super.init |
  | `protected String getTabTitle()` / `controller()` / `client()` | 标题/控制器/客户端 | client 委托 controller |
  | `void onNodeInitialize()` | 节点初始化回调 | `adapter.updateContentLater()` |
  | `static ShellS3Tab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellS3TabController.init → ShellS3FileTabController.init`

## ShellS3TabController（tabs.s3）
> 文件: cn/oyzh/easyshell/tabs/s3/ShellS3TabController.java
- 职责：S3 标签页父控制器，管理“文件/桶”两个子标签控制器并建立 S3 连接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | fileTabController | ShellS3FileTabController | FXML 注入，文件子标签控制器 |
  | bucketTabController | ShellS3BucketTabController | FXML 注入，桶子标签控制器 |
  | client | ShellS3Client | S3 客户端 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellS3Client client()` | 客户端 | 返回 client |
  | `void init(ShellConnect)` | 初始化并连接 | newClient→状态监听→showMask：`client.start()`、`hideLeft`、`fileTabController.init()` |
  | `void onTabClosed(Event)` | 关闭清理 | `IOUtil.close(client)` |
  | `List<? extends RichTabController> getSubControllers()` | 子控制器 | 返回 file/bucket 两个控制器 |
- 调用链：`init → client.start → fileTabController.init`

## ShellS3BucketTabController（tabs.s3）
> 文件: cn/oyzh/easyshell/tabs/s3/ShellS3BucketTabController.java
- 职责：S3 桶管理子标签控制器，展示/新建/删除桶。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | FXML 注入，根标签 |
  | bucketTable | ShellS3BucketTableView | FXML 注入，桶表格 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellS3TabController parent()` | 父控制器 | 强转 |
  | `ShellS3Client client()` | 客户端 | `parent().client()` |
  | `void init()` | 初始化 | `bucketTable.setClient(client)` + `refreshBucket()` |
  | `void onTabInit(FXTab)` | tab 初始化 | 监听 root 选中，选中时 `init()` |
  | `private void refreshBucket()` `@FXML` | 刷新桶 | `bucketTable.loadBucket()` |
  | `private void deleteBucket()` `@FXML` | 删除桶 | `bucketTable.deleteBucket(selectedItem,false)` |
  | `private void addBucket()` `@FXML` | 新增桶 | `bucketTable.addBucket()` |
- 调用链：`onTabInit → init → bucketTable.setClient/loadBucket`

## ShellS3FileTabController（tabs.s3）
> 文件: cn/oyzh/easyshell/tabs/s3/ShellS3FileTabController.java
- 职责：S3 文件管理子标签控制器，浏览/上传/下载/删除对象，按能力启停操作按钮。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | FXML 注入，根标签 |
  | location | ShellFileLocationTextField | FXML 注入，当前位置/收藏 |
  | manage | SVGLabel | FXML 注入，传输数量 |
  | refreshFile / touchFile / uploadDir / uploadFile / deleteFile | SVGGlyph | FXML 注入，各操作按钮 |
  | fileTable | ShellS3FileTableView | FXML 注入，文件表格 |
  | filterFile | ClearableTextField | FXML 注入，过滤 |
  | fileInfo | FXLabel | FXML 注入，文件信息 |
  | fileName | TableColumn<ShellFile,?> | FXML 注入，文件名列 |
  | taskSizeListener | ListChangeListener<ShellFileTask> | 任务数量监听器 |
  | taskTypes | List<ShellFileTaskType> | 任务类型（UPLOAD/DOWNLOAD） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellS3TabController parent()` | 父控制器 | 强转 |
  | `ShellS3Client client()` | 客户端 | `parent().client()` |
  | `void init()` | 初始化 | `fileTable.setClient` + `refreshFile`，注册任务监听，设置收藏 supplier |
  | `protected void bindListeners()` | 绑定监听 | 位置（变化后 `initFileAction`）/过滤/跳转/快捷键/信息/图标 |
  | `private void initFileAction()` | 能力按钮启停 | 依据 `fileTable.isSupportTouchAction/UploadAction/DeleteAction` 设置 disable |
  | `@FXML refreshFile/deleteFile/returnDir/intoHome/touchFile/uploadFile/uploadFolder/manage` | 文件操作 | 委托 fileTable / `ShellViewFactory.fileManage` |
  | `@EventSubscribe draggedFile(...)` | 拖拽上传 | 选中时上传 |
  | `void destroy()` | 销毁 | 移除任务监听 |
- 调用链：`init → fileTable.setClient → refreshFile`；`draggedFile → fileTable.uploadFile`

## ShellTelnetTab（tabs.telnet）
> 文件: cn/oyzh/easyshell/tabs/telnet/ShellTelnetTab.java
- 职责：Telnet 连接标签页，继承 `ShellTermTab`，图标/标题并委托控制器连接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML 路径 | `/tabs/telnet/shellTelnetTab.fxml` |
  | `void flushGraphic()` / `init(ShellConnect)` / `getTabTitle()` / `controller()` | 图标/初始化/标题/控制器 | 同其它 TermTab |
  | `ShellTelnetClient client()` | 客户端 | `controller().getClient()` |
  | `void runSnippet(String)` | 执行片段 | 委托 controller |
  | `static ShellTelnetTab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellTelnetTabController.init`

## ShellTelnetTabController（tabs.telnet）
> 文件: cn/oyzh/easyshell/tabs/telnet/ShellTelnetTabController.java
- 职责：Telnet 终端内容控制器，创建 telnet 终端 widget、建立会话并支持片段执行。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | widget | ShellTelnetTermWidget | FXML 注入，终端组件 |
  | termSize | FXText | FXML 注入，终端行列大小 |
  | client | ShellTelnetClient | telnet 客户端 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellTelnetClient getClient()` / `ShellConnect shellConnect()` | 访问器 | 返回客户端/连接 |
  | `private void initWidget()` | 初始化终端 | `widget.createTtyConnector(client)`，监听 terminalSize，设置退格/alt，`openSession`、`initPtySize` |
  | `private void initBackground()` | 背景 | `ShellConnectUtil.initTermBackground` |
  | `void init(ShellConnect)` | 初始化并连接 | newClient→showMask：`client.start()`、失败 closeTab、`hideLeft`、`initWidget`、异步背景 |
  | `void onTabClosed(Event)` | 关闭清理 | widget.close + close client |
  | `private void refesh(MouseEvent)` `@FXML` | 重连 | `ShellEventUtil.connectionOpened` + closeTab |
  | `private void snippet(MouseEvent)` `@FXML` | 片段列表 | `ShellSnippetAdapter.super.snippetList` |
  | `void runSnippet(String)` | 执行片段 | `widget.getTtyConnector().write` |
- 调用链：`init → initWidget → createTtyConnector → openSession`

## ShellRLoginTab（tabs.rlogin）
> 文件: cn/oyzh/easyshell/tabs/rlogin/ShellRLoginTab.java
- 职责：RLogin 连接标签页，继承 `ShellTermTab`，图标/标题并委托控制器连接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML 路径 | `/tabs/rlogin/shellRLoginTab.fxml` |
  | `void flushGraphic()` / `controller()` | 图标/控制器 | 图标由 osType 生成 |
  | `void init(ShellConnect)` | 初始化 | controller.init + super.init |
  | `protected String getTabTitle()` | 标题 | `controller().shellConnect().getName()(类型大写)` |
  | `ShellRLoginClient client()` | 客户端 | `controller().getClient()` |
  | `void runSnippet(String)` | 执行片段 | 委托 controller |
  | `static ShellRLoginTab of(ShellConnect)` | 工厂 | new+init |
- 调用链：`of → init → ShellRLoginTabController.init`

## ShellRLoginTabController（tabs.rlogin）
> 文件: cn/oyzh/easyshell/tabs/rlogin/ShellRLoginTabController.java
- 职责：RLogin 终端内容控制器，创建 rlogin 终端 widget、建立会话并支持片段执行。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | widget | ShellRLoginTermWidget | FXML 注入，终端组件 |
  | termSize | FXText | FXML 注入，终端行列大小 |
  | client | ShellRLoginClient | rlogin 客户端 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRLoginClient getClient()` / `ShellConnect shellConnect()` | 访问器 | 返回客户端/连接 |
  | `private void initWidget()` | 初始化终端 | `widget.createTtyConnector(client)`，监听 terminalSize，退格/alt，`openSession` |
  | `private void initBackground()` | 背景 | `ShellConnectUtil.initTermBackground` |
  | `void init(ShellConnect)` | 初始化并连接 | newClient→状态监听→showMask：start、失败 closeTab、`hideLeft`、`initWidget`、异步背景 |
  | `void onTabClosed(Event)` | 关闭清理 | widget.close + close client |
  | `private void refesh(MouseEvent)` `@FXML` | 重连 | `ShellEventUtil.connectionOpened` + closeTab |
  | `private void snippet(MouseEvent)` `@FXML` | 片段列表 | `ShellSnippetAdapter.super.snippetList` |
  | `void runSnippet(String)` | 执行片段 | `widget.getTtyConnector().write` |
- 调用链：`init → initWidget → openSession`
