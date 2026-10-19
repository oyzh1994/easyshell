# 代码审查文档 · trees/query 与 trees/snippet（查询树、片段树）

> 范围：`trees/query/`（5 个类）、`trees/snippet/`（5 个类），共 10 个类。
> 说明：两组结构高度对称——`XxxRootTreeItem` 负责按连接 id / 全量加载列表，`XxxTreeItem` 为单条目节点，`XxxTreeView` 持有增删改回调（`Consumer`）并与外部编辑器联动，`XxxTreeItemValue` 负责展示（含「未保存」标记）。

---

# query 包

## ShellQueryRootTreeItem
- 职责：查询树根节点，按连接 id（iid）加载查询列表。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | queryStore | ShellQueryStore | 查询存储（`ShellQueryStore.INSTANCE`，final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellQueryRootTreeItem(RichTreeView treeView)` | 构造 | `setValue(new ShellQueryRootTreeItemValue())`（`loadChild` 被注释，改由 `setIid` 触发） |
  | `ShellQueryTreeView getTreeView()` | 树视图 | 强转 |
  | `void reloadChild()` | 重新加载 | `super` + `clearChild` + `loadChild` |
  | `void loadChild()` | 加载子节点 | `queryStore.list(iid)` → `new ShellQueryTreeItem` → `addChild` + `refresh` |
  | `List<MenuItem> getMenuItems()` | 菜单 | 直接返回 `getTreeView().getMenuItems()` |

- 调用链：`ShellQueryTreeView.setIid → root().loadChild → queryStore.list(iid) → new ShellQueryTreeItem`

## ShellQueryRootTreeItemValue
- 职责：查询根节点展示值（名称、Query 图标）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 无实例字段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String name()` | 名称 | `I18nHelper.queries()` |
  | `SVGGlyph graphic()` | 图标 | 懒加载 `QuerySVGGlyph` |

- 调用链：`name → I18nHelper.queries`

## ShellQueryTreeItem
- 职责：单个查询节点，支持编辑/重命名/删除与「未保存」标记。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | queryStore | ShellQueryStore | 查询存储（单例，final） |
  | value | ShellQuery | 查询对象 |
  | unsaved | BooleanProperty | 未保存标记（`SimpleBooleanProperty(false)`，final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellQueryTreeItem(ShellQuery value, RichTreeView treeView)` | 构造 | `setSortable(false)` + `value(value)` |
  | `ShellQueryTreeView getTreeView()` | 树视图 | 强转 |
  | `ShellQuery value()` | 查询对象 | |
  | `List<MenuItem> getMenuItems()` | 菜单 | 视图菜单 + 编辑/重命名/删除 |
  | `void delete()` | 删除 | 确认后 `queryStore.delete` → `treeView.deleteQuery` → `remove` |
  | `private void edit()` | 编辑 | `treeView.editQuery(value)` |
  | `void rename()` | 重命名 | `MessageBox.prompt` → `queryStore.update` → 重设节点值 |
  | `void value(ShellQuery value)` | 设置值 | 赋值并 `setValue(new ShellQueryTreeItemValue(this))` |
  | `void onPrimaryDoubleClick()` | 双击编辑 | `treeView.editQuery(value)` |
  | `String queryName()` | 名称 | `value.getName()` |
  | `String getId()` | 唯一标识 | `value.getUid()` |
  | `BooleanProperty unsavedProperty()` | 未保存属性 | |
  | `void setUnsaved(boolean)/boolean isUnsaved()` | 读写未保存 | |

- 调用链：`onPrimaryDoubleClick → ShellQueryTreeView.editQuery → editCallback`；`delete → queryStore.delete → treeView.deleteQuery`

## ShellQueryTreeItemValue
- 职责：查询节点展示值（名称、Query 图标、未保存时显示 ` *` 红字）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 无实例字段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellQueryTreeItemValue(ShellQueryTreeItem item)` | 构造 | `super(item)` + `setRichMode(true)` |
  | `ShellQueryTreeItem item()` | 节点 | 强转 |
  | `String name()` | 名称 | `item().value().getName()` |
  | `SVGGlyph graphic()` | 图标 | 懒加载 `QuerySVGGlyph` |
  | `String extra()` | 附加文本 | 未保存返回 `" *"`，否则 `super.extra()` |
  | `Color extraColor()` | 颜色 | 未保存返回 `Color.RED`，否则继承 |

- 调用链：`extra/extraColor → item().isUnsaved()`

## ShellQueryTreeView
- 职责：查询树视图，管理连接 id 与增删改查询，通过回调与外部编辑器联动。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | iid | String | 连接 id |
  | queryStore | ShellQueryStore | 查询存储（单例，final） |
  | addCallback | Consumer\<ShellQuery\> | 新增查询回调 |
  | editCallback | Consumer\<ShellQuery\> | 编辑查询回调 |
  | deleteCallback | Consumer\<ShellQuery\> | 删除查询回调 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initTreeView()` | 初始化树 | `setCellFactory(RichTreeCell)` |
  | `void initRoot()` | 初始化根 | `setRoot(new ShellQueryRootTreeItem(this))` |
  | `Consumer<ShellQuery> getAddCallback()/setAddCallback(...)` | 新增回调读写 | |
  | `Consumer<ShellQuery> getEditCallback()/setEditCallback(...)` | 编辑回调读写 | |
  | `Consumer<ShellQuery> getDeleteCallback()/setDeleteCallback(...)` | 删除回调读写 | |
  | `void addQuery()` | 新增查询 | 有回调时 `MessageBox.prompt` 名称 → `new ShellQuery`（含 iid）→ `addQuery(query)` + `queryStore.insert` + 回调 |
  | `void addQuery(ShellQuery query)` | 挂载查询节点 | `root().addChild(new ShellQueryTreeItem)` |
  | `void editQuery(ShellQuery query)` | 编辑查询 | 触发 `editCallback` |
  | `void deleteQuery(ShellQuery query)` | 删除查询 | 触发 `deleteCallback` |
  | `List<MenuItem> getMenuItems()` | 菜单 | 新增查询 |
  | `void setIid(String iid)` | 设置连接 id 并加载 | 记录 iid，`root().loadChild()` + `root().expend()` |
  | `String getIid()` | 获取连接 id | |

- 调用链：`setIid → root().loadChild`；`addQuery → queryStore.insert + addCallback`；`ShellQueryTreeItem.edit → editQuery → editCallback`

---

# snippet 包

## ShellSnippetRootTreeItem
- 职责：片段树根节点，加载全部片段列表。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | snippetStore | ShellSnippetStore | 片段存储（`ShellSnippetStore.INSTANCE`，final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellSnippetRootTreeItem(RichTreeView treeView)` | 构造 | `setValue(new ShellSnippetRootTreeItemValue())` + `loadChild()` |
  | `ShellSnippetTreeView getTreeView()` | 树视图 | 强转 |
  | `void reloadChild()` | 重新加载 | `super` + `clearChild` + `loadChild` |
  | `void loadChild()` | 加载子节点 | `snippetStore.selectList()` → `new ShellSnippetTreeItem` → `addChild` + `refresh` |
  | `List<MenuItem> getMenuItems()` | 菜单 | 返回 `getTreeView().getMenuItems()` |

- 调用链：`ShellSnippetTreeView.initRoot → new ShellSnippetRootTreeItem → loadChild → snippetStore.selectList`

## ShellSnippetRootTreeItemValue
- 职责：片段根节点展示值（名称、Snippet 图标）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 无实例字段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String name()` | 名称 | `I18nHelper.snippetList()` |
  | `SVGGlyph graphic()` | 图标 | 懒加载 `SnippetSVGGlyph` |

- 调用链：`name → I18nHelper.snippetList`

## ShellSnippetTreeItem
- 职责：单个片段节点，支持编辑/重命名/删除与「未保存」标记。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | snippetStore | ShellSnippetStore | 片段存储（单例，final） |
  | value | ShellSnippet | 片段对象 |
  | unsaved | BooleanProperty | 未保存标记（final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellSnippetTreeItem(ShellSnippet value, RichTreeView treeView)` | 构造 | `setSortable(false)` + `value(value)` |
  | `ShellSnippetTreeView getTreeView()` | 树视图 | 强转 |
  | `ShellSnippet value()` | 片段对象 | |
  | `List<MenuItem> getMenuItems()` | 菜单 | 视图菜单 + 编辑/重命名/删除 |
  | `void delete()` | 删除 | 确认后 `snippetStore.delete` → `treeView.deleteSnippet` → `remove` |
  | `private void edit()` | 编辑 | `treeView.editSnippet(value)` |
  | `void rename()` | 重命名 | `MessageBox.prompt` → `snippetStore.update` → 重设节点值 |
  | `void value(ShellSnippet value)` | 设置值 | 赋值并 `setValue(new ShellSnippetTreeItemValue(this))` |
  | `void onPrimaryDoubleClick()` | 双击编辑 | `treeView.editSnippet(value)` |
  | `String snippetName()` | 名称 | `value.getName()` |
  | `String getId()` | 唯一标识 | `value.getId()` |
  | `BooleanProperty unsavedProperty()` | 未保存属性 | |
  | `void setUnsaved(boolean)/boolean isUnsaved()` | 读写未保存 | |

- 调用链：`onPrimaryDoubleClick → ShellSnippetTreeView.editSnippet → editCallback`；`delete → snippetStore.delete → treeView.deleteSnippet`

## ShellSnippetTreeItemValue
- 职责：片段节点展示值（名称、Snippet 图标、未保存时显示 ` *` 红字）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 无实例字段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellSnippetTreeItemValue(ShellSnippetTreeItem item)` | 构造 | `super(item)` + `setRichMode(true)` |
  | `ShellSnippetTreeItem item()` | 节点 | 强转 |
  | `String name()` | 名称 | `item().value().getName()` |
  | `SVGGlyph graphic()` | 图标 | 懒加载 `SnippetSVGGlyph` |
  | `String extra()` | 附加文本 | 未保存返回 `" *"`，否则继承 |
  | `Color extraColor()` | 颜色 | 未保存返回 `Color.RED`，否则继承 |

- 调用链：`extra/extraColor → item().isUnsaved()`

## ShellSnippetTreeView
- 职责：片段树视图，管理增删改片段并回调外部编辑器。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | snippetStore | ShellSnippetStore | 片段存储（单例，final） |
  | addCallback / editCallback / deleteCallback | Consumer\<ShellSnippet\> | 新增/编辑/删除回调 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initTreeView()` | 初始化树 | `setCellFactory(RichTreeCell)` |
  | `void initRoot()` | 初始化根 | `setRoot(new ShellSnippetRootTreeItem(this))` + `root().expend()` |
  | `Consumer<ShellSnippet> getAddCallback()/setAddCallback(...)` | 新增回调读写 | |
  | `Consumer<ShellSnippet> getEditCallback()/setEditCallback(...)` | 编辑回调读写 | |
  | `Consumer<ShellSnippet> getDeleteCallback()/setDeleteCallback(...)` | 删除回调读写 | |
  | `void addSnippet()` | 新增片段 | 有回调时 `MessageBox.prompt` 名称 → `new ShellSnippet` → `addSnippet(snippet)` + `snippetStore.insert` + 回调 |
  | `void addSnippet(ShellSnippet snippet)` | 挂载片段节点 | `root().addChild(new ShellSnippetTreeItem)` |
  | `void editSnippet(ShellSnippet snippet)` | 编辑片段 | 触发 `editCallback` |
  | `void deleteSnippet(ShellSnippet snippet)` | 删除片段 | 触发 `deleteCallback` |
  | `List<MenuItem> getMenuItems()` | 菜单 | 新增片段 |

- 调用链：`initRoot → ShellSnippetRootTreeItem.loadChild`；`addSnippet → snippetStore.insert + addCallback`；`ShellSnippetTreeItem.edit → editSnippet → editCallback`
