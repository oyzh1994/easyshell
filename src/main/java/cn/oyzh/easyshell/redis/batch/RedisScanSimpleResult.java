package cn.oyzh.easyshell.redis.batch;

import cn.oyzh.common.util.CollectionUtil;
import redis.clients.jedis.params.ScanParams;

import java.util.List;
import java.util.Objects;

/**
 * redis 扫描结果
 *
 * @author oyzh
 * @since 2023/6/28
 */
public class RedisScanSimpleResult {

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    /**
     * 光标
     */
    private String cursor;

    /**
     * 数据
     */
    private List<String> keys;

    public boolean isFinish() {
        return Objects.equals(this.cursor, ScanParams.SCAN_POINTER_START) || CollectionUtil.isEmpty(this.keys);
    }

    public int keySize() {
        return this.keys == null ? 0 : this.keys.size();
    }
}
