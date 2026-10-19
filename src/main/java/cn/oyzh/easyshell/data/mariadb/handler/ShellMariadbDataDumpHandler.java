package cn.oyzh.easyshell.data.mariadb.handler;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.dto.Project;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.column.MariadbSelectColumnParam;
import cn.oyzh.easyshell.mariadb.event.MariadbEvent;
import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.mariadb.procedure.MariadbProcedure;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.mariadb.record.MariadbSelectRecordParam;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.mariadb.trigger.MariadbTrigger;
import cn.oyzh.easyshell.mariadb.view.MariadbView;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbDataUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.handler.DBDataDumpHandler;
import cn.oyzh.fx.db.util.DBUtil;

import java.io.IOException;
import java.util.List;

/**
 * Mariadb数据转储处理器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbDataDumpHandler extends DBDataDumpHandler {

    /**
     * db客户端
     */
    protected ShellMariadbClient dbClient;

    /**
     * 构造 Mariadb数据转储处理器
     *
     * @param dbClient 数据库客户端
     * @param dbName   数据库名称
     */
    public ShellMariadbDataDumpHandler(ShellMariadbClient dbClient, String dbName) {
        super(dbName, DBDialect.MARIADB);
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
                this.dumpEvent();
            } else if (this.dumpType == 2) {
                MariadbTable table = this.dbClient.selectTable(this.dbName, this.tableName);
                this.dumpTable(table);
            }
            this.writeTail();
            this.fileWriter.close();
        } catch (Exception ex) {
            this.exception(ex);
        }
        this.message("Dump Finished");
        this.message("Dump File To -> " + this.dumpFile.getPath());
    }

    /**
     * 转储所有表
     *
     * @throws InterruptedException 中断异常
     * @throws IOException          IO异常
     */
    protected void dumpTable() throws InterruptedException, IOException {
        List<MariadbTable> tables = this.dbClient.selectTables(this.dbName);
        if (CollectionUtil.isNotEmpty(tables)) {
            for (MariadbTable table : tables) {
                this.checkInterrupt();
                this.dumpTable(table);
                this.processedIncr();
            }
        }
    }

    /**
     * 转储指定表
     *
     * @param table 表
     * @throws InterruptedException 中断异常
     * @throws IOException          IO异常
     */
    protected void dumpTable(MariadbTable table) throws InterruptedException, IOException {
        String line0 = "";
        String line1 = "-- ----------------------------";
        String line2 = "-- Table structure for " + table.getName();
        String line3 = "-- ----------------------------";
        String dropTable = "DROP TABLE IF EXISTS " + DBUtil.wrap(table.getName(), DBDialect.MARIADB) + ";";
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

    /**
     * 转储表记录
     *
     * @param tableName 表名称
     * @throws InterruptedException 中断异常
     * @throws IOException          IO异常
     */
    protected void dumpRecord(String tableName) throws InterruptedException, IOException {
        long start = 0;
        String line0 = "";
        String line1 = "-- ----------------------------";
        String line2 = "-- Records of " + tableName;
        String line3 = "-- ----------------------------";
        this.fileWriter.appendLines(List.of(line0, line1, line2, line3));
        MariadbColumns columns = new MariadbColumns(this.dbClient.selectColumns(new MariadbSelectColumnParam(this.dbName, tableName)));
        while (true) {
            this.checkInterrupt();
            long start1 = System.currentTimeMillis();
            MariadbSelectRecordParam param = new MariadbSelectRecordParam();
            param.setStart(start);
            param.setReadonly(true);
            param.setColumns(columns);
            param.setDbName(this.dbName);
            param.setTableName(tableName);
            param.setLimit((long) this.queryLimit);
            List<MariadbRecord> records = this.dbClient.selectRecords(param);
            if (CollectionUtil.isEmpty(records)) {
                break;
            }
            long end1 = System.currentTimeMillis();
            JulLog.info("查询耗时: {}ms", (end1 - start1));
            long start2 = System.currentTimeMillis();
            List<String> inserts = ShellMariadbDataUtil.toInsertSql(columns, records);
            this.fileWriter.appendLines(inserts);
            long end2 = System.currentTimeMillis();
            JulLog.info("写入耗时: {}ms", (end2 - start2));
            start += this.queryLimit;
            this.processed(records.size());
        }
    }

    /**
     * 转储视图
     *
     * @throws Exception 异常
     */
    protected void dumpView() throws Exception {
        List<MariadbView> views = this.dbClient.selectViews(this.dbName);
        if (CollectionUtil.isNotEmpty(views)) {
            for (MariadbView view : views) {
                this.checkInterrupt();
                this.message("Dumping View " + view.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- View structure for " + view.getName();
                String line3 = "-- ----------------------------";
                String dropTable = "DROP VIEW IF EXISTS " + DBUtil.wrap(view.getName(), DBDialect.MARIADB) + ";";
                String createDefinition = view.getCreateDefinition();
                //                String createDefinition = this.dbClient.showCreateView(this.dbName, view.getName());
                if (!createDefinition.endsWith(";")) {
                    createDefinition += ";";
                }
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTable, createDefinition));
                this.processedIncr();
            }
        }
    }

    /**
     * 转储函数
     *
     * @throws Exception 异常
     */
    protected void dumpFunction() throws Exception {
        List<MariadbFunction> functions = this.dbClient.selectFunctions(this.dbName);
        if (CollectionUtil.isNotEmpty(functions)) {
            for (MariadbFunction function : functions) {
                this.checkInterrupt();
                this.message("Dumping Function " + function.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Function structure for " + function.getName();
                String line3 = "-- ----------------------------";
                String dropFunction = "DROP FUNCTION IF EXISTS " + DBUtil.wrap(function.getName(), DBDialect.MARIADB) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = function.getCreateDefinition();
                //                String createDefinition = this.dbClient.showCreateFunction(this.dbName, function.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropFunction, line4, createDefinition, line5, line6));
                this.processedIncr();
            }
        }
    }

    /**
     * 转储过程
     *
     * @throws Exception 异常
     */
    protected void dumpProcedure() throws Exception {
        List<MariadbProcedure> procedures = this.dbClient.selectProcedures(this.dbName);
        if (CollectionUtil.isNotEmpty(procedures)) {
            for (MariadbProcedure procedure : procedures) {
                this.checkInterrupt();
                this.message("Dumping Procedure " + procedure.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Procedure structure for " + procedure.getName();
                String line3 = "-- ----------------------------";
                String dropProcedure = "DROP PROCEDURE IF EXISTS " + DBUtil.wrap(procedure.getName(), DBDialect.MARIADB) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = procedure.getCreateDefinition();
                //                String createDefinition = this.dbClient.showCreateProcedure(this.dbName, procedure.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropProcedure, line4, createDefinition, line5, line6));
                this.processedIncr();
            }
        }
    }

    /**
     * 转储触发器
     *
     * @throws Exception 异常
     */
    protected void dumpTrigger() throws Exception {
        List<MariadbTrigger> triggers = this.dbClient.selectTriggers(this.dbName);
        if (CollectionUtil.isNotEmpty(triggers)) {
            for (MariadbTrigger trigger : triggers) {
                this.message("Dumping Trigger " + trigger.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Trigger structure for " + trigger.getName();
                String line3 = "-- ----------------------------";
                String dropTrigger = "DROP TRIGGER IF EXISTS " + DBUtil.wrap(trigger.getName(), DBDialect.MARIADB) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = trigger.getCreateDefinition();
                //                String createDefinition = this.dbClient.showCreateTrigger(this.dbName, trigger.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTrigger, line4, createDefinition, line5, line6));
                this.processedIncr();
            }
        }
    }

    /**
     * 转储事件
     *
     * @throws Exception 异常
     */
    protected void dumpEvent() throws Exception {
        List<MariadbEvent> events = this.dbClient.selectEvents(this.dbName);
        if (CollectionUtil.isNotEmpty(events)) {
            for (MariadbEvent event : events) {
                this.message("Dumping Event " + event.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Event structure for " + event.getName();
                String line3 = "-- ----------------------------";
                String dropTrigger = "DROP EVENT IF EXISTS " + DBUtil.wrap(event.getName(), DBDialect.MARIADB) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = event.getCreateDefinition();
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTrigger, line4, createDefinition, line5, line6));
                this.processedIncr();
            }
        }
    }

    @Override
    protected void writeHeader() throws IOException {
        String version = this.dbClient.selectVersion();
        String clientCharacter = this.dbClient.selectClientCharacter();
        String header = "/*\n";
        header += " " + Project.load().getName() + " Data Transfer";
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
        header += "SET NAMES " + clientCharacter + ";";
        header += "\n";
        header += "SET FOREIGN_KEY_CHECKS = 0;";
        this.fileWriter.writeLines(List.of(header));
    }

    @Override
    protected void writeTail() throws IOException {
        String tail = "\n";
        tail += "SET FOREIGN_KEY_CHECKS = 1;";
        this.fileWriter.appendLines(List.of(tail));
    }
}
