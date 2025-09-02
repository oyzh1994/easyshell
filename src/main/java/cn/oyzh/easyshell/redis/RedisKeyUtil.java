package cn.oyzh.easyshell.redis;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.redis.RedisFilter;
import cn.oyzh.easyshell.redis.batch.RedisCountResult;
import cn.oyzh.easyshell.redis.batch.RedisDeleteResult;
import cn.oyzh.easyshell.redis.batch.RedisScanResult;
import cn.oyzh.easyshell.redis.batch.RedisScanSimpleResult;
import cn.oyzh.easyshell.redis.key.RedisHashValue;
import cn.oyzh.easyshell.redis.key.RedisKey;
import cn.oyzh.easyshell.redis.key.RedisListValue;
import cn.oyzh.easyshell.redis.key.RedisSetValue;
import cn.oyzh.easyshell.redis.key.RedisStreamValue;
import cn.oyzh.easyshell.redis.key.RedisStringValue;
import cn.oyzh.easyshell.redis.key.RedisZSetValue;
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

public class RedisKeyUtil {

    /**
     * 是否被过滤
     *
     * @param key     键名称
     * @param filters 过滤配置列表
     * @return 结果
     */
    public static boolean isFiltered(String key, List<RedisFilter> filters) {
        if (CollectionUtil.isEmpty(filters) || key == null) {
            return false;
        }
        // 匹配结果
        for (RedisFilter filter : filters) {
            // 未启用，不处理
            if (!filter.isEnable()) {
                continue;
            }
            // 模糊匹配
            if (filter.isPartMatch() && key.contains(filter.getKw())) {
                return true;
            }
            // 完全匹配
            if (key.equalsIgnoreCase(filter.getKw())) {
                return true;
            }
        }
        return false;
    }

    // /**
    //  * 序列化键
    //  *
    //  * @param redisKey redis键
    //  * @return 序列化内容
    //  */
    // public static String serializeNode(RedisKey redisKey) {
    //     return serializeNode(redisKey, true);
    // }

    /**
     * 序列化键
     *
     * @param redisKey redis键
     * @return 序列化内容
     */
    public static String serializeNode(RedisKey redisKey) {
        String result = null;
        // string
        if (redisKey.isStringKey()) {
            RedisStringValue stringValue = redisKey.asStringValue();
            // if (redisKey.isRawEncoding() && binaryAsHex) {
            //     return "0x'" + TextUtil.bytesToHexStr(stringValue.bytesValue()) + "'";
            // }
            result = JSONUtil.toJson(stringValue.getValue());
        } else if (redisKey.isListKey()) { // list
            RedisListValue value = redisKey.asListValue();
            List<RedisListValue.RedisListRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (RedisListValue.RedisListRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", row.getValue());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isSetKey()) {  // set
            RedisSetValue value = redisKey.asSetValue();
            List<RedisSetValue.RedisSetRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (RedisSetValue.RedisSetRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", row.getValue());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isZSetKey()) {   // zset
            RedisZSetValue value = redisKey.asZSetValue();
            List<RedisZSetValue.RedisZSetRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (RedisZSetValue.RedisZSetRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", row.getValue());
                map.put("score", row.getScore());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isStreamKey()) {  // stream
            RedisStreamValue value = redisKey.asStreamValue();
            List<RedisStreamValue.RedisStreamRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (RedisStreamValue.RedisStreamRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", row.getId());
                map.put("value", row.getValue());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isHashKey()) {   // hash
            RedisHashValue value = redisKey.asHashValue();
            List<RedisHashValue.RedisHashRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (RedisHashValue.RedisHashRow row : rows) {
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
    public static RedisKey deserializeNode(RedisKeyType type, String value) {
        // string
        if (type == RedisKeyType.STRING) {
            RedisKey node = new RedisKey();
            node.valueOfString(value == null ? "" : value);
            return node;
        }

        // list
        if (type == RedisKeyType.LIST) {
            RedisKey node = new RedisKey();
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
        if (type == RedisKeyType.SET) {
            RedisKey node = new RedisKey();
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
        if (type == RedisKeyType.ZSET) {
            RedisKey node = new RedisKey();
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
        if (type == RedisKeyType.HASH) {
            RedisKey node = new RedisKey();
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
        if (type == RedisKeyType.STREAM) {
            RedisKey node = new RedisKey();
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
    public static void createKey(RedisKey node, Integer dbIndex, RedisClient client) {
        if (node == null || client == null) {
            return;
        }
        String key = node.getKey();
        // string
        if (node.isStringKey()) {
            client.set(dbIndex, key, (String) node.asStringValue().getValue());
        } else if (node.isListKey()) {// list
            RedisListValue value = node.asListValue();
            List<RedisListValue.RedisListRow> rows = value.getValue();
            String[] arr;
            if (CollectionUtil.isEmpty(rows)) {
                arr = new String[]{""};
            } else {
                List<String> list = rows.parallelStream().map(RedisListValue.RedisListRow::getValue).collect(Collectors.toList());
                arr = ArrayUtil.toArray(list, String.class);
            }
            client.lpush(dbIndex, key, arr);
        } else if (node.isSetKey()) {// set
            RedisSetValue value = node.asSetValue();
            List<RedisSetValue.RedisSetRow> rows = value.getValue();
            String[] arr;
            if (CollectionUtil.isEmpty(rows)) {
                arr = new String[]{""};
            } else {
                List<String> list = rows.parallelStream().map(RedisSetValue.RedisSetRow::getValue).collect(Collectors.toList());
                arr = ArrayUtil.toArray(list, String.class);
            }
            client.sadd(dbIndex, key, arr);
        } else if (node.isZSetKey()) {// zset
            RedisZSetValue value = node.asZSetValue();
            List<RedisZSetValue.RedisZSetRow> rows = value.getValue();
            Map<String, Double> scoreMembers;
            if (CollectionUtil.isEmpty(rows)) {
                scoreMembers = Collections.emptyMap();
            } else {
                scoreMembers = new HashMap<>();
                for (RedisZSetValue.RedisZSetRow row : rows) {
                    scoreMembers.put(row.getValue(), row.getScore());
                }
            }
            client.zadd(dbIndex, key, scoreMembers);
        } else if (node.isHashKey()) {// hash
            RedisHashValue value = node.asHashValue();
            List<RedisHashValue.RedisHashRow> rows = value.getValue();
            Map<String, String> hash;
            if (CollectionUtil.isEmpty(rows)) {
                hash = Collections.emptyMap();
            } else {
                hash = new HashMap<>();
                for (RedisHashValue.RedisHashRow row : rows) {
                    hash.put(row.getField(), row.getValue());
                }
            }
            client.hmset(dbIndex, key, hash);
        } else if (node.isStreamKey()) {// stream
            RedisStreamValue value = node.asStreamValue();
            List<RedisStreamValue.RedisStreamRow> rows = value.getValue();
            if (CollectionUtil.isNotEmpty(rows)) {
                for (RedisStreamValue.RedisStreamRow row : rows) {
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
    public static void keyValue(RedisKey node, Integer dbIndex, String key, RedisClient client) {
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
    public static void keyObject(RedisKey node, Integer dbIndex, String key, RedisClient client) {
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
    public static RedisScanResult scanKeys(Integer dbIndex, String cursor, ScanParams params, RedisClient client) {
        // 开始时间
        long start = System.currentTimeMillis();
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        RedisScanResult scanResult = new RedisScanResult();
        if (result == null) {
            return scanResult;
        }
        // 设置游标
        scanResult.setCursor(result.getCursor());
        // 获取游标结果
        List<String> keys = result.getResult();
        // 批量获取键类型
        List<RedisKeyType> types = keyType(dbIndex, keys, client);
        if (types == null) {
            throw new RuntimeException(I18nResourceBundle.i18nString("get", "keyType", "fail"));
        }
        // 结束时间
        long end = System.currentTimeMillis();
        // 加载耗时
        short loadTime = (short) (end - start);
        // 处理键
        List<RedisKey> redisKeys = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            RedisKey redisKey = initKey(dbIndex, keys.get(i), types.get(i));
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
    public static RedisScanSimpleResult scanKeysSimple(Integer dbIndex, String cursor, ScanParams params, RedisClient client) {
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        RedisScanSimpleResult scanResult = new RedisScanSimpleResult();
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
    public static List<String> scanKeys(Integer dbIndex, RedisClient client, String pattern, int limit) {
        ScanParams params = new ScanParams();
        params.count(limit);
        params.match(pattern);
        RedisScanSimpleResult result = scanKeysSimple(dbIndex, null, params, client);
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
    public static RedisCountResult countKeys(Integer dbIndex, String cursor, ScanParams params, RedisClient client) {
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        RedisCountResult countResult = new RedisCountResult();
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
    public static RedisDeleteResult deleteKeys(Integer dbIndex, String cursor, ScanParams params, RedisClient client) {
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        RedisDeleteResult countResult = new RedisDeleteResult();
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
    public static List<RedisKey> allKeys(Integer dbIndex, String pattern, RedisClient client) {
        // 开始时间
        long start = System.currentTimeMillis();
        // 获取键列表
        Set<String> keys = client.keys(dbIndex, pattern);
        // 批量获取键类型
        List<RedisKeyType> types = keyType(dbIndex, keys, client);
        if (types == null) {
            throw new RuntimeException(I18nResourceBundle.i18nString("base.get", "base.keyType", "base.fail"));
        }
        // 结束时间
        long end = System.currentTimeMillis();
        // 加载耗时
        short loadTime = (short) (end - start);
        // 处理键
        List<RedisKey> redisKeys = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            RedisKey redisKey = initKey(dbIndex, CollectionUtil.get(keys, i), types.get(i));
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
    public static RedisKey getKey(int dbIndex, String key, boolean ttl, boolean loadValue, RedisClient client) {
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
    public static RedisKey getKey(int dbIndex, String key, boolean ttl, boolean objectEncoding, boolean loadValue, RedisClient client) {
        // 开始时间
        long start = System.currentTimeMillis();
        // 初始化键
        RedisKey redisKey = initKey(dbIndex, key, keyType(dbIndex, key, client));
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
    public static RedisKey initKey(int dbIndex, String key, RedisKeyType type) {
        // 创建键
        RedisKey redisKey = null;
        switch (type) {
            case RedisKeyType.STRING, RedisKeyType.LIST, RedisKeyType.SET, RedisKeyType.ZSET, RedisKeyType.HASH,
                 RedisKeyType.STREAM -> redisKey = new RedisKey();
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
    public static RedisKeyType keyType(Integer dbIndex, String key, RedisClient client) {
        try {
            String type = client.type(dbIndex, key);
            return RedisKeyType.valueOfType(type);
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
    public static Long count(Integer dbIndex, String key, RedisClient client) {
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
    public static boolean isHylog(Integer dbIndex, String key, RedisClient client) {
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
    public static List<RedisKeyType> keyType(Integer dbIndex, Collection<String> keys, RedisClient client) {
        try {
            List<String> types = client.typeMulti(dbIndex, keys);
            if (types.size() == keys.size()) {
                List<RedisKeyType> list = new ArrayList<>();
                for (String type : types) {
                    list.add(RedisKeyType.valueOfType(type));
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
    public static List<RedisKey> getKeys(RedisClient client, int dbIndex, String pattern, List<String> existingKeys, int limit) {
        // 当前光标
        String cursor = null;
        // 扫描参数
        ScanParams params = new ScanParams();
        params.match(pattern);
        // 全部节点
        List<RedisKey> allKeys = new ArrayList<>();
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
            RedisScanResult result = RedisKeyUtil.scanKeys(dbIndex, cursor, params, client);
            // 添加到集合
            List<RedisKey> keys = result.getKeys();
            if (CollectionUtil.isNotEmpty(keys)) {
                if (existingKeys.isEmpty()) {
                    allKeys.addAll(keys);
                    count += keys.size();
                } else {
                    for (RedisKey key : keys) {
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
