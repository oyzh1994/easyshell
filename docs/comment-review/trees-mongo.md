# 代码审查文档 · trees/mongo（MongoDB 树）

> 范围：`src/main/java/cn/oyzh/easyshell/trees/mongo/` 共 29 个类。
> 说明：由 `ShellMongoTreeView` 初始化根节点 `ShellMongoRootTreeItem`（库层级），库节点懒加载集合/存储桶(GridFS)/函数/用户/查询/终端六类"类型节点"，类型节点再懒加载对应叶子节点。业务逻辑下沉到 `ShellMongoClient`、`ShellMongoViewFactory`、`ShellMongoEventUtil`。

---

## ShellMongoTreeItem
- 职责：MongoDB 树所有节点的抽象基类，统一收窄 `getTreeView()` 返回类型。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoTreeItem(RichTreeView treeView)` | 构造节点 | `super(treeView)` |
  | `ShellMongoTreeView getTreeView()` | 返回 mongo 树视图 | `(ShellMongoTreeView) super.getTreeView()` |

- 调用链：继承 `RichTreeItem<V>`；被全部 MongoDB 树节点继承。

## ShellMongoTreeView
- 职责：MongoDB 资源树视图，持有客户端并初始化根节点。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `client` | `ShellMongoClient` | MongoDB 客户端 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setClient(ShellMongoClient)` / `getClient()` | 设置/获取客户端 | 简单存取 |
  | `ShellMongoTreeItemFilter getItemFilter()` | 懒初始化过滤器 | `new ShellMongoTreeItemFilter()` |
  | `ShellMongoTreeView()` | 构造视图 | `dragContent="mongo_tree_drag"`、单选、`RichTreeCell` 工厂；`super.setRoot(new ShellMongoRootTreeItem(this))` + `root().expend()` |
  | `ShellMongoRootTreeItem root()` | 收窄根节点类型 | `(ShellMongoRootTreeItem) super.root()` |

- 调用链：`ShellMongoTreeView → ShellMongoRootTreeItem → ShellMongoDatabaseTreeItem`

## ShellMongoTreeItemFilter
- 职责：MongoDB 树节点的关键字过滤判定。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean test(RichTreeItem<?> item)` | 判断节点是否命中关键字 | 不可过滤节点（`!isFilterable()`）直接放行；根/用户/存储桶/查询/库/函数/集合类型节点恒 true；对 `ShellMongoTreeItem<?>` 取 `value.name()` 调 `TextUtil.findText` 比较 `NOT_FOUND` |

- 调用链：继承 `RichTreeItemFilter`；使用 `TextUtil.findText`

## ShellMongoRootTreeItem
- 职责：MongoDB 树根节点，承载数据库层级的增删查与加载。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoRootTreeItem(ShellMongoTreeView)` | 构造根节点 | `setValue(new ShellMongoRootTreeItemValue())` |
  | `getClient()` / `connect()` | 客户端/连接信息 | `getTreeView().getClient()`；`getClient().getShellConnect()` |
  | `existDatabase(String)` / `createDatabase(String)` / `dropDatabase(String)` | 库存在性/创建/删除 | 委托 `getClient()` 同名方法 |
  | `addDatabase()` | 弹出新增库 | `MessageBox.prompt` 输入库名，校验存在性后 `createDatabase`，`getClient().database(name)` 后 `addChild(new ShellMongoDatabaseTreeItem(...))` |
  | `reloadChild()` | 重载 | `clearChild()` + `loadChild()` |
  | `loadChild()` | 加载全部库 | `getClient().listDatabases()` 构造子节点，`setChild` 后 `expend/doFilter/doSort` |
  | `clearChild()` | 清空子节点前关闭库 | 遍历子节点调 `ShellMongoDatabaseTreeItem.closeDB()`，再 `super.clearChild()` |
  | `getMenuItems()` | 右键菜单 | `addDatabase` / `reloadDatabase` |

- 调用链：`ShellMongoRootTreeItem → ShellMongoClient → ShellMongoDatabaseTreeItem`

## ShellMongoRootTreeItemValue
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

## ShellMongoDatabaseTreeItem
- 职责：库节点，作为集合/存储桶/函数/用户/查询/终端类型节点的父容器，并集中代理该库下数据操作到客户端。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `MongoDatabase` | 当前数据库对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoDatabaseTreeItem(MongoDatabase, RichTreeView)` | 构造库节点 | `setSortable(false)`、`setFilterable(true)`、`setValue(new ShellMongoDatabaseTreeItemValue(this))` |
  | `parent()` | 收窄父节点 | `(ShellMongoRootTreeItem) super.parent()` |
  | `dbName()` / `userName()` / `info()` / `infoName()` / `connectName()` / `shellConnect()` | 库名/用户名/连接信息 | `value.getName()`；`info()` 取 `parent().connect()` |
  | `client()` | 客户端 | `parent().getClient()` |
  | `getMenuItems()` | 右键菜单 | 非空含"关闭"；`delete`、`dumpData/runScriptFile/transportData` |
  | `dump()` / `runScriptFile()` / `transportData()` | 转储/运行脚本/传输 | `ShellMongoViewFactory.dumpData/runScriptFile/transportData` |
  | `delete()` | 删除库 | `Task` 内确认后 `parent().dropDatabase()`，成功 `ShellMongoEventUtil.databaseDropped` |
  | `closeDB()` | 关闭库 | `clearChild()+collapse()+setLoaded(false)` + `ShellMongoEventUtil.databaseClosed` |
  | `loadChild()` | 懒加载类型节点 | `Task` 内依次 `new ShellMongoCollectionsTreeItem/Buckets/Functions/Users/Queries/TerminalTreeItem`，`setChild` 后 `expend` |
  | `getQueryTypeChild()` / `getFunctionTypeChild()` | 取查询/函数类型节点 | 遍历 `richChildren()` 按 `instanceof` 匹配 |
  | `onPrimaryDoubleClick()` | 双击展开 | 未加载则 `loadChild()` |
  | `dropCollection/clearCollection/renameCollection(String...)/listCollectionNames()` | 集合删/清/重命名/列名 | 委托 `client()` |
  | `dropBucket/clearBucket/listBucketNames(String)` | 存储桶删/清/列名 | 委托 `client()` |
  | `executeSingleScript(String)` / `executeScript(String)` | 执行脚本 | `client().executeSingleScript/executeScript`，后者返回 `DBQueryResults<ShellMongoExecuteResult>` |
  | `insertCollectionRecord/deleteCollectionRecord/updateCollectionRecord(MongoRecord)` | 集合记录增删改 | 委托 `client()` |
  | `selectCollectionRecord(String,Object)` | 查询单条集合记录 | `client().selectCollectionRecord(dbName, collection, id)` |
  | `dropFunction/renameFunction(String,String)/selectFunction(String)/createFunction/alertFunction` | 函数 CRUD | 委托 `client()`；`createFunction/alertFunction` 传 `getName()`、`getCode()` |
  | `userSize()` / `listDatabaseNames()` / `listCollectionNames()` | 用户数量/库名列表/集合名列表 | 委托 `client()` |
  | `createUser(MongoUser)` / `dropUser(String)` | 创建/删除用户 | 委托 `client()` |
  | `eval(String)` | 执行脚本 | `client().eval(dbName, script)` |

- 调用链：`ShellMongoDatabaseTreeItem → ShellMongoClient`（几乎全部数据操作）→ `ShellMongoViewFactory` / `ShellMongoEventUtil`。**注意**：含被注释的 `editDB()` 与 `doFilter` 重写。

## ShellMongoDatabaseTreeItemValue
- 职责：库节点的展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()` | 收窄节点 | `(ShellMongoDatabaseTreeItem) super.item()` |
  | `name()` | 名称 | `item().dbName()` |
  | `graphic()` | 图标 | 懒创建 `DatabaseSVGGlyph` 并 `disableTheme()` |
  | `graphicColor()` | 图标色 | 有子节点返回 `Color.GREEN` |

- 调用链：继承 `RichTreeItemValue`

## ShellMongoCollectionsTreeItem
- 职责：库下"集合"类型节点，负责集合懒加载、增量同步、新增与导入导出。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `collectionSize` | `Integer` | 集合数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoCollectionsTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `parent()` | 收窄父节点 | `(ShellMongoDatabaseTreeItem) super.parent()` |
  | `getMenuItems()` | 右键菜单 | `addCollection`、`reloadData`、`exportData`、`importData` |
  | `exportData()` / `importData()` | 导出/导入 | `ShellMongoViewFactory.exportData/importData(client, dbName)` |
  | `addCollection()` | 新增集合 | `MessageBox.prompt` 名称，校验 `existCollection` 后 `createCollection`，`addCollection(collection)` |
  | `loadChild()` | 懒加载集合列表 | `client().listCollections(dbName)`；空全量 / 否则 `compare` 三向增量；`onSuccess` refresh、`onFinish` filter/sort |
  | `reloadChild()` | 重载 | `clearCollectionSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `dbName()/client()/info()/infoName()` | 委托父节点 | 逐级向上取值 |
  | `collectionSize()/getCollectionsSize()` | 集合数量（带缓存） | `parent().listCollectionNames().size()` |
  | `addCollection(MongoCollection)` | 追加集合节点 | `addChild + sortChild + clearCollectionSize()` |
  | `clearCollectionSize()` | 清缓存 | `collectionSize=null` |

- 调用链：`ShellMongoCollectionsTreeItem → ShellMongoClient.listCollections → ShellMongoCollectionTreeItem`

## ShellMongoCollectionsTreeItemValue
- 职责："集合"类型节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/name()/graphic()` | 收窄节点/名称/图标 | `I18nHelper.collections()`；`TableSVGGlyph` + `disableTheme` |
  | `extra()/extraColor()` | 附加数量 | 若 `getCollectionsSize()` 非空返回 ` (size)`；`#228B22` |

- 调用链：继承 `RichTreeItemValue`

## ShellMongoCollectionTreeItem
- 职责：集合叶子节点，提供打开/重命名/清空/删除/转储/导出及记录 CRUD 与脚本执行。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `MongoCollection` | 当前集合对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoCollectionTreeItem(MongoCollection, RichTreeView)` | 构造 | `setValue(new ShellMongoCollectionTreeItemValue(this))` |
  | `parent()/client()/dbName()/collectionName()/info()/infoName()` | 上下文取值 | 逐级委托；`collectionName()=value.getName()` |
  | `getMenuItems()` | 右键菜单 | 打开/重命名/清空/删除、转储/导出 |
  | `dump()` / `export()` | 转储/导出 | `ShellMongoViewFactory.dumpData/exportData(client, dbName, collection, ...)` |
  | `clearCollection()` | 清空集合 | 确认后 `dbItem().clearCollection(name)` + `parent().reloadChild()` |
  | `delete()` | 删除集合 | 确认后 `dbItem().dropCollection(name)` + `collectionDropped` + `clearCollectionSize()` + `remove()` |
  | `rename()` | 重命名 | 校验后 `dbItem().renameCollection(old, new)` + `collectionRenamed` |
  | `dbItem()` | 所属库节点 | `parent().parent()` |
  | `onPrimaryDoubleClick()` | 双击打开 | `ShellMongoEventUtil.collectionOpen(this, dbItem())` |
  | `reloadChild()` | 重载 | `clearChild()+setLoaded(false)+loadChild()` |
  | `recordPage(long,long,List<MongoRecordFilter>,MongoColumns)` | 分页查询记录 | 组装 `MongoSelectRecordParam`，`client().selectCollectionRecords` + `selectCollectionRecordCount`，返回 `Paging<MongoRecord>` |
  | `insertRecord/deleteRecord/updateRecord(MongoRecord)` | 记录增删改 | 委托 `dbItem()`（→库节点） |
  | `selectCollectionRecord(Object)` / `eval(String)` | 查询单条/执行脚本 | 委托 `dbItem()` |
  | `value()` | 取集合值 | 返回 `value` |

- 调用链：`ShellMongoCollectionTreeItem → ShellMongoDatabaseTreeItem → ShellMongoClient`、`ShellMongoEventUtil`、`ShellMongoViewFactory`

## ShellMongoCollectionTreeItemValue
- 职责：集合叶子节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/graphic()/name()` | 收窄节点/图标/名称 | `TableSVGGlyph`；`item().collectionName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellMongoBucketsTreeItem
- 职责：库下 GridFS"存储桶"类型节点，负责存储桶懒加载、增量同步与新增。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `bucketsSize` | `Integer` | 存储桶数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoBucketsTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `parent()` | 收窄父节点 | `(ShellMongoDatabaseTreeItem) super.parent()` |
  | `getMenuItems()` | 右键菜单 | `addBucket`、`reloadData` |
  | `addBucket()` | 新增存储桶 | `MessageBox.prompt` 名称，校验 `existBucket` 后 `createBucket`，`addBucket(bucket)` |
  | `loadChild()` | 懒加载存储桶列表 | `client().listBuckets(dbName)`；空全量 / 否则 `compare` 三向增量 |
  | `reloadChild()` | 重载 | `clearBucketsSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `dbName()/client()/info()/infoName()` | 委托父节点 | 逐级向上取值 |
  | `addBucket(MongoBucket)` | 追加存储桶节点 | `addChild + sortChild + clearBucketsSize()` |
  | `bucketsSize()/getBucketsSize()` | 存储桶数量（带缓存） | `parent().listBucketNames().size()` |
  | `clearBucketsSize()` | 清缓存 | `bucketsSize=null` |

- 调用链：`ShellMongoBucketsTreeItem → ShellMongoClient.listBuckets → ShellMongoBucketTreeItem`

## ShellMongoBucketsTreeItemValue
- 职责：GridFS 存储桶类型节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/graphic()` | 收窄节点/图标 | `BucketSVGGlyph` + `disableTheme` |
  | `name()` | 名称 | 固定返回 `"GridFS"` |
  | `extra()/extraColor()` | 附加数量 | 若 `getBucketsSize()` 非空返回 ` (size)`；`#228B22` |

- 调用链：继承 `RichTreeItemValue`

## ShellMongoBucketTreeItem
- 职责：存储桶叶子节点，提供打开/清空/删除及 GridFS 文件的分页查询/上传/下载/删除/更新与脚本执行。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `MongoBucket` | 当前存储桶对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoBucketTreeItem(MongoBucket, RichTreeView)` | 构造 | `setValue(new ShellMongoBucketTreeItemValue(this))` |
  | `parent()/client()/dbName()/bucketName()/info()/infoName()` | 上下文取值 | 逐级委托；`bucketName()=value.getName()` |
  | `getMenuItems()` | 右键菜单 | 打开/清空/删除 |
  | `clearBucket()` | 清空存储桶 | 确认后 `dbItem().clearBucket(name)` + `parent().reloadChild()` |
  | `delete()` | 删除存储桶 | 确认后 `dbItem().dropBucket(name)` + `bucketDropped` + `clearBucketsSize()` + `remove()` |
  | `dbItem()` | 所属库节点 | `parent().parent()` |
  | `onPrimaryDoubleClick()` | 双击打开 | `ShellMongoEventUtil.bucketOpen(this, dbItem())` |
  | `recordPage(long,long,List<MongoRecordFilter>,MongoColumns)` | 分页查询存储桶文件 | 组装 `MongoSelectRecordParam`，`client().selectBucketRecords` + `selectBucketRecordCount`，返回 `Paging<MongoBucketFile>` |
  | `uploadRecord(File)` / `selectRecord(Object)` / `downloadRecord(Object,String)` / `deleteRecord(Object)` / `updateRecord(MongoBucketFile)` | 文件增查/下载/删/改 | 委托 `client()` 对应 `*BucketRecord` 方法 |
  | `eval(String)` | 执行脚本 | `client().eval(dbName, script)` |
  | `value()` | 取存储桶值 | 返回 `value` |

- 调用链：`ShellMongoBucketTreeItem → ShellMongoDatabaseTreeItem/ShellMongoClient`（GridFS 文件操作）、`ShellMongoEventUtil`

## ShellMongoBucketTreeItemValue
- 职责：存储桶叶子节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/graphic()/name()` | 收窄节点/图标/名称 | `BucketSVGGlyph`；`item().bucketName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellMongoFunctionsTreeItem
- 职责：库下"函数"类型节点，负责函数懒加载、增量同步与新增。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `functionSize` | `Integer` | 函数数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoFunctionsTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `getMenuItems()` | 右键菜单 | `addFunction`、`refreshData` |
  | `add()` | 新增函数 | 新建 `MongoFunction` 设 dbName，`ShellMongoEventUtil.designFunction(function, parent())` |
  | `loadChild()` | 懒加载函数列表 | `client().listFunctions(dbName)`；空全量 / 否则 `compare` 三向增量 |
  | `reloadChild()` | 重载 | `clearFunctionSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `dbName()/client()/info()/infoName()` | 委托父节点 | 逐级向上取值 |
  | `functionSize()/getFunctionSize()` | 函数数量（带缓存） | `client().functionSize(dbName)` |
  | `addFunction(MongoFunction)` | 追加函数节点 | `addChild + sortChild + clearFunctionSize()` |
  | `clearFunctionSize()` | 清缓存 | `functionSize=null` |

- 调用链：`ShellMongoFunctionsTreeItem → ShellMongoClient.listFunctions → ShellMongoFunctionTreeItem`

## ShellMongoFunctionsTreeItemValue
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

## ShellMongoFunctionTreeItem
- 职责：函数叶子节点，提供设计/重命名/删除/克隆/信息查看与自身数据重载。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `MongoFunction` | 当前函数对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoFunctionTreeItem(MongoFunction, RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `parent()/client()/info()/dbItem()` | 上下文取值 | 逐级委托 |
  | `getMenuItems()` | 右键菜单 | design/rename/delete、克隆 |
  | `cloneFunction()` | 克隆函数 | 用 `ShellMongoUtil.genCloneName()` 生成名，新建 `MongoFunction`（复制 code），`dbItem().createFunction` → `addFunction` |
  | `delete()` | 删除函数 | 确认后 `dbItem().dropFunction(value)` + `dropFunction` 事件 + `clearFunctionSize()` + `remove()` |
  | `dbName()/infoName()/functionName()/value()` | 库名/连接名/函数名/值 | 委托或取值 |
  | `onPrimaryDoubleClick()` | 双击设计 | `ShellMongoEventUtil.designFunction(value, dbItem())` |
  | `reloadChild()/loadChild()` | 重载/加载详情 | `client().selectFunction(dbName, name)` 后 `value.copy` |
  | `rename()` | 重命名 | 校验后 `dbItem().renameFunction(old, new)` + `functionRenamed` |

- 调用链：`ShellMongoFunctionTreeItem → ShellMongoClient`、`ShellMongoEventUtil`

## ShellMongoFunctionTreeItemValue
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

## ShellMongoUsersTreeItem
- 职责：库下"用户"类型节点，负责用户懒加载、增量同步与新增。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `userSize` | `Integer` | 用户数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoUsersTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `getMenuItems()` | 右键菜单 | `createUser`、`reloadData` |
  | `createUser()` | 创建用户 | `ShellMongoViewFactory.userCreate(parent())`，取 `user` 后 `addUser(user)` |
  | `loadChild()` | 懒加载用户列表 | `client().listUsers(dbName)`；空全量 / 否则 `compare` 三向增量 |
  | `reloadChild()` | 重载 | `clearUserSize()+clearChild()+setLoaded(false)+loadChild()` |
  | `dbName()/client()/info()/infoName()` | 委托父节点 | 逐级向上取值 |
  | `addUser(MongoUser)` | 追加用户节点 | `addChild + sortChild + clearUserSize()` |
  | `userSize()/getCollectionsSize()` | 用户数量（带缓存） | `parent().userSize()`（**注意：取值方法名为 getCollectionsSize，疑为复制遗留**） |
  | `clearUserSize()` | 清缓存 | `userSize=null` |

- 调用链：`ShellMongoUsersTreeItem → ShellMongoClient.listUsers → ShellMongoUserTreeItem`、`ShellMongoViewFactory.userCreate`

## ShellMongoUsersTreeItemValue
- 职责："用户"类型节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/name()/graphic()` | 收窄节点/名称/图标 | `I18nHelper.users()`；`UserSVGGlyph` + `disableTheme` |
  | `extra()/extraColor()` | 附加数量 | 若 `getCollectionsSize()` 非空返回 ` (size)`；`#228B22` |

- 调用链：继承 `RichTreeItemValue`

## ShellMongoUserTreeItem
- 职责：用户叶子节点，提供查看/删除用户。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `MongoUser` | 当前用户对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoUserTreeItem(MongoUser, RichTreeView)` | 构造 | `setValue(new ShellMongoUserTreeItemValue(this))` |
  | `parent()/client()/dbName()/userName()/info()/infoName()` | 上下文取值 | 逐级委托；`userName()=value.getUser()` |
  | `getMenuItems()` | 右键菜单 | `viewUser`、`deleteUser` |
  | `viewUser()` | 查看用户 | `ShellMongoEventUtil.userView(value, dbItem())` |
  | `delete()` | 删除用户 | 确认后 `dbItem().dropUser(name)` + `userDeleted` + `clearUserSize()` + `remove()` |
  | `dbItem()` | 所属库节点 | `parent().parent()` |
  | `onPrimaryDoubleClick()` | 双击查看 | `viewUser()` |
  | `reloadChild()` | 重载 | `clearChild()+setLoaded(false)+loadChild()` |
  | `value()` | 取用户值 | 返回 `value` |

- 调用链：`ShellMongoUserTreeItem → ShellMongoDatabaseTreeItem.dropUser`、`ShellMongoEventUtil`

## ShellMongoUserTreeItemValue
- 职责：用户叶子节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `item()/graphic()/name()` | 收窄节点/图标/名称 | `UserSVGGlyph`；`item().userName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellMongoQueriesTreeItem
- 职责：库下"查询"类型节点，管理本地持久化的 SQL 查询（`ShellQuery`）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `querySize` | `Integer` | 查询数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoQueriesTreeItem(RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `getMenuItems()` | 右键菜单 | `addQuery`、`refreshData` |
  | `addQuery()` | 新增查询 | `ShellMongoEventUtil.queryAdd(parent())` |
  | `loadChild()` | 懒加载查询列表 | `ShellQueryStore.INSTANCE.list(infoId, dbName)`，全量 `setChild` |
  | `reloadChild()` | 重载 | `clearQuerySize()+clearChild()+setLoaded(false)+loadChild()` |
  | `addChild(ShellQuery)` | 追加查询节点 | `addChild(new ShellMongoQueryTreeItem(...))` |
  | `dbName()/client()/info()/shellConnect()` | 委托父节点 | 逐级向上取值 |
  | `querySize()/getQuerySize()` | 查询数量（带缓存） | `ShellQueryStore.INSTANCE.list(...)` 数量 |
  | `addQuery(ShellQuery)` | 追加并排序 | `addChild + sortChild + clearQuerySize()` |
  | `clearQuerySize()` | 清缓存 | `querySize=null` |

- 调用链：`ShellMongoQueriesTreeItem → ShellQueryStore.INSTANCE → ShellMongoQueryTreeItem`

## ShellMongoQueriesTreeItemValue
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

## ShellMongoQueryTreeItem
- 职责：查询叶子节点，提供打开/重命名/删除已保存的查询。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `ShellQuery` | 当前查询对象 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoQueryTreeItem(ShellQuery, RichTreeView)` | 构造 | `setFilterable(true)`、`setValue(...)` |
  | `parent()/client()/info()/dbItem()/shellConnect()` | 上下文取值 | 逐级委托 |
  | `getMenuItems()` | 右键菜单 | 打开/重命名/删除 |
  | `delete()` | 删除查询 | `ShellQueryStore.INSTANCE.delete(value)`，成功后 `queryDeleted` + `clearQuerySize()` + `remove()` |
  | `rename()` | 重命名 | `ShellQueryStore.INSTANCE.update(value)`，成功 `queryRenamed` + `refresh()`；失败回滚名称 |
  | `dbName()/queryName()/value()` | 库名/查询名/值 | `value.getName()` |
  | `onPrimaryDoubleClick()` | 双击打开 | `ShellMongoEventUtil.queryOpen(value, dbItem())` |

- 调用链：`ShellMongoQueryTreeItem → ShellQueryStore.INSTANCE`、`ShellMongoEventUtil`

## ShellMongoQueryTreeItemValue
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

## ShellMongoTerminalTreeItem
- 职责：终端叶子节点，双击打开该库的脚本终端。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellMongoTerminalTreeItem(RichTreeView)` | 构造 | `setValue(new ShellMongoTerminalTreeItemValue())` |
  | `parent()` | 收窄父节点 | `(ShellMongoDatabaseTreeItem) super.parent()` |
  | `shellConnect()` / `client()` | 连接信息/客户端 | `parent().shellConnect()`；`parent().client()` |
  | `onPrimaryDoubleClick()` | 双击打开终端 | `ShellMongoEventUtil.terminalOpen(client(), parent().dbName())` |

- 调用链：`ShellMongoTerminalTreeItem → ShellMongoEventUtil.terminalOpen`；继承 `RichTreeItem`（非 `ShellMongoTreeItem`）

## ShellMongoTerminalTreeItemValue
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
