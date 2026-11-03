package cn.oyzh.easyshell.sqlite;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBConnConfig;

import java.nio.file.Path;

/**
 * SQLite 文件连接配置。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteConnConfig extends DBConnConfig {

    /**
     * 是否只读
     */
    private boolean readOnly;

    /**
     * 文件不存在时是否创建
     */
    private boolean createIfMissing;

    /**
     * 写锁等待时间，单位毫秒
     */
    private int busyTimeout = 5000;

    /**
     * 日志模式
     */
    private String journalMode = "DELETE";

    /**
     * 是否启用外键
     */
    private boolean foreignKeys = true;

    /**
     * 同步模式
     */
    private String synchronous = "NORMAL";

    /**
     * 获取规范化后的数据库文件路径。
     *
     * @return 文件路径
     */
    public Path resolveFilePath() {
        if (StringUtil.isBlank(this.getHost())) {
            throw new IllegalArgumentException("Database file path is empty");
        }
        return Path.of(this.getHost()).toAbsolutePath().normalize();
    }

    /**
     * 是否只读。
     *
     * @return 结果
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * 设置只读。
     *
     * @param readOnly 是否只读
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * 文件不存在时是否创建。
     *
     * @return 结果
     */
    public boolean isCreateIfMissing() {
        return this.createIfMissing;
    }

    /**
     * 设置文件不存在时是否创建。
     *
     * @param createIfMissing 是否创建
     */
    public void setCreateIfMissing(boolean createIfMissing) {
        this.createIfMissing = createIfMissing;
    }

    /**
     * 获取写锁等待时间。
     *
     * @return 毫秒
     */
    public int getBusyTimeout() {
        return this.busyTimeout;
    }

    /**
     * 设置写锁等待时间。
     *
     * @param busyTimeout 毫秒
     */
    public void setBusyTimeout(int busyTimeout) {
        this.busyTimeout = Math.max(0, busyTimeout);
    }

    /**
     * 获取日志模式。
     *
     * @return 日志模式
     */
    public String getJournalMode() {
        return this.journalMode;
    }

    /**
     * 设置日志模式。
     *
     * @param journalMode 日志模式
     */
    public void setJournalMode(String journalMode) {
        this.journalMode = journalMode;
    }

    /**
     * 是否启用外键。
     *
     * @return 结果
     */
    public boolean isForeignKeys() {
        return this.foreignKeys;
    }

    /**
     * 设置是否启用外键。
     *
     * @param foreignKeys 是否启用外键
     */
    public void setForeignKeys(boolean foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    /**
     * 获取同步模式。
     *
     * @return 同步模式
     */
    public String getSynchronous() {
        return this.synchronous;
    }

    /**
     * 设置同步模式。
     *
     * @param synchronous 同步模式
     */
    public void setSynchronous(String synchronous) {
        this.synchronous = synchronous;
    }
}
