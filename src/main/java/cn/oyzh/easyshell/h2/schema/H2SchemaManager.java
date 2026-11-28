package cn.oyzh.easyshell.h2.schema;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2ColumnDefinitionParser;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.generator.table.H2TableAlertSqlGenerator;
import cn.oyzh.easyshell.h2.generator.table.H2TableCreateSqlGenerator;
import cn.oyzh.easyshell.h2.generator.view.H2ViewCreateSqlGenerator;
import cn.oyzh.easyshell.h2.table.H2AlertTableParam;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.view.H2View;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * H2 表、视图设计执行器。
 *
 * <p>H2 支持标准 ALTER TABLE，表更新使用 ALTER 序列；视图更新使用 DROP + CREATE。</p>
 *
 * @since 2026-11-28
 */
public class H2SchemaManager {

    private final ShellH2Client client;

    public H2SchemaManager(ShellH2Client client) {
        this.client = client;
    }

    public void createTable(H2Table table) {
        this.client.executeSqlSimple(H2TableCreateSqlGenerator.createTable(
                table.getDbName(), table.getName(), table.getColumns(), table.getConstraints()));
        for (String commentSql : H2TableCreateSqlGenerator.commentSql(
                table.getDbName(), table.getName(), table.getComment(), table.getColumns())) {
            this.client.executeSqlSimple(commentSql);
        }
    }

    /**
     * 重命名表。H2 的 ALTER TABLE RENAME TO 自动维护索引等依赖。
     *
     * @param originalName 原表名称
     * @param targetName   新表名称
     */
    public void renameTable(String originalName, String targetName) {
        String renameSql = "ALTER TABLE " + H2GeneratorUtil.quote(originalName)
                + " RENAME TO " + H2GeneratorUtil.quote(targetName);
        this.client.executeSqlSimple(renameSql);
    }

    /**
     * 更新表结构。使用 H2 标准 ALTER 序列。
     *
     * @param originalName 原表名称
     * @param table        新表定义
     */
    public void updateTable(String originalName, H2Table table) {
        if (StringUtil.isBlank(originalName)) {
            this.createTable(table);
            return;
        }
        String schema = StringUtil.blankToDefault(table.getDbName(), this.client.schema());
        H2Table source = this.client.selectTable(schema, originalName);
        if (source == null) {
            throw new IllegalArgumentException("H2 table does not exist: " + originalName);
        }
        Map<String, H2View> savedViews = this.loadViews();
        Set<String> savedViewNames = Set.copyOf(savedViews.keySet());
        for (H2View view : savedViews.values()) {
            if (referencesTable(view.getDefinition(), originalName)) {
                this.client.executeSqlSimple("DROP VIEW IF EXISTS "
                        + H2GeneratorUtil.quote(view.getName()) + " CASCADE");
            }
        }
        H2AlertTableParam param = new H2AlertTableParam();
        param.setTable(source);
        param.setComment(table.getComment());
        param.setColumns(table.getColumns());
        param.setChecks(new cn.oyzh.easyshell.h2.check.H2Checks(
                table.getChecks() == null
                        ? this.client.selectChecks(schema, originalName)
                        : table.getChecks()));
        param.setForeignKeys(new cn.oyzh.easyshell.h2.foreignKey.H2ForeignKeys(
                table.getForeignKeys() == null
                        ? this.client.selectForeignKeys(schema, originalName)
                        : table.getForeignKeys()));
        param.setIndexes(new cn.oyzh.easyshell.h2.index.H2Indexes(
                this.client.selectIndexes(schema, originalName)));
        param.setTriggers(new cn.oyzh.easyshell.h2.trigger.H2Triggers(
                this.client.selectTriggers(schema, originalName)));
        param.setTableName(table.getName());
        String sql = H2TableAlertSqlGenerator.generateSqlSingle(param);
        try {
            if (StringUtil.isNotBlank(sql)) {
                this.client.executeSqlSimple(sql);
            }
        } catch (Exception ex) {
            this.restoreViews(savedViews, savedViewNames, originalName, table.getName(), table.getColumns());
            throw ex;
        }
        this.restoreViews(savedViews, savedViewNames, originalName, table.getName(), table.getColumns());
    }

    private Map<String, H2View> loadViews() {
        Map<String, H2View> result = new LinkedHashMap<>();
        for (H2SchemaObject object : this.client.selectSchemaObjects("view")) {
            H2View view = this.client.selectView(object.getName());
            if (view != null && StringUtil.isNotBlank(view.getDefinition())) {
                result.put(view.getName(), view);
            }
        }
        return result;
    }

    private void restoreViews(Map<String, H2View> savedViews, Set<String> savedViewNames,
                              String oldTableName, String newTableName,
                              List<H2Column> targetColumns) {
        Set<String> existing = this.client.selectSchemaObjects("view").stream()
                .map(H2SchemaObject::getName)
                .collect(java.util.stream.Collectors.toSet());
        Map<String, String> columnRenames = new LinkedHashMap<>();
        if (targetColumns != null) {
            for (H2Column column : targetColumns) {
                String original = StringUtil.blankToDefault(column.originalName(), column.getName());
                if (StringUtil.isNotBlank(original)
                        && !StringUtil.equals(original, column.getName())) {
                    columnRenames.put(original, column.getName());
                }
            }
        }
        List<H2View> pending = savedViewNames.stream()
                .filter(name -> !existing.contains(name))
                .map(savedViews::get)
                .filter(view -> view != null)
                .toList();
        for (H2View view : pending) {
            String definition = H2GeneratorUtil.replaceColumnReferences(
                    view.getDefinition(), columnRenames);
            view.setDefinition(replaceTableReference(definition, oldTableName, newTableName));
        }
        while (!pending.isEmpty()) {
            List<H2View> failed = new ArrayList<>();
            Exception firstError = null;
            for (H2View view : pending) {
                try {
                    this.client.executeSqlSimple(
                            H2ViewCreateSqlGenerator.createView(view.getName(), view.getDefinition()));
                } catch (Exception ex) {
                    failed.add(view);
                    if (firstError == null) {
                        firstError = ex;
                    }
                }
            }
            if (failed.size() == pending.size()) {
                if (firstError != null) {
                    throw new ShellException(firstError);
                }
                return;
            }
            pending = failed;
        }
    }

    /**
     * 保存视图。H2 视图更新使用 DROP + CREATE。
     *
     * @param originalName 原视图名
     * @param view         视图定义
     */
    public void saveView(String originalName, H2View view) {
        this.saveView(StringUtil.blankToDefault(view.getDbName(), this.client.schema()),
                originalName, view);
    }

    /**
     * 在指定模式中保存视图。
     *
     * @param schema       模式名称
     * @param originalName 原视图名
     * @param view         视图定义
     */
    public void saveView(String schema, String originalName, H2View view) {
        if (StringUtil.isNotBlank(originalName)) {
            this.client.executeSqlSimple("DROP VIEW IF EXISTS "
                    + H2GeneratorUtil.qualified(schema, originalName));
        }
        this.client.executeSqlSimple(H2ViewCreateSqlGenerator.createView(
                schema, view.getName(), view.getDefinition()));
    }

    /**
     * 重命名视图。H2 无 ALTER VIEW RENAME，使用 DROP + CREATE 实现。
     *
     * @param originalName 原视图名
     * @param targetName   新视图名
     */
    public void renameView(String originalName, String targetName) {
        H2View source = this.client.selectView(originalName);
        if (source == null) {
            throw new IllegalArgumentException("H2 view does not exist: " + originalName);
        }
        if (!StringUtil.equalsIgnoreCase(originalName, targetName) && this.client.selectView(targetName) != null) {
            throw new IllegalArgumentException("H2 view already exists: " + targetName);
        }
        if (StringUtil.equals(originalName, targetName)) {
            return;
        }
        List<H2SchemaObject> dependentViews = this.client.selectSchemaObjects("view").stream()
                .filter(item -> !StringUtil.equalsIgnoreCase(item.getName(), originalName))
                .filter(item -> referencesTable(item.getSql(), originalName))
                .toList();
        StringBuilder script = new StringBuilder();
        for (H2SchemaObject dependency : dependentViews) {
            script.append("DROP VIEW ").append(H2GeneratorUtil.quote(dependency.getName())).append(";\n");
        }
        script.append("DROP VIEW ").append(H2GeneratorUtil.quote(originalName)).append(";\n");
        script.append(H2ViewCreateSqlGenerator.createView(targetName, source.getDefinition())).append('\n');
        // 依赖视图的 SQL 定义不可用（INFORMATION_SCHEMA 不提供），此处仅保留名称
        this.client.executeSqlSimple(script.toString());
    }

    /**
     * 克隆视图定义。
     *
     * @param originalName 原视图名
     * @param targetName   新视图名
     */
    public void cloneView(String originalName, String targetName) {
        H2View source = this.client.selectView(originalName);
        if (source == null) {
            throw new IllegalArgumentException("H2 view does not exist: " + originalName);
        }
        if (this.client.selectView(targetName) != null) {
            throw new IllegalArgumentException("H2 view already exists: " + targetName);
        }
        if (StringUtil.isBlank(source.getDefinition())) {
            throw new IllegalArgumentException("H2 view definition is unavailable: " + originalName);
        }
        this.client.executeSqlSimple(H2ViewCreateSqlGenerator.createView(targetName, source.getDefinition()));
    }

    /**
     * 克隆表结构；可选复制数据。索引定义按源表重建，名称追加克隆后缀。
     *
     * @param originalName  源表名
     * @param targetName    目标表名
     * @param includeRecords 是否复制数据
     */
    public void cloneTable(String originalName, String targetName, boolean includeRecords) {
        H2Table source = this.client.selectTable(originalName);
        if (source == null) {
            throw new IllegalArgumentException("H2 table does not exist: " + originalName);
        }
        if (StringUtil.isBlank(targetName)) {
            throw new IllegalArgumentException("H2 table name is empty");
        }
        if (this.client.selectTable(targetName) != null || this.client.selectView(targetName) != null) {
            throw new IllegalArgumentException("H2 object already exists: " + targetName);
        }
        source.setColumns(this.client.selectColumns(originalName));
        String createSql = this.client.showCreate(originalName);
        source.setConstraints(H2ColumnDefinitionParser.baseTableConstraints(createSql));
        source.setForeignKeys(H2ColumnDefinitionParser.foreignKeys(createSql));
        source.setChecks(H2ColumnDefinitionParser.checks(createSql));
        List<String> constraints = new ArrayList<>();
        if (StringUtil.isNotBlank(source.getConstraints())) {
            constraints.add(H2ColumnDefinitionParser.withoutConstraintNames(source.getConstraints()));
        }
        if (source.getForeignKeys() != null) {
            source.getForeignKeys().stream()
                    .filter(item -> item != null && !item.isInvalid())
                    .map(item -> H2ColumnDefinitionParser.withoutConstraintName(item.toConstraintSql()))
                    .forEach(constraints::add);
        }
        if (source.getChecks() != null) {
            source.getChecks().stream()
                    .filter(item -> item != null && !item.isInvalid())
                    .map(item -> H2ColumnDefinitionParser.withoutConstraintName(item.toConstraintSql()))
                    .forEach(constraints::add);
        }
        String definition = H2TableCreateSqlGenerator.createTable(targetName,
                source.getColumns(), String.join(",\n", constraints));
        StringBuilder script = new StringBuilder(definition).append('\n');
        if (includeRecords) {
            List<String> columns = this.client.selectColumns(originalName).stream()
                    .filter(column -> !column.isGenerated())
                    .map(column -> H2GeneratorUtil.quote(column.getName()))
                    .toList();
            if (!columns.isEmpty()) {
                String columnSql = String.join(", ", columns);
                script.append("INSERT INTO ").append(H2GeneratorUtil.quote(targetName))
                        .append(" (").append(columnSql).append(") SELECT ")
                        .append(columnSql).append(" FROM ")
                        .append(H2GeneratorUtil.quote(originalName)).append(";\n");
            }
        }
        List<H2SchemaObject> indexes = this.client.selectSchemaObjects("index").stream()
                .filter(item -> StringUtil.equalsIgnoreCase(item.getTableName(), originalName))
                .filter(item -> StringUtil.isNotBlank(item.getSql()))
                .toList();
        for (H2SchemaObject index : indexes) {
            String cloneName = index.getName() + DBUtil.genCloneName();
            String indexSql = replaceCreateObjectName(index.getSql(), cloneName);
            indexSql = replaceTableReference(indexSql, originalName, targetName);
            script.append(indexSql).append(";\n");
        }
        this.client.executeSqlSimple(script.toString());
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
                        + "(\\\"(?:[^\\\"]|\\\"\\\")*\\\"|`(?:[^`]|``)*`|[^\\s(]+)");
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.find()) {
            return sql;
        }
        return matcher.group(1) + H2GeneratorUtil.quote(targetName) + sql.substring(matcher.end());
    }

    private boolean referencesTable(String sql, String tableName) {
        if (StringUtil.isBlank(sql)) {
            return false;
        }
        Pattern pattern = Pattern.compile(
                "(?is)(?:FROM|JOIN|UPDATE|INTO)\\s+"
                        + "(?:(?:\"(?:[^\"]|\"\")*\"|`[^`]*`|\\[[^\\]]*\\]|[\\w$]+)\\s*\\.\\s*)*"
                        + "(?:\"\\Q" + tableName + "\\E\"|`\\Q" + tableName + "\\E`|\\Q"
                        + tableName + "\\E)(?![\\w$])");
        return pattern.matcher(sql).find();
    }

    public static String replaceTableReference(String sql, String oldName, String newName) {
        if (StringUtil.equals(oldName, newName)) {
            return sql;
        }
        String replacement = java.util.regex.Matcher.quoteReplacement(newName);
        // 引号化表名：ON "old" / FROM "old" 等
        String result = sql.replaceAll("(?i)(\\b(?:ON|FROM|JOIN|UPDATE|INTO|TABLE)\\s+)\"\\Q"
                        + oldName + "\\E\"",
                "$1\"" + replacement + "\"");
        // 未引号化表名
        return result.replaceAll("(?i)(\\b(?:ON|FROM|JOIN|UPDATE|INTO|TABLE)\\s+)(?![\"`])\\Q"
                        + oldName + "\\E(?![\\w])",
                "$1" + replacement);
    }
}
