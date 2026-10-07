# easyshell VNC 客户端（vnc1 包）代码审查文档

> 范围：easyshell/src/main/java/cn/oyzh/easyshell/vnc1/，共 1 个 .java，存活 1 个。
> 说明：仅新增文档，未改动任何 `.java`。

## ShellVNCClient

- 职责：vnc 客户端，实现 `ShellBaseClient` 与 `IRfbSessionListener`，管理 RFB 会话（socket/Protocol）、会话设置、视图初始化与剪切板同步。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| shellConnect | `ShellConnect`（final） | 连接配置（主机、端口、密码、字符集、代理、只读、SSL、extra 等） |
| state | `SimpleObjectProperty<ShellConnState>`（final） | 连接状态，初值 `ShellConnState.NOT_INITIALIZED` |
| stateListener | `ChangeListener<ShellConnState>`（final） | 状态监听器，把状态变更转发给 `ShellBaseClient.super.onStateChanged(state3)` |
| socket | `Socket` | 与 vnc 服务端的 TCP 套接字（直连或经代理创建） |
| protocol | `Protocol` | RFB 协议对象（com.glavsoft.rfb.protocol.Protocol） |
| uiSettings | `UiSettings` | UI 设置（缩放比例等） |
| protocolSettings | `ProtocolSettings` | 协议设置（只读、共享、编码、隧道等） |
| clipboardHandler | `VncClipboardHandler` | VNC 剪切板处理器 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ObjectProperty<ShellConnState> stateProperty()` | 返回状态属性 | 返回 `this.state` |
| `ShellVNCClient(ShellConnect shellConnect)` | 构造 vnc 客户端 | 保存 `shellConnect`，`this.addStateListener(this.stateListener)` |
| `void initClient()`（protected） | 初始化 socket、设置与协议 | 读 `hostIp/hostPort/connectTimeOutMs`；`isEnableProxy()` 时 `ShellProxyUtil.createSocket(...)`，否则 `new Socket()` + `setKeepAlive(true)` + `connect(new InetSocketAddress(...), timeout)` + `setTcpNoDelay(true)`；`new UiSettings()`、`ProtocolSettings.getDefaultSettings()`；`isReadonly()` 时 `setViewOnly(true)`，否则 `setSharedFlag/setAllowCopyRect/setAllowClipboardTransfer(true)`；`isSSLMode()` 时 `setTunnelType(TunnelType.SSL)`；extra `"encoding"` → `setPreferredEncoding(EncodingType.ofName(encoding))`；`new Transport(socket)` 并 `setBaudrateMeter(new BaudrateMeter())`；`new Protocol(transport, 密码 Supplier, protocolSettings)` |
| `void initVncView(VncFramebufferView vncView)` | 初始化 vnc 视图组件 | extra `"cursor"` → `LocalMouseCursorShape.ofCursorName(cursor)`；`FXUtil.runLater(() -> vncView.init(protocol, uiSettings.getScaleFactor(), cursorShape))`；`uiSettings.addListener(vncView)`、`protocolSettings.addListener(vncView)`；字符集空值回退 `"ISO-8859-1"`，`new VncClipboardHandler(protocol, encoding)`；`protocolSettings.addListener(clipboardHandler)`；`protocol.startNormalHandling(this, vncView, clipboardHandler)`；`clipboardHandler.setEnabled(true)` |
| `void start(int timeout)` | 发起连接并握手 | 已连接直接返回；`initClient()`；`state=CONNECTING`；`protocol.handshake()`；`isConnected()` 为真时 `state=CONNECTED` 并 `ShellClientChecker.push(this)`，否则 `FAILED`；异常时 `state=FAILED` 并抛出；finally `SystemUtil.gc()` |
| `void close()` | 关闭并释放资源 | 依次 `protocol.destroy()`、`uiSettings.clearListener()`、`protocolSettings.clearListener()`、`clipboardHandler.destroy()`、`socket.close()`（均置 null）；`state=CLOSED`；`removeStateListener(this.stateListener)`；`stateProperty().unbind()` |
| `ShellConnect getShellConnect()` | 取连接配置 | 返回 `shellConnect` |
| `boolean isConnected()` | 是否已连接 | `state == CLOSED` 返回 `false`；否则 `socket != null && socket.isConnected()` |
| `void rfbSessionStopped(String reason)` | RFB 会话停止回调（`IRfbSessionListener`） | 记录警告日志；若 `!isClosed()` 则 `state=INTERRUPTED` |
| `void zoomToFit(int width, int height, int fbWidth, int fbHeight)` | 缩放到合适比例 | `uiSettings` 非空且 `fbWidth/fbHeight` 均非零时调 `uiSettings.zoomToFit(width, height, fbWidth, fbHeight)` |

- 调用链：`ShellVNCClient.start → initClient → protocol.handshake → ShellClientChecker.push`；`视图初始化：initVncView → protocol.startNormalHandling(this, vncView, clipboardHandler) → clipboardHandler.setEnabled(true)`；`会话断开：RFB → rfbSessionStopped → state=INTERRUPTED`
