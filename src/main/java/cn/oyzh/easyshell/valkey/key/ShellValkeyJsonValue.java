package cn.oyzh.easyshell.valkey.key;

import cn.oyzh.easyshell.util.valkey.ShellValkeyCacheUtil;

/**
 * redis的json值
 *
 * @author oyzh
 * @since 2025-10-24
 */
public class ShellValkeyJsonValue implements ShellValkeyKeyValue<String> {

    /**
     * 构造方法
     */
    public ShellValkeyJsonValue() {
    }

    /**
     * 构造方法
     *
     * @param value json字符串
     */
    public ShellValkeyJsonValue(String value) {
        this.setValue(value);
    }

    /**
     * 创建json值
     *
     * @param value json字符串
     * @return json值
     */
    public static ShellValkeyJsonValue valueOf(String value) {
        return new ShellValkeyJsonValue(value);
    }

    @Override
    public void setValue(String value) {
        ShellValkeyCacheUtil.cacheValue(this.hashCode(), value, "value");
    }

    @Override
    public String getValue() {
        return (String) ShellValkeyCacheUtil.loadValue(this.hashCode(), "value");
    }

    @Override
    public boolean hasValue() {
        return ShellValkeyCacheUtil.hasValue(this.hashCode(), "value");
    }

    @Override
    public Object getUnSavedValue() {
        return ShellValkeyCacheUtil.loadValue(this.hashCode(), "unsaved");
    }

    @Override
    public void clearUnSavedValue() {
        ShellValkeyCacheUtil.deleteValue(this.hashCode(), "unsaved");
    }

    @Override
    public boolean hasUnSavedValue() {
        return ShellValkeyCacheUtil.hasValue(this.hashCode(), "unsaved");
    }

    @Override
    public void setUnSavedValue(Object unSavedValue) {
        ShellValkeyCacheUtil.cacheValue(this.hashCode(), unSavedValue, "unsaved");
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
}
