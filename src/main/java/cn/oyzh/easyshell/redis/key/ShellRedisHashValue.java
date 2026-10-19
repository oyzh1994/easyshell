package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.redis.ShellRedisCacheUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * redis的hash值
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisHashValue implements ShellRedisKeyValue<List<ShellRedisHashValue.RedisHashRow>> {

    /**
     * 值
     */
    private List<RedisHashRow> value;

    /**
     * 未保存的行
     */
    private RedisHashRow unSavedRow;

    @Override
    public List<RedisHashRow> getValue() {
        return value;
    }

    @Override
    public void setValue(List<RedisHashRow> value) {
        this.value = value;
    }

    /**
     * 获取未保存的行
     *
     * @return 未保存的行
     */
    public RedisHashRow getUnSavedRow() {
        return unSavedRow;
    }

    /**
     * 设置未保存的行
     *
     * @param unSavedRow 未保存的行
     */
    public void setUnSavedRow(RedisHashRow unSavedRow) {
        this.unSavedRow = unSavedRow;
    }

    /**
     * 构造方法
     *
     * @param value 值
     */
    public ShellRedisHashValue(List<RedisHashRow> value) {
        this.value = value;
    }

    /**
     * 创建hash值
     *
     * @param value 键值映射
     * @return hash值
     */
    public static ShellRedisHashValue valueOf(Map<String, String> value) {
        List<RedisHashRow> rows = new ArrayList<>(12);
        if (value != null) {
            for (Map.Entry<String, String> entry : value.entrySet()) {
                rows.add(new RedisHashRow(entry.getKey(), entry.getValue()));
            }
        }
        return new ShellRedisHashValue(rows);
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
        if (unSavedValue instanceof RedisHashRow) {
            this.unSavedRow = (RedisHashRow) unSavedValue;
        }
    }

    /**
     * redis的hash行
     */
    public static class RedisHashRow implements ShellRedisKeyRow {

        /**
         * 构造方法
         *
         * @param field 字段
         * @param value 值
         */
        public RedisHashRow(String field, String value) {
            this.setField(field);
            this.setValue(value);
        }

        /**
         * 设置字段
         *
         * @param field 字段
         */
        public void setField(String field) {
            ShellRedisCacheUtil.cacheValue(this.hashCode(), field, "field");
        }

        /**
         * 获取字段
         *
         * @return 字段
         */
        public String getField() {
            return (String) ShellRedisCacheUtil.loadValue(this.hashCode(), "field");
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
        public RedisHashRow clone() {
            return new RedisHashRow(this.getField(), this.getValue());
        }
    }
}
