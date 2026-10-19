# easyshell WebDAV 客户端（webdav 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/webdav/，共 4 个 .java，全部存活。

本包基于 Sardine 实现 WebDAV 文件访问，向 `ShellFileClient` 体系提供统一文件操作接口。`ShellWebdavClient` 为客户端主体，`ShellWebdavFile` 为文件模型，`ShellWebdavSardine` 扩展 Sardine 以支持代理与超时，`ShellWebdavUtil` 提供路径判断工具。

## ShellWebdavClient

- 职责：WebDAV 客户端主体，实现 `ShellFileClient<ShellWebdavFile>`，通过 `ShellWebdavSardine` 完成列举、上传下载、增删改查等操作。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| state | `final SimpleObjectProperty<ShellConnState>` | 连接状态 |
| stateListener | `final ChangeListener<ShellConnState>` | 状态监听器，回调 `onStateChanged` |
| delayInputStreams | `final List<InputStream>` | 延迟关闭的下载输入流 |
| sardine | `ShellWebdavSardine` | 实际操作的 WebDAV 客户端 |
| connect | `final ShellConnect` | 连接配置 |
| deleteCompetitor | `final Competitor` | 删除竞争器，并发数 5 |
| deleteTasks | `final ObservableList<ShellFileDeleteTask>` | 删除任务列表 |
| uploadCompetitor | `final Competitor` | 上传竞争器，并发数 5 |
| uploadTasks | `final ObservableList<ShellFileUploadTask>` | 上传任务列表 |
| downloadCompetitor | `final Competitor` | 下载竞争器，并发数 5 |
| downloadTasks | `final ObservableList<ShellFileDownloadTask>` | 下载任务列表 |
| transportCompetitor | `final Competitor` | 传输竞争器，并发数 5 |
| transportTasks | `final ObservableList<ShellFileTransportTask>` | 传输任务列表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellWebdavClient(ShellConnect connect)` | 构造 | 保存连接，状态置 NOT_INITIALIZED，注册 `stateListener` |
| `initClient()` | 初始化客户端 | 依据 `connect.isEnableProxy()` 构造带/不带代理配置的 `ShellWebdavSardine` |
| `lsFileDynamic(String, Consumer)` | 动态列举文件 | `getFullDirPath` 补全 → `sardine.list`，跳过自身与坚果云根目录项 → 构造 `ShellWebdavFile` 回调；忽略 404 |
| `delete(String file)` | 删除文件 | `getFullFilePath` → `sardine.delete` |
| `deleteDir(String dir)` | 删除目录 | `getFullDirPath` → `sardine.delete` |
| `deleteDirRecursive(String dir)` | 递归删除目录 | 直接转调 `deleteDir`（递归逻辑源码中已注释） |
| `rename(ShellWebdavFile, String)` | 重命名 | `sardine.copy` 到新路径后 `sardine.delete` 旧路径 |
| `exist(String filePath)` | 判断存在 | `existFile` 或 `existDir` |
| `existFile(String)` / `existDir(String)` | 文件/目录是否存在 | 先 `sardine.exists`，异常再 `sardine.list`；仅对 301/401/404 静默返回 false |
| `realpath(String)` | 真实路径 | 抛 `UnsupportedOperationException` |
| `isRealpathSupport()` | 是否支持 realpath | 返回 `false` |
| `touch(String filePath)` | 创建空文件 | `getFullFilePath` → `sardine.put(fullPath, new byte[0])` |
| `createDir(String filePath)` | 创建目录 | `getFullDirPath` → `sardine.createDirectory` |
| `workDir()` | 工作目录 | 抛 `UnsupportedOperationException` |
| `isWorkDirSupport()` | 是否支持工作目录 | 返回 `false` |
| `cd(String)` | 切换目录 | 抛 `UnsupportedOperationException` |
| `isCdSupport()` | 是否支持 cd | 返回 `false` |
| `get(ShellWebdavFile, String, Function)` | 下载到本地文件 | `sardine.get` 取流，`IOUtil.saveToStream` 写入本地；可选 `ShellFileProgressMonitor` 包装 |
| `getStream(ShellWebdavFile, Function)` | 获取下载流 | `sardine.get`，可选进度监控包装；finally 将流加入 `delayInputStreams` |
| `put(InputStream, String, Function)` | 上传流 | 可选用进度监控包装；先 `put2`（带 Authorization 头），失败再 `put1`；finally 关闭流 |
| `put1(String, InputStream)` | 上传方式一 | `sardine.put(fullPath, stream)`，仅 400/401/non-repeatable 静默 |
| `put2(String, InputStream)` | 上传方式二（带头） | `sardine.put(fullPath, stream, Map.of("Authorization", ...))` |
| `putStream(String, Function)` | 获取上传输出流 | 抛 `UnsupportedOperationException` |
| `isPutStreamSupport()` | 是否支持 putStream | 返回 `false` |
| `deleteCompetitor()` / `uploadCompetitor()` / `downloadCompetitor()` / `transportCompetitor()` | 获取各竞争器 | 返回对应 `Competitor` 字段 |
| `deleteTasks()` / `uploadTasks()` / `downloadTasks()` / `transportTasks()` | 获取各任务列表 | 返回对应 `ObservableList` 字段 |
| `closeDelayResources()` | 关闭延迟资源 | 关闭 `delayInputStreams` 中的流 |
| `chmod(int, String)` | 修改权限 | 返回 `false`（不支持） |
| `isChmodSupport()` | 是否支持 chmod | 返回 `false` |
| `fileInfo(String)` | 文件信息 | 先 `fileInfoFile`，为空再 `fileInfoDir` |
| `fileInfoFile(String)` / `fileInfoDir(String)` | 文件/目录信息 | 分别按文件/目录路径 `sardine.list`，取首个 `DavResource` 构造 `ShellWebdavFile` |
| `start(int timeout)` | 启动连接 | `initClient` → 状态 CONNECTING → `lsFile("/")` 探测 → CONNECTED + `ShellClientChecker.push`；失败置 FAILED；finally `SystemUtil.gc()` |
| `getShellConnect()` | 获取连接 | 返回 `connect` |
| `isConnected()` | 是否已连接 | `sardine != null` 且状态为已连接 |
| `stateProperty()` | 状态属性 | 返回 `state` |
| `close()` | 关闭客户端 | 关闭 sardine、`closeDelayResources`，置 CLOSED 并清空任务列表 |
| `getFullDirPath(String)` | 补全目录路径 | `ShellFileUtil.concat(host, path)` 再拼接 `/` |
| `getFullFilePath(String)` | 补全文件路径 | `ShellFileUtil.concat(host, path)` |
| `getAuthorization()` | 获取认证信息 | `HttpUtil.basic(user, password)` |

- 调用链：`ShellWebdavClient.start → initClient → lsFile("/")（ShellFileClient 默认方法）→ lsFileDynamic → ShellWebdavSardine.list`
- 调用链：`ShellWebdavClient.put → put2 → ShellWebdavSardine.put（失败回退 put1）`

## ShellWebdavFile

- 职责：WebDAV 文件模型，实现 `ShellFile`，以 `DavResource` 为主要数据来源。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| fileName | `String` | 文件名（可覆盖） |
| parentPath | `String` | 父路径 |
| resource | `DavResource` | Sardine 资源对象 |
| lastModified | `Date` | 覆盖用最后修改时间 |
| fileSize | `Long` | 覆盖用文件大小 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellWebdavFile(String parentPath, DavResource resource)` | 构造 | 持有 resource 与 parentPath |
| `isFile()` | 是否文件 | `!isDirectory()` |
| `isLink()` | 是否链接 | 固定 `false` |
| `getOwner()` / `getGroup()` | 拥有者/分组 | 固定返回 `-` |
| `getFileSize()` / `setFileSize(long)` | 文件大小 | fileSize 不为空用其值，否则 `resource.getContentLength()` |
| `getFileName()` | 文件名 | fileName 优先，否则 `resource.getName()`，再退化 `getDisplayName()` |
| `setFileName(String)` | 设置文件名 | 设置 fileName |
| `getParentPath()` | 父路径 | 返回 parentPath |
| `isDirectory()` | 是否目录 | `resource.isDirectory()` |
| `getPermissions()` / `setPermissions(String)` | 权限 | 读取固定 `-`，设置空实现 |
| `getModifyTime()` | 修改时间 | lastModified 优先，否则 `resource.getModified()`，格式化输出 |
| `setModifyTime(String)` | 设置修改时间 | 解析字符串存 lastModified |
| `getAddTime()` | 添加时间 | `resource.getCreation()`，为空返回 `-` |
| `copy(ShellFile)` | 复制字段 | 目标为 `ShellWebdavFile` 时拷贝 fileSize/lastModified/fileName/parentPath |
| `destroy()` | 销毁 | 置空各字段 |

- 调用链：`ShellWebdavClient.lsFileDynamic → new ShellWebdavFile(filePath, resource) → ShellWebdavFile.getFileName → DavResource.getName`

## ShellWebdavSardine

- 职责：`SardineImpl` 的扩展实现，支持自定义超时与 HTTP/SOCKS 代理。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| timeout | `final int` | 连接/读取/请求超时（毫秒） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellWebdavSardine(int, String, String)` | 构造（无代理） | 转调四参构造，proxyConfig 为 null |
| `ShellWebdavSardine(int, String, String, ShellProxyConfig)` | 构造 | 反射调用 `createDefaultCredentialsProvider` 构造凭证；按代理类型创建 `ProxySelector`；`configure` 构建 `HttpClientBuilder`；反射写入字段 `builder` 并 `build` 为 `client` |
| `close()` | 关闭 | 调用父类 `shutdown()` |
| `configure(ProxySelector, CredentialsProvider)` | 配置客户端构建器 | 先 `super.configure`，再设置 socket/connect/connectionRequest 三类超时 |

- 调用链：`ShellWebdavClient.initClient → new ShellWebdavSardine → ShellWebdavSardine.configure → SardineImpl.configure`

## ShellWebdavUtil

- 职责：WebDAV 路径判断工具，用于在列举结果中过滤自身与特殊根目录。
- 字段：无字段（纯静态方法工具类）
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isSalf(DavResource resource, String path)` | 是否文件自身 | 去除首尾 `/` 后比较 `resource.getPath()` 与 path；路径层级大于 2 时截取子路径再比较 |
| `isRoot(DavResource resource)` | 是否根目录 | 去除尾部 `/` 后按 `/` 出现次数是否小于 2 判断 |
| `isJianguoYunRoot(DavResource resource)` | 是否坚果云根目录 | `resource.getHref().getPath()` 是否等于 `/dav/` |

- 调用链：`ShellWebdavClient.lsFileDynamic → ShellWebdavUtil.isSalf / isJianguoYunRoot`
