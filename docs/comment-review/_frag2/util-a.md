# 二、util 包代码审查（上）：根 / dameng / db / mongo

> 说明：本片段覆盖 `cn.oyzh.easyshell.util`（根）、`util.dameng`、`util.db`、`util.mongo` 四个目录下的正式类。术语保持中文，类名/方法名/字段名保留原样。

## 2.1 根工具（cn.oyzh.easyshell.util）

## ShellClientUtil
- 职责：客户端工具类，根据连接类型创建对应的 `ShellBaseClient` 实例。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工厂类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static <T extends ShellBaseClient> T newClient(ShellConnect connect)` | 按连接类型 new 出客户端 | 依次判断 `isSSHType/isSFTPType/isFTPType/isSerialType/isTelnetType/isRloginType/isVNCType/isLocalType/isS3Type/isSMBType/isRDPType/isRedisType/isZKType/isWebdavType/isMysqlType/isMongoType/isMoshType/isDamengType`，分别 new `ShellSSHClient`、`ShellSFTPClient`、`ShellFTPClient`、`ShellSerialClient`、`ShellTelnetClient`、`ShellRLoginClient`、`ShellVNCClient`、`ShellLocalClient`、`ShellS3Client`、`ShellSMBClient`、`ShellRDPClient`、`ShellRedisClient`、`ShellZKClient`、`ShellWebdavClient`、`ShellMysqlClient`、`ShellMongoClient`、`ShellMoshClient`、`ShellDamengClient`；未匹配返回 null，结果强转为 `T` |
- 调用链：`newClient → (分支) new ShellSSHClient/ShellDamengClient/…`

## ShellConnectUtil
- 职责：shell 连接相关工具，负责测试连接与初始化终端背景图。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static void testConnect(StageAdapter adapter, ShellConnect shellConnect)` | 测试连接（默认超时） | 委托 `testConnect(adapter, shellConnect, null)` |
  | `static void testConnect(StageAdapter adapter, ShellConnect shellConnect, Integer timeout)` | 弹出遮罩异步测试连接 | 超时默认 15s；`StageManager.showMask` 中：RDP 且非内置走 `NetworkUtil.reachable` 判断，否则 `ShellClientUtil.newClient` 后 `start(timeout)` 判断 `isConnected`，成功 `MessageBox.okToast`、失败 `MessageBox.warn`，最后 `close()`；异常走 `MessageBox.exception` |
  | `static void initTermBackground(FXTerminalPanel terminalPanel)` | 初始化终端背景图 | 从 `ShellSettingStore.SETTING` 取 `termBackgroundImageUrl`，空/失效则返回；`terminalPanel.getFirstChild().setOpacity(0.7)`，用 `Image`+`BackgroundImage`+`BackgroundSize` 构造 `Background` 并 `setBackground` |
- 调用链：`testConnect → StageManager.showMask → ShellClientUtil.newClient → ShellBaseClient.start/isConnected/close`
- 备注：类中大量按协议分支的注释代码（`close`、`initBackground` 等）已废弃，未使用。

## ShellI18nHelper
- 职责：国际化文案获取帮助类，集中封装 `I18nResourceBundle.i18nString` 调用。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态方法集 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static String fileTip1()…fileTip21()` | 文件相关提示文案 | 返回 `I18nResourceBundle.i18nString("shell.file.tipN")`（tip10/14 缺失） |
  | `static String connectTip5()/connectTip6()/connectTip7()` | 连接相关提示 | `shell.connect.tipN` |
  | `static String keyTip1()` | 密钥提示 | `shell.key.tip1` |
  | `static String termTip1()…termTip8()/termTip15()` | 终端提示 | `shell.term.tipN` |
  | `static String x11Tip1()` | X11 提示 | `shell.x11.tip1` |
  | `static String redisKeyTip9()/redisTtlTip1()/redisAddTip1..4()/redisBatchTip1..6()/redisMoveTip1..2()/redisCopyTip1()/redisPubsubTip1()` | Redis 相关提示 | 对应 `shell.redis.*` key |
  | `static String zkNodeTip1()…zkNodeTip7()/zkAclC()` | ZK 相关提示 | `shell.zk.*` |
  | `static String rdpTip2()/rdpTip3()` | RDP 提示 | `shell.rdp.tipN` |
  | `static String settingTip1()` | 设置提示 | `shell.setting.tip1` |
  | `static String sshTip1()/sshTip2()` | SSH 提示 | `shell.ssh.tipN` |
  | `static String welcome()` | 首页欢迎语 | `shell.home.welcome` |
- 调用链：`ShellI18nHelper.xxx → I18nResourceBundle.i18nString(key)`

## ShellKeyUtil
- 职责：SSH 密钥工具类，把公钥追加到远程主机的 authorized_keys 文件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static boolean sshCopyId(List<ShellKey> keys, ShellSSHClient client)` | 复制公钥到远程 | `client.sftpClient()`；Windows 下目标为 `<userHome>.ssh/` 目录下的 `authorized_keys` 及 `C:\ProgramData\ssh\administrators_authorized_keys`（目录不存在则 `createDirRecursive`），非 Windows 为 `<userHome>.ssh/authorized_keys`；遍历 key，对每个目标文件 `client.sshExec().append(pubKey, sshFile)`；异常打印返回 false |
- 调用链：`sshCopyId → ShellSSHClient.sftpClient → ShellSFTPClient.exist/createDirRecursive`；`sshCopyId → ShellSSHClient.sshExec().append`
- 备注：整类除 `sshCopyId` 外的历史实现（`generateKeyFile`、`getPrivateKeyFromBase64`、`rsa`、`ed25519` 等）均为大段注释，未启用。

## ShellProcessUtil
- 职责：进程/应用工具类，提供重启应用能力。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static void restartApplication()` | 重启应用 | `ProcessUtil.restartApplication2(100, StageManager::exit)`，`IOException` 打印 |
- 调用链：`restartApplication → ProcessUtil.restartApplication2 → StageManager.exit`

## ShellProxyUtil
- 职责：代理工具类，构造 Java/AWS 代理对象、创建代理 Socket、判断是否需代理。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static Proxy initProxy1(ShellProxyConfig proxyConfig)` | 构造 `java.net.Proxy` | 配置为 null 返回 `Proxy.NO_PROXY`；HTTP 建 `Proxy.Type.HTTP`，SOCKS 建 `Proxy.Type.SOCKS`，地址 `InetSocketAddress(host, port)` |
  | `static ProxyConfiguration initProxy2(ShellProxyConfig proxyConfig)` | 构造 AWS `ProxyConfiguration` | 配置为 null 返回空 builder；按 HTTP/SOCKS 拼 scheme，`endpoint(URI.create(scheme))`，用户名/密码空则 null |
  | `static Socket createSocket(ShellProxyConfig proxyConfig, String targetHost, int targetPort, int socketTimeout)` | 创建带代理的 socket | new `Socket`，`setKeepAlive(true)`，`initProxy1` 后 `connect(proxy.address(), timeout)`，`setTcpNoDelay(true)`；SOCKS 走 `ProxyUtil.socks5Handshake` |
  | `static boolean isNeedProxy(ShellProxyConfig proxyConfig)` | 是否需要代理 | 配置为 null 返回 false，否则返回 `!proxyConfig.isNoneProxy()` |
- 调用链：`createSocket → initProxy1 → ProxyUtil.socks5Handshake`
- 备注：`initProxy3`（`ProxyHandler`）为注释代码，未启用。

## ShellUtil
- 职责：通用 shell 辅助工具，处理命令输出解析、字符集推断、连接转换。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static boolean isCommandNotFound(String output)` | 命令是否未找到 | `StringUtil.containsAnyIgnoreCase(output, "not found", "未找到命令", "不是内部或外部")` |
  | `static boolean isWindowsCommandNotFound(String output, String cmd)` | Windows 命令是否未找到 | `StringUtil.containsIgnoreCase(output, "'" + cmd + "'")` |
  | `static String getWindowsCommandResult(String output)` | 取 Windows 命令结果 | 空返回 ""，按 `\n` 分割，行数 <2 返回 ""，否则返回第 2 行 trim |
  | `static List<String> splitWindowsCommandResult(String output)` | 按逗号切分结果（支持引号） | 空返回空列表；逐字符扫描，`"` 翻转 inQuotes，非引号内 `,` 处切分 |
  | `static String getCharsetFromChcp(String chcp)` | 由 chcp 推断字符集 | 含 437→iso-8859-1，936→gbk，950→big5，65001→utf-8，默认 gbk |
  | `static String getCharsetFromLang(String lang)` | 由 LANG 推断字符集 | 含 `.` 则取最后一个 `.` 之后；否则原样返回 |
  | `static SSHConnect toSSHConnect(ShellConnect connect)` | 转 SSH 连接 | new `SSHConnect`，`setHost(connect.hostIp())`、`setPort(connect.hostPort())` |
- 调用链：`toSSHConnect → ShellConnect.hostIp/hostPort → SSHConnect`
- 备注：`fixWindowsFilePath`、`reverseWindowsFilePath`、`permission` 为注释代码，未启用。

## ShellViewFactory
- 职责：shell 页面工厂，集中以 `StageManager`/`PopupManager` 解析并展示各类控制器页面（新增/修改连接、文件、Docker、S3、密钥、隧道、跳板机等）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工厂类，无字段 |
- 方法（按功能分组，签名已省略 `public static`）：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void addConnectGuid()` | 新增连接引导 | 委托 `addConnectGuid(null)` |
  | `void addConnectGuid(ShellGroup group)` | 新增连接引导页 | `StageManager.parseStage(ShellAddConnectGuidController.class)`，`setProp("group")`，`display()` |
  | `void addSSHConnect(ShellGroup)` | 新增 SSH 连接 | parse `ShellAddSSHConnectController`，setProp group，display |
  | `void addLocalConnect(ShellGroup)` | 新增本地连接 | parse `ShellAddLocalConnectController` |
  | `void addTelnetConnect(ShellGroup)` | 新增 telnet 连接 | parse `ShellAddTelnetConnectController` |
  | `void addSerialConnect(ShellGroup)` | 新增串口连接 | parse `ShellAddSerialConnectController`（注意 `display()` 被调用两次） |
  | `void addSFTPConnect(ShellGroup)` | 新增 sftp 连接 | parse `ShellAddSFTPConnectController` |
  | `void addFTPConnect(ShellGroup)` | 新增 ftp 连接 | parse `ShellAddFTPConnectController` |
  | `void addS3Connect(ShellGroup, String s3Type)` | 新增 s3 连接 | parse `ShellAddS3ConnectController`，setProp group/s3Type |
  | `void addVNCConnect(ShellGroup)` | 新增 vnc 连接 | parse `ShellAddVNCConnectController`（`display()` 调两次） |
  | `void addRLoginConnect(ShellGroup)` | 新增 RLogin 连接 | parse `ShellAddRLoginConnectController` |
  | `void addSMBConnect(ShellGroup)` | 新增 smb 连接 | parse `ShellAddSMBConnectController` |
  | `void addRedisConnect(ShellGroup)` | 新增 redis 连接 | parse `ShellAddRedisConnectController`（`display()` 调两次） |
  | `void addZKConnect(ShellGroup)` | 新增 zk 连接 | parse `ShellAddZKConnectController`（`display()` 调两次） |
  | `void addRDPConnect(ShellGroup)` | 新增 rdp 连接 | parse `ShellAddRDPConnectController`（`display()` 调两次） |
  | `void addWebdavConnect(ShellGroup)` | 新增 webdav 连接 | parse `ShellAddWebdavConnectController`（`display()` 调两次） |
  | `void addMysqlConnect(ShellGroup)` | 新增 mysql 连接 | parse `ShellAddMysqlConnectController`（`display()` 调两次） |
  | `void addDamengConnect(ShellGroup)` | 新增 dameng 连接 | parse `ShellAddDamengConnectController`（`display()` 调两次） |
  | `void addMongoConnect(ShellGroup)` | 新增 mongodb 连接 | parse `ShellAddMongoConnectController` |
  | `void addMoshConnect(ShellGroup)` | 新增 mosh 连接 | parse `ShellAddMoshConnectController` |
  | `void updateSSHConnect(ShellConnect)` | 修改 SSH 连接 | parse `ShellUpdateSSHConnectController`，setProp shellConnect，display |
  | `void updateLocalConnect(ShellConnect)` | 修改本地连接 | parse `ShellUpdateLocalConnectController` |
  | `void updateTelnetConnect(ShellConnect)` | 修改 telnet 连接 | parse `ShellUpdateTelnetConnectController` |
  | `void updateSerialConnect(ShellConnect)` | 修改串口连接 | parse `ShellUpdateSerialConnectController` |
  | `void updateSFTPConnect(ShellConnect)` | 修改 sftp 连接 | parse `ShellUpdateSFTPConnectController` |
  | `void updateFTPConnect(ShellConnect)` | 修改 ftp 连接 | parse `ShellUpdateFTPConnectController` |
  | `void updateS3Connect(ShellConnect)` | 修改 s3 连接 | parse `ShellUpdateS3ConnectController` |
  | `void updateVNCConnect(ShellConnect)` | 修改 vnc 连接 | parse `ShellUpdateVNCConnectController` |
  | `void updateRLoginConnect(ShellConnect)` | 修改 RLogin 连接 | parse `ShellUpdateRLoginConnectController` |
  | `void updateSMBConnect(ShellConnect)` | 修改 smb 连接 | parse `ShellUpdateSMBConnectController` |
  | `void updateRedisConnect(ShellConnect)` | 修改 redis 连接 | parse `ShellUpdateRedisConnectController` |
  | `void updateZKConnect(ShellConnect)` | 修改 zk 连接 | parse `ShellUpdateZKConnectController` |
  | `void updateRDPConnect(ShellConnect)` | 修改 rdp 连接 | parse `ShellUpdateRDPConnectController` |
  | `void updateWebdavConnect(ShellConnect)` | 修改 webdav 连接 | parse `ShellUpdateWebdavConnectController` |
  | `void updateMysqlConnect(ShellConnect)` | 修改 mysql 连接 | parse `ShellUpdateMysqlConnectController` |
  | `void updateDamengConnect(ShellConnect)` | 修改 dameng 连接 | parse `ShellUpdateDamengConnectController` |
  | `void updateMongoConnect(ShellConnect)` | 修改 mongo 连接 | parse `ShellUpdateMongoConnectController` |
  | `void updateMoshConnect(ShellConnect)` | 修改 mosh 连接 | parse `ShellUpdateMoshConnectController` |
  | `void shellMain()` | 打开主页 | `StageManager.getStage(MainController.class)` 存在则 `toFront()`，否则 `showStage` |
  | `void setting()` | 打开设置 | 同上模式，`SettingController`，`showStage(..., getPrimaryStage())` |
  | `void tool()` | 打开工具页 | `StageManager.showStage(ShellToolController.class, getPrimaryStage())` |
  | `void about()` | 打开关于页 | `showStage(AboutController.class, getPrimaryStage())` |
  | `void dataExport()` | 数据导出页 | `showStage(ShellDataExportController.class, getPrimaryStage())` |
  | `void dataImport()` | 数据导入页 | 委托 `dataImport(null)` |
  | `void dataImport(File file)` | 数据导入页 | parse `ShellDataImportController`，setProp file，display |
  | `StageAdapter sshAuth(ShellConnect)` | SSH 认证页 | parse `ShellSSHAuthController`，setProp connect，`showAndWait`，返回 adapter |
  | `void fileTransport(ShellConnect sourceConnect)` | 文件传输页 | parse `ShellFileTransportController`，setProp sourceConnect，display |
  | `void fileManage(ShellFileClient<?> client)` | 上传/下载管理 | parse `ShellFileManageController`，setProp client，display |
  | `void fileInfo(ShellFile file, Window owner)` | 文件信息 | parse `ShellFileInfoController`，setProp file |
  | `void fileEdit(ShellFile file, ShellFileClient<?> client)` | 文件编辑 | parse `ShellFileEditController`，setProp file/client，`showAndWait` |
  | `void fileView(ShellFile file, ShellFileClient<?> client, String type)` | 文件查看 | parse `ShellFileViewController`，setProp file/type/client，`showAndWait` |
  | `void filePermission(ShellFile file, ShellFileClient<?> client, Window owner)` | 文件权限 | parse `ShellFilePermissionController`，`showAndWait` |
  | `StageAdapter filePkgUpload(String dest, List<File> files, ShellSFTPClient client)` | 打包上传 | parse `ShellFilePkgUploadController`，setProp dest/files/client，`showAndWait` |
  | `void dockerInfo(String info)` | docker 信息 | parse `ShellDockerInfoController`，setProp info |
  | `void dockerVersion(String version)` | docker 版本 | parse `ShellDockerVersionController` |
  | `void dockerHistory(List<ShellDockerImageHistory>)` | docker 历史 | parse `ShellDockerImageHistoryController`，setProp histories |
  | `void dockerInspect(String inspect, boolean image)` | docker 审查 | parse `ShellDockerInspectController`，setProp inspect/image |
  | `void dockerResource(ShellDockerExec, ShellDockerResource, String id)` | docker 资源 | parse `ShellDockerResourceController`，setProp exec/resource/id |
  | `void dockerLogs(String logs)` | docker 日志 | parse `ShellDockerLogsController` |
  | `void dockerPort(List<ShellDockerPort>)` | docker 端口 | parse `ShellDockerPortController` |
  | `void copyKeysToHost(List<ShellKey> keys)` | 复制密钥到主机 | parse `ShellCopyIdKeyController`，setProp keys |
  | `void updateKey(ShellKey key)` | 修改密钥 | key 为 null 直接返回；parse `ShellUpdateKeyController`，setProp key |
  | `void addKey()` | 添加密钥 | parse `ShellAddKeyController` |
  | `void importKey()` | 导入密钥 | parse `ShellImportKeyController` |
  | `StageAdapter addTunneling()` | 添加隧道 | parse `ShellAddTunnelingController`，`showAndWait` |
  | `StageAdapter updateTunneling(ShellTunnelingConfig)` | 编辑隧道 | parse `ShellUpdateTunnelingController`，setProp config，`showAndWait` |
  | `void splitGuid()` | 分屏引导 | parse `ShellSplitGuidController` |
  | `void termHistory(Node, ShellSSHClient, List<String>, Consumer<String>)` | 终端历史弹窗 | `PopupManager.parsePopup(ShellTermHistoryPopupController)`，setProp client/histories，`setSubmitHandler(callback)`，`showPopup(parent)` |
  | `void snippetList(Node, Consumer<ShellSnippet>)` | 片段列表弹窗 | `PopupManager.parsePopup(ShellSnippetPopupController)` |
  | `void snippet()` | 片段管理 | `getStage(ShellSnippetController)` 存在 `toFront`，否则 parse+display |
  | `StageAdapter addS3Bucket(ShellS3Client)` | 添加 bucket | parse `ShellS3AddBucketController`，`showAndWait` |
  | `StageAdapter updateS3Bucket(ShellS3Client, ShellS3Bucket)` | 修改 bucket | parse `ShellS3UpdateBucketController`，setProp client/bucket |
  | `void fileError(ShellFileTask task)` | 文件错误信息 | parse `ShellFileErrorController`，setProp task |
  | `StageAdapter addHost(ShellConnect)` | 添加跳板机业务 | parse `ShellAddHostController`，setProp connect，`showAndWait` |
  | `void runImage(ShellDockerExec, ShellDockerImage)` | 运行镜像 | parse `ShellDockerRunController`，setProp exec/image |
  | `void saveImage(ShellDockerExec, ShellDockerImage)` | 保存镜像 | parse `ShellDockerSaveController` |
  | `void tagImage(ShellDockerExec, ShellDockerImage)` | 镜像标签 | parse `ShellDockerTagController` |
  | `void commitContainer(ShellDockerExec, ShellDockerContainer)` | 保存容器 | parse `ShellDockerCommitController` |
  | `void shareFile(ShellS3Client, ShellS3File)` | 分享文件 | parse `ShellS3ShareFileController`，setProp s3File/client |
  | `StageAdapter addJump()` | 添加跳板机 | parse `ShellAddJumpController`，`showAndWait` |
  | `StageAdapter updateJump(ShellJumpConfig)` | 修改跳板机 | parse `ShellUpdateJumpController`，setProp config，`showAndWait` |
- 调用链：`ShellViewFactory 方法 → StageManager.parseStage/showStage/getStage → Controller`；弹窗类走 `PopupManager.parsePopup → PopupAdapter.showPopup`
- 备注：类中大量 redis/zk 相关的 `addRedisKey`、`redisBatchOperation`、`zkAddNode` 等方法体已整体注释；所有方法统一 `try/catch` 后 `MessageBox.exception`。个别新增方法存在 `adapter.display()` 重复调用（Serial/VNC/Redis/ZK/RDP/Webdav/Mysql/Dameng）。

## 2.2 dameng（cn.oyzh.easyshell.util.dameng）

## ShellDamengColumnUtil
- 职责：达梦字段类型工具，初始化各字段类型的能力标记，并提供默认值与类型判断。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static void init()` | 注册达梦字段类型 | 构造大量 `DBColumnField`（VARCHAR/VARCHAR2/CHAR/CLOB/NCHAR/NVARCHAR2/NUMBER/INT/INTEGER/BIGINT/SMALLINT/TINYINT/FLOAT/DOUBLE/DOUBLE PRECISION/DECIMAL/NUMERIC/DATE/TIMESTAMP/DATETIME/TIME/TIME WITH TIME ZONE/TIMESTAMP WITH (LOCAL) TIME ZONE/INTERVAL…/BLOB/IMAGE/RAW/VARBINARY/BINARY/BIT），分别设置 `suggestSize/supportSize/supportString/supportDefaultValue/supportDigits/supportInteger/supportAutoIncrement/supportTimestamp/supportBinary/supportBoolean/alias/exampleValue/minValue/maxValue`；再按字符串/数字/时间/二进制等分组 `putFiled(...)` |
  | `static void putFiled(DBColumnField columnField)` | 私有注册 | `DBColumnFieldManager.putFiled(DBDialect.DAMENG, columnField)` |
  | `static Object defaultValue(String type)` | 按类型返回默认值 | 支持默认值时：支持小数→`0.0`，整数→`0`，字符串→`""`，JSON→`"{'a':1}"`，二进制→`new byte[]{}`，否则 null |
  | `static boolean isYearType(String)` | 是否 YEAR | `"YEAR".equalsIgnoreCase(type)` |
  | `static boolean isDateType(String)` | 是否 DATE | `"DATE".equalsIgnoreCase(type)` |
  | `static boolean isDateTimeType(String)` | 是否 DATETIME | `"DATETIME".equalsIgnoreCase(type)` |
  | `static boolean isTimeType(String)` | 是否 TIME | `"TIME".equalsIgnoreCase(type)` |
- 调用链：`init → DBColumnFieldManager.putFiled(DBDialect.DAMENG, …)`

## ShellDamengDataUtil
- 职责：达梦数据工具，生成插入/更新 SQL、值规整化。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static String toInsertSql(DamengColumns, DamengRecord, boolean includeFields)` | 单条记录插入 SQL | 委托 `toInsertSql(columns, List.of(record), includeFields)` 取首条 |
  | `static List<String> toInsertSql(DamengColumns, List<DamengRecord>)` | 多条记录插入 SQL | 委托三参重载，`includeFields=false` |
  | `static List<String> toInsertSql(DamengColumns, List<DamengRecord>, boolean includeFields)` | 批量插入 SQL | `columns.sortOfPosition()` 排序，表名 `DBUtil.wrap(tableName, DAMENG)`；可选拼字段列表；值经 `valueStandardization` + `DBDataUtil.parameterizedForSql(column, value, DAMENG)` |
  | `static String toUpdateSql(DamengColumns, DamengRecord)` | 更新 SQL | `ShellDamengUtil.initPrimaryKey` 取主键；UPDATE `schema.table` SET 非主键字段；有主键按主键 WHERE，无主键用全字段 WHERE 且 `LIMIT 1` |
  | `static Object valueStandardization(Object value)` | 值规整化 | `DmdbClob`→`clob.data`，`DmdbBlob`→`blob.data`，否则原值 |
- 调用链：`toInsertSql → valueStandardization → DBDataUtil.parameterizedForSql`；`toUpdateSql → ShellDamengUtil.initPrimaryKey`
- 备注：`parameterizedForJson/Xml/Csv/Sql/Html/Xls`、`toInsertJson/Xml/Csv/Html/Xls` 等均为大段注释。

## ShellDamengNodeUtil
- 职责：达梦字段节点工具，生成/读取/设置编辑控件节点及标签、默认值处理。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static Object getNodeVal(Node) throws Exception` | 取节点值 | 委托 `DBNodeUtil.getNodeVal(node)` |
  | `static void setNodeVal(Node, Object)` | 设置节点值 | 先 `ShellDamengDataUtil.valueStandardization`，再 `DBNodeUtil.setNodeVal` |
  | `static Node generateNode(DamengColumn)` | 生成节点 | 委托 `generateNode(column, true)` |
  | `static Node generateNode(DamengColumn, boolean handlerDefaultValue)` | 生成节点 | `DBNodeUtil.generateNode(column)`，`setId("value")`，`handlerDigits/handlerComment`，`handlerDefaultValue` 为真且有默认值时 `handlerDefaultValue` |
  | `static List<FXLabel> generateTags(DamengColumn)` | 生成标签 | 按 `isNullable/isAutoIncrement/isUpdateOnCurrentTimestamp/isPrimaryKey/isUnsigned/isZeroFill` 生成对应 `FXLabel` 并 `addClass` |
  | `static void handlerDigits(Node, Integer)` | 处理小数位 | 位数 >0 且节点为 `DecimalTextField` 时 `setScaleLen` |
  | `static void handlerComment(Node, String)` | 处理注释 | 节点为 `TextInputControl` 时 `setPromptText` |
  | `static void handlerDefaultValue(Node, Object)` | 处理默认值 | `DigitalTextField`→setValue，`ComboBox`→select，`TextInputControl`→setText |
  | `static void handlerExampleValue(Node, Object)` | 处理示例值 | `DigitalTextField`/`ChooseFileTextField`→setValue，`TextInputControl`→setText |
- 调用链：`generateNode → DBNodeUtil.generateNode → handlerDigits/handlerComment/handlerDefaultValue`
- 备注：`getNodeVal`、`setNodeVal`、`generateNode` 内的原始实现均为注释。

## ShellDamengRecordUtil
- 职责：达梦记录工具，构建记录单元格节点、格式化值、生成右键菜单。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static Node getNode(DamengRecordProperty, Object, DamengColumn)` | 构建记录节点 | 值先 `ShellDamengDataUtil.valueStandardization`，`DBNodeUtil.getNode(object, column)`；节点为 `FXTextField` 时设置空值提示（`column.exampleValue()` 或 `DBUtil.nullPromptText()`）、背景 `DBNodeUtil.getNodeBackground`、注册右键菜单 `getColumnMenuItem`、文本变更监听置 `property.setChanged(true)` |
  | `static String formatValue(Object, DamengColumn)` | 格式化值 | 按类型分流：无类型时按对象类型；否则 JSON→`JsonTextFiled.format`、binary→`BinaryTextFiled.format`、integer→`NumberTextField.format`、digits→`DecimalTextField.format`、bit→`BitTextField.format`、boolean→`BooleanTextFiled.format`、date/time/year→对应 `*TextField.format`、text→`LongTextFiled.format`、默认→`FXTextField.format` |
  | `static List<FXMenuItem> getColumnMenuItem(DamengRecordProperty)` | 字段右键菜单 | 组装复制/粘贴/置空/置空串/复制为 INSERT/复制为 UPDATE 菜单项（`MenuItemHelper.*_no_graphic`） |
- 调用链：`getNode → DBNodeUtil.getNode → getColumnMenuItem`；`formatValue → 各类 TextField.format`

## ShellDamengUtil
- 职责：达梦通用工具，内部库判断、元数据打印、标识符包装、主键初始化。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `ENABLE_PRINT_METADATA` | `boolean` | 是否启用元数据打印，默认 true |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static boolean isInternalDatabase(String dbName)` | 是否内部库 | `StringUtil.equalsAnyIgnoreCase(dbName, "SYS","SYSDBA","SYSSSO","SYSAUDITOR","CTISYS")` |
  | `static void printMetaData(ResultSet) throws SQLException` | 打印元数据 | 开启时遍历列名并 `JulLog.info` |
  | `static String wrap(String name, DBDialect dialect)` | 标识符包装 | 仅 `DAMENG` 时用双引号包裹（首尾缺则补） |
  | `static DamengRecordPrimaryKey initPrimaryKey(DamengColumns, DamengRecord)` | 初始化主键 | columns 空返回 null；取 `columns.primaryKeys()` 首个，无则 null；否则 `new DamengRecordPrimaryKey` 并 `pk.init(column, record)` |
- 调用链：`initPrimaryKey → DamengRecordPrimaryKey.init`
- 备注：`checkTableType`、`checkViewType`、`wrapData/unwrapData`、`setVal`、`isSameVal`、`rollback`、`executeUpdate`、`gen*Name`、`toInsertRecordParam`、`removeComment`、`isFullColumn` 等均为注释。

## ShellDamengViewFactory
- 职责：达梦页面工厂，展示达梦数据导入导出/转储/传输/运行 SQL、模式与表/视图/函数/过程信息页面。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工厂类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void exportData(ShellDamengClient, String schema, String tableName)` | 导出数据 | 委托五参重载（mode=0，exportTable=null） |
  | `void exportData(ShellDamengClient, String schema, String tableName, int exportMode, ShellDamengDataExportTable)` | 导出数据 | parse `ShellDamengDataExportController`，setProp dbName/dbClient/tableName/exportMode/exportTable |
  | `void importData(ShellDamengClient, String schema)` | 导入数据 | parse `ShellDamengDataImportController`，setProp dbName/dbClient |
  | `void dumpData(ShellDamengClient, String schema, String tableName, int dumpType)` | 转储数据 | parse `ShellDamengDataDumpController`，setProp dumpType/dbName/dbClient/tableName |
  | `void runSqlFile(ShellDamengClient, String schema)` | 运行 sql 文件 | parse `ShellDamengDataRunSqlFileController` |
  | `void transportData(ShellConnect, String schema)` | 传输数据 | parse `ShellDamengDataTransportController`，setProp connect/dbName |
  | `StageAdapter addSchema(ShellDamengRootTreeItem)` | 添加模式 | parse `ShellDamengSchemaAddController`，setProp connectItem，`showAndWait` |
  | `void updateSchema(DamengSchema, ShellDamengRootTreeItem)` | 修改模式 | parse `ShellDamengSchemaUpdateController`，setProp database/connectItem |
  | `void tableInfo(ShellDamengTableTreeItem)` | 表信息 | parse `ShellDamengTableInfoController`，setProp item |
  | `void viewInfo(ShellDamengViewTreeItem)` | 视图信息 | parse `ShellDamengViewInfoController`，setProp item |
  | `void functionInfo(ShellDamengFunctionTreeItem)` | 函数信息 | parse `ShellDamengFunctionInfoController`，setProp item |
  | `void procedureInfo(ShellDamengProcedureTreeItem)` | 过程信息 | parse `ShellDamengProcedureInfoController`，setProp item |
- 调用链：`ShellDamengViewFactory 方法 → StageManager.parseStage → Controller`
- 备注：`databaseUpdate`、`addDatabase` 为注释代码；类注释误写为 "msyql页面工厂"。

## 2.3 db（cn.oyzh.easyshell.util.db）

## ShellDB18nHelper
- 职责：DB 国际化文案帮助类（表相关提示）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态方法集 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static String tableTip2()` | 表提示 2 | `I18nResourceBundle.i18nString("shell.db.table.tip2")` |
  | `static String tableTip3()` | 表提示 3 | `shell.db.table.tip3` |
  | `static String tableTip4()` | 表提示 4 | `shell.db.table.tip4` |
- 调用链：`ShellDB18nHelper.xxx → I18nResourceBundle.i18nString(key)`

## 2.4 mongo（cn.oyzh.easyshell.util.mongo）

## ShellMongoColumnUtil
- 职责：MongoDB 字段类型工具，注册各字段类型能力标记。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static void init()` | 注册 mongo 字段类型 | 构造 `DBColumnField`：OBJECT(supportJson)、LIST(supportJsonArray)、INT(supportInteger)、LONG(supportBigInteger)、STRING(supportString)、BOOLEAN(supportBoolean)、DOUBLE(supportDigits)、BINARY(supportBinary)、DATE(supportTimestamp)，逐个 `putFiled` |
  | `static void putFiled(DBColumnField)` | 私有注册 | `DBColumnFieldManager.putFiled(DBDialect.MONGODB, columnField)` |
- 调用链：`init → DBColumnFieldManager.putFiled(DBDialect.MONGODB, …)`

## ShellMongoConnectUtil
- 职责：MongoDB 连接工具，解析命令行参数、复制连接信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static ShellMongoConnectInfo parse(String input)` | 解析连接串 | 按空格切分，识别 `-server`(host:port)、`-timeout`(秒，除以 1000)、`-r`(只读)；返回 `ShellMongoConnectInfo`，异常打印返回 null |
  | `static void copyConnect(ShellMongoConnectInfo, ShellConnect)` | 复制连接信息 | 双方非空时设置 `readonly`、`connectTimeOut`、`host`(host:port) |
- 调用链：`parse → ShellMongoConnectInfo`；`copyConnect → ShellConnect.setHost/setReadonly/setConnectTimeOut`

## ShellMongoDataUtil
- 职责：MongoDB 数据工具，转义、构建记录脚本、生成插入/更新/替换脚本、值规整化。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static String escapeQuotes(String str)` | 转义符号 | `TextUtil.escape(str, '"'→"'")` |
  | `static String getRecordScript(MongoRecord, boolean skipId)` | 获取记录脚本 | 遍历列取 property，skipId 时跳过 `_id`；`_id` 用 `getOriginal()` 否则 `get()`；`buildRecordData` 拼接后返回 `{...}` |
  | `static Object buildRecordValue(Object, int deep)` | 构建记录值 | `ShellMongoUtil.getType(value)` 后委托三参重载 |
  | `static Object buildRecordValue(Object, String type, int deep)` | 构建记录值 | 按 type：int→`Int32(...)`、long→`Long(...)`、double/boolean→原值、obejectid→`ObjectId(...)`、date→`ISODate(...)`、binary→`Binary.createFromBase64(...)`、list/object→递归构建，其它字符串加引号并转义 `\r/\n` |
  | `static void buildRecordData(MongoColumn, Object, StringBuilder, int deep)` | 构建字段数据（列） | 拼接 `列名: 值` |
  | `static void buildRecordData(String colName, Object, StringBuilder, int deep)` | 构建字段数据（名） | 拼接 `字段名: 值` |
  | `static String toInsertScript(MongoRecord)` | 插入脚本 | 取首列集合名 + `getRecordScript`，委托两参重载 |
  | `static String toInsertScript(String collectionName, String doc)` | 插入脚本 | 模板 `db.getCollection('$collection').insert($doc);` 替换占位符 |
  | `static List<String> toInsertScript(List<MongoRecord>)` | 批量插入脚本 | 逐条 `toInsertScript(record)` |
  | `static String toUpdateScript(MongoRecord)` | 更新脚本 | `record._idColumn()`/`_idValue()` + `getRecordScript(record, true)` |
  | `static String toUpdateScript(String collectionName, Object id, String doc)` | 更新脚本 | 模板 `db.getCollection('$collection').update({_id: $id},{$set: $doc});`，id 经 `buildRecordValue` 转换 |
  | `static String toReplaceScript(MongoFunction)` | 替换脚本 | 模板 `replaceOne` 到 `ShellMongoUtil.SYSTEM_JS`，替换 name/code |
  | `static Object valueStandardization(Object value)` | 值规整化 | `Binary`→`getData()`，`Code`→`getCode()`，否则原值 |
- 调用链：`toInsertScript → getRecordScript → buildRecordData → buildRecordValue → ShellMongoUtil.getType`
- 备注：类首与 `escapeQuotes` 内的旧实现为注释。

## ShellMongoNodeUtil
- 职责：MongoDB 字段节点工具，读取/设置节点值、生成编辑控件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static Object getNodeVal(Node)` | 取节点值 | 委托 `DBNodeUtil.getNodeVal(node)` |
  | `static void setNodeVal(Node, Object)` | 设置节点值 | 先 `ShellMongoDataUtil.valueStandardization`，再 `DBNodeUtil.setNodeVal` |
  | `static Node generateNode(MongoColumn)` | 生成节点 | 按能力选控件：integer/bigInteger→`NumberTextField`、digits→`DecimalTextField`、timestamp→`DateTimeTextField`、binary→`BinaryTextFiled`、json/code→`JsonTextFiled`、jsonArray→`JsonTextFiled(array=true)`、boolean→`BooleanTextFiled`，默认 `FXTextField`；`setId("value")` |
- 调用链：`generateNode → DBNodeUtil / 各类 TextField`
- 备注：`getNodeVal/setNodeVal` 内部旧实现及 `setToNullString/setToEmptyString` 均为注释。

## ShellMongoRecordUtil
- 职责：MongoDB 记录工具，构建记录节点、格式化值、ID 取值、文档转记录、字段合并。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工具类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static Node getNode(MongoRecordProperty, Object, MongoColumn)` | 构建记录节点 | 值先规整；timestamp→`DateTimeTextField`（`DATE_FORMAT`）、code→`JsonTextFiled`、objectId→`FXTextField`，否则 `DBNodeUtil.getNode`；`TextField` 时设置空值提示/背景/右键菜单/文本变更监听 |
  | `static String formatValue(Object, MongoColumn)` | 格式化值 | 按类型分流：integer/bigInteger→`NumberTextField.format`、digits→`DecimalTextField.format`、string→`ClearableTextField.format`、binary→`BinaryTextFiled.format`、json/jsonArray→`JsonTextFiled.format`、timestamp→`DATE_FORMAT.format`、boolean→`BooleanTextFiled.format`，默认 `ClearableTextField.format` |
  | `static List<FXMenuItem> getColumnMenuItem(MongoRecordProperty)` | 字段右键菜单 | 复制/粘贴/置空/置空串/复制为插入脚本/复制为更新脚本 |
  | `static boolean isCollection(String name)` | 是否集合 | 不以 `.files`/`.chunks` 结尾且不等于 `SYSTEM_JS` |
  | `static boolean isBucket(String name)` | 是否存储桶 | `StringUtil.endsWith(name, ".files")` |
  | `static Object idValue(Object value)` | 获取 id 值 | 处理 `ObjectId`、各类 `BsonValue`（字符串/ObjectId/DBPointer/Binary/Boolean/Null/double/int32/int64/decimal128/timestamp/datetime），默认 `toString()` |
  | `static MongoColumns columns(List<MongoRecord> records)` | 收集字段列表 | 遍历记录列，去重添加到 `MongoColumns` |
  | `static List<MongoRecord> docToRecord(String dbName, String collectionName, FindIterable<Document>)` | 文档迭代器转记录 | 逐 `Document` 调三参 `docToRecord`，非空则 `fixRecordColType` 后加入列表 |
  | `static MongoRecord docToRecord(String dbName, String collectionName, Document)` | 文档转记录 | 遍历 key 建 `MongoColumn`（设 name/dbName/collectionName/type）并 `putValue`；无 key 返回 null |
  | `static void fixRecordColType(MongoRecord record, List<MongoRecord> records)` | 修正字段类型 | 当前记录某列为 null 时，从已有记录中寻找非 null 值的类型补全 |
- 调用链：`docToRecord → ShellMongoUtil.getType / fixRecordColType`；`getNode → DBNodeUtil.getNode → getColumnMenuItem`
- 备注：`getNode` 分支旧实现、`suitableColumnWidth`、`getColumnContextMenu`、`docToRecord(String,String,String)` 旧版均为注释。

## ShellMongoUtil
- 职责：MongoDB 通用工具，类型推断、注释移除、名称生成及常量。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `ID` | `String` | `"_id"` |
  | `SYSTEM_JS` | `String` | `"system.js"` |
  | `DATE_FORMAT` | `SimpleDateFormat` | `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static String getType(Object val)` | 获取值类型 | 按 Java 类型与 `BsonValue` 分支返回 int/long/double/string/list/boolean/date/binary/obejectid/object/code（默认 string） |
  | `static boolean isPrimaryType(Object val)` | 是否原始类型 | `getType` 结果属于 int/long/double/list/object/boolean |
  | `static String removeComment(String sql)` | 移除注释 | 逐行剔除 `-- `/`#`/`//` 单行注释及 `/* */` 多行注释 |
  | `static String genCloneName()` | 生成克隆名称 | `"_clone_" + UUIDUtil.uuidSimple().substring(0,5)` |
- 调用链：`ShellMongoDataUtil / ShellMongoRecordUtil → getType`

## ShellMongoViewFactory
- 职责：MongoDB 页面工厂，展示集合/存储桶文档编辑、数据导入导出转储、脚本运行、创建用户等页面。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态工厂类，无字段 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `StageAdapter collectionDocumentAdd(MongoColumns columns)` | 添加集合文档 | parse `ShellMongoCollectionDocumentAddController`，setProp columns，`showAndWait` |
  | `StageAdapter collectionDocumentUpdate(MongoRecord record)` | 编辑集合文档 | parse `ShellMongoCollectionDocumentUpdateController`，setProp document，`showAndWait` |
  | `StageAdapter bucketDocumentUpdate(MongoBucketFile record)` | 编辑存储桶文档 | parse `ShellMongoBucketDocumentUpdateController`，setProp document |
  | `void exportData(ShellMongoClient, String collectionName, String tableName)` | 导出数据 | 委托五参重载（mode=0，exportCollection=null） |
  | `void exportData(ShellMongoClient, String dbName, String collectionName, int exportMode, ShellMongoDataExportCollection)` | 导出数据 | parse `ShellMongoDataExportController`，setProp dbName/dbClient/collectionName/exportMode/exportTable |
  | `void importData(ShellMongoClient, String dbName)` | 导入数据 | parse `ShellMongoDataImportController`，setProp dbName/dbClient |
  | `void dumpData(ShellMongoClient, String dbName, String tableName, int dumpType)` | 转储数据 | parse `ShellMongoDataDumpController`，setProp dumpType/dbName/dbClient/tableName |
  | `void runScriptFile(ShellMongoClient, String dbName)` | 运行脚本文件 | parse `ShellMongoRunScriptFileController`，setProp dbName/dbClient |
  | `void transportData(ShellConnect, String dbName)` | 传输数据 | parse `ShellMongoDataTransportController`，setProp connect/dbName |
  | `StageAdapter userCreate(ShellMongoDatabaseTreeItem)` | 创建用户 | parse `ShellMongoUserCreateController`，setProp dbItem，`showAndWait` |
- 调用链：`ShellMongoViewFactory 方法 → StageManager.parseStage → Controller`
- 备注：`fileView`、`viewUser` 为注释代码。

---

## 附：本片段未展开的整文件死代码（整文件均被注释，无真实代码）

- `util/dameng/DBClientUtil.java`
- `util/dameng/DBConnectUtil.java`
- `util/dameng/DBExportUtil.java`
- `util/dameng/DamengI18nHelper.java`
- `util/dameng/DamengProcessUtil.java`
- `util/db/ShellDBRecordUtil.java`
- `util/db/ShellDBUtil.java`
