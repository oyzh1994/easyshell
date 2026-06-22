package cn.oyzh.easyshell.db.handler;

import cn.oyzh.common.file.FastFileWriter;
import cn.oyzh.easyshell.data.ShellDataHandler;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.data.handler.mysql.ShellMysqlDataDumpHandler;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;

import java.io.File;
import java.io.IOException;

/**
 * @author oyzh
 * @since 2024/08/22
 */
public abstract class DBDataDumpHandler extends ShellDataHandler {

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
    //    protected ShellMysqlClient dbClient;

    /**
     * 1. 库
     * 2. 表
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
    protected int queryLimit = 1000;

    /**
     * 方言
     */
    protected DBDialect dialect;

    public DBDataDumpHandler(String dbName) {
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
    protected abstract void writeTail() throws IOException;

    /**
     * 是否导出数据
     *
     * @return 结果
     */
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
    public static DBDataDumpHandler newHandler(ShellMysqlClient dbClient, String dbName) {
        DBDataDumpHandler handler = switch (dbClient.dialect()) {
            case MYSQL -> new ShellMysqlDataDumpHandler(dbClient, dbName);
            default -> null;
        };
        if (handler != null) {
            handler.dialect = dbClient.dialect();
        }
        return handler;
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

    public DBDialect getDialect() {
        return dialect;
    }

    public void setDialect(DBDialect dialect) {
        this.dialect = dialect;
    }
}

