# controller/zk 与 controller/s3 控制器代码审查

> 范围：`controller/zk/**`（11 类，其中 2 个文件为整文件死代码）+ `controller/s3/**`（3 类）。

---

## ShellZKAddACLController

- 职责：Zookeeper 节点权限（ACL）新增业务，支持 world/digest/IP 单条与多 IP 批量权限的创建，并可保存摘要认证信息。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | zkItem | ShellZKNodeTreeItem | zk 树节点 |
  | zkClient | ShellZKClient | zk 客户端 |
  | perms | FXHBox | 权限勾选组件 |
  | nodePath | TextField | 节点路径 |
  | permsBox | FXVBox | 权限组件容器 |
  | aclType | FXComboBox<String> | 权限类型下拉（0 world、1 digest1、2 digest2、3 digest3、4 单 IP、5 多 IP） |
  | digest1ACL | VBox | digest 权限处理 1（用户名+密码） |
  | digest2ACL | VBox | digest 权限处理 2（user:digest 文本） |
  | digest3ACL | VBox | digest 权限处理 3（已有账密下拉） |
  | ip1ACL | VBox | 单 IP 权限处理 |
  | ip2ACL | VBox | 多 IP 权限处理 |
  | digestInfo1User | ClearableTextField | 摘要信息 1，用户名 |
  | digestInfo1Password | ClearableTextField | 摘要信息 1，密码明文 |
  | digestInfo2 | ClearableTextField | 摘要信息 2，user:digest |
  | digestInfo3 | ShellZKAuthComboBox | 摘要信息 3，已有账密 |
  | digestText | Label | 摘要信息展示 |
  | copyDigestText | CopySVGGlyph | 复制摘要信息图标 |
  | digestSaveInfo | FXCheckBox | 摘要保存勾选 |
  | ipContent1 | ClearableTextField | IP 单 IP 内容 |
  | ipContent2 | FXTextArea | IP 多 IP 内容 |
  | mutexes | NodeMutexes | 节点互斥器（`final`） |
  | authStore | ShellZKAuthStore | 认证信息储存（`final`，`ShellZKAuthStore.INSTANCE`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void copyDigestText()` | 复制摘要信息 | `digestText.getText()` → `ClipboardUtil.setStringAndTip(data)` |
  | `void segmentModel()` | 将单 IP 自动补全为 `/16` 网段 | 取 `ipContent1.getTextTrim()`，非 `/16` 结尾则补 `.0`、`replace("..",".")`，`setText(ip+"/16")` |
  | `void addACL()` | 添加权限入口，按类型分发 | `aclType.validate()`、`getPerms()` 校验，按 `aclType.getSelectedIndex()` 分派 `addWorldACL/addDigestACL1/2/3/addIPACL1/2`，异常 `MessageBox.exception(ex)` |
  | `void addDigestACL1()` | 摘要权限 1（用户名+密码） | 校验 user/password 非空，`ShellZKAuthUtil.digest(user,password)`，`zkItem.existDigestACL(digest)` 去重，构造 `ACL`+`Id("digest")`，`getPerms()`→`ShellZKACLUtil.toPermInt`，成功后按 `digestSaveInfo` 保存 `new ShellZKAuth(...)` → `authStore.replace` |
  | `void addDigestACL2()` | 摘要权限 2（user:digest 文本） | 按 `:` 切分为 2 段校验，password 长度须 ≥28，去重后构造 `ACL` 调用 `addACL(acl)` |
  | `void addDigestACL3()` | 摘要权限 3（已有账密选择） | 取 `digestInfo3.getValue()`（`ShellZKAuth`），`zkAuth.digest()`，去重后构造 `ACL` → `addACL(acl)` |
  | `void addWorldACL()` | world 权限 | `zkItem.hasWorldACL()` 已存在则告警；否则 `Id("world","anyone")` 构造 `ACL` → `addACL(acl)` |
  | `void addIPACL1()` | 单 IP 权限 | `ShellZKACLUtil.checkIP(ip)`，构造 `ShellZKACL`（`Id("ip")`），`zkItem.existIPACL(acl.idVal())` 去重后 `addACL(acl)` |
  | `void addIPACL2()` | 多 IP 权限批量 | `ipContent2` 按 `;` 分行、`:` 切分 ip:perms，逐条 `checkIP` 与去重，累积 `aclList` 后 `addACL(aclList)` |
  | `boolean addACL(ACL acl)` | 单条权限新增重载 | `addACL(List.of(acl))` |
  | `boolean addACL(List<ACL> list)` | 提交权限并关闭窗口 | `zkClient.addACL(zkItem.nodePath(), list)`，`stat != null` 则 `setProp("result", true)`、`closeWindow()` |
  | `void bindListeners()` | 绑定互斥与监听 | `mutexes.addNodes(...)`+`manageBindVisible()`；`aclType.selectedIndexChanged` 切换可见性；摘要文本监听实时生成 `digestText`；`digestInfo1User` 含 `:` 时自动拆分用户名密码 |
  | `void onWindowShown(WindowEvent event)` | 初始化 zkItem/zkClient 与摘要下拉 | `getProp("zkItem")`、`getProp("zkClient")`、`initDigestData()`；已有 world 权限则 `aclType.select(1)`；`nodePath.setText(zkItem.decodeNodePath())`、`stage.hideOnEscape()` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时释放互斥器 | `mutexes.destroy()` |
  | `void initDigestData()` | 初始化 digest 下拉 | `digestInfo3.init(zkClient.iid())` |
  | `String getPerms()` | 汇总勾选的权限字母 | 读取 `perms` 5 个 `CheckBox`，拼接 `a/w/r/d/c` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.addACL()` |

- 调用链：`addACL → addDigestACL1 → ShellZKAuthUtil.digest → addACL(List) → ShellZKClient.addACL`
- 调用链：`addACL → addIPACL2 → ShellZKACLUtil.checkIP → ShellZKNodeTreeItem.existIPACL → addACL(List)`
- 调用链：`onWindowShown → initDigestData → ShellZKAuthComboBox.init`

## ShellZKUpdateACLController

- 职责：Zookeeper 节点权限（ACL）修改业务，回填已有权限并提交单个 ACL 的权限变更。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | acl | ShellZKACL | 待修改的 zk 权限信息 |
  | zkItem | ShellZKNodeTreeItem | zk 树节点 |
  | zkClient | ShellZKClient | zk 客户端 |
  | perms | FXHBox | 权限勾选组件 |
  | nodePath | TextField | 节点路径 |
  | aclType | TextField | 权限类型（只读展示） |
  | digest | TextField | 摘要权限内容 |
  | ip | TextField | ip 权限内容 |
  | ipACL | VBox | ip 权限控件 |
  | digestACL | VBox | 摘要权限控件 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void updateACL()` | 提交权限修改 | `getPerms()` 非空校验，`zkClient.getACL(zkItem.nodePath())` 取列表，遍历匹配 `acl.equals(this.acl)` 后 `acl.setPerms(ShellZKACLUtil.toPermInt(perms))` → `zkClient.setACL(...)`，成功 `setProp("result", true)`、`closeWindow()` |
  | `void onWindowShown(WindowEvent event)` | 回填权限与类型 | `getProp("acl"/"zkItem"/"zkClient")`；按 `acl.hasAdminPerm/hasWritePerm/...` 勾选 5 个 `CheckBox`；`acl.isIPACL()`/`isDigestACL()` 控制 `ipACL`/`digestACL` 可见性与文本；`aclType.setText(acl.schemeFriend().friendlyValue()+"("+value().toUpperCase()+")")`；`nodePath.setText(zkItem.decodeNodePath())`、`stage.hideOnEscape()` |
  | `String getPerms()` | 汇总勾选的权限字母 | 读取 `perms` 5 个 `CheckBox`，拼接 `a/w/r/d/c` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.updateACL()` |

- 调用链：`updateACL → ShellZKClient.getACL → ShellZKACLUtil.toPermInt → ShellZKClient.setACL`
- 调用链：`onWindowShown → ShellZKACL.schemeFriend → TextField.setText`

## ShellZKAddAuthController

- 职责：Zookeeper 认证信息（用户名/密码）新增业务，校验并持久化认证记录。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | user | ClearableTextField | 用户名 |
  | password | ClearableTextField | 密码 |
  | status | FXToggleSwitch | 启用状态 |
  | connect | ShellConnect | 连接 |
  | authStore | ShellZKAuthStore | 认证储存（`final`，`ShellZKAuthStore.INSTANCE`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void addAuth()` | 新增认证信息 | 校验 user/password 非空；`authStore.exist(connect.getId(), user, password)` 与 `authStore.exist(user, password, iid)` 双重去重；构造 `ShellZKAuth(iid,user,password)`、`setEnable(status.isSelected())`，`authStore.replace(auth)` 成功后 `setProp("auth", auth)`、`closeWindow()` |
  | `void onWindowShown(WindowEvent event)` | 初始化连接 | `getProp("connect")`、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.addAuth()` |

- 调用链：`addAuth → ShellZKAuthStore.exist → ShellZKAuthStore.replace`
- 调用链：`onWindowShown → StageController.getProp`

## ShellZKExportDataController

- 职责：Zookeeper 数据导出业务，分步骤选择文件/格式/字符集并执行导出，支持中断、进度展示。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步面板 |
  | step2 | FXVBox | 第二步面板 |
  | step3 | FXVBox | 第三步面板 |
  | exportFile | File | 导出文件 |
  | format | FXToggleGroup | 文件格式 |
  | prefix | FXToggleGroup | 前缀选项 |
  | fileName | FXText | 文件名 |
  | nodePath | FXText | 节点路径 |
  | charset | CharsetComboBox | 字符集 |
  | selectFile | FXButton | 选择文件按钮 |
  | includeTitle | FXCheckBox | 包含标题 |
  | includeACL | FXCheckBox | 包含 acl |
  | compress | FXCheckBox | 压缩 |
  | stopExportBtn | FXButton | 结束导出按钮 |
  | exportStatus | FXLabel | 导出状态 |
  | exportMsg | MsgTextArea | 导出消息 |
  | exportPath | String | 导出路径 |
  | connect | ShellConnect | 当前 zk 对象 |
  | client | ShellZKClient | 当前 zk 客户端 |
  | execTask | Thread | 导出操作任务 |
  | counter | Counter | 计数器（`final`） |
  | exportHandler | ShellZKDataExportHandler | 导出处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doExport()` | 执行导出 | 重置 `counter`/`exportMsg`/`exportStatus`，`NodeGroupUtil.disable(stage,"exec")`；初始化或 `interrupt(false)` 复用 `ShellZKDataExportHandler`，注入 client/nodePath/filePath/charset/includeACL/includeTitle/compress/prefix；`ThreadUtil.start` 中 `exportHandler.doExport()`，异常按 `InterruptedException` 区分取消/失败，`finally` 恢复按钮与标题 |
  | `void stopExport()` | 结束导出 | `ThreadUtil.interrupt(execTask)`、`execTask=null`、`exportHandler.interrupt()` |
  | `void bindListeners()` | 绑定监听 | `stage.hideOnEscape()`；`format.selectedToggleProperty` 变更时清空 `exportFile` 与 `fileName` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时停止导出 | `stopExport()` |
  | `void updateStatus(String extraMsg)` | 更新导出状态 | `counter.setExtraMsg(extraMsg)` → `exportStatus.text(counter.unknownFormat())` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.exportTitle()` |
  | `void showStep1()` | 显示第一步 | `step2/step3.disappear()`、`step1.display()` |
  | `void showStep2()` | 显示第二步并按格式启停控件 | 依 `format.selectedUserData()`：`FileNameUtil.isTxtType` 控制 `txt` 组；`xls/xlsx/csv` 启用 `includeTitle`；`xml/json` 启用 `compress` |
  | `void showStep3()` | 显示第三步，必要时建立连接 | 校验 `exportFile`（`ValidatorUtil.validFail(selectFile)`）；`client` 为空/关闭时经 `DownLatch` + `ThreadUtil.start` 生成 `ShellZKClientUtil.newClient(connect)` 并 `start(2500)`，`await(3000)` 失败则告警 |
  | `void selectFile()` | 选择导出文件 | `FXChooser.extensionFilter(fileType)`，文件名 `Zookeeper-<connect.name>-<exportData>.<fileType>`，`FileChooserHelper.save`，存在则 `FileUtil.del`，写 `fileName` |
  | `void onWindowShown(WindowEvent event)` | 初始化参数 | `getProp("connect")`、`getProp("nodePath")`（默认 `/`）、`nodePath.setText(exportPath)` |

- 调用链：`doExport → ShellZKDataExportHandler.doExport → setMessageHandler/setProcessedHandler`
- 调用链：`showStep3 → ShellZKClientUtil.newClient → ShellZKClient.start → isConnected`
- 调用链：`selectFile → FileChooserHelper.save → FileUtil.del`

## ShellZKImportDataController

- 职责：Zookeeper 数据导入业务，分步骤选择文件/格式并执行导入，支持忽略已存在、数据行起始、中断与进度展示。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步面板 |
  | step2 | FXVBox | 第二步面板 |
  | step3 | FXVBox | 第三步面板 |
  | importFile | File | 导入文件 |
  | includeACL | FXCheckBox | 包含 acl |
  | format | FXToggleGroup | 文件格式 |
  | fileName | FXText | 文件名 |
  | charset | CharsetComboBox | 字符集 |
  | ignoreExist | FXCheckBox | 存在时忽略 |
  | dataRowStarts | NumberTextField | 数据行开始 |
  | stopImportBtn | FXButton | 结束导入按钮 |
  | importStatus | FXLabel | 导入状态 |
  | importMsg | MsgTextArea | 导入消息 |
  | connect | ShellConnect | 当前 zk 对象 |
  | client | ShellZKClient | 当前 zk 客户端 |
  | execTask | Thread | 导入操作任务 |
  | counter | Counter | 计数器（`final`） |
  | importHandler | ShellZKDataImportHandler | 导入处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doImport()` | 执行导入 | 重置 `counter`/`importMsg`/`importStatus`，`NodeGroupUtil.disable(stage,"exec")`；初始化或 `interrupt(false)` 复用 `ShellZKDataImportHandler`，注入 client/includeACL/ignoreExist/filePath/charset/dataRowStarts；`ThreadUtil.start` 中 `importHandler.doImport()`，异常区分取消/失败，`finally` 恢复控件 |
  | `void stopImport()` | 结束导入 | `ThreadUtil.interrupt(execTask)`、`execTask=null`、`importHandler.interrupt()` |
  | `void bindListeners()` | 绑定监听 | `stage.hideOnEscape()`；`format.selectedToggleProperty` 变更时清空 `importFile` 与 `fileName` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时停止导入 | `stopImport()` |
  | `void updateStatus(String extraMsg)` | 更新导入状态 | `counter.setExtraMsg(extraMsg)` → `importStatus.setText(counter.unknownFormat())` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.importTitle()` |
  | `void showStep1()` | 显示第一步 | `step2/step3.disappear()`、`step1.display()` |
  | `void showStep2()` | 显示第二步 | 依 `format.selectedUserData()`：`excel/csv` 启用 `dataRowStarts`，否则禁用 |
  | `void showStep3()` | 显示第三步 | `doConnect()` 成功后隐藏 step1/step2、`step3.display()` |
  | `boolean doConnect()` | 建立/k 校验 zk 连接 | `client` 为空/关闭时 `DownLatch`+`ThreadUtil.start` 生成 `ShellZKClientUtil.newClient(connect)` 并 `start(2500)`，`await(3000)` 失败告警返回 false |
  | `void selectFile()` | 选择导入文件 | `FXChooser.extensionFilter(fileType)`，`FileChooserHelper.choose(pleaseSelectFile, filter)`，写 `fileName` |
  | `void onWindowShown(WindowEvent event)` | 初始化连接 | `getProp("connect")` |

- 调用链：`doImport → ShellZKDataImportHandler.doImport → setMessageHandler/setProcessedHandler`
- 调用链：`showStep3 → doConnect → ShellZKClientUtil.newClient → ShellZKClient.start`
- 调用链：`selectFile → FileChooserHelper.choose → FXText.setText`

## ShellZKTransportDataController

- 职责：Zookeeper 数据（来源→目标）传输业务，分步骤选择来源/目标连接与字符集并执行传输。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步面板 |
  | step2 | FXVBox | 第二步面板 |
  | step3 | FXVBox | 第三步面板 |
  | sourceInfoName | FXLabel | 来源信息名称 |
  | targetInfoName | FXLabel | 目标信息名称 |
  | sourceInfo | ShellConnectTextField | 来源信息（连接选择） |
  | sourceCharset | CharsetComboBox | 来源字符集 |
  | sourceCharsetName | FXLabel | 来源字符集名称 |
  | targetInfo | ShellConnectTextField | 目标信息（连接选择） |
  | targetCharset | CharsetComboBox | 目标字符集 |
  | targetCharsetName | FXLabel | 目标字符集名称 |
  | sourceHost | FXLabel | 来源主机 |
  | targetHost | FXLabel | 目标主机 |
  | sourceClient | ShellZKClient | 来源客户端 |
  | targetClient | ShellZKClient | 目标客户端 |
  | stopTransportBtn | FXButton | 结束传输按钮 |
  | transportStatus | FXLabel | 传输状态 |
  | transportMsg | MsgTextArea | 传输消息 |
  | existsPolicy | FXToggleGroup | 节点存在时处理策略 |
  | execTask | Thread | 传输操作任务 |
  | counter | Counter | 计数器（`final`） |
  | transportHandler | ShellZKDataTransportHandler | 传输处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doTransport()` | 执行传输 | 重置 `counter`/`transportMsg`/`transportStatus`；初始化或 `interrupt(false)` 复用 `ShellZKDataTransportHandler`，注入 source/targetClient、source/targetCharset、existsPolicy；`NodeGroupUtil.disable(stage,"exec")` 后 `ThreadUtil.start` 中 `doTransport()`，异常区分取消/失败，`finally` 恢复控件并 `SystemUtil.gcLater()` |
  | `void stopTransport()` | 结束传输 | `ThreadUtil.interrupt(execTask)`、`execTask=null`、`transportHandler.interrupt()` |
  | `void bindListeners()` | 绑定来源/目标/字符集监听 | `sourceInfo/targetInfo.selectedItemChanged` 回填 host/name 并关闭旧 client；`sourceCharset/targetCharset.selectedItemChanged` 回填字符集名称 |
  | `void onWindowShown(WindowEvent event)` | 初始化，锁定来源连接 | `stage.getProp("sourceConnect")` 非空则 `sourceInfo.selectItem(...)` 并 `disable()`；`stage.hideOnEscape()` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时停止传输 | `stopTransport()` |
  | `void updateStatus(String extraMsg)` | 更新传输状态 | `counter.setExtraMsg(extraMsg)` → `transportStatus.text(counter.unknownFormat())` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.transportTitle()` |
  | `void showStep1()` | 显示第一步 | `step2.disappear()`、`step1.display()` |
  | `void showStep2()` | 显示第二步并建立双向连接 | 校验来源/目标连接非空（`ValidatorUtil.validFail`）、`sourceInfo.compare(targetInfo)` 禁止相同；经 `DownLatch`+`ThreadUtil.start` 分别 `ShellZKClientUtil.newClient` 并 `start(2500)`，失败告警返回 |
  | `void showStep3()` | 显示第三步 | `step2.disappear()`、`step3.display()` |

- 调用链：`doTransport → ShellZKDataTransportHandler.doTransport → setSourceClient/setTargetClient`
- 调用链：`showStep2 → ShellZKClientUtil.newClient → ShellZKClient.start → isConnected`
- 调用链：`bindListeners → ShellConnectTextField.selectedItemChanged → ShellZKClient.close`

## ShellZKHistoryDataController

- 职责：Zookeeper 节点数据历史业务，列表展示历史数据并在编辑器内查看对应内容。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | listTable | ShellZKHistoryDataTableView | 数据历史列表 |
  | editor | ShellDataEditor | 数据编辑器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void bindListeners()` | 绑定列表选中监听 | `listTable.selectedItemChanged` 触发 `showData(newValue)` |
  | `void showData(ShellZKHistoryData data)` | 显示历史数据 | `data == null` 时 `editor.clear()/disable()`；否则 `StageManager.showMask` 内 `ShellZKDataUtil.getHistory(listTable.getNodePath(), data.getSaveTime(), listTable.getClient())` → `editor.showData(bytes)/enable()` |
  | `void onWindowShown(WindowEvent event)` | 初始化列表 | `getProp("client")`、`getProp("nodePath")`，`listTable.init(client, nodePath)`、`appendTitle("["+nodePath+"]")`、`stage.hideOnEscape()` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏回调 | 仅调用 `super.onWindowHidden(event)` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.dataHistory()` |
  | `void destroy()` | 销毁时释放编辑器 | `editor.destroy()` → `super.destroy()` |

- 调用链：`bindListeners → showData → ShellZKDataUtil.getHistory → ShellDataEditor.showData`
- 调用链：`onWindowShown → ShellZKHistoryDataTableView.init`

## ShellZKAddNodeController

- 职责：Zookeeper 节点添加业务，填写路径/数据/权限与创建模式后创建节点。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | aclTab | FXTab | 权限 tab |
  | dataTab | FXTab | 数据 tab |
  | nodePath | ClearableTextField | 节点路径 |
  | nodeData | Editor | 节点数据编辑器 |
  | nodePathPreview | TextField | 节点路径预览 |
  | parentNodeBox | VBox | 父节点值组件 |
  | parentNode | TextField | 父节点值 |
  | createMode | FXComboBox<String> | 创建模式下拉 |
  | zkClient | ShellZKClient | zk 客户端 |
  | nodePathText | String | 拼接后的节点路径数据 |
  | aclType | FXComboBox<String> | 权限类型下拉（0 开放、1 摘要、2 ip） |
  | perms | FXHBox | 权限勾选组件 |
  | ipACL | FXVBox | ip 权限 |
  | digestACL | FXVBox | 摘要权限 |
  | digestText | FXLabel | 摘要权限内容 |
  | ipContent | ClearableTextField | ip 权限内容 |
  | digestUser | ClearableTextField | 摘要权限用户名 |
  | digestPassword | ClearableTextField | 摘要权限密码 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void addNode()` | 添加节点 | `nodePath.validate()` 与 `getACL()` 校验；`CreateMode.fromFlag(createMode.getSelectedIndex())`，`zkClient.create(nodePathText, data.getBytes(), List.of(acl), null, createMode, true)`，成功 `setProp("addedNodePath", nodePathText)`、`closeWindow()` |
  | `void pasteData()` | 粘贴数据 | `nodeData.paste()`、`nodeData.requestFocus()` |
  | `void clearData()` | 清空数据 | `nodeData.clear()`、`nodeData.requestFocus()` |
  | `void parseToJson()` | 文本/JSON 互相格式化 | 依 `nodeData.getUserData()` 与内容调用 `JSONUtil.toJson`/`JSONUtil.toPretty` 并切换 userData |
  | `ACL getACL()` | 依据勾选与类型构造 ACL | 拼接 5 个 `CheckBox` 权限字母，`ShellZKACLUtil.toPermInt`；类型 0 `Id("world","anyone")`、类型 1 经 `ShellZKAuthUtil.digest` 生成 `Id("digest")`、类型 2 经 `ShellZKACLUtil.checkIP` 生成 `Id("ip")` |
  | `void bindListeners()` | 绑定监听 | `nodePath.addTextChangeListener` 经 `ShellZKNodeUtil.concatPath` 生成 `nodePathText` 并更新预览；`aclType.selectedIndexChanged` 切换 ip/digest 权限可见性；摘要 user/password 变化实时生成 `digestText` |
  | `void onWindowShown(WindowEvent event)` | 初始化父节点与客户端 | `parentNodeBox.managedProperty().bind(...)`、`getProp("zkItem"/"zkClient")`，设置父节点可见性与文本；`nodePath.requestFocus()`、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.addNode()` |
  | `void destroy()` | 销毁时释放编辑器 | `nodeData.destroy()` → `super.destroy()` |

- 调用链：`addNode → getACL → ShellZKACLUtil.toPermInt → ShellZKClient.create`
- 调用链：`bindListeners → ShellZKNodeUtil.concatPath → nodePathPreview.setText`
- 调用链：`parseToJson → JSONUtil.toJson/toPretty → Editor.setText`

## ShellZKAuthNodeController

- 职责：Zookeeper 节点认证业务，支持用户名密码认证与已有认证列表认证两种方式。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | user | ClearableTextField | 用户名 |
  | password | ClearableTextField | 密码 |
  | nodePath | TextField | 节点路径 |
  | saveInfo1 | FXCheckBox | 保存信息勾选 |
  | zkNode | ShellZKNode | zk 节点 |
  | zkItem | ShellZKNodeTreeItem | zk 树节点 |
  | authType | FXComboBox<String> | 认证方式下拉 |
  | authType1 | FXVBox | 认证方式 1（用户名密码） |
  | authType2 | FXVBox | 认证方式 2（认证列表） |
  | authList | ShellZKAuthComboBox | 认证信息列表 |
  | authStore | ShellZKAuthStore | 认证储存（`final`，`ShellZKAuthStore.INSTANCE`） |
  | mutexes | NodeMutexes | 节点互斥器（`final`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void auth()` | 认证入口，按可见方式取账密 | 若 `authType1.isVisible()` 取 user/password 并校验非空；否则 `authType2.isVisible()` 取 `authList.getValue()`；最终调用 `auth(user, password)` |
  | `void auth(String user, String password)` | 认证节点 | `zkItem.client()` 取客户端，`ShellZKAuthUtil.authNode(user, password, zkClient, zkNode)`；结果 1 时构造 `ShellZKAuth`，`authType1` 下按 `saveInfo1` 执行 `authStore.replace`，`setProp("auth"/"success")`、`MessageBox.okToast`、`closeWindow()`；否则依 `zkNode.aclEmpty()/hasDigestACL()` 提示不同失败信息 |
  | `void bindListeners()` | 绑定监听 | `authType.selectedIndexChanged` 经 `mutexes.visible` 切换 authType1/authType2；`user.addTextChangeListener` 含 `:` 时自动拆分用户名密码 |
  | `void onWindowShown(WindowEvent event)` | 初始化节点与认证列表 | `getProp("zkItem")`、`zkNode=zkItem.value()`、`nodePath.setText(zkNode.decodeNodePath())`；`mutexes.addNodes(...)`+`manageBindVisible()`；`authList.init(zkItem.iid())`；依 digestACL 数量/为空选择默认值；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.authNode()` |
  | `void destroy()` | 销毁时释放互斥器 | `mutexes.destroy()` → `super.destroy()` |

- 调用链：`auth → ShellZKAuthUtil.authNode → ShellZKAuthStore.replace`
- 调用链：`onWindowShown → ShellZKAuthComboBox.init → ShellZKNode.decodeNodePath`

## ShellS3AddBucketController

- 职责：S3 存储桶新增业务，设置版本控制/对象锁定/保留策略后创建桶。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ClearableTextField | 桶名称 |
  | versioning | FXToggleSwitch | 版本控制 |
  | objectLock | FXToggleSwitch | 对象锁定 |
  | region | ReadOnlyTextField | 区域（只读） |
  | retention | FXToggleSwitch | 保留 |
  | retentionValidity | NumberTextField | 保留期 |
  | retentionMode | ShellS3RetentionModeComboBox | 保留模式 |
  | retentionValidityType | ShellS3RetentionValidityTypeComboBox | 保留时间类型 |
  | client | ShellS3Client | 客户端 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void add()` | 添加桶 | `name` 非空校验（`ValidatorUtil.validFail`）；构造 `ShellS3Bucket` 并 set 名称/版本控制/对象锁定/区域（`client.region().id()`）/保留相关；`client.createBucket(bucket)`，成功 `setProp("bucket", bucket)`、`MessageBox.okToast`、`closeWindow()` |
  | `void bindListeners()` | 绑定联动监听 | `objectLock.selectedChanged` 联动开启并禁用 `versioning`；`retention.selectedChanged` 联动开启并禁用 `objectLock`、`NodeGroupUtil.enable/disable(stage,"retention")`；`retentionValidityType.selectedIndexChanged` 选中 1 时 `retentionValidity.setValue(1)` |
  | `void onWindowShown(WindowEvent event)` | 初始化客户端与区域 | `getProp("client")`、`region.setText(client.region().id())`、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.addBucket()` |

- 调用链：`add → ShellS3Client.region().id → ShellS3Client.createBucket`
- 调用链：`bindListeners → FXToggleSwitch.selectedChanged → NodeGroupUtil.enable/disable`

## ShellS3ShareFileController

- 职责：S3 文件分享业务，按持续时间类型生成预签名分享地址并可复制。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | file | ReadOnlyTextField | 文件（只读） |
  | shareUrl | ReadOnlyTextArea | 分享地址（只读） |
  | duration | NumberTextField | 持续时间 |
  | durationType | ShellS3EffectiveTimeCombobox | 持续单位 |
  | s3File | ShellS3File | 文件对象 |
  | client | ShellS3Client | 客户端 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void generate()` | 生成分享地址 | 依 `durationType`（`isDays/isMinutes/isSeconds/isHours`）构造 `Duration`；`client.generatePresignedUrl(s3File.getBucketName(), s3File.getFileKey(), duration)` 后 `shareUrl.setText(url)` |
  | `void copyUrl()` | 复制分享地址 | `ClipboardUtil.copy(shareUrl.getText())`、`MessageBox.okToast(operationSuccess)` |
  | `void onWindowShown(WindowEvent event)` | 初始化文件与客户端 | `getProp("s3File"/"client")`、`file.setText(s3File.getFileKey())`、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.shareFile()` |

- 调用链：`generate → ShellS3EffectiveTimeCombobox.isDays/isMinutes/... → ShellS3Client.generatePresignedUrl`
- 调用链：`copyUrl → ClipboardUtil.copy → MessageBox.okToast`

## ShellS3UpdateBucketController

- 职责：S3 存储桶修改业务，回填桶配置并提交版本控制/对象锁定/保留策略变更。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 密钥/桶名称（只读） |
  | versioning | FXToggleSwitch | 版本控制 |
  | objectLock | FXToggleSwitch | 对象锁定 |
  | region | ReadOnlyTextField | 区域（只读） |
  | retention | FXToggleSwitch | 保留 |
  | retentionValidity | NumberTextField | 保留期 |
  | retentionMode | ShellS3RetentionModeComboBox | 保留模式 |
  | retentionValidityType | ShellS3RetentionValidityTypeComboBox | 保留时间类型 |
  | client | ShellS3Client | 客户端 |
  | bucket | ShellS3Bucket | 桶对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void update()` | 修改桶 | 读取 versioning/objectLock/retention 相关值构造新 `ShellS3Bucket`（名称取 `bucket.getName()`、保留取 `bucket.isRetention()`）；`client.updateBucket(bucket)`，成功后 `bucket.copy(bucket)`、`setProp("bucket", bucket)`、`MessageBox.okToast`、`closeWindow()` |
  | `void bindListeners()` | 绑定联动监听 | `objectLock.selectedChanged` 联动开启并禁用 `versioning`；`retention.selectedChanged` 联动开启并禁用 `objectLock`、`NodeGroupUtil.enable/disable(stage,"retention")`；`retentionValidityType.selectedIndexChanged` 选中 1 时 `retentionValidity.setValue(1)` |
  | `void onWindowShown(WindowEvent event)` | 回填桶配置 | `getProp("client"/"bucket")`；`name/region.setText`，回填 versioning/objectLock/retention 并禁用 objectLock/retention，`retentionMode.select/getRetentionMode`、`retentionValidity/retentionValidityType.select`；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.updateBucket()` |

- 调用链：`update → ShellS3Client.updateBucket → ShellS3Bucket.copy → setProp("bucket")`
- 调用链：`onWindowShown → ShellS3RetentionModeComboBox.select → NumberTextField.setValue`

## 跳过（整文件死代码）

- `ShellZKHistoryViewController`（`controller/zk/history/`）：整文件被注释，无有效代码，跳过。
- `ZKNodeImportController`（`controller/zk/node/`）：整文件被注释，无有效代码，跳过。
