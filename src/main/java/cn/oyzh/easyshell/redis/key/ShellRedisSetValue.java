package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.redis.ShellRedisCacheUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * redis的set值
 *
 * @author oyzh
 * @since 2024-12-02
 */
public class ShellRedisSetValue implements ShellRedisKeyValue<List<ShellRedisSetValue.RedisSetRow>> {

    /**
     * 值
     */
    private List<RedisSetRow> value;

    /**
     * 获取未保存的行
     *
     * @return 未保存的行
     */
    public RedisSetRow getUnSavedRow() {
        return unSavedRow;
    }

    /**
     * 设置未保存的行
     *
     * @param unSavedRow 未保存的行
     */
    public void setUnSavedRow(RedisSetRow unSavedRow) {
        this.unSavedRow = unSavedRow;
    }

    @Override
    public List<RedisSetRow> getValue() {
        return value;
    }

    @Override
    public void setValue(List<RedisSetRow> value) {
        this.value = value;
    }

    /**
     * 未保存的行
     */
    private RedisSetRow unSavedRow;

    /**
     * 构造方法
     */
    public ShellRedisSetValue() {
    }

    /**
     * 构造方法
     *
     * @param value 值
     */
    public ShellRedisSetValue(List<RedisSetRow> value) {
        this.value = value;
    }

    /**
     * 创建set值
     *
     * @param members 成员集合
     * @return set值
     */
    public static ShellRedisSetValue valueOf(Set<String> members) {
        List<RedisSetRow> rows = new ArrayList<>(12);
        if (members != null) {
            for (String member : members) {
                rows.add(new RedisSetRow(member));
            }
        }
        return new ShellRedisSetValue(rows);
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
        if (unSavedValue instanceof RedisSetRow) {
            this.unSavedRow = (RedisSetRow) unSavedValue;
        }
    }

    /**
     * redis的set行
     */
    public static class RedisSetRow implements ShellRedisKeyRow {

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
        public RedisSetRow(String value) {
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
        public RedisSetRow clone() {
            return new RedisSetRow(this.getValue());
        }
    }
}
