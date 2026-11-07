package cn.oyzh.easyshell.valkey.key;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.valkey.ShellValkeyCacheUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * redis的hash值
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyHashValue implements ShellValkeyKeyValue<List<ShellValkeyHashValue.ValkeyHashRow>> {

    /**
     * 值
     */
    private List<ValkeyHashRow> value;

    /**
     * 未保存的行
     */
    private ValkeyHashRow unSavedRow;

    @Override
    public List<ValkeyHashRow> getValue() {
        return value;
    }

    @Override
    public void setValue(List<ValkeyHashRow> value) {
        this.value = value;
    }

    /**
     * 获取未保存的行
     *
     * @return 未保存的行
     */
    public ValkeyHashRow getUnSavedRow() {
        return unSavedRow;
    }

    /**
     * 设置未保存的行
     *
     * @param unSavedRow 未保存的行
     */
    public void setUnSavedRow(ValkeyHashRow unSavedRow) {
        this.unSavedRow = unSavedRow;
    }

    /**
     * 构造方法
     *
     * @param value 值
     */
    public ShellValkeyHashValue(List<ValkeyHashRow> value) {
        this.value = value;
    }

    /**
     * 创建hash值
     *
     * @param value 键值映射
     * @return hash值
     */
    public static ShellValkeyHashValue valueOf(Map<String, String> value) {
        List<ValkeyHashRow> rows = new ArrayList<>(12);
        if (value != null) {
            for (Map.Entry<String, String> entry : value.entrySet()) {
                rows.add(new ValkeyHashRow(entry.getKey(), entry.getValue()));
            }
        }
        return new ShellValkeyHashValue(rows);
    }

    @Override
    public boolean hasValue() {
        return CollectionUtil.isNotEmpty(this.value);
    }

    @Override
    public Object getUnSavedValue() {
        return this.unSavedRow;
    }

    @Override
    public void clearUnSavedValue() {
        if (this.unSavedRow != null) {
            this.unSavedRow.setValue(null);
            this.unSavedRow = null;
        }
    }

    @Override
    public boolean hasUnSavedValue() {
        return this.unSavedRow != null && this.unSavedRow.getValue() != null;
    }

    @Override
    public void setUnSavedValue(Object unSavedValue) {
        if (unSavedValue instanceof ValkeyHashRow) {
            this.unSavedRow = (ValkeyHashRow) unSavedValue;
        }
    }

    /**
     * redis的hash行
     */
    public static class ValkeyHashRow implements ShellValkeyKeyRow {

        /**
         * 构造方法
         *
         * @param field 字段
         * @param value 值
         */
        public ValkeyHashRow(String field, String value) {
            this.setField(field);
            this.setValue(value);
        }

        /**
         * 设置字段
         *
         * @param field 字段
         */
        public void setField(String field) {
            ShellValkeyCacheUtil.cacheValue(this.hashCode(), field, "field");
        }

        /**
         * 获取字段
         *
         * @return 字段
         */
        public String getField() {
            return (String) ShellValkeyCacheUtil.loadValue(this.hashCode(), "field");
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
        public ValkeyHashRow clone() {
            return new ValkeyHashRow(this.getField(), this.getValue());
        }
    }
}
