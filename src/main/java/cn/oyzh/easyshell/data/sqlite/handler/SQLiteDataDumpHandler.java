package cn.oyzh.easyshell.data.sqlite.handler;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.generator.record.SQLiteRecordSqlGenerator;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaObject;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.handler.DBDataDumpHandler;

import java.util.List;
import java.util.Map;

/**
 * SQLite 数据转储处理器。
 *
 * @since 2026-11-02
 */
public class SQLiteDataDumpHandler extends DBDataDumpHandler {

    private final ShellSqliteClient client;

    public SQLiteDataDumpHandler(ShellSqliteClient client, String dbName) {
        super(dbName, DBDialect.SQLITE);
        this.client = client;
    }

    @Override
    public void doDump() throws Exception {
        this.message("Dump Starting");
        try {
            this.writeHeader();
            try {
                if (this.dumpType == 1) {
                    this.dumpDatabase();
                } else if (this.dumpType == 2) {
                    this.dumpTable(this.tableName);
                } else {
                    throw new IllegalArgumentException("Unsupported SQLite dump type: " + this.dumpType);
                }
            } catch (Exception ex) {
                this.handleDumpError(ex);
            }
        } finally {
            try {
                this.writeTail();
            } finally {
                this.fileWriter.close();
                this.message("Dump Finished");
                this.message("Dump File To -> " + this.dumpFile.getPath());
            }
        }
    }

    private void dumpDatabase() throws Exception {
        for (SQLiteSchemaObject object : this.client.selectSchemaObjects("table")) {
            this.checkInterrupt();
            try {
                this.dumpTable(object.getName(), false);
            } catch (Exception ex) {
                this.handleDumpError(ex);
            }
        }
        for (SQLiteSchemaObject object : this.client.selectSchemaObjects("index")) {
            if (object.getSql() == null) {
                continue;
            }
            this.checkInterrupt();
            try {
                this.fileWriter.writeLine(object.getSql() + ";");
                this.processedIncr();
            } catch (Exception ex) {
                this.handleDumpError(ex);
            }
        }
        for (SQLiteSchemaObject object : this.client.selectSchemaObjects("view")) {
            this.checkInterrupt();
            try {
                this.fileWriter.writeLine(object.getSql() + ";");
                this.processedIncr();
            } catch (Exception ex) {
                this.handleDumpError(ex);
            }
        }
        for (SQLiteSchemaObject object : this.client.selectSchemaObjects("trigger")) {
            if (object.getSql() == null) {
                continue;
            }
            this.checkInterrupt();
            try {
                this.fileWriter.writeLine(object.getSql() + ";");
                this.processedIncr();
            } catch (Exception ex) {
                this.handleDumpError(ex);
            }
        }
    }

    private void dumpTable(String tableName) throws Exception {
        this.dumpTable(tableName, true);
    }

    private void dumpTable(String tableName, boolean writeDependencies) throws Exception {
        SQLiteTable table = this.client.selectTable(tableName);
        if (table == null) {
            throw new IllegalArgumentException("SQLite table does not exist: " + tableName);
        }
        this.fileWriter.writeLine("-- Table structure for " + tableName);
        this.fileWriter.writeLine(table.getDefinition() + ";");
        this.processedIncr();
        if (this.isDumpRecord()) {
            this.dumpRecords(tableName);
        }
        if (writeDependencies) {
            this.dumpTableDependencies(tableName, "index");
            this.dumpTableDependencies(tableName, "trigger");
        }
    }

    private void dumpTableDependencies(String tableName, String type) throws Exception {
        for (SQLiteSchemaObject object : this.client.selectSchemaObjects(type)) {
            if (StringUtil.equalsIgnoreCase(object.getTableName(), tableName)
                    && StringUtil.isNotBlank(object.getSql())) {
                this.checkInterrupt();
                this.fileWriter.writeLine(object.getSql() + ";");
                this.processedIncr();
            }
        }
    }

    private void dumpRecords(String tableName) throws Exception {
        List<SQLiteColumn> columns = this.client.selectColumns(tableName).stream()
                .filter(column -> !column.isGenerated())
                .toList();
        if (columns.isEmpty()) {
            return;
        }
        long offset = 0;
        while (true) {
            this.checkInterrupt();
            List<Map<String, Object>> records =
                    this.client.selectRecords(tableName, columns, offset, this.queryLimit);
            if (records.isEmpty()) {
                break;
            }
            for (Map<String, Object> record : records) {
                this.fileWriter.writeLine(SQLiteRecordSqlGenerator.insert(tableName,
                        columns.stream().map(SQLiteColumn::getName).toList(), record));
                this.processedIncr();
            }
            offset += records.size();
            if (records.size() < this.queryLimit) {
                break;
            }
        }
    }

    @Override
    protected void writeHeader() throws java.io.IOException {
        this.fileWriter.writeLine("PRAGMA foreign_keys=OFF;");
        this.fileWriter.writeLine("BEGIN;");
        this.fileWriter.writeLine("PRAGMA defer_foreign_keys=ON;");
    }

    @Override
    protected void writeTail() throws java.io.IOException {
        this.fileWriter.writeLine("COMMIT;");
        this.fileWriter.writeLine("PRAGMA foreign_keys=ON;");
    }
}
