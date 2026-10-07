package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.easyshell.util.redis.ShellRedisCacheUtil;

/**
 * redis的string值
 *
 * @author oyzh
 * @since 2024-12-02
 */
public class ShellRedisStringValue implements ShellRedisKeyValue<Object> {

    /**
     * 统计值
     */
    private Long count;

    /**
     * 统计值标志位
     */
    private Boolean hyLog;

    /**
     * 获取统计值
     *
     * @return 统计值
     */
    public Long getCount() {
        return count;
    }

    /**
     * 设置统计值
     *
     * @param count 统计值
     */
    public void setCount(Long count) {
        this.count = count;
    }

    /**
     * 获取统计值标志位
     *
     * @return 统计值标志位
     */
    public Boolean getHyLog() {
        return hyLog;
    }

    /**
     * 设置统计值标志位
     *
     * @param hyLog 统计值标志位
     */
    public void setHyLog(Boolean hyLog) {
        this.hyLog = hyLog;
    }

    /**
     * 构造方法
     */
    public ShellRedisStringValue() {
    }

    /**
     * 构造方法
     *
     * @param value 字符串
     */
    public ShellRedisStringValue(String value) {
        this.setValue(value);
    }

    /**
     * 构造方法
     *
     * @param value 字节数组
     */
    public ShellRedisStringValue(byte[] value) {
        this.setValue(value);
    }

    /**
     * 创建string值
     *
     * @param value 字符串
     * @return string值
     */
    public static ShellRedisStringValue valueOf(String value) {
        return new ShellRedisStringValue(value);
    }

    /**
     * 创建string值
     *
     * @param value 字节数组
     * @return string值
     */
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

    /**
     * 是否统计值
     *
     * @return 结果
     */
    public boolean isHyLog() {
        return this.count != null || BooleanUtil.isTrue(this.hyLog);
    }

    /**
     * 获取字符串值
     *
     * @return 字符串值
     */
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

    /**
     * 获取字节数组值
     *
     * @return 字节数组值
     */
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
