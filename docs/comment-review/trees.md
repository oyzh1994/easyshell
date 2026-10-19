# 代码审查文档 · trees（数据库/中间件资源树）

> 范围：`src/main/java/cn/oyzh/easyshell/trees/` 下各资源树（**不含**顶层 `connect/`、`query/`、`snippet/`，这三者见 `trees-connect.md`、`trees-query-snippet.md`）。共 **119** 个类。
>
> 结构一致：`ShellXxxTreeView`（持客户端）→ `ShellXxxRootTreeItem`（根/库/模式层）→ 类型节点（懒加载子节点）→ 叶子节点。业务逻辑下沉到 `ShellXxxClient`（数据 CRUD）、`ShellXxxViewFactory`（弹窗）、`ShellXxxEventUtil`（事件）。`*TreeItemValue` 为节点展示值（名称/图标/额外文本/颜色）；`ShellXxxTreeItem`（抽象基类）仅收窄 `getTreeView()` 返回类型。
>
> 每棵树的详细逐类审查见对应分册：

| 树 | 说明 | 类数 | 文件 |
|---|---|---|---|
| dameng | 达梦数据库树（根=模式） | 29 | [trees-dameng.md](./trees-dameng.md) |
| mysql | MySQL 数据库树（根=库） | 33 | [trees-mysql.md](./trees-mysql.md) |
| mongo | MongoDB 树（根=库） | 29 | [trees-mongo.md](./trees-mongo.md) |
| redis | Redis 树（集群/普通双根） | 19 | [trees-redis.md](./trees-redis.md) |
| zk | ZooKeeper 节点树 | 9 | [trees-zk.md](./trees-zk.md) |

## 关键调用链（跨树共性）

- 视图初始化：`ShellXxxTreeView` 构造 → `setRoot(new ShellXxxRootTreeItem(this))` → `root().expend()`（redis 按集群/普通模式选择不同根）。
- 根加载：`RootTreeItem.loadChild()` → `ShellXxxClient.selectSchemas()/databases()` → 建库/模式节点。
- 库/模式节点加载类型节点：`DatabaseTreeItem.loadChild()` → `new ShellXxxTables/Views/Functions/Procedures/...TreeItem(...)`。
- 类型节点懒加载叶子：`TaskBuilder` 内 `Client.selectXxxSimple()` → 空则全量 `setChild`，否则按 `compare` 删除/新增/更新三向增量 → `onFinish` 执行 `doFilter/doSort`。
- 叶子操作：`叶子.reloadChild()` → `Client.selectXxx()` → `value.copy(...)`；双击 → `ShellXxxEventUtil.xxxOpen()` → 打开对应 Tab。
- 集合/库节点删除后：`ShellXxxEventUtil.xxxDropped()` 广播 + `parent().clearXxxSize()` 清数量缓存 + `remove()`。

## 审查附注（共性可疑点）

- `ShellDamengTreeItemFilter.test` 白名单误用 `ShellMysqlRootTreeItem`（复制粘贴遗留），达梦根节点未被豁免过滤。
- `hasPrimaryKey()`（dameng/mysql 表节点）返回 `columns.primaryKeys().isEmpty()`，命名与语义相反。
- 视图重命名（dameng/mysql `ShellXxxViewTreeItem.rename()`）调用的是 `dbItem().renameTable(...)`。
- `ShellMongoUsersTreeItem` 的"带缓存数量"取值方法名为 `getCollectionsSize()`（疑为复制遗留）。
- 各树节点内散落大量被注释的事件/搜索/位标志死代码（如 `ShellZKNodeTreeItem`、`ShellZKTreeView`、`ShellDamengSchemaTreeItem`）。
