package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.redis.RedisCacheUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-12-02
 */
public class RedisHashValue implements RedisKeyValue<List<RedisHashValue.RedisHashRow>> {

    private List<RedisHashRow> value;

    private RedisHashRow unSavedRow;

    @Override
    public List<RedisHashRow> getValue() {
        return value;
    }

    @Override
    public void setValue(List<RedisHashRow> value) {
        this.value = value;
    }

    public RedisHashRow getUnSavedRow() {
        return unSavedRow;
    }

    public void setUnSavedRow(RedisHashRow unSavedRow) {
        this.unSavedRow = unSavedRow;
    }

    public RedisHashValue(List<RedisHashRow> value) {
        this.value = value;
    }

    public static RedisHashValue valueOf(Map<String, String> value) {
        List<RedisHashRow> rows = new ArrayList<>(12);
        if (value != null) {
            for (Map.Entry<String, String> entry : value.entrySet()) {
                rows.add(new RedisHashRow(entry.getKey(), entry.getValue()));
            }
        }
        return new RedisHashValue(rows);
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

    public static class RedisHashRow implements RedisKeyRow {

        public RedisHashRow(String field, String value) {
            this.setField(field);
            this.setValue(value);
        }

        public void setField(String field) {
            RedisCacheUtil.cacheValue(this.hashCode(), field, "field");
        }

        public String getField() {
            return (String) RedisCacheUtil.loadValue(this.hashCode(), "field");
        }

        @Override
        public void setValue(String value) {
            RedisCacheUtil.cacheValue(this.hashCode(), value, "value");
        }

        @Override
        public String getValue() {
            return (String) RedisCacheUtil.loadValue(this.hashCode(), "value");
        }

        @Override
        public RedisHashRow clone() {
            return new RedisHashRow(this.getField(), this.getValue());
        }
    }
}
