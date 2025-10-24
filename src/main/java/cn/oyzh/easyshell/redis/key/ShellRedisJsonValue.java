package cn.oyzh.easyshell.redis.key;

import cn.oyzh.easyshell.util.redis.ShellRedisCacheUtil;

/**
 * @author oyzh
 * @since 2025-10-24
 */
public class ShellRedisJsonValue implements ShellRedisKeyValue<String> {

    public ShellRedisJsonValue() {
    }

    public ShellRedisJsonValue(String value) {
        this.setValue(value);
    }

    public static ShellRedisJsonValue valueOf(String value) {
        return new ShellRedisJsonValue(value);
    }

    @Override
    public void setValue(String value) {
        ShellRedisCacheUtil.cacheValue(this.hashCode(), value, "value");
    }

    @Override
    public String getValue() {
        return (String) ShellRedisCacheUtil.loadValue(this.hashCode(), "value");
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
}
