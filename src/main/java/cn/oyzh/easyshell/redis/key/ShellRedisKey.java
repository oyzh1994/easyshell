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
 * @since 2023/6/16
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

    public ShellRedisKeyType getType() {
        return type;
    }

    public void setType(ShellRedisKeyType type) {
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

    public ShellRedisKeyValue<?> getValue() {
        return value;
    }

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

    private ShellRedisKeyValue<?> value;

    public void valueOfSet(Set<String> members) {
        this.setValue(ShellRedisSetValue.valueOf(members));
    }

    public void valueOfZSet(List<String> members, List<Double> scores) {
        this.setValue(ShellRedisZSetValue.valueOf(members, scores));
    }

    public void valueOfCoordinates(List<String> members, List<GeoCoordinate> coordinates) {
        this.setValue(ShellRedisZSetValue.valueOfCoordinates(members, coordinates));
    }

    public void valueOfHash(Map<String, String> values) {
        this.setValue(ShellRedisHashValue.valueOf(values));
    }

    public void valueOfList(List<String> elements) {
        this.setValue(ShellRedisListValue.valueOf(elements));
    }

    public void valueOfStream(List<StreamEntry> entries) {
        this.setValue(ShellRedisStreamValue.valueOf(entries));
    }

    public void valueOfString(String value) {
        this.setValue(ShellRedisStringValue.valueOf(value));
    }

    public void valueOfBytes(byte[] value) {
        this.setValue(ShellRedisStringValue.valueOf(value));
    }

    public ShellRedisSetValue asSetValue() {
        return (ShellRedisSetValue) this.getValue();
    }

    public ShellRedisZSetValue asZSetValue() {
        return (ShellRedisZSetValue) this.getValue();
    }

    public ShellRedisListValue asListValue() {
        return (ShellRedisListValue) this.getValue();
    }

    public ShellRedisHashValue asHashValue() {
        return (ShellRedisHashValue) this.getValue();
    }

    public ShellRedisStringValue asStringValue() {
        ShellRedisKeyValue<?> value = this.getValue();
        if (value == null) {
            value = new ShellRedisStringValue();
            this.setValue(value);
        }
        return (ShellRedisStringValue) value;
    }

    public ShellRedisStreamValue asStreamValue() {
        return (ShellRedisStreamValue) this.getValue();
    }

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
