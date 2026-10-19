# controller 杂项控制器代码审查

> 范围：`controller/data/**`、`jump/**`、`key/**`、`snippet/**`、`split/**`、`ssh/**`、`tunneling/**`，共 14 类。

---

## ShellDataExportController
- 职责：导出 EasyShell 本地数据（连接/分组/密钥/片段）到 JSON 文件的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | exportFile | File | 导出目标文件 |
  | fileName | FXText | 文件名显示 |
  | connect | FXCheckBox | 是否导出连接 |
  | group | FXCheckBox | 是否导出分组 |
  | key | FXCheckBox | 是否导出密钥 |
  | snippet | FXCheckBox | 是否导出片段 |

  > 原 `keyStore/groupStore/snippetStore/connectStore` 字段及其存储逻辑均被整段注释，属死代码，未列入。

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doExport()` | 执行导出 | 未选文件则 `MessageBox.warn`；`ShellSyncManager.getSyncData(key,group,snippet,connect)` 取同步数据，`FileUtil.writeUtf8String(export.toJSONString(), exportFile)` 写文件后 `closeWindow()` + `MessageBox.okToast` |
  | `void onWindowShown(WindowEvent event)` | 初始化 | `stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.exportData()` |
  | `void selectFile()` | 选择导出文件 | `FXChooser.jsonExtensionFilter()`，默认文件名 `EasyShell-连接-yyyyMMdd.json`，`FileChooserHelper.save` 后回填 `fileName` |

- 调用链：`doExport → ShellSyncManager.getSyncData → FileUtil.writeUtf8String`

## ShellDataImportController
- 职责：从 JSON 文件导入 EasyShell 本地数据（连接/分组/密钥/片段）的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | importFile | File | 导入来源文件 |
  | fileName | FXText | 文件名显示 |
  | selectFile | FXButton | 选择文件按钮 |
  | connect | FXCheckBox | 是否导入连接 |
  | group | FXCheckBox | 是否导入分组 |
  | key | FXCheckBox | 是否导入密钥 |
  | snippet | FXCheckBox | 是否导入片段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doImport()` | 执行导入 | 未选文件则 warn；`FileUtil.readUtf8String` + `ShellDataExport.fromJSON(text)`，`ShellSyncManager.saveSyncData(export,key,group,snippet,connect)` 保存，`ShellEventUtil.dataImported()` + `closeWindow()` + `MessageBox.okToast`（原逐 store replace 逻辑已注释，死代码） |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.importData()` |
  | `void selectFile()` | 选择导入文件 | `FXChooser.jsonExtensionFilter()` + `FileChooserHelper.choose`，随后 `parseFile()` |
  | `void parseFile()` | 校验导入文件 | 依次校验存在性、是否目录、是否 JSON 后缀（`FileNameUtil`）、是否空内容，非法则 `MessageBox.warn(...)` |
  | `void onWindowShown(WindowEvent event)` | 初始化 | `stage.hideOnEscape()`；取 prop `file`，非空则禁用选择按钮并 `parseFile()` |

- 调用链：`doImport → ShellDataExport.fromJSON → ShellSyncManager.saveSyncData → ShellEventUtil.dataImported`
- 调用链：`selectFile → FileChooserHelper.choose → parseFile`

## ShellAddHostController
- 职责：从已有连接生成 SSH 跳板配置的新增对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | sshName | ClearableTextField | 跳板名称 |
  | host | ShellConnectTextField | 选择的主机连接 |
  | enable | FXToggleSwitch | 是否启用 |
  | keyStore | ShellKeyStore | 密钥存储（`ShellKeyStore.INSTANCE`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void testConnect()` | 测试连接 | 取 `host.getSelectedItem()`，`ShellConnectUtil.testConnect(stage, connect, connect.connectTimeOutMs())` |
  | `void add()` | 生成跳板配置 | 校验名称/主机；`ShellSSHUtil.convert(connect)` 转 `SSHConnect` 后 `config.copy(sshConnect)` 并 setName/enabled；`setProp("jumpConfig", config)` + `closeWindow()`（原按认证方式逐字段赋值已注释，死代码） |
  | `void onWindowShown(WindowEvent event)` | 初始化 | 取 prop `connect` 并从 `host` 列表移除当前连接，`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.addHost()` |

- 调用链：`add → ShellSSHUtil.convert → ShellJumpConfig.copy → setProp("jumpConfig")`

## ShellAddJumpController
- 职责：手动新增 SSH 跳板（跳板机）配置的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | sshName | ClearableTextField | 跳板名称 |
  | sshHost | ClearableTextField | SSH 主机地址 |
  | sshPort | PortTextField | SSH 主机端口 |
  | sshTimeout | NumberTextField | 连接超时（秒） |
  | sshUser | ClearableTextField | SSH 用户 |
  | sshPassword | PasswordTextField | SSH 密码 |
  | sshKey | ShellKeyComboBox | SSH 密钥 |
  | sshAgent | ReadOnlyTextField | SSH Agent 地址 |
  | sshAuthMethod | ShellSSHAuthTypeComboBox | 认证方式 |
  | sshCertificate | ChooseFileTextField | SSH 证书文件 |
  | sshCertificatePwd | PasswordTextField | 证书密码 |
  | enable | FXToggleSwitch | 是否启用 |
  | forwardAgent | FXCheckBox | 是否转发 SSH 代理 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 组装 `ip:port` | 校验端口/主机通过后返回拼接串 |
  | `void testConnect()` | 测试连接 | 构造临时 `ShellConnect`（host/timeout/forwardAgent/user/password/authMethod/certificate 等），`ShellConnectUtil.testConnect` |
  | `void add()` | 生成跳板配置 | 逐项校验（名称/用户/密码或证书或密钥按认证方式）；构造 `ShellJumpConfig` 并 set 各字段（timeout×1000）；`isManagerAuth` 时用 `key.getId()/getPassword()/getPublicKey()/getPrivateKey()`，否则用证书路径与证书密码；`setProp("jumpConfig", config)` + `closeWindow()` |
  | `void bindListeners()` | 认证方式联动 | 认证方式变化时用 `NodeGroupUtil.display/disappear` 切换 `password`/`certificate`/`sshAgent`/`sshKey` 节点显隐 |
  | `void onWindowShown(WindowEvent event)` | 初始化 | `stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.addJumpHost()` |
  | `void onStageInitialize(StageAdapter stage)` | 预填 Agent 地址 | `OSUtil.isWindows()` 用 `PageantConnector.DESCRIPTOR.getIdentityAgent()`，否则 `UnixDomainSocketConnector.DESCRIPTOR.getIdentityAgent()` |

  > `chooseSSHCertificate()` 整段被注释，死代码跳过。

- 调用链：`add → ShellJumpConfig.set* → setProp("jumpConfig")`
- 调用链：`bindListeners → sshAuthMethod.selectedIndexChanged → NodeGroupUtil.display/disappear`

## ShellUpdateJumpController
- 职责：编辑已有 SSH 跳板（跳板机）配置的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | sshName | ClearableTextField | 跳板名称 |
  | sshHost | ClearableTextField | SSH 主机地址 |
  | sshPort | PortTextField | SSH 主机端口 |
  | sshTimeout | NumberTextField | 连接超时（秒） |
  | sshUser | ClearableTextField | SSH 用户 |
  | sshPassword | PasswordTextField | SSH 密码 |
  | sshKey | ShellKeyComboBox | SSH 密钥 |
  | sshAgent | ReadOnlyTextField | SSH Agent 地址 |
  | sshAuthMethod | ShellSSHAuthTypeComboBox | 认证方式 |
  | sshCertificate | ChooseFileTextField | SSH 证书文件 |
  | sshCertificatePwd | PasswordTextField | 证书密码 |
  | config | ShellJumpConfig | 待编辑的跳板配置 |
  | enable | FXToggleSwitch | 是否启用 |
  | forwardAgent | FXCheckBox | 是否转发 SSH 代理 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 组装 `ip:port` | 同新增，校验后拼接 |
  | `void testConnect()` | 测试连接 | 同新增，构造临时 `ShellConnect` 并 `ShellConnectUtil.testConnect` |
  | `void update()` | 修改跳板配置 | 逐项校验后写回 `config` 各字段（timeout×1000、认证方式按 manager/certificate 分支），`setProp("jumpConfig", config)` + `closeWindow()` |
  | `void bindListeners()` | 认证方式联动 | `NodeGroupUtil.display/disappear` 切换认证相关节点显隐 |
  | `void onWindowShown(WindowEvent event)` | 回填表单 | 取 prop `config` 并回填名称/主机/用户/端口/启用/超时/密码；按 `isPasswordAuth/isCertificateAuth/isSSHAgentAuth/isKeyAuth` 选中对应认证项并回填证书或按 id 选密钥；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.updateJumpHost()` |
  | `void onStageInitialize(StageAdapter stage)` | 预填 Agent 地址 | 同新增，按操作系统取 `PageantConnector`/`UnixDomainSocketConnector` 的 IdentityAgent |

  > `chooseSSHCertificate()` 整段被注释，死代码跳过。

- 调用链：`onWindowShown → getProp("config") → 各控件回填`
- 调用链：`update → ShellJumpConfig.set* → setProp("jumpConfig")`

## ShellAddKeyController
- 职责：新增 SSH 密钥（支持生成或手工填写）的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ClearableTextField | 密钥名称 |
  | privateKey | ReadOnlyTextArea | 私钥内容（只读） |
  | publicKey | ReadOnlyTextArea | 公钥内容（只读） |
  | keyType | ShellKeyTypeComboBox | 密钥类型 |
  | keyLength | ShellKeyLengthComboBox | 密钥长度 |
  | keyPassword | PasswordTextField | 密钥密码 |
  | keyStore | ShellKeyStore | 密钥存储（`ShellKeyStore.INSTANCE`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void add()` | 保存密钥 | 校验名称/公钥/私钥；构造 `ShellKey` 并 set 各字段；`keyStore.insert(shellKey)` 成功则 `ShellEventUtil.keyAdded` + toast + `closeWindow()`，否则 warn |
  | `void bindListeners()` | 类型联动 | `keyType.selectedItemChanged` 时 `keyLength.init(t1)` 并清空公/私钥；初始化时 `keyLength.init("RSA")` |
  | `void onWindowShown(WindowEvent event)` | 初始化 | `stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.addKey1()` |
  | `void generateKey()` | 生成密钥对 | `StageManager.showMask` 内按类型调 `SSHKeyUtil.generateRsa/generateEd25519/generateEcdsa/generateDsa`，结果数组 [0] 写公钥、[1] 写私钥 |

- 调用链：`generateKey → SSHKeyUtil.generateRsa/Ed25519/Ecdsa/Dsa → publicKey/privateKey.setText`
- 调用链：`add → ShellKeyStore.insert → ShellEventUtil.keyAdded`

## ShellCopyIdKeyController
- 职责：将选中的 SSH 密钥复制（ssh-copy-id）到目标主机的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | host | ShellConnectTextField | 目标连接 |
  | keyInfo | ReadOnlyTextArea | 待复制密钥信息（只读） |
  | message | MsgTextArea | 执行日志消息区 |
  | keys | List<ShellKey> | 待复制的密钥列表 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void bindListeners()` | 空实现 | 仅调 `super` |
  | `void onWindowShown(WindowEvent event)` | 初始化 | 取 prop `keys`，拼接各密钥名称写入 `keyInfo`；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.copyKeys1ToHost()` |
  | `void copyKeys()` | 执行复制 | 校验目标连接；`ShellClientUtil.newClient(connect)`；`StageManager.showMask` 内 `client.start(3000)`，未连接则提示；`ShellKeyUtil.sshCopyId(keys, client)` 并写 message，最后 `client.close()` |
  | `void destroy()` | 销毁 | `host.destroy()`、`message.destroy()` 后 `super.destroy()` |

- 调用链：`copyKeys → ShellClientUtil.newClient → client.start → ShellKeyUtil.sshCopyId → client.close`

## ShellImportKeyController
- 职责：从公/私钥文本或文件导入 SSH 密钥并保存的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ClearableTextField | 密钥名称 |
  | privateKey | FXTextArea | 私钥内容 |
  | publicKey | FXTextArea | 公钥内容 |
  | keyType | ReadOnlyTextField | 密钥类型（只读，自动识别） |
  | keyLength | ClearableTextField | 密钥长度 |
  | keyPassword | PasswordTextField | 密钥密码 |
  | keyStore | ShellKeyStore | 密钥存储（`ShellKeyStore.INSTANCE`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void bindListeners()` | 自动识别类型/长度 | 定义 `changeFunc` 调 `fillKeyType()`/`fillKeySize()`，失败时 `checkKeyPassword(ex)` 后重试；监听公/私钥文本变化触发 |
  | `void onWindowShown(WindowEvent event)` | 初始化 | `stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.importKey1()` |
  | `void importKey()` | 保存导入的密钥 | 校验名称/公钥/私钥；构造 `ShellKey`（类型取 `keyType.getText()`、长度 `Integer.parseInt(keyLength)`）；`keyStore.insert` 成功则 `ShellEventUtil.keyAdded` + toast + `closeWindow()` |
  | `void pastePubKey()/pastePriKey()` | 粘贴公钥/私钥 | 分别 `publicKey.paste()` / `privateKey.paste()` |
  | `void choosePubKey()` | 选择公钥文件 | 选文件并限 500KB；空名称时以文件名去后缀命名；读取内容经 `handlePubKey` 处理；`.ppk` 时同时处理私钥，否则尝试读取同名无后缀私钥文件 |
  | `void choosePriKey()` | 选择私钥文件 | 同上逻辑反向：读取私钥经 `handlePriKey`，`.ppk` 时同时处理公钥，否则尝试读取同名 `.pub` 公钥 |
  | `void fillKeyType() throws Exception` | 识别密钥类型 | 用 `SSHKeyUtil.getKeyType(privateKey/公共key, pwd)`，命中后 `keyType.setText(ShellKeyTypeComboBox.getTypeName(keyType))` |
  | `void fillKeySize() throws Exception` | 识别密钥长度 | `SSHKeyUtil.getKeySize` 取长度，ED25519 默认 256，否则清空 |
  | `boolean checkKeyPassword(Exception ex)` | 检测并提示密钥密码 | 若异常信息含 “No password provider...” 则 `MessageBox.prompt` 输入密码写回并返回 true，否则 `MessageBox.exception` 返回 false |
  | `String handlePubKey(String pubKey)` | 处理 Putty 公钥 | 以 “PuTTY” 开头时解析类型/注释/Public-Lines 行拼装为通用格式 |
  | `String handlePriKey(String priKey)` | 处理 Putty 私钥 | 以 “PuTTY” 开头时按类型补 BEGIN/END 头尾并抽取 Private-Lines |

- 调用链：`choosePubKey → FileUtil.readUtf8String → handlePubKey → publicKey.text`；`bindListeners → fillKeyType/fillKeySize → SSHKeyUtil.getKeyType/getKeySize`
- 调用链：`importKey → ShellKeyStore.insert → ShellEventUtil.keyAdded`

## ShellUpdateKeyController
- 职责：编辑已有 SSH 密钥的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ClearableTextField | 密钥名称 |
  | privateKey | FXTextArea | 私钥内容 |
  | publicKey | FXTextArea | 公钥内容 |
  | keyType | ReadOnlyTextField | 密钥类型（只读） |
  | keyLength | ReadOnlyTextField | 密钥长度（只读） |
  | keyPassword | PasswordTextField | 密钥密码 |
  | key | ShellKey | 待编辑密钥 |
  | keyStore | ShellKeyStore | 密钥存储（`ShellKeyStore.INSTANCE`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void save()` | 保存修改 | 校验名称/公钥/私钥；写回 `key` 名称/密码/公钥/私钥；`keyStore.update(key)` 成功则 `ShellEventUtil.keyUpdated` + toast + `closeWindow()` |
  | `void onWindowShown(WindowEvent event)` | 回填表单 | 取 prop `key`，回填名称/类型/公钥/密码/长度/私钥；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.updateKey1()` |

- 调用链：`save → ShellKeyStore.update → ShellEventUtil.keyUpdated`

## ShellSnippetController
- 职责：代码片段管理窗口业务，支持编辑/保存/运行片段并发送到终端。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | sendAll | FXCheckBox | 发送到所有终端 |
  | sendClear | FXCheckBox | 发送后清除内容 |
  | sendLine | FXCheckBox | 发送时追加换行符 |
  | content | ShellSnippetEditor | 片段内容编辑器 |
  | snippetTreeView | ShellSnippetTreeView | 片段列表树 |
  | rightBox | FXVBox | 右侧组件 |
  | snippetStore | ShellSnippetStore | 片段存储（`ShellSnippetStore.INSTANCE`） |
  | snippet | ShellSnippet | 当前片段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 初始化 | `stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.snippet()` |
  | `void save()` | 保存片段 | `snippet.setContent(content.getText())` 后 `snippetStore.update(snippet)`，再 `setUnsaved(false)`（原新增分支已注释，死代码） |
  | `void setUnsaved(boolean unsaved)` | 设置未保存标记 | 选中项为 `ShellSnippetTreeItem` 时 `setUnsaved` 并 `refresh()` |
  | `void run()` | 运行片段 | 内容空则返回；`sendLine` 选中时补 `\r`；转义 `\t \n \r \b`；`ShellEventUtil.runSnippet(content, sendAll.isSelected())`；`sendClear` 选中时清空并置 `snippet=null` |
  | `void contentKeyPressed(KeyEvent e)` | 内容快捷键 | `Ctrl+S` 调 `save()`，`Ctrl+R` 调 `run()` |
  | `void bindListeners()` | 绑定事件 | 内容变化置未保存；树选中项变化时 `doEdit(item.value())` 并更新标题；设置树的增/改/删回调为 `doEdit`/`doEdit`/`doDelete` |
  | `void doEdit(ShellSnippet snippet)` | 编辑片段 | 设当前 `snippet`；空则清空内容，否则回填内容 |
  | `void doDelete(ShellSnippet snippet)` | 删除片段 | 删除的是当前片段则清空 `snippet` 与内容 |
  | `void destroy()` | 销毁 | `content.destroy()`、`snippetTreeView.destroy()` 后 `super.destroy()` |

  > `widthResizer` 相关拉伸辅助字段与方法均被注释，死代码跳过。

- 调用链：`run → ShellEventUtil.runSnippet → 终端执行`
- 调用链：`snippetTreeView 选中 → bindListeners → doEdit → content.setText`

## ShellSplitGuidController
- 职责：终端分屏引导对话框业务（选择分屏类型与目标连接）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 步骤1 容器 |
  | step2 | FXVBox | 步骤2 容器 |
  | type | FXToggleGroup | 分屏类型选择 |
  | splitListView | ShellSplitListView | 连接列表 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void showStep1()` | 回到步骤1 | `splitListView.unSelectAll()`、`step2.disappear()`、`step1.display()` |
  | `void showStep2()` | 进入步骤2 | 依 `type.selectedUserData()`（type1~type8）设 `maxSize` 为 2/3/4/6/9，`splitListView.setMaxSelected(maxSize)`，切换步骤显隐 |
  | `void toSplit()` | 执行分屏 | `ShellEventUtil.showSplit(type, splitListView.getSelectedConnects())` 后 `closeWindow()` |
  | `void onWindowShown(WindowEvent event)` | 初始化 | `stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.termSplitView()` |

- 调用链：`toSplit → ShellEventUtil.showSplit → closeWindow`

## ShellSSHAuthController
- 职责：SSH 认证信息输入对话框业务（密码/证书/密钥三种方式）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | certPwd | PasswordTextField | 证书密码 |
  | certKey | ChooseFileTextField | 证书文件 |
  | authType | ShellSSHAuthTypeComboBox2 | SSH 认证类型 |
  | remember | FXCheckBox | 是否记住 |
  | sshKey | ShellKeyComboBox | SSH 密钥 |
  | connect | ShellConnect | 目标连接 |
  | connectStore | ShellConnectStore | 连接存储（`ShellConnectStore.INSTANCE`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doAuth()` | 执行认证 | 按 `authType` 分支校验密码/证书/密钥；`remember` 选中时 `assembleInfo(connect,...)` + `connectStore.update(connect)` + `setProp("connect", connect)`，否则复制 connect 组装后 setProp；`closeWindow()` |
  | `void assembleInfo(ShellConnect connect, String certKey, String certPwd, String password, String userName, String authType, ShellKey sshKey)` | 组装认证信息 | 非空字段分别 set 证书/证书密码/密码/用户，密钥非空 set `keyId`，最后 setAuthMethod |
  | `void bindListeners()` | 认证类型联动 | `NodeGroupUtil.display/disappear` 切换 `password`/`certificate`/`sshKey` 节点显隐 |
  | `void onWindowShown(WindowEvent event)` | 初始化 | 取 prop `connect`，回填用户名，`removeProp("connect")`，标题追加 `[host]` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.auth()` |

- 调用链：`doAuth → (remember ? connectStore.update : copy) → assembleInfo → setProp("connect")`

## ShellAddTunnelingController
- 职责：新增 SSH 隧道（端口转发）配置的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tunnelingName | ClearableTextField | 隧道名称 |
  | localHost | ClearableTextField | 本地地址 |
  | localPort | PortTextField | 本地端口 |
  | remoteHost | ClearableTextField | 远程地址 |
  | remotePort | PortTextField | 远程端口 |
  | tunnelingType | ShellTunnelingTypeComboBox | 隧道类型 |
  | enable | FXToggleSwitch | 是否启用 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void add()` | 生成隧道配置 | 校验名称/本地地址/类型，非动态认证还需校验远程地址；构造 `ShellTunnelingConfig` 并 set 各字段；`setProp("tunnelingConfig", config)` + `closeWindow()` |
  | `void bindListeners()` | 类型联动 | 动态认证时 `disable` 远程地址与端口，否则 `enable` |
  | `void onWindowShown(WindowEvent event)` | 初始化 | `stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.addTunneling()` |

- 调用链：`add → ShellTunnelingConfig.set* → setProp("tunnelingConfig")`

## ShellUpdateTunnelingController
- 职责：编辑已有 SSH 隧道（端口转发）配置的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tunnelingName | ClearableTextField | 隧道名称 |
  | localHost | ClearableTextField | 本地地址 |
  | localPort | PortTextField | 本地端口 |
  | remoteHost | ClearableTextField | 远程地址 |
  | remotePort | PortTextField | 远程端口 |
  | tunnelingType | ShellTunnelingTypeComboBox | 隧道类型 |
  | config | ShellTunnelingConfig | 待编辑隧道配置 |
  | enable | FXToggleSwitch | 是否启用 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void update()` | 修改隧道配置 | 校验后写回 `config` 各字段；`setProp("tunnelingConfig", config)` + `closeWindow()` |
  | `void bindListeners()` | 类型联动 | 动态认证时禁用远程地址/端口，否则启用 |
  | `void onWindowShown(WindowEvent event)` | 回填表单 | 取 prop `config`，回填启用/名称/类型/本地/远程各字段；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.updateTunneling()` |

- 调用链：`onWindowShown → getProp("config") → 各控件回填`；`update → config.set* → setProp("tunnelingConfig")`
