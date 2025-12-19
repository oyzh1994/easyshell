package cn.oyzh.easyshell.db.handler;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.handler.mysql.ShellMysqlDataRunSqlFileHandler;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author oyzh
 * @since 2024/08/29
 */
public abstract class DBDataRunSqlFileHandler extends DBDataHandler {

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
    protected ShellMysqlClient dbClient;

    /**
     * 连接信息
     */
    protected ShellConnect dbInfo;

    /**
     * 插入限制
     */
    protected int insertLimit = 1000;

    /**
     * 批量限制
     */
    protected int batchLimit = 100;

    /**
     * 遇到错误时继续
     */
    protected boolean continueWithErrors = true;

    /**
     * 方言
     */
    private DBDialect dialect;

    public DBDataRunSqlFileHandler(ShellMysqlClient dbClient, String dbName) {
        this.dbClient = dbClient;
        this.dbName = dbName;
    }

    /**
     * 设置sql文件
     *
     * @param sqlFile sql文件
     * @return 当前对象
     */
    public DBDataRunSqlFileHandler sqlFile(File sqlFile) {
        this.sqlFile = sqlFile;
        return this;
    }

    /**
     * 运行sql文件
     */
    public abstract void runSqlFile() throws Exception;

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
                    this.doBatchInsert(this.insertList, false);
                } else {
                    AtomicReference<Exception> exceptionRef = new AtomicReference<>();
                    List<List<String>> lists = CollectionUtil.split(this.insertList, this.batchLimit);
                    List<Runnable> tasks = new ArrayList<>();
                    for (List<String> list : lists) {
                        tasks.add(() -> {
                            try {
                                this.doBatchInsert(list, true);
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
     * @param sqlList  sql列表
     * @param parallel 是否并发
     */
    protected void doBatchInsert(List<String> sqlList, boolean parallel) {
        try {
            int result = this.dbClient.insertBatch(this.dbName, sqlList, parallel);
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
    public static DBDataRunSqlFileHandler newHandler(ShellMysqlClient dbClient, String dbName) {
        DBDataRunSqlFileHandler handler = switch (dbClient.dialect()) {
            case MYSQL -> new ShellMysqlDataRunSqlFileHandler(dbClient, dbName);
            default -> null;
        };
        if (handler != null) {
            handler.setDialect(dbClient.dialect());
        }
        return handler;
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

    public ShellMysqlClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(ShellMysqlClient dbClient) {
        this.dbClient = dbClient;
    }

    public ShellConnect getDbInfo() {
        return dbInfo;
    }

    public DBDataRunSqlFileHandler setDbInfo(ShellConnect dbInfo) {
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

    public DBDialect getDialect() {
        return dialect;
    }

    public void setDialect(DBDialect dialect) {
        this.dialect = dialect;
    }

    public List<String> getInsertList() {
        return insertList;
    }

    public void setInsertList(List<String> insertList) {
        this.insertList = insertList;
    }
}

