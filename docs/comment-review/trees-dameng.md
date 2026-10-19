# 代码审查文档 · trees/dameng（达梦数据库树）

> 范围：`src/main/java/cn/oyzh/easyshell/trees/dameng/` 共 29 个类。
> 说明：由 `ShellDamengTreeView` 初始化根节点 `ShellDamengRootTreeItem`（模式层级），模式节点懒加载表/视图/函数/过程/查询/终端五个"类型节点"，类型节点再懒加载对应叶子节点。多数叶子节点为薄封装，业务逻辑下沉到 `ShellDamengClient`（数据 CRUD）、`ShellDamengViewFactory`（弹窗）、`ShellDamengEventUtil`（事件）。

---

## ShellDamengTreeItem
- 职责：达梦数据库树所有节点的抽象基类，统一收窄 `getTreeView()` 返回类型。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengTreeItem(RichTreeView treeView)` | 构造节点，透传树视图给父类 | `super(treeView)` |
  | `ShellDamengTreeView getTreeView()` | 重写以返回达梦树视图 | `(ShellDamengTreeView) super.getTreeView()` |

- 调用链：继承 `RichTreeItem<V>`；被全部达梦树节点继承。

## ShellDamengTreeView
- 职责：达梦数据库资源树视图，持有客户端并初始化根节点。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `client` | `ShellDamengClient` | 达梦数据库客户端 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setClient(ShellDamengClient)` / `getClient()` | 设置/获取客户端 | 简单存取 |
  | `ShellDamengTreeItemFilter getItemFilter()` | 懒初始化过滤器 | `new ShellDamengTreeItemFilter()`，缓存于 `itemFilter` |
  | `ShellDamengTreeView()` | 构造视图 | 设置 `dragContent="db_tree_drag"`、单选模式、`RichTreeCell` 工厂；`super.setRoot(new ShellDamengRootTreeItem(this))`，随后 `root().expend()` |
  | `ShellDamengRootTreeItem root()` | 收窄根节点类型 | `(ShellDamengRootTreeItem) super.root()` |

- 调用链：`ShellDamengTreeView → ShellDamengRootTreeItem → ShellDamengSchemaTreeItem`

## ShellDamengTreeItemFilter
- 职责：达梦树节点的关键字过滤判定。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean test(RichTreeItem<?> item)` | 判断节点是否命中关键字 | 类型节点（视图/表/查询/模式/函数/过程）恒返回 true 不参与过滤；对 `ShellDamengTreeItem<?>` 取 `value.name()` 调 `TextUtil.findText(...)` 与 `MatchText.NOT_FOUND` 比较；其余返回 true |

- 调用链：继承 `RichTreeItemFilter`；使用 `TextUtil.findText`。**注意**：白名单中引入了 `ShellMysqlRootTreeItem`（MySQL 根节点）而非 `ShellDamengRootTreeItem`，应为复制粘贴遗留，达梦根节点未被排除。

## ShellDamengRootTreeItem
- 职责：达梦树叶的根节点，承载模式（schema）层级的增删改查与加载。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengRootTreeItem(ShellDamengTreeView)` | 构造根节点 | `setValue(new ShellDamengRootTreeItemValue())` |
  | `ShellDamengClient client()` | 获取客户端 | `getTreeView().getClient()` |
  | `ShellConnect connect()` | 获取连接信息 | `client().getShellConnect()` |
  | `existSchema(String)` / `createSchema(DamengSchema)` / `alterSchema(DamengSchema)` / `dropSchema(String)` | 模式存在性/创建/修改/删除 | 委托 `client()` 同名方法 |
  | `addSchema()` | 弹出新增模式窗口 | `ShellDamengViewFactory.addSchema(this)`，取 `databaseName` 后 `addDatabase` |
  | `addDatabase(String)` | 追加模式节点 | `client().schema(name)` 后 `addChild(new ShellDamengSchemaTreeItem(...))` |
  | `reloadChild()` | 重载 | `clearChild()` + `loadChild()` |
  | `loadChild()` | 加载全部模式 | `client().selectSchemas()` 构造子节点，`setChild` 后 `expend/doFilter/doSort` |
  | `clearChild()` | 清空子节点前关闭模式 | 遍历子节点调 `ShellDamengSchemaTreeItem.closeDB()`，再 `super.clearChild()` |
  | `getMenuItems()` | 右键菜单 | `MenuItemHelper.addSchema` / `reloadSchema` |

- 调用链：`ShellDamengRootTreeItem → ShellDamengClient → ShellDamengSchemaTreeItem`；`ShellDamengViewFactory.addSchema`

## ShellDamengRootTreeItemValue
- 职责：根节点的展示值（名称与图标）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `name()` | 显示名称 | `I18nHelper.schema()` |
  | `graphic()` | 图标 | 懒创建 `SchemaSVGGlyph` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengSchemaTreeItem
- 职责：模式节点，作为表/视图/函数/过程/查询/终端类型节点的父容器，并集中代理该模式下几乎所有数据操作到客户端。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `DamengSchema` | 当前模式对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengSchemaTreeItem(DamengSchema, RichTreeView)` | 构造模式节点 | `setSortable(false)`、`setFilterable(true)`、`setValue(new ShellDamengSchemaTreeItemValue(this))` |
  | `parent()` | 收窄父节点 | `(ShellDamengRootTreeItem) super.parent()` |
  | `schema()` / `userName()` / `info()` / `infoName()` / `connectName()` / `connect()` | 模式名/用户名/连接信息 | `value.getName()`；`info()` 取 `parent().connect()` |
  | `client()` | 客户端 | `parent().client()` |
  | `getMenuItems()` | 右键菜单 | 非空时含"关闭"；`editSchema/deleteSchema`、`dumpData/runSqlFile/transportData` |
  | `runSqlFile()` / `transportData()` / `dump()` | 运行 SQL 文件/传输/转储 | `ShellDamengViewFactory.runSqlFile/transportData/dumpData` |
  | `delete()` | 删除模式 | `Task` 内确认后 `parent().dropSchema()`，成功 `ShellDamengEventUtil.schemaDropped`，`onSuccess` 刷新 |
  | `editDB()` | 编辑模式 | `ShellDamengViewFactory.updateSchema(this.value, this.parent())` |
  | `closeDB()` | 关闭模式 | `clearChild()` + `collapse()` + `setLoaded(false)` + `ShellDamengEventUtil.schemaClosed` |
  | `loadChild()` | 懒加载类型节点 | `Task` 内依次 `new ShellDamengTablesTreeItem / Views / Functions / Procedures / Queries / TerminalTreeItem`，`setChild` 后 `expend` |
  | `getTableTypeChild()/getTableChild()` | 取表类型节点/表节点列表 | 遍历 `richChildren()` 按 `instanceof` 匹配 |
  | `getQueryTypeChild()` / `getFunctionTypeChild()/getFunctionChild()` | 取查询、函数类型/节点列表 | 同上 |
  | `getProcedureTypeChild()/getProcedureChild()` | 取过程类型/节点列表 | 同上 |
  | `getViewTypeChild()/getViewChild()` | 取视图类型/节点列表 | 同上 |
  | `tableSize()` / `viewSize()` | 表/视图数量 | `client().tableSize/viewSize(schema())` |
  | `onPrimaryDoubleClick()` | 双击展开 | 未加载则 `loadChild()` |
  | `createTable(...)` / `createTable(DamengCreateTableParam)` | 建表 | 组装 `DamengCreateTableParam`，`client().createTable` |
  | `createTableParam(...)` | 构造建表参数 | 返回 `DamengCreateTableParam` |
  | `alterTable(...)` / `alterTableParam(...)` | 改表 | 组装 `DamengAlertTableParam`（含 `selectePrimaryKeys`、`existAutoIncrement`），`client().alertTable` |
  | `selectePrimaryKeys(String)` | 查询主键列 | `client().selectePrimaryKeys(schema, table)` |
  | `existAutoIncrement(String)` | 判断自增列 | `client().existAutoIncrement` |
  | `renameTable/renameFunction/renameProcedure` | 重命名 | 委托 `client()` 同名方法 |
  | `clearTable/truncateTable/dropTable(String)` | 清空/截断/删除表 | 委托 `client()` |
  | `executeSql` / `executeSingleSql` / `explainSql` | 执行与解析 SQL | `client().executeSql/executeSingleSql/explainSql(schema, sql)`，返回 `DBQueryResults<...>` |
  | `createFunction/alertFunction/dropFunction` | 函数增改删 | 组装 `DamengCreateFunctionParam`/`DamengAlertFunctionParam` 后调客户端 |
  | `selectProcedure/createProcedure/alertProcedure/dropProcedure` | 过程查询与增改删 | 组装 `DamengCreateProcedureParam`/`DamengAlertProcedureParam` |
  | `selectFunction(String)` / `selectView(String)` / `selectTable(String)` | 查询函数/视图/表 | 委托 `client()` |
  | `createView/alertView/dropView/existView` | 视图增改删与存在性 | 组装 `DamengCreateViewParam`/`DamengAlertViewParam` |
  | `itemVisible()` / `isSupportCheckFeature()` / `dialect()` | 可见性/特性/方言 | `client()` 对应方法；`dialect()` 返回 `DBDialect` |
  | `deleteRecord(DamengDeleteRecordParam)` / `selectRecord(DamengSelectRecordParam)` | 删除/查询记录 | 委托 `client()` |
  | `checks/triggers/columns/indexes/foreignKeys(String)` | 查询约束/触发器/列/索引/外键 | 大多直接委托 `client()`；`columns` 组装 `DamengSelectColumnParam` |
  | `cloneTable/cloneView/cloneFunction/cloneProcedure` | 克隆 | 委托 `client()` 对应方法 |

- 调用链：`ShellDamengSchemaTreeItem → ShellDamengClient`（几乎全部数据操作）→ `ShellDamengViewFactory` / `ShellDamengEventUtil`；子节点构造流向 `ShellDamengTablesTreeItem` 等类型节点。**注意**：文件内保留大量事件相关的注释死代码（`getEventTypeChild/getEventChild`、`renameEvent/selectEvent/alertEvent/dropEvent/cloneEvent`）及被注释的 `doFilter(RichTreeItemFilter)` 重写。

## ShellDamengSchemaTreeItemValue
- 职责：模式节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄所属节点 | `(ShellDamengSchemaTreeItem) super.item()` |
  | `name()` | 名称 | `item().schema()` |
  | `graphic()` | 图标 | 懒创建 `SchemaSVGGlyph` 并 `disableTheme()` |
  | `graphicColor()` | 图标色 | 有子节点时返回 `Color.GREEN` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengTablesTreeItem
- 职责：模式下的"表"类型节点，负责表的懒加载、增量同步与新增。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `tableSize` | `Integer` | 表数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengTablesTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(new ShellDamengTablesTreeItemValue(this))` |
  | `parent()` | 收窄父节点 | `(ShellDamengSchemaTreeItem) super.parent()` |
  | `getMenuItems()` | 右键菜单 | `addTable`、`reloadData`、`exportData`、`importData` |
  | `exportData()` / `importData()` | 导出/导入数据 | `ShellDamengViewFactory.exportData/importData(client, schema)` |
  | `addTable()` | 新增表 | 新建 `DamengTable` 设 schema，`ShellDamengEventUtil.designTable(table, parent())` |
  | `itemVisible()` | 可见性 | `isVisible()` |
  | `loadChild()` | 懒加载表列表 | `TaskBuilder` 内 `client().selectTablesSimple(schema)`；空则全量 `setChild`，否则按 `compare` 做删除/新增/更新三向增量（`parallelStream`）；`onFinish` 执行 `doFilter/doSort` |
  | `reloadChild()` | 重载 | `clearTableSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `schema()` / `client()` / `info()` / `infoName()` | 委托父节点 | 逐级向上取值 |
  | `tableSize()` / `getTableSize()` | 表数量（带缓存） | 委托 `parent().tableSize()` |
  | `onPrimaryDoubleClick()` | 双击展开 | 未加载则 `loadChild()` |
  | `addTable(DamengTable)` | 追加表节点 | `addChild` + `sortChild` + `clearTableSize()` |
  | `clearTableSize()` | 清缓存 | `tableSize=null` |

- 调用链：`ShellDamengTablesTreeItem → ShellDamengClient.selectTablesSimple → ShellDamengTableTreeItem`；`ShellDamengViewFactory.exportData/importData`

## ShellDamengTablesTreeItemValue
- 职责："表"类型节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellDamengTablesTreeItem) super.item()` |
  | `name()` | 名称 | `I18nHelper.table()` |
  | `graphic()` | 图标 | 懒创建 `TableSVGGlyph` 并 `disableTheme()` |
  | `graphicColor()` | 图标色 | 有子节点返回 `Color.GREEN` |
  | `extra()` | 附加数量 | 若 `item().tableSize()` 非空返回 ` (size)` |
  | `extraColor()` | 数量色 | `Color.valueOf("#228B22")` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengTableTreeItem
- 职责：表叶子节点，提供表的打开/设计/重命名/清空/截断/删除/克隆/导入导出及记录 CRUD 与列/索引/约束元数据访问。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `DamengTable` | 当前表对象 |
  | `columns` | `DamengColumns` | 列缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengTableTreeItem(DamengTable, RichTreeView)` | 构造 | `setValue(new ShellDamengTableTreeItemValue(this))` |
  | `parent()` / `client()` / `schema()` / `tableName()` / `info()` / `infoName()` | 上下文取值 | 逐级委托；`tableName()` 取 `value.getName()` |
  | `getMenuItems()` | 右键菜单 | 打开/设计/重命名/清空/截断/删除、转储/导出、克隆子菜单（含/不含数据）、表信息 |
  | `cloneTable(boolean)` / `doCloneTable(boolean)` | 克隆表 | `StageManager.showMask`；`dbItem().cloneTable(...)`，`selectTable` 后经 `getTableTypeChild().addTable` 追加 |
  | `dump()` / `export()` | 转储/导出 | `ShellDamengViewFactory.dumpData/exportData(client, schema, table, ...)` |
  | `designTable()` | 设计表 | 先 `reloadChild()`，再 `ShellDamengEventUtil.designTable(value, dbItem())` |
  | `truncateTable()` / `clearTable()` | 截断/清空 | 确认后 `dbItem().truncateTable/clearTable`，触发 `ShellDamengEventUtil.tableTruncated/tableCleared` |
  | `delete()` | 删除表 | 确认后 `dbItem().dropTable` + `tableDropped` + `parent().clearTableSize()` + `remove()` |
  | `tableInfo()` | 表信息 | `ShellDamengViewFactory.tableInfo(this)` |
  | `rename()` | 重命名 | 校验名称后 `dbItem().renameTable(old, new)`，更新 `value` 并 `refresh()`，触发 `tableRenamed` |
  | `dbItem()` | 所属模式节点 | `parent().parent()`，父为空返回 null |
  | `recordPage(long,long,List<DamengRecordFilter>,List<DamengColumn>)` | 分页查询记录 | 组装 `DamengSelectRecordParam`，`client().selectRecords` + `selectRecordCount`，返回 `Paging<DamengRecord>` |
  | `columns()` / `indexes()` / `checks()` / `foreignKeys()` / `triggers()` | 元数据查询 | 委托 `client()` 同名方法 |
  | `onPrimaryDoubleClick()` | 双击打开表 | `ShellDamengEventUtil.tableOpen(this, dbItem())` |
  | `getPrimaryKey()` | 取主键列（优先自增） | 懒加载 `columns`，遍历 `primaryKeys()` 找 `isAutoIncrement()`，否则取第一个 |
  | `loadChild()` / `reloadChild()` | 刷新表数据 | `client().selectTable(schema, name)` 后 `value.copy(table)` |
  | `hasPrimaryKey()` | 是否存在主键 | 懒加载 `columns`，返回 `columns.primaryKeys().isEmpty()`（**语义疑似反了**：返回空即 true） |
  | `insertRecord(DBRecordData[, DamengRecordPrimaryKey])` | 新增记录 | 组装 `DamengInsertRecordParam` 调 `client().insertRecord` |
  | `deleteRecord(DBRecordData)` / `deleteRecord(DamengRecordPrimaryKey)` | 删除记录 | 组装 `DamengDeleteRecordParam` 调 `client().deleteRecord` |
  | `selectRecord(DamengRecordPrimaryKey)` | 查询记录 | 组装 `DamengSelectRecordParam` |
  | `updateRecord(DBRecordData, DamengRecordPrimaryKey)` / `updateRecord(DBRecordData, DBRecordData)` | 修改记录 | 组装 `DamengUpdateRecordParam` |
  | `value()` | 取表值 | 返回 `value` |

- 调用链：`ShellDamengTableTreeItem → ShellDamengClient`（元数据/记录 CRUD）、`ShellDamengEventUtil`（打开/设计/删除等事件）、`ShellDamengViewFactory`（转储/导出/信息）

## ShellDamengTableTreeItemValue
- 职责：表叶子节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellDamengTableTreeItem) super.item()` |
  | `graphic()` | 图标 | 懒创建 `TableSVGGlyph` |
  | `name()` | 名称 | `item().tableName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengViewsTreeItem
- 职责：模式下的"视图"类型节点，负责视图懒加载、增量同步与新增。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `viewSize` | `Integer` | 视图数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengViewsTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(new ShellDamengViewsTreeItemValue(this))` |
  | `parent()` | 收窄父节点 | `(ShellDamengSchemaTreeItem) super.parent()` |
  | `getMenuItems()` | 右键菜单 | `addView`、`refreshData` |
  | `add()` | 新增视图 | 新建 `DamengView` 设 schema，`ShellDamengEventUtil.designView(dbView, parent())` |
  | `itemVisible()` | 可见性 | `isVisible()` |
  | `loadChild()` | 懒加载视图列表 | `TaskBuilder` 内 `client().selectViewsSimple(schema)`，空全量 / 否则按 `compare` 三向增量同步 |
  | `reloadChild()` | 重载 | `clearViewSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `schema()` / `client()` / `info()` / `infoName()` | 委托父节点 | 逐级向上取值 |
  | `viewSize()` / `getViewSize()` | 视图数量（带缓存） | 委托 `parent().viewSize()` |
  | `onPrimaryDoubleClick()` | 双击展开 | 未加载则 `loadChild()` |
  | `addView(DamengView)` | 追加视图节点 | `addChild` + `sortChild` + `clearViewSize()` |
  | `clearViewSize()` | 清缓存 | `viewSize=null` |

- 调用链：`ShellDamengViewsTreeItem → ShellDamengClient.selectViewsSimple → ShellDamengViewTreeItem`

## ShellDamengViewsTreeItemValue
- 职责："视图"类型节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellDamengViewsTreeItem) super.item()` |
  | `name()` | 名称 | `I18nHelper.view()` |
  | `graphic()` | 图标 | 懒创建 `ViewSVGGlyph` 并 `disableTheme()` |
  | `graphicColor()` | 图标色 | 有子节点返回 `Color.GREEN` |
  | `extra()` | 附加数量 | 若 `item().viewSize()` 非空返回 ` (size)` |
  | `extraColor()` | 数量色 | `Color.valueOf("#228B22")` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengViewTreeItem
- 职责：视图叶子节点，提供视图打开/设计/重命名/删除/克隆及记录 CRUD 与列访问。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `DamengView` | 当前视图对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengViewTreeItem(DamengView, RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(new ShellDamengViewTreeItemValue(this))` |
  | `parent()` / `client()` / `schema()` / `info()` / `infoName()` | 上下文取值 | 逐级委托父节点 |
  | `viewColumns()` | 获取视图列 | 懒加载：`value.setColumns(new DamengColumns(this.columns()))` |
  | `columns()` | 查询列 | `client().viewColumns(schema, viewName)` 包装为 `DamengColumns` |
  | `getMenuItems()` | 右键菜单 | 打开/设计/重命名/删除、克隆、视图信息 |
  | `cloneView()` / `doCloneView()` | 克隆视图 | `StageManager.showMask`；`dbItem().cloneView(...)`，`selectView` 后 `getViewTypeChild().addView` |
  | `viewInfo()` | 视图信息 | `ShellDamengViewFactory.viewInfo(this)` |
  | `designView()` | 设计视图 | `ShellDamengEventUtil.designView(value, dbItem())` |
  | `delete()` | 删除视图 | 确认后 `dbItem().dropView(value)` + `dropView` 事件 + `parent().clearViewSize()` + `remove()` |
  | `dbItem()` | 所属模式节点 | `parent().parent()` |
  | `recordPage(long,long,List<DamengRecordFilter>,List<DamengColumn>)` | 分页查询记录 | `client().viewRecords(...)` + `selectRecordCount`，返回 `Paging<DamengRecord>` |
  | `onPrimaryDoubleClick()` | 双击打开视图 | `ShellDamengEventUtil.viewOpen(this, dbItem())` |
  | `getPrimaryKey()` | 取主键列（优先自增） | 需列时 `viewColumns()`，遍历找 `isAutoIncrement()` |
  | `isUpdatable()` | 视图是否可更新 | `value.isUpdatable()` |
  | `viewName()` | 视图名称 | `value.getName()` |
  | `insertRecord/deleteRecord/selectRecord/updateRecord` | 记录 CRUD | 组装对应 `Dameng*RecordParam` 调 `client()` |
  | `rename()` | 重命名 | 校验后 `dbItem().renameTable(old, new)`（**注意：视图重命名复用了 renameTable**），更新 `value` + `refresh()` + `viewRenamed` |

- 调用链：`ShellDamengViewTreeItem → ShellDamengClient`（`viewRecords/viewColumns/记录 CRUD`）、`ShellDamengEventUtil`、`ShellDamengViewFactory.viewInfo`

## ShellDamengViewTreeItemValue
- 职责：视图叶子节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellDamengViewTreeItem) super.item()` |
  | `graphic()` | 图标 | 懒创建 `ViewSVGGlyph` |
  | `name()` | 名称 | `item().viewName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengFunctionsTreeItem
- 职责：模式下的"函数"类型节点，负责函数懒加载、增量同步与新增。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `functionSize` | `Integer` | 函数数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengFunctionsTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(new ShellDamengFunctionsTreeItemValue(this))` |
  | `parent()` | 收窄父节点 | `(ShellDamengSchemaTreeItem) super.parent()` |
  | `getMenuItems()` | 右键菜单 | `addFunction`、`refreshData` |
  | `add()` | 新增函数 | 新建 `DamengFunction` 设 schema，`ShellDamengEventUtil.designFunction(function, parent())` |
  | `itemVisible()` | 可见性 | `isVisible()` |
  | `loadChild()` | 懒加载函数列表 | `TaskBuilder` 内 `client().selectFunctionsSimple(schema)`，空全量 / 否则按 `compare` 三向增量同步；`onFinish` 执行 `doFilter/doSort` |
  | `reloadChild()` | 重载 | `clearFunctionSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `schema()` / `client()` / `info()` / `infoName()` | 委托父节点 | 逐级向上取值 |
  | `onPrimaryDoubleClick()` | 双击展开 | 未加载则 `loadChild()` |
  | `functionSize()` / `getFunctionSize()` | 函数数量（带缓存） | `client().functionSize(schema)` |
  | `addFunction(DamengFunction)` | 追加函数节点 | `addChild` + `sortChild` + `clearFunctionSize()` |
  | `clearFunctionSize()` | 清缓存 | `functionSize=null` |

- 调用链：`ShellDamengFunctionsTreeItem → ShellDamengClient.selectFunctionsSimple → ShellDamengFunctionTreeItem`

## ShellDamengFunctionsTreeItemValue
- 职责："函数"类型节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellDamengFunctionsTreeItem) super.item()` |
  | `name()` | 名称 | `I18nHelper.function()` |
  | `graphic()` | 图标 | 懒创建 `FunctionSVGGlyph` 并 `disableTheme()` |
  | `graphicColor()` | 图标色 | 有子节点返回 `Color.GREEN` |
  | `extra()` | 附加数量 | 若 `item().functionSize()` 非空返回 ` (size)` |
  | `extraColor()` | 数量色 | `Color.valueOf("#228B22")` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengFunctionTreeItem
- 职责：函数叶子节点，提供设计/重命名/删除/克隆/信息查看与自身数据重载。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `DamengFunction` | 当前函数对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengFunctionTreeItem(DamengFunction, RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(new ShellDamengFunctionTreeItemValue(this))` |
  | `parent()` / `client()` / `info()` | 上下文取值 | 逐级委托父节点 |
  | `getMenuItems()` | 右键菜单 | design/rename/delete、克隆、函数信息 |
  | `functionInfo()` | 函数信息 | `ShellDamengViewFactory.functionInfo(this)` |
  | `cloneFunction()` / `doCloneFunction()` | 克隆函数 | `StageManager.showMask`；`dbItem().cloneFunction(...)`，`selectFunction` 后 `getFunctionTypeChild().addFunction` |
  | `delete()` | 删除函数 | 确认后 `dbItem().dropFunction(value)` + `dropFunction` 事件 + `parent().clearFunctionSize()` + `remove()` |
  | `dbItem()` | 所属模式节点 | `parent().parent()` |
  | `schema()` / `infoName()` / `functionName()` / `value()` | 模式名/连接名/函数名/值 | 委托或取 `value` |
  | `onPrimaryDoubleClick()` | 双击设计 | `ShellDamengEventUtil.designFunction(value, dbItem())` |
  | `reloadChild()` | 重载 | `clearChild()+setLoaded(false)+loadChild()` |
  | `loadChild()` | 加载函数详情 | `client().selectFunction(schema, name)` 后 `value.copy(function)` |
  | `onPrimarySingleClick()` | 单击 | 两分支均调用 `super.onPrimarySingleClick()`（冗余分支） |
  | `rename()` | 重命名 | 校验后 `dbItem().renameFunction(old, new)`，更新 `value` + `refresh()` + `functionRenamed` |

- 调用链：`ShellDamengFunctionTreeItem → ShellDamengClient`（`selectFunction/dropFunction/cloneFunction/renameFunction`）、`ShellDamengEventUtil`、`ShellDamengViewFactory.functionInfo`

## ShellDamengFunctionTreeItemValue
- 职责：函数叶子节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellDamengFunctionTreeItem) super.item()` |
  | `graphic()` | 图标 | 懒创建 `FunctionSVGGlyph` |
  | `name()` | 名称 | `item().functionName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengProceduresTreeItem
- 职责：模式下的"过程"类型节点，负责过程懒加载、增量同步与新增。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `procedureSize` | `Integer` | 过程数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengProceduresTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(new ShellDamengProceduresTreeItemValue(this))` |
  | `parent()` | 收窄父节点 | `(ShellDamengSchemaTreeItem) super.parent()` |
  | `getMenuItems()` | 右键菜单 | `addProcedure`、`refreshData` |
  | `add()` | 新增过程 | 新建 `DamengProcedure` 设 schema，`ShellDamengEventUtil.designProcedure(procedure, parent())` |
  | `itemVisible()` | 可见性 | `isVisible()` |
  | `loadChild()` | 懒加载过程列表 | `TaskBuilder` 内 `client().selectProceduresSimple(schema)`，空全量 / 否则按 `compare` 三向增量同步 |
  | `reloadChild()` | 重载 | `clearProcedureSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `schema()` / `client()` / `info()` / `infoName()` | 委托父节点 | 逐级向上取值 |
  | `onPrimaryDoubleClick()` | 双击展开 | 未加载则 `loadChild()` |
  | `procedureSize()` / `getProcedureSize()` | 过程数量（带缓存） | `client().procedureSize(schema)` |
  | `addProcedure(DamengProcedure)` | 追加过程节点 | `addChild` + `sortChild` + `clearProcedureSize()` |
  | `clearProcedureSize()` | 清缓存 | `procedureSize=null` |

- 调用链：`ShellDamengProceduresTreeItem → ShellDamengClient.selectProceduresSimple → ShellDamengProcedureTreeItem`

## ShellDamengProceduresTreeItemValue
- 职责："过程"类型节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellDamengProceduresTreeItem) super.item()` |
  | `name()` | 名称 | `I18nHelper.procedure()` |
  | `graphic()` | 图标 | 懒创建 `ProcedureSVGGlyph` 并 `disableTheme()` |
  | `graphicColor()` | 图标色 | 有子节点返回 `Color.GREEN` |
  | `extra()` | 附加数量 | 若 `item().procedureSize()` 非空返回 ` (size)` |
  | `extraColor()` | 数量色 | `Color.valueOf("#228B22")` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengProcedureTreeItem
- 职责：过程叶子节点，提供设计/重命名/删除/克隆/信息查看。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `DamengProcedure` | 当前过程对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengProcedureTreeItem(DamengProcedure, RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(new ShellDamengProcedureTreeItemValue(this))` |
  | `value()` | 取过程值 | 返回 `value` |
  | `parent()` / `client()` / `info()` | 上下文取值 | 逐级委托父节点 |
  | `getMenuItems()` | 右键菜单 | design/rename/delete、克隆、过程信息 |
  | `procedureInfo()` | 过程信息 | `ShellDamengViewFactory.procedureInfo(this)` |
  | `cloneProcedure()` / `doCloneProcedure()` | 克隆过程 | `StageManager.showMask`；`dbItem().cloneProcedure(...)`，`selectProcedure` 后 `getProcedureTypeChild().addProcedure` |
  | `delete()` | 删除过程 | 确认后 `dbItem().dropProcedure(value)` + `dropProcedure` 事件 + `parent().clearProcedureSize()` + `remove()` |
  | `dbItem()` | 所属模式节点 | `parent().parent()` |
  | `schema()` / `infoName()` / `procedureName()` | 模式名/连接名/过程名 | 委托父节点或取 `value.getName()` |
  | `onPrimaryDoubleClick()` | 双击设计 | `ShellDamengEventUtil.designProcedure(value, dbItem())` |
  | `rename()` | 重命名 | 校验后先 `value.setName(new)` 再 `dbItem().renameProcedure(old, new)`，`refresh()` + `procedureRenamed` |

- 调用链：`ShellDamengProcedureTreeItem → ShellDamengClient`（`selectProcedure/dropProcedure/cloneProcedure/renameProcedure`）、`ShellDamengEventUtil`、`ShellDamengViewFactory.procedureInfo`

## ShellDamengProcedureTreeItemValue
- 职责：过程叶子节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellDamengProcedureTreeItem) super.item()` |
  | `graphic()` | 图标 | 懒创建 `ProcedureSVGGlyph` |
  | `name()` | 名称 | `item().procedureName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengQueriesTreeItem
- 职责：模式下的"查询"类型节点，管理本地持久化的 SQL 查询（`ShellQuery`）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `querySize` | `Integer` | 查询数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengQueriesTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(new ShellDamengQueriesTreeItemValue(this))` |
  | `parent()` | 收窄父节点 | `(ShellDamengSchemaTreeItem) super.parent()` |
  | `getMenuItems()` | 右键菜单 | `addQuery`、`refreshData` |
  | `addQuery()` | 新增查询 | `ShellDamengEventUtil.queryAdd(parent())` |
  | `itemVisible()` | 可见性 | `isVisible()` |
  | `loadChild()` | 懒加载查询列表 | `TaskBuilder` 内 `ShellQueryStore.INSTANCE.list(infoId, schema)`，全量 `setChild` |
  | `reloadChild()` | 重载 | `clearQuerySize()+clearChild()+setLoaded(false)+loadChild()` |
  | `addChild(ShellQuery)` | 追加查询节点 | `addChild(new ShellDamengQueryTreeItem(...))` |
  | `schema()` / `client()` / `info()` | 委托父节点 | 逐级向上取值 |
  | `onPrimaryDoubleClick()` | 双击展开 | 未加载则 `loadChild()` |
  | `querySize()` / `getQuerySize()` | 查询数量（带缓存） | `ShellQueryStore.INSTANCE.list(...)` 数量 |
  | `addQuery(ShellQuery)` | 追加并排序 | `addChild` + `sortChild` + `clearQuerySize()` |
  | `clearQuerySize()` | 清缓存 | `querySize=null` |

- 调用链：`ShellDamengQueriesTreeItem → ShellQueryStore.INSTANCE → ShellDamengQueryTreeItem`；`ShellDamengEventUtil.queryAdd`

## ShellDamengQueriesTreeItemValue
- 职责："查询"类型节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellDamengQueriesTreeItem) super.item()` |
  | `name()` | 名称 | `I18nHelper.queries()` |
  | `graphic()` | 图标 | 懒创建 `QuerySVGGlyph` 并 `disableTheme()` |
  | `graphicColor()` | 图标色 | 有子节点返回 `Color.GREEN` |
  | `extra()` | 附加数量 | 若 `item().querySize()` 非空返回 ` (size)` |
  | `extraColor()` | 数量色 | `Color.valueOf("#228B22")` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengQueryTreeItem
- 职责：查询叶子节点，提供打开/重命名/删除已保存的 SQL 查询。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `ShellQuery` | 当前查询对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengQueryTreeItem(ShellQuery, RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(new ShellDamengQueryTreeItemValue(this))` |
  | `value()` | 取查询值 | 返回 `value` |
  | `parent()` / `client()` / `info()` | 上下文取值 | 逐级委托父节点 |
  | `getMenuItems()` | 右键菜单 | 打开/重命名/删除 |
  | `delete()` | 删除查询 | `ShellQueryStore.INSTANCE.delete(value)`，成功后 `queryDeleted` 事件 + `parent().clearQuerySize()` + `remove()` |
  | `rename()` | 重命名 | `ShellQueryStore.INSTANCE.update(value)`，成功 `queryRenamed` + `refresh()`；失败回滚名称 |
  | `dbItem()` | 所属模式节点 | `parent().parent()` |
  | `schema()` / `queryName()` | 模式名/查询名 | `parent().schema()`；`value.getName()` |
  | `onPrimaryDoubleClick()` | 双击打开 | `ShellDamengEventUtil.queryOpen(value, dbItem())` |

- 调用链：`ShellDamengQueryTreeItem → ShellQueryStore.INSTANCE`（删除/更新）、`ShellDamengEventUtil`（打开/重命名/删除）

## ShellDamengQueryTreeItemValue
- 职责：查询叶子节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellDamengQueryTreeItem) super.item()` |
  | `graphic()` | 图标 | 懒创建 `QuerySVGGlyph` |
  | `name()` | 名称 | `item().queryName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellDamengTerminalTreeItem
- 职责：终端叶子节点，双击打开该模式的 SQL 终端。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellDamengTerminalTreeItem(RichTreeView)` | 构造 | `setValue(new ShellDamengTerminalTreeItemValue())` |
  | `parent()` | 收窄父节点 | `(ShellDamengSchemaTreeItem) super.parent()` |
  | `client()` | 客户端 | `parent().client()` |
  | `onPrimaryDoubleClick()` | 双击打开终端 | `ShellDamengEventUtil.terminalOpen(parent())` |

- 调用链：`ShellDamengTerminalTreeItem → ShellDamengEventUtil.terminalOpen`；直接继承 `RichTreeItem`（非 `ShellDamengTreeItem`）

## ShellDamengTerminalTreeItemValue
- 职责：终端节点的展示值（名称与图标）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `graphic()` | 图标 | 懒创建 `TerminalSVGGlyph` |
  | `name()` | 名称 | `I18nHelper.terminal()` |

- 调用链：继承 `RichTreeItemValue`

---

附注（不影响功能，供审查参考）：
- `ShellDamengTreeItemFilter.test` 白名单误用 `ShellMysqlRootTreeItem`，达梦根节点未被豁免过滤。
- `ShellDamengTableTreeItem.hasPrimaryKey()` 返回 `columns.primaryKeys().isEmpty()`，命名与语义相反。
- `ShellDamengViewTreeItem.rename()` 调用 `dbItem().renameTable(...)` 重命名视图。
- `ShellDamengSchemaTreeItem` 内含多处事件相关的注释死代码（`getEventChild`、`renameEvent` 等）及被注释的 `doFilter` 重写。
- `ShellDamengFunctionTreeItem.onPrimarySingleClick()` 两分支逻辑完全相同，属冗余。
