# easyshell 弹窗控制器（popups 包）代码审查文档

> 范围：`easyshell/src/main/java/cn/oyzh/easyshell/popups/`（含 zk/redis/term/snippet/dameng/mysql/db/mongo 子包），共 26 个 `.java`，其中存活 20 个、注释死代码 6 个。
> 说明：仅新增文档，未改动任何 `.java`。

所有存活类均继承 `cn.oyzh.fx.plus.controller.PopupController`，通过 `@PopupAttribute(value = FXConst.POPUP_PATH + ".../xxx.fxml")` 绑定 FXML；调用方统一走 `PopupManager.parsePopup(XxxPopupController.class)` 拿到 `PopupAdapter`，再 `setProp(...)` 传参、`setSubmitHandler(...)` 注册回调、`showPopup(node)` 弹出。弹窗内部通过 `getProp(...)` 取参、`submit(...)` 回传结果、`closeWindow()` 关闭。

---

## zk 子包

## ShellZKNodeQRCodePopupController
- 职责：把 ZooKeeper 节点的节点路径与数据渲染成二维码并展示。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| qrcode | `ImageView` | 二维码图片控件，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | 先 `super.onWindowShowing(event)`，再调用 `initQRCode()` |
| `initQRCode()` | 生成二维码 | `getProp("zkNode")` 取 `ShellZKNode`、`getProp("nodeData")` 取数据；拼装「节点路径 + 节点数据」文本；`NodeUtil.getWidth/getHeight(qrcode)` 取控件宽高；`QRCodeUtil.createImage(text, "utf-8", codeW, codeH)` 生成 `BufferedImage`；`FXUtil.toImage(source)` 转 `Image` 后 `qrcode.setImage(...)`；异常时 `closeWindow()`、`JulLog.warn` 并 `MessageBox.warn(I18nHelper.operationFail())` |

- 调用链：`ShellZKNodeDataTabController.node2QRCode → PopupManager.parsePopup → setProp("zkNode","nodeData") → showPopup → onWindowShowing → initQRCode → QRCodeUtil.createImage → FXUtil.toImage`

---

## redis 子包

## ShellRedisKeyFilterPopupController
- 职责：Redis 键过滤条件输入弹窗，支持过滤历史记录。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| keyFilter | `SearchTextField` | 过滤模式输入框，`@FXML` 注入 |
| historyStore | `RedisKeyFilterHistoryStore` | 过滤历史存储，`final` 单例 `RedisKeyFilterHistoryStore.INSTANCE` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `onWindowShown(WindowEvent event)` | 窗口展示完成回调 | `getProp("pattern")` 非空时回填 `keyFilter`；`keyFilter.requestFocus()`；`keyFilter.setHistoryPopup(new ShellRedisKeyFilterHistoryPopup())` 挂载历史下拉 |
| `apply()` | 应用过滤，`@FXML` | 取输入文本；非空且不为 `"*"` 时构造 `ShellRedisKeyFilterHistory` 并 `historyStore.insert(history)`；`submit(pattern)` 回传；`closeWindow()` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |

- 调用链：`ShellRedisDatabaseTreeItem.filterKey → PopupManager.parsePopup → setProp("pattern") → setSubmitHandler → showPopup → onWindowShown → apply → RedisKeyFilterHistoryStore.insert → submit(pattern) → 回调 setFilterPattern/unloadChild/loadChild`

---

## ShellRedisKeyQRCodePopupController
- 职责：把 Redis 键信息（键名、库索引、数据）渲染成二维码并展示。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| qrcode | `ImageView` | 二维码图片控件，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | 先 `super`，再 `initQRCode()` |
| `initQRCode()` | 生成二维码 | `getProp("key")` 取 `ShellRedisKey`、`getProp("keyData")` 取数据；拼装「键 + 数据库 + 数据」文本；`NodeUtil.getWidth/getHeight(qrcode)` 取尺寸；`QRCodeUtil.createImage(text, "utf-8", codeW, codeH)` → `FXUtil.toImage` → `qrcode.setImage`；异常时 `closeWindow()`，并用 `ExceptionUtil.hasMessage(ex, "Data too big")` 区分：数据过大提示 `I18nHelper.dataTooLarge()`，否则打印堆栈 + `JulLog.warn` + `MessageBox.warn(I18nHelper.operationFail())` |

- 调用链：`ShellRedisStringKeyController / ShellRedisJsonKeyController → PopupManager.parsePopup → setProp("key","keyData") → showPopup → onWindowShowing → initQRCode → QRCodeUtil.createImage → FXUtil.toImage`

---

## ShellRedisPageSettingPopupController
- 职责：Redis 数据列表每页条数设置弹窗。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| limit | `NumberTextField` | 每页限制输入框，`@FXML` 注入 |
| setting | `ShellSetting` | 全局设置对象，`final`，取 `ShellSettingStore.SETTING` |
| settingStore | `ShellSettingStore` | 设置存储，`final` 单例 `ShellSettingStore.INSTANCE` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `apply()` | 保存并应用，`@FXML` | `limit.getIntValue()` → `setting.setRowPageLimit(limit)` → `settingStore.update(setting)` → `submit(limit)` → `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `bindListeners()` | 监听绑定 | 仅调 `super.bindListeners()`（空覆写） |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `limit.setValue(setting.getRowPageLimit())` 回显当前值 |
| `onWindowHidden(WindowEvent event)` | 窗口隐藏回调 | 置空 `limit` 引用 |

- 调用链：`ShellRedisRowKeyController.pageSetting → PopupManager.parsePopup → showPopup → onWindowShowing → （用户点击应用）apply → ShellSettingStore.update → submit(limit) → 回调 firstPage`

---

## term 子包

## ShellTermHistoryPopupController
- 职责：SSH 终端命令历史搜索与选择弹窗。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| client | `ShellSSHClient` | SSH 客户端，`getProp("client")` 注入 |
| kw | `ClearableTextField` | 关键字输入框，`@FXML` 注入 |
| listView | `ShellTermHistoryListView` | 历史列表组件，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `initList()` | 按关键字刷新历史列表 | `ShellSSHUtil.histories(client, kw, 100)` 拉取历史（注释写「最近 200 条」但实际传参为 `100`，注释与代码不一致）→ `listView.init(histories)`；异常 `MessageBox.exception(ex)` |
| `bindListeners()` | 绑定监听 | `kw.addTextChangeListener` 内用 `ThreadUtil.start(this::initList)` 异步刷新；`listView.setOnItemPicked` 里取 `getPickedItem()` → `submit(history)` → `closeWindow()` |
| `onWindowShown(WindowEvent event)` | 窗口展示完成回调 | `getProp("client")`、`getProp("histories")`，用初始历史 `listView.init(histories)` |

- 调用链：`ShellViewFactory.termHistory(parent, client, histories, callback) → PopupManager.parsePopup → setProp("client","histories") → setSubmitHandler(callback) → showPopup → onWindowShown → initList（关键字变化时 ShellSSHUtil.histories）→ 选中项 submit → 回调`

---

## snippet 子包

## ShellSnippetPopupController
- 职责：代码片段搜索、预览与选择弹窗。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| kw | `ClearableTextField` | 关键字输入框，`@FXML` 注入 |
| listView | `ShellSnippetListView` | 片段列表组件，`@FXML` 注入 |
| editor | `ShellDataEditor` | 片段内容预览编辑器，`@FXML` 注入 |
| snippetStore | `ShellSnippetStore` | 片段存储，`final` 单例 `ShellSnippetStore.INSTANCE` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `initList()` | 按名称关键字刷新片段列表 | `snippetStore.listByName(kw)` → `listView.init(snippets)`；异常 `MessageBox.exception(ex)` |
| `bindListeners()` | 绑定监听 | `kw.addTextChangeListener` 内 `ThreadUtil.start(this::initList)`；`listView.selectedItemChanged` 中取 `getPickedItem()`，为空 `editor.clear()`，否则 `editor.showData(snippet.getContent())`；`listView.setOnItemPicked` 中 `submit(snippet)` → `closeWindow()` |
| `onWindowShown(WindowEvent event)` | 窗口展示完成回调 | `initList()` 初始化列表 |

- 调用链：`ShellViewFactory.snippetList(parent, callback) → PopupManager.parsePopup → setSubmitHandler(callback) → showPopup → onWindowShown → initList → ShellSnippetStore.listByName → 选中项 editor.showData → submit(snippet) → 回调`

---

## dameng 子包

## ShellDamengColumnFieldPopupController
- 职责：达梦表字段选择弹窗（多选字段列表，支持行上下移动）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| onSubmit | `Runnable` | 提交回调，`getProp("onSubmit")` 注入 |
| listView | `DamengColumnListView` | 字段列表组件，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `submit()` | 提交，`@FXML` | `onSubmit != null` 时 `onSubmit.run()`，再 `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `moveUpRow()` | 上移行，`@FXML` | `ListViewUtil.moveUp(listView)` |
| `moveDownRow()` | 下移行，`@FXML` | `ListViewUtil.moveDown(listView)` |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("onSubmit")`、`getProp("columns")`、`getProp("selectedColumns")`；`listView.init(columns)` → `listView.select(selectedColumns)` |

- 调用链：`DamengFieldTextFiled.initPopup → PopupManager.parsePopup → setProp("columns","selectedColumns","onSubmit") → showPopup → onWindowShowing → submit → onSubmit.run()（回写 getSelectedColumnNames + initText）`

---

## ShellDamengFieldInfoPopupController
- 职责：达梦表字段详细信息展示弹窗（名称、类型、长度、值、注释、默认值、标签）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| name | `TextField` | 名称，`@FXML` 注入 |
| size | `NumberTextField` | 字段长，`@FXML` 注入 |
| type | `TextField` | 类型，`@FXML` 注入 |
| value | `TextField` | 值，`@FXML` 注入 |
| comment | `TextArea` | 注释，`@FXML` 注入 |
| defaultValue | `TextField` | 默认值，`@FXML` 注入 |
| sizeBox | `FXHBox` | 长度组件容器，`@FXML` 注入 |
| tagsBox | `FXHBox` | 标签组件容器，`@FXML` 注入 |
| valueBox | `FXHBox` | 值组件容器，`@FXML` 注入 |
| defaultValueBox | `FXHBox` | 默认值组件容器，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("column")` 取 `DamengColumn`；`supportSize()` 为真则回填 `size` 并 `sizeBox.display()`；`supportValue()` 为真则 `value.setText` + `valueBox.display()`；`supportDefaultValue()` 为真则 `defaultValue.setText(column.getDefaultValueString())` + `defaultValueBox.display()`；`ShellDamengNodeUtil.generateTags(column)` 生成标签，非空则 `tagsBox.addChild(tags)` + `tagsBox.display()` 并按首个/其余设置 `HBox.setMargin(new Insets(5,0,0,10))`、`Insets(5,0,0,5)`；最后回填 `name/type/comment` |
| `onPopupInitialize(PopupAdapter window)` | 弹窗初始化 | 对 `tagsBox/sizeBox/valueBox/defaultValueBox` 调用 `managedBindVisible()`，实现「可见性联动 managed」 |

- 调用链：`DamengRecordColumn（字段单元格）→ PopupManager.parsePopup → setProp("column") → showPopup → onWindowShowing → ShellDamengNodeUtil.generateTags → 回填各控件`

---

## ShellDamengIndexFieldPopupController
- 职责：达梦索引字段编辑弹窗（增删索引列、行上下移动）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| onSubmit | `Runnable` | 提交回调，`getProp("onSubmit")` 注入 |
| listView | `DamengIndexColumnListView` | 索引列列表组件，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `submit()` | 提交，`@FXML` | `onSubmit.run()`（非空）后 `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `addRow()` | 添加行，`@FXML` | `listView.addColumn(new DamengIndex.IndexColumn())` → `listView.selectLast()` |
| `deleteRow()` | 删除行，`@FXML` | `listView.removeSelectedItem()` |
| `moveUpRow()` | 上移行，`@FXML` | `ListViewUtil.moveUp(listView)` |
| `moveDownRow()` | 下移行，`@FXML` | `ListViewUtil.moveDown(listView)` |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("onSubmit")`、`getProp("dbIndex")`（`DamengIndex`）、`getProp("columnList")`；`listView.init(dbIndex, columnList)` |

- 调用链：`DamengIndexFieldTextFiled.initPopup → PopupManager.parsePopup → setProp("dbIndex","columnList","onSubmit") → showPopup → onWindowShowing → submit → onSubmit.run()`

---

## ShellDamengRecordEnumPopupController
- 职责：达梦数据枚举值多选弹窗（用复选框列出全部值并勾选已选值）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| onSubmit | `Runnable` | 提交回调，`getProp("onSubmit")` 注入 |
| listView | `FXListView<CheckBox>` | 复选框列表组件，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `submit()` | 提交，`@FXML` | `onSubmit.run()`（非空）后 `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `bindListeners()` | 监听绑定 | 仅调 `super.bindListeners()`（空覆写） |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("onSubmit")`、`getProp("values")`（已选）、`getProp("allValues")`（全部）；遍历 `allValues` 创建 `FXCheckBox`，命中 `values` 时 `setSelected(true)`，逐个 `listView.addItem(checkBox)` |
| `onPopupInitialize(PopupAdapter window)` | 弹窗初始化 | 仅调 `super.onPopupInitialize(window)`（空覆写） |

- 调用链：`（源码内未发现 `new`/`parsePopup` 外部调用点，仅在 `shellDamengRecordEnumPopup.fxml` 中以 `fx:controller` 绑定）→ onWindowShowing → submit → onSubmit.run()`
- 备注：该类没有检索到其他存活 `.java` 的调用点，疑似遗留/尚未接线。

---

## ShellDamengRecordFilterPopupController
- 职责：达梦表/视图的数据过滤条件编辑弹窗。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| filterTable | `FXTableView<DamengRecordFilter>` | 过滤条件表单，`@FXML` 注入 |
| treeItem | `TreeItem<?>` | 当前表/视图树节点，`getProp("item")` 注入 |
| columnList | `List<DamengColumn>` | 字段列表缓存，按 `treeItem` 类型惰性获取 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `apply()` | 应用过滤，`@FXML` | `submit(filterTable.getItems())` → `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `bindListeners()` | 监听绑定 | 仅调 `super.bindListeners()`（空覆写） |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("item")`、`getProp("filters")`；`filterTable.setItem(filters)` |
| `onWindowHidden(WindowEvent event)` | 窗口隐藏回调 | 置空 `columnList` |
| `addFilter()` | 添加过滤条件，`@FXML` | 新建 `DamengRecordFilter`；`columnList` 为空时按 `treeItem instanceof ShellDamengTableTreeItem` → `item.columns()`，或 `ShellDamengViewTreeItem` → `item.columns()`；`filter.setColumns(columnList)` → `filterTable.addItem(filter)` |
| `deleteFilter()` | 删除过滤条件，`@FXML` | 取 `filterTable.getSelectedItem()`，非空则从 `filterTable.getItems()` 移除；异常 `MessageBox.exception(ex)` |

- 调用链：`ShellDamengTableRecordTabController / ShellDamengViewRecordTabController → PopupManager.parsePopup → setProp("item","filters") → showPopup → onWindowShowing → addFilter/deleteFilter → apply → submit(items)`

---

## mysql 子包

## ShellMysqlColumnFieldPopupController
- 职责：MySQL 表字段选择弹窗（多选字段列表，支持行上下移动）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| onSubmit | `Runnable` | 提交回调，`getProp("onSubmit")` 注入 |
| listView | `ShellMysqlColumnListView` | 字段列表组件，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `submit()` | 提交，`@FXML` | `onSubmit.run()`（非空）后 `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `moveUpRow()` | 上移行，`@FXML` | `ListViewUtil.moveUp(listView)` |
| `moveDownRow()` | 下移行，`@FXML` | `ListViewUtil.moveDown(listView)` |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("onSubmit")`、`getProp("columns")`、`getProp("selectedColumns")`；`listView.init(columns)` → `listView.select(selectedColumns)` |

- 调用链：`ShellMysqlFieldTextFiled.initPopup → PopupManager.parsePopup → setProp("columns","selectedColumns","onSubmit") → showPopup → onWindowShowing → submit → onSubmit.run()（回写 getSelectedColumnNames + initText）`

---

## ShellMysqlFieldInfoPopupController
- 职责：MySQL 表字段详细信息展示弹窗（名称、类型、长度、值、注释、默认值、标签）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| name | `TextField` | 名称，`@FXML` 注入 |
| size | `NumberTextField` | 字段长，`@FXML` 注入 |
| type | `TextField` | 类型，`@FXML` 注入 |
| value | `TextField` | 值，`@FXML` 注入 |
| comment | `TextArea` | 注释，`@FXML` 注入 |
| defaultValue | `TextField` | 默认值，`@FXML` 注入 |
| sizeBox | `FXHBox` | 长度组件容器，`@FXML` 注入 |
| tagsBox | `FXHBox` | 标签组件容器，`@FXML` 注入 |
| valueBox | `FXHBox` | 值组件容器，`@FXML` 注入 |
| defaultValueBox | `FXHBox` | 默认值组件容器，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("column")` 取 `MysqlColumn`；`supportSize()/supportValue()/supportDefaultValue()` 分别回填 `size/value/defaultValue` 并 `display()` 对应容器；`ShellMysqlNodeUtil.generateTags(column)` 生成标签并 `tagsBox.addChild(tags)` + `tagsBox.display()`，按首个/其余设置 `HBox.setMargin`；最后回填 `name/type/comment` |
| `onPopupInitialize(PopupAdapter window)` | 弹窗初始化 | 对 `tagsBox/sizeBox/valueBox/defaultValueBox` 调用 `managedBindVisible()` |

- 调用链：`ShellMysqlRecordColumn（字段单元格）→ PopupManager.parsePopup → setProp("column") → showPopup → onWindowShowing → ShellMysqlNodeUtil.generateTags → 回填各控件`

---

## ShellMysqlIndexFieldPopupController
- 职责：MySQL 索引字段编辑弹窗（增删索引列、行上下移动）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| onSubmit | `Runnable` | 提交回调，`getProp("onSubmit")` 注入 |
| listView | `ShellMysqlIndexColumnListView` | 索引列列表组件，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `submit()` | 提交，`@FXML` | `onSubmit.run()`（非空）后 `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `addRow()` | 添加行，`@FXML` | `listView.addColumn(new MysqlIndex.IndexColumn())` → `listView.selectLast()` |
| `deleteRow()` | 删除行，`@FXML` | `listView.removeSelectedItem()` |
| `moveUpRow()` | 上移行，`@FXML` | `ListViewUtil.moveUp(listView)` |
| `moveDownRow()` | 下移行，`@FXML` | `ListViewUtil.moveDown(listView)` |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("onSubmit")`、`getProp("dbIndex")`（`MysqlIndex`）、`getProp("columnList")`；`listView.init(dbIndex, columnList)` |

- 调用链：`ShellMysqlIndexFieldTextFiled.initPopup → PopupManager.parsePopup → setProp("dbIndex","columnList","onSubmit") → showPopup → onWindowShowing → submit → onSubmit.run()`

---

## ShellMysqlRecordEnumPopupController
- 职责：MySQL 数据枚举值多选弹窗（用复选框列出全部值并勾选已选值）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| onSubmit | `Runnable` | 提交回调，`getProp("onSubmit")` 注入 |
| listView | `FXListView<CheckBox>` | 复选框列表组件，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `submit()` | 提交，`@FXML` | `onSubmit.run()`（非空）后 `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `bindListeners()` | 监听绑定 | 仅调 `super.bindListeners()`（空覆写） |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("onSubmit")`、`getProp("values")`、`getProp("allValues")`；遍历 `allValues` 创建 `FXCheckBox`，命中 `values` 时 `setSelected(true)`，逐个 `listView.addItem(checkBox)` |
| `onPopupInitialize(PopupAdapter window)` | 弹窗初始化 | 仅调 `super.onPopupInitialize(window)`（空覆写） |

- 调用链：`（源码内未发现 `new`/`parsePopup` 外部调用点，仅在 `shellMysqlRecordEnumPopup.fxml` 中以 `fx:controller` 绑定）→ onWindowShowing → submit → onSubmit.run()`
- 备注：该类没有检索到其他存活 `.java` 的调用点，疑似遗留/尚未接线。

---

## ShellMysqlRecordFilterPopupController
- 职责：MySQL 表/视图的数据过滤条件编辑弹窗。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| filterTable | `FXTableView<MysqlRecordFilter>` | 过滤条件表单，`@FXML` 注入 |
| treeItem | `TreeItem<?>` | 当前表/视图树节点，`getProp("item")` 注入 |
| columnList | `List<MysqlColumn>` | 字段列表缓存，按 `treeItem` 类型惰性获取 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `apply()` | 应用过滤，`@FXML` | `submit(filterTable.getItems())` → `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `bindListeners()` | 监听绑定 | 仅调 `super.bindListeners()`（空覆写） |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("item")`、`getProp("filters")`；`filterTable.setItem(filters)` |
| `onWindowHidden(WindowEvent event)` | 窗口隐藏回调 | 置空 `columnList` |
| `addFilter()` | 添加过滤条件，`@FXML` | 新建 `MysqlRecordFilter`；`columnList` 为空时按 `treeItem instanceof ShellMysqlTableTreeItem` → `item.columns()`，或 `ShellMysqlViewTreeItem` → `item.columns()`；`filter.setColumns(columnList)` → `filterTable.addItem(filter)` |
| `deleteFilter()` | 删除过滤条件，`@FXML` | 取 `filterTable.getSelectedItem()`，非空则从 `filterTable.getItems()` 移除；异常 `MessageBox.exception(ex)` |

- 调用链：`ShellMysqlTableRecordTabController / ShellMysqlViewRecordTabController → PopupManager.parsePopup → setProp("item","filters") → showPopup → onWindowShowing → addFilter/deleteFilter → apply → submit(items)`

---

## db 子包

## ShellDBColumnEnumPopupController
- 职责：通用数据库字段枚举值编辑弹窗（用可清除文本框逐行增删枚举值）。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| onSubmit | `Runnable` | 提交回调，`getProp("onSubmit")` 注入 |
| listView | `FXListView<ClearableTextField>` | 枚举值输入列表组件，`@FXML` 注入 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `submit()` | 提交，`@FXML` | `onSubmit.run()`（非空）后 `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `addRow()` | 添加行，`@FXML` | `listView.addItem(createNode(""))` → `listView.selectLast()` |
| `deleteRow()` | 删除行，`@FXML` | `listView.removeSelectedItem()` |
| `createNode(String text)` | 创建文本输入节点 | new `ClearableTextField(text)`；`setRealHeight(22)`、`setRealWidth(100)`、`setFlexWidth("100% - 20")`、`setBorder(ControlUtil.strokeOfWidthBottom(Color.GRAY, 0.5))`、`ListViewUtil.selectRowOnMouseClicked(textField)`，返回组件 |
| `bindListeners()` | 监听绑定 | 仅调 `super.bindListeners()`（空覆写） |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("onSubmit")`、`getProp("values")`；`CollectionUtil.isNotEmpty(values)` 时逐个 `listView.addItem(createNode(value))` |
| `onPopupInitialize(PopupAdapter window)` | 弹窗初始化 | `listView.setCellFactory(...)`：单元格 `updateItem` 中空则 `setGraphic(null)`，否则 `setGraphic(item)`、`setPrefHeight(25)`、`ListViewUtil.highlightCell(this)` |

- 调用链：`ShellDBEnumTextFiled.initPopup → PopupManager.parsePopup → setProp("values","onSubmit") → showPopup → onWindowShowing（createNode 生成行）→ submit → onSubmit.run()（回读 listView items 文本 → initText）`

---

## ShellDBPageSettingPopupController
- 职责：通用数据库（表记录）每页条数设置弹窗，写入 `ShellSetting.recordPageLimit`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| limit | `NumberTextField` | 每页限制输入框，`@FXML` 注入 |
| setting | `ShellSetting` | 全局设置对象，`final`，取 `ShellSettingStore.SETTING` |
| settingStore | `ShellSettingStore` | 设置存储，`final` 单例 `ShellSettingStore.INSTANCE` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `apply()` | 保存并应用，`@FXML` | `limit.getIntValue()` → `setting.setRecordPageLimit(limit)` → `settingStore.update(setting)` → `submit(limit)` → `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `bindListeners()` | 监听绑定 | 仅调 `super.bindListeners()`（空覆写） |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `limit.setValue(setting.getRecordPageLimit())` |
| `onWindowHidden(WindowEvent event)` | 窗口隐藏回调 | 置空 `limit` 引用 |

- 调用链：`ShellDamengTableRecordTabController / ShellDamengViewRecordTabController / ShellMysqlTableRecordTabController / ShellMysqlViewRecordTabController → PopupManager.parsePopup → showPopup → onWindowShowing → apply → ShellSettingStore.update → submit(limit)`

---

## mongo 子包

## ShellMongoPageSettingPopupController
- 职责：Mongo 记录每页条数设置弹窗，写入 `ShellSetting.mongoRecordPageLimit`。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| limit | `NumberTextField` | 每页限制输入框，`@FXML` 注入 |
| setting | `ShellSetting` | 全局设置对象，`final`，取 `ShellSettingStore.SETTING` |
| settingStore | `ShellSettingStore` | 设置存储，`final` 单例 `ShellSettingStore.INSTANCE` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `apply()` | 保存并应用，`@FXML` | `limit.getIntValue()` → `setting.setMongoRecordPageLimit(limit)` → `settingStore.update(setting)` → `submit(limit)` → `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `bindListeners()` | 监听绑定 | 仅调 `super.bindListeners()`（空覆写） |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `limit.setValue(setting.getMongoRecordPageLimit())` |
| `onWindowHidden(WindowEvent event)` | 窗口隐藏回调 | 置空 `limit` 引用 |

- 调用链：`ShellMongoCollectionRecordTabController / ShellMongoBucketRecordTabController → PopupManager.parsePopup → showPopup → onWindowShowing → apply → ShellSettingStore.update → submit(limit)`

---

## ShellMongoRecordFilterPopupController
- 职责：Mongo 记录的数据过滤条件编辑弹窗。
- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| filterTable | `FXTableView<MongoRecordFilter>` | 过滤条件表单，`@FXML` 注入 |
| columnList | `List<MongoColumn>` | 字段列表，`getProp("columns")` 注入（无 `treeItem` 字段，与 dameng/mysql 版本实现不同） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `apply()` | 应用过滤，`@FXML` | `submit(filterTable.getItems())` → `closeWindow()`；异常 `MessageBox.exception(ex)` |
| `close()` | 关闭窗口，`@FXML` | `closeWindow()` |
| `bindListeners()` | 监听绑定 | 仅调 `super.bindListeners()`（空覆写） |
| `onWindowShowing(WindowEvent event)` | 窗口展示回调 | `getProp("filters")` → `filterTable.setItem(filters)`；`getProp("columns")` → 赋值 `columnList` |
| `addFilter()` | 添加过滤条件，`@FXML` | 新建 `MongoRecordFilter` → `filter.setColumns(columnList)` → `filterTable.addItem(filter)` |
| `deleteFilter()` | 删除过滤条件，`@FXML` | 取 `filterTable.getSelectedItem()`，非空则从 `filterTable.getItems()` 移除；异常 `MessageBox.exception(ex)` |

- 调用链：`ShellMongoCollectionRecordTabController / ShellMongoBucketRecordTabController → PopupManager.parsePopup → setProp("filters","columns") → showPopup → onWindowShowing → addFilter/deleteFilter → apply → submit(items)`

---

## 跳过清单
| 文件 | 备注 |
|---|---|
| popups/dameng/DamengColumnConfigPopupController.java | 整文件注释（死代码），首行 `//package ...`，全部语句被 `//` 注释 |
| popups/dameng/DamengColumnEnumPopupController.java | 整文件注释（死代码），原为字段枚举弹窗，被 `ShellDBColumnEnumPopupController` 等取代 |
| popups/dameng/DamengPageSettingPopupController.java | 整文件注释（死代码），原为达梦页码设置，被 `ShellDBPageSettingPopupController` 取代 |
| popups/dameng/DamengTableRecordFilterPopupController.java | 整文件注释（死代码），原为达梦表过滤，被 `ShellDamengRecordFilterPopupController` 取代 |
| popups/dameng/DamengViewRecordFilterPopupController.java | 整文件注释（死代码），原为达梦视图过滤，被 `ShellDamengRecordFilterPopupController` 取代 |
| popups/mysql/ShellMysqlColumnEnumPopupController.java | 整文件注释（死代码），原为 MySQL 字段枚举弹窗，被 `ShellDBColumnEnumPopupController` 取代 |
