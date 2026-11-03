package cn.oyzh.easyshell.sqlite.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.index.SQLiteIndex;
import cn.oyzh.easyshell.sqlite.table.SQLiteCreateTableParam;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteTrigger;
import cn.oyzh.fx.db.DBSqlGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SQLite 创建表 SQL 生成器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteTableCreateSqlGenerator extends DBSqlGenerator {

    public List<String> generate(SQLiteCreateTableParam param) {
        this.sqlList = new ArrayList<>();
        this.sqlBuilder = new StringBuilder(createTable(param.tableName(), param.getColumns(),
                param.getTable().getConstraints(), param.getTable().isWithoutRowid(),
                param.getTable().isStrict()));
        if (param.hasIndex()) {
            for (SQLiteIndex index : param.getIndexes()) {
                if (!index.isDeleted() && StringUtil.isNotBlank(index.getDefinition())) {
                    this.sqlList.add(index.normalizedDefinition(null, param.tableName()));
                }
            }
        }
        if (param.hasTrigger()) {
            for (SQLiteTrigger trigger : param.getTriggers()) {
                if (!trigger.isDeleted() && StringUtil.isNotBlank(trigger.getDefinition())) {
                    this.sqlList.add(trigger.normalizedDefinition(null, param.tableName()));
                }
            }
        }
        return super.buildSql();
    }

    public String generateSingle(SQLiteCreateTableParam param) {
        this.generate(param);
        return super.buildSqlSingle();
    }

    public static List<String> generateSql(SQLiteCreateTableParam param) {
        return new SQLiteTableCreateSqlGenerator().generate(param);
    }

    public static String generateSqlSingle(SQLiteCreateTableParam param) {
        return new SQLiteTableCreateSqlGenerator().generateSingle(param);
    }

    public static String createTable(String name, Collection<SQLiteColumn> columns,
                                     String constraints, boolean withoutRowid, boolean strict) {
        SQLiteGeneratorUtil.requireName(name);
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("SQLite table requires at least one column");
        }
        List<String> definitions = new ArrayList<>();
        int primaryKeyCount = 0;
        int autoIncrementCount = 0;
        boolean tablePrimaryKey = containsTablePrimaryKey(constraints);
        for (SQLiteColumn column : columns) {
            validateColumn(column);
            if (column.isPrimaryKey() && !tablePrimaryKey) {
                primaryKeyCount++;
            }
            if (column.isAutoIncrement()) {
                autoIncrementCount++;
            }
            definitions.add(columnDefinition(column, !tablePrimaryKey));
        }
        if (tablePrimaryKey && autoIncrementCount > 0) {
            throw new IllegalArgumentException("AUTOINCREMENT cannot be combined with a table-level PRIMARY KEY");
        }
        if (autoIncrementCount > 1) {
            throw new IllegalArgumentException("SQLite supports only one AUTOINCREMENT column");
        }
        if (autoIncrementCount == 1 && primaryKeyCount != 1) {
            throw new IllegalArgumentException("SQLite AUTOINCREMENT requires a single INTEGER PRIMARY KEY");
        }
        if (primaryKeyCount > 1) {
            throw new IllegalArgumentException("SQLite supports only one column-level PRIMARY KEY; use a table constraint");
        }
        if (withoutRowid && autoIncrementCount > 0) {
            throw new IllegalArgumentException("SQLite AUTOINCREMENT is not supported by WITHOUT ROWID tables");
        }
        if (columns.stream().anyMatch(column -> column.isAutoIncrement()
                && (!column.isPrimaryKey() || !"INTEGER".equalsIgnoreCase(String.valueOf(column.getType()).trim())))) {
            throw new IllegalArgumentException("SQLite AUTOINCREMENT requires an INTEGER PRIMARY KEY");
        }
        if (StringUtil.isNotBlank(constraints)) {
            definitions.add(constraints.trim());
        }
        StringBuilder sql = new StringBuilder("CREATE TABLE ")
                .append(SQLiteGeneratorUtil.quote(name)).append(" (\n  ");
        sql.append(String.join(",\n  ", definitions)).append("\n)");
        if (withoutRowid) {
            sql.append(" WITHOUT ROWID");
        }
        if (strict) {
            sql.append(sql.charAt(sql.length() - 1) == 'D' ? ", STRICT" : " STRICT");
        }
        return sql.append(";").toString();
    }

    public static String columnDefinition(SQLiteColumn column) {
        return columnDefinition(column, true);
    }

    public static String columnDefinition(SQLiteColumn column, boolean includePrimaryKey) {
        StringBuilder sql = new StringBuilder(SQLiteGeneratorUtil.quote(column.getName()));
        if (StringUtil.isNotBlank(column.getType())) {
            String type = column.getType().trim();
            sql.append(' ').append(type);
            if (column.getSize() != null && column.supportSize() && !type.contains("(")) {
                sql.append('(').append(column.getSize()).append(')');
            }
        }
        if (includePrimaryKey && column.isPrimaryKey()) {
            sql.append(" PRIMARY KEY");
            if (column.isAutoIncrement()) {
                sql.append(" AUTOINCREMENT");
            }
        }
        if (column.isNotNull() && (!includePrimaryKey || !column.isPrimaryKey())) {
            sql.append(" NOT NULL");
        }
        if (column.isUnique()) {
            sql.append(" UNIQUE");
        }
        if (StringUtil.isNotBlank(column.getDefaultValue())) {
            sql.append(" DEFAULT ").append(column.getDefaultValue().trim());
        }
        if (StringUtil.isNotBlank(column.getCollate())) {
            sql.append(" COLLATE ").append(column.getCollate().trim());
        }
        if (StringUtil.isNotBlank(column.getCheck())) {
            sql.append(" CHECK (").append(column.getCheck().trim()).append(')');
        }
        if (column.isGenerated()) {
            String storage = StringUtil.blankToDefault(column.getGeneratedStorage(), "VIRTUAL");
            sql.append(" GENERATED ALWAYS AS (").append(column.getGeneratedExpression().trim())
                    .append(") ").append(storage.toUpperCase());
        }
        return sql.toString();
    }

    private static boolean containsTablePrimaryKey(String constraints) {
        return constraints != null && Pattern.compile("\\bPRIMARY\\s+KEY\\b", Pattern.CASE_INSENSITIVE)
                .matcher(constraints).find();
    }

    private static void validateColumn(SQLiteColumn column) {
        if (column == null) {
            throw new IllegalArgumentException("SQLite column is null");
        }
        SQLiteGeneratorUtil.requireName(column.getName());
        if (column.isGenerated() && StringUtil.isNotBlank(column.getDefaultValue())) {
            throw new IllegalArgumentException("Generated column cannot have a default value");
        }
        if (column.isGenerated() && (column.isPrimaryKey() || column.isUnique() || column.isAutoIncrement())) {
            throw new IllegalArgumentException("Generated column cannot be a key or unique column");
        }
        if (column.isGenerated()) {
            String storage = StringUtil.blankToDefault(column.getGeneratedStorage(), "VIRTUAL");
            if (!"VIRTUAL".equalsIgnoreCase(storage) && !"STORED".equalsIgnoreCase(storage)) {
                throw new IllegalArgumentException("Generated column storage must be VIRTUAL or STORED");
            }
        }
    }
}
