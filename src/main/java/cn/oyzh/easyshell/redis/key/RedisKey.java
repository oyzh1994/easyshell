package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.RedisKeyType;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.resps.StreamEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis键
 *
 * @author oyzh
 * @since 2023/6/16
 */
public class RedisKey implements Comparable<RedisKey>, ObjectCopier<RedisKey> {

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
    private RedisKeyType type;

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
    public void type(RedisKeyType type) {
        this.type = type;
    }

    /**
     * 是否string键
     *
     * @return 结果
     */
    public boolean isStringKey() {
        return RedisKeyType.STRING == this.type;
    }

    /**
     * 是否set键
     *
     * @return 结果
     */
    public boolean isSetKey() {
        return RedisKeyType.SET == this.type;
    }

    /**
     * 是否zset键
     *
     * @return 结果
     */
    public boolean isZSetKey() {
        return RedisKeyType.ZSET == this.type;
    }

    /**
     * 是否list键
     *
     * @return 结果
     */
    public boolean isListKey() {
        return RedisKeyType.LIST == this.type;
    }

    /**
     * 是否hash键
     *
     * @return 结果
     */
    public boolean isHashKey() {
        return RedisKeyType.HASH == this.type;
    }

    /**
     * 是否stream键
     *
     * @return 结果
     */
    public boolean isStreamKey() {
        return RedisKeyType.STREAM == this.type;
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    public short getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(short loadTime) {
        this.loadTime = loadTime;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public RedisKeyType getType() {
        return type;
    }

    public void setType(RedisKeyType type) {
        this.type = type;
    }

    public Long getObjectIdletime() {
        return objectIdletime;
    }

    public void setObjectIdletime(Long objectIdletime) {
        this.objectIdletime = objectIdletime;
    }

    public Long getObjectRefcount() {
        return objectRefcount;
    }

    public void setObjectRefcount(Long objectRefcount) {
        this.objectRefcount = objectRefcount;
    }

    public String getObjectedEncoding() {
        return objectedEncoding;
    }

    public void setObjectedEncoding(String objectedEncoding) {
        this.objectedEncoding = objectedEncoding;
    }

    public RedisKeyValue<?> getValue() {
        return value;
    }

    public void setValue(RedisKeyValue<?> value) {
        this.value = value;
    }

    @Override
    public int compareTo(RedisKey node) {
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

    private RedisKeyValue<?> value;

    public void valueOfSet(Set<String> members) {
        this.setValue(RedisSetValue.valueOf(members));
    }

    public void valueOfZSet(List<String> members, List<Double> scores) {
        this.setValue(RedisZSetValue.valueOf(members, scores));
    }

    public void valueOfCoordinates(List<String> members, List<GeoCoordinate> coordinates) {
        this.setValue(RedisZSetValue.valueOfCoordinates(members, coordinates));
    }

    public void valueOfHash(Map<String, String> values) {
        this.setValue(RedisHashValue.valueOf(values));
    }

    public void valueOfList(List<String> elements) {
        this.setValue(RedisListValue.valueOf(elements));
    }

    public void valueOfStream(List<StreamEntry> entries) {
        this.setValue(RedisStreamValue.valueOf(entries));
    }

    public void valueOfString(String value) {
        this.setValue(RedisStringValue.valueOf(value));
    }

    public void valueOfBytes(byte[] value) {
        this.setValue(RedisStringValue.valueOf(value));
    }

    public RedisSetValue asSetValue() {
        return (RedisSetValue) this.getValue();
    }

    public RedisZSetValue asZSetValue() {
        return (RedisZSetValue) this.getValue();
    }

    public RedisListValue asListValue() {
        return (RedisListValue) this.getValue();
    }

    public RedisHashValue asHashValue() {
        return (RedisHashValue) this.getValue();
    }

    public RedisStringValue asStringValue() {
        RedisKeyValue<?> value = this.getValue();
        if (value == null) {
            value = new RedisStringValue();
            this.setValue(value);
        }
        return (RedisStringValue) value;
    }

    public RedisStreamValue asStreamValue() {
        return (RedisStreamValue) this.getValue();
    }

    public String typeName() {
        return this.type.name();
    }

    @Override
    public void copy(RedisKey t1) {
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
