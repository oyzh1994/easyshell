package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.redis.ShellRedisCacheUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author oyzh
 * @since 2024-12-02
 */
public class ShellRedisSetValue implements ShellRedisKeyValue<List<ShellRedisSetValue.RedisSetRow>> {

    private List<RedisSetRow> value;

    public RedisSetRow getUnSavedRow() {
        return unSavedRow;
    }

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

    private RedisSetRow unSavedRow;

    public ShellRedisSetValue() {
    }

    public ShellRedisSetValue(List<RedisSetRow> value) {
        this.value = value;
    }

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

    public static class RedisSetRow implements ShellRedisKeyRow {

        private byte index;

        public byte getIndex() {
            return index;
        }

        public void setIndex(byte index) {
            this.index = index;
        }

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
