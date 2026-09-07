package cn.oyzh.easyshell.data.dameng.handler;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.column.DamengSelectColumnParam;
import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.dameng.table.DamengSelectTableParam;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.trigger.DamengTrigger;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.util.dameng.ShellDamengDataUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.handler.DBDataDumpHandler;
import cn.oyzh.fx.db.util.DBUtil;

import java.io.IOException;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/10
 */
public class ShellDamengDataDumpHandler extends DBDataDumpHandler {

    /**
     * db客户端
     */
    protected ShellDamengClient dbClient;

    public ShellDamengDataDumpHandler(ShellDamengClient dbClient, String dbName) {
        super(dbName);
        this.dbClient = dbClient;
    }

    @Override
    public void doDump() throws Exception {
        if (this.fileWriter == null || this.dumpType == null || this.dataType == null) {
            throw new RuntimeException("parameter invalid!");
        }
        this.message("Dump Starting");
        this.writeHeader();
        try {
            if (this.dumpType == 1) {
                this.dumpTable();
                this.dumpView();
                this.dumpFunction();
                this.dumpProcedure();
                this.dumpTrigger();
            } else if (this.dumpType == 2) {
                DamengTable table = this.dbClient.selectTable(this.dbName, this.tableName);
                this.dumpTable(table);
                this.processedIncr();
            }
            this.writeTail();
            this.fileWriter.close();
        } catch (Exception ex) {
            this.exception(ex);
        }
        this.message("Dump Finished");
        this.message("Dump File To -> " + this.dumpFile.getPath());
    }

    @Override
    protected void writeHeader() throws IOException {
        String version = this.dbClient.selectVersion();
        String clientCharacter = this.dbClient.selectClientCharacter();
        String header = "/*\n";
        header += " EasyDB Data Transfer";
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

    protected void dumpTable() throws InterruptedException, IOException {
        DamengSelectTableParam selectTableParam = new DamengSelectTableParam();
        selectTableParam.setFull(true);
        selectTableParam.setSchema(this.dbName);
        List<DamengTable> tables = this.dbClient.selectTables(selectTableParam);
        if (CollectionUtil.isNotEmpty(tables)) {
            for (DamengTable table : tables) {
                this.checkInterrupt();
                this.dumpTable(table);
                this.processedIncr();
            }
        }
    }

    protected void dumpTable(DamengTable table) throws InterruptedException, IOException {
        String line0 = "";
        String line1 = "-- ----------------------------";
        String line2 = "-- Table structure for " + table.getName();
        String line3 = "-- ----------------------------";
        String dropTable = "DROP TABLE IF EXISTS " + DBUtil.wrap(table.getName(), DBDialect.DAMENG) + ";";
        String createDefinition = table.getCreateDefinition();
        if (!createDefinition.endsWith(";")) {
            createDefinition += ";";
        }
        // TODO: 去除特定架构
        createDefinition = createDefinition.replaceAll("CREATE\\s+TABLE\\s+\"[^\"]+\"\\.", "CREATE TABLE ");
        this.message("Dumping Table " + table.getName());
        this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTable, createDefinition));
        // 查询字段
        DamengColumns columns = new DamengColumns(this.dbClient.selectColumns(new DamengSelectColumnParam(this.dbName, table.getName())));
        // 设置字段注释
        for (DamengColumn column : columns) {
            String tableComment = """
                    COMMENT ON COLUMN "$1"."$2" IS '$3';
                    """;
            tableComment = tableComment.replace("$1", column.getTableName());
            tableComment = tableComment.replace("$2", column.getName());
            if (StringUtil.isNotBlank(column.getComment())) {
                tableComment = tableComment.replace("$3", column.getComment());
            } else {
                tableComment = tableComment.replace("$3", "");
            }
            this.fileWriter.appendLine(tableComment);
        }
        // 设置表注释
        String tableComment = """
                COMMENT ON TABLE "$1" IS '$2';
                """;
        tableComment = tableComment.replace("$1", table.getName());
        if (StringUtil.isNotBlank(table.getComment())) {
            tableComment = tableComment.replace("$2", table.getComment());
        } else {
            tableComment = tableComment.replace("$2", "");
        }
        this.fileWriter.appendLine(tableComment);
        if (this.isDumpRecord()) {
            this.message("Dumping Records of Table " + table.getName());
            this.dumpRecord(table, columns);
        }
    }

    protected void dumpRecord(DamengTable table, DamengColumns columns) throws InterruptedException, IOException {

        String createDefinition = table.getCreateDefinition();


        String tableName = table.getName();
        long start = 0;
        String line0 = "-- ----------------------------";
        String line1 = "-- Records of " + tableName;
        String line2 = "-- ----------------------------";
        this.fileWriter.appendLines(List.of(line0, line1, line2));
        boolean hasIdentity = columns.hasAutoIncrement() && !StringUtil.containsIgnoreCase(createDefinition, " AUTO_INCREMENT ");
        if (hasIdentity) {
            String line = "SET IDENTITY_INSERT " + DBUtil.wrap(tableName, DBDialect.DAMENG) + " ON;";
            this.fileWriter.appendLine(line);
        }
        while (true) {
            this.checkInterrupt();
            long start1 = System.currentTimeMillis();
            DamengSelectRecordParam param = new DamengSelectRecordParam();
            param.setStart(start);
            param.setReadonly(true);
            param.setColumns(columns);
            param.setSchema(this.dbName);
            param.setTableName(tableName);
            param.setLimit((long) this.queryLimit);
            List<DamengRecord> records = this.dbClient.selectRecords(param);
            if (CollectionUtil.isEmpty(records)) {
                break;
            }
            long end1 = System.currentTimeMillis();
            JulLog.info("查询耗时: {}ms", (end1 - start1));
            long start2 = System.currentTimeMillis();
            List<String> inserts = ShellDamengDataUtil.toInsertSql(columns, records, true);
            this.fileWriter.appendLines(inserts);
            long end2 = System.currentTimeMillis();
            JulLog.info("写入耗时: {}ms", (end2 - start2));
            start += this.queryLimit;
            this.processed(records.size());
        }
        if (hasIdentity) {
            String line = "SET IDENTITY_INSERT " + DBUtil.wrap(tableName, DBDialect.DAMENG) + " OFF;";
            this.fileWriter.appendLine(line);
        }
    }

    protected void dumpView() throws Exception {
        List<DamengView> views = this.dbClient.selectViews(this.dbName);
        if (CollectionUtil.isNotEmpty(views)) {
            for (DamengView view : views) {
                this.checkInterrupt();
                this.message("Dumping View " + view.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- View structure for " + view.getName();
                String line3 = "-- ----------------------------";
                String dropTable = "DROP VIEW IF EXISTS " + DBUtil.wrap(view.getName(), DBDialect.DAMENG) + ";";
                String createDefinition = this.dbClient.showCreateView(this.dbName, view.getName());
                if (!createDefinition.endsWith(";")) {
                    createDefinition += ";";
                }
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTable, createDefinition));
                this.processedIncr();
            }
        }
    }

    protected void dumpFunction() throws Exception {
        List<DamengFunction> functions = this.dbClient.selectFunctions(this.dbName);
        if (CollectionUtil.isNotEmpty(functions)) {
            for (DamengFunction function : functions) {
                this.checkInterrupt();
                this.message("Dumping Function " + function.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Function structure for " + function.getName();
                String line3 = "-- ----------------------------";
                String dropFunction = "DROP FUNCTION IF EXISTS " + DBUtil.wrap(function.getName(), DBDialect.DAMENG) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = this.dbClient.showCreateFunction(this.dbName, function.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropFunction, line4, createDefinition, line5, line6));
                this.processedIncr();
            }
        }
    }

    protected void dumpProcedure() throws Exception {
        List<DamengProcedure> procedures = this.dbClient.selectProcedures(this.dbName);
        if (CollectionUtil.isNotEmpty(procedures)) {
            for (DamengProcedure procedure : procedures) {
                this.checkInterrupt();
                this.message("Dumping Procedure " + procedure.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Procedure structure for " + procedure.getName();
                String line3 = "-- ----------------------------";
                String dropProcedure = "DROP PROCEDURE IF EXISTS " + DBUtil.wrap(procedure.getName(), DBDialect.DAMENG) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = this.dbClient.showCreateProcedure(this.dbName, procedure.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropProcedure, line4, createDefinition, line5, line6));
                this.processedIncr();
            }
        }
    }

    protected void dumpTrigger() throws Exception {
        List<DamengTrigger> triggers = this.dbClient.selectTriggers(this.dbName);
        if (CollectionUtil.isNotEmpty(triggers)) {
            for (DamengTrigger trigger : triggers) {
                this.message("Dumping Trigger " + trigger.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Trigger structure for " + trigger.getName();
                String line3 = "-- ----------------------------";
                String dropTrigger = "DROP TRIGGER IF EXISTS " + DBUtil.wrap(trigger.getName(), DBDialect.DAMENG) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = this.dbClient.showCreateTrigger(this.dbName, trigger.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTrigger, line4, createDefinition, line5, line6));
                this.processedIncr();
            }
        }
    }

    // protected void dumpEvent() throws Exception {
    //     List<DamengEvent> events = this.dbClient.events(this.dbName);
    //     if (CollectionUtil.isNotEmpty(events)) {
    //         for (DamengEvent event : events) {
    //             this.message("Dumping Event " + event.getName());
    //             String line0 = "";
    //             String line1 = "-- ----------------------------";
    //             String line2 = "-- Event structure for " + event.getName();
    //             String line3 = "-- ----------------------------";
    //             String dropTrigger = "DROP EVENT IF EXISTS " + DBUtil.wrap(event.getName()) + ";";
    //             String line4 = "delimiter ;;";
    //             String line5 = ";;";
    //             String line6 = "delimiter ;";
    //             String createDefinition = event.getCreateDefinition();
    //             this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTrigger, line4, createDefinition, line5, line6));
    //         }
    //         this.processed(events.size());
    //     }
    // }
}

