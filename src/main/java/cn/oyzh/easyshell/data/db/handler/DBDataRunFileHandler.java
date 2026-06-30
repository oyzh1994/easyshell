package cn.oyzh.easyshell.data.db.handler;

import cn.oyzh.easyshell.data.ShellBatchInsertable;
import cn.oyzh.easyshell.data.ShellDataRunFileHandler;
import cn.oyzh.easyshell.data.db.DBDialect;
import cn.oyzh.easyshell.domain.ShellConnect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/29
 */
public abstract class DBDataRunFileHandler<D> extends ShellDataRunFileHandler implements ShellBatchInsertable<D> {

    /**
     * 库名称
     */
    protected String dbName;

    /**
     * 文件
     */
    protected File file;

    /**
     * 连接信息
     */
    protected ShellConnect connect;

    /**
     * 插入限制，insertLimit/batchLimit=连接数，mysql默认是151，尽量不要超过连接数
     */
    protected int insertLimit = 5000;

    /**
     * 批量限制
     */
    protected int batchLimit = 50;

    /**
     * 遇到错误时继续
     */
    protected boolean continueWithErrors = true;

    /**
     * 方言
     */
    protected DBDialect dialect;

    public DBDataRunFileHandler(String dbName) {
        this.dbName = dbName;
    }

    /**
     * 设置文件
     *
     * @param file sql文件
     * @return 当前对象
     */
    public DBDataRunFileHandler<D> file(File file) {
        this.file = file;
        return this;
    }

    /**
     * 插入集合
     */
    protected List<D> insertList;

    @Override
    public List<D> getInsertList() {
        if (this.insertList == null) {
            this.insertList = new ArrayList<>();
        }
        return this.insertList;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ShellConnect connect() {
        return connect;
    }

    public DBDataRunFileHandler<D> connect(ShellConnect connect) {
        this.connect = connect;
        return this;
    }

    @Override
    public int getBatchLimit() {
        return batchLimit;
    }

    public void setBatchLimit(int batchLimit) {
        this.batchLimit = batchLimit;
    }

    @Override
    public int getInsertLimit() {
        return insertLimit;
    }

    public void setInsertLimit(int insertLimit) {
        this.insertLimit = insertLimit;
    }

    public boolean isContinueWithErrors() {
        return continueWithErrors;
    }

    public void setContinueWithErrors(boolean continueWithErrors) {
        this.continueWithErrors = continueWithErrors;
    }

    public DBDialect getDialect() {
        return dialect;
    }

    public void setDialect(DBDialect dialect) {
        this.dialect = dialect;
    }
}

