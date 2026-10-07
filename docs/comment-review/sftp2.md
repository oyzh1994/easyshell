# easyshell SFTP 客户端（sftp2 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/sftp2/，共 6 个 .java，全部存活。

本包基于 Apache MINA SSHD 的 `SftpClient` 封装 SFTP 文件访问能力，向 `ShellFileClient` 体系提供统一的文件操作接口。`ShellSFTPClient` 复用 `ShellBaseSSHClient`（ssh2 包）的 SSH 会话，通过 `ShellSFTPChannelPool` 复用 SFTP 通道，`ShellSFTPCache` 缓存 `id` 命令查询结果，`ShellSFTPUtil` 负责权限位与字符串互转。

## ShellSFTPCache

- 职责：缓存 SFTP 会话中查询到的拥有者（owner）与分组（group）名称，避免重复执行 `id` 命令。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| ownerCache | `Map<Integer, String>` | 拥有者缓存，key 为拥有者 id，value 为拥有者名称，`ConcurrentHashMap` 初始容量 4 |
| groupCache | `Map<Integer, String>` | 分组缓存，key 为分组 id，value 为分组名称，`ConcurrentHashMap` 初始容量 4 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getOwner(int uid, ShellSFTPClient client)` | 获取拥有者名称 | 缓存未命中时调用 `client.exec_id_un(uid)` 并写回 ownerCache |
| `getGroup(int gid, ShellSFTPClient client)` | 获取分组名称 | 缓存未命中时调用 `client.exec_id_gn(gid)` 并写回 groupCache |
| `close()` | 释放资源（`AutoCloseable`） | 清空 ownerCache 与 groupCache；源文件中已注释掉 pathCache/attrsCache 相关代码 |

- 调用链：`ShellSFTPClient.fileInfo → ShellSFTPCache.getOwner → ShellSFTPClient.exec_id_un → ShellBaseSSHClient.exec`

## ShellSFTPChannel

- 职责：封装单个 `SftpClient`，提供目录列举、上传下载、增删改查、权限修改等文件级操作，并缓存当前工作目录。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| channel | `SftpClient` | 底层 SFTP 客户端通道 |
| pwd | `String` | 缓存的工作目录，首次 `pwd()` 时通过 `canonicalPath(".")` 获取 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSFTPChannel(SftpClient sftpClient)` | 构造通道 | 直接持有传入的 `SftpClient` |
| `ls(String path)` | 列举目录项 | `ShellFileUtil.fixFilePath` 修正路径后调用 `channel.readEntries(path)` |
| `realpath(String path)` | 获取链接/真实路径 | 调用 `channel.canonicalPath(path)` |
| `lsFile(String path)` | 列举文件为列表 | 内部转调 `lsFile(path, files::add)` |
| `lsFile(String path, Consumer<ShellSFTPFile> fileCallback)` | 遍历列举文件 | 跳过 `.` 与 `..`，对每个 `DirEntry` 构造 `ShellSFTPFile(filePath, entry)` 并回调 |
| `rm(String path)` | 删除文件 | 调用 `channel.remove(path)` |
| `rmdir(String path)` | 删除目录 | 调用 `channel.rmdir(path)` |
| `pwd()` | 获取工作目录 | 懒加载缓存 `pwd`，来源 `channel.canonicalPath(".")` |
| `mkdir(String path)` | 创建目录 | 调用 `channel.mkdir(path)` |
| `exist(String path)` | 判断存在 | 调用 `stat(path)`，捕获 `No such file` 异常返回 `false` |
| `touch(String path)` | 创建空文件 | 以空 `ByteArrayInputStream` 调 `put(stream, path)` |
| `rename(String path, String newPath)` | 重命名 | 调用 `channel.rename(path, newPath)` |
| `stat(String path)` | 获取文件属性 | 调用 `channel.stat(path)`，`No such file` 时返回 `null` |
| `put(String src, String dest)` | 按本地路径上传 | `channel.put(Path.of(src), dest)` |
| `put(InputStream src, String dest)` | 按流上传 | `channel.put(src, dest)` 后 `IOUtil.close(src)` |
| `write(String dest)` | 获取写入流 | 调用 `channel.write(dest)` |
| `get(String src)` | 获取下载流 | 调用 `channel.read(src)` |
| `get(String src, String dest)` | 下载到本地文件 | `channel.read` → `IOUtil.saveToFile` → `IOUtil.close(stream)` |
| `get(String src, OutputStream dest)` | 下载到输出流 | `channel.read` → `IOUtil.saveToStream` → 关闭入/出流 |
| `chmod(int permission, String path)` | 修改权限（Windows 无效） | `ShellFileUtil.octalToRwx` → `ShellSFTPUtil.parsePermissions1` → `attrs.setPermissions` → `channel.setStat` |
| `close()` | 关闭通道 | `IOUtil.close(this.channel)` 并置空 |
| `isClosed()` | 是否已关闭 | 返回 `!this.channel.isOpen()` |

- 调用链：`ShellSFTPClient.get → ShellSFTPChannel.get → ShellSFTPChannelPool.borrowObject → ShellSFTPClient.newSFTPChannel`

## ShellSFTPChannelPool

- 职责：基于通用 `Pool` 的 SFTP 通道池，最小 1 个、最大 5 个通道，借用不到时等待获取。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| client | `ShellSFTPClient` | 所属 SFTP 客户端，用于创建新通道 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSFTPChannelPool(ShellSFTPClient client)` | 构造通道池 | `super(1, 5)` 设置最小/最大容量，`setWaitingBorrow(true)` 开启等待借用 |
| `returnObject(ShellSFTPChannel channel)` | 归还通道 | 通道为空或已关闭时直接丢弃；池已满时关闭通道；否则调用 `super.returnObject` |
| `newObject()` | 创建新通道 | 调用 `client.newSFTPChannel()` |
| `close()` | 关闭池 | 关闭 `list()` 中所有通道，`super.clear()`，置空 client |

- 调用链：`ShellSFTPClient.takeChannel → ShellSFTPChannelPool.borrowObject(继承 Pool) → ShellSFTPChannelPool.newObject → ShellSFTPClient.newSFTPChannel`

## ShellSFTPClient

- 职责：SFTP 客户端主体，实现 `ShellFileClient<ShellSFTPFile>`，统一对外提供文件操作、任务与竞争器（`Competitor`）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| cache | `ShellSFTPCache` | 缓存管理器 |
| channelPool | `ShellSFTPChannelPool` | SFTP 通道池 |
| delayChannels | `final List<ShellSFTPChannel>` | 延迟关闭的文件通道（流式上传/下载使用） |
| deleteCompetitor | `final Competitor` | 删除竞争器，并发数 5 |
| deleteTasks | `final ObservableList<ShellFileDeleteTask>` | 删除任务列表 |
| uploadCompetitor | `final Competitor` | 上传竞争器，并发数 2 |
| uploadTasks | `final ObservableList<ShellFileUploadTask>` | 上传任务列表 |
| downloadCompetitor | `final Competitor` | 下载竞争器，并发数 2 |
| downloadTasks | `final ObservableList<ShellFileDownloadTask>` | 下载任务列表 |
| transportCompetitor | `final Competitor` | 传输竞争器，并发数 1 |
| transportTasks | `final ObservableList<ShellFileTransportTask>` | 传输任务列表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSFTPClient(ShellConnect shellConnect)` | 构造（外部会话） | 转调三参构造，sshClient/session 为 null |
| `ShellSFTPClient(ShellConnect, ShellSSHJGitClient, ClientSession)` | 构造 | 初始化 `ShellSFTPCache`、`ShellSFTPChannelPool`，注册 `stateListener` |
| `close()` | 关闭客户端 | 依次关闭 channelPool、cache、父类，`closeDelayResources`，置状态 CLOSED 并清空任务列表 |
| `start(int timeout)` | 启动连接 | 已连接/连接中则返回；初始化客户端并获取会话；成功置 CONNECTED 并 `ShellClientChecker.push`，失败置 FAILED；finally 触发 `SystemUtil.gc()` |
| `initClient(int timeout)` | 初始化客户端 | 调 `super.initClient` 后设置 `SFTP_CHANNEL_OPEN_TIMEOUT` |
| `newSFTPChannel()` | 创建通道 | 转调 `newSFTPChannel(3)` |
| `newSFTPChannel(int maxRetry)` | 创建通道（带重试） | `takeSession` → `SftpClientFactory.createSftpClient` → `setNameDecodingCharset`；遇 `sshClient is null` 且未超重试则 sleep 50ms 递归重试 |
| `takeChannel()` | 借用通道 | `channelPool.borrowObject()` |
| `returnChannel(ShellSFTPChannel)` | 归还通道 | `channelPool.returnObject(channel)` |
| `exec_id_un(int uid)` | 获取用户名 | 执行 `id -un <uid>` |
| `exec_id_gn(int gid)` | 获取分组名 | 执行 `id -gn <gid>` |
| `delete(String file)` | 删除文件 | 取通道 → `channel.rm` → 归还 |
| `deleteDir(String dir)` | 删除目录 | 取通道 → `channel.rmdir` → 归还 |
| `deleteDirRecursive(String dir)` | 递归删除目录 | 列举目录项，子目录递归、文件调 `delete`，最后 `deleteDir` |
| `createDir(String filePath)` | 创建目录 | 取通道 → `channel.mkdir`，返回 true |
| `workDir()` | 获取工作目录 | 取通道 → `channel.pwd` |
| `cd(String filePath)` | 切换目录 | 抛 `UnsupportedOperationException` |
| `stat(String filePath)` | 获取文件属性 | 取通道 → `channel.stat` |
| `exist(String filePath)` | 判断存在 | 取通道 → `channel.exist` |
| `realpath(String filePath)` | 获取真实路径 | 取通道 → `channel.realpath` |
| `touch(String filePath)` | 创建文件 | 取通道 → `channel.touch` |
| `put(File, String, Function)` | 上传本地文件 | 新建通道；无回调按路径 `channel.put`，有回调经 `ShellFileProgressMonitor.of` 包装输入流 |
| `put(InputStream, String, Function)` | 上传流 | 新建通道；回调逻辑同上 |
| `putStream(String, Function)` | 获取上传输出流 | 新建通道并加入 `delayChannels`，可选 `ShellFileProgressMonitor` 包装 |
| `get(ShellSFTPFile, String, Function)` | 下载到本地 | 新建通道；无回调 `channel.get(path, local)`，有回调经进度监控包装输出流 |
| `getStream(ShellSFTPFile, Function)` | 获取下载流 | 新建通道并加入 `delayChannels`，可选进度监控包装 |
| `chmod(int, String)` | 修改权限 | 取通道 → `channel.chmod`，返回 true |
| `chmodRecursive(int, ShellSFTPFile)` | 递归改权限 | 先执行 `chmod N -R <path>`，输出为空视为成功，否则回退 `ShellFileClient.super.chmodRecursive` |
| `fileInfo(String filePath)` | 文件信息 | `stat` 获取属性；owner/group 为空时经 `cache.getOwner/getGroup` 补全；构造 `ShellSFTPFile(parent, name, attrs)` |
| `lsFileDynamic(String, Consumer)` | 动态列举文件 | 取通道 → `channel.lsFile(path, callback)` |
| `rename(ShellSFTPFile, String)` | 重命名 | 拼接新路径 → 取通道 → `channel.rename` |
| `deleteCompetitor()` / `uploadCompetitor()` / `downloadCompetitor()` / `transportCompetitor()` | 获取各竞争器 | 返回对应 `Competitor` 字段 |
| `deleteTasks()` / `uploadTasks()` / `downloadTasks()` / `transportTasks()` | 获取各任务列表 | 返回对应 `ObservableList` 字段 |
| `closeDelayResources()` | 关闭延迟资源 | 关闭并清空 `delayChannels` |
| `isCdSupport()` | 是否支持 cd | 返回 `false` |

- 调用链：`ShellSFTPClient.lsFileDynamic → ShellSFTPChannel.ls → SftpClient.readEntries`；`ShellSFTPClient.delete → takeChannel → ShellSFTPChannel.rm → returnChannel`

## ShellSFTPFile

- 职责：SFTP 文件模型，实现 `ShellFile`，兼容「目录项 + 属性」与「属性」两种来源，支持链接属性与权限字符串展示。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| entry | `SftpClient.DirEntry` | 目录项（列举场景） |
| attrs | `SftpClient.Attributes` | 文件属性（fileInfo 场景） |
| owner | `String` | 拥有者名称 |
| group | `String` | 分组名称 |
| fileName | `String` | 文件名 |
| parentPath | `String` | 父路径 |
| linkAttrs | `SftpClient.Attributes` | 链接目标属性 |
| permissionsProperty | `StringProperty` | 权限字符串属性（懒加载） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSFTPFile(String parentPath, SftpClient.DirEntry entry)` | 构造（目录项） | 从 `entry.getLongFilename()` 按空白分割取 owner/group，并 `updatePermissions` |
| `ShellSFTPFile(String parentPath, String fileName, SftpClient.Attributes attrs)` | 构造（属性） | 持有 attrs 并 `updatePermissions` |
| `getEntry()` / `setEntry(...)` | 目录项读写 | 返回/设置 entry |
| `getAttrs()` | 获取属性 | attrs 为空时回退 `entry.getAttributes()` |
| `getLinkAttrs()` / `setLinkAttrs(...)` | 链接属性读写 | 返回/设置 linkAttrs |
| `getOwner()` | 获取拥有者 | owner 为空时回退 `getAttrs().getOwner()` |
| `getGroup()` | 获取分组 | group 为空时回退 `getAttrs().getGroup()` |
| `getFileSize()` / `setFileSize(long)` | 文件大小 | 读写 `getAttrs().getSize()` |
| `getFileName()` | 获取文件名 | fileName 为空时回退 `entry.getFilename()` |
| `setFileName(String)` | 设置文件名 | 设置 fileName |
| `getParentPath()` / `setParentPath(...)` | 父路径读写 | 返回/设置 parentPath |
| `getFilePath()` | 获取完整路径 | 文件名以 `/` 开头直接返回，否则 `ShellFileUtil.concat(parentPath, fileName)` |
| `permissionsProperty()` | 权限属性 | 懒加载 `SimpleStringProperty` |
| `updatePermissions()` | 更新权限 | `ShellSFTPUtil.formatPermissions(getAttrs())` 写入权限属性 |
| `getPermissions()` | 获取权限 | `.`/`..` 返回空串，否则返回权限属性值 |
| `setPermissions(String)` | 设置权限 | 前置 `ShellSFTPUtil.getFileType` 后写入属性 |
| `getAddTime()` | 获取访问时间 | 取 `getAttrs().getAccessTime()` 格式化为日期时间 |
| `getModifyTime()` | 获取修改时间 | 取 `getAttrs().getModifyTime()` 格式化 |
| `getMTime()` | 获取修改时间戳 | `getAttrs().getModifyTime().toMillis()` |
| `setModifyTime(String)` | 设置修改时间 | 解析字符串为 `FileTime` 后写入 attrs |
| `getUid()` / `getGid()` | 获取拥有者/分组 id | `getAttrs().getUserId()/getGroupId()` |
| `isFile()` | 是否普通文件 | 链接文件且 linkAttrs 非空时看 linkAttrs，否则看 attrs |
| `isDirectory()` | 是否目录 | 链接文件且 linkAttrs 非空时看 linkAttrs，否则看 attrs |
| `isLink()` | 是否符号链接 | `getAttrs().isSymbolicLink()` |
| `copy(ShellFile)` | 复制字段 | 目标为 `ShellSFTPFile` 时逐字段拷贝并 `updatePermissions` |
| `destroy()` | 销毁 | 解绑并置空权限属性，清空各字段 |

- 调用链：`ShellSFTPFile.updatePermissions → ShellSFTPUtil.formatPermissions → ShellSFTPUtil.getFileType`

## ShellSFTPUtil

- 职责：SFTP 工具类，提供链接（realpath）解析与权限位/字符串互转。
- 字段：无字段（纯静态方法工具类）
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `realpath(ShellSFTPFile file, ShellSFTPClient client)` | 解析链接路径（客户端版） | 文件为链接且正常时调 `client.realpath`，命中则 `client.stat(linkPath)` 写入 linkAttrs；`No such file` 仅告警 |
| `realpath(ShellSFTPFile file, ShellSFTPChannel channel)` | 解析链接路径（通道版） | 逻辑同上，改用 `channel.realpath` / `channel.stat` |
| `formatPermissions(SftpClient.Attributes attrs)` | 属性转 10 位权限串 | 拼接文件类型位 + rwx，并处理 SUID/SGID/Sticky（s/S、t/T） |
| `parsePermissions1(String permissionString)` | 9 位权限串转整数位 | 逐位解析 r/w/x 与 s/S、t/T，合成八进制权限位 |
| `getFileType(SftpClient.Attributes attrs)` | 获取文件类型字符 | 目录 `d`、符号链接 `l`、普通文件 `-`，否则 `?` |

- 调用链：`ShellSFTPChannel.chmod → ShellSFTPUtil.parsePermissions1 → SftpClient.Attributes.setPermissions`
