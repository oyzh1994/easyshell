# easyshell FTP 客户端（ftp 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/ftp/，共 3 个 .java，全部存活。

本包基于 Apache Commons Net 的 `FTPClient` / `FTPSClient` 实现 FTP/FTPS 文件访问，向 `ShellFileClient` 体系提供统一文件操作接口。`ShellFTPClient` 为客户端主体，`ShellFTPFile` 为文件模型，`ShellFTPUtil` 提供权限字符串转换。

## ShellFTPClient

- 职责：FTP/FTPS 客户端主体，实现 `ShellFileClient<ShellFTPFile>`，负责连接、登录、列举、上传下载及增删改查。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| client | `FTPClient` | 底层 FTP 客户端（SSL 模式下为 `FTPSClient`） |
| shellConnect | `ShellConnect` | 连接配置 |
| streamMode | `transient boolean` | 是否处于流模式（流式上传/下载），用于 `closeDelayResources` 收尾 |
| state | `final SimpleObjectProperty<ShellConnState>` | 连接状态，初值 NOT_INITIALIZED |
| stateListener | `final ChangeListener<ShellConnState>` | 状态监听器，回调 `onStateChanged` |
| deleteTasks | `final ObservableList<ShellFileDeleteTask>` | 删除任务列表 |
| uploadTasks | `final ObservableList<ShellFileUploadTask>` | 上传任务列表 |
| downloadTasks | `final ObservableList<ShellFileDownloadTask>` | 下载任务列表 |
| transportTasks | `final ObservableList<ShellFileTransportTask>` | 传输任务列表 |
| deleteCompetitor | `final Competitor` | 删除竞争器，并发数 5 |
| uploadCompetitor | `final Competitor` | 上传竞争器，并发数 2 |
| downloadCompetitor | `final Competitor` | 下载竞争器，并发数 2 |
| transportCompetitor | `final Competitor` | 传输竞争器，并发数 2 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellFTPClient(ShellConnect shellConnect)` | 构造 | 保存连接，注册 `stateListener` |
| `stateProperty()` | 状态属性 | 返回 `state` |
| `close()` | 关闭客户端 | `logout` + `disconnect`，置 CLOSED，移除监听器并清空任务列表 |
| `initClient()` | 初始化客户端 | SSL 模式用 `FTPSClient` 并设置接受全部证书的 `TrustManager`；设置字符集；启用代理时 `setProxy` |
| `start(int timeout)` | 启动连接 | 初始化 → CONNECTING → 设置连接/数据超时 → `connect(hostIp, port)` → 登录 → FTPS 执行 PBSZ/PROT → 主动/被动模式 → keepAlive/soTimeout → `lsFile("/")` 探测 → CONNECTED + `ShellClientChecker.push`；finally `SystemUtil.gc()` |
| `getShellConnect()` | 获取连接 | 返回 `shellConnect` |
| `isConnected()` | 是否已连接 | `client != null && client.isConnected()` |
| `delete(String file)` | 删除文件 | `client.deleteFile(file)` |
| `deleteDir(String dir)` | 删除目录 | 依次尝试 `removeDirectory`、`SITE RMTDIR`、`SITE RMDA`、`RMD` 命令 |
| `deleteDirRecursive(String dir)` | 递归删除目录 | `listFiles` 遍历，子目录递归、文件调 `delete`，最后 `deleteDir` 删除空目录 |
| `rename(ShellFTPFile, String)` | 重命名 | 拼接新路径后 `client.rename` |
| `put(InputStream, String, Function)` | 上传流 | `BINARY_FILE_TYPE` + 流模式；可选进度监控包装；`storeFile` 后 `setModificationTime` |
| `putStream(String, Function)` | 获取上传输出流 | `storeFileStream`，可选进度监控包装，置流模式 |
| `get(ShellFTPFile, String, Function)` | 下载到本地文件 | `BINARY_FILE_TYPE` + 流模式；可选进度监控包装 `FileOutputStream`；`retrieveFile` |
| `getStream(ShellFTPFile, Function)` | 获取下载流 | `BINARY_FILE_TYPE` + 流模式；`retrieveFileStream`，可选进度监控包装 |
| `deleteCompetitor()` / `uploadCompetitor()` / `downloadCompetitor()` / `transportCompetitor()` | 获取各竞争器 | 返回对应 `Competitor` 字段 |
| `deleteTasks()` / `uploadTasks()` / `downloadTasks()` / `transportTasks()` | 获取各任务列表 | 返回对应 `ObservableList` 字段 |
| `closeDelayResources()` | 关闭延迟资源 | 流模式下调用 `completePendingCommand()` 收尾 |
| `lsFileDynamic(String, Consumer)` | 动态列举文件 | `listFiles` 遍历；符号链接经 `getFile("/" + link)` 解析后构造 `ShellFTPFile` |
| `createDir(String filePath)` | 创建目录 | `client.makeDirectory(filePath)` |
| `workDir()` | 工作目录 | `client.printWorkingDirectory()` |
| `cd(String filePath)` | 切换目录 | `client.changeWorkingDirectory(filePath)` |
| `getFile(String filePath)` | 获取单个 FTPFile | 先 `mlistFile`，失败则按父路径 `listFiles` 匹配文件名 |
| `touch(String filePath)` | 创建空文件 | 不存在时以空字节流 `storeFile` |
| `exist(String filePath)` | 判断存在 | `size` 返回值判断（550 时用 `mlistFile` 复核）；再尝试 `listFiles` 判断目录；`IndexOutOfBoundsException` 返回 false |
| `realpath(String filePath)` | 真实路径 | 抛 `UnsupportedOperationException` |
| `chmod(int, String)` | 修改权限 | `SITE CHMOD <permissions> <path>` |
| `fileInfo(String filePath)` | 文件信息 | `getFile` 获取文件，符号链接解析后构造 `ShellFTPFile` |
| `forkClient()` | 派生子客户端 | 新建带 `isForked=true` 的匿名子类并 `start()` |
| `isRealpathSupport()` | 是否支持 realpath | 返回 `false` |

- 调用链：`ShellFTPClient.start → initClient → lsFile("/")（ShellFileClient 默认方法）→ lsFileDynamic → FTPClient.listFiles`
- 调用链：`ShellFTPClient.fileInfo → getFile → FTPClient.mlistFile / listFiles → new ShellFTPFile`

## ShellFTPFile

- 职责：FTP 文件模型，实现 `ShellFile`，包装 `FTPFile`，支持符号链接与权限位读写。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| file | `FTPFile` | FTP 文件对象 |
| linkFile | `FTPFile` | 符号链接目标文件 |
| parentPath | `String` | 父路径 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellFTPFile(String parentPath, FTPFile file, FTPFile linkFile)` | 构造 | 保存 file、linkFile、parentPath |
| `isLink()` | 是否符号链接 | `file.isSymbolicLink()` |
| `isDirectory()` | 是否目录 | 链接时看 linkFile，否则看 file |
| `isFile()` | 是否普通文件 | 链接时看 linkFile，否则看 file |
| `getFileName()` | 文件名 | `file.getName()` |
| `setFileName(String)` | 设置文件名 | `file.setName(newName)` |
| `getParentPath()` / `setParentPath(String)` | 父路径读写 | 返回/设置 parentPath |
| `getFileSize()` / `setFileSize(long)` | 文件大小 | 读写 `file.getSize()` |
| `getOwner()` | 拥有者 | `file.getUser()` |
| `getGroup()` | 分组 | `file.getGroup()` |
| `getModifyTime()` | 修改时间 | 非正常文件返回空串，否则格式化 `file.getTimestamp()` |
| `setModifyTime(String)` | 设置修改时间 | 解析为 `Calendar` 后 `file.setTimestamp` |
| `getPermissions()` | 获取权限 | `ShellFTPUtil.getPermissionsString(file)` |
| `setPermissions(String)` | 设置权限 | 10 位时截去类型位，逐位写入 USER/GROUP/WORLD 的读写执行权限 |
| `copy(ShellFile)` | 复制字段 | 目标为 `ShellFTPFile` 时拷贝 file/linkFile/parentPath |
| `destroy()` | 销毁 | 置空 file、linkFile、parentPath |

- 调用链：`ShellFTPFile.getPermissions → ShellFTPUtil.getPermissionsString → ShellFTPUtil.getFileTypePrefix / getPermissionChar`

## ShellFTPUtil

- 职责：FTP 工具类，将 `FTPFile` 的权限位转换为 10 位权限字符串。
- 字段：无字段（纯静态方法工具类）
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getPermissionsString(FTPFile file)` | 生成 10 位权限串 | 拼接类型前缀 + USER/GROUP/WORLD 各三位权限字符 |
| `getFileTypePrefix(FTPFile file)` | 文件类型前缀 | 目录 `d`、符号链接 `l`、其他 `-` |
| `getPermissionChar(FTPFile file, int who, int permission)` | 单个权限字符 | `file.hasPermission(who, permission)` 为真返回权限符号，否则 `-` |
| `getPermissionSymbol(int permission)` | 权限符号 | READ `r`、WRITE `w`、EXECUTE `x`，默认 `-` |

- 调用链：`ShellFTPClient.lsFileDynamic → new ShellFTPFile → ShellFTPFile.getPermissions → ShellFTPUtil.getPermissionsString`
