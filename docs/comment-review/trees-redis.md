# 代码审查文档 · trees/redis（Redis 树）

> 范围：`src/main/java/cn/oyzh/easyshell/trees/redis/` 共 19 个类。
> 说明：由 `ShellRedisTreeView` 初始化根节点——集群模式直接以 `ShellRedisDatabaseTreeItem` 为根，否则以 `ShellRedisRootTreeItem` 为根；根节点加载 db 节点，db 节点按 key 类型（string/list/set/zset/hash/stream/json）懒加载键节点。业务逻辑下沉到 `ShellRedisClient`、`ShellRedisKeyUtil`、`ShellRedisViewFactory`、`RedisCollectStore`。

---

## ShellRedisTreeItem
- 职责：Redis 树所有节点的抽象基类，统一收窄 `getTreeView()` 返回类型。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisTreeItem(RichTreeView treeView)` | 构造节点 | `super(treeView)` |
  | `ShellRedisTreeView getTreeView()` | 返回 redis 树视图 | `(ShellRedisTreeView) super.getTreeView()` |

- 调用链：继承 `RichTreeItem<V>`；被全部 Redis 树节点继承。

## ShellRedisTreeView
- 职责：Redis 资源树视图，持客户端，按集群/普通模式切换根节点，订阅键刷新/复制/移动事件刷新目标 db。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `client` | `ShellRedisClient` | Redis 客户端 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setClient(ShellRedisClient)` | 设置客户端并按模式建根 | `FXUtil.runWait`：集群模式 → 根为 `new ShellRedisDatabaseTreeItem(null, this)`，否则 `new ShellRedisRootTreeItem(this)` |
  | `getClient()` / `shellConnect()` | 客户端/连接信息 | `client`；`client.shellConnect()` |
  | `initTreeView()` | 初始化单元格工厂 | `RichTreeCell` |
  | `getItemFilter()` | 懒初始化过滤器 | `new ShellRedisTreeItemFilter()` |
  | `dbItems()` | 获取所有 db 子节点 | 遍历 `root().getChildren()` 过滤 `ShellRedisDatabaseTreeItem` |
  | `onKeyFlushed(@EventSubscribe)` | 响应单键刷新事件 | 连接匹配后按 `event.data()` 刷新对应 db |
  | `keyCopied(int)` / `keyMoved(int)` | 复制/移动后刷新目标 db | 遍历 db，命中 `dbIndex` 则 `reloadChild()` |
  | `onKeysCopied(@EventSubscribe)` / `onKeysMoved(@EventSubscribe)` | 批量复制/移动事件 | 连接匹配后刷新目标库（移动含来源库） |
  | `loadItems()` | 加载节点 | `disable()` → `root().loadChild()` → `enable()` |
  | `sortAsc()` / `sortDesc()` | 排序 | 记录选中项 → 根排序 → `select` 恢复 → `refresh` |

- 调用链：`ShellRedisTreeView → ShellRedisRootTreeItem / ShellRedisDatabaseTreeItem`；监听 `ShellRedisKeyFlushedEvent` / `ShellRedisKeysCopiedEvent` / `ShellRedisKeysMovedEvent`

## ShellRedisTreeItemFilter
- 职责：Redis 树节点过滤器，按类型/收藏筛选键节点并做关键字匹配。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `type` | `byte` | 过滤类型：0 全部 / 1 收藏 / 2 string / 3 list / 4 set / 5 zset / 6 hash / 7 stream |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getType()/setType(byte)` | 类型存取 | 简单存取 |
  | `boolean test(RichTreeItem<?> item)` | 判断是否命中 | 根节点放行；对 `ShellRedisKeyTreeItem` 按 `type` 判定收藏/类型是否匹配，再取 `key()` 调 `TextUtil.findText` 比较 `NOT_FOUND` |

- 调用链：`ShellRedisTreeView.getItemFilter`；使用 `TextUtil.findText`

## ShellRedisRootTreeItem
- 职责：Redis 根节点（非集群），加载全部 db 子节点。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisRootTreeItem(ShellRedisTreeView)` | 构造根节点 | `setFilterable(true)`、`setValue(new ShellRedisRootTreeItemValue(this))` |
  | `getMenuItems()` | 右键菜单 | `reloadDatabase`、`importData`、`exportData` |
  | `importData()` / `exportData()` | 导入/导出 | `ShellRedisViewFactory.redisImportData/redisExportData(shellConnect, null)` |
  | `reloadChild()` | 重载 | `clearChild()` + `loadChild()` |
  | `keyChildrenSize()` | 键子节点数量 | `getChildren()` 过滤 `ShellRedisKeyTreeItem` |
  | `loadChild()` | 加载 db | `TaskBuilder`：`onStart=loadDatabase`，`onSuccess=refresh`，`onFinish=expend` |
  | `loadDatabase()` | 生成 db 节点 | `client().databases()` 循环 `new ShellRedisDatabaseTreeItem(dbIndex, ...)`；`setChild` 后台 `BackgroundService` 异步刷新各 db 键数量 |
  | `shellConnect()` / `client()` | 连接信息/客户端 | 取树视图 |
  | `onPrimaryDoubleClick()` | 双击 | 未加载则 `loadChild()` |

- 调用链：`ShellRedisRootTreeItem → ShellRedisClient.databases → ShellRedisDatabaseTreeItem`

## ShellRedisRootTreeItemValue
- 职责：根节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `graphic()` | 图标 | 懒创建 `DatabaseSVGGlyph`（等待中不复建）并 `disableTheme()` |
  | `name()` | 名称 | `I18nHelper.database()` |
  | `extra()` | 附加文本 | `(键数量)` |
  | `extraColor()` | 附加色 | `Color.FORESTGREEN` |

- 调用链：继承 `RichTreeItemValue`

## ShellRedisDatabaseTreeItem
- 职责：db 节点，管理键的扫描/加载/过滤/导入导出/传输/批量操作与键数量。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `setting` | `ShellSetting` | 全局设置（`ShellSettingStore.SETTING`，final），用于 `getKeyLoadLimit()` |
  | `dbIndex` | `Integer` | db 索引（null 视作 0；集群模式） |
  | `value` | `String` | 展示名（集群 `I18nHelper.cluster()`，否则 `db{index}`） |
  | `filterPattern` | `String` | 键扫描模式（SCAN pattern） |
  | `dbSize` | `Long` | 键数量缓存 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisDatabaseTreeItem(Integer, ShellRedisTreeView)` | 构造 | `setSortable(true)`；`dbIndex=dbIndex??0`；`setValue(new ShellRedisDatabaseTreeItemValue(this))` |
  | `dbIndex()/value()/getFilterPattern()/setFilterPattern()/dbSize()/flushDbSize()` | 存取/刷新键数量 | `flushDbSize`：非哨兵模式 `client().dbSize(dbIndex)` |
  | `getMenuItems()` | 右键菜单 | addKey/filterKey、排序、刷新、导入/导出/传输、批量操作、加载全部、卸载 |
  | `batchOperation()` / `transportData()` / `importData()` / `exportData()` | 批量/传输/导入/导出 | `ShellRedisViewFactory.*` |
  | `isClusterMode()/isSentinelMode()` | 模式判断 | `client()` |
  | `client()/shellConnect()` | 客户端/连接信息 | `getTreeView().getClient()`；`client().shellConnect()` |
  | `addKey()` | 新增键 | `ShellRedisViewFactory.addRedisKey(...)` 后 `keyAdded(key)` |
  | `compareTo(Object)` | 比较 | 按 `dbIndex` 升序 |
  | `loadChild()` | 异步加载键 | `TaskBuilder`：`flushDbSize` + `loadChild(limit)` |
  | `loadChild(int limit)` | 加载键子节点 | 以 `filterPattern`（默认 `*`）`ShellRedisKeyUtil.getKeys` 增量获取；达 limit 追加 `ShellRedisMoreTreeItem`，否则移除；`initKeyItem` 按类型建节点；`finally` filter/sort 并恢复选中 |
  | `keyChildren()` / `keyChildrenSize()` / `moreChildren()` | 键子节点/数量/更多节点 | 过滤 `unfilteredChildren()` |
  | `initKeyItem(ShellRedisKey)` | 按类型建键节点 | 分支 `new ShellRedisString/List/Set/ZSet/Hash/Stream/JsonKeyTreeItem` |
  | `onPrimaryDoubleClick()` | 双击 | 已加载调父类，否则 `loadChild()` |
  | `keyAdded(String)` | 键新增事件 | `ShellRedisKeyUtil.getKey` 后 `flushDbSize` + `addChild(initKeyItem(...))` |
  | `reloadChild()` | 重载 | `clearChild()` + `loadChild()` |
  | `filterKey()` | 键过滤 | 弹 `ShellRedisKeyFilterPopupController`，提交后设模式并重载 |
  | `unloadChild()` | 卸载 | `clearChild()` + `setLoaded(false)` |
  | `loadChildAll()` | 加载全部 | `TaskBuilder`：`loadChild(0)`（不限量） |
  | `sortChild(boolean)` | 子节点排序 | `ShellRedisMoreTreeItem` 恒末；其余按 `compareTo`，降序取反 |

- 调用链：`ShellRedisDatabaseTreeItem → ShellRedisKeyUtil.getKeys / ShellRedisClient / ShellRedisViewFactory` → `ShellRedisMoreTreeItem` / `ShellRedis*KeyTreeItem`

## ShellRedisDatabaseTreeItemValue
- 职责：db 节点展示值。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisDatabaseTreeItemValue(ShellRedisDatabaseTreeItem)` | 构造 | `setRichMode(true)` |
  | `item()` / `name()` | 收窄节点/名称 | `item().value()` |
  | `graphic()` | 图标 | 懒创建 `DatabaseSVGGlyph` |
  | `extra()` | 附加文本 | `(dbSize)` + 可选 `[键过滤:pattern]` |
  | `extraColor()` | 附加色 | `Color.FORESTGREEN` |

- 调用链：继承 `RichTreeItemValue`

## ShellRedisKeyTreeItem
- 职责：Redis 键节点抽象基类，封装键通用操作（收藏/重命名/删除/移动/复制/TTL/内存占用/编码/类型判定），并提供数据存取模板方法。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `LINE_MAX` | `int`(static final=1MB) | 单行最大值阈值 |
  | `DATA_MAX` | `int`(static final=100MB) | 数据最大值阈值 |
  | `dbItem` | `ShellRedisDatabaseTreeItem`(final) | 所属 db 节点 |
  | `value` | `ShellRedisKey` | Redis 键对象 |
  | `memoryUsageInfoProperty` | `StringProperty` | 内存占用信息属性（懒创建） |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisKeyTreeItem(ShellRedisKey, ShellRedisDatabaseTreeItem)` | 构造 | `setFilterable(true)`、`setValue(new ShellRedisKeyTreeItemValue(this))` |
  | `value()` | 取键对象 | 返回 `value` |
  | `data(Object)` / `data()` / `clearData()` / `unsavedValue()` / `isDataUnsaved()` | 未保存数据存取 | 经 `keyValue().setUnSavedValue/clearUnSavedValue`；`data()` 未保存取 `unsavedValue()` 否则 `rawValue()` |
  | `getMenuItems()` | 右键菜单 | 重命名/移动/复制/删除、更新 TTL、收藏/取消收藏 |
  | `updateTtl()` / `moveKey()` / `copyKey()` | TTL/移动/复制 | `ShellRedisViewFactory.redisTtlKey/redisMoveKey/redisCopyKey`；移动后 `flushDbSize` + `remove` + `treeView.keyMoved(dbIndex)` |
  | `shellConnect()` / `infoName()` / `dbIndex()` / `key()` / `keyBinary()` / `client()` | 上下文 | 逐级委托；`dbIndex()=dbItem.dbIndex()` |
  | `saveKeyValue()` / `setKeyValue(Object)` / `refreshKeyValue()` | 键值模板方法（默认空实现） | 由子类覆写 |
  | `isCollect()/collect()/unCollect()/iid()` | 收藏判定/收藏/取消 | `RedisCollectStore.INSTANCE`；`iid()` 取连接 id |
  | `delete()` | 删除键 | 确认后 `client().del` + `unCollect` + `parent().flushDbSize` + `remove` + `clearSelection` |
  | `parent()` | 收窄父节点 | `(ShellRedisDatabaseTreeItem) super.parent()` |
  | `rename()` | 重命名键 | 校验空/同名/已存在后 `client().rename`，成功更新 `value` 并 `refresh` |
  | `ttl()` / `isExpire()` | TTL/是否过期 | `client().ttl`；`ttl==-2` 为过期 |
  | `rawValue()` / `rawData()`（abstract） | 原始数据 | 子类实现 |
  | `type()` / `loadTime()` / `typeName()` | 类型/加载耗时/类型名 | 取 `value` |
  | `deleteByExpired()` | 过期删除 | `unCollect` + `remove` |
  | `memoryUsage()` / `memoryUsageInfo()` / `flushMemoryUsage()` / `memoryUsageInfoProperty()` | 内存占用 | `client().memoryUsage`；格式化 B/KB/MB/GB |
  | `isRawEncoding(boolean)` | 是否 raw 编码 | 懒 `client().objectEncoding` 后 `value.isRawEncoding()` |
  | `keyValue()` | 键值对象 | `value.getValue()` |
  | `keyEquals(ShellRedisKey)` / `keyCopy(ShellRedisKey)` | 相等/复制 | 委托 `value.compareTo/copy` |
  | `isJsonKey()/isStringKey()/isListKey()/isStreamKey()/isHashKey()/isSetKey()/isZSetKey()` | 类型判定 | 委托 `value` |

- 调用链：`ShellRedisKeyTreeItem → ShellRedisClient`（del/rename/ttl/objectEncoding/memoryUsage）、`RedisCollectStore.INSTANCE`、`ShellRedisViewFactory`

## ShellRedisKeyTreeItemValue
- 职责：键节点展示值，按类型给图标与颜色，未保存数据显示橙色图标。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisKeyTreeItemValue(ShellRedisKeyTreeItem)` | 构造 | `setRichMode(true)` |
  | `item()` / `name()` | 收窄节点/名称 | `item().key()` |
  | `graphic()` | 图标 | 按类型 `String/List/Set/ZSet/Json/Hash/StreamSVGGlyph`，兜底 `KeySVGGlyph`，`disableTheme()` |
  | `extra()` / `extraColor()` | 附加文本/色 | `[类型名]`；按类型返回不同十六进制色 |
  | `graphicColor()` | 图标色 | 数据未保存 → `Color.ORANGERED` |

- 调用链：继承 `RichTreeItemValue`

## ShellRedisRowKeyTreeItem
- 职责：行类型键节点（list/set/zset/hash/stream）抽象基类，维护 `currentRow` 并提供行操作模板。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `currentRow` | `R extends ShellRedisKeyRow`（protected） | 当前操作行 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `currentRow()` / `currentRow(R)` | 行存取 | 链式 setter |
  | `ShellRedisRowKeyTreeItem(ShellRedisKey, ShellRedisDatabaseTreeItem)` | 构造 | 透传父类 |
  | `deleteRow()` / `reloadRow()` / `checkRowExists()` | 行操作（默认 false） | 子类覆写 |
  | `rows()` | 行列表 | 先 `refreshKeyValue()`，再按 set/zset/list/hash/stream 取 `value.asXxxValue().getValue()` |
  | `isSelectRow()` | 是否选中行 | `currentRow != null` |
  | `rawData()` | 原始数据 | `currentRow.getValue()` |
  | `isDataTooBig()` | 数据是否过大（默认 false） | 子类覆写 |

- 调用链：`ShellRedisRowKeyTreeItem → ShellRedisKeyValue`；被 5 种行类型键节点继承

## ShellRedisStringKeyTreeItem
- 职责：string 类型键节点，支持原始/字符串编码读写，并兼容 HyperLogLog 计数。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无，继承） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisStringKeyTreeItem(ShellRedisKey, ShellRedisDatabaseTreeItem)` | 构造 | 透传 |
  | `saveKeyValue()` | 保存 | `setKeyValue` + `keyValue().setValue` + `flushCount` + `clearData` |
  | `setKeyValue(Object)` | 写入 | String → `client().set`；byte[] → `set(keyBinary, bytes)` |
  | `refreshKeyValue()` | 刷新 | raw 编码 `setValueOfBytes(get(byte))`，否则 `valueOfString(get)`；`flushCount` |
  | `rawValue()` / `rawData()` | 原始值 | 无值时刷新后返回 `keyValue().getValue()` |
  | `isDataTooBig()` | 数据是否过大 | String/byte[] 与 `DATA_MAX`、`LINE_MAX` 比较 |
  | `isHyLog()` / `count()` / `flushCount()` | HyperLogLog 判定/计数 | `PFCOUNT`；"WRONGTYPE" 异常则标记非 HLL |
  | `keyValue()` | 键值 | `(ShellRedisStringValue) super.keyValue()` |

- 调用链：`ShellRedisStringKeyTreeItem → ShellRedisClient`（set/get/pfcount）→ `ShellRedisStringValue`

## ShellRedisListKeyTreeItem
- 职责：list 类型键节点，行级 lset/lrem/lindex/lrange。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无，继承 `currentRow: RedisListRow`） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisListKeyTreeItem(...)` | 构造 | 透传 |
  | `data()/data(Object)/unsavedValue()` | 行数据存取 | 收窄为 `RedisListRow`，`data(Object)` 克隆后写入 |
  | `saveKeyValue()` | 保存行 | `setKeyValue(row)` + 更新 `currentRow.value` + `clearData` |
  | `setKeyValue(Object)` | 写入 | `client().lset(dbIndex, key, index-1, value)` |
  | `deleteRow()` | 删除行 | `client().lrem(key, value)` > 0 时移除行 |
  | `reloadRow()` | 重载行 | `client().lindex(index-1)` |
  | `refreshKeyValue()` | 刷新 | `client().lrange` → `valueOfList` + `clearData` |
  | `rawValue()` | 原始行 | `currentRow` |
  | `isDataTooBig()` | 是否过大 | 与 `DATA_MAX`/`LINE_MAX` 比较 |

- 调用链：`ShellRedisListKeyTreeItem → ShellRedisClient`（lset/lrem/lindex/lrange）→ `ShellRedisListValue.RedisListRow`

## ShellRedisSetKeyTreeItem
- 职责：set 类型键节点，行级 sadd/srem/smembers/sismember。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无，继承 `currentRow: RedisSetRow`） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisSetKeyTreeItem(...)` | 构造 | 透传 |
  | `data()/data(Object)/unsavedValue()` | 行数据存取 | 收窄 `RedisSetRow` |
  | `saveKeyValue()` / `setKeyValue(Object)` | 保存/写入 | 值变化先 `srem` 旧值再 `sadd` 新值 |
  | `deleteRow()` | 删除 | `client().srem` > 0 移除行 |
  | `refreshKeyValue()` | 刷新 | `client().smembers` → `valueOfSet` |
  | `rawValue()` | 原始行 | `currentRow` |
  | `checkRowExists()` | 成员是否存在 | 未保存且与当前行不同 → `client().sismember` |
  | `isDataTooBig()` | 是否过大 | 与阈值比较 |

- 调用链：`ShellRedisSetKeyTreeItem → ShellRedisClient`（sadd/srem/smembers/sismember）→ `ShellRedisSetValue.RedisSetRow`

## ShellRedisZSetKeyTreeItem
- 职责：zset 类型键节点，支持"有序集合/地理坐标"两种视图，行含 score 或经纬度。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `showType` | `byte` | 显示类型：0 有序集合 / 1 地理坐标 |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `currentRow()/data()/data(Object)/unsavedValue()` | 行数据存取 | 收窄 `RedisZSetRow` |
  | `score()/score(Double)` | 分数存取 | 经 `data()` |
  | `latitude()/longitude()` 及 setter | 经纬度存取 | 经 `data()` |
  | `currentRow(RedisZSetRow)` | 设置当前行 | `currentRow=row` + `clearData()` |
  | `ShellRedisZSetKeyTreeItem(...)` | 构造 | 透传 |
  | `reverseView()` / `isCoordinateView()` | 切换/判断视图 | 反转 `showType` + `ShellRedisEventUtil.redisZSetReverseView(this)` |
  | `isSupportCoordinate()` / `getServerVersion()` | 坐标支持/服务端版本 | `ShellRedisVersionUtil.isCommandSupported(version, "geopos")` |
  | `saveKeyValue()` / `setKeyValue(Object)` | 保存/写入 | 值变先 `zrem` 旧值；坐标视图 `geoadd`，否则 `zadd` |
  | `deleteRow()` | 删除成员 | `client().zrem` |
  | `refreshKeyValue()` | 刷新 | 坐标视图 `zrange + geopos → valueOfCoordinates`，否则 `zrange + zmscore_ext → valueOfZSet` |
  | `rawValue()` | 原始行 | `currentRow` |
  | `checkRowExists()` | 成员是否存在 | 未保存且值变 → `zrank` 非空 |
  | `isDataTooBig()` | 是否过大 | 与阈值比较 |

- 调用链：`ShellRedisZSetKeyTreeItem → ShellRedisClient`（zadd/zrem/zrange/zmscore_ext/geoadd/geopos/zrank）、`ShellRedisVersionUtil`、`ShellRedisEventUtil.redisZSetReverseView` → `ShellRedisZSetValue.RedisZSetRow`

## ShellRedisHashKeyTreeItem
- 职责：hash 类型键节点，行级 hset/hdel/hget/hgetAll/hexists。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无，继承 `currentRow: RedisHashRow`） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisHashKeyTreeItem(...)` | 构造 | 透传 |
  | `data()/data(Object)/unsavedValue()` | 行数据存取 | 收窄 `RedisHashRow` |
  | `field()/field(String)` | 字段存取 | 经 `data()` |
  | `checkRowExists()` | 字段是否存在 | 字段变化 → `client().hexists` |
  | `saveKeyValue()` / `setKeyValue(Object)` | 保存/写入 | `hset` 新字段，字段名变化则 `hdel` 旧字段 |
  | `deleteRow()` | 删除字段 | `client().hdel` |
  | `refreshKeyValue()` | 刷新 | `client().hgetAll` → `valueOfHash` |
  | `rawValue()` | 原始行 | `currentRow` |
  | `reloadRow()` | 重载字段值 | `client().hget` |
  | `isDataTooBig()` | 是否过大 | 值与字段均与阈值比较 |

- 调用链：`ShellRedisHashKeyTreeItem → ShellRedisClient`（hset/hdel/hget/hgetAll/hexists）→ `ShellRedisHashValue.RedisHashRow`

## ShellRedisStreamKeyTreeItem
- 职责：stream 类型键节点，行级 xdel/xrange。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无，继承 `currentRow: RedisStreamRow`） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisStreamKeyTreeItem(...)` | 构造 | 透传 |
  | `deleteRow()` | 删除条目 | `client().xdel(streamId)` > 0 移除行 |
  | `refreshKeyValue()` | 刷新 | `client().xrange` → `valueOfStream` + `clearData` |
  | `rawValue()` | 原始行 | `currentRow` |

- 调用链：`ShellRedisStreamKeyTreeItem → ShellRedisClient`（xdel/xrange）→ `ShellRedisStreamValue.RedisStreamRow`

## ShellRedisJsonKeyTreeItem
- 职责：json 类型键节点，jsonSet/jsonGet。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无，继承） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisJsonKeyTreeItem(...)` | 构造 | 透传 |
  | `saveKeyValue()` | 保存 | `setKeyValue` + 更新 `keyValue().setValue` + `clearData` |
  | `setKeyValue(Object)` | 写入 | String/byte[] → `client().jsonSet` |
  | `refreshKeyValue()` | 刷新 | `client().jsonGet` → `valueOfJson` |
  | `rawValue()` / `rawData()` | 原始值 | 无值刷新后返回 `keyValue().getValue()` |
  | `isDataTooBig()` | 是否过大 | String/byte[] 与阈值比较 |
  | `keyValue()` | 键值 | `(ShellRedisJsonValue) super.keyValue()` |

- 调用链：`ShellRedisJsonKeyTreeItem → ShellRedisClient`（jsonSet/jsonGet）→ `ShellRedisJsonValue`

## ShellRedisMoreTreeItem
- 职责："加载更多"占位节点，双击触发父 db 继续加载。
- 字段：

  | 字段 | 类型 | 含义 |
  |---|---|---|
  | （无） | | |

- 方法：

  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ShellRedisMoreTreeItem(ShellRedisTreeView)` | 构造 | `setSortable(false)`、`setFilterable(false)`、`setValue(...)` |
  | `parent()` | 收窄父节点 | `(ShellRedisDatabaseTreeItem) getParent()` |
  | `onPrimaryDoubleClick()` | 双击加载更多 | `parent().loadChild()` |
  | `compareTo(Object)` | 比较 | 同类返回 0，否则恒排末位（1） |

- 调用链：`ShellRedisMoreTreeItem → ShellRedisDatabaseTreeItem.loadChild`

## ShellRedisMoreTreeItemValue
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
