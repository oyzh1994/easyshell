package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.redis.ShellRedisCacheUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * redis的list值
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisListValue implements ShellRedisKeyValue<List<ShellRedisListValue.RedisListRow>> {

    /**
     * 值
     */
    private List<RedisListRow> value;

    /**
     * 获取未保存的行
     *
     * @return 未保存的行
     */
    public RedisListRow getUnSavedRow() {
        return unSavedRow;
    }

    /**
     * 设置未保存的行
     *
     * @param unSavedRow 未保存的行
     */
    public void setUnSavedRow(RedisListRow unSavedRow) {
        this.unSavedRow = unSavedRow;
    }

    @Override
    public List<RedisListRow> getValue() {
        return value;
    }

    @Override
    public void setValue(List<RedisListRow> value) {
        this.value = value;
    }

    /**
     * 未保存的行
     */
    private RedisListRow unSavedRow;

    /**
     * 构造方法
     *
     * @param value 值
     */
    public ShellRedisListValue(List<RedisListRow> value) {
        this.value = value;
    }

    /**
     * 创建list值
     *
     * @param elements 元素集合
     * @return list值
     */
    public static ShellRedisListValue valueOf(List<String> elements) {
        List<RedisListRow> rows = new ArrayList<>(12);
        if (elements != null) {
            int index = 0;
            for (String element : elements) {
                rows.add(new RedisListRow(index++, element));
            }
        }
        return new ShellRedisListValue(rows);
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
        if (unSavedValue instanceof RedisListRow) {
            this.unSavedRow = (RedisListRow) unSavedValue;
        }
    }

    /**
     * redis的list行
     */
    public static class RedisListRow implements ShellRedisKeyRow {

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
        public RedisListRow(int index, String value) {
            this.index = index;
            this.setValue(value);
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
        public RedisListRow clone() {
            return new RedisListRow(this.index, this.getValue());
        }
    }
}
