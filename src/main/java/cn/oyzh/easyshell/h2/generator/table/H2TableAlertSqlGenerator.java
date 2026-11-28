package cn.oyzh.easyshell.h2.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.check.H2Check;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.foreignKey.H2ForeignKey;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.index.H2Index;
import cn.oyzh.easyshell.h2.table.H2AlertTableParam;
import cn.oyzh.easyshell.h2.trigger.H2Trigger;
import cn.oyzh.fx.db.DBSqlGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * H2修改表SQL生成器
 *
 * <p>H2 支持标准 ALTER TABLE，字段与约束更新使用 ALTER 序列而非临时表重建。</p>
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class H2TableAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改表参数
     */
    private void _generate(H2AlertTableParam param) {
        String originalName = StringUtil.blankToDefault(
                param.getTable().originalName(), param.tableName());
        String targetTableName = param.tableName();
        String schema = param.getTable().getDbName();
        String tableName = originalName;
        String qualifiedTable = H2GeneratorUtil.qualified(schema, tableName);
        Map<String, String> renamedColumns = renamedColumns(param.getColumns());
        List<String> scripts = new ArrayList<>();
        if (!Objects.equals(param.getTable().getComment(), param.getComment())) {
            scripts.add("COMMENT ON TABLE " + qualifiedTable + " IS "
                    + H2GeneratorUtil.literal(StringUtil.blankToDefault(param.getComment(), null)));
        }
        if (param.getColumns() != null) {
            for (H2Column column : param.getColumns()) {
                if (column == null || column.isDeleted()) {
                    continue;
                }
                if (column.isCreated()) {
                    scripts.add("ALTER TABLE " + qualifiedTable
                            + " ADD COLUMN " + H2TableCreateSqlGenerator.columnDefinition(column, renamedColumns));
                    if (StringUtil.isNotBlank(column.getComment())) {
                        scripts.add(H2TableCreateSqlGenerator.commentOnColumnSql(
                                schema, tableName, column.getName(), column.getComment()));
                    }
                    continue;
                }
                String sourceName = StringUtil.blankToDefault(column.originalName(), column.getName());
                if (!StringUtil.equals(sourceName, column.getName())) {
                    scripts.add("ALTER TABLE " + qualifiedTable
                            + " ALTER COLUMN " + H2GeneratorUtil.quote(sourceName)
                            + " RENAME TO " + H2GeneratorUtil.quote(column.getName()));
                }
                appendColumnAlterScripts(scripts, schema, tableName, sourceName, column);
                if (column.isCommentChanged()) {
                    scripts.add(H2TableCreateSqlGenerator.commentOnColumnSql(
                            schema, tableName, column.getName(), column.getComment()));
                }
            }
            for (H2Column column : param.getColumns()) {
                if (column != null && column.isDeleted()) {
                    String name = StringUtil.blankToDefault(column.originalName(), column.getName());
                    scripts.add("ALTER TABLE " + qualifiedTable
                            + " DROP COLUMN " + H2GeneratorUtil.quote(name));
                }
            }
        }
        if (param.getChecks() != null) {
            for (H2Check check : param.getChecks()) {
                if (check == null) {
                    continue;
                }
                boolean changed = check.isDeleted() || check.isChanged();
                if (!check.isCreated() && StringUtil.isNotBlank(check.originalName()) && changed) {
                    scripts.add("ALTER TABLE " + qualifiedTable
                            + " DROP CONSTRAINT " + H2GeneratorUtil.quote(check.originalName()));
                }
                if (!check.isDeleted() && (check.isCreated() || check.isChanged())
                        && StringUtil.isNotBlank(check.getClause())) {
                    scripts.add("ALTER TABLE " + qualifiedTable
                            + " ADD CONSTRAINT " + H2GeneratorUtil.quote(check.getName())
                            + " CHECK (" + H2GeneratorUtil.replaceColumnReferences(
                            check.getClause().trim(), renamedColumns) + ")");
                }
            }
        }
        if (param.getForeignKeys() != null) {
            for (H2ForeignKey foreignKey : param.getForeignKeys()) {
                if (foreignKey == null) {
                    continue;
                }
                boolean changed = foreignKey.isDeleted() || foreignKey.isChanged();
                if (!foreignKey.isCreated() && StringUtil.isNotBlank(foreignKey.originalName()) && changed) {
                    scripts.add("ALTER TABLE " + qualifiedTable
                            + " DROP CONSTRAINT " + H2GeneratorUtil.quote(foreignKey.originalName()));
                }
                if (!foreignKey.isDeleted() && (foreignKey.isCreated() || foreignKey.isChanged())
                        && !foreignKey.isInvalid()) {
                    scripts.add("ALTER TABLE " + qualifiedTable
                            + " ADD " + H2GeneratorUtil.replaceColumnReferences(
                            foreignKey.toConstraintSql(), renamedColumns));
                }
            }
        }
        if (param.getIndexes() != null) {
            for (H2Index index : param.getIndexes()) {
                if (index == null) {
                    continue;
                }
                boolean changed = index.isDeleted() || index.isChangedDefinition();
                if (!index.isCreated() && StringUtil.isNotBlank(index.originalName()) && changed) {
                    scripts.add("DROP INDEX " + H2GeneratorUtil.quote(index.originalName()));
                }
                if (!index.isDeleted() && (index.isCreated() || index.isChangedDefinition())
                        && StringUtil.isNotBlank(index.getDefinition())) {
                    scripts.add(index.normalizedDefinition(originalName, tableName));
                }
            }
        }
        if (param.getTriggers() != null) {
            for (H2Trigger trigger : param.getTriggers()) {
                if (trigger == null) {
                    continue;
                }
                boolean changed = trigger.isDeleted() || trigger.isChangedDefinition();
                if (!trigger.isCreated() && StringUtil.isNotBlank(trigger.originalName()) && changed) {
                    scripts.add("DROP TRIGGER " + H2GeneratorUtil.quote(trigger.originalName()));
                }
                if (!trigger.isDeleted() && (trigger.isCreated() || trigger.isChangedDefinition())
                        && StringUtil.isNotBlank(trigger.getDefinition())) {
                    scripts.add(trigger.normalizedDefinition(originalName, tableName));
                }
            }
        }
        if (!StringUtil.equalsIgnoreCase(originalName, targetTableName)) {
            scripts.add("ALTER TABLE " + H2GeneratorUtil.qualified(schema, originalName)
                    + " RENAME TO " + H2GeneratorUtil.quote(targetTableName));
        }
        this.sqlBuilder.append(String.join(";\n", scripts));
        if (!scripts.isEmpty()) {
            this.sqlBuilder.append(';');
        }
    }

    /**
     * 按属性生成字段级 ALTER 语句。
     *
     * @param scripts    语句集合
     * @param tableName  表名称
     * @param columnName 数据库中的字段名称
     * @param column     新字段定义
     */
    private static void appendColumnAlterScripts(List<String> scripts, String schema, String tableName,
                                                 String columnName, H2Column column) {
        String table = H2GeneratorUtil.qualified(schema, tableName);
        String name = H2GeneratorUtil.quote(columnName);
        if (column.isGenerated()) {
            return;
        }
        if (column.isTypeChanged() || column.isSizeChanged() || column.isDigitsChanged()) {
            scripts.add("ALTER TABLE " + table + " ALTER COLUMN " + name
                    + " SET DATA TYPE " + H2TableCreateSqlGenerator.dataType(column));
        }
        if (column.isNotNullChanged()) {
            if (column.isNotNull()) {
                scripts.add("ALTER TABLE " + table + " ALTER COLUMN " + name + " SET NOT NULL");
            } else if (!column.isPrimaryKey()) {
                scripts.add("ALTER TABLE " + table + " ALTER COLUMN " + name + " SET NULL");
            }
        }
        if (column.isDefaultValueChanged()) {
            if (StringUtil.isNotBlank(column.getDefaultValue())) {
                scripts.add("ALTER TABLE " + table + " ALTER COLUMN " + name
                        + " SET DEFAULT " + column.getDefaultValue().trim());
            } else {
                scripts.add("ALTER TABLE " + table + " ALTER COLUMN " + name + " SET DEFAULT NULL");
            }
        }
        if (column.isCheckChanged()) {
            if (StringUtil.isNotBlank(column.getCheckName())) {
                scripts.add("ALTER TABLE " + table + " DROP CONSTRAINT "
                        + H2GeneratorUtil.quote(column.getCheckName()));
            }
            if (StringUtil.isNotBlank(column.getCheck())) {
                scripts.add("ALTER TABLE " + table + " ADD CHECK ("
                        + H2GeneratorUtil.replaceColumnReferences(
                        column.getCheck().trim(),
                        Map.of(StringUtil.blankToDefault(column.originalName(), column.getName()),
                                column.getName())) + ")");
            }
        }
    }

    private static Map<String, String> renamedColumns(List<H2Column> columns) {
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

    /**
     * 生成SQL列表
     *
     * @param param 修改表参数
     * @return SQL列表
     */
    public List<String> generate(H2AlertTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改表参数
     * @return SQL语句
     */
    public String generateSingle(H2AlertTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改表参数
     * @return SQL列表
     */
    public static List<String> generateSql(H2AlertTableParam param) {
        return new H2TableAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改表参数
     * @return SQL语句
     */
    public static String generateSqlSingle(H2AlertTableParam param) {
        return new H2TableAlertSqlGenerator().generateSingle(param);
    }
}
