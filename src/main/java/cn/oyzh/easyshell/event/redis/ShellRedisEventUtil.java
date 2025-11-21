package cn.oyzh.easyshell.event.redis;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.redis.ShellRedisZSetKeyTreeItem;
import cn.oyzh.event.EventUtil;
import redis.clients.jedis.CommandArguments;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-02-14
 */

public class ShellRedisEventUtil {

    // /**
    //  * list行添加事件
    //  *
    //  * @param item   redis树节点
    //  * @param key    键名称
    //  * @param member 成员
    //  */
    // public static void redisListRowAdded(ShellRedisListKeyTreeItem item, String key, String member) {
    //     ShellRedisListRowAddedEvent event = new ShellRedisListRowAddedEvent();
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
    // public static void redisSetMemberAdded(ShellRedisSetKeyTreeItem item, String key, String member) {
    //     ShellRedisSetMemberAddedEvent event = new ShellRedisSetMemberAddedEvent();
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
    // public static void redisZSetMemberAdded(ShellRedisZSetKeyTreeItem item, String key, String member, Double score) {
    //     ShellRedisZSetMemberAddedEvent event = new ShellRedisZSetMemberAddedEvent();
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
    // public static void redisZSetCoordinateAdded(ShellRedisZSetKeyTreeItem item, String key, String member, double longitude, double latitude) {
    //     ShellRedisZSetCoordinateAddedEvent event = new ShellRedisZSetCoordinateAddedEvent();
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
    // public static void redisStreamMessageAdded(ShellRedisStreamKeyTreeItem item, String key, String message) {
    //     ShellRedisStreamMessageAddedEvent event = new ShellRedisStreamMessageAddedEvent();
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
    // public static void redisHashFieldAdded(ShellRedisHashKeyTreeItem item, String key, String field, String value) {
    //     ShellRedisHashFieldAddedEvent event = new ShellRedisHashFieldAddedEvent();
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
    // public static void redisHyLogElementsAdded(ShellRedisStringKeyTreeItem item, String key, String[] elements) {
    //     ShellRedisHyLogElementsAddedEvent event = new ShellRedisHyLogElementsAddedEvent();
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
    //     ShellRedisKeyAddedEvent event = new ShellRedisKeyAddedEvent();
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
    //     ShellRedisKeyDeletedEvent event = new ShellRedisKeyDeletedEvent();
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
        ShellRedisKeyFlushedEvent event = new ShellRedisKeyFlushedEvent();
        event.data(dbIndex);
        event.setConnect(connect);
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

    // /**
    //  * 键更名事件
    //  *
    //  * @param item   redis树节点
    //  * @param oldKey 旧名称
    //  */
    // public static void redisKeyRenamed(ShellRedisKeyTreeItem item, String oldKey) {
    //     ShellRedisKeyRenamedEvent event = new ShellRedisKeyRenamedEvent();
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
    //     ShellRedisKeyCopiedEvent event = new ShellRedisKeyCopiedEvent();
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
    // public static void redisKeyMoved(ShellRedisKeyTreeItem item, int targetDB) {
    //     ShellRedisKeyMovedEvent event = new ShellRedisKeyMovedEvent();
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
        ShellRedisKeysCopiedEvent event = new ShellRedisKeysCopiedEvent();
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
        ShellRedisKeysMovedEvent event = new ShellRedisKeysMovedEvent();
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
    // public static void redisPubsubOpen(ShellRedisPubsubItem item) {
    //     ShellRedisPubsubOpenEvent event = new ShellRedisPubsubOpenEvent();
    //     event.data(item);
    //     EventUtil.post(event);
    // }

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
    public static void redisZSetReverseView(ShellRedisZSetKeyTreeItem item) {
        ShellRedisZSetReverseViewEvent event = new ShellRedisZSetReverseViewEvent();
        event.data(item);
        EventUtil.post(event);
    }

}
