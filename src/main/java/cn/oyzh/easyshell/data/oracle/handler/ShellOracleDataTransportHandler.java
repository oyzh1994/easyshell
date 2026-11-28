package cn.oyzh.easyshell.data.oracle.handler;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.column.OracleSelectColumnParam;
import cn.oyzh.easyshell.oracle.record.OracleInsertRecordParam;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.oracle.record.OracleSelectRecordParam;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.type.OracleTypeDefinition;
import cn.oyzh.easyshell.util.oracle.ShellOracleUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.data.handler.DBDataTransportHandler;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Oracle数据传输处理器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleDataTransportHandler extends DBDataTransportHandler<String> {

    /**
     * 来源客户端
     */
    protected ShellOracleClient sourceClient;

    /**
     * 目标客户端
     */
    protected ShellOracleClient targetClient;

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
     * 构造 Oracle数据传输处理器
     */
    public ShellOracleDataTransportHandler() {
        super(DBDialect.ORACLE);
    }

    @Override
    protected void doActionInternal() throws Exception {
        this.message("Transport Starting");
        try {
            if (!this.targetClient.existSchema(this.targetDatabase)) {
                String container = this.targetClient.selectCurrentContainer();
                this.message("Target schema \"" + this.targetDatabase + "\" does not exist in the current connection"
                        + (StringUtil.isBlank(container) ? "" : " (container: " + container + ")")
                        + ". Please check the connection Service Name (PDB) and reconnect before transporting.");
                return;
            }
            // 目标用户可能已被删除重建，丢弃缓存会话，避免 ORA-23362/ORA-01129
            this.targetClient.resetSchemaConnection(this.targetDatabase);
            if (this.needTransportTypes() && !this.transportTypes()) {
                return;
            }
            if (CollectionUtil.isNotEmpty(this.tables)) {
                // 全部表
                List<OracleTable> allTables = new ArrayList<>();
                for (DBDataTransportObject t : this.tables) {
                    OracleTable table = new OracleTable();
                    table.setName(t.getName());
                    table.setSchema(this.sourceDatabase);
                    allTables.add(table);
                }
                // 按外键依赖排序，父表在前
                this.message("sort tables");
                ShellOracleUtil.sortTablesInHandler(allTables, this.sourceClient, this);
                for (OracleTable table : allTables) {
                    this.transportTable(table.getName());
                }
                //// 传输表实际业务
                //ShellOracleUtil.sortTablesByCallback(this.sourceClient, allTables, (ExceptionConsumer<OracleTable>) t -> {
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
            if (CollectionUtil.isNotEmpty(this.sequences)) {
                for (DBDataTransportObject sequence : this.sequences) {
                    if (isSystemGeneratedSequence(sequence.getName())) {
                        this.message("Skip System Sequence " + sequence.getName());
                        continue;
                    }
                    this.transportSequence(sequence.getName());
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

    private boolean needTransportTypes() throws Exception {
        if (CollectionUtil.isEmpty(this.tables)) {
            return CollectionUtil.isNotEmpty(this.views)
                    || CollectionUtil.isNotEmpty(this.functions)
                    || CollectionUtil.isNotEmpty(this.procedures)
                    || CollectionUtil.isNotEmpty(this.triggers);
        }
        for (DBDataTransportObject table : this.tables) {
            try {
                List<OracleColumn> columns = this.sourceClient.selectColumns(
                        new OracleSelectColumnParam(this.sourceDatabase, table.getName()));
                for (OracleColumn column : columns) {
                    if (column.isObjectType() || column.isCollectionType()) {
                        return true;
                    }
                }
            } catch (Exception ex) {
                this.exception(ex);
            }
        }
        return false;
    }

    /**
     * 传输用户自定义类型；类型必须先于依赖它的表、视图和存储程序创建。
     *
     * @throws Exception 异常
     */
    private boolean transportTypes() throws Exception {
        List<OracleTypeDefinition> types = this.sourceClient.selectTypes(this.sourceDatabase);
        if (CollectionUtil.isEmpty(types)) {
            return true;
        }
        if (!this.targetClient.hasCreateTypePrivilege()) {
            this.message("Target schema \"" + this.targetDatabase + "\" needs the CREATE TYPE privilege."
                    + " Please ask a DBA to run: GRANT CREATE TYPE TO \"" + this.targetDatabase + "\";");
            return false;
        }
        for (OracleTypeDefinition type : types) {
            this.checkInterrupt();
            this.dropTargetType(type.getName());
            String createDefinition = ShellOracleUtil.removeSchema(
                    type.getCreateDefinition(), this.sourceDatabase);
            this.targetClient.executeSqlSimple(this.targetDatabase, createDefinition);
            this.message("Create Type " + type.getName());
            if (StringUtil.isNotBlank(type.getBodyDefinition())) {
                String bodyDefinition = ShellOracleUtil.removeSchema(
                        type.getBodyDefinition(), this.sourceDatabase);
                this.targetClient.executeSqlSimple(this.targetDatabase, bodyDefinition);
                this.message("Create Type Body " + type.getName());
            }
            this.processedIncr();
        }
        return true;
    }

    private void dropTargetType(String typeName) throws Exception {
        if (this.targetClient.existObject(this.targetDatabase, typeName, "TYPE")) {
            this.targetClient.executeSqlSimple(this.targetDatabase,
                    "DROP TYPE " + DBUtil.wrap(typeName, DBDialect.ORACLE) + " FORCE");
            this.message("Drop Type " + typeName);
        } else {
            this.message("Skip Drop Type " + typeName);
        }
        this.processedIncr();
    }

    private static boolean isSystemGeneratedSequence(String sequenceName) {
        return sequenceName != null && sequenceName.startsWith("ISEQ$$_");
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
            this.dropTargetObject("TABLE", tableName, "Table");

            // 创建表
            String createDefinition = this.sourceClient.showCreateTable(this.sourceDatabase, tableName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellOracleUtil.removeSchema(createDefinition,this.sourceDatabase);
            this.targetClient.executeSqlSimple(this.targetDatabase, createDefinition);
            this.message("Create Table " + tableName);
            this.processedIncr();

            // 传输表数据
            if (this.dataType == 0) {
                this.message("Transport Table " + tableName + " Starting");
                OracleColumns columns = new OracleColumns(this.sourceClient.selectColumns(new OracleSelectColumnParam(this.sourceDatabase, tableName)));
                OracleColumns targetColumns = new OracleColumns(this.targetClient.selectColumns(
                        new OracleSelectColumnParam(this.targetDatabase, tableName)));
                long start = 0;
                while (true) {
                    this.checkInterrupt();
                    try {
                        OracleSelectRecordParam param = new OracleSelectRecordParam();
                        param.setStart(start);
                        param.setReadonly(true);
                        param.setTableName(tableName);
                        param.setSchema(this.sourceDatabase);
                        param.setLimit((long) this.selectLimit);
                        param.setColumns(columns);
                        List<OracleRecord> records = this.sourceClient.selectRecords(param);
                        if (CollectionUtil.isEmpty(records)) {
                            break;
                        }
                        for (OracleRecord record : records) {
                            try {
                                this.transportRecord(tableName, targetColumns, record);
                            } catch (Exception ex) {
                                if (this.isContinueWithErrors()) {
                                    this.exception(ex);
                                } else {
                                    throw ex;
                                }
                            }
                        }
                        start += this.selectLimit;
                    } catch (Exception ex) {
                        if (this.isContinueWithErrors()) {
                            this.exception(ex);
                            break;
                        } else {
                            throw ex;
                        }
                    }
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

    /**
     * 通过客户端新增入口传输单条记录，确保LOB、复杂类型和特殊标量使用真实参数绑定。
     */
    private void transportRecord(String tableName, OracleColumns targetColumns,
                                 OracleRecord sourceRecord) {
        DBRecordData data = new DBRecordData();
        for (OracleColumn column : targetColumns) {
            if (column.isDmlWritable()) {
                data.put(column, sourceRecord.getValue(column.getName()));
            }
        }
        OracleInsertRecordParam param = new OracleInsertRecordParam();
        param.setSchema(this.targetDatabase);
        param.setTableName(tableName);
        param.setRecord(data);
        this.targetClient.insertRecord(param);
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
            this.dropTargetObject("VIEW", viewName, "View");

            // 创建视图
            String createDefinition = this.sourceClient.showCreateView(this.sourceDatabase, viewName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellOracleUtil.removeSchema(createDefinition,this.sourceDatabase);
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
            this.dropTargetObject("FUNCTION", functionName, "Function");

            // 创建函数
            String createDefinition = this.sourceClient.showCreateFunction(this.sourceDatabase, functionName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellOracleUtil.removeSchema(createDefinition,this.sourceDatabase);
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
            this.dropTargetObject("PROCEDURE", procedureName, "Procedure");

            // 创建过程
            String createDefinition = this.sourceClient.showCreateProcedure(this.sourceDatabase, procedureName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellOracleUtil.removeSchema(createDefinition,this.sourceDatabase);
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
            this.dropTargetObject("TRIGGER", triggerName, "Trigger");

            // 创建触发器
            String createDefinition = this.sourceClient.showCreateTrigger(this.sourceDatabase, triggerName);
            // TODO: 去除特定架构
//            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellOracleUtil.removeSchema(createDefinition,this.sourceDatabase);
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
            this.dropTargetObject("SEQUENCE", sequenceName, "Sequence");

            // 创建触发器
            String createDefinition = this.sourceClient.showCreateSequence(this.sourceDatabase, sequenceName);
            // TODO: 去除特定架构
            //            createDefinition = createDefinition.replaceFirst(DBUtil.wrap(this.sourceDatabase, this.dialect) + ".", "");
            createDefinition = ShellOracleUtil.removeSchema(createDefinition,this.sourceDatabase);
            this.targetClient.executeSqlSimple(this.targetDatabase, createDefinition);
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

    private void dropTargetObject(String objectType, String objectName, String objectLabel) throws Exception {
        if (this.targetClient.existObject(this.targetDatabase, objectName, objectType)) {
            String dropSql = "DROP " + objectType + " " + DBUtil.wrap(objectName, DBDialect.ORACLE) + ";";
            this.targetClient.executeSqlSimple(this.targetDatabase, dropSql);
            this.message("Drop " + objectLabel + " " + objectName);
        } else {
            this.message("Skip Drop " + objectLabel + " " + objectName);
        }
        this.processedIncr();
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
    public ShellOracleClient getSourceClient() {
        return sourceClient;
    }

    /**
     * 设置来源客户端
     *
     * @param sourceClient 来源客户端
     */
    public void setSourceClient(ShellOracleClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    /**
     * 获取目标客户端
     *
     * @return 目标客户端
     */
    public ShellOracleClient getTargetClient() {
        return targetClient;
    }

    /**
     * 设置目标客户端
     *
     * @param targetClient 目标客户端
     */
    public void setTargetClient(ShellOracleClient targetClient) {
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

