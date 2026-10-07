# SSH 标签页

> 范围：`cn/oyzh/easyshell/tabs/ssh/` 递归。文件 `ShellSSHSFTPTabController.java`、`ShellSSHTermTabController.java` 为整文件注释的死代码，已跳过。

## ShellSSHTab
> 文件: cn/oyzh/easyshell/tabs/ssh/ShellSSHTab.java
- 职责：SSH 连接标签页，继承 `ShellTermTab`，负责加载 FXML、图标/标题与片段执行。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected String url()` | FXML 路径 | 固定返回 `/tabs/ssh/shellSSHTab2.fxml`（效率模式判断已注释） |
  | `public void flushGraphic()` | 刷新图标 | 无图标时 `ShellOsTypeComboBox.getGlyph(osType)` 生成 `SVGGlyph` |
  | `public void init(ShellConnect connect)` | 初始化 | 先 `controller().init(connect)`，再 `super.init(connect)`，异常打印栈 |
  | `protected String getTabTitle()` | 标题 | `名称(类型大写)` |
  | `public ShellSSHTabController controller()` | 控制器 | 强转 `super.controller()` |
  | `public ShellSSHClient client()` | 客户端 | 委托 `controller().getClient()` |
  | `public void runSnippet(String content)` | 执行片段 | 委托 `controller().runSnippet(content)` |
  | `static ShellSSHTab of(ShellConnect connect)` | 工厂 | new 后 `init(connect)` |

- 调用链：`of → init → ShellSSHTabController.init → ShellClientUtil.newClient → client.start`

## ShellSSHTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/ShellSSHTabController.java
- 职责：SSH 内容控制器，创建客户端并初始化各子面板（效率/服务器/Docker/进程/监控/配置）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | ShellSSHClient | SSH 客户端 |
  | connectStore | ShellConnectStore（final） | 连接存储，取 `ShellConnectStore.INSTANCE` |
  | effTabController | ShellSSHEffTabController | 效率模式面板（FXML 注入） |
  | serverTabController | ShellSSHServerTabController | 服务器信息面板（FXML 注入） |
  | dockerTabController | ShellSSHDockerTabController | Docker 面板（FXML 注入） |
  | processTabController | ShellSSHProcessTabController | 进程面板（FXML 注入） |
  | monitorTabController | ShellSSHMonitorTabController | 监控面板（FXML 注入） |
  | configTabController | ShellSSHConfigTabController | 配置面板（FXML 注入） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellSSHClient getClient()` | 取客户端 | 返回 `client` |
  | `ShellConnect shellConnect()` | 取连接 | `client.getShellConnect()` |
  | `void init(ShellConnect connect)` | 初始化 | `ShellClientUtil.newClient`；注册状态监听（INTERRUPTED 弹警告）；`StageManager.showMask` 内 `client.start()`，失败则 `closeTab`；`effTabController.init()`，并向 5 个子面板 `setClient(client)` |
  | `void onTabClosed(Event event)` | 关闭回调 | `IOUtil.close(client)`，`connectStore.update(shellConnect())` |
  | `List<? extends RichTabController> getSubControllers()` | 子控制器列表 | 汇总 docker/server/process/monitor/config/eff 面板 |
  | `void runSnippet(String content)` | 执行片段 | 委托 `effTabController.runSnippet` |

- 调用链：`init → ShellClientUtil.newClient → client.start → effTabController.init/setClient`；`onTabClosed → IOUtil.close → connectStore.update`

## ShellSSHEffTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/ShellSSHEffTabController.java
- 职责：SSH 效率模式面板，左文件表 + 右终端的分屏界面，含上传下载、服务监控、终端历史与片段。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | splitPane | FXSplitPane | 分割面板（FXML） |
  | leftBox | FXVBox | 左侧文件区（FXML） |
  | widget | ShellSSHTermWidget | SSH 终端组件（FXML） |
  | root | FXTab | 根标签（FXML） |
  | location | ShellFileLocationTextField | 当前路径输入（FXML） |
  | manage | SVGLabel | 上传/下载管理入口（FXML） |
  | refreshFile | SVGGlyph | 刷新文件（FXML） |
  | deleteFile | SVGGlyph | 删除文件（FXML） |
  | hiddenPane | HiddenSVGPane | 隐藏文件开关（FXML） |
  | fileTable | ShellSSHSFTPFileTableView | 文件表格（FXML） |
  | filterFile | ClearableTextField | 文件过滤（FXML） |
  | followTerminalDir | FXToggleSwitch | 跟随终端目录（FXML） |
  | serverMonitorInfo | FXLabel | 服务监控信息（FXML） |
  | serverMonitorTask | Future<?> | 服务监控定时任务 |
  | serverMonitor | FXToggleSwitch | 服务监控开关（FXML） |
  | showFile | FXToggleSwitch | 显示文件开关（FXML） |
  | termSize | FXText | 终端大小显示（FXML） |
  | fileInfo | FXLabel | 文件信息（FXML） |
  | fileName | TableColumn<ShellFile,?> | 文件名列（FXML） |
  | taskSizeListener | ListChangeListener<ShellFileTask> | 上传下载任务数监听器 |
  | taskTypes | List<ShellFileTaskType>（final） | 关注的任务类型：UPLOAD、DOWNLOAD |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init()` | 初始化面板 | `initWidget()` → `initFile()`；按 `shellConnect` 恢复 showFile/serverMonitor/followTerminalDir/hiddenFile 开关；异步 `initBackground` |
  | `void initWidget()` | 初始化终端组件 | `widget.initBackspaceCode/setAltSendsEscape/openSession(initTtyConnector())`，聚焦 |
  | `TtyConnector initTtyConnector()` | 创建连接器 | `widget.createTtyConnector(client)`；监听 `terminalSizeProperty` 更新 termSize；`enableZModem` 时用 `TtyTerminalUtil.createZModemTtyConnector` |
  | `void initFile()` | 初始化文件表 | `fileTable.setSSHClient/refreshFile`；`sftpClient.addTaskSizeListener` 更新 manage 计数；监听 `workDirProperty` 同步目录 |
  | `void initBackground()` | 背景 | `ShellConnectUtil.initTermBackground` |
  | `protected void bindListeners()` | 绑定事件 | 路径跳转、位置同步、跟随目录（`setResolveWorkerDir`）、监控开关、文件显隐、过滤、文件信息、快捷键与图标 cellFactory |
  | `void onTabClosed(Event)` | 关闭 | `widget.close()` |
  | `void refreshFile()/deleteFile()/returnDir()/intoHome()/mkdir()/touchFile()/uploadFile()/uploadFolder()` | 文件操作（@FXML） | 委托 `fileTable` 对应方法 |
  | `void draggedFile(ShellFileDraggedEvent)` | 拖拽上传（@EventSubscribe） | 选中态下 `uploadByPkg` 或 `uploadFile` |
  | `void manage()` | 管理传输（@FXML） | `ShellViewFactory.fileManage(sftpClient)` |
  | `void termHistory(MouseEvent)` | 终端历史（@FXML） | `ShellSSHUtil.histories` + `ShellViewFactory.termHistory`，选择后写入终端 |
  | `void refesh(MouseEvent)` | 刷新连接（@FXML） | `ShellEventUtil.connectionOpened` 后关闭本行 |
  | `void snippet(MouseEvent)` | 片段列表（@FXML） | `ShellSnippetAdapter.super.snippetList` |
  | `void runSnippet(String content)` | 写片段到终端 | `widget.getTtyConnector().write(content)` |
  | `void copyPathToTerminal()` | 复制路径到终端（@FXML） | 写入 `fileTable.getLocation()` |
  | `void initMonitorTask()/closeMonitorTask()` | 服务监控任务 | `TaskManager.startInterval` 每 3s 取 `serverExec.monitor()` 拼 CPU/内存/网络/磁盘文本 |
  | `void destroy()` | 销毁 | `closeMonitorTask` + `removeTaskSizeListener` + super |

- 调用链：`init → initWidget → widget.openSession`；`initFile → sftpClient.addTaskSizeListener`；`serverMonitor 开 → initMonitorTask → serverExec.monitor`

## ShellSSHServerTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/ShellSSHServerTabController.java
- 职责：SSH 服务器信息父面板，承载服务概览表与 CPU/磁盘/网络/内存/GPU 子标签。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | client | ShellSSHClient | 客户端 |
  | serverTable | FXTableView<ShellServerInfo> | 服务信息表（FXML） |
  | cpuController | ShellSSHServerCpuTabController | CPU 子面板（FXML） |
  | diskController | ShellSSHServerDiskTabController | 磁盘子面板（FXML） |
  | networkController | ShellSSHServerNetworkTabController | 网络子面板（FXML） |
  | memoryController | ShellSSHServerMemoryTabController | 内存子面板（FXML） |
  | gpuController | ShellSSHServerGpuTabController | GPU 子面板（FXML） |
  | serverExec | ShellServerExec | 服务执行对象 |
  | subscription | Subscription | 选中订阅句柄 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void setClient(ShellSSHClient client)` | 设置客户端 | 保存 client 并 `client.serverExec()` |
  | `void init()` | 初始化 | `StageManager.showMask`：`diskController.init()`；`serverExec.info()` 填入 serverTable |
  | `void onTabInit(FXTab tab)` | 初始化回调 | 订阅 root 选中：选中时 `init()` 并退订 |
  | `List<? extends RichTabController> getSubControllers()` | 子控制器 | 返回 5 个 server 子面板 |

- 调用链：`setClient → serverExec`；`onTabInit → 选中 → init → serverExec.info`

## ShellSSHConfigTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/ShellSSHConfigTabController.java
- 职责：SSH 服务配置父面板，按操作系统动态加载存在的配置文件子标签。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | tabPane | FXTabPane | 子标签容器（FXML） |
  | client | ShellSSHClient | 客户端 |
  | subControllers | List<RichTabController>（final） | 已加载的子控制器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void setClient(ShellSSHClient client)` | 设置客户端 | 仅保存 client（动态增删逻辑已注释） |
  | `ShellSSHClient getClient()` | 取客户端 | 返回 client |
  | `protected void bindListeners()` | 绑定 | super 后监听 root 选中，`tabPane.isChildEmpty()` 时 `loadTabs()` |
  | `List<? extends RichTabController> getSubControllers()` | 子控制器 | 返回 subControllers |
  | `void loadTabs()` | 加载配置标签 | Windows：加载 WinHosts/WinSshd/WinEnvironment；Linux：用 `sftpClient.exist` 判断后按序加载 profile/environment/bash/hosts/resolv/sshd 及用户 .profile/.zshrc/.bashrc/.bash_profile；注册 `selectedItemChanged` 惰性刷新首个控制器；最后刷新首个 `ShellSSHBaseConfigTabController` |
  | `void initSubTab(FXMLResult result)` | 初始化子标签 | 取 node/controller，`parent(this)`、`onTabInit`，加入 subControllers 与 tabPane |

- 调用链：`bindListeners → root 选中 → loadTabs → FXMLLoaderExt.loadFromUrl → initSubTab → ShellSSHBaseConfigTabController.refresh`

## ShellSSHDockerTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/ShellSSHDockerTabController.java
- 职责：SSH Docker 父面板，承载容器/镜像/守护配置/扩展子标签。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | docker | FXTab | 根标签（FXML） |
  | containerController | ShellSSHDockerContainerTabController | 容器面板（FXML） |
  | imageController | ShellSSHDockerImageTabController | 镜像面板（FXML） |
  | daemonController | ShellSSHDockerDaemonTabController | 守护配置面板（FXML） |
  | extraController | ShellSSHDockerExtraTabController | 扩展面板（FXML） |
  | client | ShellSSHClient | 客户端 |
  | initialized | boolean | 初始化标志 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void setClient(ShellSSHClient)/(getClient)` | 存取客户端 | 保存/返回 client |
  | `void init()` | 初始化 | 幂等；`client.dockerExec()`；`containerController.init(exec)`；`exec.docker_ps()` 判断命令缺失/未运行并提示，否则 `refreshContainer()` |
  | `void onTabInit(FXTab tab)` | 初始化回调 | 监听 docker 选中，选中时 `init()` |
  | `void loadContainer()/loadImage()` | 加载容器/镜像 | 委托子面板 refresh |
  | `List<? extends RichTabController> getSubControllers()` | 子控制器 | 返回 4 个子面板 |

- 调用链：`onTabInit → 选中 → init → dockerExec → containerController.refreshContainer`

## ShellSSHMonitorTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/ShellSSHMonitorTabController.java
- 职责：SSH 服务监控面板，用折线图展示 CPU/内存/磁盘/网络实时指标并定时刷新。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | client | ShellSSHClient | 客户端 |
  | serverExec | ShellServerExec | 服务执行对象 |
  | refreshBtn | FXToggleSwitch | 自动刷新开关（FXML） |
  | refreshTask | Future<?> | 刷新任务 |
  | cpuChart | FXLineChart<String,Double> | CPU 图（FXML） |
  | memoryChart | FXLineChart<String,Double> | 内存图（FXML） |
  | diskChart | FXLineChart<String,Double> | 磁盘图（FXML） |
  | networkChart | FXLineChart<String,Double> | 网络图（FXML） |
  | dateFormat | SimpleDateFormat（final） | 时间格式 `HH:mm:ss` |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void setClient(ShellSSHClient client)` | 设置客户端 | 保存 client 与 `client.serverExec()` |
  | `void init(ShellServerMonitor monitor)` | 初始化图表 | 依次 initCpu/Memory/Disk/NetworkChart |
  | `void initCpuChart/initMemoryChart(ShellServerMonitor)` | 单值图 | 首个 Series 不存在则新建并 `ChartHelper.addOrUpdateData` |
  | `void initDiskChart/initNetworkChart(ShellServerMonitor)` | 双值图 | 读写/收发两条 Series，值为 -1 时跳过 |
  | `void initRefreshTask()/closeRefreshTask()` | 刷新任务 | `TaskManager.startInterval(renderPane, 3000)` / `ExecutorUtil.cancel` |
  | `void renderPane()` | 渲染 | client 有效则取 `serverExec.monitor()` 并 `init` |
  | `void onTabInit(FXTab tab)` | 初始化回调 | root 选中且 refreshBtn 选中时启动任务，否则关闭；refreshBtn 变更切换任务 |
  | `void destroy()` | 销毁 | `closeRefreshTask` + super |

- 调用链：`refreshBtn 开 → initRefreshTask → renderPane → serverExec.monitor → init → ChartHelper.addOrUpdateData`

## ShellSSHProcessTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/ShellSSHProcessTabController.java
- 职责：SSH 进程监控面板，展示进程列表并支持过滤、用户过滤与自动刷新。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | client | ShellSSHClient | 客户端 |
  | filterProcess | ClearableTextField | 进程过滤（FXML） |
  | processType | ShellProcessTypeComboBox | 进程用户类型（FXML） |
  | processTable | ShellProcessInfoTableView | 进程表（FXML） |
  | refreshBtn | FXToggleSwitch | 自动刷新开关（FXML） |
  | refreshTask | Future<?> | 刷新任务 |
  | processExec | ShellProcessExec | 进程执行对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void setClient(ShellSSHClient client)` | 设置客户端 | 保存 client、`client.processExec()`，绑定到 `getProcessTable()` |
  | `void initRefreshTask()/closeRefreshTask()` | 刷新任务 | `ExecutorUtil.start(renderPane,0,3000)` / cancel |
  | `void renderPane()` | 渲染 | `processExec.ps()` → `processTable.updateData/sort` |
  | `void onTabInit(FXTab tab)` | 初始化回调 | root 选中切换任务；processType 变更时设置用户（非 0 用 `client.whoami()`）；过滤；刷新；搜索快捷键 |
  | `ShellProcessInfoTableView getProcessTable()` | 取表 | 返回 processTable（Windows 分支已注释） |
  | `void destroy()` | 销毁 | `closeRefreshTask` + super |

- 调用链：`refreshBtn 开 → initRefreshTask → renderPane → processExec.ps → processTable.updateData`

## ShellSSHBaseConfigTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHBaseConfigTabController.java
- 职责：SSH 配置文件标签的抽象基类，封装查看/复制/保存/应用与快捷键、高亮过滤。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | save | SVGGlyph | 保存按钮（FXML） |
  | refresh | SVGGlyph | 刷新按钮（FXML） |
  | apply | SVGGlyph | 应用按钮（FXML） |
  | data | ShellDataEditor | 数据编辑器（FXML） |
  | dataFilter | HighlightTextField | 数据过滤/高亮（FXML） |
  | init | boolean | 是否已初始化 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isInit()` | 是否已初始化 | 返回 init |
  | `void refresh()` | 刷新 | 置 init；`fileContent()` 读文件文本填入 data |
  | `void copy()` | 复制 | `ClipboardUtil.copy(data.getText())` |
  | `void save()` | 保存 | 取 `filePath()`；`sftpClient` 写临时文件再 `exec.cat_file(tempFile, filePath)`；空输出则删除临时文件；忽略 No such file 错误 |
  | `void apply()` | 应用 | `exec.source(filePath())`，非空输出告警 |
  | `protected void bindListeners()` | 绑定 | 内容区快捷键（save/refresh/apply/search），按钮提示组合键，data 的高亮属性绑定 dataFilter |
  | `ShellSSHClient client()` | 客户端 | `parent().getClient()` |
  | `ShellSFTPClient sftpClient()` | SFTP 客户端 | `client().sftpClient()` |
  | `abstract FXTab contentTab()` | 子类实现 | 返回根标签 |
  | `abstract String filePath()` | 子类实现 | 返回远端文件路径 |
  | `abstract String fileContent()` | 子类实现 | 返回文件内容 |

- 调用链：`refresh → fileContent → ShellSSHExec.cat_*`；`save → sftpClient.put → exec.cat_file`

## ShellSSHConfigBashTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigBashTabController.java
- 职责：`/etc/bash.bashrc` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | bash | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 bash |
  | `protected String filePath()` | 路径 | `/etc/bash.bashrc` |
  | `protected String fileContent()` | 内容 | `exec.cat_bash_bashrc()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHConfigEnvironmentTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigEnvironmentTabController.java
- 职责：`/etc/environment` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | environment | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 environment |
  | `protected String filePath()` | 路径 | `/etc/environment` |
  | `protected String fileContent()` | 内容 | `exec.cat_environment()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHConfigHostsTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigHostsTabController.java
- 职责：`/etc/hosts` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | hosts | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 hosts |
  | `protected String filePath()` | 路径 | `/etc/hosts` |
  | `protected String fileContent()` | 内容 | `exec.cat_hosts()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHConfigProfileTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigProfileTabController.java
- 职责：`/etc/profile` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | profile | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 profile |
  | `protected String filePath()` | 路径 | `/etc/profile` |
  | `protected String fileContent()` | 内容 | `exec.cat_profile()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHConfigResolvTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigResolvTabController.java
- 职责：`/etc/resolv.conf` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | resolv | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 resolv |
  | `protected String filePath()` | 路径 | `/etc/resolv.conf` |
  | `protected String fileContent()` | 内容 | `exec.cat_resolv()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHConfigSshdTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigSshdTabController.java
- 职责：`/etc/ssh/sshd_config` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | sshd | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 sshd |
  | `protected String filePath()` | 路径 | `/etc/ssh/sshd_config` |
  | `protected String fileContent()` | 内容 | `exec.cat_sshd_config()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHConfigUserBashProfileTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigUserBashProfileTabController.java
- 职责：`~/.bash_profile` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userBashProfile | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 userBashProfile |
  | `protected String filePath()` | 路径 | `~/.bash_profile` |
  | `protected String fileContent()` | 内容 | `exec.cat_user_bash_profile()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHConfigUserBashrcTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigUserBashrcTabController.java
- 职责：`~/.bashrc` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userBashrc | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 userBashrc |
  | `protected String filePath()` | 路径 | `~/.bashrc` |
  | `protected String fileContent()` | 内容 | `exec.cat_user_bashrc()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHConfigUserProfileTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigUserProfileTabController.java
- 职责：`~/.profile` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userProfile | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 userProfile |
  | `protected String filePath()` | 路径 | `~/.profile` |
  | `protected String fileContent()` | 内容 | `exec.cat_user_profile()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHConfigUserZshrcTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigUserZshrcTabController.java
- 职责：`~/.zshrc` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userZshrc | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 userZshrc |
  | `protected String filePath()` | 路径 | `~/.zshrc` |
  | `protected String fileContent()` | 内容 | `exec.cat_user_zshrc()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHConfigWinEnvironmentTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigWinEnvironmentTabController.java
- 职责：Windows 环境配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | winEnvironment | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 winEnvironment |
  | `protected String filePath()` | 路径 | 空串（不落盘） |
  | `protected String fileContent()` | 内容 | `exec.cat_environment()` |

- 调用链：`refresh → fileContent → ShellSSHExec.cat_environment`

## ShellSSHConfigWinHostsTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigWinHostsTabController.java
- 职责：Windows `HOSTS` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | winHosts | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 winHosts |
  | `protected String filePath()` | 路径 | `/C:/Windows/System32/drivers/etc/HOSTS` |
  | `protected String fileContent()` | 内容 | `exec.cat_hosts()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHConfigWinSshdTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/config/ShellSSHConfigWinSshdTabController.java
- 职责：Windows `sshd_config` 配置标签控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | winSshd | FXTab | 根标签（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `protected FXTab contentTab()` | 根标签 | 返回 winSshd |
  | `protected String filePath()` | 路径 | `/C:/ProgramData/ssh/sshd_config` |
  | `protected String fileContent()` | 内容 | `exec.cat_sshd_config()` |

- 调用链：`refresh/save → filePath/fileContent → ShellSSHExec`

## ShellSSHDockerContainerTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/docker/ShellSSHDockerContainerTabController.java
- 职责：Docker 容器面板，展示/过滤/删除容器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | refreshContainer | SVGGlyph | 刷新按钮（FXML） |
  | deleteContainer | SVGGlyph | 删除按钮（FXML） |
  | filterContainer | ClearableTextField | 过滤框（FXML） |
  | containerTable | ShellDockerContainerTableView | 容器表（FXML） |
  | containerStatus | ShellDockerContainerStatusComboBox | 状态过滤（FXML） |
  | initialized | boolean | 初始化标志 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init(ShellDockerExec exec)` | 初始化 | 幂等；`containerTable.setExec(exec)` |
  | `void onTabInit(FXTab tab)` | 初始化回调 | root 选中时 `init(client().dockerExec())`；过滤/状态变更；快捷键（search/refresh/delete） |
  | `ShellSSHDockerTabController parent()` | 父控制器 | 强转 |
  | `ShellSSHClient client()` | 客户端 | `parent().getClient()` |
  | `void refreshContainer()` | 刷新 | `containerTable.loadContainer()` |
  | `void deleteContainer()/deleteContainerForce()` | 删除 | `containerTable.deleteContainer(item, false/true)` |
  | `void onContainerRun(ShellContainerRunEvent)` | 运行事件（@EventSubscribe） | exec 匹配则刷新容器 |

- 调用链：`onTabInit → 选中 → init → containerTable.setExec`；`refreshContainer → containerTable.loadContainer`

## ShellSSHDockerImageTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/docker/ShellSSHDockerImageTabController.java
- 职责：Docker 镜像面板，展示/过滤/删除镜像。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | refreshImage | SVGGlyph | 刷新按钮（FXML） |
  | deleteImage | SVGGlyph | 删除按钮（FXML） |
  | filterImage | ClearableTextField | 过滤框（FXML） |
  | imageTable | ShellDockerImageTableView | 镜像表（FXML） |
  | initialized | boolean | 初始化标志 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init()` | 初始化 | 幂等；`imageTable.setExec(client().dockerExec())` 后 `refreshImage()` |
  | `void onTabInit(FXTab tab)` | 初始化回调 | root 选中时 init；过滤；快捷键 |
  | `ShellSSHClient client()` | 客户端 | `parent().getClient()` |
  | `void refreshImage()` | 刷新 | setExec + `imageTable.loadImage()` |
  | `void deleteImage()/deleteImageForce()` | 删除 | `imageTable.deleteImage(item, false/true)` |
  | `void onImageTag/onContainerCommit(...)` | 事件（@EventSubscribe） | exec 匹配则刷新镜像 |

- 调用链：`onTabInit → 选中 → init → refreshImage → imageTable.loadImage`

## ShellSSHDockerDaemonTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/docker/ShellSSHDockerDaemonTabController.java
- 职责：Docker 守护进程配置文件（daemon.json）查看与保存。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | filePath | FXLabel | 文件路径显示（FXML） |
  | data | ShellDataEditor | 数据编辑器（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void refresh()` | 刷新 | 首次取 `dockerExec().getDaemonFilePath()`；文件存在则 `exec.cat_file` 填入 data |
  | `void copy()` | 复制 | `ClipboardUtil.copy` |
  | `void save()` | 保存 | sftp 写临时文件 → `exec.cat_file(tempFile, jsonFile)` → 删除临时文件；忽略 No such file |
  | `void onDataKeyPressed(KeyEvent)` | 按键（@FXML） | Ctrl+S 触发保存 |
  | `void onTabInit(FXTab tab)` | 初始化回调 | root 选中时 refresh |
  | `ShellSSHClient client()/ShellSFTPClient sftpClient()` | 客户端 | 取父级 client / sftp |

- 调用链：`onTabInit → refresh → dockerExec.getDaemonFilePath → exec.cat_file`；`save → sftpClient.put → exec.cat_file`

## ShellSSHDockerExtraTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/docker/ShellSSHDockerExtraTabController.java
- 职责：Docker 扩展操作面板，提供信息/版本/重启/清理等动作。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellSSHDockerTabController parent()` | 父控制器 | 强转 |
  | `ShellSSHClient client()` | 客户端 | `parent().getClient()` |
  | `void dockerInfo()/dockerVersion()/dockerComposeVersion()` | 信息/版本（@FXML） | 调用 `exec.docker_info/version/compose_version`，经 `ShellViewFactory` 或弹窗展示 |
  | `void dockerRestart()` | 重启（@FXML） | mac/windows 不支持；确认后 `exec.docker_restart()` |
  | `void dockerPruneContainer()/Image()/Network()/Volume()` | 清理（@FXML） | 确认后调用对应 `docker_*_prune_f`，容器/镜像清理后回父级 loadContainer/loadImage |

- 调用链：`dockerPruneContainer → exec.docker_container_prune_f → parent().loadContainer`

## ShellSSHServerCpuTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/server/ShellSSHServerCpuTabController.java
- 职责：服务器 CPU 信息展示。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | cpuInfo | ReadOnlyTextArea | CPU 信息（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void refresh()` | 强制刷新（@FXML） | `refresh(true)` |
  | `void refresh(boolean force)` | 刷新 | 非强制且非空则跳过；`exec.cpu_info()` 填入 |
  | `void copyInfo()` | 复制（@FXML） | `ClipboardUtil.copy` |
  | `void onTabInit(FXTab tab)` | 初始化回调 | root 选中时 `refresh(false)` |
  | `ShellSSHClient client()` | 客户端 | `parent().getClient()` |

- 调用链：`onTabInit → 选中 → refresh(false) → exec.cpu_info`

## ShellSSHServerDiskTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/server/ShellSSHServerDiskTabController.java
- 职责：服务器磁盘信息表格展示。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | diskTable | ShellDiskInfoTableView | 磁盘表（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void refresh()` | 强制刷新（@FXML） | `refresh(true)` |
  | `void refresh(boolean force)` | 刷新 | 非强制且非空则跳过；调 `init()` |
  | `void init()` | 初始化 | `exec.disk_info()` → `diskTable.setItem` |
  | `void onTabInit(FXTab tab)` | 初始化回调 | root 选中时 `refresh(false)` |
  | `ShellSSHClient client()` | 客户端 | `parent().getClient()` |

- 调用链：`onTabInit → 选中 → refresh → init → exec.disk_info → diskTable.setItem`

## ShellSSHServerGpuTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/server/ShellSSHServerGpuTabController.java
- 职责：服务器 GPU 信息展示。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | gpuInfo | Editor | GPU 信息编辑器（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void refresh()` | 强制刷新（@FXML） | `refresh(true)` |
  | `void refresh(boolean force)` | 刷新 | 非强制且非空则跳过；`exec.gpu_info()` 填入 |
  | `void copyInfo()` | 复制（@FXML） | `ClipboardUtil.copy` |
  | `void onTabInit(FXTab tab)` | 初始化回调 | root 选中时 `refresh(false)` |
  | `ShellSSHClient client()` | 客户端 | `parent().getClient()` |

- 调用链：`onTabInit → 选中 → refresh(false) → exec.gpu_info`

## ShellSSHServerMemoryTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/server/ShellSSHServerMemoryTabController.java
- 职责：服务器内存信息展示。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | memoryInfo | ReadOnlyTextArea | 内存信息（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void refresh()` | 强制刷新（@FXML） | `refresh(true)` |
  | `void refresh(boolean force)` | 刷新 | 非强制且非空则跳过；`exec.memory_info()` 填入 |
  | `void copyInfo()` | 复制（@FXML） | `ClipboardUtil.copy` |
  | `void onTabInit(FXTab tab)` | 初始化回调 | root 选中时 `refresh(false)` |
  | `ShellSSHClient client()` | 客户端 | `parent().getClient()` |

- 调用链：`onTabInit → 选中 → refresh(false) → exec.memory_info`

## ShellSSHServerNetworkTabController
> 文件: cn/oyzh/easyshell/tabs/ssh/server/ShellSSHServerNetworkTabController.java
- 职责：服务器网卡信息展示。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTab | 根标签（FXML） |
  | networkCardInfo | ReadOnlyTextArea | 网卡信息（FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void refresh()` | 强制刷新（@FXML） | `refresh(true)` |
  | `void refresh(boolean force)` | 刷新 | 非强制且非空则跳过；`exec.network_interface_info()` 填入 |
  | `void copyInfo()` | 复制（@FXML） | `ClipboardUtil.copy` |
  | `void onTabInit(FXTab tab)` | 初始化回调 | root 选中时 `refresh(false)` |
  | `ShellSSHClient client()` | 客户端 | `parent().getClient()` |

- 调用链：`onTabInit → 选中 → refresh(false) → exec.network_interface_info`
