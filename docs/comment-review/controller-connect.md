# controller/connect 连接控制器代码审查

> 范围：`src/main/java/cn/oyzh/easyshell/controller/connect/` 递归全部类，共 37 个。
> 涵盖 SSH/SFTP/FTP/Local/Telnet/Serial/VNC/RDP/RLogin/SMB/Mosh/Mysql/Dameng/Mongo/Redis/ZK/Webdav/S3 各协议的新增与更新对话框，以及 `ShellAddConnectGuidController` 连接新增引导。

---

# 连接新增/更新控制器（组 A）

覆盖 SSH / SFTP / FTP / LOCAL 各协议的连接新增与更新对话框，以及协议选择引导页。统一模板：`@StageAttribute` 声明 fxml 与模态，`@FXML` 表单控件字段，`testConnect`/`add`|`update` 交互，`bindListeners` 绑定联动，`onWindowShown` 回填数据，`getViewTitle` 标题。保存经 `ShellConnectStore`，广播经 `ShellEventUtil.connectAdded/connectUpdated`。

> 死代码说明：各文件内被整段注释的旧逻辑（`enableBackground`/`backgroundTab`/`backgroundImage`、`downloadX11`、`chooseCertificate`、`chooseBackgroundImage`）均已跳过，不计入字段与方法表。

## ShellAddConnectGuidController

- 职责：新增连接引导页，按选中的协议类型分发到对应的新增对话框。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | type | FXToggleGroup | 协议类型 tab 组件，用于取选中项 userData |
  | group | ShellGroup | 新增连接归属的分组（来自 prop "group"） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void toAdd()` | 按选中协议打开新增窗口并关闭引导页 | 取 `type.selectedUserData()`，用 `ShellPrototype` 常量逐一 equalsIgnoreCase 匹配（SSH/LOCAL/TELNET/SFTP/FTP/S3/SERIAL/VNC/RLOGIN/SMB/REDIS/ZOOKEEPER/RDP/WEBDAV/MYSQL/DAMENG/MONGO/MOSH，另有 s3_cos/s3_obs/s3_oss/s3_minio 走 `addS3Connect(group, 平台)`），调用对应 `ShellViewFactory.addXxxConnect(group)`；结束后 `this.closeWindow()`，异常 `MessageBox.exception` |
  | `void onWindowShown(WindowEvent)` | 窗口显示回调 | `this.group = this.getProp("group")`；`this.stage.hideOnEscape()` |
  | `String getViewTitle()` | 视图标题 | 返回 `I18nHelper.addGuid()` |

- 调用链：`toAdd → ShellViewFactory.addSSHConnect → ShellAddSSHConnectController.add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
- 调用链：`toAdd → ShellViewFactory.addLocalConnect → ShellAddLocalConnectController.add → ShellConnectStore.insert → ShellEventUtil.connectAdded`

## ShellAddSSHConnectController

- 职责：SSH 连接新增对话框，采集认证/终端/代理/X11/跳板机/隧道等完整配置并保存。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | certificate | ChooseFileTextField | 证书文件路径 |
  | certificatePwd | PasswordTextField | 证书密码 |
  | sshAgent | ReadOnlyTextField | ssh 代理（agent）地址 |
  | key | ShellKeyComboBox | 密钥选择 |
  | tabPane | FXTabPane | tab 容器 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | termType | ShellTermTypeComboBox | 终端类型 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt 修饰 |
  | connectTimeOut | NumberTextField | 连接超时（秒） |
  | x11forwarding | FXToggleSwitch | x11 转发开关 |
  | x11Tab | FXTab | x11 面板 |
  | x11Host | ClearableTextField | x11 地址 |
  | x11Port | PortTextField | x11 端口 |
  | x11Cookie | ClearableTextField | x11 认证信息 |
  | x11CookieBth | FXButton | x11 cookie 加载按钮 |
  | env | FXTextArea | 环境变量 |
  | authMethod | ShellSSHAuthTypeComboBox | 认证方式 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | group | ShellGroup | 分组（来自 prop "group"） |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理认证信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | jumpTableView | ShellJumpTableView | 跳板机配置表 |
  | tunnelingTableView | ShellTunnelingTableView | 隧道配置表 |
  | enableCompress | FXCheckBox | 启用压缩 |
  | enableZModem | FXCheckBox | 启用 ZModem |
  | forwardAgent | FXCheckBox | 转发 ssh 代理 |
  | connectStore | ShellConnectStore | 连接储存对象（`ShellConnectStore.INSTANCE`，final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 校验并拼装 host:port | 切回 tab0，`hostPort.validate()`/`hostIp.validate()` 失败返回 null，否则拼接 `hostIp + ":" + hostPort.getValue()` |
  | `ShellX11Config getX11Config()` | 组装 x11 配置 | new `ShellX11Config`，setHost/setPort/setCookie |
  | `ShellProxyConfig getProxyConfig()` | 组装代理配置 | new `ShellProxyConfig`，setHost/setPort/setUser/setPassword/setAuthType/setProtocol |
  | `void testConnect()` | 测试连通 | 组装临时 `ShellConnect`（type=ssh、host、timeout、认证、跳板机、forwardAgent、代理），调 `ShellConnectUtil.testConnect(stage, shellConnect, timeout*1000)` |
  | `void add()` | 校验并新增 | 校验 host/userName/密码/证书/密钥（按 `authMethod`），x11 与代理开启时校验并 `tabPane.select`，名称空则用 host 占位；组装 `ShellConnect` 后 `connectStore.replace` 成功则 `ShellEventUtil.connectAdded`、`setProp("connect")`、`closeWindow()`，失败 warn |
  | `void bindListeners()` | 绑定控件联动 | hostIp 输入含 ":" 时拆分 ip/端口；`authMethod` 变更按 password/certificate/sshAgent/sshKey 显示隐藏 `NodeGroupUtil.display/disappear`；`enableProxy` 开关启用/禁用 proxyTab 及认证框；`proxyAuthType` 联动 proxyAuthInfoBox |
  | `void onWindowShown(WindowEvent)` | 窗口显示回调 | `group = getProp("group")`；`stage.switchOnTab()`；`stage.hideOnEscape()` |
  | `void onStageInitialize(StageAdapter)` | 舞台初始化 | x11 各控件 disableProperty 绑定 `x11forwarding.selectedProperty().not()`；按 OS 设置 sshAgent 文本（Windows 用 `PageantConnector`，其余用 `UnixDomainSocketConnector`） |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.connectAddTitle()` |
  | `void loadX11Cookie()` | 加载 x11 cookie | 仅 Linux：`RuntimeUtil.execForStr("xauth list")`，取首行末段 `ArrayUtil.last` 填入 x11Cookie，空则 clear |
  | `void addHost()` | 添加主机（跳板） | `ShellViewFactory.addHost(null)`，取 prop "jumpConfig" 后 `jumpTableView.addItem` 并 `updateOrder()` |
  | `void addJump()` | 添加跳板 | `ShellViewFactory.addJump()`，同上回填 jumpTableView |
  | `void updateJump()` | 编辑跳板 | 取选中项，`ShellViewFactory.updateJump(config)`，成功后 `refresh()` + `updateOrder()` |
  | `void deleteJump()` | 删除跳板 | `MessageBox.confirm` 后 `jumpTableView.removeSelectedItem()` + `updateOrder()` |
  | `void moveJumpUp()` | 上移跳板 | `TableViewUtil.moveUp(jumpTableView)` + refresh + updateOrder |
  | `void moveJumpDown()` | 下移跳板 | `TableViewUtil.moveDown(jumpTableView)` + refresh + updateOrder |
  | `void addTunneling()` | 添加隧道 | `ShellViewFactory.addTunneling()`，取 prop "tunnelingConfig" 回填 tunnelingTableView |
  | `void updateTunneling()` | 编辑隧道 | `ShellViewFactory.updateTunneling(config)`，成功后 `refresh()` |
  | `void deleteTunneling()` | 删除隧道 | `MessageBox.confirm` 后 `tunnelingTableView.removeSelectedItem()` |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded → setProp("connect") → closeWindow`
- 调用链：`testConnect → getProxyConfig → ShellConnectUtil.testConnect`
- 调用链：`addHost → ShellViewFactory.addHost → jumpTableView.addItem → updateOrder`

## ShellUpdateSSHConnectController

- 职责：SSH 连接修改对话框，加载既有 `ShellConnect` 回填表单并在保存时更新。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | certificate | ChooseFileTextField | 证书文件路径 |
  | certificatePwd | PasswordTextField | 证书密码 |
  | sshAgent | ReadOnlyTextField | ssh 代理（agent）地址 |
  | key | ShellKeyComboBox | 密钥选择 |
  | tabPane | FXTabPane | tab 容器 |
  | shellConnect | ShellConnect | 待修改的 ssh 连接（prop "shellConnect"） |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | termType | ShellTermTypeComboBox | 终端类型 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt 修饰 |
  | connectTimeOut | NumberTextField | 连接超时 |
  | x11forwarding | FXToggleSwitch | x11 转发开关 |
  | x11Tab | FXTab | x11 面板 |
  | x11Host | ClearableTextField | x11 地址 |
  | x11Port | PortTextField | x11 端口 |
  | x11Cookie | ClearableTextField | x11 认证信息 |
  | x11CookieBth | FXButton | x11 cookie 加载按钮 |
  | env | FXTextArea | 环境变量 |
  | authMethod | ShellSSHAuthTypeComboBox | 认证方式 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理认证信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | jumpTableView | ShellJumpTableView | 跳板机配置表 |
  | tunnelingTableView | ShellTunnelingTableView | 隧道配置表 |
  | enableCompress | FXCheckBox | 启用压缩 |
  | enableZModem | FXCheckBox | 启用 ZModem |
  | forwardAgent | FXCheckBox | 转发 ssh 代理 |
  | connectStore | ShellConnectStore | 连接储存对象（final） |
  | jumpConfigStore | ShellJumpConfigStore | 跳板储存对象（final） |
  | tunnelingConfigStore | ShellTunnelingConfigStore | 隧道储存对象（final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 校验并拼装 host:port | 同新增版，校验失败返回 null |
  | `ShellX11Config getX11Config()` | 组装 x11 配置 | 复用 `shellConnect.getX11Config()`，为空则新建并 `setIid(shellConnect.getId())` 后回填 |
  | `ShellProxyConfig getProxyConfig()` | 组装代理配置 | 复用 `shellConnect.getProxyConfig()`，为空则新建并 setIid，再回填字段 |
  | `void testConnect()` | 测试连通 | 组装临时 `ShellConnect`（额外 `setId(shellConnect.getId())`）后 `ShellConnectUtil.testConnect` |
  | `void update()` | 校验并修改 | 同新增版校验流程；直接写入 `shellConnect` 各字段，`connectStore.replace` 成功后 `ShellEventUtil.connectUpdated`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件联动 | 与新增版一致（hostIp 拆分、authMethod 显隐、enableProxy、proxyAuthType） |
  | `void onWindowShown(WindowEvent)` | 回填数据 | 取 prop "shellConnect"，回填 name/hostIp/remark/osType/env/hostPort/charset/termType/connectTimeOut/认证/退格/alt/forwardAgent/ZModem/压缩/跳板机/隧道/x11/代理；认证分支 `selectFirst/select(1)/select(2)/selectLast`；末尾 `switchOnTab()`、`hideOnEscape()` |
  | `void onStageInitialize(StageAdapter)` | 舞台初始化 | x11 控件 disable 绑定；按 OS 设置 sshAgent 文本 |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.connectUpdateTitle()` |
  | `void loadX11Cookie()` | 加载 x11 cookie | 同新增版，仅 Linux 且取 `xauth list` |
  | `void addHost()` | 添加主机 | `ShellViewFactory.addHost(this.shellConnect)`，回填 jumpTableView |
  | `void addJump()` | 添加跳板 | `ShellViewFactory.addJump()`，回填 jumpTableView |
  | `void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)`，refresh + updateOrder |
  | `void deleteJump()` | 删除跳板 | confirm 后 removeSelectedItem + `jumpConfigStore.delete(config)` + updateOrder |
  | `void moveJumpUp()` | 上移跳板 | TableViewUtil.moveUp + refresh + updateOrder |
  | `void moveJumpDown()` | 下移跳板 | TableViewUtil.moveDown + refresh + updateOrder |
  | `void addTunneling()` | 添加隧道 | `ShellViewFactory.addTunneling()`，回填 tunnelingTableView |
  | `void updateTunneling()` | 编辑隧道 | `ShellViewFactory.updateTunneling(config)`，refresh |
  | `void deleteTunneling()` | 删除隧道 | confirm 后 removeSelectedItem + `tunnelingConfigStore.delete(config)` |

- 调用链：`update → ShellConnectStore.replace → ShellEventUtil.connectUpdated → closeWindow`
- 调用链：`deleteJump → ShellJumpConfigStore.delete → jumpTableView.updateOrder`
- 调用链：`onWindowShown → getProp("shellConnect") → 回填控件`

## ShellAddSFTPConnectController

- 职责：SFTP 连接新增对话框，采集认证/代理/跳板机/压缩等配置并保存（无终端/X11/隧道）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | certificate | ChooseFileTextField | 证书文件路径 |
  | certificatePwd | PasswordTextField | 证书密码 |
  | sshAgent | ReadOnlyTextField | ssh 代理（agent）地址 |
  | key | ShellKeyComboBox | 密钥选择 |
  | tabPane | FXTabPane | tab 容器 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时 |
  | authMethod | ShellSSHAuthTypeComboBox | 认证方式 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理认证信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | jumpTableView | ShellJumpTableView | 跳板机配置表 |
  | enableCompress | FXCheckBox | 启用压缩 |
  | group | ShellGroup | 分组（prop "group"） |
  | connectStore | ShellConnectStore | 连接储存对象（final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 校验并拼装 host:port | 校验失败返回 null |
  | `ShellProxyConfig getProxyConfig()` | 组装代理配置 | new `ShellProxyConfig` 并回填 |
  | `void testConnect()` | 测试连通 | 组装临时 `ShellConnect`（type=sftp），调 `ShellConnectUtil.testConnect` |
  | `void add()` | 校验并新增 | 校验 host/userName/密码/证书/密钥与代理；名称空则用 host 占位；组装后 `connectStore.replace` 成功则 `ShellEventUtil.connectAdded`、`setProp("connect")`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件联动 | hostIp 拆分、authMethod 显隐、enableProxy、proxyAuthType（无 x11 逻辑） |
  | `void onWindowShown(WindowEvent)` | 窗口显示回调 | `group = getProp("group")`；`osType.selectType(ShellPrototype.SFTP)`；`switchOnTab()`、`hideOnEscape()` |
  | `void onStageInitialize(StageAdapter)` | 舞台初始化 | 按 OS 设置 sshAgent 文本（无 x11 disable 绑定） |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.connectAddTitle()` |
  | `void addHost()` | 添加主机 | `ShellViewFactory.addHost(null)`，回填 jumpTableView |
  | `void addJump()` | 添加跳板 | `ShellViewFactory.addJump()`，回填 jumpTableView |
  | `void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)`，refresh + updateOrder |
  | `void deleteJump()` | 删除跳板 | confirm 后 removeSelectedItem + updateOrder |
  | `void moveJumpUp()` | 上移跳板 | TableViewUtil.moveUp + refresh + updateOrder |
  | `void moveJumpDown()` | 下移跳板 | TableViewUtil.moveDown + refresh + updateOrder |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded → closeWindow`
- 调用链：`testConnect → getProxyConfig → ShellConnectUtil.testConnect`
- 调用链：`onWindowShown → osType.selectType(ShellPrototype.SFTP)`

## ShellUpdateSFTPConnectController

- 职责：SFTP 连接修改对话框，加载既有连接回填并在保存时更新。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | certificate | ChooseFileTextField | 证书文件路径 |
  | certificatePwd | PasswordTextField | 证书密码 |
  | sshAgent | ReadOnlyTextField | ssh 代理（agent）地址 |
  | key | ShellKeyComboBox | 密钥选择 |
  | tabPane | FXTabPane | tab 容器 |
  | shellConnect | ShellConnect | 待修改连接（prop "shellConnect"） |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时 |
  | authMethod | ShellSSHAuthTypeComboBox | 认证方式 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理认证信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | jumpTableView | ShellJumpTableView | 跳板机配置表 |
  | enableCompress | FXCheckBox | 启用压缩 |
  | connectStore | ShellConnectStore | 连接储存对象（final） |
  | jumpConfigStore | ShellJumpConfigStore | 跳板储存对象（final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 校验并拼装 host:port | 校验失败返回 null |
  | `ShellProxyConfig getProxyConfig()` | 组装代理配置 | 复用 `shellConnect.getProxyConfig()`，为空新建并 setIid 后回填 |
  | `void testConnect()` | 测试连通 | 组装临时 `ShellConnect`（type=sftp、setId）后 `ShellConnectUtil.testConnect` |
  | `void update()` | 校验并修改 | 校验 host/userName/密码/证书/密钥与代理后写入 `shellConnect`，`connectStore.replace` 成功后 `ShellEventUtil.connectUpdated`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件联动 | hostIp 拆分、authMethod 显隐、enableProxy、proxyAuthType |
  | `void onWindowShown(WindowEvent)` | 回填数据 | 取 prop "shellConnect"，回填 name/hostIp/remark/osType/hostPort/charset/connectTimeOut/认证/压缩/跳板机/代理；认证分支 selectFirst/select(1)/select(2)/selectLast；末尾 switchOnTab、hideOnEscape |
  | `void onStageInitialize(StageAdapter)` | 舞台初始化 | 按 OS 设置 sshAgent 文本 |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.connectUpdateTitle()` |
  | `void addHost()` | 添加主机 | `ShellViewFactory.addHost(this.shellConnect)`，回填 jumpTableView |
  | `void addJump()` | 添加跳板 | `ShellViewFactory.addJump()`，回填 jumpTableView |
  | `void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)`，refresh + updateOrder |
  | `void deleteJump()` | 删除跳板 | confirm 后 removeSelectedItem + `jumpConfigStore.delete(config)` + updateOrder |
  | `void moveJumpUp()` | 上移跳板 | TableViewUtil.moveUp + refresh + updateOrder |
  | `void moveJumpDown()` | 下移跳板 | TableViewUtil.moveDown + refresh + updateOrder |

- 调用链：`update → ShellConnectStore.replace → ShellEventUtil.connectUpdated → closeWindow`
- 调用链：`deleteJump → ShellJumpConfigStore.delete → jumpTableView.updateOrder`
- 调用链：`onWindowShown → getProp("shellConnect") → 回填控件`

## ShellAddFTPConnectController

- 职责：FTP 连接新增对话框，采集认证/SSL/被动模式/代理等配置并保存。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 容器 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | sslMode | FXCheckBox | ssl 模式 |
  | passiveMode | FXCheckBox | 被动模式 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理认证信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | group | ShellGroup | 分组（prop "group"） |
  | connectStore | ShellConnectStore | 连接储存对象（final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 校验并拼装 host:port | 校验失败返回 null |
  | `ShellProxyConfig getProxyConfig()` | 组装代理配置 | new `ShellProxyConfig` 并回填 |
  | `void testConnect()` | 测试连通 | 组装临时 `ShellConnect`（type=`ShellPrototype.FTP`、SSLMode、ftpPassiveMode、认证、代理）后 `ShellConnectUtil.testConnect` |
  | `void add()` | 校验并新增 | 校验 host/userName 与代理；名称空则用 host 占位；组装后 `connectStore.replace` 成功则 `ShellEventUtil.connectAdded`、`setProp("connect")`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件联动 | hostIp 拆分、enableProxy、proxyAuthType（无认证方式分支） |
  | `void onWindowShown(WindowEvent)` | 窗口显示回调 | `group = getProp("group")`；`osType.selectType(ShellPrototype.FTP)`；`switchOnTab()`、`hideOnEscape()` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.connectAddTitle()` |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded → closeWindow`
- 调用链：`testConnect → getProxyConfig → ShellConnectUtil.testConnect`

## ShellUpdateFTPConnectController

- 职责：FTP 连接修改对话框，加载既有连接回填并在保存时更新。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 容器 |
  | shellConnect | ShellConnect | 待修改连接（prop "shellConnect"） |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | sslMode | FXCheckBox | ssl 模式 |
  | passiveMode | FXCheckBox | 被动模式 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理认证信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | connectStore | ShellConnectStore | 连接储存对象（final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 校验并拼装 host:port | 校验失败返回 null |
  | `ShellProxyConfig getProxyConfig()` | 组装代理配置 | 复用 `shellConnect.getProxyConfig()`，为空新建并 setIid 后回填 |
  | `void testConnect()` | 测试连通 | 组装临时 `ShellConnect`（type=ftp、setId、SSLMode、ftpPassiveMode）后 `ShellConnectUtil.testConnect` |
  | `void update()` | 校验并修改 | 校验 host/userName 与代理后写入 `shellConnect`，`connectStore.replace` 成功后 `ShellEventUtil.connectUpdated`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件联动 | hostIp 拆分、enableProxy、proxyAuthType |
  | `void onWindowShown(WindowEvent)` | 回填数据 | 取 prop "shellConnect"，回填 name/hostIp/remark/osType/hostPort/charset/sslMode/passiveMode/connectTimeOut/认证/代理；末尾 switchOnTab、hideOnEscape |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.connectUpdateTitle()` |

- 调用链：`update → ShellConnectStore.replace → ShellEventUtil.connectUpdated → closeWindow`
- 调用链：`onWindowShown → getProp("shellConnect") → 回填 SSLMode/被动模式`

## ShellAddLocalConnectController

- 职责：本地终端连接新增对话框，采集名称/字符集/终端类型等轻量配置并插入。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tabPane | FXTabPane | tab 容器 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | charset | CharsetComboBox | 字符集 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt 修饰 |
  | termType | ShellTermTypeComboBox | 终端类型 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | group | ShellGroup | 分组（prop "group"） |
  | connectStore | ShellConnectStore | 连接储存对象（final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void add()` | 校验并新增 | `name.validate()` 通过后组装 `ShellConnect`（name/remark/osType/charset/backspaceType/altSendsEscape/termType），`setType("local")`、`setGroupId(group)`；`connectStore.insert` 成功则 `ShellEventUtil.connectAdded`、`setProp("connect")`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件联动 | 仅 `super.bindListeners()`（背景配置逻辑已注释） |
  | `void onWindowShown(WindowEvent)` | 窗口显示回调 | `group = getProp("group")`；`name` 默认 `I18nHelper.localTerminal()`；按 OS 选中 osType（Windows/Macos/Linux）；`switchOnTab()`、`hideOnEscape()` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.connectAddTitle()` |

- 调用链：`add → ShellConnectStore.insert → ShellEventUtil.connectAdded → closeWindow`（本地新增走 `insert`，与其它协议 `replace` 不同）
- 调用链：`onWindowShown → OSUtil 判断 → osType.select`

## ShellUpdateLocalConnectController

- 职责：本地终端连接修改对话框，加载既有连接回填并在保存时更新。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tabPane | FXTabPane | tab 容器 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | charset | CharsetComboBox | 字符集 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt 修饰 |
  | termType | ShellTermTypeComboBox | 终端类型 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | shellConnect | ShellConnect | 待修改连接（prop "shellConnect"） |
  | connectStore | ShellConnectStore | 连接储存对象（final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void update()` | 校验并修改 | `name.validate()` 通过后写入 `shellConnect`（name/remark/osType/charset/backspaceType/altSendsEscape/termType）；`connectStore.update` 成功则 `ShellEventUtil.connectUpdated`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件联动 | 仅 `super.bindListeners()`（背景配置逻辑已注释） |
  | `void onWindowShown(WindowEvent)` | 回填数据 | 取 prop "shellConnect"，回填 name/remark/osType/charset/termType/backspaceType/altSendsEscape；`switchOnTab()`、`hideOnEscape()` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.connectUpdateTitle()` |

- 调用链：`update → ShellConnectStore.update → ShellEventUtil.connectUpdated → closeWindow`（本地更新走 `update`）
- 调用链：`onWindowShown → getProp("shellConnect") → 回填控件`

## ShellAddTelnetConnectController

- 职责：Telnet 连接新增对话框控制器，采集表单、测试连接并保存新连接。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt 修饰 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | group | ShellGroup | 分组 |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 获取连接地址 | 先 `tabPane.select(0)`，校验 `hostPort`/`hostIp`，失败返回 null；成功返回 `hostIp + ":" + hostPort.getValue()` |
  | `private ShellProxyConfig getProxyConfig()` | 获取代理配置信息 | 新建 `ShellProxyConfig`，逐项填充 host/port/user/password/authType/protocol |
  | `@FXML private void testConnect()` | 测试连接 | 取 host 非空后构造 `ShellConnect`（type=telnet），调用 `ShellConnectUtil.testConnect(stage, shellConnect, timeout*1000)` |
  | `@FXML private void add()` | 添加信息 | 校验代理配置，名称默认取 host；`connectStore.replace(shellConnect)` 保存成功后 `ShellEventUtil.connectAdded`，`setProp("connect")` 并 `closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 文本变化时按 `:` 切分 ip/端口；`enableProxy` 开关启用/禁用 proxyTab 及认证框；`proxyAuthType` 变化切换认证框可用性 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `group` 属性、`osType.selectType(ShellPrototype.TELNET)`、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectAddTitle()` |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
- 调用链：`add → getHost → PortTextField.validate`
- 注：文件内 `enableBackground`/`backgroundTab`/`backgroundImage` 字段与 `chooseBackgroundImage()` 方法及背景校验分支均为注释死代码，已跳过。

## ShellUpdateTelnetConnectController

- 职责：Telnet 连接修改对话框控制器，回填已有连接、测试连接并保存修改。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | shellConnect | ShellConnect | ssh 信息（待修改连接） |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt 修饰 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 获取连接地址 | 校验 `hostPort`/`hostIp`，失败回到 tab0 并返回 null；成功返回 `hostIp:port` |
  | `private ShellProxyConfig getProxyConfig()` | 获取代理配置信息 | 复用 `shellConnect.getProxyConfig()`，为空则新建并 `setIid(shellConnect.getId())`，再逐项填充 |
  | `@FXML private void testConnect()` | 测试连接 | 构造临时 `ShellConnect`（type=telnet，`setId(shellConnect.getId())`），调用 `ShellConnectUtil.testConnect` |
  | `@FXML private void update()` | 修改信息 | 校验代理配置、名称默认 host；改写 `shellConnect` 各字段后 `connectStore.replace`，成功则 `ShellEventUtil.connectUpdated` 并 `closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 文本变化切分 ip/端口；代理开关与认证方式联动启用/禁用 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `shellConnect` 属性并回填 name/hostIp/remark/osType/hostPort/charset/connectTimeOut/backspaceType/altSendsEscape/认证与代理字段；`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectUpdateTitle()` |

- 调用链：`update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
- 调用链：`onWindowShown → getProp("shellConnect")`
- 注：`enableBackground`/`backgroundTab`/`backgroundImage` 及 `chooseBackgroundImage()` 为注释死代码，已跳过。

## ShellAddSerialConnectController

- 职责：串口（Serial）连接新增对话框控制器，采集串口参数并保存新连接。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tabPane | FXTabPane | tab 组件 |
  | name | ClearableTextField | 名称 |
  | portName | ShellSerialPortNameTextFiled | 端口名 |
  | baudRate | ShellSerialBaudRateTextFiled | 波特率 |
  | numDataBits | ShellSerialNumDataBitsComboBox | 数据位 |
  | parityBits | ShellSerialParityBitsComboBox | 校验位 |
  | numStopBits | ShellSerialNumStopBitsComboBox | 停止位 |
  | flowControl | ShellSerialFlowControlComboBox | 流控 |
  | remark | FXTextArea | 备注 |
  | charset | CharsetComboBox | 字符集 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt 修饰 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | group | ShellGroup | 分组 |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `@FXML private void testConnect()` | 测试连接 | 校验 `portName`，构造 `ShellConnect`（type=serial）填充串口参数，调用 `ShellConnectUtil.testConnect` |
  | `@FXML private void add()` | 添加连接信息 | 校验 `portName`/`baudRate`，名称默认取 portName；填充串口与终端字段；`connectStore.insert(shellConnect)` 成功后 `ShellEventUtil.connectAdded`、`setProp("connect")`、`closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | 仅调用 `super`，无额外监听（原背景配置绑定已注释） |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `group` 属性、`osType.selectType(ShellPrototype.SERIAL)`、`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectAddTitle()` |

- 调用链：`add → ShellConnectStore.insert → ShellEventUtil.connectAdded`
- 调用链：`testConnect → ShellConnectUtil.testConnect`
- 注：`enableBackground`/`backgroundTab`/`backgroundImage` 及 `chooseBackgroundImage()` 为注释死代码，已跳过。

## ShellUpdateSerialConnectController

- 职责：串口（Serial）连接修改对话框控制器，回填并保存串口连接修改。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tabPane | FXTabPane | tab 组件 |
  | name | ClearableTextField | 名称 |
  | portName | ShellSerialPortNameTextFiled | 端口名 |
  | baudRate | ShellSerialBaudRateTextFiled | 波特率 |
  | numDataBits | ShellSerialNumDataBitsComboBox | 数据位 |
  | parityBits | ShellSerialParityBitsComboBox | 校验位 |
  | numStopBits | ShellSerialNumStopBitsComboBox | 停止位 |
  | flowControl | ShellSerialFlowControlComboBox | 流控 |
  | remark | FXTextArea | 备注 |
  | charset | CharsetComboBox | 字符集 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt 修饰 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | shellConnect | ShellConnect | 连接（待修改） |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `@FXML private void testConnect()` | 测试连接 | 校验 `portName`，构造 type=serial 的 `ShellConnect` 并填充串口参数，调用 `ShellConnectUtil.testConnect` |
  | `@FXML private void update()` | 修改连接信息 | 校验 `portName`/`baudRate`，名称默认 portName；改写 `shellConnect` 后 `connectStore.update(shellConnect)`，成功则 `ShellEventUtil.connectUpdated` 并 `closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | 仅调用 `super`，无额外监听 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `shellConnect` 回填 name/remark/osType/charset/connectTimeOut/backspaceType/altSendsEscape 及串口字段（`parityBits.init`、`flowControl.init`、`numStopBits.init`、`numDataBits.init`）；`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectUpdateTitle()` |

- 调用链：`update → ShellConnectStore.update → ShellEventUtil.connectUpdated`
- 调用链：`onWindowShown → getProp("shellConnect")`
- 注：文件内 `enableBackground`/`backgroundTab`/`backgroundImage` 及 `chooseBackgroundImage()` 为注释死代码，已跳过。
- 缺陷：`update()` 中 `this.shellConnect.setSerialNumDataBits(numStopBits);`（约 236 行）将停止位误写入数据位，应为 `setSerialNumStopBits(numStopBits)`，导致停止位未被保存且数据位被覆盖。

## ShellAddVNCConnectController

- 职责：VNC 连接新增对话框控制器，采集 VNC 参数并保存新连接。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | password | PasswordTextField | 密码 |
  | cursor | ShellVNCCursorComboBox | 光标 |
  | encoding | ShellVNCEncodingComboBox | 编码 |
  | tabPane | FXTabPane | tab 组件 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | sslMode | FXCheckBox | ssl 模式 |
  | readonly | FXCheckBox | 只读模式 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | group | ShellGroup | 分组 |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 获取连接地址 | 校验 `hostPort`/`hostIp`，失败回到 tab0 返回 null；成功返回 `hostIp:port` |
  | `private ShellProxyConfig getProxyConfig()` | 获取代理配置信息 | 新建 `ShellProxyConfig` 并逐项填充 |
  | `@FXML private void testConnect()` | 测试连接 | 构造 type=vnc 的 `ShellConnect`，写入 sslMode/readonly，`putExtra("cursor"/"encoding")`，调用 `ShellConnectUtil.testConnect` |
  | `@FXML private void add()` | 添加信息 | 校验代理配置、名称默认 host；填充字段与扩展项后 `connectStore.replace`，成功则 `ShellEventUtil.connectAdded`、`setProp("connect")`、`closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 文本切分 ip/端口；代理开关与认证方式联动启用/禁用 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `group` 属性、`osType.selectType(ShellPrototype.VNC)`、`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectAddTitle()` |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
- 调用链：`add → getProxyConfig → ShellProxyAuthTypeComboBox.getAuthType`

## ShellUpdateVNCConnectController

- 职责：VNC 连接修改对话框控制器，回填并保存 VNC 连接修改。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | password | PasswordTextField | 密码 |
  | cursor | ShellVNCCursorComboBox | 光标 |
  | encoding | ShellVNCEncodingComboBox | 编码 |
  | tabPane | FXTabPane | tab 组件 |
  | shellConnect | ShellConnect | ssh 信息（待修改） |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | sslMode | FXCheckBox | ssl 模式 |
  | readonly | FXCheckBox | 只读模式 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 获取连接地址 | 校验 `hostPort`/`hostIp`，失败回到 tab0 返回 null；成功返回 `hostIp:port` |
  | `private ShellProxyConfig getProxyConfig()` | 获取代理配置信息 | 复用 `shellConnect.getProxyConfig()`，为空新建并 `setIid(id)`，再逐项填充 |
  | `@FXML private void testConnect()` | 测试连接 | 构造 type=vnc 临时 `ShellConnect`（`setId(shellConnect.getId())`），写入 ssl/readonly 与 extra，调用 `ShellConnectUtil.testConnect` |
  | `@FXML private void update()` | 修改信息 | 校验代理配置、名称默认 host；改写 `shellConnect` 及 extra 后 `connectStore.replace`，成功则 `ShellEventUtil.connectUpdated` 并 `closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 文本切分 ip/端口；代理开关与认证方式联动 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `shellConnect` 回填 name/hostIp/remark/osType/hostPort/charset/sslMode/readonly/connectTimeOut，读取 `cursor`/`encoding` extra、密码及代理配置；`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectUpdateTitle()` |

- 调用链：`update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
- 调用链：`onWindowShown → getProp("shellConnect")`

## ShellAddRDPConnectController

- 职责：RDP 连接新增对话框控制器，采集 RDP 参数并保存新连接。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | resolution | ClearableTextField | 分辨率 |
  | domain | ClearableTextField | 域 |
  | color | ShellRdpColorComboBox | 颜色 |
  | method | ShellRdpMethodComboBox | 方式 |
  | sslMode | FXCheckBox | ssl 模式 |
  | remoteAudio | FXCheckBox | 远程音频 |
  | redirectClipboard | FXCheckBox | 剪贴板同步 |
  | group | ShellGroup | 分组 |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 获取连接地址 | 校验 `hostPort`/`hostIp`，失败回到 tab0 返回 null；成功返回 `hostIp:port` |
  | `@FXML private void testConnect()` | 测试连接 | 构造 type=rdp 的 `ShellConnect`，写入 user/domain/sslMode/password 及 extra（color/method/remoteAudio/redirectClipboard），调用 `ShellConnectUtil.testConnect(stage, shellConnect, 3000)`（固定 3000ms） |
  | `@FXML private void add()` | 添加信息 | 名称默认 host；填充字段与 extra 后 `connectStore.replace`，成功则 `ShellEventUtil.connectAdded`、`setProp("connect")`、`closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 文本变化按 `:` 切分 ip/端口 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `group` 属性、`osType.selectType(ShellPrototype.RDP)`、`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectAddTitle()` |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
- 调用链：`testConnect → ShellConnectUtil.testConnect`

## ShellUpdateRDPConnectController

- 职责：RDP 连接修改对话框控制器，回填并保存 RDP 连接修改。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | shellConnect | ShellConnect | ssh 信息（待修改） |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | resolution | ClearableTextField | 分辨率 |
  | domain | ClearableTextField | 域 |
  | color | ShellRdpColorComboBox | 颜色 |
  | method | ShellRdpMethodComboBox | 方式 |
  | sslMode | FXCheckBox | ssl 模式 |
  | remoteAudio | FXCheckBox | 远程音频 |
  | redirectClipboard | FXCheckBox | 剪贴板同步 |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 获取连接地址 | 校验 `hostPort`/`hostIp`，失败回到 tab0 返回 null；成功返回 `hostIp:port` |
  | `@FXML private void testConnect()` | 测试连接 | 构造 type=rdp 临时 `ShellConnect` 并填充认证与 extra，调用 `ShellConnectUtil.testConnect(stage, shellConnect, 3000)` |
  | `@FXML private void update()` | 修改信息 | 名称默认 host；改写 `shellConnect` 字段与 extra 后 `connectStore.replace`，成功则 `ShellEventUtil.connectUpdated` 并 `closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 文本切分 ip/端口 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `shellConnect` 回填 name/hostIp/remark/osType/hostPort/domain/userName/password/sslMode，按 extra 是否存在回填 resolution/color/method/remoteAudio/redirectClipboard；`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectUpdateTitle()` |

- 调用链：`update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
- 调用链：`onWindowShown → getProp("shellConnect")`

## ShellAddRLoginConnectController

- 职责：RLogin 连接新增对话框控制器，采集表单、测试连接并保存新连接。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt 修饰 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | group | ShellGroup | 分组 |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 获取连接地址 | 校验 `hostPort`/`hostIp`，失败回到 tab0 返回 null；成功返回 `hostIp:port` |
  | `private ShellProxyConfig getProxyConfig()` | 获取代理配置信息 | 新建 `ShellProxyConfig` 并逐项填充 |
  | `@FXML private void testConnect()` | 测试连接 | 构造 type=rlogin 的 `ShellConnect`，写入认证与代理后调用 `ShellConnectUtil.testConnect` |
  | `@FXML private void add()` | 添加信息 | 校验代理配置、名称默认 host；填充字段后 `connectStore.replace`，成功则 `ShellEventUtil.connectAdded`、`setProp("connect")`、`closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 文本切分 ip/端口；代理开关与认证方式联动启用/禁用 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `group` 属性、`osType.selectType(ShellPrototype.RLOGIN)`、`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectAddTitle()` |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
- 调用链：`add → getHost → PortTextField.validate`
- 注：文件内 `enableBackground`/`backgroundTab`/`backgroundImage` 及 `chooseBackgroundImage()` 为注释死代码，已跳过。

## ShellUpdateRLoginConnectController

- 职责：RLogin 连接修改对话框控制器，回填已有连接、测试连接并保存修改。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | shellConnect | ShellConnect | ssh 信息（待修改连接） |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt 修饰 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 获取连接地址 | 校验 `hostPort`/`hostIp`，失败回到 tab0 返回 null；成功返回 `hostIp:port` |
  | `private ShellProxyConfig getProxyConfig()` | 获取代理配置信息 | 复用 `shellConnect.getProxyConfig()`，为空新建并 `setIid(id)`，再逐项填充 |
  | `@FXML private void testConnect()` | 测试连接 | 构造 type=rlogin 临时 `ShellConnect`（`setId(shellConnect.getId())`），写入认证与代理，调用 `ShellConnectUtil.testConnect` |
  | `@FXML private void update()` | 修改信息 | 校验代理配置、名称默认 host；改写 `shellConnect` 各字段后 `connectStore.replace`，成功则 `ShellEventUtil.connectUpdated` 并 `closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 文本切分 ip/端口；代理开关与认证方式联动 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `shellConnect` 回填 name/hostIp/remark/osType/hostPort/charset/connectTimeOut/backspaceType/altSendsEscape/认证与代理字段；`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectUpdateTitle()` |

- 调用链：`update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
- 调用链：`onWindowShown → getProp("shellConnect")`
- 注：`enableBackground`/`backgroundTab`/`backgroundImage` 及 `chooseBackgroundImage()` 为注释死代码，已跳过。

## ShellAddSMBConnectController

- 职责：SMB 连接新增对话框控制器，采集共享名/域等参数并保存新连接。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ShellSMBUserTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | shareName | ClearableTextField | 共享名称 |
  | domain | ClearableTextField | 域 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | group | ShellGroup | 分组 |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 获取连接地址 | 校验 `hostPort`/`hostIp`，失败回到 tab0 返回 null；成功返回 `hostIp:port` |
  | `private ShellProxyConfig getProxyConfig()` | 获取代理配置信息 | 新建 `ShellProxyConfig` 并逐项填充 |
  | `@FXML private void testConnect()` | 测试连接 | 构造 type=smb 的 `ShellConnect`，写入认证、代理及 domain/shareName，调用 `ShellConnectUtil.testConnect` |
  | `@FXML private void add()` | 添加信息 | 校验 `userName`/`shareName` 与代理配置、名称默认 host；填充字段后 `connectStore.replace`，成功则 `ShellEventUtil.connectAdded`、`setProp("connect")`、`closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 文本切分 ip/端口；代理开关与认证方式联动启用/禁用 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `group` 属性、`osType.selectType(ShellPrototype.SMB)`、`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectAddTitle()` |
  | `@Override public void destroy()` | 销毁 | 调用 `userName.destroy()` 后 `super.destroy()` |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
- 调用链：`testConnect → ShellConnectUtil.testConnect`

## ShellUpdateSMBConnectController

- 职责：SMB 连接修改对话框控制器，回填并保存 SMB 连接修改。

- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ShellSMBUserTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | shellConnect | ShellConnect | ssh 信息（待修改） |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | shareName | ClearableTextField | 共享名称 |
  | domain | ClearableTextField | 域 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | connectStore | ShellConnectStore | ssh 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 获取连接地址 | 校验 `hostPort`/`hostIp`，失败回到 tab0 返回 null；成功返回 `hostIp:port` |
  | `private ShellProxyConfig getProxyConfig()` | 获取代理配置信息 | 复用 `shellConnect.getProxyConfig()`，为空新建并 `setIid(id)`，再逐项填充 |
  | `@FXML private void testConnect()` | 测试连接 | 构造 type=smb 临时 `ShellConnect`（`setId(shellConnect.getId())`），写入认证、代理及 domain/shareName，调用 `ShellConnectUtil.testConnect` |
  | `@FXML private void update()` | 修改信息 | 校验 `userName`/`shareName` 与代理配置、名称默认 host；改写 `shellConnect` 后 `connectStore.replace`，成功则 `ShellEventUtil.connectUpdated` 并 `closeWindow()` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 文本切分 ip/端口；代理开关与认证方式联动 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口展示 | 取 `shellConnect` 回填 name/hostIp/remark/osType/hostPort/charset/connectTimeOut/认证、domain/shareName 及代理配置；`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | 返回 `I18nHelper.connectUpdateTitle()` |
  | `@Override public void destroy()` | 销毁 | 调用 `userName.destroy()` 后 `super.destroy()` |

- 调用链：`update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
- 调用链：`onWindowShown → getProp("shellConnect")`

## ShellAddMysqlConnectController

- 职责：MySQL 连接新增对话框控制器，采集表单并保存新连接。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | name | ClearableTextField | 名称 |
  | env | FXTextArea | 环境 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | group | ShellGroup | 分组（非 FXML，来自 prop） |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | sslMode | FXCheckBox | ssl 模式 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | jumpTableView | ShellJumpTableView | 跳板机配置 |
  | connectStore | ShellConnectStore（final） | 连接储存对象，取 `ShellConnectStore.INSTANCE` |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 校验并拼接连接地址 | 先 `tabPane.select(0)`，`hostPort`/`hostIp` 校验失败返回 null，否则返回 `hostIp + ":" + hostPort.getValue()` |
  | `private ShellProxyConfig getProxyConfig()` | 组装代理配置 | `new ShellProxyConfig`，set host/port/user/password/authType/protocol |
  | `@FXML private void testConnect()` | 测试连接 | `getHost`；新建 ShellConnect 设 `ShellPrototype.MYSQL`、环境、ssl、认证、跳板、代理；`ShellConnectUtil.testConnect(this.stage, shellConnect, timeout * 1000)` |
  | `@FXML private void add()` | 添加信息 | `getHost` → 校验 userName/password → 若开启代理则校验代理配置并 `tabPane.select(proxyTab)` → 名称为空时以 host 命名 → 构造 ShellConnect 并设类型/分组 → `connectStore.replace` 成功则广播、`MessageBox.okToast`、`setProp("connect")`、`closeWindow` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 文本含“:”时拆分 ip/port；`enableProxy.selectedChanged` 启停 proxyTab；`proxyAuthType.selectedIndexChanged` 启停 proxyAuthInfoBox |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口显示 | 取 prop group；`osType.selectType(ShellPrototype.MYSQL)`；`stage.switchOnTab()`；`stage.hideOnEscape()` |
  | `@Override public String getViewTitle()` | 视图标题 | `I18nHelper.connectAddTitle()` |
  | `@FXML private void addHost()` | 添加主机 | `ShellViewFactory.addHost(null)`，取 `jumpConfig` 加入 jumpTableView 并 `updateOrder` |
  | `@FXML private void addJump()` | 添加跳板 | `ShellViewFactory.addJump()`，取 `jumpConfig` 加入表格 |
  | `@FXML private void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)`，有返回则 refresh+updateOrder |
  | `@FXML private void deleteJump()` | 删除跳板 | 确认后 `jumpTableView.removeSelectedItem()` + updateOrder |
  | `@FXML private void moveJumpUp()` | 上移跳板 | `TableViewUtil.moveUp` + refresh + updateOrder |
  | `@FXML private void moveJumpDown()` | 下移跳板 | `TableViewUtil.moveDown` + refresh + updateOrder |
  | `@Override public void onStageInitialize(StageAdapter stage)` | 阶段初始化 | `env.setText(ShellMysqlHelper.defaultEnvironment())` 设置默认环境 |

- 调用链：
  - `add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
  - `testConnect → ShellConnectUtil.testConnect`
  - `onStageInitialize → ShellMysqlHelper.defaultEnvironment`

## ShellUpdateMysqlConnectController

- 职责：MySQL 连接修改对话框控制器，回填并更新既有连接。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | shellConnect | ShellConnect | 连接信息（prop 传入） |
  | name | ClearableTextField | 名称 |
  | env | FXTextArea | 环境 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | sslMode | FXCheckBox | ssl 模式 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | jumpTableView | ShellJumpTableView | 跳板机配置 |
  | connectStore | ShellConnectStore（final） | 连接储存对象 |
  | jumpConfigStore | ShellJumpConfigStore（final） | 跳板储存对象，删除跳板时使用 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 校验并拼接连接地址 | 同新增版，`hostPort`/`hostIp` 校验失败切回 tab0 并返回 null |
  | `private ShellProxyConfig getProxyConfig()` | 组装代理配置 | 复用 `shellConnect.getProxyConfig()`，为空时新建并 `setIid(shellConnect.getId())`，再覆盖各字段 |
  | `@FXML private void testConnect()` | 测试连接 | 新建临时 ShellConnect 设 `MYSQL`、`setId(this.shellConnect.getId())`、认证/ssl/跳板/代理；`ShellConnectUtil.testConnect` |
  | `@FXML private void update()` | 修改信息 | 校验 host/user/password/代理 → 默认名 → 直接改写 `this.shellConnect` 各字段 → `connectStore.replace` 成功则 `ShellEventUtil.connectUpdated`、okToast、closeWindow |
  | `@Override protected void bindListeners()` | 绑定监听器 | 与新增版一致：hostIp 拆分、代理开关、代理认证联动 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口显示 | 取 prop `shellConnect`；回填 name/hostIp/remark/osType/hostPort/connectTimeOut/user/password/env/ssl/跳板/代理；`switchOnTab`、`hideOnEscape` |
  | `@Override public String getViewTitle()` | 视图标题 | `I18nHelper.connectUpdateTitle()` |
  | `@FXML private void addHost()` | 添加主机 | `ShellViewFactory.addHost(this.shellConnect)` |
  | `@FXML private void addJump()` | 添加跳板 | `ShellViewFactory.addJump()` |
  | `@FXML private void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)` |
  | `@FXML private void deleteJump()` | 删除跳板 | 确认后 `removeSelectedItem` + `jumpConfigStore.delete(config)` + updateOrder |
  | `@FXML private void moveJumpUp()` | 上移跳板 | `TableViewUtil.moveUp` + refresh + updateOrder |
  | `@FXML private void moveJumpDown()` | 下移跳板 | `TableViewUtil.moveDown` + refresh + updateOrder |
  | `@Override public void onStageInitialize(StageAdapter stage)` | 阶段初始化 | `env.setText(ShellMysqlHelper.defaultEnvironment())` |

- 调用链：
  - `update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
  - `deleteJump → ShellJumpConfigStore.delete → ShellJumpTableView.updateOrder`
  - `testConnect → ShellConnectUtil.testConnect`

## ShellAddDamengConnectController

- 职责：达梦连接新增对话框控制器，采集表单并保存新连接。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | name | ClearableTextField | 名称 |
  | env | FXTextArea | 环境 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | group | ShellGroup | 分组 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | sslMode | FXCheckBox | ssl 模式 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | jumpTableView | ShellJumpTableView | 跳板机配置 |
  | connectStore | ShellConnectStore（final） | 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 校验并拼接连接地址 | 同 mysql 新增版 |
  | `private ShellProxyConfig getProxyConfig()` | 组装代理配置 | `new ShellProxyConfig` 并填充字段 |
  | `@FXML private void testConnect()` | 测试连接 | 类型设 `ShellPrototype.DAMENG`，其余同 mysql 新增版 |
  | `@FXML private void add()` | 添加信息 | 校验 host/user/password/代理 → 默认名 → 构造 ShellConnect 设 `DAMENG` 与 groupId → `connectStore.replace` 成功后 `connectAdded` + okToast + setProp + closeWindow |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 拆分、代理开关、代理认证联动 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口显示 | 取 prop group；`osType.selectType(ShellPrototype.DAMENG)`；switchOnTab；hideOnEscape |
  | `@Override public String getViewTitle()` | 视图标题 | `I18nHelper.connectAddTitle()` |
  | `@FXML private void addHost()` | 添加主机 | `ShellViewFactory.addHost(null)` |
  | `@FXML private void addJump()` | 添加跳板 | `ShellViewFactory.addJump()` |
  | `@FXML private void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)` |
  | `@FXML private void deleteJump()` | 删除跳板 | 确认后 `removeSelectedItem` + updateOrder |
  | `@FXML private void moveJumpUp()` | 上移跳板 | `TableViewUtil.moveUp` + refresh + updateOrder |
  | `@FXML private void moveJumpDown()` | 下移跳板 | `TableViewUtil.moveDown` + refresh + updateOrder |
  | `@Override public void onStageInitialize(StageAdapter stage)` | 阶段初始化 | `env.setText(ShellDamengHelper.defaultEnvironment())` |

- 调用链：
  - `add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
  - `testConnect → ShellConnectUtil.testConnect`
  - `onStageInitialize → ShellDamengHelper.defaultEnvironment`

## ShellUpdateDamengConnectController

- 职责：达梦连接修改对话框控制器，回填并更新既有连接。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | shellConnect | ShellConnect | 连接信息 |
  | name | ClearableTextField | 名称 |
  | env | FXTextArea | 环境 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | sslMode | FXCheckBox | ssl 模式 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | jumpTableView | ShellJumpTableView | 跳板机配置 |
  | connectStore | ShellConnectStore（final） | 连接储存对象 |
  | jumpConfigStore | ShellJumpConfigStore（final） | 跳板储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 校验并拼接连接地址 | 同新增版 |
  | `private ShellProxyConfig getProxyConfig()` | 组装代理配置 | 复用现有 proxyConfig，为空则新建并 setIid |
  | `@FXML private void testConnect()` | 测试连接 | 类型设 `DAMENG`，`setId(this.shellConnect.getId())` |
  | `@FXML private void update()` | 修改信息 | 校验后改写 `this.shellConnect` → `connectStore.replace` 成功则 `connectUpdated` + okToast + closeWindow |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 拆分、代理开关、代理认证联动 |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口显示 | 取 prop shellConnect 并回填 name/hostIp/remark/osType/hostPort/connectTimeOut/认证/env/ssl/跳板/代理 |
  | `@Override public String getViewTitle()` | 视图标题 | `I18nHelper.connectUpdateTitle()` |
  | `@FXML private void addHost()` | 添加主机 | `ShellViewFactory.addHost(this.shellConnect)` |
  | `@FXML private void addJump()` | 添加跳板 | `ShellViewFactory.addJump()` |
  | `@FXML private void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)` |
  | `@FXML private void deleteJump()` | 删除跳板 | 确认后 `removeSelectedItem` + `jumpConfigStore.delete(config)` + updateOrder |
  | `@FXML private void moveJumpUp()` | 上移跳板 | `TableViewUtil.moveUp` + refresh + updateOrder |
  | `@FXML private void moveJumpDown()` | 下移跳板 | `TableViewUtil.moveDown` + refresh + updateOrder |
  | `@Override public void onStageInitialize(StageAdapter stage)` | 阶段初始化 | `env.setText(ShellDamengHelper.defaultEnvironment())` |

- 调用链：
  - `update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
  - `deleteJump → ShellJumpConfigStore.delete → ShellJumpTableView.updateOrder`
  - `testConnect → ShellConnectUtil.testConnect`

## ShellAddMongoConnectController

- 职责：MongoDB 连接新增对话框控制器，采集表单并保存新连接。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | authDatabase | ClearableTextField | 认证数据库 |
  | specifiedDatabase | ClearableTextField | 指定数据库 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | group | ShellGroup | 分组 |
  | jumpTableView | ShellJumpTableView | 跳板机配置 |
  | enableSSL | FXToggleSwitch | ssl 模式（特有，开关而非复选框） |
  | sslTab | FXTab | ssl 面板（特有） |
  | sslClientKey | ChooseFileTextField | ssl 客户端密钥 |
  | sslClientCrt | ChooseFileTextField | ssl 客户端证书 |
  | sslClientPwd | PasswordTextField | ssl 客户端密码 |
  | sslCaCrt | ChooseFileTextField | ssl ca 证书 |
  | connectStore | ShellConnectStore（final） | 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 校验并拼接连接地址 | 同 mysql 新增版 |
  | `private ShellProxyConfig getProxyConfig()` | 组装代理配置 | `new ShellProxyConfig` 并填充字段 |
  | `private ShellSSLConfig getSSLConfig()` | 组装 ssl 配置 | `new ShellSSLConfig`，set caCrt/clientCrt/clientKey/clientPwd |
  | `@FXML private void testConnect()` | 测试连接 | 类型设 `MONGO`，另设 specifiedDatabase、authDatabase、sslConfig、SSLMode |
  | `@FXML private void add()` | 添加信息 | host 校验；authDatabase 非空时才校验 user/password；校验代理；默认名；构造 ShellConnect 设 `MONGO`/groupId/数据库/ssl/代理；`connectStore.replace` 成功后 `connectAdded` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 拆分、代理开关、代理认证联动，另加 `enableSSL.selectedChanged` 启停 sslTab |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口显示 | 取 prop group；`osType.selectType(ShellPrototype.MONGO)`；switchOnTab；hideOnEscape |
  | `@Override public String getViewTitle()` | 视图标题 | `I18nHelper.connectAddTitle()` |
  | `@FXML private void addHost()` | 添加主机 | `ShellViewFactory.addHost(null)` |
  | `@FXML private void addJump()` | 添加跳板 | `ShellViewFactory.addJump()` |
  | `@FXML private void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)` |
  | `@FXML private void deleteJump()` | 删除跳板 | 确认后 `removeSelectedItem` + updateOrder |
  | `@FXML private void moveJumpUp()` | 上移跳板 | `TableViewUtil.moveUp` + refresh + updateOrder |
  | `@FXML private void moveJumpDown()` | 下移跳板 | `TableViewUtil.moveDown` + refresh + updateOrder |

- 调用链：
  - `add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
  - `testConnect → ShellConnectUtil.testConnect`
  - `add → getSSLConfig → ShellConnect.setSslConfig`

## ShellUpdateMongoConnectController

- 职责：MongoDB 连接修改对话框控制器，回填并更新既有连接。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | shellConnect | ShellConnect | 连接信息 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | authDatabase | ClearableTextField | 认证数据库 |
  | specifiedDatabase | ClearableTextField | 指定数据库 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | jumpTableView | ShellJumpTableView | 跳板机配置 |
  | enableSSL | FXToggleSwitch | ssl 模式 |
  | sslTab | FXTab | ssl 面板 |
  | sslClientKey | ChooseFileTextField | ssl 客户端密钥 |
  | sslClientCrt | ChooseFileTextField | ssl 客户端证书 |
  | sslClientPwd | PasswordTextField | ssl 客户端密码 |
  | sslCaCrt | ChooseFileTextField | ssl ca 证书 |
  | connectStore | ShellConnectStore（final） | 连接储存对象 |
  | jumpConfigStore | ShellJumpConfigStore（final） | 跳板储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 校验并拼接连接地址 | 同新增版 |
  | `private ShellProxyConfig getProxyConfig()` | 组装代理配置 | 复用现有 proxyConfig，为空则新建并 setIid |
  | `private ShellSSLConfig getSSLConfig()` | 组装 ssl 配置 | 复用现有 sslConfig，为空则新建并 setIid |
  | `@FXML private void testConnect()` | 测试连接 | 类型设 `MONGO`，`setId(this.shellConnect.getId())` |
  | `@FXML private void update()` | 修改信息 | authDatabase 非空才校验认证；改写 `this.shellConnect` → `connectStore.replace` 成功则 `connectUpdated` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 拆分、代理开关、代理认证联动、`enableSSL` 启停 sslTab |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口显示 | 取 prop shellConnect；回填 name/hostIp/remark/osType/hostPort/connectTimeOut/specifiedDatabase/authDatabase/代理/跳板/ssl |
  | `@Override public String getViewTitle()` | 视图标题 | `I18nHelper.connectUpdateTitle()` |
  | `@FXML private void addHost()` | 添加主机 | `ShellViewFactory.addHost(this.shellConnect)` |
  | `@FXML private void addJump()` | 添加跳板 | `ShellViewFactory.addJump()` |
  | `@FXML private void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)` |
  | `@FXML private void deleteJump()` | 删除跳板 | 确认后 `removeSelectedItem` + `jumpConfigStore.delete(config)` + updateOrder |
  | `@FXML private void moveJumpUp()` | 上移跳板 | `TableViewUtil.moveUp` + refresh + updateOrder |
  | `@FXML private void moveJumpDown()` | 下移跳板 | `TableViewUtil.moveDown` + refresh + updateOrder |

- 调用链：
  - `update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
  - `deleteJump → ShellJumpConfigStore.delete → ShellJumpTableView.updateOrder`
  - `testConnect → ShellConnectUtil.testConnect`

## ShellAddRedisConnectController

- 职责：Redis 连接新增对话框控制器，采集表单并保存新连接。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | executeTimOut | NumberTextField | 执行超时时间（特有） |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | group | ShellGroup | 分组 |
  | jumpTableView | ShellJumpTableView | 跳板机配置 |
  | readonlyMode | FXCheckBox | 只读模式（特有） |
  | enableSSL | FXToggleSwitch | ssl 模式 |
  | sslTab | FXTab | ssl 面板 |
  | sslClientKey | ChooseFileTextField | ssl 客户端密钥 |
  | sslClientCrt | ChooseFileTextField | ssl 客户端证书 |
  | sslClientPwd | PasswordTextField | ssl 客户端密码 |
  | sslCaCrt | ChooseFileTextField | ssl ca 证书 |
  | connectStore | ShellConnectStore（final） | 连接储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 校验并拼接连接地址 | 同 mysql 新增版 |
  | `private ShellProxyConfig getProxyConfig()` | 组装代理配置 | `new ShellProxyConfig` 并填充字段 |
  | `private ShellSSLConfig getSSLConfig()` | 组装 ssl 配置 | `new ShellSSLConfig`，set caCrt/clientCrt/clientKey/clientPwd |
  | `@FXML private void testConnect()` | 测试连接 | 类型设 `REDIS`，另设 sslConfig/SSLMode/代理（无 env） |
  | `@FXML private void add()` | 添加信息 | host 校验 → 校验代理 → 默认名 → 构造 ShellConnect 设 `REDIS`/groupId/executeTimeOut/connectTimeOut/readonly/ssl/代理 → `connectStore.replace` 成功后 `connectAdded` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 拆分、代理开关、代理认证联动、`enableSSL` 启停 sslTab |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口显示 | 取 prop group；`osType.selectType(ShellPrototype.REDIS)`；switchOnTab；hideOnEscape |
  | `@Override public String getViewTitle()` | 视图标题 | `I18nHelper.connectAddTitle()` |
  | `@FXML private void addHost()` | 添加主机 | `ShellViewFactory.addHost(null)` |
  | `@FXML private void addJump()` | 添加跳板 | `ShellViewFactory.addJump()` |
  | `@FXML private void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)` |
  | `@FXML private void deleteJump()` | 删除跳板 | 确认后 `removeSelectedItem` + updateOrder |
  | `@FXML private void moveJumpUp()` | 上移跳板 | `TableViewUtil.moveUp` + refresh + updateOrder |
  | `@FXML private void moveJumpDown()` | 下移跳板 | `TableViewUtil.moveDown` + refresh + updateOrder |

- 调用链：
  - `add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
  - `testConnect → ShellConnectUtil.testConnect`
  - `add → getSSLConfig → ShellConnect.setSslConfig`

- 死代码说明：`add()` 中用户名校验被整段注释（`// if (!this.userName.validate()) { return; }`），故新增时不校验 userName。

## ShellUpdateRedisConnectController

- 职责：Redis 连接修改对话框控制器，回填并更新既有连接。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab 组件 |
  | shellConnect | ShellConnect | 连接信息 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接 ip |
  | hostPort | PortTextField | 连接端口 |
  | executeTimOut | NumberTextField | 执行超时时间 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | jumpTableView | ShellJumpTableView | 跳板机配置 |
  | readonlyMode | FXCheckBox | 只读模式 |
  | enableSSL | FXToggleSwitch | ssl 模式 |
  | sslTab | FXTab | ssl 面板 |
  | sslClientKey | ChooseFileTextField | ssl 客户端密钥 |
  | sslClientCrt | ChooseFileTextField | ssl 客户端证书 |
  | sslClientPwd | PasswordTextField | ssl 客户端密码 |
  | sslCaCrt | ChooseFileTextField | ssl ca 证书 |
  | connectStore | ShellConnectStore（final） | 连接储存对象 |
  | jumpConfigStore | ShellJumpConfigStore（final） | 跳板储存对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `private String getHost()` | 校验并拼接连接地址 | 同新增版 |
  | `private ShellProxyConfig getProxyConfig()` | 组装代理配置 | 复用现有 proxyConfig，为空则新建并 setIid |
  | `private ShellSSLConfig getSSLConfig()` | 组装 ssl 配置 | 复用现有 sslConfig，为空则新建并 setIid |
  | `@FXML private void testConnect()` | 测试连接 | 类型设 `REDIS`，`setId(this.shellConnect.getId())` |
  | `@FXML private void update()` | 修改信息 | 校验代理 → 默认名 → 改写 `this.shellConnect`（含 executeTimeOut/connectTimeOut/readonly/ssl/代理）→ `connectStore.replace` 成功则 `connectUpdated` |
  | `@Override protected void bindListeners()` | 绑定监听器 | hostIp 拆分、代理开关、代理认证联动、`enableSSL` 启停 sslTab |
  | `@Override public void onWindowShown(WindowEvent event)` | 窗口显示 | 取 prop shellConnect；回填 name/hostIp/remark/osType/hostPort/readonlyMode/executeTimOut/connectTimeOut/认证/代理/跳板/ssl |
  | `@Override public String getViewTitle()` | 视图标题 | `I18nHelper.connectUpdateTitle()` |
  | `@FXML private void addHost()` | 添加主机 | `ShellViewFactory.addHost(this.shellConnect)` |
  | `@FXML private void addJump()` | 添加跳板 | `ShellViewFactory.addJump()` |
  | `@FXML private void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)` |
  | `@FXML private void deleteJump()` | 删除跳板 | 确认后 `removeSelectedItem` + `jumpConfigStore.delete(config)` + updateOrder |
  | `@FXML private void moveJumpUp()` | 上移跳板 | `TableViewUtil.moveUp` + refresh + updateOrder |
  | `@FXML private void moveJumpDown()` | 下移跳板 | `TableViewUtil.moveDown` + refresh + updateOrder |

- 调用链：
  - `update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
  - `deleteJump → ShellJumpConfigStore.delete → ShellJumpTableView.updateOrder`
  - `testConnect → ShellConnectUtil.testConnect`

- 死代码说明：`update()` 中用户名校验被整段注释（`// if (!this.userName.validate()) { return; }`），故修改时不校验 userName。

## ShellAddZKConnectController

- 职责：Zookeeper 连接新增对话框控制器，采集名称/主机端口/超时/只读模式/跳板机/代理/SASL 参数，测试并保存新连接。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tabPane | FXTabPane | tab组件 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接ip |
  | hostPort | PortTextField | 连接端口 |
  | executeTimOut | NumberTextField | 执行超时时间 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | group | ShellGroup | 分组 |
  | jumpTableView | ShellJumpTableView | 跳板机配置 |
  | readonlyMode | FXCheckBox | 只读模式 |
  | saslTab | FXTab | sasl面板 |
  | saslAuth | FXToggleSwitch | 开启sasl |
  | saslType | ShellZKSASLTypeComboBox | sasl类型 |
  | saslUser | ClearableTextField | sasl用户 |
  | saslPassword | ClearableTextField | sasl密码 |
  | connectStore | ShellConnectStore | ssh连接储存对象（`final`，取 `ShellConnectStore.INSTANCE`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 组合 host:port 并校验 | `hostIp.getTextTrim()`、`tabPane.select(0)`；`hostPort.validate()`/`hostIp.validate()` 失败返回 null；返回 `hostIp + ":" + hostPort.getValue()` |
  | `ShellProxyConfig getProxyConfig()` | 构建代理配置 | `new ShellProxyConfig()`；setHost/setPort/setUser/setPassword/setAuthType/setProtocol |
  | `ShellZKSASLConfig getSASLConfig()` | 构建 SASL 认证配置 | `new ShellZKSASLConfig()`；setUserName/setType/setPassword |
  | `void testConnect()` | 测试连接 | `getHost()`；`new ShellConnect()` setType("Zookeeper")/setHost/setConnectTimeOut/setJumpConfigs/setSaslAuth/setSaslConfig/setProxyConfig/setEnableProxy；`ShellConnectUtil.testConnect(stage, shellConnect, timeout*1000)` |
  | `void add()` | 新增保存连接 | `getHost()`；`enableProxy` 时校验 proxyHost/proxyPort 及密码认证；名称空则以 `host.replace(":","_")` 命名；`new ShellConnect()` 填充全部字段（含 readonly/jumpConfigs/proxy/sasl/groupId）；`connectStore.replace(...)` 成功后 `ShellEventUtil.connectAdded`、`MessageBox.okToast`、`setProp("connect")`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件监听 | `hostIp.addTextChangeListener` 遇 ":" 拆分 ip/port；`enableProxy.selectedChanged` 启停 `NodeGroupUtil.enable/disable(proxyTab,"proxy")` 与 proxyAuthInfoBox；`proxyAuthType.selectedIndexChanged`；`saslAuth.selectedChanged` 启停 saslTab |
  | `void onWindowShown(WindowEvent event)` | 窗口显示初始化 | `getProp("group")`；`osType.selectType(ShellPrototype.ZOOKEEPER)`；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.connectAddTitle()` |
  | `void addHost()` | 添加主机跳板 | `ShellViewFactory.addHost(null)`；取 prop "jumpConfig" 后 `jumpTableView.addItem(...)`、`updateOrder()` |
  | `void addJump()` | 添加跳板 | `ShellViewFactory.addJump()`；取 prop "jumpConfig" 后 addItem/updateOrder |
  | `void updateJump()` | 编辑跳板 | `jumpTableView.getSelectedItem()`；`ShellViewFactory.updateJump(config)`；`jumpTableView.refresh()`、`updateOrder()` |
  | `void deleteJump()` | 删除跳板 | `MessageBox.confirm(...)`；`jumpTableView.removeSelectedItem()`、`updateOrder()`（仅移除界面项） |
  | `void moveJumpUp()` | 上移跳板 | `TableViewUtil.moveUp(jumpTableView)`；refresh/updateOrder |
  | `void moveJumpDown()` | 下移跳板 | `TableViewUtil.moveDown(jumpTableView)`；refresh/updateOrder |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
- 调用链：`testConnect → ShellConnectUtil.testConnect`
- 调用链：`add → getSASLConfig → ShellZKSASLConfig.setType`

## ShellUpdateZKConnectController

- 职责：Zookeeper 连接修改对话框控制器，回填已有连接并更新保存，含跳板机删除同步与 SASL 配置清理。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tabPane | FXTabPane | tab组件 |
  | shellConnect | ShellConnect | 待修改的连接信息 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接ip |
  | hostPort | PortTextField | 连接端口 |
  | executeTimOut | NumberTextField | 执行超时时间 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | jumpTableView | ShellJumpTableView | 跳板机配置 |
  | readonlyMode | FXCheckBox | 只读模式 |
  | saslTab | FXTab | sasl面板 |
  | saslAuth | FXToggleSwitch | 开启sasl |
  | saslType | ShellZKSASLTypeComboBox | sasl类型 |
  | saslUser | ClearableTextField | sasl用户 |
  | saslPassword | ClearableTextField | sasl密码 |
  | connectStore | ShellConnectStore | ssh连接储存对象（`final`） |
  | jumpConfigStore | ShellJumpConfigStore | ssh跳板储存对象（`final`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 组合 host:port 并校验 | 同新增类；`hostPort.validate()`/`hostIp.validate()` 失败返回 null |
  | `ShellProxyConfig getProxyConfig()` | 复用/构建代理配置 | 取 `shellConnect.getProxyConfig()`，为空则 `new ShellProxyConfig()` 并 `setIid(shellConnect.getId())`；再 setHost/setPort/setUser/setPassword/setAuthType/setProtocol |
  | `ShellZKSASLConfig getSASLConfig()` | 构建 SASL 认证配置 | `new ShellZKSASLConfig()`；setUserName/setType/setPassword |
  | `void testConnect()` | 测试连接 | `getHost()`；`new ShellConnect()` setType("Zookeeper")/setHost/setConnectTimeOut/`setId(shellConnect.getId())`/setJumpConfigs/setSaslAuth/setSaslConfig/setProxyConfig/setEnableProxy；`ShellZKSASLUtil.removeSasl(shellConnect.getId())`；`ShellConnectUtil.testConnect(...)` |
  | `void update()` | 修改保存连接 | `getHost()`；校验代理；名称空则以 host 命名；将表单值写回 `shellConnect`；`connectStore.replace(shellConnect)` 成功后 `ShellZKSASLUtil.removeSasl(id)`、`ShellEventUtil.connectUpdated`、`MessageBox.okToast`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件监听 | `hostIp.addTextChangeListener` 拆分 ip/port；`enableProxy`/`proxyAuthType`/`saslAuth` 联动启停对应面板，同新增类 |
  | `void onWindowShown(WindowEvent event)` | 窗口显示回填 | `getProp("shellConnect")`；回填 name/hostIp/remark/osType/hostPort/readonly/超时；`enableProxy` 与 proxyConfig 回填（密码认证时 `proxyAuthType.select(1)`）；`jumpTableView.setItem(...)`；saslAuth 与 saslConfig 回填 |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.connectUpdateTitle()` |
  | `void addHost()` | 添加主机跳板 | `ShellViewFactory.addHost(this.shellConnect)`；取 prop "jumpConfig" 后 addItem/updateOrder |
  | `void addJump()` | 添加跳板 | `ShellViewFactory.addJump()`；addItem/updateOrder |
  | `void updateJump()` | 编辑跳板 | `ShellViewFactory.updateJump(config)`；refresh/updateOrder |
  | `void deleteJump()` | 删除跳板 | `MessageBox.confirm`；`jumpTableView.removeSelectedItem()`、`jumpConfigStore.delete(config)`、`updateOrder()` |
  | `void moveJumpUp()` | 上移跳板 | `TableViewUtil.moveUp`；refresh/updateOrder |
  | `void moveJumpDown()` | 下移跳板 | `TableViewUtil.moveDown`；refresh/updateOrder |

- 调用链：`update → ShellConnectStore.replace → ShellZKSASLUtil.removeSasl → ShellEventUtil.connectUpdated`
- 调用链：`testConnect → ShellZKSASLUtil.removeSasl → ShellConnectUtil.testConnect`
- 调用链：`deleteJump → ShellJumpConfigStore.delete → ShellJumpTableView.updateOrder`

## ShellAddWebdavConnectController

- 职责：Webdav 连接新增对话框控制器，采集用户名/密码/地址/字符集/代理等参数，测试并保存新连接（无跳板机面板）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab组件 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | host | ClearableTextField | 连接地址 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | group | ShellGroup | 分组 |
  | connectStore | ShellConnectStore | ssh连接储存对象（`final`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 校验并返回连接地址 | `tabPane.select(0)`；`host.validate()` 失败返回 null；返回 `host.getTextTrim()` |
  | `ShellProxyConfig getProxyConfig()` | 构建代理配置 | `new ShellProxyConfig()`；setHost/setPort/setUser/setPassword/setAuthType/setProtocol |
  | `void testConnect()` | 测试连接 | `getHost()`；`new ShellConnect()` setType(`ShellPrototype.WEBDAV`)/setHost/setConnectTimeOut/setUser/setPassword/setProxyConfig/setEnableProxy；`ShellConnectUtil.testConnect(stage, shellConnect, timeout*1000)` |
  | `void add()` | 新增保存连接 | `getHost()`；`userName.validate()`；校验代理；名称空则以 host 命名；`new ShellConnect()` 填充 name/osType/remark/charset/host/connectTimeOut/user/password/proxy/type/groupId；`connectStore.replace(...)` 成功后 `ShellEventUtil.connectAdded`、`MessageBox.okToast`、`setProp("connect")`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件监听 | `enableProxy.selectedChanged` 启停 proxyTab 与 proxyAuthInfoBox；`proxyAuthType.selectedIndexChanged` 联动密码认证时启用 |
  | `void onWindowShown(WindowEvent event)` | 窗口显示初始化 | `getProp("group")`；`osType.selectType(ShellPrototype.WEBDAV)`；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.connectAddTitle()` |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
- 调用链：`testConnect → ShellConnectUtil.testConnect`

## ShellUpdateWebdavConnectController

- 职责：Webdav 连接修改对话框控制器，回填已有连接并更新保存。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | tabPane | FXTabPane | tab组件 |
  | shellConnect | ShellConnect | 待修改的连接信息 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | host | ClearableTextField | 连接地址 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | connectStore | ShellConnectStore | ssh连接储存对象（`final`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 校验并返回连接地址 | `tabPane.select(0)`；`host.validate()` 失败返回 null；返回 `host.getTextTrim()` |
  | `ShellProxyConfig getProxyConfig()` | 复用/构建代理配置 | 取 `shellConnect.getProxyConfig()`，为空则 `new ShellProxyConfig()` 并 `setIid(id)`；再填充字段 |
  | `void testConnect()` | 测试连接 | `getHost()`；`new ShellConnect()` setType(`ShellPrototype.WEBDAV`)/setHost/setConnectTimeOut/`setId(shellConnect.getId())`/setUser/setPassword/setProxyConfig/setEnableProxy；`ShellConnectUtil.testConnect(...)` |
  | `void update()` | 修改保存连接 | `getHost()`；`userName.validate()`；校验代理；名称空则以 host 命名；表单值写回 `shellConnect`（含 charset/user/password/proxy）；`connectStore.replace(...)` 成功后 `ShellEventUtil.connectUpdated`、`MessageBox.okToast`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件监听 | `enableProxy.selectedChanged` 与 `proxyAuthType.selectedIndexChanged` 联动代理面板/认证信息组件 |
  | `void onWindowShown(WindowEvent event)` | 窗口显示回填 | `getProp("shellConnect")`；回填 name/host/remark/osType/charset/connectTimeOut/user/password；`enableProxy` 与 proxyConfig 回填（密码认证 `proxyAuthType.select(1)`） |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.connectUpdateTitle()` |

- 调用链：`update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
- 调用链：`testConnect → ShellConnectUtil.testConnect`

## ShellAddS3ConnectController

- 职责：S3 连接新增对话框控制器，采集类型/区域/AK/SK/地址/字符集/代理等参数，按 s3Type 预置默认区域与地址，测试并保存新连接。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | type | ShellS3TypeCombobox | 类型 |
  | tabPane | FXTabPane | tab组件 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | host | ClearableTextField | 连接地址 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | region | ShellS3RegionTextField | 区域 |
  | group | ShellGroup | 分组 |
  | connectStore | ShellConnectStore | ssh连接储存对象（`final`） |

  （源码中 `appId` / `appId` 相关 `@FXML` 字段整体被注释，属未启用代码，不纳入。）

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 校验并返回连接地址 | `tabPane.select(0)`；`host.validate()` 失败返回 null；返回 `host.getTextTrim()` |
  | `ShellProxyConfig getProxyConfig()` | 构建代理配置 | `new ShellProxyConfig()`；setHost/setPort/setUser/setPassword/setAuthType/setProtocol |
  | `void testConnect()` | 测试连接 | `getHost()`；`new ShellConnect()` setType("s3")/setHost/setConnectTimeOut/setUser/setPassword/setProxyConfig/setEnableProxy/`setS3Type(type.getType())`/`setRegion(region.getText())`；`ShellConnectUtil.testConnect(...)` |
  | `void add()` | 新增保存连接 | `getHost()`；`userName.validate()`；校验代理；名称空则以 host 命名；`new ShellConnect()` 填充 name/osType/remark/charset/host/connectTimeOut/user/password/proxy/`setS3Type`/`setRegion`/type/groupId；`connectStore.replace(...)` 成功后 `ShellEventUtil.connectAdded`、`MessageBox.okToast`、`setProp("connect")`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件监听 | `host.addTextChangeListener` 处理区域：含 ":" 时 `ShellS3Util.parseRegion(t1)` 回填 region；`enableProxy`/`proxyAuthType` 联动面板；`type.selectedItemChanged` 调 `initS3Type` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示初始化 | `getProp("group")`、`getProp("s3Type")` 并 `initS3Type(s3Type)`；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `void initS3Type(String s3Type)` | 按 s3 类型预置默认值 | s3/S3→type "S3"、osType `ShellPrototype.S3`；Minio→type "Minio"、osType "Minio"、`region.select(Region.US_EAST_1)`；Cos→type "Tencent"、预置腾讯 cos 地址与区域；Obs→type "Huawei"、预置华为 obs 地址与区域；Oss→type "Alibaba"、预置阿里 oss 地址与区域 |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.connectAddTitle()` |
  | `void destroy()` | 销毁时释放资源 | `region.destroy()`、`super.destroy()` |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
- 调用链：`host 文本变化 → ShellS3Util.parseRegion → ShellS3RegionTextField.setText`

## ShellUpdateS3ConnectController

- 职责：S3 连接修改对话框控制器，回填已有 S3 连接（含类型/区域）并更新保存。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | type | ShellS3TypeCombobox | 类型 |
  | tabPane | FXTabPane | tab组件 |
  | shellConnect | ShellConnect | 待修改的连接信息 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | host | ClearableTextField | 连接地址 |
  | charset | CharsetComboBox | 字符集 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | enableProxy | FXToggleSwitch | 开启代理 |
  | proxyTab | FXTab | 代理面板 |
  | proxyHost | ClearableTextField | 代理地址 |
  | proxyPort | NumberTextField | 代理端口 |
  | proxyAuthInfoBox | FXHBox | 代理信息组件 |
  | proxyUser | ClearableTextField | 代理用户 |
  | proxyPassword | PasswordTextField | 代理密码 |
  | proxyProtocol | ShellProxyProtocolComboBox | 代理协议 |
  | proxyAuthType | ShellProxyAuthTypeComboBox | 代理认证方式 |
  | region | ShellS3RegionTextField | 区域 |
  | connectStore | ShellConnectStore | ssh连接储存对象（`final`） |

  （源码中 `appId` 相关 `@FXML` 字段整体被注释，属未启用代码，不纳入。）

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 校验并返回连接地址 | `tabPane.select(0)`；`host.validate()` 失败返回 null；返回 `host.getTextTrim()` |
  | `ShellProxyConfig getProxyConfig()` | 复用/构建代理配置 | 取 `shellConnect.getProxyConfig()`，为空则 `new ShellProxyConfig()` 并 `setIid(id)`；再填充字段 |
  | `void testConnect()` | 测试连接 | `getHost()`；`new ShellConnect()` setType("s3")/setHost/setConnectTimeOut/`setId(shellConnect.getId())`/setUser/setPassword/setProxyConfig/setEnableProxy/`setS3Type`/`setRegion`；`ShellConnectUtil.testConnect(...)` |
  | `void update()` | 修改保存连接 | `getHost()`；`userName.validate()`；校验代理；名称空则以 host 命名；表单值写回 `shellConnect`（含 charset/user/password/proxy/`setS3Type`/`setRegion`）；`connectStore.replace(...)` 成功后 `ShellEventUtil.connectUpdated`、`MessageBox.okToast`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件监听 | `host.addTextChangeListener` 含 ":" 时 `ShellS3Util.parseRegion` 回填 region；`enableProxy`/`proxyAuthType` 联动面板 |
  | `void onWindowShown(WindowEvent event)` | 窗口显示回填 | `getProp("shellConnect")`；回填 name/host/remark/osType/charset/`region.select(getRegion())`/connectTimeOut/user/password/`type.selectType(getS3Type())`；`enableProxy` 与 proxyConfig 回填（密码认证 `proxyAuthType.select(1)`） |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.connectUpdateTitle()` |
  | `void destroy()` | 销毁时释放资源 | `region.destroy()`、`super.destroy()` |

- 调用链：`update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
- 调用链：`testConnect → ShellConnectUtil.testConnect`

## ShellAddMoshConnectController

- 职责：Mosh 连接新增对话框控制器，采集主机端口/终端参数/认证方式（密码、SSH密钥、SSH代理、证书）/环境变量/Mosh 密钥，测试并保存新连接。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | certificate | ChooseFileTextField | 证书 |
  | certificatePwd | PasswordTextField | 证书密码 |
  | sshAgent | ReadOnlyTextField | ssh代理 |
  | key | ShellKeyComboBox | 密钥 |
  | tabPane | FXTabPane | tab组件 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | termType | ShellTermTypeComboBox | 终端类型 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt修饰 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | env | FXTextArea | 环境 |
  | authMethod | ShellSSHAuthTypeComboBox | 认证方式 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | group | ShellGroup | 分组 |
  | moshKey | ClearableTextField | mosh密钥 |
  | connectStore | ShellConnectStore | ssh连接储存对象（`final`） |

  （源码中 `enableBackground`/`backgroundTab`/`backgroundImage` 相关 `@FXML` 字段整体被注释，属未启用代码，不纳入。）

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 组合 host:port 并校验 | `hostIp.getTextTrim()`、`tabPane.select(0)`；`hostPort.validate()`/`hostIp.validate()` 失败返回 null；返回 `hostIp + ":" + hostPort.getValue()` |
  | `void testConnect()` | 测试连接 | `getHost()`；`new ShellConnect()` setHost/setConnectTimeOut/setType(`ShellPrototype.MOSH`)/`setKeyId(key.getKeyId())`/setUser/setPassword/setAuthMethod/setCertificate/setCertificatePwd；`ShellConnectUtil.testConnect(...)` |
  | `void add()` | 新增保存连接 | `getHost()`；用户名非空时按 `authMethod` 校验密码/证书/密钥（`ValidatorUtil.validFail`）；名称空则以 host 命名；`new ShellConnect()` 填充 name/osType/remark/charset/host/termType/backspaceType/altSendsEscape/connectTimeOut/environment/`setMoshKey`/keyId/user/password/certificate/certificatePwd/authMethod/type/groupId；`connectStore.replace(...)` 成功后 `ShellEventUtil.connectAdded`、`MessageBox.okToast`、`setProp("connect")`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件监听 | `hostIp.addTextChangeListener` 遇 ":" 拆分 ip/port；`authMethod.selectedIndexChanged` 依据 `isPasswordAuth`/`isCertificateAuth`/`isSSHAgentAuth` 用 `NodeGroupUtil.display/disappear` 切换 password/sshKey/sshAgent/certificate 节点 |
  | `void onWindowShown(WindowEvent event)` | 窗口显示初始化 | `getProp("group")`；`osType.selectType(ShellPrototype.MOSH)`；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `void onStageInitialize(StageAdapter stage)` | 阶段初始化时填充 sshAgent | Windows 取 `PageantConnector.DESCRIPTOR.getIdentityAgent()`，否则取 `UnixDomainSocketConnector.DESCRIPTOR.getIdentityAgent()` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.connectAddTitle()` |

- 调用链：`add → ShellConnectStore.replace → ShellEventUtil.connectAdded`
- 调用链：`add → ValidatorUtil.validFail → Keyboard 校验失败返回`
- 调用链：`onStageInitialize → OSUtil.isWindows → PageantConnector/UnixDomainSocketConnector.getIdentityAgent`

## ShellUpdateMoshConnectController

- 职责：Mosh 连接修改对话框控制器，回填已有连接（认证方式、终端、Mosh 密钥等）并更新保存。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | userName | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | certificate | ChooseFileTextField | 证书 |
  | certificatePwd | PasswordTextField | 证书密码 |
  | sshAgent | ReadOnlyTextField | ssh代理 |
  | key | ShellKeyComboBox | 密钥 |
  | tabPane | FXTabPane | tab组件 |
  | shellConnect | ShellConnect | 待修改的连接信息 |
  | name | ClearableTextField | 名称 |
  | remark | FXTextArea | 备注 |
  | hostIp | ClearableTextField | 连接ip |
  | hostPort | PortTextField | 连接端口 |
  | charset | CharsetComboBox | 字符集 |
  | termType | ShellTermTypeComboBox | 终端类型 |
  | backspaceType | ShellTermBackspaceTypeCombobox | 终端退格类型 |
  | altSendsEscape | FXCheckBox | alt修饰 |
  | connectTimeOut | NumberTextField | 连接超时时间 |
  | env | FXTextArea | 环境 |
  | authMethod | ShellSSHAuthTypeComboBox | 认证方式 |
  | osType | ShellOsTypeComboBox | 系统类型 |
  | moshKey | ClearableTextField | mosh密钥 |
  | connectStore | ShellConnectStore | ssh连接储存对象（`final`） |

  （源码中 `enableBackground`/`backgroundTab`/`backgroundImage` 相关 `@FXML` 字段整体被注释，属未启用代码，不纳入。）

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getHost()` | 组合 host:port 并校验 | `hostIp.getTextTrim()`、`tabPane.select(0)`；`hostPort.validate()`/`hostIp.validate()` 失败返回 null；返回 `hostIp + ":" + hostPort.getValue()` |
  | `void testConnect()` | 测试连接 | `getHost()`；`new ShellConnect()` setHost/setConnectTimeOut/setType(`ShellPrototype.MOSH`)/`setId(shellConnect.getId())`/setKeyId/setUser/setPassword/setAuthMethod/setCertificate/setCertificatePwd；`ShellConnectUtil.testConnect(...)` |
  | `void update()` | 修改保存连接 | `getHost()`；用户名非空时按 `authMethod` 校验密码/证书/密钥；名称空则以 host 命名；表单值写回 `shellConnect`（含 termType/backspaceType/altSendsEscape/environment/`setMoshKey`/认证字段）；`connectStore.replace(...)` 成功后 `ShellEventUtil.connectUpdated`、`MessageBox.okToast`、`closeWindow()` |
  | `void bindListeners()` | 绑定控件监听 | `hostIp.addTextChangeListener` 拆分 ip/port；`authMethod.selectedIndexChanged` 用 `NodeGroupUtil.display/disappear` 切换各认证节点 |
  | `void onWindowShown(WindowEvent event)` | 窗口显示回填 | `getProp("shellConnect")`；回填 name/hostIp/remark/osType/env/hostPort/charset/termType/connectTimeOut/user/password；按 `isPasswordAuth`/`isCertificateAuth`/`isSSHAgentAuth`/`isManagerAuth` 选择 authMethod 并回填证书/证书密码/`key.selectById`；`backspaceType.selectType`、altSendsEscape、`moshKey.setText` |
  | `void onStageInitialize(StageAdapter stage)` | 阶段初始化时填充 sshAgent | Windows 取 `PageantConnector.DESCRIPTOR.getIdentityAgent()`，否则取 `UnixDomainSocketConnector.DESCRIPTOR.getIdentityAgent()` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.connectUpdateTitle()` |

- 调用链：`update → ShellConnectStore.replace → ShellEventUtil.connectUpdated`
- 调用链：`onWindowShown → ShellConnect.isManagerAuth → ShellKeyComboBox.selectById`
- 调用链：`testConnect → ShellConnectUtil.testConnect`
