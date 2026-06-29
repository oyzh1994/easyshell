package cn.oyzh.easyshell.data.mongo.handler;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.script.MongoScriptEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author oyzh
 * @since 2024/08/29
 */
public abstract class DBDataRunFileHandler extends DBDataHandler {

    /**
     * 库名称
     */
    protected String dbName;

    /**
     * sql文件
     */
    protected File sqlFile;

    /**
     * db客户端
     */
    protected ShellMongoClient dbClient;

    /**
     * 连接信息
     */
    protected ShellConnect dbInfo;

    /**
     * 插入限制，insertLimit/batchLimit=连接数，mysql默认是151，尽量不要超过连接数
     */
    protected int insertLimit = 500;

    /**
     * 批量限制
     */
    protected int batchLimit = 50;

    /**
     * 遇到错误时继续
     */
    protected boolean continueWithErrors = true;

    protected MongoScriptEngine engine;

    public DBDataRunFileHandler(ShellMongoClient dbClient, String dbName) {
        this.dbClient = dbClient;
        this.dbName = dbName;
        this.engine = dbClient.shellEngine();
        this.engine.db(this.dbName);
    }

    /**
     * 设置sql文件
     *
     * @param sqlFile sql文件
     * @return 当前对象
     */
    public DBDataRunFileHandler sqlFile(File sqlFile) {
        this.sqlFile = sqlFile;
        return this;
    }

    /**
     * 运行文件
     */
    public abstract void runFile() throws Exception;

    /**
     * 插入集合
     */
    protected List<String> insertList;

    /**
     * 添加插入sql
     *
     * @param sql 插入sql
     */
    protected void addInsertSql(String sql) throws Exception {
        if (StringUtil.isNotBlank(sql)) {
            if (this.insertList == null) {
                this.insertList = new ArrayList<>();
            }
            this.insertList.add(sql);
            if (this.insertList.size() >= this.insertLimit) {
                this.doBatchInsert();
            }
        }
    }

    /**
     * 执行批量插入
     */
    protected void doBatchInsert() throws Exception {
        if (CollectionUtil.isNotEmpty(this.insertList)) {
            try {
                if (this.insertList.size() <= this.batchLimit) {
                    this.doBatchInsert(this.insertList);
                } else {
                    AtomicReference<Exception> exceptionRef = new AtomicReference<>();
                    List<List<String>> lists = CollectionUtil.split(this.insertList, this.batchLimit);
                    List<Runnable> tasks = new ArrayList<>();
                    for (List<String> list : lists) {
                        tasks.add(() -> {
                            try {
                                this.doBatchInsert(list);
                            } catch (Exception ex) {
                                exceptionRef.set(ex);
                            }
                        });
                    }
                    ThreadUtil.submit(tasks);
                    if (exceptionRef.get() != null) {
                        throw exceptionRef.get();
                    }
                }
            } finally {
                this.insertList.clear();
            }
        }
    }

    /**
     * 执行批量插入
     *
     * @param sqlList sql列表
     */
    protected void doBatchInsert(List<String> sqlList) {
        try {
            int result = 0;
            for (String s : sqlList)
                try {
                    Object res = this.engine.eval(s);
                    if (res != null) {
                        result++;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            this.processedIncr(result);
        } catch (Exception ex) {
            this.processedDecr(sqlList.size());
            throw ex;
        }
    }

    /**
     * 创建新的处理器
     *
     * @param dbClient db客户端
     * @param dbName   数据库
     * @return DBDataDumpHandler
     */
    public static DBDataRunFileHandler newHandler(ShellMongoClient dbClient, String dbName) {
        return new ShellMongoDataRunFileHandler(dbClient, dbName);
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public File getSqlFile() {
        return sqlFile;
    }

    public void setSqlFile(File sqlFile) {
        this.sqlFile = sqlFile;
    }

    public ShellMongoClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(ShellMongoClient dbClient) {
        this.dbClient = dbClient;
    }

    public ShellConnect getDbInfo() {
        return dbInfo;
    }

    public DBDataRunFileHandler setDbInfo(ShellConnect dbInfo) {
        this.dbInfo = dbInfo;
        return this;
    }

    public int getInsertLimit() {
        return insertLimit;
    }

    public void setInsertLimit(int insertLimit) {
        this.insertLimit = insertLimit;
    }

    public int getBatchLimit() {
        return batchLimit;
    }

    public void setBatchLimit(int batchLimit) {
        this.batchLimit = batchLimit;
    }

    public boolean isContinueWithErrors() {
        return continueWithErrors;
    }

    public void setContinueWithErrors(boolean continueWithErrors) {
        this.continueWithErrors = continueWithErrors;
    }
}

