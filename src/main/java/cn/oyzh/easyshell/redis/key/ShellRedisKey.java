package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.resps.StreamEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis键
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisKey implements Comparable<ShellRedisKey>, ObjectCopier<ShellRedisKey> {

    /**
     * db索引
     */
    private int dbIndex;

    /**
     * 加载耗时
     */
    private short loadTime;

    /**
     * ttl值
     */
    private Long ttl;

    /**
     * key名称
     */
    private String key;

    /**
     * 键类型
     */
    private ShellRedisKeyType type;

    /**
     * 空闲时间
     */
    private Long objectIdletime;

    /**
     * 引用数量
     */
    private Long objectRefcount;

    /**
     * 编码值
     */
    private String objectedEncoding;

    /**
     * 设置键类型
     *
     * @param type 键类型
     */
    public void type(ShellRedisKeyType type) {
        this.type = type;
    }

    /**
     * 是否string键
     *
     * @return 结果
     */
    public boolean isStringKey() {
        return ShellRedisKeyType.STRING == this.type;
    }

    /**
     * 是否set键
     *
     * @return 结果
     */
    public boolean isSetKey() {
        return ShellRedisKeyType.SET == this.type;
    }

    /**
     * 是否zset键
     *
     * @return 结果
     */
    public boolean isZSetKey() {
        return ShellRedisKeyType.ZSET == this.type;
    }

    /**
     * 是否list键
     *
     * @return 结果
     */
    public boolean isListKey() {
        return ShellRedisKeyType.LIST == this.type;
    }

    /**
     * 是否hash键
     *
     * @return 结果
     */
    public boolean isHashKey() {
        return ShellRedisKeyType.HASH == this.type;
    }

    /**
     * 是否stream键
     *
     * @return 结果
     */
    public boolean isStreamKey() {
        return ShellRedisKeyType.STREAM == this.type;
    }

    /**
     * 是否json键
     *
     * @return 结果
     */
    public boolean isJsonKey() {
        return ShellRedisKeyType.JSON == this.type;
    }

    /**
     * 获取数据库索引
     *
     * @return 数据库索引
     */
    public int getDbIndex() {
        return dbIndex;
    }

    /**
     * 设置数据库索引
     *
     * @param dbIndex 数据库索引
     */
    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    /**
     * 获取加载耗时
     *
     * @return 加载耗时
     */
    public short getLoadTime() {
        return loadTime;
    }

    /**
     * 设置加载耗时
     *
     * @param loadTime 加载耗时
     */
    public void setLoadTime(short loadTime) {
        this.loadTime = loadTime;
    }

    /**
     * 获取ttl值
     *
     * @return ttl值
     */
    public Long getTtl() {
        return ttl;
    }

    /**
     * 设置ttl值
     *
     * @param ttl ttl值
     */
    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    /**
     * 获取键名称
     *
     * @return 键名称
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置键名称
     *
     * @param key 键名称
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 获取键类型
     *
     * @return 键类型
     */
    public ShellRedisKeyType getType() {
        return type;
    }

    /**
     * 设置键类型
     *
     * @param type 键类型
     */
    public void setType(ShellRedisKeyType type) {
        this.type = type;
    }

    /**
     * 获取空闲时间
     *
     * @return 空闲时间
     */
    public Long getObjectIdletime() {
        return objectIdletime;
    }

    /**
     * 设置空闲时间
     *
     * @param objectIdletime 空闲时间
     */
    public void setObjectIdletime(Long objectIdletime) {
        this.objectIdletime = objectIdletime;
    }

    /**
     * 获取引用数量
     *
     * @return 引用数量
     */
    public Long getObjectRefcount() {
        return objectRefcount;
    }

    /**
     * 设置引用数量
     *
     * @param objectRefcount 引用数量
     */
    public void setObjectRefcount(Long objectRefcount) {
        this.objectRefcount = objectRefcount;
    }

    /**
     * 获取编码值
     *
     * @return 编码值
     */
    public String getObjectedEncoding() {
        return objectedEncoding;
    }

    /**
     * 设置编码值
     *
     * @param objectedEncoding 编码值
     */
    public void setObjectedEncoding(String objectedEncoding) {
        this.objectedEncoding = objectedEncoding;
    }

    /**
     * 获取键值
     *
     * @return 键值
     */
    public ShellRedisKeyValue<?> getValue() {
        return value;
    }

    /**
     * 设置键值
     *
     * @param value 键值
     */
    public void setValue(ShellRedisKeyValue<?> value) {
        this.value = value;
    }

    @Override
    public int compareTo(ShellRedisKey node) {
        if (node == null || node.getKey() == null) {
            return -1;
        }
        return this.getKey().compareToIgnoreCase(node.getKey());
    }

    /**
     * 获取空闲时间字符串
     *
     * @return 空闲时间字符串
     */
    public String objectIdletimeString() {
        return this.objectIdletime == null ? "N/A" : this.objectIdletime + "";
    }

    /**
     * 获取字符编码字符串
     *
     * @return 字符编码字符串
     */
    public String objectedEncodingString() {
        return this.objectedEncoding == null ? "N/A" : this.objectedEncoding;
    }

    /**
     * 获取引用计数字符串
     *
     * @return 引用计数字符串
     */
    public String objectRefcountString() {
        return this.objectRefcount == null || this.objectRefcount == Integer.MAX_VALUE ? "N/A" : this.objectRefcount + "";
    }

    /**
     * 是否raw格式
     *
     * @return 结果
     */
    public boolean isRawEncoding() {
        return StringUtil.equalsIgnoreCase("raw", this.objectedEncoding);
    }

    /**
     * 获取键的二进制数据
     *
     * @return 键的二进制数据
     */
    public byte[] keyBinary() {
        return this.key == null ? null : this.key.getBytes();
    }

    /**
     * 键值
     */
    private ShellRedisKeyValue<?> value;

    /**
     * 以set方式设置键值
     *
     * @param members 成员集合
     */
    public void valueOfSet(Set<String> members) {
        this.setValue(ShellRedisSetValue.valueOf(members));
    }

    /**
     * 以zset方式设置键值
     *
     * @param members 成员集合
     * @param scores  分数集合
     */
    public void valueOfZSet(List<String> members, List<Double> scores) {
        this.setValue(ShellRedisZSetValue.valueOf(members, scores));
    }

    /**
     * 以地理坐标方式设置键值
     *
     * @param members     成员集合
     * @param coordinates 坐标集合
     */
    public void valueOfCoordinates(List<String> members, List<GeoCoordinate> coordinates) {
        this.setValue(ShellRedisZSetValue.valueOfCoordinates(members, coordinates));
    }

    /**
     * 以hash方式设置键值
     *
     * @param values 键值映射
     */
    public void valueOfHash(Map<String, String> values) {
        this.setValue(ShellRedisHashValue.valueOf(values));
    }

    /**
     * 以list方式设置键值
     *
     * @param elements 元素集合
     */
    public void valueOfList(List<String> elements) {
        this.setValue(ShellRedisListValue.valueOf(elements));
    }

    /**
     * 以stream方式设置键值
     *
     * @param entries 流条目集合
     */
    public void valueOfStream(List<StreamEntry> entries) {
        this.setValue(ShellRedisStreamValue.valueOf(entries));
    }

    /**
     * 以字符串方式设置键值
     *
     * @param value 字节数组
     */
    public void valueOfString(byte[] value) {
        this.setValue(ShellRedisStringValue.valueOf(value));
    }

    /**
     * 以字符串方式设置键值
     *
     * @param value 字符串
     */
    public void valueOfString(String value) {
        this.setValue(ShellRedisStringValue.valueOf(value));
    }

    /**
     * 以json方式设置键值
     *
     * @param value json字符串
     */
    public void valueOfJson(String value) {
        this.setValue(ShellRedisJsonValue.valueOf(value));
    }

    /**
     * 以字节数组方式设置键值
     *
     * @param value 字节数组
     */
    public void valueOfBytes(byte[] value) {
        this.setValue(ShellRedisStringValue.valueOf(value));
    }

    /**
     * 转换为set值
     *
     * @return set值
     */
    public ShellRedisSetValue asSetValue() {
        return (ShellRedisSetValue) this.getValue();
    }

    /**
     * 转换为zset值
     *
     * @return zset值
     */
    public ShellRedisZSetValue asZSetValue() {
        return (ShellRedisZSetValue) this.getValue();
    }

    /**
     * 转换为list值
     *
     * @return list值
     */
    public ShellRedisListValue asListValue() {
        return (ShellRedisListValue) this.getValue();
    }

    /**
     * 转换为hash值
     *
     * @return hash值
     */
    public ShellRedisHashValue asHashValue() {
        return (ShellRedisHashValue) this.getValue();
    }

    /**
     * 转换为字符串值
     *
     * @return 字符串值
     */
    public ShellRedisStringValue asStringValue() {
        ShellRedisKeyValue<?> value = this.getValue();
        if (value == null) {
            value = new ShellRedisStringValue();
            this.setValue(value);
        }
        return (ShellRedisStringValue) value;
    }

    /**
     * 转换为json值
     *
     * @return json值
     */
    public ShellRedisJsonValue asJsonValue() {
        ShellRedisKeyValue<?> value = this.getValue();
        if (value == null) {
            value = new ShellRedisJsonValue();
            this.setValue(value);
        }
        return (ShellRedisJsonValue) value;
    }

    /**
     * 转换为stream值
     *
     * @return stream值
     */
    public ShellRedisStreamValue asStreamValue() {
        return (ShellRedisStreamValue) this.getValue();
    }

    /**
     * 获取键类型名称
     *
     * @return 键类型名称
     */
    public String typeName() {
        return this.type.name();
    }

    @Override
    public void copy(ShellRedisKey t1) {
        this.setKey(t1.getKey());
        this.setTtl(t1.getTtl());
        this.setType(t1.getType());
        this.setValue(t1.getValue());
        this.setDbIndex(t1.getDbIndex());
        this.setObjectIdletime(t1.getObjectIdletime());
        this.setObjectRefcount(t1.getObjectRefcount());
        this.setObjectedEncoding(t1.getObjectedEncoding());
    }
}
