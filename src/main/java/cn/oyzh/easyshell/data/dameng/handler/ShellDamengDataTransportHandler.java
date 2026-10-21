package cn.oyzh.easyshell.data.dameng.handler;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.column.DamengSelectColumnParam;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.util.dameng.ShellDamengDataUtil;
import cn.oyzh.easyshell.util.dameng.ShellDamengUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.data.handler.DBDataTransportHandler;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Dameng数据传输处理器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengDataTransportHandler extends DBDataTransportHandler<String> {

    /**
     * 来源客户端
     */
    protected ShellDamengClient sourceClient;

    /**
     * 目标客户端
     */
    protected ShellDamengClient targetClient;

    /**
     * 视图
     */
    protected List<DBDataTransportObject> views;

    /**
     * 表
     */
    protected List<DBDataTransportObject> tables;

    /**
     * 触发器
     */
    protected List<DBDataTransportObject> triggers;

    /**
     * 函数
     */
    protected List<DBDataTransportObject> functions;

    /**
     * 过程
     */
    protected List<DBDataTransportObject> procedures;

    /**
     * 构造 Dameng数据传输处理器
     */
    public ShellDamengDataTransportHandler() {
        super(DBDialect.DAMENG);
    }

    @Override
    public void doTransport() throws Exception {
        this.message("Transport Starting");
        try {
            if (CollectionUtil.isNotEmpty(this.tables)) {
                // 全部表
                List<DamengTable> allTables = new ArrayList<>();
                for (DBDataTransportObject t : this.tables) {
                    DamengTable table = new DamengTable();
                    table.setName(t.getName());
                    table.setSchema(this.sourceDatabase);
                    allTables.add(table);
                }
                // 按外键依赖排序，父表在前
                this.message("sort tables");
                ShellDamengUtil.sortTablesInHandler(allTables, this.sourceClient, this);
                for (DamengTable table : allTables) {
                    this.transportTable(table.getName());
                }
                //// 传输表实际业务
                //ShellDamengUtil.sortTablesByCallback(this.sourceClient, allTables, (ExceptionConsumer<DamengTable>) t -> {
                //    this.transportTable(t.getName());
                //});
            }
            if (CollectionUtil.isNotEmpty(this.views)) {
                for (DBDataTransportObject view : this.views) {
                    this.transportView(view.getName());
                }
            }
            if (CollectionUtil.isNotEmpty(this.functions)) {
                for (DBDataTransportObject function : this.functions) {
                    this.transportFunction(function.getName());
                }
            }
            if (CollectionUtil.isNotEmpty(this.procedures)) {
                for (DBDataTransportObject procedure : this.procedures) {
                    this.transportProcedure(procedure.getName());
                }
            }
            if (CollectionUtil.isNotEmpty(this.triggers)) {
                for (DBDataTransportObject trigger : this.triggers) {
                    this.transportTrigger(trigger.getName());
                }
            }
        } catch (Exception ex) {
            this.exception(ex);
        } finally {
            this.message("Transport Finished");
        }
    }

    /**
     * 传输表
     *
     * @param tableName 表名称
     * @throws Exception 异常
     */
    private void transportTable(String tableName) throws Exception {
        try {
            this.checkInterrupt();
            // 删除表
            String dropTable = "DROP TABLE IF EXISTS " + DBUtil.wrap(tableName, DBDialect.DAMENG) + ";";
            this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
            this.message("Drop Table " + tableName);
            this.processedIncr();

            // 创建表
            String createDefinition = this.sourceClient.showCreateTable(this.sourceDatabase, tableName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellDamengUtil.removeSchema(createDefinition,this.sourceDatabase);
            this.targetClient.executeSqlSimple(this.targetDatabase, createDefinition);
            this.message("Create Table " + tableName);
            this.processedIncr();

            // 传输表数据
            if (this.dataType == 0) {
                this.message("Transport Table " + tableName + " Starting");
                DamengColumns columns = new DamengColumns(this.sourceClient.selectColumns(new DamengSelectColumnParam(this.sourceDatabase, tableName)));
                boolean hasIdentity = columns.hasAutoIncrement() && !StringUtil.containsIgnoreCase(createDefinition, " AUTO_INCREMENT ");
                if (hasIdentity) {
                    String line0 = "SET IDENTITY_INSERT " + DBUtil.wrap(tableName, DBDialect.DAMENG) + " ON;";
                    this.addInsert(line0);
                }
                long start = 0;
                while (true) {
                    this.checkInterrupt();
                    try {
                        DamengSelectRecordParam param = new DamengSelectRecordParam();
                        param.setStart(start);
                        param.setReadonly(true);
                        param.setTableName(tableName);
                        param.setSchema(this.sourceDatabase);
                        param.setLimit((long) this.selectLimit);
                        List<DamengRecord> records = this.sourceClient.selectRecords(param);
                        if (CollectionUtil.isEmpty(records)) {
                            break;
                        }
                        List<String> list = ShellDamengDataUtil.toInsertSql(columns, records, true);
                        this.addInsert(list);
                        start += this.selectLimit;
                        // 更新状态
                        this.processed(0);
                    } catch (Exception ex) {
                        if (this.isContinueWithErrors()) {
                            this.exception(ex);
                            this.processedDecr();
                            this.getInsertList().clear();
                        } else {
                            throw ex;
                        }
                    }
                }
                if (hasIdentity) {
                    String line1 = "SET IDENTITY_INSERT " + DBUtil.wrap(tableName, DBDialect.DAMENG) + " OFF;";
                    this.addInsert(line1);
                }
                // 收尾批量插入
                this.doBatchInsert();
                this.message("Transport Table " + tableName + " Finished");
            }
        } catch (Exception ex) {
            if (this.isContinueWithErrors()) {
                this.exception(ex);
                this.processedDecr();
                this.getInsertList().clear();
            } else {
                throw ex;
            }
        }
    }

    /**
     * 传输视图
     *
     * @param viewName 视图名称
     * @throws InterruptedException 异常
     */
    private void transportView(String viewName) throws Exception {
        try {
            this.checkInterrupt();
            // 删除视图
            String dropTable = "DROP VIEW IF EXISTS " + DBUtil.wrap(viewName, DBDialect.DAMENG) + ";";
            this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
            this.message("Drop View " + viewName);
            this.processedIncr();

            // 创建视图
            String createDefinition = this.sourceClient.showCreateView(this.sourceDatabase, viewName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellDamengUtil.removeSchema(createDefinition,this.sourceDatabase);
            this.targetClient.executeSqlSimple(this.targetDatabase, createDefinition);
            this.message("Create View " + viewName);
            this.processedIncr();
        } catch (Exception ex) {
            if (this.isContinueWithErrors()) {
                this.exception(ex);
                this.processedDecr();
                this.getInsertList().clear();
            } else {
                throw ex;
            }
        }
    }

    /**
     * 传输函数
     *
     * @param functionName 函数名称
     * @throws InterruptedException 异常
     */
    private void transportFunction(String functionName) throws Exception {
        try {
            this.checkInterrupt();
            // 删除函数
            String dropTable = "DROP FUNCTION IF EXISTS " + DBUtil.wrap(functionName, DBDialect.DAMENG) + ";";
            this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
            this.message("Drop Function " + functionName);
            this.processedIncr();

            // 创建函数
            String createDefinition = this.sourceClient.showCreateFunction(this.sourceDatabase, functionName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellDamengUtil.removeSchema(createDefinition,this.sourceDatabase);
            this.targetClient.executeSqlSimple(this.targetDatabase, createDefinition);
            this.message("Create Function " + functionName);
            this.processedIncr();
        } catch (Exception ex) {
            if (this.isContinueWithErrors()) {
                this.exception(ex);
                this.processedDecr();
                this.getInsertList().clear();
            } else {
                throw ex;
            }
        }
    }

    /**
     * 传输过程
     *
     * @param procedureName 过程名称
     * @throws InterruptedException 异常
     */
    private void transportProcedure(String procedureName) throws Exception {
        try {
            this.checkInterrupt();
            // 删除过程
            String dropTable = "DROP PROCEDURE IF EXISTS " + DBUtil.wrap(procedureName, DBDialect.DAMENG) + ";";
            this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
            this.message("Drop Procedure " + procedureName);
            this.processedIncr();

            // 创建过程
            String createDefinition = this.sourceClient.showCreateProcedure(this.sourceDatabase, procedureName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellDamengUtil.removeSchema(createDefinition,this.sourceDatabase);
            this.targetClient.executeSqlSimple(this.targetDatabase, createDefinition);
            this.message("Create Procedure " + procedureName);
            this.processedIncr();
        } catch (Exception ex) {
            if (this.isContinueWithErrors()) {
                this.exception(ex);
                this.processedDecr();
                this.getInsertList().clear();
            } else {
                throw ex;
            }
        }
    }

    /**
     * 传输触发器
     *
     * @param triggerName 触发器名称
     * @throws InterruptedException 异常
     */
    private void transportTrigger(String triggerName) throws Exception {
        try {
            this.checkInterrupt();
            // 删除触发器
            String dropTable = "DROP TRIGGER IF EXISTS " + DBUtil.wrap(triggerName, DBDialect.DAMENG) + ";";
            this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
            this.message("Drop Trigger " + triggerName);
            this.processedIncr();

            // 创建触发器
            String createDefinition = this.sourceClient.showCreateTrigger(this.sourceDatabase, triggerName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellDamengUtil.removeSchema(createDefinition,this.sourceDatabase);
            this.targetClient.executeSqlSimple(this.targetDatabase, createDefinition);
            this.message("Create Trigger " + triggerName);
            this.processedIncr();
        } catch (Exception ex) {
            if (this.isContinueWithErrors()) {
                this.exception(ex);
                this.processedDecr();
                this.getInsertList().clear();
            } else {
                throw ex;
            }
        }
    }

    @Override
    public void doBatchInsert(List<String> list, boolean parallel) {
        try {
            int result = this.targetClient.insertBatch(this.targetDatabase, list, parallel);
            this.processedIncr(result);
        } catch (Exception ex) {
            this.processedDecr(list.size());
            throw ex;
        }
    }

    /**
     * 获取来源客户端
     *
     * @return 来源客户端
     */
    public ShellDamengClient getSourceClient() {
        return sourceClient;
    }

    /**
     * 设置来源客户端
     *
     * @param sourceClient 来源客户端
     */
    public void setSourceClient(ShellDamengClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    /**
     * 获取目标客户端
     *
     * @return 目标客户端
     */
    public ShellDamengClient getTargetClient() {
        return targetClient;
    }

    /**
     * 设置目标客户端
     *
     * @param targetClient 目标客户端
     */
    public void setTargetClient(ShellDamengClient targetClient) {
        this.targetClient = targetClient;
    }

    /**
     * 获取视图列表
     *
     * @return 视图列表
     */
    public List<DBDataTransportObject> getViews() {
        return views;
    }

    /**
     * 设置视图列表
     *
     * @param views 视图列表
     */
    public void setViews(List<DBDataTransportObject> views) {
        this.views = views;
    }

    /**
     * 获取表列表
     *
     * @return 表列表
     */
    public List<DBDataTransportObject> getTables() {
        return tables;
    }

    /**
     * 设置表列表
     *
     * @param tables 表列表
     */
    public void setTables(List<DBDataTransportObject> tables) {
        this.tables = tables;
    }

    /**
     * 获取触发器列表
     *
     * @return 触发器列表
     */
    public List<DBDataTransportObject> getTriggers() {
        return triggers;
    }

    /**
     * 设置触发器列表
     *
     * @param triggers 触发器列表
     */
    public void setTriggers(List<DBDataTransportObject> triggers) {
        this.triggers = triggers;
    }

    /**
     * 获取函数列表
     *
     * @return 函数列表
     */
    public List<DBDataTransportObject> getFunctions() {
        return functions;
    }

    /**
     * 设置函数列表
     *
     * @param functions 函数列表
     */
    public void setFunctions(List<DBDataTransportObject> functions) {
        this.functions = functions;
    }

    /**
     * 获取过程列表
     *
     * @return 过程列表
     */
    public List<DBDataTransportObject> getProcedures() {
        return procedures;
    }

    /**
     * 设置过程列表
     *
     * @param procedures 过程列表
     */
    public void setProcedures(List<DBDataTransportObject> procedures) {
        this.procedures = procedures;
    }

    @Override
    public boolean enableParallel() {
        return false;
    }
}

