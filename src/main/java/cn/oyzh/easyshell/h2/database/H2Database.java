package cn.oyzh.easyshell.h2.database;

import cn.oyzh.fx.db.DBDatabse;

/**
 * H2数据库
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class H2Database implements DBDatabse {

    /**
     * 库名称
     */
    private String name;

    /**
     * 版本
     */
    private String version;

    /**
     * 产品名
     */
    private String product;

    /**
     * 文件路径或内存标识
     */
    private String filePath;

    /**
     * 文件大小，仅文件模式
     */
    private long fileSize;

    /**
     * 用户名
     */
    private String user;

    /**
     * 默认 schema
     */
    private String schema;

    /**
     * 是否只读
     */
    private boolean readOnly;

    /**
     * 事务隔离级别
     */
    private String isolationLevel;

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

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String product) {
        this.product = product;
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

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getIsolationLevel() {
        return this.isolationLevel;
    }

    public void setIsolationLevel(String isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

}
