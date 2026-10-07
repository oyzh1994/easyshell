# 代码审查文档 · trees/mysql（MySQL 数据库树）

> 范围：`src/main/java/cn/oyzh/easyshell/trees/mysql/` 共 33 个类。
> 说明：由 `ShellMysqlTreeView` 初始化根节点 `ShellMysqlRootTreeItem`（库层级），库节点懒加载表/视图/函数/过程/事件/查询/终端七类"类型节点"，类型节点再懒加载对应叶子节点。业务逻辑下沉到 `ShellMysqlClient`（数据 CRUD）、`ShellMysqlViewFactory`（弹窗）、`ShellMysqlEventUtil`（事件）。

---

## ShellMysqlTreeItem
- 职责：MySQL 树所有节点的抽象基类，统一收窄 `getTreeView()` 返回类型。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlTreeItem(RichTreeView treeView)` | 构造节点 | `super(treeView)` |
  | `ShellMysqlTreeView getTreeView()` | 返回 mysql 树视图 | `(ShellMysqlTreeView) super.getTreeView()` |

- 调用链：继承 `RichTreeItem<V>`；被全部 MySQL 树节点继承。

## ShellMysqlTreeView
- 职责：MySQL 资源树视图，持有客户端并初始化根节点。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `client` | `ShellMysqlClient` | MySQL 客户端 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setClient(ShellMysqlClient)` / `getClient()` | 设置/获取客户端 | 简单存取 |
  | `ShellMysqlTreeItemFilter getItemFilter()` | 懒初始化过滤器 | `new ShellMysqlTreeItemFilter()` |
  | `ShellMysqlTreeView()` | 构造视图 | `dragContent="mysql_tree_drag"`、单选、`RichTreeCell` 工厂；`super.setRoot(new ShellMysqlRootTreeItem(this))` |
  | `ShellMysqlRootTreeItem root()` | 收窄根节点类型 | `(ShellMysqlRootTreeItem) super.root()` |

- 调用链：`ShellMysqlTreeView → ShellMysqlRootTreeItem → ShellMysqlDatabaseTreeItem`

## ShellMysqlTreeItemFilter
- 职责：MySQL 树节点的关键字过滤判定。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean test(RichTreeItem<?> item)` | 判断节点是否命中关键字 | 根/库/表/视图/事件/查询/函数/过程类型节点恒返回 true；对 `ShellMysqlTreeItem<?>` 取 `value.name()` 调 `TextUtil.findText` 比较 `NOT_FOUND` |

- 调用链：继承 `RichTreeItemFilter`；使用 `TextUtil.findText`

## ShellMysqlRootTreeItem
- 职责：MySQL 树根节点，承载数据库（库）层级的增删改查与加载。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlRootTreeItem(ShellMysqlTreeView)` | 构造根节点 | `setValue(new ShellMysqlRootTreeItemValue())` |
  | `client()` / `connect()` | 客户端/连接信息 | `getTreeView().getClient()`；`client().getShellConnect()` |
  | `existDatabase/createDatabase/alterDatabase/dropDatabase/databaseCollation(String)` | 库存在性/创建/修改/删除/排序规则 | 委托 `client()` 同名方法 |
  | `addDatabase()` | 弹出新增库窗口 | `ShellMysqlViewFactory.addDatabase(this)`，取 `databaseName` 后 `addDatabase(name)` |
  | `addDatabase(String)` | 追加库节点 | `client().database(name)` 后 `addChild(new ShellMysqlDatabaseTreeItem(...))` |
  | `reloadChild()` | 重载 | `clearChild()` + `loadChild()` |
  | `loadChild()` | 加载全部库 | `client().databases()` 构造子节点，`setChild` 后 `expend/doFilter/doSort` |
  | `clearChild()` | 清空子节点前关闭库 | 遍历子节点调 `ShellMysqlDatabaseTreeItem.closeDB()`，再 `super.clearChild()` |
  | `getMenuItems()` | 右键菜单 | `addDatabase` / `reloadDatabase` |

- 调用链：`ShellMysqlRootTreeItem → ShellMysqlClient → ShellMysqlDatabaseTreeItem`；`ShellMysqlViewFactory.addDatabase`

## ShellMysqlRootTreeItemValue
- 职责：根节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `name()` | 名称 | `I18nHelper.database()` |
  | `graphic()` | 图标 | 懒创建 `DatabaseSVGGlyph` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlDatabaseTreeItem
- 职责：库节点，作为表/视图/函数/过程/事件/查询/终端类型节点的父容器，并集中代理该库下几乎所有数据操作到客户端。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `MysqlDatabase` | 当前数据库对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlDatabaseTreeItem(MysqlDatabase, RichTreeView)` | 构造库节点 | `setSortable(false)`、`setFilterable(true)`、`setValue(new ShellMysqlDatabaseTreeItemValue(this))` |
  | `parent()` | 收窄父节点 | `(ShellMysqlRootTreeItem) super.parent()` |
  | `dbName()` / `userName()` / `info()` / `infoName()` / `connectName()` / `connect()` | 库名/用户名/连接信息 | `value.getName()`；`info()` 取 `parent().connect()` |
  | `client()` | 客户端 | `parent().client()` |
  | `getMenuItems()` | 右键菜单 | 非空含"关闭"；`editDB/delete`、`dumpData/runSqlFile/transportData` |
  | `runSqlFile()` / `transportData()` / `dump()` | 运行 SQL 文件/传输/转储 | `ShellMysqlViewFactory.runSqlFile/transportData/dumpData` |
  | `delete()` | 删除库 | `Task` 内确认后 `parent().dropDatabase()`，成功 `ShellMysqlEventUtil.databaseDropped` |
  | `editDB()` | 编辑库 | `ShellMysqlViewFactory.databaseUpdate(this.value, this.parent())` |
  | `closeDB()` | 关闭库 | `clearChild()+collapse()+setLoaded(false)` + `ShellMysqlEventUtil.databaseClosed` |
  | `loadChild()` | 懒加载类型节点 | `Task` 内依次 `new ShellMysqlTablesTreeItem/Views/Functions/Procedures/Events/Queries/TerminalTreeItem`，`setChild` 后 `expend` |
  | `getTableTypeChild()/getTableChild()` | 取表类型节点/表节点列表 | 遍历 `richChildren()` 按 `instanceof` 匹配 |
  | `getQueryTypeChild()` / `getFunctionTypeChild()/getFunctionChild()` | 取查询、函数类型/节点列表 | 同上 |
  | `getProcedureTypeChild()/getProcedureChild()` | 取过程类型/节点列表 | 同上 |
  | `getEventTypeChild()/getEventChild()` | 取事件类型/节点列表 | 同上 |
  | `getViewTypeChild()/getViewChild()` | 取视图类型/节点列表 | 同上 |
  | `tableSize()` / `viewSize()` | 表/视图数量 | `client().tableSize/viewSize(dbName)` |
  | `onPrimaryDoubleClick()` | 双击展开 | 未加载则 `loadChild()` |
  | `createTable(...)` / `createTable(MysqlCreateTableParam)` / `createTableParam(...)` | 建表 | 组装 `MysqlCreateTableParam`，`client().createTable` |
  | `alterTable(...)` / `alterTable(MysqlAlertTableParam)` / `alterTableParam(...)` | 改表 | 组装 `MysqlAlertTableParam` 并设置 `existPrimaryKey`，`client().alertTable` |
  | `existPrimaryKey(String)` | 主键是否存在 | `client().existPrimaryKey(dbName, tableName)` |
  | `renameTable/renameEvent/renameFunction/renameProcedure(String,String)` | 重命名 | 委托 `client()` 同名方法 |
  | `clearTable/truncateTable/dropTable(String)` | 清空/截断/删除表 | 委托 `client()` |
  | `executeSql/executeSingleSql/explainSql(String)` | 执行/单条执行/解析 SQL | `client()` 对应方法，返回 `DBQueryResults<...>` |
  | `createFunction/alertFunction/dropFunction` | 函数增改删 | 组装 `MysqlCreateFunctionParam`/`MysqlAlertFunctionParam` |
  | `selectProcedure/createProcedure/alertProcedure/dropProcedure` | 过程查询与增改删 | 组装 `MysqlCreateProcedureParam`/`MysqlAlertProcedureParam` |
  | `selectFunction/selectView/selectTable(String)` | 查询函数/视图/表 | 委托 `client()` |
  | `createView/alertView/dropView/existView` | 视图增改删与存在性 | 组装 `MysqlCreateViewParam`/`MysqlAlertViewParam` |
  | `selectEvent/alertEvent/createEvent/dropEvent` | 事件查询与增改删 | 委托 `client()` |
  | `itemVisible()` / `isSupportCheckFeature()` / `dialect()` | 可见性/检查约束特性/方言 | `client()` 对应方法 |
  | `deleteRecord(MysqlDeleteRecordParam)` / `selectRecord(MysqlSelectRecordParam)` | 删除/查询记录 | 委托 `client()` |
  | `checks/triggers/columns/indexes/foreignKeys(String)` | 查询约束/触发器/列/索引/外键 | 大多委托 `client()`；`columns` 组装 `MysqlSelectColumnParam` |
  | `cloneTable/cloneView/cloneFunction/cloneProcedure/cloneEvent` | 克隆 | 委托 `client()` 对应方法 |

- 调用链：`ShellMysqlDatabaseTreeItem → ShellMysqlClient`（几乎全部数据操作）→ `ShellMysqlViewFactory` / `ShellMysqlEventUtil`。**注意**：文件中含被注释的 `doFilter` 重写。

## ShellMysqlDatabaseTreeItemValue
- 职责：库节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellMysqlDatabaseTreeItem) super.item()` |
  | `name()` | 名称 | `item().dbName()` |
  | `graphic()` | 图标 | 懒创建 `DatabaseSVGGlyph` 并 `disableTheme()` |
  | `graphicColor()` | 图标色 | 有子节点返回 `Color.GREEN` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlTablesTreeItem
- 职责：库下"表"类型节点，负责表懒加载、增量同步与新增/导入导出。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `tableSize` | `Integer` | 表数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlTablesTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `parent()` | 收窄父节点 | `(ShellMysqlDatabaseTreeItem) super.parent()` |
  | `getMenuItems()` | 右键菜单 | `addTable`、`reloadData`、`exportData`、`importData` |
  | `exportData()` / `importData()` | 导出/导入 | `ShellMysqlViewFactory.exportData/importData(client, dbName)` |
  | `addTable()` | 新增表 | 新建 `MysqlTable` 设 dbName，`ShellMysqlEventUtil.designTable(table, parent())` |
  | `itemVisible()` | 可见性 | `isVisible()` |
  | `loadChild()` | 懒加载表列表 | `TaskBuilder` 内 `client().selectTablesSimple(dbName)`；空则全量 `setChild`，否则按 `compare` 三向增量同步 |
  | `reloadChild()` | 重载 | `clearTableSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `dbName()/client()/info()/infoName()` | 委托父节点 | 逐级向上取值 |
  | `tableSize()` / `getTableSize()` | 表数量（带缓存） | 委托 `parent().tableSize()` |
  | `onPrimaryDoubleClick()` | 双击展开 | 未加载则 `loadChild()` |
  | `addTable(MysqlTable)` | 追加表节点 | `addChild + sortChild + clearTableSize()` |
  | `clearTableSize()` | 清缓存 | `tableSize=null` |

- 调用链：`ShellMysqlTablesTreeItem → ShellMysqlClient.selectTablesSimple → ShellMysqlTableTreeItem`

## ShellMysqlTablesTreeItemValue
- 职责："表"类型节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` / `name()` / `graphic()` | 收窄节点/名称/图标 | `I18nHelper.table()`；`TableSVGGlyph` + `disableTheme` |
  | `extra()` / `extraColor()` | 附加数量 | 若 `getTableSize()` 非空返回 ` (size)`；`#228B22` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlTableTreeItem
- 职责：表叶子节点，提供打开/设计/重命名/清空/截断/删除/克隆/转储/导出及记录 CRUD 与元数据访问。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `MysqlTable` | 当前表对象 |
  | `columns` | `MysqlColumns` | 列缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlTableTreeItem(MysqlTable, RichTreeView)` | 构造 | `setValue(new ShellMysqlTableTreeItemValue(this))` |
  | `parent()/client()/dbName()/tableName()/info()/infoName()` | 上下文取值 | 逐级委托；`tableName()=value.getName()` |
  | `getMenuItems()` | 右键菜单 | 打开/设计/重命名/清空/截断/删除、转储/导出、克隆子菜单（含/不含数据）、表信息 |
  | `cloneTable(boolean)` / `doCloneTable(boolean)` | 克隆表 | `StageManager.showMask`；`dbItem().cloneTable(...)`，`selectTable` 后 `getTableTypeChild().addTable` |
  | `dump()` / `export()` | 转储/导出 | `ShellMysqlViewFactory.dumpData/exportData(client, dbName, table, ...)` |
  | `designTable()` | 设计表 | 先 `reloadChild()`，再 `ShellMysqlEventUtil.designTable(value, dbItem())` |
  | `truncateTable()` / `clearTable()` | 截断/清空 | 确认后 `dbItem().truncateTable/clearTable`，触发 `tableTruncated/tableCleared` |
  | `delete()` | 删除表 | 确认后 `dbItem().dropTable` + `tableDropped` + `clearTableSize()` + `remove()` |
  | `tableInfo()` | 表信息 | `ShellMysqlViewFactory.tableInfo(this)` |
  | `rename()` | 重命名 | 校验后 `dbItem().renameTable(old, new)`，更新 `value` + `refresh()` + `tableRenamed` |
  | `dbItem()` | 所属库节点 | `parent().parent()` |
  | `recordPage(long,long,List<MysqlRecordFilter>,List<MysqlColumn>)` | 分页查询记录 | 组装 `MysqlSelectRecordParam`，`client().selectRecords` + `selectRecordCount`，返回 `Paging<MysqlRecord>` |
  | `columns()/indexes()/checks()/foreignKeys()/triggers()` | 元数据查询 | 委托 `client()` 同名方法 |
  | `onPrimaryDoubleClick()` | 双击打开表 | `ShellMysqlEventUtil.tableOpen(this, dbItem())` |
  | `getPrimaryKey()` | 取主键列（优先自增） | 懒加载 `columns`，遍历 `primaryKeys()` 找 `isAutoIncrement()`，否则取第一个 |
  | `loadChild()/reloadChild()` | 刷新表数据 | `client().selectTable(param)`（full）后 `value.copy(table)` |
  | `hasPrimaryKey()` | 是否存在主键 | 懒加载 `columns`，返回 `columns.primaryKeys().isEmpty()`（**语义疑似反了**） |
  | `insertRecord(DBRecordData[, MysqlRecordPrimaryKey])` | 新增记录 | 组装 `MysqlInsertRecordParam` 调 `client().insertRecord` |
  | `deleteRecord(DBRecordData)` / `deleteRecord(MysqlRecordPrimaryKey)` | 删除记录 | 组装 `MysqlDeleteRecordParam` |
  | `selectRecord(MysqlRecordPrimaryKey)` | 查询记录 | 组装 `MysqlSelectRecordParam` |
  | `updateRecord(DBRecordData, MysqlRecordPrimaryKey)` / `updateRecord(DBRecordData, DBRecordData)` | 修改记录 | 组装 `MysqlUpdateRecordParam` |
  | `value()` | 取表值 | 返回 `value` |

- 调用链：`ShellMysqlTableTreeItem → ShellMysqlClient`（元数据/记录 CRUD）、`ShellMysqlEventUtil`、`ShellMysqlViewFactory`

## ShellMysqlTableTreeItemValue
- 职责：表叶子节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/graphic()/name()` | 收窄节点/图标/名称 | `TableSVGGlyph`；`item().tableName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlViewsTreeItem
- 职责：库下"视图"类型节点，负责视图懒加载、增量同步与新增。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `viewSize` | `Integer` | 视图数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlViewsTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `parent()` | 收窄父节点 | `(ShellMysqlDatabaseTreeItem) super.parent()` |
  | `getMenuItems()` | 右键菜单 | `addView`、`refreshData` |
  | `add()` | 新增视图 | 新建 `MysqlView` 设 dbName，`ShellMysqlEventUtil.designView(dbView, parent())` |
  | `itemVisible()` | 可见性 | `isVisible()` |
  | `loadChild()` | 懒加载视图列表 | `TaskBuilder` 内 `client().selectViewsSimple(dbName)`，空全量 / 否则按 `compare` 三向增量同步 |
  | `reloadChild()` | 重载 | `clearViewSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `dbName()/client()/info()/infoName()` | 委托父节点 | 逐级向上取值 |
  | `viewSize()` / `getViewSize()` | 视图数量（带缓存） | 委托 `parent().viewSize()` |
  | `onPrimaryDoubleClick()` | 双击展开 | 未加载则 `loadChild()` |
  | `addView(MysqlView)` | 追加视图节点 | `addChild + sortChild + clearViewSize()` |
  | `clearViewSize()` | 清缓存 | `viewSize=null` |

- 调用链：`ShellMysqlViewsTreeItem → ShellMysqlClient.selectViewsSimple → ShellMysqlViewTreeItem`

## ShellMysqlViewsTreeItemValue
- 职责："视图"类型节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/name()/graphic()` | 收窄节点/名称/图标 | `I18nHelper.view()`；`ViewSVGGlyph` + `disableTheme` |
  | `extra()/extraColor()` | 附加数量 | 若 `getViewSize()` 非空返回 ` (size)`；`#228B22` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlViewTreeItem
- 职责：视图叶子节点，提供视图打开/设计/重命名/删除/克隆/信息及记录 CRUD 与列访问。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `MysqlView` | 当前视图对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlViewTreeItem(MysqlView, RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `parent()/client()/dbName()/info()/infoName()` | 上下文取值 | 逐级委托 |
  | `viewColumns()` / `columns()` | 视图列（懒加载）/查询列 | `client().viewColumns(dbName, viewName)` 包装为 `MysqlColumns` |
  | `getMenuItems()` | 右键菜单 | 打开/设计/重命名/删除、克隆、视图信息 |
  | `cloneView()/doCloneView()` | 克隆视图 | `StageManager.showMask`；`dbItem().cloneView(...)` → `getViewTypeChild().addView` |
  | `viewInfo()` / `designView()` | 视图信息/设计 | `ShellMysqlViewFactory.viewInfo`；`ShellMysqlEventUtil.designView` |
  | `delete()` | 删除视图 | 确认后 `dbItem().dropView(value)` + `dropView` 事件 + `clearViewSize()` + `remove()` |
  | `dbItem()` | 所属库节点 | `parent().parent()` |
  | `recordPage(long,long,List<MysqlRecordFilter>,List<MysqlColumn>)` | 分页查询记录 | `client().viewRecords(...)` + `selectRecordCount`，返回 `Paging<MysqlRecord>` |
  | `onPrimaryDoubleClick()` | 双击打开视图 | `ShellMysqlEventUtil.viewOpen(this, dbItem())` |
  | `getPrimaryKey()` / `isUpdatable()` | 主键列/是否可更新 | `viewColumns()` 遍历找自增；`value.isUpdatable()` |
  | `viewName()` | 视图名称 | `value.getName()` |
  | `insertRecord/deleteRecord/selectRecord/updateRecord` | 记录 CRUD | 组装 `Mysql*RecordParam` 调 `client()` |
  | `rename()` | 重命名 | 校验后 `dbItem().renameTable(old, new)`（**视图重命名复用 renameTable**）+ `viewRenamed` |

- 调用链：`ShellMysqlViewTreeItem → ShellMysqlClient`、`ShellMysqlEventUtil`、`ShellMysqlViewFactory.viewInfo`

## ShellMysqlViewTreeItemValue
- 职责：视图叶子节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/graphic()/name()` | 收窄节点/图标/名称 | `ViewSVGGlyph`；`item().viewName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlFunctionsTreeItem
- 职责：库下"函数"类型节点，负责函数懒加载、增量同步与新增。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `functionSize` | `Integer` | 函数数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlFunctionsTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `getMenuItems()` | 右键菜单 | `addFunction`、`refreshData` |
  | `add()` | 新增函数 | 新建 `MysqlFunction` 设 dbName，`ShellMysqlEventUtil.designFunction(function, parent())` |
  | `loadChild()` | 懒加载函数列表 | `client().selectFunctionsSimple(dbName)`；空全量 / 否则 `compare` 三向增量 |
  | `reloadChild()` | 重载 | `clearFunctionSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `dbName()/client()/info()/infoName()` | 委托父节点 | 逐级向上取值 |
  | `functionSize()/getFunctionSize()` | 函数数量（带缓存） | `client().functionSize(dbName)` |
  | `addFunction(MysqlFunction)` | 追加函数节点 | `addChild + sortChild + clearFunctionSize()` |
  | `clearFunctionSize()` | 清缓存 | `functionSize=null` |

- 调用链：`ShellMysqlFunctionsTreeItem → ShellMysqlClient.selectFunctionsSimple → ShellMysqlFunctionTreeItem`

## ShellMysqlFunctionsTreeItemValue
- 职责："函数"类型节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/name()/graphic()` | 收窄节点/名称/图标 | `I18nHelper.function()`；`FunctionSVGGlyph` + `disableTheme` |
  | `extra()/extraColor()` | 附加数量 | 若 `getFunctionSize()` 非空返回 ` (size)`；`#228B22` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlFunctionTreeItem
- 职责：函数叶子节点，提供设计/重命名/删除/克隆/信息查看与自身数据重载。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `MysqlFunction` | 当前函数对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlFunctionTreeItem(MysqlFunction, RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `parent()/client()/info()/dbItem()` | 上下文取值 | 逐级委托；`dbItem()=parent().parent()` |
  | `getMenuItems()` | 右键菜单 | design/rename/delete、克隆、函数信息 |
  | `functionInfo()` / `cloneFunction()` / `doCloneFunction()` | 信息/克隆 | `ShellMysqlViewFactory.functionInfo`；`dbItem().cloneFunction(...)` → `addFunction` |
  | `delete()` | 删除函数 | 确认后 `dbItem().dropFunction(value)` + `dropFunction` 事件 + `clearFunctionSize()` + `remove()` |
  | `dbName()/infoName()/functionName()/value()` | 库名/连接名/函数名/值 | 委托或取 `value` |
  | `onPrimaryDoubleClick()` | 双击设计 | `ShellMysqlEventUtil.designFunction(value, dbItem())` |
  | `reloadChild()/loadChild()` | 重载/加载详情 | `client().selectFunction(dbName, name)` 后 `value.copy` |
  | `rename()` | 重命名 | 校验后 `dbItem().renameFunction(old, new)` + `functionRenamed` |

- 调用链：`ShellMysqlFunctionTreeItem → ShellMysqlClient`、`ShellMysqlEventUtil`、`ShellMysqlViewFactory.functionInfo`

## ShellMysqlFunctionTreeItemValue
- 职责：函数叶子节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/graphic()/name()` | 收窄节点/图标/名称 | `FunctionSVGGlyph`；`item().functionName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlProceduresTreeItem
- 职责：库下"过程"类型节点，负责过程懒加载、增量同步与新增。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `procedureSize` | `Integer` | 过程数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlProceduresTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `getMenuItems()` | 右键菜单 | `addProcedure`、`refreshData` |
  | `add()` | 新增过程 | 新建 `MysqlProcedure` 设 dbName，`ShellMysqlEventUtil.designProcedure(procedure, parent())` |
  | `loadChild()` | 懒加载过程列表 | `client().selectProceduresSimple(dbName)`；空全量 / 否则 `compare` 三向增量 |
  | `reloadChild()` | 重载 | `clearProcedureSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `dbName()/client()/info()/infoName()` | 委托父节点 | 逐级向上取值 |
  | `procedureSize()/getProcedureSize()` | 过程数量（带缓存） | `client().procedureSize(dbName)` |
  | `addProcedure(MysqlProcedure)` | 追加过程节点 | `addChild + sortChild + clearProcedureSize()` |
  | `clearProcedureSize()` | 清缓存 | `procedureSize=null` |

- 调用链：`ShellMysqlProceduresTreeItem → ShellMysqlClient.selectProceduresSimple → ShellMysqlProcedureTreeItem`

## ShellMysqlProceduresTreeItemValue
- 职责："过程"类型节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/name()/graphic()` | 收窄节点/名称/图标 | `I18nHelper.procedure()`；`ProcedureSVGGlyph` + `disableTheme` |
  | `extra()/extraColor()` | 附加数量 | 若 `getProcedureSize()` 非空返回 ` (size)`；`#228B22` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlProcedureTreeItem
- 职责：过程叶子节点，提供设计/重命名/删除/克隆/信息查看。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `MysqlProcedure` | 当前过程对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlProcedureTreeItem(MysqlProcedure, RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `parent()/client()/info()/dbItem()` | 上下文取值 | 逐级委托 |
  | `getMenuItems()` | 右键菜单 | design/rename/delete、克隆、过程信息 |
  | `procedureInfo()` / `cloneProcedure()` / `doCloneProcedure()` | 信息/克隆 | `ShellMysqlViewFactory.procedureInfo`；`dbItem().cloneProcedure(...)` → `addProcedure` |
  | `delete()` | 删除过程 | 确认后 `dbItem().dropProcedure(value)` + `dropProcedure` 事件 + `clearProcedureSize()` + `remove()` |
  | `dbName()/infoName()/procedureName()/value()` | 库名/连接名/过程名/值 | 委托或取值 |
  | `onPrimaryDoubleClick()` | 双击设计 | `ShellMysqlEventUtil.designProcedure(value, dbItem())` |
  | `rename()` | 重命名 | 校验后 `value.setName(new)` 再 `dbItem().renameProcedure(old, new)` + `procedureRenamed` |

- 调用链：`ShellMysqlProcedureTreeItem → ShellMysqlClient`、`ShellMysqlEventUtil`、`ShellMysqlViewFactory.procedureInfo`

## ShellMysqlProcedureTreeItemValue
- 职责：过程叶子节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/graphic()/name()` | 收窄节点/图标/名称 | `ProcedureSVGGlyph`；`item().procedureName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlEventsTreeItem
- 职责：库下"事件"类型节点，负责事件懒加载、增量同步与新增。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `eventSize` | `Integer` | 事件数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlEventsTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `getMenuItems()` | 右键菜单 | `addEvent`、`refreshData` |
  | `add()` | 新增事件 | 新建 `MysqlEvent` 设 dbName，`ShellMysqlEventUtil.designEvent(event, parent())` |
  | `loadChild()` | 懒加载事件列表 | `client().selectEventsSimple(dbName)`；空全量 / 否则 `compare` 三向增量 |
  | `reloadChild()` | 重载 | `clearEventSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `dbName()/client()/info()/infoName()` | 委托父节点 | 逐级向上取值 |
  | `eventSize()/getEventSize()` | 事件数量（带缓存） | `client().eventSize(dbName)` |
  | `addEvent(MysqlEvent)` | 追加事件节点 | `addChild + sortChild + clearEventSize()` |
  | `clearEventSize()` | 清缓存 | `eventSize=null` |

- 调用链：`ShellMysqlEventsTreeItem → ShellMysqlClient.selectEventsSimple → ShellMysqlEventTreeItem`

## ShellMysqlEventsTreeItemValue
- 职责："事件"类型节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/name()/graphic()` | 收窄节点/名称/图标 | `I18nHelper.event()`；`EventSVGGlyph` + `disableTheme` |
  | `extra()/extraColor()` | 附加数量 | 若 `getEventSize()` 非空返回 ` (size)`；`#228B22` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlEventTreeItem
- 职责：事件叶子节点，提供设计/重命名/删除/克隆/信息查看。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `MysqlEvent` | 当前事件对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlEventTreeItem(MysqlEvent, RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `parent()/client()/info()/dbItem()` | 上下文取值 | 逐级委托 |
  | `getMenuItems()` | 右键菜单 | design/rename/delete、克隆、事件信息 |
  | `eventInfo()` / `cloneEvent()` / `doCloneEvent()` | 信息/克隆 | `ShellMysqlViewFactory.eventInfo`；`dbItem().cloneEvent(...)` → `addEvent` |
  | `delete()` | 删除事件 | 确认后 `dbItem().dropEvent(value)` + `dropEvent` 事件 + `clearEventSize()` + `remove()` |
  | `dbName()/infoName()/eventName()/value()` | 库名/连接名/事件名/值 | 委托或取值 |
  | `onPrimaryDoubleClick()` | 双击设计 | `ShellMysqlEventUtil.designEvent(value, dbItem())` |
  | `rename()` | 重命名 | 校验后 `dbItem().renameEvent(old, new)`，更新 `value` + `refresh()` + `eventRenamed` |

- 调用链：`ShellMysqlEventTreeItem → ShellMysqlClient`、`ShellMysqlEventUtil`、`ShellMysqlViewFactory.eventInfo`

## ShellMysqlEventTreeItemValue
- 职责：事件叶子节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/graphic()/name()` | 收窄节点/图标/名称 | `EventSVGGlyph`；`item().eventName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlQueriesTreeItem
- 职责：库下"查询"类型节点，管理本地持久化的 SQL 查询（`ShellQuery`）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `querySize` | `Integer` | 查询数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlQueriesTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `getMenuItems()` | 右键菜单 | `addQuery`、`refreshData` |
  | `addQuery()` | 新增查询 | `ShellMysqlEventUtil.queryAdd(parent())` |
  | `loadChild()` | 懒加载查询列表 | `ShellQueryStore.INSTANCE.list(infoId, dbName)`，全量 `setChild` |
  | `reloadChild()` | 重载 | `clearQuerySize()+clearChild()+setLoaded(false)+loadChild()` |
  | `addChild(ShellQuery)` | 追加查询节点 | `addChild(new ShellMysqlQueryTreeItem(...))` |
  | `dbName()/client()/info()` | 委托父节点 | 逐级向上取值 |
  | `querySize()/getQuerySize()` | 查询数量（带缓存） | `ShellQueryStore.INSTANCE.list(...)` 数量 |
  | `addQuery(ShellQuery)` | 追加并排序 | `addChild + sortChild + clearQuerySize()` |
  | `clearQuerySize()` | 清缓存 | `querySize=null` |

- 调用链：`ShellMysqlQueriesTreeItem → ShellQueryStore.INSTANCE → ShellMysqlQueryTreeItem`

## ShellMysqlQueriesTreeItemValue
- 职责："查询"类型节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/name()/graphic()` | 收窄节点/名称/图标 | `I18nHelper.queries()`；`QuerySVGGlyph` + `disableTheme` |
  | `extra()/extraColor()` | 附加数量 | 若 `getQuerySize()` 非空返回 ` (size)`；`#228B22` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlQueryTreeItem
- 职责：查询叶子节点，提供打开/重命名/删除已保存的 SQL 查询。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `ShellQuery` | 当前查询对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlQueryTreeItem(ShellQuery, RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `parent()/client()/info()/dbItem()` | 上下文取值 | 逐级委托 |
  | `getMenuItems()` | 右键菜单 | 打开/重命名/删除 |
  | `delete()` | 删除查询 | `ShellQueryStore.INSTANCE.delete(value)`，成功后 `queryDeleted` + `clearQuerySize()` + `remove()` |
  | `rename()` | 重命名 | `ShellQueryStore.INSTANCE.update(value)`，成功 `queryRenamed` + `refresh()`；失败回滚名称 |
  | `dbName()/queryName()/value()` | 库名/查询名/值 | `value.getName()` |
  | `onPrimaryDoubleClick()` | 双击打开 | `ShellMysqlEventUtil.queryOpen(value, dbItem())` |

- 调用链：`ShellMysqlQueryTreeItem → ShellQueryStore.INSTANCE`、`ShellMysqlEventUtil`

## ShellMysqlQueryTreeItemValue
- 职责：查询叶子节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/graphic()/name()` | 收窄节点/图标/名称 | `QuerySVGGlyph`；`item().queryName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellMysqlTerminalTreeItem
- 职责：终端叶子节点，双击打开该库的 SQL 终端。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMysqlTerminalTreeItem(RichTreeView)` | 构造 | `setValue(new ShellMysqlTerminalTreeItemValue())` |
  | `parent()` | 收窄父节点 | `(ShellMysqlDatabaseTreeItem) super.parent()` |
  | `client()` | 客户端 | `parent().client()` |
  | `onPrimaryDoubleClick()` | 双击打开终端 | `ShellMysqlEventUtil.terminalOpen(parent())` |

- 调用链：`ShellMysqlTerminalTreeItem → ShellMysqlEventUtil.terminalOpen`；继承 `RichTreeItem`（非 `ShellMysqlTreeItem`）

## ShellMysqlTerminalTreeItemValue
- 职责：终端节点展示值。
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
