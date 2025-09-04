package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.redis.ShellRedisCacheUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-02
 */
public class ShellRedisListValue implements ShellRedisKeyValue<List<ShellRedisListValue.RedisListRow>> {

    private List<RedisListRow> value;

    public RedisListRow getUnSavedRow() {
        return unSavedRow;
    }

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

    private RedisListRow unSavedRow;

    public ShellRedisListValue(List<RedisListRow> value) {
        this.value = value;
    }

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

    public static class RedisListRow implements ShellRedisKeyRow {

        private final int index;

        public int getIndex() {
            return index;
        }

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
