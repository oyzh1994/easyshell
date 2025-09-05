package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.easyshell.util.redis.ShellRedisCacheUtil;

/**
 * @author oyzh
 * @since 2024-12-02
 */
public class ShellRedisStringValue implements ShellRedisKeyValue<Object> {

    /**
     * 统计值
     */
    private Long count;

    public Boolean getHyLog() {
        return hyLog;
    }

    public void setHyLog(Boolean hyLog) {
        this.hyLog = hyLog;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    /**
     * 统计值标志位
     */
    private Boolean hyLog;

    public ShellRedisStringValue() {
    }

    public ShellRedisStringValue(String value) {
        this.setValue(value);
    }

    public ShellRedisStringValue(byte[] value) {
        this.setValue(value);
    }

    public static ShellRedisStringValue valueOf(String value) {
        return new ShellRedisStringValue(value);
    }

    public static ShellRedisStringValue valueOf(byte[] value) {
        return new ShellRedisStringValue(value);
    }

    @Override
    public void setValue(Object value) {
        ShellRedisCacheUtil.cacheValue(this.hashCode(), value, "value");
    }

    @Override
    public Object getValue() {
        return ShellRedisCacheUtil.loadValue(this.hashCode(), "value");
    }

    @Override
    public boolean hasValue() {
        return ShellRedisCacheUtil.hasValue(this.hashCode(), "value");
    }

    @Override
    public Object getUnSavedValue() {
        return ShellRedisCacheUtil.loadValue(this.hashCode(), "unsaved");
    }

    @Override
    public void clearUnSavedValue() {
        ShellRedisCacheUtil.deleteValue(this.hashCode(), "unsaved");
    }

    @Override
    public boolean hasUnSavedValue() {
        return ShellRedisCacheUtil.hasValue(this.hashCode(), "unsaved");
    }

    @Override
    public void setUnSavedValue(Object unSavedValue) {
        ShellRedisCacheUtil.cacheValue(this.hashCode(), unSavedValue, "unsaved");
    }

    public boolean isHyLog() {
        return this.count != null || BooleanUtil.isTrue(this.hyLog);
    }

    public String stringValue() {
        Object value = this.getValue();
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof byte[] bytes) {
            return new String(bytes);
        }
        return "";
    }

    public byte[] bytesValue() {
        Object value = this.getValue();
        if (value instanceof String s) {
            return s.getBytes();
        }
        if (value instanceof byte[] bytes) {
            return bytes;
        }
        return new byte[0];
    }
}
