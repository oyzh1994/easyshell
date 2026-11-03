package cn.oyzh.easyshell.data.sqlite.handler;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.generator.record.SQLiteRecordSqlGenerator;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaObject;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.data.handler.DBDataTransportHandler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQLite 数据库间表、视图和数据传输处理器。
 *
 * @since 2026-11-02
 */
public class SQLiteDataTransportHandler extends DBDataTransportHandler<String> {

    private ShellSqliteClient sourceClient;

    private ShellSqliteClient targetClient;

    private List<DBDataTransportObject> tables;

    private List<DBDataTransportObject> views;

    private final List<String> tablesWithDependencies = new ArrayList<>();

    private final List<String> viewsWithTriggers = new ArrayList<>();

    public SQLiteDataTransportHandler() {
        super(DBDialect.SQLITE);
    }

    @Override
    public void doTransport() throws Exception {
        this.message("Transport Starting");
        Path sourcePath = Path.of(this.sourceClient.getShellConnect().getHost())
                .toAbsolutePath().normalize();
        Path targetPath = Path.of(this.targetClient.getShellConnect().getHost())
                .toAbsolutePath().normalize();
        if (sourcePath.equals(targetPath)) {
            throw new IllegalArgumentException("Source and target SQLite files must be different");
        }
        if (this.tables != null) {
            for (DBDataTransportObject table : this.tables) {
                this.checkInterrupt();
                try {
                    this.transportTable(table.getName());
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleTransportError(ex);
                }
            }
        }
        if (this.views != null) {
            this.viewsWithTriggers.addAll(this.views.stream()
                    .map(DBDataTransportObject::getName)
                    .toList());
        }
        this.doBatchInsert();
        for (String viewName : this.viewsWithTriggers) {
            this.checkInterrupt();
            try {
                this.transportView(viewName);
                this.processedIncr();
            } catch (Exception ex) {
                this.handleTransportError(ex);
            }
        }
        this.transportDeferredDependencies();
        this.message("Transport Finished");
    }

    private void transportTable(String tableName) throws Exception {
        SQLiteTable source = this.sourceClient.selectTable(tableName);
        if (source == null) {
            throw new IllegalArgumentException("Source SQLite table does not exist: " + tableName);
        }
        List<SQLiteColumn> columns = this.sourceClient.selectColumns(tableName);
        source.setColumns(columns);
        this.targetClient.executeSqlSimple("DROP TABLE IF EXISTS " + SQLiteGeneratorUtil.quote(tableName));
        if (source.getDefinition() == null || source.getDefinition().isBlank()) {
            throw new IllegalArgumentException("Source SQLite table definition is empty: " + tableName);
        }
        this.targetClient.executeSqlSimple(source.getDefinition());
        if (this.dataType != 1) {
            List<SQLiteColumn> insertColumns = columns.stream()
                    .filter(column -> !column.isGenerated())
                    .toList();
            long offset = 0;
            while (true) {
                this.checkInterrupt();
                List<Map<String, Object>> records = this.sourceClient.selectRecords(
                        tableName, insertColumns, offset, this.selectLimit);
                if (records.isEmpty()) {
                    break;
                }
                for (Map<String, Object> record : records) {
                    this.addInsert(SQLiteRecordSqlGenerator.insert(tableName,
                            insertColumns.stream().map(SQLiteColumn::getName).toList(), record));
                }
                offset += records.size();
                if (records.size() < this.selectLimit) {
                    break;
                }
            }
        }
        this.tablesWithDependencies.add(tableName);
    }

    private void transportView(String viewName) throws Exception {
        String definition = this.sourceClient.showCreate(viewName);
        if (definition == null) {
            throw new IllegalArgumentException("Source SQLite view does not exist: " + viewName);
        }
        this.targetClient.executeSqlSimple("DROP VIEW IF EXISTS " + SQLiteGeneratorUtil.quote(viewName));
        this.targetClient.executeSqlSimple(definition);
    }

    private void transportDeferredDependencies() throws Exception {
        for (String tableName : this.tablesWithDependencies) {
            this.checkInterrupt();
            try {
                this.transportDependencies(tableName, "index");
                this.transportDependencies(tableName, "trigger");
            } catch (Exception ex) {
                this.handleTransportError(ex);
            }
        }
        for (String viewName : this.viewsWithTriggers) {
            this.checkInterrupt();
            try {
                this.transportDependencies(viewName, "trigger");
            } catch (Exception ex) {
                this.handleTransportError(ex);
            }
        }
    }

    private void transportDependencies(String objectName, String type) {
        for (SQLiteSchemaObject object : this.sourceClient.selectSchemaObjects(type)) {
            if (objectName.equalsIgnoreCase(object.getTableName()) && object.getSql() != null) {
                this.targetClient.executeSqlSimple("DROP " + object.getType().toUpperCase()
                        + " IF EXISTS " + SQLiteGeneratorUtil.quote(object.getName()));
                this.targetClient.executeSqlSimple(object.getSql());
            }
        }
    }

    private void handleTransportError(Exception ex) throws Exception {
        if (!this.isContinueWithErrors()) {
            throw ex;
        }
        this.exception(ex);
    }

    @Override
    public void doBatchInsert(List<String> list, boolean parallel) throws Exception {
        if (this.isContinueWithErrors()) {
            for (String sql : list) {
                try {
                    int result = this.targetClient.insertBatch(
                            this.targetDatabase, List.of(sql), false);
                    this.processedIncr(result);
                } catch (Exception ex) {
                    this.exception(ex);
                }
            }
        } else {
            int result = this.targetClient.insertBatch(this.targetDatabase, list, false);
            this.processedIncr(result);
        }
    }

    @Override
    public boolean enableParallel() {
        return false;
    }

    public void setSourceClient(ShellSqliteClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    public void setTargetClient(ShellSqliteClient targetClient) {
        this.targetClient = targetClient;
    }

    public void setTables(List<DBDataTransportObject> tables) {
        this.tables = tables;
    }

    public void setViews(List<DBDataTransportObject> views) {
        this.views = views;
    }
}
