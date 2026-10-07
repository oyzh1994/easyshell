# EasyShell controller 包代码审查文档

> 范围：`src/main/java/cn/oyzh/easyshell/controller/` 递归全部类（不含子包细分文档，见下方索引）。
> 说明：本文档为正式代码的中文代码审查文档，逐类给出「职责 / 字段 / 方法 / 调用链」。整文件被注释掉的死代码已跳过并注明。
> 因体量较大，按业务域拆分为多个 `controller-*.md` 文件。

## 文档索引

| 文件 | 覆盖范围 |
|---|---|
| controller.md | 文档索引 + 根包控制器（About/Header/Main/Setting/ShellMain）+ main 包（Connect/Message） |
| controller-connect.md | `controller/connect/**` 各协议连接新增/更新控制器（37 类） |
| controller-dameng-mysql.md | `controller/dameng/**`（11 类）+ `controller/mysql/**`（12 类） |
| controller-mongo-redis.md | `controller/mongo/**`（11 类，含 2 处死代码）+ `controller/redis/**`（15 类） |
| controller-zk-s3.md | `controller/zk/**`（11 类，含 2 处死代码）+ `controller/s3/**`（3 类） |
| controller-file-tool.md | `controller/file/**`（8 类）+ `controller/tool/**`（7 类，含 1 处死代码） |
| controller-docker.md | `controller/docker/**`（11 类） |
| controller-misc.md | `controller/data|jump|key|snippet|split|ssh|tunneling/**`（14 类） |

## 根包说明

根包 5 个控制器对应主窗口与全局功能：`MainController` 为主窗口容器，`HeaderController` 为头部菜单栏，`ShellMainController` 为 shell 工作区容器，`SettingController` 为全局设置，`AboutController` 为关于对话框。下节为根包与 `main` 包的逐类审查。

---

## AboutController

- 职责：展示"关于"窗口，填充程序名称/版本/类型、JDK 信息以及开发者数据/日志/缓存目录。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | FXText | 程序名称 |
  | type | FXText | 程序类型 |
  | version | FXText | 程序版本 |
  | updateDate | FXText | 更新日期 |
  | copyright | FXText | 版权信息 |
  | jdkArch | FXText | jdk 架构 |
  | jdkName | FXText | jdk 名称 |
  | jdkVendor | FXText | jdk 厂商 |
  | jdkVersion | FXText | jdk 版本 |
  | developerData | FXViaFolder | 程序数据目录 |
  | developerLogs | FXViaFolder | 程序日志目录 |
  | developerCache | FXViaFolder | 程序缓存目录 |
  | project | Project | 项目信息（`final`，由 `Project.load()` 初始化） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 窗口显示时回填全部展示信息并设置标题 | 调用 `super.onWindowShown(event)`；`project.getName()/getCopyright()/getVersion()/getUpdateDate()`；`StringUtil.equals(project.getType(),"build")` 选择 `I18nHelper.buildType1()` 或 `buildType2()`；`System.getProperty("os.arch"/"java.vm.name"/"java.vm.vendor"/"java.vm.version")`；`JulUtil.getLogsDir()`、`ShellConst.getStorePath()`、`ShellConst.getCachePath()`；`stage.appendTitle(...)`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | 返回 `I18nHelper.aboutTitle()` |

- 调用链：`onWindowShown → ShellConst.getStorePath → FXViaFolder.setText`
- 调用链：`onWindowShown → StringUtil.equals → I18nHelper.buildType1/buildType2`
- 调用链：`getViewTitle → I18nHelper.aboutTitle`

## HeaderController

- 职责：主页头部业务，提供设置/关于/退出/传输/密钥/片段/消息/工具箱/主题切换/布局等菜单动作，并在 macOS 下构建系统菜单栏。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | project | Project | 项目信息（`final`，由 `Project.load()` 初始化） |
  | setting | ShellSetting | shell 相关配置（`final`，取自 `ShellSettingStore.SETTING`） |
  | root | FXHeaderBar | 头部组件 |
  | layoutPane | LayoutSVGPane | 布局组件 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void setting()` | 打开设置窗口 | `ShellViewFactory.setting()` |
  | `void about()` | 打开关于窗口 | `ShellViewFactory.about()` |
  | `void quit()` | 退出应用 | 依据 `setting.isExitDirectly()`：直接 `StageManager.exit()`；否则 `MessageBox.confirm(I18nHelper.quit()+" "+project.getName())` 确认后退出 |
  | `void transport()` | 打开文件传输 | `ShellViewFactory.fileTransport(null)` |
  | `void key()` | 显示密钥管理 | `ShellEventUtil.showKey()` |
  | `void snippet()` | 打开片段管理 | `ShellViewFactory.snippet()` |
  | `void message()` | 打开消息窗口 | `ShellEventUtil.showMessage()` |
  | `void tools()` | 打开工具箱 | `ShellViewFactory.tool()` |
  | `void themeToggle()` | 切换明暗主题 | `ThemeManager.currentTheme()`、`ThemeUtil.getInverseTheme(current)`，写回 `setting` 的 theme/bg/fg/accent 后 `ShellSettingStore.INSTANCE.replace(...)`、`ThemeManager.apply(target)` |
  | `void layout()` | 切换左右布局 | 依据 `layoutPane.isLayout1()` 调用 `ShellEventUtil.layout2()` 或 `ShellEventUtil.layout1()` |
  | `void layout1(Layout1Event event)` | 布局1事件订阅 | `layoutPane.setTipText(I18nHelper.showLeftSide())`、`layoutPane.layout1()` |
  | `void layout2(Layout2Event event)` | 布局2事件订阅 | `layoutPane.setTipText(I18nHelper.hiddenLeftSide())`、`layoutPane.layout2()` |
  | `void onWindowShowing(WindowEvent event)` | 窗口显示时初始化布局提示 | `super.onWindowShowing(event)`、`layoutPane.setTipText(I18nHelper.hiddenLeftSide())` |
  | `void onStageInitialize(StageAdapter stage)` | macOS 下构建系统菜单栏 | `OSUtil.isMacOS()` 时创建 `MenuBar`，添加 easyshell/功能/主题/字体/帮助菜单；绑定 `ShellEventUtil::layout1/layout2/addGroup/showTerminal/changelog`、`ShellViewFactory::addConnectGuid/dataExport/dataImport/splitGuid`、`this::key/snippet/message/tools/themeToggle/about/setting/quit`；遍历 `Themes.allThemes()`、`FontUtil.getFamilies()`；`menuBar.setUseSystemMenuBar(true)`、`root.setRight(menuBar)` |

- 调用链：`quit → MessageBox.confirm → StageManager.exit`
- 调用链：`themeToggle → ThemeUtil.getInverseTheme → ThemeManager.apply`
- 调用链：`layout → layoutPane.isLayout1 → ShellEventUtil.layout1/layout2`

## MainController

- 职责：主窗口控制器，管理子控制器、窗口关闭确认，以及页面尺寸/位置的记忆与恢复。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | project | Project | 项目信息（`final`，由 `Project.load()` 初始化） |
  | headerController | HeaderController | 头部页面（子控制器） |
  | shellMainController | ShellMainController | shell 主页业务（子控制器） |
  | setting | ShellSetting | shell 相关配置（`final`） |
  | settingStore | ShellSettingStore | 设置存储（`final`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `List<? extends StageController> getSubControllers()` | 返回子控制器列表 | `Arrays.asList(shellMainController, headerController)` |
  | `void onWindowCloseRequest(WindowEvent event)` | 处理窗口关闭请求 | `setting.isExitDirectly()` 时 `StageManager.exit()`；否则 `MessageBox.confirm(I18nHelper.quit()+" "+project.getName())`，取消时 `event.consume()` |
  | `void onSystemExit()` | 系统退出时保存页面尺寸/位置 | `setting.isRememberPageSize()` 时写入 pageWidth/pageHeight/pageMaximized；`isRememberPageLocation()` 时写入 pageScreenX/pageScreenY；存在变更则 `settingStore.replace(setting)`，最后 `super.onSystemExit()` |
  | `void onStageInitialize(StageAdapter stage)` | 初始化窗口尺寸与位置 | `setting.isRememberPageSize()` 时按 `isPageMaximized()` 决定 `stage.setMaximized(true)` 或 `stage.setSize(...)`；`isRememberPageLocation()` 时 `stage.setLocation(...)`；异常 `MessageBox`/`JulLog.warn` 兜底 |
  | `String getViewTitle()` | 返回窗口标题 | `I18nResourceBundle.i18nString("shell.title.main")` |

- 调用链：`onWindowCloseRequest → MessageBox.confirm → StageManager.exit`
- 调用链：`onSystemExit → settingStore.replace → super.onSystemExit`
- 调用链：`onStageInitialize → stage.setSize → stage.setLocation`

## SettingController

- 职责：应用设置窗口，覆盖基础/终端/窗口/快捷键/同步/redis/zookeeper/字体/主题/区域等配置的展示、实时应用与持久化。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | settingTreeView | SettingLeftTreeView | 设置组件（左侧树） |
  | shortcutKeyTableView | ShellShortcutKeyTableView | 快捷键组件 |
  | mainPane | SettingMainPane | 主面板 |
  | exitMode | FXToggleGroup | 退出方式分组 |
  | exitMode1 | RadioButton | 退出方式1（总是询问） |
  | exitMode2 | RadioButton | 退出方式2（直接退出） |
  | pageSize | FXCheckBox | 记住页面大小 |
  | pageLocation | FXCheckBox | 记住页面位置 |
  | theme | ThemeComboBox | 主题 |
  | bgColor | FXColorPicker | 背景色 |
  | fgColor | FXColorPicker | 前景色 |
  | accentColor | FXColorPicker | 强调色 |
  | bgColorBox | FXHBox | 背景色所在容器 |
  | fgColorBox | FXHBox | 前景色所在容器 |
  | accentColorBox | FXHBox | 强调色所在容器 |
  | fontSize | FontSizeComboBox | 字体大小 |
  | fontWeight | FontWeightComboBox | 字体粗细 |
  | fontFamily | FontFamilyTextField | 字体名称 |
  | editorFontSize | FontSizeComboBox | 编辑器字体大小 |
  | editorFontWeight | FontWeightComboBox | 编辑器字体粗细 |
  | editorFontFamily | FontFamilyTextField | 编辑器字体名称 |
  | terminalFontSize | FontSizeComboBox | 终端字体大小 |
  | terminalFontWeight | FontWeightComboBox | 终端字体粗细 |
  | terminalFontFamily | FontFamilyTextField | 终端字体名称 |
  | locale | LocaleComboBox | 区域 |
  | opacity | FXSlider | 窗口透明度 |
  | hiddenLeftAfterConnected | FXToggleSwitch | 连接后收起左侧 |
  | termType | ShellTemShellComboBox | 终端类型 |
  | termBeep | FXToggleSwitch | 蜂鸣声 |
  | termMaxLineCount | NumberTextField | 最大行数 |
  | termCopyOnSelected | FXToggleSwitch | 选中时复制 |
  | termFps | ShellTermFpsComboBox | 刷新率 |
  | termCursorStyle | ShellTermCursorStyleComboBox | 光标样式 |
  | termCursorBlinks | ShellTermCursorBlinkComboBox | 光标闪烁 |
  | termUseAntialiasing | FXToggleSwitch | 使用抗锯齿 |
  | termParseHyperlink | FXToggleSwitch | 解析超链接 |
  | termBackgroundImage | ChooseFileTextField | 背景图片 |
  | keyLoadLimit | NumberTextField | 键加载限制（redis） |
  | loadMode | FXToggleGroup | 节点加载方式分组（zookeeper） |
  | loadMode0 | RadioButton | 节点加载方式0 |
  | loadMode1 | RadioButton | 节点加载方式1 |
  | loadMode2 | RadioButton | 节点加载方式2 |
  | viewport | FXToggleGroup | 节点视图分组 |
  | viewport0 | RadioButton | 节点视图0 |
  | viewport1 | RadioButton | 节点视图1 |
  | zkContentViewport | FXToggleGroup | 内容视图分组 |
  | zkContentViewport0 | RadioButton | 内容视图0 |
  | zkContentViewport1 | RadioButton | 内容视图1 |
  | nodeLoadLimit | NumberTextField | 节点加载限制（zookeeper） |
  | setting | ShellSetting | 配置对象（`final`） |
  | settingStore | ShellSettingStore | 配置持久化对象（`final`） |
  | initReady | boolean | 是否初始化完毕，完毕后方实时生效 |
  | syncType | ShellSyncTypeCombobox | 同步-类型 |
  | syncToken | PasswordTextField | 同步-token |
  | syncId | ClearableTextField | 同步-id |
  | syncKey | CheckBox | 同步-密钥 |
  | syncGroup | CheckBox | 同步-分组 |
  | syncSnippet | CheckBox | 同步-片段 |
  | syncConnect | CheckBox | 同步-连接 |
  | syncTime | FXLabel | 同步-更新时间 |
  | enableShortcutKey | FXToggleSwitch | 启用快捷键 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShowing(WindowEvent event)` | 窗口显示时把 setting 回填到各控件 | 设置退出方式/页面大小/页面位置复选；`theme.select(...)`；颜色经 `StringUtil.emptyToDefault` 兜底；`initReady=true`；字体/区域/透明度/终端/redis/zookeeper/同步各项 `select*/setValue/setSelected`；`nodeLoadLimit()`；异常 `MessageBox.exception(ex)` |
  | `void saveSetting()` | 保存全部设置并即时应用 | 读取各控件值写入 `setting`；`checkConfigForRestart(locale)` 判断是否需重启；`applySync()`；`settingStore.update(setting)` 成功后 `closeWindow()`、`I18nManager.apply`、`FontManager.apply`、`ThemeManager.apply`、`OpacityManager.apply`，需重启则 `ShellProcessUtil.restartApplication()` |
  | `String checkConfigForRestart(String locale)` | 检查区域变更是否需要重启 | `!Objects.equals(setting.getLocale(), locale)` 返回 `I18nResourceBundle.i18nString("base.restartTip1")`，否则返回空串 |
  | `void bindListeners()` | 绑定颜色/字体/主题监听，实时应用 | 图形色块 `disableProperty().bind(...)`；`theme.selectedItemChanged`、各 `fgColor/bgColor/accentColor.valueProperty()`、各字体 `selectedItemChanged` 在 `initReady` 时调用 `applyAndSave()`；非系统主题时 `accentColorBox.enable()` |
  | `void applyAndSave()` | 立即应用并保存主题/字体 | 写回 `setting` 主题与字体项，`ThemeManager.apply(setting.themeConfig())`、`settingStore.replace(setting)`、`FontManager.apply(setting.fontConfig())` |
  | `void onWindowShown(WindowEvent event)` | 构建左侧设置树并定位 | `mainPane.getLeftTreeView()` 上 `addItem` 添加 基础/终端/窗口/快捷键/同步/redis/zookeeper/字体(含子项)/主题/区域 节点；`selectItem("ssh_box")`；`OSUtil.isLinux()` 时 `NodeGroupUtil.disappear(...,"x11")`；`updateSyncInfo()`；`stage.hideOnEscape()` |
  | `void resetFgColor()` | 重置前景色 | `fgColor.setValue(theme.getValue().getForegroundColor())` |
  | `void resetBgColor()` | 重置背景色 | `bgColor.setValue(theme.getValue().getBackgroundColor())` |
  | `void resetAccentColor()` | 重置强调色 | `accentColor.setValue(theme.getValue().getAccentColor())` |
  | `void resetLocale()` | 重置区域 | `locale.select((String) null)` |
  | `void resetOpacity()` | 重置透明度 | `opacity.setValue(OpacityManager.defaultOpacity*100)` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.settingTitle()` |
  | `void resetFontFamily()` | 重置字体名称 | `fontFamily.selectItem(AppSetting.defaultFontFamily())` |
  | `void resetFontSize()` | 重置字体大小 | `fontSize.selectSize(AppSetting.defaultFontSize())` |
  | `void resetFontWeight()` | 重置字体粗细 | `fontWeight.selectWeight(AppSetting.defaultFontWeight())` |
  | `void resetEditorFontFamily()` | 重置编辑器字体名称 | `editorFontFamily.selectItem(AppSetting.defaultEditorFontFamily())` |
  | `void resetEditorFontSize()` | 重置编辑器字体大小 | `editorFontSize.selectSize(AppSetting.defaultEditorFontSize())` |
  | `void resetEditorFontWeight()` | 重置编辑器字体粗细 | `editorFontWeight.selectWeight(AppSetting.defaultEditorFontWeight())` |
  | `void resetTerminalFontFamily()` | 重置终端字体名称 | `terminalFontFamily.selectItem(AppSetting.defaultTerminalFontFamily())` |
  | `void resetTerminalFontSize()` | 重置终端字体大小 | `terminalFontSize.selectSize(AppSetting.defaultTerminalFontSize())` |
  | `void resetTerminalFontWeight()` | 重置终端字体粗细 | `terminalFontWeight.selectWeight(AppSetting.defaultTerminalFontWeight())` |
  | `void testBashPath()` | 测试终端 shell 路径是否存在 | `OSUtil.isWindows()` 分支按 `git-bash/git-sh/msys2-bash/cygwin-bash` 逐一 `FileUtil.exists` 探测，其余走 `RuntimeUtil.execForStr("where "+bash)`；非 Windows 走 `RuntimeUtil.execForStr("which "+bash)` 并排除 "not found"；结果 `MessageBox.info/warn` |
  | `void initSync()` | 校验并初始化同步配置 | `syncToken.isEmpty()` 时 `requestFocus()` + `MessageBox.warn`；否则 `applySync()` 后 `settingStore.replace(setting)` |
  | `void doSync()` | 执行云端同步 | `initSync()` 后 `StageManager.showMask(...)`，内部 `ShellSyncManager.doSync()`、`updateSyncInfo()`，异常 `MessageBox.exception` |
  | `void clearSync()` | 清除云端同步数据 | `MessageBox.confirm(I18nHelper.clearSyncData())` 确认后 `initSync()`；`StageManager.showMask(...)` 内 `ShellSyncManager.clearSync()`、`updateSyncInfo()` |
  | `void updateSyncInfo()` | 刷新同步时间与 syncId 展示 | 依据 `setting.getSyncTime()/getSyncId()`，`DateHelper.formatDateTimeSimple(new Date(time))`、`syncTime.text/clear` |
  | `void applySync()` | 把同步控件值写回 setting | 写回 syncId/syncToken/syncType/syncKey/syncGroup/syncSnippet/syncConnect |
  | `void onStageInitialize(StageAdapter stage)` | 初始化背景图片文件过滤器 | 构造 `FileExtensionFilter(I18nHelper.pleaseSelectFile(),"*.jpg",",*.png","*.jpeg","*.gif")` 并 `termBackgroundImage.setFilter(filter)` |

- 调用链：`saveSetting → checkConfigForRestart → settingStore.update → ShellProcessUtil.restartApplication`
- 调用链：`doSync → initSync → applySync → ShellSyncManager.doSync → updateSyncInfo`
- 调用链：`applyAndSave → ThemeManager.apply → FontManager.apply`

## ShellMainController

- 职责：shell 主页控制器，管理连接树/标签页的左右布局切换、窗口标题刷新及子控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXSplitPane | 根节点（分割面板） |
  | connect | FXVBox | 左侧组件 |
  | tabPane | ShellTabPane | shell 切换面板 |
  | connectController | ConnectController | 连接（子控制器） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void flushViewTitle(ShellConnect connect)` | 刷新窗口标题 | 非空时 `stage.appendTitle(" ("+connect.getName()+")")`，否则 `stage.restoreTitle()` |
  | `void treeItemChanged(ShellTreeItemChangedEvent event)` | 树节点变化事件订阅 | `event.data() instanceof ShellConnectTreeItem item` 时 `flushViewTitle(item.value())`，否则 `flushViewTitle(null)` |
  | `void layout2(Layout2Event event)` | 布局2事件订阅（显示左侧） | `connect.display()`；分割线未显示时 `root.setShowDivider(true)`、按 `root.getPosition0(0.25)` 设置 `setDividerPositions` |
  | `void layout1(Layout1Event event)` | 布局1事件订阅（隐藏左侧） | `connect.disappear()`；分割线显示时 `root.recordPosition0()`、`setShowDivider(false)`、`setDividerPositions(0,1)` |
  | `List<SubStageController> getSubControllers()` | 返回子控制器列表 | `List.of(connectController)` |

- 调用链：`treeItemChanged → flushViewTitle → stage.appendTitle/restoreTitle`
- 调用链：`layout1 → ShellMainController.connect.disappear → root.recordPosition0`
- 调用链：`layout2 → connect.display → root.setDividerPositions`

## ConnectController

- 职责：shell 连接树业务，提供节点定位、过滤、排序、数据导入导出及文件拖拽。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tree | ShellConnectTreeView | 左侧 ssh 树 |
  | sortPane | SortSVGPane | 节点排序组件 |
  | filter | ClearableTextField | 连接过滤 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void positionNode()` | 定位到选中节点 | `tree.scrollTo(tree.getSelectedItem())` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时取消按键监听 | `super.onWindowHidden(event)`、`KeyListener.unListenReleased(tree, KeyCode.F5)` |
  | `void bindListeners()` | 绑定树变化/拖拽/F5 刷新监听 | `tree.selectItemChanged(ShellEventUtil::treeItemChanged)`；`stage.initDragFile(tree.getDragContent(), this::dragFile)`；`KeyListener.listenReleased(tree, KeyCode.F5, ...-> tree.reload())` |
  | `void dragFile(List<File> files)` | 拖拽文件处理 | `ShellEventUtil.fileDragged(files)` |
  | `void sortTree()` | 对树进行升/降序切换排序 | 依据 `sortPane.isAsc()`：`tree.sortAsc()`+`sortPane.desc()`，否则 `tree.sortDesc()`+`sortPane.asc()` |
  | `void dataImport()` | 数据导入 | `ShellViewFactory.dataImport(null)` |
  | `void dataExport()` | 数据导出 | `ShellViewFactory.dataExport()` |
  | `void onStageInitialize(StageAdapter stage)` | 初始化过滤输入监听 | `filter.addTextChangeListener` 内 `tree.setHighlight(t1)`、`tree.filter()` |

- 调用链：`sortTree → sortPane.isAsc → tree.sortAsc/sortDesc`
- 调用链：`bindListeners → KeyListener.listenReleased → tree.reload`
- 调用链：`onStageInitialize → filter.addTextChangeListener → tree.filter`

## MessageController

- 该文件整体已被注释（死代码，无有效类），跳过。
