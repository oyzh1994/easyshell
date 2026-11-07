package cn.oyzh.easyshell.valkey.key;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.valkey.ShellValkeyCacheUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * redis的set值
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeySetValue implements ShellValkeyKeyValue<List<ShellValkeySetValue.ValkeySetRow>> {

    /**
     * 值
     */
    private List<ValkeySetRow> value;

    /**
     * 获取未保存的行
     *
     * @return 未保存的行
     */
    public ValkeySetRow getUnSavedRow() {
        return unSavedRow;
    }

    /**
     * 设置未保存的行
     *
     * @param unSavedRow 未保存的行
     */
    public void setUnSavedRow(ValkeySetRow unSavedRow) {
        this.unSavedRow = unSavedRow;
    }

    @Override
    public List<ValkeySetRow> getValue() {
        return value;
    }

    @Override
    public void setValue(List<ValkeySetRow> value) {
        this.value = value;
    }

    /**
     * 未保存的行
     */
    private ValkeySetRow unSavedRow;

    /**
     * 构造方法
     */
    public ShellValkeySetValue() {
    }

    /**
     * 构造方法
     *
     * @param value 值
     */
    public ShellValkeySetValue(List<ValkeySetRow> value) {
        this.value = value;
    }

    /**
     * 创建set值
     *
     * @param members 成员集合
     * @return set值
     */
    public static ShellValkeySetValue valueOf(Set<String> members) {
        List<ValkeySetRow> rows = new ArrayList<>(12);
        if (members != null) {
            for (String member : members) {
                rows.add(new ValkeySetRow(member));
            }
        }
        return new ShellValkeySetValue(rows);
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
        if (unSavedValue instanceof ValkeySetRow) {
            this.unSavedRow = (ValkeySetRow) unSavedValue;
        }
    }

    /**
     * redis的set行
     */
    public static class ValkeySetRow implements ShellValkeyKeyRow {

        /**
         * 索引
         */
        private byte index;

        /**
         * 获取索引
         *
         * @return 索引
         */
        public byte getIndex() {
            return index;
        }

        /**
         * 设置索引
         *
         * @param index 索引
         */
        public void setIndex(byte index) {
            this.index = index;
        }

        /**
         * 构造方法
         *
         * @param value 值
         */
        public ValkeySetRow(String value) {
            this.setValue(value);
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
        public ValkeySetRow clone() {
            return new ValkeySetRow(this.getValue());
        }
    }
}
