package cn.oyzh.easyshell.redis;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.batch.ShellRedisCountResult;
import cn.oyzh.easyshell.redis.batch.ShellRedisDeleteResult;
import cn.oyzh.easyshell.redis.batch.ShellRedisScanResult;
import cn.oyzh.easyshell.redis.batch.ShellRedisScanSimpleResult;
import cn.oyzh.easyshell.redis.key.ShellRedisHashValue;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.easyshell.redis.key.ShellRedisListValue;
import cn.oyzh.easyshell.redis.key.ShellRedisSetValue;
import cn.oyzh.easyshell.redis.key.ShellRedisStreamValue;
import cn.oyzh.easyshell.redis.key.ShellRedisStringValue;
import cn.oyzh.easyshell.redis.key.ShellRedisZSetValue;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.StreamEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis键工具类
 *
 * @author oyzh
 * @since 2023/06/30
 */

public class ShellRedisKeyUtil {

    // /**
    //  * 是否被过滤
    //  *
    //  * @param key     键名称
    //  * @param filters 过滤配置列表
    //  * @return 结果
    //  */
    // public static boolean isFiltered(String key, List<RedisFilter> filters) {
    //     if (CollectionUtil.isEmpty(filters) || key == null) {
    //         return false;
    //     }
    //     // 匹配结果
    //     for (RedisFilter filter : filters) {
    //         // 未启用，不处理
    //         if (!filter.isEnable()) {
    //             continue;
    //         }
    //         // 模糊匹配
    //         if (filter.isPartMatch() && key.contains(filter.getKw())) {
    //             return true;
    //         }
    //         // 完全匹配
    //         if (key.equalsIgnoreCase(filter.getKw())) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    // /**
    //  * 序列化键
    //  *
    //  * @param redisKey redis键
    //  * @return 序列化内容
    //  */
    // public static String serializeNode(ShellRedisKey redisKey) {
    //     return serializeNode(redisKey, true);
    // }

    /**
     * 序列化键
     *
     * @param redisKey redis键
     * @return 序列化内容
     */
    public static String serializeNode(ShellRedisKey redisKey) {
        String result = null;
        // string
        if (redisKey.isStringKey()) {
            ShellRedisStringValue stringValue = redisKey.asStringValue();
            // if (redisKey.isRawEncoding() && binaryAsHex) {
            //     return "0x'" + TextUtil.bytesToHexStr(stringValue.bytesValue()) + "'";
            // }
            result = JSONUtil.toJson(stringValue.getValue());
        } else if (redisKey.isListKey()) { // list
            ShellRedisListValue value = redisKey.asListValue();
            List<ShellRedisListValue.RedisListRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (ShellRedisListValue.RedisListRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", row.getValue());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isSetKey()) {  // set
            ShellRedisSetValue value = redisKey.asSetValue();
            List<ShellRedisSetValue.RedisSetRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (ShellRedisSetValue.RedisSetRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", row.getValue());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isZSetKey()) {   // zset
            ShellRedisZSetValue value = redisKey.asZSetValue();
            List<ShellRedisZSetValue.RedisZSetRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (ShellRedisZSetValue.RedisZSetRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", row.getValue());
                map.put("score", row.getScore());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isStreamKey()) {  // stream
            ShellRedisStreamValue value = redisKey.asStreamValue();
            List<ShellRedisStreamValue.RedisStreamRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (ShellRedisStreamValue.RedisStreamRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", row.getId());
                map.put("value", row.getValue());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isHashKey()) {   // hash
            ShellRedisHashValue value = redisKey.asHashValue();
            List<ShellRedisHashValue.RedisHashRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (ShellRedisHashValue.RedisHashRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("field", row.getField());
                map.put("value", row.getValue());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        }
        return result;
    }

    /**
     * 反序列化键
     *
     * @param type  类型
     * @param value 值
     * @return redis键
     */
    public static ShellRedisKey deserializeNode(ShellRedisKeyType type, String value) {
        // string
        if (type == ShellRedisKeyType.STRING) {
            ShellRedisKey node = new ShellRedisKey();
            node.valueOfString(value == null ? "" : value);
            return node;
        }

        // list
        if (type == ShellRedisKeyType.LIST) {
            ShellRedisKey node = new ShellRedisKey();
            List<String> list = new ArrayList<>();
            if (StringUtil.isNotBlank(value)) {
                JSONArray array = JSONUtil.parseArray(value);
                for (int i = 0; i < array.size(); i++) {
                    list.add(array.getJSONObject(i).getString("value"));
                }
            }
            node.valueOfList(list);
            return node;
        }

        // set
        if (type == ShellRedisKeyType.SET) {
            ShellRedisKey node = new ShellRedisKey();
            Set<String> list = new HashSet<>();
            if (StringUtil.isNotBlank(value)) {
                JSONArray array = JSONUtil.parseArray(value);
                for (int i = 0; i < array.size(); i++) {
                    list.add(array.getJSONObject(i).getString("value"));
                }
            }
            node.valueOfSet(list);
            return node;
        }

        // zset
        if (type == ShellRedisKeyType.ZSET) {
            ShellRedisKey node = new ShellRedisKey();
            List<String> list1 = new ArrayList<>();
            List<Double> list2 = new ArrayList<>();
            if (StringUtil.isNotBlank(value)) {
                JSONArray array = JSONUtil.parseArray(value);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    list1.add(object.getString("value"));
                    list2.add(object.getDouble("score"));
                }
            }
            node.valueOfZSet(list1, list2);
            return node;
        }

        // hash
        if (type == ShellRedisKeyType.HASH) {
            ShellRedisKey node = new ShellRedisKey();
            Map<String, String> map = new HashMap<>();
            if (StringUtil.isNotBlank(value)) {
                JSONArray array = JSONUtil.parseArray(value);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    map.put(object.getString("field"), object.getString("value"));
                }
            }
            node.valueOfHash(map);
            return node;
        }

        // stream
        if (type == ShellRedisKeyType.STREAM) {
            ShellRedisKey node = new ShellRedisKey();
            List<StreamEntry> list = new ArrayList<>();
            if (StringUtil.isNotBlank(value)) {
                JSONArray array = JSONUtil.parseArray(value);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    StreamEntryID id = new StreamEntryID(object.getString("id"));
                    String fields = object.getString("value");
                    Map<String, String> fieldMap;
                    if (StringUtil.isNotBlank(fields)) {
                        fieldMap = new HashMap<>();
                    } else {
                        fieldMap = JSONUtil.toBean(fields, HashMap.class);
                    }
                    StreamEntry entry = new StreamEntry(id, fieldMap);
                    list.add(entry);
                }
            }
            node.valueOfStream(list);
            return node;
        }
        return null;
    }

    /**
     * 创建键
     *
     * @param node    redis键
     * @param dbIndex db索引
     * @param client  redis客户端
     */
    public static void createKey(ShellRedisKey node, Integer dbIndex, ShellRedisClient client) {
        if (node == null || client == null) {
            return;
        }
        String key = node.getKey();
        // string
        if (node.isStringKey()) {
            client.set(dbIndex, key, (String) node.asStringValue().getValue());
        } else if (node.isListKey()) {// list
            ShellRedisListValue value = node.asListValue();
            List<ShellRedisListValue.RedisListRow> rows = value.getValue();
            String[] arr;
            if (CollectionUtil.isEmpty(rows)) {
                arr = new String[]{""};
            } else {
                List<String> list = rows.parallelStream().map(ShellRedisListValue.RedisListRow::getValue).collect(Collectors.toList());
                arr = ArrayUtil.toArray(list, String.class);
            }
            client.lpush(dbIndex, key, arr);
        } else if (node.isSetKey()) {// set
            ShellRedisSetValue value = node.asSetValue();
            List<ShellRedisSetValue.RedisSetRow> rows = value.getValue();
            String[] arr;
            if (CollectionUtil.isEmpty(rows)) {
                arr = new String[]{""};
            } else {
                List<String> list = rows.parallelStream().map(ShellRedisSetValue.RedisSetRow::getValue).collect(Collectors.toList());
                arr = ArrayUtil.toArray(list, String.class);
            }
            client.sadd(dbIndex, key, arr);
        } else if (node.isZSetKey()) {// zset
            ShellRedisZSetValue value = node.asZSetValue();
            List<ShellRedisZSetValue.RedisZSetRow> rows = value.getValue();
            Map<String, Double> scoreMembers;
            if (CollectionUtil.isEmpty(rows)) {
                scoreMembers = Collections.emptyMap();
            } else {
                scoreMembers = new HashMap<>();
                for (ShellRedisZSetValue.RedisZSetRow row : rows) {
                    scoreMembers.put(row.getValue(), row.getScore());
                }
            }
            client.zadd(dbIndex, key, scoreMembers);
        } else if (node.isHashKey()) {// hash
            ShellRedisHashValue value = node.asHashValue();
            List<ShellRedisHashValue.RedisHashRow> rows = value.getValue();
            Map<String, String> hash;
            if (CollectionUtil.isEmpty(rows)) {
                hash = Collections.emptyMap();
            } else {
                hash = new HashMap<>();
                for (ShellRedisHashValue.RedisHashRow row : rows) {
                    hash.put(row.getField(), row.getValue());
                }
            }
            client.hmset(dbIndex, key, hash);
        } else if (node.isStreamKey()) {// stream
            ShellRedisStreamValue value = node.asStreamValue();
            List<ShellRedisStreamValue.RedisStreamRow> rows = value.getValue();
            if (CollectionUtil.isNotEmpty(rows)) {
                for (ShellRedisStreamValue.RedisStreamRow row : rows) {
                    client.xadd(dbIndex, key, row.getStreamId(), row.getFields());
                }
            }
        }
    }

    /**
     * 获取键值
     *
     * @param node    redis键
     * @param dbIndex db索引
     * @param key     键
     * @param client  redis客户端
     */
    public static void keyValue(ShellRedisKey node, Integer dbIndex, String key, ShellRedisClient client) {
        // string
        if (node.isStringKey()) {
            String value = client.get(dbIndex, key);
            node.valueOfString(value);
        } else if (node.isListKey()) {// list
            List<String> value = client.lrange(dbIndex, key);
            node.valueOfList(value);
        } else if (node.isHashKey()) {// hash
            Map<String, String> value = client.hgetAll(dbIndex, key);
            node.valueOfHash(value);
        } else if (node.isSetKey()) {// set
            Set<String> value = client.smembers(dbIndex, key);
            node.valueOfSet(value);
        } else if (node.isZSetKey()) { // zset
            List<String> value = client.zrange(dbIndex, key);
            List<Double> scores = client.zmscore_ext(dbIndex, key, ArrayUtil.toArray(value, String.class));
            node.valueOfZSet(value, scores);
        } else if (node.isStreamKey()) {// stream
            node.valueOfStream(client.xrange(dbIndex, key));
        }
    }

    /**
     * 获取键对象信息
     *
     * @param node    redis键
     * @param dbIndex db索引
     * @param key     键
     * @param client  redis客户端
     */
    public static void keyObject(ShellRedisKey node, Integer dbIndex, String key, ShellRedisClient client) {
        Long objectRefcount = client.objectRefcount(dbIndex, key);
        Long objectIdletime = client.objectIdletime(dbIndex, key);
        String objectEncoding = client.objectEncoding(dbIndex, key);
        node.setObjectIdletime(objectIdletime);
        node.setObjectRefcount(objectRefcount);
        node.setObjectedEncoding(objectEncoding);
    }

    /**
     * 扫描键
     *
     * @param dbIndex 都不索引
     * @param cursor  光标
     * @param params  参数
     * @param client  redis客户端
     * @return 扫描结果
     */
    public static ShellRedisScanResult scanKeys(Integer dbIndex, String cursor, ScanParams params, ShellRedisClient client) {
        // 开始时间
        long start = System.currentTimeMillis();
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        ShellRedisScanResult scanResult = new ShellRedisScanResult();
        if (result == null) {
            return scanResult;
        }
        // 设置游标
        scanResult.setCursor(result.getCursor());
        // 获取游标结果
        List<String> keys = result.getResult();
        // 批量获取键类型
        List<ShellRedisKeyType> types = keyType(dbIndex, keys, client);
        if (types == null) {
            throw new RuntimeException(I18nResourceBundle.i18nString("get", "keyType", "fail"));
        }
        // 结束时间
        long end = System.currentTimeMillis();
        // 加载耗时
        short loadTime = (short) (end - start);
        // 处理键
        List<ShellRedisKey> redisKeys = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            ShellRedisKey redisKey = initKey(dbIndex, keys.get(i), types.get(i));
            redisKey.setLoadTime(loadTime);
            redisKeys.add(redisKey);
        }
        scanResult.setKeys(redisKeys);
        return scanResult;
    }

    /**
     * 扫描键，简单模式
     *
     * @param dbIndex 都不索引
     * @param cursor  光标
     * @param params  参数
     * @param client  redis客户端
     * @return 扫描结果
     */
    public static ShellRedisScanSimpleResult scanKeysSimple(Integer dbIndex, String cursor, ScanParams params, ShellRedisClient client) {
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        ShellRedisScanSimpleResult scanResult = new ShellRedisScanSimpleResult();
        if (result == null) {
            return scanResult;
        }
        // 设置游标
        scanResult.setCursor(result.getCursor());
        // 获取游标结果
        List<String> keys = result.getResult();
        scanResult.setKeys(keys);
        return scanResult;
    }

    /**
     * 扫描键，简单限制
     *
     * @param dbIndex 都不索引
     * @param client  redis客户端
     * @param pattern 键模式
     * @param limit   最大限制
     * @return 键列表
     */
    public static List<String> scanKeys(Integer dbIndex, ShellRedisClient client, String pattern, int limit) {
        ScanParams params = new ScanParams();
        params.count(limit);
        params.match(pattern);
        ShellRedisScanSimpleResult result = scanKeysSimple(dbIndex, null, params, client);
        return result.getKeys();
    }

    /**
     * 统计键
     *
     * @param dbIndex 都不索引
     * @param cursor  光标
     * @param params  参数
     * @param client  redis客户端
     * @return 本次键数量
     */
    public static ShellRedisCountResult countKeys(Integer dbIndex, String cursor, ScanParams params, ShellRedisClient client) {
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        ShellRedisCountResult countResult = new ShellRedisCountResult();
        if (result == null) {
            return countResult;
        }
        // 设置游标
        countResult.setCursor(result.getCursor());
        countResult.setCount(CollectionUtil.size(result.getResult()));
        return countResult;
    }

    /**
     * 删除键
     *
     * @param dbIndex 都不索引
     * @param cursor  光标
     * @param params  参数
     * @param client  redis客户端
     * @return 本次键数量
     */
    public static ShellRedisDeleteResult deleteKeys(Integer dbIndex, String cursor, ScanParams params, ShellRedisClient client) {
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        ShellRedisDeleteResult countResult = new ShellRedisDeleteResult();
        if (result == null) {
            return countResult;
        }
        // 设置游标
        countResult.setCursor(result.getCursor());
        countResult.setCount(CollectionUtil.size(result.getResult()));
        client.del(dbIndex, result.getResult());
        return countResult;
    }

    /**
     * 获取所有键
     *
     * @param dbIndex db索引
     * @param pattern 键模式
     * @param client  redis客户端
     * @return 键列表
     */
    public static List<ShellRedisKey> allKeys(Integer dbIndex, String pattern, ShellRedisClient client) {
        // 开始时间
        long start = System.currentTimeMillis();
        // 获取键列表
        Set<String> keys = client.keys(dbIndex, pattern);
        // 批量获取键类型
        List<ShellRedisKeyType> types = keyType(dbIndex, keys, client);
        if (types == null) {
            throw new RuntimeException(I18nResourceBundle.i18nString("base.get", "base.keyType", "base.fail"));
        }
        // 结束时间
        long end = System.currentTimeMillis();
        // 加载耗时
        short loadTime = (short) (end - start);
        // 处理键
        List<ShellRedisKey> redisKeys = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            ShellRedisKey redisKey = initKey(dbIndex, CollectionUtil.get(keys, i), types.get(i));
            redisKey.setLoadTime(loadTime);
            redisKeys.add(redisKey);
        }
        return redisKeys;
    }

    /**
     * 获取键
     *
     * @param dbIndex   db索引
     * @param key       键
     * @param ttl       是否获取ttl
     * @param loadValue 是否加载值
     * @param client    redis客户端
     * @return redis键
     */
    public static ShellRedisKey getKey(int dbIndex, String key, boolean ttl, boolean loadValue, ShellRedisClient client) {
        return getKey(dbIndex, key, ttl, false, loadValue, client);
    }

    /**
     * 获取键
     *
     * @param dbIndex        db索引
     * @param key            键
     * @param ttl            是否获取ttl
     * @param objectEncoding 是否获取对象编码
     * @param loadValue      是否加载值
     * @param client         redis客户端
     * @return redis键
     */
    public static ShellRedisKey getKey(int dbIndex, String key, boolean ttl, boolean objectEncoding, boolean loadValue, ShellRedisClient client) {
        // 开始时间
        long start = System.currentTimeMillis();
        // 初始化键
        ShellRedisKey redisKey = initKey(dbIndex, key, keyType(dbIndex, key, client));
        if (redisKey == null) {
            return null;
        }
        // ttl
        if (ttl) {
            redisKey.setTtl(client.ttl(dbIndex, key));
        }
        // 对象编码
        if (objectEncoding) {
            redisKey.setObjectedEncoding(client.objectEncoding(dbIndex, key));
        }
        // 值
        if (loadValue) {
            keyValue(redisKey, dbIndex, key, client);
        }
        // 结束时间
        long end = System.currentTimeMillis();
        // 加载耗时
        long loadTime = end - start;
        redisKey.setLoadTime((short) loadTime);
        return redisKey;
    }

    /**
     * 初始化键
     *
     * @param dbIndex db索引
     * @param key     键名称
     * @param type    键类型
     * @return redis键
     */
    public static ShellRedisKey initKey(int dbIndex, String key, ShellRedisKeyType type) {
        // 创建键
        ShellRedisKey redisKey = null;
        switch (type) {
            case ShellRedisKeyType.STRING, ShellRedisKeyType.LIST, ShellRedisKeyType.SET, ShellRedisKeyType.ZSET, ShellRedisKeyType.HASH,
                 ShellRedisKeyType.STREAM -> redisKey = new ShellRedisKey();
            case null, default -> JulLog.warn("type:{} is not support!", type);
        }
        // 处理键
        if (redisKey != null) {
            redisKey.setKey(key);
            redisKey.type(type);
            redisKey.setDbIndex(dbIndex);
        }
        return redisKey;
    }

    /**
     * 获取键类型
     *
     * @param dbIndex 数据库索引
     * @param key     键
     * @param client  redis客户端
     * @return 结果
     */
    public static ShellRedisKeyType keyType(Integer dbIndex, String key, ShellRedisClient client) {
        try {
            String type = client.type(dbIndex, key);
            return ShellRedisKeyType.valueOfType(type);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取键统计值
     *
     * @param dbIndex 数据库索引
     * @param key     键
     * @param client  redis客户端
     * @return 统计值
     */
    public static Long count(Integer dbIndex, String key, ShellRedisClient client) {
        try {
            return client.pfcount(dbIndex, key);
        } catch (Exception ex) {
            if (!StringUtil.containsAny(ex.getMessage(), "WRONGTYPE Key is not a valid HyperLogLog string value")) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 是否统计值
     *
     * @param dbIndex 数据库索引
     * @param key     键
     * @param client  redis客户端
     * @return 结果
     */
    public static boolean isHylog(Integer dbIndex, String key, ShellRedisClient client) {
        try {
            client.pfcount(dbIndex, key);
            return true;
        } catch (Exception ex) {
            if (!StringUtil.containsAny(ex.getMessage(), "WRONGTYPE Key is not a valid HyperLogLog string value")) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取键类型
     *
     * @param dbIndex 数据库索引
     * @param keys    键
     * @param client  redis客户端
     * @return 结果
     */
    public static List<ShellRedisKeyType> keyType(Integer dbIndex, Collection<String> keys, ShellRedisClient client) {
        try {
            List<String> types = client.typeMulti(dbIndex, keys);
            if (types.size() == keys.size()) {
                List<ShellRedisKeyType> list = new ArrayList<>();
                for (String type : types) {
                    list.add(ShellRedisKeyType.valueOfType(type));
                }
                return list;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取键列表
     *
     * @param client       客户端
     * @param dbIndex      db索引
     * @param pattern      模式
     * @param existingKeys 已存在的键列表
     * @param limit        限制数量
     * @return 键列表
     */
    public static List<ShellRedisKey> getKeys(ShellRedisClient client, int dbIndex, String pattern, List<String> existingKeys, int limit) {
        // 当前光标
        String cursor = null;
        // 扫描参数
        ScanParams params = new ScanParams();
        params.match(pattern);
        // 全部节点
        List<ShellRedisKey> allKeys = new ArrayList<>();
        // 数据计数
        int count = 0;
        // 扫描数据
        while (true) {
            // 设置加载数量
            if (limit > 0) {
                params.count(Math.min(limit - count, 1000));
            } else {
                params.count(1000);
            }
            // 扫描数据
            ShellRedisScanResult result = ShellRedisKeyUtil.scanKeys(dbIndex, cursor, params, client);
            // 添加到集合
            List<ShellRedisKey> keys = result.getKeys();
            if (CollectionUtil.isNotEmpty(keys)) {
                if (existingKeys.isEmpty()) {
                    allKeys.addAll(keys);
                    count += keys.size();
                } else {
                    for (ShellRedisKey key : keys) {
                        if (!existingKeys.contains(key.getKey())) {
                            allKeys.add(key);
                            count++;
                        }
                    }
                }
            }
            // 查询结束
            if (result.isFinish() || (limit > 0 && count >= limit)) {
                break;
            }
            // 更新光标
            cursor = result.getCursor();
        }
        if (limit > 0 && allKeys.size() > limit) {
            return allKeys.subList(0, limit);
        }
        return allKeys;
    }
}
