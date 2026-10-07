# controller/file 与 controller/tool 控制器代码审查

> 范围：`controller/file/**`（8 类）+ `controller/tool/**`（7 类，其中 1 个文件为整文件死代码）。

---

## ShellFileEditController

- 职责：远程文件编辑窗口，将远程文件下载到本地临时文件后在数据编辑器中打开编辑，支持格式选择、字体大小、高亮过滤与十六进制查看，并回传保存。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | file | ShellFile | 远程文件（`private`） |
  | destPath | String | 本地目标临时文件路径（`private`） |
  | client | ShellFileClient | 文件客户端（`private`） |
  | data | ShellDataEditor | 数据编辑器（`@FXML`） |
  | format | EditorFormatTypeComboBox | 编辑器格式（`@FXML`） |
  | fontSize | FontSizeComboBox | 字体大小（`@FXML`） |
  | filter | HighlightTextField | 高亮过滤输入（`@FXML`） |
  | setting | ShellSetting | shell 配置（`final`，取自 `ShellSettingStore.SETTING`） |
  | settingStore | ShellSettingStore | 设置存储（`final`，取自 `ShellSettingStore.INSTANCE`） |
  | hexTab | FXTab | 十六进制页签（`@FXML`） |
  | hexView | HexView | hex 组件（`@FXML`） |
  | statusLabel | HexStatusLabel | hex 状态标签（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void save()` | 保存编辑内容并回传远程 | `StageManager.showMask(...)`；`FileUtil.writeUtf8String(content, destPath)`；若 `client instanceof ShellMongoClient` 走 `mongoClient.reuploadBucketRecord(bucketFile.getDbName(), getBucketName(), getId(), fileName, localFile)`，否则 `client.put(destPath, file.getFilePath())`；`file.setFileSize(localFile.length())`、`file.setModifyTime(DateHelper.formatDateTime())`、`restoreTitle()` |
  | `void init()` | 下载远程文件到本地并加载到编辑器 | `StageManager.showMask(...)`；`FileUtil.touch(destPath)`、`client.get(file, destPath)`；依据 `FileNameUtil.extName` 与 `EditorFormatType.ofExtension` 调用 `data.showData(...)` 或 `data.showDetectData(...)`；`data.scrollToTop()` |
  | `String getData()` | 获取编辑器或文件数据 | `data.isEmpty()` 时 `FileUtil.readBytes(destPath)` 转字符串，否则 `data.getText()` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化文件、标题与字体配置 | `stage.hideOnEscape()`；`getProp("file")`、`getProp("client")`；`setTitle(...)`；`destPath = ShellFileUtil.getTempFile(file.getExtName())`；`fontSize.selectSize(setting.getEditorFontSize())`；调用 `init()` |
  | `void onWindowHiding(WindowEvent event)` | 窗口隐藏时清理本地临时文件 | `FileUtil.del(destPath)` |
  | `void bindListeners()` | 绑定编辑器/格式/字体/过滤/hex 监听 | 文本变化 `stage.restoreTitle()` 与 `appendTitle(" *")`；`data.formatTypeProperty()` 变化同步 `format.select`；`format.selectedItemChanged` 调 `data.setFormatType`；`fontSize.selectedItemChanged` 调 `data.setFontSize`、`setting.setEditorFontSize`、`settingStore.update`；`EditorUtil.bindHighlight(data, filter)`；`hexTab.selectedProperty()` 触发 `initHex()` |
  | `void onWindowCloseRequest(WindowEvent event)` | 关闭前检查未保存改动 | 若 `getViewTitle().endsWith(" *")` 且 `MessageBox.confirm(ShellI18nHelper.fileTip21())` 为否，则 `event.consume()`；否则 `super.onWindowCloseRequest(event)` |
  | `String getViewTitle()` | 返回窗口标题 | 返回 `I18nHelper.editFile()` |
  | `void searchNext()` | 高亮搜索下一个 | `EditorUtil.searchNextHighlight(data, filter)` |
  | `void onFilterKeyPressed(KeyEvent event)` | 过滤框回车触发搜索 | `event.getCode() == KeyCode.ENTER` 时调用 `searchNext()` |
  | `void onDataKeyPressed(KeyEvent event)` | 编辑器快捷键处理 | `KeyboardUtil.isCtrlS(event)` 时调用 `save()` |
  | `void initHex()` | 初始化十六进制视图 | `hexView.enable()`、`hexView.openFile(new File(destPath))`、`statusLabel.init(hexView)` |

- 调用链：`onWindowShown → ShellFileUtil.getTempFile → init → client.get`
- 调用链：`save → FileUtil.writeUtf8String → client.put / ShellMongoClient.reuploadBucketRecord`
- 调用链：`bindListeners → EditorUtil.bindHighlight → hexTab.selectedProperty → initHex`

## ShellFileErrorController

- 职责：文件任务错误信息窗口，只读展示 `ShellFileTask` 的错误消息。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | error | ReadOnlyTextArea | 错误信息只读文本域（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 显示任务错误信息 | `getProp("task")` 取 `ShellFileTask`；`error.setText(task.getErrorMsg())`；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | 返回 `I18nHelper.errorInfo()` |

- 调用链：`onWindowShown → getProp("task") → error.setText`

## ShellFileInfoController

- 职责：文件信息窗口，只读展示文件的分组、拥有者、名称、大小与权限。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | group | ReadOnlyTextField | 分组（`@FXML`） |
  | owner | ReadOnlyTextField | 拥有者（`@FXML`） |
  | name | ReadOnlyTextField | 名称（`@FXML`） |
  | size | ReadOnlyTextField | 大小（`@FXML`） |
  | permissions | ReadOnlyTextField | 权限（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 回填文件信息 | `getProp("file")` 取 `ShellFile`；设置 `group/owner/name/permissions`；目录时 `size.setText("-")`，否则 `NumberUtil.formatSize(file.getFileSize(), 2)`；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | 返回 `I18nHelper.fileInfo()` |

- 调用链：`onWindowShown → getProp("file") → NumberUtil.formatSize`

## ShellFileManageController

- 职责：文件上传、下载任务管理窗口，分页签展示上传与下载任务列表。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tabPane | FXTabPane | 根页签容器（`@FXML`） |
  | uploadTable | ShellFileUploadTaskTableView | 上传任务列表（`@FXML`） |
  | downloadTable | ShellFileDownloadTaskTableView | 下载任务列表（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void bindListeners()` | 绑定监听 | 仅调用 `super.bindListeners()`，无额外逻辑 |
  | `void onWindowShown(WindowEvent event)` | 装载上传/下载任务列表 | `getProp("client")` 取 `ShellFileClient`；`uploadTable.setItems(client.uploadTasks())`、`downloadTable.setItems(client.downloadTasks())`；上传为空且下载非空时 `tabPane.select(1)`；`super.onWindowShown(event)` |
  | `String getViewTitle()` | 返回窗口标题 | 返回 `ShellI18nHelper.fileTip17()` |

- 调用链：`onWindowShown → ShellFileClient.uploadTasks/downloadTasks → tabPane.select`

## ShellFilePermissionController

- 职责：文件权限修改窗口，通过九个勾选框设置属主/属组/其他用户的读写执行权限，可选递归应用到子目录。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | file | ShellFile | 远程文件（`private`） |
  | client | ShellFileClient | 文件客户端（`private`） |
  | ownerName | ReadOnlyTextField | 拥有者名称（`@FXML`） |
  | groupName | ReadOnlyTextField | 分组名称（`@FXML`） |
  | ownerR | FXCheckBox | 拥有者读取（`@FXML`） |
  | ownerW | FXCheckBox | 拥有者写入（`@FXML`） |
  | ownerE | FXCheckBox | 拥有者执行（`@FXML`） |
  | groupsR | FXCheckBox | 分组读取（`@FXML`） |
  | groupsW | FXCheckBox | 分组写入（`@FXML`） |
  | groupsE | FXCheckBox | 分组执行（`@FXML`） |
  | othersR | FXCheckBox | 其他读取（`@FXML`） |
  | othersW | FXCheckBox | 其他写入（`@FXML`） |
  | othersE | FXCheckBox | 其他执行（`@FXML`） |
  | includeSub | FXCheckBox | 包含子目录（`@FXML`） |
  | perms | FXLabel | 权限字符展示（`@FXML`） |
  | preview | FXLabel | 八进制权限预览（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void save()` | 保存权限并回传远程 | `StageManager.showMask(...)`；`getPerms()` → `ShellFileUtil.toPermissionInt(perms)`；勾选 `includeSub` 时 `client.chmodRecursive(permission, file)`，否则 `client.chmod(permission, file.getFilePath())`；成功则 `file.setPermissions(perms)`、`closeWindow()`，失败 `MessageBox.warn(I18nHelper.operationFail())` |
  | `void bindListeners()` | 九个勾选框变化时刷新权限预览 | 各 `ownerR/W/E`、`groupsR/W/E`、`othersR/W/E` 的 `selectedChanged` 绑定 `flushPerms()`；结尾 `super.bindListeners()` |
  | `void onWindowShown(WindowEvent event)` | 回填文件权限与属主属组 | `stage.switchOnTab()`、`stage.hideOnEscape()`；`getProp("file")`、`getProp("client")`；设置 `ownerName/groupName`；依据 `file.hasOwnerReadPermission()` 等九个方法设置勾选框；目录时 `includeSub.enable()`；`appendTitle("-"+file.getFileName())`、`flushPerms()` |
  | `String getPerms()` | 拼接九位 rwx 权限字符串 | 依次依据九个勾选框拼接 `r/w/x` 或 `-`（`private`） |
  | `void flushPerms()` | 刷新权限字符与八进制预览 | `getPerms()` → `ShellFileUtil.rwxToOctal(perms)`；`perms.setText`、`preview.setText(permission.substring(1))`（`private`） |
  | `String getViewTitle()` | 返回窗口标题 | 返回 `I18nHelper.filePermission()` |

- 调用链：`onWindowShown → file.hasOwnerReadPermission → ownerR.setSelected → flushPerms`
- 调用链：`save → getPerms → ShellFileUtil.toPermissionInt → client.chmodRecursive/chmod`
- 调用链：`bindListeners → ownerR.selectedChanged → flushPerms → ShellFileUtil.rwxToOctal`

## ShellFilePkgUploadController

- 职责：文件打包上传窗口，将待上传文件列表压缩为 tar.gz 后写回属性并关闭窗口。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | files | List<File> | 待打包文件列表（`private`） |
  | root | ReadOnlyTextArea | 打包进度信息只读文本域（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void init()` | 打印文件并异步压缩为 tar.gz | 生成临时文件名 `upload_pack_<uuid>.tar.gz`（`FileUtil.newTmpFile`）；遍历打印 `I18nHelper.compress()`；`TaskManager.startAsync(...)` 中 `CompressUtil.compress(files, compressFile.getPath(), CompressUtil.CompressType.TAR_GZ)`；成功 `setProp("compressFile", compressFile)`、`closeWindow()`，失败 `MessageBox.exception(ex)`、打印压缩失败（`private`） |
  | `void onWindowShown(WindowEvent event)` | 显示时取文件列表并启动打包 | `stage.hideOnEscape()`；`files = getProp("files")`；调用 `init()` |
  | `String getViewTitle()` | 返回窗口标题 | 返回 `I18nHelper.packageUpload()` |

- 调用链：`onWindowShown → getProp("files") → init → CompressUtil.compress → closeWindow`

## ShellFileTransportController

- 职责：文件传输窗口，选择来源与目标连接后进行 SFTP 目录浏览与文件互传，含隐藏文件切换、路径跳转、传输任务列表与过滤。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步（连接选择）容器（`@FXML`） |
  | step2 | FXSplitPane | 第二步（文件浏览）容器（`@FXML`） |
  | sourceInfoName | FXLabel | 来源信息名称（`@FXML`） |
  | targetInfoName | FXLabel | 目标信息名称（`@FXML`） |
  | sourceInfo | ShellConnectTextField | 来源连接（`@FXML`） |
  | targetInfo | ShellConnectTextField | 目标连接（`@FXML`） |
  | sourceHost | FXLabel | 来源主机（`@FXML`） |
  | targetHost | FXLabel | 目标主机（`@FXML`） |
  | sourceFile | ShellFileTransportFileTableView | 来源文件表格（`@FXML`） |
  | targetFile | ShellFileTransportFileTableView | 目标文件表格（`@FXML`） |
  | filterSourceFile | ClearableTextField | 过滤来源文件（`@FXML`） |
  | filterTargetFile | ClearableTextField | 过滤目标文件（`@FXML`） |
  | sourceLocation | ShellFileLocationTextField | 来源当前位置（`@FXML`） |
  | targetLocation | ShellFileLocationTextField | 目标当前位置（`@FXML`） |
  | hiddenSourcePane | HiddenSVGPane | 隐藏来源文件开关（`@FXML`） |
  | hiddenTargetPane | HiddenSVGPane | 隐藏目标文件开关（`@FXML`） |
  | fileBox | FXHBox | 文件组件盒子（`@FXML`） |
  | sourceClient | ShellFileClient | 来源客户端（`private`） |
  | targetClient | ShellFileClient | 目标客户端（`private`） |
  | transportTable | ShellFileTransportTaskTableView | 文件传输任务列表（`@FXML`） |
  | sFileName | TableColumn<ShellFile, ?> | 源文件名列（`@FXML`） |
  | tFileName | TableColumn<ShellFile, ?> | 目标文件名列（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doTransport1(List<ShellFile> files)` | 从来源传输到目标 | 校验 `CollectionUtil.isEmpty`；遍历文件检查 `targetFile.existFile(file.getFileName())`，存在则 `MessageBox.confirm(ShellI18nHelper.fileTip19())`；`doTransport(files, targetFile.getLocation(), sourceClient, targetClient)`（`private`） |
  | `void doTransport2(List<ShellFile> files)` | 从目标传输到来源 | 同上，检查 `sourceFile.existFile`，存在则确认；`doTransport(files, sourceFile.getLocation(), targetClient, sourceClient)`（`private`） |
  | `void doTransport(List<ShellFile> files, String remotePath, ShellFileClient sourceClient, ShellFileClient targetClient)` | 逐个文件执行传输 | 遍历 `files`，对 `file.isNormal()` 的项调用 `sourceClient.doTransport(remotePath, file, targetClient)`（`private`） |
  | `void bindListeners()` | 绑定连接选择、过滤与列图标 | `sourceInfo.selectedItemChanged` 更新 `sourceHost/sourceInfoName` 并关闭旧 `sourceClient`；`targetInfo` 同理；`filterSourceFile/filterTargetFile` 文本变化设 `setFilterText`；`sFileName/tFileName` 设 `IconTableCell<>(ShellFileUtil::getIcon)` |
  | `void onWindowShown(WindowEvent event)` | 显示时处理预置来源并隐藏 ESC | `stage.getProp("sourceConnect")` 非空则 `sourceInfo.selectItem(...)` 并 `disable()`；`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | 返回 `I18nHelper.transportFile()` |
  | `void showStep2()` | 校验连接并进入文件浏览步骤 | 校验 `sourceInfo/targetInfo.getSelectedItem()` 为空则 `ValidatorUtil.validFail`；`StageManager.showMask(...)` 内由 `ShellClientUtil.newClient` 创建客户端（`ShellSSHClient` 取 `sshClient.sftpClient()`），`start(2500)`、`isConnected()` 校验失败则 `MessageBox.warn`；成功后 `step1.disappear()`、`step2.display()`、`initFileTable()` |
  | `void initFileTable()` | 初始化两侧文件表格 | `sourceFile/targetFile.setClient`；`setFileCollectSupplier(ShellFileUtil.fileCollect(...))`；监听两侧 `transportTasks()` 变更刷新 `transportTable` 并在任务成功移除时 `onFileAdded`；监听 `locationProperty` 同步 `sourceLocation/targetLocation`；`setOnJumpLocation` 绑定 `cd`；`setTransportCallback(this::doTransport1/doTransport2)`；`loadFile()`（`protected`） |
  | `void refreshSourceFile()` | 刷新来源文件 | `sourceFile.loadFile()` |
  | `void refreshTargetFile()` | 刷新目标文件 | `targetFile.loadFile()` |
  | `void onWindowCloseRequest(WindowEvent event)` | 关闭前确认并取消任务、断开连接 | 若 `transportTable` 非空且 `MessageBox.confirm(ShellI18nHelper.fileTip18())` 为否则 `event.consume()`；否则 `transportTable.cancel()` 并关闭 `sourceClient/targetClient`，`super.onWindowCloseRequest(event)` |
  | `void hiddenSourceFile()` | 切换来源隐藏文件显示 | 依据 `hiddenSourcePane.isHidden()` 调用 `hidden()/show()`；`sourceFile.setShowHiddenFile(...)`；`setTipText(I18nHelper.showHiddenFiles()/doNotShowHiddenFiles())` |
  | `void hiddenTargetFile()` | 切换目标隐藏文件显示 | 同来源逻辑作用于 `hiddenTargetPane` 与 `targetFile` |
  | `void intoSourceHome()` | 进入来源主目录 | `sourceFile.intoHome()` |
  | `void returnSourceDir()` | 返回来源上级目录 | `sourceFile.returnDir()` |
  | `void intoTargetHome()` | 进入目标主目录 | `targetFile.intoHome()` |
  | `void returnTargetDir()` | 返回目标上级目录 | `targetFile.returnDir()` |

- 调用链：`showStep2 → ShellClientUtil.newClient → ShellSSHClient.sftpClient → initFileTable`
- 调用链：`sourceFile.setTransportCallback(doTransport1) → targetFile.existFile → doTransport → ShellFileClient.doTransport`
- 调用链：`onWindowCloseRequest → transportTable.cancel → sourceClient.close/targetClient.close`

## ShellFileViewController

- 职责：文件查看窗口，按类型（文本/图片/视频/音频/未知）下载远程文件到本地临时文件并渲染对应视图，文本类型支持编辑保存、格式与字体设置、高亮搜索与十六进制查看。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXVBox | 根节点（`@FXML`） |
  | file | ShellFile | 远程文件（`private`） |
  | destPath | String | 本地目标临时文件路径（`private`） |
  | client | ShellFileClient | 文件客户端（`private`） |
  | txt | ShellDataEditor | 文本编辑器（`@FXML`） |
  | filterBox | FXHBox | 过滤组件（`@FXML`） |
  | format | EditorFormatTypeComboBox | 编辑器格式（`@FXML`） |
  | fontSize | FontSizeComboBox | 字体大小（`@FXML`） |
  | filter | HighlightTextField | 高亮过滤输入（`@FXML`） |
  | img | FXImageView | 图片视图（`@FXML`） |
  | video | FXMediaView | 视频视图（`@FXML`） |
  | audio | FXMediaView | 音频视图（`@FXML`） |
  | music | MusicSVGGlyph | 音乐图标（`@FXML`） |
  | mediaControl | MediaControlBox | 媒体控制（`@FXML`） |
  | type | String | 文件类型（`private`） |
  | setting | ShellSetting | shell 配置（`final`，取自 `ShellSettingStore.SETTING`） |
  | settingStore | ShellSettingStore | 设置存储（`final`，取自 `ShellSettingStore.INSTANCE`） |
  | hexTab | FXTab | 十六进制页签（`@FXML`） |
  | hexView | HexView | hex 组件（`@FXML`） |
  | statusLabel | HexStatusLabel | hex 状态标签（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void save()` | 保存文本内容并回传远程 | `StageManager.showMask(...)`；`FileUtil.writeUtf8String(content, destPath)`；Mongo 客户端走 `reuploadBucketRecord(...)`，否则 `client.put(destPath, file.getFilePath())`；`file.setFileSize`、`file.setModifyTime(DateHelper.formatDateTime())`、`restoreTitle()` |
  | `void init()` | 下载远程文件并初始化视图 | `StageManager.showMask(...)`；`FileUtil.touch(destPath)`、`client.get(file, destPath)`、`initView()`（`private`） |
  | `String getData()` | 获取编辑器或文件数据 | `txt.isEmpty()` 时 `FileUtil.readBytes(destPath)` 转字符串，否则 `txt.getText()`（`private`） |
  | `void initView()` | 按类型初始化视图 | 文本/未知类型时绑定文本变化、`EditorUtil.bindHighlight`、格式/字体监听、`showData/showDetectData`、`showLineNum`、`scrollToTop`、`display`；图片 `img.setUrl(destPath)`、`display`、`layoutRoot`；视频 `video.setUrl`、`mediaControl.setup(...)`、`play`；音频绑定 `root` 宽高变化至 `layoutRoot`、`audio.setUrl`、`mediaControl.setup(...)`（`private`） |
  | `void bindListeners()` | 绑定 hex 页签监听 | `hexTab.selectedProperty()` 为真时调用 `initHex()`；结尾 `super.bindListeners()` |
  | `void onWindowShown(WindowEvent event)` | 显示时初始化文件与标题 | `type = getProp("type")`；`super.onWindowShown(event)`；`stage.hideOnEscape()`；`file = getProp("file")`、`client = getProp("client")`；`setTitle(...)`；`destPath = ShellFileUtil.getTempFile(file.getExtName())`；调用 `init()` |
  | `void onWindowCloseRequest(WindowEvent event)` | 关闭前检查未保存改动 | 若 `getViewTitle().endsWith(" *")` 且确认失败则 `event.consume()`；否则 `super.onWindowCloseRequest(event)` |
  | `void layoutRoot()` | 按类型调整根布局与窗口尺寸 | 图片类型 `FXUtil.runPulse` 依据 `img.getRealWidth()` 加 Windows/其它偏移后 `stage.setWidth`；视频类型 `FXUtil.runTimer` 依据 `video.getRealWidth()` 设宽；音频类型依据 `root.getRealWidth()/getRealHeight()` 计算 `music.setSize` 与 `VBox.setMargin`（`private`） |
  | `boolean isAudioType()` | 是否音频类型 | `"audio".equalsIgnoreCase(type)`（`private`） |
  | `boolean isVideoType()` | 是否视频类型 | `"video".equalsIgnoreCase(type)`（`private`） |
  | `boolean isImageType()` | 是否图片类型 | `"img".equalsIgnoreCase(type)`（`private`） |
  | `boolean isTxtType()` | 是否文本类型 | `"txt".equalsIgnoreCase(type)`（`private`） |
  | `boolean isUnknownType()` | 是否未知类型 | `"unknown".equalsIgnoreCase(type)`（`private`） |
  | `void onWindowHiding(WindowEvent event)` | 窗口隐藏时清理本地临时文件 | `FileUtil.del(destPath)` |
  | `String getViewTitle()` | 返回窗口标题 | 返回 `I18nHelper.view1File()` |
  | `void searchNext()` | 高亮搜索下一个 | `EditorUtil.searchNextHighlight(txt, filter)` |
  | `void onFilterKeyPressed(KeyEvent event)` | 过滤框回车触发搜索 | `event.getCode() == KeyCode.ENTER` 时调用 `searchNext()` |
  | `void onDataKeyPressed(KeyEvent event)` | 编辑器快捷键处理 | `KeyboardUtil.isCtrlS(event)` 时调用 `save()` |
  | `void initHex()` | 初始化十六进制视图 | `hexView.enable()`、`hexView.openFile(new File(destPath))`、`statusLabel.init(hexView)`（`private`） |
  | `void onStageInitialize(StageAdapter stage)` | 初始化阶段绑定过滤框可见性 | `super.onStageInitialize(stage)`；`filterBox.visibleProperty().bind(txt.visibleProperty())` |

- 调用链：`onWindowShown → ShellFileUtil.getTempFile → init → initView → layoutRoot`
- 调用链：`save → FileUtil.writeUtf8String → client.put / ShellMongoClient.reuploadBucketRecord`
- 调用链：`initView → mediaControl.setup → FXMediaView.play`

## ShellToolCacheTabController

- 职责：工具箱缓存页签，统计或清理程序缓存目录与日志目录。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | cacheArea | FXTextArea | 缓存操作输出文本域（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void calcCache()` | 统计缓存文件数量与总大小 | `StageManager.showMask(...)`；`LongAdder fileSize/fileCount`；`FileUtil.calcDir(cacheDir, fileCount, fileSize, callback, null)`，其中 `cacheDir = new File(ShellConst.getCachePath())`；`FileUtil.calcDir(logsDir, ...)`，其中 `logsDir = new File(JulUtil.getLogsDir())`；回调经 `FXUtil.runWait` 更新 `cacheArea` |
  | `void clearCache()` | 清理缓存并统计已删除文件 | 结构同上，改用 `FileUtil.clearDir(...)` 分别处理缓存与日志目录，回调经 `FXUtil.runWait` 更新 `cacheArea` |

- 调用链：`calcCache → FileUtil.calcDir → NumberUtil.formatSize → FXUtil.runWait`
- 调用链：`clearCache → FileUtil.clearDir(ShellConst.getCachePath/JulUtil.getLogsDir)`

## ShellToolController

- 职责：工具箱窗口容器，聚合缓存、telnet、端口扫描、网络扫描、zookeeper 各页签子控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | cacheTabController | ShellToolCacheTabController | 缓存页签（`@FXML`） |
  | telnetTabController | ShellToolTelnetTabController | telnet 页签（`@FXML`） |
  | portScanTabController | ShellToolPortScanTabController | 端口扫描页签（`@FXML`） |
  | networkScanTabController | ShellToolNetworkScanTabController | 网络扫描页签（`@FXML`） |
  | zookeeperTabController | ShellToolZookeeperTabController | zookeeper 页签（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 显示时切换页签并隐藏 ESC | `super.onWindowShown(event)`；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | 返回 `I18nHelper.tools()` |
  | `List<? extends StageController> getSubControllers()` | 返回子控制器列表 | `List.of(cacheTabController, telnetTabController, portScanTabController, networkScanTabController, zookeeperTabController)` |

- 调用链：`getSubControllers → List.of(cacheTabController, telnetTabController, portScanTabController, networkScanTabController, zookeeperTabController)`

## ShellToolNetworkScanTabController

- 职责：工具箱网络扫描页签，按网段与起止地址区间扫描指定主机是否开放多种常见服务端口。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | scanThread | Thread | 网络扫描线程（`private`） |
  | scanStopBtn | FXButton | 扫描停止按钮（`@FXML`） |
  | scanStartBtn | FXButton | 扫描开始按钮（`@FXML`） |
  | scanSegment1 | NumberTextField | 网段1（`@FXML`） |
  | scanSegment2 | NumberTextField | 网段2（`@FXML`） |
  | scanSegment3 | NumberTextField | 网段3（`@FXML`） |
  | scanStart | NumberTextField | 起始地址（`@FXML`） |
  | scanEnd | NumberTextField | 结束地址（`@FXML`） |
  | scanTable | ShellNetworkScanResultTableView | 扫描结果表格（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void execNetworkScan()` | 校验输入并逐 IP 并发扫描端口 | 依次 `scanSegment1/2/3/scanStart/scanEnd.validate()`；`scanStart.getIntValue() > scanEnd.getIntValue()` 时 `MessageBox.warn(I18nHelper.invalidData())`；置按钮状态、`scanTable.clearItems()`；`ThreadUtil.start(...)` 内对每个 IP 构造 `ShellNetworkScanResult`，组装多个 `NetworkUtil.reachable(host, <PORT>, 500)` 任务（SSH/FTP/VNC/RDP/HTTP/HTTPS/TELNET/RLOGIN/Mysql/Redis/Zookeeper/Oracle/MongoDB/PostgreSQL/Memcached/Elasticsearch/SQLServer/RTSP），`ThreadUtil.submit(tasks)`、`scanTable.addItem(result)`；`finally` 恢复按钮状态 |
  | `void stopNetworkScan()` | 停止网络扫描 | `ThreadUtil.interrupt(scanThread)`、`scanThread = null`、恢复按钮状态 |

- 调用链：`execNetworkScan → ThreadUtil.start → ThreadUtil.submit → NetworkUtil.reachable`
- 调用链：`stopNetworkScan → ThreadUtil.interrupt`

## ShellToolPortScanTabController

- 职责：工具箱端口扫描页签，对指定主机在起止端口范围内多线程扫描开放端口并展示结果。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | portScanMsg | FXLabel | 扫描进度提示（`@FXML`） |
  | portScanThread | NumberTextField | 扫描线程数（`@FXML`） |
  | scanThread | Thread | 扫描线程（`private`） |
  | portScanStopBtn | FXButton | 扫描停止按钮（`@FXML`） |
  | portScanStartBtn | FXButton | 扫描开始按钮（`@FXML`） |
  | portScanHost | ClearableTextField | 扫描地址（`@FXML`） |
  | portScanStartPort | PortTextField | 起始端口（`@FXML`） |
  | portScanEndPort | PortTextField | 结束端口（`@FXML`） |
  | portScanTable | ShellPortScanResultTableView | 扫描结果表格（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void execPortScan()` | 校验输入并多线程扫描端口 | `portScanHost.validate()`；`startPort >= endPort` 时 `MessageBox.warn(I18nHelper.invalid())`；置按钮状态、`portScanTable.clearItems()`；`NetworkUtil.scanMultiple(startPort, endPort, 500, portScanThread, host, callback, finishCallback)`；回调累加 `AtomicInteger totalCount/successCount`，成功时构造 `ShellPortScanResult`、`NetworkUtil.detectDesc(port)`、`portScanTable.addItem/doSort`，更新 `portScanMsg`；结束回调恢复按钮状态 |
  | `void stopPortScan()` | 停止端口扫描 | `ThreadUtil.interrupt(scanThread)`、`scanThread = null`、恢复按钮状态 |

- 调用链：`execPortScan → NetworkUtil.scanMultiple → NetworkUtil.detectDesc → portScanTable.addItem`

## ShellToolTelnetTabController

- 职责：工具箱 telnet 页签，连接目标主机端口并读取输出显示在文本域。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | telnetHost | ClearableTextField | telnet 地址（`@FXML`） |
  | telnetPort | PortTextField | telnet 端口（`@FXML`） |
  | telnetTimeout | NumberTextField | telnet 超时（`@FXML`） |
  | telnetArea | FXTextArea | telnet 输出文本域（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void execTelnet()` | 连接并读取 telnet 输出 | `telnetHost.validate()`；`StageManager.showMask(...)`；`new TelnetClient()`、`setConnectTimeout(timeout*1000)`、`client.connect(host, port)`；`isConnected()` 时起线程经 `InputStreamReader` 循环读入 `telnetArea.appendText`，用 `DownLatch.await(timeout, SECONDS)` 控制超时并 `client.disconnect()`；失败则 `client.disconnect()` 与 `MessageBox.warn(I18nHelper.connectFail())` |

- 调用链：`execTelnet → TelnetClient.connect → InputStreamReader.read → telnetArea.appendText`

## ShellToolX11TabController

- 说明：整文件为注释掉的死代码，类声明与继承 `SubStageController` 均被注释，无任何有效字段与方法，跳过。

## ShellToolZookeeperTabController

- 职责：工具箱 zookeeper 页签，依据用户名密码生成 zookeeper digest 摘要并支持复制。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | user | ClearableTextField | 用户（`@FXML`） |
  | pwd | ClearableTextField | 密码（`@FXML`） |
  | digest | TextField | 生成的摘要（`@FXML`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void genDigest()` | 生成摘要 | `StringUtil.isBlank(user.getText())` 时 `ValidatorUtil.validFail(user)`；`pwd` 同理；否则 `ShellZKAuthUtil.digest(user.getText(), pwd.getText())` 后 `digest.setText` |
  | `void copyDigest()` | 复制摘要 | `digest.copy()`、`MessageBox.info(I18nHelper.copySuccess())` |
  | `void bindListeners()` | 绑定用户/密码监听 | `user.addTextChangeListener` 中若输入含 `:` 则拆分为用户名与密码并回填，否则 `digest.clear()`；`pwd.addTextChangeListener` 中 `digest.clear()`；结尾 `super.bindListeners()` |

- 调用链：`genDigest → ShellZKAuthUtil.digest → digest.setText`
- 调用链：`bindListeners → user.addTextChangeListener → pwd.setText / digest.clear`
