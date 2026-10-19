# easyshell 终端模块代码审查文档 — 总览与 shell 框架

> 范围：`easyshell/src/main/java/cn/oyzh/easyshell/terminal/` 全部 490 个 `.java`。
> 本文档覆盖根目录 `Shell*` 框架类与全局约定；各子系统另见：
> - [terminal-redis.md](./terminal-redis.md) — `redis/`（370 类）
> - [terminal-zk.md](./terminal-zk.md) — `zk/`（72 类）
> - [terminal-db.md](./terminal-db.md) — `dameng/` `mysql/` `mongo/`（27 类）
>
> 说明：仅新增文档，未改动任何 `.java`。

## 1. 模块结构

`terminal/` 是一个「终端 + 命令处理器」的 shell 框架，按数据源分为 redis / zookeeper / 达梦 / mysql / mongo 五套子系统，每套都由同构的组件拼装：

```
TerminalPane（终端面板，UI+输入输出+自定义命令解析）
├── KeyHandler     按键处理（回车提交）
├── HelpHandler    帮助
├── MouseHandler   鼠标
├── HistoryHandler 历史（多子系统共用 ShellTerminalHistoryHandler）
└── CompleteHandler 补全
TerminalManager（启动期注册全部 CommandHandler）
CommandHandler 体系：
   BaseTerminalCommandHandler（fx 框架）
     ├── 抽象基类：RedisTerminalCommandHandler / ZKTerminalCommandHandler / Dameng|Mysql|MongoTerminalCommandHandler
     │     ├── 类别基类：RedisKeyTerminalCommandHandler（键补全）/ RedisNKeysTerminalCommandHandler（多键补全）
     │     ├── 子命令基类：RedisAcl/RedisCluster/... （覆写 commandHelp 转发 HELP）
     │     └── 具体命令处理器（只覆写 getCommandType / commandSubName）
     └── 补全期动态匿名处理器（newCommandHandler）
```

## 2. 全局约定

- **命令处理器识别**：`BaseTerminalCommandHandler` 通过 `commandName()` / `commandSubName()` / `commandArg()` 组合出可匹配的命令串；`execute(command, terminal)` 返回 `TerminalExecuteResult`。
- **redis 命令处理器**：几乎所有具体处理器只覆写 `ProtocolCommand getCommandType()`；键相关命令继承 `RedisKeyTerminalCommandHandler` 以支持键名补全；子命令类继承各「总处理器」并覆写 `commandSubName()`。
- **zk 命令处理器**：CLI 命令统一继承 `ZKCliTerminalCommandHandler`，用 `cliCommand()` 返回 Apache ZooKeeper 的 `CliCommand`；四字命令继承 `ZKFourLetterWordCommandHandler`。
- **db 命令处理器**：仅有 `show/use` 等元命令有专用处理器，其余 SQL 由面板 `eval()` 兜底执行。
- **补全**：`BaseTerminalCompleteHandler` 分 `noMatch/oneMatch/multiMatch` 三态输出。

## 3. 根目录 shell 框架类

### ShellProcessTermWidget

- 职责：本地 shell 进程终端组件，创建 bash/cmd 等 PTY 进程并接入终端。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| setting | `ShellSetting`（final） | 全局程序设置，取 `ShellSettingStore.SETTING` |
| envs | `HashMap<String,String>` | 延迟初始化的环境变量表，缓存 `System.getenv()` 并按 OS 追加 `TERM`、`LANG` 等 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellProcessTermWidget()` | 构造 | `super(new ShellSettingsProvider())` 后 `ShellTerminalUtil.applySetting(this, setting)` |
| `String[] getProcessCommand()` | 计算要启动的 shell 命令 | 若 `setting.getTermType()` 非空则按 macOS/Windows/Linux 分支返回（git-sh/git-bash/msys2-bash/cygwin-bash/cmd.exe）；否则默认 `/bin/bash`，Linux 用 `$SHELL -l`，macOS 用 `$SHELL --login`，Windows 用 `cmd.exe` |
| `PtyProcess createProcess()` | 创建 PTY 进程 | `new PtyProcessBuilder().setDirectory(cwd).setCommand(cmd).setEnvironment(envs).setCygwin(cygwin).setUseWinConPty(useWinConPty)...start()`；`sh.exe` 判定 cygwin，Windows ARM 强制非 cygwin |
| `TtyProcessTtyConnector createTtyConnector()` | 创建 TTY 连接器 | 调 `createProcess()`，包 `TtyProcessTtyConnector`，`getName()` 返回 `"default-tty"` |
| `Map<String,String> getEnvironments()` | 取环境变量 | 首次初始化，macOS 加 `LC_CTYPE/LANG`，统一 `TERM=xterm` |
| `void putEnvironment(String,String)` | 追加环境变量 | 写 `getEnvironments()` |

- 调用链：`TerminalPane → ShellProcessTermWidget.createTtyConnector → createProcess → PtyProcessBuilder.start → TtyProcessTtyConnector`

### ShellStreamTermWidget

- 职责：基于流（如 SSH/远程流）的终端组件，不创建本地进程。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| setting | `ShellSetting`（final） | 程序设置 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellStreamTermWidget()` | 构造 | `super(new ShellSettingsProvider())` + `applySetting` |
| `TtyStreamConnector getTtyConnector()` | 取流连接器 | 强转 `super.getTtyConnector()` |
| `TtyConnector createTtyConnector()` | 禁止本地建连 | 直接 `throw new UnsupportedOperationException()` |

- 调用链：`ShellStreamTermWidget → TtyTermWidget(override) → 外部注入 TtyStreamConnector`

### ShellSettingsProvider

- 职责：终端设置提供者，向 jediterm 提供字体、配色、快捷键、刷新率等。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| setting | `ShellSetting`（final） | 程序设置 |
| terminalFontSize | `float`（transient） | 当前终端字号，初值取设置 |
| backspaceCode | `Object` | 退格码，未设置时回退接口默认 |
| altSendsEscape | `boolean` | Alt 是否发送 Escape |

- 方法（快捷键类均按 `setting.isEnableShortcutKey()` 决定是否绑定，macOS 用 META、其他用 CTRL+SHIFT）：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getOpenUrlActionPresentation/getCopyActionPresentation/getPasteActionPresentation/getClearBufferActionPresentation` | 打开URL/复制/粘贴/清屏 动作 | 返回 `FXTerminalActionPresentation` + `KeyCodeCombination` |
| `getPageUpActionPresentation/getPageDownActionPresentation/getLineUpActionPresentation/getLineDownActionPresentation` | 翻页/上下行 | Shift+PAGE_UP/DOWN，行上下用 META/CTRL+SHIFT+UP/DOWN |
| `getFindActionPresentation/getSelectAllActionPresentation` | 查找/全选 | F / A |
| `getIncrTermSizePresentation/getDecrTermSizePresentation/getResetTermSizePresentation` | 字号增减/重置 | `+`/`=`、`-`、`/` |
| `ColorPalette getTerminalColorPalette()` | 取调色板 | `TtyColorPalette.INSTANCE` |
| `Font getFXTerminalFont()` | 取字体 | `setting.terminalFontConfig()` + `FontManager.toFont` |
| `float getTerminalFontSize()` / `void setTerminalFontSize(float)` | 字号读写 | set 时同步持久化 `ShellSettingStore.INSTANCE.update(setting)` |
| `boolean useAntialiasing()`、`int maxRefreshRate()`、`int caretBlinkingMs()`、`boolean forceActionOnMouseReporting()`（恒 true）、`int getBufferMaxLinesCount()`、`boolean altSendsEscape()`、`boolean audibleBell()`、`boolean copyOnSelect()` | 各终端行为开关 | 均读 `setting` |
| `Object getBackspaceCode()` / `void setBackspaceCode(Object)` | 退格码 | 未设置回退接口默认 |
| `void setAltSendsEscape(boolean)` | Alt 行为 | 记录日志并保存 |
| `TextStyle getFoundPatternColor()` / `TextStyle getHyperlinkColor()` | 查找高亮色/超链接色 | 依 `ThemeManager.isDarkMode()` 选色 |
| `HyperlinkStyle.HighlightMode getHyperlinkHighlightingMode()` | 超链接高亮模式 | `ALWAYS` |

- 调用链：`ShellProcessTermWidget/ShellStreamTermWidget 构造 → ShellSettingsProvider → ShellSettingStore.SETTING`

### ShellTerminalHistoryHandler

- 职责：终端历史记录处理器（redis/zk/db 各面板共用），单例。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| INSTANCE | `ShellTerminalHistoryHandler`（static final） | 全局单例 |
| cacheList | `List<ShellTerminalHistory>`（final） | 内存缓存，初容量 24 |
| historyStore | `ShellTerminalHistoryStore`（final） | 历史持久化存储 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `void clearHistory()` | 清空 | `historyStore.clear()` + `cacheList.clear()` |
| `List<ShellTerminalHistory> listHistory()` | 列历史 | 缓存空时 `historyStore.selectList()` 回填 |
| `void addHistory(TerminalHistory)` | 追加 | 新建 `ShellTerminalHistory`，写 `saveTime/line`，`historyStore.insert` 后入缓存 |

- 调用链：`TerminalPane(历史动作) → ShellTerminalHistoryHandler.addHistory → ShellTerminalHistoryStore`

### ShellTerminalUtil

- 职责：shell 终端通用工具。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static void applySetting(TtyTermWidget, ShellSetting)` | 应用设置到组件 | 若 `isTermParseHyperlink` 则 `addHyperlinkFilter(new TtyHyperlinkFilter())`；按 `getTermCursorBlinks()/getTermCursorStyle()` 设置 `CursorShape`（BLINK/STEADY × UNDERLINE/VERTICAL_BAR/BLOCK） |

- 调用链：`Shell*TermWidget 构造 → ShellTerminalUtil.applySetting → widget.getTerminalPanel().setCursorShape`

### ShellColorPalette、ShellDefaultTtyConnector、ShellTerminalCopyPasteHandler

- 状态：**三个类整体被注释禁用**（文件内容全部为 `//` 注释），不参与编译。
- `ShellColorPalette`（原 `extends ColorPalette`）：缓存前景/背景色与反射 `Method`，`getPaletteColor` 对基础色（0/7/8/15）取主题色，其余反射 `ColorPaletteImpl.WINDOWS_PALETTE/ XTERM_PALETTE`。
- `ShellDefaultTtyConnector`（原 `extends ProcessTtyConnector`）：读写日志、`resize` 调 `PtyProcess.setWinSize`、`terminalSizeProperty`、`getTermSize/getWinSize`。
- `ShellTerminalCopyPasteHandler`（原 `extends DefaultTerminalCopyPasteHandler`）：`getContents` 用 `SSHUtil.removeAnsi` 去 ANSI。

## 4. 关键调用链汇总

- 本地 shell 建连：`ShellProcessTermWidget.createTtyConnector → createProcess → PtyProcessBuilder.start`
- 终端输入提交（通用）：`TerminalPane 回车 → KeyHandler → findHandler(input) → CommandHandler.execute(command, pane) → TerminalExecuteResult`
- redis 执行：`RedisTerminalCommandHandler.execute → RedisTerminalUtil.getCommand(getCommandType(),command) → ShellRedisClient.execCommand → RedisTerminalUtil.formatOut`
- redis 键补全：`RedisTerminalKeyTerminalCommandHandler.completion → ShellRedisClient.keys(pattern,keyType) → coverInput/outputByPrompt`
- zk CLI：`ZKCliTerminalCommandHandler.execute → ZKCliCommandWrapper.exec → CliCommand.exec → ZKCliPrintStream.onResponse → TerminalExecuteResult.appendResult`
- zk 四字命令：`ZKFourLetterWordCommandHandler.execute → ZKFourLetterWordCommand.exec(host,port) → FourLetterWordMain.send4LetterWord`
- db 兜底执行：`Dameng|MysqlTerminalPane.findHandler 兜底匿名处理器 → pane.eval(sql) → ShellXxxClient.executeSql → formatResultSet`
- 历史：`ShellTerminalHistoryHandler.addHistory → ShellTerminalHistoryStore.insert`
