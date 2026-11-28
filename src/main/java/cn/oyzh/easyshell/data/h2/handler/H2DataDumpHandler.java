package cn.oyzh.easyshell.data.h2.handler;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2ColumnDefinitionParser;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.generator.record.H2RecordSqlGenerator;
import cn.oyzh.easyshell.h2.schema.H2SchemaObject;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.handler.DBDataDumpHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * H2 数据转储处理器。
 *
 * @since 2026-11-02
 */
public class H2DataDumpHandler extends DBDataDumpHandler {

    private final ShellH2Client client;

    private final String schema;

    private final List<String> foreignKeyScripts = new ArrayList<>();

    public H2DataDumpHandler(ShellH2Client client, String dbName) {
        super(dbName, DBDialect.H2);
        this.client = client;
        this.schema = client.schema(dbName);
    }

    @Override
    protected void doActionInternal() throws Exception {
        this.message("Dump Starting");
        this.foreignKeyScripts.clear();
        try {
            this.writeHeader();
            try {
                if (this.dumpType == 1) {
                    this.dumpDatabase();
                } else if (this.dumpType == 2) {
                    this.dumpTable(this.tableName);
                } else {
                    throw new IllegalArgumentException("Unsupported H2 dump type: " + this.dumpType);
                }
                this.writeForeignKeyScripts();
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
        this.fileWriter.writeLine("CREATE SCHEMA IF NOT EXISTS "
                + H2GeneratorUtil.quote(this.schema) + ";");
        for (H2SchemaObject object : this.client.selectSchemaObjectsIn(this.schema, "table")) {
            this.checkInterrupt();
            try {
                this.dumpTable(object.getName(), false);
            } catch (Exception ex) {
                this.handleDumpError(ex);
            }
        }
        for (H2SchemaObject object : this.client.selectSchemaObjectsIn(this.schema, "index")) {
            if (object.getSql() == null) {
                continue;
            }
            this.checkInterrupt();
            try {
                this.writeSql(object.getSql());
                this.processedIncr();
            } catch (Exception ex) {
                this.handleDumpError(ex);
            }
        }
        for (H2SchemaObject object : this.client.selectSchemaObjectsIn(this.schema, "view")) {
            this.checkInterrupt();
            try {
                // 视图 DDL 由 VIEW_DEFINITION 重建，schema 对象不携带 sql
                String sql = this.viewDdl(object.getName());
                if (sql == null) {
                    continue;
                }
                this.writeSql(sql);
                this.processedIncr();
            } catch (Exception ex) {
                this.handleDumpError(ex);
            }
        }
        for (H2SchemaObject object : this.client.selectSchemaObjectsIn(this.schema, "trigger")) {
            if (object.getSql() == null) {
                continue;
            }
            this.checkInterrupt();
            try {
                this.writeSql(object.getSql());
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
        tableName = this.client.canonicalObjectName(this.schema, tableName);
        H2Table table = this.client.selectTable(this.schema, tableName);
        if (table == null) {
            throw new IllegalArgumentException("H2 table does not exist: " + tableName);
        }
        // selectTable 不加载建表语句，统一使用 showCreate 生成 DDL
        String definition = this.client.showCreate(this.schema, tableName);
        if (definition == null || definition.isBlank()) {
            throw new IllegalArgumentException("H2 table definition is empty: " + tableName);
        }
        String resolvedTableName = tableName;
        this.foreignKeyScripts.addAll(H2ColumnDefinitionParser.foreignKeys(definition).stream()
                .filter(foreignKey -> !foreignKey.isInvalid())
                .map(foreignKey -> "ALTER TABLE "
                        + H2GeneratorUtil.qualified(this.schema, resolvedTableName)
                        + " ADD " + foreignKey.toConstraintSql())
                .toList());
        this.fileWriter.writeLine("-- Table structure for " + tableName);
        this.writeSql(H2ColumnDefinitionParser.withoutForeignKeys(definition));
        this.processedIncr();
        if (this.isDumpRecord()) {
            this.dumpRecords(tableName);
        }
        if (writeDependencies) {
            this.dumpTableDependencies(tableName, "index");
            this.dumpTableDependencies(tableName, "trigger");
        }
    }

    private void writeForeignKeyScripts() throws java.io.IOException {
        for (String sql : this.foreignKeyScripts) {
            this.writeSql(sql);
            this.processedIncr();
        }
        this.foreignKeyScripts.clear();
    }

    private void dumpTableDependencies(String tableName, String type) throws Exception {
        for (H2SchemaObject object : this.client.selectSchemaObjectsIn(this.schema, type)) {
            if (StringUtil.equalsIgnoreCase(object.getTableName(), tableName)
                    && StringUtil.isNotBlank(object.getSql())) {
                this.checkInterrupt();
                this.writeSql(object.getSql());
                this.processedIncr();
            }
        }
    }

    /**
     * 写入一条 SQL，自动去除尾部多余分号，保证每行以单个分号结束。
     *
     * @param sql SQL 内容
     */
    private void writeSql(String sql) throws java.io.IOException {
        String value = sql.strip();
        while (value.endsWith(";")) {
            value = value.substring(0, value.length() - 1).stripTrailing();
        }
        this.fileWriter.writeLine(value + ";");
    }

    /**
     * 依据 VIEW_DEFINITION 重建视图 DDL。
     *
     * @param viewName 视图名称
     * @return 建视图语句，定义不可用时返回 null
     */
    private String viewDdl(String viewName) {
        String definition = this.client.viewDefinition(this.schema, viewName);
        if (StringUtil.isBlank(definition)) {
            return null;
        }
        return "CREATE VIEW " + H2GeneratorUtil.qualified(this.schema, viewName)
                + " AS " + definition.strip();
    }

    private void dumpRecords(String tableName) throws Exception {
        List<H2Column> columns = this.client.selectColumns(this.schema, tableName).stream()
                .filter(column -> !column.isGenerated())
                .toList();
        if (columns.isEmpty()) {
            return;
        }
        long offset = 0;
        while (true) {
            this.checkInterrupt();
            List<Map<String, Object>> records =
                    this.client.selectRecords(this.schema, tableName, columns,
                            offset, this.queryLimit);
            if (records.isEmpty()) {
                break;
            }
            for (Map<String, Object> record : records) {
                this.fileWriter.writeLine(H2RecordSqlGenerator.insertWithTypes(
                        this.schema, tableName, columns, record));
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
        // H2 不支持 SQLite 的 PRAGMA 语法，仅写注释头
        this.fileWriter.writeLine("-- H2 database dump, generated by EasyShell");
        this.fileWriter.writeLine("-- EasyShell H2 source schema: "
                + H2GeneratorUtil.quote(this.schema));
    }

    @Override
    protected void writeTail() throws java.io.IOException {
        this.fileWriter.writeLine("-- dump finished");
    }
}
