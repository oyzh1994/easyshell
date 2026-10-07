package cn.oyzh.easyshell.redis.batch;

import redis.clients.jedis.params.ScanParams;

import java.util.Objects;

/**
 * redis 计数结果
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisCountResult {

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
     * 获取数量
     *
     * @return 数量
     */
    public Integer getCount() {
        return count;
    }

    /**
     * 设置数量
     *
     * @param count 数量
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * 光标
     */
    private String cursor;

    /**
     * 数据
     */
    private Integer count;

    /**
     * 是否完成
     *
     * @return 结果
     */
    public boolean isFinish() {
        return Objects.equals(this.cursor, ScanParams.SCAN_POINTER_START) || count == null || count == 0;
    }
}
