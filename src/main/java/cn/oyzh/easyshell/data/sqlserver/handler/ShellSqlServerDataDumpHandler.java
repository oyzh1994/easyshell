package cn.oyzh.easyshell.data.sqlserver.handler;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.column.SqlServerSelectColumnParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.record.SqlServerSelectRecordParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerSequence;
import cn.oyzh.easyshell.sqlserver.table.SqlServerSelectTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.trigger.SqlServerTrigger;
import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerDataImportHelper;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.handler.DBDataDumpHandler;
import cn.oyzh.fx.db.util.DBUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SqlServer数据转储处理器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerDataDumpHandler extends DBDataDumpHandler {

    /**
     * db客户端
     */
    protected ShellSqlServerClient dbClient;

    /**
     * 序列值重置sql，待所有对象转储完成后统一写入
     */
    private final List<String> sequenceResetList = new ArrayList<>();

    /**
     * 转储前缓存的视图，避免对象删除顺序影响定义读取
     */
    private List<SqlServerView> views;

    /**
     * 转储前缓存的触发器，避免对象删除顺序影响定义读取
     */
    private List<SqlServerTrigger> triggers;

    /**
     * 构造 SqlServer数据转储处理器
     *
     * @param dbClient 数据库客户端
     * @param dbName   数据库名称
     */
    public ShellSqlServerDataDumpHandler(ShellSqlServerClient dbClient, String dbName) {
        super(dbName, DBDialect.SQLSERVER);
        this.dbClient = dbClient;
    }

    @Override
    public void doDump() throws Exception {
        if (this.fileWriter == null || this.dumpType == null || this.dataType == null) {
            throw new RuntimeException("parameter invalid!");
        }
        this.message("Dump Starting");
        try {
            this.writeHeader();
            if (this.dumpType == 1) {
                // 先缓存视图和触发器定义，再执行对象DROP与CREATE
                this.views = this.dbClient.selectViews(this.dbName);
                this.triggers = this.dbClient.selectTriggers(this.dbName);
                try {
                    this.dumpCustomType();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
                try {
                    this.dumpSequence();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
                try {
                    this.dumpTable();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
                try {
                    this.dumpView();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
                try {
                    this.dumpFunction();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
                try {
                    this.dumpProcedure();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
                try {
                    this.dumpTrigger();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
            } else if (this.dumpType == 2) {
                try {
                    SqlServerTable table = this.dbClient.selectTable(this.dbName, this.tableName);
                    String drop = "DROP TABLE IF EXISTS "
                            + DBUtil.wrap(this.dbName, table.getName(), DBDialect.SQLSERVER) + ";";
                    this.fileWriter.appendLines(List.of("GO", drop, "GO"));
                    this.dumpTable(table);
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
            }
        } catch (Exception ex) {
            this.handleDumpError(ex);
        } finally {
            try {
                this.writeTail();
            } catch (Exception ex) {
                this.handleDumpError(ex);
            } finally {
                try {
                    this.fileWriter.close();
                } finally {
                    this.message("Dump Finished");
                    this.message("Dump File To -> " + this.dumpFile.getPath());
                }
            }
        }
    }

    @Override
    protected void writeHeader() throws IOException {
        String version = this.dbClient.selectVersion();
        String clientCharacter = this.dbClient.selectClientCharacter();
        String header = "/*\n";
        header += " EasyShell Data Transfer";
        header += "\n\n";
        header += " Source Server : " + this.dbClient.getShellConnect().getName();
        header += "\n";
        header += " Source Server Type : " + this.dbClient.dialect().name();
        header += "\n";
        header += " Source Server Version : " + version;
        header += "\n";
        header += " Source Host : " + this.dbClient.getShellConnect().getHost();
        header += "\n";
        header += " Source Schema : " + this.dbName;
        header += "\n\n";
        header += " Target Server Type : " + this.dbClient.dialect().name();
        header += "\n";
        header += " Target Server Version : " + version;
        header += "\n";
        header += " File Encoding : " + clientCharacter;
        header += "\n\n";
        header += " Date : " + DateHelper.formatDateTimeSimple();
        header += "\n";
        header += "*/";

        header += "\n\n";
        this.fileWriter.writeLines(List.of(header));
    }

    @Override
    protected void writeTail() throws IOException {
        // 写入序列值重置sql
        if (CollectionUtil.isNotEmpty(this.sequenceResetList)) {
            this.fileWriter.appendLines(List.of("-- ----------------------------", "-- Sequence values", "-- ----------------------------"));
            for (String reset : this.sequenceResetList) {
                this.fileWriter.appendLine(reset);
                this.fileWriter.appendLine("GO");
            }
            this.sequenceResetList.clear();
        }
    }

    /**
     * 转储所有表
     *
     * @throws Exception            异常
     */
    protected void dumpTable() throws Exception {
        SqlServerSelectTableParam selectTableParam = new SqlServerSelectTableParam();
        selectTableParam.setFull(true);
        selectTableParam.setSchema(this.dbName);
        this.message("select tables");
        List<SqlServerTable> tables = this.dbClient.selectTables(selectTableParam);
        if (CollectionUtil.isNotEmpty(tables)) {
            // 按外键依赖排序，父表在前
            this.message("sort tables");
            ShellSqlServerUtil.sortTablesInHandler(tables, this.dbClient, this);
            // 删除阶段必须子表优先，避免目标库已有外键阻止DROP
            this.fileWriter.appendLines(List.of("", "-- Drop tables", "GO"));
            for (int i = tables.size() - 1; i >= 0; i--) {
                String drop = "DROP TABLE IF EXISTS "
                        + DBUtil.wrap(this.dbName, tables.get(i).getName(), DBDialect.SQLSERVER) + ";";
                this.fileWriter.appendLines(List.of(drop, "GO"));
            }
            for (SqlServerTable table : tables) {
                try {
                    this.checkInterrupt();
                    this.dumpTable(table);
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
            }
            //// 传输表实际业务
            //ShellSqlServerUtil.sortTablesByCallback(this.dbClient, tables, (ExceptionConsumer<SqlServerTable>) t -> {
            //    this.checkInterrupt();
            //    this.dumpTable(t);
            //    this.processedIncr();
            //});
        }
    }

    /**
     * 转储自定义枚举、域和复合类型。
     *
     * @throws Exception 异常
     */
    protected void dumpCustomType() throws Exception {
        List<String> definitions = this.dbClient.selectCustomTypeDefinitions(this.dbName);
        if (CollectionUtil.isNotEmpty(definitions)) {
            this.message("Dumping custom types");
            this.fileWriter.appendLines(List.of(
                    "",
                    "-- ----------------------------",
                    "-- Custom types",
                    "-- ----------------------------"
            ));
            this.fileWriter.appendLines(definitions);
        }
    }

    /**
     * 转储指定表
     *
     * @param table 表
     * @throws Exception 异常
     */
    protected void dumpTable(SqlServerTable table) throws Exception {
        String line0 = "";
        String line1 = "-- ----------------------------";
        String line2 = "-- Table structure for " + table.getName();
        String line3 = "-- ----------------------------";
        String createDefinition = table.getCreateDefinition();
        if (!createDefinition.endsWith(";")) {
            createDefinition += ";";
        }
        this.message("Dumping Table " + table.getName());
        this.fileWriter.appendLines(List.of(line0, line1, line2, line3,
                "GO", createDefinition, "GO"));
        // 查询字段
        SqlServerColumns columns = new SqlServerColumns(this.dbClient.selectColumns(new SqlServerSelectColumnParam(this.dbName, table.getName())));
        if (this.isDumpRecord()) {
            this.message("Dumping Records of Table " + table.getName());
            this.dumpRecord(table, columns);
            // 收集序列重置sql，待转储完成后写入，避免序列定义被后续语句覆盖
            this.sequenceResetList.addAll(ShellSqlServerDataImportHelper.buildSequenceResetSql(columns, this.dbName));
        }
        this.processedIncr();
    }

    /**
     * 转储表记录
     *
     * @param table   表
     * @param columns 字段列表
     * @throws Exception 异常
     */
    protected void dumpRecord(SqlServerTable table, SqlServerColumns columns) throws Exception {
        String tableName = table.getName();
        long start = 0;
        String line0 = "-- ----------------------------";
        String line1 = "-- Records of " + tableName;
        String line2 = "-- ----------------------------";
        this.fileWriter.appendLines(List.of(line0, line1, line2));
        while (true) {
            long nextStart = start + this.queryLimit;
            try {
                this.checkInterrupt();
                long start1 = System.currentTimeMillis();
                SqlServerSelectRecordParam param = new SqlServerSelectRecordParam();
                param.setStart(start);
                param.setReadonly(true);
                param.setColumns(columns);
                param.setSchema(this.dbName);
                param.setTableName(tableName);
                param.setLimit((long) this.queryLimit);
                List<SqlServerRecord> records = this.dbClient.selectRecords(param);
                if (CollectionUtil.isEmpty(records)) {
                    break;
                }
                long end1 = System.currentTimeMillis();
                JulLog.info("查询耗时: {}ms", (end1 - start1));
                long start2 = System.currentTimeMillis();
                List<String> inserts = ShellSqlServerDataImportHelper.toInsertSql(columns, records, this.dbName);
                this.fileWriter.appendLines(inserts);
                long end2 = System.currentTimeMillis();
                JulLog.info("写入耗时: {}ms", (end2 - start2));
                this.processed(records.size());
            } catch (Exception ex) {
                this.handleDumpError(ex);
            } finally {
                start = nextStart;
            }
        }
    }

    /**
     * 补全sql定义结尾的分号
     *
     * @param definition sql定义
     * @return 补全后的sql定义
     */
    private String sqlDefinition(String definition) {
        if (StringUtil.isBlank(definition)) {
            return "";
        }
        definition = definition.stripTrailing();
        return definition.endsWith(";") ? definition : definition + ";";
    }

    /**
     * 转储视图
     *
     * @throws Exception 异常
     */
    protected void dumpView() throws Exception {
        List<SqlServerView> views = this.views != null ? this.views : this.dbClient.selectViews(this.dbName);
        if (CollectionUtil.isNotEmpty(views)) {
            for (SqlServerView view : views) {
                try {
                    this.checkInterrupt();
                    this.message("Dumping View " + view.getName());
                    String line0 = "";
                    String line1 = "-- ----------------------------";
                    String line2 = "-- View structure for " + view.getName();
                    String line3 = "-- ----------------------------";
                    String dropTable = "DROP VIEW IF EXISTS " + DBUtil.wrap(this.dbName, view.getName(), DBDialect.SQLSERVER) + ";";
                    String createDefinition = view.getCreateDefinition();
                    //                String createDefinition = this.dbClient.showCreateView(this.dbName, view.getName());
                    if (!createDefinition.endsWith(";")) {
                        createDefinition += ";";
                    }
                    this.fileWriter.appendLines(List.of(line0, line1, line2, line3,
                            "GO", dropTable, "GO", createDefinition, "GO"));
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
            }
        }
    }

    /**
     * 转储函数
     *
     * @throws Exception 异常
     */
    protected void dumpFunction() throws Exception {
        List<SqlServerFunction> functions = this.dbClient.selectFunctions(this.dbName);
        if (CollectionUtil.isNotEmpty(functions)) {
            for (SqlServerFunction function : functions) {
                try {
                    this.checkInterrupt();
                    this.message("Dumping Function " + function.getName());
                    String line0 = "";
                    String line1 = "-- ----------------------------";
                    String line2 = "-- Function structure for " + function.getName();
                    String line3 = "-- ----------------------------";
                    String dropFunction = ShellSqlServerUtil.dropRoutineSql(this.dbName, function.getName(), function.getParams(), false);
                    String createDefinition = function.getCreateDefinition();
                    // TODO: 去除特定架构
                    //                String createDefinition = this.dbClient.showCreateFunction(this.dbName, function.getName());
                    createDefinition = this.sqlDefinition(createDefinition);
                    this.fileWriter.appendLines(List.of(line0, line1, line2, line3,
                            "GO", dropFunction, "GO", createDefinition, "GO"));
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
            }
        }
    }

    /**
     * 转储过程
     *
     * @throws Exception 异常
     */
    protected void dumpProcedure() throws Exception {
        List<SqlServerProcedure> procedures = this.dbClient.selectProcedures(this.dbName);
        if (CollectionUtil.isNotEmpty(procedures)) {
            for (SqlServerProcedure procedure : procedures) {
                try {
                    this.checkInterrupt();
                    this.message("Dumping Procedure " + procedure.getName());
                    String line0 = "";
                    String line1 = "-- ----------------------------";
                    String line2 = "-- Procedure structure for " + procedure.getName();
                    String line3 = "-- ----------------------------";
                    String dropProcedure = ShellSqlServerUtil.dropRoutineSql(this.dbName, procedure.getName(), procedure.getParams(), true);
                    String createDefinition = procedure.getCreateDefinition();
                    //                String createDefinition = this.dbClient.showCreateProcedure(this.dbName, procedure.getName());
                    createDefinition = this.sqlDefinition(createDefinition);
                    this.fileWriter.appendLines(List.of(line0, line1, line2, line3,
                            "GO", dropProcedure, "GO", createDefinition, "GO"));
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
            }
        }
    }

    /**
     * 转储触发器
     *
     * @throws Exception 异常
     */
    protected void dumpTrigger() throws Exception {
        List<SqlServerTrigger> triggers = this.triggers != null ? this.triggers : this.dbClient.selectTriggers(this.dbName);
        if (CollectionUtil.isNotEmpty(triggers)) {
            for (SqlServerTrigger trigger : triggers) {
                try {
                    this.checkInterrupt();
                    this.message("Dumping Trigger " + trigger.getName());
                    String line0 = "";
                    String line1 = "-- ----------------------------";
                    String line2 = "-- Trigger structure for " + trigger.getName();
                    String line3 = "-- ----------------------------";
                    String dropTrigger = "DROP TRIGGER IF EXISTS "
                            + DBUtil.wrap(this.dbName, trigger.getName(), DBDialect.SQLSERVER) + ";";
                    String createDefinition = trigger.getCreateDefinition();
                    //                String createDefinition = this.dbClient.showCreateTrigger(this.dbName, trigger.getName());
                    createDefinition = this.sqlDefinition(createDefinition);
                    // 触发器函数不在普通函数列表中，必须一并转储，否则目标库创建触发器会找不到函数
                    String functionDefinition = trigger.getFunctionDefinition();
                    if (StringUtil.isNotBlank(functionDefinition)) {
                        this.fileWriter.appendLine(this.sqlDefinition(functionDefinition));
                    }
                    this.fileWriter.appendLines(List.of(line0, line1, line2, line3,
                            "GO", dropTrigger, "GO", createDefinition, "GO"));
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
            }
        }
    }

    /**
     * 转储序列
     *
     * @throws Exception 异常
     */
    protected void dumpSequence() throws Exception {
        List<SqlServerSequence> sequences = this.dbClient.selectSequences(this.dbName);
        if (CollectionUtil.isNotEmpty(sequences)) {
            for (SqlServerSequence sequence : sequences) {
                try {
                    this.checkInterrupt();
                    this.message("Dumping Sequence " + sequence.getName());
                    String line0 = "";
                    String line1 = "-- ----------------------------";
                    String line2 = "-- Sequence structure for " + sequence.getName();
                    String line3 = "-- ----------------------------";
                    String dropSequence = "DROP SEQUENCE IF EXISTS " + DBUtil.wrap(this.dbName, sequence.getName(), DBDialect.SQLSERVER) + ";";
                    String createDefinition = sequence.getCreateDefinition();
                    //                createDefinition = this.dbClient.showCreateSequence(this.dbName, sequence.getName());
                    createDefinition = this.sqlDefinition(createDefinition);
                    this.fileWriter.appendLines(List.of(line0, line1, line2, line3,
                            "GO", dropSequence, "GO", createDefinition, "GO"));
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
            }
        }
    }
}
