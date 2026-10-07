# easyshell query 包代码审查

范围：`easyshell/src/main/java/cn/oyzh/easyshell/query/`（递归全部）。

共覆盖 47 个正式类（另有 10 个整文件被注释掉的死代码仅列于文末清单，不展开）。

本包是各数据库/中间件「查询控制台」的配套实现，按产品（dameng/mysql/mongo/redis/zk）分包，每个产品一套对称的 5 类结构：

- `XxxQueryEditor`：继承 `DBQueryEditor` 的 SQL/命令文本域，负责注释切换（`doComment`）、语法美化（dameng/mysql）、右键菜单与运行回调。
- `XxxQueryPromptItem`：继承 `DBQueryPromptItem` 的提示项，用 `type` 编码类型并提供 `isXxxType()` 判定；`wrapContent()` 负责自动补全时对内容加引号包裹。
- `XxxQueryPromptListView`：继承 `DBQueryPromptListView`，按提示项类型渲染对应 SVG 图标标签。
- `XxxQueryPromptPopup`：继承 `DBQueryPromptPopup`，绑定 token 解析器与列表视图，重写 `initPrompts`/`autoComplete`。
- `XxxQueryToken` + `XxxQueryTokenAnalyzer`：分词结果 + 分词解析器，识别当前光标处的 token 并计算候选提示词相关度。
- `XxxQueryUtil`：静态元数据索引（关键字/库/表/视图/函数/过程/字段等），异步 `updateIndex` 刷新。
- `XxxQueryResult`/`XxxExecuteResult`/`XxxExplainResult`（dameng/mysql/mongo）：查询结果模型，解析 `ResultSet` 或 `List<Record>`。

`query` 根包（`cn.oyzh.easyshell.query`）下 7 个文件已整文件注释，属死代码，无正式类。

---

# 一、通用（cn.oyzh.easyshell.query）

该包 7 个文件（`ShellQueryEditor`、`ShellQueryPromptItem`、`ShellQueryPromptListView`、`ShellQueryPromptPopup`、`ShellQueryToken`、`ShellQueryTokenAnalyzer`、`ShellQueryUtil`）整文件均为注释，无正式类，仅列于文末清单。

---

# 二、达梦（cn.oyzh.easyshell.query.dameng）

## DamengQueryResult
- 职责：达梦查询结果抽象根类，持有字段列表与行列表并提供读取辅助。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | columns | DamengColumns | 结果字段列表 |
  | records | List\<DamengRecord\> | 结果行列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `int getCount()` | 行数 | `records` 为空返回 0，否则 `records.size()` |
  | `String schema()` | 模式名 | 取首字段的 `column.getSchema()` |
  | `String tableName()` | 表名 | 取首字段的 `column.getTableName()` |
  | `DamengColumn getPrimaryKey()` | 主键字段 | 遍历 `columns` 找 `isAutoIncrement()` 者 |
  | `boolean isUpdatable()` | 是否可更新 | 遍历 `columns` 存在自增列即 true |
  | `List<DamengColumn> columnList()` | 字段列表 | `columns` 为空返回 `Collections.emptyList()` |
  | `DamengColumns getColumns()/void setColumns(DamengColumns)` | 字段存取 | 简单存取 |
  | `List<DamengRecord> getRecords()/void setRecords(List<DamengRecord>)` | 行存取 | 简单存取 |

- 调用链：`调用方 → DamengQueryResult.getCount/getPrimaryKey → columns/records`

## DamengExecuteResult
- 职责：达梦 SQL 执行结果，将 `ResultSet` 解析为字段+记录。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | fullColumn | boolean | 是否全字段 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void parseResult(ResultSet, Connection, boolean readonly)` | 解析结果集 | `ShellDamengHelper.parseColumns(resultSet)` → 逐行 `new DamengRecord(columns, readonly)` → 按下标 `resultSet.getObject` → `record.putValue`（几何转换代码已注释） |
  | `void setFullColumn(boolean)/boolean isFullColumn()` | 全字段标志存取 | 简单存取 |

- 调用链：`parseResult → ShellDamengHelper.parseColumns → DamengRecord.putValue`

## DamengExplainResult
- 职责：达梦 EXPLAIN 结果解析（无全字段标志）。
- 字段：无（继承 `columns`、`records`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void parseResult(ResultSet, Connection, boolean readonly)` | 解析结果集 | `ShellDamengHelper.parseColumns` → 逐行 `DamengRecord.putValue` |

- 调用链：`parseResult → ShellDamengHelper.parseColumns → DamengRecord.putValue`

## ShellDamengQueryEditor
- 职责：达梦 SQL 查询文本域（提示、注释切换、SQL 美化）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dialect | DBDialect | 数据库方言 |
  | promptPopup | ShellDamengQueryPromptPopup | 提示词弹窗 |
  | runCallback | Runnable | 运行回调 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBDialect getDialect()/void setDialect(DBDialect)` | 方言存取 | 简单存取 |
  | `ShellDamengQueryPromptPopup promptPopup()` | 获取提示弹窗 | 懒加载 `new ShellDamengQueryPromptPopup()` |
  | `void initNode()` | 初始化 | `super.initNode` → `promptPopup.setOnItemSelected(autoComplete)` → `setFormatType(EditorFormatType.SQL)` |
  | `void doComment()` | 注释/反注释切换 | 取选区 → `getSelectionLines` → 判断是否全部以 `-- ` 开头 → 逐行加/去 `-- ` 前缀，修正选区 |
  | `void pretty()` | SQL 美化 | `DBSqlParser.prettySql(sql, dialect)` → `setText` |
  | `List<? extends MenuItem> getMenuItems()` | 右键菜单 | 有选区时加「运行所选」`MenuItemHelper.runSelected` |
  | `void setRunCallback(Runnable)` | 设置运行回调 | 简单存取 |
  | `void run()` | 运行 | `runCallback.run()` |
  | `Font getEditorFont()` | 编辑器字体 | `FontManager.toFont(ShellSettingStore.SETTING.editorFontConfig())` |

- 调用链：`initNode → promptPopup → autoComplete`；`doComment → getSelectionLines`

## ShellDamengQueryPromptItem
- 职责：达梦提示项，用 `type` 编码类型并包裹列名。
- 字段：无（类型即父类 `type`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isDatabaseType()` | 是否库 | `1 == type` |
  | `boolean isTableType()` | 是否表 | `2 == type` |
  | `boolean isColumnType()` | 是否字段 | `3 == type` |
  | `boolean isKeywordType()` | 是否关键字 | `4 == type` |
  | `boolean isViewType()` | 是否视图 | `5 == type` |
  | `boolean isFunctionType()` | 是否函数 | `6 == type` |
  | `boolean isProcedureType()` | 是否过程 | `7 == type` |
  | `String wrapContent()` | 包裹内容 | 字段类型时 `DBUtil.wrap(content, DBDialect.DAMENG)`，否则原样 |

- 调用链：`ShellDamengQueryPromptPopup.autoComplete → item.wrapContent → DBUtil.wrap`

## ShellDamengQueryPromptListView
- 职责：达梦提示列表视图，按提示项类型渲染图标标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SVGLabel initPromptLabel(ShellDamengQueryPromptItem item)` | 渲染提示标签 | 按类型 new `DatabaseSVGGlyph`/`KeywordsSVGGlyph`/`TableSVGGlyph`/`ColumnSVGGlyph`/`ViewSVGGlyph`/`FunctionSVGGlyph`/`ProcedureSVGGlyph` → `new SVGLabel(content, glyph)`，部分 `setRealWidth(240)` |
  | `FXLabel initExtLabel(ShellDamengQueryPromptItem item)` | 渲染附加标签 | 表/视图/字段时 `new FXLabel(extContent)`，填充色 `#D3D3D3` |

- 调用链：`ShellDamengQueryPromptPopup.initListView → ShellDamengQueryPromptListView.initPromptLabel/initExtLabel`

## ShellDamengQueryPromptPopup
- 职责：达梦查询提示弹窗。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBQueryPromptListView<ShellDamengQueryPromptItem> initListView()` | 初始化列表 | `new ShellDamengQueryPromptListView()` |
  | `DBQueryTokenAnalyzer<...> tokenAnalyzer()` | 获取解析器 | `ShellDamengQueryTokenAnalyzer.INSTANCE` |
  | `void autoComplete(DBQueryEditor, ShellDamengQueryPromptItem item)` | 自动补全 | `token != null` 时 `super.replaceText(editor, item.wrapContent())` |

- 调用链：`prompt → tokenAnalyzer → initPrompts → listView.init`；`autoComplete → item.wrapContent → replaceText`

## ShellDamengQueryToken
- 职责：达梦分词结果，按分隔符判定可能的提示类型。
- 字段：无（token 及位置继承自 `DBQueryToken`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isPossibilityKeyword()` | 可能是关键字 | token 为 `' '`/`'n'`/`'\0'` |
  | `boolean isPossibilityTable()` | 可能是表 | token 为 `' '`/`'"'`/`','`/`'.'` |
  | `boolean isPossibilityView()` | 可能是视图 | 同上集合 |
  | `boolean isPossibilityFunction()` | 可能是函数 | 同上集合 |
  | `boolean isPossibilityProcedure()` | 可能是过程 | 同上集合 |
  | `boolean isPossibilityColumn()` | 可能是字段 | 同上集合 |
  | `boolean isPossibilityDatabase()` | 可能是库 | token 为 `'"'`/`' '` |

- 调用链：`ShellDamengQueryTokenAnalyzer.initPrompts → token.isPossibilityXxx`

## ShellDamengQueryTokenAnalyzer
- 职责：达梦分词解析器，从光标处切出当前 token 并产出候选提示项。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | ShellDamengQueryTokenAnalyzer | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengQueryToken currentToken(String content, int currentIndex)` | 取当前 token | 截取 `content[0, currentIndex]` → `ArrayUtil.reverse` 逆向找分隔符（`\n`/空格/`` ` ``/`.`/`,`）→ 填充 token 类型与起止索引 |
  | `List<ShellDamengQueryPromptItem> initPrompts(ShellDamengQueryToken token, float minCorr)` | 生成提示项 | 按 `isPossibilityXxx` 组装任务：`ShellDamengQueryUtil.getKeywords/getSchemas/getTables/getViews/getFunctions/getProcedures/getColumns` → `TextUtil.clacCorr` 过滤 → `ThreadUtil.submit` → 按 `correlation` 排序并 `reversed` |

- 调用链：`ShellDamengQueryPromptPopup.initPrompts → ShellDamengQueryTokenAnalyzer.initPrompts → ShellDamengQueryUtil.getXxx → TextUtil.clacCorr`

## ShellDamengQueryUtil
- 职责：达梦查询元数据索引（关键字/库/表/视图/函数/过程/字段）缓存与异步更新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | indexStatus | int | 索引状态：0 未初始化/1 初始化中/2 已初始化 |
  | DB_KEYWORDS | List\<String\> | SQL 关键字（静态块填充 dml/ddl/query/函数） |
  | DB_SCHEMAS | List\<DamengSchema\> | 模式列表 |
  | DB_TABLES | CopyOnWriteArrayList\<DamengTable\> | 表列表 |
  | DB_VIEWS | CopyOnWriteArrayList\<DamengView\> | 视图列表 |
  | DB_FUNCTIONS | CopyOnWriteArrayList\<DamengFunction\> | 函数列表 |
  | DB_PROCEDURES | CopyOnWriteArrayList\<DamengProcedure\> | 过程列表 |
  | DB_COLUMNS | CopyOnWriteArrayList\<DamengColumn\> | 字段列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static List<String> getKeywords()` | 关键字 | 返回 `DB_KEYWORDS` |
  | `static List<DamengSchema> getSchemas()` | 模式 | 返回 `DB_SCHEMAS` |
  | `static List<DamengTable> getTables()` | 表 | 返回 `DB_TABLES` |
  | `static List<DamengView> getViews()` | 视图 | 返回 `DB_VIEWS` |
  | `static List<DamengFunction> getFunctions()` | 函数 | 返回 `DB_FUNCTIONS` |
  | `static List<DamengProcedure> getProcedures()` | 过程 | 返回 `DB_PROCEDURES` |
  | `static List<DamengColumn> getColumns()` | 字段 | 返回 `DB_COLUMNS` |
  | `static void updateIndex(ShellDamengClient client)` | 刷新索引 | `indexStatus==0` 时置 1 → clear 各列表 → `client.selectSchemas` 填充库 → 对非内部库（`ShellDamengUtil.isInternalDatabase`）并行收集表/视图/函数/过程 → `ThreadUtil.submit` → 置 2；异常回置 0；整体 `ThreadUtil.start` 异步 |

- 调用链：`updateIndex → client.selectSchemas/selectTablesSimple/selectViewsSimple/selectFunctionsSimple/selectProceduresSimple`

---

# 三、MySQL（cn.oyzh.easyshell.query.mysql）

## ShellMysqlQueryResult
- 职责：MySQL 查询结果抽象根类，持有字段列表与行列表并提供读取辅助。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | columns | MysqlColumns | 结果字段列表 |
  | records | List\<MysqlRecord\> | 结果行列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `int getCount()` | 行数 | `records` 为空返回 0 |
  | `String dbName()` | 数据库名 | 取首字段 `column.getDbName()` |
  | `String tableName()` | 表名 | 取首字段 `column.getTableName()` |
  | `MysqlColumn getPrimaryKey()` | 主键 | 遍历找 `isAutoIncrement()` 字段 |
  | `boolean isUpdatable()` | 是否可更新 | 存在自增列即 true |
  | `List<MysqlColumn> columnList()` | 字段列表 | 为空返回 `Collections.emptyList()` |
  | `MysqlColumns getColumns()/void setColumns(MysqlColumns)` | 字段存取 | 简单存取 |
  | `List<MysqlRecord> getRecords()/void setRecords(List<MysqlRecord>)` | 行存取 | 简单存取 |

- 调用链：`调用方 → ShellMysqlQueryResult.getCount/dbName/tableName → columns/records`

## ShellMysqlExecuteResult
- 职责：MySQL SQL 执行结果，将 `ResultSet` 解析为字段+记录。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | fullColumn | boolean | 是否全字段 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void parseResult(ResultSet, Connection, boolean readonly)` | 解析结果集 | `ShellMysqlHelper.parseColumns` → 逐行 `new MysqlRecord(columns, readonly)` → 几何列 `ShellMysqlHelper.getGeometryString` → `record.putValue` |
  | `void setFullColumn(boolean)/boolean isFullColumn()` | 全字段标志存取 | 简单存取 |

- 调用链：`parseResult → ShellMysqlHelper.parseColumns → (几何)getGeometryString → MysqlRecord.putValue`

## ShellMysqlExplainResult
- 职责：MySQL EXPLAIN 结果解析（无全字段标志）。
- 字段：无（继承 `columns`、`records`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void parseResult(ResultSet, Connection, boolean readonly)` | 解析结果集 | `ShellMysqlHelper.parseColumns` → 逐行 `MysqlRecord.putValue` |

- 调用链：`parseResult → ShellMysqlHelper.parseColumns → MysqlRecord.putValue`

## ShellMysqlQueryEditor
- 职责：MySQL SQL 查询文本域（提示、注释切换、SQL 美化）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dialect | DBDialect | 数据库方言 |
  | promptPopup | ShellMysqlQueryPromptPopup | 提示词弹窗 |
  | runCallback | Runnable | 运行回调 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBDialect getDialect()/void setDialect(DBDialect)` | 方言存取 | 简单存取 |
  | `ShellMysqlQueryPromptPopup promptPopup()` | 获取提示弹窗 | 懒加载 `new ShellMysqlQueryPromptPopup()` |
  | `void initNode()` | 初始化 | `super.initNode` → `promptPopup.setOnItemSelected(autoComplete)` → `setFormatType(SQL)` |
  | `void doComment()` | 注释/反注释切换 | 取选区 → `getSelectionLines` → 判断是否全以 `-- ` 开头 → 逐行加/去 `-- ` 前缀并修正选区 |
  | `void pretty()` | SQL 美化 | `DBSqlParser.prettySql(sql, dialect)` → `setText` |
  | `List<? extends MenuItem> getMenuItems()` | 右键菜单 | 有选区时加「运行所选」 |
  | `void setRunCallback(Runnable)/void run()` | 运行回调 | 存取并触发 `runCallback.run()` |
  | `Font getEditorFont()` | 编辑器字体 | `FontManager.toFont(...editorFontConfig())` |

- 调用链：`initNode → promptPopup → autoComplete`；`doComment → getSelectionLines`

## ShellMysqlQueryPromptItem
- 职责：MySQL 提示项，用 `type` 编码类型并包裹列名。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isDatabaseType()` | 是否库 | `1 == type` |
  | `boolean isTableType()` | 是否表 | `2 == type` |
  | `boolean isColumnType()` | 是否字段 | `3 == type` |
  | `boolean isKeywordType()` | 是否关键字 | `4 == type` |
  | `boolean isViewType()` | 是否视图 | `5 == type` |
  | `boolean isFunctionType()` | 是否函数 | `6 == type` |
  | `boolean isProcedureType()` | 是否过程 | `7 == type` |
  | `String wrapContent()` | 包裹内容 | 字段类型时 `DBUtil.wrap(content, DBDialect.MYSQL)`，否则原样 |

- 调用链：`ShellMysqlQueryPromptPopup.autoComplete → item.wrapContent → DBUtil.wrap`

## ShellMysqlQueryPromptListView
- 职责：MySQL 提示列表视图，按提示项类型渲染图标标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SVGLabel initPromptLabel(ShellMysqlQueryPromptItem item)` | 渲染提示标签 | 按类型 new `DatabaseSVGGlyph`/`KeywordsSVGGlyph`/`TableSVGGlyph`/`ColumnSVGGlyph`/`ViewSVGGlyph`/`FunctionSVGGlyph`/`ProcedureSVGGlyph` → `new SVGLabel(content, glyph)` |
  | `FXLabel initExtLabel(ShellMysqlQueryPromptItem item)` | 渲染附加标签 | 表/视图/字段时 `new FXLabel(extContent)`，填充色 `#D3D3D3` |

- 调用链：`ShellMysqlQueryPromptPopup.initListView → ShellMysqlQueryPromptListView.initPromptLabel/initExtLabel`

## ShellMysqlQueryPromptPopup
- 职责：MySQL 查询提示弹窗。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBQueryPromptListView<ShellMysqlQueryPromptItem> initListView()` | 初始化列表 | `new ShellMysqlQueryPromptListView()` |
  | `DBQueryTokenAnalyzer<...> tokenAnalyzer()` | 获取解析器 | `ShellMysqlQueryTokenAnalyzer.INSTANCE` |
  | `void autoComplete(DBQueryEditor, ShellMysqlQueryPromptItem item)` | 自动补全 | `token != null` 时 `super.replaceText(editor, item.wrapContent())` |

- 调用链：`prompt → tokenAnalyzer → initPrompts → listView.init`；`autoComplete → item.wrapContent → replaceText`

## ShellMysqlQueryToken
- 职责：MySQL 分词结果，按分隔符判定可能的提示类型。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isPossibilityKeyword()` | 可能是关键字 | token 为 `' '`/`'\n'`/`'\0'` |
  | `boolean isPossibilityTable()` | 可能是表 | token 为 `' '`/`` '`' ``/`','`/`'.'` |
  | `boolean isPossibilityView()` | 可能是视图 | 同上集合 |
  | `boolean isPossibilityFunction()` | 可能是函数 | 同上集合 |
  | `boolean isPossibilityProcedure()` | 可能是过程 | 同上集合 |
  | `boolean isPossibilityColumn()` | 可能是字段 | 同上集合 |
  | `boolean isPossibilityDatabase()` | 可能是库 | token 为 `` '`' ``/`' '` |

- 调用链：`ShellMysqlQueryTokenAnalyzer.initPrompts → token.isPossibilityXxx`

## ShellMysqlQueryTokenAnalyzer
- 职责：MySQL 分词解析器，从光标处切出 token 并产出候选提示项。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | ShellMysqlQueryTokenAnalyzer | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlQueryToken currentToken(String content, int currentIndex)` | 取当前 token | 截取 → `ArrayUtil.reverse` 逆向找分隔符（`\n`/空格/`` ` ``/`.`/`,`）→ 填充类型与起止索引 |
  | `List<ShellMysqlQueryPromptItem> initPrompts(ShellMysqlQueryToken token, float minCorr)` | 生成提示项 | 按 `isPossibilityXxx` 组装任务：`ShellMysqlQueryUtil.getKeywords/getDatabases/getTables/getViews/getFunctions/getProcedures/getColumns` → `TextUtil.clacCorr` 过滤 → `ThreadUtil.submit` → 按 `correlation` 排序并 `reversed` |

- 调用链：`ShellMysqlQueryPromptPopup.initPrompts → ShellMysqlQueryTokenAnalyzer.initPrompts → ShellMysqlQueryUtil.getXxx → TextUtil.clacCorr`

## ShellMysqlQueryUtil
- 职责：MySQL 查询元数据索引（关键字/库/表/视图/函数/过程/字段）缓存与异步更新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | indexStatus | int | 索引状态：0 未初始化/1 初始化中/2 已初始化 |
  | DB_KEYWORDS | List\<String\> | SQL 关键字（静态块填充） |
  | DB_DATABASES | List\<MysqlDatabase\> | 数据库列表 |
  | DB_TABLES | CopyOnWriteArrayList\<MysqlTable\> | 表列表 |
  | DB_VIEWS | CopyOnWriteArrayList\<MysqlView\> | 视图列表 |
  | DB_FUNCTIONS | CopyOnWriteArrayList\<MysqlFunction\> | 函数列表 |
  | DB_PROCEDURES | CopyOnWriteArrayList\<MysqlProcedure\> | 过程列表 |
  | DB_COLUMNS | CopyOnWriteArrayList\<MysqlColumn\> | 字段列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static List<String> getKeywords()` | 关键字 | 返回 `DB_KEYWORDS` |
  | `static List<MysqlDatabase> getDatabases()` | 数据库 | 返回 `DB_DATABASES` |
  | `static List<MysqlTable> getTables()` | 表 | 返回 `DB_TABLES` |
  | `static List<MysqlView> getViews()` | 视图 | 返回 `DB_VIEWS` |
  | `static List<MysqlFunction> getFunctions()` | 函数 | 返回 `DB_FUNCTIONS` |
  | `static List<MysqlProcedure> getProcedures()` | 过程 | 返回 `DB_PROCEDURES` |
  | `static List<MysqlColumn> getColumns()` | 字段 | 返回 `DB_COLUMNS` |
  | `static void updateIndex(ShellMysqlClient client)` | 刷新索引 | `indexStatus==0` 时置 1 → clear 列表 → `client.databases` 填库 → 对非内部库并行收集表/视图/函数/过程 → `ThreadUtil.submit` → 置 2；异常回置 0；字段索引更新代码已注释 |

- 调用链：`updateIndex → client.databases/selectTablesSimple/selectViewsSimple/selectFunctionsSimple/selectProceduresSimple`

---

# 四、MongoDB（cn.oyzh.easyshell.query.mongo）

## ShellMongoQueryResult
- 职责：MongoDB 查询结果抽象根类，持有字段列表与行列表并提供读取辅助。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | columns | MongoColumns | 结果字段列表 |
  | records | List\<MongoRecord\> | 结果行列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `int getCount()` | 行数 | `records` 为空返回 0 |
  | `void parseResult(List<MongoRecord> records)` | 解析行 | 存 `records`，`ShellMongoRecordUtil.columns(records)` 推导字段 |
  | `String dbName()` | 数据库名 | 取首字段 `column.getDbName()` |
  | `String collectionName()` | 集合名 | 取首字段 `column.getCollectionName()` |
  | `MongoColumn getPrimaryKey()` | 主键 | 找 `column.is_id()` 字段 |
  | `boolean isUpdatable()` | 是否可更新 | 存在 `is_id()` 列即 true |
  | `List<MongoColumn> columnList()` | 字段列表 | 为空返回 `Collections.emptyList()` |
  | `MongoColumns getColumns()/void setColumns(MongoColumns)` | 字段存取 | 简单存取 |
  | `List<MongoRecord> getRecords()/void setRecords(List<MongoRecord>)` | 行存取 | 简单存取 |

- 调用链：`parseResult → ShellMongoRecordUtil.columns`

## ShellMongoExecuteResult
- 职责：MongoDB 执行结果（`ResultSet` 版本为空实现）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void parseResult(ResultSet, Connection, boolean readonly)` | 解析结果集 | 空实现（Mongo 走 `parseResult(List<MongoRecord>)` 路径） |

- 调用链：`parseResult（空）`

## ShellMongoQueryEditor
- 职责：MongoDB 查询文本域（提示、`//` 注释切换）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | promptPopup | ShellMongoQueryPromptPopup | 提示词弹窗 |
  | runCallback | Runnable | 运行回调 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoQueryPromptPopup promptPopup()` | 获取提示弹窗 | 懒加载 `new ShellMongoQueryPromptPopup()` |
  | `void initNode()` | 初始化 | `setFormatType(SQL)` → `promptPopup.setOnItemSelected(autoComplete)` → `super.initNode` |
  | `void doComment()` | 注释/反注释切换 | 逐行加/去 `// ` 前缀（用 `//` 而非 `--`） |
  | `List<? extends MenuItem> getMenuItems()` | 右键菜单 | 有选区时加「运行所选」 |
  | `Set<String> getPrompts()` | 提示符集合 | 首次 `ShellMongoQueryUtil.getKeywords/getCollections/getFunctions` 合并后 `setPrompts` |
  | `void setRunCallback(Runnable)/void run()` | 运行回调 | 存取并触发 |
  | `Font getEditorFont()` | 编辑器字体 | `FontManager.toFont(...)` |

- 调用链：`initNode → promptPopup → autoComplete`；`getPrompts → ShellMongoQueryUtil.getKeywords/getCollections/getFunctions`

## ShellMongoQueryPromptItem
- 职责：MongoDB 提示项，用 `type` 编码类型。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isFunctionType()` | 是否函数 | `2 == type` |
  | `boolean isCollectionType()` | 是否集合 | `1 == type` |
  | `boolean isKeywordType()` | 是否关键字 | `4 == type` |

- 调用链：`ShellMongoQueryTokenAnalyzer.initPrompts → item.setType(1/2/4)`

## ShellMongoQueryPromptListView
- 职责：MongoDB 提示列表视图，按类型渲染图标（无附加标签）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SVGLabel initPromptLabel(ShellMongoQueryPromptItem item)` | 渲染提示标签 | 关键字 `KeywordsSVGGlyph`、集合 `TableSVGGlyph`、函数 `FunctionSVGGlyph`，均 `setColor(Color.BLACK)` |
  | `FXLabel initExtLabel(ShellMongoQueryPromptItem item)` | 附加标签 | 恒返回 `null` |

- 调用链：`ShellMongoQueryPromptPopup.initListView → ShellMongoQueryPromptListView.initPromptLabel`

## ShellMongoQueryPromptPopup
- 职责：MongoDB 查询提示弹窗。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBQueryPromptListView<ShellMongoQueryPromptItem> initListView()` | 初始化列表 | `new ShellMongoQueryPromptListView()` |
  | `DBQueryTokenAnalyzer<...> tokenAnalyzer()` | 获取解析器 | `ShellMongoQueryTokenAnalyzer.INSTANCE` |
  | `boolean tokenAvailable()` | token 是否可用 | token 非空且（可能关键字/可能函数/非空） |

- 调用链：`prompt → tokenAnalyzer → initPrompts → listView.init`

## ShellMongoQueryToken
- 职责：MongoDB 分词结果，按分隔符判定可能类型（类内大量旧字段/方法已注释）。
- 字段：无（token/位置继承自 `DBQueryToken`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isPossibilityKeyword()` | 可能是关键字 | token 为空格/空白字符/`'\n'` |
  | `boolean isPossibilityFunction()` | 可能是函数 | token 为 `'.'` |
  | `boolean isPossibilityCollection()` | 可能是集合 | token 为 `'"'`/`'\''` |

- 调用链：`ShellMongoQueryTokenAnalyzer.initPrompts → token.isPossibilityXxx`

## ShellMongoQueryTokenAnalyzer
- 职责：MongoDB 分词解析器，从光标处切出 token 并产出候选提示项。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | ShellMongoQueryTokenAnalyzer | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoQueryToken currentToken(String content, int currentIndex)` | 取当前 token | 截取 → `ArrayUtil.reverse` 逆向找分隔符（`\n`/空格/`.`/`"`/`'`）→ 填充类型与起止索引 |
  | `List<ShellMongoQueryPromptItem> initPrompts(ShellMongoQueryToken token, float minCorr)` | 生成提示项 | 关键字 `ShellMongoQueryUtil.getKeywords`（type 4）、集合 `getCollections`（type 1）、函数 `getFunctions`（type 2，内容加 `()`）→ `TextUtil.clacCorr` 过滤（空文本 corr=1）→ `ThreadUtil.submit` → 按相关度排序并 `reversed` |

- 调用链：`ShellMongoQueryPromptPopup.initPrompts → ShellMongoQueryTokenAnalyzer.initPrompts → ShellMongoQueryUtil.getKeywords/getCollections/getFunctions`

## ShellMongoQueryUtil
- 职责：MongoDB 查询关键字/函数/集合缓存与异步更新。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | indexStatus | int | 索引状态：0 未初始化/1 初始化中/2 已初始化 |
  | DB_KEYWORDS | Set\<String\> | 关键字（静态块含 `db`） |
  | DB_FUNCTIONS | Set\<String\> | 函数（`MongoScriptUtil` 的库/集合函数） |
  | DB_COLLECTIONS | List\<String\> | 集合列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static Set<String> getKeywords()` | 关键字 | 返回 `DB_KEYWORDS` |
  | `static Set<String> getFunctions()` | 函数 | 返回 `DB_FUNCTIONS` |
  | `static List<String> getCollections()` | 集合 | 返回 `DB_COLLECTIONS` |
  | `static void updateIndex(ShellMongoClient client, String dbName)` | 刷新集合索引 | `indexStatus==0` 时置 1 → clear → `client.listCollectionNames(dbName)` → 置 2；异常回置 0；`ThreadUtil.start` 异步 |

- 调用链：`updateIndex → ShellMongoClient.listCollectionNames`

---

# 五、Redis（cn.oyzh.easyshell.query.redis）

## ShellRedisQueryEditor
- 职责：Redis 命令查询文本域（提示、客户端与 db 索引持有）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbIndex | int | Redis 库索引 |
  | client | ShellRedisClient | Redis 客户端 |
  | promptPopup | ShellRedisQueryPromptPopup | 提示组件 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisClient getClient()/void setClient(ShellRedisClient)` | 客户端存取 | 简单存取 |
  | `int getDbIndex()/void setDbIndex(int)` | 库索引存取 | 简单存取 |
  | `ShellRedisQueryPromptPopup promptPopup()` | 获取提示弹窗 | 懒加载 |
  | `Set<String> getPrompts()` | 提示符集合 | 首次 `ShellRedisQueryUtil.getKeywords/getParams` 合并后 `setPrompts` |
  | `void initNode()` | 初始化 | `promptPopup.setOnItemSelected(autoComplete)` → `super.initNode` |
  | `Font getEditorFont()` | 编辑器字体 | `FontManager.toFont(...)` |

- 调用链：`initNode → promptPopup → autoComplete`；`getPrompts → ShellRedisQueryUtil.getKeywords/getParams`

## ShellRedisQueryParam
- 职责：Redis 命令参数模型，按空格拆分命令与参数。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbIndex | int | 库索引 |
  | content | String | 原始命令内容 |
  | params | List\<String\> | 拆分后的参数列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `int getDbIndex()/void setDbIndex(int)` | 库索引存取 | 简单存取 |
  | `String getContent()` | 内容 | 返回 `content` |
  | `List<String> getParams()/void setParams(List<String>)` | 参数存取 | 简单存取 |
  | `void setContent(String content)` | 设置内容并解析 | `content.trim().split(" ")` → 过滤空白项填充 `params` |
  | `String getCommand()` | 命令 | `params.getFirst()` |

- 调用链：`setContent → params 拆分；getCommand → params.getFirst`

## ShellRedisQueryPromptItem
- 职责：Redis 提示项，用 `type` 编码类型。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isKeywordType()` | 是否关键字 | `1 == type` |
  | `boolean isParamType()` | 是否参数 | `2 == type` |
  | `boolean isKeyType()` | 是否键 | `3 == type` |

- 调用链：`ShellRedisQueryTokenAnalyzer.initPrompts → item.setType(1/2/3)`

## ShellRedisQueryPromptListView
- 职责：Redis 提示列表视图，按类型渲染图标（无附加标签）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SVGLabel initPromptLabel(ShellRedisQueryPromptItem item)` | 渲染提示标签 | 关键字 `KeywordsSVGGlyph`、参数 `ParamSVGGlyph`、键 `KeysSVGGlyph` |
  | `FXLabel initExtLabel(ShellRedisQueryPromptItem item)` | 附加标签 | 恒返回 `null` |

- 调用链：`ShellRedisQueryPromptPopup.initListView → ShellRedisQueryPromptListView.initPromptLabel`

## ShellRedisQueryPromptPopup
- 职责：Redis 查询提示弹窗，按命令动态扫描键作为候选。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbIndex | Integer | 库索引 |
  | redisClient | ShellRedisClient | Redis 客户端 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBQueryPromptListView<ShellRedisQueryPromptItem> initListView()` | 初始化列表 | `new ShellRedisQueryPromptListView()` |
  | `boolean initPrompts(ShellRedisQueryToken token)` | 初始化提示 | 可能为键时 `ShellRedisKeyUtil.scanKeys(dbIndex, redisClient, "*", 30)` → `ShellRedisQueryUtil.setKeys`，否则清空键 → `tokenAnalyzer().initPrompts(token, 0.5f)` → `listView.init` |
  | `DBQueryTokenAnalyzer<...> tokenAnalyzer()` | 获取解析器 | `ShellRedisQueryTokenAnalyzer.INSTANCE` |
  | `void prompt(DBQueryEditor, KeyEvent event)` | 弹窗处理 | 编辑器为 `ShellRedisQueryEditor` 时取 `dbIndex`/`client` → `super.prompt` |
  | `boolean tokenAvailable()` | token 是否可用 | token 非空且（可能参数/非空） |

- 调用链：`prompt → 取 editor 的 dbIndex/client → initPrompts → ShellRedisKeyUtil.scanKeys → setKeys`

## ShellRedisQueryResult
- 职责：Redis 命令执行结果模型（耗时/结果/消息/成功标志）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | cost | long | 耗时（毫秒） |
  | result | Object | 执行结果 |
  | message | String | 消息 |
  | success | boolean | 是否成功 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `long getCost()/void setCost(long)` | 耗时存取 | 简单存取 |
  | `Object getResult()/void setResult(Object)` | 结果存取 | 简单存取 |
  | `String getMessage()/void setMessage(String)` | 消息存取 | 简单存取 |
  | `boolean isSuccess()/void setSuccess(boolean)` | 成功标志存取 | 简单存取 |
  | `String costSeconds()` | 耗时秒字符串 | `String.format("%.2f"+秒, cost/1000.0)` |
  | `boolean hasData()` | 是否有数据 | `result != null` |

- 调用链：`调用方 → setCost/setResult/setSuccess → costSeconds/hasData`

## ShellRedisQueryToken
- 职责：Redis 分词结果，判定关键字/参数/键，并识别键相关命令。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | input | String | 原始输入 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getInput()/void setInput(String)` | 输入存取 | 简单存取 |
  | `boolean isPossibilityKeyword()` | 可能是关键字 | token 为 null 或 `' '` |
  | `boolean isPossibilityParam()` | 可能是参数 | token 为 `' '` |
  | `boolean isPossibilityKey()` | 可能是键 | token 为空格、input 中空格数 ≤1 且以某键命令（`ShellRedisQueryUtil.keyCommands()`）开头 |

- 调用链：`ShellRedisQueryTokenAnalyzer.initPrompts → token.isPossibilityXxx`；`isPossibilityKey → keyCommands`

## ShellRedisQueryTokenAnalyzer
- 职责：Redis 分词解析器，从光标处切出 token 并产出候选提示项。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | ShellRedisQueryTokenAnalyzer | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisQueryToken currentToken(String input, int currentIndex)` | 取当前 token | 截取 → 含空格或 `-` 时逆向找到空格作分隔（遇 `\n` 返回 null）→ 填充 `input/token/起止索引` |
  | `List<ShellRedisQueryPromptItem> initPrompts(ShellRedisQueryToken token, float minCorr)` | 生成提示项 | 关键字 `getKeywords`（type 1）、参数 `getParams`（type 2）、键 `getKeys`（type 3，空文本放行）→ `TextUtil.clacCorr` 过滤 → `ThreadUtil.submit` → 排序并 `reversed` |

- 调用链：`ShellRedisQueryPromptPopup.initPrompts → ShellRedisQueryTokenAnalyzer.initPrompts → ShellRedisQueryUtil.getKeywords/getParams/getKeys`

## ShellRedisQueryUtil
- 职责：Redis 关键字/参数/键缓存，提供键相关命令清单。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | KEYWORDS | Set\<String\> | 关键字（由 jedis `Protocol` 各枚举命令/关键字填充） |
  | PARAMS | Set\<String\> | 参数（`Protocol` 各类 Keyword 枚举） |
  | KEYS | Set\<String\> | 键（由弹窗动态扫描填充） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static Set<String> getKeywords()` | 关键字 | 返回 `KEYWORDS` |
  | `static Set<String> getParams()` | 参数 | 返回 `PARAMS` |
  | `static Set<String> getKeys()` | 键 | 返回 `KEYS` |
  | `static void setKeys(Collection<String> keys)` | 设置键 | clear 后选择性 `addAll` |
  | `static List<Protocol.Command> keyCommands()` | 键相关命令 | 汇总 key/string/bit/hash/list/set/zset/geo/hyperlog/stream/other 各命令数组 |

- 调用链：`ShellRedisQueryToken.isPossibilityKey → keyCommands`；`ShellRedisQueryPromptPopup.initPrompts → setKeys`

---

# 六、ZooKeeper（cn.oyzh.easyshell.query.zk）

## ShellZKQueryEditor
- 职责：ZooKeeper 命令查询文本域（提示、客户端持有）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | client | ShellZKClient | ZK 客户端 |
  | promptPopup | ShellZKQueryPromptPopup | 提示组件 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellZKClient getClient()/void setClient(ShellZKClient)` | 客户端存取 | 简单存取 |
  | `ShellZKQueryPromptPopup promptPopup()` | 获取提示弹窗 | 懒加载 |
  | `Set<String> getPrompts()` | 提示符集合 | 首次 `ShellZKQueryUtil.getKeywords/getParams` 合并后 `setPrompts` |
  | `void initNode()` | 初始化 | `promptPopup.setOnItemSelected(autoComplete)` → `super.initNode` |
  | `Font getEditorFont()` | 编辑器字体 | `FontManager.toFont(...)` |

- 调用链：`initNode → promptPopup → autoComplete`；`getPrompts → ShellZKQueryUtil.getKeywords/getParams`

## ShellZKQueryParam
- 职责：ZooKeeper 命令参数解析模型（识别命令、路径、数据、选项与 ACL）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | content | String | 原始命令行 |
  | params | List\<String\> | 拆分后的参数列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getContent()` | 内容 | 返回 `content` |
  | `void setContent(String content)` | 设置内容并解析 | `content.trim().split(" ")` → 过滤空白项填充 `params` |
  | `boolean isLs()/isGetEphemerals()/isGetAllChildrenNumber()/isWhoami()/isSrvr()/isMntr()/isEnvi()/isConf()/isCons()/isRuok()/isCrst()/isSrst()/isStat4()/isWchc()/isWchs()/isWchp()/isDump()/isReqs()/isDirs()/isLs2()/isGet()/isSet()/isCreate()/isSync()/isGetACL()/isStat()/isSetQuota()/isListquota()/isRmr()/isDeleteall()/isDelete()/isSetACL()` | 命令判定 | 各以 `String.equalsIgnoreCase(getCommand())` 与命令名比较 |
  | `String getPath()` | 提取路径 | 按命令及 `-s`/`-c`/`-e` 等选项跳过位置参数，返回路径参数（`create`/`setAcl`/`setquota` 走特殊循环） |
  | `String getData()` | 提取数据 | `set` 取末位参数；`create` 跳过选项与路径后的参数 |
  | `long getParamB()` | 取 `-b` 值 | 扫描 `-b` 后的参数 `Long.parseLong`，缺省 -1 |
  | `int getParamN()` | 取 `-n` 值 | 扫描 `-n` 后参数，缺省 -1 |
  | `int getParamV()` | 取 `-v` 值 | 扫描 `-v` 后参数，缺省 -1 |
  | `CreateMode getCreateMode()` | 节点创建模式 | 依 `-e`/`-c`/`-s` 返回 CONTAINER/EPHEMERAL(_SEQUENTIAL)/PERSISTENT(_SEQUENTIAL) |
  | `List<ACL> getACL()` | 解析 ACL | `create`/`setAcl` 时取含 `:` 的参数 `ShellZKACLUtil.parseAcl`，默认 `ShellZKACLUtil.OPEN_ACL` |
  | `boolean hasParamStat()` | 是否含 stat 选项 | `ls2`/`stat` 恒 true，否则含 `-s` |
  | `String getCommand()` | 命令 | `CollectionUtil.getFirst(params)` |

- 调用链：`setContent → params`；`getPath/getData/getCreateMode/getACL → isXxx 系列 + params 定位`

## ShellZKQueryPromptItem
- 职责：ZK 提示项，用 `type` 编码类型。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isKeywordType()` | 是否关键字 | `1 == type` |
  | `boolean isParamType()` | 是否参数 | `2 == type` |
  | `boolean isNodeType()` | 是否节点 | `3 == type` |

- 调用链：`ShellZKQueryTokenAnalyzer.initPrompts → item.setType(1/2/3)`

## ShellZKQueryPromptListView
- 职责：ZK 提示列表视图，按类型渲染图标（无附加标签）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SVGLabel initPromptLabel(ShellZKQueryPromptItem item)` | 渲染提示标签 | 关键字 `KeywordsSVGGlyph`、节点 `SVGGlyph("/font/zk/file-text.svg")`、参数 `ParamSVGGlyph` |
  | `FXLabel initExtLabel(ShellZKQueryPromptItem item)` | 附加标签 | 恒返回 `null` |

- 调用链：`ShellZKQueryPromptPopup.initListView → ShellZKQueryPromptListView.initPromptLabel`

## ShellZKQueryPromptPopup
- 职责：ZK 查询提示弹窗，按路径动态拉取子节点作为候选。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | zkClient | ShellZKClient | ZK 客户端 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBQueryPromptListView<ShellZKQueryPromptItem> initListView()` | 初始化列表 | `new ShellZKQueryPromptListView()` |
  | `boolean initPrompts(ShellZKQueryToken token)` | 初始化提示 | 可能为节点时取 `token.getPath()`，非空则 `zkClient.getChildren(path)` → `ShellZKNodeUtil.concatPath` 拼全路径 → `ShellZKQueryUtil.setNodes`，否则清空 → `tokenAnalyzer().initPrompts(token, 0.5f)` → `listView.init` |
  | `DBQueryTokenAnalyzer<...> tokenAnalyzer()` | 获取解析器 | `ShellZKQueryTokenAnalyzer.INSTANCE` |
  | `void prompt(DBQueryEditor, KeyEvent event)` | 弹窗处理 | 编辑器为 `ShellZKQueryEditor` 时取 `zkClient` → `super.prompt` |
  | `boolean tokenAvailable()` | token 是否可用 | token 非空且（可能参数/非空） |

- 调用链：`prompt → 取 editor 的 zkClient → initPrompts → ShellZKClient.getChildren → ShellZKNodeUtil.concatPath → setNodes`

## ShellZKQueryResult
- 职责：ZK 命令执行结果模型，携带原始结果并支持按类型强转。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | cost | long | 耗时（毫秒） |
  | stat | Stat | 节点状态 |
  | result | Object | 执行结果 |
  | message | String | 消息 |
  | success | boolean | 是否成功 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String costSeconds()` | 耗时秒字符串 | `String.format("%.2f "+秒, cost/1000.0)` |
  | `byte[] asData()` | 结果转字节数组 | `(byte[]) result` |
  | `Integer asCount()` | 结果转计数 | `(Integer) result` |
  | `List<ClientInfo> asClientInfo()` | 结果转客户端信息 | `(List<ClientInfo>) result` |
  | `List<ShellZKEnvNode> asEnvInfo()` | 结果转环境信息 | `(List<ShellZKEnvNode>) result` |
  | `StatsTrack asQuota()` | 结果转配额 | `(StatsTrack) result` |
  | `List<ACL> asACL()` | 结果转 ACL | `(List<ACL>) result` |
  | `List<String> asNode()` | 结果转节点列表 | `(List<String>) result` |
  | `long getCost()/void setCost(long)` 等 | 字段存取 | `cost/stat/result/message/success` 简单存取 |

- 调用链：`调用方 → asData/asCount/asACL/... → result 强转`

## ShellZKQueryToken
- 职责：ZK 分词结果，判定关键字/节点/参数并推导父路径。
- 字段：无（token/位置继承自 `DBQueryToken`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isPossibilityKeyword()` | 可能是关键字 | token 为 null |
  | `boolean isPossibilityNode()` | 可能是节点 | token 为 `' '` 且内容非空 |
  | `boolean isPossibilityParam()` | 可能是参数 | token 为 `'-'` |
  | `String getPath()` | 父路径 | 内容以 `/` 开头时 `ShellZKNodeUtil.getParentPath(content)`，否则 null |

- 调用链：`ShellZKQueryPromptPopup.initPrompts → token.isPossibilityNode/getPath`；`ShellZKQueryTokenAnalyzer.initPrompts → token.isPossibilityXxx`

## ShellZKQueryTokenAnalyzer
- 职责：ZK 分词解析器，从光标处切出 token 并产出候选提示项。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | INSTANCE | ShellZKQueryTokenAnalyzer | 单例 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellZKQueryToken currentToken(String input, int currentIndex)` | 取当前 token | 截取 → 含空格或 `-` 时逆向找空格或 `-` 作分隔（遇 `\n` 返回 null）→ 填充起止索引与内容 |
  | `List<ShellZKQueryPromptItem> initPrompts(ShellZKQueryToken token, float minCorr)` | 生成提示项 | 关键字 `getKeywords`（type 1）、参数 `getParams`（type 2，corr 恒 1）、节点 `getNodes`（type 3）→ `TextUtil.clacCorr` 过滤 → `ThreadUtil.submit` → 排序并 `reversed` |

- 调用链：`ShellZKQueryPromptPopup.initPrompts → ShellZKQueryTokenAnalyzer.initPrompts → ShellZKQueryUtil.getKeywords/getParams/getNodes`

## ShellZKQueryUtil
- 职责：ZK 关键字/参数/节点缓存。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | NODES | Set\<String\> | 节点列表（弹窗动态填充） |
  | KEYWORDS | Set\<String\> | 命令关键字（静态块：数据/节点/权限/子节点/配额/其他） |
  | PARAMS | Set\<String\> | 选项参数（`-s`/`-e`/`-c`/`-n`/`-b`/`-v`） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static Set<String> getKeywords()` | 关键字 | 返回 `KEYWORDS` |
  | `static Set<String> getNodes()` | 节点 | 返回 `NODES` |
  | `static Set<String> getParams()` | 参数 | 返回 `PARAMS` |
  | `static void setNodes(Collection<String> nodes)` | 设置节点 | clear 后选择性 `addAll` |

- 调用链：`ShellZKQueryPromptPopup.initPrompts → setNodes`；`ShellZKQueryTokenAnalyzer.initPrompts → getKeywords/getParams/getNodes`

---

# 附：整文件被注释掉的死代码清单（不展开）

以下 10 个文件整文件均为注释、无真实代码：

| 文件 | 说明 |
|---|---|
| `query/ShellQueryEditor.java` | 通用查询编辑器（旧实现，已被各 `XxxQueryEditor` 取代） |
| `query/ShellQueryPromptItem.java` | 通用提示项 |
| `query/ShellQueryPromptListView.java` | 通用提示列表视图 |
| `query/ShellQueryPromptPopup.java` | 通用提示弹窗 |
| `query/ShellQueryToken.java` | 通用 token |
| `query/ShellQueryTokenAnalyzer.java` | 通用 token 解析器 |
| `query/ShellQueryUtil.java` | 通用工具类（含被注释的 `clacCorr`） |
| `query/dameng/DamengQueryResults.java` | 达梦多结果容器（`package` 行亦被注释） |
| `query/mysql/ShellMysqlQueryResults.java` | MySQL 多结果容器 |
| `query/mongo/ShellMongoQueryResults.java` | MongoDB 多结果容器 |

> 上述根包 7 个类被各产品子包的同类实现整体替代；3 个 `XxxQueryResults` 为通用多结果容器，功能已由外部 `DBQueryResults` 等承担。
