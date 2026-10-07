# 数据模块 · dameng 包

> 范围：`cn/oyzh/easyshell/data/dameng/`（含 `dto/`、`file/`、`handler/`、`ui/` 子包），递归共 36 个 `.java` 文件。
> 说明：其中 20 个文件为存活代码，本文逐一展开；另有 16 个文件为**整文件被注释掉的死代码**（自 `//package ...` 起全部注释），依据规则不在正文列出，仅在文末“跳过清单”登记。

---

## ShellDamengDataExportColumn

- 职责：Dameng 数据导出用的字段对象，在 `DamengColumn` 基础上扩展“是否选中”标记。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | selected | boolean | 该字段是否被选中导出，默认 `true` |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isSelected()` / `setSelected(boolean)` | 读取/设置选中状态 | 纯 getter/setter（构造合并简写） |

- 调用链：`ShellDamengDataExportTable.columns → new ShellDamengDataExportColumn().copy → ShellDamengDataExportColumn.isSelected`

---

## ShellDamengDataExportTable

- 职责：描述一张待导出的表，承载表名、导出字段、记录、文件路径与格式后缀，并提供 JavaFX 控件工厂。实现 `DBName`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | name | String | 表名称 |
  | records | List\<DamengRecord\> | 预置记录列表（“查询导出”场景使用，为 null 表示走分页查询导出） |
  | columns | List\<ShellDamengDataExportColumn\> | 字段列表 |
  | filePathProperty | StringProperty | 文件路径 JavaFX 属性 |
  | selectedProperty | BooleanProperty | 是否选中 JavaFX 属性 |
  | extensionProperty | ObjectProperty\<FileExtensionFilter\> | 导出文件扩展名（后缀格式）属性 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `selectedProperty()` | 懒加载选中属性 | 首次创建 `SimpleBooleanProperty(false)`，并挂监听：选中且无路径时调用 `updateFilePath()` |
  | `isSelected()` / `setSelected(boolean)` | 读写选中状态 | 基于 `selectedProperty` |
  | `getSelectedControl()` | 生成表格行的选中复选框 | 用 `AtomicBoolean ignoreChanged` 防止复选框与属性互相回写死循环；绑定 `TableViewUtil.selectRowOnMouseClicked` |
  | `filePathProperty()` / `getFilePath()` / `setFilePath(String)` | 文件路径属性/读写 | 读写 `filePathProperty` |
  | `getFilePathControl()` | 生成保存文件输入框 `SaveFileTextField` | 同步路径、扩展名、初始文件名；监听 path/extension 变更刷新；绑定 Ctrl+S 保存与行点击 |
  | `extensionProperty()` / `getExtension()` / `setExtension(FileExtensionFilter)` | 扩展名属性/读写 | 首次创建时挂监听，变更时 `updateFilePath()` |
  | `fileName()`（private） | 依据扩展名拼接文件名 | `name + 扩展名`（去掉后缀点后的部分）；无扩展名返回空串 |
  | `columns(List<? extends DamengColumn>)` | 由数据库字段构造导出字段 | 逐列 `new ShellDamengDataExportColumn().copy(column)`，默认全部选中 |
  | `columns()` | 返回字段副本列表 | `new ArrayList<>(this.columns)` |
  | `selectedColumns()` / `selectedColumnNames()` | 过滤出选中字段 / 字段名 | 遍历 `columns` 按 `isSelected()` 收集 |
  | `hasColumns()` | 是否有字段 | `CollectionUtil.isNotEmpty(this.columns)` |
  | `updateFilePath()`（private） | 更新默认导出路径 | 当已选中或已有路径时，置为“桌面目录 + 文件名”，依赖 `FXChooser.getDesktopDirectory()` |
  | `getName`/`setName`/`getColumns`/`setColumns`/`getRecords`/`setRecords` | 基础访问器 | 构造/getter/setter 合并简写 |

- 调用链：`ShellDamengDataExportHandler.exportTable → ShellDamengDataExportTable.selectedColumns/getFilePath → ShellDamengTypeFileWriter.writeObjects`

---

## ShellDamengDataImportFile

- 职责：描述一条“导入文件 → 目标表”的映射，承载源文件、模式、客户端与目标表名，并提供 JavaFX 控件工厂。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | schema | String | 模式名称 |
  | dbClient | ShellDamengClient | 数据库客户端 |
  | fileProperty | ObjectProperty\<File\> | 源文件 JavaFX 属性 |
  | targetTableName | String | 目标表名称（为空时回退为文件名派生表名） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setSchema` / `setDbClient` | 基础 setter | 构造/getter/setter 合并简写 |
  | `fileProperty()` / `getFile()` / `setFile(File)` | 文件属性/读写 | 懒加载 `SimpleObjectProperty` |
  | `getFilePath()` / `getFileName()` | 文件路径 / 文件名 | 基于 `getFile()`，为空返回 null |
  | `getFilePathControl()` | 生成选择文件输入框 `ChooseFileTextField` | 绑定 `setOnSelectedFile` 与 file 属性监听；行点击选中 |
  | `getTargetTableControl()` | 生成目标表下拉框 `DamengTableComboBox` | `StageManager.showMask` 中异步 `comboBox.init(schema, getTableName(), dbClient)`；选中变更写入 `targetTableName` |
  | `getTableName()` | 由文件名去扩展名推导表名 | `fileName.substring(0, lastIndexOf("."))`，文件名为空则原样返回 |
  | `getTargetTableName()` | 目标表名 | 未显式设置时回退 `getTableName()` |
  | `setTargetTableName(String)` | 设置目标表名 | setter |

- 调用链：`ShellDamengDataImportHandler.importRecord → ShellDamengDataImportFile.getTargetTableName/getFile → DBDataTypeFileReader`

---

## ShellDamengCsvTypeFileWriter

- 职责：CSV 格式导出写入器，输出带 txtIdentifier 包裹、逗号分隔的行。继承 `ShellDamengTypeFileWriter`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | columns | DamengColumns | 字段列表 |
  | config | DBDataExportConfig | 导出配置 |
  | writer | LineFileWriter | 行文件写入器（final） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengCsvTypeFileWriter(String, DBDataExportConfig, DamengColumns)` | 构造 | 保存 columns/config，`LineFileWriter.create(filePath, config.getCharset())` |
  | `writeHeader()` | 写表头行 | `formatLine(columns.columnNames(), ",", txtIdentifier, recordSeparator)` |
  | `writeObject(Map)` | 写单行记录 | 按 `columns.index()` 把各字段值摆到对应列位，`parameterized()` 参数化后 `formatLine` 落盘 |
  | `close()` | 关闭并释放 | `writer.close()`，置空 config/columns |

- 调用链：`ShellDamengDataExportHandler.initWriter → ShellDamengCsvTypeFileWriter.writeHeader/writeObjects → formatLine`

---

## ShellDamengDataImportHelper

- 职责：Dameng 导入辅助，提供值参数化与“记录 → INSERT SQL”生成。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static parameterized(DamengColumn, Object, DBDataImportConfig)` | 导入侧值参数化 | null/空串→null；日期类型按 `dateFormat` 解析并格式化为 `yyyy-MM-dd HH:mm:ss`；时间戳支持 `CharSequence`/`Date`；字符串 `TextUtil.escape` |
  | `static toInsertSql(DamengColumns, List<DamengRecord>, DBDataImportConfig)` | 生成批量 INSERT 语句 | 表名/字段用 `DBUtil.wrap(..., DBDialect.DAMENG)` 包裹；每行先 `DBUtil.unwrapData` 再 `parameterized` 再 `DBUtil.wrapData`；用 `deleteCharAt(len-2)` 去除尾部分隔符 |

- 调用链：`ShellDamengDataImportHandler.writeRecord → ShellDamengDataImportHelper.toInsertSql → dbClient.insertBatch`

---

## ShellDamengExcelTypeFileWriter

- 职责：Excel（xls/xlsx）格式导出写入器，基于 POI `Workbook`。继承 `ShellDamengTypeFileWriter`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | columns | DamengColumns | 字段列表 |
  | config | DBDataExportConfig | 导出配置 |
  | workbook | Workbook | POI 工作簿 |
  | xlsRowIndex | int | 当前写入行号，初始为 1 |
  | filePath | String | 目标文件路径 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengExcelTypeFileWriter(String, DBDataExportConfig, DamengColumns)` | 构造 | 依据后缀判断 xls/xlsx，`WorkbookHelper.create(isXlsx)` |
  | `writeHeader()` | 建 sheet、写表头并落盘 | 重置 `xlsRowIndex=1`；`createSheet(tableName)`；第 0 行按 `sortOfPosition()` 写列名；`WorkbookHelper.write` |
  | `writeObject(Map, boolean)`（private） | 写一行，可控制是否立即 flush | 按列索引摆放参数化后的值，用 `switch` 按运行时类型分派 `cell.setCellValue`（Date/Double/String/Boolean/Calendar/LocalDate/LocalDateTime/Number/其他 toString） |
  | `writeObject(Map)` | 写单行（立即落盘） | 委托 `writeObject(object, true)` |
  | `writeObjects(List<Map>)` | 批量写多行后一次落盘 | 逐行 `writeObject(object, false)`，最后 `WorkbookHelper.write` |
  | `close()` | 关闭工作簿并释放 | `workbook.close()`，置空各字段 |

- 调用链：`ShellDamengDataExportHandler.initWriter → ShellDamengExcelTypeFileWriter.writeHeader/writeObjects → WorkbookHelper.write`

---

## ShellDamengHtmlTypeFileWriter

- 职责：HTML 表格格式导出写入器。继承 `ShellDamengTypeFileWriter`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | columns | DamengColumns | 字段列表 |
  | config | DBDataExportConfig | 导出配置 |
  | writer | LineFileWriter | 行文件写入器 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengHtmlTypeFileWriter(String, DBDataExportConfig, DamengColumns)` | 构造 | 保存配置，`LineFileWriter.create` |
  | `writeHeader()` | 写 HTML 头与 `<tr><th>` 表头 | 内嵌 CSS 样式表（斑马纹、表头配色），按 `sortOfPosition()` 输出 `<th>` |
  | `writeTrial()` | 写 HTML 尾 | 输出 `</table></body></html>` |
  | `writeObject(Map)` | 写一行 `<tr><td>` | 按列索引摆放参数化值，逐值拼 `<td>` |
  | `close()` | 关闭并释放 | `writer.close()` |

- 调用链：`ShellDamengDataExportHandler.initWriter → ShellDamengHtmlTypeFileWriter.writeHeader/writeTrial/writeObjects`

---

## ShellDamengJsonTypeFileWriter

- 职责：JSON 数组格式导出写入器，支持“早期版本”的 `{"RECORDS":[...]}` 包装。继承 `ShellDamengTypeFileWriter`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | columns | DamengColumns | 字段列表 |
  | config | DBDataExportConfig | 导出配置 |
  | writer | LineFileWriter | 行文件写入器 |
  | firstWrite | boolean | 是否首次写入，初始 `true`（用于逗号分隔） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengJsonTypeFileWriter(String, DBDataExportConfig, DamengColumns)` | 构造 | `LineFileWriter.create` |
  | `writeHeader()` | 写 JSON 头 | `isEarlyVersion()` 为真写 `{` + `"RECORDS": [`，否则写 `[` |
  | `writeTrial()` | 写 JSON 尾 | 早版本写 `]}`，否则写 `]` |
  | `writeObject(Map)` | 写一条 JSON 对象 | 非首次先写 `,\n`；逐字段按 `parameterized` 值类型决定是否加引号（Number 裸写，null 写 `null`），手工拼 JSON |
  | `close()` | 关闭并释放 | `writer.close()` |

- 调用链：`ShellDamengDataExportHandler.initWriter → ShellDamengJsonTypeFileWriter.writeHeader/writeObjects → writer.write`

---

## ShellDamengSqlTypeFileWriter

- 职责：SQL（INSERT 语句）格式导出写入器。继承 `ShellDamengTypeFileWriter`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | columns | DamengColumns | 字段列表 |
  | config | DBDataExportConfig | 导出配置 |
  | writer | LineFileWriter | 行文件写入器（final） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengSqlTypeFileWriter(String, DBDataExportConfig, DamengColumns)` | 构造 | `LineFileWriter.create` |
  | `writeObject(Map)` | 生成并写入一条 INSERT | 表名 `DBUtil.wrap(..., DBDialect.DAMENG)`；`isIncludeFields()` 决定是否拼字段列表；值经 `parameterized` 后逗号拼接；`delete(len-2, len)` 去尾逗号，末尾加 `);` |
  | `parameterized(DamengColumn, Object, DBDataExportConfig)` | 重写 SQL 值参数化 | null→`NULL`；先 `ShellDamengDataUtil.valueStandardization` 规整；日期/时间戳按 `dateFormat` 加单引号；二进制→`0x...`；布尔→`1`/`0`；bit→`b'...'`；其余走 `DBUtil.wrapData` |
  | `close()` | 关闭并释放 | `writer.close()` |

- 调用链：`ShellDamengDataExportHandler.initWriter → ShellDamengSqlTypeFileWriter.writeObjects → parameterized → DBUtil.wrapData`

---

## ShellDamengTxtTypeFileWriter

- 职责：TXT 定长分隔文本格式导出写入器。继承 `ShellDamengTypeFileWriter`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | columns | DamengColumns | 字段列表 |
  | config | DBDataExportConfig | 导出配置 |
  | writer | LineFileWriter | 行文件写入器 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengTxtTypeFileWriter(String, DBDataExportConfig, DamengColumns)` | 构造 | `LineFileWriter.create` |
  | `writeHeader()` | 写表头 | `formatLine(columns.columnNames(), fieldSeparator, txtIdentifier, recordSeparator)` |
  | `writeObject(Map)` | 写记录行 | 按列索引摆放参数化值后 `formatLine` |
  | `close()` | 关闭并释放 | `writer.close()` |

- 调用链：`ShellDamengDataExportHandler.initWriter → ShellDamengTxtTypeFileWriter.writeHeader/writeObjects → formatLine`

---

## ShellDamengTypeFileWriter

- 职责：类型文件写入器抽象基类，定义写入头/尾/对象模板与公共值参数化、行格式化逻辑。实现 `Closeable`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `init()` | 初始化钩子 | 空实现，供子类覆盖 |
  | `parameterized(DamengColumn, Object, DBDataExportConfig)` | 通用值参数化 | null→null；`ShellDamengDataUtil.valueStandardization` 规整；日期/时间戳按格式输出；JSON `TextUtil.escape`；二进制→`0x`；布尔→`1`/`0`；bit→`b'..'`；字符串 `TextUtil.escape`；整型/数值原样 |
  | `writeHeader()` / `writeTrial()` | 头/尾写入钩子 | 空实现 |
  | `writeObject(Map)`（abstract） | 单条对象写入 | 抽象，子类实现 |
  | `writeObjects(List<Map>)` | 批量写入 | 逐条调用 `writeObject` |
  | `formatLine(Object[], String, String, String)` | 数组版格式化 | 委托 List 版 |
  | `formatLine(List<?>, String, String, String)` | 行格式化 | 每值拼 `分隔符 + 标识符 + 值 + 标识符`，末尾追加记录分隔符，`substring(1)` 去掉首个分隔符 |

- 调用链：`ShellDamengDataExportHandler → 具体写入器 → ShellDamengTypeFileWriter.parameterized/formatLine`

---

## ShellDamengXmlTypeFileWriter

- 职责：XML 格式导出写入器，支持“字段作为属性 / 作为子节点”两种模式。继承 `ShellDamengTypeFileWriter`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | columns | DamengColumns | 字段列表 |
  | config | DBDataExportConfig | 导出配置 |
  | writer | LineFileWriter | 行文件写入器 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengXmlTypeFileWriter(String, DBDataExportConfig, DamengColumns)` | 构造 | `LineFileWriter.create` |
  | `writeHeader()` | 写 XML 声明与 `<RECORDS>` | 固定头 |
  | `writeTrial()` | 写 `</RECORDS>` | 固定尾 |
  | `writeObject(Map)` | 写 `<RECORD>` 节点 | `isFieldToAttr()` 为真时字段拼成属性；否则拼成子元素，值为 null 时用自闭合 `<name/>` |
  | `close()` | 关闭并释放 | `IOUtil.close(writer)` |

- 调用链：`ShellDamengDataExportHandler.initWriter → ShellDamengXmlTypeFileWriter.writeHeader/writeObjects → writeLine`

---

## ShellDamengDataDumpHandler

- 职责：数据库转储（结构 + 数据导出为 SQL 脚本）处理器。继承 `DBDataDumpHandler`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbClient | ShellDamengClient | 数据库客户端 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengDataDumpHandler(ShellDamengClient, String)` | 构造 | `super(dbName, DBDialect.DAMENG)` |
  | `doDump()` | 转储总入口 | 校验 fileWriter/dumpType/dataType；`dumpType==1` 全量（表/视图/函数/过程/触发器），`==2` 单表（含数据）；写头尾并关闭；异常走 `exception()` |
  | `writeHeader()` | 写脚本头注释 | 汇总来源/目标服务器、版本、主机、模式、编码、日期等信息 |
  | `writeTail()` | 写脚本尾 | 空实现 |
  | `sortTables(List<DamengTable>)`（private） | 按外键依赖拓扑排序 | 构建“父表→子表”邻接表与入度图，Kahn BFS 排序使父表在前；查询 `selectForeignKeyTables/selectForeignKeys`；存在循环依赖时把剩余表追加到末尾 |
  | `dumpTable()` | 转储所有表 | `selectTables(full=true)` → `sortTables` → 逐表 `dumpTable(table)` |
  | `dumpTable(DamengTable)` | 转储单表结构 | 输出 DROP/建表语句，去掉模式前缀；逐列生成 `COMMENT ON COLUMN`，再写表注释；`isDumpRecord()` 时 `dumpRecord` |
  | `dumpRecord(DamengTable, DamengColumns)` | 转储表数据 | 分批 `selectRecords`（`queryLimit` 分页），`ShellDamengDataUtil.toInsertSql` 生成插入；有自增且非 AUTO_INCREMENT 时用 `SET IDENTITY_INSERT ... ON/OFF` 包裹 |
  | `dumpView()` / `dumpFunction()` / `dumpProcedure()` / `dumpTrigger()` | 转储视图/函数/过程/触发器 | 分别 `selectViews/Functions/Procedures/Triggers`，生成 DROP + 建定义（去模式前缀，`delimiter ;;` 包裹），逐个 `processedIncr` |

- 调用链：`doDump → dumpTable/dumpView/dumpFunction/dumpProcedure/dumpTrigger → dumpRecord → ShellDamengDataUtil.toInsertSql`

---

## ShellDamengDataExportHandler

- 职责：数据导出处理器，按表把查询结果写入指定格式文件。继承 `DBDataExportHandler`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbClient | ShellDamengClient | 数据库客户端 |
  | config | DBDataExportConfig | 导出配置（final） |
  | tables | List\<ShellDamengDataExportTable\> | 待导出表列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengDataExportHandler(ShellDamengClient, String)` | 构造 | `super(schema)`，`new DBDataExportConfig()` |
  | `doExport()` | 导出总入口 | 遍历 tables，逐个 `exportTable` 并 `processedIncr` |
  | `initWriter(String, DamengColumns)`（private） | 按导出类型创建写入器 | 依据 `isSqlType/isExcelType/isHtmlType/isJsonType/isXmlType/isCsvType/isTxtType` 返回对应 `ShellDamengTypeFileWriter` |
  | `exportTable(ShellDamengDataExportTable)` | 导出单表 | try-with-resources 创建 writer；`columns` 非空时分页 `selectRecords`（或直接用 `table.getRecords()`），写头→分批 `writeRecord`→写尾；遇错按 `isContinueWithError` 决定是否中断 |
  | `writeHeader/writeRecord/writeTail`（private） | 写头/写记录/写尾 | 分别委托 writer 的 `writeHeader/writeObjects/writeTrial`；`writeRecord` 会把记录 `toMap()` 后逐值 `valueStandardization` 规整 |
  | `dateFormat(String)` | 设日期格式 | 空则默认 `yyyy-MM-dd HH:mm:ss` |
  | `recordSeparator/txtIdentifier/fieldSeparator/includeFields/fieldToAttr/earlyVersion/continueWithError` | 透传导出配置 | 逐项写入 `config` |
  | `getDbClient/setDbClient/getTables/setTables/getConfig` | 基础访问器 | 构造/getter/setter 合并简写 |

- 调用链：`doExport → exportTable → initWriter → ShellDamengTypeFileWriter.writeObjects → ShellDamengDataUtil.valueStandardization`

---

## ShellDamengDataImportHandler

- 职责：数据导入处理器，把各类格式文件读入并批量插入数据库。继承 `DBDataImportHandler<String>`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbClient | ShellDamengClient | 数据库客户端（final） |
  | files | List\<ShellDamengDataImportFile\> | 待导入文件列表 |
  | config | DBDataImportConfig | 导入配置（final） |
  | insertList | List\<String\> | 待批量执行的 INSERT SQL 缓冲 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengDataImportHandler(ShellDamengClient, String)` | 构造 | `super(name)`，`new DBDataImportConfig()` |
  | `doImport()` | 导入总入口 | 遍历 files 逐个 `importRecord`，最后 `processed(files.size())` |
  | `importRecord(ShellDamengDataImportFile)` | 导入单文件 | 复制模式下先 `dbClient.clearTable`；try-with-resources 建 reader；查目标表字段；循环 `readRecords`+`writeRecord` 直到无数据；最后收尾 `doBatchInsert` |
  | `initReader(File)`（private） | 按类型创建读取器 | 依据 `isCsvType/isJsonType/isXmlType/isExcelType/isTxtType` 返回对应 `DBDataTypeFileReader` |
  | `readRecords(DBDataTypeFileReader, int)`（private） | 读取一批记录 | `reader.readObjects(count)` 转 `DamengRecord`（`putValue` 逐字段） |
  | `writeRecord(DamengColumns, List<DamengRecord>)`（private） | 生成并缓存插入 SQL | 调 `ShellDamengDataImportHelper.toInsertSql` 后 `addInsertSql` |
  | `addInsertSql(List<String>)`（private） | 追加 SQL 并在达上限时批量执行 | 缓冲累积，`size >= batchLimit` 时 `doBatchInsert` |
  | `doBatchInsert(List<String>, boolean)` | 执行批量插入 | `dbClient.insertBatch`，成功 `processedIncr`，失败 `processedDecr` 后抛出 |
  | `dateFormat/importMode/columnIndex/dataStartIndex/recordLabel/attrToColumn/recordSeparator/txtIdentifier/fieldSeparator` | 透传导入配置 | 逐项写入 `config` |
  | `getDbClient/getFiles/setFiles/getConfig` | 基础访问器 | 构造/getter/setter 合并简写 |

- 调用链：`doImport → importRecord → readRecords/writeRecord → ShellDamengDataImportHelper.toInsertSql → dbClient.insertBatch`

---

## ShellDamengDataRunSqlFileHandler

- 职责：SQL 脚本文件执行处理器，逐行解析脚本（含注释、建表/建函数等多行语句、批量插入）并执行。继承 `DBDataRunFileHandler<String>`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbClient | ShellDamengClient | 数据库客户端（final） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengDataRunSqlFileHandler(ShellDamengClient, String)` | 构造 | `super(dbName)` |
  | `runFile()` | 解析并执行 SQL 文件 | 逐行读取（UTF-8）：跳过 `-- `/`#` 行注释与 `/* */` 多行注释；`SET IDENTITY_INSERT ... ON/OFF` 控制批量插入边界；`INSERT INTO` 累积到 `addInsert`；`SET `/`DROP `/`COMMENT ON ` 直接 `executeSqlSimple`；用 `createFlag1`（表/视图）与 `createFlag2`（函数/触发器/过程，`delimiter ;` 分段）状态机聚合多行 DDL 后执行；收尾 `doBatchInsert`；异常按 `continueWithErrors` 决定继续或中断并清空缓冲 |
  | `doBatchInsert(List<String>, boolean)` | 执行批量插入 | `dbClient.insertBatch`，成功/失败分别 `processedIncr`/`processedDecr` |
  | `enableParallel()` | 是否允许并行 | 返回 `false` |

- 调用链：`runFile → dbClient.executeSqlSimple / addInsert → doBatchInsert → dbClient.insertBatch`

---

## ShellDamengDataTransportHandler

- 职责：跨库数据传输处理器，把源库的表/视图/函数/过程/触发器复制到目标库。继承 `DBDataTransportHandler<String>`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | sourceClient | ShellDamengClient | 来源客户端 |
  | targetClient | ShellDamengClient | 目标客户端 |
  | views | List\<DBDataTransportObject\> | 待传视图列表 |
  | tables | List\<DBDataTransportObject\> | 待传表列表 |
  | triggers | List\<DBDataTransportObject\> | 待传触发器列表 |
  | functions | List\<DBDataTransportObject\> | 待传函数列表 |
  | procedures | List\<DBDataTransportObject\> | 待传过程列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengDataTransportHandler()` | 构造 | `super(DBDialect.DAMENG)` |
  | `doTransport()` | 传输总入口 | 依次传输 tables/views/functions/procedures/triggers，异常 `exception()`，finally 打印结束 |
  | `transportTable(String)`（private） | 传输单表 | 目标库 DROP 表 → 源库 `showCreateTable`（去模式前缀）建表 → 分批 `selectRecords` → `ShellDamengDataUtil.toInsertSql` → `addInsert`；有自增时用 `SET IDENTITY_INSERT` 包裹；最后 `doBatchInsert` |
  | `transportView/transportFunction/transportProcedure/transportTrigger`（private） | 传输视图/函数/过程/触发器 | 目标库 DROP → 源库 `showCreateView/Function/Procedure/Trigger`（去模式前缀）→ 目标库执行创建；每步 `processedIncr` |
  | `doBatchInsert(List<String>, boolean)` | 目标库批量插入 | `targetClient.insertBatch`，成功/失败分别 `processedIncr`/`processedDecr` |
  | `getSourceClient/setSourceClient/getTargetClient/setTargetClient/getViews/setViews/getTables/setTables/getTriggers/setTriggers/getFunctions/setFunctions/getProcedures/setProcedures` | 基础访问器 | 构造/getter/setter 合并简写 |
  | `enableParallel()` | 是否允许并行 | 返回 `false` |

- 调用链：`doTransport → transportTable/transportView/... → sourceClient.showCreate* / ShellDamengDataUtil.toInsertSql → targetClient.insertBatch`

---

## ShellDamengDataExportColumnListView

- 职责：导出字段勾选列表视图，用复选框呈现字段选中态。继承 `FXListView<FXCheckBox>`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `init(List<ShellDamengDataExportColumn>)` | 按字段列表构建复选框项 | `clearItems` 后逐列 `new FXCheckBox`，设置选中态与文本（字段名），`selectedChanged` 回写 `column.setSelected`，绑定鼠标点击选中行 |

- 调用链：`init → FXCheckBox.selectedChanged → ShellDamengDataExportColumn.setSelected`

---

## ShellDamengDataExportTableTableView

- 职责：导出表表格视图，提供选中表集合与存在性判断。继承 `FXTableView<ShellDamengDataExportTable>`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getSelectedTables()` | 获取选中的导出表 | 遍历 `getItems()` 按 `isSelected()` 收集 |
  | `hasSelectedTable()` | 是否存在选中表 | 遍历命中即返回 true |

- 调用链：`ShellDamengDataExportTableTableView.getSelectedTables → ShellDamengDataExportHandler.setTables`

---

## ShellDamengDataImportFileTableView

- 职责：导入文件表格视图，纯展示用。继承 `FXTableView<ShellDamengDataImportFile>`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | （无） | 仅继承父类能力，无自定义方法 | 单元格工厂/列绑定在 FXML 或父类完成 |

- 调用链：`ShellDamengDataImportFileTableView → FXTableView（父类）`

---

## 跳过清单

以下 16 个文件整个文件均为注释（自 `//package ...` 起全部注释），属于死代码，未在正文列出：

| 文件 | 原注释中声明的类型 | 备注 |
|---|---|---|
| `file/DamengDataExportConfig.java` | `public class DamengDataExportConfig` | 导出配置模型，含 `dateFormat`/`fieldToAttr`/`includeFields`/`recordSeparator`/`fieldSeparator`/`txtIdentifier`/`charset`/`earlyVersion` 及成套 getter/setter。 |
| `file/DamengDataExportHelper.java` | `public class DamengDataExportHelper` | 导出参数化助手，含 `parameterizedForJson/Xml/Csv/Sql/Html/Xls` 与 `toExportSql/Json/Xml/Csv/Html/Xls`。注：首行 `package cn.oyzh.easyshell.data.dameng.file;` 为活代码，但其后类体全部注释，无可编译类。 |
| `file/DamengDataImportConfig.java` | `public class DamengDataImportConfig` | 导入配置模型，含 `importMode`（1 追加/2 复制）/`columnIndex`/`dataStartIndex`/`recordLabel`/`attrToColumn` 及分隔符、字符集等。 |
| `file/ShellDamengCsvTypeFileReader.java` | `public class ShellDamengCsvTypeFileReader extends ShellDamengTypeFileReader` | CSV 读取器，基于 `SkipAbleFileReader` 跳行、按逗号解析。 |
| `file/ShellDamengExcelTypeFileReader.java` | `public class ShellDamengExcelTypeFileReader extends ShellDamengTypeFileReader` | Excel 读取器，基于 POI `Workbook` 逐行读取单元格。 |
| `file/ShellDamengJsonTypeFileReader.java` | `public class ShellDamengJsonTypeFileReader extends ShellDamengTypeFileReader` | JSON 读取器，基于 fastjson2 `JSONReader`，支持纯数组与 `{"recordLabel":[...]}` 包装。 |
| `file/ShellDamengTxtTypeFileReader.java` | `public class ShellDamengTxtTypeFileReader extends ShellDamengTypeFileReader` | TXT 读取器，基于 `SkipAbleFileReader`，按 txtIdentifier/字段分隔符解析。 |
| `file/ShellDamengTypeFileReader.java` | `public abstract class ShellDamengTypeFileReader implements Closeable` | 读取器抽象基类，含 `init`/`readObject`(abstract)/`readObjects`/`parseLine`。 |
| `file/ShellDamengXmlTypeFileReader.java` | `public class ShellDamengXmlTypeFileReader extends ShellDamengTypeFileReader` | XML 读取器，基于 StAX `XMLEventReader`，支持属性作字段/子节点两模式。 |
| `ui/ShellDamengDataExportTableComboBox.java` | `public class ShellDamengDataExportTableComboBox extends FXComboBox<ShellDamengDataExportTable>` | 导出表下拉框，`initNode` 设置字符串转换器（显示表名）。 |
| `ui/ShellDamengDataTransportFunctionListView.java` | `public class ShellDamengDataTransportFunctionListView extends DBDataTransportObjectListView` | 函数传输列表视图，`of(List<DamengFunction>)` 构造传输对象。 |
| `ui/ShellDamengDataTransportProcedureListView.java` | `public class ShellDamengDataTransportProcedureListView extends DBDataTransportObjectListView` | 过程传输列表视图，`of(List<DamengProcedure>)`。 |
| `ui/ShellDamengDataTransportTableListView.java` | `public class ShellDamengDataTransportTableListView extends DBDataTransportObjectListView` | 表传输列表视图，`of(List<DamengTable>)`。 |
| `ui/ShellDamengDataTransportTriggerListView.java` | `public class ShellDamengDataTransportTriggerListView extends DBDataTransportObjectListView` | 触发器传输列表视图，`of(List<DamengTrigger>)`。 |
| `ui/ShellDamengDataTransportViewListView.java` | `public class ShellDamengDataTransportViewListView extends DBDataTransportObjectListView` | 视图传输列表视图，`of(List<DamengView>)`。 |
| `ui/ShellDataImportTableComboBox.java` | `public class ShellDataImportTableComboBox extends FXComboBox<ShellDamengDataImportFile>` | 导入文件下拉框，转换器显示 `getTableName()`，另含 `getSelectedTableName()`。 |

- 覆盖存活的类数：**20**
- 跳过的死代码文件数：**16**
- 文件总数：**36**
