package cn.oyzh.easyshell.data.mongo.handler;

import cn.oyzh.common.file.FastFileWriter;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mongo.ShellMongoClient;

import java.io.File;
import java.io.IOException;

/**
 * @author oyzh
 * @since 2024/08/22
 */
public abstract class DBDataDumpHandler extends DBDataHandler {

    /**
     * 数据类型
     * 0 数据和结构
     * 1 仅结构
     */
    protected Byte dataType;

    /**
     * 库名称
     */
    protected String dbName;

    /**
     * 转储文件
     */
    protected File dumpFile;

    /**
     * 文件写入器
     */
    protected FastFileWriter fileWriter;

//    /**
//     * db客户端
//     */
//    protected ShellMongoClient dbClient;

    /**
     * 1. 库
     * 2. 集合
     */
    protected Byte dumpType;

    /**
     * 表名称
     */
    protected String tableName;

    /**
     * 连接信息
     */
    protected ShellConnect dbInfo;

    /**
     * 查询限制
     */
    protected int queryLimit = 500;

    public DBDataDumpHandler( String dbName) {
        this.dbName = dbName;
    }

    /**
     * 设置转储文件
     *
     * @param dumpFile 转储文件
     * @return 当前对象
     */
    public DBDataDumpHandler dumpFile(File dumpFile) throws IOException {
        this.dumpFile = dumpFile;
        if (this.fileWriter != null) {
            this.fileWriter.close();
        }
        this.fileWriter = new FastFileWriter(dumpFile);
        return this;
    }

    /**
     * 执行转储
     *
     * @throws Exception 异常
     */
    public abstract void doDump() throws Exception;

    /**
     * 写入头部
     */
    protected abstract void writeHeader() throws IOException;

    /**
     * 写入尾部
     */
    protected void writeTail() throws IOException {
        this.fileWriter.close();
    }

    public boolean isDumpRecord() {
        return this.dataType == 0;
    }

    /**
     * 创建新的处理器
     *
     * @param dbClient db客户端
     * @param dbName   数据库
     * @return DBDataDumpHandler
     */
    public static DBDataDumpHandler newHandler(ShellMongoClient dbClient, String dbName) {
        return new ShellMongoDataDumpHandler(dbClient, dbName);
    }

    public Byte getDataType() {
        return dataType;
    }

    public void setDataType(Byte dataType) {
        this.dataType = dataType;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public File getDumpFile() {
        return dumpFile;
    }

    public void setDumpFile(File dumpFile) {
        this.dumpFile = dumpFile;
    }

    public FastFileWriter getFileWriter() {
        return fileWriter;
    }

    public void setFileWriter(FastFileWriter fileWriter) {
        this.fileWriter = fileWriter;
    }

    public Byte getDumpType() {
        return dumpType;
    }

    public DBDataDumpHandler setDumpType(Byte dumpType) {
        this.dumpType = dumpType;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public DBDataDumpHandler setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public ShellConnect getDbInfo() {
        return dbInfo;
    }

    public DBDataDumpHandler setDbInfo(ShellConnect dbInfo) {
        this.dbInfo = dbInfo;
        return this;
    }

    public int getQueryLimit() {
        return queryLimit;
    }

    public DBDataDumpHandler setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
        return this;
    }
}

