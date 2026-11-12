# EasyShell 启动期类加载延迟优化

> 分析日期：2026-11-13  
> 适用版本：1.2.5  
> 目标：在不影响现有功能的前提下，按条收敛启动期的非必要类加载。  
> 当前状态：L1、L4 已实施并完成冷启动验证；L2/L3 已评估并回滚，暂不实施；其余项目待逐项评估。

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

### [ ] M1. 延迟文件扩展名图标

- 现状：首次调用 `IconUtil` 时，大 switch 使 JVM 验证并加载 `151` 个 `File*SVGGlyph` 类，但运行时只有极少数图标实例。
- 证据：`1.196-1.213s` 批量加载文件图标类；类直方图中只观察到 3 个实际图标实例。
- 代码位置：`fx-plus/src/main/java/cn/oyzh/fx/plus/util/IconUtil.java:292`
- 当前约加载：`151` 类
- 预计收益：`0.75-1.5 MB`
- 首次使用触发点：本地、SFTP、FTP、SMB、S3 等文件列表第一次渲染图标。
- 候选方案：把扩展名到图标构造器的分派改为小型延迟工厂；避免在同一个巨大方法体中直接 `new` 全部图标类型。
- 风险等级：中，属于 fx-base 公共能力，影响范围比 EasyShell 单点改动大。
- 验证范围：常见文档、图片、音视频、压缩包、源码、无扩展名、隐藏文件、目录和链接图标；至少覆盖本地文件列表和一种远程文件列表。
- 回退方式：恢复 `IconUtil` 原分派逻辑。
- 状态：待处理
- 实施记录：
  - 改动前类数：
  - 改动后类数：
  - 内存变化：
  - 功能验证：
  - 结论：待定

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

## 8. 统一验收模板

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

## 9. 测量方法

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

## 10. 本轮分析产物

- 类加载日志：`target/classload-latest.log`
- L1 类加载日志：`target/classload-l1.log`、`target/classload-l1-prod.log`、`target/classload-l1-static.log`、`target/classload-l1-static-final.log`
- L4 类加载日志：`target/classload-l4.log`、`target/classload-l4-2.log`
- 类直方图：`target/class-histogram.txt`
- 入口类：`src/main/java/cn/oyzh/easyshell/EasyShellBootstrap.java`
- 启动主流程：`src/main/java/cn/oyzh/easyshell/EasyShellApp.java`




