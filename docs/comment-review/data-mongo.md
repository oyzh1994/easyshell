# 数据模块 · mongo 包

本文件覆盖 `cn.oyzh.easyshell.data.mongo` 包（含 `config/`、`dto/`、`file/`、`handler/`、`ui/` 子目录）下全部 33 个 `.java` 文件中的有效类，逐类说明职责、字段、方法与调用链；整文件被注释掉的死代码在末尾“跳过清单”注明。

## ShellMongoDataImportHelper

- 职责：Mongo 数据导入的静态值解析工具，把文本单元格转换成合适的 Java 类型。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static Object parseValue(String value)` | 解析单个字符串值 | `null` 直接返回 `null`；`RegexUtil.isDecimal` 命中则 `Double.parseDouble`；`RegexUtil.isNumber` 命中则 `Integer.parseInt`；以 `[` 开头且以 `]` 结尾则 `JSONUtil.parseArray`；其余原样返回字符串 |

- 调用链：`ShellMongoDataImportHelper.parseValue` ← 仅被已注释的 `MysqlCsvTypeFileReader` / `MysqlTxtTypeFileReader` / `ShellMongoXmlTypeFileReader` 调用（当前活跃代码中无调用方）

## ShellMongoDataExportCollection

- 职责：一次导出任务中的“集合（表）”模型，承载集合名、待导出记录、字段列表及文件路径/选中状态等 JavaFX 属性，并提供对应 UI 控件。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `name` | `String` | 集合（表）名称，实现 `DBName` |
  | `records` | `List<MongoRecord>` | 记录列表，查询导出时使用（非空则直接导出这批记录） |
  | `columns` | `List<ShellMongoDataExportColumn>` | 导出字段列表 |
  | `filePathProperty` | `StringProperty` | 导出文件路径，懒初始化 |
  | `selectedProperty` | `BooleanProperty` | 是否选中，懒初始化；选中且无路径时自动生成默认路径 |
  | `extensionProperty` | `ObjectProperty<FileExtensionFilter>` | 文件扩展后缀，变化时刷新文件路径 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `selectedProperty()` / `isSelected()` / `setSelected(boolean)` | 选中状态读写 | 属性懒创建并注册监听：选中且 `getFilePath()==null` 时调用 `updateFilePath()` |
  | `getSelectedControl()` | 生成行内选中 `FXCheckBox` | 双向绑定 `selectedProperty`，用 `AtomicBoolean ignoreChanged` 防回环，`TableViewUtil.selectRowOnMouseClicked` 支持点击行 |
  | `filePathProperty()` / `getFilePath()` / `setFilePath(String)` | 文件路径读写 | 惰性属性 |
  | `getFilePathControl()` | 生成 `SaveFileTextField` | 绑定 `filePathProperty` 与 `extensionProperty`；选择文件回调写回路径与初始文件名；`rowOnCtrlS` 支持 Ctrl+S |
  | `extensionProperty()` / `getExtension()` / `setExtension(...)` | 扩展后缀读写 | 属性监听变化即 `updateFilePath()` |
  | `fileName()`（private） | 生成默认文件名 | `name` + 扩展后缀去掉首个字符（`.xxx`） |
  | `columns(List<? extends MongoColumn>)` | 由 MongoColumn 列表构建导出字段 | 逐个 `new ShellMongoDataExportColumn()` 并 `copy(column)` |
  | `columns()` | 返回字段列表副本 | `new ArrayList<>(this.columns)` |
  | `selectedColumns()` / `selectedColumnNames()` | 取选中字段/字段名 | 遍历 `columns`，按 `isSelected()` 过滤 |
  | `hasColumns()` | 是否有字段 | `CollectionUtil.isNotEmpty(columns)` |
  | `updateFilePath()`（private） | 刷新默认文件路径 | 桌面目录 + `File.separator` + `fileName()` |
  | `getName()` / `setName(String)` | 名称读写 | 实现 `DBName` |
  | `getColumns()` / `setColumns(...)` / `getRecords()` / `setRecords(...)` | 字段、记录读写 | 普通 getter/setter |

- 调用链：`ShellMongoDataExportController` → `ShellMongoDataExportCollection` → `ShellMongoDataExportHandler.tables` / `ShellMongoDataExportCollectionTableView` / `ShellMongoQuerySelectTabController`

## ShellMongoDataExportColumn

- 职责：导出字段模型，继承 `MongoColumn`，额外携带“是否选中”标记。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `selected` | `boolean` | 是否选中，默认 `true` |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isSelected()` / `setSelected(boolean)` | 选中状态读写 | 普通读写 |

- 调用链：`ShellMongoDataExportCollection.columns(...)` 生成 → `ShellMongoDataExportCollectionListView` 渲染勾选框 → `ShellMongoDataExportController`

## ShellMongoDataImportFile

- 职责：导入任务中的“待导入文件 + 目标集合”模型，提供文件选择控件与目标集合下拉控件。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dbName` | `String` | 目标数据库名称 |
  | `dbClient` | `ShellMongoClient` | 数据库客户端 |
  | `fileProperty` | `ObjectProperty<File>` | 文件属性，懒初始化 |
  | `targetTableName` | `String` | 目标集合名称（为空时用文件名推断） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setDbName(String)` / `setDbClient(ShellMongoClient)` | 注入数据库信息 | 普通 setter |
  | `fileProperty()` / `getFile()` / `setFile(File)` | 文件属性读写 | 惰性属性 |
  | `getFilePath()` / `getFileName()` | 取文件路径/文件名 | 基于 `getFile()` 返回，空则 `null` |
  | `getFilePathControl()` | 生成 `ChooseFileTextField` | 绑定 `fileProperty`，选文件回调 `setFile`，`selectRowOnMouseClicked` |
  | `getTargetTableControl()` | 生成 `ShellMongoCollectionComboBox` | `StageManager.showMask(() -> comboBox.init(dbName, getTableName(), dbClient))`；选中回调 `setTargetTableName` |
  | `getTableName()` | 由文件名推断集合名 | 去掉最后一个 `.` 及其后缀 |
  | `getTargetTableName()` / `setTargetTableName(String)` | 目标集合名读写 | 未设置时回退到 `getTableName()` |

- 调用链：`ShellMongoDataImportController` → `ShellMongoDataImportFile` → `ShellMongoDataImportFileTableView` / `ShellMongoDataImportHandler.files`

## ShellMongoTypeFileWriter

- 职责：导出文件写入器抽象基类，定义写入头/尾/对象模板方法，并统一做“值参数化”与“行格式化”。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `init()` | 初始化钩子 | 空实现，子类可覆盖 |
  | `parameterized(MongoColumn, Object, DBDataExportConfig)` | 值标准化/转字符串 | 先 `ShellMongoDataUtil.valueStandardization`；按列类型分支：字符串 `TextUtil.escape`、ObjectId 转 `toHexString`、整型/大整型/数字/布尔原样、时间戳 `DateUtil.format`、二进制 `"0x"+HexUtil.encodeHexStr`、JSON/JSONArray/Code `JSONUtil.toJson`，兜底 `toString` |
  | `writeHeader()` / `writeTrial()` | 写头/写尾钩子 | 默认空实现 |
  | `writeObject(Map)`（abstract） | 写单条对象 | 由子类实现 |
  | `writeObjects(List<Map>)` | 批量写对象 | 逐个调用 `writeObject` |
  | `formatLine(Object[], ...)` / `formatLine(List<?>, ...)` | 拼装分隔行 | 每列 `fieldSeparator + txtIdentifier + val + txtIdentifier`，末尾接 `recordSeparator`，`substring(1)` 去掉首个分隔符；数组版先以 `Objects.requireNonNullElse(v,"")` 兜 null |

- 调用链：`ShellMongoDataExportHandler.exportTable` → `ShellMongoTypeFileWriter.{writeHeader,writeObjects,writeTrial}` → 各 `ShellMongo*TypeFileWriter` 子类

## ShellMongoCsvTypeFileWriter

- 职责：CSV 类型导出写入器，逗号分隔、带文本标识符。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `columns` | `MongoColumns` | 字段列表 |
  | `config` | `DBDataExportConfig` | 导出配置 |
  | `writer` | `LineFileWriter`（final） | 行文件写入器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(String filePath, DBDataExportConfig, MongoColumns)` | 初始化 | `LineFileWriter.create(filePath, config.getCharset())` |
  | `writeHeader()` | 写列名行 | `formatLine(columns.columnNames(), ",", txtIdentifier, recordSeparator)` |
  | `writeObject(Map)` | 写一条记录 | 按 `columns.index(key)` 定位，`columns.column(key)` 取值并 `parameterized`，再 `formatLine(values, ","...)` |
  | `close()` | 关闭并清空 | 关闭 `writer`，置空 `config`/`columns` |

- 调用链：`ShellMongoDataExportHandler.initWriter`（`isCsvType()` 分支）→ `ShellMongoCsvTypeFileWriter`

## ShellMongoExcelTypeFileWriter

- 职责：Excel（xls/xlsx）类型导出写入器，基于 `WorkbookHelper` 生成工作簿，按列类型写单元格。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `columns` | `MongoColumns` | 字段列表 |
  | `config` | `DBDataExportConfig` | 导出配置 |
  | `workbook` | `Workbook` | xls 工作簿 |
  | `xlsRowIndex` | `int` | 当前数据行号，初始 1 |
  | `filePath` | `String` | 输出文件路径 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(String filePath, DBDataExportConfig, MongoColumns)` | 初始化 | 依据后缀是否 `.xlsx` 调 `WorkbookHelper.create(isXlsx)` |
  | `writeHeader()` | 建 sheet + 列名行 | 重置 `xlsRowIndex=1`，`createSheet(columns.collectionName())`，第 0 行写列名，`WorkbookHelper.write` 落盘 |
  | `writeObject(Map, boolean flush)`（private） | 写数据行 | 组装 `values` 后 `getActiveSheet` → `createRow`，按运行时类型 `switch`（Date/Double/String/Boolean/Calendar/LocalDate/LocalDateTime/Number）写单元格，`flush` 时落盘 |
  | `writeObject(Map)` | 单条写 | 调 `writeObject(object, true)` |
  | `writeObjects(List<Map>)` | 批量写 | 逐条 `writeObject(object, false)`，最后一次性 `WorkbookHelper.write` |
  | `close()` | 关闭 | 关闭 `workbook` 并置空字段 |

- 调用链：`ShellMongoDataExportHandler.initWriter`（`isExcelType()` 分支）→ `ShellMongoExcelTypeFileWriter`

## ShellMongoHtmlTypeFileWriter

- 职责：HTML 类型导出写入器，输出带内联样式的 `<table>` 表格。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `columns` | `MongoColumns` | 字段列表 |
  | `config` | `DBDataExportConfig` | 导出配置 |
  | `writer` | `LineFileWriter` | 行文件写入器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(filePath, config, columns)` | 初始化 | `LineFileWriter.create` |
  | `writeHeader()` | 写 HTML 头与表头 | 文本块输出 `<!DOCTYPE html>`+样式+`<table>`，再拼 `<tr>` 与各 `<th>列名</th>` |
  | `writeTrial()` | 写 HTML 尾 | 文本块输出 `</table></body></html>` |
  | `writeObject(Map)` | 写一行 `<tr>` | 按列索引取值 `parameterized`，拼 `<td>值</td>` |
  | `close()` | 关闭并清空 | 关闭 `writer`，置空字段 |

- 调用链：`ShellMongoDataExportHandler.initWriter`（`isHtmlType()` 分支）→ `ShellMongoHtmlTypeFileWriter`

## ShellMongoJsTypeFileWriter

- 职责：JS（Mongo insert 脚本）类型导出写入器，每条记录生成 `insert` 脚本行。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `columns` | `MongoColumns` | 字段列表 |
  | `config` | `DBDataExportConfig` | 导出配置 |
  | `writer` | `LineFileWriter` | 行文件写入器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(filePath, config, columns)` | 初始化 | `LineFileWriter.create` |
  | `writeObject(Map)` | 生成 insert 脚本并写行 | 用 `columns` 建 `MongoRecord`，逐项 `putValue`，再 `ShellMongoDataUtil.toInsertScript(record)` 写一行 |
  | `close()` | 关闭并清空 | 关闭 `writer`，置空字段 |

- 调用链：`ShellMongoDataExportHandler.initWriter`（`isJsType()` 分支）→ `ShellMongoJsTypeFileWriter` → `ShellMongoDataUtil.toInsertScript`

## ShellMongoJsonTypeFileWriter

- 职责：JSON 类型导出写入器，支持普通数组与“早期版本”`{"RECORDS":[...]}` 两种结构。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `columns` | `MongoColumns` | 字段列表 |
  | `config` | `DBDataExportConfig` | 导出配置 |
  | `writer` | `LineFileWriter` | 行文件写入器 |
  | `firstWrite` | `boolean` | 是否首次写入，初始 `true`（用于控制逗号） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(filePath, config, columns)` | 初始化 | `LineFileWriter.create` |
  | `writeHeader()` | 写 JSON 开头 | 早期版本写 `{` + `"RECORDS": [`；否则写 `[` |
  | `writeTrial()` | 写 JSON 结尾 | 早期版本写 `\n]}`；否则写 `\n]` |
  | `writeObject(Map)` | 写一个 JSON 对象 | 非首次先写 `,\n`；逐字段拼 `"key" : value`；`parameterized` 后按 `ShellMongoUtil.isPrimaryType` 决定是否加引号，`null` 写 `null`；用 `size` 递减控制字段间逗号 |
  | `close()` | 关闭并清空 | 关闭 `writer`，置空字段 |

- 调用链：`ShellMongoDataExportHandler.initWriter`（`isJsonType()` 分支）→ `ShellMongoJsonTypeFileWriter`

## ShellMongoTxtTypeFileWriter

- 职责：TXT 类型导出写入器，用配置的分隔符/文本标识符拼装纯文本行。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `columns` | `MongoColumns` | 字段列表 |
  | `config` | `DBDataExportConfig` | 导出配置 |
  | `writer` | `LineFileWriter` | 行文件写入器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(filePath, config, columns)` | 初始化 | `LineFileWriter.create` |
  | `writeHeader()` | 写列名行 | `formatLine(columnNames, fieldSeparator, txtIdentifier, recordSeparator)` |
  | `writeObject(Map)` | 写一条记录 | 按列索引取值 `parameterized` 后 `formatLine` |
  | `close()` | 关闭并清空 | 关闭 `writer`，置空字段 |

- 调用链：`ShellMongoDataExportHandler.initWriter`（`isTxtType()` 分支）→ `ShellMongoTxtTypeFileWriter`

## ShellMongoXmlTypeFileWriter

- 职责：XML 类型导出写入器，支持“字段作为属性”和“字段作为子节点”两种输出形态，并额外做引号转义。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `columns` | `MongoColumns` | 字段列表 |
  | `config` | `DBDataExportConfig` | 导出配置 |
  | `writer` | `LineFileWriter` | 行文件写入器 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(filePath, config, columns)` | 初始化 | `LineFileWriter.create` |
  | `writeHeader()` | 写 XML 声明与 `<RECORDS>` | `<?xml ...?>` + `<RECORDS>` |
  | `writeTrial()` | 写 `</RECORDS>` | 收尾 |
  | `writeObject(Map)` | 写一条 `<RECORD>` | `config.isFieldToAttr()` 为真则写 `<RECORD k="v" .../>`；否则写 `<RECORD>` 内每个 `<key>value</key>`（值为 `null` 写自闭合 `<key/>`） |
  | `parameterized(...)`（覆盖） | 值标准化 + 属性模式转义 | 先 `valueStandardization`，调 `super.parameterized`，若 `isFieldToAttr()` 且列为字符串/JSON/JSONArray/Code 则 `ShellMongoDataUtil.escapeQuotes` |
  | `close()` | 关闭并清空 | `IOUtil.close(writer)` 并置空字段 |

- 调用链：`ShellMongoDataExportHandler.initWriter`（`isXmlType()` 分支）→ `ShellMongoXmlTypeFileWriter`

## ShellMongoDataDumpHandler

- 职责：Mongo 数据转储处理器，把集合结构、文档、函数导出为可执行的 Mongosh 脚本文件。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dbClient` | `ShellMongoClient` | MongoDB 客户端 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(ShellMongoClient, String dbName)` | 初始化 | `super(dbName, DBDialect.MONGODB)` |
  | `doDump()` | 转储入口 | 校验 writer/dumpType/dataType；`writeHeader`；`dumpType==1` 时 `dumpCollection()`+`dumpFunction()`，`==2` 时构造单个 `MongoCollection` 转储并 `processedIncr`；`writeTail` 后关文件、报告完成与文件路径 |
  | `dumpCollection()` | 转储所有集合 | `dbClient.listCollections(dbName)` 遍历，逐个 `dumpCollection(table)` + `processedIncr` |
  | `dumpCollection(MongoCollection)` | 转储单个集合结构 | 输出注释与 `db.getCollection('x').drop()` / `db.createCollection('x')`；`isDumpRecord()` 时调 `dumpRecord` |
  | `dumpRecord(String)` | 分页转储文档 | 循环构造 `MongoSelectRecordParam`（start/limit/readonly）→ `selectCollectionRecords` → `ShellMongoDataUtil.toInsertScript` 追加，`start += queryLimit` 直到空；记录查询/写入耗时日志 |
  | `dumpFunction()` | 转储集合函数 | 遍历 `listFunctions`，生成删除/创建 `system.js` 记录的脚本，函数体 `\r`/`\n` 转义 |
  | `writeHeader()` | 写转储文件头注释 | 拼接工具名、源/目标服务器、版本、主机、库名、编码、日期 |
  | `writeTail()` | 写尾 | 空实现 |
  | `getDbClient()` / `setDbClient(...)` | 客户端读写 | 普通 getter/setter |

- 调用链：`ShellMongoDataDumpController` → `ShellMongoDataDumpHandler` → `ShellMongoClient.{listCollections,selectCollectionRecords,listFunctions,selectVersion}` → `ShellMongoDataUtil.toInsertScript`

## ShellMongoDataExportHandler

- 职责：Mongo 数据导出处理器，按所选格式（Excel/HTML/JSON/XML/CSV/TXT/JS）分页导出集合数据到文件。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dbClient` | `ShellMongoClient` | 数据库客户端 |
  | `tables` | `List<ShellMongoDataExportCollection>` | 待导出集合列表 |
  | `config` | `DBDataExportConfig`（final） | 导出配置 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(ShellMongoClient, String dbName)` | 初始化 | `super(dbName)`，新建 `DBDataExportConfig` |
  | `doExport()` | 导出入口 | 遍历 `tables`，逐个 `exportTable` + `processedIncr`，`checkInterrupt` 支持中断 |
  | `initWriter(String, MongoColumns)`（private） | 按类型选择写入器 | 依次判断 Excel/HTML/JSON/XML/CSV/TXT/JS，命中则 new 对应 `ShellMongo*TypeFileWriter` |
  | `exportTable(ShellMongoDataExportCollection)` | 导出单表 | 依据 `selectedColumns` 建 `MongoColumns`；`try-with-resources` 打开 writer，`writeHeader`；`columns` 非空则分页：`records==null` 时构造查询参数调 `selectCollectionRecords`，否则直接用 `table.getRecords()` 并停止；`writeRecord` 写入；异常时按 `isContinueWithError()` 决定继续或抛出；最后 `writeTail` |
  | `writeHeader/writeRecord/writeTail(ShellMongoTypeFileWriter)`（private） | 委托写入 | 分别调 `writer.writeHeader()`、`writer.writeObjects(records.toMap())`、`writer.writeTrial()` |
  | `dateFormat / recordSeparator / txtIdentifier / fieldSeparator / includeFields / fieldToAttr / earlyVersion / continueWithError(...)` | 配置透传 setter | 写入 `config`，`dateFormat` 空值回退 `"yyyy-MM-dd HH:mm:ss"` |
  | `getDbClient/setDbClient/getTables/setTables/getConfig` | 读写 | 普通 getter/setter |

- 调用链：`ShellMongoDataExportController` → `ShellMongoDataExportHandler.exportTable` → `ShellMongoTypeFileWriter` 子类 + `ShellMongoClient.selectCollectionRecords`

## ShellMongoDataImportHandler

- 职责：Mongo 数据导入处理器，从 JSON/XML/Excel 文件分批读取记录并批量插入集合。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dbClient` | `ShellMongoClient` | MongoDB 客户端 |
  | `files` | `List<ShellMongoDataImportFile>` | 待导入文件列表 |
  | `config` | `DBDataImportConfig`（final） | 导入配置 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(ShellMongoClient, String dbName)` | 初始化 | `super(dbName)`，新建 `DBDataImportConfig` |
  | `doImport()` | 导入入口 | 遍历 `files` 逐个 `importRecord`，最后 `processed(files.size())` |
  | `importRecord(ShellMongoDataImportFile)` | 导入单文件 | 复制模式下先 `dbClient.clearCollection`；`try-with-resources` 打开 reader，循环 `readRecords` 直到空，`addInsert` 累积；结束后 `doBatchInsert` |
  | `initReader(File)`（private） | 按类型选择读取器 | JSON→`DBDataJsonTypeFileReader`，XML→`DBDataXmlTypeFileReader`，Excel→`DBDataExcelTypeFileReader` |
  | `readRecords(DBDataTypeFileReader, int)`（private） | 读取并转换为 MongoRecord | `reader.readObjects(count)` 得到 Map 列表，逐条以文件名（去扩展名）为集合名构建 `MongoColumns` 与 `MongoRecord` |
  | `doBatchInsert(List<MongoRecord>, boolean)`（覆盖） | 批量插入 | 先给每条记录的列设 `dbName`，调 `dbClient.insertCollectionRecord(list)`，成功 `processedIncr`，失败 `processedDecr` 后抛出 |
  | `dateFormat / importMode / columnIndex / dataStartIndex / recordLabel / attrToColumn / recordSeparator / txtIdentifier / fieldSeparator(...)` | 配置透传 setter | 写入 `config`，`dateFormat` 空值回退 `"yyyy-MM-dd HH:mm:ss"` |
  | `getDbClient/setDbClient/getFiles/setFiles/getConfig` | 读写 | 普通 getter/setter |

- 调用链：`ShellMongoDataImportController` → `ShellMongoDataImportHandler.importRecord` → `DBData*TypeFileReader` + `ShellMongoClient.{clearCollection,insertCollectionRecord}`

## ShellMongoDataTransportHandler

- 职责：Mongo 数据库间数据传输处理器（跨客户端），逐集合删除/重建并复制文档，另可复制函数。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `sourceClient` | `ShellMongoClient` | 来源客户端 |
  | `targetClient` | `ShellMongoClient` | 目标客户端 |
  | `tables` | `List<DBDataTransportObject>` | 待传输表列表 |
  | `functions` | `List<DBDataTransportObject>` | 待传输函数列表 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()` | 初始化 | `super(DBDialect.MONGODB)` |
  | `doTransport()` | 传输入口 | 遍历 `tables` 逐个 `transportTable`，再遍历 `functions` 逐个 `transportFunction`；异常 `exception(ex)`，finally 报告完成 |
  | `transportTable(String)`（private） | 传输单表 | `targetClient.dropCollection` → `createCollection` → 分页 `sourceClient.selectCollectionRecords`，`addInsert` 写入目标，`start += selectLimit` 直到空 |
  | `transportFunction(String)`（private） | 传输单函数 | `targetClient.dropFunction`；`sourceClient.selectFunction` 取代码后 `targetClient.createFunction` 重建 |
  | `doBatchInsert(List<MongoRecord>, boolean)`（覆盖） | 批量插入目标 | 给每条记录的列设 `targetDatabase`，调 `targetClient.insertCollectionRecord`，成功 `processedIncr`，失败 `processedDecr` 后抛出 |
  | `setFunctions/getFunctions/getSourceClient/setSourceClient/getTargetClient/setTargetClient/getTables/setTables` | 读写 | 普通 getter/setter |

- 调用链：`ShellMongoDataTransportController` → `ShellMongoDataTransportHandler` → `ShellMongoClient.{dropCollection,createCollection,selectCollectionRecords,dropFunction,selectFunction,createFunction,insertCollectionRecord}`

## ShellMongoRunScriptFileHandler

- 职责：执行 Mongo 脚本文件的处理器，逐行解析脚本、剥离注释、按 `db.` 开头且以 `;` 结尾切分语句并交给脚本引擎执行。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dbClient` | `ShellMongoClient` | MongoDB 客户端 |
  | `engine` | `MongoScriptEngine` | 脚本引擎，构造时 `dbClient.shellEngine()` 并 `engine.db(dbName)` |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(ShellMongoClient, String dbName)` | 初始化 | `super(dbName)`；创建脚本引擎并绑定库名 |
  | `runFile()` | 执行脚本文件 | `FileUtil.getReader(file, UTF_8)` 逐行读：跳过 `//` 单行注释、处理 `/* */` 多行注释（`commentFlag`）；以 `db.` 开头置 `createFlag`，遇到以 `;` 结尾闭合语句则拼接 `builder` 并 `addInsert` + `processedIncr`；异常时 `exception` + `processedDecr`，`continueWithErrors` 为假则中断，否则清理缓冲；收尾 `doBatchInsert` |
  | `doBatchInsert(List<String>, boolean)`（覆盖） | 批量执行脚本 | 逐条 `engine.eval(s)`，结果非空则 `processedIncr` |
  | `getDbClient()` / `setDbClient(...)` | 客户端读写 | 普通 getter/setter |

- 调用链：`ShellMongoRunScriptFileController` → `ShellMongoRunScriptFileHandler.runFile` → `MongoScriptEngine.eval`

## ShellMongoDataExportCollectionTableView

- 职责：导出集合表格视图，提供“获取选中集合”“是否存在选中集合”的便捷方法。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getSelectedTables()` | 取选中集合列表 | 遍历 `getItems()`，按 `item.isSelected()` 收集 |
  | `hasSelectedTable()` | 是否有选中集合 | 遍历 `getItems()`，命中即返回 `true` |

- 调用链：`ShellMongoDataExportController` → `ShellMongoDataExportCollectionTableView`

## ShellMongoDataExportColumnListView

- 职责：导出字段勾选列表视图，用 `FXCheckBox` 渲染字段并回写选中状态。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `init(List<ShellMongoDataExportColumn> columns)` | 初始化列表项 | 先 `clearItems`；对每个字段建 `FXCheckBox`（文本为列名、初值取 `isSelected`），`selectedChanged` 回写 `column.setSelected`，`ListViewUtil.selectRowOnMouseClicked` 支持点击行 |

- 调用链：`ShellMongoDataExportController` → `ShellMongoDataExportColumnListView.init` → `ShellMongoDataExportColumn.setSelected`

## ShellMongoDataImportFileTableView

- 职责：导入文件表格视图，继承 `FXTableView<ShellMongoDataImportFile>`；原数据库注入与初始化逻辑已整体注释，当前仅保留类声明。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | （无） | 类体的方法/字段均处于注释状态 | 被注释的 `setDbName/setDbClient/initItems/initNode` 仅为历史逻辑，未启用 |

- 调用链：`ShellMongoDataImportController` → `ShellMongoDataImportFileTableView`（当前为空壳视图）

## 跳过清单（整文件被注释掉的死代码）

以下 13 个文件整体处于注释状态，不作为有效类收录：

- `config/ShellMongoDataExportConfig.java`
- `config/ShellMongoDataImportConfig.java`
- `dto/ShellMongoDataTransportCollection.java`
- `dto/ShellMongoDataTransportFunction.java`
- `file/MysqlCsvTypeFileReader.java`
- `file/MysqlTxtTypeFileReader.java`
- `file/ShellMongoExcelTypeFileReader.java`
- `file/ShellMongoJsonTypeFileReader.java`
- `file/ShellMongoTypeFileReader.java`
- `file/ShellMongoXmlTypeFileReader.java`
- `ui/ShellMongoDataExportCollectionComboBox.java`
- `ui/ShellMongoDataTransportFunctionListView.java`
- `ui/ShellMongoDataTransportTableListView.java`

说明：有效类共 20 个（`ShellMongoDataImportHelper`、`ShellMongoDataExportCollection`、`ShellMongoDataExportColumn`、`ShellMongoDataImportFile`、`ShellMongoTypeFileWriter`、`ShellMongoCsvTypeFileWriter`、`ShellMongoExcelTypeFileWriter`、`ShellMongoHtmlTypeFileWriter`、`ShellMongoJsTypeFileWriter`、`ShellMongoJsonTypeFileWriter`、`ShellMongoTxtTypeFileWriter`、`ShellMongoXmlTypeFileWriter`、`ShellMongoDataDumpHandler`、`ShellMongoDataExportHandler`、`ShellMongoDataImportHandler`、`ShellMongoDataTransportHandler`、`ShellMongoRunScriptFileHandler`、`ShellMongoDataExportCollectionTableView`、`ShellMongoDataExportColumnListView`、`ShellMongoDataImportFileTableView`），加上 13 个死代码文件，合计 33 个 `.java` 文件。
