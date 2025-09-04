package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.redis.ShellRedisCacheUtil;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.resps.StreamEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-12-02
 */
public class ShellRedisStreamValue implements ShellRedisKeyValue<List<ShellRedisStreamValue.RedisStreamRow>> {

    private List<RedisStreamRow> value;

    public RedisStreamRow getUnSavedRow() {
        return unSavedRow;
    }

    public void setUnSavedRow(RedisStreamRow unSavedRow) {
        this.unSavedRow = unSavedRow;
    }

    @Override
    public List<RedisStreamRow> getValue() {
        return value;
    }

    @Override
    public void setValue(List<RedisStreamRow> value) {
        this.value = value;
    }

    private RedisStreamRow unSavedRow;

    public ShellRedisStreamValue(List<RedisStreamRow> value) {
        this.value = value;
    }

    public static ShellRedisStreamValue valueOf(List<StreamEntry> value) {
        List<RedisStreamRow> rows = new ArrayList<>(12);
        if (value != null) {
            for (StreamEntry entry : value) {
                rows.add(new RedisStreamRow(entry));
            }
        }
        return new ShellRedisStreamValue(rows);
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
        if (unSavedValue instanceof RedisStreamRow) {
            this.unSavedRow = (RedisStreamRow) unSavedValue;
        }
    }

    public static class RedisStreamRow implements ShellRedisKeyRow {

        public RedisStreamRow(StreamEntry entry) {
            this.setId(entry.getID().toString());
            this.setValue(JSONUtil.toJson(entry.getFields()));
        }

        public void setId(String id) {
            ShellRedisCacheUtil.cacheValue(this.hashCode(), id, "id");
        }

        public String getId() {
            return (String) ShellRedisCacheUtil.loadValue(this.hashCode(), "id");
        }

        public String getValue() {
            return (String) ShellRedisCacheUtil.loadValue(this.hashCode(), "value");
        }

        @Override
        public void setValue(String value) {
            ShellRedisCacheUtil.cacheValue(this.hashCode(), value, "value");
        }

        public StreamEntryID getStreamId() {
            return new StreamEntryID(this.getId());
        }

        public Map<String, String> getFields() {
            return JSONUtil.parseObject(this.getValue()).toJavaObject(Map.class);
        }
    }
}
