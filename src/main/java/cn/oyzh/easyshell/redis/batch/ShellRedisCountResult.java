package cn.oyzh.easyshell.redis.batch;

import redis.clients.jedis.params.ScanParams;

import java.util.Objects;

/**
 * redis 扫描结果
 *
 * @author oyzh
 * @since 2023/6/28
 */
public class ShellRedisCountResult {

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public Integer getCount() {
        return count;
    }

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

    public boolean isFinish() {
        return Objects.equals(this.cursor, ScanParams.SCAN_POINTER_START) || count == null || count == 0;
    }
}
