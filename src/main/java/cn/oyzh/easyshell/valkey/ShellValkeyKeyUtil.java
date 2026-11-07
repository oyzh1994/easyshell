package cn.oyzh.easyshell.valkey;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.valkey.batch.ShellValkeyCountResult;
import cn.oyzh.easyshell.valkey.batch.ShellValkeyDeleteResult;
import cn.oyzh.easyshell.valkey.batch.ShellValkeyScanResult;
import cn.oyzh.easyshell.valkey.batch.ShellValkeyScanSimpleResult;
import cn.oyzh.easyshell.valkey.key.ShellValkeyHashValue;
import cn.oyzh.easyshell.valkey.key.ShellValkeyJsonValue;
import cn.oyzh.easyshell.valkey.key.ShellValkeyKey;
import cn.oyzh.easyshell.valkey.key.ShellValkeyListValue;
import cn.oyzh.easyshell.valkey.key.ShellValkeySetValue;
import cn.oyzh.easyshell.valkey.key.ShellValkeyStreamValue;
import cn.oyzh.easyshell.valkey.key.ShellValkeyStringValue;
import cn.oyzh.easyshell.valkey.key.ShellValkeyZSetValue;
import cn.oyzh.i18n.I18nResourceBundle;
import cn.oyzh.i18n.I18nHelper;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.valkey.StreamEntryID;
import io.valkey.params.ScanParams;
import io.valkey.resps.ScanResult;
import io.valkey.resps.StreamEntry;

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
 * @since 2025-09-01
 */

public class ShellValkeyKeyUtil {

    // /**
    //  * 是否被过滤
    //  *
    //  * @param key     键名称
    //  * @param filters 过滤配置列表
    //  * @return 结果
    //  */
    // public static boolean isFiltered(String key, List<ValkeyFilter> filters) {
    //     if (CollectionUtil.isEmpty(filters) || key == null) {
    //         return false;
    //     }
    //     // 匹配结果
    //     for (ValkeyFilter filter : filters) {
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
    // public static String serializeNode(ShellValkeyKey redisKey) {
    //     return serializeNode(redisKey, true);
    // }

    /**
     * 序列化键
     *
     * @param redisKey redis键
     * @return 序列化内容
     */
    public static String serializeNode(ShellValkeyKey redisKey) {
        String result = null;
        // string
        if (redisKey.isStringKey()) {
            ShellValkeyStringValue stringValue = redisKey.asStringValue();
            if (redisKey.isRawEncoding()) {
                return "0x'" + HexUtil.bytesToHex(stringValue.bytesValue()) + "'";
            }
            if (stringValue.getValue() instanceof String s) {
                result = s;
            } else if (stringValue.getValue() instanceof byte[] bytes) {
                result = new String(bytes);
            }
        } else if (redisKey.isJsonKey()) {
            ShellValkeyJsonValue stringValue = redisKey.asJsonValue();
            result = stringValue.getValue();
        } else if (redisKey.isListKey()) { // list
            ShellValkeyListValue value = redisKey.asListValue();
            List<ShellValkeyListValue.ValkeyListRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (ShellValkeyListValue.ValkeyListRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", row.getValue());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isSetKey()) {  // set
            ShellValkeySetValue value = redisKey.asSetValue();
            List<ShellValkeySetValue.ValkeySetRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (ShellValkeySetValue.ValkeySetRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", row.getValue());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isZSetKey()) {   // zset
            ShellValkeyZSetValue value = redisKey.asZSetValue();
            List<ShellValkeyZSetValue.ValkeyZSetRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (ShellValkeyZSetValue.ValkeyZSetRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", row.getValue());
                map.put("score", row.getScore());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isStreamKey()) {  // stream
            ShellValkeyStreamValue value = redisKey.asStreamValue();
            List<ShellValkeyStreamValue.ValkeyStreamRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (ShellValkeyStreamValue.ValkeyStreamRow row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", row.getId());
                map.put("value", row.getValue());
                list.add(map);
            }
            result = JSONUtil.toJson(list);
        } else if (redisKey.isHashKey()) {   // hash
            ShellValkeyHashValue value = redisKey.asHashValue();
            List<ShellValkeyHashValue.ValkeyHashRow> rows = value.getValue();
            if (CollectionUtil.isEmpty(rows)) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>(rows.size());
            for (ShellValkeyHashValue.ValkeyHashRow row : rows) {
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
    public static ShellValkeyKey deserializeNode(ShellValkeyKeyType type, String value) {
        // string
        if (type == ShellValkeyKeyType.STRING) {
            ShellValkeyKey redisKey = new ShellValkeyKey();
            redisKey.setType(ShellValkeyKeyType.STRING);
            if (StringUtil.startWith(value, "0x'")) {
                value = value.substring(3, value.length() - 1);
                byte[] bytes = HexUtil.decodeHexStr(value);
                redisKey.valueOfString(bytes);
            } else {
                redisKey.valueOfString(value == null ? "" : value);
            }
            return redisKey;
        }

        // json
        if (type == ShellValkeyKeyType.JSON) {
            ShellValkeyKey redisKey = new ShellValkeyKey();
            redisKey.setType(ShellValkeyKeyType.JSON);
            redisKey.valueOfJson(value == null ? "" : value);
            return redisKey;
        }

        // list
        if (type == ShellValkeyKeyType.LIST) {
            ShellValkeyKey redisKey = new ShellValkeyKey();
            redisKey.setType(ShellValkeyKeyType.LIST);
            List<String> list = new ArrayList<>();
            if (StringUtil.isNotBlank(value)) {
                JSONArray array = JSONUtil.parseArray(value);
                for (int i = 0; i < array.size(); i++) {
                    list.add(array.getJSONObject(i).getString("value"));
                }
            }
            redisKey.valueOfList(list);
            return redisKey;
        }

        // set
        if (type == ShellValkeyKeyType.SET) {
            ShellValkeyKey redisKey = new ShellValkeyKey();
            redisKey.setType(ShellValkeyKeyType.SET);
            Set<String> list = new HashSet<>();
            if (StringUtil.isNotBlank(value)) {
                JSONArray array = JSONUtil.parseArray(value);
                for (int i = 0; i < array.size(); i++) {
                    list.add(array.getJSONObject(i).getString("value"));
                }
            }
            redisKey.valueOfSet(list);
            return redisKey;
        }

        // zset
        if (type == ShellValkeyKeyType.ZSET) {
            ShellValkeyKey redisKey = new ShellValkeyKey();
            redisKey.setType(ShellValkeyKeyType.ZSET);
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
            redisKey.valueOfZSet(list1, list2);
            return redisKey;
        }

        // hash
        if (type == ShellValkeyKeyType.HASH) {
            ShellValkeyKey redisKey = new ShellValkeyKey();
            redisKey.setType(ShellValkeyKeyType.HASH);
            Map<String, String> map = new HashMap<>();
            if (StringUtil.isNotBlank(value)) {
                JSONArray array = JSONUtil.parseArray(value);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    map.put(object.getString("field"), object.getString("value"));
                }
            }
            redisKey.valueOfHash(map);
            return redisKey;
        }

        // stream
        if (type == ShellValkeyKeyType.STREAM) {
            ShellValkeyKey redisKey = new ShellValkeyKey();
            redisKey.setType(ShellValkeyKeyType.STREAM);
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
            redisKey.valueOfStream(list);
            return redisKey;
        }
        return null;
    }

    /**
     * 创建键
     *
     * @param redisKey redis键
     * @param dbIndex  db索引
     * @param client   redis客户端
     */
    public static void createKey(ShellValkeyKey redisKey, Integer dbIndex, ShellValkeyClient client) {
        if (redisKey == null || client == null) {
            return;
        }
        String key = redisKey.getKey();
        // string
        if (redisKey.isStringKey()) {
            client.set(dbIndex, key, (String) redisKey.asStringValue().getValue());
        } else if (redisKey.isJsonKey()) {
            client.jsonSet(dbIndex, key, redisKey.asJsonValue().getValue());
        } else if (redisKey.isListKey()) {// list
            ShellValkeyListValue value = redisKey.asListValue();
            List<ShellValkeyListValue.ValkeyListRow> rows = value.getValue();
            String[] arr;
            if (CollectionUtil.isEmpty(rows)) {
                arr = new String[]{""};
            } else {
                List<String> list = rows.parallelStream().map(ShellValkeyListValue.ValkeyListRow::getValue).collect(Collectors.toList());
                arr = ArrayUtil.toArray(list, String.class);
            }
            client.lpush(dbIndex, key, arr);
        } else if (redisKey.isSetKey()) {// set
            ShellValkeySetValue value = redisKey.asSetValue();
            List<ShellValkeySetValue.ValkeySetRow> rows = value.getValue();
            String[] arr;
            if (CollectionUtil.isEmpty(rows)) {
                arr = new String[]{""};
            } else {
                List<String> list = rows.parallelStream().map(ShellValkeySetValue.ValkeySetRow::getValue).collect(Collectors.toList());
                arr = ArrayUtil.toArray(list, String.class);
            }
            client.sadd(dbIndex, key, arr);
        } else if (redisKey.isZSetKey()) {// zset
            ShellValkeyZSetValue value = redisKey.asZSetValue();
            List<ShellValkeyZSetValue.ValkeyZSetRow> rows = value.getValue();
            Map<String, Double> scoreMembers;
            if (CollectionUtil.isEmpty(rows)) {
                scoreMembers = Collections.emptyMap();
            } else {
                scoreMembers = new HashMap<>();
                for (ShellValkeyZSetValue.ValkeyZSetRow row : rows) {
                    scoreMembers.put(row.getValue(), row.getScore());
                }
            }
            client.zadd(dbIndex, key, scoreMembers);
        } else if (redisKey.isHashKey()) {// hash
            ShellValkeyHashValue value = redisKey.asHashValue();
            List<ShellValkeyHashValue.ValkeyHashRow> rows = value.getValue();
            Map<String, String> hash;
            if (CollectionUtil.isEmpty(rows)) {
                hash = Collections.emptyMap();
            } else {
                hash = new HashMap<>();
                for (ShellValkeyHashValue.ValkeyHashRow row : rows) {
                    hash.put(row.getField(), row.getValue());
                }
            }
            client.hmset(dbIndex, key, hash);
        } else if (redisKey.isStreamKey()) {// stream
            ShellValkeyStreamValue value = redisKey.asStreamValue();
            List<ShellValkeyStreamValue.ValkeyStreamRow> rows = value.getValue();
            if (CollectionUtil.isNotEmpty(rows)) {
                for (ShellValkeyStreamValue.ValkeyStreamRow row : rows) {
                    client.xadd(dbIndex, key, row.getStreamId(), row.getFields());
                }
            }
        }
    }

    /**
     * 获取键值
     *
     * @param redisKey redis键
     * @param dbIndex  db索引
     * @param key      键
     * @param client   redis客户端
     */
    public static void keyValue(ShellValkeyKey redisKey, Integer dbIndex, String key, ShellValkeyClient client) {
        // string
        if (redisKey.isStringKey()) {
            String value = client.get(dbIndex, key);
            redisKey.valueOfString(value);
        } else if (redisKey.isJsonKey()) {
            String value = client.jsonGet(dbIndex, key);
            redisKey.valueOfJson(value);
        } else if (redisKey.isListKey()) {// list
            List<String> value = client.lrange(dbIndex, key);
            redisKey.valueOfList(value);
        } else if (redisKey.isHashKey()) {// hash
            Map<String, String> value = client.hgetAll(dbIndex, key);
            redisKey.valueOfHash(value);
        } else if (redisKey.isSetKey()) {// set
            Set<String> value = client.smembers(dbIndex, key);
            redisKey.valueOfSet(value);
        } else if (redisKey.isZSetKey()) { // zset
            List<String> value = client.zrange(dbIndex, key);
            List<Double> scores = client.zmscore_ext(dbIndex, key, ArrayUtil.toArray(value, String.class));
            redisKey.valueOfZSet(value, scores);
        } else if (redisKey.isStreamKey()) {// stream
            redisKey.valueOfStream(client.xrange(dbIndex, key));
        }
    }

    /**
     * 获取键对象信息
     *
     * @param redisKey redis键
     * @param dbIndex  db索引
     * @param key      键
     * @param client   redis客户端
     */
    public static void keyObject(ShellValkeyKey redisKey, Integer dbIndex, String key, ShellValkeyClient client) {
        Long objectRefcount = client.objectRefcount(dbIndex, key);
        Long objectIdletime = client.objectIdletime(dbIndex, key);
        String objectEncoding = client.objectEncoding(dbIndex, key);
        redisKey.setObjectIdletime(objectIdletime);
        redisKey.setObjectRefcount(objectRefcount);
        redisKey.setObjectedEncoding(objectEncoding);
    }

    /**
     * 扫描键
     *
     * @param dbIndex 数据库索引
     * @param cursor  光标
     * @param params  参数
     * @param client  redis客户端
     * @return 扫描结果
     */
    public static ShellValkeyScanResult scanKeys(Integer dbIndex, String cursor, ScanParams params, ShellValkeyClient client) {
        // 开始时间
        long start = System.currentTimeMillis();
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        ShellValkeyScanResult scanResult = new ShellValkeyScanResult();
        if (result == null) {
            return scanResult;
        }
        // 设置游标
        scanResult.setCursor(result.getCursor());
        // 获取游标结果
        List<String> keys = result.getResult();
        // 批量获取键类型
        List<ShellValkeyKeyType> types = keyType(dbIndex, keys, client);
        if (types == null) {
            throw new RuntimeException(I18nHelper.getKeyTypeFail());
        }
        // 结束时间
        long end = System.currentTimeMillis();
        // 加载耗时
        short loadTime = (short) (end - start);
        // 处理键
        List<ShellValkeyKey> redisKeys = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            ShellValkeyKey redisKey = initKey(dbIndex, keys.get(i), types.get(i));
            if (redisKey != null) {
                redisKey.setLoadTime(loadTime);
                redisKeys.add(redisKey);
            }
        }
        scanResult.setKeys(redisKeys);
        return scanResult;
    }

    /**
     * 扫描键，简单模式
     *
     * @param dbIndex 数据库索引
     * @param cursor  光标
     * @param params  参数
     * @param client  redis客户端
     * @return 扫描结果
     */
    public static ShellValkeyScanSimpleResult scanKeysSimple(Integer dbIndex, String cursor, ScanParams params, ShellValkeyClient client) {
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        ShellValkeyScanSimpleResult scanResult = new ShellValkeyScanSimpleResult();
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
     * @param dbIndex 数据库索引
     * @param client  redis客户端
     * @param pattern 键模式
     * @param limit   最大限制
     * @return 键列表
     */
    public static List<String> scanKeys(Integer dbIndex, ShellValkeyClient client, String pattern, int limit) {
        ScanParams params = new ScanParams();
        params.count(limit);
        params.match(pattern);
        ShellValkeyScanSimpleResult result = scanKeysSimple(dbIndex, null, params, client);
        return result.getKeys();
    }

    /**
     * 统计键
     *
     * @param dbIndex 数据库索引
     * @param cursor  光标
     * @param params  参数
     * @param client  redis客户端
     * @return 本次键数量
     */
    public static ShellValkeyCountResult countKeys(Integer dbIndex, String cursor, ScanParams params, ShellValkeyClient client) {
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        ShellValkeyCountResult countResult = new ShellValkeyCountResult();
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
     * @param dbIndex 数据库索引
     * @param cursor  光标
     * @param params  参数
     * @param client  redis客户端
     * @return 本次键数量
     */
    public static ShellValkeyDeleteResult deleteKeys(Integer dbIndex, String cursor, ScanParams params, ShellValkeyClient client) {
        // 扫描
        ScanResult<String> result = client.scan(dbIndex, cursor, params);
        ShellValkeyDeleteResult countResult = new ShellValkeyDeleteResult();
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
    public static List<ShellValkeyKey> allKeys(Integer dbIndex, String pattern, ShellValkeyClient client) {
        // 开始时间
        long start = System.currentTimeMillis();
        // 获取键列表
        Set<String> keys = client.keys(dbIndex, pattern);
        // 批量获取键类型
        List<ShellValkeyKeyType> types = keyType(dbIndex, keys, client);
        if (types == null) {
            throw new RuntimeException(I18nResourceBundle.i18nString("base.get", "base.keyType", "base.fail"));
        }
        // 结束时间
        long end = System.currentTimeMillis();
        // 加载耗时
        short loadTime = (short) (end - start);
        // 处理键
        List<ShellValkeyKey> redisKeys = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            ShellValkeyKey redisKey = initKey(dbIndex, CollectionUtil.get(keys, i), types.get(i));
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
    public static ShellValkeyKey getKey(int dbIndex, String key, boolean ttl, boolean loadValue, ShellValkeyClient client) {
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
    public static ShellValkeyKey getKey(int dbIndex, String key, boolean ttl, boolean objectEncoding, boolean loadValue, ShellValkeyClient client) {
        // 开始时间
        long start = System.currentTimeMillis();
        // 初始化键
        ShellValkeyKey redisKey = initKey(dbIndex, key, keyType(dbIndex, key, client));
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
    public static ShellValkeyKey initKey(int dbIndex, String key, ShellValkeyKeyType type) {
        // 创建键
        ShellValkeyKey redisKey = null;
        switch (type) {
            case ShellValkeyKeyType.STRING,
                 ShellValkeyKeyType.JSON,
                 ShellValkeyKeyType.LIST,
                 ShellValkeyKeyType.SET,
                 ShellValkeyKeyType.ZSET,
                 ShellValkeyKeyType.HASH,
                 ShellValkeyKeyType.STREAM -> redisKey = new ShellValkeyKey();
            case null, default -> JulLog.warn("type:{} is not support!", type);
        }
        // 处理键
        if (redisKey != null) {
            redisKey.type(type);
            redisKey.setKey(key);
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
    public static ShellValkeyKeyType keyType(Integer dbIndex, String key, ShellValkeyClient client) {
        try {
            String type = client.type(dbIndex, key);
            return ShellValkeyKeyType.valueOfType(type);
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
    public static Long count(Integer dbIndex, String key, ShellValkeyClient client) {
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
    public static boolean isHylog(Integer dbIndex, String key, ShellValkeyClient client) {
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
    public static List<ShellValkeyKeyType> keyType(Integer dbIndex, Collection<String> keys, ShellValkeyClient client) {
        try {
            List<String> types = client.typeMulti(dbIndex, keys);
            if (types.size() == keys.size()) {
                List<ShellValkeyKeyType> list = new ArrayList<>();
                for (String type : types) {
                    list.add(ShellValkeyKeyType.valueOfType(type));
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
    public static List<ShellValkeyKey> getKeys(ShellValkeyClient client, int dbIndex, String pattern, List<String> existingKeys, int limit) {
        // 当前光标
        String cursor = null;
        // 扫描参数
        ScanParams params = new ScanParams();
        params.match(pattern);
        // 全部节点
        List<ShellValkeyKey> allKeys = new ArrayList<>();
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
            ShellValkeyScanResult result = ShellValkeyKeyUtil.scanKeys(dbIndex, cursor, params, client);
            // 添加到集合
            List<ShellValkeyKey> keys = result.getKeys();
            if (CollectionUtil.isNotEmpty(keys)) {
                if (existingKeys.isEmpty()) {
                    allKeys.addAll(keys);
                    count += keys.size();
                } else {
                    for (ShellValkeyKey key : keys) {
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
