## AboutController
- 职责：关于对话框业务，展示程序名称、版本、版权与 JDK、开发者目录等信息。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | FXText | 程序名称 |
  | type | FXText | 程序类型（构建/发布） |
  | version | FXText | 程序版本 |
  | updateDate | FXText | 更新日期 |
  | copyright | FXText | 版权信息 |
  | jdkArch | FXText | JDK 架构 |
  | jdkName | FXText | JDK 名称（java.vm.name） |
  | jdkVendor | FXText | JDK 厂商（java.vm.vendor） |
  | jdkVersion | FXText | JDK 版本（java.vm.version） |
  | developerData | FXViaFolder | 程序数据目录 |
  | developerLogs | FXViaFolder | 程序日志目录 |
  | developerCache | FXViaFolder | 程序缓存目录 |
  | project | Project | 项目信息（final，`Project.load()` 加载） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 窗口显示时填充各文本 | 调 `super.onWindowShown`；用 `project` 设置名称/版权/版本/类型（`StringUtil.equals` 判断 build 后取 `I18nHelper.buildType1/2`）；用 `System.getProperty` 取 os.arch 与 java.vm.* 设置 JDK 信息；取 `JulUtil.getLogsDir()`、`ShellConst.getStorePath()`、`ShellConst.getCachePath()` 设置开发者目录；`stage.appendTitle(...)`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回视图标题 | 返回 `I18nHelper.aboutTitle()` |

- 调用链：`onWindowShown → Project.load → System.getProperty → stage.appendTitle`

## HeaderController
- 职责：主页头部业务，提供设置/关于/退出/主题切换等菜单与工具栏操作，并在 macOS 上构建系统菜单栏。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | project | Project | 项目信息（final，`Project.load()`） |
  | setting | ShellSetting | shell 相关配置（final，`ShellSettingStore.SETTING`） |
  | root | FXHeaderBar | 头部组件（@FXML） |
  | layoutPane | LayoutSVGPane | 布局组件（@FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private void setting()` | 打开设置窗口 | `ShellViewFactory.setting()` |
  | `private void about()` | 打开关于窗口 | `ShellViewFactory.about()` |
  | `private void quit()` | 退出应用 | 若 `setting.isExitDirectly()` 直接 `StageManager.exit()`，否则 `MessageBox.confirm` 确认后 `StageManager.exit()`；均记录 `JulLog` |
  | `private void transport()` | 传输数据 | `ShellViewFactory.fileTransport(null)` |
  | `private void key()` | 密钥 | `ShellEventUtil.showKey()` |
  | `private void snippet()` | 片段 | `ShellViewFactory.snippet()` |
  | `private void message()` | 消息 | `ShellEventUtil.showMessage()` |
  | `private void tools()` | 工具箱 | `ShellViewFactory.tool()` |
  | `private void themeToggle()` | 主题切换 | `ThemeManager.currentTheme()` 取当前主题，`ThemeUtil.getInverseTheme` 取反，写回 setting 的 theme/bg/fg/accent，`ShellSettingStore.INSTANCE.replace`，`ThemeManager.apply(target)` |
  | `private void layout()` | 布局切换 | 依据 `layoutPane.isLayout1()` 调 `ShellEventUtil.layout2()` 或 `ShellEventUtil.layout1()` |
  | `private void layout1(Layout1Event event)` | 布局1事件处理 | `EventSubscribe`；`layoutPane.setTipText(I18nHelper.showLeftSide())`、`layoutPane.layout1()` |
  | `private void layout2(Layout2Event event)` | 布局2事件处理 | `EventSubscribe`；`layoutPane.setTipText(I18nHelper.hiddenLeftSide())`、`layoutPane.layout2()` |
  | `void onWindowShowing(WindowEvent event)` | 窗口显示前 | 调 `super`；`layoutPane.setTipText(I18nHelper.hiddenLeftSide())` |
  | `void onStageInitialize(StageAdapter stage)` | 窗口初始化 | 调 `super`；macOS 下构建 `MenuBar`：项目菜单（收起/显示左侧、新建连接、新建分组、导入导出、最小化）、功能菜单（密钥/片段/消息/工具/分屏/本地终端/更新日志）、主题菜单（遍历 `Themes.allThemes()` 生成项 + 切换主题）、字体菜单（遍历 `FontUtil.getFamilies()` 生成项，应用 `FontManager.apply`）、帮助菜单（关于/设置/退出），最后 `menuBar.setUseSystemMenuBar(true)` 与 `root.setRight(menuBar)` |

- 调用链：`onStageInitialize → Themes.allThemes/FontUtil.getFamilies → FXMenuItem 回调`；`themeToggle → ThemeManager.currentTheme → ThemeUtil.getInverseTheme → ThemeManager.apply`；`quit → StageManager.exit`

## MainController
- 职责：主页业务，管理子控制器并处理窗口关闭确认、页面大小/位置记忆与恢复。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | project | Project | 项目信息（final，`Project.load()`） |
  | headerController | HeaderController | 头部页面控制器（@FXML） |
  | shellMainController | ShellMainController | shell 主页业务控制器（@FXML） |
  | setting | ShellSetting | shell 相关配置（final，`ShellSettingStore.SETTING`） |
  | settingStore | ShellSettingStore | 设置存储（final，`ShellSettingStore.INSTANCE`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `List<? extends StageController> getSubControllers()` | 返回子控制器 | 返回 `Arrays.asList(shellMainController, headerController)` |
  | `void onWindowCloseRequest(WindowEvent event)` | 关闭请求处理 | `JulLog.warn`；`setting.isExitDirectly()` 时直接 `StageManager.exit()`，否则 `MessageBox.confirm` 确认后 `StageManager.exit()`，取消则 `event.consume()` |
  | `void onSystemExit()` | 系统退出前保存 | `setting.isRememberPageSize()` 时记录宽高与最大化状态；`setting.isRememberPageLocation()` 时记录屏幕 X/Y；有变更则 `settingStore.replace(setting)`；调 `super.onSystemExit()` |
  | `void onStageInitialize(StageAdapter stage)` | 窗口初始化 | `super`；按 `setting` 恢复最大化/宽高（`stage.setMaximized`/`stage.setSize`）与位置（`stage.setLocation`）；异常 `ex.printStackTrace()` 并 `JulLog.warn` |
  | `String getViewTitle()` | 返回视图标题 | 返回 `I18nResourceBundle.i18nString("shell.title.main")` |

- 调用链：`onWindowCloseRequest → MessageBox.confirm → StageManager.exit`；`onSystemExit → settingStore.replace`；`onStageInitialize → stage.setSize/setLocation`

## SettingController
- 职责：应用设置业务，集中维护主题、字体、终端、同步、Redis/Zookeeper 等配置的展示、编辑与实时应用。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | settingTreeView | SettingLeftTreeView | 设置树组件（@FXML） |
  | shortcutKeyTableView | ShellShortcutKeyTableView | 快捷键组件（@FXML） |
  | mainPane | SettingMainPane | 主面板（@FXML） |
  | exitMode | FXToggleGroup | 退出方式选项组（@FXML） |
  | exitMode1 | RadioButton | 退出方式1：退出时询问（@FXML） |
  | exitMode2 | RadioButton | 退出方式2：直接退出（@FXML） |
  | pageSize | FXCheckBox | 记住页面大小（@FXML） |
  | pageLocation | FXCheckBox | 记住页面位置（@FXML） |
  | theme | ThemeComboBox | 主题（@FXML） |
  | bgColor | FXColorPicker | 背景色（@FXML） |
  | fgColor | FXColorPicker | 前景色（@FXML） |
  | accentColor | FXColorPicker | 强调色（@FXML） |
  | bgColorBox | FXHBox | 背景色容器（@FXML） |
  | fgColorBox | FXHBox | 前景色容器（@FXML） |
  | accentColorBox | FXHBox | 强调色容器（@FXML） |
  | fontSize | FontSizeComboBox | 字体大小（@FXML） |
  | fontWeight | FontWeightComboBox | 字体粗细（@FXML） |
  | fontFamily | FontFamilyTextField | 字体名称（@FXML） |
  | editorFontSize | FontSizeComboBox | 编辑器字体大小（@FXML） |
  | editorFontWeight | FontWeightComboBox | 编辑器字体粗细（@FXML） |
  | editorFontFamily | FontFamilyTextField | 编辑器字体名称（@FXML） |
  | terminalFontSize | FontSizeComboBox | 终端字体大小（@FXML） |
  | terminalFontWeight | FontWeightComboBox | 终端字体粗细（@FXML） |
  | terminalFontFamily | FontFamilyTextField | 终端字体名称（@FXML） |
  | locale | LocaleComboBox | 区域（@FXML） |
  | opacity | FXSlider | 窗口透明度（@FXML） |
  | hiddenLeftAfterConnected | FXToggleSwitch | 连接后收起左侧（@FXML） |
  | termType | ShellTemShellComboBox | 终端类型（@FXML） |
  | termBeep | FXToggleSwitch | 终端蜂鸣声（@FXML） |
  | termMaxLineCount | NumberTextField | 终端最大行数（@FXML） |
  | termCopyOnSelected | FXToggleSwitch | 终端选中时复制（@FXML） |
  | termFps | ShellTermFpsComboBox | 终端刷新率（@FXML） |
  | termCursorStyle | ShellTermCursorStyleComboBox | 终端光标样式（@FXML） |
  | termCursorBlinks | ShellTermCursorBlinkComboBox | 终端光标闪烁（@FXML） |
  | termUseAntialiasing | FXToggleSwitch | 终端使用抗锯齿（@FXML） |
  | termParseHyperlink | FXToggleSwitch | 终端解析超链接（@FXML） |
  | termBackgroundImage | ChooseFileTextField | 终端背景图片（@FXML） |
  | keyLoadLimit | NumberTextField | 键加载限制（Redis，@FXML） |
  | loadMode | FXToggleGroup | 节点加载方式选项组（Zookeeper，@FXML） |
  | loadMode0 / loadMode1 / loadMode2 | RadioButton | 节点加载方式0/1/2（Zookeeper，@FXML，用途相同） |
  | viewport | FXToggleGroup | 节点视图选项组（Zookeeper，@FXML） |
  | viewport0 / viewport1 | RadioButton | 节点视图0/1（Zookeeper，@FXML，用途相同） |
  | zkContentViewport | FXToggleGroup | 内容视图选项组（Zookeeper，@FXML） |
  | zkContentViewport0 / zkContentViewport1 | RadioButton | 内容视图0/1（Zookeeper，@FXML，用途相同） |
  | nodeLoadLimit | NumberTextField | 节点加载限制（Zookeeper，@FXML） |
  | setting | ShellSetting | 配置对象（final，`ShellSettingStore.SETTING`） |
  | settingStore | ShellSettingStore | 配置持久化对象（final，`ShellSettingStore.INSTANCE`） |
  | initReady | boolean | 是否初始化完毕（完毕后变更实时生效） |
  | syncType | ShellSyncTypeCombobox | 同步类型（@FXML） |
  | syncToken | PasswordTextField | 同步 token（@FXML） |
  | syncId | ClearableTextField | 同步 id（@FXML） |
  | syncKey | CheckBox | 同步密钥（@FXML） |
  | syncGroup | CheckBox | 同步分组（@FXML） |
  | syncSnippet | CheckBox | 同步片段（@FXML） |
  | syncConnect | CheckBox | 同步连接（@FXML） |
  | syncTime | FXLabel | 同步更新时间（@FXML） |
  | enableShortcutKey | FXToggleSwitch | 启用快捷键（@FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShowing(WindowEvent event)` | 窗口显示前回填控件值 | `super`；用 `setting` 回填退出方式、页面大小/位置、主题与三色（`StringUtil.emptyToDefault`）、字体（`selectSize`/`selectItem`/`selectWeight`）、区域、透明度、终端各项、Redis/ZK 各项与同步项，并置 `initReady=true`；异常 `MessageBox.exception` |
  | `private void saveSetting()` | 保存全部设置 | 读取控件值写入 `setting`（终端、字体、主题、区域、透明度、页面、Redis/ZK、同步 `applySync`）；`settingStore.update` 成功后 `closeWindow`、`I18nManager.apply`、`FontManager.apply`、`ThemeManager.apply`、`OpacityManager.apply`；`checkConfigForRestart` 返回非空且 `MessageBox.confirm` 时 `ShellProcessUtil.restartApplication()`；异常 `MessageBox.exception` |
  | `private String checkConfigForRestart(String locale)` | 检查是否需要重启 | 若 `setting.getLocale()` 与入参不一致返回 `I18nResourceBundle.i18nString("base.restartTip1")`，否则空串 |
  | `protected void bindListeners()` | 绑定监听 | `super`；颜色容器禁用绑定；主题/字体各选择控件变化时若 `initReady` 则 `applyAndSave()`；非系统主题时 `accentColorBox.enable()` |
  | `private void applyAndSave()` | 立即应用并保存 | 写入主题三色后 `ThemeManager.apply`，写入字体后 `settingStore.replace` 并 `FontManager.apply` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时构建设置树 | `mainPane.getLeftTreeView()` 添加基础/终端/窗口/快捷键/同步/Redis/ZK/字体（含子项）/主题/区域节点，`selectItem("ssh_box")`；Linux 下 `NodeGroupUtil.disappear(stage,"x11")`；`updateSyncInfo()`；`super`；`stage.hideOnEscape()` |
  | `private void resetFgColor()` | 重置前景色 | `theme.getValue().getForegroundColor()` |
  | `private void resetBgColor()` | 重置背景色 | `theme.getValue().getBackgroundColor()` |
  | `private void resetAccentColor()` | 重置强调色 | `theme.getValue().getAccentColor()` |
  | `private void resetLocale()` | 重置区域 | `locale.select((String) null)` |
  | `private void resetOpacity()` | 重置透明度 | `opacity.setValue(OpacityManager.defaultOpacity * 100)` |
  | `String getViewTitle()` | 返回视图标题 | `I18nHelper.settingTitle()` |
  | `private void resetFontFamily()` | 重置字体名称 | `AppSetting.defaultFontFamily()` |
  | `private void resetFontSize()` | 重置字体大小 | `AppSetting.defaultFontSize()` |
  | `private void resetFontWeight()` | 重置字体粗细 | `AppSetting.defaultFontWeight()` |
  | `private void resetEditorFontFamily()` | 重置编辑器字体 | `AppSetting.defaultEditorFontFamily()` |
  | `private void resetEditorFontSize()` | 重置编辑器字体大小 | `AppSetting.defaultEditorFontSize()` |
  | `private void resetEditorFontWeight()` | 重置编辑器字体粗细 | `AppSetting.defaultEditorFontWeight()` |
  | `private void resetTerminalFontFamily()` | 重置终端字体 | `AppSetting.defaultTerminalFontFamily()` |
  | `private void resetTerminalFontSize()` | 重置终端字体大小 | `AppSetting.defaultTerminalFontSize()` |
  | `private void resetTerminalFontWeight()` | 重置终端字体粗细 | `AppSetting.defaultTerminalFontWeight()` |
  | `private void testBashPath()` | 测试 bash 路径 | 取 `termType.getSelectedItem()`；Windows 下按 git-bash/git-sh/msys2-bash/cygwin-bash 判断 `FileUtil.exists`，否则 `RuntimeUtil.execForStr("where "+bash")`；非 Windows `RuntimeUtil.execForStr("which "+bash)`；结果经 `StringUtil` 判断后 `MessageBox.info/warn` |
  | `private void initSync()` | 初始化同步信息 | token 为空则 `requestFocus` 并 `MessageBox.warn(I18nHelper.pleaseInputContent())`；否则 `applySync()` 后 `settingStore.replace` |
  | `private void doSync()` | 执行同步 | `initSync()`；`StageManager.showMask` 内 `ShellSyncManager.doSync()` 并 `updateSyncInfo()`；异常 `JulLog.warn` 与 `MessageBox.exception` |
  | `private void clearSync()` | 清除同步 | `MessageBox.confirm(I18nHelper.clearSyncData())` 确认；`initSync()`；`StageManager.showMask` 内 `ShellSyncManager.clearSync()` 并 `updateSyncInfo()`；异常同上 |
  | `private void updateSyncInfo()` | 更新同步信息 | 用 `setting.getSyncTime()` 经 `DateHelper.formatDateTimeSimple` 写 `syncTime`；用 `setting.getSyncId()` 写 `syncId`，为空则 `clear` |
  | `private void applySync()` | 应用同步设置 | 写入 `syncId/syncToken/syncType/syncKey/syncGroup/syncSnippet/syncConnect` |
  | `void onStageInitialize(StageAdapter stage)` | 窗口初始化 | `super`；构造 `FileExtensionFilter` 并 `termBackgroundImage.setFilter(filter)` |

- 调用链：`saveSetting → applySync → settingStore.update → I18nManager/FontManager/ThemeManager/OpacityManager.apply → ShellProcessUtil.restartApplication`；`doSync → initSync → applySync → StageManager.showMask → ShellSyncManager.doSync`；`bindListeners → applyAndSave → ThemeManager.apply/FontManager.apply`

## ShellMainController
- 职责：shell 主页业务，管理连接面板与标签面板的分屏布局及标题刷新。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXSplitPane | 根节点/分屏组件（@FXML） |
  | connect | FXVBox | 左侧连接组件（@FXML） |
  | tabPane | ShellTabPane | shell 切换面板（@FXML） |
  | connectController | ConnectController | 连接控制器（@FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private void flushViewTitle(ShellConnect connect)` | 刷新窗口标题 | 连接非空时 `stage.appendTitle(" ("+connect.getName()+")")`，否则 `stage.restoreTitle()` |
  | `private void treeItemChanged(ShellTreeItemChangedEvent event)` | 树节点变化事件 | `EventSubscribe`；`event.data()` 为 `ShellConnectTreeItem` 时 `flushViewTitle(item.value())`，否则 `flushViewTitle(null)` |
  | `private void layout2(Layout2Event event)` | 显示左侧布局 | `EventSubscribe`；`connect.display()`；分屏未显示分隔条时 `root.setShowDivider(true)` 并按 `root.getPosition0(0.25)` 设置分隔位置 |
  | `private void layout1(Layout1Event event)` | 隐藏左侧布局 | `EventSubscribe`；`connect.disappear()`；分屏显示分隔条时 `root.recordPosition0()` 后 `setShowDivider(false)` 与 `setDividerPositions(0,1)` |
  | `List<SubStageController> getSubControllers()` | 返回子控制器 | 返回 `List.of(connectController)` |

- 调用链：`treeItemChanged → flushViewTitle → stage.appendTitle`；`layout2 → connect.display → root.setShowDivider`；`layout1 → connect.disappear → root.recordPosition0`

## ConnectController
- 职责：shell 连接业务，负责左侧连接树的选择、过滤、排序与文件拖拽导入。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tree | ShellConnectTreeView | 左侧 ssh 连接树（@FXML） |
  | sortPane | SortSVGPane | 节点排序组件（@FXML） |
  | filter | ClearableTextField | 连接过滤输入框（@FXML） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private void positionNode()` | 定位节点 | `tree.scrollTo(tree.getSelectedItem())` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏处理 | `super`；`KeyListener.unListenReleased(tree, KeyCode.F5)` 取消 F5 监听 |
  | `protected void bindListeners()` | 绑定监听 | `super`；`tree.selectItemChanged(ShellEventUtil::treeItemChanged)`；`stage.initDragFile(tree.getDragContent(), this::dragFile)`；`KeyListener.listenReleased(tree, KeyCode.F5, e -> tree.reload())` |
  | `private void dragFile(List<File> files)` | 拖拽文件处理 | `ShellEventUtil.fileDragged(files)` |
  | `private void sortTree()` | 树排序 | `sortPane.isAsc()` 时 `tree.sortAsc()` 并 `sortPane.desc()`，否则 `tree.sortDesc()` 并 `sortPane.asc()` |
  | `private void dataImport()` | 数据导入 | `ShellViewFactory.dataImport(null)` |
  | `private void dataExport()` | 数据导出 | `ShellViewFactory.dataExport()` |
  | `void onStageInitialize(StageAdapter stage)` | 窗口初始化 | `super`；`filter.addTextChangeListener` 中 `tree.setHighlight(t1)` 并 `tree.filter()` |

- 调用链：`bindListeners → stage.initDragFile → dragFile → ShellEventUtil.fileDragged`；`filter 变化 → tree.setHighlight → tree.filter`；`sortTree → tree.sortAsc/sortDesc`
