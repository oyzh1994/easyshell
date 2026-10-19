# 代码审查文档 · trees/zk（ZooKeeper 树）

> 范围：`src/main/java/cn/oyzh/easyshell/trees/zk/` 共 9 个类。
> 说明：`ShellZKTreeView` 初始化 zk 节点树，`ShellZKNodeTreeItem` 为节点叶子（递归自嵌套构成整棵 znode 树），支持树状/列表两种呈现模式（列表模式下带"返回上级"节点）。业务逻辑下沉到 `ShellZKClient`、`ShellZKNodeUtil`、`ShellZKDataUtil`、`ShellZKACLUtil`、`ShellZKViewFactory`、`ShellZKCollectStore`。

---

## ShellZKTreeItem
- 职责：ZooKeeper 树所有节点的抽象基类，统一收窄 `getTreeView()` 返回类型。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellZKTreeItem(RichTreeView treeView)` | 构造节点 | `super(treeView)` |
  | `ShellZKTreeView getTreeView()` | 返回 zk 树视图 | `(ShellZKTreeView) super.getTreeView()` |

- 调用链：继承 `RichTreeItem<V>`；被 `ShellZKNodeTreeItem` / `ShellZKMoreTreeItem` / `ShellZKReturnTreeItem` 继承。

## ShellZKTreeItemFilter
- 职责：zk 树节点过滤器，按节点类型（全部/收藏/持久/临时）与关键字过滤。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `type` | `byte` | 过滤类型：0 全部 / 1 收藏 / 2 持久 / 3 临时 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean test(RichTreeItem<?> item)` | 判断是否命中 | 对 `ShellZKNodeTreeItem`：根节点放行；按 `type` 判定收藏/持久/临时；再取 `decodeNodePath()` 调 `TextUtil.findText` 比较 `NOT_FOUND` |
  | `getType()/setType(byte)` | 类型存取 | 简单存取 |

- 调用链：`ShellZKTreeView.getItemFilter`；使用 `TextUtil.findText`

## ShellZKTreeView
- 职责：ZooKeeper 节点树视图，持客户端与根节点，支持加载根、查找节点、节点新增定位、认证变更、树状/列表模式及未保存数据检测。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `client` | `ShellZKClient` | ZooKeeper 客户端 |
  | `contentListViewport` | `boolean`(final) | 是否列表模式，取 `ShellSettingStore.SETTING.isZkContentListViewport()` |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `client(ShellZKClient)` / `client()` | 设置/获取客户端 | 简单存取 |
  | `connect()` | 获取连接信息 | `client.getShellConnect()` |
  | `initTreeView()` | 初始化单元格工厂 | `RichTreeCell` |
  | `getItemFilter()` | 懒初始化过滤器 | `new ShellZKTreeItemFilter()` |
  | `root()` | 收窄根节点 | `(ShellZKNodeTreeItem) super.root()` |
  | `findNodeItem(String)` / `findNodeItem(ShellZKNodeTreeItem, String)` | 按路径递归查找节点 | 路径相等返回；互不包含/无子节点返回 null；否则遍历 `itemChildren()` 递归 |
  | `expand()` / `collapse()` | 展开/收缩全部（选中项） | `item.expandAll()/collapseAll()` 后 `select(item)` |
  | `nodeAdded(String)` | 节点新增事件处理 | 找父节点（列表模式父非根则 `loadRoot(pPath)` 重载）；已存在 `refreshNode`，父已加载 `addChild`，否则 `loadChild(false)`；再 filter/sort 并 `selectAndScroll` |
  | `getAllNodeItem()` / `getAllNodeItem(ShellZKNodeTreeItem,List)` | 递归收集全部节点 | 私有，供认证/未保存检测遍历 |
  | `authChanged(ShellZKAuth)` | 认证变更 | `client.addAuth(...)`；对需要认证或含该 digest 的节点 `authChanged()` |
  | `loadRoot()` / `loadRoot(String)` | 加载根 | `ShellZKNodeUtil.getNode(client, path)` → `new ShellZKNodeTreeItem` → `root(rootItem)` → `rootItem.loadRoot()` |
  | `hasUnsavedData()` | 是否有未保存数据 | 遍历全部节点 `isDataUnsaved()` |

- 调用链：`ShellZKTreeView → ShellZKClient / ShellZKNodeUtil / ShellZKACLUtil → ShellZKNodeTreeItem`。**注意**：含大量被注释的搜索/事件死代码（`onNodeCreated/onNodeRemoved/onNodeChanged/onSearchTrigger/onSearchFinish` 等）。

## ShellZKNodeTreeItem
- 职责：zk 节点叶子（自嵌套构成整棵树），封装节点数据/ACL/配额/状态刷新、增删改、加载子节点、认证、收藏、列表模式返回上级等。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `ShellZKNode`（protected） | zk 节点对象 |
  | `contentListViewport` | `boolean`(final) | 是否列表模式 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellZKNodeTreeItem(ShellZKNode, ShellZKTreeView)` | 构造 | `setFilterable(true)`、`setValue(new ShellZKNodeTreeItemValue(this))` |
  | `value()` / `value(ShellZKNode)` | 节点存取 | 简单存取 |
  | `setNeedAuth/isNeedAuth/setCanceled/isCanceled` | 位标志存取 | 经 `bitValue()` 第 14/15 位；`isNeedAuth` 亦查 `client().isNeedAuth(value)` |
  | `getNodeData()` / `nodeData(byte[])` / `getData()` / `getUnsavedData()` / `clear()` / `isDataUnsaved()` | 节点数据存取 | `getData()` 优先未保存数据，空则空数组；`nodeData` 设未保存并 refresh |
  | `nodePath()` / `nodeName()` / `decodeNodePath()` / `decodeNodeName()` | 路径/名称 | 委托 `value` |
  | `loadChild()` | 异步加载子节点 | `TaskBuilder`：`CostUtil` 计时 + `loadChild(false)` + filter/sort |
  | `getMenuItems()` | 右键菜单 | 加载中显示取消；否则 新增/重命名/克隆/删除、重载/导出、数据历史/复制路径/认证、排序、卸载、加载全部/展开/收缩全部（非列表模式），并按节点类型/权限 `setDisable` |
  | `dataHistory()` / `copyNodePath()` / `cloneNode()` | 数据历史/复制路径/克隆 | `ShellZKViewFactory.zkHistoryData`；`ClipboardUtil.copy`；克隆节点 `client().create(...)` 后 `treeView.nodeAdded` |
  | `cancel()` | 取消加载 | `setCanceled(true)` |
  | `addNode()` | 新增子节点 | `ShellZKViewFactory.zkAddNode(this, client)` → `nodeAdded(addedNodePath)` |
  | `authNode()` | 节点认证 | `ShellZKViewFactory.zkAuthNode`，成功后 `treeView.authChanged(auth)` |
  | `exportData()` | 导出节点 | `ShellZKViewFactory.zkExportData(zkConnect(), nodePath())` |
  | `rename()` | 重命名 | 校验后 `create` 新路径 + `deleteNode` 旧节点 + `nodeAdded(newNodePath)` |
  | `delete()` / `deleteNode()` | 删除节点 | `Task` 内 `client().delete(path, null, isParentNode)` + `ShellZKDataUtil.deleteHistory` + 刷新父状态 + `remove()` |
  | `parent()` | 父节点 | `(ShellZKNodeTreeItem) getParent()` |
  | `unloadChild()` / `loadChildAll()` | 卸载/加载全部 | `loadChild(true, 0)`（递归不限量） |
  | `collapseAll()` / `expandAll()` | 收缩/展开全部 | `Task` 内递归 + `select(this)` |
  | `getNodeItem(String)` | 按路径取直接子节点 | 遍历 `unfilteredChildren()` 比对 `decodeNodePath()` |
  | `loadPrent()` | 加载父节点 | 取父路径节点重建根并 `loadChild()` |
  | `remove()` | 移除节点 | 根节点则收藏取消 + `loadPrent`；否则移除自身、刷新父状态、恢复选中（`nextSibling` 或父节点） |
  | `addChild(String)` / `addChild(ShellZKNode)` | 新增子节点 | `ShellZKNodeUtil.getNode` 后 `addChild(new ShellZKNodeTreeItem(...))` |
  | `client()` / `zkConnect()` | 客户端/连接信息 | 取树视图 |
  | `refreshNode/refreshData/refreshACL/refreshQuota/refreshStat()` | 刷新节点/数据/ACL/配额/状态 | `ShellZKNodeUtil.refreshXxx`；`NoAuthException` → `setNeedAuth(true)`，`NoNodeException`（配额）→ `quota(null)` |
  | `saveData()` | 保存节点数据 | `client().setData(path, data)` 后更新 `value.stat/setNodeData` + `saveHistory` + `clear` |
  | `reloadChild()` | 重载 | 非等待/加载中时 `refreshNode()` + `loadChild()` |
  | `loadChild(boolean)` / `loadChild(boolean,int)` / `doLoadChild(boolean,int)` | 加载子节点 | `doLoadChild`：无子清空；否则 `ShellZKNodeUtil.getChildNode` 增量添加，达 limit 追加 `ShellZKMoreTreeItem`；列表模式非根追加 `ShellZKReturnTreeItem`，树状模式移除；递归 `loop`；列表模式更新根 |
  | `copy(ShellZKNode)` / `nodeEquals(ShellZKNode)` | 复制/相等 | 委托 `value` |
  | `moreChildren()` / `returnChildren()` / `itemChildren()` / `itemChildrenSize()` | 子节点辅助 | 过滤 `unfilteredChildren()` |
  | `sortChild(boolean)` / `sortAsc()` / `sortDesc()` | 排序 | `ShellZKReturnTreeItem` 恒首、`ShellZKMoreTreeItem` 恒末；`sortAsc/Desc` 递归子节点 |
  | `isCollect()/collect()/unCollect()/iid()` | 收藏 | `ShellZKCollectStore.INSTANCE`；`iid()` 取连接 id |
  | `deleteACL(ShellZKACL)` / `acl()` / `aclEmpty()` / `hasReadPerm()` / `hasWorldACL()` / `existDigestACL(String)` / `existIPACL(String)` | ACL 操作/查询 | 大多委托 `value`；`deleteACL` 调 `client()` |
  | `statInfos()` / `getNumChildren()` | 状态信息/子节点数 | 委托 `value` |
  | `isPersistentNode()/isEphemeralNode()/isRootNode()/isChildrenNode()/isParentNode()` | 节点类型判定 | 委托 `value` |
  | `connectName()` | 连接名称 | `zkConnect().getName()` |
  | `quota()` / `saveQuota(long,int)` | 配额查询/保存 | 懒刷新；`saveQuota` 先 `delQuota` 再按需 `createQuota` |
  | `loadTime()` | 加载耗时 | `value.loadTime()`，0 归一为 1 |
  | `onPrimaryDoubleClick()` | 双击 | 未加载则 `loadChild()` |
  | `saveHistory()` | 保存数据历史（私有） | `ShellZKDataUtil.addHistory` |
  | `authChanged()` | 授权变更响应 | `setNeedAuth(false)` + `refreshNode` + `loadRoot` + `refresh` |
  | `loadRoot()` | 加载根/子 | 根节点按设置 `isLoadFirst`→`loadChild` / `isLoadAll`→`loadChildAll`；否则 `loadChild` |
  | `destroy()` | 销毁 | 清节点数据/未保存数据；非根则 `value=null` + 父类 destroy |
  | `dataSize()` / `dataSizeInfo()` | 数据大小/格式化 | `getData().length`，B/KB/MB/GB |

- 调用链：`ShellZKNodeTreeItem → ShellZKClient`（create/delete/setData/quota）、`ShellZKNodeUtil`（getNode/getChildNode/refreshXxx）、`ShellZKDataUtil`（历史）、`ShellZKViewFactory`（弹窗）、`ShellZKCollectStore.INSTANCE`（收藏）；自嵌套子节点。**注意**：含大量被注释的位标志/事件相关死代码（`setBeChanged/isBeChanged`、`setBeDeleted`、`setBeChildChanged`、`isDataTooBig`、`compareTo` 重写等）。

## ShellZKNodeTreeItemValue
- 职责：zk 节点展示值，按认证/临时状态切换图标，节点数据未保存时图标橙色。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `showNodePath` | `boolean`(final) | 是否显示完整路径，取 `ShellSettingStore.SETTING.isShowNodePath()` |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellZKNodeTreeItemValue(ShellZKNodeTreeItem)` | 构造 | `setRichMode(true)` |
  | `item()` | 收窄节点 | `(ShellZKNodeTreeItem) super.item()` |
  | `graphic()` | 图标 | 依 `isNeedAuth/ isEphemeralNode` 选 `LockSVGGlyph/TempSVGGlyph/NodeSVGGlyph`，设置颜色并 `disableTheme` |
  | `extra()` | 附加文本 | `(显示子数/总子数)` 或 `(总数)` |
  | `extraColor()` | 附加色 | `Color.FORESTGREEN` |
  | `graphicColor()` | 图标色 | 数据未保存 → `Color.ORANGE` |
  | `name()` | 名称 | `showNodePath ? decodeNodePath() : decodeNodeName()` |

- 调用链：继承 `RichTreeItemValue`

## ShellZKMoreTreeItem
- 职责："加载更多"占位节点，双击触发父节点继续加载。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellZKMoreTreeItem(ShellZKTreeView)` | 构造 | `setSortable(false)`、`setFilterable(false)`、`setValue(...)` |
  | `parent()` | 收窄父节点 | `(ShellZKNodeTreeItem) getParent()` |
  | `onPrimaryDoubleClick()` | 双击加载更多 | `parent().loadChild()` |
  | `compareTo(Object)` | 比较 | 同类返回 0，否则恒排末位（1） |

- 调用链：`ShellZKMoreTreeItem → ShellZKNodeTreeItem.loadChild`

## ShellZKMoreTreeItemValue
- 职责："加载更多"节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `graphic()` | 图标 | 懒创建 `MoreSVGGlyph` |
  | `name()` | 名称 | `I18nHelper.loadMore()` |

- 调用链：继承 `RichTreeItemValue`

## ShellZKReturnTreeItem
- 职责：列表模式下的"返回上级"占位节点，双击回退到父目录。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellZKReturnTreeItem(ShellZKTreeView)` | 构造 | `setSortable(false)`、`setFilterable(false)`、`setValue(...)` |
  | `parent()` | 收窄父节点 | `(ShellZKNodeTreeItem) getParent()` |
  | `onPrimaryDoubleClick()` | 双击返回上级 | `parent().loadPrent()` |
  | `compareTo(Object)` | 比较 | 同类返回 0，否则恒排首位（-1） |

- 调用链：`ShellZKReturnTreeItem → ShellZKNodeTreeItem.loadPrent`

## ShellZKReturnTreeItemValue
- 职责："返回上级"节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `graphic()` | 图标 | 懒创建 `ParentDirSVGGlyph` |
  | `name()` | 名称 | `I18nHelper.parentDir()` |

- 调用链：继承 `RichTreeItemValue`
