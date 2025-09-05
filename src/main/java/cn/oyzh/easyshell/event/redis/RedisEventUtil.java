// package cn.oyzh.easyshell.event.redis;
//
// import cn.oyzh.easyshell.event.redis.key.ShellRedisZSetReverseViewEvent;
// import cn.oyzh.easyshell.trees.redis.RedisZSetKeyTreeItem;
// import cn.oyzh.event.EventUtil;
// import cn.oyzh.fx.plus.changelog.ChangelogEvent;
//
// /**
//  * redis事件工具
//  *
//  * @author oyzh
//  * @since 2023/11/20
//  */
//
// public class RedisEventUtil {
//
//     // /**
//     //  * 连接关闭事件
//     //  *
//     //  * @param client redis客户端
//     //  */
//     // public static void clientClosed(ShellRedisClient client) {
//     //     RedisClientClosedEvent event = new RedisClientClosedEvent();
//     //     event.data(client);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 连接关闭事件
//     //  *
//     //  * @param client redis客户端
//     //  */
//     // public static void connectionClosed(ShellRedisClient client) {
//     //     RedisConnectionClosedEvent event = new RedisConnectionClosedEvent();
//     //     event.data(client);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 连接成功事件
//     //  *
//     //  * @param client redis客户端
//     //  */
//     // public static void connectionConnected(ShellRedisClient client) {
//     //     RedisConnectionConnectedEvent event = new RedisConnectionConnectedEvent();
//     //     event.data(client);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 终端关闭事件
//     //  *
//     //  * @param redisConnect redis信息
//     //  */
//     // public static void terminalClose(ShellConnect redisConnect, Integer dbIndex) {
//     //     RedisTerminalCloseEvent event = new RedisTerminalCloseEvent();
//     //     event.data(redisConnect);
//     //     event.setDbIndex(dbIndex);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * list行添加事件
//     //  *
//     //  * @param item   redis树节点
//     //  * @param key    键名称
//     //  * @param member 成员
//     //  */
//     // public static void listRowAdded(RedisListKeyTreeItem item, String key, String member) {
//     //     ShellRedisListRowAddedEvent event = new ShellRedisListRowAddedEvent();
//     //     event.data(item);
//     //     event.setKey(key);
//     //     event.setMember(member);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * set成员添加事件
//     //  *
//     //  * @param item   redis树节点
//     //  * @param key    键名称
//     //  * @param member 成员
//     //  */
//     // public static void setMemberAdded(RedisSetKeyTreeItem item, String key, String member) {
//     //     ShellRedisSetMemberAddedEvent event = new ShellRedisSetMemberAddedEvent();
//     //     event.data(item);
//     //     event.setKey(key);
//     //     event.setMember(member);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * zset成员添加事件
//     //  *
//     //  * @param item   redis树节点
//     //  * @param key    键名称
//     //  * @param member 成员
//     //  * @param score  成员
//     //  */
//     // public static void zSetMemberAdded(RedisZSetKeyTreeItem item, String key, String member, Double score) {
//     //     ShellRedisZSetMemberAddedEvent event = new ShellRedisZSetMemberAddedEvent();
//     //     event.data(item);
//     //     event.setKey(key);
//     //     event.setScore(score);
//     //     event.setMember(member);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * zset地理坐标添加事件
//     //  *
//     //  * @param item      redis树节点
//     //  * @param key       键名称
//     //  * @param member    成员
//     //  * @param longitude 经度
//     //  * @param latitude  纬度
//     //  */
//     // public static void zSetCoordinateAdded(RedisZSetKeyTreeItem item, String key, String member, double longitude, double latitude) {
//     //     ShellRedisZSetCoordinateAddedEvent event = new ShellRedisZSetCoordinateAddedEvent();
//     //     event.data(item);
//     //     event.setKey(key);
//     //     event.setMember(member);
//     //     event.setLatitude(latitude);
//     //     event.setLongitude(longitude);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * stream消息添加事件
//     //  *
//     //  * @param item    redis树节点
//     //  * @param key     键名称
//     //  * @param message 内容
//     //  */
//     // public static void streamMessageAdded(RedisStreamKeyTreeItem item, String key, String message) {
//     //     ShellRedisStreamMessageAddedEvent event = new ShellRedisStreamMessageAddedEvent();
//     //     event.data(item);
//     //     event.setKey(key);
//     //     event.setMessage(message);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * hash字段添加事件
//     //  *
//     //  * @param item  redis树节点
//     //  * @param key   键名称
//     //  * @param field 字段名称
//     //  * @param value 字段值
//     //  */
//     // public static void hashFieldAdded(RedisHashKeyTreeItem item, String key, String field, String value) {
//     //     ShellRedisHashFieldAddedEvent event = new ShellRedisHashFieldAddedEvent();
//     //     event.data(item);
//     //     event.setKey(key);
//     //     event.setField(field);
//     //     event.setValue(value);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * hylog元素添加事件
//     //  *
//     //  * @param item     redis树节点
//     //  * @param key      键名称
//     //  * @param elements 统计元素
//     //  */
//     // public static void hyLogElementsAdded(RedisStringKeyTreeItem item, String key, String[] elements) {
//     //     ShellRedisHyLogElementsAddedEvent event = new ShellRedisHyLogElementsAddedEvent();
//     //     event.data(item);
//     //     event.setKey(key);
//     //     event.setElements(elements);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 树节点过滤事件
//     //  */
//     // public static void treeChildFilter() {
//     //     EventUtil.post(new TreeChildFilterEvent());
//     // }
//
// //    /**
// //     * 树节点变化事件
// //     */
// //    public static void treeChildChanged() {
// //        EventUtil.post(new TreeChildChangedEvent());
// //    }
//
//     // /**
//     //  * 键添加事件
//     //  *
//     //  * @param connect redis连接
//     //  * @param type    键类型
//     //  * @param key     键名称
//     //  */
//     // public static void keyAdded(ShellConnect connect, String type, String key, int dbIndex) {
//     //     ShellRedisKeyAddedEvent event = new ShellRedisKeyAddedEvent();
//     //     event.data(connect);
//     //     event.setKey(key);
//     //     event.setType(type);
//     //     event.setDbIndex(dbIndex);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 键删除事件
//     //  *
//     //  * @param connect redis连接
//     //  * @param key     键名称
//     //  * @param dbIndex 库
//     //  */
//     // public static void keyDeleted(ShellConnect connect, String key, int dbIndex) {
//     //     ShellRedisKeyDeletedEvent event = new ShellRedisKeyDeletedEvent();
//     //     event.data(connect);
//     //     event.setKey(key);
//     //     event.setDbIndex(dbIndex);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 键刷新事件
//     //  *
//     //  * @param item redis树节点
//     //  */
//     // public static void keyFlushed(Integer item) {
//     //     ShellRedisKeyFlushedEvent event = new ShellRedisKeyFlushedEvent();
//     //     event.data(item);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 键过滤事件
//     //  *
//     //  * @param item redis树节点
//     //  */
//     // public static void keyFiltered(Integer item) {
//     //     RedisKeyFilteredEvent event = new RedisKeyFilteredEvent();
//     //     event.data(item);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 连接已新增事件
//     //  *
//     //  * @param redisConnect redis信息
//     //  */
//     // public static void connectAdded(ShellConnect redisConnect) {
//     //     RedisConnectAddedEvent event = new RedisConnectAddedEvent();
//     //     event.data(redisConnect);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 连接已修改事件
//     //  *
//     //  * @param redisConnect Redis信息
//     //  */
//     // public static void connectUpdated(ShellConnect redisConnect) {
//     //     RedisConnectUpdatedEvent event = new RedisConnectUpdatedEvent();
//     //     event.data(redisConnect);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 终端打开事件
//     //  */
//     // public static void terminalOpen() {
//     //     terminalOpen(null, null);
//     // }
//
//     // /**
//     //  * 终端打开事件
//     //  *
//     //  * @param client  redis客户端
//     //  * @param dbIndex db索引
//     //  */
//     // public static void terminalOpen(ShellRedisClient client, Integer dbIndex) {
//     //     RedisTerminalOpenEvent event = new RedisTerminalOpenEvent();
//     //     event.data(client);
//     //     event.setDbIndex(dbIndex);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 过滤主页事件
//     //  */
//     // public static void filterMain() {
//     //     RedisFilterMainEvent event = new RedisFilterMainEvent();
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 搜索开始事件
//     //  */
//     // public static void searchStart(RedisSearchParam searchParam) {
//     //     RedisSearchStartEvent event = new RedisSearchStartEvent();
//     //     event.data(searchParam);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 搜索结束事件
//     //  */
//     // public static void searchFinish(RedisSearchParam searchParam) {
//     //     RedisSearchFinishEvent event = new RedisSearchFinishEvent();
//     //     event.data(searchParam);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 搜索触发事件
//     //  */
//     // public static void searchFire() {
//     //     EventUtil.post(new RedisSearchFireEvent());
//     // }
//
//     // /**
//     //  * 连接已删除事件
//     //  *
//     //  * @param redisConnect Redis信息
//     //  */
//     // public static void connectDeleted(ShellConnect redisConnect) {
//     //     RedisConnectDeletedEvent event = new RedisConnectDeletedEvent();
//     //     event.data(redisConnect);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 键ttl更新事件
//     //  *
//     //  * @param connect redis树节点
//     //  * @param ttl     ttl值
//     //  */
//     // public static void keyTTLUpdated(ShellConnect connect, Long ttl, String key, int dbIndex) {
//     //     ShellRedisKeyTTLUpdatedEvent event = new ShellRedisKeyTTLUpdatedEvent();
//     //     event.data(connect);
//     //     event.setTtl(ttl);
//     //     event.setKey(key);
//     //     event.setDbIndex(dbIndex);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 键更名事件
//     //  *
//     //  * @param item   redis树节点
//     //  * @param oldKey 旧名称
//     //  */
//     // public static void keyRenamed(RedisKeyTreeItem item, String oldKey) {
//     //     ShellRedisKeyRenamedEvent event = new ShellRedisKeyRenamedEvent();
//     //     event.data(item);
//     //     event.setOldKey(oldKey);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 键复制事件
//     //  *
//     //  * @param connect     redis树节点
//     //  * @param targetDB 目标库
//     //  */
//     // public static void keyCopied(ShellConnect connect, List<String> key, int dbIndex, int targetDB) {
//     //     ShellRedisKeyCopiedEvent event = new ShellRedisKeyCopiedEvent();
//     //     event.data(key);
//     //     event.setDbIndex(dbIndex);
//     //     event.setConnect(connect);
//     //     event.setTargetDB(targetDB);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 键移动事件
//     //  *
//     //  * @param item     redis树节点
//     //  * @param targetDB 目标库
//     //  */
//     // public static void keyMoved(RedisKeyTreeItem item, int targetDB) {
//     //     ShellRedisKeyMovedEvent event = new ShellRedisKeyMovedEvent();
//     //     event.data(item);
//     //     event.setTargetDB(targetDB);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 多个键移动事件
//     //  *
//     //  * @param item     redis树节点
//     //  * @param targetDB 目标库
//     //  */
//     // public static void keysMoved(Integer item, int targetDB) {
//     //     ShellRedisKeysMovedEvent event = new ShellRedisKeysMovedEvent();
//     //     event.data(item);
//     //     event.setTargetDB(targetDB);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 服务信息事件
//     //  *
//     //  * @param client redis客户端
//     //  */
//     // public static void server(ShellRedisClient client) {
//     //     RedisServerEvent event = new RedisServerEvent();
//     //     event.data(client);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 订阅打开事件
//     //  *
//     //  * @param item redis节点
//     //  */
//     // public static void pubsubOpen(RedisPubsubItem item) {
//     //     ShellRedisPubsubOpenEvent event = new ShellRedisPubsubOpenEvent();
//     //     event.data(item);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 添加分组
//     //  */
//     // public static void addGroup() {
//     //     EventUtil.post(new RedisAddGroupEvent());
//     // }
//
// //    /**
// //     * 添加连接
// //     */
// //    public static void addConnect() {
// //        EventUtil.post(new RedisAddConnectEvent());
// //    }
//
//     // /**
//     //  * 展开左侧
//     //  */
//     // public static void leftExtend() {
//     //     EventUtil.post(new RedisLeftExtendEvent());
//     // }
//     //
//     // /**
//     //  * 收缩左侧
//     //  */
//     // public static void leftCollapse() {
//     //     EventUtil.post(new RedisLeftCollapseEvent());
//     // }
//
//     // /**
//     //  * 过滤添加事件
//     //  */
//     // public static void filterAdded() {
//     //     RedisFilterAddedEvent event = new RedisFilterAddedEvent();
//     //     EventUtil.post(event);
//     // }
//
//     /**
//      * 更新日志事件
//      */
//     public static void changelog() {
//         EventUtil.post(new ChangelogEvent());
//     }
//
//     // /**
//     //  * 树节点选中事件
//     //  */
//     // public static void treeChildSelected(RedisKeyTreeItem item) {
//     //     TreeChildSelectedEvent event = new TreeChildSelectedEvent();
//     //     event.data(item);
//     //     EventUtil.post(event);
//     // }
//
//     /**
//      * zset反转视图事件
//      *
//      * @param item 节点
//      */
//     public static void zSetReverseView(RedisZSetKeyTreeItem item) {
//         ShellRedisZSetReverseViewEvent event = new ShellRedisZSetReverseViewEvent();
//         event.data(item);
//         EventUtil.post(event);
//     }
//
//     // /**
//     //  * 连接丢失事件
//     //  *
//     //  * @param client redis客户端
//     //  */
//     // public static void connectionOpened(RedisDatabaseTreeItem client) {
//     //     RedisConnectOpenedEvent event = new RedisConnectOpenedEvent();
//     //     event.data(client);
//     //     EventUtil.postSync(event);
//     // }
//
//     // public static void treeItemChanged(TreeItem<?> treeItem) {
//     //     RedisTreeItemChangedEvent event = new RedisTreeItemChangedEvent();
//     //     event.data(treeItem);
//     //     EventUtil.postSync(event);
//     // }
//     //
//     // /**
//     //  * 布局1
//     //  */
//     // public static void layout1() {
//     //     EventUtil.post(new Layout1Event());
//     // }
//     //
//     // /**
//     //  * 布局2
//     //  */
//     // public static void layout2() {
//     //     EventUtil.post(new Layout2Event());
//     // }
//
//     // /**
//     //  * 分组已添加
//     //  */
//     // public static void groupAdded(String group) {
//     //     RedisGroupAddedEvent event = new RedisGroupAddedEvent();
//     //     event.data(group);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 分组已删除
//     //  */
//     // public static void groupDeleted(String group) {
//     //     RedisGroupDeletedEvent event = new RedisGroupDeletedEvent();
//     //     event.data(group);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 分组已更名
//     //  */
//     // public static void groupRenamed(String group, String oldName) {
//     //     RedisGroupRenamedEvent event = new RedisGroupRenamedEvent();
//     //     event.data(group);
//     //     event.setOldName(oldName);
//     //     EventUtil.post(event);
//     // }
//
//     // /**
//     //  * 客户端操作
//     //  */
//     // public static void clientAction(String connectName, CommandArguments arguments) {
//     //     ShellRedisClientActionEvent event = new ShellRedisClientActionEvent();
//     //     event.data(connectName);
//     //     event.setArguments(arguments);
//     //     EventUtil.postAsync(event);
//     // }
//
//     // /**
//     //  * 添加查询事件
//     //  *
//     //  * @param client Redis查询
//     //  */
//     // public static void addQuery(ShellRedisClient client) {
//     //     RedisAddQueryEvent event = new RedisAddQueryEvent();
//     //     event.data(client);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 查询已添加事件
//     //  *
//     //  * @param query Redis查询
//     //  */
//     // public static void queryAdded(ShellQuery query) {
//     //     RedisQueryAddedEvent event = new RedisQueryAddedEvent();
//     //     event.data(query);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 查询打开事件
//     //  *
//     //  * @param client Redis客户端
//     //  * @param query  Redis查询
//     //  */
//     // public static void openQuery(ShellRedisClient client, ShellQuery query) {
//     //     RedisOpenQueryEvent event = new RedisOpenQueryEvent();
//     //     event.data(query);
//     //     event.setClient(client);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 查询更名事件
//     //  *
//     //  * @param query Redis查询
//     //  */
//     // public static void queryRenamed(ShellQuery query) {
//     //     RedisQueryRenamedEvent event = new RedisQueryRenamedEvent();
//     //     event.data(query);
//     //     EventUtil.post(event);
//     // }
//     //
//     // /**
//     //  * 查询删除事件
//     //  *
//     //  * @param query Redis查询
//     //  */
//     // public static void queryDeleted(ShellQuery query) {
//     //     RedisQueryDeletedEvent event = new RedisQueryDeletedEvent();
//     //     event.data(query);
//     //     EventUtil.post(event);
//     // }
//
// //    /**
// //     * 显示导出连接页面
// //     */
// //    public static void showExportConnect() {
// //        EventUtil.post(new RedisShowExportConnectEvent());
// //    }
//
// //    /**
// //     * 显示导入连接页面
// //     *
// //     * @param file 文件
// //     */
// //    public static void showImportConnect(File file) {
// //        RedisShowImportConnectEvent event = new RedisShowImportConnectEvent();
// //        event.data(file);
// //        EventUtil.post(event);
// //    }
//
// //    /**
// //     * 显示设置页面
// //     */
// //    public static void showSetting() {
// //        EventUtil.post(new RedisShowSettingEvent());
// //    }
//
// //    /**
// //     * 显示传输数据页面
// //     */
// //    public static void showTransportData() {
// //        showTransportData(null, null);
// //    }
// //
// //    /**
// //     * 显示传输数据页面
// //     *
// //     * @param connect zk连接
// //     */
// //    public static void showTransportData(ShellConnect connect, Integer dbIndex) {
// //        RedisShowTransportDataEvent event = new RedisShowTransportDataEvent();
// //        event.data(connect);
// //        event.setDbIndex(dbIndex);
// //        EventUtil.post(event);
// //    }
//
// //    /**
// //     * 显示导出数据页面
// //     *
// //     * @param connect zk连接
// //     */
// //    public static void showExportData(ShellConnect connect, Integer dbIndex) {
// //        RedisShowExportDataEvent event = new RedisShowExportDataEvent();
// //        event.data(connect);
// //        event.setDbIndex(dbIndex);
// //        EventUtil.post(event);
// //    }
//
// //    /**
// //     * 显示导入数据页面
// //     *
// //     * @param connect zk连接
// //     */
// //    public static void showImportData(ShellConnect connect) {
// //        RedisShowImportDataEvent event = new RedisShowImportDataEvent();
// //        event.data(connect);
// //        EventUtil.post(event);
// //    }
//
// //    /**
// //     * 显示添加连接页面
// //     */
// //    public static void showAddConnect() {
// //        showAddConnect(null);
// //    }
//
// //    /**
// //     * 显示修改连接页面
// //     *
// //     * @param connect zk连接
// //     */
// //    public static void showUpdateConnect(ShellConnect connect) {
// //        RedisShowUpdateConnectEvent event = new RedisShowUpdateConnectEvent();
// //        event.data(connect);
// //        EventUtil.post(event);
// //    }
//
// //    /**
// //     * 显示添加连接页面
// //     *
// //     * @param group 分组
// //     */
// //    public static void showAddConnect(RedisGroup group) {
// //        RedisShowAddConnectEvent event = new RedisShowAddConnectEvent();
// //        event.data(group);
// //        EventUtil.post(event);
// //    }
//
// //    /**
// //     * 显示工具页面
// //     */
// //    public static void showTool() {
// //        EventUtil.post(new RedisShowToolEvent());
// //    }
//
// //    /**
// //     * 显示关于页面
// //     */
// //    public static void showAbout() {
// //        EventUtil.post(new RedisShowAboutEvent());
// //    }
//
// //    /**
// //     * 显示迁移数据页面
// //     */
// //    public static void showMigrationData() {
// //        EventUtil.post(new RedisShowMigrationDataEvent());
// //    }
//
// //    /**
// //     * 显示迁移提示页面
// //     */
// //    public static void showMigrationTips() {
// //        EventUtil.post(new RedisShowMigrationTipsEvent());
// //    }
//
// //    /**
// //     * 显示添加键页面
// //     *
// //     * @param dbItem db库
// //     */
// //    public static void showAddKey(RedisDatabaseTreeItem dbItem) {
// //        RedisShowAddKeyEvent event = new RedisShowAddKeyEvent();
// //        event.data(dbItem);
// //        EventUtil.post(event);
// //    }
// //
// //    /**
// //     * 显示键ttl页面
// //     *
// //     * @param item 键
// //     */
// //    public static void showTTLKey(RedisKeyTreeItem item) {
// //        RedisShowTTLKeyEvent event = new RedisShowTTLKeyEvent();
// //        event.data(item);
// //        EventUtil.post(event);
// //    }
//
//     // /**
//     //  * 连接已导入事件
//     //  */
//     // public static void connectImported() {
//     //     EventUtil.post(new RedisConnectImportedEvent());
//     // }
// }
