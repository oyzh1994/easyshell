# easyshell SMB 客户端（smb 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/smb/，共 6 个 .java，全部存活。

本包基于 smbj 库实现 SMB 协议的文件客户端，涵盖连接管理、目录/文件读写、上传下载、代理 socket 工厂，以及为支持国密/兼容老服务端而自行实现的 AES-CMAC 算法与安全提供者。

---

## ShellSMBAesCmac

- 职责：`com.hierynomus.security.Mac` 的 AES-CMAC 实现，累积数据缓冲后一次计算出 CMAC。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| key | `byte[]` | 密钥 |
| buffer | `byte[]` | 数据缓冲区，初始为空数组 |
| initialized | `boolean` | 是否已初始化 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `init(byte[] key)` | 初始化 | `Arrays.copyOf` 复制密钥，清空 buffer，置 `initialized=true` |
| `update(byte b)` | 追加单字节 | 转调 `update(new byte[]{b}, 0, 1)` |
| `update(byte[] data)` | 追加数据 | 转调 `update(data, 0, data.length)` |
| `update(byte[] data, int offset, int length)` | 追加数据段 | 未初始化抛 `IllegalStateException`；扩容新数组并 `System.arraycopy` |
| `doFinal()` | 计算最终值 | 未初始化抛异常；`ShellSMBUtil.calculateAesCmac(key, buffer)`，成功后 `reset()`，`GeneralSecurityException` 包装为 `RuntimeException` |
| `reset()` | 重置 | buffer 置为空数组 |

- 调用链：`doFinal → ShellSMBUtil.calculateAesCmac → generateSubKey/leftShiftOneBit/xor`

## ShellSMBClient

- 职责：SMB 协议文件客户端，实现 `ShellFileClient<ShellSMBFile>`，封装连接、会话、共享及目录/文件操作。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| smbShare | `DiskShare` | smb共享 |
| smbSession | `Session` | smb会话 |
| smbConn | `Connection` | smb连接 |
| smbClient | `SMBClient` | smb客户端 |
| connect | `ShellConnect` | 连接配置（final） |
| delayFiles | `List<File>` | 延迟处理的文件（final） |
| state | `SimpleObjectProperty<ShellConnState>` | 连接状态（final，初值 `NOT_INITIALIZED`） |
| stateListener | `ChangeListener<ShellConnState>` | 当前状态监听器（final） |
| deleteCompetitor | `Competitor` | 删除竞争器，并发度 5（final） |
| deleteTasks | `ObservableList<ShellFileDeleteTask>` | 删除任务列表（final） |
| uploadTasks | `ObservableList<ShellFileUploadTask>` | 上传任务列表（final） |
| downloadTasks | `ObservableList<ShellFileDownloadTask>` | 下载任务列表（final） |
| transportTasks | `ObservableList<ShellFileTransportTask>` | 传输任务列表（final） |
| uploadCompetitor | `Competitor` | 上传竞争器，并发度 2（final） |
| downloadCompetitor | `Competitor` | 下载竞争器，并发度 2（final） |
| transportCompetitor | `Competitor` | 传输竞争器，并发度 2（final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSMBClient(ShellConnect connect)` | 构造客户端 | 保存 connect，设置状态并 `addStateListener(stateListener)` |
| `initClient()` | 初始化客户端 | `SmbConfig.builder()` 设置超时；`isEnableProxy()` 时 `withSocketFactory(new ShellSMBSocketFactory(...))`；构建 `SMBClient` |
| `start(int timeout)` | 启动连接 | 连接主机端口，按 `isGuest()`/`isAnonymous()`/常规构造 `AuthenticationContext`，`authenticate` → `connectShare` → `lsFile("/")` 探测，置 `CONNECTED`，`ShellClientChecker.push(this)`；失败置 `FAILED`；`finally` 中 `SystemUtil.gc()` |
| `isGuest()` | 是否访客用户 | `"guest".equalsIgnoreCase(user)` |
| `isAnonymous()` | 是否匿名用户 | `"anonymous".equalsIgnoreCase(user)` |
| `getShellConnect()` | 获取连接 | 返回 connect |
| `isConnected()` | 是否已连接 | `smbConn != null && smbConn.isConnected()` |
| `stateProperty()` | 状态属性 | 返回 state |
| `close()` | 关闭 | 依次关闭 share/session/conn/client，`closeDelayResources()`，置 `CLOSED` 并移除监听器 |
| `lsFileDynamic(String filePath, Consumer<ShellSMBFile> fileCallback)` | 动态列举 | `smbShare.list(filePath)`，过滤非普通文件后 `new ShellSMBFile(filePath, information)` 回调 |
| `delete(String file)` | 删除文件 | `smbShare.rm(file)` |
| `deleteDir(String dir)` | 删除目录 | `smbShare.rmdir(dir, false)` |
| `deleteDirRecursive(String dir)` | 递归删除目录 | `smbShare.rmdir(dir, true)` |
| `rename(ShellSMBFile file, String newName)` | 重命名 | 目录走 `openDir` + `rename`，文件走 `openFile` + `rename`，均 `IOUtil.close` |
| `exist(String filePath)` | 是否存在 | 先 `fileExists` 再 `folderExists`，均吞异常 |
| `realpath(String filePath)` | 绝对路径 | 抛 `UnsupportedOperationException` |
| `touch(String filePath)` | 创建空文件 | `openFile`（`FILE_OVERWRITE_IF`）后 `write(new byte[]{}, 0)` |
| `createDir(String filePath)` | 创建目录 | `smbShare.mkdir(filePath)` |
| `workDir()` | 工作目录 | 抛 `UnsupportedOperationException` |
| `cd(String filePath)` | 切换目录 | 抛 `UnsupportedOperationException` |
| `get(ShellSMBFile remoteFile, String localFile, Function<Long,Boolean> callback)` | 下载到本地 | `readFile` 打开 → `FileOutputStream`，带回调时包 `ShellFileProgressMonitor` → `IOUtil.saveToStream` |
| `getStream(ShellSMBFile remoteFile, Function<Long,Boolean> callback)` | 获取输入流 | `readFile` → 加入 `delayFiles` → `getInputStream`，回调时包 `ShellFileProgressMonitor` |
| `put(InputStream localFile, String remoteFile, Function<Long,Boolean> callback)` | 上传 | `writeFile` → `getOutputStream` → `IOUtil.saveToStream` → 关闭流与文件 |
| `putStream(String remoteFile, Function<Long,Boolean> callback)` | 获取输出流 | `writeFile` → 加入 `delayFiles` → `getOutputStream` |
| `deleteCompetitor()` | 删除竞争器 | 返回 deleteCompetitor |
| `deleteTasks()` | 删除任务列表 | 返回 deleteTasks |
| `uploadCompetitor()` | 上传竞争器 | 返回 uploadCompetitor |
| `uploadTasks()` | 上传任务列表 | 返回 uploadTasks |
| `downloadCompetitor()` | 下载竞争器 | 返回 downloadCompetitor |
| `downloadTasks()` | 下载任务列表 | 返回 downloadTasks |
| `transportCompetitor()` | 传输竞争器 | 返回 transportCompetitor |
| `transportTasks()` | 传输任务列表 | 返回 transportTasks |
| `closeDelayResources()` | 关闭延迟资源 | 遍历关闭 `delayFiles` 并清空 |
| `chmod(int permissions, String filePath)` | 权限变更 | 抛 `UnsupportedOperationException` |
| `fileInfo(String filePath)` | 文件信息 | `smbShare.getFileInformation(filePath)` → `new ShellSMBFile(filePath, allInformation)` |
| `isCdSupport()` | 是否支持cd | false |
| `isChmodSupport()` | 是否支持chmod | false |
| `isRealpathSupport()` | 是否支持realpath | false |
| `isWorkDirSupport()` | 是否支持workDir | false |
| `readFile(String filePath)` | 私有：读取文件 | `openFile`，`GENERIC_READ`，`FILE_OPEN_IF` |
| `writeFile(String filePath)` | 私有：写入文件 | `openFile`，`GENERIC_WRITE`，`FILE_OVERWRITE_IF` |
| `openFile(String filePath)` | 私有：打开文件 | `openFile`，`GENERIC_ALL`，`FILE_OPEN_IF` |
| `openDir(String filePath)` | 私有：打开目录 | `openDirectory`，`GENERIC_ALL`，`FILE_OPEN_IF` |

- 调用链：`start → initClient → ShellSMBSocketFactory / SMBClient.connect → authenticate → connectShare → lsFileDynamic`；`get → readFile → ShellFileProgressMonitor → IOUtil.saveToStream`；`put → writeFile → IOUtil.saveToStream`

## ShellSMBFile

- 职责：SMB 文件模型，实现 `ShellFile`，统一适配 `FileAllInformation` 与 `FileIdBothDirectoryInformation` 两种 smbj 信息对象。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| fileName | `String` | 文件名称 |
| parentPath | `String` | 父路径 |
| allInformation | `FileAllInformation` | 文件属性（详情） |
| information | `FileIdBothDirectoryInformation` | 文件属性（目录列举） |
| lastModified | `Date` | 最后修改时间 |
| fileSize | `Long` | 文件大小 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSMBFile(String parentPath, FileIdBothDirectoryInformation information)` | 构造（目录列举） | 保存 information 与 parentPath |
| `ShellSMBFile(String filePath, FileAllInformation information)` | 构造（详情） | 保存 allInformation，`ShellFileUtil.name/parent` 推导名与父路径 |
| `attrs()` | 私有：文件属性位 | allInformation 优先，否则取 information |
| `isFile()` | 是否文件 | `!isDirectory() && !isLink()` |
| `isLink()` | 是否链接 | 按 `FILE_ATTRIBUTE_REPARSE_POINT` 与 EA size 掩码 `0xA0000000L` 判断 |
| `getOwner()` | 属主 | 返回 `"-"` |
| `getGroup()` | 属组 | 返回 `"-"` |
| `getFileSize()` | 文件大小 | fileSize 优先，否则 `getAllocationSize()` |
| `setFileSize(long fileSize)` | 设置大小 | 赋值 fileSize |
| `getFileName()` | 文件名 | fileName 优先，否则 `getNameInformation()`/`getFileName()` |
| `setFileName(String fileName)` | 设置文件名 | 赋值 |
| `isDirectory()` | 是否目录 | 按 `FILE_ATTRIBUTE_DIRECTORY` 判断 |
| `getParentPath()` | 父路径 | 返回 parentPath |
| `getPermissions()` | 权限 | 返回 `"-"` |
| `setPermissions(String permissions)` | 设置权限 | 空实现 |
| `getModifyTime()` | 修改时间 | lastModified 优先，否则取 `getChangeTime()`，`DateHelper.formatDateTime` |
| `setModifyTime(String modifyTime)` | 设置修改时间 | `DateHelper.parseDateTime` |
| `getAddTime()` | 添加时间 | 取 `getCreationTime()`，`DateHelper.formatDateTime` |
| `copy(ShellFile t1)` | 拷贝 | 从 `ShellSMBFile` 复制 fileSize/lastModified/fileName/parentPath |
| `destroy()` | 销毁 | 各字段置 null |

- 调用链：`ShellSMBClient.lsFileDynamic → new ShellSMBFile(path, FileIdBothDirectoryInformation)`；`ShellSMBClient.fileInfo → new ShellSMBFile(path, FileAllInformation)`；`isFile/isLink/isDirectory → attrs`

## ShellSMBSecurityProvider

- 职责：SMB 安全提供者，继承 smbj 的 `JceSecurityProvider`，对 `AesCmac` 名字返回自实现。

- 字段：无字段

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getMac(String name)` | 获取 Mac 实现 | `"AesCmac".equalsIgnoreCase(name)` 时返回 `new ShellSMBAesCmac()`，否则 `super.getMac(name)` |

- 调用链：`getMac → ShellSMBAesCmac`

## ShellSMBSocketFactory

- 职责：smb socket 工厂，继承 smbj 的 `ProxySocketFactory`，根据代理配置创建直连或代理 socket。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| connectTimeout | `int` | 连接超时时间（final） |
| proxyConfig | `ShellProxyConfig` | 代理配置（final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSMBSocketFactory()` | 构造 | 转调 `this(null, DEFAULT_CONNECT_TIMEOUT)` |
| `ShellSMBSocketFactory(ShellProxyConfig proxyConfig, int connectTimeout)` | 构造 | 保存 proxyConfig 与 connectTimeout |
| `createSocket(String address, int port)` | 创建 socket | `createSocket(new InetSocketAddress(address, port), null)` |
| `createSocket(String address, int port, InetAddress localAddress, int localPort)` | 创建 socket | 携带本地绑定地址 |
| `createSocket(InetAddress address, int port)` | 创建 socket | 转调私有方法 |
| `createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)` | 创建 socket | 携带本地绑定地址 |
| `createSocket(InetSocketAddress address, InetSocketAddress bindAddress)` | 私有：创建 socket | 需要代理时 `ShellProxyUtil.createSocket(...)` 并按需 `bind`；否则 `new Socket()` 直连 `connect(address, connectTimeout)`；`JulLog.debug` 记录 |

- 调用链：`ShellSMBClient.initClient → ShellSMBSocketFactory.createSocket → ShellProxyUtil.createSocket / Socket.connect`

## ShellSMBUtil

- 职责：SMB 工具类，提供 AES-CMAC 算法核心计算及子密钥派生。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| RB | `byte[]` | AES-CMAC 子密钥生成常量（static final，末字节 0x87） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `calculateAesCmac(byte[] key, byte[] message)` | 计算AES-CMAC值 | 校验密钥长度(16/24/32)；`AES/ECB/NoPadding`；`L = AES(0^16)` 派生 k1/k2；分块迭代 `xor` + `doFinal`，末块按完整/填充处理 |
| `generateSubKey(byte[] key)` | 生成子密钥 | `leftShiftOneBit` 后若最高位进位则与 RB 异或 |
| `leftShiftOneBit(byte[] input)` | 左移一位 | 逐字节带进位左移 |
| `xor(byte[] a, byte[] b)` | 按位异或 | 16 字节逐字节异或 |

- 调用链：`ShellSMBAesCmac.doFinal → calculateAesCmac → generateSubKey → leftShiftOneBit → xor`
