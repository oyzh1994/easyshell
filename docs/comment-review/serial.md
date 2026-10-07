# easyshell 串口终端（serial 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/serial/，共 4 个 .java，全部存活。

## 包结构总览

```
ShellSerialClient          —— 串口客户端，实现 ShellBaseClient，封装 com.fazecast.jSerialComm.SerialPort
ShellSerialDataListener    —— 串口数据监听器，实现 SerialPortDataListener，缓冲接收到的字符
ShellSerialTermWidget      —— 串口终端组件，继承 ShellStreamTermWidget，负责创建 Tty 连接器
ShellSerialTtyConnector    —— 串口 Tty 连接器，继承 TtyStreamConnector，桥接终端与串口读写
```

协作关系：`ShellSplitTermController` 构造 `ShellSerialTermWidget` → `createTtyConnector(ShellSerialClient)` 生成 `ShellSerialTtyConnector`；连接器在 `ready()` 时创建 `ShellSerialDataListener` 并注册到客户端；客户端 `start()` 打开串口后交由 `ShellClientChecker` 托管状态。

## ShellSerialClient

- 职责：串口客户端，封装 `SerialPort` 的打开、参数设置、读写、关闭与状态维护。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| serialPort | `SerialPort` | jSerialComm 串口对象，未初始化时为 `null` |
| shellConnect | `final ShellConnect` | 连接配置（端口名、波特率、校验位、数据位、停止位、流控等） |
| state | `final SimpleObjectProperty<ShellConnState>` | 连接状态属性，初值 `ShellConnState.NOT_INITIALIZED` |
| stateListener | `final ChangeListener<ShellConnState>` | 状态变更监听器，回调 `ShellBaseClient.super.onStateChanged(state3)` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `stateProperty()` | 返回状态属性（覆写接口） | 返回 `this.state` |
| `ShellSerialClient(ShellConnect shellConnect)` | 构造 | 保存配置并 `this.addStateListener(this.stateListener)` |
| `initClient()` | 初始化串口 | `SerialPort.getCommPort(shellConnect.getSerialPortName())`；依次设置 `setParity / setBaudRate / setNumDataBits / setNumStopBits / setFlowControl` |
| `start(int timeout)` | 打开串口连接 | 已连接/连接中则直接返回；置 `CONNECTING` → `setComPortTimeouts(TIMEOUT_NONBLOCKING, timeout, timeout)` → `openPort(timeout)`；成功置 `CONNECTED` 并 `ShellClientChecker.push(this)`，失败置 `FAILED` 并记录错误码/位置；`finally` 中 `SystemUtil.gc()` |
| `write(byte[] data)` | 写入字节 | `data != null && serialPort != null` 时 `serialPort.writeBytes(data, data.length)` |
| `write(String str)` | 写入字符串 | 非空则 `str.getBytes()` 后转 `write(byte[])` |
| `close()` | 关闭连接 | `flushDataListener()`、`removeDataListener()`、关闭输入/输出流、`closePort()`，置 `serialPort = null`、状态 `CLOSED`、移除状态监听器 |
| `getOutputStream()` | 获取输出流 | `serialPort == null ? null : serialPort.getOutputStream()` |
| `getInputStream()` | 获取输入流 | `serialPort == null ? null : serialPort.getInputStream()` |
| `addDataListener(ShellSerialDataListener listener)` | 注册数据监听器 | `serialPort != null` 时 `serialPort.addDataListener(listener)` |
| `getSerialPort()` | 获取串口对象 | 返回 `serialPort` |
| `getShellConnect()` | 获取连接配置（覆写接口） | 返回 `shellConnect` |
| `isConnected()` | 是否已连接（覆写接口） | `serialPort != null && serialPort.isOpen()` |
| `getPortName()` | 获取端口名 | `serialPort == null` 时回退到 `shellConnect.getSerialPortName()`，否则 `serialPort.getSystemPortName()` |
| `getLastErrorCode()` | 最后错误码 | `serialPort == null ? null : serialPort.getLastErrorCode()` |
| `getLastErrorLocation()` | 最后错误位置 | `serialPort == null ? null : serialPort.getLastErrorLocation()` |

- 调用链：`ShellClientUtil.newClient(ShellConnect)`（`connect.isSerialType()` 分支）→ `new ShellSerialClient(connect)` → `start(timeout)` → `initClient()` → `ShellClientChecker.push(this)`
- 备注（疑似缺陷）：`initClient()` 中第 73 行 `this.serialPort.setNumStopBits(this.shellConnect.getSerialNumDataBits())` 使用“数据位”getter 设置“停止位”，`ShellConnect` 实际提供了 `getSerialNumStopBits()`，此处应为误用，会导致停止位配置不生效。

## ShellSerialDataListener

- 职责：串口数据监听器，将串口收到的字节按字符集解码为字符并缓存到队列，供终端消费。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| characters | `final Queue<Character>` | 字符缓冲队列，实现为 `ArrayDeque<>` |
| charset | `final Charset` | 解码字符集 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSerialDataListener()` | 无参构造 | 委托 `this(Charset.defaultCharset())` |
| `ShellSerialDataListener(Charset charset)` | 指定字符集构造 | 赋值 `this.charset = charset` |
| `getListeningEvents()` | 声明监听事件类型（覆写） | 返回 `SerialPort.LISTENING_EVENT_DATA_AVAILABLE` |
| `serialEvent(SerialPortEvent event)` | 数据到达回调（覆写） | 取 `event.getSource()` 强转 `SerialPort`；事件类型不匹配直接返回；`while (serialPort.bytesAvailable() != 0)` 循环读取，每次 `ThreadUtil.sleep(5)` 后按可用字节数读取并解码追加到 `characters`；debug 级记录“串口返回数据” |
| `isEmpty()` | 队列是否为空 | 返回 `characters.isEmpty()` |
| `takeChar()` | 取出一个字符 | `characters.poll()`，异常时 `printStackTrace()` 并返回 `null` |

- 调用链：`ShellSerialTtyConnector.ready()` → `new ShellSerialDataListener(charset())` → `ShellSerialClient.addDataListener(listener)` → `SerialPort.addDataListener` → 数据到达触发 `serialEvent(SerialPortEvent)` → `ShellSerialTtyConnector.read(char[], int, int)` → `takeChar()`

## ShellSerialTermWidget

- 职责：串口终端组件，继承 `ShellStreamTermWidget`，用于创建串口 Tty 连接器。
- 字段：无字段。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `createTtyConnector(ShellSerialClient client)` | 创建串口 Tty 连接器 | `new ShellSerialTtyConnector(client)` |
| `getTtyConnector()` | 获取连接器（覆写，收窄返回类型） | `(ShellSerialTtyConnector) super.getTtyConnector()` |

- 调用链：`ShellSplitTermController` 中 `client instanceof ShellSerialClient serialClient` 分支 → `new ShellSerialTermWidget()` → `widget.createTtyConnector(serialClient)` → `widget.openSession(ttyConnector)`

## ShellSerialTtyConnector

- 职责：串口 Tty 连接器，继承 `TtyStreamConnector`，将终端的读写操作桥接到串口客户端。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| client | `ShellSerialClient` | 关联的串口客户端 |
| listener | `ShellSerialDataListener` | 串口数据监听器，`ready()` 时惰性创建 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellSerialTtyConnector(ShellSerialClient client)` | 构造 | `super(client.getCharset())` 传入字符集，赋值 `this.client = client` |
| `read(char[] buf, int offset, int length)` | 读取字符到缓冲（覆写） | 循环从 `listener` 取字符写入 `buf`，满 `length` 或队列空则停；无数据时将 `buf` 填充 `(char) 0`，不足部分补齐；返回值 `len == 0 ? 1 : len`（注意：`offset` 参数未使用，始终从 `buf[0]` 写入） |
| `write(String str)` | 写出字符串（覆写） | debug 记录后 `str.getBytes(charset())` → `client.write(byte[])` |
| `write(byte[] bytes)` | 写出字节（覆写） | 先 `super.write(bytes)`，再解码为字符串记录 debug，最后 `client.write(bytes)` |
| `isConnected()` | 是否连接（覆写） | 委托 `client.isConnected()` |
| `ready()` | 就绪检查（覆写） | `listener == null` 时惰性创建 `ShellSerialDataListener(charset())` 并 `client.addDataListener(listener)`，再 `super.ready()` |
| `getName()` | 连接器名称（覆写） | 返回 `"serial-tty"` |
| `close()` | 关闭（覆写） | `super.close()` → `IOUtil.close(client)` → `client = null` |
| `input()` | 输入流（覆写） | 返回 `null` |
| `output()` | 输出流（覆写） | 返回 `null` |

- 调用链：`ShellSerialTermWidget.createTtyConnector(ShellSerialClient)` → `new ShellSerialTtyConnector(client)` → `ready()` 创建并注册 `ShellSerialDataListener` → 终端读取走 `read()` → `ShellSerialDataListener.takeChar()`；终端写出走 `write()` → `ShellSerialClient.write(byte[])` → `SerialPort.writeBytes`
- 备注：`read()` 忽略 `offset` 参数，仅从 `buf[0]` 起填充，与 `TtyStreamConnector` 常规约定（应写入 `buf[offset...]`）存在偏差，审查时需关注。
