# easyshell 过滤器组件（filter 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/filter/（含 zk/redis/mysql 子包），共 11 个 .java，其中存活 2 个、注释死代码 9 个。

## 包结构总览

```
filter/
├── zk/
│   ├── ShellZKNodeFilterTypeComboBox.java      存活  —— ZK 节点过滤类型选择框
│   ├── ShellZKNodeFilterParam.java             死代码
│   ├── ShellZKNodeFilterTextField.java         死代码
│   └── ShellZKNodeFilterTextFieldSkin.java     死代码
├── redis/
│   ├── ShellRedisKeyFilterTypeComboBox.java    存活  —— Redis 键过滤类型选择框
│   ├── ShellRedisKeyFilterParam.java           死代码
│   ├── ShellRedisKeyFilterTextField.java       死代码
│   └── ShellRedisKeyFilterTextFieldSkin.java   死代码
└── mysql/
    ├── ShellMysqlDataFilterParam.java          死代码
    ├── ShellMysqlDataFilterTextField.java      死代码
    └── ShellMysqlDataFilterTextFieldSkin.java  死代码
```

说明：9 个死代码文件整文件被 `//` 注释，无编译产出；存活的 2 个类均为 `FXComboBox<String>` 子类，通过实现 `I18nSelectAdapter<String>` 提供下拉项数据源，供各 Tab 控制器的过滤类型（`filterType`）选择使用。过滤条件本身（参数、文本域、皮肤）已迁出并移交给其他机制，故相关的 `*FilterParam` / `*FilterTextField` / `*FilterTextFieldSkin` 被整体废弃。

## ShellZKNodeFilterTypeComboBox

- 职责：ZooKeeper 节点过滤类型下拉选择框，提供全部/持久/临时/采集等过滤类型选项。
- 字段：无字段（选项通过 `values` 动态生成）。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `values(Locale locale)` | 生成下拉项（实现 `I18nSelectAdapter`） | `clearItems()` 后依次 `addItem`：`I18nHelper.allNodes()`（全部节点）、`I18nHelper.collectNodes()`（采集节点）、`I18nHelper.persistentNodes()`（持久节点）、`I18nHelper.temporaryNodes()`（临时节点），返回 `getItems()` |
| `initNode()` | 初始化节点事件（覆写） | 添加 `MouseEvent.MOUSE_CLICKED` 事件过滤器：主键单击 `show()` 展开，否则 `hide()` 收起；随后 `super.initNode()` |

- 调用链：`ShellZKNodeTabController`（字段 `filterType`，第 71 行）→ `filterType.selectedIndexChanged((obs, old, new) -> this.doFilter())`（第 244 行）→ `doFilter()` 中 `this.filterType.getSelectedIndex()`（第 357 行）判定过滤类型
- 备注：`values` 依赖国际化文案作为下拉项文本，`doFilter` 依据选中索引（而非文本）判定类型，两者需保持顺序一致。

## ShellRedisKeyFilterTypeComboBox

- 职责：Redis 键过滤类型下拉选择框，提供全部/采集/各数据类型（STRING、LIST、SET、ZSET、HASH、STREAM）等过滤类型选项。
- 字段：无字段（选项通过 `values` 动态生成）。
- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `values(Locale locale)` | 生成下拉项（实现 `I18nSelectAdapter`） | `clearItems()` 后依次 `addItem`：`I18nHelper.allKeys()`（全部键）、`I18nHelper.collectKeys()`（采集键），随后硬编码 `addItem("STRING"/"LIST"/"SET"/"ZSET"/"HASH"/"STREAM")`，返回 `getItems()` |
| `initNode()` | 初始化节点事件（覆写） | 添加 `MouseEvent.MOUSE_CLICKED` 事件过滤器：主键单击 `show()`，否则 `hide()`；随后 `super.initNode()` |

- 调用链：`ShellRedisKeysTabController`（字段 `filterType`，第 120 行）→ `filterType.selectedIndexChanged((obs, old, new) -> this.doFilter())`（第 173 行）→ `doFilter()` 中 `this.filterType.getSelectedIndex()`（第 146 行）判定过滤类型
- 备注：数据类型项为硬编码英文常量（未国际化），与 ZK 同类均以“选中索引”判定类型。

## 跳过清单

以下 9 个 `.java` 文件整文件已被 `//` 注释（死代码），未纳入本次类级审查。

| 文件 | 所属子包 | 状态 | 说明 |
|---|---|---|---|
| ShellZKNodeFilterParam.java | zk | 死代码 | zk 节点过滤参数（matchCase/matchFull 等），整文件注释 |
| ShellZKNodeFilterTextField.java | zk | 死代码 | zk 节点过滤文本域，整文件注释 |
| ShellZKNodeFilterTextFieldSkin.java | zk | 死代码 | zk 节点过滤文本域皮肤，整文件注释 |
| ShellRedisKeyFilterParam.java | redis | 死代码 | redis 键过滤参数，整文件注释 |
| ShellRedisKeyFilterTextField.java | redis | 死代码 | redis 键过滤文本域，整文件注释 |
| ShellRedisKeyFilterTextFieldSkin.java | redis | 死代码 | redis 键过滤文本域皮肤，整文件注释 |
| ShellMysqlDataFilterParam.java | mysql | 死代码 | mysql 数据过滤参数，整文件注释 |
| ShellMysqlDataFilterTextField.java | mysql | 死代码 | mysql 数据过滤文本域，整文件注释 |
| ShellMysqlDataFilterTextFieldSkin.java | mysql | 死代码 | mysql 数据过滤文本域皮肤，整文件注释 |
