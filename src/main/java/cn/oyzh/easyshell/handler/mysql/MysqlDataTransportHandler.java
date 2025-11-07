package cn.oyzh.easyshell.handler.mysql;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportEvent;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportFunction;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportProcedure;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportTable;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportTrigger;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportView;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.column.MysqlSelectColumnParam;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlSelectRecordParam;
import cn.oyzh.easyshell.util.mysql.DBDataUtil;
import cn.oyzh.easyshell.util.mysql.DBUtil;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/06
 */
public class MysqlDataTransportHandler extends DataTransportHandler {

    @Override
    public void doTransport() throws Exception {
        this.message("Transport Starting");
        try {
            this.targetClient.executeSqlSimple(this.targetDatabase, "SET FOREIGN_KEY_CHECKS = 0;");
            if (CollectionUtil.isNotEmpty(this.tables)) {
                for (DataTransportTable table : this.tables) {
                    this.transportTable(table.getName());
                }
            }
            if (CollectionUtil.isNotEmpty(this.views)) {
                for (DataTransportView view : this.views) {
                    this.transportView(view.getName());
                }
            }
            if (CollectionUtil.isNotEmpty(this.functions)) {
                for (DataTransportFunction function : this.functions) {
                    this.transportFunction(function.getName());
                }
            }
            if (CollectionUtil.isNotEmpty(this.procedures)) {
                for (DataTransportProcedure procedure : this.procedures) {
                    this.transportProcedure(procedure.getName());
                }
            }
            if (CollectionUtil.isNotEmpty(this.triggers)) {
                for (DataTransportTrigger trigger : this.triggers) {
                    this.transportTrigger(trigger.getName());
                }
            }
            if (CollectionUtil.isNotEmpty(this.events)) {
                for (DataTransportEvent event : this.events) {
                    this.transportEvent(event.getName());
                }
            }
            this.targetClient.executeSqlSimple(this.targetDatabase, "SET FOREIGN_KEY_CHECKS = 1;");
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
     * @throws InterruptedException 异常
     */
    private void transportTable(String tableName) throws InterruptedException {
        this.checkInterrupt();
        // 删除表
        String dropTable = "DROP TABLE IF EXISTS " + DBUtil.wrap(tableName, DBDialect.MYSQL) + ";";
        this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
        this.message("Drop Table " + tableName);
        this.processedIncr();

        // 创建表
        String createTable = this.sourceClient.showCreateTable(this.sourceDatabase, tableName);
        this.targetClient.executeSqlSimple(this.targetDatabase, createTable);
        this.message("Create Table " + tableName);
        this.processedIncr();

        // 传输表
        this.message("Transport Table " + tableName + " Starting");
        List<MysqlColumn> columns = this.sourceClient.selectColumns(new MysqlSelectColumnParam(this.sourceDatabase, tableName));
        MysqlColumns dbColumns = new MysqlColumns(columns);
        long start = 0;
        while (true) {
            this.checkInterrupt();
            MysqlSelectRecordParam param = new MysqlSelectRecordParam();
            param.setStart(start);
            param.setReadonly(true);
            param.setTableName(tableName);
            param.setDbName(this.sourceDatabase);
            param.setLimit((long) this.selectLimit);
            List<MysqlRecord> records = this.sourceClient.selectRecords(param);
            if (CollectionUtil.isEmpty(records)) {
                break;
            }
            List<String> list = DBDataUtil.toInsertSql(dbColumns, records);
            this.addInsertSql(list);
            start += this.selectLimit;
        }
        this.message("Transport Table " + tableName + " Finished");
    }

    /**
     * 传输视图
     *
     * @param viewName 视图名称
     * @throws InterruptedException 异常
     */
    private void transportView(String viewName) throws InterruptedException {
        this.checkInterrupt();
        // 删除视图
        String dropTable = "DROP VIEW IF EXISTS " + DBUtil.wrap(viewName, DBDialect.MYSQL) + ";";
        this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
        this.message("Drop View " + viewName);
        this.processedIncr();

        // 创建视图
        String createView = this.sourceClient.showCreateView(this.sourceDatabase, viewName);
        this.targetClient.executeSqlSimple(this.targetDatabase, createView);
        this.message("Create View " + viewName);
        this.processedIncr();
    }

    /**
     * 传输函数
     *
     * @param functionName 函数名称
     * @throws InterruptedException 异常
     */
    private void transportFunction(String functionName) throws InterruptedException {
        this.checkInterrupt();
        // 删除函数
        String dropTable = "DROP FUNCTION IF EXISTS " + DBUtil.wrap(functionName, DBDialect.MYSQL) + ";";
        this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
        this.message("Drop Function " + functionName);
        this.processedIncr();

        // 创建函数
        String createView = this.sourceClient.showCreateFunction(this.sourceDatabase, functionName);
        this.targetClient.executeSqlSimple(this.targetDatabase, createView);
        this.message("Create Function " + functionName);
        this.processedIncr();
    }

    /**
     * 传输过程
     *
     * @param procedureName 过程名称
     * @throws InterruptedException 异常
     */
    private void transportProcedure(String procedureName) throws InterruptedException {
        this.checkInterrupt();
        // 删除过程
        String dropTable = "DROP PROCEDURE IF EXISTS " + DBUtil.wrap(procedureName, DBDialect.MYSQL) + ";";
        this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
        this.message("Drop Procedure " + procedureName);
        this.processedIncr();

        // 创建过程
        String createView = this.sourceClient.showCreateProcedure(this.sourceDatabase, procedureName);
        this.targetClient.executeSqlSimple(this.targetDatabase, createView);
        this.message("Create Procedure " + procedureName);
        this.processedIncr();
    }

    /**
     * 传输触发器
     *
     * @param triggerName 触发器名称
     * @throws InterruptedException 异常
     */
    private void transportTrigger(String triggerName) throws InterruptedException {
        this.checkInterrupt();
        // 删除触发器
        String dropTable = "DROP TRIGGER IF EXISTS " + DBUtil.wrap(triggerName, DBDialect.MYSQL) + ";";
        this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
        this.message("Drop Trigger " + triggerName);
        this.processedIncr();

        // 创建触发器
        String createView = this.sourceClient.showCreateTrigger(this.sourceDatabase, triggerName);
        this.targetClient.executeSqlSimple(this.targetDatabase, createView);
        this.message("Create Trigger " + triggerName);
        this.processedIncr();
    }

    /**
     * 传输事件
     *
     * @param eventName 事件名称
     * @throws InterruptedException 异常
     */
    private void transportEvent(String eventName) throws InterruptedException {
        this.checkInterrupt();
        // 删除事件
        String dropTable = "DROP EVENT IF EXISTS " + DBUtil.wrap(eventName, DBDialect.MYSQL) + ";";
        this.targetClient.executeSqlSimple(this.targetDatabase, dropTable);
        this.message("Drop Event " + eventName);
        this.processedIncr();

        // 创建事件
        String createEvent = this.sourceClient.showCreateEvent(this.sourceDatabase, eventName);
        this.targetClient.executeSqlSimple(this.targetDatabase, createEvent);
        this.message("Create Event " + eventName);
        this.processedIncr();
    }
}

