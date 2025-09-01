package cn.oyzh.easyshell.redis.batch;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.redis.key.RedisKey;
import redis.clients.jedis.params.ScanParams;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * redis 扫描结果
 *
 * @author oyzh
 * @since 2023/6/28
 */
public class RedisScanResult {

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public List<RedisKey> getKeys() {
        return keys;
    }

    public void setKeys(List<RedisKey> keys) {
        this.keys = keys;
    }

    /**
     * 光标
     */
    private String cursor;

    /**
     * 数据
     */
    private List<RedisKey> keys;

    public boolean isFinish() {
        return Objects.equals(this.cursor, ScanParams.SCAN_POINTER_START) || CollectionUtil.isEmpty(this.keys);
    }

    public int keySize() {
        return this.keys == null ? 0 : this.keys.size();
    }

    public List<String> keys() {
        return this.keys == null ? Collections.emptyList() : this.keys.parallelStream().map(RedisKey::getKey).collect(Collectors.toList());
    }
}
