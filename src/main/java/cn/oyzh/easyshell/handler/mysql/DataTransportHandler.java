package cn.oyzh.easyshell.handler.mysql;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportEvent;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportFunction;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportProcedure;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportTable;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportTrigger;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportView;
import cn.oyzh.easyshell.mysql.DBDialect;
import cn.oyzh.easyshell.mysql.MysqlClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/06
 */
public abstract class DataTransportHandler extends DataHandler {

    /**
     * 来源客户端
     */
    protected MysqlClient sourceClient;

    /**
     * 目标客户端
     */
    protected MysqlClient targetClient;

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
    protected int selectLimit = 5000;

    /**
     * 批量限制
     */
    protected int batchLimit = 250;

    /**
     * 视图
     */
    protected List<DataTransportView> views;

    /**
     * 表
     */
    protected List<DataTransportTable> tables;

    /**
     * 触发器
     */
    protected List<DataTransportTrigger> triggers;

    /**
     * 函数
     */
    protected List<DataTransportFunction> functions;

    /**
     * 过程
     */
    protected List<DataTransportProcedure> procedures;

    /**
     * 事件
     */
    protected List<DataTransportEvent> events;

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
                    ThreadUtil.submitVirtual(tasks);
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
     * @return DataTransportHandler
     */
    public static DataTransportHandler newHandler(DBDialect dialect) {
        DataTransportHandler handler = switch (dialect) {
            case MYSQL -> new MysqlDataTransportHandler();
            default -> null;
        };
        if (handler != null) {
            handler.setDialect(dialect);
        }
        return handler;
    }

    public MysqlClient getSourceClient() {
        return sourceClient;
    }

    public void setSourceClient(MysqlClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    public MysqlClient getTargetClient() {
        return targetClient;
    }

    public void setTargetClient(MysqlClient targetClient) {
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

    public List<DataTransportView> getViews() {
        return views;
    }

    public void setViews(List<DataTransportView> views) {
        this.views = views;
    }

    public List<DataTransportTable> getTables() {
        return tables;
    }

    public void setTables(List<DataTransportTable> tables) {
        this.tables = tables;
    }

    public List<DataTransportTrigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<DataTransportTrigger> triggers) {
        this.triggers = triggers;
    }

    public List<DataTransportFunction> getFunctions() {
        return functions;
    }

    public void setFunctions(List<DataTransportFunction> functions) {
        this.functions = functions;
    }

    public List<DataTransportProcedure> getProcedures() {
        return procedures;
    }

    public void setProcedures(List<DataTransportProcedure> procedures) {
        this.procedures = procedures;
    }

    public List<DataTransportEvent> getEvents() {
        return events;
    }

    public void setEvents(List<DataTransportEvent> events) {
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

