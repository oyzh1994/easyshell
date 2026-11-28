package cn.oyzh.easyshell.data.h2.handler;

import cn.oyzh.easyshell.data.h2.util.H2SqlSchemaRewriter;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2ColumnDefinitionParser;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.generator.record.H2RecordSqlGenerator;
import cn.oyzh.easyshell.h2.generator.view.H2ViewCreateSqlGenerator;
import cn.oyzh.easyshell.h2.schema.H2SchemaObject;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.ShellH2ConnConfig;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.data.handler.DBDataTransportHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * H2 数据库间表、视图和数据传输处理器。
 *
 * @since 2026-11-02
 */
public class H2DataTransportHandler extends DBDataTransportHandler<String> {

    private ShellH2Client sourceClient;

    private ShellH2Client targetClient;

    private List<DBDataTransportObject> tables;

    private List<DBDataTransportObject> views;

    private final List<String> tablesWithDependencies = new ArrayList<>();

    private final List<String> viewsWithTriggers = new ArrayList<>();

    private final List<String> foreignKeyScripts = new ArrayList<>();

    public H2DataTransportHandler() {
        super(DBDialect.H2);
    }

    @Override
    protected void doActionInternal() throws Exception {
        this.message("Transport Starting");
        this.foreignKeyScripts.clear();
        if (this.sameDatabase()
                && this.sourceClient.schema(this.sourceDatabase)
                .equalsIgnoreCase(this.targetClient.schema(this.targetDatabase))) {
            throw new IllegalArgumentException("Source and target H2 schemas must be different");
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
        this.transportForeignKeys();
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

    private boolean sameDatabase() {
        ShellH2ConnConfig source = this.sourceClient.h2Config();
        ShellH2ConnConfig target = this.targetClient.h2Config();
        if (source.isFileMode() && target.isFileMode()) {
            return source.resolveFilePath().equals(target.resolveFilePath());
        }
        if (source.isMemMode() && target.isMemMode()) {
            return Objects.equals(source.getHost(), target.getHost());
        }
        return source.isTcpMode() && target.isTcpMode()
                && Objects.equals(source.getHost(), target.getHost())
                && Objects.equals(source.getPort(), target.getPort())
                && Objects.equals(source.getDatabase(), target.getDatabase());
    }

    private void transportTable(String tableName) throws Exception {
        String sourceSchema = this.sourceClient.schema(this.sourceDatabase);
        String targetSchema = this.targetClient.schema(this.targetDatabase);
        String sourceTableName = this.sourceClient.canonicalObjectName(sourceSchema, tableName);
        String targetTableName = this.targetClient.canonicalObjectName(targetSchema, tableName);
        H2Table source = this.sourceClient.selectTable(sourceSchema, sourceTableName);
        if (source == null) {
            throw new IllegalArgumentException("Source H2 table does not exist: " + sourceTableName);
        }
        List<H2Column> columns = this.sourceClient.selectColumns(sourceSchema, sourceTableName);
        source.setColumns(columns);
        this.targetClient.executeSqlSimple("DROP TABLE IF EXISTS "
                + H2GeneratorUtil.qualified(targetSchema, targetTableName));
        String definition = this.sourceClient.showCreate(sourceSchema, sourceTableName);
        if (definition == null || definition.isBlank()) {
            throw new IllegalArgumentException("Source H2 table definition is empty: " + sourceTableName);
        }
        String targetDefinition = H2ColumnDefinitionParser.requalifyTable(
                H2ColumnDefinitionParser.withoutForeignKeys(definition),
                targetSchema, targetTableName);
        this.targetClient.executeSqlSimple(targetSchema, targetDefinition);
        this.foreignKeyScripts.addAll(H2ColumnDefinitionParser.foreignKeys(definition).stream()
                .filter(foreignKey -> !foreignKey.isInvalid())
                .map(foreignKey -> "ALTER TABLE "
                        + H2GeneratorUtil.qualified(targetSchema, targetTableName)
                        + " ADD " + foreignKey.toConstraintSql())
                .toList());
        if (this.dataType != 1) {
            List<H2Column> insertColumns = columns.stream()
                    .filter(column -> !column.isGenerated())
                    .toList();
            long offset = 0;
            while (true) {
                this.checkInterrupt();
                List<Map<String, Object>> records = this.sourceClient.selectRecords(
                        sourceSchema, sourceTableName, insertColumns, offset, this.selectLimit);
                if (records.isEmpty()) {
                    break;
                }
                for (Map<String, Object> record : records) {
                    this.addInsert(H2RecordSqlGenerator.insertWithTypes(
                            targetSchema, targetTableName, insertColumns, record));
                }
                offset += records.size();
                if (records.size() < this.selectLimit) {
                    break;
                }
            }
        }
        this.tablesWithDependencies.add(sourceTableName);
    }

    private void transportForeignKeys() throws Exception {
        for (String sql : this.foreignKeyScripts) {
            this.targetClient.executeSqlSimple(this.targetClient.schema(this.targetDatabase), sql);
        }
        this.foreignKeyScripts.clear();
    }

    private void transportView(String viewName) throws Exception {
        String sourceSchema = this.sourceClient.schema(this.sourceDatabase);
        String targetSchema = this.targetClient.schema(this.targetDatabase);
        String sourceViewName = this.sourceClient.canonicalObjectName(sourceSchema, viewName);
        String targetViewName = this.targetClient.canonicalObjectName(targetSchema, viewName);
        String definition = this.sourceClient.viewDefinition(sourceSchema, sourceViewName);
        if (definition == null) {
            throw new IllegalArgumentException("Source H2 view does not exist: " + viewName);
        }
        definition = this.requalifySchema(definition, sourceSchema, targetSchema);
        this.targetClient.executeSqlSimple("DROP VIEW IF EXISTS "
                + H2GeneratorUtil.qualified(targetSchema, targetViewName));
        this.targetClient.executeSqlSimple(targetSchema,
                H2ViewCreateSqlGenerator.createView(targetSchema, targetViewName, definition));
    }

    private void transportDeferredDependencies() throws Exception {
        String sourceSchema = this.sourceClient.schema(this.sourceDatabase);
        String targetSchema = this.targetClient.schema(this.targetDatabase);
        for (String tableName : this.tablesWithDependencies) {
            this.checkInterrupt();
            try {
                this.transportDependencies(sourceSchema, targetSchema, tableName, "index");
                this.transportDependencies(sourceSchema, targetSchema, tableName, "trigger");
            } catch (Exception ex) {
                this.handleTransportError(ex);
            }
        }
        for (String viewName : this.viewsWithTriggers) {
            this.checkInterrupt();
            try {
                this.transportDependencies(sourceSchema, targetSchema, viewName, "trigger");
            } catch (Exception ex) {
                this.handleTransportError(ex);
            }
        }
    }

    private void transportDependencies(String sourceSchema, String targetSchema,
                                       String objectName, String type) {
        for (H2SchemaObject object : this.sourceClient.selectSchemaObjectsIn(sourceSchema, type)) {
            if (objectName.equalsIgnoreCase(object.getTableName()) && object.getSql() != null) {
                this.targetClient.executeSqlSimple(targetSchema,
                        "DROP " + object.getType().toUpperCase() + " IF EXISTS "
                                + H2GeneratorUtil.qualified(targetSchema, object.getName()));
                String dependencySource = H2SqlSchemaRewriter.firstQualifiedSchema(object.getSql());
                if (dependencySource == null) {
                    dependencySource = sourceSchema;
                }
                this.targetClient.executeSqlSimple(targetSchema,
                        this.requalifySchema(object.getSql(), dependencySource, targetSchema));
            }
        }
    }

    /**
     * 将对象 SQL 中显式限定的来源模式替换为目标模式。
     *
     * @param sql          来源 SQL
     * @param sourceSchema 来源模式
     * @param targetSchema 目标模式
     * @return 目标 SQL
     */
    private String requalifySchema(String sql, String sourceSchema, String targetSchema) {
        if (sql == null || sourceSchema.equalsIgnoreCase(targetSchema)) {
            return sql;
        }
        return H2SqlSchemaRewriter.rewriteStatement(sql, sourceSchema, targetSchema);
    }

    private void handleTransportError(Exception ex) throws Exception {
        this.handleFailure(ex, this.isContinueWithErrors());
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
                    this.handleFailure(ex, true);
                }
            }
        } else {
            try {
                int result = this.targetClient.insertBatch(this.targetDatabase, list, false);
                this.processedIncr(result);
            } catch (Exception ex) {
                this.handleFailure(ex, false);
            }
        }
    }

    @Override
    public boolean enableParallel() {
        return false;
    }

    public void setSourceClient(ShellH2Client sourceClient) {
        this.sourceClient = sourceClient;
    }

    public void setTargetClient(ShellH2Client targetClient) {
        this.targetClient = targetClient;
    }

    public void setTables(List<DBDataTransportObject> tables) {
        this.tables = tables;
    }

    public void setViews(List<DBDataTransportObject> views) {
        this.views = views;
    }
}
