package cn.oyzh.easyshell.data.sqlserver.handler;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.easyshell.sqlserver.trigger.SqlServerTrigger;
import cn.oyzh.easyshell.sqlserver.column.SqlServerSelectColumnParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.record.SqlServerSelectRecordParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerSequence;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.type.SqlServerTypeDefinition;
import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerDataImportHelper;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.data.handler.DBDataTransportHandler;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlServer数据传输处理器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerDataTransportHandler extends DBDataTransportHandler<String> {

    /**
     * 来源客户端
     */
    protected ShellSqlServerClient sourceClient;

    /**
     * 目标客户端
     */
    protected ShellSqlServerClient targetClient;

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
     * 序列
     */
    protected List<DBDataTransportObject> sequences;

    /**
     * 构造 SqlServer数据传输处理器
     */
    public ShellSqlServerDataTransportHandler() {
        super(DBDialect.SQLSERVER);
    }

    @Override
    protected void doActionInternal() throws Exception {
        this.message("Transport Starting");
        try {
            // 类型和独立序列是表结构的依赖，必须先于表传输
            this.transportTypes();
            this.transportSequences();
            if (CollectionUtil.isNotEmpty(this.tables)) {
                // 全部表
                List<SqlServerTable> allTables = new ArrayList<>();
                for (DBDataTransportObject t : this.tables) {
                    SqlServerTable table = new SqlServerTable();
                    table.setName(t.getName());
                    table.setSchema(this.sourceDatabase);
                    allTables.add(table);
                }
                // 按外键依赖排序，父表在前
                this.message("sort tables");
                ShellSqlServerUtil.sortTablesInHandler(allTables, this.sourceClient, this);
                // 删除目标对象时必须逆序，先删子表再删父表，避免被外键阻塞
                for (int i = allTables.size() - 1; i >= 0; i--) {
                    this.dropTargetTable(allTables.get(i).getName());
                }
                for (SqlServerTable table : allTables) {
                    this.transportTable(table.getName());
                }
                //// 传输表实际业务
                //ShellSqlServerUtil.sortTablesByCallback(this.sourceClient, allTables, (ExceptionConsumer<SqlServerTable>) t -> {
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
     * 传输用户自定义别名类型和表类型。
     */
    private void transportTypes() {
        List<SqlServerTypeDefinition> types = this.sourceClient.selectTypes(this.sourceDatabase);
        for (int i = types.size() - 1; i >= 0; i--) {
            SqlServerTypeDefinition type = types.get(i);
            String drop = "DROP TYPE IF EXISTS "
                    + DBUtil.wrap(this.targetDatabase, type.getName(), DBDialect.SQLSERVER) + ";";
            this.targetClient.executeSqlSimple(this.targetDatabase, drop);
        }
        for (SqlServerTypeDefinition type : types) {
            String sql = ShellSqlServerUtil.replaceSchemaForDdl(
                    type.getCreateDefinition(), this.sourceDatabase, this.targetDatabase);
            this.targetClient.executeSqlSimple(this.targetDatabase, sql);
        }
        if (CollectionUtil.isNotEmpty(types)) {
            this.message("Transport custom types");
        }
    }

    /**
     * 传输独立序列，作为表默认值的依赖。
     */
    private void transportSequences() throws Exception {
        List<SqlServerSequence> sourceSequences = this.sourceClient.selectSequences(this.sourceDatabase);
        if (CollectionUtil.isNotEmpty(sourceSequences)) {
            for (SqlServerSequence sequence : sourceSequences) {
                this.transportSequence(sequence.getName());
            }
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
            // 创建表
            String createDefinition = this.sourceClient.showCreateTable(this.sourceDatabase, tableName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellSqlServerUtil.replaceSchemaForDdl(
                    createDefinition, this.sourceDatabase, this.targetDatabase);
            this.executeDefinition(createDefinition);
            this.message("Create Table " + tableName);
            this.processedIncr();

            // 传输表数据
            if (this.dataType == 0) {
                this.message("Transport Table " + tableName + " Starting");
                SqlServerColumns columns = new SqlServerColumns(this.sourceClient.selectColumns(new SqlServerSelectColumnParam(this.sourceDatabase, tableName)));
                long start = 0;
                while (true) {
                    this.checkInterrupt();
                    try {
                        SqlServerSelectRecordParam param = new SqlServerSelectRecordParam();
                        param.setStart(start);
                        param.setReadonly(true);
                        param.setTableName(tableName);
                        param.setSchema(this.sourceDatabase);
                        param.setLimit((long) this.selectLimit);
                        List<SqlServerRecord> records = this.sourceClient.selectRecords(param);
                        if (CollectionUtil.isEmpty(records)) {
                            break;
                        }
                        List<String> list = ShellSqlServerDataImportHelper.toInsertSql(columns, records, this.targetDatabase);
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
                // 收尾批量插入
                this.doBatchInsert();
                // SqlServer序列值重置
                for (String sql : ShellSqlServerDataImportHelper.buildSequenceResetSql(columns, this.targetDatabase)) {
                    this.targetClient.executeSqlSimple(this.targetDatabase, sql);
                }
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

    private void dropTargetTable(String tableName) {
        String dropTable = "DROP TABLE IF EXISTS "
                + DBUtil.wrap(this.targetDatabase, tableName, DBDialect.SQLSERVER) + ";";
        this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
        this.message("Drop Table " + tableName);
        this.processedIncr();
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
            String dropTable = "DROP VIEW IF EXISTS " + DBUtil.wrap(this.targetDatabase, viewName, DBDialect.SQLSERVER) + ";";
            this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
            this.message("Drop View " + viewName);
            this.processedIncr();

            // 创建视图
            String createDefinition = this.sourceClient.showCreateView(this.sourceDatabase, viewName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellSqlServerUtil.replaceSchemaForDdl(
                    createDefinition, this.sourceDatabase, this.targetDatabase);
            this.executeDefinition(createDefinition);
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
            // 删除函数：重载函数必须带输入参数签名
            SqlServerFunction sourceFunction = this.sourceClient.selectFunction(this.sourceDatabase, functionName);
            String dropTable = sourceFunction == null || sourceFunction.getName() == null
                    ? "DROP FUNCTION IF EXISTS " + DBUtil.wrap(this.targetDatabase, functionName, DBDialect.SQLSERVER) + ";"
                    : ShellSqlServerUtil.dropRoutineSql(this.targetDatabase, functionName, sourceFunction.getParams(), false);
            this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
            this.message("Drop Function " + functionName);
            this.processedIncr();

            // 创建函数
            String createDefinition = this.sourceClient.showCreateFunction(this.sourceDatabase, functionName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellSqlServerUtil.replaceSchemaForDdl(
                    createDefinition, this.sourceDatabase, this.targetDatabase);
            this.executeDefinition(createDefinition);
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
            // 删除过程：重载过程必须带输入参数签名
            SqlServerProcedure sourceProcedure = this.sourceClient.selectProcedure(this.sourceDatabase, procedureName);
            String dropTable = sourceProcedure == null || sourceProcedure.getName() == null
                    ? "DROP PROCEDURE IF EXISTS " + DBUtil.wrap(this.targetDatabase, procedureName, DBDialect.SQLSERVER) + ";"
                    : ShellSqlServerUtil.dropRoutineSql(this.targetDatabase, procedureName, sourceProcedure.getParams(), true);
            this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
            this.message("Drop Procedure " + procedureName);
            this.processedIncr();

            // 创建过程
            String createDefinition = this.sourceClient.showCreateProcedure(this.sourceDatabase, procedureName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellSqlServerUtil.replaceSchemaForDdl(
                    createDefinition, this.sourceDatabase, this.targetDatabase);
            this.executeDefinition(createDefinition);
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
            // 创建触发器定义
            String createDefinition = this.sourceClient.showCreateTrigger(this.sourceDatabase, triggerName);
            // SqlServer删除触发器必须指定所属表
            String triggerTable = this.parseTriggerTable(createDefinition);
            String dropTable = "DROP TRIGGER IF EXISTS "
                    + DBUtil.wrap(this.targetDatabase, triggerName, DBDialect.SQLSERVER) + ";";
            this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
            this.message("Drop Trigger " + triggerName);
            this.processedIncr();

            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellSqlServerUtil.replaceSchemaForDdl(
                    createDefinition, this.sourceDatabase, this.targetDatabase);
            this.executeDefinition(createDefinition);
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

    /**
     * 传输序列
     *
     * @param sequenceName 序列名称
     * @throws InterruptedException 异常
     */
    private void transportSequence(String sequenceName) throws Exception {
        try {
            this.checkInterrupt();
            // 删除触发器
            String dropTable = "DROP SEQUENCE IF EXISTS " + DBUtil.wrap(this.targetDatabase, sequenceName, DBDialect.SQLSERVER) + ";";
            this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
            this.message("Drop Sequence " + sequenceName);
            this.processedIncr();

            // 创建触发器
            String createDefinition = this.sourceClient.showCreateSequence(this.sourceDatabase, sequenceName);
            // TODO: 去除特定架构
            //            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellSqlServerUtil.replaceSchemaForDdl(
                    createDefinition, this.sourceDatabase, this.targetDatabase);
            this.executeDefinition(createDefinition);
            SqlServerSequence sourceSequence = this.sourceClient.selectSequence(this.sourceDatabase, sequenceName);
            if (sourceSequence != null && sourceSequence.getCurrentVal() != null) {
                String sequence = DBUtil.wrap(this.targetDatabase, sequenceName, DBDialect.SQLSERVER);
                    java.math.BigInteger next = sourceSequence.getCurrentVal();
                    if (sourceSequence.getIncrementBy() != null) {
                        next = next.add(sourceSequence.getIncrementBy());
                    }
                    this.targetClient.executeSqlSimple(this.targetDatabase,
                            "ALTER SEQUENCE " + sequence + " RESTART WITH " + next + ";");
            }
            this.message("Create Sequence " + sequenceName);
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
     * 执行建对象定义，定义中可能包含建表、注释、索引等多条语句
     *
     * @param definition 创建定义
     */
    private void executeDefinition(String definition) {
        for (String sql : ShellSqlServerUtil.splitSqlServerBatches(definition)) {
            this.targetClient.executeSqlSimple(this.targetDatabase, sql);
        }
    }

    /**
     * 从触发器创建语句中解析所属表名称
     *
     * @param definition 触发器创建语句
     * @return 表名称
     */
    private String parseTriggerTable(String definition) {
        if (StringUtil.isBlank(definition)) {
            return null;
        }
        Matcher matcher = Pattern.compile("(?is)\\bON\\s+((?:\\[[^\\]]+\\]|[\\p{L}\\p{N}_$]+)(?:\\s*\\.\\s*(?:\\[[^\\]]+\\]|[\\p{L}\\p{N}_$]+))?)").matcher(definition);
        if (!matcher.find()) {
            return null;
        }
        String table = matcher.group(1);
        table = ShellSqlServerUtil.removeSchema(table, this.sourceDatabase);
        return table.replace("[", "").replace("]", "").replace(" ", "");
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
    public ShellSqlServerClient getSourceClient() {
        return sourceClient;
    }

    /**
     * 设置来源客户端
     *
     * @param sourceClient 来源客户端
     */
    public void setSourceClient(ShellSqlServerClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    /**
     * 获取目标客户端
     *
     * @return 目标客户端
     */
    public ShellSqlServerClient getTargetClient() {
        return targetClient;
    }

    /**
     * 设置目标客户端
     *
     * @param targetClient 目标客户端
     */
    public void setTargetClient(ShellSqlServerClient targetClient) {
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

    public List<DBDataTransportObject> getSequences() {
        return sequences;
    }

    public void setSequences(List<DBDataTransportObject> sequences) {
        this.sequences = sequences;
    }

    @Override
    public boolean enableParallel() {
        return false;
    }
}

