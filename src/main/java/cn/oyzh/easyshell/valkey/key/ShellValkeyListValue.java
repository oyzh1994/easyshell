package cn.oyzh.easyshell.valkey.key;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.valkey.ShellValkeyCacheUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * redis的list值
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyListValue implements ShellValkeyKeyValue<List<ShellValkeyListValue.ValkeyListRow>> {

    /**
     * 值
     */
    private List<ValkeyListRow> value;

    /**
     * 获取未保存的行
     *
     * @return 未保存的行
     */
    public ValkeyListRow getUnSavedRow() {
        return unSavedRow;
    }

    /**
     * 设置未保存的行
     *
     * @param unSavedRow 未保存的行
     */
    public void setUnSavedRow(ValkeyListRow unSavedRow) {
        this.unSavedRow = unSavedRow;
    }

    @Override
    public List<ValkeyListRow> getValue() {
        return value;
    }

    @Override
    public void setValue(List<ValkeyListRow> value) {
        this.value = value;
    }

    /**
     * 未保存的行
     */
    private ValkeyListRow unSavedRow;

    /**
     * 构造方法
     *
     * @param value 值
     */
    public ShellValkeyListValue(List<ValkeyListRow> value) {
        this.value = value;
    }

    /**
     * 创建list值
     *
     * @param elements 元素集合
     * @return list值
     */
    public static ShellValkeyListValue valueOf(List<String> elements) {
        List<ValkeyListRow> rows = new ArrayList<>(12);
        if (elements != null) {
            int index = 0;
            for (String element : elements) {
                rows.add(new ValkeyListRow(index++, element));
            }
        }
        return new ShellValkeyListValue(rows);
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
        if (unSavedValue instanceof ValkeyListRow) {
            this.unSavedRow = (ValkeyListRow) unSavedValue;
        }
    }

    /**
     * redis的list行
     */
    public static class ValkeyListRow implements ShellValkeyKeyRow {

        /**
         * 索引
         */
        private final int index;

        /**
         * 获取索引
         *
         * @return 索引
         */
        public int getIndex() {
            return index;
        }

        /**
         * 构造方法
         *
         * @param index 索引
         * @param value 值
         */
        public ValkeyListRow(int index, String value) {
            this.index = index;
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
        public ValkeyListRow clone() {
            return new ValkeyListRow(this.index, this.getValue());
        }
    }
}
