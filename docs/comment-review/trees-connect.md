# 代码审查文档 · trees/connect（连接树）

> 范围：`src/main/java/cn/oyzh/easyshell/trees/connect/` 共 9 个类。
> 说明：本组类继承 `RichTreeItem` / `RichTreeItemValue`，通过 `ShellConnectManager` 接口统一分组与连接的增删查；持久化依赖 `ShellGroupStore` / `ShellConnectStore`（均为单例 `INSTANCE`），事件通过 `ShellEventUtil` 广播。

---

## ShellConnectManager
- 职责：定义连接树中「分组节点 / 连接节点」的增删查询契约，屏蔽根节点与分组节点的实现差异。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 接口无字段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void addGroup(ShellGroup group)` | 添加分组 | 由实现持久化并挂载分组节点 |
  | `void addGroupItem(ShellConnectGroupTreeItem item)` | 添加分组节点 | 移动/拖拽分组时使用 |
  | `List<ShellConnectGroupTreeItem> getGroupItems()` | 获取直接子分组节点 | 实现遍历直接子级 |
  | `List<ShellConnectGroupTreeItem> getAllGroupItems()` | 获取所有分组节点（递归） | 实现递归收集 |
  | `void addConnect(ShellConnect shellConnect)` | 添加连接 | 由实现决定落在分组还是根 |
  | `void addConnectItem(ShellConnectTreeItem item)` | 添加连接节点 | 挂载已有节点 |
  | `void addConnectItems(List<ShellConnectTreeItem> items)` | 批量添加连接节点 | 用于分组删除后连接迁移 |
  | `boolean delConnectItem(ShellConnectTreeItem item)` | 删除连接节点 | 持久化删除并移除节点 |
  | `List<ShellConnectTreeItem> getConnectItems()` | 获取直接子连接节点 | |
  | `List<ShellConnectTreeItem> getAllConnectItems()` | 获取所有连接节点（递归） | |
  | `default List<ShellConnectTreeItem> getConnectedItems()` | 获取已连接节点 | 默认直接返回 `getConnectItems()`，根节点重写为递归 |

- 调用链：`ShellConnectRootTreeItem / ShellConnectGroupTreeItem implements ShellConnectManager`

## ShellConnectGroupTreeItem
- 职责：连接树的分组节点，承载子分组/连接，支持重命名、递归删除、移动、拖拽与展开态持久化。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | value | ShellGroup | 分组对象（final） |
  | groupStore | ShellGroupStore | 分组存储（`ShellGroupStore.INSTANCE`，final） |
  | connectStore | ShellConnectStore | 连接存储（`ShellConnectStore.INSTANCE`，final） |
  | onBranchCollapsed | EventHandler\<TreeModificationEvent\<TreeItem\<?\>\>\> | 收缩事件处理器，写回 `expand=false` 并级联收缩后代 |
  | onBranchExpanded | EventHandler\<TreeModificationEvent\<TreeItem\<?\>\>\> | 展开事件处理器，写回 `expand=true` |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellConnectGroupTreeItem(ShellGroup group, RichTreeView treeView)` | 构造分组节点 | `setValue(new ShellConnectGroupTreeItemValue)`；按 `value.isExpand()` 设置展开；`addEventFilter` 注册展开/收缩处理器 |
  | `void collapseDescendants()` | 收缩所有后代节点 | 记忆 `treeView.getSelectedItem()`，递归收缩后 `select` 恢复选中 |
  | `private void collapseDescendants(TreeItem<?> item)` | 递归收缩 | 对每个子节点 `setExpanded(false)` 并递归 |
  | `List<MenuItem> getMenuItems()` | 分组菜单 | 新增连接/新增分组/分隔/重命名/删除/移动到(\*) /分隔/视图菜单/升序/降序 |
  | `private void buildMoveToMenuItems(Menu moveTo, ShellConnectManager manager)` | 构建「移动到」子菜单 | 递归遍历分组，禁用当前分组 |
  | `private void moveTo(ShellConnectManager manager)` | 移动到目标 | `remove()` 后 `manager.addGroupItem(this)` |
  | `void rename()` | 重命名 | `MessageBox.prompt` → `groupStore.replace` → `refresh` + `ShellEventUtil.groupRenamed` |
  | `void delete()` | 删除分组 | 确认后遍历 `getAllGroupItems()` 逐个 `groupStore.delete`；子连接清除 groupId 并迁移到 `treeView.root()`；`ShellEventUtil.groupDeleted`；`remove()` |
  | `private void addConnect()` | 新增连接 | `ShellViewFactory.addConnectGuid(this.value)` |
  | `ShellConnectManager manager()` | 获取父管理器 | 父节点强转 `ShellConnectManager` |
  | `void addGroup()` | 新增子分组 | `MessageBox.prompt` 后 `addGroup(group)` |
  | `void addGroup(ShellGroup group)` | 添加分组 | 设 `pid=getGroupId()`，`groupStore.replace` 后 `addChild` + `ShellEventUtil.groupAdded` |
  | `void addGroupItem(ShellConnectGroupTreeItem item)` | 挂载分组节点 | 设 pid、`groupStore.replace`、`addChild`（去重判断） |
  | `void addConnect(ShellConnect shellConnect)` | 添加连接 | `addConnectItem(new ShellConnectTreeItem(...))` |
  | `void addConnectItem(ShellConnectTreeItem item)` | 挂载连接节点 | 若 groupId 不同则 `connectStore.replace`，再 `addChild` |
  | `void addConnectItems(List<ShellConnectTreeItem> items)` | 批量挂载连接 | `addChild((List) items)` |
  | `boolean delConnectItem(ShellConnectTreeItem item)` | 删除连接 | `connectStore.delete` 后 `removeChild` |
  | `List<ShellConnectTreeItem> getConnectItems()` | 直接子连接 | 遍历 `unfilteredChildren()` 过滤类型 |
  | `List<ShellConnectTreeItem> getAllConnectItems()` | 递归连接 | `findConnectItems` |
  | `boolean allowDrag()/allowDrop()` | 允许拖拽/放置 | 均返回 true |
  | `boolean allowDropNode(DragNodeItem item)` | 校验可放置 | 连接需 groupId 不同；分组需非自身且父 id 不同 |
  | `void onDropNode(DragNodeItem item)` | 处理放置 | 连接→`addConnectItem`，分组→`addGroupItem` |
  | `String getParentId()/getGroupId()/getGroupName()` | 读取父id/分组id/名称 | 委托 `value` |
  | `List<ShellConnectGroupTreeItem> getGroupItems()/getAllGroupItems()` | 直接/递归分组 | 遍历子节点 |
  | `protected void findGroupItems(List<...>)` | 收集分组（含自身） | 递归 |
  | `protected void findConnectItems(List<...>)` | 收集连接 | 递归分组 |
  | `void destroy()` | 销毁 | 移除展开/收缩过滤器并置空处理器 |

- 调用链：`getMenuItems → MenuItemHelper.* → 回调(rename / delete / addGroup / addConnect / moveTo)`；`delete → groupStore.delete → root().addConnectItems → ShellEventUtil.groupDeleted`

## ShellConnectGroupTreeItemValue
- 职责：分组节点的展示值（名称、文件夹图标、子节点数量）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 无实例字段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellConnectGroupTreeItemValue(ShellConnectGroupTreeItem item)` | 构造 | `super(item)` + `setRichMode(true)` |
  | `ShellConnectGroupTreeItem item()` | 返回节点 | 强转 `super.item()` |
  | `String name()` | 名称 | `item().value().getName()` |
  | `SVGGlyph graphic()` | 图标 | 懒加载 `FolderSVGGlyph` |
  | `String extra()` | 附加文本 | 子节点非空时返回 `(子节点数)` |
  | `Color extraColor()` | 附加文本颜色 | `Color.DARKGREY` |

- 调用链：`extra → item().getChildrenSize()`（`graphicColor` 已被整段注释，忽略）

## ShellConnectRootTreeItem
- 职责：连接树根节点，负责加载/持久化分组与连接，提供根级菜单、导入导出、拖拽与连接事件处理。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | groupStore | ShellGroupStore | 分组存储（单例，final） |
  | connectStore | ShellConnectStore | 连接存储（单例，final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellConnectRootTreeItem(ShellConnectTreeView treeView)` | 构造根节点 | `setValue(new ShellConnectRootTreeItemValue())` + `loadChild()` |
  | `ShellConnectTreeView getTreeView()` | 返回树视图 | 强转 |
  | `List<? extends MenuItem> getMenuItems()` | 根菜单 | 新增连接/新增分组/分隔/导出/导入/刷新/分隔/视图菜单/升序/降序 |
  | `private void exportData()` | 导出 | `ShellViewFactory.dataExport()` |
  | `private void importData()` | 导入 | `ShellViewFactory.dataImport(null)` |
  | `private void addConnect()` | 新增连接 | `ShellViewFactory.addConnectGuid()` |
  | `void addGroup()` | 新增分组 | `MessageBox.prompt` → `addGroup(group)` |
  | `private ShellConnectGroupTreeItem getGroupItem(String groupId)` | 按 gid 查分组 | `getAllGroupItems()` 并行流匹配 |
  | `List<ShellConnectGroupTreeItem> getGroupItems()/getAllGroupItems()` | 直接/递归分组 | 遍历子节点 / 递归分组 |
  | `void connectAdded(ShellConnect shellConnect)` | 连接新增事件 | `addConnect` |
  | `void connectUpdated(ShellConnect shellConnect)` | 连接变更事件 | 遍历 `getAllConnectItems()` 用 `==` 命中后更新其 value |
  | `void addGroup(ShellGroup group)` | 添加根分组 | `pid=null`，`groupStore.replace` 后挂载 + `groupAdded` |
  | `void addGroupItem(ShellConnectGroupTreeItem item)` | 挂载分组节点 | `pid=null`，`replace`，`addChild` |
  | `void addConnect(ShellConnect info)` | 添加连接（按分组归位） | 找到分组则 `groupItem.addConnect`，否则挂根并 `expend()` |
  | `void addConnectItem(ShellConnectTreeItem item)` | 挂载连接节点 | 清空 groupId 并 `connectStore.update`，`addChild`，`expend` |
  | `void addConnectItems(List<...>)` | 批量挂载连接 | `addChild` + `expend` |
  | `boolean delConnectItem(ShellConnectTreeItem item)` | 删除连接 | `connectStore.delete` + `removeChild` |
  | `List<ShellConnectTreeItem> getConnectItems()/getAllConnectItems()/getConnectedItems()` | 连接集合 | 直接 / 递归 / 递归（当前实现不过滤连接状态） |
  | `boolean allowDrop()/allowDropNode(DragNodeItem)` | 拖拽校验 | 连接需 groupId 非空；分组需父 id 非空 |
  | `void onDropNode(DragNodeItem item)` | 放置处理 | 连接→`addConnectItem`，分组→`addGroupItem` |
  | `void reloadChild()` | 重新加载 | `super.reloadChild` + `clearChild` + `loadChild` |
  | `void loadChild()` | 加载子节点 | `groupStore.load` → `addGroupChild`；`connectStore.loadFull` → `addConnect`；`refresh` + `doFilter` + `doSort` |
  | `private void addGroupChild(List<ShellGroup> groups, String pid, RichTreeItem<?> pItem)` | 递归构建分组树 | 按 pid 过滤并递归 `addGroupChild`，最后 `pItem.addChild` |

- 调用链：`ShellConnectTreeView.initRoot → new ShellConnectRootTreeItem → loadChild → groupStore.load / connectStore.loadFull → addGroupChild / addConnect`

## ShellConnectRootTreeItemValue
- 职责：连接根节点展示值（名称「主机列表」、Linux 图标）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 无实例字段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `String name()` | 名称 | `I18nHelper.hostList()` |
  | `SVGGlyph graphic()` | 图标 | 懒加载 `LinuxSVGGlyph` |

- 调用链：`name → I18nHelper.hostList`

## ShellConnectTreeItem
- 职责：单个连接的树节点，提供打开/打开SFTP/编辑/重命名/复制信息/克隆/删除/移动/拖拽及连接类型判断。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | value | ShellConnect | 连接信息（可变） |
  | connectStore | ShellConnectStore | 连接存储（单例，final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellConnectTreeItem(ShellConnect value, RichTreeView treeView)` | 构造 | `value(value)` 设置值与节点值 |
  | `ShellConnectTreeView getTreeView()` | 树视图 | 强转 |
  | `List<MenuItem> getMenuItems()` | 连接菜单 | 打开/（SSH 时打开 SFTP）/编辑/重命名/复制信息/克隆/删除/分隔；按类型追加传输数据项（file/redis/zk/mysql/mongo）；「移动到」/分隔/视图菜单 |
  | `private void buildMoveToMenuItems(Menu moveTo, ShellConnectManager manager)` | 构建移动子菜单 | 递归分组，禁用当前分组 |
  | `private void moveTo(ShellConnectManager manager)` | 移动 | `remove()` + `manager.addConnectItem(this)` |
  | `boolean isSSHType()...isRloginType()` 等 | 连接类型判断 | 逐个委托 `value.isXxxType()` |
  | `void clearChild()` | 清空子节点 | `super.clearChild` + `setLoaded(false)` |
  | `void loadChild()` | 加载子节点 | `ShellEventUtil.connectionOpened(this.value)` |
  | `private void editConnect()` | 编辑连接 | 按类型分派 `ShellViewFactory.updateXxxConnect(value)` |
  | `private void copyInfo()` | 复制连接信息 | 按类型拼接协议/主机/端口/用户/密码等，`ClipboardUtil.copy`，失败 `MessageBox.warn` |
  | `private void cloneConnect()` | 克隆连接 | 复制并改名为「原名-克隆」，`connectStore.replace` 后 `connectManager().addConnect` |
  | `void delete()` | 删除连接 | 确认后 `connectManager().delConnectItem(this)` + `ShellEventUtil.connectDeleted` |
  | `void rename()` | 重命名 | `MessageBox.prompt` → `connectStore.update` → 重设节点值 |
  | `void value(ShellConnect value)` | 设置连接 | 赋值并 `setValue(new ShellConnectTreeItemValue(this))` |
  | `ShellConnectManager connectManager()` | 父管理器 | 父节点强转 |
  | `boolean allowDrag()` | 允许拖拽 | true |
  | `void onPrimaryDoubleClick()` | 双击打开 | `ShellEventUtil.connectionOpened(this.value)` |
  | `private void openSFTP()` | 打开 SFTP | 复制连接并 `setType("sftp")`，再 `connectionOpened` |
  | `String connectName()/getId()/getGroupId()` | 名称/id/分组id | 委托 `value` |

- 调用链：`onPrimaryDoubleClick → ShellEventUtil.connectionOpened → value`；`editConnect → ShellViewFactory.updateXxxConnect`；`delete → connectManager().delConnectItem → ShellEventUtil.connectDeleted`

## ShellConnectTreeItemFilter
- 职责：连接节点关键字过滤器（对比名称，忽略大小写）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | 无实例字段 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `boolean test(RichTreeItem<?> item)` | 过滤判定 | 关键字非空且为 `ShellConnectTreeItem` 时按 `connectName()` 忽略大小写包含匹配，否则放行 |

- 调用链：`ShellConnectTreeView.setHighlight → getItemFilter().setKw → test → connectName`

## ShellConnectTreeItemValue
- 职责：连接节点展示值（名称、按操作系统类型取图标、附加信息与着色）。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | setting | ShellSetting | 全局设置（`ShellSettingStore.SETTING`，final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellConnectTreeItemValue(ShellConnectTreeItem item)` | 构造 | `super(item)` + `setRichMode(true)` |
  | `ShellConnectTreeItem item()` | 节点 | 强转 |
  | `String name()` | 名称 | `item().value().getName()` |
  | `String extra()` | 附加文本 | 按设置拼接 `user@host` 与类型（大写），去掉多余 `@` |
  | `SVGGlyph graphic()` | 图标 | 懒加载 `ShellOsTypeComboBox.getGlyph(osType)` |
  | `Color extraColor()` | 颜色 | 按连接类型返回不同色值（SSH/SFTP/FTP/Redis/ZK/…），默认深灰 |

- 调用链：`graphic → ShellOsTypeComboBox.getGlyph`；`extra → setting.isConnectShowMoreInfo / isConnectShowType`

## ShellConnectTreeView
- 职责：连接树视图，初始化根与单元格工厂，订阅分组/连接/导入事件，提供搜索过滤与视图菜单。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | setting | ShellSetting | 全局设置（final） |
  | settingStore | ShellSettingStore | 设置存储（final） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `void initTreeView()` | 初始化树 | `dragContent="shell_connect_tree_drag"`，`setCellFactory(RichTreeCell)` |
  | `void initRoot()` | 初始化根 | `setRoot(new ShellConnectRootTreeItem(this))` + `root().expend()` |
  | `ShellConnectRootTreeItem root()` | 根节点 | 强转 |
  | `void addGroup(ShellAddGroupEvent event)` | 分组新增事件（@EventSubscribe） | `root().addGroup()` |
  | `private void connectAdded(ShellConnectAddedEvent event)` | 连接新增事件 | `root().connectAdded(event.data())` |
  | `private void connectUpdated(ShellConnectUpdatedEvent event)` | 连接变更事件 | `root().connectUpdated(event.data())` |
  | `private void connectImported(ShellDataImportedEvent event)` | 数据导入事件 | `root().reloadChild()` |
  | `ShellConnectTreeItemFilter getItemFilter()` | 过滤器 | 懒建 `ShellConnectTreeItemFilter` |
  | `void setHighlight(String highlightText)` | 设置高亮/搜索 | `super` + `getItemFilter().setKw` |
  | `List<MenuItem> getMenuItems()` | 视图菜单 | 「查看」子菜单：显示类型、显示更多信息（复选） |
  | `private void showType()` | 切换显示类型 | 取反设置，`settingStore.update` + `refresh` |
  | `private void showMoreInfo()` | 切换显示更多信息 | 取反设置，`settingStore.update` + `refresh` |
  | `List<ShellConnectGroupTreeItem> getGroupItems()` | 分组节点 | `root().getGroupItems()` |

- 调用链：`initRoot → ShellConnectRootTreeItem.loadChild`；`connectAdded / connectUpdated / connectImported 事件 → root()`；`setHighlight → getItemFilter().setKw`
