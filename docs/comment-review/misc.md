# easyshell 根目录与整包死代码说明（misc）

> 范围：`easyshell/src/main/java/cn/oyzh/easyshell/` 根目录直接文件（3 个），以及整包均为注释死代码的包（`ssh`、`ssh1`、`sftp`、`sftp1`、`vnc`、`db`）。
> 说明：仅新增文档，未改动任何 `.java`。整文件被注释掉的死代码不展开类段落，仅在“跳过清单”逐文件登记。

## 一、根目录文件

## EasyShellApp

- 职责：程序主入口与 JavaFX 应用生命周期管理，负责全局初始化（设置、国际化、字体、主题、事件总线）与退出清理。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| PROJECT | `Project`（static final） | 项目信息，由 `Project.load()` 加载 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static void main(String[] args)` | 程序主入口 | 设置系统属性（禁用 mysql 主动清理线程、关闭 BouncyCastle 自签名长度检查）；安装默认未捕获异常处理器；`SysConst` 写入项目名与 temp/store/cache 目录；`ShellStoreUtil.init()`；按平台设应用图标（Windows 用 `ICON_32_PATH`）；注册事件总线与同步/异步/默认事件配置；`launch(EasyShellApp.class, args)` |
| `void init()` | FX 初始化钩子 | 设 `FXConst.INSTANCE`；禁用 CSS 日志；取 `ShellSettingStore.SETTING`；依次 `I18nManager.apply`/`FontManager.apply`/`ThemeManager.apply`/`OpacityManager.apply`；注册 `ShellExceptionParser.INSTANCE` 异常解析器与事件监听；`super.init()` |
| `void start(Stage primaryStage)` | 启动钩子 | 注册 `TerminalManager` 的 5 个终端加载器（ZK/Redis/Mysql/Mongo/Dameng）；注册 `DBConditionManager`、`DBColumnFieldManager` 的 MYSQL/MONGODB/DAMENG 初始化器；按 `JarUtil.isInJar()` 决定 GC 间隔与对象观察、元数据打印；启动后台线程周期性 `SystemUtil.gc()` |
| `void stop()` | 停止钩子 | `ShellClientChecker.stop()`；`ShellStoreUtil.destroy()`；注销事件监听；清理缓存目录与 15 天前日志；`super.stop()` |
| `void showMainView()` | 显示主界面 | `ShellViewFactory.shellMain()` |
| `void onEventMsg(EventFormatter)` | 事件消息订阅 | `@EventSubscribe`，将消息加入 `ShellMessageTabController.EVENT_MESSAGES` |

- 调用链：`EasyShellBootstrap.main → EasyShellApp.main → launch → init → start → showMainView → ShellViewFactory.shellMain`

## EasyShellBootstrap

- 职责：极简启动器，转发到 `EasyShellApp.main`。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static void main(String[] args)` | 程序入口 | 直接调用 `EasyShellApp.main(args)` |

- 调用链：`EasyShellBootstrap.main → EasyShellApp.main`

## ShellConst

- 职责：全局常量与路径工具，提供图标路径与存储/临时/缓存目录。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| ICON_PATH | `String`（static final） | 应用图标 `/image/shell_no_bg.png` |
| ICON_24_PATH | `String`（static final，`@Deprecated`） | 托盘图标，Windows 专用 |
| ICON_32_PATH | `String`（static final） | 任务栏图标，Windows 专用 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static String getStorePath()` | 存储路径 | 在 Jar 中返回 `~/.easyshell/`，否则 `~/.easyshell_dev/` |
| `static String getTempPath()` | 临时路径 | 在 Jar 中返回 `tmp/easyshell/`，否则 `tmp/easyshell_dev/` |
| `static String getCachePath()` | 缓存路径 | `getTempPath() + "cache/"` |
| `static String getKeyCachePath()` | 键缓存路径 | `getTempPath() + "key_cache/"` |
| `static String getNodeCachePath()` | 节点缓存路径 | `getTempPath() + "node_cache/"` |

- 调用链：`EasyShellApp.main → SysConst.tempDir/getStorePath/getCachePath(ShellConst)`

## 二、整包死代码（跳过清单）

以下包的全部 `.java` 文件均为**整文件被注释**（每行以 `//` 开头，package、import、类体全部被注释），属死代码。依据“整文件被注释掉的死代码不列出”的规则，不展开类段落，仅逐文件登记原注释中声明的类型与角色。这些包已被 `ssh2`（SSH）、`sftp2`（SFTP）等重写版本取代。

### ssh（29 个，全部死代码）

| 文件 | 原注释声明的类型 | 备注 |
|---|---|---|
| `ssh/ShellBaseSSHClient.java` | `class ShellBaseSSHClient` | shell 客户端基类 |
| `ssh/ShellSSHClient.java` | `class ShellSSHClient` | ssh 客户端 |
| `ssh/ShellSSHAuthInteractive.java` | `class ShellSSHAuthInteractive` | ssh 认证用户信息 |
| `ssh/ShellSSHChannel.java` | `class ShellSSHChannel` | ssh 通道封装 |
| `ssh/ShellSSHShell.java` | `class ShellSSHShell` | ssh shell 会话 |
| `ssh/ShellSSHTermWidget.java` | `class ShellSSHTermWidget` | ssh 终端组件 |
| `ssh/ShellSSHTtyConnector.java` | `class ShellSSHTtyConnector` | ssh TTY 连接器 |
| `ssh/ShellSSHUtil.java` | `class ShellSSHUtil` | ssh 工具类 |
| `ssh/ShellSSHClientChecker.java` | `class ShellClientChecker` | 客户端监测器 |
| `ssh/ShellSSHClientActionUtil.java` | `class ShellClientActionUtil` | 客户端动作工具 |
| `ssh/docker/ShellDockerContainer.java` | `class ShellDockerContainer` | docker 容器定义 |
| `ssh/docker/ShellDockerExec.java` | `class ShellDockerExec` | docker 执行器 |
| `ssh/docker/ShellDockerImage.java` | `class ShellDockerImage` | docker 镜像 |
| `ssh/docker/ShellDockerImageHistory.java` | `class ShellDockerImageHistory` | docker 镜像历史 |
| `ssh/docker/ShellDockerParser.java` | `class ShellDockerParser` | docker 解析器 |
| `ssh/docker/ShellDockerPort.java` | `class ShellDockerPort` | docker 端口信息 |
| `ssh/docker/ShellDockerResource.java` | `class ShellDockerResource` | docker 资源 |
| `ssh/server/ShellServerExec.java` | `class ShellServerExec` | 服务器信息执行器 |
| `ssh/server/ShellServerInfo.java` | `class ShellServerInfo` | 服务器信息 |
| `ssh/server/ShellServerDisk.java` | `class ShellServerDisk` | 服务器磁盘 |
| `ssh/server/ShellServerNetwork.java` | `class ShellServerNetwork` | 服务器网络 |
| `ssh/server/ShellServerMonitor.java` | `class ShellServerMonitor` | 服务器监控 |
| `ssh/exec/ShellSSHExec.java` | `class ShellSSHExec` | ssh 命令执行器 |
| `ssh/exec/ShellSSHExecParser.java` | `class ShellSSHExecParser` | ssh 命令解析器 |
| `ssh/exec/ShellSSHDiskInfo.java` | `class ShellSSHDiskInfo` | 磁盘信息 |
| `ssh/process/ShellProcessExec.java` | `class ShellProcessExec` | 进程执行器 |
| `ssh/process/ShellProcessInfo.java` | `class ShellProcessInfo` | 进程信息 |
| `ssh/process/ShellProcessAttr.java` | `class ShellProcessAttr` | 进程属性 |
| `ssh/process/ShellProcessParser.java` | `class ShellProcessParser` | 进程解析器 |

### ssh1（27 个，全部死代码）

| 文件 | 原注释声明的类型 | 备注 |
|---|---|---|
| `ssh1/ShellBaseSSHClient.java` | `class ShellBaseSSHClient` | shell 客户端基类 |
| `ssh1/ShellSSHClient.java` | `class ShellSSHClient` | ssh 客户端 |
| `ssh1/ShellSSHAuthInteractive.java` | `class ShellSSHAuthInteractive` | 认证 UI（密码 + 验证码双因子） |
| `ssh1/ShellSSHTermWidget.java` | `class ShellSSHTermWidget` | ssh 终端组件 |
| `ssh1/ShellSSHTtyConnector.java` | `class ShellSSHTtyConnector` | ssh TTY 连接器 |
| `ssh1/ShellSSHUtil.java` | `class ShellSSHUtil` | ssh 工具类 |
| `ssh1/ShellSSHClientChecker.java` | `class ShellClientChecker` | 客户端监测器 |
| `ssh1/ShellSSHClientActionUtil.java` | `class ShellClientActionUtil` | 客户端动作工具 |
| `ssh1/docker/ShellDockerContainer.java` | `class ShellDockerContainer` | docker 容器定义 |
| `ssh1/docker/ShellDockerExec.java` | `class ShellDockerExec` | docker 执行器 |
| `ssh1/docker/ShellDockerImage.java` | `class ShellDockerImage` | docker 镜像 |
| `ssh1/docker/ShellDockerImageHistory.java` | `class ShellDockerImageHistory` | docker 镜像历史 |
| `ssh1/docker/ShellDockerParser.java` | `class ShellDockerParser` | docker 解析器 |
| `ssh1/docker/ShellDockerPort.java` | `class ShellDockerPort` | docker 端口信息 |
| `ssh1/docker/ShellDockerResource.java` | `class ShellDockerResource` | docker 资源 |
| `ssh1/server/ShellServerExec.java` | `class ShellServerExec` | 服务器信息执行器 |
| `ssh1/server/ShellServerInfo.java` | `class ShellServerInfo` | 服务器信息 |
| `ssh1/server/ShellServerDisk.java` | `class ShellServerDisk` | 服务器磁盘 |
| `ssh1/server/ShellServerNetwork.java` | `class ShellServerNetwork` | 服务器网络 |
| `ssh1/server/ShellServerMonitor.java` | `class ShellServerMonitor` | 服务器监控 |
| `ssh1/exec/ShellSSHExec.java` | `class ShellSSHExec` | ssh 命令执行器 |
| `ssh1/exec/ShellSSHExecParser.java` | `class ShellSSHExecParser` | ssh 命令解析器 |
| `ssh1/exec/ShellSSHDiskInfo.java` | `class ShellSSHDiskInfo` | 磁盘信息 |
| `ssh1/process/ShellProcessExec.java` | `class ShellProcessExec` | 进程执行器 |
| `ssh1/process/ShellProcessInfo.java` | `class ShellProcessInfo` | 进程信息 |
| `ssh1/process/ShellProcessAttr.java` | `class ShellProcessAttr` | 进程属性 |
| `ssh1/process/ShellProcessParser.java` | `class ShellProcessParser` | 进程解析器 |

### sftp（8 个，全部死代码）

| 文件 | 原注释声明的类型 | 备注 |
|---|---|---|
| `sftp/ShellSFTPClient.java` | `class ShellSFTPClient` | sftp 客户端 |
| `sftp/ShellSFTPChannel.java` | `class ShellSFTPChannel` | sftp 通道 |
| `sftp/ShellSFTPChannelPool.java` | `class ShellSFTPChannelPool` | sftp 通道管理器 |
| `sftp/ShellSFTPCache.java` | `class ShellSFTPCache` | sftp 链接文件管理器 |
| `sftp/ShellSFTPFile.java` | `class ShellSFTPFile` | sftp 文件 |
| `sftp/ShellSFTPAttr.java` | `class ShellSFTPAttr` | sftp 文件属性 |
| `sftp/ShellSFTPRealpathManager.java` | `class ShellSFTPRealpathManager` | sftp 链接文件管理器 |
| `sftp/ShellSFTPUtil.java` | `class ShellSFTPUtil` | sftp 工具类 |

### sftp1（6 个，全部死代码）

| 文件 | 原注释声明的类型 | 备注 |
|---|---|---|
| `sftp1/ShellSFTPClient.java` | `class ShellSFTPClient` | sftp 客户端 |
| `sftp1/ShellSFTPClintPool.java` | `class ShellSFTPClintPool` | sftp 通道管理器 |
| `sftp1/ShellSFTPFile.java` | `class ShellSFTPFile` | sftp 文件 |
| `sftp1/ShellSFTPAttr.java` | `class ShellSFTPAttr` | sftp 文件属性 |
| `sftp1/ShellSFTPRealpathCache.java` | `class ShellSFTPCache` | sftp 链接文件管理器 |
| `sftp1/ShellSFTPUtil.java` | `class ShellSFTPUtil` | sftp 工具类 |

### vnc（3 个，全部死代码）

| 文件 | 原注释声明的类型 | 备注 |
|---|---|---|
| `vnc/ShellVNCClient.java` | `class ShellVNCClient` | vnc 客户端 |
| `vnc/ShellVNCConnection.java` | `class ShellVNCConnection` | vnc 连接 |
| `vnc/ShellVNCRenderService.java` | `class ShellVNCRenderService` | vnc 渲染服务 |

### db（7 个，全部死代码）

| 文件 | 原注释声明的类型 | 备注 |
|---|---|---|
| `db/DBColumn.java` | `interface DBColumn` | 数据库列接口 |
| `db/DBColumnField.java` | `class DBColumnField` | 字段域 |
| `db/DBColumnFieldManager.java` | `class DBColumnFieldManager` | 字段域管理器 |
| `db/DBConnConfig.java` | `class DBConnConfig` | 连接配置 |
| `db/DBConnManager.java` | `class DBConnManager` | 连接管理器 |
| `db/DBRecordProperty.java` | `class DBRecordProperty` | db 表记录属性 |
| `db/DBSqlGenerator.java` | `class DBSqlGenerator` | SQL 生成器 |

> 覆盖存活的类数：**3**（根目录 3 个）
> 跳过的死代码文件数：**80**（`ssh` 29 + `ssh1` 27 + `sftp` 8 + `sftp1` 6 + `vnc` 3 + `db` 7）
