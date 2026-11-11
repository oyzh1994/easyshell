# Ghostty 终端引擎接入

EasyShell 在保留 JediTerm 的同时接入了 Ghostty 终端引擎，两者共用同一套
`ShellXxxTermWidget` / `TtyConnector` / 标签页 / 分屏架构。

## 启用方式

1. 打开“设置 → 终端”。
2. 在“终端引擎”中选择：
   - `Ghostty`：使用 libghostty-vt 作为终端内核；
   - `JediTerm`：原有实现。

默认引擎由 `TtyTerminalEngine.BUILTIN_DEFAULT` 和启动参数 `fx.tty.engine` 决定，
设置界面保存后对之后打开的终端生效。
3. 保存后新打开的终端使用所选引擎，已有标签页保持不变。

也可以使用启动参数强制覆盖（优先级高于设置界面）：

```
-Dfx.tty.engine=ghostty
```

设置界面仍是日常使用方式；启动参数主要用于测试或临时强制切换。

使用 Ghostty 时必须允许 FFM 访问本机库：

```
--enable-native-access=ALL-UNNAMED
```

该参数已加入 Surefire 测试配置和 `package/*.toml` 的 `java-options`。

## 实现位置

| 功能 | 位置 |
| --- | --- |
| 引擎枚举、全局默认 | `cn.oyzh.fx.tty.TtyTerminalEngine` |
| 双引擎终端基类 | `cn.oyzh.fx.tty.TtyTermWidget` |
| `TtyConnector` → Ghostty 适配 | `cn.oyzh.fx.tty.TtyConnectorTerminal` |
| ZModem 终端效果抽象 | `cn.oyzh.fx.tty.zmodem.TtyZModemTerminal` |
| 字体/主题映射 | `cn.oyzh.fx.tty.TtyGhosttySettings` |
| Ghostty JavaFX 终端 | `cn.oyzh.fx.tty.ghostty.TerminalView` |
| 设置项 | `ShellSetting#terminalEngine` |
| 设置界面 | `setting.fxml` + `SettingController#termEngine` |
| 引擎下拉框 | `ShellTermEngineComboBox` |

## 支持的连接类型

本地 Shell、SSH、Telnet、RLogin、串口、Mosh 都通过 `TtyConnector` 接入：

- 输出方向：连接器读取到的字符转 UTF-8，交给 Ghostty 解析；
- 输入方向：Ghostty 产生的 UTF-8 按键字节转字符串，由连接器按自身字符集写入；
- 尺寸变化：转发到连接器的 `resize(TermSize)`，继续复用原有远端 PTY 尺寸逻辑。

ZModem 的终端进度通过 `TtyZModemTerminal` 抽象，不再硬编码 JediTerm 的
`Terminal`：

- JediTerm 适配器调用原 `Terminal` 的换行/光标/写字符方法；
- Ghostty 适配器通过 `TerminalView.writeLocalOutput` 把 VT 序列直接写入
  libghostty-vt 的终端状态，不发送给远端进程。

因此本地、SSH 等连接上的 ZModem 传输进度在 Ghostty 模式下也能正常显示。

## 已知限制

- 引擎切换只对之后打开的会话生效；已打开会话不会热切换（屏幕状态无法无损迁移）。
- Ghostty 使用内置快捷键方案；EasyShell 自定义终端快捷键的完整映射仍在 JediTerm 侧。
- 终端背景图等 JediTerm 专有视觉特性暂未映射到 Ghostty。
- Ghostty 的 Windows aarch64 库已构建并校验，但尚未在真实 ARM64 Windows 设备上运行验证。

## 测试

```bash
# fx-tty 双引擎与 Ghostty 测试
mvn -pl fx-tty -Dtest='cn.oyzh.fx.tty.*Test,cn.oyzh.fx.tty.ghostty.*Test' test

# easyshell Ghostty 集成测试（真实本地 PTY + OSC 标题 + 输入链路）
mvn -pl easyshell -Dtest=ShellGhosttyIntegrationTest test
```
