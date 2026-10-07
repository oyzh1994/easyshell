# controller/dameng 与 controller/mysql 控制器代码审查

> 范围：`controller/dameng/**`（11 类）与 `controller/mysql/**`（12 类）。
> 涵盖数据导出/导入/传输/转储/执行 SQL 文件、库/模式新增修改、表/视图/函数/存储过程/事件信息查看等。

---

## ShellDamengDataDumpController

- 职责：达梦库/表数据转储（dump）为 SQL 文件的业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbInfo | ShellConnect | 连接信息 |
  | dbClient | ShellDamengClient | db客户端 |
  | dumpType | int | 转储类型（1 库、2 表） |
  | stopDumpBtn | FXButton | 结束转储按钮（@FXML） |
  | dumpStatus | FXLabel | 转储状态（@FXML） |
  | dumpMsg | MsgTextArea | 转储消息（@FXML） |
  | connect | ReadOnlyTextField | 连接（@FXML） |
  | database | ReadOnlyTextField | 数据库（@FXML） |
  | tableBox | FXVBox | 表组件（@FXML） |
  | table | ReadOnlyTextField | 表（@FXML） |
  | dataType | DBDataDumpTypeComboBox | 数据类型（@FXML） |
  | execTask | Thread | 转储操作任务 |
  | counter | Counter | 计数器 |
  | dumpFile | File | 转储文件 |
  | dumpHandler | ShellDamengDataDumpHandler | 转储处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean checkDumpFile()` | 检查转储文件 | 无文件时按库/表名拼 `_full`/`_structure` 后缀，调 `FXChooser.sqlExtensionFilter` 与 `FileChooserHelper.save` 选保存路径，`FileUtil.touch` 建空文件 |
  | `void doDump() throws IOException` | 执行转储 | 校验文件后重置计数器、清空消息；新建或打断 `ShellDamengDataDumpHandler`，设置 queryLimit(10000)、dumpFile、表名、转储类型、数据类型；`NodeGroupUtil.disable` 后 `ThreadUtil.start` 启动任务，内部调 `dumpHandler.doDump()` 并更新状态 |
  | `void stopDump()` | 结束转储 | `ThreadUtil.interrupt(execTask)` 后置空，调 `dumpHandler.interrupt()` |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 `dbClient`、`dumpType`、`dbName`、`tableName` prop，填充连接/库名；dumpType==2 时显示表组件 |
  | `void onWindowHidden(WindowEvent event)` | 隐藏时终止 | 调 `stopDump` |
  | `void updateStatus(String extraMsg)` | 更新状态 | 设置计数器额外消息并用 `counter.unknownFormat()` 刷新 `dumpStatus` |
  | `String getViewTitle()` | 视图标题 | `I18nResourceBundle.i18nString("base.title.dump")` |
  | `void onStageInitialize(StageAdapter stage)` | 舞台初始化 | `tableBox.managedBindVisible()` 绑定可见性 |
  | `void bindListeners()` | 绑定监听 | `dataType.selectedItemChanged` 时将 `dumpFile` 置空 |

- 调用链：`doDump → checkDumpFile → FileChooserHelper.save → ThreadUtil.start → ShellDamengDataDumpHandler.doDump`
- 调用链：`onWindowHidden → stopDump → ShellDamengDataDumpHandler.interrupt`

## ShellDamengDataExportController

- 职责：达梦数据导出为 sql/txt/csv/json/xls/html/xml 多格式文件的五步向导业务控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步（@FXML） |
  | step2 | FXVBox | 第二步（@FXML） |
  | step3 | FXVBox | 第三步（@FXML） |
  | step4 | FXVBox | 第四步（@FXML） |
  | step5 | FXVBox | 第五步（@FXML） |
  | database | ShellDamengSchemaComboBox | 数据库（@FXML） |
  | tableCombobox | DBNameComboBox | 导出表下拉框（@FXML） |
  | tableColumns | ShellDamengDataExportColumnListView | 导出表字段列表（@FXML） |
  | exportTableView | ShellDamengDataExportTableTableView | 导出表组件（@FXML） |
  | fileType | FXToggleGroup | 文件类型（@FXML） |
  | dbClient | ShellDamengClient | db客户端 |
  | datePreview | FXLabel | 日期预览（@FXML） |
  | dateFormat | DBDataDateTextFiled | 日期格式（@FXML） |
  | recordSeparator | DBDataRecordSeparatorComboBox | 记录分隔符（@FXML） |
  | fieldSeparator | DBDataFieldSeparatorComboBox | 字段分隔符（@FXML） |
  | txtIdentifier | DBDataTxtIdentifierComboBox | 文本识别符（@FXML） |
  | includeFields | FXCheckBox | 包含列标题（@FXML） |
  | fieldToAttr | FXCheckBox | 字段作为属性（@FXML） |
  | earlyVersion | FXCheckBox | 早期版本（@FXML） |
  | continueWithError | FXCheckBox | 遇到错误时继续（@FXML） |
  | stopExportBtn | FXButton | 结束导出按钮（@FXML） |
  | exportStatus | FXLabel | 导出状态（@FXML） |
  | exportMsg | MsgTextArea | 导出消息（@FXML） |
  | execTask | Thread | 导出操作任务 |
  | counter | Counter | 计数器 |
  | exportHandler | ShellDamengDataExportHandler | 导出处理器 |
  | dbName | String | 数据库 |
  | tableName | String | 表 |
  | exportMode | int | 0 正常导出、1 查询导出 |
  | exportTable | ShellDamengDataExportTable | 导出表 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doExport()` | 执行导出 | 新建或打断 `ShellDamengDataExportHandler`，设置文件类型、表、queryLimit(非 Excel 为 10000)、日期格式、字段/记录分隔符、文本识别符、字段属性等；`ThreadUtil.start` 后调 `exportHandler.doExport()` |
  | `void stopExport()` | 结束导出 | `ThreadUtil.interrupt(execTask)`，调 `exportHandler.interrupt()` |
  | `void bindListeners()` | 绑定监听 | 表下拉选择变化时 `tableColumns.init`；日期格式变化刷新预览；数据库变化设 `dbName` 并 `StageManager.showMask(initTables)` |
  | `void flushDatePreview()` | 刷新日期预览 | 按 `dateFormat` 用 `DateUtil.format` 生成预览，异常显示"格式无效" |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 dbClient/dbName/tableName/exportMode/exportTable prop；有库名则锁定数据库下拉，否则 `database.init(dbClient)` |
  | `void onWindowHidden(WindowEvent event)` | 隐藏时终止 | 调 `stopExport` |
  | `void updateStatus(String extraMsg)` | 更新状态 | 刷新 `exportStatus` 文本 |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.exportTitle()` |
  | `void showStep1()` | 显示第一步 | step1.display，step2.disappear |
  | `void initTables()` | 初始化表列表 | 正常导出调 `dbClient.selectTables` 生成导出表；查询导出直接加入 `exportTable`；按文件类型设扩展名 |
  | `void showStep2()` | 显示第二步 | 校验文件类型后 `StageManager.showMask(initTables)` |
  | `void showStep3()` | 显示第三步 | 校验已选表，按需 `dbClient.selectColumns` 加载字段并填 `tableCombobox` |
  | `void showStep4()` | 显示第四步 | 按文件类型（sql/txt/json/xls/xlsx/csv/html/xml）用 `NodeGroupUtil` 显示/隐藏对应配置项 |
  | `void showStep5()` | 显示第五步 | step4.disappear，step5.display |
  | `void selectAllTable()` | 全选表 | 遍历 `exportTableView.getItems()` 置选中 |
  | `void unselectAllTable()` | 取消全选表 | 遍历置 `setSelected(false)` |
  | `void selectAllFiled()` | 全选字段 | 遍历 `tableColumns.getItems()` 置选中 |
  | `void unselectAllField()` | 取消全选字段 | 遍历置 `setSelected(false)` |

- 调用链：`showStep3 → ShellDamengClient.selectColumns → tableCombobox.addItem`
- 调用链：`doExport → ThreadUtil.start → ShellDamengDataExportHandler.doExport`
- 调用链：`bindListeners → database.selectedItemChanged → StageManager.showMask → initTables`

## ShellDamengDataImportController

- 职责：达梦数据从 json/txt/csv/xml/excel 文件导入数据库的五步向导业务控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步（@FXML） |
  | step2 | FXVBox | 第二步（@FXML） |
  | step3 | FXVBox | 第三步（@FXML） |
  | step4 | FXVBox | 第四步（@FXML） |
  | step5 | FXVBox | 第五步（@FXML） |
  | importFileTableView | ShellDamengDataImportFileTableView | 导入表组件（@FXML） |
  | fileType | FXToggleGroup | 文件类型（@FXML） |
  | dbClient | ShellDamengClient | db客户端 |
  | stopImportBtn | FXButton | 结束导入按钮（@FXML） |
  | importStatus | FXLabel | 导入状态（@FXML） |
  | importMsg | MsgTextArea | 导入消息（@FXML） |
  | recordLabel | DBDataRecordLabelComboBox | 行标签（@FXML） |
  | attrToColumn | FXCheckBox | 标签属性作为表字段（@FXML） |
  | columnIndex | NumberTextField | 字段索引（@FXML） |
  | dataStartIndex | NumberTextField | 数据起始索引（@FXML） |
  | datePreview | FXLabel | 日期预览（@FXML） |
  | dateFormat | DBDataDateTextFiled | 日期格式（@FXML） |
  | recordSeparator | DBDataRecordSeparatorComboBox | 记录分隔符（@FXML） |
  | fieldSeparator | DBDataFieldSeparatorComboBox | 字段分隔符（@FXML） |
  | txtIdentifier | DBDataTxtIdentifierComboBox | 文本识别符（@FXML） |
  | importMode | FXToggleGroup | 导入模式（@FXML） |
  | execTask | Thread | 导入操作任务 |
  | counter | Counter | 计数器 |
  | importHandler | ShellDamengDataImportHandler | 导入处理器 |
  | database | ShellDamengSchemaComboBox | 数据库（@FXML） |
  | dbName | String | 数据库 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doImport()` | 执行导入 | 新建或打断 `ShellDamengDataImportHandler`，设置文件类型、文件、分隔符、日期格式、导入模式、字段/数据索引、行标签；`ThreadUtil.start` 调 `importHandler.doImport()` |
  | `void stopImport()` | 结束导入 | `ThreadUtil.interrupt(execTask)`，调 `importHandler.interrupt()` |
  | `void flushDatePreview()` | 刷新日期预览 | 同导出：`DateUtil.format` 生成预览 |
  | `void bindListeners()` | 绑定监听 | 日期格式刷新预览；数据库变化清空并重设 `importFileTableView`；文件列表 `ListChangeListener` 触发 `initFileTable` |
  | `void initFileTable()` | 初始化文件表格 | 遍历文件项 `setSchema(dbName)`、`setDbClient(dbClient)` |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 dbName/dbClient prop；有库名则锁定下拉，否则 `database.init(dbClient)` |
  | `void onWindowHidden(WindowEvent event)` | 隐藏时终止 | 调 `stopImport` |
  | `void updateStatus(String extraMsg)` | 更新状态 | 刷新 `importStatus` 文本 |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.importTitle()` |
  | `void showStep1()` | 显示第一步 | 清空文件表，step1.display |
  | `void showStep2()` | 显示第二步 | 校验文件类型 |
  | `void showStep3()` | 显示第三步 | 校验已选文件，按类型显隐记录标签/分隔符/索引等控件 |
  | `void showStep4()` | 显示第四步 | step4.display 并清空导入消息 |
  | `void showStep5()` | 显示第五步 | step5.display |
  | `void addFile()` | 添加文件 | `FXChooser.extensionFilter`、`FileChooserHelper.choose` 选文件后加入表格 |
  | `void deleteFile()` | 删除文件 | `importFileTableView.removeSelectedItem()` |

- 调用链：`doImport → ThreadUtil.start → ShellDamengDataImportHandler.doImport`
- 调用链：`bindListeners → importFileTableView.itemList().addListener → initFileTable`
- 调用链：`addFile → FileChooserHelper.choose → importFileTableView.addItem`

## ShellDamengDataRunSqlFileController

- 职责：达梦执行 SQL 文件（脚本）的业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbInfo | ShellConnect | 连接信息 |
  | dbClient | ShellDamengClient | db客户端 |
  | stopSqlFileBtn | FXButton | 结束运行sql按钮（@FXML） |
  | execStatus | FXLabel | 执行状态（@FXML） |
  | execMsg | MsgTextArea | 执行消息（@FXML） |
  | connect | ReadOnlyTextField | 连接（@FXML） |
  | database | ShellDamengSchemaComboBox | 数据库（@FXML） |
  | continueWithErrors | FXCheckBox | 遇到错误时继续（@FXML） |
  | file | ChooseFileTextField | 文件（@FXML） |
  | execTask | Thread | sql操作任务 |
  | counter | Counter | 计数器 |
  | sqlFileHandler | DBDataRunFileHandler<String> | sql处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean checkSqlFile()` | 检查sql文件 | 文件为空时提示"请选择文件" |
  | `void runSqlFile()` | 执行sql | 校验文件与数据库；新建或打断 `ShellDamengDataRunSqlFileHandler`；设置 file、continueWithErrors；`ThreadUtil.start` 调 `sqlFileHandler.runFile()` |
  | `void stopSqlFile()` | 结束sql | `ThreadUtil.interrupt(execTask)`，调 `sqlFileHandler.interrupt()` |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 dbClient prop，`database.init(dbClient, dbName)`，填连接名 |
  | `void onWindowHidden(WindowEvent event)` | 隐藏时终止 | 调 `stopSqlFile` |
  | `void updateStatus(String extraMsg)` | 更新状态 | 刷新 `execStatus` 文本 |
  | `String getViewTitle()` | 视图标题 | `I18nResourceBundle.i18nString("base.runSqlFile")` |
  | `void onStageInitialize(StageAdapter stage)` | 舞台初始化 | `file.setFilter(FXChooser.sqlExtensionFilter())` |

- 调用链：`runSqlFile → checkSqlFile → ThreadUtil.start → ShellDamengDataRunSqlFileHandler.runFile`
- 调用链：`onWindowHidden → stopSqlFile → DBDataRunFileHandler.interrupt`

## ShellDamengDataTransportController

- 职责：达梦源库到目标库（可跨连接）表/视图/函数/过程/触发器的数据传输业务控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | database | String | 模式 |
  | step1 | FXVBox | 第一步（@FXML） |
  | step2 | FXVBox | 第二步（@FXML） |
  | step3 | FXVBox | 第三步（@FXML） |
  | sourceInfoName | FXLabel | 来源信息名称（@FXML） |
  | sourceDatabaseName | FXLabel | 来源库名称（@FXML） |
  | targetInfoName | FXLabel | 目标信息名称（@FXML） |
  | targetDatabaseName | FXLabel | 目标库名称（@FXML） |
  | sourceInfo | ShellConnectTextField | 来源信息（@FXML） |
  | targetInfo | ShellConnectTextField | 目标信息（@FXML） |
  | sourceDatabase | ShellDamengSchemaComboBox | 来源库组件（@FXML） |
  | targetDatabase | ShellDamengSchemaComboBox | 目标库组件（@FXML） |
  | sourceHost | FXLabel | 来源主机（@FXML） |
  | targetHost | FXLabel | 目标主机（@FXML） |
  | sourceVersion | FXLabel | 来源服务版本（@FXML） |
  | targetVersion | FXLabel | 目标服务版本（@FXML） |
  | sourceType | FXLabel | 来源服务类型（@FXML） |
  | targetType | FXLabel | 目标服务类型（@FXML） |
  | sourceClient | ShellDamengClient | 来源客户端 |
  | targetClient | ShellDamengClient | 目标客户端 |
  | stopTransportBtn | FXButton | 结束传输按钮（@FXML） |
  | transportStatus | FXLabel | 传输状态（@FXML） |
  | transportMsg | MsgTextArea | 传输消息（@FXML） |
  | tablePane | FXTitledPane | 表组件（@FXML） |
  | viewPane | FXTitledPane | 视图组件（@FXML） |
  | functionPane | FXTitledPane | 函数组件（@FXML） |
  | procedurePane | FXTitledPane | 过程组件（@FXML） |
  | triggerPane | FXTitledPane | 触发器组件（@FXML） |
  | tableList | DBDataTransportNameListView | 表列表（@FXML） |
  | viewList | DBDataTransportNameListView | 视图列表（@FXML） |
  | functionList | DBDataTransportNameListView | 函数列表（@FXML） |
  | procedureList | DBDataTransportNameListView | 过程列表（@FXML） |
  | triggerList | DBDataTransportNameListView | 触发器列表（@FXML） |
  | execTask | Thread | 传输操作任务 |
  | counter | Counter | 计数器 |
  | transportHandler | ShellDamengDataTransportHandler | 传输处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doTransport()` | 执行传输 | 新建或打断 `ShellDamengDataTransportHandler`，设置源/目标客户端、库及视图/表/触发器/函数/过程选择；`ThreadUtil.start` 调 `transportHandler.doTransport()` |
  | `void stopTransport()` | 结束传输 | `ThreadUtil.interrupt(execTask)`，调 `transportHandler.interrupt()` |
  | `void bindListeners()` | 绑定监听 | 源/目标信息变化 `StageManager.showMask(doConnect)`；库变化刷新名称并清列表；各列表选中变化刷新面板文字 |
  | `void doConnect(int type, ShellConnect connect)` | 执行连接 | type=1 处理来源、2 处理目标；`ShellClientUtil.newClient` 建客户端、`start()`、`selectVersion()`，并 `sourceDatabase.init` 加载库列表 |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 connect/dbName prop，`sourceInfo.selectItem(connect)` |
  | `void onWindowHidden(WindowEvent event)` | 隐藏时终止 | 调 `stopTransport` |
  | `void updateStatus(String extraMsg)` | 更新状态 | 刷新 `transportStatus` 文本 |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.transportTitle()` |
  | `void showStep1()` | 显示第一步 | step1.display |
  | `void showStep2()` | 显示第二步 | 校验源/目标连接与库、防同库；调 `sourceClient.selectViews/selectTables/selectTriggers/selectFunctions/selectProcedures` 填充各列表 |
  | `void showStep3()` | 显示第三步 | step3.display |
  | `void clearList()` | 清空数据列表 | 清空 view/table/function/procedure 列表 |
  | `void flushPaneText(String name)` | 刷新面板文字 | 按面板名刷新 `(选中/总数)` 文本 |

- 调用链：`bindListeners → sourceInfo.selectedItemChanged → StageManager.showMask → doConnect → ShellClientUtil.newClient`
- 调用链：`doTransport → ThreadUtil.start → ShellDamengDataTransportHandler.doTransport`
- 调用链：`showStep2 → ShellDamengClient.selectTables → tableList.of`

## ShellDamengFunctionInfoController

- 职责：达梦函数信息查看业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 名称（@FXML） |
  | comment | ReadOnlyTextArea | 注释（@FXML） |
  | definition | Editor | 定义（@FXML） |
  | createDefinition | Editor | ddl（@FXML） |
  | treeItem | ShellDamengFunctionTreeItem | 函数节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initInfo()` | 初始化信息 | 经 `treeItem.dbItem()` 取 schema，`selectFunction` 获取 `DamengFunction` 并填充名称、定义、ddl |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "item" prop 设 `treeItem`，`StageManager.showMask(initInfo)` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.functionInfo()` |
  | `void destroy()` | 销毁 | `definition.destroy()`、`createDefinition.destroy()` |

- 调用链：`onWindowShown → StageManager.showMask → initInfo → ShellDamengSchemaTreeItem.selectFunction`

## ShellDamengProcedureInfoController

- 职责：达梦存储过程信息查看业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 名称（@FXML） |
  | comment | ReadOnlyTextArea | 注释（@FXML） |
  | definition | Editor | 定义（@FXML） |
  | createDefinition | Editor | ddl（@FXML） |
  | treeItem | ShellDamengProcedureTreeItem | 过程节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initInfo()` | 初始化信息 | 经 `treeItem.dbItem()` 取 schema，`selectProcedure` 获取 `DamengProcedure` 并填充名称、定义、ddl |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "item" prop，`StageManager.showMask(initInfo)` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.procedureInfo()` |
  | `void destroy()` | 销毁 | 销毁两个 Editor |

- 调用链：`onWindowShown → StageManager.showMask → initInfo → ShellDamengSchemaTreeItem.selectProcedure`

## ShellDamengSchemaAddController

- 职责：达梦新增模式（schema）业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ClearableTextField | 名称（@FXML） |
  | connectItem | ShellDamengRootTreeItem | db连接节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void add()` | 添加db库 | `name.validate()` 校验；`connectItem.existSchema(dbName)` 查重；构造 `DamengSchema` 后 `connectItem.createSchema`，设 "databaseName" prop，触发 `ShellDamengEventUtil.schemaAdded` 并关窗 |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.addSchema()` |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "connectItem" prop，`stage.switchOnTab`、`hideOnEscape` |

- 调用链：`add → existSchema → createSchema → ShellDamengEventUtil.schemaAdded`

## ShellDamengSchemaUpdateController

- 职责：达梦编辑模式（schema）业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 名称（@FXML） |
  | schema | DamengSchema | db模式 |
  | connectItem | ShellDamengRootTreeItem | db连接节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void save()` | 编辑db库 | 构造 `DamengSchema` 设名，调 `connectItem.alterSchema(schema)`，成功触发 `ShellDamengEventUtil.schemaUpdated` 并关窗，否则提示失败 |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "database"/"connectItem" prop，回填名称，`switchOnTab`、`hideOnEscape` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.updateSchema()` |

- 调用链：`save → ShellDamengRootTreeItem.alterSchema → ShellDamengEventUtil.schemaUpdated`

## ShellDamengTableInfoController

- 职责：达梦表信息查看业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 名称（@FXML） |
  | tableSpace | ReadOnlyTextField | 表空间（@FXML） |
  | comment | ReadOnlyTextArea | 注释（@FXML） |
  | createDefinition | Editor | 定义（@FXML） |
  | treeItem | ShellDamengTableTreeItem | 表节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initInfo()` | 初始化信息 | 经 `treeItem.dbItem()` 取 schema，`selectTable` 获取 `DamengTable` 并填充名称、注释、表空间、ddl |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "item" prop，`StageManager.showMask(initInfo)` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.tableInfo()` |

- 调用链：`onWindowShown → StageManager.showMask → initInfo → ShellDamengSchemaTreeItem.selectTable`

## ShellDamengViewInfoController

- 职责：达梦视图信息查看业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 名称（@FXML） |
  | comment | ReadOnlyTextArea | 注释（@FXML） |
  | definition | Editor | 定义（@FXML） |
  | createDefinition | Editor | ddl（@FXML） |
  | treeItem | ShellDamengViewTreeItem | 视图节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initInfo()` | 初始化信息 | 经 `treeItem.dbItem()` 取 schema，`selectView` 获取 `DamengView` 并填充名称、注释、定义、ddl |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "item" prop，`StageManager.showMask(initInfo)` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.viewInfo()` |

- 备注：类内 `destroy()` 方法整段被注释（死代码，已跳过）。
- 调用链：`onWindowShown → StageManager.showMask → initInfo → ShellDamengSchemaTreeItem.selectView`

## ShellMysqlDataDumpController

- 职责：MySQL 库/表数据转储（dump）为 SQL 文件的业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbInfo | ShellConnect | 连接信息 |
  | dbClient | ShellMysqlClient | db客户端 |
  | dumpType | int | 转储类型（1 库、2 表） |
  | stopDumpBtn | FXButton | 结束转储按钮（@FXML） |
  | dumpStatus | FXLabel | 转储状态（@FXML） |
  | dumpMsg | MsgTextArea | 转储消息（@FXML） |
  | connect | ReadOnlyTextField | 连接（@FXML） |
  | database | ReadOnlyTextField | 数据库（@FXML） |
  | tableBox | FXVBox | 表组件（@FXML） |
  | table | ReadOnlyTextField | 表（@FXML） |
  | dataType | DBDataDumpTypeComboBox | 数据类型（@FXML） |
  | execTask | Thread | 转储操作任务 |
  | counter | Counter | 计数器 |
  | dumpFile | File | 转储文件 |
  | dumpHandler | DBDataDumpHandler | 转储处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean checkDumpFile()` | 检查转储文件 | 按库/表名拼 `_full`/`_structure` 后缀，`FileChooserHelper.save` 选路径并建文件 |
  | `void doDump() throws IOException` | 执行转储 | 新建或打断 `ShellMysqlDataDumpHandler`，设 queryLimit(10000)、文件、表名、类型；`ThreadUtil.start` 调 `dumpHandler.doDump()` |
  | `void stopDump()` | 结束转储 | `ThreadUtil.interrupt(execTask)`，调 `dumpHandler.interrupt()` |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 dbClient/dumpType/dbName/tableName，dumpType==2 显示表组件 |
  | `void onWindowHidden(WindowEvent event)` | 隐藏时终止 | 调 `stopDump` |
  | `void updateStatus(String extraMsg)` | 更新状态 | 刷新 `dumpStatus` 文本 |
  | `String getViewTitle()` | 视图标题 | `I18nResourceBundle.i18nString("base.title.dump")` |
  | `void onStageInitialize(StageAdapter stage)` | 舞台初始化 | `tableBox.managedBindVisible()` |
  | `void bindListeners()` | 绑定监听 | `dataType` 变化清空 `dumpFile` |

- 调用链：`doDump → checkDumpFile → ThreadUtil.start → ShellMysqlDataDumpHandler.doDump`
- 调用链：`onWindowHidden → stopDump → DBDataDumpHandler.interrupt`

## ShellMysqlDataExportController

- 职责：MySQL 数据导出为多格式文件的五步向导业务控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步（@FXML） |
  | step2 | FXVBox | 第二步（@FXML） |
  | step3 | FXVBox | 第三步（@FXML） |
  | step4 | FXVBox | 第四步（@FXML） |
  | step5 | FXVBox | 第五步（@FXML） |
  | database | ShellMysqlDatabaseComboBox | 数据库（@FXML） |
  | tableCombobox | DBNameComboBox | 导出表下拉框（@FXML） |
  | tableColumns | ShellMysqlDataExportColumnListView | 导出表字段列表（@FXML） |
  | exportTableView | ShellMysqlDataExportTableTableView | 导出表组件（@FXML） |
  | fileType | FXToggleGroup | 文件类型（@FXML） |
  | dbClient | ShellMysqlClient | db客户端 |
  | datePreview | FXLabel | 日期预览（@FXML） |
  | dateFormat | DBDataDateTextFiled | 日期格式（@FXML） |
  | recordSeparator | DBDataRecordSeparatorComboBox | 记录分隔符（@FXML） |
  | fieldSeparator | DBDataFieldSeparatorComboBox | 字段分隔符（@FXML） |
  | txtIdentifier | DBDataTxtIdentifierComboBox | 文本识别符（@FXML） |
  | includeFields | FXCheckBox | 包含列标题（@FXML） |
  | fieldToAttr | FXCheckBox | 字段作为属性（@FXML） |
  | earlyVersion | FXCheckBox | 早期版本（@FXML） |
  | continueWithError | FXCheckBox | 遇到错误时继续（@FXML） |
  | stopExportBtn | FXButton | 结束导出按钮（@FXML） |
  | exportStatus | FXLabel | 导出状态（@FXML） |
  | exportMsg | MsgTextArea | 导出消息（@FXML） |
  | execTask | Thread | 导出操作任务 |
  | counter | Counter | 计数器 |
  | exportHandler | ShellMysqlDataExportHandler | 导出处理器 |
  | dbName | String | 数据库 |
  | tableName | String | 表 |
  | exportMode | int | 0 正常导出、1 查询导出 |
  | exportTable | ShellMysqlDataExportTable | 导出表 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doExport()` | 执行导出 | 新建或打断 `ShellMysqlDataExportHandler`，设置文件类型、表、queryLimit、日期格式、分隔符、识别符等；`ThreadUtil.start` 调 `exportHandler.doExport()` |
  | `void stopExport()` | 结束导出 | `ThreadUtil.interrupt(execTask)`，调 `exportHandler.interrupt()` |
  | `void bindListeners()` | 绑定监听 | 表下拉变化 `tableColumns.init`；日期格式刷新预览；数据库变化设 `dbName` 并 `StageManager.showMask(initTables)` |
  | `void flushDatePreview()` | 刷新日期预览 | `DateUtil.format` 生成预览 |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 prop；有库名锁定下拉，否则 `database.init(dbClient)` |
  | `void onWindowHidden(WindowEvent event)` | 隐藏时终止 | 调 `stopExport` |
  | `void updateStatus(String extraMsg)` | 更新状态 | 刷新 `exportStatus` 文本 |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.exportTitle()` |
  | `void showStep1()` | 显示第一步 | step1.display |
  | `void initTables()` | 初始化表列表 | `dbClient.selectTables` 生成导出表；查询导出用 `exportTable` |
  | `void showStep2()` | 显示第二步 | 校验文件类型后 `StageManager.showMask(initTables)` |
  | `void showStep3()` | 显示第三步 | 校验已选表，`dbClient.selectColumns` 加载字段填 `tableCombobox` |
  | `void showStep4()` | 显示第四步 | 按文件类型显隐对应配置项 |
  | `void showStep5()` | 显示第五步 | step5.display |
  | `void selectAllTable()` | 全选表 | 遍历选中 |
  | `void unselectAllTable()` | 取消全选表 | 遍历取消 |
  | `void selectAllFiled()` | 全选字段 | 遍历 `tableColumns` 选中 |
  | `void unselectAllField()` | 取消全选字段 | 遍历取消 |

- 调用链：`showStep3 → ShellMysqlClient.selectColumns → tableCombobox.addItem`
- 调用链：`doExport → ThreadUtil.start → ShellMysqlDataExportHandler.doExport`
- 调用链：`bindListeners → database.selectedItemChanged → StageManager.showMask → initTables`

## ShellMysqlDataImportController

- 职责：MySQL 数据从多格式文件导入数据库的五步向导业务控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | step1 | FXVBox | 第一步（@FXML） |
  | step2 | FXVBox | 第二步（@FXML） |
  | step3 | FXVBox | 第三步（@FXML） |
  | step4 | FXVBox | 第四步（@FXML） |
  | step5 | FXVBox | 第五步（@FXML） |
  | importFileTableView | ShellMysqlDataImportFileTableView | 导入表组件（@FXML） |
  | fileType | FXToggleGroup | 文件类型（@FXML） |
  | dbClient | ShellMysqlClient | db客户端 |
  | stopImportBtn | FXButton | 结束导入按钮（@FXML） |
  | importStatus | FXLabel | 导入状态（@FXML） |
  | importMsg | MsgTextArea | 导入消息（@FXML） |
  | recordLabel | DBDataRecordLabelComboBox | 行标签（@FXML） |
  | attrToColumn | FXCheckBox | 标签属性作为表字段（@FXML） |
  | columnIndex | NumberTextField | 字段索引（@FXML） |
  | dataStartIndex | NumberTextField | 数据起始索引（@FXML） |
  | datePreview | FXLabel | 日期预览（@FXML） |
  | dateFormat | DBDataDateTextFiled | 日期格式（@FXML） |
  | recordSeparator | DBDataRecordSeparatorComboBox | 记录分隔符（@FXML） |
  | fieldSeparator | DBDataFieldSeparatorComboBox | 字段分隔符（@FXML） |
  | txtIdentifier | DBDataTxtIdentifierComboBox | 文本识别符（@FXML） |
  | importMode | FXToggleGroup | 导入模式（@FXML） |
  | execTask | Thread | 导入操作任务 |
  | counter | Counter | 计数器 |
  | importHandler | ShellMysqlDataImportHandler | 导入处理器 |
  | database | ShellMysqlDatabaseComboBox | 数据库（@FXML） |
  | dbName | String | 数据库 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doImport()` | 执行导入 | 新建或打断 `ShellMysqlDataImportHandler`，设置文件类型、文件、分隔符、日期格式、导入模式、索引、行标签；`ThreadUtil.start` 调 `importHandler.doImport()` |
  | `void stopImport()` | 结束导入 | `ThreadUtil.interrupt(execTask)`，调 `importHandler.interrupt()` |
  | `void flushDatePreview()` | 刷新日期预览 | `DateUtil.format` 生成预览 |
  | `void bindListeners()` | 绑定监听 | 日期格式刷新预览；数据库变化重置文件表；`itemList` 变化触发 `initFileTable` |
  | `void initFileTable()` | 初始化文件表格 | 遍历项 `setDbName(dbName)`、`setDbClient(dbClient)` |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 dbName/dbClient prop；有库名锁定下拉，否则 `database.init(dbClient)` |
  | `void onWindowHidden(WindowEvent event)` | 隐藏时终止 | 调 `stopImport` |
  | `void updateStatus(String extraMsg)` | 更新状态 | 刷新 `importStatus` 文本 |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.importTitle()` |
  | `void showStep1()` | 显示第一步 | 清空文件表，step1.display |
  | `void showStep2()` | 显示第二步 | 校验文件类型 |
  | `void showStep3()` | 显示第三步 | 校验文件后按类型显隐控件 |
  | `void showStep4()` | 显示第四步 | step4.display 并清空消息 |
  | `void showStep5()` | 显示第五步 | step5.display |
  | `void addFile()` | 添加文件 | 选择文件后加入表格 |
  | `void deleteFile()` | 删除文件 | `importFileTableView.removeSelectedItem()` |

- 调用链：`doImport → ThreadUtil.start → ShellMysqlDataImportHandler.doImport`
- 调用链：`bindListeners → importFileTableView.itemList().addListener → initFileTable`
- 调用链：`addFile → FileChooserHelper.choose → importFileTableView.addItem`

## ShellMysqlDataRunSqlFileController

- 职责：MySQL 执行 SQL 文件（脚本）的业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbInfo | ShellConnect | 连接信息 |
  | dbClient | ShellMysqlClient | db客户端 |
  | stopSqlFileBtn | FXButton | 结束运行sql按钮（@FXML） |
  | execStatus | FXLabel | 执行状态（@FXML） |
  | execMsg | MsgTextArea | 执行消息（@FXML） |
  | connect | ReadOnlyTextField | 连接（@FXML） |
  | database | ShellMysqlDatabaseComboBox | 数据库（@FXML） |
  | continueWithErrors | FXCheckBox | 遇到错误时继续（@FXML） |
  | file | ChooseFileTextField | 文件（@FXML） |
  | execTask | Thread | sql操作任务 |
  | counter | Counter | 计数器 |
  | sqlFileHandler | DBDataRunFileHandler | sql处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean checkSqlFile()` | 检查sql文件 | 文件为空时提示 |
  | `void runSqlFile()` | 执行sql | 校验文件与数据库；新建或打断 `ShellMysqlDataRunSqlFileHandler`；设 file、continueWithErrors；`ThreadUtil.start` 调 `sqlFileHandler.runFile()` |
  | `void stopSqlFile()` | 结束sql | `ThreadUtil.interrupt(execTask)`，调 `sqlFileHandler.interrupt()` |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 dbClient prop，`database.init(dbClient, dbName)` |
  | `void onWindowHidden(WindowEvent event)` | 隐藏时终止 | 调 `stopSqlFile` |
  | `void updateStatus(String extraMsg)` | 更新状态 | 刷新 `execStatus` 文本 |
  | `String getViewTitle()` | 视图标题 | `I18nResourceBundle.i18nString("base.runSqlFile")` |
  | `void onStageInitialize(StageAdapter stage)` | 舞台初始化 | `file.setFilter(FXChooser.sqlExtensionFilter())` |

- 调用链：`runSqlFile → checkSqlFile → ThreadUtil.start → ShellMysqlDataRunSqlFileHandler.runFile`
- 调用链：`onWindowHidden → stopSqlFile → DBDataRunFileHandler.interrupt`

## ShellMysqlDataTransportController

- 职责：MySQL 源库到目标库（可跨连接）表/视图/事件/函数/过程/触发器的数据传输业务控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | database | String | 数据库 |
  | step1 | FXVBox | 第一步（@FXML） |
  | step2 | FXVBox | 第二步（@FXML） |
  | step3 | FXVBox | 第三步（@FXML） |
  | sourceInfoName | FXLabel | 来源信息名称（@FXML） |
  | sourceDatabaseName | FXLabel | 来源库名称（@FXML） |
  | targetInfoName | FXLabel | 目标信息名称（@FXML） |
  | targetDatabaseName | FXLabel | 目标库名称（@FXML） |
  | sourceInfo | ShellConnectTextField | 来源信息（@FXML） |
  | targetInfo | ShellConnectTextField | 目标信息（@FXML） |
  | sourceDatabase | ShellMysqlDatabaseComboBox | 来源库组件（@FXML） |
  | targetDatabase | ShellMysqlDatabaseComboBox | 目标库组件（@FXML） |
  | sourceHost | FXLabel | 来源主机（@FXML） |
  | targetHost | FXLabel | 目标主机（@FXML） |
  | sourceVersion | FXLabel | 来源服务版本（@FXML） |
  | targetVersion | FXLabel | 目标服务版本（@FXML） |
  | sourceType | FXLabel | 来源服务类型（@FXML） |
  | targetType | FXLabel | 目标服务类型（@FXML） |
  | sourceClient | ShellMysqlClient | 来源客户端 |
  | targetClient | ShellMysqlClient | 目标客户端 |
  | stopTransportBtn | FXButton | 结束传输按钮（@FXML） |
  | transportStatus | FXLabel | 传输状态（@FXML） |
  | transportMsg | MsgTextArea | 传输消息（@FXML） |
  | tablePane | FXTitledPane | 表组件（@FXML） |
  | viewPane | FXTitledPane | 视图组件（@FXML） |
  | functionPane | FXTitledPane | 函数组件（@FXML） |
  | procedurePane | FXTitledPane | 过程组件（@FXML） |
  | triggerPane | FXTitledPane | 触发器组件（@FXML） |
  | eventPane | FXTitledPane | 事件组件（@FXML） |
  | tableList | DBDataTransportNameListView | 表列表（@FXML） |
  | eventList | DBDataTransportNameListView | 事件列表（@FXML） |
  | viewList | DBDataTransportNameListView | 视图列表（@FXML） |
  | functionList | DBDataTransportNameListView | 函数列表（@FXML） |
  | procedureList | DBDataTransportNameListView | 过程列表（@FXML） |
  | triggerList | DBDataTransportNameListView | 触发器列表（@FXML） |
  | execTask | Thread | 传输操作任务 |
  | counter | Counter | 计数器 |
  | transportHandler | ShellMysqlDataTransportHandler | 传输处理器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void doTransport()` | 执行传输 | 新建或打断 `ShellMysqlDataTransportHandler`，设置源/目标客户端、库及视图/事件/表/触发器/函数/过程选择；`ThreadUtil.start` 调 `transportHandler.doTransport()` |
  | `void stopTransport()` | 结束传输 | `ThreadUtil.interrupt(execTask)`，调 `transportHandler.interrupt()` |
  | `void bindListeners()` | 绑定监听 | 源/目标信息变化 `StageManager.showMask(doConnect)`；库变化刷新名称并清列表；各列表选中变化刷新面板文字 |
  | `void doConnect(int type, ShellConnect connect)` | 执行连接 | type=1 来源、2 目标；`ShellClientUtil.newClient`、`start()`、`selectVersion()`，并 `sourceDatabase.init` 加载库 |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 connect/dbName prop，`sourceInfo.selectItem(connect)` |
  | `void onWindowHidden(WindowEvent event)` | 隐藏时终止 | 调 `stopTransport` |
  | `void updateStatus(String extraMsg)` | 更新状态 | 刷新 `transportStatus` 文本 |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.transportTitle()` |
  | `void showStep1()` | 显示第一步 | step1.display |
  | `void showStep2()` | 显示第二步 | 校验源/目标连接与库、防同库；调 `sourceClient.selectViews/selectEvents/selectTables/selectTriggers/selectFunctions/selectProcedures` 填充各列表 |
  | `void showStep3()` | 显示第三步 | step3.display |
  | `void clearList()` | 清空数据列表 | 清空 view/event/table/function/procedure 列表 |
  | `void flushPaneText(String name)` | 刷新面板文字 | 按面板名刷新 `(选中/总数)` 文本 |

- 调用链：`bindListeners → sourceInfo.selectedItemChanged → StageManager.showMask → doConnect → ShellClientUtil.newClient`
- 调用链：`doTransport → ThreadUtil.start → ShellMysqlDataTransportHandler.doTransport`
- 调用链：`showStep2 → ShellMysqlClient.selectEvents → eventList.of`

## ShellMysqlDatabaseAddController

- 职责：MySQL 新增数据库业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ClearableTextField | 名称（@FXML） |
  | charset | ShellMysqlCharsetComboBox | 字符集（@FXML） |
  | collation | ShellMysqlCollationComboBox | 排序方式（@FXML） |
  | connectItem | ShellMysqlRootTreeItem | db连接节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void add()` | 添加db库 | `name.validate()` 校验；`connectItem.existDatabase` 查重；构造 `MysqlDatabase` 设置字符集/排序，调 `connectItem.createDatabase`，设 "databaseName" prop，触发 `ShellMysqlEventUtil.databaseAdded` 并关窗 |
  | `void bindListeners()` | 绑定监听 | 字符集变化时 `collation.init(newValue, client)`、`select(0)`、`enable`，无值则清空并禁用 |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.addDatabase()` |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "connectItem" prop，`charset.init(client)`、启用字符集、禁用排序，`switchOnTab`、`hideOnEscape` |

- 调用链：`add → existDatabase → createDatabase → ShellMysqlEventUtil.databaseAdded`
- 调用链：`bindListeners → charset.selectedItemChanged → collation.init`

## ShellMysqlDatabaseUpdateController

- 职责：MySQL 编辑数据库（字符集/排序规则）业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 名称（@FXML） |
  | charset | ShellMysqlCharsetComboBox | 字符集（@FXML） |
  | collation | ShellMysqlCollationComboBox | 排序方式（@FXML） |
  | database | MysqlDatabase | db库对象 |
  | connectItem | ShellMysqlRootTreeItem | db连接节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void save()` | 编辑db库 | 构造 `MysqlDatabase`，与 `database` 原字符集/排序比较后按需设置，调 `connectItem.alterDatabase`，成功触发 `ShellMysqlEventUtil.databaseUpdated` 更新原对象并关窗，否则提示失败 |
  | `void bindListeners()` | 绑定监听 | 字符集变化时 `collation.init(newValue, client)`、`select(0)` |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "database"/"connectItem" prop，回填名称，`charset`/`collation` 初始化并选中当前值，`switchOnTab`、`hideOnEscape` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.updateDatabase()` |

- 调用链：`save → ShellMysqlRootTreeItem.alterDatabase → ShellMysqlEventUtil.databaseUpdated`
- 调用链：`bindListeners → charset.selectedItemChanged → collation.init`

## ShellMysqlEventInfoController

- 职责：MySQL 事件信息查看业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 名称（@FXML） |
  | comment | ReadOnlyTextArea | 注释（@FXML） |
  | definition | Editor | 定义（@FXML） |
  | createDefinition | Editor | ddl（@FXML） |
  | treeItem | ShellMysqlEventTreeItem | 事件节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initInfo()` | 初始化信息 | 经 `treeItem.dbItem()` 取 database，`selectEvent` 获取 `MysqlEvent` 并填充名称、注释、定义、ddl |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "item" prop，`StageManager.showMask(initInfo)` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.eventInfo()` |
  | `void destroy()` | 销毁 | 销毁两个 Editor |

- 调用链：`onWindowShown → StageManager.showMask → initInfo → ShellMysqlDatabaseTreeItem.selectEvent`

## ShellMysqlFunctionInfoController

- 职责：MySQL 函数信息查看业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 名称（@FXML） |
  | comment | ReadOnlyTextArea | 注释（@FXML） |
  | definition | Editor | 定义（@FXML） |
  | createDefinition | Editor | ddl（@FXML） |
  | treeItem | ShellMysqlFunctionTreeItem | 函数节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initInfo()` | 初始化信息 | 经 `treeItem.dbItem()` 取 database，`selectFunction` 获取 `MysqlFunction` 并填充名称、注释、定义、ddl |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "item" prop，`StageManager.showMask(initInfo)` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.functionInfo()` |
  | `void destroy()` | 销毁 | 销毁两个 Editor |

- 调用链：`onWindowShown → StageManager.showMask → initInfo → ShellMysqlDatabaseTreeItem.selectFunction`

## ShellMysqlProcedureInfoController

- 职责：MySQL 存储过程信息查看业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 名称（@FXML） |
  | comment | ReadOnlyTextArea | 注释（@FXML） |
  | definition | Editor | 定义（@FXML） |
  | createDefinition | Editor | ddl（@FXML） |
  | treeItem | ShellMysqlProcedureTreeItem | 过程节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initInfo()` | 初始化信息 | 经 `treeItem.dbItem()` 取 database，`selectProcedure` 获取 `MysqlProcedure` 并填充名称、注释、定义、ddl |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "item" prop，`StageManager.showMask(initInfo)` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.procedureInfo()` |
  | `void destroy()` | 销毁 | 销毁两个 Editor |

- 调用链：`onWindowShown → StageManager.showMask → initInfo → ShellMysqlDatabaseTreeItem.selectProcedure`

## ShellMysqlTableInfoController

- 职责：MySQL 表信息查看业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 名称（@FXML） |
  | tableEngine | ReadOnlyTextField | 引擎（@FXML） |
  | tableCharset | ReadOnlyTextField | 字符集（@FXML） |
  | tableCollation | ReadOnlyTextField | 排序方式（@FXML） |
  | tableRowFormatBox | FXVBox | 行格式组件（@FXML） |
  | tableRowFormat | ReadOnlyTextField | 行格式（@FXML） |
  | tableAutoIncrementBox | FXVBox | 自动递增组件（@FXML） |
  | tableAutoIncrement | ReadOnlyTextField | 自动递增（@FXML） |
  | comment | ReadOnlyTextArea | 注释（@FXML） |
  | createDefinition | Editor | 定义（@FXML） |
  | treeItem | ShellMysqlTableTreeItem | 表节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initInfo()` | 初始化信息 | 经 `treeItem.dbItem()` 取 database，`selectTable` 获取 `MysqlTable`，填充名称、注释、引擎、字符集、排序、ddl；InnoDB 显示行格式，有自增则显示自增值 |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "item" prop，`StageManager.showMask(initInfo)` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.tableInfo()` |

- 调用链：`onWindowShown → StageManager.showMask → initInfo → ShellMysqlDatabaseTreeItem.selectTable`

## ShellMysqlViewInfoController

- 职责：MySQL 视图信息查看业务对话框控制器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | ReadOnlyTextField | 名称（@FXML） |
  | comment | ReadOnlyTextArea | 注释（@FXML） |
  | definition | Editor | 定义（@FXML） |
  | createDefinition | Editor | ddl（@FXML） |
  | treeItem | ShellMysqlViewTreeItem | 视图节点 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initInfo()` | 初始化信息 | 经 `treeItem.dbItem()` 取 database，`selectView` 获取 `MysqlView` 并填充名称、注释、定义、ddl |
  | `void onWindowShown(WindowEvent event)` | 显示初始化 | 取 "item" prop，`StageManager.showMask(initInfo)` |
  | `String getViewTitle()` | 视图标题 | `I18nHelper.viewInfo()` |

- 备注：类内 `destroy()` 方法整段被注释（死代码，已跳过）。
- 调用链：`onWindowShown → StageManager.showMask → initInfo → ShellMysqlDatabaseTreeItem.selectView`
