# EasyShell 启动期类加载延迟优化

> 分析日期：2026-11-13  
> 适用版本：1.2.5  
> 目标：在不影响现有功能的前提下，按条收敛启动期的非必要类加载。  
> 当前状态：L1、L4、M1 已实施；L2/L3 已评估并回滚，暂不实施；JVM/JavaFX 参数已完成冷启动实测，当前配置不再调整。

## 1. 结论摘要

- 优化前最新包的冷启动类加载量约为 `6395`，全部集中在启动后 `1.6s` 内，之后不再增长。
- 启动后 12 秒 JVM 私有内存约 `74.5 MB`，后续在 `82-85 MB` 区间随 GC 波动。
- 之前的新包约 `8103` 类，当前减少约 `1711` 类。聊天懒加载和关闭更新提醒的启动检测已经取得主要效果。
- L1 已完成：六种数据库的 `132` 个条件类不再随应用启动加载。各 `*ConditionUtil` 通过静态块自注册，`getConditionControl()` 通过幂等的 `ensureRegistered()` 触发类初始化，首次真正取条件时才初始化。
- L4 已完成：9 个终端 Manager 的注册从启动阶段移到对应 Pane 的静态初始化块；两次冷启动中 9 个 Manager 和原先由方法引用生成的 9 个 `EasyShellApp$$Lambda` 均未加载，EasyShell 应用类由 `210` 降至 `192`。
- L1 后直接运行 fat JAR 的原始总类数为 `6320-6339`，不能与安装包的 `6395` 直接相减：fat JAR 额外加载约 `95` 个 Spring Boot loader 类，而安装包 compact launcher 约 `10` 个。按唯一类名对比，EasyShell 应用类由 `306` 降至 `174`，减少的正好是 `132` 个条件类。
- L2/L3 已做过一次试改：可延迟 `22` 个协议 Tab 和 `34` 个图标，共 `56` 个目标类；两次冷启动的唯一类名均为 `6280`，较 L1 的 `6320` 净减少 `40` 类，Metaspace used 两次均为 `27.89 MB`。没有同轮 L1 基线进程，不能把该值直接归因于 L2/L3。
- 该试改需要把连接 Tab 和图标分派改为基于类名的反射工厂，增加了类名维护和运行时失败风险；实际收益仅约 `0.25-0.35 MB` 启动期元数据，Heap、任务管理器观感和启动时间基本无变化。综合收益与维护成本，L2/L3 已回滚，暂不实施。
- 当前剩余问题主要不是业务功能真的提前使用，而是大方法、长分支或静态注册导致 JVM 验证/链接阶段提前加载了整个类族。
- 继续做延迟加载的收益已经进入边际递减区间。预计全部候选做完可再减少约 `400-450` 个类、`2-4 MB` 实际内存，不建议为此大规模重构 JavaFX 主界面或连接模型。

## 2. 基线数据

| 项目 | 旧包 | 优化前新包 | L1 前基线 |
| --- | ---: | ---: | ---: |
| 启动类数量 | 约 8847 | 约 8103 | 6395 |
| 12 秒私有内存 | 约 90 MB | 约 90 MB | 约 74.5 MB |
| 后续稳定私有内存 | - | - | 约 82-85 MB |
| 加载是否持续增长 | - | 2.5 秒后增长约 1725 类 | 1.6 秒后不再增长 |

L1 前 Metaspace 观测值：

| 指标 | 数值 |
| --- | ---: |
| Metaspace used | 约 34.6 MB |
| Class space used | 约 5.4 MB |
| Non-Class space used | 约 28.8 MB |
| 已加载类数量 | 约 6916 |

说明：类数量与私有内存不是线性关系。当前 JavaFX/CSS/字体/绘制和堆对象占主要部分，单个类的元数据平均约 5 KB。

L1 后冷启动观测（直接运行 `target/easyshell-1.2.5.jar`）：

| 指标 | 第 1 次 | 第 2 次（安装包 JVM 参数） |
| --- | ---: | ---: |
| 类加载日志原始总数 | 6339 | 6323 |
| 2 秒内加载 | 6247 | 6320 |
| EasyShell 唯一类名 | 174 | 174 |
| 六种数据库条件包 | 0 | 0 |

与基线按唯一类名对比，EasyShell 应用类为 `306 -> 174`，正好减少 `132` 个条件类。第 2 次使用与安装包相同的 JVM 参数，12 秒私有内存为 `90.0 MB`，Metaspace used 为 `34.6 MB`。由于启动方式从 compact launcher 变为 fat JAR，这两个内存指标不能与基线 `74.5 MB` 直接相减；132 个类预计对应的 `0.6-0.8 MB` Metaspace 收益也被启动方式差异和 GC 波动淹没。

注册点修正后再次冷启动为 `6320` 类，六种数据库条件包仍为 `0`。

## 3. L1 前启动构成

| 类别 | 约加载类数 | 判断 |
| --- | ---: | --- |
| JavaFX 和 com.sun.javafx | 2412 | 主界面必需，不建议延迟 |
| cn.oyzh.fx UI 框架 | 524 | 大部分是主界面控件、主题、图标，选择性优化 |
| EasyShell 应用类 | 348 | 连接树、设置、条件、Tab 等，选择性优化 |
| 数据库/驱动 | 143 | SQLite 必需，其它驱动可评估隔离 |
| SSL/安全 | 48 | 当前已较低，不再处理 |
| HTTP | 3 | 已无启动更新检测的批量加载 |
| LLM/LangChain | 4 | 聊天主组件已懒加载，剩余是设置枚举 |
| 编辑器/语法高亮 | 25 | 当前已有明显收敛，暂不处理 |

关键时间线：

| 时间 | 事件 |
| ---: | --- |
| 0.35s | `EasyShellBootstrap`、`EasyShellApp` 启动 |
| 0.43s | SQLite 存储初始化，`DriverManager` 通过 ServiceLoader 加载多个 JDBC 驱动 |
| 0.57s | 读取 `ShellSetting`，带入少量 LLM 枚举 |
| 0.72s | 主题系统加载全部内置主题类 |
| 0.80-1.00s | 主 FXML、控制器、JavaFX 控件和 CSS 初始化 |
| 1.02-1.10s | 连接树、配置 Store、OS 图标、连接 Tab 类加载 |
| 1.15s | 事件类批量加载 |
| 1.20s | `IconUtil` 首次使用，文件扩展名图标类批量加载 |
| 1.43-1.45s | 终端 Manager 和数据库条件初始化器注册（L1 前） |
| 1.60s | 类加载基本结束 |

## 4. 收敛原则

1. 一次只实施一个编号项，独立提交、独立打包、独立验证。
2. 每项必须记录改动前后的类数量和至少一次冷启动内存数据。
3. 冷启动测量至少执行 2 次，取稳定值；单次波动小于约 `50` 类或 `0.3 MB` 时视为噪声。
4. 优先选择“首次使用触发点明确、失败后可回退”的改动。
5. 若实测收益低于上述噪声，直接标记为 `放弃`，不要继续扩大改动面。
6. 不把“减少类数量”本身当作目标；必须同时满足功能回归通过、内存或启动时间有可测改善。

建议收敛顺序：

```text
L1 数据库条件初始化（已完成）
  -> L4 终端 Manager 注册（已完成）
  -> L2 连接 Tab 分派
  -> L3 OS/协议图标分派
  -> 评估 M1 文件图标
  -> 评估 M2 主题实例
  -> M3 JDBC 驱动隔离默认不做
```

## 5. 低风险优化项

### [x] L1. 延迟数据库条件初始化

- 现状：`EasyShellApp.start()` 注册 6 种数据库方言的初始化器时，提前加载 `132` 个条件类。
- 证据：日志中 `1.44s` 左右集中加载 `mysql/mariadb/mongo/dameng/postgresql/sqlite` 各 `22` 个条件类；运行时直方图中这些条件类没有对应业务实例。
- 代码位置：`EasyShellApp.start()`（原注册点）及六种 `*ConditionUtil` 的静态初始化块和 `ensureRegistered()`（最终注册点）
- 当前约加载：`132` 类
- 预计收益：`132` 类，Metaspace 约 `0.6-0.8 MB`
- 首次使用触发点：第一次加载该方言的 `*ConditionUtil`。`generateNode()` 由静态块兜底，条件下拉框由 `getConditionControl()` 中的 `ensureRegistered()` 显式触发。
- 候选方案：将六个方言的 `registerInitializer` 从应用启动阶段移动到数据库 Tab/客户端首次初始化阶段；或让 `DBConditionManager` 在按方言取条件时再执行对应 initializer。
- 风险等级：低
- 验证范围：MySQL、MariaDB、MongoDB、达梦、PostgreSQL、SQLite 的过滤条件列表和查询执行。
- 回退方式：恢复 `EasyShellApp.start()` 中的统一注册。
- 实施方式：从 `EasyShellApp.start()` 删除六种方言的 `DBConditionManager.registerInitializer(...)`，改为在各方言 `*ConditionUtil` 中提供幂等的 `ensureRegistered()`，并由静态块自动调用；`getConditionControl()` 在创建下拉框前也调用一次，以强制触发对应工具类初始化。这样 `generateNode()` 早于 `getConditionControl()` 执行时也不会得到空条件列表。
- 状态：已完成
- 实施记录：
  - 打包时间：2026-11-13 01:20
  - JAR 版本：`target/easyshell-1.2.5.jar`
  - 启动次数：4
  - 改动前类数：EasyShell 唯一类名 `306`，其中六种条件包 `132` 类
  - 改动后类数：EasyShell 唯一类名 `174`，其中六种条件包 `0` 类；原始日志总数 `6320-6339`
  - 内存变化：使用安装包 JVM 参数时，12 秒私有内存 `90.0 MB`，Metaspace used `34.6 MB`；因 fat JAR 与 compact 安装包启动方式不同，不能与基线直接相减，未观察到稳定内存下降
  - 功能验证：`mvn -q -DskipTests compile`、`mvn -q -DskipTests package` 通过；`PostgresqlConditionTest`、MariaDB 方言注册测试通过。指定测试集共 27 个测试，25 个通过，2 个为与本次改动无关的既有失败（SQLite 空过滤 SQL 前缀、Windows 路径分隔符导致 MariaDB 架构测试文件集为空）。另逐一调用六种 `*ConditionUtil.ensureRegistered()` 后查询 `DBConditionManager.conditions()`，均返回 `20` 个条件，同时覆盖 `generateNode()` 提前调用和下拉框首次创建两条路径
  - 未手工验证：尚未逐项连接六种数据库并打开过滤面板
  - 结论：保留。类加载达到预期，减少 `132` 类；绝对量较小，实际内存和启动时间收益不明显

### [ ] L2. 拆分连接 Tab 分派

- 现状：`ShellTabPane.connectionOpened()` 的长 if/else 链包含所有连接类型的静态 `of()` 调用，启动时即加载 SSH、SFTP、Redis、MySQL 等连接 Tab 类，但没有创建这些 Tab 的实例。
- 证据：`1.096-1.099s` 连续加载全部 `23` 个协议 Tab 类（SSH、Local、Telnet、SFTP、SMB、FTP、S3、Serial、VNC、RLogin、Redis、Valkey、ZK、Webdav、MySQL、SQLite、MariaDB、MongoDB、Mosh、达梦、PostgreSQL、HTTP、RDP）；连同 `ShellConnectTab`、`ShellTermTab`、`ShellKeyTab`、`ShellSplitTab`、`ShellMessageTab`、`ShellTerminalTab`、`ShellChangelogTab` 等共 `29` 个 Tab 相关类。启动后的类直方图中只有 `1` 个 `ShellHomeTab` 实例，没有任何协议 Tab 实例。
- 代码位置：`src/main/java/cn/oyzh/easyshell/tabs/ShellTabPane.java:337`
- 当前约加载：`29` 类，其中协议 Tab `23` 类
- 预计收益：`0.15-0.25 MB`
- 首次使用触发点：用户打开某种类型的连接。
- 候选方案：把每种连接的分派拆到独立小方法，或将类型到工厂的映射改为延迟查找，避免一个方法体引用全部 Tab 类型。
- 风险等级：低
- 验证范围：SSH、本地、Telnet、SFTP、SMB、FTP、S3、串口、VNC、RLogin、Redis、Valkey、ZK、WebDAV、MySQL、SQLite、MariaDB、MongoDB、Mosh、达梦、PostgreSQL、HTTP、RDP 的打开和复用。
- 回退方式：恢复原 if/else 分派。
- 状态：已评估并回滚，暂不实施
- 评估记录：
  - 试改结果：除 `ShellSSHTab` 外，其余 `22` 个协议 Tab 均可从启动期移除；`ShellSSHTab` 仍会加载，因为 `getConnectTab()`、`connectEdit()` 直接引用该类
  - 冷启动结果：两次唯一类名均为 `6280`，较 L1 的 `6320` 净减少 `40` 类；该净差值受启动时序和 Lambda 类增删影响，不能全部归因于 L2/L3
  - 验证结果：编译、打包和两次冷启动通过；23 个 Tab 类的 `FXTab` 继承关系、静态 `of(ShellConnect)` 方法及返回类型校验全部通过
  - 决策：回滚。收益约 `0.15-0.2 MB` 元数据，低于反射分派、类名维护和运行时失败风险带来的成本

### [ ] L3. 拆分 OS/协议图标分派

- 现状：`ShellOsTypeComboBox.getGlyph()` 的大 switch 在首次绘制连接树时加载全部图标类，即使当前连接只使用其中少数类型。
- 证据：`1.079-1.083s` 一次性加载 switch 中全部 `38` 个唯一 `SVGGlyph` 类，包含 12 个 OS 图标、协议图标、云厂商图标和数据库图标；启动后的类直方图中这些类总共只有 `8` 个实例（Linux 5、Windows 1、RLogin 1、Redis 1），其余 `34` 个类只有类元数据，没有对象实例。
- 代码位置：`src/main/java/cn/oyzh/easyshell/fx/ShellOsTypeComboBox.java:184`
- 当前约加载：`38` 类
- 预计收益：约 `0.2 MB`
- 首次使用触发点：连接树或连接编辑界面首次绘制某类型图标。
- 候选方案：按操作系统/协议拆成独立方法；或使用延迟工厂，只在命中某个类型时解析对应类。
- 风险等级：低
- 验证范围：连接树图标、连接编辑页类型下拉框、导入导出后的图标显示。
- 回退方式：恢复单个 switch。
- 状态：已评估并回滚，暂不实施
- 评估记录：
  - 试改结果：启动期只加载当前连接树实际使用的 `4` 个类型（Linux、Windows、RLogin、Redis），其余 `34` 个图标类可从启动期移除
  - 冷启动结果：L2/L3 合计唯一类名为 `6280`，Metaspace used 两次均为 `27.89 MB`；图标对象实例不变，Heap 基本无收益
  - 验证结果：30 个 EasyShell 图标类和 8 个 fx-plus 图标类的继承关系、无参构造器校验共 `38/38` 通过
  - 决策：回滚。收益约 `0.1-0.15 MB` 元数据，不足以抵消反射工厂带来的额外抽象和维护成本

### [x] L4. 延迟终端 Manager 注册

- 现状：启动时通过方法引用注册 9 个终端 Manager，方法引用解析会加载对应 Manager 类。
- 证据：`1.44s` 左右集中加载 ZK、Redis、Valkey、MySQL、MariaDB、Mongo、达梦、PostgreSQL、SQLite 终端 Manager。
- 代码位置：原注册点为 `src/main/java/cn/oyzh/easyshell/EasyShellApp.java:236`；现分别位于 9 个 `*TerminalPane` 的静态初始化块。
- 当前约加载：`9` 个 Manager 类，以及原方法引用在 `EasyShellApp` 中生成的 `9` 个合成 Lambda 类，共 `18` 个 EasyShell 应用类。
- 预计收益：约 `0.05 MB`
- 首次使用触发点：第一次初始化对应类型的终端 Pane。Pane 的静态块调用 `TerminalManager.setLoadHandler(...)`，首次查询命令处理器时再执行 Manager 注册。
- 实施方式：从 `EasyShellApp.start()` 删除 9 个 `setLoadHandler` 调用；在每个 `*TerminalPane` 的 `TERMINAL_NAME` 常量之后增加静态块，保存同包 Manager 的 `registerHandlers` 方法引用。未引入反射、类名映射或额外注册中心。
- 风险等级：低
- 验证范围：各数据库终端首次打开、重复打开、命令补全和命令执行。
- 回退方式：删除 9 个 Pane 静态块，恢复 `EasyShellApp.start()` 中的统一注册。
- 状态：已完成编码、打包和启动验证，待实际连接回归
- 实施记录：
  - 打包时间：2026-11-13 01:52
  - JAR：`target/easyshell-1.2.5.jar`，`140,922,492` 字节
  - 启动次数：2 次冷启动
  - 改动前类数：EasyShell 应用类 `210`；日志中 9 个 Manager 和 9 个 `EasyShellApp$$Lambda` 均存在，原始类加载数为 `6320`
  - 改动后类数：EasyShell 应用类 `192`，减少 `18`；两次冷启动的 9 个 Manager 均为 `0`，原始类加载数均为 `6314`。原始总数只减少 `6`，是因为其他 JVM 合成 Lambda 和启动时序存在增删，应用类对比更能反映本项改动
  - 内存变化：`18` 个类理论收益约 `0.05 MB`，低于冷启动波动，任务管理器观感无可归因变化
  - 功能验证：`mvn -q -DskipTests compile`、`mvn -q -DskipTests package` 通过；逐一初始化 9 个 Pane 后，对应 `TerminalManager.listHandler(name)` 的 handler 数量分别为 ZK `4`、Redis `4`、Valkey `4`、MySQL `6`、MariaDB `6`、Mongo `7`、达梦 `6`、PostgreSQL `6`、SQLite `6`，初始化前均为 `0`
  - 未手工验证：尚未实际连接 9 种服务并逐项执行命令；需回归首次打开、关闭后重开、命令补全和 `help`/`clear` 等内置命令
  - 结论：保留。达到预期且无额外维护抽象；收益很低，不期待明显内存变化

## 6. 中等收益优化项

### [x] M1. 延迟文件扩展名图标

- 现状：首次加载 `IconUtil` 时，JVM 验证其方法体并带入 `149` 个启动期不会被业务调用的 `File*SVGGlyph` 类；`FolderSVGGlyph`、`FileSVGGlyph` 是启动期真实使用，不属于可延迟范围。
- 证据：JFR 冷启动录制的 151 个文件图标类加载事件中，2 个来自连接树/Tab 实际使用，其余 149 个紧跟在 `IconUtil` 首次加载后；调用栈中没有 `getSVGIcon`/`getSystemIcon`，确认是类链接/验证触发，不是兜底方法执行。
- 代码位置：`fx-plus/src/main/java/cn/oyzh/fx/plus/util/FileIconUtil.java`（文件图标）和 `IconUtil.java`（仅应用图标）
- 当前约加载：启动期仅 2 个真实使用的文件图标类
- 预计收益：约 `149` 类，Metaspace 约 `0.75-1.5 MB`
- 首次使用触发点：首次加载 `FileIconUtil`；当前启动主界面不加载 `IconTableCell`，因此至少会推迟到打开文件列表、首次构建文件单元格时。
- 候选方案：把 `getSystemIcon`、`getSVGIcon`、`iconToFxImage`、`SYSTEM_ICON_CACHE` 及全部文件图标 import 从 `IconUtil` 拆到 `FileIconUtil`；`IconTableCell` 的兜底改为调用 `FileIconUtil`。
- 风险等级：中低，属于 fx-base 公共能力调整；不保留会反向引用 `FileIconUtil` 的兼容转发，避免把大方法重新带回启动路径。
- 验证范围：fx-plus 编译/安装成功，EasyShell 重新打包冷启动成功；JFR 确认 `FileIconUtil` 启动期未加载。
- 回退方式：恢复 `IconUtil` 原文件图标分派逻辑，并还原 `IconTableCell` 调用。
- 状态：已完成
- 实施记录：
  - 改动前类数：启动期批量加载 151 个文件图标类，其中 149 个无业务调用
  - 改动后类数：启动期仅加载 `FolderSVGGlyph`、`FileSVGGlyph` 2 个；`FileIconUtil` 加载次数为 0
  - 内存变化：JFR `jdk.ClassLoad` 事件从 `5528` 降至 `5376`（-152）；未单独测量稳定的 RSS 差值
  - 功能验证：`fx-plus` 编译、`install` 成功；EasyShell 重新打包并通过冷启动 JFR 验证
  - 结论：保留。启动期文件图标类加载达到预期，且不引入反射或运行时工厂抽象。

### [ ] M2. 延迟内置主题实例

- 现状：`Themes` 的静态字段在类初始化时创建全部内置主题对象，并加载全部主题类。
- 证据：`0.72s` 左右加载 53 个主题类，运行时直方图约 50 个主题实例；当前只使用其中一个主题。
- 代码位置：`fx-plus/src/main/java/cn/oyzh/fx/plus/theme/Themes.java:66`
- 当前约加载：`53` 类
- 预计收益：约 `0.3 MB`
- 首次使用触发点：应用当前主题、打开主题选择器或切换主题。
- 候选方案：静态目录只保存主题名称和样式表信息；当前主题和用户选中的主题按名称延迟实例化。
- 风险等级：中，需要覆盖主题切换、自定义主题、系统主题跟随和设置持久化。
- 验证范围：启动应用主题、设置页主题列表、明暗切换、自定义配色、重启后恢复。
- 回退方式：恢复 `Themes` 的静态实例字段。
- 状态：待处理
- 实施记录：
  - 改动前类数：
  - 改动后类数：
  - 内存变化：
  - 功能验证：
  - 结论：待定

### [ ] M3. JDBC 驱动按需隔离（默认不做）

- 现状：SQLite 设置库初始化时，`DriverManager` 通过 ServiceLoader 加载 H2、MySQL、MariaDB、达梦、PostgreSQL 等驱动。
- 证据：`0.43-0.46s` 加载除 SQLite 外的约 39 个驱动相关类。
- 当前约加载：约 `39` 类
- 预计收益：约 `0.2 MB`
- 候选方向：按数据库类型隔离驱动 classpath，或绕过 `DriverManager` 的全局 ServiceLoader 扫描。
- 风险等级：高，涉及连接创建、驱动发现、打包和模块边界。
- 建议：收益明显低于改动成本，默认不做；除非后续引入更多 JDBC 驱动并造成可测的内存问题。
- 状态：放弃

## 7. 不建议延迟的类别

### [x] N1. JavaFX、FXML、CSS 和绘制管线

- 约 `2412` 个 JavaFX 类，是主界面启动的硬依赖。
- 延迟会造成主窗口、控件 Skin、字体或 CSS 初始化异常。
- 不作为本轮优化目标。

### [x] N2. SQLite 和连接树基础能力

- SQLite 设置库在启动时需要读取窗口、主题、更新提醒等配置。
- 连接树、分组和连接 Store 是首屏功能。
- 延迟会引入首屏闪烁或连接列表加载延迟，收益不足以抵消复杂度。

### [x] N3. 事件类型

- 当前只加载约 29 个事件类，且事件分发依赖其类型信息。
- 类文件体积很小，不建议拆分。

### [x] N4. 已懒加载的聊天和编辑器

- 当前聊天主视图、RichTextArea、fx-editor 没有在启动期加载。
- LLM 包仅剩少量设置枚举，继续优化收益很低。
- 保持现状，只做回归验证。

## 8. JVM 启动参数与运行时内存实测

### 8.1 测试口径

- 测试包：`C:\Package\EasyShell\1.2.5\EasyShell`，使用正式 `EasyShell.cfg`，兼容图形模式 `graphicMode=2`。
- 测试方式：每轮冷启动，等待首屏稳定后读取 `Process.PrivateWorkingSetSize`（PWS）、`PrivateMemorySize64`、`WorkingSet64`，并用 `jcmd` 读取堆、Metaspace、类数量和 CodeCache。
- 样本量：CodeCache 组和基线各 5 次；JavaFX、CDS、图形模式各 3 次。差值小于约 `1 MB` 视为冷启动噪声，不据此改配置。
- 当前基线：PWS 平均 `65.75 MB`（`64.92-66.32 MB`），私有内存平均 `72.73 MB`，工作集约 `128-138 MB`，堆约 `66-68 MB`，Metaspace used 约 `34,565 KB`，已加载类 `6,887`，CodeCache used 约 `1.9 MB`。
- 这里的 `6,887` 是 JVM 统计的已加载类（包含数组类、隐藏类和 Lambda 类），会高于类加载日志中的唯一业务类名数量，两者不能直接混用。

### 8.2 JVM 参数对照

| 方案 | PWS 均值 | 相对基线 | 结论 |
| --- | ---: | ---: | --- |
| 当前正式参数 | 65.75 MB | - | 保留 |
| `-XX:ReservedCodeCacheSize=16m` | 65.60 MB | -0.15 MB | 噪声内，不采用 |
| 关闭分段 CodeCache + 16 MB 预留 + 512 KB 初始值 | 65.76 MB | +0.01 MB | 无收益，不采用 |
| `-XX:-SegmentedCodeCache` | 65.86 MB | +0.11 MB | 无收益，不采用 |
| `-XX:InitialCodeCacheSize=512k` | 66.28 MB | +0.53 MB | 略差，不采用 |

另外测试过但不进入正式配置的项：

- `-Xms32m`：最多约 `0.5-1 MB`，处于噪声内；考虑到扩容带来的额外 GC 和 CPU，保留当前不显式设置 `-Xms`。
- `-XX:-UsePerfData`：无稳定收益，且会损失运行时性能计数能力。
- `-XX:-UseStringDeduplication`：无稳定收益，反而失去字符串去重能力，保留开启。
- `-XX:ZCollectionInterval=5`：最多约 `1 MB`，但会引入周期性 GC，不建议。
- `-XX:+UseCompactObjectHeaders`：无稳定收益，不采用。
- AOT 缓存：启动更快，但实测总私有内存约 `100 MB`，不符合本轮降内存目标。
- G1、Serial：均明显差于当前 ZGC 小堆配置，不采用。
- 资源目录中的 `-XX:+PrintFlagsFinal` 只存在于 `vmArgs.windows.txt` 开发参数，正式 `EasyShell.cfg` 没有该参数，无需处理。

结论：当前 JVM 参数已经进入边际区，继续压缩 CodeCache、PerfData、字符串去重或初始堆，收益都在噪声内，甚至可能增加 CPU 与 GC 开销。正式配置保持现状。

### 8.3 CDS 实测

生成与 ZGC 非压缩指针匹配的 `classes_nocoops.jsa` 后，类元数据账面值下降，但进程内存没有下降：

| 指标 | 基线 | CDS |
| --- | ---: | ---: |
| PWS 均值 | 66.69 MB | 67.46 MB |
| Metaspace used | 约 34,567 KB | 约 24,535 KB |
| NMT Class Metadata committed | 29 MB | 20 MB |
| NMT Shared class space committed | 0 MB | 13 MB |
| NMT Total committed | 124 MB | 124 MB |

CDS 把约 `10 MB` 类元数据从 Metaspace 转移到了 `.jsa` 的共享映射，账面上 Metaspace 变小，但总 committed 和进程 PWS 没有下降。正式配置继续使用 `-Xshare:off`，不启用 CDS。

### 8.4 JavaFX 参数实测

| 参数 | PWS 均值 | 相对基线 | 结论 |
| --- | ---: | ---: | --- |
| 基线 | 65.75 MB | - | 保留 |
| `-Dprism.cacheshapes=false` | 65.51 MB | -0.24 MB | 噪声内，不采用 |
| `-Dprism.glyphCache=512` | 66.14 MB | +0.39 MB | 无收益，不采用 |
| shapes off + glyph 512 | 66.08 MB | +0.33 MB | 无收益，不采用 |
| `-Dprism.disableRegionCaching=true` | 66.05 MB | +0.30 MB | 无收益，不采用 |
| `-Dprism.dirtyopts=false` | 65.77 MB | +0.02 MB | 无收益，不采用 |
| `-Dprism.cachefonts=false -Dprism.cacheimages=false` | 66.41 MB | 同轮基线 66.40 MB，差 +0.01 MB | 5+5 次复测，噪声内，不采用 |
| `-Dprism.numrenderbuckets=1 -Dquantum.multithreaded=false` | 66.73 MB | 同轮基线 67.17 MB，差 -0.44 MB | 3 次，噪声内，不采用 |

后两组补测使用同轮基线，不与首行历史基线直接相减。JavaFX 的 shape、glyph、region、字体和图片缓存参数均没有可测收益；关闭缓存或单线程渲染还可能增加重复绘制 CPU，因此不进入正式配置。

### 8.5 图形模式实测

| 图形模式 | 私有内存均值 | PWS 均值 | 工作集均值 | 类数均值 | 主要对象 |
| --- | ---: | ---: | ---: | ---: | --- |
| 兼容模式（`graphicMode=2`） | 73.41 MB | 66.52 MB | 130.61 MB | 6,887 | `JavaSurface` 1,211、`SWRTTexture` 903 |
| 性能/均衡模式（`graphicMode=1`） | 155.02 MB | 90.64 MB | 172.23 MB | 6,961 | `D3DPipeline` 1,724、`D3DTexture` 233 |

D3D 把软件渲染的全窗像素缓冲从 Java 堆移走，堆从约 `66 MB` 降到约 `60 MB`，但进程私有内存增加约 `82 MB`、PWS 增加约 `24 MB`，并额外加载约 `74` 个类。因此当前兼容模式/软件渲染是更省内存的选择，测试后已恢复 `graphicMode=2`。

### 8.6 启动堆转储证据

启动 15 秒、执行 GC 后生成约 `40.5 MB` 的堆转储，存活对象 `271,784` 个、类 `6,888` 个。按 shallow size 统计：

- `JavaSurface` 背后的 `int[]` 约 `8.19 MB`（长度 `2,047,500`），对应主窗口软件渲染全窗缓冲。它是软件渲染的必要成本；改 D3D 会把成本转移到进程私有内存，整体更差。
- `ZipFile$Source.cen` 共 `139` 个 JAR，合计约 `6.76 MB`；`entries` 约 `0.80 MB`，`table` 约 `0.14 MB`。JAR 中央目录合计约 `7.70 MB`，是 compact 包多 JAR 结构的固定开销。
- CSS 相关对象中，`SimpleSelector` 3,606 个约 `285 KB`，`ParsedValueImpl` 6,578 个约 `250 KB`；连同 `Declaration`、`Rule`、`Size` 等合计约 `1-2 MB`。
- 字体 CMap、Marlin 渲染缓存等各为数百 KB，没有明显异常大对象。

### 8.7 后续空间判断

- 参数层：JVM 参数、CodeCache、CDS、AOT、JavaFX 缓存均已测试，没有稳定且值得保留的收益。
- 打包层：合并 JAR 对内存几乎没有收益。137 个依赖 JAR 共 `58,609` 个条目，合并后唯一路径仍有 `58,388` 个；中央目录估算仅从 `6,034,166` 字节降到 `6,017,366` 字节，约 `16.8 KB`，再加 137 个 EOCD 也只有约 `20 KB`。此前“合并可省 5-7 MB”的估算不成立，详见 8.8。
- 应用层：CSS/主题懒加载仍有约 `1-2 MB` 理论空间，但需要处理主题切换、自定义主题和回退路径，收益低于复杂度，暂不作为目标。
- 渲染层：全窗软件缓冲约 `8.19 MB` 是当前低进程内存模式的代价；D3D 只是换账本，实测更差。

结论：本轮不新增或修改 JVM/JavaFX 参数，也不合并 JAR。当前没有证据表明还存在低风险且可测的显著内存收益；裁剪未使用依赖和 JAR 条目已在 8.9 复核，只有 2 个小依赖可删，对启动类加载没有收益，详见 8.9。

### 8.8 JAR 合并可行性复核

运行态结构已经确认：

- compact 启动器会把 payload 解到 `~/.cache/compact-jar-cache`，启动子 JVM 时使用 `application.jar + 137 个 lib/*.jar + classpath.jar`。
- 当前 `application.jar` 约 `15.6 MB`，137 个依赖 JAR 合计约 `242.3 MB`，其中实际保存的是未压缩类与资源。
- 堆转储中 `ZipFile$Source.cen` 约 `6.76 MB`、`entries` 约 `0.80 MB`、`table` 约 `0.14 MB`。这是 139 个独立 JAR（应用、137 个依赖和 classpath JAR）的索引，而不是可随 JAR 数量直接消失的额外副本。

合并前后的索引计算：

| 指标 | 当前 137 个依赖 JAR | 合并为 1 个 JAR | 变化 |
| --- | ---: | ---: | ---: |
| 条目数 | 58,609 | 58,388（去重后） | -221 |
| 中央目录估算 | 6,034,166 B | 6,017,366 B | -16,800 B |
| 每 JAR EOCD | 137 x 22 B | 22 B | -3,014 B |
| 合计节省 | - | - | 约 19.8 KB |

中央目录仍要为几乎全部条目保存文件名、偏移和属性，所以合并 JAR 不会让 `ZipFile$Source.cen` 从 6-7 MB 变成几百 KB。`entries` 和 `table` 也基本随总条目数增长，合并只能省掉重复条目和少量每 JAR 固定开销，预计总收益不到 `0.3 MB`。

技术上也存在实际兼容点：

- `META-INF/services/java.sql.Driver` 有 6 个提供者，直接覆盖会导致部分 JDBC 驱动消失；必须按 ServiceLoader 语义合并文本。
- POI 的 3 个 Service 文件各有 2 个提供者，也必须合并。
- 77 个重复路径中有 69 个内容不同，包括 `fx-plus` 对 JavaFX FXML 类的定制、`fx-editor` 对 RichText 类的定制，以及 `base-mongodb`/`mongodb-driver-core`、`base-store`/`h2` 的重复类。合并必须严格保持当前 classpath 的优先顺序，否则会换掉实际生效的实现。
- 还有 222 个 `META-INF/versions` 多版本条目、8 个原生库条目，以及各依赖自己的 `MANIFEST.MF`、OSGi 元数据和资源查找路径；不能简单用 `jar -uf` 覆盖。
- 当前依赖没有签名条目，但未来若引入签名 JAR，合并后还必须重新签名并验证 `jarsigner`。
- `base-pkg` 现有的 `mergeLibs` 只是把依赖目录重新加入可执行 JAR，并不是将依赖类扁平化，因此没有现成的“合并成一个 JAR”开关。
- 如果目标只是消除 `ZipFile` 中央目录，理论上可以把依赖改成目录 classpath：目录类路径不需要 JAR 中央目录，可能省下约 7.7 MB。但 137 个依赖展开后会产生约 58,600 个文件，破坏 compact 单文件缓存和清理逻辑，并显著增加安装、Windows Defender、冷启动和更新成本；多版本 JAR 还需额外展开。该方案比合并 JAR 改造更大，同样不建议。

因此，合并 JAR 在工程上可以做，但需要自定义确定性合并器，处理 Service、重复类优先级、多版本条目、原生库和资源路径；换来的实测预期只有几百 KB 以内，风险收益比不成立。若未来仍要压缩中央目录，方向应是减少条目本身，例如裁剪确实未使用的依赖/资源，而不是减少 JAR 文件数量；该方向已在 8.9 复核，只有 2 个依赖和 7 个空壳 JAR，包体约省 0.66 MB，启动类加载收益为 0。

### 8.9 未使用依赖与 JAR 条目复核

结论先说：确实还存在未使用项，但数量很少，而且对启动内存没有可见收益。`curator-recipes` 已完成替换删除；`javax.annotation-api` 已完成运行时移除，保留 test scope 以兼容既有测试链路；另有 7 个只有 MANIFEST 的空壳 JavaFX JAR。没有发现能解释 MB 级内存差异的大块无用依赖。

本轮使用四类独立证据，没有把 Maven 的“未使用声明”直接当结论：

1. `maven-dependency-plugin:3.11.0:analyze`。3.8.1 读取 Java 25 class（major 69）会失败，3.11.0 才能完成。
2. 用 137 个运行时依赖 JAR 的显式 classpath 运行 `jdeps -verbose:class`，检查 `application.jar` 的 64 个直接 JAR 引用以及依赖之间的引用。
3. 解压扫描 `application.jar` 和 137 个 JAR 的全部 class 常量池与非 class 资源，查找类名/反射/配置字符串，排除仅靠 `jdeps` 看不到的动态引用。
4. 结合 `-Xlog:class+load`：137 个依赖 JAR 中只有 28 个在启动阶段加载了类，109 个为 0；但 0 启动类不等于未使用，必须继续查 ServiceLoader、反射和懒加载路径。

Maven 3.11.0 共报告 8 个 `Unused declared dependencies`，逐项复核结果：

| 依赖 | 实际证据 | 结论 |
| --- | --- | --- |
| `com.google.zxing:core` | `base-common` 的 `cn.oyzh.common.qrcode.QRCodeUtil` 直接引用 `MultiFormatWriter`、`BitMatrix`、`BarcodeFormat` 等；EasyShell 的 ZK/Redis/Valkey 三个二维码 Popup 调用 `QRCodeUtil.createImage` | 使用中，保留 |
| `javax.annotation:javax.annotation-api` | `src/main` 0 处引用；最终发布包的 `BOOT-INF/classes` 和全部运行时 JAR 中，15 个精确类名命中 0；唯一精确引用来自 test scope 的 Sisu | 已移除生产依赖，降为 test scope |
| `com.google.guava:guava` | `curator-client` 254 处、`curator-framework` 13 处、`curator-recipes` 28 处、`minio` 21 处类引用 | 使用中，保留 |
| `org.xerial:sqlite-jdbc` | 有 `META-INF/services/java.sql.Driver`；启动日志在 `0.446s` 加载 `org.sqlite.JDBC` 等 83 个类，本地存储依赖它 | 使用中，保留 |
| `org.apache.curator:curator-recipes` | `application.jar`、其他依赖和资源的 class/字符串扫描中，除自身外 0 处引用；应用直接使用 `curator-framework`/`curator-client`，不使用 recipes API | 已删除并通过构建验证，改为直接依赖 framework/client |
| `org.openjfx:javafx-media`（无 classifier 的通用包） | 该 JAR 只有 `META-INF/MANIFEST.MF`，307 B、0 个 class；真正生效的是 `javafx-media-27.0.1-win.jar`，启动时加载 `javafx.scene.media.MediaView` | 通用空壳无类可加载；媒体功能仍在用，保留依赖 |
| `org.slf4j:slf4j-nop` | `META-INF/services/org.slf4j.spi.SLF4JServiceProvider`；启动日志在 `0.450s` 加载 `org.slf4j.nop.NOPServiceProvider` | ServiceLoader 使用中，保留 |
| `org.apache.commons:commons-compress` | `base-common` 的 `CompressUtil`/`ArchiveUtil` 引用；EasyShell 在 `ShellFilePkgUploadController` 调用 `CompressUtil.compress(..., TAR_GZ)`；POI 和 MinIO 也引用 | 使用中，保留 |

真正可删项及量化：

| 项目 | JAR 文件大小 | 中央目录估算 | 启动加载类数 |
| --- | ---: | ---: | ---: |
| `curator-recipes-4.3.0.jar` | 646,099 B | 24,677 B | 0 |
| `javax.annotation-api-1.3.2.jar` | 13,476 B | 1,335 B | 0 |
| 合计 | 659,575 B（约 0.66 MB） | 26,012 B | 0 |

这 0.66 MB 是 JAR 文件大小，不是运行时堆或 Metaspace。两者在启动阶段都没有加载类，所以删除它们不能降低启动类数，也不能证明能降低 MB 级内存；理论上只少 JAR 的中央目录/文件句柄开销。

`curator-recipes` 实施记录：

- 2026-11-13：`pom.xml` 中删除 `curator-recipes`，改为直接声明 `curator-framework` 和 `curator-client`，版本仍为 `4.3.0`，并保留原有的 zookeeper/guava 排除。
- 依赖映射核对：应用实际导入的 12 个 Curator 类型中，`CuratorZookeeperClient`、`RetryPolicy`、`RetryOneTime`、`ZookeeperFactory` 位于 `curator-client`；`AuthInfo`、`CuratorFramework`、`CuratorFrameworkFactory`、`Builders`、`ConnectionState` 等位于 `curator-framework`；没有任何一个来自 `curator-recipes`。`base-zookeeper`、framework、client 也没有反向引用 recipes。
- 构建验证：`mvn -Dmaven.test.skip=true package` 成功；`mvn -DskipTests test-compile` 成功，209 个测试源码也都能在无 recipes 的 classpath 下编译。
- 包体变化：`target/easyshell-1.2.5.jar` 从 `140,922,891 B` 降到 `140,626,983 B`，减少 `295,908 B`（约 289 KB）；`BOOT-INF/lib` 从 137 个依赖 JAR 降到 136 个，`curator-recipes-4.3.0.jar` 已不存在；framework/client 保留，`org.apache.zookeeper:zookeeper` 仍未进入依赖树。
- 内存预期：recipes 在启动日志中加载 0 个类，因此不降低启动类数或 Metaspace；收益是包体/类路径条目减少，不应期待 MB 级内存变化。
- 未实测：尚未连接真实 ZK 服务执行连接、节点 CRUD、ACL、监听等回归；由于源码和依赖字节码均无 recipes API 引用，功能风险低，但最终仍以一次真实 ZK 连接验证为准。

`javax.annotation-api` 穷尽排查与实施记录（2026-11-13）：

- 结论：最终发布包中未发现对 `javax.annotation-api:1.3.2` 所含 15 个类的任何静态引用；生产依赖已移除，为不改变已有测试链路，保留为 `test` scope。
- 排查范围不只是项目源码 import，也不是直接采用 Maven 的 unused 结论：直接打开 Spring Boot 可执行 JAR，遍历 `BOOT-INF/classes` 和全部嵌套运行时 JAR；逐个解压 class 常量池、Service 配置、MANIFEST、POM 及其他资源，按字节搜索 slash/dot 两种类名。
- 目标类固定为：`Resource`、`Resource.AuthenticationType`、`Resources`、`ManagedBean`、`Generated`、`PreDestroy`、`Priority`、`PostConstruct`、`sql.DataSourceDefinition`、`sql.DataSourceDefinitions`、`security.RolesAllowed`、`security.PermitAll`、`security.DeclareRoles`、`security.RunAs`、`security.DenyAll`。
- 结果：移除前的 136 个运行时依赖 JAR 中，15 个精确类名命中 0；`javax.annotation` 泛前缀命中 95 处，全部是 JSR-305 的 `Nonnull`、`Nullable`、`meta.TypeQualifier*` 等，这些类不由 `javax.annotation-api` 提供。移除后新包复扫 135 个 JAR 和应用类共 66,774 个条目，精确命中仍为 0。
- 测试类路径单独扫描 187 个 JAR：唯一精确引用位于 test scope 的 `org.eclipse.sisu.inject-1.1.0.jar`，其中 `LifecycleBuilder` 使用 `PostConstruct/PreDestroy`，`Implementations` 使用 `Priority`。依赖路径为 `base-pkg:test -> maven-core:test -> sisu.inject:test`，不会进入 Spring Boot 发布包。
- 实施：`pom.xml` 将该依赖改为 `<scope>test</scope>`；`dependency:tree -Dscope=runtime` 已无该依赖，`-Dscope=test` 仍保留，209 个测试源码的既有类路径不变。
- 验证：`mvn -B -DskipTests test-compile` 成功；`mvn -B -Dmaven.test.skip=true package` 成功；`BOOT-INF/lib` 为 135 个 JAR，`javax.annotation-api-1.3.2.jar` 不存在；当前包体 `140,600,292 B`，较移除前 `140,626,983 B` 少 `26,691 B`（依赖自身 13,476 B，其余包含重建压缩波动，不作为精确内存收益）。
- 边界：字节级扫描能证明发行物中没有该 API 的直接或字符串引用，但理论上无法排除第三方在运行时用字符串拼接加反射生成类名；本轮已覆盖所有显式类名和包前缀，未发现此类构造。该依赖在启动阶段加载 0 个类，因此不降低启动类数或内存。

JAR 条目层面的证据：

- 7 个通用 JavaFX/JFX Incubator JAR 只有 `META-INF/MANIFEST.MF`，合计 2,170 B、中央目录 847 B：`javafx-base/controls/graphics/fxml/media`、`jfx-incubator-input/richtext`（均无 classifier）。它们是实际空壳，但对内存的贡献接近 0。
- 137 个依赖 JAR 共 58,609 个条目，去重后 58,388 个，有 77 个重复路径、221 个多余条目；其中 8 个路径字节相同、69 个内容不同。合并/去重中央目录最多约省 16.8 KB，详见 8.8。
- 重复条目不能直接当“无用条目”删除：包括 `fx-plus`/`fx-editor` 对 JavaFX/RichText 的定制类、`base-mongodb`/`mongodb-driver-core`、`base-store`/`h2` 的重名类，以及必须合并的 6 个 JDBC Service 文件和 3 组 POI Service 文件。简单删除会改变 classpath 优先级或丢失 provider。
- 109 个“启动时 0 类”的 JAR 中，已确认 `zxing`、`commons-compress`、`guava`、JAXB/JDBC provider 等属于懒加载或反射/ServiceLoader 路径，不能按启动加载数判定未使用。

因此，“项目还有未使用依赖/JAR 条目”这个判断只在很小的包体清理层面成立。`curator-recipes` 和 `javax.annotation-api` 均已从生产包清理，另外 7 个空壳 JAR 合计仅约 2 KB。没有实际证据表明还有大块未使用依赖；继续从依赖/JAR 清单里追求内存下降，收益已经基本挖尽。

## 9. 统一验收模板

每项收敛时复制以下模板到对应条目的“实施记录”：

```text
- 打包时间：
- JAR 版本：
- 启动次数：
- 冷启动 2 秒类数：
- 冷启动 12 秒私有内存：
- 稳定期私有内存：
- 与基线差值：
- 触发功能：
- 功能验证结果：
- 是否发现新增异常日志：
- 结论：保留 / 回退 / 放弃
```

## 10. 测量方法

类加载日志：

```powershell
$env:JAVA_TOOL_OPTIONS='-Xlog:class+load=info:file=C:/Users/Administrator/IdeaProjects/easyshell/target/classload-verify.log:uptime,level,tags'
Start-Process -FilePath 'C:\Package\EasyShell\1.2.5\EasyShell\EasyShell.exe' -WindowStyle Hidden -PassThru
```

统计类数量和加载时间：

```powershell
$items = Get-Content 'target\classload-verify.log' -Encoding utf8 | ForEach-Object {
    if ($_ -match '^\[(?<t>[0-9.]+)s\]\[info\]\[class,load\] (?<c>[^ ]+)') {
        [pscustomobject]@{ Time=[double]$Matches.t; Class=$Matches.c }
    }
}
$items.Count
($items | Where-Object Time -le 2).Count
```

运行时内存和 Metaspace：

```powershell
Get-Process EasyShell | Select-Object Id,WorkingSet64,PrivateMemorySize64
jcmd <JVM_PID> VM.metaspace
jcmd <JVM_PID> GC.class_histogram
```

## 11. 本轮分析产物

- 类加载日志：`target/classload-latest.log`
- L1 类加载日志：`target/classload-l1.log`、`target/classload-l1-prod.log`、`target/classload-l1-static.log`、`target/classload-l1-static-final.log`
- L4 类加载日志：`target/classload-l4.log`、`target/classload-l4-2.log`
- 类直方图：`target/class-histogram.txt`
- JVM/JavaFX 基准：`target/bench-final-codecache.csv`、`bench-final-lowmem.csv`、`bench-final-prism.csv`、`bench-final-prism-extra.csv`、`bench-final-prism-cacheoff-5x.csv`、`bench-final-cds.csv`、`bench-final-cds-nmt.txt`、`bench-graphic-mode.csv`
- JAR 合并评估：`target/analyze-jar-merge.ps1`、`target/analyze-jar-duplicates.ps1`、`target/analyze-jar-entries.ps1`
- 依赖/JAR 条目复核：`target/mvn-dependency-analyze-311.log`、`target/runtime-dependencies-381.txt`、`target/jdeps-app-explicit.txt`、`target/jdeps-jar-graph.csv`、`target/mvn-package-no-recipes.log`、`target/mvn-test-compile-no-recipes.log`、`target/mvn-tree-no-recipes.log`、`target/javax-annotation-exhaustive-scan.txt`、`target/javax-annotation-testcp-scan.txt`、`target/javax-annotation-runtime-rescan.txt`、`target/mvn-test-compile-javax-test-scope-retry.log`、`target/mvn-package-javax-test-scope.log`、`target/mvn-tree-javax-runtime.log`、`target/mvn-tree-javax-test.log`
- 启动堆转储脚本与分析：`target/dump-startup-heap.ps1`、`target/analyze-hprof.js`、`target/startup-live-analysis.txt`
- 入口类：`src/main/java/cn/oyzh/easyshell/EasyShellBootstrap.java`
- 启动主流程：`src/main/java/cn/oyzh/easyshell/EasyShellApp.java`




