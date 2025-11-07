package cn.oyzh.easyshell.handler.mysql;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.column.MysqlSelectColumnParam;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlSelectRecordParam;
import cn.oyzh.easyshell.mysql.table.MysqlSelectTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.mysql.trigger.MysqlTrigger;
import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.easyshell.util.mysql.DBDataUtil;
import cn.oyzh.easyshell.util.mysql.DBUtil;

import java.io.IOException;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/10
 */
public class MysqlDataDumpHandler extends DataDumpHandler {

    public MysqlDataDumpHandler(ShellMysqlClient dbClient, String dbName) {
        super(dbClient, dbName);
    }

    @Override
    public void doDump() throws Exception {
        if (this.fileWriter == null || this.dumpType == null || this.dataType == null) {
            throw new RuntimeException("parameter invalid!");
        }
        this.message("Dump Starting");
        this.writeHeader();
        if (this.dumpType == 1) {
            this.dumpTable();
            this.dumpView();
            this.dumpFunction();
            this.dumpProcedure();
            this.dumpTrigger();
            this.dumpEvent();
        } else if (this.dumpType == 2) {
            MysqlSelectTableParam selectTableParam = new MysqlSelectTableParam();
            selectTableParam.setFull(true);
            selectTableParam.setDbName(this.dbName);
            selectTableParam.setTableName(this.tableName);
            MysqlTable table = this.dbClient.selectFullTable(selectTableParam);
            this.dumpTable(table);
        }
        this.writeTail();
        this.fileWriter.close();
        this.message("Dump Finished");
        this.message("Dump File To -> " + this.dumpFile.getPath());
    }

    protected void dumpTable() throws InterruptedException, IOException {
        MysqlSelectTableParam selectTableParam = new MysqlSelectTableParam();
        selectTableParam.setFull(true);
        selectTableParam.setDbName(this.dbName);
        List<MysqlTable> tables = this.dbClient.selectTables(selectTableParam);
        if (CollectionUtil.isNotEmpty(tables)) {
            for (MysqlTable table : tables) {
                this.checkInterrupt();
                this.dumpTable(table);
            }
            this.processed(tables.size());
        }
    }

    protected void dumpTable(MysqlTable table) throws InterruptedException, IOException {
        String line0 = "";
        String line1 = "-- ----------------------------";
        String line2 = "-- Table structure for " + table.getName();
        String line3 = "-- ----------------------------";
        String dropTable = "DROP TABLE IF EXISTS " + DBUtil.wrap(table.getName(), DBDialect.MYSQL) + ";";
        String createDefinition = table.getCreateDefinition();
        if (!createDefinition.endsWith(";")) {
            createDefinition += ";";
        }
        this.message("Dumping Table " + table.getName());
        this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTable, createDefinition));
        if (this.isDumpRecord()) {
            this.message("Dumping Records of Table " + table.getName());
            this.dumpRecord(table.getName());
        }
    }

    protected void dumpRecord(String tableName) throws InterruptedException, IOException {
        long start = 0;
        String line0 = "";
        String line1 = "-- ----------------------------";
        String line2 = "-- Records of " + tableName;
        String line3 = "-- ----------------------------";
        this.fileWriter.appendLines(List.of(line0, line1, line2, line3));
        MysqlColumns columns = new MysqlColumns(this.dbClient.selectColumns(new MysqlSelectColumnParam(this.dbName, tableName)));
        while (true) {
            this.checkInterrupt();
            long start1 = System.currentTimeMillis();
            MysqlSelectRecordParam param = new MysqlSelectRecordParam();
            param.setStart(start);
            param.setReadonly(true);
            param.setColumns(columns);
            param.setDbName(this.dbName);
            param.setTableName(tableName);
            param.setLimit((long) this.queryLimit);
            List<MysqlRecord> records = this.dbClient.selectRecords(param);
            if (CollectionUtil.isEmpty(records)) {
                break;
            }
            long end1 = System.currentTimeMillis();
            JulLog.info("查询耗时: {}ms", (end1 - start1));
            long start2 = System.currentTimeMillis();
            List<String> inserts = DBDataUtil.toInsertSql(columns, records);
            this.fileWriter.appendLines(inserts);
            long end2 = System.currentTimeMillis();
            JulLog.info("写入耗时: {}ms", (end2 - start2));
            start += this.queryLimit;
            this.processed(records.size());
        }
    }

    protected void dumpView() throws Exception {
        List<MysqlView> views = this.dbClient.views(this.dbName);
        if (CollectionUtil.isNotEmpty(views)) {
            for (MysqlView view : views) {
                this.checkInterrupt();
                this.message("Dumping View " + view.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- View structure for " + view.getName();
                String line3 = "-- ----------------------------";
                String dropTable = "DROP VIEW IF EXISTS " + DBUtil.wrap(view.getName(), DBDialect.MYSQL) + ";";
                String createDefinition = this.dbClient.showCreateView(this.dbName, view.getName());
                if (!createDefinition.endsWith(";")) {
                    createDefinition += ";";
                }
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTable, createDefinition));
            }
            this.processed(views.size());
        }
    }

    protected void dumpFunction() throws Exception {
        List<MysqlFunction> functions = this.dbClient.functions(this.dbName);
        if (CollectionUtil.isNotEmpty(functions)) {
            for (MysqlFunction function : functions) {
                this.checkInterrupt();
                this.message("Dumping Function " + function.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Function structure for " + function.getName();
                String line3 = "-- ----------------------------";
                String dropFunction = "DROP FUNCTION IF EXISTS " + DBUtil.wrap(function.getName(), DBDialect.MYSQL) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = this.dbClient.showCreateFunction(this.dbName, function.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropFunction, line4, createDefinition, line5, line6));
            }
            this.processed(functions.size());
        }
    }

    protected void dumpProcedure() throws Exception {
        List<MysqlProcedure> procedures = this.dbClient.procedures(this.dbName);
        if (CollectionUtil.isNotEmpty(procedures)) {
            for (MysqlProcedure procedure : procedures) {
                this.checkInterrupt();
                this.message("Dumping Procedure " + procedure.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Procedure structure for " + procedure.getName();
                String line3 = "-- ----------------------------";
                String dropProcedure = "DROP PROCEDURE IF EXISTS " + DBUtil.wrap(procedure.getName(), DBDialect.MYSQL) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = this.dbClient.showCreateProcedure(this.dbName, procedure.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropProcedure, line4, createDefinition, line5, line6));
            }
            this.processed(procedures.size());
        }
    }

    protected void dumpTrigger() throws Exception {
        List<MysqlTrigger> triggers = this.dbClient.triggers(this.dbName);
        if (CollectionUtil.isNotEmpty(triggers)) {
            for (MysqlTrigger trigger : triggers) {
                this.message("Dumping Trigger " + trigger.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Trigger structure for " + trigger.getName();
                String line3 = "-- ----------------------------";
                String dropTrigger = "DROP TRIGGER IF EXISTS " + DBUtil.wrap(trigger.getName(), DBDialect.MYSQL) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = this.dbClient.showCreateTrigger(this.dbName, trigger.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTrigger, line4, createDefinition, line5, line6));
            }
            this.processed(triggers.size());
        }
    }

    protected void dumpEvent() throws Exception {
        List<MysqlEvent> events = this.dbClient.events(this.dbName);
        if (CollectionUtil.isNotEmpty(events)) {
            for (MysqlEvent event : events) {
                this.message("Dumping Event " + event.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Event structure for " + event.getName();
                String line3 = "-- ----------------------------";
                String dropTrigger = "DROP EVENT IF EXISTS " + DBUtil.wrap(event.getName(), DBDialect.MYSQL) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = event.getCreateDefinition();
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTrigger, line4, createDefinition, line5, line6));
            }
            this.processed(events.size());
        }
    }
}

