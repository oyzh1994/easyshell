# easyshell 文件传输抽象层（file 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/file/，共 11 个 .java，全部存活。

---

## ShellFile

- 职责：定义远程文件/目录的统一抽象接口，暴露文件名、路径、大小、权限、图标、等待动画等能力，供各协议文件客户端复用。
- 字段：无字段（接口，`extends ObjectCopier<ShellFile>, Destroyable`）。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isFile()` / `isDirectory()` / `isLink()` | 文件类型判定 | 抽象方法，由实现类提供 |
| `getOwner()` / `getGroup()` | 获取拥有者/分组 | 抽象方法 |
| `getFileSize()` / `setFileSize(long)` | 读写文件大小 | 抽象方法 |
| `getFileSizeDisplay()` | 文件大小显示文本 | 目录/返回项/当前项返回 `-`，否则 `NumberUtil.formatSize(size, 2)` |
| `getFileName()` / `setFileName(String)` | 读写文件名 | 抽象方法 |
| `getParentPath()` / `getPermissions()` / `setPermissions(String)` / `getModifyTime()` / `setModifyTime(String)` | 父路径、权限、修改时间 | 抽象方法 |
| `getFileOrder()` | 排序权重 | 返回目录 `-10`、链接 `-9`、隐藏目录 `-8`、隐藏文件 `-7`、目录 `-6`、普通 `0` |
| `getFilePath()` | 完整文件路径 | 调用 `ShellFileUtil.concat(getParentPath(), getFileName())` |
| `getIcon()` | 返回节点图标 | 依类型返回 `ReturnFolderSVGGlyph`/`FolderLinkSVGGlyph`/`FileLinkSVGGlyph`/`FolderSVGGlyph`/`FileSVGGlyph`；隐藏文件 `setOpacity(0.5)` |
| `startWaiting()` / `stopWaiting()` / `isWaiting()` | 等待动画开关/查询 | 委托 `getIcon()` 的 `startWaiting/stopWaiting/isWaiting` |
| `isCurrentFile()` / `isReturnDirectory()` | 是否 `.` / `..` 项 | 文件名比较 |
| `isNormal()` | 是否正常项 | `!isCurrentFile() && !isReturnDirectory()` |
| `isHiddenFile()` | 是否隐藏文件 | 非 `.`/`..` 且文件名以 `.` 开头 |
| `hasOwnerReadPermission()` 等 9 个 `has*Permission()` | 拥有者/组/其他用户读/写/执行权限 | 分别委托 `ShellFileUtil.hasOwnerReadPermission(...)` 等同名工具方法 |
| `isRoot()` | 是否根目录 | `isDirectory() && "/".equals(getFilePath())` |
| `getExtName()` | 文件扩展名 | `FileNameUtil.extName(getFileName())` |

- 调用链：`ShellFile.getFilePath() → ShellFileUtil.concat() → ...`；`ShellFile.hasOwnerReadPermission() → ShellFileUtil.hasOwnerReadPermission() → ...`；`ShellFile.getIcon() → SVGGlyph 子类`

---

## ShellFileClient

- 职责：文件操作客户端统一接口（继承 `ShellBaseClient`），定义文件列举、增删改、上传下载、跨客户端传输及任务管理能力。
- 字段：无字段（接口，`extends ShellBaseClient`，泛型 `<E extends ShellFile>`）。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `lsFile(String)` | 列举文件为列表 | 默认实现收集 `lsFileDynamic` 回调结果 |
| `lsFileDynamic(String, Consumer<E>)` | 动态列举文件 | 抽象方法，逐个回调 |
| `lsFileBatch(String, Consumer<List<E>>, int)` | 批量列举 | 默认实现按 `batchSize` 分组回调 |
| `lsFileRecursive(E, ExceptionConsumer<E>)` | 递归列举 | 文件/链接回调，目录递归 |
| `delete(E)` / `delete(String)` | 删除文件 | 默认按路径调用 `delete(String)` |
| `deleteDir(E)` / `deleteDir(String)` | 删除文件夹 | 抽象方法 |
| `deleteDirRecursive(E)` / `deleteDirRecursive(String)` | 递归删除 | 抽象方法 |
| `rename(E, String)` | 重命名 | 抽象方法 |
| `exist(String)` / `realpath(String)` / `touch(String)` | 存在性/真实路径/创建文件 | 抽象方法 |
| `createDir(String)` | 创建目录 | 抽象方法 |
| `createDirRecursive(String)` | 递归创建目录 | 默认逐级 `exist` 判断，缺失则 `createDir`，捕获 `No such file` 重试 |
| `workDir()` / `cd(String)` | 当前位置/切换目录 | 抽象方法 |
| `get(E, String)` / `get(E, String, Function<Long,Boolean>)` | 下载文件 | 委托重载，回调返回是否继续 |
| `getStream(E, Function<Long,Boolean>)` | 下载输入流 | 抽象方法 |
| `put(String,String)` / `put(File,String)` / `put(File,String,Function)` / `put(InputStream,String,Function)` | 上传文件 | 逐层委托，最终包装 `FileInputStream` |
| `putStream(String, Function<Long,Boolean>)` | 上传输出流 | 抽象方法 |
| `doDelete(E)` | 创建删除任务 | `new ShellFileDeleteTask(deleteCompetitor(), file, this)`，加入 `deleteTasks()` 并 `doDelete` |
| `deleteCompetitor()` / `deleteTasks()` | 删除竞争器/任务列表 | 抽象方法 |
| `doUpload(File, String, Consumer<Boolean>)` | 创建上传任务 | `new ShellFileUploadTask(...)`，加入 `uploadTasks()` |
| `uploadCompetitor()` / `uploadTasks()` | 上传竞争器/任务列表 | 抽象方法 |
| `doDownload(E, File)` / `doDownload(E, String)` | 创建下载任务 | `new ShellFileDownloadTask(...)`，加入 `downloadTasks()` |
| `downloadCompetitor()` / `downloadTasks()` | 下载竞争器/任务列表 | 抽象方法 |
| `doTransport(String, E, ShellFileClient<E>)` | 创建传输任务 | `new ShellFileTransportTask(...)`，加入 `transportTasks()` |
| `transportCompetitor()` / `transportTasks()` | 传输竞争器/任务列表 | 抽象方法 |
| `getTaskSize()` | 任务数量 | `uploadTasks().size() + downloadTasks().size()` |
| `isDownloadTaskEmpty()` / `isUploadTaskEmpty()` / `isDeleteTaskEmpty()` / `isTransportTaskEmpty()` | 各任务是否为空 | 对应列表 `isEmpty()` |
| `isTaskEmpty(List<ShellFileTaskType>)` | 指定类型任务是否为空 | 按 `DELETE/UPLOAD/DOWNLOAD/TANSPORT` 逐项判断 |
| `addTaskSizeListener(Runnable, List<ShellFileTaskType>)` / `removeTaskSizeListener(...)` | 任务数量监听 | 依类型向对应任务列表增删 `ListChangeListener` |
| `closeDelayResources()` | 关闭延迟资源（如文件流） | 抽象方法 |
| `chmod(int, String)` | 设置权限 | 抽象方法 |
| `chmodRecursive(int, E)` | 递归设置权限 | `chmod` 后对目录递归 |
| `fileInfo(String)` | 获取文件信息 | 抽象方法 |
| `isCdSupport()` / `isChmodSupport()` / `isRealpathSupport()` / `isWorkDirSupport()` / `isPutStreamSupport()` / `isCreateDirSupport()` / `isCreateDirRecursiveSupport()` | 能力开关 | 默认均返回 `true` |

- 调用链：`ShellFileClient.doDelete → new ShellFileDeleteTask → ShellFileDeleteTask.doDelete(...)`；`doUpload → ShellFileUploadTask`；`doDownload → ShellFileDownloadTask`；`doTransport → ShellFileTransportTask`；`createDirRecursive → exist → createDir`

---

## ShellFileDeleteTask

- 职责：单个远程文件/目录的删除任务，在独立线程中借助竞争器串行执行删除。
- 字段（父类 `ShellFileTask` 另含 `worker`、`error`、`competitor`、`status`）：

| 字段 | 类型 | 含义 |
|---|---|---|
| remoteFile | `ShellFile` | 待删除的远程文件 |
| client | `ShellFileClient` | 文件客户端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellFileDeleteTask(Competitor, ShellFile, ShellFileClient<?>)` | 构造函数 | 保存文件与客户端，状态置 `IN_PREPARATION` |
| `doDelete(Runnable, Consumer<Throwable>)` | 启动删除线程 | `ThreadUtil.start`；`competitor.tryLock(this)` 失败置 `FAILED`；`client.forkClient()` 后执行 `doDelete()`；finally `finishCallback.run()` + `competitor.release(this)` |
| `doDelete()`（私有） | 实际删除 | 状态 `EXECUTE_ING`；`remoteFile.startWaiting()`；目录走 `client.deleteDirRecursive`，否则 `client.delete`；异常置 `FAILED`，成功置 `FINISHED`；最后 `client.isForked()` 时 `IOUtil.close(client)` |
| `getFilePath()` | 文件路径 | `remoteFile.getFilePath()` |
| `getRemoteFile()` | 获取远程文件 | 返回 `remoteFile` |

- 调用链：`doDelete(cb,ecb) → ThreadUtil.start → competitor.tryLock → client.forkClient → doDelete() → deleteDirRecursive/delete → updateStatus → IOUtil.close`

---

## ShellFileDownloadTask

- 职责：文件/目录下载任务，在后台线程中逐个文件下载并实时维护进度、速度、状态等 JavaFX 属性。
- 字段（父类另含 `worker`、`error`、`competitor`、`status`）：

| 字段 | 类型 | 含义 |
|---|---|---|
| finishCallback | `Runnable` | 任务结束时的回调 |
| progressProperty | `StringProperty` | 进度属性（百分比） |
| fileCountProperty | `LongProperty` | 剩余待处理文件数 |
| speedProperty | `StringProperty` | 下载速度 |
| statusProperty | `StringProperty` | 状态显示文本 |
| fileSizeProperty | `StringProperty` | 当前大小/总大小 |
| currentFileProperty | `StringProperty` | 当前文件名 |
| totalSize | `long` | 总大小 |
| startTime | `long` | 开始时间 |
| currentSize | `long` | 当前已下载大小 |
| remoteFile | `ShellFile` | 远程文件 |
| localPath | `String` | 本地路径 |
| fileList | `List<ShellFile>` | 待下载文件列表 |
| client | `ShellFileClient` | 文件客户端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellFileDownloadTask(Competitor, ShellFile, String, ShellFileClient)` | 构造函数 | 状态置 `IN_PREPARATION` |
| `finishDownload()` | 结束下载 | `error == null` 时执行 `finishCallback`，并按需 `IOUtil.close(client)` |
| `doDownload(Runnable, Consumer<Throwable>)` | 启动下载线程 | `ThreadUtil.start`；`tryLock` 失败置 `FAILED`；`forkClient → initFile → doDownload`；异常回调；finally `release` |
| `doDownload()`（私有） | 实际下载循环 | 逐个取 `fileList.getFirst()`，计算本地路径并建父目录，`client.get(file, localFilePath, callback)`；回调中 `updateSpeed/updateProgress/updateFileSize`；失败时 `currentSize -= currSize` |
| `cancel()` | 取消 | `super.cancel()` 后 `finishDownload()` |
| `retry()` | 重试 | 清 `error`，置 `EXECUTE_ING`，新线程重跑 `doDownload()` |
| `initFile()` | 初始化文件列表 | 文件直接入列并累加 `totalSize`；目录用 `client.lsFileRecursive` 递归收集 |
| `updateSpeed()` | 计算并更新速度 | 耗时 `>=1`，`speed = currentSize / costTime * 1000` |
| `updateFileSize()` / `updateFileCount()` / `updateProgress()` | 更新大小/数量/进度 | 分别更新属性文本与百分比 |
| `speedProperty()` / `fileSizeProperty()` / `fileCountProperty()` / `progressProperty()` / `currentFileProperty()` / `statusProperty()` | 属性访问器 | 暴露 JavaFX 属性 |
| `getSrcPath()` / `getDestPath()` | 源/目标路径 | 源为 `remoteFile.getFilePath()`；目标为 `ShellFileUtil.concat(localPath, remoteFile.getFileName())` |
| `updateStatus(ShellFileStatus)` | 状态文案 | 覆写父类，按状态设置 i18n 文本（含 `error.getMessage()`） |

- 调用链：`doDownload(cb,ecb) → ThreadUtil.start → competitor.tryLock → client.forkClient → initFile → client.lsFileRecursive → doDownload() → client.get → updateSpeed/updateProgress/updateFileSize → finishDownload → competitor.release`

---

## ShellFileProgressMonitor

- 职责：提供带进度回调的输入/输出流包装，读/写过程中回调字节数，回调返回 `false` 时抛出 `InterruptedIOException` 以中断传输。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `of(InputStream, Function<Long,Boolean>)` | 包装输入流 | `FileInputStream` 走 `ShellFileInputStream2`，否则 `ShellFileInputStream` |
| `of2(FileInputStream, Function<Long,Boolean>)` | 包装文件输入流 | 返回 `ShellFileInputStream2` |
| `of(OutputStream, Function<Long,Boolean>)` | 包装输出流 | 返回 `ShellFileOutputStream` |

- 嵌套类：

| 嵌套类 | 说明 | 关键逻辑 |
|---|---|---|
| `ShellFileInputStream` | 通用输入流包装 | 覆写 `read`/`readAllBytes`/`readNBytes` 等，读后 `applyCallback(len)`；`close` 释放流与回调 |
| `ShellFileInputStream2` | 基于 `FileInputStream` 的包装 | 构造函数 `super(in.getFD())`；覆写 `read(byte[])`/`read(byte[],int,int)`/`readNBytes` |
| `ShellFileInputStream3` | 输入流包装变体 | 继承 `ShellFileInputStream2`，`applyCallback` 以 `len/2` 计（当前无静态入口调用，但类本身被保留为可用实现） |
| `ShellFileOutputStream` | 输出流包装 | 覆写 `write(byte[],int,int)` 后回调；回调 `false` 抛 `InterruptedIOException` |

- 调用链：`ShellFileProgressMonitor.of(in, cb) → new ShellFileInputStream2 / ShellFileInputStream`；`read(...) → applyCallback(len) → callback.apply(len)`（false → `InterruptedIOException`）

---

## ShellFileStatus

- 职责：枚举文件任务状态。
- 字段（枚举常量）：

| 字段 | 类型 | 含义 |
|---|---|---|
| IN_PREPARATION | `ShellFileStatus` | 预处理 |
| EXECUTE_ING | `ShellFileStatus` | 执行中 |
| FINISHED | `ShellFileStatus` | 已结束 |
| FAILED | `ShellFileStatus` | 已失败 |
| CANCELED | `ShellFileStatus` | 已取消 |

- 方法：无。

---

## ShellFileTask

- 职责：文件任务基类，统一维护状态、错误、竞争器与工作线程。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| worker | `Thread` | 工作线程 |
| error | `Throwable` | 错误 |
| competitor | `Competitor` | 竞争器（final） |
| status | `ShellFileStatus` | 状态（transient） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellFileTask(Competitor)` | 构造函数 | 保存竞争器 |
| `cancel()` | 取消任务 | 清 `error`，`competitor.release(this)`，置 `CANCELED`，`ThreadUtil.interrupt(worker)` |
| `updateStatus(ShellFileStatus)` | 更新状态 | 赋值并 debug 日志 |
| `isFailed()` / `isCanceled()` | 状态判定 | 比较 `status` |
| `getErrorMsg()` | 错误信息 | `error == null` 返回空串，否则 `error.getMessage()` |

- 调用链：`ShellFileTask.cancel() → Competitor.release → updateStatus(CANCELED) → ThreadUtil.interrupt`；子类 `ShellFileDeleteTask`/`ShellFileDownloadTask`/`ShellFileUploadTask`/`ShellFileTransportTask` 均继承本类

---

## ShellFileTaskType

- 职责：枚举文件任务类型。
- 字段（枚举常量）：

| 字段 | 类型 | 含义 |
|---|---|---|
| DELETE | `ShellFileTaskType` | 删除 |
| UPLOAD | `ShellFileTaskType` | 上传 |
| DOWNLOAD | `ShellFileTaskType` | 下载 |
| TANSPORT | `ShellFileTaskType` | 传输（源码常量名即 `TANSPORT`） |

- 方法：无。

---

## ShellFileTransportTask

- 职责：客户端到客户端的文件/目录传输任务（如两个远程会话之间传输），维护进度、速度、状态属性。
- 字段（父类另含 `worker`、`error`、`competitor`、`status`）：

| 字段 | 类型 | 含义 |
|---|---|---|
| finishCallback | `Runnable` | 任务结束时的回调 |
| progressProperty | `StringProperty` | 进度属性 |
| fileCountProperty | `LongProperty` | 剩余文件数 |
| speedProperty | `StringProperty` | 传输速度 |
| statusProperty | `StringProperty` | 状态文本 |
| fileSizeProperty | `StringProperty` | 当前大小/总大小 |
| currentFileProperty | `StringProperty` | 当前文件名 |
| totalSize | `long` | 总大小 |
| startTime | `long` | 开始时间 |
| currentSize | `long` | 当前已传输大小 |
| remotePath | `String` | 目标远程路径 |
| localFile | `ShellFile` | 源（本地）文件 |
| fileList | `List<ShellFile>` | 待传输文件列表 |
| clientName | `String` | 客户端名称 |
| localClient | `ShellFileClient` | 本地（源）客户端 |
| remoteClient | `ShellFileClient` | 远程（目标）客户端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellFileTransportTask(Competitor, String, ShellFile, ShellFileClient, ShellFileClient)` | 构造函数 | 保存源文件、目标路径与两端客户端，`clientName = localClient.connectName()`，状态置 `IN_PREPARATION` |
| `finishTransport()` | 结束传输 | `error == null` 时执行回调，并按需关闭 fork 出来的两端客户端 |
| `doTransport(Runnable, Consumer<Throwable>)` | 启动传输线程 | `tryLock`；两端 `forkClient()`；`initFile → doTransport()`；finally `release` |
| `doTransport()`（私有） | 实际传输循环 | 目录则 `remoteClient.exist`/`createDirRecursive` 建父目录；`localClient.getStream` 取输入；`remoteClient.isPutStreamSupport()` 时 `putStream + transferTo`，否则 `put`；每轮关闭流与 `closeDelayResources` |
| `cancel()` / `retry()` | 取消/重试 | `cancel` 调 `super.cancel()+finishTransport`；`retry` 新线程重跑 |
| `initFile()` | 初始化文件列表 | `remotePath` 追加 `localFile.getFileName()`；文件直接入列，目录用 `localClient.lsFileRecursive` 收集 |
| `updateSpeed()` / `updateFileSize()` / `updateFileCount()` / `updateProgress()` | 更新速度/大小/数量/进度 | 与下载任务同构 |
| `speedProperty()` / `fileSizeProperty()` / `fileCountProperty()` / `progressProperty()` / `currentFileProperty()` / `statusProperty()` | 属性访问器 | 暴露 JavaFX 属性 |
| `getSrcPath()` / `getDestPath()` / `getClientName()` | 源/目标路径与客户端名 | 源为 `localFile.getFilePath()`，目标为 `remotePath` |
| `updateStatus(ShellFileStatus)` | 状态文案 | 覆写父类，按状态设置 i18n 文本 |

- 调用链：`doTransport(cb,ecb) → ThreadUtil.start → competitor.tryLock → localClient.forkClient/remoteClient.forkClient → initFile → localClient.lsFileRecursive → doTransport() → localClient.getStream → remoteClient.putStream/put → closeDelayResources → finishTransport → release`

---

## ShellFileUploadTask

- 职责：本地文件/目录上传任务，维护进度、速度、状态属性。
- 字段（父类另含 `worker`、`error`、`competitor`、`status`）：

| 字段 | 类型 | 含义 |
|---|---|---|
| finishCallback | `Runnable` | 任务结束时的回调 |
| progressProperty | `StringProperty` | 进度属性 |
| fileCountProperty | `LongProperty` | 剩余文件数 |
| speedProperty | `StringProperty` | 上传速度 |
| statusProperty | `StringProperty` | 状态文本 |
| fileSizeProperty | `StringProperty` | 当前大小/总大小 |
| currentFileProperty | `StringProperty` | 当前文件名 |
| totalSize | `long` | 总大小 |
| startTime | `long` | 开始时间 |
| currentSize | `long` | 当前已上传大小 |
| remotePath | `String` | 远程路径 |
| localFile | `File` | 本地文件 |
| fileList | `List<File>` | 待上传文件列表 |
| client | `ShellFileClient<?>` | 文件客户端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellFileUploadTask(Competitor, File, String, ShellFileClient<?>)` | 构造函数 | 状态置 `IN_PREPARATION` |
| `finishUpload()` | 结束上传 | `error == null` 时执行回调，并按需关闭 fork 客户端 |
| `doUpload(Runnable, Consumer<Throwable>)` | 启动上传线程 | `tryLock`；`forkClient → initFile → doUpload()`；finally `release` |
| `doUpload()`（私有） | 实际上传循环 | 目录则 `client.exist`/`createDirRecursive` 建远程父目录；`client.put(file, remoteFilePath, callback)`；回调更新速度/进度/大小；失败回退 `currentSize` |
| `cancel()` / `retry()` | 取消/重试 | 同下载任务模式 |
| `initFile()` | 初始化文件列表 | `remotePath` 追加 `localFile.getName()`；文件直接入列，目录用 `FileUtil.getAllFiles` 收集 |
| `updateSpeed()` / `updateFileSize()` / `updateFileCount()` / `updateProgress()` | 更新速度/大小/数量/进度 | 与下载任务同构 |
| `speedProperty()` / `fileSizeProperty()` / `fileCountProperty()` / `progressProperty()` / `currentFileProperty()` / `statusProperty()` | 属性访问器 | 暴露 JavaFX 属性 |
| `getSrcPath()` / `getDestPath()` | 源/目标路径 | 源为 `localFile.getPath()`，目标为 `remotePath` |
| `updateStatus(ShellFileStatus)` | 状态文案 | 覆写父类，按状态设置 i18n 文本 |

- 调用链：`doUpload(cb,ecb) → ThreadUtil.start → competitor.tryLock → client.forkClient → initFile → FileUtil.getAllFiles → doUpload() → client.put → updateSpeed/updateProgress/updateFileSize → finishUpload → release`

---

## ShellFileUtil

- 职责：文件相关通用工具方法集合（路径处理、Unix 权限解析与转换、可编辑/可查看判定、文件收藏、临时文件、图标）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| FILE_COLLECT_STORE | `ShellFileCollectStore` | 文件收藏存储（static final，`ShellFileCollectStore.INSTANCE`） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isNormal(String)` | 是否正常文件名 | 非 `.`、`..` |
| `parent(String)` | 父路径 | 空返回原值；无 `/` 返回 `null`；`index==0` 返回 `/` |
| `name(String)` | 文件名 | 取最后一个 `/` 之后部分 |
| `concat(String, String)` | 拼接路径 | 统一反斜杠为 `/`，按首尾 `/` 去重拼接 |
| `hasOwnerReadPermission(String)` 等 9 个 `has*Permission(String)` | 权限位判断 | 按权限串下标取 `r/w/x` 判断 |
| `fileEditable(ShellFile)` | 是否可编辑 | 非文件返回 false；大小 > 20MB 返回 false，否则 true |
| `fileViewable(String)` | 可查看类型 | 按扩展名返回 `img`/`video`/`audio`/`txt`/`unknown` |
| `toPermissionInt(String)` | Unix 权限转数字 | 处理 9/10 位，`owner*100 + group*10 + others` |
| `calculatePermissionGroup(String)`（私有） | 计算单组权限值 | 按 `r/w/x` 累加 4/2/1 |
| `rwxToOctal(String)` | rwx 转八进制字符串 | 兼容 10/11 位，返回 `"0"+owner+group+others` |
| `octalToRwx(String)` | 八进制转 rwx | 校验 3 位、每位 0-7，返回 9 位 rwx |
| `fixFilePath(String)` | 修正路径 | 补前导 `/`，统一分隔符，压缩 `//` |
| `fixWindowsFilePath(String)` | Windows 路径处理 | 去前导 `/`，`/` 替换为 `\` |
| `fileCollect(ShellFileClient<?>)` | 获取有效收藏列表 | 遍历存储，`fileInfo` 校验目录，无效或 `No such file` 则删除 |
| `isFileCollect(ShellFileClient<?>, ShellFile)` | 是否已收藏 | `FILE_COLLECT_STORE.exist(iid, filePath)` |
| `collectFile(ShellFileClient<?>, ShellFile)` | 收藏文件 | 构建 `ShellFileCollect` 后 `replace` |
| `unCollectFile(ShellFileClient<?>, ShellFile)` | 取消收藏 | `FILE_COLLECT_STORE.delete(iid, filePath)` |
| `getTempFile(String)` | 生成临时文件路径 | `ShellConst.getCachePath() + UUIDUtil.uuidSimple() + "." + extName` |
| `getIcon(ShellFile, Object)` | 获取图标 | 返回 `file.getIcon()` |

- 调用链：`ShellFileUtil.collectFile → ShellFileCollectStore.replace`；`fileCollect → fileClient.fileInfo → ShellFileCollectStore.delete`；`getTempFile → ShellConst.getCachePath + UUIDUtil.uuidSimple`；`ShellFile.getFilePath → ShellFileUtil.concat`
