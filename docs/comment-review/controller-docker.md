# controller/docker 控制器代码审查

> 范围：`controller/docker/**` 递归全部类，共 11 个。

---

## ShellDockerCommitController
- 职责：将 Docker 容器提交为镜像的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | containerName | ReadOnlyTextField | 容器名称（只读） |
  | repository | ClearableTextField | 仓库名 |
  | tag | ClearableTextField | 标签 |
  | comment | FXTextArea | 提交注释 |
  | preview | ReadOnlyTextArea | 命令预览 |
  | exec | ShellDockerExec | Docker 执行对象 |
  | container | ShellDockerContainer | 目标容器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 初始化 | 取 prop `exec`/`container`，`repository.setText(container.getImage())`、`containerName.setText(container.getNames())`、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `void bindListeners()` | 绑定即时预览 | tag/comment/repository 文本变化均调 `uopdatePreview()` |
  | `ShellDockerCommit initParam()` | 组装提交参数 | new `ShellDockerCommit` 并 set 仓库/标签/注释/`containerId` |
  | `void uopdatePreview()` | 刷新命令预览 | `exec.docker_commit_cmd(commit)` 后写入 `preview` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.saveContainer()` |
  | `void run()` | 提交镜像 | `StageManager.showMask` 内 `exec.docker_commit(commit)`；空输出 `MessageBox.warn`，否则 `MessageBox.info(imageId)` + `ShellEventUtil.containerCommit(exec)` + `closeWindow()` |

- 调用链：`bindListeners → uopdatePreview → ShellDockerExec.docker_commit_cmd`
- 调用链：`run → ShellDockerExec.docker_commit → ShellEventUtil.containerCommit`

## ShellDockerImageHistoryController
- 职责：查看 Docker 镜像构建历史的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | historyTable | ShellDockerImageHistoryTableView | 镜像历史表 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 填充历史表 | 取 prop `histories` 列表，`historyTable.setItem(histories)`、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.imageHistory()` |

- 调用链：`onWindowShown → ShellDockerImageHistoryTableView.setItem`

## ShellDockerInfoController
- 职责：展示 Docker 系统信息的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | data | ShellDataEditor | 信息编辑器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void copyInfo()` | 复制信息 | `ClipboardUtil.copy(data.getText())` + `MessageBox.okToast` |
  | `void onWindowShown(WindowEvent event)` | 填充信息 | 取 prop `info`，`data.setText(inspect)`、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | 返回字面量 `"Docker Info"` |

- 调用链：`copyInfo → ClipboardUtil.copy → MessageBox.okToast`

## ShellDockerInspectController
- 职责：展示 Docker 容器/镜像 inspect 结果的对话框业务，支持高亮搜索。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | data | ShellDataEditor | inspect 内容编辑器 |
  | filter | HighlightTextField | 高亮过滤关键字 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void copyInspect()` | 复制内容 | `ClipboardUtil.copy(data.getText())` + `MessageBox.okToast` |
  | `void bindListeners()` | 绑定高亮 | `EditorUtil.bindHighlight(data, filter)`（原手动过滤监听被注释，为死代码） |
  | `void onWindowShown(WindowEvent event)` | 填充内容与标题 | 取 prop `inspect`/`image`，`data.setText(inspect)`、`stage.switchOnTab()`、`stage.hideOnEscape()`；依 `BooleanUtil.isTrue(image)` 设标题为 `I18nHelper.imageInspect()` 或 `containerInspect()` |
  | `void searchNext()` | 搜索下一个 | `EditorUtil.searchNextHighlight(data, filter)` |

- 调用链：`bindListeners → EditorUtil.bindHighlight`
- 调用链：`searchNext → EditorUtil.searchNextHighlight`

## ShellDockerLogsController
- 职责：展示 Docker 容器日志的对话框业务，支持高亮搜索。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | data | ShellDataEditor | 日志编辑器 |
  | filter | HighlightTextField | 高亮过滤关键字 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void copyLogs()` | 复制日志 | `ClipboardUtil.copy(data.getText())` + `MessageBox.okToast` |
  | `void bindListeners()` | 绑定高亮 | `EditorUtil.bindHighlight(data, filter)`（原手动监听被注释，死代码） |
  | `void onWindowShown(WindowEvent event)` | 填充日志 | 取 prop `logs`，`data.setText(logs)`、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.containerLogs()` |
  | `void searchNext()` | 搜索下一个 | `EditorUtil.searchNextHighlight(data, filter)` |

- 调用链：`onWindowShown → ShellDataEditor.setText`
- 调用链：`searchNext → EditorUtil.searchNextHighlight`

## ShellDockerPortController
- 职责：查看 Docker 容器端口的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | portTable | ShellDockerPortTableView | 端口表 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 填充端口表 | 取 prop `ports` 列表，`portTable.setItem(ports)`、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.dockerPorts()` |

- 调用链：`onWindowShown → ShellDockerPortTableView.setItem`

## ShellDockerResourceController
- 职责：查看并修改 Docker 容器资源限制（内存/CPU）的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | memory | NumberTextField | 内存限制 |
  | memorySwap | NumberTextField | 内存交换区 |
  | cpuShares | NumberTextField | CPU shares |
  | nanoCpus | DecimalTextField | NanoCpus |
  | cpuPeriod | DecimalTextField | CPU period |
  | cpuQuota | NumberTextField | CPU quota |
  | exec | ShellDockerExec | Docker 执行对象 |
  | containerId | String | 容器 id |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 回填当前资源 | 取 prop `exec`/`id`/`resource`；将 memory/memorySwap 按 `/1024/1024`、nanoCpus `/1000000000`、cpuPeriod `/1000` 换算后 setValue |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.containerResource()` |
  | `void save()` | 保存资源限制 | 收集各字段构造 `ShellDockerResource`（nanoCpus 转 long、cpuPeriod ×1000），`exec.docker_update(resource, containerId)`；按输出分支：空→warn；不含容器 id→warn(output)；输出含多余行→`MessageBox.info(msg)`；否则 `closeWindow()` |

- 调用链：`save → ShellDockerExec.docker_update → (MessageBox.warn/info | closeWindow)`

## ShellDockerRunController
- 职责：以给定镜像运行 Docker 容器的对话框业务，含端口/卷/环境/标签配置与命令预览。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ClearableTextField | 容器名称 |
  | imageName | ReadOnlyTextField | 镜像名称（只读） |
  | restart | FXToggleGroup | 重启策略 |
  | i / t / d | CheckBox | -i / -t / -d 参数 |
  | rm | FXCheckBox | --rm 参数 |
  | privileged | FXCheckBox | --privileged 参数 |
  | params | ClearableTextField | 附加参数 |
  | portTable | ShellDockerRunPortTableView | 端口表 |
  | volumeTable | ShellDockerRunVolumeTableView | 卷表 |
  | envTable | ShellDockerRunEnvTableView | 环境变量表 |
  | labelTable | ShellDockerRunLabelTableView | 标签表 |
  | preview | ReadOnlyTextArea | 命令预览 |
  | baseTab | FXTab | 基础 tab |
  | exec | ShellDockerExec | Docker 执行对象 |
  | image | ShellDockerImage | 源镜像 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void bindListeners()` | 绑定即时预览 | name/params 文本、i/t/d/rm/privileged 选中、restart 选中、四个表 items 变化、baseTab 选中变化均调 `updatePreview()` |
  | `ShellDockerRun initParam()` | 组装运行参数 | new `ShellDockerRun` 并 set 各开关、envs/ports/labels/volumes、params、imageName、容器名、restart（`selectedUserData()`）、privileged |
  | `void updatePreview()` | 刷新命令预览 | 空守卫后 `exec.docker_run_cmd(run)` 写入 `preview` |
  | `void onWindowShown(WindowEvent event)` | 初始化 | 取 prop `exec`/`image`，设置镜像名与默认容器名（镜像名 `:` `/` 替换为 `_`）、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.runContainer()` |
  | `void run()` | 运行容器 | `StageManager.showMask` 内 `exec.docker_run(run)`；空→warn；否则 `MessageBox.info(containerId)` + `ShellEventUtil.containerRun(exec)` + `closeWindow()` |
  | `void addPort()/deletePort()` | 增删端口 | `portTable.addItem/selectLast` 或确认后 `removeItem(selectedItems)` |
  | `void addVolume()/deleteVolume()` | 增删卷 | 同上，操作 `volumeTable` |
  | `void addEnv()/deleteEnv()` | 增删环境变量 | 同上，操作 `envTable` |
  | `void addLabel()/deleteLabel()` | 增删标签 | 同上，操作 `labelTable` |

- 调用链：`bindListeners → updatePreview → ShellDockerExec.docker_run_cmd`
- 调用链：`run → ShellDockerExec.docker_run → ShellEventUtil.containerRun`

## ShellDockerSaveController
- 职责：将 Docker 镜像保存为 tar 文件的对话框业务，带进度显示。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ClearableTextField | 保存文件路径 |
  | imageName | ReadOnlyTextField | 镜像名称（只读） |
  | process | FXProgressBar | 进度条 |
  | preview | ReadOnlyTextArea | 命令预览 |
  | exec | ShellDockerExec | Docker 执行对象 |
  | image | ShellDockerImage | 源镜像 |
  | execThread | Thread | 导出执行线程 |
  | processThread | Thread | 进度刷新线程 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void bindListeners()` | 绑定即时预览 | name 文本变化调 `uopdatePreview()` |
  | `ShellDockerSave initParam()` | 组装保存参数 | new `ShellDockerSave` 并 set 文件路径与镜像名 |
  | `void uopdatePreview()` | 刷新命令预览 | `exec.docker_save_cmd(save)` 写入 `preview` |
  | `void onWindowShown(WindowEvent event)` | 初始化默认路径 | 取 prop `exec`/`image`；默认路径 = `exec.getClient().getUserHome()` + 镜像名（`:` `/`→`_`）+ `.tar` |
  | `void onWindowHidden(WindowEvent event)` | 关闭时中断线程 | `ThreadUtil.interrupt(execThread)`、`ThreadUtil.interrupt(processThread)` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.saveImage()` |
  | `void run()` | 保存镜像 | `NodeGroupUtil.disable(stage,"run")`；启动 processThread 轮询 SFTP 文件大小按比例更新 `process`（乘 1.02 修正、上限 1，文件未出现时以 0.005 递增封顶 0.97），结束提示 `fileSaved()` 并 enable；execThread 调 `exec.docker_save(save)` 后置 finished=true |

- 调用链：`run → ThreadUtil.start(execThread: ShellDockerExec.docker_save) / (processThread: ShellSFTPClient.fileInfo → FXProgressBar.setProgress)`

## ShellDockerTagController
- 职责：修改 Docker 镜像标签（tag）的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ClearableTextField | 新镜像名称 |
  | imageName | ReadOnlyTextField | 原镜像名称（只读） |
  | exec | ShellDockerExec | Docker 执行对象 |
  | image | ShellDockerImage | 源镜像 |
  | preview | ReadOnlyTextArea | 命令预览 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void bindListeners()` | 绑定即时预览 | name 文本变化调 `uopdatePreview()` |
  | `ShellDockerTag initParam()` | 组装打标签参数 | new `ShellDockerTag` 并 set 原镜像名与新镜像名 |
  | `void uopdatePreview()` | 刷新命令预览 | `exec.docker_tag_cmd(tag)` 写入 `preview` |
  | `void onWindowShown(WindowEvent event)` | 初始化 | 取 prop `exec`/`image`，name/imageName 均预填镜像名，`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | `I18nHelper.updateTag()` |
  | `void run()` | 执行打标签 | `exec.docker_tag(tag)` 后 `ShellEventUtil.imageTag(exec)`、`closeWindow()` |

- 调用链：`run → ShellDockerExec.docker_tag → ShellEventUtil.imageTag`

## ShellDockerVersionController
- 职责：展示 Docker 版本信息的对话框业务。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | version | ShellDataEditor | 版本信息编辑器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void copyVersion()` | 复制版本信息 | `ClipboardUtil.copy(version.getText())` + `MessageBox.okToast` |
  | `void onWindowShown(WindowEvent event)` | 填充版本信息 | 取 prop `version`，`version.setText(inspect)`、`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 窗口标题 | 返回字面量 `"Docker Version"` |
  | `void destroy()` | 销毁 | `version.destroy()` 后 `super.destroy()` |

- 调用链：`copyVersion → ClipboardUtil.copy → MessageBox.okToast`
