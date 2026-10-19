# controller/mongo 与 controller/redis 控制器代码审查

> 范围：`controller/mongo/**`（11 类，其中 2 个文件为整文件死代码）+ `controller/redis/**`（15 类）。

---

## ShellMongoDataDumpController

- 职责：MongoDB 数据转储对话框业务，按库/表将数据转储为文件，并实时反馈进度与消息。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbInfo | ShellConnect | 连接信息 |
  | dbClient | ShellMongoClient | mongodb 客户端 |
  | dumpType | int | 转储类型，1 库 / 2 表 |
  | stopDumpBtn | FXButton | 结束转储按钮 |
  | dumpStatus | FXLabel | 转储状态 |
  | dumpMsg | MsgTextArea | 转储消息 |
  | connect | ReadOnlyTextField | 连接 |
  | database | ReadOnlyTextField | 数据库 |
  | tableBox | FXVBox | 表组件 |
  | table | ReadOnlyTextField | 表 |
  | dataType | DBDataDumpTypeComboBox | 数据类型 |
  | execTask | Thread | 转储操作任务 |
  | counter | Counter | 计数器（`final`） |
  | dumpFile | File | 转储文件 |
  | dumpHandler | DBDataDumpHandler | 转储处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean checkDumpFile()` | 检查转储文件，缺失时弹出保存对话框 | `dumpFile` 为空或不存在时按 `dumpType` 取 `database/table` 文本，`dataType.isFull()` 决定 `_full`/`_structure` 后缀；`FXChooser.jsExtensionFilter()`、`FileChooserHelper.save(...)`、`FileUtil.touch(...)` |
  | `void doDump()` | 执行转储 | `checkDumpFile()`；`counter.reset()`、`dumpMsg.clear()`；新建 `ShellMongoDataDumpHandler(dbClient, database)` 并 `setQueryLimit(10000)/setMessageHandler/setProcessedHandler`；或 `interrupt(false)` 复用；设置 `dumpFile/tableName/dumpType/dataType`；`NodeGroupUtil.disable(stage,"exec")`、`stage.appendTitle(...)`；`ThreadUtil.start(...)` 内 `dumpHandler.doDump()`，异常分支按 `InterruptedException` 区分取消/失败；`finally` 恢复 `NodeGroupUtil.enable`、`stopDumpBtn.disable()`、`stage.restoreTitle()`、`SystemUtil.gcLater()` |
  | `void stopDump()` | 结束转储 | `ThreadUtil.interrupt(execTask)`、`execTask=null`；非空则 `dumpHandler.interrupt()` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时回填转储参数 | `super.onWindowShown(event)`；`getProp("dbClient"/"dumpType"/"dbName"/"tableName")`；`dbClient.getShellConnect()`；`database.setText/connect.setText`；`dumpType==2` 时 `table.setText`、`tableBox.display()`；`stage.hideOnEscape()` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时停止转储 | `super.onWindowHidden(event)`、`stopDump()` |
  | `void updateStatus(String extraMsg)` | 更新状态文本 | 非空时 `counter.setExtraMsg(extraMsg)`；`dumpStatus.text(counter.unknownFormat())` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nResourceBundle.i18nString("base.title.dump")` |
  | `void onStageInitialize(StageAdapter stage)` | 初始化表组件可见性绑定 | `super.onStageInitialize(stage)`、`tableBox.managedBindVisible()` |
  | `void bindListeners()` | 绑定数据类型变化 | `super.bindListeners()`；`dataType.selectedItemChanged(...)` 时 `dumpFile=null` |

- 调用链：`doDump → checkDumpFile → FileChooserHelper.save → FileUtil.touch`
- 调用链：`doDump → ThreadUtil.start → ShellMongoDataDumpHandler.doDump`
- 调用链：`onWindowHidden → stopDump → DBDataDumpHandler.interrupt`

## ShellMongoDataExportController

- 职责：MongoDB 数据导出对话框业务，分五步完成库/表/字段选择、导出格式与选项配置并执行导出。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步 |
  | step2 | FXVBox | 第二步 |
  | step3 | FXVBox | 第三步 |
  | step4 | FXVBox | 第四步 |
  | step5 | FXVBox | 第五步 |
  | database | ShellMongoDatabaseComboBox | 数据库 |
  | tableCombobox | DBNameComboBox | 导出表下拉框 |
  | tableColumns | ShellMongoDataExportColumnListView | 导出表字段列表 |
  | exportTableView | ShellMongoDataExportCollectionTableView | 导出表组件 |
  | fileType | FXToggleGroup | 文件类型 |
  | dbClient | ShellMongoClient | mongodb 客户端 |
  | datePreview | FXLabel | 日期预览 |
  | dateFormat | DBDataDateTextFiled | 日期格式 |
  | recordSeparator | DBDataRecordSeparatorComboBox | 记录分隔符 |
  | fieldSeparator | DBDataFieldSeparatorComboBox | 字段分割符 |
  | txtIdentifier | DBDataTxtIdentifierComboBox | 文本识别符 |
  | includeFields | FXCheckBox | 包含列标题 |
  | fieldToAttr | FXCheckBox | 字段作为属性 |
  | earlyVersion | FXCheckBox | 早期版本 |
  | continueWithError | FXCheckBox | 遇到错误时继续 |
  | stopExportBtn | FXButton | 结束导出按钮 |
  | exportStatus | FXLabel | 导出状态 |
  | exportMsg | MsgTextArea | 导出消息 |
  | execTask | Thread | 导出操作任务 |
  | counter | Counter | 计数器（`final`） |
  | exportHandler | ShellMongoDataExportHandler | 导出处理器 |
  | dbName | String | 数据库 |
  | tableName | String | 表 |
  | exportMode | int | 0 正常导出 / 1 查询导出 |
  | exportTable | ShellMongoDataExportCollection | 导出表 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doExport()` | 执行导出 | `counter.reset()`、`exportMsg.clear()`；新建 `ShellMongoDataExportHandler(dbClient, dbName)` 并设置消息/进度处理器，或 `interrupt(false)`；依次设置文件类型/表 `exportTableView.getSelectedTables()`/查询上限（非 Excel 时 10000）/日期格式/字段作为属性/早期版本/分隔符/包含列标题/文本识别符/错误继续；`NodeGroupUtil.disable(stage,"exec")`、`stage.appendTitle(...)`；`ThreadUtil.start(...)` 内 `exportHandler.doExport()`，异常区分取消/失败；`finally` 恢复界面与标题 |
  | `void stopExport()` | 结束导出 | `ThreadUtil.interrupt(execTask)`、`execTask=null`；非空则 `exportHandler.interrupt()` |
  | `void bindListeners()` | 绑定表/日期/库变化 | `tableCombobox.selectedItemChanged` 时 `tableColumns.init(collection.getColumns())` 或 `clearItems()`；`dateFormat.textProperty()` 变化 `flushDatePreview()`；`database.selectedItemChanged` 时写 `dbName` 并 `StageManager.showMask(this::initTables)` |
  | `void flushDatePreview()` | 刷新日期预览 | `dateFormat.getTextTrim()` 与 `DateUtil.format(new Date(), format)` 拼接显示；异常显示 `invalidFormat()` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化库/导出模式 | `getProp("dbClient"/"dbName"/"collectionName")`；可选 `exportMode`、`exportTable`；`dbName` 非空时 `database.setItem/selectFirst/disable`，否则 `database.init(dbClient)/enable`；`stage.hideOnEscape()` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时停止导出 | `stopExport()` |
  | `void updateStatus(String extraMsg)` | 更新状态文本 | 非空时 `counter.setExtraMsg(extraMsg)`；`exportStatus.text(counter.unknownFormat())` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.exportTitle()` |
  | `void showStep1()` | 显示第一步 | `step1.display()`、`step2.disappear()` |
  | `void initTables()` | 初始化表列表 | `exportTableView.clearItems()`；`exportMode==0` 时 `dbClient.listCollections(dbName)` 逐个构造 `ShellMongoDataExportCollection` 并按 `tableName` 设选中，否则加入 `exportTable`；按 `fileType.selectedToggle().getUserData()` 设置扩展名 `FXChooser.extensionFilter(...)` |
  | `void showStep2()` | 显示第二步 | `fileType.selectedToggle()` 为空时 `MessageBox.warn(pleaseSelectType())` 返回；`StageManager.showMask(this::initTables)`；切换 step 显示 |
  | `void showStep3()` | 显示第三步 | `exportTableView.hasSelectedTable()` 校验；遍历选中表，缺列时 `dbClient.selectColumns(new MongoSelectRecordParam(dbName, name))`；加入 `tableCombobox` 并 `selectFirst()`；切换 step |
  | `void showStep4()` | 显示第四步 | 根据文件类型（sql/txt/json/xls/xlsx/csv/html/xml/js）用 `NodeGroupUtil.display/disappear` 切换各选项组件可见性；`flushDatePreview()`；切换 step |
  | `void showStep5()` | 显示第五步 | `step4.disappear()`、`step5.display()` |
  | `void selectAllTable()` | 全选表 | 遍历 `exportTableView.getItems()` 置 `setSelected(true)` |
  | `void unselectAllTable()` | 取消全选表 | 遍历 `exportTableView.getItems()` 置 `setSelected(false)` |
  | `void selectAllFiled()` | 全选字段 | 遍历 `tableColumns.getItems()` 置 `setSelected(true)` |
  | `void unselectAllField()` | 取消全选字段 | 遍历 `tableColumns.getItems()` 置 `setSelected(false)` |

- 调用链：`showStep2 → StageManager.showMask → initTables → ShellMongoClient.listCollections`
- 调用链：`showStep3 → ShellMongoClient.selectColumns → tableCombobox.addItem`
- 调用链：`doExport → ThreadUtil.start → ShellMongoDataExportHandler.doExport`

## ShellMongoDataImportController

- 职责：MongoDB 数据导入对话框业务，分五步选择文件类型/文件、配置解析选项并执行导入。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步 |
  | step2 | FXVBox | 第二步 |
  | step3 | FXVBox | 第三步 |
  | step4 | FXVBox | 第四步 |
  | step5 | FXVBox | 第五步 |
  | importFileTableView | ShellMongoDataImportFileTableView | 导入表组件 |
  | fileType | FXToggleGroup | 文件类型 |
  | dbClient | ShellMongoClient | mongodb 客户端 |
  | stopImportBtn | FXButton | 结束导入按钮 |
  | importStatus | FXLabel | 导入状态 |
  | importMsg | MsgTextArea | 导入消息 |
  | recordLabel | DBDataRecordLabelComboBox | 行标签 |
  | attrToColumn | FXCheckBox | 标签属性作为表字段 |
  | columnIndex | NumberTextField | 字段索引 |
  | dataStartIndex | NumberTextField | 数据起始索引 |
  | datePreview | FXLabel | 日期预览 |
  | dateFormat | DBDataDateTextFiled | 日期格式 |
  | recordSeparator | DBDataRecordSeparatorComboBox | 记录分隔符 |
  | fieldSeparator | DBDataFieldSeparatorComboBox | 字段分割符 |
  | txtIdentifier | DBDataTxtIdentifierComboBox | 文本识别符 |
  | importMode | FXToggleGroup | 导入模式 |
  | execTask | Thread | 导入操作任务 |
  | counter | Counter | 计数器（`final`） |
  | importHandler | ShellMongoDataImportHandler | 导入处理器 |
  | database | ShellMongoDatabaseComboBox | 数据库 |
  | dbName | String | 数据库 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doImport()` | 执行导入 | `counter.reset()`、`importMsg.clear()`；新建 `ShellMongoDataImportHandler(dbClient, dbName)` 设置消息/进度处理器，或 `interrupt(false)`；设置文件类型/文件集 `importFileTableView.getItems()`/分隔符/文本识别符/日期格式/标签转字段/导入模式/字段索引-1/数据起始-1/记录标签（`recordLabel.isRoot()` 时置 null）；`NodeGroupUtil.disable(stage,"exec")`、`stage.appendTitle(...)`；`ThreadUtil.start(...)` 内 `importHandler.doImport()`，异常区分取消/`MessageBox.exception`；`finally` 恢复界面 |
  | `void stopImport()` | 结束导入 | `ThreadUtil.interrupt(execTask)`、`execTask=null`；非空则 `importHandler.interrupt()` |
  | `void flushDatePreview()` | 刷新日期预览 | `dateFormat.getTextTrim()` 与 `DateUtil.format(new Date(), format)` 拼接；异常显示 `invalidFormat()` |
  | `void bindListeners()` | 绑定日期/库/文件列表变化 | `dateFormat.textProperty()` 监听 `flushDatePreview()`；`database.selectedItemChanged` 时写 `dbName`、`importFileTableView.clearItems()`、`initFileTable()`；`importFileTableView.itemList()` 增改时 `initFileTable()`；最后 `initFileTable()` |
  | `void initFileTable()` | 初始化文件表格 | 遍历 `importFileTableView.itemList()` 为其 `setDbName(dbName)`、`setDbClient(dbClient)` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化库 | `getProp("dbName"/"dbClient")`；`dbName` 非空时 `database.addItem/selectFirst/disable`，否则 `database.init(dbClient)/enable`；`stage.hideOnEscape()` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时停止导入 | `stopImport()` |
  | `void updateStatus(String extraMsg)` | 更新状态文本 | 非空时 `counter.setExtraMsg(extraMsg)`；`importStatus.text(counter.unknownFormat())` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.importTitle()` |
  | `void showStep1()` | 显示第一步 | `importFileTableView.clearItems()`、切换 step |
  | `void showStep2()` | 显示第二步 | 未选文件类型时 `MessageBox.warn(pleaseSelectType())` 返回；切换 step |
  | `void showStep3()` | 显示第三步 | 无文件时 `MessageBox.warn(pleaseSelectFile())` 返回；按文件类型（json/txt/csv/xml/excel）用 `NodeGroupUtil.display/disappear` 切换解析选项组件；`flushDatePreview()`；切换 step |
  | `void showStep4()` | 显示第四步 | `importMsg.clear()`、切换 step |
  | `void showStep5()` | 显示第五步 | 切换 step |
  | `void addFile()` | 添加文件 | `FXChooser.extensionFilter(fileType.selectedUserData())`、`FileChooserHelper.choose(...)`；选中后构造 `ShellMongoDataImportFile` 并 `importFileTableView.addItem(...)` |
  | `void deleteFile()` | 删除文件 | `importFileTableView.removeSelectedItem()` |

- 调用链：`addFile → FileChooserHelper.choose → importFileTableView.addItem → initFileTable`
- 调用链：`doImport → ThreadUtil.start → ShellMongoDataImportHandler.doImport`
- 调用链：`bindListeners → database.selectedItemChanged → initFileTable`

## ShellMongoDataTransportController

- 职责：MongoDB 数据传输对话框业务，分三步选择来源/目标连接与库、勾选表/函数并执行传输。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | database | String | 数据库 |
  | step1 | FXVBox | 第一步 |
  | step2 | FXVBox | 第二步 |
  | step3 | FXVBox | 第三步 |
  | sourceInfoName | FXLabel | 来源信息名称 |
  | sourceDatabaseName | FXLabel | 来源库名称 |
  | targetInfoName | FXLabel | 目标信息名称 |
  | targetDatabaseName | FXLabel | 目标库名称 |
  | sourceInfo | ShellConnectTextField | 来源信息 |
  | targetInfo | ShellConnectTextField | 目标信息 |
  | sourceDatabase | ShellMongoDatabaseComboBox | 来源库组件 |
  | targetDatabase | ShellMongoDatabaseComboBox | 目标库组件 |
  | sourceHost | FXLabel | 来源主机 |
  | targetHost | FXLabel | 目标主机 |
  | sourceVersion | FXLabel | 来源服务版本 |
  | targetVersion | FXLabel | 目标服务版本 |
  | sourceType | FXLabel | 来源服务类型 |
  | targetType | FXLabel | 目标服务类型 |
  | sourceClient | ShellMongoClient | 来源客户端 |
  | targetClient | ShellMongoClient | 目标客户端 |
  | stopTransportBtn | FXButton | 结束传输按钮 |
  | transportStatus | FXLabel | 传输状态 |
  | transportMsg | MsgTextArea | 传输消息 |
  | tablePane | FXTitledPane | 表组件 |
  | functionPane | FXTitledPane | 函数组件 |
  | tableList | DBDataTransportNameListView | 表列表 |
  | functionList | DBDataTransportNameListView | 函数列表 |
  | execTask | Thread | 传输操作任务 |
  | counter | Counter | 计数器（`final`） |
  | transportHandler | ShellMongoDataTransportHandler | 传输处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doTransport()` | 执行传输 | `counter.reset()`、清空消息与状态；新建 `ShellMongoDataTransportHandler()` 设置消息/进度处理器，或 `interrupt(false)`；设置来源/目标客户端与库、表 `tableList.getSelectedObjects()`、函数 `functionList.getSelectedObjects()`；`NodeGroupUtil.disable(stage,"exec")`、`stage.appendTitle(...)`；`ThreadUtil.start(...)` 内 `transportHandler.doTransport()`，异常区分取消/失败；`finally` 恢复界面 |
  | `void stopTransport()` | 结束传输 | `ThreadUtil.interrupt(execTask)`、`execTask=null`；非空则 `transportHandler.interrupt()` |
  | `void bindListeners()` | 绑定来源/目标选择与列表变化 | `sourceInfo.selectedItemChanged` → `StageManager.showMask(() -> doConnect(1, ...))`；`targetInfo.selectedItemChanged` → `doConnect(2, ...)`；来源/目标库变化时刷新对应名称标签并 `clearList()`；`tableList/functionList.setSelectedChanged` → `flushPaneText("table"/"function")` |
  | `void doConnect(int type, ShellConnect connect)` | 建立来源/目标连接 | `type==1` 时写 `sourceHost/sourceType/sourceInfoName`、关闭旧 `sourceClient`、`ShellClientUtil.newClient(connect)` 后 `start()`、`selectVersion()`、`sourceDatabase.enable/init(client, database)`；`type==2` 时同理处理目标（`targetDatabase.init(client)`）；为空则清空标签并禁用库；异常 `MessageBox.warn(connectInitFail())` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时预选来源连接 | `super.onWindowShown(event)`、`stage.hideOnEscape()`；`getProp("connect")`、`database=getProp("dbName")`；`connect` 非空时 `sourceInfo.selectItem(connect)` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时停止传输 | `super.onWindowHidden(event)`、`stopTransport()` |
  | `void updateStatus(String extraMsg)` | 更新状态文本 | 非空时 `counter.setExtraMsg(extraMsg)`；`transportStatus.text(counter.unknownFormat())` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.transportTitle()` |
  | `void showStep1()` | 显示第一步 | 切换 step |
  | `void showStep2()` | 显示第二步 | 校验来源/目标连接与库非空并逐项 `requestFocus()`+`MessageBox.warn`；来源与目标同名同库时提示 `pleaseCheckDatabase()`；`tableList/functionList` 为空时用 `sourceClient.listCollections/listFunctions(...)` 填充；切换 step |
  | `void showStep3()` | 显示第三步 | 切换 step |
  | `void clearList()` | 清空数据列表 | `tableList.clearItems()`、`functionList.clearItems()` |
  | `void flushPaneText(String name)` | 刷新面板文字 | `table` 时 `tablePane.setAppendText("(选/总)")`；`function` 时 `functionPane.setAppendText(...)` |

- 调用链：`sourceInfo.selectedItemChanged → doConnect → ShellClientUtil.newClient → sourceDatabase.init`
- 调用链：`showStep2 → ShellMongoClient.listCollections/listFunctions → tableList.of/functionList.of`
- 调用链：`doTransport → ThreadUtil.start → ShellMongoDataTransportHandler.doTransport`

## ShellMongoRunScriptFileController

- 职责：MongoDB 运行脚本文件对话框业务，选择 .js 文件与数据库后执行并反馈进度。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbInfo | ShellConnect | 连接信息 |
  | dbClient | ShellMongoClient | mongodb 客户端 |
  | stopScriptFileBtn | FXButton | 结束运行 sql 按钮 |
  | execStatus | FXLabel | 执行状态 |
  | execMsg | MsgTextArea | 执行消息 |
  | connect | ReadOnlyTextField | 连接 |
  | database | ShellMongoDatabaseComboBox | 数据库 |
  | continueWithErrors | FXCheckBox | 遇到错误时继续 |
  | file | ChooseFileTextField | 文件 |
  | execTask | Thread | sql 操作任务 |
  | counter | Counter | 计数器（`final`） |
  | scriptFileHandler | DBDataRunFileHandler | sql 处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean checkSqlFile()` | 检查脚本文件是否已选 | `file.getFile()` 为空时 `MessageBox.warn(pleaseSelectFile())` 返回 false |
  | `void runScriptFile()` | 运行脚本文件 | `checkSqlFile()`；`database.getSelectedItem()` 为空时 `MessageBox.warn(pleaseSelectDatabase())`；`counter.reset()`、`execMsg.clear()`；新建 `ShellMongoRunScriptFileHandler(dbClient, database)` 设置消息/进度处理器，或 `interrupt(false)`；设置 `file.getFile()`、`continueWithErrors.isSelected()`；`NodeGroupUtil.disable(stage,"exec")`、`stage.appendTitle(...)`；`ThreadUtil.start(...)` 内 `scriptFileHandler.runFile()`，异常区分取消/失败；`finally` 恢复界面 |
  | `void stopScriptFile()` | 结束脚本文件 | `ThreadUtil.interrupt(execTask)`、`execTask=null`；非空则 `scriptFileHandler.interrupt()` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化库与连接 | `super.onWindowShown(event)`；`getProp("dbClient")`、`getProp("dbName")`；`database.init(dbClient, dbName)`、`connect.setText(dbInfo.getName())`；`stage.hideOnEscape()` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时停止运行 | `super.onWindowHidden(event)`、`stopScriptFile()` |
  | `void updateStatus(String extraMsg)` | 更新状态文本 | 非空时 `counter.setExtraMsg(extraMsg)`；`execStatus.text(counter.unknownFormat())` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nResourceBundle.i18nString("base.runSqlFile")` |
  | `void onStageInitialize(StageAdapter stage)` | 初始化文件过滤器 | `super.onStageInitialize(stage)`、`file.setFilter(FXChooser.jsExtensionFilter())` |

- 调用链：`runScriptFile → ShellMongoRunScriptFileHandler.runFile → DBDataRunFileHandler.runFile`
- 调用链：`onWindowShown → database.init → connect.setText`
- 调用链：`onWindowHidden → stopScriptFile → scriptFileHandler.interrupt`

## ShellMongoBucketDocumentUpdateController

- 职责：修改存储桶文档对话框业务，编辑文件名与元数据后回写 document 属性并关闭窗口。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | filename | ClearableTextField | 文件名 |
  | metadata | JsonEditor | 元数据 |
  | record | MongoBucketFile | 数据 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void update()` | 修改存储桶文档 | 取 `metadata.getTextTrim()` 并 `strip()`；空白置 null，`{` 开头且 `JSONUtil.isJson` 时 `MongoScriptUtil.toDocument(JSONUtil.parseObject(...))`；否则 `MessageBox.warn(invalidMetadata())` 返回；构造 `MongoBucketFile` 设置 fileName/metadata/id/dbName/bucketName（均取自 `record`）；`setProp("document", bucketFile)`、`closeWindow()`；异常 `MessageBox.exception(ex)` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时回填文档数据 | `super.onWindowShown(event)`；`removeProp("document")` 取 `record`；`filename.setText(record.getFileName())`、`metadata.setText(record.getMetadataJson())`；`stage.switchOnTab()`、`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.updateDocument()` |

- 调用链：`update → JSONUtil.isJson → MongoScriptUtil.toDocument → closeWindow`
- 调用链：`onWindowShown → removeProp("document") → filename.setText/metadata.setText`

## ShellMongoCollectionDocumentAddController

- 职责：添加集合记录对话框业务，按列默认值生成文档脚本供编辑并回写 doc 属性。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | doc | Editor | 文档 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void add()` | 添加文档 | 取 `doc.getText()` 后 `setProp("doc", doc)`、`closeWindow()`；异常 `MessageBox.exception(ex)` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.addDocument()` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时按列生成初始文档 | `super.onWindowShown(event)`；`getProp("columns")`；列为空时设置空 JSON 模板文本，否则构造 `MongoRecord(columns)` 逐列 `putValue(name, defaultValue())` 后 `doc.setText(ShellMongoDataUtil.getRecordScript(record, false))`；`stage.switchOnTab()`、`stage.hideOnEscape()` |

- 调用链：`onWindowShown → MongoRecord.putValue → ShellMongoDataUtil.getRecordScript → doc.setText`
- 调用链：`add → setProp("doc") → closeWindow`

## ShellMongoCollectionDocumentUpdateController

- 职责：修改集合记录对话框业务，展示记录脚本供编辑并回写 doc 属性。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | doc | Editor | 文档 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void update()` | 更新文档 | 取 `doc.getText()` 后 `setProp("doc", doc)`、`closeWindow()`；异常 `MessageBox.exception(ex)` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.updateDocument()` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时回填记录脚本 | `super.onWindowShown(event)`；`getProp("document")` 取 `MongoRecord`；`ShellMongoDataUtil.getRecordScript(record, true)` 后 `doc.setText(text)`；`stage.switchOnTab()`、`stage.hideOnEscape()` |

- 调用链：`onWindowShown → ShellMongoDataUtil.getRecordScript → doc.setText`
- 调用链：`update → setProp("doc") → closeWindow`

## ShellMongoUserCreateController

- 职责：创建 MongoDB 用户对话框业务，填写用户名/密码、为各库勾选角色并创建用户。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | user | ClearableTextField | 用户名 |
  | password | PasswordTextField | 密码 |
  | roleTableView | FXTableView<MongoUserRoleDb> | 角色 |
  | dbItem | ShellMongoDatabaseTreeItem | db 节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void create()` | 创建用户 | `user.validate()`、`password.validate()` 校验；构造 `MongoUser` 设置 db（`dbItem.dbName()`）/user/password；遍历 `roleTableView.getItems()`，跳过 `isEmpty()` 项，按 `item.getRoles()` 逐角色构造 `MongoUserRole`（setDb/setRole）汇入 roles；`mongoUser.setRoles(roles)`；`dbItem.createUser(mongoUser)` 成功则 `setProp("user", mongoUser)`、`closeWindow()`，否则 `MessageBox.warn(operationFail())`；异常 `MessageBox.exception(ex)` |
  | `String getViewTitle()` | 返回窗口标题 | `I18nHelper.createUser()` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时加载库列表 | `super.onWindowShown(event)`；`getProp("dbItem")`；`dbItem.listDatabaseNames()` 逐库构造 `MongoUserRoleDb`（setDb）并 `roleTableView.addItem(...)`；`stage.switchOnTab()`、`stage.hideOnEscape()` |

- 调用链：`onWindowShown → dbItem.listDatabaseNames → roleTableView.addItem`
- 调用链：`create → MongoUserRoleDb.getRoles → dbItem.createUser → closeWindow`

## ShellMongoBucketDocumentViewController

- 该文件整体已被注释（死代码，无有效类），跳过。

## ShellMongoUserViewController

- 该文件整体已被注释（死代码，无有效类），跳过。

## ShellRedisExportDataController

- 职责：Redis 数据导出四步向导业务，配置文件格式/键类型/保留 TTL/压缩/包含标题并异步执行导出，可中途停止。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步面板 |
  | step2 | FXVBox | 第二步面板 |
  | step3 | FXVBox | 第三步面板 |
  | step4 | FXVBox | 第四步面板 |
  | exportFile | File | 导出文件 |
  | format | FXToggleGroup | 文件格式选择组 |
  | fileName | FXText | 文件名展示 |
  | selectFile | FXButton | 选择文件按钮 |
  | retainTTL | FXCheckBox | 是否保留 TTL |
  | includeTitle | FXCheckBox | 是否包含标题行 |
  | compress | FXCheckBox | 是否压缩 |
  | keys | ReadOnlyTextArea | 受影响的键展示区 |
  | db | ShellRedisDatabaseComboBox | 数据库选择框 |
  | pattern | ClearableTextField | 键模式 |
  | stringType | FXCheckBox | string 类型 |
  | listType | FXCheckBox | list 类型 |
  | streamType | FXCheckBox | stream 类型 |
  | setType | FXCheckBox | set 类型 |
  | zsetType | FXCheckBox | zset 类型 |
  | hashType | FXCheckBox | hash 类型 |
  | jsonType | FXCheckBox | json 类型 |
  | stopExportBtn | FXButton | 结束导出按钮 |
  | exportStatus | FXLabel | 导出状态标签 |
  | exportMsg | MsgTextArea | 导出消息输出区 |
  | dbIndex | Integer | 指定导出库索引（可为 null） |
  | connect | ShellConnect | 当前连接 |
  | client | ShellRedisClient | 当前客户端 |
  | execTask | Thread | 导出执行线程 |
  | counter | Counter | 计数器（`final`） |
  | exportHandler | ShellRedisDataExportHandler | 导出处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 窗口显示时读取属性 | 取 `getProp("connect")` 赋给 `connect`、`getProp("dbIndex")` 赋给 `dbIndex` |
  | `String getViewTitle()` | 返回标题 | `I18nHelper.exportTitle()` |
  | `void doExport()` | 执行导出 | 重置 `counter`、清空 `exportMsg/exportStatus`；`NodeGroupUtil.disable(stage,"exec")`；懒建 `exportHandler` 并设 `setMessageHandler`/`setProcessedHandler`；按 `format.selectedUserData()` 设文件类型，按勾选拼 `keyTypes`（注意 list 分支被加入 `"zset"`，疑似笔误）；`ThreadUtil.start` 中调 `exportHandler.doExport()` |
  | `void stopExport()` | 结束导出 | `ThreadUtil.interrupt(execTask)`；`exportHandler.interrupt()` |
  | `void bindListeners()` | 绑定监听 | `stage.hideOnEscape()`；`format.selectedToggleProperty()` 变化时清空 `exportFile`/`fileName` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时停止导出 | 调用 `stopExport()` |
  | `void updateStatus(String extraMsg)` | 更新状态文本 | `counter.setExtraMsg` 后 `exportStatus.text(counter.unknownFormat())` |
  | `void showStep1()` | 切换到第一步 | 隐藏 step2/3/4，显示 step1 |
  | `void showStep2()` | 切换到第二步 | `ShellClientUtil.newClient` 建连并 `start(2500)`，`DownLatch` 等待；初始化 `db` 项与库数 `client.databases()` |
  | `void showStep3()` | 切换到第三步 | 依 `format.selectedUserData()` 判断是否启用 `includeTitle`/`compress` |
  | `void showStep4()` | 切换到第四步 | `exportFile` 为空时 `ValidatorUtil.validFail(selectFile)` |
  | `void selectFile()` | 选择导出文件 | `FXChooser.extensionFilter`、`FileChooserHelper.save`；存在则 `FileUtil.del` |
  | `void showKeys()` | 展示受影响的键 | `client.fullKeys`（全库）或 `client.allKeys(db, pattern)`，写入 `keys` |

- 调用链：`showStep2 → ShellClientUtil.newClient → client.start`
- 调用链：`doExport → ShellRedisDataExportHandler.doExport`
- 调用链：`showKeys → client.allKeys → ReadOnlyTextArea.appendLines`

## ShellRedisImportDataController

- 职责：Redis 数据导入三步向导业务，配置文件格式/忽略已存在/数据行起始并异步执行导入，可中途停止。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步面板 |
  | step2 | FXVBox | 第二步面板 |
  | step3 | FXVBox | 第三步面板 |
  | importFile | File | 导入文件 |
  | format | FXToggleGroup | 文件格式选择组 |
  | fileName | FXText | 文件名展示 |
  | ignoreExist | FXCheckBox | 存在时忽略 |
  | dataRowStarts | NumberTextField | 数据行起始 |
  | selectFile | FXButton | 选择文件按钮 |
  | stopImportBtn | FXButton | 结束导入按钮 |
  | importStatus | FXLabel | 导入状态标签 |
  | importMsg | MsgTextArea | 导入消息输出区 |
  | dbIndex | Integer | db 索引 |
  | connect | ShellConnect | 当前连接 |
  | client | ShellRedisClient | 当前客户端 |
  | execTask | Thread | 导入执行线程 |
  | counter | Counter | 计数器（`final`） |
  | importHandler | ShellRedisDataImportHandler | 导入处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 窗口显示时读取属性 | 取 `getProp("connect")`、`getProp("dbIndex")` |
  | `String getViewTitle()` | 返回标题 | `I18nHelper.importTitle()` |
  | `void doImport()` | 执行导入 | 重置 `counter`；懒建 `importHandler` 并设 `setMessageHandler`/`setProcessedHandler`；设 `dbIndex`/文件类型/`setIgnoreExist`/`filePath`；`ThreadUtil.start` 中调 `importHandler.doImport()` |
  | `void stopImport()` | 结束导入 | `ThreadUtil.interrupt(execTask)`；`importHandler.interrupt()` |
  | `void bindListeners()` | 绑定监听 | `stage.hideOnEscape()`；`format.selectedToggleProperty()` 变化时清空 `importFile`/`fileName` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时停止导入 | 调用 `stopImport()` |
  | `void updateStatus(String extraMsg)` | 更新状态文本 | `counter.setExtraMsg` 后 `importStatus.text(counter.unknownFormat())` |
  | `void showStep1()` | 切换到第一步 | 隐藏 step2/3，显示 step1 |
  | `void showStep2()` | 切换到第二步 | 文件类型为 excel/csv 时启用 `dataRowStarts`，否则禁用 |
  | `void showStep3()` | 切换到第三步 | `importFile` 为空校验；`doConnect()` 成功后切换 |
  | `boolean doConnect()` | 建立连接 | `ShellClientUtil.newClient` + `start(2500)`，`DownLatch` 等待，失败 `MessageBox.warn` 返回 false |
  | `void selectFile()` | 选择导入文件 | `FileChooserHelper.choose`，回填 `fileName` |

- 调用链：`showStep3 → doConnect → ShellClientUtil.newClient`
- 调用链：`doImport → ShellRedisDataImportHandler.doImport`
- 调用链：`bindListeners → FXToggleGroup.selectedToggleProperty.addListener`

## ShellRedisTransportDataController

- 职责：Redis 跨连接/跨库数据传输四步向导业务，配置来源/目标连接与库、存在策略、键类型并异步执行传输，可中途停止。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步面板 |
  | step2 | FXVBox | 第二步面板 |
  | step3 | FXVBox | 第三步面板 |
  | step4 | FXVBox | 第四步面板 |
  | sourceInfoName | FXLabel | 来源信息名称 |
  | targetInfoName | FXLabel | 目标信息名称 |
  | sourceInfo | ShellConnectTextField | 来源信息选择 |
  | sourceDatabase | ShellRedisDatabaseComboBox | 来源数据库 |
  | sourceDatabaseName | FXLabel | 来源数据库名称 |
  | targetInfo | ShellConnectTextField | 目标信息选择 |
  | targetDatabase | ShellRedisDatabaseComboBox | 目标数据库 |
  | targetDatabaseName | FXLabel | 目标数据库名称 |
  | sourceHost | FXLabel | 来源主机 |
  | targetHost | FXLabel | 目标主机 |
  | sourceClient | ShellRedisClient | 来源客户端 |
  | targetClient | ShellRedisClient | 目标客户端 |
  | stopTransportBtn | FXButton | 结束传输按钮 |
  | transportStatus | FXLabel | 传输状态标签 |
  | transportMsg | MsgTextArea | 传输消息输出区 |
  | existsPolicy | FXToggleGroup | 节点存在时处理策略 |
  | retainTTL | FXCheckBox | 是否保留 TTL |
  | keys | ReadOnlyTextArea | 受影响的键展示区 |
  | pattern | ClearableTextField | 键模式 |
  | stringType | FXCheckBox | string 类型 |
  | listType | FXCheckBox | list 类型 |
  | streamType | FXCheckBox | stream 类型 |
  | setType | FXCheckBox | set 类型 |
  | zsetType | FXCheckBox | zset 类型 |
  | hashType | FXCheckBox | hash 类型 |
  | execTask | Thread | 传输执行线程 |
  | counter | Counter | 计数器（`final`） |
  | transportHandler | ShellRedisDataTransportHandler | 传输处理器 |
  | presetDbIndex | Integer | 预选的 db 索引 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 有 `getProp("sourceConnect")` 则选中并禁用 `sourceInfo`；有 `getProp("dbIndex")` 则设 `presetDbIndex` 并禁用 `sourceDatabase`；`stage.hideOnEscape()` |
  | `String getViewTitle()` | 返回标题 | `I18nHelper.transportTitle()` |
  | `void doTransport()` | 执行传输 | 重置 `counter`；懒建 `transportHandler`；设来源/目标客户端与库、`existsPolicy.selectedUserData()`、按勾选拼 `keyTypes`（注意 list 分支被加入 `"zset"`，疑似笔误）、`pattern`、`setRetainTTL`；`ThreadUtil.start` 调 `transportHandler.doTransport()` |
  | `void stopTransport()` | 结束传输 | `ThreadUtil.interrupt(execTask)`；`transportHandler.interrupt()` |
  | `void bindListeners()` | 绑定监听 | `sourceInfo/targetInfo` 选中项变化时更新主机/名称并调 `initSourceDatabase`/`initTargetDatabase`；库选择变化时更新 `sourceDatabaseName`/`targetDatabaseName` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏时停止传输 | 调用 `stopTransport()` |
  | `void updateStatus(String extraMsg)` | 更新状态文本 | `counter.setExtraMsg` 后 `transportStatus.text(counter.unknownFormat())` |
  | `void initSourceDatabase(ShellConnect sourceInfo)` | 初始化来源连接与库 | 关闭旧 `sourceClient`；`ShellClientUtil.newClient` + `start(2500)`；`setDbCount(client.databases())`，按 `presetDbIndex` 选择或 `selectFirst()` |
  | `void initTargetDatabase(ShellConnect targetInfo)` | 初始化目标连接与库 | 同上，使用 `targetClient`/`targetDatabase`，固定 `selectFirst()` |
  | `void showKeys()` | 展示受影响的键 | `sourceClient.allKeys(sourceDatabase.getDB(), pattern)` 写入 `keys` |
  | `void showStep1()` / `void showStep2()` / `void showStep3()` / `void showStep4()` | 步骤切换 | showStep2 校验来源/目标连接非空、非同连接同库，客户端有效后切换 |

- 调用链：`bindListeners → initSourceDatabase → ShellClientUtil.newClient → sourceDatabase.setDbCount`
- 调用链：`doTransport → ShellRedisDataTransportHandler.doTransport`
- 调用链：`showStep2 → MessageBox.warn(I18nHelper.databasesCannotBeTheSame)`

## ShellRedisKeyAddController

- 职责：新增指定类型的 Redis 键，按类型显示对应输入控件并选择命令写入，支持粘贴/清空/JSON 格式化。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | key | ClearableTextField | 键名称 |
  | bitBox | FXVBox | 位图输入组件 |
  | streamBox | FXVBox | stream 输入组件 |
  | stringBox | FXVBox | string 输入组件 |
  | jsonBox | FXVBox | json 输入组件 |
  | listBox | FXVBox | list 输入组件 |
  | setBox | FXVBox | set 输入组件 |
  | zSetBox | FXVBox | zSet 输入组件 |
  | hashBox | FXVBox | hash 输入组件 |
  | hylogBox | FXVBox | hylog 输入组件 |
  | coordinateBox | FXVBox | coordinate 输入组件 |
  | fieldName | Editor | 字段名编辑器 |
  | bitValue | FXToggleSwitch | bit 值开关 |
  | bitIndex | NumberTextField | bit 索引 |
  | ttlValue | NumberTextField | ttl 值 |
  | scoreValue | DecimalTextField | 分数值 |
  | longitudeValue | DecimalTextField | 经度值 |
  | latitudeValue | DecimalTextField | 纬度值 |
  | root | FXVBox | 根容器 |
  | streamIDValue | ClearableTextField | stream 消息 ID |
  | type | ShellRedisKeyTypeComboBox | 键类型选择 |
  | client | ShellRedisClient | redis 客户端 |
  | dbIndex | Integer | 数据库索引 |
  | mutexes | NodeMutexes | 节点互斥组件（`final`） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | `stage.switchOnTab()`；`mutexes.manageBindVisible()` 与 `addNodes` 注册各类型面板；取 `getProp("client")`/`("dbIndex")`/`("type")` 并 `type.select`；`key.requestFocus()` |
  | `String getViewTitle()` | 返回标题 | `I18nResourceBundle.i18nString("shell.redis.title.key.add")` |
  | `void destroy()` | 释放资源 | `mutexes.destroy()`，遍历 `valueEditors()` 逐个 `destroy()` |
  | `void addKey()` | 添加键 | 校验 ttl/键名/`client.exists`；据 `type.getSelectedIndex()` 分派 addXxxNode；成功后 `client.expire` 并 `setProp("key", key)`、`closeWindow()` |
  | `Editor valueEditor()` | 取当前可见值编辑器 | 依 `stringBox/jsonBox/.../streamBox` 的 `isVisible()` 用 `lookup` 取 Editor |
  | `List<Editor> valueEditors()` | 取全部值编辑器 | 对九个面板 `lookup` 收集 `List<Editor>` |
  | `String valueText()` | 取当前值文本 | `valueEditor().getText()` |
  | `boolean addStringNode(int dbIndex, String key)` | 添加 string | `client.set` |
  | `boolean addJsonNode(int dbIndex, String key)` | 添加 json | `client.jsonSet` |
  | `boolean addListNode(int dbIndex, String key)` | 添加 list | `client.lpush` |
  | `boolean addSetNode(int dbIndex, String key)` | 添加 set | `client.sadd` |
  | `boolean addZSetNode(int dbIndex, String key)` | 添加 zset | 校验 `scoreValue`；`client.zadd` |
  | `boolean addHashNode(int dbIndex, String key)` | 添加 hash | 校验 `fieldName`；`client.hset` |
  | `boolean addHyLogNode(int dbIndex, String key)` | 添加 hyperLogLog | 按行拆分去空；`client.pfadd` |
  | `boolean addGEONode(int dbIndex, String key)` | 添加 geo | 校验经纬度；`client.geoadd` |
  | `boolean addStreamNode(int dbIndex, String key)` | 添加 stream | 校验 JSON 与 `streamIDValue`；`XAddParams.id`；`client.xadd` |
  | `boolean addBitNode(int dbIndex, String key)` | 添加 bit | `client.setbit` |
  | `void pasteData()` | 粘贴数据 | `valueEditor().paste()` 并请求焦点 |
  | `void clearData()` | 清空数据 | `valueEditor().clear()` 并请求焦点 |
  | `void parseToJson()` | JSON 与文本互转 | 依 `getUserData()` 调 `JSONUtil.toJson`/`JSONUtil.toPretty` 并切换 userData |
  | `void bindListeners()` | 绑定监听 | `type.selectedIndexChanged` 按索引用 `mutexes.visible` 显示对应面板，`root.parentAutosize()` |

- 调用链：`addKey → client.exists → addXxxNode → client.<命令> → closeWindow`
- 调用链：`bindListeners → NodeMutexes.visible`
- 调用链：`onWindowShown → mutexes.manageBindVisible → type.select`

## ShellRedisKeyBatchOperationController

- 职责：多标签页 Redis 键批量操作业务，提供删除/设置 TTL/清空库/移动/复制/统计键等批量动作。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | root | FXTabPane | 根标签面板 |
  | ttl | NumberTextField | ttl 值 |
  | pattern1 | ClearableTextField | 删除键表达式 |
  | pattern2 | ClearableTextField | 设置 ttl 键表达式 |
  | pattern4 | ClearableTextField | 移动键表达式 |
  | pattern5 | ClearableTextField | 复制键表达式 |
  | pattern6 | ClearableTextField | 统计键表达式 |
  | keys1 | MsgTextArea | 删除键展示区 |
  | keys2 | MsgTextArea | 设置 ttl 键展示区 |
  | keys3 | MsgTextArea | 清空库键展示区 |
  | keys4 | MsgTextArea | 移动键展示区 |
  | keys5 | MsgTextArea | 复制键展示区 |
  | keys6 | MsgTextArea | 统计键展示区 |
  | dbIndex | Integer | db 索引 |
  | client | ShellRedisClient | redis 客户端 |
  | moveTargetDB | ShellRedisDatabaseComboBox | 移动目标库 |
  | copyTargetDB | ShellRedisDatabaseComboBox | 复制目标库 |
  | replaceOnCopy | FXCheckBox | 复制时替换 |
  | execTask | Thread | 异步任务线程 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 取 `getProp("client")`/`("dbIndex")`；`moveTargetDB`/`copyTargetDB` 设库数并 `selectFirst()`；标题追加 db；标签切换时设置 `exec`/`active` 分组 |
  | `String getViewTitle()` | 返回标题 | `I18nResourceBundle.i18nString("shell.redis.title.key.batchOperation")` |
  | `void delKeys()` | 批量删除键 | `throwSentinelException`；确认后 `ThreadUtil.start`，`findKeys` 扫描并逐个 `client.del`；`ShellRedisEventUtil.redisKeyFlushed`；统计成功/失败 |
  | `void expireKeys()` | 批量设置过期 | `ttl == -1` 用 `client.persist`，否则 `client.expire`；逐个处理并统计 |
  | `void flushDB()` | 清空当前库 | 确认后 `client.flushDB(dbIndex)`；`redisKeyFlushed`；`MessageBox.okToast` |
  | `void moveKeys()` | 批量移动键 | `throwClusterException`/`throwSentinelException`；目标库相同告警；逐个 `client.move`；`ShellRedisEventUtil.redisKeysMoved` |
  | `void copyKeys()` | 批量复制键 | 追加 `throwCommandException("copy")`；逐个 `client.copy`；`ShellRedisEventUtil.redisKeysCopied` |
  | `void countKeys()` | 统计键数量 | 循环 `ShellRedisKeyUtil.scanKeysSimple` 累加 `keySize`，`keys6.text` 展示 `I18nHelper.found()` |
  | `void stopExec()` | 停止执行 | `ThreadUtil.interrupt(execTask)`；`NodeGroupUtil.enable`；`stage.restoreTitle()` |
  | `void showKeys(Collection<String> keys, FXTextArea area)` | 展示键列表 | 清空后逐行 `序号 + ") " + key` |
  | `List<String> findKeys(FXTextArea area, String pattern)` | 扫描键 | 循环 `ShellRedisKeyUtil.scanKeysSimple` 收集键，实时 `area.text` 展示数量 |
  | `void showKeys1()` … `void showKeys5()` | 各标签页预览键 | 调 `ShellRedisKeyUtil.scanKeys` 后 `showKeys` 展示（showKeys3 用 `"*"`） |
  | `void bindListeners()` | 绑定监听 | `pattern1/2/4` 文本变化时清空对应 `keys1/2/4` |
  | `void onWindowHiding(WindowEvent event)` | 窗口隐藏时中断任务 | `ThreadUtil.interrupt(execTask)` |

- 调用链：`delKeys → findKeys → ShellRedisKeyUtil.scanKeysSimple → client.del`
- 调用链：`copyKeys → client.throwCommandException → client.copy → ShellRedisEventUtil.redisKeysCopied`
- 调用链：`onWindowShown → ShellRedisDatabaseComboBox.setDbCount → selectFirst`

## ShellRedisKeyCopyController

- 职责：单键复制对话框业务，将指定键复制到目标库并可选替换已存在键。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | key | DisabledTextField | 键展示（只读） |
  | replace | FXCheckBox | 存在时替换 |
  | targetDB | ShellRedisDatabaseComboBox | 目标数据库 |
  | client | ShellRedisClient | redis 客户端 |
  | submit | SubmitButton | 提交按钮 |
  | treeItem | ShellRedisKeyTreeItem | 树节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void copyKey()` | 复制键 | `throwClusterException`/`throwSentinelException`/`throwCommandException("copy")`；目标库相同告警；`client.copy(fromDBIndex, key, key, targetDBIndex, replace)`；成功后 `setProp("dbIndex", ...)`、`closeWindow()` |
  | `void bindListeners()` | 绑定监听 | `targetDB.selectedIndexChanged` 目标库等于源库时禁用 `submit` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 取 `getProp("treeItem")`；`client = treeItem.client()`；`key.setText("key（dbN）")`；`targetDB.setDbCount`、`selectFirst`、`requestFocus` |
  | `String getViewTitle()` | 返回标题 | `I18nHelper.copyKey()` |

- 调用链：`copyKey → client.copy → setProp("dbIndex") → closeWindow`
- 调用链：`onWindowShown → treeItem.client → targetDB.setDbCount`

## ShellRedisKeyMoveController

- 职责：单键移动对话框业务，将指定键移动到目标库并可选保留 TTL（源库键被移除）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | key | DisabledTextField | 键展示（只读） |
  | retainTTL | FXCheckBox | 保留 ttl |
  | targetDB | ShellRedisDatabaseComboBox | 目标数据库 |
  | client | ShellRedisClient | redis 客户端 |
  | submit | SubmitButton | 提交按钮 |
  | treeItem | ShellRedisKeyTreeItem | 树节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void moveKey()` | 移动键 | `throwClusterException`/`throwSentinelException`；目标库相同告警；`client.exists` 目标已存在告警；`retainTTL` 时 `client.ttl`；`client.move`；成功后 `client.expire` 恢复 ttl、`setProp("dbIndex", ...)`、`closeWindow()` |
  | `void bindListeners()` | 绑定监听 | `targetDB.selectedIndexChanged` 目标库等于源库时禁用 `submit` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 取 `getProp("treeItem")`；`client = treeItem.client()`；`key.setText("key（dbN）")`；`targetDB.setDbCount`、`selectFirst`、`requestFocus` |
  | `void onWindowHidden(WindowEvent event)` | 窗口隐藏回调 | 仅调用 `super.onWindowHidden(event)`，无额外逻辑 |
  | `String getViewTitle()` | 返回标题 | `I18nHelper.moveKey()` |

- 调用链：`moveKey → client.move → client.expire → closeWindow`
- 调用链：`bindListeners → targetDB.selectedIndexChanged → submit.disable`

## ShellRedisKeyTTLController

- 职责：单键 TTL 设置对话框业务，可持久化、按 1 分钟/1 小时/1 天设置或追加，并预览到期时间。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | showTime | Long | 窗口显示时的时间戳 |
  | ttl | NumberTextField | ttl 值 |
  | client | ShellRedisClient | redis 客户端 |
  | expirePreview | FXLabel | 到期时间预览 |
  | treeItem | ShellRedisKeyTreeItem | 树节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void ttlSetting()` | 设置 TTL | `ttlValue <= -1` 时 `client.persist`；`== 0` 时确认后 `client.del`；否则 `client.expire`；`ShellRedisEventUtil.redisKeyTTLUpdated`；`MessageBox.okToast`、`closeWindow()` |
  | `void persistKey()` | 永久保存 | `ttl.setValue(-1L)` |
  | `void expireAt1D()` | 设为 1 天 | `ttl.setValue(24*3600)` |
  | `void expireAt1H()` | 设为 1 小时 | `ttl.setValue(3600)` |
  | `void expireAt1M()` | 设为 1 分钟 | `ttl.setValue(60)` |
  | `void appendWith1M()` | 追加 1 分钟 | `ttl <= -1` 则 `expireAt1M()`，否则 `ttl.setValue(ttl + 60)` |
  | `void appendWith1H()` | 追加 1 小时 | `ttl <= -1` 则 `expireAt1H()`，否则 `ttl.setValue(ttl + 3600)` |
  | `void appendWith1D()` | 追加 1 天 | `ttl <= -1` 则 `expireAt1D()`，否则 `ttl.setValue(ttl + 24*3600)` |
  | `void resetTTL()` | 重置为原值 | `ttl.setValue(treeItem.ttl())` |
  | `void bindListeners()` | 绑定监听 | `ttl.addTextChangeListener`：`ttl <= -1` 显示 `neverExpire()`，否则用 `Const.DATE_FORMAT` 格式化 `showTime + ttl*1000` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 记录 `showTime`；取 `getProp("treeItem")`、`client = treeItem.client()`；按 `treeItem.ttl()` 回填 `ttl` 与预览 |
  | `String getViewTitle()` | 返回标题 | `I18nResourceBundle.i18nString("shell.redis.title.key.ttlUpdate")` |

- 调用链：`ttlSetting → client.persist / client.expire / client.del → ShellRedisEventUtil.redisKeyTTLUpdated`
- 调用链：`bindListeners → Const.DATE_FORMAT.format`
- 调用链：`onWindowShown → treeItem.ttl → expirePreview.setText`

## ShellRedisHashFieldAddController

- 职责：为 hash 键新增字段行（字段名+值），校验重复后写入。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | fieldName | Editor | 字段名 |
  | rowValue | Editor | 行数据 |
  | treeItem | ShellRedisHashKeyTreeItem | redis 键树节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void addRow()` | 添加字段 | 校验 `fieldName`/`rowValue`；`client.hexists` 已存在告警；`client.hset`；`setProp("result", true)`、`closeWindow()` |
  | `void pasteData()` | 粘贴数据 | `rowValue.paste()` 并请求焦点 |
  | `void clearData()` | 清空数据 | `rowValue.clear()` 并请求焦点 |
  | `void parseToJson()` | JSON 与文本互转 | 依 `getUserData()` 调 `JSONUtil.toJson`/`JSONUtil.toPretty` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 取 `getProp("treeItem")`；`stage.switchOnTab()`、`hideOnEscape()` |
  | `String getViewTitle()` | 返回标题 | `I18nResourceBundle.i18nString("shell.redis.title.hashFieldAdd")` |
  | `void destroy()` | 释放资源 | `fieldName.destroy()`、`rowValue.destroy()` |

- 调用链：`addRow → client.hexists → client.hset → closeWindow`
- 调用链：`parseToJson → JSONUtil.toPretty → Editor.setText`

## ShellRedisHylogElementsAddController

- 职责：为 hyperLogLog（string）键新增元素，按行拆分去空后批量加入。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | rowValue | Editor | 行数据 |
  | treeItem | ShellRedisStringKeyTreeItem | redis 键树节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void addRow()` | 添加元素 | 校验 `rowValue`；`lines()` 拆分并 `CollectionUtil.removeBlank`；`client.pfadd`；失败 `MessageBox.warn`；`setProp("result", true)`、`closeWindow()` |
  | `void pasteData()` | 粘贴数据 | `rowValue.paste()` 并请求焦点 |
  | `void clearData()` | 清空数据 | `rowValue.clear()` 并请求焦点 |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 取 `getProp("treeItem")`；`stage.switchOnTab()`、`hideOnEscape()` |
  | `String getViewTitle()` | 返回标题 | `I18nResourceBundle.i18nString("shell.redis.title.hyLogElementsAdd")` |
  | `void destroy()` | 释放资源 | `rowValue.destroy()` |

- 调用链：`addRow → CollectionUtil.removeBlank → client.pfadd → closeWindow`
- 调用链：`onWindowShown → getProp("treeItem") → stage.switchOnTab`

## ShellRedisListElementAddController

- 职责：为 list 键新增元素，支持选择头插/尾插模式。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | rowValue | Editor | 行数据 |
  | insertMode | FXToggleGroup | 插入模式选择组 |
  | treeItem | ShellRedisListKeyTreeItem | redis 键树节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void addRow()` | 添加元素 | 校验 `rowValue`；`insertMode.selectedUserData()` 为 `"0"` 时 `client.lpushx`、`"1"` 时 `client.rpushx`；`setProp("result", true)`、`closeWindow()` |
  | `void pasteData()` | 粘贴数据 | `rowValue.paste()` 并请求焦点 |
  | `void clearData()` | 清空数据 | `rowValue.clear()` 并请求焦点 |
  | `void parseToJson()` | JSON 与文本互转 | 依 `getUserData()` 调 `JSONUtil.toJson`/`JSONUtil.toPretty` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 取 `getProp("treeItem")`；`stage.switchOnTab()`、`hideOnEscape()` |
  | `String getViewTitle()` | 返回标题 | `I18nResourceBundle.i18nString("shell.redis.title.listRowAdd")` |
  | `void destroy()` | 释放资源 | `rowValue.destroy()` |

- 调用链：`addRow → insertMode.selectedUserData → client.lpushx / client.rpushx → closeWindow`
- 调用链：`bindListeners（无）→ onWindowShown → getProp("treeItem")`

## ShellRedisSetMemberAddController

- 职责：为 set 键新增成员，校验成员已存在后写入。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | rowValue | Editor | 行数据 |
  | treeItem | ShellRedisSetKeyTreeItem | redis 键树节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void addRow()` | 添加成员 | 校验 `rowValue`；`client.sismember` 已存在告警；`client.sadd`；`setProp("result", true)`、`closeWindow()` |
  | `void pasteData()` | 粘贴数据 | `rowValue.paste()` 并请求焦点 |
  | `void clearData()` | 清空数据 | `rowValue.clear()` 并请求焦点 |
  | `void parseToJson()` | JSON 与文本互转 | 依 `getUserData()` 调 `JSONUtil.toJson`/`JSONUtil.toPretty` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 取 `getProp("treeItem")`；`stage.switchOnTab()`、`hideOnEscape()` |
  | `String getViewTitle()` | 返回标题 | `I18nResourceBundle.i18nString("shell.redis.title.setMemberAdd")` |
  | `void destroy()` | 释放资源 | `rowValue.destroy()` |

- 调用链：`addRow → client.sismember → client.sadd → closeWindow`
- 调用链：`parseToJson → JSONUtil.toJson → Editor.setUserData`

## ShellRedisStreamMessageAddController

- 职责：为 stream 键新增消息，指定消息 ID 与 JSON 字段内容。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | rowValue | Editor | 消息内容（JSON） |
  | streamID | ClearableTextField | 消息 id |
  | treeItem | ShellRedisStreamKeyTreeItem | redis 键树节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void addRow()` | 添加消息 | 校验 `rowValue`、`JSONUtil.isJson`、`JSONUtil.parseObject` 非空、`streamID`；`XAddParams.id`；`client.xadd`；`setProp("result", true)`、`closeWindow()` |
  | `void pasteData()` | 粘贴数据 | `rowValue.paste()` 并请求焦点 |
  | `void clearData()` | 清空数据 | `rowValue.clear()` 并请求焦点 |
  | `void parseToJson()` | JSON 与文本互转 | 依 `getUserData()` 调 `JSONUtil.toJson`/`JSONUtil.toPretty` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 取 `getProp("treeItem")`；`stage.switchOnTab()`、`hideOnEscape()`、`rowValue.requestFocus()` |
  | `String getViewTitle()` | 返回标题 | `I18nResourceBundle.i18nString("shell.redis.title.streamMessageAdd")` |
  | `void destroy()` | 释放资源 | `rowValue.destroy()` |

- 调用链：`addRow → JSONUtil.isJson → JSONUtil.parseObject → client.xadd`
- 调用链：`addRow → XAddParams.id → closeWindow`

## ShellRedisZSetCoordinateAddController

- 职责：为 geo/zset 键新增坐标成员（经纬度+名称），校验成员已存在后写入。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | rowValue | Editor | 坐标名称 |
  | longitude | DecimalTextField | 经度 |
  | latitude | DecimalTextField | 纬度 |
  | treeItem | ShellRedisZSetKeyTreeItem | redis 键树节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void addRow()` | 添加坐标 | 校验 `rowValue`、`longitude`、`latitude`；`client.zrank` 已存在告警；`client.geoadd`；`setProp("result", true)`、`closeWindow()` |
  | `void pasteData()` | 粘贴数据 | `rowValue.paste()` 并请求焦点 |
  | `void clearData()` | 清空数据 | `rowValue.clear()` 并请求焦点 |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 取 `getProp("treeItem")`；`stage.switchOnTab()`、`hideOnEscape()` |
  | `String getViewTitle()` | 返回标题 | `I18nResourceBundle.i18nString("shell.redis.title.zSetCoordinateAdd")` |
  | `void destroy()` | 释放资源 | `rowValue.destroy()` |

- 调用链：`addRow → client.zrank → client.geoadd → closeWindow`
- 调用链：`onWindowShown → getProp("treeItem") → stage.hideOnEscape`

## ShellRedisZSetMemberAddController

- 职责：为 zset 键新增成员（成员+分数），校验成员已存在后写入。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | rowValue | Editor | 行数据（成员） |
  | score | DecimalTextField | 分数 |
  | treeItem | ShellRedisZSetKeyTreeItem | redis 键树节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void addRow()` | 添加成员 | 校验 `rowValue`、`score`；`client.zrank` 已存在告警；`client.zadd`；`setProp("result", true)`、`closeWindow()` |
  | `void pasteData()` | 粘贴数据 | `rowValue.paste()` 并请求焦点 |
  | `void clearData()` | 清空数据 | `rowValue.clear()` 并请求焦点 |
  | `void parseToJson()` | JSON 与文本互转 | 依 `getUserData()` 调 `JSONUtil.toJson`/`JSONUtil.toPretty` |
  | `void onWindowShown(WindowEvent event)` | 窗口显示时初始化 | 取 `getProp("treeItem")`；`stage.switchOnTab()`、`hideOnEscape()` |
  | `String getViewTitle()` | 返回标题 | `I18nResourceBundle.i18nString("shell.redis.title.zSetMemberAdd")` |
  | `void destroy()` | 释放资源 | `rowValue.destroy()` |

- 调用链：`addRow → client.zrank → client.zadd → closeWindow`
- 调用链：`addRow → score.getValue → client.zadd`
