# easyshell RDP 客户端（rdp 包）代码审查文档

> 范围：easyshell/src/main/java/cn/oyzh/easyshell/rdp/，共 2 个 .java，全部存活。
> 说明：仅新增文档，未改动任何 `.java`。

## ShellRDPClient

- 职责：rdp 客户端，实现 `ShellBaseClient`；内建模式用 rdp4j 直连，非内建模式生成 `.rdp` 文件交给系统外部程序打开。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| shellConnect | `ShellConnect`（final） | 连接配置（主机、端口、用户、密码、域、SSL、extra 等） |
| state | `SimpleObjectProperty<ShellConnState>`（final） | 连接状态，初值 `ShellConnState.NOT_INITIALIZED` |
| stateListener | `ChangeListener<ShellConnState>`（final） | 状态监听器，转发给 `ShellBaseClient.super.onStateChanged(state3)` |
| client | `RdpClient` | rdp4j 客户端（com.tangluobo.rdp4j.RdpClient），仅内建模式使用 |
| frontend | `FxRdpFrontend` | rdp4j 前端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellRDPClient(ShellConnect shellConnect)` | 构造 rdp 客户端 | 保存 `shellConnect`，`this.addStateListener(this.stateListener)` |
| `void initClient()`（protected） | 初始化 rdp4j 客户端 | `client == null` 时 `new FxRdpFrontend()`、`new RdpClient(frontend)`，并 `client.setOnDisconnected(...)` 记录断开原因与异常 |
| `void start(int timeout)` | 发起连接 | `ShellRDPUtil.isBuiltIn(shellConnect)` 为真：`initClient()`；记录 `starTime`，`state=CONNECTING`；`ThreadUtil.startVirtual` 内取 `host/port/user/password/domain`（域为空置 `null`）、extra `"resolution"`（默认 `1920x1080`，按 `x` 拆分）、extra `"color"`（默认 `32`）、`isSSLMode()`、extra `"remoteAudio"`（默认 `false`）、extra `"redirectClipboard"`（默认 `false`），执行 `client.connect(host, port, user, password, domain, width, height, color, sslMode, remoteAudio, redirectClipboard)`；`DownLatch.await(timeout)` 阻塞；异常抛出；`isConnected()` 为真时 `state=CONNECTED` 并 `ShellClientChecker.push(this)`，否则 `close()` 并把状态置 `FAILED`（原状态为 `FAILED` 时先置 `null` 再置 `FAILED`）；catch 中告警并取 `getCause()` 后抛 `ShellException`。非内建模式：`ShellRDPUtil.initRDPFile(shellConnect)` 后 macOS 用 `ProcessBuilderUtil.exec("open", path)`、Windows 用 `ProcessBuilderUtil.exec("mstsc", path)` |
| `void initRdpView(RdpView rdpView)` | 初始化 rdp 视图组件 | `client == null` 时先 `initClient()`；`rdpView.steup(client, frontend)` |
| `ShellConnect getShellConnect()` | 取连接配置 | 返回 `shellConnect` |
| `boolean isConnected()` | 是否已连接 | `client != null` 时返回 `client.isConnected()`；否则 `NetworkUtil.reachable(hostIp, hostPort, 1000)` |
| `ObjectProperty<ShellConnState> stateProperty()` | 返回状态属性 | 返回 `this.state` |
| `void close()` | 关闭连接 | `client.disconnect()`；`state=CLOSED`；`removeStateListener(this.stateListener)`；`stateProperty().unbind()` |

- 调用链：`start(内建) → initClient → RdpClient.connect → ShellClientChecker.push`；`start(非内建) → ShellRDPUtil.initRDPFile → ProcessBuilderUtil.exec(open / mstsc)`

> 审查提示：`start` 的 catch 分支日志文案为 `"Mysql client start error"`（疑似复制遗留），与 RDP 无关。

## ShellRDPUtil

- 职责：rdp 工具类，判定是否内建模式、生成 `.rdp` 配置文件并加密 rdp 密码。
- 字段：无字段（仅静态方法）。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static String cryptRdpPassword(String password)` | 加密 rdp 密码 | `Crypt32Util.cryptProtectData(password.getBytes(StandardCharsets.UTF_16LE))`，再 `HexUtil.bytesToHex(bytes)` 返回十六进制串 |
| `static boolean isBuiltIn(ShellConnect connect)` | 是否内建版本 | extra `"method"` 非空且等于 `0` |
| `static File initRDPFile(ShellConnect connect)` | 生成 `.rdp` 文件 | 读 `ip/port/user/domain/password` 及 extra `"color"/"resolution"/"remoteAudio"/"redirectClipboard"`；`ShellConst.getCachePath()` 下以 `UUIDUtil.uuidSimple() + ".rdp"` 建临时文件；用 `ArrayList<String>` 逐行组装：`full address:s:ip:port`、`username:s:`、域非空时 `session bpp:i:color`、`audiomode:i:1/0`、`redirectclipboard:i:1/0`、域非空时 `domain:s`、密码非空时 Windows 写 `password 51:b:<cryptRdpPassword>` 否者 `ClipboardUtil.setString(password)`、分辨率非空则加 `use multimon:i:0`/`screen mode id:i:1`/`desktopwidth:i:`/`desktopheight:i:`（macOS 另加 `dynamic resolution:i:0`）；`FileUtil.writeUtf8Lines(list, tempFile)` 写入并返回文件 |

- 调用链：`ShellRDPClient.start(非内建) → ShellRDPUtil.initRDPFile → ShellRDPUtil.cryptRdpPassword(Windows) / ClipboardUtil.setString(非 Windows) → FileUtil.writeUtf8Lines`
