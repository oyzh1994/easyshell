package cn.oyzh.easyshell.redis.batch;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
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
public class ShellRedisScanResult {

    /**
     * 获取光标
     *
     * @return 光标
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * 设置光标
     *
     * @param cursor 光标
     */
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    /**
     * 获取键集合
     *
     * @return 键集合
     */
    public List<ShellRedisKey> getKeys() {
        return keys;
    }

    /**
     * 设置键集合
     *
     * @param keys 键集合
     */
    public void setKeys(List<ShellRedisKey> keys) {
        this.keys = keys;
    }

    /**
     * 光标
     */
    private String cursor;

    /**
     * 数据
     */
    private List<ShellRedisKey> keys;

    /**
     * 是否完成
     *
     * @return 结果
     */
    public boolean isFinish() {
        return Objects.equals(this.cursor, ScanParams.SCAN_POINTER_START) || CollectionUtil.isEmpty(this.keys);
    }

    /**
     * 获取键数量
     *
     * @return 键数量
     */
    public int keySize() {
        return this.keys == null ? 0 : this.keys.size();
    }

    /**
     * 获取键名称集合
     *
     * @return 键名称集合
     */
    public List<String> keys() {
        return this.keys == null ? Collections.emptyList() : this.keys.parallelStream().map(ShellRedisKey::getKey).collect(Collectors.toList());
    }
}
