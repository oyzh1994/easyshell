package cn.oyzh.easyshell.db.handler;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportEvent;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportFunction;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportProcedure;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportTable;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportTrigger;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataTransportView;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.handler.mysql.ShellMysqlDataTransportHandler;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/06
 */
public abstract class DBDataTransportHandler extends DBDataHandler {

    /**
     * 来源客户端
     */
    protected ShellMysqlClient sourceClient;

    /**
     * 目标客户端
     */
    protected ShellMysqlClient targetClient;

    /**
     * 来源库
     */
    protected String sourceDatabase;

    /**
     * 目标库
     */
    protected String targetDatabase;

    /**
     * 查询限制
     */
    protected int selectLimit = 1000;

    /**
     * 批量限制
     */
    protected int batchLimit = 100;

    /**
     * 视图
     */
    protected List<ShellMysqlDataTransportView> views;

    /**
     * 表
     */
    protected List<ShellMysqlDataTransportTable> tables;

    /**
     * 触发器
     */
    protected List<ShellMysqlDataTransportTrigger> triggers;

    /**
     * 函数
     */
    protected List<ShellMysqlDataTransportFunction> functions;

    /**
     * 过程
     */
    protected List<ShellMysqlDataTransportProcedure> procedures;

    /**
     * 事件
     */
    protected List<ShellMysqlDataTransportEvent> events;

    /**
     * 方言
     */
    private DBDialect dialect;

    /**
     * 执行传输
     */
    public abstract void doTransport() throws Exception;

    /**
     * 插入集合
     */
    protected List<String> insertList;

    /**
     * 添加插入sql
     *
     * @param sqlList sql列表
     */
    protected void addInsertSql(List<String> sqlList) {
        if (CollectionUtil.isNotEmpty(sqlList)) {
            if (this.insertList == null) {
                this.insertList = new ArrayList<>();
            }
            this.insertList.addAll(sqlList);
            if (this.insertList.size() >= this.batchLimit) {
                this.doBatchInsert();
            }
        }
    }

    /**
     * 执行批量插入
     */
    protected void doBatchInsert() {
        if (CollectionUtil.isNotEmpty(this.insertList)) {
            try {
                if (this.insertList.size() <= this.batchLimit) {
                    this.doBatchInsert(this.insertList, false);
                } else {
                    List<List<String>> lists = CollectionUtil.split(this.insertList, this.batchLimit);
                    List<Runnable> tasks = new ArrayList<>();
                    for (List<String> list : lists) {
                        tasks.add(() -> this.doBatchInsert(list, true));
                    }
                    ThreadUtil.submit(tasks);
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
            int result = this.targetClient.insertBatch(this.targetDatabase, sqlList, parallel);
            this.processedIncr(result);
        } catch (Exception ex) {
            this.processedDecr(sqlList.size());
            throw ex;
        }
    }

    /**
     * 创建新的处理器
     *
     * @param dialect 方言
     * @return DBDataTransportHandler
     */
    public static DBDataTransportHandler newHandler(DBDialect dialect) {
        DBDataTransportHandler handler = switch (dialect) {
            case MYSQL -> new ShellMysqlDataTransportHandler();
            default -> null;
        };
        if (handler != null) {
            handler.setDialect(dialect);
        }
        return handler;
    }

    public ShellMysqlClient getSourceClient() {
        return sourceClient;
    }

    public void setSourceClient(ShellMysqlClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    public ShellMysqlClient getTargetClient() {
        return targetClient;
    }

    public void setTargetClient(ShellMysqlClient targetClient) {
        this.targetClient = targetClient;
    }

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    public String getTargetDatabase() {
        return targetDatabase;
    }

    public void setTargetDatabase(String targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    public int getSelectLimit() {
        return selectLimit;
    }

    public void setSelectLimit(int selectLimit) {
        this.selectLimit = selectLimit;
    }

    public int getBatchLimit() {
        return batchLimit;
    }

    public void setBatchLimit(int batchLimit) {
        this.batchLimit = batchLimit;
    }

    public List<ShellMysqlDataTransportView> getViews() {
        return views;
    }

    public void setViews(List<ShellMysqlDataTransportView> views) {
        this.views = views;
    }

    public List<ShellMysqlDataTransportTable> getTables() {
        return tables;
    }

    public void setTables(List<ShellMysqlDataTransportTable> tables) {
        this.tables = tables;
    }

    public List<ShellMysqlDataTransportTrigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<ShellMysqlDataTransportTrigger> triggers) {
        this.triggers = triggers;
    }

    public List<ShellMysqlDataTransportFunction> getFunctions() {
        return functions;
    }

    public void setFunctions(List<ShellMysqlDataTransportFunction> functions) {
        this.functions = functions;
    }

    public List<ShellMysqlDataTransportProcedure> getProcedures() {
        return procedures;
    }

    public void setProcedures(List<ShellMysqlDataTransportProcedure> procedures) {
        this.procedures = procedures;
    }

    public List<ShellMysqlDataTransportEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ShellMysqlDataTransportEvent> events) {
        this.events = events;
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

