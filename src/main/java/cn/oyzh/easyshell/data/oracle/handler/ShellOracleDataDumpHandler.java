package cn.oyzh.easyshell.data.oracle.handler;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.column.OracleSelectColumnParam;
import cn.oyzh.easyshell.oracle.function.OracleFunction;
import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.oracle.record.OracleSelectRecordParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.oracle.table.OracleSelectTableParam;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.trigger.OracleTrigger;
import cn.oyzh.easyshell.oracle.type.OracleTypeDefinition;
import cn.oyzh.easyshell.oracle.view.OracleView;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.handler.DBDataDumpHandler;
import cn.oyzh.fx.db.util.DBUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Oracle数据转储处理器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleDataDumpHandler extends DBDataDumpHandler {

    /**
     * db客户端
     */
    protected ShellOracleClient dbClient;

    /**
     * 构造 Oracle数据转储处理器
     *
     * @param dbClient 数据库客户端
     * @param dbName   数据库名称
     */
    public ShellOracleDataDumpHandler(ShellOracleClient dbClient, String dbName) {
        super(dbName, DBDialect.ORACLE);
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
                try {
                    this.dumpType();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
                try {
                    this.dumpTable();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
                try {
                    this.dumpSequence();
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
                    this.dumpView();
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
                    this.dumpType();
                    OracleTable table = this.dbClient.selectTable(this.dbName, this.tableName);
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

    }

    /**
     * 转储用户自定义类型；类型必须先于依赖它的表和集合创建。
     *
     * @throws Exception 异常
     */
    protected void dumpType() throws Exception {
        List<OracleTypeDefinition> types = this.dbClient.selectTypes(this.dbName);
        for (OracleTypeDefinition type : types) {
            this.checkInterrupt();
            this.message("Dumping Type " + type.getName());
            String line0 = "";
            String line1 = "-- ----------------------------";
            String line2 = "-- Type structure for " + type.getName();
            String line3 = "-- ----------------------------";
            String dropType = ShellOracleHelper.buildDropIfExistsSql("TYPE", type.getName(), "FORCE");
            String createDefinition = sqlStatement(
                    ShellOracleUtil.removeSchema(type.getCreateDefinition(), this.dbName));
            this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropType, createDefinition));
            if (StringUtil.isNotBlank(type.getBodyDefinition())) {
                String body = sqlPlusBlock(
                        ShellOracleUtil.removeSchema(type.getBodyDefinition(), this.dbName));
                this.fileWriter.appendLines(List.of(
                        "-- Type body structure for " + type.getName(), line3, body));
            }
            this.processedIncr();
        }
    }

    /**
     * 转储所有表
     *
     * @throws Exception            异常
     */
    protected void dumpTable() throws Exception {
        OracleSelectTableParam selectTableParam = new OracleSelectTableParam();
        selectTableParam.setFull(true);
        selectTableParam.setSchema(this.dbName);
        this.message("select tables");
        List<OracleTable> tables = this.dbClient.selectTables(selectTableParam);
        if (CollectionUtil.isNotEmpty(tables)) {
            // 按外键依赖排序，父表在前
            this.message("sort tables");
            ShellOracleUtil.sortTablesInHandler(tables, this.dbClient, this);
            for (OracleTable table : tables) {
                try {
                    this.checkInterrupt();
                    this.dumpTable(table);
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
            }
            //// 传输表实际业务
            //ShellOracleUtil.sortTablesByCallback(this.dbClient, tables, (ExceptionConsumer<OracleTable>) t -> {
            //    this.checkInterrupt();
            //    this.dumpTable(t);
            //    this.processedIncr();
            //});
        }
    }

    /**
     * 转储指定表
     *
     * @param table 表
     * @throws Exception 异常
     */
    protected void dumpTable(OracleTable table) throws Exception {
        String line0 = "";
        String line1 = "-- ----------------------------";
        String line2 = "-- Table structure for " + table.getName();
        String line3 = "-- ----------------------------";
        String dropOptions = this.dumpType == 1 ? "CASCADE CONSTRAINTS" : "";
        String dropTable = ShellOracleHelper.buildDropIfExistsSql("TABLE", table.getName(), dropOptions);
        String createDefinition = table.getCreateDefinition();
        createDefinition = sqlStatement(createDefinition);
        //        createDefinition = createDefinition.replaceAll("CREATE\\s+TABLE\\s+\"[^\"]+\"\\.", "CREATE TABLE ");
        // TODO: 去除特定架构
        //        createDefinition = createDefinition.replaceFirst(DBUtil.wrap(table.getSchema(), this.dialect) + ".", "");
        createDefinition = ShellOracleUtil.removeSchema(createDefinition, table.getSchema());
        this.message("Dumping Table " + table.getName());
        this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTable, createDefinition));
        // 查询字段
        OracleColumns columns = new OracleColumns(this.dbClient.selectColumns(new OracleSelectColumnParam(this.dbName, table.getName())));
        // 设置字段注释
        for (OracleColumn column : columns) {
            String tableComment = """
                    COMMENT ON COLUMN "$1"."$2" IS $3;
                    """;
            tableComment = tableComment.replace("$1", column.getTableName());
            tableComment = tableComment.replace("$2", column.getName());
            if (StringUtil.isNotBlank(column.getComment())) {
                tableComment = tableComment.replace("$3", DBUtil.wrapData(column.getComment(), this.dialect).toString());
            } else {
                tableComment = tableComment.replace("$3", "''");
            }
            this.fileWriter.appendLine(tableComment);
        }
        // 设置表注释
        String tableComment = """
                COMMENT ON TABLE "$1" IS $2;
                """;
        tableComment = tableComment.replace("$1", table.getName());
        if (StringUtil.isNotBlank(table.getComment())) {
            tableComment = tableComment.replace("$2", DBUtil.wrapData(table.getComment(), this.dialect).toString());
        } else {
            tableComment = tableComment.replace("$2", "''");
        }
        this.fileWriter.appendLine(tableComment);
        Map<String, BigDecimal> identityMaxValues = Map.of();
        if (this.isDumpRecord()) {
            this.message("Dumping Records of Table " + table.getName());
            identityMaxValues = this.dumpRecord(table, columns);
        }
        this.dumpIdentityRestart(table, createDefinition, columns, identityMaxValues);
        this.processedIncr();
    }

    /**
     * 转储表记录
     *
     * @param table   表
     * @param columns 字段列表
     * @throws Exception 异常
     */
    protected Map<String, BigDecimal> dumpRecord(OracleTable table, OracleColumns columns) throws Exception {
        String tableName = table.getName();
        long start = 0;
        List<OracleColumn> identityColumns = columns.stream()
                .filter(OracleColumn::isIdentityAutoIncrementType)
                .toList();
        Map<String, BigDecimal> identityMaxValues = new HashMap<>();
        String line0 = "-- ----------------------------";
        String line1 = "-- Records of " + tableName;
        String line2 = "-- ----------------------------";
        this.fileWriter.appendLines(List.of(line0, line1, line2));
        while (true) {
            long nextStart = start + this.queryLimit;
            try {
                this.checkInterrupt();
                long start1 = System.currentTimeMillis();
                OracleSelectRecordParam param = new OracleSelectRecordParam();
                param.setStart(start);
                param.setReadonly(true);
                param.setColumns(columns);
                param.setSchema(this.dbName);
                param.setTableName(tableName);
                param.setLimit((long) this.queryLimit);
                List<OracleRecord> records = this.dbClient.selectRecords(param);
                if (CollectionUtil.isEmpty(records)) {
                    break;
                }
                long end1 = System.currentTimeMillis();
                JulLog.info("查询耗时: {}ms", (end1 - start1));
                long start2 = System.currentTimeMillis();
                List<String> inserts = ShellOracleDataUtil.toInsertSql(columns, records, true);
                if (StringUtil.isNotBlank(this.dbName)) {
                    inserts = inserts.stream()
                            .map(insert -> ShellOracleUtil.removeSchema(insert, this.dbName))
                            .toList();
                }
                this.fileWriter.appendLines(inserts);
                long end2 = System.currentTimeMillis();
                JulLog.info("写入耗时: {}ms", (end2 - start2));
                this.processed(records.size());
                this.collectIdentityMaxValues(records, identityColumns, identityMaxValues);
            } catch (Exception ex) {
                this.handleDumpError(ex);
            } finally {
                start = nextStart;
            }
        }
        return identityMaxValues;
    }

    private void collectIdentityMaxValues(List<OracleRecord> records, List<OracleColumn> identityColumns,
                                          Map<String, BigDecimal> maxValues) {
        if (identityColumns.isEmpty()) {
            return;
        }
        for (OracleRecord record : records) {
            for (OracleColumn column : identityColumns) {
                BigDecimal value = toDecimal(record.getValue(column.getName()));
                if (value != null) {
                    maxValues.merge(column.getName(), value, BigDecimal::max);
                }
            }
        }
    }

    private void dumpIdentityRestart(OracleTable table, String tableDefinition, OracleColumns columns,
                                     Map<String, BigDecimal> maxValues) throws IOException {
        if (maxValues.isEmpty()) {
            return;
        }
        for (OracleColumn column : columns) {
            if (!column.isIdentityAutoIncrementType()) {
                continue;
            }
            BigDecimal maxValue = maxValues.get(column.getName());
            if (maxValue == null) {
                continue;
            }
            String generation = identityGenerationClause(tableDefinition, column.getName());
            String sql = "ALTER TABLE " + DBUtil.wrap(table.getName(), DBDialect.ORACLE)
                    + " MODIFY (" + DBUtil.wrap(column.getName(), DBDialect.ORACLE)
                    + " " + generation
                    + " (START WITH " + maxValue.add(BigDecimal.ONE).toPlainString() + "));";
            this.fileWriter.appendLine(sql);
        }
    }

    private static String identityGenerationClause(String tableDefinition, String columnName) {
        String quotedColumn = Pattern.quote(DBUtil.wrap(columnName, DBDialect.ORACLE));
        Pattern pattern = Pattern.compile("(?is)" + quotedColumn
                + ".{0,300}?(GENERATED\\s+(?:ALWAYS|BY\\s+DEFAULT(?:\\s+ON\\s+NULL)?)\\s+AS\\s+IDENTITY)");
        Matcher matcher = pattern.matcher(tableDefinition == null ? "" : tableDefinition);
        return matcher.find() ? matcher.group(1) : "GENERATED BY DEFAULT AS IDENTITY";
    }

    private static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 转储视图
     *
     * @throws Exception 异常
     */
    protected void dumpView() throws Exception {
        List<OracleView> views = this.dbClient.selectViews(this.dbName);
        if (CollectionUtil.isNotEmpty(views)) {
            for (OracleView view : views) {
                try {
                    this.checkInterrupt();
                    this.message("Dumping View " + view.getName());
                    String line0 = "";
                    String line1 = "-- ----------------------------";
                    String line2 = "-- View structure for " + view.getName();
                    String line3 = "-- ----------------------------";
                    String dropTable = ShellOracleHelper.buildDropIfExistsSql("VIEW", view.getName());
                    String createDefinition = view.getCreateDefinition();
                    //                String createDefinition = this.dbClient.showCreateView(this.dbName, view.getName());
                    // TODO: 去除特定架构
                    //                createDefinition = createDefinition.replaceFirst(DBUtil.wrap(view.getSchema(), this.dialect) + ".", "");
                    createDefinition = ShellOracleUtil.removeSchema(createDefinition, view.getSchema());
                    if (!createDefinition.endsWith(";")) {
                        createDefinition += ";";
                    }
                    this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTable, createDefinition));
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
        List<OracleFunction> functions = this.dbClient.selectFunctions(this.dbName);
        if (CollectionUtil.isNotEmpty(functions)) {
            for (OracleFunction function : functions) {
                try {
                    this.checkInterrupt();
                    this.message("Dumping Function " + function.getName());
                    String line0 = "";
                    String line1 = "-- ----------------------------";
                    String line2 = "-- Function structure for " + function.getName();
                    String line3 = "-- ----------------------------";
                    String dropFunction = ShellOracleHelper.buildDropIfExistsSql("FUNCTION", function.getName());
                    String createDefinition = function.getCreateDefinition();
                    // TODO: 去除特定架构
                    //                String createDefinition = this.dbClient.showCreateFunction(this.dbName, function.getName());
                    //                createDefinition = createDefinition.replaceFirst(DBUtil.wrap(function.getSchema(), this.dialect) + ".", "");
                    createDefinition = sqlPlusBlock(ShellOracleUtil.removeSchema(createDefinition, function.getSchema()));
                    this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropFunction, createDefinition));
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
        List<OracleProcedure> procedures = this.dbClient.selectProcedures(this.dbName);
        if (CollectionUtil.isNotEmpty(procedures)) {
            for (OracleProcedure procedure : procedures) {
                try {
                    this.checkInterrupt();
                    this.message("Dumping Procedure " + procedure.getName());
                    String line0 = "";
                    String line1 = "-- ----------------------------";
                    String line2 = "-- Procedure structure for " + procedure.getName();
                    String line3 = "-- ----------------------------";
                    String dropProcedure = ShellOracleHelper.buildDropIfExistsSql("PROCEDURE", procedure.getName());
                    String createDefinition = procedure.getCreateDefinition();
                    //                String createDefinition = this.dbClient.showCreateProcedure(this.dbName, procedure.getName());
                    // TODO: 去除特定架构
                    //                createDefinition = createDefinition.replaceFirst(DBUtil.wrap(procedure.getSchema(), this.dialect) + ".", "");
                    createDefinition = sqlPlusBlock(ShellOracleUtil.removeSchema(createDefinition, procedure.getSchema()));
                    this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropProcedure, createDefinition));
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
        List<OracleTrigger> triggers = this.dbClient.selectTriggers(this.dbName);
        if (CollectionUtil.isNotEmpty(triggers)) {
            for (OracleTrigger trigger : triggers) {
                try {
                    this.checkInterrupt();
                    this.message("Dumping Trigger " + trigger.getName());
                    String line0 = "";
                    String line1 = "-- ----------------------------";
                    String line2 = "-- Trigger structure for " + trigger.getName();
                    String line3 = "-- ----------------------------";
                    String dropTrigger = ShellOracleHelper.buildDropIfExistsSql("TRIGGER", trigger.getName());
                    String createDefinition = trigger.getCreateDefinition();
                    //                String createDefinition = this.dbClient.showCreateTrigger(this.dbName, trigger.getName());
                    // TODO: 去除特定架构
                    //                createDefinition = createDefinition.replaceFirst(DBUtil.wrap(trigger.getSchema(), this.dialect) + ".", "");
                    createDefinition = sqlPlusBlock(ShellOracleUtil.removeSchema(createDefinition, trigger.getSchema()));
                    this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTrigger, createDefinition));
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
        List<OracleSequence> sequences = this.dbClient.selectSequences(this.dbName);
        if (CollectionUtil.isNotEmpty(sequences)) {
            for (OracleSequence sequence : sequences) {
                if (isSystemGeneratedSequence(sequence.getName())) {
                    continue;
                }
                try {
                    this.checkInterrupt();
                    this.message("Dumping Sequence " + sequence.getName());
                    String line0 = "";
                    String line1 = "-- ----------------------------";
                    String line2 = "-- Sequence structure for " + sequence.getName();
                    String line3 = "-- ----------------------------";
                    String dropSequence = ShellOracleHelper.buildDropIfExistsSql("SEQUENCE", sequence.getName());
                    String createDefinition = sequence.getCreateDefinition();
                    //                String createDefinition = this.dbClient.showCreateTrigger(this.dbName, trigger.getName());
                    // TODO: 去除特定架构
                    //                createDefinition = createDefinition.replaceFirst(DBUtil.wrap(trigger.getSchema(), this.dialect) + ".", "");
                    createDefinition = sqlStatement(ShellOracleUtil.removeSchema(createDefinition, sequence.getSchema()));
                    this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropSequence, createDefinition));
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleDumpError(ex);
                }
            }
        }
    }

    private static boolean isSystemGeneratedSequence(String sequenceName) {
        return sequenceName != null && sequenceName.startsWith("ISEQ$$_");
    }

    private static String sqlStatement(String definition) {
        String value = definition == null ? "" : definition.strip();
        return value.endsWith(";") ? value : value + ";";
    }

    private static String sqlPlusBlock(String definition) {
        String value = definition == null ? "" : definition.stripTrailing();
        return value.endsWith("/") ? value : value + "\n/";
    }
}
