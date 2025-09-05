package cn.oyzh.easyshell.event;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.dto.redis.RedisPubsubItem;
import cn.oyzh.easyshell.event.client.ShellClientActionEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectAddedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectDeletedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectEditEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectImportedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectOpenedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectUpdatedEvent;
import cn.oyzh.easyshell.event.connection.ShellConnectionClosedEvent;
import cn.oyzh.easyshell.event.connection.ShellConnectionConnectedEvent;
import cn.oyzh.easyshell.event.docker.ShellContainerCommitEvent;
import cn.oyzh.easyshell.event.docker.ShellContainerRunEvent;
import cn.oyzh.easyshell.event.file.ShellFileDraggedEvent;
import cn.oyzh.easyshell.event.group.ShellAddGroupEvent;
import cn.oyzh.easyshell.event.group.ShellGroupAddedEvent;
import cn.oyzh.easyshell.event.group.ShellGroupDeletedEvent;
import cn.oyzh.easyshell.event.group.ShellGroupRenamedEvent;
import cn.oyzh.easyshell.event.key.ShellKeyAddedEvent;
import cn.oyzh.easyshell.event.key.ShellKeyUpdatedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisClientActionEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisHashFieldAddedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisHyLogElementsAddedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeyAddedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeyCopiedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeyDeletedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeyFlushedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeyMovedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeyRenamedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeyTTLUpdatedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeysMovedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisListRowAddedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisPubsubOpenEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisSetMemberAddedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisStreamMessageAddedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisZSetCoordinateAddedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisZSetMemberAddedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisZSetReverseViewEvent;
import cn.oyzh.easyshell.event.snippet.ShellRunSnippetEvent;
import cn.oyzh.easyshell.event.tree.ShellTreeItemChangedEvent;
import cn.oyzh.easyshell.event.window.ShellShowKeyEvent;
import cn.oyzh.easyshell.event.window.ShellShowMessageEvent;
import cn.oyzh.easyshell.event.window.ShellShowSplitEvent;
import cn.oyzh.easyshell.event.window.ShellShowTerminalEvent;
import cn.oyzh.easyshell.event.zk.ZKClientActionEvent;
import cn.oyzh.easyshell.event.zk.ZKNodeACLAddedEvent;
import cn.oyzh.easyshell.event.zk.ZKNodeACLUpdatedEvent;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.trees.redis.RedisHashKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.RedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.RedisListKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.RedisSetKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.RedisStreamKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.RedisStringKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.RedisZSetKeyTreeItem;
import cn.oyzh.easyshell.util.zk.ShellZKClientActionArgument;
import cn.oyzh.event.EventUtil;
import cn.oyzh.fx.gui.event.Layout1Event;
import cn.oyzh.fx.gui.event.Layout2Event;
import cn.oyzh.fx.plus.changelog.ChangelogEvent;
import javafx.scene.control.TreeItem;
import redis.clients.jedis.CommandArguments;

import java.io.File;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-02-14
 */

public class ShellEventUtil {

    /**
     * 连接打开事件
     *
     * @param connect 连接
     */
    public static void connectionOpened(ShellConnect connect) {
        ShellConnectOpenedEvent event = new ShellConnectOpenedEvent();
        event.data(connect);
        EventUtil.postSync(event);
    }

    /**
     * 连接编辑事件
     *
     * @param connect shell连接
     */
    public static void connectEdit(ShellConnect connect) {
        ShellConnectEditEvent event = new ShellConnectEditEvent();
        event.data(connect);
        EventUtil.post(event);
    }

    /**
     * 连接关闭事件
     *
     * @param client shell客户端
     */
    public static void connectionClosed(ShellBaseClient client) {
        ShellConnectionClosedEvent event = new ShellConnectionClosedEvent();
        event.data(client);
        EventUtil.post(event);
    }

    /**
     * 连接成功事件
     *
     * @param client shell客户端
     */
    public static void connectionConnected(ShellBaseClient client) {
        ShellConnectionConnectedEvent event = new ShellConnectionConnectedEvent();
        event.data(client);
        EventUtil.post(event);
    }

    /**
     * 连接已新增事件
     *
     * @param shellConnect shell连接
     */
    public static void connectAdded(ShellConnect shellConnect) {
        ShellConnectAddedEvent event = new ShellConnectAddedEvent();
        event.data(shellConnect);
        EventUtil.post(event);
    }

    /**
     * 连接已修改事件
     *
     * @param shellConnect shell连接
     */
    public static void connectUpdated(ShellConnect shellConnect) {
        ShellConnectUpdatedEvent event = new ShellConnectUpdatedEvent();
        event.data(shellConnect);
        EventUtil.post(event);
    }

    /**
     * 连接已删除事件
     *
     * @param shellConnect shell连接
     */
    public static void connectDeleted(ShellConnect shellConnect) {
        ShellConnectDeletedEvent event = new ShellConnectDeletedEvent();
        event.data(shellConnect);
        EventUtil.post(event);
    }

    /**
     * 添加分组
     */
    public static void addGroup() {
        EventUtil.post(new ShellAddGroupEvent());
    }

    /**
     * 更新日志事件
     */
    public static void changelog() {
        EventUtil.post(new ChangelogEvent());
    }

    /**
     * 节点选中事件
     *
     * @param item 节点
     */
    public static void treeItemChanged(TreeItem<?> item) {
        ShellTreeItemChangedEvent event = new ShellTreeItemChangedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 布局1
     */
    public static void layout1() {
        EventUtil.post(new Layout1Event());
    }

    /**
     * 布局2
     */
    public static void layout2() {
        EventUtil.post(new Layout2Event());
    }

    /**
     * 分组已添加
     */
    public static void groupAdded(String group) {
        ShellGroupAddedEvent event = new ShellGroupAddedEvent();
        event.data(group);
        EventUtil.post(event);
    }

    /**
     * 分组已删除
     */
    public static void groupDeleted(String group) {
        ShellGroupDeletedEvent event = new ShellGroupDeletedEvent();
        event.data(group);
        EventUtil.post(event);
    }

    /**
     * 分组已更名
     */
    public static void groupRenamed(String group, String oldName) {
        ShellGroupRenamedEvent event = new ShellGroupRenamedEvent();
        event.data(group);
        event.setOldName(oldName);
        EventUtil.post(event);
    }

//    /**
//     * 显示导出连接页面
//     */
//    public static void showExportConnect() {
//        EventUtil.post(new ShellShowExportConnectEvent());
//    }

//    /**
//     * 显示导入连接页面
//     *
//     * @param file 文件
//     */
//    public static void showImportConnect(File file) {
//        ShellShowImportConnectEvent event = new ShellShowImportConnectEvent();
//        event.data(file);
//        EventUtil.post(event);
//    }
//
//    /**
//     * 显示设置页面
//     */
//    public static void showSetting() {
//        EventUtil.post(new ShellShowSettingEvent());
//    }

//    /**
//     * 显示添加连接页面
//     */
//    public static void showAddConnect() {
//        showAddConnect(null);
//    }

//    /**
//     * 显示修改连接页面
//     *
//     * @param connect shell连接
//     */
//    public static void showUpdateConnect(ShellConnect connect) {
//        ShellShowUpdateConnectEvent event = new ShellShowUpdateConnectEvent();
//        event.data(connect);
//        EventUtil.post(event);
//    }

//    /**
//     * 显示添加连接页面
//     *
//     * @param group 分组
//     */
//    public static void showAddConnect(ShellGroup group) {
//        ShellShowAddConnectEvent event = new ShellShowAddConnectEvent();
//        event.data(group);
//        EventUtil.post(event);
//    }

    /**
     * 显示密钥管理
     */
    public static void showKey() {
        EventUtil.post(new ShellShowKeyEvent());
    }

    /**
     * 显示消息页面
     */
    public static void showMessage() {
        EventUtil.post(new ShellShowMessageEvent());
    }

//    /**
//     * 显示工具页面
//     */
//    public static void showTool() {
//        EventUtil.post(new ShellShowToolEvent());
//    }
//
//    /**
//     * 显示关于页面
//     */
//    public static void showAbout() {
//        EventUtil.post(new ShellShowAboutEvent());
//    }

    /**
     * 连接已导入事件
     */
    public static void connectImported() {
        EventUtil.post(new ShellConnectImportedEvent());
    }

//    /**
//     * 显示文件信息页面
//     *
//     * @param file 文件
//     */
//    public static void showFileInfo(ShellSFTPFile file) {
//        ShellShowFileInfoEvent event = new ShellShowFileInfoEvent();
//        event.data(file);
//        EventUtil.post(event);
//    }

//    /**
//     * 文件已保存事件
//     *
//     * @param file 文件
//     */
//    public static void fileSaved(ShellFile file) {
//        ShellFileSavedEvent event = new ShellFileSavedEvent();
//        event.data(file);
//        EventUtil.post(event);
//    }

    /**
     * 文件已拖拽事件
     *
     * @param files 文件
     */
    public static void fileDragged(List<File> files) {
        ShellFileDraggedEvent event = new ShellFileDraggedEvent();
        event.data(files);
        EventUtil.post(event);
    }

    /**
     * 打开终端页面
     */
    public static void showTerminal() {
        ShellShowTerminalEvent event = new ShellShowTerminalEvent();
        EventUtil.post(event);
    }

//    /**
//     * 显示传输文件页面
//     */
//    public static void showTransportFile() {
//        showTransportFile(null);
//    }

//    /**
//     * 显示传输数据页面
//     *
//     * @param connect shell连接
//     */
//    public static void showTransportFile(ShellConnect connect) {
//        ShellShowTransportFileEvent event = new ShellShowTransportFileEvent();
//        event.data(connect);
//        EventUtil.post(event);
//    }

    /**
     * 密钥已新增事件
     *
     * @param shellKey 密钥
     */
    public static void keyAdded(ShellKey shellKey) {
        ShellKeyAddedEvent event = new ShellKeyAddedEvent();
        event.data(shellKey);
        EventUtil.post(event);
    }

    /**
     * 密钥已修改事件
     *
     * @param shellKey 密钥
     */
    public static void keyUpdated(ShellKey shellKey) {
        ShellKeyUpdatedEvent event = new ShellKeyUpdatedEvent();
        event.data(shellKey);
        EventUtil.post(event);
    }

    /**
     * 客户端操作
     *
     * @param connectName 客户端名称
     * @param action      操作
     */
    public static void clientAction(String connectName, String action) {
        ShellClientActionEvent event = new ShellClientActionEvent();
        event.data(connectName);
        event.setAction(action);
        EventUtil.postAsync(event);
    }

//    /**
//     * 跳板已新增事件
//     *
//     * @param config 跳板配置
//     */
//    public static void jumpAdded(ShellJumpConfig config) {
//        ShellJumpAddedEvent event = new ShellJumpAddedEvent();
//        event.data(config);
//        EventUtil.post(event);
//    }
//
//    /**
//     * 跳板已修改事件
//     *
//     * @param config 跳板配置
//     */
//    public static void jumpUpdated(ShellJumpConfig config) {
//        ShellJumpUpdatedEvent event = new ShellJumpUpdatedEvent();
//        event.data(config);
//        EventUtil.post(event);
//    }

    /**
     * 显示分屏页面
     *
     * @param type     类型
     * @param connects 连接列表
     */
    public static void showSplit(String type, List<ShellConnect> connects) {
        ShellShowSplitEvent event = new ShellShowSplitEvent();
        event.data(type);
        event.setConnects(connects);
        EventUtil.post(event);
    }

    /**
     * 执行片段
     *
     * @param content 内容
     * @param runAll  是否在所有tab执行
     */
    public static void runSnippet(String content, boolean runAll) {
        ShellRunSnippetEvent event = new ShellRunSnippetEvent();
        event.data(content);
        event.setRunAll(runAll);
        EventUtil.postAsync(event);
    }

    /**
     * 容器运行事件
     *
     * @param exec 执行器
     */
    public static void containerRun(ShellDockerExec exec) {
        ShellContainerRunEvent event = new ShellContainerRunEvent();
        event.data(exec);
        EventUtil.postAsync(event);
    }

    /**
     * 容器保存事件
     *
     * @param exec 执行器
     */
    public static void containerCommit(ShellDockerExec exec) {
        ShellContainerCommitEvent event = new ShellContainerCommitEvent();
        event.data(exec);
        EventUtil.postAsync(event);
    }

    /**
     * list行添加事件
     *
     * @param item   redis树节点
     * @param key    键名称
     * @param member 成员
     */
    public static void redisListRowAdded(RedisListKeyTreeItem item, String key, String member) {
        ShellRedisListRowAddedEvent event = new ShellRedisListRowAddedEvent();
        event.data(item);
        event.setKey(key);
        event.setMember(member);
        EventUtil.post(event);
    }

    /**
     * set成员添加事件
     *
     * @param item   redis树节点
     * @param key    键名称
     * @param member 成员
     */
    public static void redisSetMemberAdded(RedisSetKeyTreeItem item, String key, String member) {
        ShellRedisSetMemberAddedEvent event = new ShellRedisSetMemberAddedEvent();
        event.data(item);
        event.setKey(key);
        event.setMember(member);
        EventUtil.post(event);
    }

    /**
     * zset成员添加事件
     *
     * @param item   redis树节点
     * @param key    键名称
     * @param member 成员
     * @param score  成员
     */
    public static void redisZSetMemberAdded(RedisZSetKeyTreeItem item, String key, String member, Double score) {
        ShellRedisZSetMemberAddedEvent event = new ShellRedisZSetMemberAddedEvent();
        event.data(item);
        event.setKey(key);
        event.setScore(score);
        event.setMember(member);
        EventUtil.post(event);
    }

    /**
     * zset地理坐标添加事件
     *
     * @param item      redis树节点
     * @param key       键名称
     * @param member    成员
     * @param longitude 经度
     * @param latitude  纬度
     */
    public static void redisZSetCoordinateAdded(RedisZSetKeyTreeItem item, String key, String member, double longitude, double latitude) {
        ShellRedisZSetCoordinateAddedEvent event = new ShellRedisZSetCoordinateAddedEvent();
        event.data(item);
        event.setKey(key);
        event.setMember(member);
        event.setLatitude(latitude);
        event.setLongitude(longitude);
        EventUtil.post(event);
    }

    /**
     * stream消息添加事件
     *
     * @param item    redis树节点
     * @param key     键名称
     * @param message 内容
     */
    public static void redisStreamMessageAdded(RedisStreamKeyTreeItem item, String key, String message) {
        ShellRedisStreamMessageAddedEvent event = new ShellRedisStreamMessageAddedEvent();
        event.data(item);
        event.setKey(key);
        event.setMessage(message);
        EventUtil.post(event);
    }

    /**
     * hash字段添加事件
     *
     * @param item  redis树节点
     * @param key   键名称
     * @param field 字段名称
     * @param value 字段值
     */
    public static void redisHashFieldAdded(RedisHashKeyTreeItem item, String key, String field, String value) {
        ShellRedisHashFieldAddedEvent event = new ShellRedisHashFieldAddedEvent();
        event.data(item);
        event.setKey(key);
        event.setField(field);
        event.setValue(value);
        EventUtil.post(event);
    }

    /**
     * hylog元素添加事件
     *
     * @param item     redis树节点
     * @param key      键名称
     * @param elements 统计元素
     */
    public static void redisHyLogElementsAdded(RedisStringKeyTreeItem item, String key, String[] elements) {
        ShellRedisHyLogElementsAddedEvent event = new ShellRedisHyLogElementsAddedEvent();
        event.data(item);
        event.setKey(key);
        event.setElements(elements);
        EventUtil.post(event);
    }

    /**
     * 键添加事件
     *
     * @param connect redis连接
     * @param type    键类型
     * @param key     键名称
     */
    public static void redisKeyAdded(ShellConnect connect, String type, String key, int dbIndex) {
        ShellRedisKeyAddedEvent event = new ShellRedisKeyAddedEvent();
        event.data(connect);
        event.setKey(key);
        event.setType(type);
        event.setDbIndex(dbIndex);
        EventUtil.post(event);
    }

    /**
     * 键删除事件
     *
     * @param connect redis连接
     * @param key     键名称
     * @param dbIndex 库
     */
    public static void redisKeyDeleted(ShellConnect connect, String key, int dbIndex) {
        ShellRedisKeyDeletedEvent event = new ShellRedisKeyDeletedEvent();
        event.data(connect);
        event.setKey(key);
        event.setDbIndex(dbIndex);
        EventUtil.post(event);
    }

    /**
     * 键刷新事件
     *
     * @param item redis树节点
     */
    public static void redisKeyFlushed(Integer item) {
        ShellRedisKeyFlushedEvent event = new ShellRedisKeyFlushedEvent();
        event.data(item);
        EventUtil.post(event);
    }


    /**
     * 键ttl更新事件
     *
     * @param connect redis树节点
     * @param ttl     ttl值
     */
    public static void redisKeyTTLUpdated(ShellConnect connect, Long ttl, String key, int dbIndex) {
        ShellRedisKeyTTLUpdatedEvent event = new ShellRedisKeyTTLUpdatedEvent();
        event.data(connect);
        event.setTtl(ttl);
        event.setKey(key);
        event.setDbIndex(dbIndex);
        EventUtil.post(event);
    }

    /**
     * 键更名事件
     *
     * @param item   redis树节点
     * @param oldKey 旧名称
     */
    public static void redisKeyRenamed(RedisKeyTreeItem item, String oldKey) {
        ShellRedisKeyRenamedEvent event = new ShellRedisKeyRenamedEvent();
        event.data(item);
        event.setOldKey(oldKey);
        EventUtil.post(event);
    }

    /**
     * 键复制事件
     *
     * @param connect  redis树节点
     * @param targetDB 目标库
     */
    public static void redisKeyCopied(ShellConnect connect, List<String> key, int dbIndex, int targetDB) {
        ShellRedisKeyCopiedEvent event = new ShellRedisKeyCopiedEvent();
        event.data(key);
        event.setDbIndex(dbIndex);
        event.setConnect(connect);
        event.setTargetDB(targetDB);
        EventUtil.post(event);
    }

    /**
     * 键移动事件
     *
     * @param item     redis树节点
     * @param targetDB 目标库
     */
    public static void redisKeyMoved(RedisKeyTreeItem item, int targetDB) {
        ShellRedisKeyMovedEvent event = new ShellRedisKeyMovedEvent();
        event.data(item);
        event.setTargetDB(targetDB);
        EventUtil.post(event);
    }

    /**
     * 多个键移动事件
     *
     * @param connect  连接
     * @param item     redis树节点
     * @param targetDB 目标库
     */
    public static void redisKeysMoved(ShellConnect connect, Integer item, int targetDB) {
        ShellRedisKeysMovedEvent event = new ShellRedisKeysMovedEvent();
        event.data(item);
        event.setTargetDB(targetDB);
        EventUtil.post(event);
    }

    /**
     * 订阅打开事件
     *
     * @param item redis节点
     */
    public static void redisPubsubOpen(RedisPubsubItem item) {
        ShellRedisPubsubOpenEvent event = new ShellRedisPubsubOpenEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * redis客户端操作
     */
    public static void redisClientAction(String connectName, CommandArguments arguments) {
        ShellRedisClientActionEvent event = new ShellRedisClientActionEvent();
        event.data(connectName);
        event.setArguments(arguments);
        EventUtil.postAsync(event);
    }

    /**
     * zset反转视图事件
     *
     * @param item 节点
     */
    public static void redisZSetReverseView(RedisZSetKeyTreeItem item) {
        ShellRedisZSetReverseViewEvent event = new ShellRedisZSetReverseViewEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 节点acl添加事件
     *
     * @param zkConnect zk连接
     */
    public static void zkNodeACLAdded(ShellConnect zkConnect, String nodePath) {
        ZKNodeACLAddedEvent event = new ZKNodeACLAddedEvent();
        event.data(zkConnect);
        event.setNodePath(nodePath);
        EventUtil.post(event);
    }

    /**
     * 节点acl修改事件
     *
     * @param zkConnect zk连接
     */
    public static void zkNodeACLUpdated(ShellConnect zkConnect, String nodePath) {
        ZKNodeACLUpdatedEvent event = new ZKNodeACLUpdatedEvent();
        event.data(zkConnect);
        event.setNodePath(nodePath);
        EventUtil.post(event);
    }

    /**
     * 客户端操作
     */
    public static void zkClientAction(String connectName, String action) {
        ZKClientActionEvent event = new ZKClientActionEvent();
        event.data(connectName);
        event.setAction(action);
        EventUtil.postAsync(event);
    }

    /**
     * 客户端操作
     */
    public static void zkClientAction(String connectName, String action, List<ShellZKClientActionArgument> arguments) {
        ZKClientActionEvent event = new ZKClientActionEvent();
        event.data(connectName);
        event.setAction(action);
        event.arguments(arguments);
        EventUtil.postAsync(event);
    }

}
