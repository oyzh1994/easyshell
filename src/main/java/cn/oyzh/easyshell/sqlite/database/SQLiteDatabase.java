package cn.oyzh.easyshell.sqlite.database;

import cn.oyzh.fx.db.DBDatabse;

/**
 * SQLite数据库
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteDatabase implements DBDatabse {

    /**
     * 库名称
     */
    private String name;

    /**
     * 版本
     */
    private String version;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小
     */
    private long fileSize;

    /**
     * 页大小
     */
    private int pageSize;

    /**
     * 页数
     */
    private long pageCount;

    /**
     * 编码
     */
    private String encoding;

    /**
     * 日志模式
     */
    private String journalMode;

    /**
     * 是否启用外键
     */
    private boolean foreignKeys;

    /**
     * 同步模式
     */
    private String synchronous;

    /**
     * 是否只读
     */
    private boolean readOnly;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getPageCount() {
        return this.pageCount;
    }

    public void setPageCount(long pageCount) {
        this.pageCount = pageCount;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getJournalMode() {
        return this.journalMode;
    }

    public void setJournalMode(String journalMode) {
        this.journalMode = journalMode;
    }

    public boolean isForeignKeys() {
        return this.foreignKeys;
    }

    public void setForeignKeys(boolean foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public String getSynchronous() {
        return this.synchronous;
    }

    public void setSynchronous(String synchronous) {
        this.synchronous = synchronous;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
