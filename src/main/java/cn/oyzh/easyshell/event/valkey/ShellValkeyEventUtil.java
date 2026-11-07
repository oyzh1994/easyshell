package cn.oyzh.easyshell.event.valkey;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyZSetKeyTreeItem;
import cn.oyzh.event.EventUtil;
import io.valkey.CommandArguments;

import java.util.List;

/**
 * redis事件工具类，用于发布redis相关事件
 *
 * @author oyzh
 * @since 2025-11-21
 */

public class ShellValkeyEventUtil {

    // /**
    //  * list行添加事件
    //  *
    //  * @param item   redis树节点
    //  * @param key    键名称
    //  * @param member 成员
    //  */
    // public static void redisListRowAdded(ShellValkeyListKeyTreeItem item, String key, String member) {
    //     ShellValkeyListRowAddedEvent event = new ShellValkeyListRowAddedEvent();
    //     event.data(item);
    //     event.setKey(key);
    //     event.setMember(member);
    //     EventUtil.post(event);
    // }
    //
    // /**
    //  * set成员添加事件
    //  *
    //  * @param item   redis树节点
    //  * @param key    键名称
    //  * @param member 成员
    //  */
    // public static void redisSetMemberAdded(ShellValkeySetKeyTreeItem item, String key, String member) {
    //     ShellValkeySetMemberAddedEvent event = new ShellValkeySetMemberAddedEvent();
    //     event.data(item);
    //     event.setKey(key);
    //     event.setMember(member);
    //     EventUtil.post(event);
    // }
    //
    // /**
    //  * zset成员添加事件
    //  *
    //  * @param item   redis树节点
    //  * @param key    键名称
    //  * @param member 成员
    //  * @param score  成员
    //  */
    // public static void redisZSetMemberAdded(ShellValkeyZSetKeyTreeItem item, String key, String member, Double score) {
    //     ShellValkeyZSetMemberAddedEvent event = new ShellValkeyZSetMemberAddedEvent();
    //     event.data(item);
    //     event.setKey(key);
    //     event.setScore(score);
    //     event.setMember(member);
    //     EventUtil.post(event);
    // }
    //
    // /**
    //  * zset地理坐标添加事件
    //  *
    //  * @param item      redis树节点
    //  * @param key       键名称
    //  * @param member    成员
    //  * @param longitude 经度
    //  * @param latitude  纬度
    //  */
    // public static void redisZSetCoordinateAdded(ShellValkeyZSetKeyTreeItem item, String key, String member, double longitude, double latitude) {
    //     ShellValkeyZSetCoordinateAddedEvent event = new ShellValkeyZSetCoordinateAddedEvent();
    //     event.data(item);
    //     event.setKey(key);
    //     event.setMember(member);
    //     event.setLatitude(latitude);
    //     event.setLongitude(longitude);
    //     EventUtil.post(event);
    // }
    //
    // /**
    //  * stream消息添加事件
    //  *
    //  * @param item    redis树节点
    //  * @param key     键名称
    //  * @param message 内容
    //  */
    // public static void redisStreamMessageAdded(ShellValkeyStreamKeyTreeItem item, String key, String message) {
    //     ShellValkeyStreamMessageAddedEvent event = new ShellValkeyStreamMessageAddedEvent();
    //     event.data(item);
    //     event.setKey(key);
    //     event.setMessage(message);
    //     EventUtil.post(event);
    // }
    //
    // /**
    //  * hash字段添加事件
    //  *
    //  * @param item  redis树节点
    //  * @param key   键名称
    //  * @param field 字段名称
    //  * @param value 字段值
    //  */
    // public static void redisHashFieldAdded(ShellValkeyHashKeyTreeItem item, String key, String field, String value) {
    //     ShellValkeyHashFieldAddedEvent event = new ShellValkeyHashFieldAddedEvent();
    //     event.data(item);
    //     event.setKey(key);
    //     event.setField(field);
    //     event.setValue(value);
    //     EventUtil.post(event);
    // }
    //
    // /**
    //  * hylog元素添加事件
    //  *
    //  * @param item     redis树节点
    //  * @param key      键名称
    //  * @param elements 统计元素
    //  */
    // public static void redisHyLogElementsAdded(ShellValkeyStringKeyTreeItem item, String key, String[] elements) {
    //     ShellValkeyHyLogElementsAddedEvent event = new ShellValkeyHyLogElementsAddedEvent();
    //     event.data(item);
    //     event.setKey(key);
    //     event.setElements(elements);
    //     EventUtil.post(event);
    // }

    // /**
    //  * 键添加事件
    //  *
    //  * @param connect redis连接
    //  * @param type    键类型
    //  * @param key     键名称
    //  */
    // public static void redisKeyAdded(ShellConnect connect, String type, String key, int dbIndex) {
    //     ShellValkeyKeyAddedEvent event = new ShellValkeyKeyAddedEvent();
    //     event.data(connect);
    //     event.setKey(key);
    //     event.setType(type);
    //     event.setDbIndex(dbIndex);
    //     EventUtil.post(event);
    // }

    // /**
    //  * 键删除事件
    //  *
    //  * @param connect redis连接
    //  * @param key     键名称
    //  * @param dbIndex 库
    //  */
    // public static void redisKeyDeleted(ShellConnect connect, String key, int dbIndex) {
    //     ShellValkeyKeyDeletedEvent event = new ShellValkeyKeyDeletedEvent();
    //     event.data(connect);
    //     event.setKey(key);
    //     event.setDbIndex(dbIndex);
    //     EventUtil.post(event);
    // }

    /**
     * 键刷新事件
     *
     * @param connect 连接
     * @param dbIndex 数据库
     */
    public static void redisKeyFlushed(ShellConnect connect, Integer dbIndex) {
        ShellValkeyKeyFlushedEvent event = new ShellValkeyKeyFlushedEvent();
        event.data(dbIndex);
        event.setConnect(connect);
        EventUtil.post(event);
    }

    /**
     * 键ttl更新事件
     *
     * @param connect 连接
     * @param ttl     ttl值
     * @param key     键名称
     * @param dbIndex 数据库索引
     */
    public static void redisKeyTTLUpdated(ShellConnect connect, Long ttl, String key, int dbIndex) {
        ShellValkeyKeyTTLUpdatedEvent event = new ShellValkeyKeyTTLUpdatedEvent();
        event.data(connect);
        event.setTtl(ttl);
        event.setKey(key);
        event.setDbIndex(dbIndex);
        EventUtil.post(event);
    }

    // /**
    //  * 键更名事件
    //  *
    //  * @param item   redis树节点
    //  * @param oldKey 旧名称
    //  */
    // public static void redisKeyRenamed(ShellValkeyKeyTreeItem item, String oldKey) {
    //     ShellValkeyKeyRenamedEvent event = new ShellValkeyKeyRenamedEvent();
    //     event.data(item);
    //     event.setOldKey(oldKey);
    //     EventUtil.post(event);
    // }

    // /**
    //  * 键复制事件
    //  *
    //  * @param connect  redis树节点
    //  * @param targetDB 目标库
    //  */
    // public static void redisKeyCopied(ShellConnect connect, List<String> key, int dbIndex, int targetDB) {
    //     ShellValkeyKeyCopiedEvent event = new ShellValkeyKeyCopiedEvent();
    //     event.data(key);
    //     event.setDbIndex(dbIndex);
    //     event.setConnect(connect);
    //     event.setTargetDB(targetDB);
    //     EventUtil.post(event);
    // }
    //
    // /**
    //  * 键移动事件
    //  *
    //  * @param item     redis树节点
    //  * @param targetDB 目标库
    //  */
    // public static void redisKeyMoved(ShellValkeyKeyTreeItem item, int targetDB) {
    //     ShellValkeyKeyMovedEvent event = new ShellValkeyKeyMovedEvent();
    //     event.data(item);
    //     event.setTargetDB(targetDB);
    //     EventUtil.post(event);
    // }

    /**
     * 多个键复制事件
     *
     * @param connect  redis树节点
     * @param keys     键列表
     * @param dbIndex  库
     * @param targetDB 目标库
     */
    public static void redisKeysCopied(ShellConnect connect, List<String> keys, int dbIndex, int targetDB) {
        ShellValkeyKeysCopiedEvent event = new ShellValkeyKeysCopiedEvent();
        event.data(keys);
        event.setSourceDB(dbIndex);
        event.setConnect(connect);
        event.setTargetDB(targetDB);
        EventUtil.post(event);
    }

    /**
     * 多个键移动事件
     *
     * @param connect  连接
     * @param dbIndex  数据库
     * @param targetDB 目标库
     */
    public static void redisKeysMoved(ShellConnect connect, Integer dbIndex, int targetDB) {
        ShellValkeyKeysMovedEvent event = new ShellValkeyKeysMovedEvent();
        event.data(dbIndex);
        event.setConnect(connect);
        event.setTargetDB(targetDB);
        EventUtil.post(event);
    }

    // /**
    //  * 订阅打开事件
    //  *
    //  * @param item redis节点
    //  */
    // public static void redisPubsubOpen(ShellValkeyPubsubItem item) {
    //     ShellValkeyPubsubOpenEvent event = new ShellValkeyPubsubOpenEvent();
    //     event.data(item);
    //     EventUtil.post(event);
    // }

    /**
     * redis客户端操作
     *
     * @param connectName 连接名称
     * @param arguments   命令参数
     */
    public static void redisClientAction(String connectName, CommandArguments arguments) {
        ShellValkeyClientActionEvent event = new ShellValkeyClientActionEvent();
        event.data(connectName);
        event.setArguments(arguments);
        EventUtil.postAsync(event);
    }

    /**
     * zset反转视图事件
     *
     * @param item 节点
     */
    public static void redisZSetReverseView(ShellValkeyZSetKeyTreeItem item) {
        ShellValkeyZSetReverseViewEvent event = new ShellValkeyZSetReverseViewEvent();
        event.data(item);
        EventUtil.post(event);
    }

}
