# 文件传输/远程协议标签页（FTP/SFTP/SMB/WebDAV/S3/Telnet/RLogin）

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
