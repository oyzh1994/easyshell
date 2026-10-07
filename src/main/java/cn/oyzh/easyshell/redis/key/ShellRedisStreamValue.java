package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.redis.ShellRedisCacheUtil;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.resps.StreamEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * redis的stream值
 *
 * @author oyzh
 * @since 2024-12-02
 */
public class ShellRedisStreamValue implements ShellRedisKeyValue<List<ShellRedisStreamValue.RedisStreamRow>> {

    /**
     * 值
     */
    private List<RedisStreamRow> value;

    /**
     * 获取未保存的行
     *
     * @return 未保存的行
     */
    public RedisStreamRow getUnSavedRow() {
        return unSavedRow;
    }

    /**
     * 设置未保存的行
     *
     * @param unSavedRow 未保存的行
     */
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

    /**
     * 未保存的行
     */
    private RedisStreamRow unSavedRow;

    /**
     * 构造方法
     *
     * @param value 值
     */
    public ShellRedisStreamValue(List<RedisStreamRow> value) {
        this.value = value;
    }

    /**
     * 创建stream值
     *
     * @param value 流条目集合
     * @return stream值
     */
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

    /**
     * redis的stream行
     */
    public static class RedisStreamRow implements ShellRedisKeyRow {

        /**
         * 构造方法
         *
         * @param entry 流条目
         */
        public RedisStreamRow(StreamEntry entry) {
            this.setId(entry.getID().toString());
            this.setValue(JSONUtil.toJson(entry.getFields()));
        }

        /**
         * 设置id
         *
         * @param id id
         */
        public void setId(String id) {
            ShellRedisCacheUtil.cacheValue(this.hashCode(), id, "id");
        }

        /**
         * 获取id
         *
         * @return id
         */
        public String getId() {
            return (String) ShellRedisCacheUtil.loadValue(this.hashCode(), "id");
        }

        /**
         * 获取值
         *
         * @return 值
         */
        public String getValue() {
            return (String) ShellRedisCacheUtil.loadValue(this.hashCode(), "value");
        }

        @Override
        public void setValue(String value) {
            ShellRedisCacheUtil.cacheValue(this.hashCode(), value, "value");
        }

        /**
         * 获取流id
         *
         * @return 流id
         */
        public StreamEntryID getStreamId() {
            return new StreamEntryID(this.getId());
        }

        /**
         * 获取字段映射
         *
         * @return 字段映射
         */
        public Map<String, String> getFields() {
            return JSONUtil.parseObject(this.getValue()).toJavaObject(Map.class);
        }
    }
}
