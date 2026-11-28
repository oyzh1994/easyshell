package cn.oyzh.easyshell.h2.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.index.H2Index;
import cn.oyzh.easyshell.h2.table.H2CreateTableParam;
import cn.oyzh.easyshell.h2.trigger.H2Trigger;
import cn.oyzh.easyshell.util.h2.ShellH2ColumnUtil;
import cn.oyzh.fx.db.DBSqlGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * H2创建表SQL生成器
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class H2TableCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建表参数
     */
    private void _generate(H2CreateTableParam param) {
        this.sqlList = new ArrayList<>();
        this.sqlBuilder = new StringBuilder(createTable(
                param.getTable().getDbName(), param.tableName(), param.getColumns(),
                param.getTable().getConstraints()));
        if (param.hasIndex()) {
            for (H2Index index : param.getIndexes()) {
                if (!index.isDeleted() && StringUtil.isNotBlank(index.getDefinition())) {
                    this.sqlList.add(index.normalizedDefinition(null, param.tableName()));
                }
            }
        }
        if (param.hasTrigger()) {
            for (H2Trigger trigger : param.getTriggers()) {
                if (!trigger.isDeleted() && StringUtil.isNotBlank(trigger.getDefinition())) {
                    this.sqlList.add(trigger.normalizedDefinition(null, param.tableName()));
                }
            }
        }
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建表参数
     * @return SQL列表
     */
    public List<String> generate(H2CreateTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建表参数
     * @return SQL语句
     */
    public String generateSingle(H2CreateTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建表参数
     * @return SQL列表
     */
    public static List<String> generateSql(H2CreateTableParam param) {
        return new H2TableCreateSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建表参数
     * @return SQL语句
     */
    public static String generateSqlSingle(H2CreateTableParam param) {
        return new H2TableCreateSqlGenerator().generateSingle(param);
    }

    /**
     * 创建表SQL
     *
     * @param name        表名称
     * @param columns     字段集合
     * @param constraints 表约束
     * @return SQL语句
     */
    public static String createTable(String name, Collection<H2Column> columns,
                                     String constraints) {
        return createTable(null, name, columns, constraints);
    }

    /**
     * 创建指定模式中的表。
     *
     * @param schema      模式名称
     * @param name        表名称
     * @param columns     字段集合
     * @param constraints 表约束
     * @return SQL 语句
     */
    public static String createTable(String schema, String name, Collection<H2Column> columns,
                                     String constraints) {
        H2GeneratorUtil.requireName(name);
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("H2 table requires at least one column");
        }
        List<String> definitions = new ArrayList<>();
        Map<String, String> renamedColumns = renamedColumns(columns);
        int identityCount = 0;
        for (H2Column column : columns) {
            if (column != null) {
                column.normalizeGeneratedAttributes();
            }
            validateColumn(column);
            if (column.isAutoIncrement()) {
                identityCount++;
            }
            definitions.add(columnDefinition(column, renamedColumns));
        }
        if (identityCount > 1) {
            throw new IllegalArgumentException("H2 supports only one IDENTITY column");
        }
        if (StringUtil.isNotBlank(constraints)) {
            definitions.add(H2GeneratorUtil.replaceColumnReferences(
                    constraints.trim(), renamedColumns));
        }
        StringBuilder sql = new StringBuilder("CREATE TABLE ")
                .append(H2GeneratorUtil.qualified(schema, name)).append(" (\n  ");
        sql.append(String.join(",\n  ", definitions)).append("\n)");
        return sql.append(";").toString();
    }

    /**
     * 生成表与字段注释 SQL。
     *
     * @param schema  模式名称
     * @param tableName 表名称
     * @param comment 表注释
     * @param columns 字段列表
     * @return 注释 SQL 列表
     */
    public static List<String> commentSql(String schema, String tableName,
                                          String comment, Collection<H2Column> columns) {
        List<String> sqlList = new ArrayList<>();
        String tableComment = commentOnTableSql(schema, tableName, comment);
        if (tableComment != null) {
            sqlList.add(tableComment);
        }
        if (columns != null) {
            for (H2Column column : columns) {
                if (column == null || column.isDeleted()) {
                    continue;
                }
                if (StringUtil.isBlank(column.getComment())) {
                    continue;
                }
                String columnComment = commentOnColumnSql(
                        schema, tableName, column.getName(), column.getComment());
                if (columnComment != null) {
                    sqlList.add(columnComment);
                }
            }
        }
        return sqlList;
    }

    public static String commentOnTableSql(String schema, String tableName, String comment) {
        if (StringUtil.isBlank(comment)) {
            return null;
        }
        return "COMMENT ON TABLE " + H2GeneratorUtil.qualified(schema, tableName)
                + " IS " + H2GeneratorUtil.literal(comment.trim());
    }

    public static String commentOnColumnSql(String schema, String tableName,
                                            String columnName, String comment) {
        if (StringUtil.isBlank(columnName)) {
            return null;
        }
        return "COMMENT ON COLUMN " + H2GeneratorUtil.qualified(schema, tableName)
                + "." + H2GeneratorUtil.quote(columnName)
                + " IS " + H2GeneratorUtil.literal(StringUtil.blankToDefault(comment, null));
    }

    /**
     * 获取字段定义
     *
     * @param column 字段对象
     * @return 字段定义
     */
    public static String columnDefinition(H2Column column) {
        return columnDefinition(column, new LinkedHashMap<>());
    }

    /**
     * 获取字段定义
     *
     * @param column            字段对象
     * @param renamedColumns    字段重命名映射
     * @return 字段定义
     */
    public static String columnDefinition(H2Column column, Map<String, String> renamedColumns) {
        StringBuilder sql = new StringBuilder(H2GeneratorUtil.quote(column.getName()));
        if (StringUtil.isNotBlank(column.getType())) {
            sql.append(' ').append(dataType(column));
        }
        if (column.isAutoIncrement()) {
            sql.append(" GENERATED BY DEFAULT AS IDENTITY");
        }
        if (column.isPrimaryKey()) {
            sql.append(" PRIMARY KEY");
        }
        if (column.isNotNull() && !column.isPrimaryKey()) {
            sql.append(" NOT NULL");
        }
        if (column.isUnique()) {
            sql.append(" UNIQUE");
        }
        if (!column.isGenerated() && StringUtil.isNotBlank(column.getDefaultValue())) {
            sql.append(" DEFAULT ").append(column.getDefaultValue().trim());
        }
        if (StringUtil.isNotBlank(column.getCheck())) {
            sql.append(" CHECK (").append(H2GeneratorUtil.replaceColumnReferences(
                    column.getCheck().trim(), renamedColumns)).append(')');
        }
        if (column.isGenerated()) {
            sql.append(" GENERATED ALWAYS AS (").append(H2GeneratorUtil.replaceColumnReferences(
                    StringUtil.blankToDefault(column.getGeneratedExpression(), "").trim(), renamedColumns))
                    .append(')');
        }
        return sql.toString();
    }

    /**
     * 获取包含精度和小数位的 H2 数据类型。
     *
     * @param column 字段
     * @return 数据类型
     */
    public static String dataType(H2Column column) {
        String type = column.getType().trim();
        if (type.contains("(") || column.getSize() == null || !column.supportSize()) {
            return type;
        }
        StringBuilder value = new StringBuilder(type).append('(').append(column.getSize());
        boolean supportsScale = column.supportDigits()
                && !"DECFLOAT".equals(ShellH2ColumnUtil.normalizeType(type));
        if (supportsScale && column.getDigits() != null) {
            value.append(',').append(column.getDigits());
        }
        return value.append(')').toString();
    }

    private static Map<String, String> renamedColumns(Collection<H2Column> columns) {
        Map<String, String> result = new LinkedHashMap<>();
        if (columns == null) {
            return result;
        }
        for (H2Column column : columns) {
            if (column == null) {
                continue;
            }
            String originalName = StringUtil.blankToDefault(column.originalName(), column.getName());
            if (StringUtil.isNotBlank(originalName) && !StringUtil.equals(originalName, column.getName())) {
                result.put(originalName, column.getName());
            }
        }
        return result;
    }

    private static boolean containsTablePrimaryKey(String constraints) {
        return constraints != null && Pattern.compile("\\bPRIMARY\\s+KEY\\b", Pattern.CASE_INSENSITIVE)
                .matcher(constraints).find();
    }

    private static void validateColumn(H2Column column) {
        if (column == null) {
            throw new IllegalArgumentException("H2 column is null");
        }
        H2GeneratorUtil.requireName(column.getName());
        if (column.isGenerated() && StringUtil.isNotBlank(column.getDefaultValue())) {
            throw new IllegalArgumentException("Generated column cannot have a default value");
        }
        if (column.isGenerated() && (column.isPrimaryKey() || column.isUnique() || column.isAutoIncrement())) {
            throw new IllegalArgumentException("Generated column cannot be a key or identity column");
        }
        if (column.isGenerated()) {
            if (StringUtil.isBlank(column.getGeneratedExpression())) {
                throw new IllegalArgumentException("Generated column expression is empty");
            }
        }
    }
}
