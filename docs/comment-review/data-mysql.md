# 数据模块 · mysql 包

> 范围：`cn/oyzh/easyshell/data/mysql/`（含 `config/`、`dto/`、`file/`、`handler/`、`ui/` 子目录），递归共 41 个 `.java` 文件。
> 本包承担 MySQL 的「数据导出 / 导入 / 转储 / 导入 SQL 文件 / 库间传输」四类数据搬运能力：`file/` 提供多格式读写器，`handler/` 是各操作的执行主体，`dto/` 承载界面数据，`ui/` 为若干 JavaFX 列表/表格控件，根目录 `ShellMysqlDataImportHelper` 负责把记录转成 `INSERT` 语句。
> 其中 **21 个文件为整文件被注释掉的死代码**（`//package ...` 起全部注释，含全部 `config/`、`dto/*Transport*`、各类型 `*TypeFileReader`、`ui/` 传输列表视图等），依据「整文件被注释掉的死代码不列出」的规则，正文仅展开 **20 个存活类**，死代码文件在文末“跳过清单”逐文件登记。

---

**一、根目录**

## ShellMysqlDataImportHelper

- 职责：MySQL 数据导入的静态工具类，负责字段值的类型化参数处理，并将记录批量转换为 `INSERT INTO` 语句。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无） | | 纯静态工具类，无实例字段 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `static Object parameterized(MysqlColumn, Object, DBDataImportConfig)` | 对单个字段值做参数化 | null 或空串直接返回 null；日期列（`isDateType`）按 `config.getDateFormat()` 解析为 `Date` 后再格式化为 `yyyy-MM-dd HH:mm:ss`；`supportTimestamp` 时对 `CharSequence`/`Date` 同样格式化；`supportString` 时经 `TextUtil.escape` 转义 |
| `static List<String> toInsertSql(MysqlColumns, List<MysqlRecord>, DBDataImportConfig)` | 记录转插入 SQL 列表 | 逐记录拼接 `INSERT INTO <表>(<列...>) VALUES (<值...>)`；列名与字符串值分别经 `DBUtil.wrap`/`DBUtil.wrapData`（方言 MYSQL）；取值先 `DBUtil.unwrapData` 解包，再交给 `parameterized`；末尾调试 `System.out.println(sql)` |

- 调用链：`ShellMysqlDataImportHandler.writeRecord → ShellMysqlDataImportHelper.toInsertSql → parameterized → DBUtil.wrapData`

---

**二、dto 子包**

## ShellMysqlDataExportColumn

- 职责：导出字段模型，在 `MysqlColumn` 基础上增加「是否选中」标记，供导出界面勾选列。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `selected` | `boolean` | 是否被选中，默认 `true` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `isSelected()` / `setSelected(boolean)` | 读取/设置选中状态 | 直接读写 `selected` |

- 调用链：`ShellMysqlDataExportTable.columns(...) → ShellMysqlDataExportColumn.copy → (界面) ShellMysqlDataExportColumnListView.init`

## ShellMysqlDataExportTable

- 职责：导出表模型，实现 `DBName`，承载表名、记录、字段列表，以及「文件路径、是否选中、扩展后缀」三个 JavaFX 属性及其配套界面控件。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `name` | `String` | 表名称 |
| `records` | `List<MysqlRecord>` | 预置记录（查询导出场景用） |
| `columns` | `List<ShellMysqlDataExportColumn>` | 字段列表 |
| `filePathProperty` | `StringProperty` | 导出文件路径属性 |
| `selectedProperty` | `BooleanProperty` | 是否选中属性 |
| `extensionProperty` | `ObjectProperty<FileExtensionFilter>` | 导出文件扩展后缀属性 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `selectedProperty()` | 懒加载选中属性 | 创建时监听：选中且无文件路径时调用 `updateFilePath()` |
| `isSelected()` / `setSelected(boolean)` | 选中状态读写 | 委托 `selectedProperty` |
| `getSelectedControl()` | 生成选中复选框控件 | 双向同步 `FXCheckBox` 与 `selectedProperty`（用 `AtomicBoolean ignoreChanged` 防回环），并绑定行点击选中 |
| `filePathProperty()` / `getFilePath()` / `setFilePath(String)` | 文件路径属性/读写 | 懒加载 `SimpleStringProperty` |
| `getFilePathControl()` | 生成保存文件文本框 | 关联扩展后缀与初始文件名；选择文件后回填路径；监听路径/后缀变化刷新控件；绑定 Ctrl+S 与行点击 |
| `extensionProperty()` / `getExtension()` / `setExtension(FileExtensionFilter)` | 扩展后缀属性/读写 | 懒加载，变更时触发 `updateFilePath()` |
| `fileName()`（private） | 生成默认文件名 | 表名 + 扩展后缀（去掉后缀首个 `.`） |
| `columns(List<? extends MysqlColumn>)` | 由字段列表重建导出列 | 逐个 `ShellMysqlDataExportColumn.copy` 后收入 `columns` |
| `columns()` | 返回字段副本 | 复制为新 `ArrayList` |
| `selectedColumns()` | 获取选中字段 | 遍历 `columns` 收集 `isSelected()` 者 |
| `selectedColumnNames()` | 获取选中字段名 | 对 `selectedColumns()` 取 `getName()` |
| `hasColumns()` | 是否有字段 | `CollectionUtil.isNotEmpty(columns)` |
| `updateFilePath()`（private） | 更新默认文件路径 | 选中或已有路径时设为「桌面目录 + 文件名」 |
| `getName()` / `setName(String)`（override `DBName`） | 表名读写 | 直接读写 `name` |
| `getColumns()` / `setColumns(List<ShellMysqlDataExportColumn>)` | 字段列表读写 | 直接读写 |
| `getRecords()` / `setRecords(List<MysqlRecord>)` | 记录列表读写 | 直接读写 |

- 调用链：`ShellMysqlDataExportHandler.exportTable → table.selectedColumns() → ShellMysqlTypeFileWriter.writeObjects`

## ShellMysqlDataImportFile

- 职责：导入文件模型，持有数据库名、客户端、文件属性与目标表名，并提供文件选择、目标表选择控件及表名推断。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `dbName` | `String` | 数据库名称 |
| `dbClient` | `ShellMysqlClient` | 数据库客户端 |
| `fileProperty` | `ObjectProperty<File>` | 导入文件属性 |
| `targetTableName` | `String` | 目标表名称（空则取文件名推断） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `setDbName(String)` / `setDbClient(ShellMysqlClient)` | 设置数据库名/客户端 | 直接赋值 |
| `fileProperty()` / `getFile()` / `setFile(File)` | 文件属性/读写 | 懒加载 `SimpleObjectProperty` |
| `getFilePath()` / `getFileName()` | 文件路径/文件名 | 从 `getFile()` 派生，null 安全 |
| `getFilePathControl()` | 生成文件选择文本框 | `ChooseFileTextField`，选文件回填；监听文件属性变化刷新文本 |
| `getTargetTableControl()` | 生成目标表下拉框 | `ShellMysqlTableComboBox`，`StageManager.showMask` 内 `init(dbName, tableName, dbClient)`；选中变化写回 `targetTableName` |
| `getTableName()` | 推断表名 | 取文件名去掉最后一个 `.` 之后的部分 |
| `getTargetTableName()` | 获取目标表名 | 未显式设置时回退 `getTableName()` |
| `setTargetTableName(String)` | 设置目标表名 | 直接赋值 |

- 调用链：`ShellMysqlDataImportHandler.importRecord → file.getTargetTableName / getFile → dbClient.selectColumns / clearTable`

---

**三、file 子包（多格式文件读写器）**

## ShellMysqlTypeFileWriter（抽象）

- 职责：所有导出文件写入器的抽象基类，统一「字段值参数化」与「分隔符行格式化」逻辑。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无） | | 抽象基类，无自有字段（子类各自持有 columns/config/writer） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `init()` | 初始化钩子 | 默认空实现 |
| `parameterized(MysqlColumn, Object, DBDataExportConfig)` | 通用字段值参数化 | 几何类型包 `ST_GeomFromText(...)`；日期/时间戳按 `config.getDateFormat()` 格式化；JSON 转义；二进制转 `0x...` 十六进制；BIT 转 `b'...'`；枚举/整型/数字直接返回，其余 `toString()` |
| `writeHeader()` / `writeTrial()` | 头部/尾部钩子 | 默认空实现，子类覆写 |
| `writeObject(Map<String,Object>)`（抽象） | 写单条对象 | 子类实现 |
| `writeObjects(List<Map<String,Object>>)` | 写多条对象 | 遍历调用 `writeObject` |
| `formatLine(Object[], String, String, String)` | 数组→行 | null 转 `""` 后转调列表版 |
| `formatLine(List<?>, String, String, String)` | 列表→行 | 每值包「字段分隔符 + 文本标识符」，行尾追加记录分隔符，返回去掉首个分隔符的子串 |

- 调用链：`ShellMysqlDataExportHandler.initWriter → (CSV/TXT/... Writer).writeObjects → formatLine / parameterized`

## ShellMysqlCsvTypeFileWriter

- 职责：CSV 格式导出写入器；以逗号固定分隔字段。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `columns` | `MysqlColumns` | 字段列表 |
| `config` | `DBDataExportConfig` | 导出配置 |
| `writer` | `LineFileWriter` | 行文件写入器（final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlCsvTypeFileWriter(String, DBDataExportConfig, MysqlColumns)` | 构造 | `LineFileWriter.create(filePath, config.getCharset())` |
| `writeHeader()` | 写表头 | `formatLine(columns.columnNames(), ",", 文本标识符, 记录分隔符)` |
| `writeObject(Map<String,Object>)` | 写一行数据 | 按列下标定位、逐列 `parameterized` 后 `formatLine` 写出 |
| `close()` | 关闭 | 关闭 writer 并置空 config/columns |

- 调用链：`ShellMysqlDataExportHandler.exportTable → writeHeader/writeObjects → ShellMysqlCsvTypeFileWriter.formatLine`

## ShellMysqlExcelTypeFileWriter

- 职责：Excel（xls/xlsx）导出写入器；用 POI 工作簿按类型分派单元格赋值。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `columns` | `MysqlColumns` | 字段列表 |
| `config` | `DBDataExportConfig` | 导出配置 |
| `workbook` | `Workbook` | POI 工作簿 |
| `xlsRowIndex` | `int` | 当前数据行索引，初始 1 |
| `filePath` | `String` | 目标文件路径 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlExcelTypeFileWriter(String, DBDataExportConfig, MysqlColumns)` | 构造 | 依据 `.xlsx` 后缀 `WorkbookHelper.create(isXlsx)` 建工作簿 |
| `writeHeader()` | 写表头并落盘 | 重置行索引；`createSheet(表名)`；按 `sortOfPosition()` 写列名行；`WorkbookHelper.write` 落盘 |
| `writeObject(Map, boolean)`（private） | 写一行（可选刷新） | 逐列 `parameterized`；`switch` 按 `Date/Double/String/Boolean/Calendar/LocalDate/LocalDateTime/Number` 分派 `setCellValue`；`flush` 为真时落盘 |
| `writeObject(Map)`（override） | 写单行 | 转调 `writeObject(object, true)` |
| `writeObjects(List<Map>)`（override） | 写多行 | 逐行 `writeObject(object, false)`，末尾统一落盘一次 |
| `close()` | 关闭 | 关闭工作簿并置空各字段 |

- 调用链：`ShellMysqlDataExportHandler.exportTable → writeHeader/writeRecord(writeObjects) → WorkbookHelper.write`

## ShellMysqlHtmlTypeFileWriter

- 职责：HTML 表格导出写入器；内嵌 CSS 样式，输出 `<table>`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `columns` | `MysqlColumns` | 字段列表 |
| `config` | `DBDataExportConfig` | 导出配置 |
| `writer` | `LineFileWriter` | 行文件写入器 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlHtmlTypeFileWriter(String, DBDataExportConfig, MysqlColumns)` | 构造 | 创建 `LineFileWriter` |
| `writeHeader()` | 写 HTML 头与表头行 | 文本块输出 `<!DOCTYPE html>...<table>`，再按 `sortOfPosition()` 生成 `<th>` 行，`writeLine` 写出 |
| `writeTrial()` | 写 HTML 尾 | 文本块输出 `</table></body></html>` |
| `writeObject(Map)` | 写一行 | 逐列 `parameterized` 后拼 `<td>...</td>`，`writeLine` 写出 |
| `close()` | 关闭 | 关闭并置空 writer/config/columns |

- 调用链：`ShellMysqlDataExportHandler.exportTable → writeHeader/writeTail/writeObjects → ShellMysqlHtmlTypeFileWriter.writeLine`

## ShellMysqlJsonTypeFileWriter

- 职责：JSON 导出写入器；支持「早期版本」（`{"RECORDS":[...]}` 包装）与纯数组两种结构。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `columns` | `MysqlColumns` | 字段列表 |
| `config` | `DBDataExportConfig` | 导出配置 |
| `writer` | `LineFileWriter` | 行文件写入器 |
| `firstWrite` | `boolean` | 是否首次写入，初始 `true`（控制逗号分隔） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlJsonTypeFileWriter(String, DBDataExportConfig, MysqlColumns)` | 构造 | 创建 `LineFileWriter` |
| `writeHeader()` | 写头 | `earlyVersion` 时输出 `{` + `"RECORDS": [`，否则输出 `[` |
| `writeTrial()` | 写尾 | `earlyVersion` 时输出 `\n]}`，否则输出 `\n]` |
| `writeObject(Map)` | 写一条 JSON 对象 | 非首条先写 `,\n`；逐键 `parameterized`，`Number` 裸写、null 写 `null`、其余加引号；键值间按剩余数量补逗号；置 `firstWrite=false` |
| `close()` | 关闭 | 关闭并置空 writer/config/columns |

- 调用链：`ShellMysqlDataExportHandler.exportTable → writeHeader/writeObjects/writeTail → ShellMysqlJsonTypeFileWriter.writeObject`

## ShellMysqlSqlTypeFileWriter

- 职责：SQL 导出写入器；生成 `INSERT INTO ... VALUES (...)` 语句，并覆写更贴合 SQL 语法的 `parameterized`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `columns` | `MysqlColumns` | 字段列表 |
| `config` | `DBDataExportConfig` | 导出配置 |
| `writer` | `LineFileWriter` | 行文件写入器（final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlSqlTypeFileWriter(String, DBDataExportConfig, MysqlColumns)` | 构造 | 创建 `LineFileWriter` |
| `writeObject(Map)` | 生成并写出一条 INSERT | 拼 `INSERT INTO <表>`；`isIncludeFields` 时按 `sortOfPosition()` 拼列名（去尾逗号）；再逐列取 `object.get`、`parameterized` 拼值；去尾逗号补 `);` 后 `writeLine` |
| `close()` | 关闭 | 关闭并置空 config/columns |
| `parameterized(MysqlColumn, Object, DBDataExportConfig)`（override） | SQL 值参数化 | null → `NULL`；几何 `ST_GeomFromText`；日期/时间戳加单引号；二进制 `0x...`（空→`NULL`）；BIT `b'...'`（空→`NULL`）；其余交 `DBUtil.wrapData`（MQSQL 方言） |

- 调用链：`ShellMysqlDataExportHandler.initWriter（isSqlType）→ ShellMysqlSqlTypeFileWriter.writeObject → DBUtil.wrapData`

## ShellMysqlTxtTypeFileWriter

- 职责：纯文本导出写入器；字段/文本/记录分隔符全部取自配置（可自定义）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `columns` | `MysqlColumns` | 字段列表 |
| `config` | `DBDataExportConfig` | 导出配置 |
| `writer` | `LineFileWriter` | 行文件写入器 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlTxtTypeFileWriter(String, DBDataExportConfig, MysqlColumns)` | 构造 | 创建 `LineFileWriter` |
| `writeHeader()` | 写表头 | 以配置的字段/文本/记录分隔符 `formatLine(columns.columnNames())` |
| `writeObject(Map)` | 写一行 | 逐列 `parameterized` 后按配置分隔符 `formatLine` |
| `close()` | 关闭 | 关闭并置空 writer/config/columns |

- 调用链：`ShellMysqlDataExportHandler.exportTable → writeHeader/writeObjects → ShellMysqlTxtTypeFileWriter.formatLine`

## ShellMysqlXmlTypeFileWriter

- 职责：XML 导出写入器；支持「字段作属性」（`<RECORD a=".." />`）与「字段作子节点」（`<RECORD><a>..</a></RECORD>`）两种形态。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `columns` | `MysqlColumns` | 字段列表 |
| `config` | `DBDataExportConfig` | 导出配置 |
| `writer` | `LineFileWriter` | 行文件写入器 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlXmlTypeFileWriter(String, DBDataExportConfig, MysqlColumns)` | 构造 | 创建 `LineFileWriter` |
| `writeHeader()` | 写 XML 声明与根节点 | 输出 `<?xml ...?>` 与 `<RECORDS>` |
| `writeTrial()` | 写根节点闭合 | 输出 `</RECORDS>` |
| `writeObject(Map)` | 写一条 RECORD | `isFieldToAttr` 时拼属性式 `<RECORD k="v" .../>`；否则逐键拼子节点，null 值自闭合 `<k/>`，非 null `<k>v</k>` |
| `close()` | 关闭 | `IOUtil.close(writer)` 并置空 config/columns |

- 调用链：`ShellMysqlDataExportHandler.exportTable → writeHeader/writeTail/writeObjects → ShellMysqlXmlTypeFileWriter.writeObject`

---

**四、handler 子包（执行主体）**

## ShellMysqlDataDumpHandler

- 职责：转储处理器；将库的「表结构 + 表记录 + 视图/函数/过程/触发器/事件」导出为可执行 SQL 脚本。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `dbClient` | `ShellMysqlClient` | 数据库客户端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlDataDumpHandler(ShellMysqlClient, String)` | 构造 | `super(dbName, DBDialect.MYSQL)` |
| `doDump()`（override） | 转储主流程 | 校验 `fileWriter/dumpType/dataType`；`writeHeader` → `dumpType==1` 时依次转储表/视图/函数/过程/触发器/事件，`dumpType==2` 时仅转储指定表 → `writeTail` + `close`；全程 `message` 上报 |
| `dumpTable()` | 转储全部表 | `selectTables` 后逐表 `dumpTable(table)` + `processedIncr` |
| `dumpTable(MysqlTable)` | 转储单表 | 拼注释、`DROP TABLE`、建表语句后 `appendLines`；`isDumpRecord()` 时调 `dumpRecord` |
| `dumpRecord(String)` | 分页转储表记录 | 循环 `MysqlSelectRecordParam`（只读、`queryLimit`）→ `selectRecords` → `ShellMysqlDataUtil.toInsertSql` → `appendLines`；记录查询/写入耗时 |
| `dumpView()` / `dumpFunction()` / `dumpProcedure()` / `dumpTrigger()` / `dumpEvent()` | 转储各类对象 | 分别 `selectViews/Functions/Procedures/Triggers/Events`；拼注释 + `DROP ... IF EXISTS` + 创建语句（函数/过程/触发器/事件额外用 `delimiter ;;` 包裹）后 `appendLines` |
| `writeHeader()`（override） | 写脚本头 | 输出 `Project` 名、源/目标服务器信息、版本、主机、Schema、编码、时间；再 `SET NAMES`、`SET FOREIGN_KEY_CHECKS = 0` |
| `writeTail()`（override） | 写脚本尾 | 恢复 `SET FOREIGN_KEY_CHECKS = 1` |

- 调用链：`doDump → dumpTable → dumpRecord → ShellMysqlDataUtil.toInsertSql → dbClient.selectRecords`

## ShellMysqlDataExportHandler

- 职责：数据导出处理器；按导出类型挑选格式写入器，分页查询记录并写出，配置项以链式 setter 暴露。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `dbClient` | `ShellMysqlClient` | 数据库客户端 |
| `config` | `DBDataExportConfig` | 导出配置（final，构造内新建） |
| `tables` | `List<ShellMysqlDataExportTable>` | 待导出的表 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlDataExportHandler(ShellMysqlClient, String)` | 构造 | `super(dbName)`；`new DBDataExportConfig()` |
| `doExport()`（override） | 导出主流程 | 遍历 `tables` → `exportTable` + `processedIncr` |
| `initWriter(String, MysqlColumns)`（private） | 依类型建写入器 | 按 `isSqlType/isExcelType/isHtmlType/isJsonType/isXmlType/isCsvType/isTxtType` 依次返回对应 `ShellMysqlTypeFileWriter`，无匹配返回 null |
| `exportTable(ShellMysqlDataExportTable)` | 导出单表 | try-with-resources 建 writer；`writeHeader`；`columns` 非空时循环分页 `selectRecords`（或直接用 `table.getRecords()`）→ `writeRecord`；单轮异常按 `isContinueWithError` 决定 `exception` 后继续或抛出；末尾 `writeTail`；记录查询/写入耗时 |
| `writeHeader(...)` / `writeTail(...)`（private） | 转发头/尾 | 分别调 `writer.writeHeader()` / `writer.writeTrial()` |
| `writeRecord(...)`（private） | 写记录批次 | 记录 `toMap()` 收集后 `writer.writeObjects` |
| `dateFormat(String)` | 设置日期格式 | 空则用 `yyyy-MM-dd HH:mm:ss` |
| `recordSeparator/txtIdentifier/fieldSeparator/includeFields/fieldToAttr/earlyVersion/continueWithError(...)` | 各配置项 setter | 逐项写入 `config` |
| `getDbClient()` / `setDbClient(...)` | 客户端读写 | — |
| `getTables()` / `setTables(...)` | 表列表读写 | — |
| `getConfig()` | 获取导出配置 | 返回 `config` |

- 调用链：`doExport → exportTable → initWriter → ShellMysqlTypeFileWriter.writeObjects`

## ShellMysqlDataImportHandler

- 职责：数据导入处理器；按类型挑选文件读取器，读取记录转 INSERT 后批量插入目标表。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `dbClient` | `ShellMysqlClient` | 数据库客户端（final） |
| `files` | `List<ShellMysqlDataImportFile>` | 待导入文件 |
| `config` | `DBDataImportConfig` | 导入配置（final，构造内新建） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlDataImportHandler(ShellMysqlClient, String)` | 构造 | `super(dbName)`；`new DBDataImportConfig()` |
| `doImport()`（override） | 导入主流程 | 遍历 `files` → `checkInterrupt` + `importRecord`；末尾 `processed(files.size())` |
| `importRecord(ShellMysqlDataImportFile)` | 导入单文件 | 取 `getTargetTableName`；`isCopyMode()` 时先 `dbClient.clearTable`；try-with-resources 建 reader；取目标表字段，循环 `readRecords(readLimit)` → `writeRecord`（转 INSERT 入队）；末尾 `doBatchInsert`；finally 清 `insertList` |
| `initReader(File)`（private） | 依类型建读取器 | 按 `isCsvType/isJsonType/isXmlType/isExcelType/isTxtType` 返回 `DBDataCsvTypeFileReader` 等 |
| `readRecords(DBDataTypeFileReader, int)`（private） | 批量读记录 | `reader.readObjects(count)` 后逐 Map 构造 `MysqlRecord` 并 `putValue` |
| `writeRecord(MysqlColumns, List<MysqlRecord>)`（private） | 记录转 INSERT 入队 | `ShellMysqlDataImportHelper.toInsertSql` → `addInsert` |
| `doBatchInsert(List<String>, boolean)`（override） | 批量插入 | `dbClient.insertBatch(this.name, list, parallel)`，成功 `processedIncr`，失败 `processedDecr` 后重抛 |
| `dateFormat/importMode/columnIndex/dataStartIndex/recordLabel/attrToColumn/recordSeparator/txtIdentifier/fieldSeparator(...)` | 各配置项 setter | 逐项写入 `config` |
| `getFiles()` / `setFiles(...)` | 文件列表读写 | — |
| `getConfig()` | 获取导入配置 | 返回 `config` |

- 调用链：`doImport → importRecord → readRecords → writeRecord → ShellMysqlDataImportHelper.toInsertSql → doBatchInsert → dbClient.insertBatch`

## ShellMysqlDataRunSqlFileHandler

- 职责：执行 SQL 脚本文件处理器；逐行解析、跳过注释、识别多行语句边界，并按 `INSERT` 批量、其余单条执行。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `dbClient` | `ShellMysqlClient` | 数据库客户端（final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlDataRunSqlFileHandler(ShellMysqlClient, String)` | 构造 | `super(dbName)` |
| `runFile()`（override） | 执行脚本主循环 | 逐行读取，跳过 `-- `、`#` 单行注释与 `/* */` 多行注释；非建对象状态下 `INSERT INTO` 走 `addInsert` 批量，`SET `/`DROP ` 走 `dbClient.executeSqlSimple`；用 `createFlag1`（表/视图多行）、`createFlag2`（函数/过程/触发器/事件，`delimiter ;` 界定）跟踪拼接；单条异常 `exception` + `processedDecr`，`continueWithErrors` 为假则中断；末尾 `doBatchInsert` |
| `doBatchInsert(List<String>, boolean)`（override） | 批量插入 | 同导入处理器：`insertBatch` + `processedIncr`，失败 `processedDecr` 重抛 |

- 调用链：`runFile → dbClient.executeSqlSimple（单条）/ addInsert（批量）→ doBatchInsert → dbClient.insertBatch`

## ShellMysqlDataTransportHandler

- 职责：库间传输处理器；把源库的表（结构 + 数据）及视图/函数/过程/触发器/事件迁移到目标库。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| `sourceClient` | `ShellMysqlClient` | 来源客户端 |
| `targetClient` | `ShellMysqlClient` | 目标客户端 |
| `views` | `List<DBDataTransportObject>` | 待传输视图 |
| `tables` | `List<DBDataTransportObject>` | 待传输表 |
| `triggers` | `List<DBDataTransportObject>` | 待传输触发器 |
| `functions` | `List<DBDataTransportObject>` | 待传输函数 |
| `procedures` | `List<DBDataTransportObject>` | 待传输过程 |
| `events` | `List<DBDataTransportObject>` | 待传输事件 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellMysqlDataTransportHandler()` | 构造 | `super(DBDialect.MYSQL)` |
| `doTransport()`（override） | 传输主流程 | 目标库关/开外键检查；依次传输 表→视图→函数→过程→触发器→事件；异常 `exception` 汇总，finally `message` 结束 |
| `transportTable(String)` | 传输单表 | 目标库 `DROP TABLE IF EXISTS` → 源库 `showCreateTable` 到目标库 → 分页 `selectRecords` 取数据，`ShellMysqlDataUtil.toInsertSql` 入队 → `doBatchInsert` |
| `transportView/Function/Procedure/Trigger/Event(String)` | 传输其它对象 | 各自 `DROP ... IF EXISTS` 后由源库 `showCreateView/Function/Procedure/Trigger/Event` 取定义并到目标库执行 |
| `doBatchInsert(List<String>, boolean)`（override） | 目标库批量插入 | `targetClient.insertBatch(targetDatabase, list, parallel)` + `processedIncr` |
| `getSourceClient/setSourceClient`、`getTargetClient/setTargetClient` | 客户端读写 | — |
| `getViews/setViews`...（views/tables/triggers/functions/procedures/events 各一对） | 各类对象列表读写 | — |

- 调用链：`doTransport → transportTable → sourceClient.selectRecords → ShellMysqlDataUtil.toInsertSql → doBatchInsert → targetClient.insertBatch`

---

**五、ui 子包**

## ShellMysqlDataExportColumnListView

- 职责：导出字段勾选列表视图；把字段渲染成复选框列表。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无） | | 仅继承 `FXListView<FXCheckBox>` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `init(List<ShellMysqlDataExportColumn>)` | 初始化列表项 | 清空后逐列建 `FXCheckBox`（选中态、文本取列名），勾选变化回写 `column.setSelected`，绑定行点击选中，加入列表 |

- 调用链：`(导出界面) ShellMysqlDataExportColumnListView.init → ShellMysqlDataExportColumn.setSelected`

## ShellMysqlDataExportTableTableView

- 职责：导出表表格视图；提供选中表的查询与判断。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无） | | 仅继承 `FXTableView<ShellMysqlDataExportTable>` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getSelectedTables()` | 获取选中表 | 遍历 `getItems()` 收集 `isSelected()` 者 |
| `hasSelectedTable()` | 是否有选中表 | 遍历命中即返回 true，否则 false |

- 调用链：`(导出界面) ShellMysqlDataExportTableTableView.getSelectedTables → ShellMysqlDataExportHandler.setTables`

## ShellMysqlDataImportFileTableView

- 职责：导入文件表格视图（当前为空壳类）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| （无） | | 类体已全部被注释，无任何成员 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| （无） | | 无方法，仅保留类声明 `extends FXTableView<ShellMysqlDataImportFile>` |

- 调用链：`ShellMysqlDataImportFileTableView → FXTableView<ShellMysqlDataImportFile>`（无扩展逻辑）

---

## 跳过清单

以下 21 个文件**整文件均被注释**（自 `//package ...` 起，package、import、类体全部注释），属死代码，未在正文展开，仅登记其注释中声明的类型：

| 文件 | 原注释中声明的类型 | 备注 |
|---|---|---|
| `config/ShellMysqlDataExportConfig.java` | `public class ShellMysqlDataExportConfig` | 导出配置模型（日期格式、记录/字段分隔符、文本标识符、字符集、`fieldToAttr`、`includeFields`、`earlyVersion` 等属性及 getter/setter）。现导出配置已改用 `cn.oyzh.fx.db.data.dto.DBDataExportConfig`。 |
| `config/ShellMysqlDataImportConfig.java` | `public class ShellMysqlDataImportConfig` | 导入配置模型（导入模式 `importMode`、列索引、数据起始索引、`recordLabel`、`attrToColumn`、分隔符、字符集等，含 `isAppendMode/isCopyMode/fieldSeparatorChar/txtIdentifierChar`）。现导入配置已改用 `DBDataImportConfig`。 |
| `dto/ShellMysqlDataTransportEvent.java` | `public class ShellMysqlDataTransportEvent` | 事件传输对象（`name` + `selected`）。已被 `DBDataTransportObject` 取代。 |
| `dto/ShellMysqlDataTransportFunction.java` | `public class ShellMysqlDataTransportFunction` | 函数传输对象（`name` + `selected`），同上被取代。 |
| `dto/ShellMysqlDataTransportProcedure.java` | `public class ShellMysqlDataTransportProcedure` | 过程传输对象（`name` + `selected`），同上被取代。 |
| `dto/ShellMysqlDataTransportTable.java` | `public class ShellMysqlDataTransportTable` | 表传输对象（`name` + `selected`），同上被取代。 |
| `dto/ShellMysqlDataTransportTrigger.java` | `public class ShellMysqlDataTransportTrigger` | 触发器传输对象（`name` + `selected`），同上被取代。 |
| `dto/ShellMysqlDataTransportView.java` | `public class ShellMysqlDataTransportView` | 视图传输对象（`name` + `selected`），同上被取代。 |
| `file/ShellMysqlTypeFileReader.java` | `public abstract class ShellMysqlTypeFileReader implements Closeable` | 类型文件读取器框架抽象类（`readObject`/`readObjects`、`parseLine` 解析带文本标识符的行）。现读取器已改用框架 `DBDataTypeFileReader` 系列。 |
| `file/ShellMysqlCsvTypeFileReader.java` | `public class ShellMysqlCsvTypeFileReader extends ShellMysqlTypeFileReader` | CSV 读取器（`SkipAbleFileReader` 跳行、`parseLine` 以逗号分隔表头与数据）。 |
| `file/ShellMysqlExcelTypeFileReader.java` | `public class ShellMysqlExcelTypeFileReader extends ShellMysqlTypeFileReader` | Excel 读取器（POI 工作簿，按 `CellType` 取布尔/日期/数值/字符串）。 |
| `file/ShellMysqlJsonTypeFileReader.java` | `public class ShellMysqlJsonTypeFileReader extends ShellMysqlTypeFileReader` | JSON 读取器（fastjson2 `JSONReader`，支持纯数组与 `{recordLabel:[...]}` 包装两种格式）。 |
| `file/ShellMysqlTxtTypeFileReader.java` | `public class ShellMysqlTxtTypeFileReader extends ShellMysqlTypeFileReader` | 文本读取器（可按配置自定义记录分隔符，表头与数据用配置的字段分隔符解析）。 |
| `file/ShellMysqlXmlTypeFileReader.java` | `public class ShellMysqlXmlTypeFileReader extends ShellMysqlTypeFileReader` | XML 读取器（StAX `XMLEventReader`，支持属性作字段 / 子节点作字段两种模式）。 |
| `ui/ShellMysqlDataExportTableComboBox.java` | `public class ShellMysqlDataExportTableComboBox extends FXComboBox<ShellMysqlDataExportTable>` | 导出表下拉框，覆写 `initNode` 设置以表名显示项的转换器。 |
| `ui/ShellMysqlDataTransportEventListView.java` | `public class ShellMysqlDataTransportEventListView extends DBDataTransportObjectListView` | 事件传输列表视图，`of(List<MysqlEvent>)` 转换对象后 `init`。 |
| `ui/ShellMysqlDataTransportFunctionListView.java` | `public class ShellMysqlDataTransportFunctionListView extends DBDataTransportObjectListView` | 函数传输列表视图，`of(List<MysqlFunction>)`。 |
| `ui/ShellMysqlDataTransportProcedureListView.java` | `public class ShellMysqlDataTransportProcedureListView extends DBDataTransportObjectListView` | 过程传输列表视图，`of(List<MysqlProcedure>)`。 |
| `ui/ShellMysqlDataTransportTableListView.java` | `public class ShellMysqlDataTransportTableListView extends DBDataTransportObjectListView` | 表传输列表视图，`of(List<MysqlTable>)`。 |
| `ui/ShellMysqlDataTransportTriggerListView.java` | `public class ShellMysqlDataTransportTriggerListView extends DBDataTransportObjectListView` | 触发器传输列表视图，`of(List<MysqlTrigger>)`。 |
| `ui/ShellMysqlDataTransportViewListView.java` | `public class ShellMysqlDataTransportViewListView extends DBDataTransportObjectListView` | 视图传输列表视图，`of(List<MysqlView>)`。 |

- 覆盖存活的类数：**20**
- 跳过的死代码文件数：**21**
- 说明：`ui/ShellMysqlDataImportFileTableView.java` 类声明行存活但类体已全部注释，按存活类登记（无字段无方法）。
