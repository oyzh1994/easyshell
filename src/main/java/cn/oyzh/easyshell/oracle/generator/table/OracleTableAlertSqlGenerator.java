package cn.oyzh.easyshell.oracle.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.check.OracleCheck;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.foreignKey.OracleForeignKey;
import cn.oyzh.easyshell.oracle.index.OracleIndex;
import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.easyshell.oracle.table.OracleAlertTableParam;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.trigger.OracleTrigger;
import cn.oyzh.easyshell.util.oracle.ShellOracleColumnUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Oracle表结构修改SQL生成器，根据表变更参数生成对应的 ALTER TABLE 等语句
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleTableAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 当前数据库是否支持IDENTITY；11g自动降级为序列触发器。
     */
    private boolean identitySupported = true;

    /**
     * 执行SQL生成，依次处理主键、字段、索引、外键、检查、触发器、表空间和表注释的变更
     *
     * @param param 表修改参数
     */
    private void _generate(OracleAlertTableParam param) {
        OracleTable table = param.getTable();
        String fullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE);
        // 修改字段
        if (param.columnChanged()) {
            this.columnHandle(param);
        }
        // 主键变更，应在修改字段后
        if (param.primaryKeyChanged()) {
            this.primaryKeyHandle(param);
        }
        // 修改索引
        if (param.hasIndex()) {
            this.indexHandle(param);
        }
        // 修改外键
        if (param.hasForeignKey()) {
            this.foreignKeyHandle(param);
        }
        // 修改检查
        if (param.hasCheck()) {
            this.checkHandle(param);
        }
        // 修改触发器
        if (param.hasTrigger()) {
            this.triggerHandle(param);
        }
        // 修改表空间
        if (table.hasTableSpace()) {
            StringBuilder builder = new StringBuilder();
            builder.append("ALTER TABLE ")
                    .append(fullName)
                    .append(" MOVE TABLESPACE ")
                    .append(table.getTableSpace()).append(";");
            this.sqlList.add(builder.toString());
        }
        // 修改表注释
        if (table.hasComment()) {
            StringBuilder builder = new StringBuilder();
            builder.append("COMMENT ON TABLE ")
                    .append(fullName)
                    .append(" IS ")
                    .append(DBUtil.wrapData(table.getComment(), DBDialect.ORACLE)).append(";");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 触发器处理
     *
     * @param param 表修改参数
     */
    protected void triggerHandle(OracleAlertTableParam param) {
        DBObjects<OracleTrigger> triggers = param.getTriggers();
        // 删除、变更的语句先执行，否则可能异常
        for (OracleTrigger trigger : triggers) {
            if (DBObjectList.isDeleted(trigger) || DBObjectList.isChanged(trigger)) {
                StringBuilder builder = new StringBuilder();
                builder.append("DROP TRIGGER ")
                        .append(DBUtil.wrap(trigger.originalName(), DBDialect.ORACLE))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
            if (DBObjectList.isChanged(trigger) || DBObjectList.isCreated(trigger)) {
                StringBuilder builder = new StringBuilder();
                String body = ShellOracleHelper.normalizeTriggerBody(trigger.getDefinition());
                builder.append("CREATE OR REPLACE TRIGGER ")
                        .append(DBUtil.wrap(param.getSchema(), trigger.getName(), DBDialect.ORACLE))
                        .append(" ")
                        .append(trigger.getPolicy())
                        .append(" ON ")
                        .append(DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE))
                        .append(" FOR EACH ROW ")
                        .append(body);
                this.sqlList.add(builder.toString());
            }
        }
    }

    /**
     * 字段处理
     *
     * @param param 表修改参数
     */
    protected void columnHandle(OracleAlertTableParam param) {
        int len;
        StringBuilder builder = new StringBuilder();
        String fullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE);

        // 字段重命名
        for (OracleColumn column : param.getColumns()) {
            if (OracleColumns.isChanged(column) && column.isNameChanged()) {
                String renameSql = "ALTER TABLE " + fullName + " RENAME COLUMN "
                        + DBUtil.wrap(column.originalName(), DBDialect.ORACLE) + " TO "
                        + DBUtil.wrap(column.getName(), DBDialect.ORACLE) + ";";
                this.sqlList.add(renameSql);
            }
        }

        // 删除字段
        for (OracleColumn column : param.getColumns()) {
            if (OracleColumns.isDeleted(column)) {
                this.sqlList.add("ALTER TABLE " + fullName + " DROP COLUMN "
                        + DBUtil.wrap(column.getName(), DBDialect.ORACLE) + ";");
            }
        }

        // 新增字段
        StringUtil.clear(builder);
        builder.append("ALTER TABLE ")
                .append(fullName)
                .append(" ");
        builder.append(" ADD (\n");
        len = builder.length();
        for (OracleColumn column : param.getColumns()) {
            if (OracleColumns.isCreated(column)) {
                builder.append("\t");
                builder.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE));
                this.appendColumnType(builder, column, true);
                builder.append(",\n");
            }
        }
        // 删除最后一个字符
        StringUtil.deleteLast(builder, ",");
        if (len != builder.length()) {
            builder.append(");");
            this.sqlList.add(builder.toString());
        }

        // 被虚拟列引用的基础列不能直接修改，先临时删除相关虚拟列。
        List<OracleColumn> dependentVirtualColumns = this.dependentVirtualColumns(param);
        for (int i = dependentVirtualColumns.size() - 1; i >= 0; i--) {
            OracleColumn column = dependentVirtualColumns.get(i);
            this.sqlList.add("ALTER TABLE " + fullName + " DROP COLUMN "
                    + DBUtil.wrap(column.getName(), DBDialect.ORACLE) + ";");
        }

        // 修改字段
        for (OracleColumn column : param.getColumns()) {
            if (OracleColumns.isChanged(column) && this.hasDefinitionChanged(column)) {
                if (dependentVirtualColumns.contains(column)) {
                    continue;
                }
                if (column.isGeneratedChanged() && column.isGenerated()) {
                    this.convertColumnToVirtual(fullName, param, column);
                    continue;
                }
                if (column.isGeneratedChanged() && !column.isGenerated()) {
                    this.materializeVirtualColumn(fullName, param, column);
                    continue;
                }
                if (this.requiresColumnRebuild(column)) {
                    this.rebuildColumn(fullName, param, column);
                    this.autoIncrementHandle(param, column);
                    continue;
                }
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ")
                        .append(fullName)
                        .append(" MODIFY (")
                        .append(DBUtil.wrap(column.getName(), DBDialect.ORACLE));
                this.appendColumnType(builder, column, false);
                builder.append(");");
                this.sqlList.add(builder.toString());
                this.autoIncrementHandle(param, column);
            }
        }

        for (OracleColumn column : param.getColumns()) {
            if (OracleColumns.isCreated(column)) {
                this.autoIncrementHandle(param, column);
            }
        }

        // 基础列修改完成后按原依赖顺序重建虚拟列。
        for (OracleColumn column : dependentVirtualColumns) {
            StringUtil.clear(builder);
            builder.append("ALTER TABLE ").append(fullName).append(" ADD (")
                    .append(DBUtil.wrap(column.getName(), DBDialect.ORACLE));
            this.appendColumnType(builder, column, true);
            builder.append(");");
            this.sqlList.add(builder.toString());
        }

        // 注释
        for (OracleColumn column : param.getColumns()) {
            if (OracleColumns.isChanged(column) || OracleColumns.isCreated(column)) {
                if (column.hasComment() && column.isCommentChanged()) {
                    String commentSql = "COMMENT ON COLUMN "
                            + DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE)
                            + "." + DBUtil.wrap(column.getName(), DBDialect.ORACLE) + " IS "
                            + DBUtil.wrapData(column.getComment(), DBDialect.ORACLE) + ";";
                    this.sqlList.add(commentSql);
                }
            }
        }
    }

    /**
     * Oracle在列非空时不允许直接减小NUMBER精度或标度，此时需要重建该列。
     */
    private boolean requiresColumnRebuild(OracleColumn column) {
        if (column.isPrimaryKey() || column.isAutoIncrement() || column.isGenerated()) {
            return false;
        }
        if (!column.isTypeChanged() && !column.isSizeChanged() && !column.isDigitsChanged()) {
            return false;
        }
        String originalType = StringUtil.blankToDefault(column.getTypeOriginal(), column.getType());
        String targetType = StringUtil.blankToDefault(column.getType(), originalType);
        if (!this.isNumericType(originalType) || !this.isNumericType(targetType)) {
            return false;
        }
        int originalScale = this.effectiveScale(
                originalType, column.getDigitsOriginal());
        int targetScale = this.effectiveScale(
                targetType, column.getDigits());
        if (targetScale < originalScale) {
            return true;
        }
        int originalPrecision = column.getSizeOriginal() == null ? 0 : column.getSizeOriginal();
        int targetPrecision = column.getSize() == null ? 0 : column.getSize();
        return targetPrecision > 0 && originalPrecision > 0 && targetPrecision < originalPrecision;
    }

    private int effectiveScale(String type, Integer digits) {
        String upper = StringUtil.blankToDefault(type, "").toUpperCase(java.util.Locale.ROOT);
        if (upper.equals("INT") || upper.contains("INTEGER")
                || upper.equals("SMALLINT") || upper.equals("BIGINT")) {
            return 0;
        }
        return digits == null ? 0 : digits;
    }

    private boolean isNumericType(String type) {
        String upper = StringUtil.blankToDefault(type, "").toUpperCase(java.util.Locale.ROOT);
        return upper.contains("NUMBER") || upper.contains("INTEGER") || upper.equals("INT")
                || upper.contains("DECIMAL") || upper.contains("NUMERIC")
                || upper.startsWith("FLOAT") || upper.startsWith("BINARY_FLOAT")
                || upper.startsWith("BINARY_DOUBLE");
    }

    /**
     * 通过临时列转换并替换原列，避免Oracle要求先清空数据。
     */
    private void rebuildColumn(String tableFullName, OracleAlertTableParam param, OracleColumn column) {
        String temporary = "TMP_" + Integer.toUnsignedString(
                (param.tableName() + "." + column.getName()).hashCode(), 16)
                .toUpperCase(java.util.Locale.ROOT);
        String original = DBUtil.wrap(column.getName(), DBDialect.ORACLE);
        String temporaryName = DBUtil.wrap(temporary, DBDialect.ORACLE);
        StringBuilder builder = new StringBuilder("ALTER TABLE ")
                .append(tableFullName).append(" ADD (").append(temporaryName);
        this.appendColumnDataType(builder, column);
        builder.append(");");
        this.sqlList.add(builder.toString());
        this.sqlList.add("UPDATE " + tableFullName + " SET " + temporaryName
                + " = " + original + ";");
        if (!column.isNullable()) {
            this.sqlList.add("ALTER TABLE " + tableFullName + " MODIFY ("
                    + temporaryName + " NOT NULL);");
        }
        this.sqlList.add("ALTER TABLE " + tableFullName + " DROP COLUMN " + original + ";");
        this.sqlList.add("ALTER TABLE " + tableFullName + " RENAME COLUMN "
                + temporaryName + " TO " + original + ";");
        if (!column.isAutoIncrement() && column.getDefaultValueFix() != null) {
            this.sqlList.add("ALTER TABLE " + tableFullName + " MODIFY (" + original
                    + " DEFAULT " + column.getDefaultValueFix() + ");");
        }
        if (StringUtil.isNotBlank(column.getComment())) {
            this.sqlList.add("COMMENT ON COLUMN " + tableFullName + "." + original + " IS "
                    + DBUtil.wrapData(column.getComment(), DBDialect.ORACLE) + ";");
        }
    }

    /**
     * 查找因基础列定义变化而必须临时重建的已有虚拟列。
     */
    private List<OracleColumn> dependentVirtualColumns(OracleAlertTableParam param) {
        List<OracleColumn> changedBaseColumns = param.getColumns().stream()
                .filter(column -> !OracleColumns.isCreated(column))
                .filter(column -> !OracleColumns.isDeleted(column))
                .filter(column -> OracleColumns.isChanged(column) && this.hasDefinitionChanged(column))
                .toList();
        if (changedBaseColumns.isEmpty()) {
            return List.of();
        }
        List<OracleColumn> virtualColumns = param.getColumns().stream()
                .filter(column -> column.isGenerated()
                        && !OracleColumns.isCreated(column)
                        && !OracleColumns.isDeleted(column))
                .toList();
        Set<OracleColumn> dependent = new HashSet<>();
        for (OracleColumn virtualColumn : virtualColumns) {
            for (OracleColumn baseColumn : changedBaseColumns) {
                if (this.expressionReferences(
                        virtualColumn.getGeneratedExpression(), baseColumn.getName())
                        || this.expressionReferences(
                        virtualColumn.getGeneratedExpression(), baseColumn.originalName())) {
                    dependent.add(virtualColumn);
                    break;
                }
            }
        }
        // 虚拟列之间也可能存在引用，按原列顺序逐步补齐依赖，删除时反向执行。
        boolean added;
        do {
            added = false;
            for (OracleColumn virtualColumn : virtualColumns) {
                if (dependent.contains(virtualColumn)) {
                    continue;
                }
                for (OracleColumn dependency : dependent) {
                    if (this.expressionReferences(
                            virtualColumn.getGeneratedExpression(), dependency.getName())) {
                        dependent.add(virtualColumn);
                        added = true;
                        break;
                    }
                }
            }
        } while (added);
        List<OracleColumn> result = new ArrayList<>();
        for (OracleColumn virtualColumn : virtualColumns) {
            if (dependent.contains(virtualColumn)) {
                result.add(virtualColumn);
            }
        }
        return result;
    }

    private boolean expressionReferences(String expression, String columnName) {
        if (StringUtil.isBlank(expression) || StringUtil.isBlank(columnName)) {
            return false;
        }
        return java.util.regex.Pattern.compile(
                        "(?i)(?<![A-Z0-9_$#])" + java.util.regex.Pattern.quote(columnName)
                                + "(?![A-Z0-9_$#])")
                .matcher(expression).find();
    }

    /**
     * Oracle不能直接移除虚拟列表达式，先复制计算值到临时普通列，再替换原列。
     */
    private void materializeVirtualColumn(String tableFullName, OracleAlertTableParam param,
                                          OracleColumn column) {
        String temporary = "TMP_" + Integer.toUnsignedString(
                (param.tableName() + "." + column.getName()).hashCode(), 16)
                .toUpperCase(java.util.Locale.ROOT);
        String original = DBUtil.wrap(column.getName(), DBDialect.ORACLE);
        String temporaryName = DBUtil.wrap(temporary, DBDialect.ORACLE);
        StringBuilder builder = new StringBuilder("ALTER TABLE ")
                .append(tableFullName).append(" ADD (").append(temporaryName);
        this.appendColumnType(builder, column, true);
        builder.append(");");
        this.sqlList.add(builder.toString());
        this.sqlList.add("UPDATE " + tableFullName + " SET " + temporaryName
                + " = " + original + ";");
        this.sqlList.add("ALTER TABLE " + tableFullName + " DROP COLUMN " + original + ";");
        this.sqlList.add("ALTER TABLE " + tableFullName + " RENAME COLUMN "
                + temporaryName + " TO " + original + ";");
        if (StringUtil.isNotBlank(column.getComment())) {
            this.sqlList.add("COMMENT ON COLUMN " + tableFullName + "." + original + " IS "
                    + DBUtil.wrapData(column.getComment(), DBDialect.ORACLE) + ";");
        }
    }

    /**
     * Oracle不能直接把普通列改成虚拟列，通过临时虚拟列替换原列。
     */
    private void convertColumnToVirtual(String tableFullName, OracleAlertTableParam param,
                                        OracleColumn column) {
        if (!column.supportGenerated()) {
            throw new IllegalArgumentException(
                    "Oracle column type does not support virtual columns: " + column.getName());
        }
        String expression = StringUtil.blankToDefault(
                column.getGeneratedExpression(), "").trim();
        if (StringUtil.isBlank(expression)) {
            throw new IllegalArgumentException(
                    "Oracle virtual column expression is empty: " + column.getName());
        }
        if (column.isPrimaryKey()) {
            throw new IllegalArgumentException(
                    "Oracle primary key columns cannot be converted to virtual columns: "
                            + column.getName());
        }
        if (column.isAutoIncrementOriginal()) {
            throw new IllegalArgumentException(
                    "Oracle auto-increment columns cannot be converted to virtual columns: "
                            + column.getName());
        }
        String temporary = "TMP_" + Integer.toUnsignedString(
                (param.tableName() + "." + column.getName()).hashCode(), 16)
                .toUpperCase(java.util.Locale.ROOT);
        String original = DBUtil.wrap(column.getName(), DBDialect.ORACLE);
        String temporaryName = DBUtil.wrap(temporary, DBDialect.ORACLE);
        StringBuilder builder = new StringBuilder("ALTER TABLE ")
                .append(tableFullName).append(" ADD (").append(temporaryName);
        this.appendColumnType(builder, column, true);
        builder.append(");");
        this.sqlList.add(builder.toString());
        this.sqlList.add("ALTER TABLE " + tableFullName + " DROP COLUMN " + original + ";");
        this.sqlList.add("ALTER TABLE " + tableFullName + " RENAME COLUMN "
                + temporaryName + " TO " + original + ";");
        if (StringUtil.isNotBlank(column.getComment())) {
            this.sqlList.add("COMMENT ON COLUMN " + tableFullName + "." + original + " IS "
                    + DBUtil.wrapData(column.getComment(), DBDialect.ORACLE) + ";");
        }
    }

    private boolean hasDefinitionChanged(OracleColumn column) {
        return column.isNameChanged()
                || column.isTypeChanged()
                || column.isSizeChanged()
                || column.isDigitsChanged()
                || column.isDefaultValueChanged()
                || column.isNullableChanged()
                || column.isAutoIncrementChanged()
                || column.isAutoIncrementTypeChanged()
                || column.isGeneratedChanged()
                || column.isGeneratedExpressionChanged();
    }

    /**
     * 维护兼容Oracle 11g的序列和触发器式自增。
     */
    protected void autoIncrementHandle(OracleAlertTableParam param, OracleColumn column) {
        if (!OracleColumns.isCreated(column) && !column.isAutoIncrementChanged()
                && !column.isNameChanged() && !column.isAutoIncrementTypeChanged()) {
            return;
        }
        boolean originalIdentity = StringUtil.equalsIgnoreCase(column.getAutoIncrementTypeOriginal(), "IDENTITY");
        // Oracle不支持把已有普通列通过MODIFY转换为IDENTITY列。
        // 对已有列使用等价的序列和触发器实现，新建列仍保留原生IDENTITY。
        boolean targetIdentity = this.isIdentity(column)
                && (OracleColumns.isCreated(column) || originalIdentity);
        String sourceColumnName = column.isNameChanged() ? column.originalName() : column.getName();
        String generatedSequenceName = autoIncrementObjectName(param.tableName(), sourceColumnName, "SEQ");
        String generatedTriggerName = autoIncrementObjectName(param.tableName(), sourceColumnName, "TRG");
        String oldSequenceName = StringUtil.blankToDefault(
                column.getAutoIncrementSequenceName(), generatedSequenceName);
        String oldTriggerName = StringUtil.blankToDefault(
                column.getAutoIncrementTriggerName(), generatedTriggerName);
        String newSequenceName = autoIncrementObjectName(param.tableName(), column.getName(), "SEQ");
        String newTriggerName = autoIncrementObjectName(param.tableName(), column.getName(), "TRG");
        boolean customSequence = StringUtil.isNotBlank(column.getAutoIncrementSequenceName())
                && !StringUtil.equalsIgnoreCase(column.getAutoIncrementSequenceName(), generatedSequenceName);
        boolean customTrigger = StringUtil.isNotBlank(column.getAutoIncrementTriggerName())
                && !StringUtil.equalsIgnoreCase(column.getAutoIncrementTriggerName(), generatedTriggerName);
        String targetSequenceName = customSequence ? oldSequenceName : newSequenceName;
        String targetTriggerName = customTrigger ? oldTriggerName : newTriggerName;
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE);
        String targetColumnName = DBUtil.wrap(column.getName(), DBDialect.ORACLE);
        if (targetIdentity && !originalIdentity) {
            if (!OracleColumns.isCreated(column)) {
                if (column.isAutoIncrementOriginal()) {
                    this.dropAutoIncrementTrigger(param, oldTriggerName);
                    if (!customSequence) {
                        this.dropAutoIncrementSequence(param, oldSequenceName);
                    }
                }
                this.sqlList.add("ALTER TABLE " + tableFullName + " MODIFY (" + targetColumnName
                        + " GENERATED BY DEFAULT ON NULL AS IDENTITY);");
            }
            return;
        }
        if (!column.isAutoIncrement()) {
            if (originalIdentity) {
                this.sqlList.add("ALTER TABLE " + tableFullName + " MODIFY (" + targetColumnName + " DROP IDENTITY);");
            } else if (column.isAutoIncrementOriginal()) {
                this.dropAutoIncrementTrigger(param, oldTriggerName);
                if (!customSequence) {
                    this.dropAutoIncrementSequence(param, oldSequenceName);
                }
            }
            return;
        }
        if (originalIdentity && targetIdentity) {
            return;
        }
        if (originalIdentity) {
            this.sqlList.add("ALTER TABLE " + tableFullName + " MODIFY (" + targetColumnName + " DROP IDENTITY);");
        }
        boolean renameRequired = !oldSequenceName.equals(targetSequenceName)
                || !oldTriggerName.equals(targetTriggerName);
        if (renameRequired && column.isAutoIncrementOriginal()) {
            this.dropAutoIncrementTrigger(param, oldTriggerName);
            if (!customSequence && !oldSequenceName.equals(targetSequenceName)) {
                this.dropAutoIncrementSequence(param, oldSequenceName);
            }
        }
        boolean createSequence = !customSequence
                && (OracleColumns.isCreated(column) || !column.isAutoIncrementOriginal()
                || !oldSequenceName.equals(targetSequenceName));
        if (createSequence) {
            this.createAutoIncrementSequence(param, targetSequenceName, column.getName(),
                    OracleColumns.isCreated(column));
        }
        this.sqlList.add("CREATE OR REPLACE TRIGGER " + DBUtil.wrap(param.getSchema(), targetTriggerName, DBDialect.ORACLE)
                + " BEFORE INSERT ON " + tableFullName
                + " FOR EACH ROW WHEN (NEW." + targetColumnName + " IS NULL) BEGIN SELECT "
                + DBUtil.wrap(param.getSchema(), targetSequenceName, DBDialect.ORACLE)
                + ".NEXTVAL INTO :NEW." + targetColumnName + " FROM DUAL; END;");
    }

    /**
     * 新建序列。已有表从当前最大值之后开始，避免自增列与存量主键冲突。
     */
    private void createAutoIncrementSequence(OracleAlertTableParam param, String sequenceName,
                                             String columnName, boolean created) {
        String sequenceFullName = DBUtil.wrap(param.getSchema(), sequenceName, DBDialect.ORACLE);
        if (created) {
            this.sqlList.add("CREATE SEQUENCE " + sequenceFullName
                    + " START WITH 1 INCREMENT BY 1 NOCACHE;");
            return;
        }
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE);
        String columnFullName = DBUtil.wrap(columnName, DBDialect.ORACLE);
        String sequenceLiteral = sequenceFullName.replace("'", "''");
        this.sqlList.add("DECLARE\n"
                + "    V_START NUMBER;\n"
                + "BEGIN\n"
                + "    SELECT NVL(MAX(" + columnFullName + "), 0) + 1 INTO V_START FROM "
                + tableFullName + ";\n"
                + "    EXECUTE IMMEDIATE 'CREATE SEQUENCE " + sequenceLiteral
                + " START WITH ' || TO_CHAR(V_START) || ' INCREMENT BY 1 NOCACHE';\n"
                + "END;");
    }

    private void dropAutoIncrementTrigger(OracleAlertTableParam param, String triggerName) {
        this.sqlList.add("DROP TRIGGER " + DBUtil.wrap(param.getSchema(), triggerName, DBDialect.ORACLE) + ";");
    }

    private void dropAutoIncrementSequence(OracleAlertTableParam param, String sequenceName) {
        this.sqlList.add("DROP SEQUENCE " + DBUtil.wrap(param.getSchema(), sequenceName, DBDialect.ORACLE) + ";");
    }

    /**
     * 生成兼容Oracle 11g 30字符标识符限制的对象名称。
     */
    protected static String autoIncrementObjectName(String tableName, String columnName, String suffix) {
        String raw = tableName + "_" + columnName;
        String normalized = raw.toUpperCase().replaceAll("[^A-Z0-9_$#]", "_");
        int maxBaseLength = 30 - suffix.length() - 1;
        return normalized.substring(0, Math.min(normalized.length(), maxBaseLength)) + "_" + suffix;
    }

    /**
     * 主键处理
     *
     * @param param 表修改参数
     */
    protected void primaryKeyHandle(OracleAlertTableParam param) {
        int len;
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE);

        // 删除主键
        if (param.isExistPrimaryKey()) {
            for (String primaryKey : param.getPrimaryKeys()) {
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append(" DROP CONSTRAINT ");
                builder.append(DBUtil.wrap(primaryKey, DBDialect.ORACLE));
                builder.append(" CASCADE;");
                this.sqlList.add(builder.toString());
            }
        }

        // 新增主键
        StringUtil.clear(builder);
        builder.append("ALTER TABLE ").append(tableFullName).append(" ");
        builder.append(" ADD PRIMARY KEY (");
        len = builder.length();
        for (OracleColumn column : param.primaryKeys()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE));
            builder.append(",");
        }
        StringUtil.deleteLast(builder, ",");
        if (len != builder.length()) {
            builder.append(");");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 索引处理
     *
     * @param param 表修改参数
     */
    protected void indexHandle(OracleAlertTableParam param) {
        DBObjects<OracleIndex> indexes = param.getIndexes();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE);

        // 删除索引
        for (OracleIndex index : indexes) {
            // 索引删除用独立DROP INDEX语句（Oracle不支持 ALTER TABLE DROP INDEX）
            if (DBObjectList.isDeleted(index) || DBObjectList.isChanged(index)) {
                StringBuilder builder = new StringBuilder("DROP INDEX ");
                builder.append(DBUtil.wrap(index.originalName(), DBDialect.ORACLE))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 新增、编辑索引
        for (OracleIndex index : indexes) {
            // 索引新增用独立CREATE INDEX语句
            if (DBObjectList.isCreated(index) || DBObjectList.isChanged(index)) {
                StringBuilder builder = new StringBuilder("CREATE ");
                builder.append(OracleTableCreateSqlGenerator.indexTypePrefix(index));
                builder.append("INDEX ")
                        .append(DBUtil.wrap(index.getName(), DBDialect.ORACLE))
                        .append(" ON ").append(tableFullName)
                        .append(" (");
                for (OracleIndex.IndexColumn column : index.getColumns()) {
                    builder.append(DBUtil.wrap(column.getColumnName(), DBDialect.ORACLE));
                    builder.append(",");
                }
                StringUtil.deleteLast(builder, ",");
                builder.append(");");
                this.sqlList.add(builder.toString());
            }
        }
    }

    /**
     * 外键处理
     *
     * @param param 表修改参数
     */
    protected void foreignKeyHandle(OracleAlertTableParam param) {
        DBObjects<OracleForeignKey> foreignKeys = param.getForeignKeys();
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE);

        // 删除外键
        for (OracleForeignKey foreignKey : foreignKeys) {
            if (DBObjectList.isDeleted(foreignKey) || DBObjectList.isChanged(foreignKey)) {
                String fkName = foreignKey.originalName();
                // 名称为null是临时数据
                if (StringUtil.isNotBlank(fkName)) {
                    StringUtil.clear(builder);
                    builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                    builder.append(" DROP CONSTRAINT ")
                            .append(DBUtil.wrap(foreignKey.originalName(), DBDialect.ORACLE))
                            .append(";");
                    this.sqlList.add(builder.toString());
                }
            }
        }

        // 新增外键
        for (OracleForeignKey foreignKey : foreignKeys) {
            if (DBObjectList.isCreated(foreignKey) || DBObjectList.isChanged(foreignKey)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append(" ADD CONSTRAINT ")
                        .append(DBUtil.wrap(foreignKey.getName(), DBDialect.ORACLE))
                        .append(" FOREIGN KEY (");
                for (String column : foreignKey.getColumns()) {
                    builder.append(DBUtil.wrap(column, DBDialect.ORACLE)).append(",");
                }
                StringUtil.deleteLast(builder, ",");
                builder.append(")")
                        .append(" REFERENCES ")
                        .append(DBUtil.wrap(foreignKey.getReferencedDatabase(), foreignKey.getReferencedTable(), DBDialect.ORACLE))
                        .append(" (");
                for (String column : foreignKey.getReferencedColumns()) {
                    builder.append(DBUtil.wrap(column, DBDialect.ORACLE)).append(",");
                }
                StringUtil.deleteLast(builder, ",");
                builder.append(")")
                    .append(OracleTableCreateSqlGenerator.deletePolicy(foreignKey.getDeletePolicy()));
                // 拼接,
                builder.append(";");
                this.sqlList.add(builder.toString());
            }
        }
    }

    /**
     * 检查处理
     *
     * @param param 表修改参数
     */
    protected void checkHandle(OracleAlertTableParam param) {
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE);
        DBObjects<OracleCheck> checks = param.getChecks();

        // 删除检查
        for (OracleCheck check : checks) {
            if (DBObjectList.isDeleted(check) || DBObjectList.isChanged(check)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append("DROP CONSTRAINT ")
                        .append(DBUtil.wrap(check.originalName(), DBDialect.ORACLE))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 新增、编辑检查
        for (OracleCheck check : checks) {
            if (DBObjectList.isCreated(check) || DBObjectList.isChanged(check)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append(" ADD CONSTRAINT ")
                        .append(DBUtil.wrap(check.getName(), DBDialect.ORACLE))
                        .append(" CHECK (")
                        .append(check.getClause())
                        .append(")");
                builder.append(";");
                this.sqlList.add(builder.toString());
            }
        }
    }

    /**
     * 追加列类型定义（类型、长度、默认值、NULL、IDENTITY等）
     *
     * @param builder SQL构建器
     * @param column  列
     * @param created 是否为新增列
     */
    private void appendColumnType(StringBuilder builder, OracleColumn column, boolean created) {
        this.appendColumnDataType(builder, column);
        if (column.isGenerated()) {
            if (!column.supportGenerated()) {
                throw new IllegalArgumentException(
                        "Oracle column type does not support virtual columns: " + column.getName());
            }
            String expression = StringUtil.blankToDefault(
                    column.getGeneratedExpression(), "").trim();
            if (StringUtil.isBlank(expression)) {
                throw new IllegalArgumentException(
                        "Oracle virtual column expression is empty: " + column.getName());
            }
            builder.append(" GENERATED ALWAYS AS (").append(expression).append(") VIRTUAL");
        }
        boolean isIdentity = !column.isGenerated() && this.isIdentity(column);
        if (created && isIdentity) {
            builder.append(" GENERATED BY DEFAULT ON NULL AS IDENTITY");
        }
        if (!column.isGenerated() && !column.isAutoIncrement()
                && column.getDefaultValueFix() != null) {
            builder.append(" DEFAULT ").append(column.getDefaultValueFix());
        }
        if (isIdentity) {
            // IDENTITY列由Oracle强制为NOT NULL。
        } else if (column.isNullable()) {
            // Oracle 11g不允许对已经可空的列重复执行 MODIFY ... NULL。
            if (created || !column.isNullableOriginal()) {
                builder.append(" NULL");
            }
        } else {
            if (created || column.isNullableOriginal()) {
                builder.append(" NOT NULL");
            }
        }
    }

    private void appendColumnDataType(StringBuilder builder, OracleColumn column) {
        builder.append(" ").append(column.getType());
        Integer size = column.getSize() != null
                ? column.getSize()
                : ShellOracleColumnUtil.requiredSize(column.getType());
        if (column.supportSize() && size != null) {
            builder.append("(").append(size);
            if (column.supportDigits() && column.getDigits() != null) {
                builder.append(",").append(column.getDigits());
            }
            builder.append(")");
        } else if (column.supportValue() && column.getValue() != null) {
            builder.append("(").append(column.getValue()).append(")");
        }
    }

    /**
     * 生成表修改SQL列表
     *
     * @param param 表修改参数
     * @return SQL列表
     */
    public List<String> generate(OracleAlertTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 按数据库能力生成表修改SQL列表。
     *
     * @param param             表修改参数
     * @param identitySupported 是否支持IDENTITY
     * @return SQL列表
     */
    public List<String> generate(OracleAlertTableParam param, boolean identitySupported) {
        this.identitySupported = identitySupported;
        return this.generate(param);
    }

    /**
     * 生成单条表修改SQL
     *
     * @param param 表修改参数
     * @return 单条SQL
     */
    public String generateSingle(OracleAlertTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成表修改SQL列表
     *
     * @param param 表修改参数
     * @return SQL列表
     */
    public static List<String> generateSql(OracleAlertTableParam param) {
        return new OracleTableAlertSqlGenerator().generate(param);
    }

    /**
     * 按数据库能力生成表修改SQL列表。
     *
     * @param param             表修改参数
     * @param identitySupported 是否支持IDENTITY
     * @return SQL列表
     */
    public static List<String> generateSql(OracleAlertTableParam param, boolean identitySupported) {
        return new OracleTableAlertSqlGenerator().generate(param, identitySupported);
    }

    /**
     * 生成单条表修改SQL
     *
     * @param param 表修改参数
     * @return 单条SQL
     */
    public static String generateSqlSingle(OracleAlertTableParam param) {
        return new OracleTableAlertSqlGenerator().generateSingle(param);
    }

    private boolean isIdentity(OracleColumn column) {
        return this.identitySupported && column.isAutoIncrement() && column.isIdentityAutoIncrementType();
    }

}
