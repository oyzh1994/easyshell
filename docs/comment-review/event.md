# easyshell event 包代码审查

范围：`easyshell/src/main/java/cn/oyzh/easyshell/event/`（递归全部）。
共覆盖 118 个正式类（另有 12 个整文件被注释掉的死代码仅列于文末清单，不展开）。

事件包按“工具类发布事件 + 事件对象承载数据”的模式组织：

- 各类 `ShellXxxEventUtil`：静态方法工厂，`new 事件对象 → data()/setXxx() → EventUtil.post/postSync/postAsync`。
- 各事件对象：继承 `cn.oyzh.event.Event<T>`（`T` 即 `data()` 的泛型类型），实现 `EventFormatter` 者可返回可读描述 `eventFormat()`。
- 通用事件根类集中在 `event` 根包，数据库类事件按产品（dameng/mysql/mongo/redis/zk）分包。

---

# 一、通用/根事件（cn.oyzh.easyshell.event）

## ShellEventUtil
- 职责：通用界面与业务事件的发布入口（连接、分组、密钥、窗口、终端、布局、片段、Docker 等）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 纯静态方法工具类，无字段 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static void connectionOpened(ShellConnect connect)` | 连接打开事件 | `new ShellConnectOpenedEvent` → `event.data(connect)` → `EventUtil.postSync` |
  | `static void connectEdit(ShellConnect connect)` | 连接编辑事件 | `ShellConnectEditEvent` + `EventUtil.post` |
  | `static void connectionClosed(ShellBaseClient client)` | 连接关闭事件 | `ShellConnectionClosedEvent` + `post` |
  | `static void connectionConnected(ShellBaseClient client)` | 连接成功事件 | `ShellConnectionConnectedEvent` + `post` |
  | `static void connectAdded(ShellConnect shellConnect)` | 连接已新增 | `ShellConnectAddedEvent` + `post` |
  | `static void connectUpdated(ShellConnect shellConnect)` | 连接已修改 | `ShellConnectUpdatedEvent` + `post` |
  | `static void connectDeleted(ShellConnect shellConnect)` | 连接已删除 | `ShellConnectDeletedEvent` + `post` |
  | `static void addGroup()` | 添加分组 | `EventUtil.post(new ShellAddGroupEvent())` |
  | `static void changelog()` | 更新日志 | `EventUtil.post(new ChangelogEvent())` |
  | `static void treeItemChanged(TreeItem<?> item)` | 节点选中变更 | `ShellTreeItemChangedEvent` + `post` |
  | `static void layout1()` | 布局1 | `post(new Layout1Event())` |
  | `static void layout2()` | 布局2 | `post(new Layout2Event())` |
  | `static void groupAdded(String group)` | 分组已新增 | `ShellGroupAddedEvent` + `post` |
  | `static void groupDeleted(String group)` | 分组已删除 | `ShellGroupDeletedEvent` + `post` |
  | `static void groupRenamed(String group, String oldName)` | 分组已更名 | `ShellGroupRenamedEvent`，`setOldName(oldName)` + `post` |
  | `static void showKey()` | 显示密钥管理 | `post(new ShellShowKeyEvent())` |
  | `static void showMessage()` | 显示消息页面 | `post(new ShellShowMessageEvent())` |
  | `static void dataImported()` | 数据导入 | `post(new ShellDataImportedEvent())` |
  | `static void fileDragged(List<File> files)` | 文件已拖拽 | `ShellFileDraggedEvent` + `post` |
  | `static void showTerminal()` | 打开终端页面 | `post(new ShellShowTerminalEvent())` |
  | `static void keyAdded(ShellKey shellKey)` | 密钥已新增 | `ShellKeyAddedEvent` + `post` |
  | `static void keyUpdated(ShellKey shellKey)` | 密钥已修改 | `ShellKeyUpdatedEvent` + `post` |
  | `static void clientAction(String connectName, String action)` | 客户端操作 | `ShellClientActionEvent`，`setAction` + `EventUtil.postAsync` |
  | `static void showSplit(String type, List<ShellConnect> connects)` | 显示分屏页面 | `ShellShowSplitEvent`，`setConnects` + `post` |
  | `static void runSnippet(String content, boolean runAll)` | 执行片段 | `ShellRunSnippetEvent`，`setRunAll` + `postAsync` |
  | `static void containerRun(ShellDockerExec exec)` | 容器运行 | `ShellContainerRunEvent` + `postAsync` |
  | `static void containerCommit(ShellDockerExec exec)` | 容器保存 | `ShellContainerCommitEvent` + `postAsync` |
  | `static void imageTag(ShellDockerExec exec)` | 镜像标签修改 | `ShellImageTagEvent` + `postAsync` |
  | `static void printSql(String sql, ShellConnect connect)` | 打印 sql | `ShellPrintSqlEvent`，`setConnect` + `post` |

- 说明：源码中含大量注释掉的旧事件方法（redis/zk/导入导出/跳板等），已被新工具类 `ShellRedisEventUtil`/`ShellZKEventUtil` 等替代。
- 调用链：`调用方 → ShellEventUtil.xxx → new ShellXxxEvent → event.data()/setXxx() → EventUtil.post/postSync/postAsync → 订阅方`

## ShellClientActionEvent
- 职责：通用客户端操作事件（记录“连接名 > 操作”）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | action | String | 操作描述 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getAction()/void setAction(String)` | 操作存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `data() + " > " + action` |

- 调用链：`ShellEventUtil.clientAction → ShellClientActionEvent.eventFormat`

---

# 二、连接/分组/密钥等通用事件

## ShellConnectAddedEvent
- 职责：连接已新增事件。
- 字段：无（数据即 `data()`，类型 `ShellConnect`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `String.format("[%s:%s] added", I18nHelper.connect(), data().getName())` |
- 调用链：`ShellEventUtil.connectAdded → ShellConnectAddedEvent.eventFormat`

## ShellConnectDeletedEvent
- 职责：连接已删除事件。
- 字段：无（`data()` 为 `ShellConnect`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[连接:name deleted]` |
- 调用链：`ShellEventUtil.connectDeleted → ShellConnectDeletedEvent`

## ShellConnectEditEvent
- 职责：连接编辑事件。
- 字段：无（`data()` 为 `ShellConnect`）。
- 方法：无（仅继承 `Event<ShellConnect>`）。
- 调用链：`ShellEventUtil.connectEdit → ShellConnectEditEvent`

## ShellConnectOpenedEvent
- 职责：连接打开事件。
- 字段：无（`data()` 为 `ShellConnect`）。
- 方法：无。
- 调用链：`ShellEventUtil.connectionOpened → ShellConnectOpenedEvent`

## ShellConnectUpdatedEvent
- 职责：连接已修改事件。
- 字段：无（`data()` 为 `ShellConnect`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[连接:name updated]` |
- 调用链：`ShellEventUtil.connectUpdated → ShellConnectUpdatedEvent`

## ShellConnectionClosedEvent
- 职责：连接已关闭事件。
- 字段：无（`data()` 为 `ShellBaseClient`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[连接:connectName closed]` |
  | `ShellConnect connect()` | 取连接 | `data().getShellConnect()` |
- 调用链：`ShellEventUtil.connectionClosed → ShellConnectionClosedEvent.connect`

## ShellConnectionConnectedEvent
- 职责：连接成功事件。
- 字段：无（`data()` 为 `ShellBaseClient`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[连接:connectName connected]` |
  | `ShellConnect connect()` | 取连接 | `data().getShellConnect()` |
- 调用链：`ShellEventUtil.connectionConnected → ShellConnectionConnectedEvent.connect`

## ShellDataImportedEvent
- 职责：数据导入事件。
- 字段：无（`data()` 为 `Object`）。
- 方法：无。
- 调用链：`ShellEventUtil.dataImported → ShellDataImportedEvent`

## ShellPrintSqlEvent（cn.oyzh.easyshell.event.db）
- 职责：打印 sql 事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | connect | ShellConnect | 关联连接 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void setConnect(ShellConnect)` | 设置连接 | 简单存储 |
  | `String eventFormat()` | 事件描述 | `" " + connect.getName() + " > " + data()` |
- 调用链：`ShellEventUtil.printSql → ShellPrintSqlEvent.eventFormat`

## ShellContainerCommitEvent
- 职责：Docker 容器保存（commit）事件。
- 字段：无（`data()` 为 `ShellDockerExec`）。
- 方法：无。
- 调用链：`ShellEventUtil.containerCommit → ShellContainerCommitEvent`

## ShellContainerRunEvent
- 职责：Docker 容器运行事件。
- 字段：无（`data()` 为 `ShellDockerExec`）。
- 方法：无。
- 调用链：`ShellEventUtil.containerRun → ShellContainerRunEvent`

## ShellImageTagEvent
- 职责：Docker 镜像标签事件。
- 字段：无（`data()` 为 `ShellDockerExec`）。
- 方法：无。
- 调用链：`ShellEventUtil.imageTag → ShellImageTagEvent`

## ShellFileDraggedEvent
- 职责：文件已拖拽事件。
- 字段：无（`data()` 为 `List<File>`）。
- 方法：无。
- 调用链：`ShellEventUtil.fileDragged → ShellFileDraggedEvent`

## ShellAddGroupEvent
- 职责：添加分组事件。
- 字段：无（`data()` 为 `Object`）。
- 方法：无。
- 调用链：`ShellEventUtil.addGroup → ShellAddGroupEvent`

## ShellGroupAddedEvent
- 职责：分组已新增事件。
- 字段：无（`data()` 为 `String` 分组名）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[文件夹:name added]`（`I18nHelper.folder()`） |
- 调用链：`ShellEventUtil.groupAdded → ShellGroupAddedEvent`

## ShellGroupDeletedEvent
- 职责：分组已删除事件。
- 字段：无（`data()` 为 `String`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[文件夹:name deleted]` |
- 调用链：`ShellEventUtil.groupDeleted → ShellGroupDeletedEvent`

## ShellGroupRenamedEvent
- 职责：分组已更名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | oldName | String | 旧名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getOldName()/void setOldName(String)` | 旧名称存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `[文件夹:name renamed from oldName]` |
- 调用链：`ShellEventUtil.groupRenamed → ShellGroupRenamedEvent`

## ShellKeyAddedEvent
- 职责：密钥已新增事件。
- 字段：无（`data()` 为 `ShellKey`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[密钥:name added]` |
- 调用链：`ShellEventUtil.keyAdded → ShellKeyAddedEvent`

## ShellKeyUpdatedEvent
- 职责：密钥已更新事件。
- 字段：无（`data()` 为 `ShellKey`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[密钥:name updated]` |
- 调用链：`ShellEventUtil.keyUpdated → ShellKeyUpdatedEvent`

## ShellRunSnippetEvent
- 职责：执行片段事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | runAll | boolean | 是否在所有 tab 执行 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean isRunAll()/void setRunAll(boolean)` | 是否全执行 | 简单存取 |
- 调用链：`ShellEventUtil.runSnippet → ShellRunSnippetEvent`

## ShellTreeItemChangedEvent
- 职责：树节点选中变更事件。
- 字段：无（`data()` 为 `TreeItem<?>`）。
- 方法：无。
- 调用链：`ShellEventUtil.treeItemChanged → ShellTreeItemChangedEvent`

## ShellShowKeyEvent
- 职责：显示密钥管理页面事件。
- 字段：无（`data()` 为 `Object`）。
- 方法：无。
- 调用链：`ShellEventUtil.showKey → ShellShowKeyEvent`

## ShellShowMessageEvent
- 职责：显示消息页面事件。
- 字段：无（`data()` 为 `Object`）。
- 方法：无。
- 调用链：`ShellEventUtil.showMessage → ShellShowMessageEvent`

## ShellShowSplitEvent
- 职责：显示分屏页面事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | connects | List<ShellConnect> | 待分屏的连接列表 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `List<ShellConnect> getConnects()/void setConnects(List<ShellConnect>)` | 连接列表存取 | 简单存取（`data()` 为分屏类型 `String`） |
- 调用链：`ShellEventUtil.showSplit → ShellShowSplitEvent`

## ShellShowTerminalEvent
- 职责：显示终端页面事件。
- 字段：无（`data()` 为 `Object`）。
- 方法：无。
- 调用链：`ShellEventUtil.showTerminal → ShellShowTerminalEvent`

---

# 三、达梦事件（cn.oyzh.easyshell.event.dameng）

## ShellDamengEventUtil
- 职责：发布达梦数据库（表/视图/函数/存储过程/模式/查询/终端）相关事件。
- 字段：无（纯静态方法工具类）。
- 方法（均为 `new 事件 → data()/setXxx() → EventUtil.post`，`dropXxx` 用 `postSync`）：
  | 方法 | 说明 | 关键字段 |
  |---|---|---|
  | `tableOpen(ShellDamengTableTreeItem item, ShellDamengSchemaTreeItem dbItem)` | 表打开 | data=item，setDbItem |
  | `tableAlerted(String tableName, ...dbItem)` | 表结构变更 | data=tableName |
  | `tableRenamed(String tableName, String newTableName, ...dbItem)` | 表重命名 | data，setNewTableName |
  | `viewRenamed(String viewName, String newViewName, ...dbItem)` | 视图重命名 | data，setNewViewName |
  | `functionRenamed(String, String, ...dbItem)` | 函数重命名 | data，setNewFunctionName |
  | `procedureRenamed(String, String, ...dbItem)` | 存储过程重命名 | data，setNewProcedureName |
  | `tableCleared(ShellDamengTableTreeItem, ...dbItem)` | 表清空 | data=item |
  | `tableTruncated(...)` | 表截断 | data=item |
  | `tableDropped(...)` | 表删除 | data=item |
  | `schemaClosed(ShellDamengSchemaTreeItem dbItem)` | 模式关闭 | data=dbItem |
  | `schemaAdded(ShellDamengRootTreeItem, DamengSchema)` | 模式新增 | data=schema，setConnectItem |
  | `schemaUpdated(...)` | 模式更新 | data=schema，setConnectItem |
  | `schemaDropped(ShellDamengSchemaTreeItem)` | 模式删除 | data=dbItem |
  | `queryAdd(ShellDamengSchemaTreeItem)` | 查询新增 | data=item |
  | `queryDeleted(ShellDamengQueryTreeItem)` | 查询删除 | data=item |
  | `queryOpen(ShellQuery, ...dbItem)` | 查询打开 | data=query，setDbItem |
  | `queryRenamed(id, name, newName, ...dbItem)` | 查询重命名 | data 三次（id/name/newName） |
  | `viewOpen(ShellDamengViewTreeItem, ...dbItem)` | 视图打开 | data=item |
  | `designFunction(DamengFunction, ...dbItem)` | 函数设计 | data=function |
  | `designProcedure(DamengProcedure, ...dbItem)` | 存储过程设计 | data=procedure |
  | `viewAlerted(String viewName, ...dbItem)` | 视图变更 | data=viewName |
  | `designView(DamengView, ...dbItem)` | 视图设计 | data=dbView |
  | `dropView(ShellDamengViewTreeItem)` | 视图删除 | data=treeItem，**postSync** |
  | `dropFunction(ShellDamengFunctionTreeItem)` | 函数删除 | data=treeItem，**postSync** |
  | `dropProcedure(ShellDamengProcedureTreeItem)` | 存储过程删除 | data=treeItem，**postSync** |
  | `designTable(DamengTable, ...dbItem)` | 表设计 | data=table |
  | `terminalOpen(ShellDamengSchemaTreeItem)` | 终端打开 | data=dbItem |

- 调用链：`业务调用 → ShellDamengEventUtil.xxx → new ShellDamengXxxEvent → data()/setXxx() → EventUtil.post/postSync`

## ShellDamengFunctionDesignEvent
- 职责：达梦函数设计事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式（数据库）节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String functionName()` | 函数名称 | `data().getName()` |
  | `ShellDamengSchemaTreeItem getDbItem()/void setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.designFunction → ShellDamengFunctionDesignEvent`

## ShellDamengFunctionDroppedEvent
- 职责：达梦函数已删除事件。
- 字段：无（`data()` 为 `ShellDamengFunctionTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String functionName()` | 函数名称 | `data().functionName()` |
  | `ShellDamengSchemaTreeItem getDbItem()` | 模式节点 | `data().dbItem()` |
- 调用链：`ShellDamengEventUtil.dropFunction → ShellDamengFunctionDroppedEvent`

## ShellDamengFunctionRenamedEvent
- 职责：达梦函数已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |
  | newFunctionName | String | 新函数名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getNewFunctionName()/void setNewFunctionName(String)` | 新名称存取 | 简单存取 |
  | `String functionName()` | 函数名称 | `data()` |
  | `String schema()` | 模式名称 | `dbItem.schema()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.functionRenamed → ShellDamengFunctionRenamedEvent`

## ShellDamengProcedureDesignEvent
- 职责：达梦存储过程设计事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String procedureName()` | 存储过程名称 | `data().getName()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.designProcedure → ShellDamengProcedureDesignEvent`

## ShellDamengProcedureDroppedEvent
- 职责：达梦存储过程已删除事件。
- 字段：无（`data()` 为 `ShellDamengProcedureTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String procedureName()` | 名称 | `data().procedureName()` |
  | `ShellDamengSchemaTreeItem getDbItem()` | 模式节点 | `data().dbItem()` |
- 调用链：`ShellDamengEventUtil.dropProcedure → ShellDamengProcedureDroppedEvent`

## ShellDamengProcedureRenamedEvent
- 职责：达梦存储过程已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |
  | newProcedureName | String | 新存储过程名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNewProcedureName()/setNewProcedureName(String)` | 新名称存取 | 简单存取 |
  | `String procedureName()` | 名称 | `data()` |
  | `String schema()` | 模式名称 | `dbItem.schema()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.procedureRenamed → ShellDamengProcedureRenamedEvent`

## ShellDamengQueryAddEvent
- 职责：达梦查询新增事件。
- 字段：无（`data()` 为 `ShellDamengSchemaTreeItem`）。
- 方法：无。
- 调用链：`ShellDamengEventUtil.queryAdd → ShellDamengQueryAddEvent`

## ShellDamengQueryDeletedEvent
- 职责：达梦查询已删除事件。
- 字段：无（`data()` 为 `ShellDamengQueryTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String queryId()` | 查询 id | `data().value().getUid()` |
  | `String eventFormat()` | 事件描述 | `[查询:queryName] deleted` |
- 调用链：`ShellDamengEventUtil.queryDeleted → ShellDamengQueryDeletedEvent`

## ShellDamengQueryOpenEvent
- 职责：达梦查询打开事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String queryId()` | 查询 id | `data().getUid()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.queryOpen → ShellDamengQueryOpenEvent`

## ShellDamengQueryRenamedEvent
- 职责：达梦查询已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |
  | queryName | String | 查询名称 |
  | newQueryName | String | 新查询名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getQueryName()/setQueryName(String)`、`getNewQueryName()/setNewQueryName(String)` | 名称存取 | 简单存取 |
  | `String schema()` | 模式名称 | `dbItem.schema()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `[查询:queryName] renamed, new name:newQueryName` |
- 调用链：`ShellDamengEventUtil.queryRenamed → ShellDamengQueryRenamedEvent`

## ShellDamengSchemaAddedEvent
- 职责：达梦模式已新增事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | connectItem | ShellDamengRootTreeItem | 连接根节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getConnectItem()/setConnectItem(...)` | 连接节点存取 | 简单存取（`data()` 为 `DamengSchema`） |
- 调用链：`ShellDamengEventUtil.schemaAdded → ShellDamengSchemaAddedEvent`

## ShellDamengSchemaClosedEvent
- 职责：达梦模式已关闭事件。
- 字段：无（`data()` 为 `ShellDamengSchemaTreeItem`）。
- 方法：无。
- 调用链：`ShellDamengEventUtil.schemaClosed → ShellDamengSchemaClosedEvent`

## ShellDamengSchemaDroppedEvent
- 职责：达梦模式已删除事件。
- 字段：无（`data()` 为 `ShellDamengSchemaTreeItem`）。
- 方法：无。
- 调用链：`ShellDamengEventUtil.schemaDropped → ShellDamengSchemaDroppedEvent`

## ShellDamengSchemaUpdatedEvent
- 职责：达梦模式已更新事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | connectItem | ShellDamengRootTreeItem | 连接根节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getConnectItem()/setConnectItem(...)` | 连接节点存取 | 简单存取（`data()` 为 `DamengSchema`） |
- 调用链：`ShellDamengEventUtil.schemaUpdated → ShellDamengSchemaUpdatedEvent`

## ShellDamengTableAlertedEvent
- 职责：达梦表已变更事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取（`data()` 为表名 `String`） |
- 调用链：`ShellDamengEventUtil.tableAlerted → ShellDamengTableAlertedEvent`

## ShellDamengTableClearedEvent
- 职责：达梦表已清空事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String tableName()` | 表名称 | `data().tableName()` |
  | `String schema()` | 模式名称 | `dbItem.schema()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.tableCleared → ShellDamengTableClearedEvent`

## ShellDamengTableDesignEvent
- 职责：达梦表设计事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String tableName()` | 表名称 | `data().getName()` |
  | `String schema()` | 模式名称 | `dbItem.schema()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取（`data()` 为 `DamengTable`） |
- 调用链：`ShellDamengEventUtil.designTable → ShellDamengTableDesignEvent`

## ShellDamengTableDroppedEvent
- 职责：达梦表已删除事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String tableName()` | 表名称 | `data().tableName()` |
  | `String schema()` | 模式名称 | `dbItem.schema()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.tableDropped → ShellDamengTableDroppedEvent`

## ShellDamengTableOpenEvent
- 职责：达梦表打开事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String tableName()` | 表名称 | `data().tableName()` |
  | `String schema()` | 模式名称 | `dbItem.schema()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.tableOpen → ShellDamengTableOpenEvent`

## ShellDamengTableRenamedEvent
- 职责：达梦表已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |
  | newTableName | String | 新表名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNewTableName()/setNewTableName(String)` | 新名称存取 | 简单存取 |
  | `String tableName()` | 表名称 | `data()` |
  | `String schema()` | 模式名称 | `dbItem.schema()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.tableRenamed → ShellDamengTableRenamedEvent`

## ShellDamengTableTruncatedEvent
- 职责：达梦表已截断事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String tableName()` | 表名称 | `data().tableName()` |
  | `String schema()` | 模式名称 | `dbItem.schema()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.tableTruncated → ShellDamengTableTruncatedEvent`

## ShellDamengTerminalOpenEvent
- 职责：达梦终端打开事件。
- 字段：无（`data()` 为 `ShellDamengSchemaTreeItem`）。
- 方法：无。
- 调用链：`ShellDamengEventUtil.terminalOpen → ShellDamengTerminalOpenEvent`

## ShellDamengViewAlertedEvent
- 职责：达梦视图已变更事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取（`data()` 为视图名 `String`） |
- 调用链：`ShellDamengEventUtil.viewAlerted → ShellDamengViewAlertedEvent`

## ShellDamengViewDesignEvent
- 职责：达梦视图设计事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String viewName()` | 视图名称 | `data().getName()` |
  | `String schema()` | 模式名称 | `dbItem.schema()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取（`data()` 为 `DamengView`） |
- 调用链：`ShellDamengEventUtil.designView → ShellDamengViewDesignEvent`

## ShellDamengViewDroppedEvent
- 职责：达梦视图已删除事件。
- 字段：无（`data()` 为 `ShellDamengViewTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String viewName()` | 视图名称 | `data().viewName()` |
  | `ShellDamengSchemaTreeItem getDbItem()` | 模式节点 | `data().dbItem()` |
- 调用链：`ShellDamengEventUtil.dropView → ShellDamengViewDroppedEvent`

## ShellDamengViewOpenEvent
- 职责：达梦视图打开事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String viewName()` | 视图名称 | `data().viewName()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.viewOpen → ShellDamengViewOpenEvent`

## ShellDamengViewRenamedEvent
- 职责：达梦视图已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellDamengSchemaTreeItem | 模式节点 |
  | newViewName | String | 新视图名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNewViewName()/setNewViewName(String)` | 新名称存取 | 简单存取 |
  | `String viewName()` | 视图名称 | `data()` |
  | `String schema()` | 模式名称 | `dbItem.schema()` |
  | `getDbItem()/setDbItem(...)` | 模式节点存取 | 简单存取 |
- 调用链：`ShellDamengEventUtil.viewRenamed → ShellDamengViewRenamedEvent`

---

# 四、MySQL 事件（cn.oyzh.easyshell.event.mysql）

## ShellMysqlEventUtil
- 职责：发布 MySQL（表/视图/函数/存储过程/事件/数据库/查询/终端）相关事件。
- 字段：无（纯静态方法工具类）。
- 方法（均为 `new 事件 → data()/setXxx() → EventUtil.post`，`dropXxx` 用 `postSync`）：
  | 方法 | 说明 | 关键字段 |
  |---|---|---|
  | `tableOpen(ShellMysqlTableTreeItem, ShellMysqlDatabaseTreeItem)` | 表打开 | data=item，setDbItem |
  | `tableAlerted(String tableName, ...dbItem)` | 表结构变更 | data=tableName |
  | `tableRenamed(String, String, ...dbItem)` | 表重命名 | data，setNewTableName |
  | `viewRenamed(String, String, ...dbItem)` | 视图重命名 | data，setNewViewName |
  | `eventRenamed(String, String, ...dbItem)` | 事件重命名 | data，setNewEventName |
  | `functionRenamed(String, String, ...dbItem)` | 函数重命名 | data，setNewFunctionName |
  | `procedureRenamed(String, String, ...dbItem)` | 存储过程重命名 | data，setNewProcedureName |
  | `tableCleared(...)` / `tableTruncated(...)` / `tableDropped(...)` | 表清空/截断/删除 | data=item |
  | `databaseClosed(ShellMysqlDatabaseTreeItem)` | 数据库关闭 | data=dbItem |
  | `databaseAdded(ShellMysqlRootTreeItem, MysqlDatabase)` | 数据库新增 | data=database，setConnectItem |
  | `databaseUpdated(...)` | 数据库更新 | data=database，setConnectItem |
  | `databaseDropped(...)` | 数据库删除 | data=dbItem |
  | `queryAdd(ShellMysqlDatabaseTreeItem)` | 查询新增 | data=item |
  | `queryDeleted(ShellMysqlQueryTreeItem)` | 查询删除 | data=item |
  | `queryOpen(ShellQuery, ...dbItem)` | 查询打开 | data=query |
  | `queryRenamed(id, name, newName, ...dbItem)` | 查询重命名 | data 三次 |
  | `viewOpen(ShellMysqlViewTreeItem, ...dbItem)` | 视图打开 | data=item |
  | `designFunction(MysqlFunction, ...dbItem)` | 函数设计 | data=function |
  | `designProcedure(MysqlProcedure, ...dbItem)` | 存储过程设计 | data=procedure |
  | `designEvent(MysqlEvent, ...dbItem)` | 事件设计 | data=event |
  | `viewAlerted(String, ...dbItem)` | 视图变更 | data=viewName |
  | `designView(MysqlView, ...dbItem)` | 视图设计 | data=dbView |
  | `dropView(ShellMysqlViewTreeItem)` | 视图删除 | data=treeItem，**postSync** |
  | `dropFunction(ShellMysqlFunctionTreeItem)` | 函数删除 | data=treeItem，**postSync** |
  | `dropProcedure(ShellMysqlProcedureTreeItem)` | 存储过程删除 | data=treeItem，**postSync** |
  | `dropEvent(ShellMysqlEventTreeItem)` | 事件删除 | data=treeItem，**postSync** |
  | `designTable(MysqlTable, ...dbItem)` | 表设计 | data=table |
  | `terminalOpen(ShellMysqlDatabaseTreeItem)` | 终端打开 | data=dbItem |

- 说明：`printSql(...)` 在本工具类中已注释，改由 `cn.oyzh.easyshell.event.db.ShellPrintSqlEvent` 提供。
- 调用链：`业务调用 → ShellMysqlEventUtil.xxx → new ShellMysqlXxxEvent → data()/setXxx() → EventUtil.post/postSync`

## ShellMysqlDatabaseAddedEvent
- 职责：MySQL 数据库已新增事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | connectItem | ShellMysqlRootTreeItem | 连接根节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getConnectItem()/setConnectItem(...)` | 连接节点存取 | 简单存取（`data()` 为 `MysqlDatabase`） |
- 调用链：`ShellMysqlEventUtil.databaseAdded → ShellMysqlDatabaseAddedEvent`

## ShellMysqlDatabaseClosedEvent
- 职责：MySQL 数据库已关闭事件。
- 字段：无（`data()` 为 `ShellMysqlDatabaseTreeItem`）。
- 方法：无。
- 调用链：`ShellMysqlEventUtil.databaseClosed → ShellMysqlDatabaseClosedEvent`

## ShellMysqlDatabaseDroppedEvent
- 职责：MySQL 数据库已删除事件。
- 字段：无（`data()` 为 `ShellMysqlDatabaseTreeItem`）。
- 方法：无。
- 调用链：`ShellMysqlEventUtil.databaseDropped → ShellMysqlDatabaseDroppedEvent`

## ShellMysqlDatabaseUpdatedEvent
- 职责：MySQL 数据库已更新事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | connectItem | ShellMysqlRootTreeItem | 连接根节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getConnectItem()/setConnectItem(...)` | 连接节点存取 | 简单存取（`data()` 为 `MysqlDatabase`） |
- 调用链：`ShellMysqlEventUtil.databaseUpdated → ShellMysqlDatabaseUpdatedEvent`

## ShellMysqlEventDesignEvent
- 职责：MySQL 事件（调度事件）设计事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventName()` | 事件名称 | `data().getName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为 `MysqlEvent`） |
- 调用链：`ShellMysqlEventUtil.designEvent → ShellMysqlEventDesignEvent`

## ShellMysqlEventDroppedEvent
- 职责：MySQL 事件已删除事件。
- 字段：无（`data()` 为 `ShellMysqlEventTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventName()` | 事件名称 | `data().eventName()` |
  | `ShellMysqlDatabaseTreeItem getDbItem()` | 数据库节点 | `data().dbItem()` |
- 调用链：`ShellMysqlEventUtil.dropEvent → ShellMysqlEventDroppedEvent`

## ShellMysqlEventRenamedEvent
- 职责：MySQL 事件已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |
  | newEventName | String | 新事件名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNewEventName()/setNewEventName(String)` | 新名称存取 | 简单存取 |
  | `String eventName()` | 事件名称 | `data()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMysqlEventUtil.eventRenamed → ShellMysqlEventRenamedEvent`

## ShellMysqlFunctionDesignEvent
- 职责：MySQL 函数设计事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String functionName()` | 函数名称 | `data().getName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为 `MysqlFunction`） |
- 调用链：`ShellMysqlEventUtil.designFunction → ShellMysqlFunctionDesignEvent`

## ShellMysqlFunctionDroppedEvent
- 职责：MySQL 函数已删除事件。
- 字段：无（`data()` 为 `ShellMysqlFunctionTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String functionName()` | 函数名称 | `data().functionName()` |
  | `ShellMysqlDatabaseTreeItem getDbItem()` | 数据库节点 | `data().dbItem()` |
- 调用链：`ShellMysqlEventUtil.dropFunction → ShellMysqlFunctionDroppedEvent`

## ShellMysqlFunctionRenamedEvent
- 职责：MySQL 函数已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |
  | newFunctionName | String | 新函数名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNewFunctionName()/setNewFunctionName(String)` | 新名称存取 | 简单存取 |
  | `String functionName()` | 函数名称 | `data()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMysqlEventUtil.functionRenamed → ShellMysqlFunctionRenamedEvent`

## ShellMysqlProcedureDesignEvent
- 职责：MySQL 存储过程设计事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String procedureName()` | 存储过程名称 | `data().getName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为 `MysqlProcedure`） |
- 调用链：`ShellMysqlEventUtil.designProcedure → ShellMysqlProcedureDesignEvent`

## ShellMysqlProcedureDroppedEvent
- 职责：MySQL 存储过程已删除事件。
- 字段：无（`data()` 为 `ShellMysqlProcedureTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String procedureName()` | 名称 | `data().procedureName()` |
  | `ShellMysqlDatabaseTreeItem getDbItem()` | 数据库节点 | `data().dbItem()` |
- 调用链：`ShellMysqlEventUtil.dropProcedure → ShellMysqlProcedureDroppedEvent`

## ShellMysqlProcedureRenamedEvent
- 职责：MySQL 存储过程已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |
  | newProcedureName | String | 新存储过程名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNewProcedureName()/setNewProcedureName(String)` | 新名称存取 | 简单存取 |
  | `String procedureName()` | 名称 | `data()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMysqlEventUtil.procedureRenamed → ShellMysqlProcedureRenamedEvent`

## ShellMysqlQueryAddEvent
- 职责：MySQL 查询新增事件。
- 字段：无（`data()` 为 `ShellMysqlDatabaseTreeItem`）。
- 方法：无。
- 调用链：`ShellMysqlEventUtil.queryAdd → ShellMysqlQueryAddEvent`

## ShellMysqlQueryDeletedEvent
- 职责：MySQL 查询已删除事件。
- 字段：无（`data()` 为 `ShellMysqlQueryTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String queryId()` | 查询 id | `data().value().getUid()` |
  | `String eventFormat()` | 事件描述 | `[查询:queryName] deleted` |
- 调用链：`ShellMysqlEventUtil.queryDeleted → ShellMysqlQueryDeletedEvent`

## ShellMysqlQueryOpenEvent
- 职责：MySQL 查询打开事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String queryId()` | 查询 id | `data().getUid()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为 `ShellQuery`） |
- 调用链：`ShellMysqlEventUtil.queryOpen → ShellMysqlQueryOpenEvent`

## ShellMysqlQueryRenamedEvent
- 职责：MySQL 查询已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |
  | queryName | String | 查询名称 |
  | newQueryName | String | 新查询名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getQueryName()/setQueryName(String)`、`getNewQueryName()/setNewQueryName(String)` | 名称存取 | 简单存取 |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `[查询:queryName] renamed, new name:newQueryName` |
- 调用链：`ShellMysqlEventUtil.queryRenamed → ShellMysqlQueryRenamedEvent`

## ShellMysqlTableAlertedEvent
- 职责：MySQL 表已变更事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为表名 `String`） |
- 调用链：`ShellMysqlEventUtil.tableAlerted → ShellMysqlTableAlertedEvent`

## ShellMysqlTableClearedEvent
- 职责：MySQL 表已清空事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String tableName()` | 表名称 | `data().tableName()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMysqlEventUtil.tableCleared → ShellMysqlTableClearedEvent`

## ShellMysqlTableDesignEvent
- 职责：MySQL 表设计事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String tableName()` | 表名称 | `data().getName()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为 `MysqlTable`） |
- 调用链：`ShellMysqlEventUtil.designTable → ShellMysqlTableDesignEvent`

## ShellMysqlTableDroppedEvent
- 职责：MySQL 表已删除事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String tableName()` | 表名称 | `data().tableName()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMysqlEventUtil.tableDropped → ShellMysqlTableDroppedEvent`

## ShellMysqlTableOpenEvent
- 职责：MySQL 表打开事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String tableName()` | 表名称 | `data().tableName()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMysqlEventUtil.tableOpen → ShellMysqlTableOpenEvent`

## ShellMysqlTableRenamedEvent
- 职责：MySQL 表已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |
  | newTableName | String | 新表名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNewTableName()/setNewTableName(String)` | 新名称存取 | 简单存取 |
  | `String tableName()` | 表名称 | `data()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMysqlEventUtil.tableRenamed → ShellMysqlTableRenamedEvent`

## ShellMysqlTableTruncatedEvent
- 职责：MySQL 表已截断事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String tableName()` | 表名称 | `data().tableName()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMysqlEventUtil.tableTruncated → ShellMysqlTableTruncatedEvent`

## ShellMysqlTerminalOpenEvent
- 职责：MySQL 终端打开事件。
- 字段：无（`data()` 为 `ShellMysqlDatabaseTreeItem`）。
- 方法：无。
- 调用链：`ShellMysqlEventUtil.terminalOpen → ShellMysqlTerminalOpenEvent`

## ShellMysqlViewAlertedEvent
- 职责：MySQL 视图已变更事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为视图名 `String`） |
- 调用链：`ShellMysqlEventUtil.viewAlerted → ShellMysqlViewAlertedEvent`

## ShellMysqlViewDesignEvent
- 职责：MySQL 视图设计事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String viewName()` | 视图名称 | `data().getName()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为 `MysqlView`） |
- 调用链：`ShellMysqlEventUtil.designView → ShellMysqlViewDesignEvent`

## ShellMysqlViewDroppedEvent
- 职责：MySQL 视图已删除事件。
- 字段：无（`data()` 为 `ShellMysqlViewTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String viewName()` | 视图名称 | `data().viewName()` |
  | `ShellMysqlDatabaseTreeItem getDbItem()` | 数据库节点 | `data().dbItem()` |
- 调用链：`ShellMysqlEventUtil.dropView → ShellMysqlViewDroppedEvent`

## ShellMysqlViewOpenEvent
- 职责：MySQL 视图打开事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String viewName()` | 视图名称 | `data().viewName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMysqlEventUtil.viewOpen → ShellMysqlViewOpenEvent`

## ShellMysqlViewRenamedEvent
- 职责：MySQL 视图已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMysqlDatabaseTreeItem | 数据库节点 |
  | newViewName | String | 新视图名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNewViewName()/setNewViewName(String)` | 新名称存取 | 简单存取 |
  | `String viewName()` | 视图名称 | `data()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMysqlEventUtil.viewRenamed → ShellMysqlViewRenamedEvent`

---

# 五、MongoDB 事件（cn.oyzh.easyshell.event.mongo）

## ShellMongoEventUtil
- 职责：发布 MongoDB（数据库/集合/桶/查询/函数/用户/终端）相关事件。
- 字段：无（纯静态方法工具类）。
- 方法（除 `dropXxx` 用 `postSync` 外均为 `post`）：
  | 方法 | 说明 | 关键字段 |
  |---|---|---|
  | `databaseClosed(ShellMongoDatabaseTreeItem)` | 数据库关闭 | data=dbItem |
  | `databaseAdded(ShellMongoRootTreeItem, MongoDatabase)` | 数据库新增 | data=database，setConnectItem |
  | `databaseUpdated(...)` | 数据库更新 | data=database，setConnectItem |
  | `databaseDropped(...)` | 数据库删除 | data=dbItem |
  | `queryAdd(ShellMongoDatabaseTreeItem)` | 查询新增 | data=item |
  | `queryAdded(ShellQuery, ...item)` | 查询已新增 | data=query，setDbItem |
  | `queryDeleted(ShellMongoQueryTreeItem)` | 查询删除 | data=item |
  | `queryOpen(ShellQuery, ...item)` | 查询打开 | data=query，setDbItem |
  | `queryRenamed(id, name, newName, ...item)` | 查询重命名 | data=id，setQueryName/setNewQueryName |
  | `collectionDropped(...)` / `collectionOpen(...)` | 集合删除/打开 | data=collectionItem，setDbItem |
  | `collectionRenamed(String, String, ...dbItem)` | 集合重命名 | data，setNewCollectionName |
  | `bucketDropped(...)` / `bucketOpen(...)` | 桶删除/打开 | data=collectionItem，setDbItem |
  | `terminalOpen(ShellMongoClient, String dbName)` | 终端打开 | data=client，setDbName |
  | `dropFunction(ShellMongoFunctionTreeItem)` | 函数删除 | data=treeItem，**postSync** |
  | `designFunction(MongoFunction, ...dbItem)` | 函数设计 | data=function |
  | `functionRenamed(String, String, ...dbItem)` | 函数重命名 | data，setNewFunctionName |
  | `userView(MongoUser, ...dbItem)` | 用户查看 | data=mongoUser，setDbItem |
  | `userDeleted(ShellMongoUserTreeItem)` | 用户删除 | data=userTreeItem |

- 调用链：`业务调用 → ShellMongoEventUtil.xxx → new ShellMongoXxxEvent → data()/setXxx() → EventUtil.post/postSync`

## ShellMongoBucketDroppedEvent
- 职责：MongoDB 桶已删除事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMongoDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String bucketName()` | 桶名称 | `data().bucketName()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `[桶:bucketName] dropped` |
- 调用链：`ShellMongoEventUtil.bucketDropped → ShellMongoBucketDroppedEvent`

## ShellMongoBucketOpenEvent
- 职责：MongoDB 桶打开事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMongoDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String bucketName()` | 桶名称 | `data().bucketName()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMongoEventUtil.bucketOpen → ShellMongoBucketOpenEvent`

## ShellMongoCollectionDroppedEvent
- 职责：MongoDB 集合已删除事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMongoDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String collectionName()` | 集合名称 | `data().collectionName()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `[集合:collectionName] dropped` |
- 调用链：`ShellMongoEventUtil.collectionDropped → ShellMongoCollectionDroppedEvent`

## ShellMongoCollectionOpenEvent
- 职责：MongoDB 集合打开事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMongoDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String collectionName()` | 集合名称 | `data().collectionName()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
- 调用链：`ShellMongoEventUtil.collectionOpen → ShellMongoCollectionOpenEvent`

## ShellMongoCollectionRenamedEvent
- 职责：MongoDB 集合已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMongoDatabaseTreeItem | 数据库节点 |
  | newCollectionName | String | 新集合名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNewCollectionName()/setNewCollectionName(String)` | 新名称存取 | 简单存取 |
  | `String tableName()` | 集合名称 | `data()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `[集合:name] renamed, new name:newCollectionName` |
- 调用链：`ShellMongoEventUtil.collectionRenamed → ShellMongoCollectionRenamedEvent`

## ShellMongoDatabaseAddedEvent
- 职责：MongoDB 数据库已新增事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | connectItem | ShellMongoRootTreeItem | 连接根节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[数据库:name] added` |
  | `getConnectItem()/setConnectItem(...)` | 连接节点存取 | 简单存取（`data()` 为 `MongoDatabase`） |
- 调用链：`ShellMongoEventUtil.databaseAdded → ShellMongoDatabaseAddedEvent`

## ShellMongoDatabaseClosedEvent
- 职责：MongoDB 数据库已关闭事件。
- 字段：无（`data()` 为 `ShellMongoDatabaseTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[数据库:value] closed` |
- 调用链：`ShellMongoEventUtil.databaseClosed → ShellMongoDatabaseClosedEvent`

## ShellMongoDatabaseDroppedEvent
- 职责：MongoDB 数据库已删除事件。
- 字段：无（`data()` 为 `ShellMongoDatabaseTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[数据库:dbName] deleted`（注意：未实现 `EventFormatter` 接口，方法为普通方法） |
- 调用链：`ShellMongoEventUtil.databaseDropped → ShellMongoDatabaseDroppedEvent`

## ShellMongoDatabaseUpdatedEvent
- 职责：MongoDB 数据库已更新事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | connectItem | ShellMongoRootTreeItem | 连接根节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String eventFormat()` | 事件描述 | `[数据库:name] updated` |
  | `getConnectItem()/setConnectItem(...)` | 连接节点存取 | 简单存取（`data()` 为 `MongoDatabase`） |
- 调用链：`ShellMongoEventUtil.databaseUpdated → ShellMongoDatabaseUpdatedEvent`

## ShellMongoFunctionDesignEvent
- 职责：MongoDB 函数设计事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMongoDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String functionName()` | 函数名称 | `data().getName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为 `MongoFunction`） |
- 调用链：`ShellMongoEventUtil.designFunction → ShellMongoFunctionDesignEvent`

## ShellMongoFunctionDroppedEvent
- 职责：MongoDB 函数已删除事件。
- 字段：无（`data()` 为 `ShellMongoFunctionTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String functionName()` | 函数名称 | `data().functionName()` |
  | `ShellMongoDatabaseTreeItem getDbItem()` | 数据库节点 | `data().dbItem()` |
  | `String eventFormat()` | 事件描述 | `[函数:functionName] dropped` |
- 调用链：`ShellMongoEventUtil.dropFunction → ShellMongoFunctionDroppedEvent`

## ShellMongoFunctionRenamedEvent
- 职责：MongoDB 函数已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMongoDatabaseTreeItem | 数据库节点 |
  | newFunctionName | String | 新函数名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNewFunctionName()/setNewFunctionName(String)` | 新名称存取 | 简单存取 |
  | `String functionName()` | 函数名称 | `data()` |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `[函数:name] renamed, new name:newFunctionName` |
- 调用链：`ShellMongoEventUtil.functionRenamed → ShellMongoFunctionRenamedEvent`

## ShellMongoQueryAddEvent
- 职责：MongoDB 查询新增事件。
- 字段：无（`data()` 为 `ShellMongoDatabaseTreeItem`）。
- 方法：无。
- 调用链：`ShellMongoEventUtil.queryAdd → ShellMongoQueryAddEvent`

## ShellMongoQueryAddedEvent
- 职责：MongoDB 查询已新增事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMongoDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为 `ShellQuery`） |
  | `String eventFormat()` | 事件描述 | `[查询:name] added` |
- 调用链：`ShellMongoEventUtil.queryAdded → ShellMongoQueryAddedEvent`

## ShellMongoQueryDeletedEvent
- 职责：MongoDB 查询已删除事件。
- 字段：无（`data()` 为 `ShellMongoQueryTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String queryId()` | 查询 id | `data().value().getUid()` |
  | `String eventFormat()` | 事件描述 | `[查询:queryName] deleted` |
- 调用链：`ShellMongoEventUtil.queryDeleted → ShellMongoQueryDeletedEvent`

## ShellMongoQueryOpenEvent
- 职责：MongoDB 查询打开事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMongoDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String queryId()` | 查询 id | `data().getUid()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为 `ShellQuery`） |
- 调用链：`ShellMongoEventUtil.queryOpen → ShellMongoQueryOpenEvent`

## ShellMongoQueryRenamedEvent
- 职责：MongoDB 查询已重命名事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMongoDatabaseTreeItem | 数据库节点 |
  | queryName | String | 查询名称 |
  | newQueryName | String | 新查询名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getQueryName()/setQueryName(String)`、`getNewQueryName()/setNewQueryName(String)` | 名称存取 | 简单存取 |
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `[查询:name] renamed, new name:newQueryName` |
- 调用链：`ShellMongoEventUtil.queryRenamed → ShellMongoQueryRenamedEvent`

## ShellMongoTerminalCloseEvent
- 职责：MongoDB 终端关闭事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbName | String | 数据库名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getDbName()/setDbName(String)` | 数据库名称存取 | 简单存取（`data()` 为 `ShellMongoClient`） |
- 调用链：`（无对应工具方法，由使用方直接发布）→ ShellMongoTerminalCloseEvent`

## ShellMongoTerminalOpenEvent
- 职责：MongoDB 终端打开事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbName | String | 数据库名称 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getDbName()/setDbName(String)` | 数据库名称存取 | 简单存取（`data()` 为 `ShellMongoClient`） |
- 调用链：`ShellMongoEventUtil.terminalOpen → ShellMongoTerminalOpenEvent`

## ShellMongoUserDeletedEvent
- 职责：MongoDB 用户已删除事件。
- 字段：无（`data()` 为 `ShellMongoUserTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MongoUser user()` | 用户对象 | `data().value()` |
  | `String userName()` | 用户名 | `data().userName()` |
  | `String eventFormat()` | 事件描述 | `[用户:userName] deleted` |
- 调用链：`ShellMongoEventUtil.userDeleted → ShellMongoUserDeletedEvent`

## ShellMongoUserViewEvent
- 职责：MongoDB 用户查看事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dbItem | ShellMongoDatabaseTreeItem | 数据库节点 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String dbName()` | 数据库名称 | `dbItem.dbName()` |
  | `getDbItem()/setDbItem(...)` | 数据库节点存取 | 简单存取（`data()` 为 `MongoUser`） |
- 调用链：`ShellMongoEventUtil.userView → ShellMongoUserViewEvent`

---

# 六、Redis 事件（cn.oyzh.easyshell.event.redis）

## ShellRedisEventUtil
- 职责：发布 Redis 相关事件（键刷新、TTL 更新、键复制/移动、客户端操作、ZSet 反转视图）。
- 字段：无（纯静态方法工具类）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `redisKeyFlushed(ShellConnect, Integer dbIndex)` | 键刷新 | `ShellRedisKeyFlushedEvent`，data=dbIndex，setConnect + `post` |
  | `redisKeyTTLUpdated(ShellConnect, Long ttl, String key, int dbIndex)` | 键 TTL 更新 | `ShellRedisKeyTTLUpdatedEvent`，data=connect，setTtl/setKey/setDbIndex + `post` |
  | `redisKeysCopied(ShellConnect, List<String> keys, int dbIndex, int targetDB)` | 多键复制 | `ShellRedisKeysCopiedEvent`，data=keys，setSourceDB/setConnect/setTargetDB + `post` |
  | `redisKeysMoved(ShellConnect, Integer dbIndex, int targetDB)` | 多键移动 | `ShellRedisKeysMovedEvent`，data=dbIndex，setConnect/setTargetDB + `post` |
  | `redisClientAction(String connectName, CommandArguments arguments)` | 客户端操作 | `ShellRedisClientActionEvent`，data=connectName，setArguments + `postAsync` |
  | `redisZSetReverseView(ShellRedisZSetKeyTreeItem item)` | ZSet 反转视图 | `ShellRedisZSetReverseViewEvent`，data=item + `post` |

- 说明：源码含大量注释掉的旧事件方法（键增删改、成员添加等）。
- 调用链：`业务调用 → ShellRedisEventUtil.xxx → new ShellRedisXxxEvent → data()/setXxx() → EventUtil.post/postAsync`

## ShellRedisClientActionEvent
- 职责：Redis 客户端操作事件（记录“连接名 > 命令参数”）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | arguments | CommandArguments | Jedis 命令参数 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `CommandArguments getArguments()/void setArguments(...)` | 命令参数存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | data 为空返回 null；否则拼接 `data() + " >"` 并遍历 `arguments` 逐个追加 `new String(rawable.getRaw())` |
- 调用链：`ShellRedisEventUtil.redisClientAction → ShellRedisClientActionEvent.eventFormat`

## ShellRedisKeyFlushedEvent
- 职责：Redis 键刷新事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | connect | ShellConnect | 关联连接 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellConnect getConnect()/void setConnect(...)` | 连接存取 | 简单存取（`data()` 为 `Integer` 库索引） |
- 调用链：`ShellRedisEventUtil.redisKeyFlushed → ShellRedisKeyFlushedEvent`

## ShellRedisKeyTTLUpdatedEvent
- 职责：Redis 键 TTL 更新事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | ttl | Long | TTL 值 |
  | key | String | 键名称 |
  | dbIndex | int | 数据库索引 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `Long getTtl()/void setTtl(Long)` | TTL 存取 | 简单存取 |
  | `String getKey()/void setKey(String)` | 键名存取 | 简单存取 |
  | `int getDbIndex()/void setDbIndex(int)` | 库索引存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `[连接] ttlUpdated[key-dbN] ttl:x`（`data()` 为 `ShellConnect`） |
- 调用链：`ShellRedisEventUtil.redisKeyTTLUpdated → ShellRedisKeyTTLUpdatedEvent`

## ShellRedisKeysCopiedEvent
- 职责：Redis 多个键复制事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | sourceDB | int | 源数据库 |
  | targetDB | int | 目标数据库 |
  | connect | ShellConnect | 连接 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `int getTargetDB()/void setTargetDB(int)` | 目标库存取 | 简单存取 |
  | `int getSourceDB()/void setSourceDB(int)` | 源库存取 | 简单存取 |
  | `ShellConnect getConnect()/void setConnect(...)` | 连接存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `[连接] copyKey[keys-db源] targetDatabase:目标`（`data()` 为 `List<String>`） |
- 调用链：`ShellRedisEventUtil.redisKeysCopied → ShellRedisKeysCopiedEvent`

## ShellRedisKeysMovedEvent
- 职责：Redis 多个键移动事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | targetDB | int | 目标数据库 |
  | connect | ShellConnect | 连接 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `int getTargetDB()/void setTargetDB(int)` | 目标库存取 | 简单存取 |
  | `int getSourceDB()` | 源库 | 返回 `data()`（`data()` 为 `Integer` 源库索引） |
  | `ShellConnect getConnect()/void setConnect(...)` | 连接存取 | 简单存取 |
- 调用链：`ShellRedisEventUtil.redisKeysMoved → ShellRedisKeysMovedEvent`

## ShellRedisZSetReverseViewEvent
- 职责：Redis ZSet 反转视图事件。
- 字段：无（`data()` 为 `ShellRedisZSetKeyTreeItem`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `Integer dbIndex()` | 数据库索引 | `data().dbIndex()` |
- 调用链：`ShellRedisEventUtil.redisZSetReverseView → ShellRedisZSetReverseViewEvent`

---

# 七、ZooKeeper 事件（cn.oyzh.easyshell.event.zk）

## ShellZKEventUtil
- 职责：发布 ZooKeeper 相关事件（历史恢复、客户端操作）。
- 字段：无（纯静态方法工具类）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `zkHistoryRestoreUpdated(ShellZKClient client, String nodePath)` | 历史恢复 | `ShellZKHistoryRestoreEvent`，data=client，setNodePath + `post` |
  | `zkClientAction(String connectName, String action)` | 客户端操作 | `ShellZKClientActionEvent`，data=connectName，setAction + `postAsync` |
  | `zkClientAction(String connectName, String action, List<ShellZKClientActionArgument> arguments)` | 客户端操作（带参数） | 同上并 `arguments(arguments)` + `postAsync` |

- 调用链：`业务调用 → ShellZKEventUtil.xxx → new ShellZKXxxEvent → data()/setXxx() → EventUtil.post/postAsync`

## ShellZKClientActionEvent
- 职责：ZK 客户端操作事件（记录“连接名 > 操作 参数...”）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | action | String | 操作 |
  | arguments | List<ShellZKClientActionArgument> | 参数列表（默认容量 12） |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getAction()/void setAction(String)` | 操作存取 | 简单存取 |
  | `List<...> getArguments()/void setArguments(List<...>)` | 参数列表存取 | 简单存取 |
  | `void arguments(List<ShellZKClientActionArgument>)` | 批量添加参数 | `arguments.addAll(...)` |
  | `void arguments(ShellZKClientActionArgument...)` | 批量添加参数（变长） | `addAll(Arrays.asList(...))` |
  | `void argument(ShellZKClientActionArgument)` | 添加参数对象 | `add(...)` |
  | `void argument(String, Object)` | 添加命名参数 | `new ShellZKClientActionArgument(name,value)` |
  | `void argument(Object)` | 添加值参数 | `new ShellZKClientActionArgument(value)` |
  | `String eventFormat()` | 事件描述 | 拼 `data() + " > " + action`；遍历参数：String 超 1024 用 `I18nHelper.dataTooLarge()`，byte[] 同，Number 直接，其他对象 `JSONUtil.toJson` |
- 调用链：`ShellZKEventUtil.zkClientAction → ShellZKClientActionEvent.eventFormat`

## ShellZKHistoryRestoreEvent
- 职责：ZK 历史恢复事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | nodePath | String | 节点路径 |

- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String getNodePath()/void setNodePath(String)` | 节点路径存取 | 简单存取 |
  | `String eventFormat()` | 事件描述 | `[连接:connectName path:nodePath restored data]`（`data()` 为 `ShellZKClient`） |
- 调用链：`ShellZKEventUtil.zkHistoryRestoreUpdated → ShellZKHistoryRestoreEvent`

---

# 附：整文件被注释掉的死代码（跳过，未展开）

以下 12 个文件整文件被 `//` 注释，无可执行正式代码：

- dameng/function/ShellDamengFunctionAddedEvent.java
- dameng/function/ShellDamengFunctionAlertedEvent.java
- dameng/procedure/ShellDamengProcedureAddedEvent.java
- dameng/procedure/ShellDamengProcedureAlertedEvent.java
- dameng/query/ShellDamengQueryAddedEvent.java
- dameng/sql/ShellPrintSqlEvent.java
- dameng/table/ShellDamengTableAddedEvent.java
- dameng/table/ShellDamengTableFilteredEvent.java
- dameng/terminal/ShellDamengTerminalCloseEvent.java
- dameng/view/ShellDamengViewAddedEvent.java
- dameng/view/ShellDamengViewFilteredEvent.java
- mysql/sql/ShellPrintSqlEvent.java

---

# 统计

- 目录文件总数：130
- 死代码文件：12
- 本次覆盖正式类：118
  - 通用/根事件：27
  - 达梦：28（含 util）
  - MySQL：31（含 util）
  - MongoDB：22（含 util）
  - Redis：7（含 util）
  - ZooKeeper：3（含 util）
