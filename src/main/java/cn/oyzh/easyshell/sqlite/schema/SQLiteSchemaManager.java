package cn.oyzh.easyshell.sqlite.schema;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.generator.function.SQLiteFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.table.SQLiteTableCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.view.SQLiteViewCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQLite 表、视图和函数设计执行器。
 *
 * @since 2026-11-02
 */
public class SQLiteSchemaManager {

    private final ShellSqliteClient client;

    public SQLiteSchemaManager(ShellSqliteClient client) {
        this.client = client;
    }

    public void createTable(SQLiteTable table) {
        this.client.executeSqlSimple(SQLiteTableCreateSqlGenerator.createTable(
                table.getName(), table.getColumns(), table.getConstraints(),
                table.isWithoutRowid(), table.isStrict()));
    }

    /**
     * 重建表并保留数据、索引、触发器和依赖视图。
     *
     * @param originalName 原表名称
     * @param table        新表定义
     */
    public void updateTable(String originalName, SQLiteTable table) {
        if (StringUtil.isBlank(originalName)) {
            this.createTable(table);
            return;
        }
        SQLiteTable source = this.client.selectTable(originalName);
        if (source == null) {
            throw new IllegalArgumentException("SQLite table does not exist: " + originalName);
        }
        List<SQLiteColumn> sourceColumns = this.client.selectColumns(originalName);
        List<SQLiteSchemaObject> objects = this.client.selectSchemaObjects("index", "trigger", "view");
        List<SQLiteSchemaObject> dependencies = objects.stream()
                .filter(item -> "view".equals(item.getType())
                        ? referencesTable(item.getSql(), originalName)
                        : StringUtil.equalsIgnoreCase(item.getTableName(), originalName))
                .toList();

        String temporaryName = "__easyshell_new_" + UUID.randomUUID().toString().replace("-", "");
        String createTemporary = SQLiteTableCreateSqlGenerator.createTable(
                temporaryName, table.getColumns(), table.getConstraints(),
                table.isWithoutRowid(), table.isStrict());
        List<String> copyTargetColumns = new ArrayList<>();
        List<String> copySourceColumns = new ArrayList<>();
        for (SQLiteColumn column : table.getColumns()) {
            if (column.isGenerated()) {
                continue;
            }
            String sourceName = StringUtil.blankToDefault(column.originalName(), column.getName());
            SQLiteColumn sourceColumn = sourceColumns.stream()
                    .filter(item -> StringUtil.equalsIgnoreCase(item.getName(), sourceName))
                    .findFirst()
                    .orElse(null);
            if (sourceColumn == null || sourceColumn.isGenerated()) {
                continue;
            }
            copyTargetColumns.add(SQLiteGeneratorUtil.quote(column.getName()));
            copySourceColumns.add(SQLiteGeneratorUtil.quote(sourceName));
        }
        StringBuilder script = new StringBuilder("BEGIN;\n").append(createTemporary).append('\n');
        if (!copyTargetColumns.isEmpty()) {
            script.append("INSERT INTO ").append(SQLiteGeneratorUtil.quote(temporaryName))
                    .append(" (").append(String.join(", ", copyTargetColumns)).append(")\nSELECT ")
                    .append(String.join(", ", copySourceColumns)).append(" FROM ")
                    .append(SQLiteGeneratorUtil.quote(originalName)).append(";\n");
        }
        for (SQLiteSchemaObject dependency : dependencies) {
            if ("view".equals(dependency.getType())) {
                script.append("DROP VIEW IF EXISTS ").append(SQLiteGeneratorUtil.quote(dependency.getName())).append(";\n");
            }
        }
        script.append("DROP TABLE ").append(SQLiteGeneratorUtil.quote(originalName)).append(";\n");
        script.append("ALTER TABLE ").append(SQLiteGeneratorUtil.quote(temporaryName))
                .append(" RENAME TO ").append(SQLiteGeneratorUtil.quote(table.getName())).append(";\n");
        for (SQLiteSchemaObject dependency : dependencies) {
            if (!"view".equals(dependency.getType()) && StringUtil.isNotBlank(dependency.getSql())) {
                script.append(replaceTableReference(dependency.getSql(), originalName, table.getName())).append(";\n");
            }
        }
        for (SQLiteSchemaObject dependency : dependencies) {
            if ("view".equals(dependency.getType()) && StringUtil.isNotBlank(dependency.getSql())) {
                script.append(replaceTableReference(dependency.getSql(), originalName, table.getName())).append(";\n");
            }
        }
        script.append("COMMIT;");
        this.executeScript(script.toString());
    }

    public void saveView(String originalName, SQLiteView view) {
        List<SQLiteSchemaObject> dependencies = StringUtil.isBlank(originalName) ? List.of()
                : this.client.selectSchemaObjects("view").stream()
                .filter(item -> !StringUtil.equalsIgnoreCase(item.getName(), originalName)
                        && referencesTable(item.getSql(), originalName))
                .toList();
        List<SQLiteSchemaObject> triggers = StringUtil.isBlank(originalName) ? List.of()
                : this.viewTriggers(originalName, dependencies);
        StringBuilder script = new StringBuilder("BEGIN;\n");
        for (SQLiteSchemaObject trigger : triggers) {
            script.append("DROP TRIGGER IF EXISTS ")
                    .append(SQLiteGeneratorUtil.quote(trigger.getName())).append(";\n");
        }
        for (SQLiteSchemaObject dependency : dependencies) {
            script.append("DROP VIEW IF EXISTS ").append(SQLiteGeneratorUtil.quote(dependency.getName())).append(";\n");
        }
        if (StringUtil.isNotBlank(originalName)) {
            script.append("DROP VIEW IF EXISTS ").append(SQLiteGeneratorUtil.quote(originalName)).append(";\n");
        }
        script.append(SQLiteViewCreateSqlGenerator.createView(view.getName(), view.getDefinition()));
        for (SQLiteSchemaObject dependency : dependencies) {
            script.append('\n').append(replaceTableReference(
                    dependency.getSql(), originalName, view.getName())).append(';');
        }
        for (SQLiteSchemaObject trigger : triggers) {
            script.append('\n').append(replaceTableReference(
                    trigger.getSql(), originalName, view.getName())).append(';');
        }
        script.append("\nCOMMIT;");
        this.executeScript(script.toString());
    }

    /**
     * 重命名视图，同时保留依赖视图和视图触发器。
     *
     * @param originalName 原视图名
     * @param targetName   新视图名
     */
    public void renameView(String originalName, String targetName) {
        SQLiteView source = this.client.selectView(originalName);
        if (source == null) {
            throw new IllegalArgumentException("SQLite view does not exist: " + originalName);
        }
        if (!StringUtil.equalsIgnoreCase(originalName, targetName) && this.client.selectView(targetName) != null) {
            throw new IllegalArgumentException("SQLite view already exists: " + targetName);
        }
        if (StringUtil.equals(originalName, targetName)) {
            return;
        }
        List<SQLiteSchemaObject> dependentViews = this.client.selectSchemaObjects("view").stream()
                .filter(item -> !StringUtil.equalsIgnoreCase(item.getName(), originalName))
                .filter(item -> referencesTable(item.getSql(), originalName))
                .toList();
        List<SQLiteSchemaObject> triggers = this.viewTriggers(originalName, dependentViews);
        StringBuilder script = new StringBuilder("BEGIN;\n");
        for (SQLiteSchemaObject dependency : dependentViews) {
            script.append("DROP VIEW IF EXISTS ")
                    .append(SQLiteGeneratorUtil.quote(dependency.getName())).append(";\n");
        }
        for (SQLiteSchemaObject trigger : triggers) {
            script.append("DROP TRIGGER IF EXISTS ")
                    .append(SQLiteGeneratorUtil.quote(trigger.getName())).append(";\n");
        }
        script.append("DROP VIEW IF EXISTS ").append(SQLiteGeneratorUtil.quote(originalName)).append(";\n");
        script.append(SQLiteViewCreateSqlGenerator.createView(targetName, source.getDefinition())).append('\n');
        for (SQLiteSchemaObject dependency : dependentViews) {
            script.append(replaceTableReference(dependency.getSql(), originalName, targetName)).append(";\n");
        }
        for (SQLiteSchemaObject trigger : triggers) {
            script.append(replaceTableReference(trigger.getSql(), originalName, targetName)).append(";\n");
        }
        script.append("COMMIT;");
        this.executeScript(script.toString());
    }

    private List<SQLiteSchemaObject> viewTriggers(String originalName,
                                                  List<SQLiteSchemaObject> dependentViews) {
        Set<String> viewNames = new LinkedHashSet<>();
        viewNames.add(originalName);
        for (SQLiteSchemaObject dependency : dependentViews) {
            viewNames.add(dependency.getName());
        }
        return this.client.selectSchemaObjects("trigger").stream()
                .filter(item -> viewNames.stream()
                        .anyMatch(name -> StringUtil.equalsIgnoreCase(item.getTableName(), name)))
                .filter(item -> StringUtil.isNotBlank(item.getSql()))
                .toList();
    }

    /**
     * 克隆视图定义。
     *
     * @param originalName 原视图名
     * @param targetName   新视图名
     */
    public void cloneView(String originalName, String targetName) {
        SQLiteView source = this.client.selectView(originalName);
        if (source == null) {
            throw new IllegalArgumentException("SQLite view does not exist: " + originalName);
        }
        if (this.client.selectView(targetName) != null) {
            throw new IllegalArgumentException("SQLite view already exists: " + targetName);
        }
        this.client.executeSqlSimple(SQLiteViewCreateSqlGenerator.createView(targetName, source.getDefinition()));
    }

    /**
     * 克隆表结构；可选复制数据，并保留显式索引。
     *
     * @param originalName  源表名
     * @param targetName    目标表名
     * @param includeRecords 是否复制数据
     */
    public void cloneTable(String originalName, String targetName, boolean includeRecords) {
        SQLiteTable source = this.client.selectTable(originalName);
        if (source == null) {
            throw new IllegalArgumentException("SQLite table does not exist: " + originalName);
        }
        if (StringUtil.isBlank(targetName)) {
            throw new IllegalArgumentException("SQLite table name is empty");
        }
        if (this.client.selectTable(targetName) != null || this.client.selectView(targetName) != null) {
            throw new IllegalArgumentException("SQLite object already exists: " + targetName);
        }
        String definition = replaceCreateObjectName(source.getDefinition(), targetName);
        StringBuilder script = new StringBuilder("BEGIN;\n").append(definition).append(";\n");
        if (includeRecords) {
            List<String> columns = this.client.selectColumns(originalName).stream()
                    .filter(column -> !column.isGenerated())
                    .map(column -> SQLiteGeneratorUtil.quote(column.getName()))
                    .toList();
            if (!columns.isEmpty()) {
                String columnSql = String.join(", ", columns);
                script.append("INSERT INTO ").append(SQLiteGeneratorUtil.quote(targetName))
                        .append(" (").append(columnSql).append(") SELECT ")
                        .append(columnSql).append(" FROM ")
                        .append(SQLiteGeneratorUtil.quote(originalName)).append(";\n");
            }
        }
        List<SQLiteSchemaObject> indexes = this.client.selectSchemaObjects("index").stream()
                .filter(item -> StringUtil.equalsIgnoreCase(item.getTableName(), originalName))
                .filter(item -> StringUtil.isNotBlank(item.getSql()))
                .toList();
        for (SQLiteSchemaObject index : indexes) {
            String cloneName = index.getName() + DBUtil.genCloneName();
            String indexSql = replaceCreateObjectName(index.getSql(), cloneName);
            indexSql = replaceTableReference(indexSql, originalName, targetName);
            script.append(indexSql).append(";\n");
        }
        script.append("COMMIT;");
        this.executeScript(script.toString());
    }

    /**
     * 替换 CREATE TABLE/INDEX/VIEW/TRIGGER 后的对象名称，保留其余 DDL。
     *
     * @param sql       原 DDL
     * @param targetName 目标名称
     * @return 新 DDL
     */
    public static String replaceCreateObjectName(String sql, String targetName) {
        if (StringUtil.isBlank(sql)) {
            return sql;
        }
        Pattern pattern = Pattern.compile(
                "(?is)^(\\s*CREATE\\s+(?:TEMP(?:ORARY)?\\s+)?(?:UNIQUE\\s+)?"
                        + "(?:TABLE|INDEX|VIEW|TRIGGER)\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?)"
                        + "(\\\"(?:[^\\\"]|\\\"\\\")*\\\"|`(?:[^`]|``)*`|\\[[^\\]]*\\]|[^\\s(]+)");
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.find()) {
            return sql;
        }
        return matcher.group(1) + SQLiteGeneratorUtil.quote(targetName) + sql.substring(matcher.end());
    }

    public void saveFunction(String originalName, SQLiteFunction function) {
        SQLiteFunction.validate(function);
        String kind = StringUtil.blankToDefault(function.getKind(), SQLiteFunction.KIND_SQL_EXPRESSION);
        if (SQLiteFunction.KIND_TABLE_VIEW.equals(kind)) {
            this.saveView(originalName, toView(function));
            return;
        }
        if (SQLiteFunction.KIND_GENERATED_COLUMN.equals(kind)) {
            this.client.executeSqlSimple(SQLiteFunctionCreateSqlGenerator.createFunction(function));
            return;
        }
        String sql = SQLiteFunctionCreateSqlGenerator.createFunction(function);
        this.client.executeSqlSimple("EXPLAIN " + sql.replaceFirst("(?i)^SELECT", "SELECT"));
    }

    private SQLiteView toView(SQLiteFunction function) {
        SQLiteView view = new SQLiteView();
        view.setName(function.getName());
        view.setDefinition(function.getDefinition());
        return view;
    }

    private void executeScript(String script) {
        boolean foreignKeys = this.client.database().isForeignKeys();
        this.client.executeSqlSimple("PRAGMA foreign_keys=OFF");
        try {
            this.client.executeSqlSimple(script);
        } finally {
            this.client.executeSqlSimple("PRAGMA foreign_keys=" + (foreignKeys ? "ON" : "OFF"));
        }
    }

    private boolean referencesTable(String sql, String tableName) {
        if (StringUtil.isBlank(sql)) {
            return false;
        }
        Pattern pattern = Pattern.compile(
                "(?i)(?:FROM|JOIN|UPDATE|INTO)\\s+(?:[\\[\"`]?)" + Pattern.quote(tableName) + "(?:[\\]\"`]?)\\b");
        return pattern.matcher(sql).find();
    }

    public static String replaceTableReference(String sql, String oldName, String newName) {
        if (StringUtil.equals(oldName, newName)) {
            return sql;
        }
        String escaped = Pattern.quote(oldName);
        return sql.replaceAll("(?i)(\\b(?:ON|FROM|JOIN|UPDATE|INTO|TABLE)\\s+(?:[\\[\"`]?))"
                        + escaped + "([\\]\"`]?)(?![\\w])",
                "$1" + java.util.regex.Matcher.quoteReplacement(newName) + "$2");
    }
}
