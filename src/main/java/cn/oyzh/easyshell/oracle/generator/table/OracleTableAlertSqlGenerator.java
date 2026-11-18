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
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * Oracle表结构修改SQL生成器，根据表变更参数生成对应的 ALTER TABLE 等语句
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleTableAlertSqlGenerator extends DBSqlGenerator {

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

        // 修改字段
        for (OracleColumn column : param.getColumns()) {
            if (OracleColumns.isChanged(column)) {
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
     * 维护兼容Oracle 11g的序列和触发器式自增。
     */
    protected void autoIncrementHandle(OracleAlertTableParam param, OracleColumn column) {
        if (!OracleColumns.isCreated(column) && !column.isAutoIncrementChanged()
                && !column.isNameChanged() && !column.isAutoIncrementTypeChanged()) {
            return;
        }
        boolean originalIdentity = StringUtil.equalsIgnoreCase(column.getAutoIncrementTypeOriginal(), "IDENTITY");
        boolean targetIdentity = column.isIdentityAutoIncrementType();
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
        if (!customSequence
                && (OracleColumns.isCreated(column) || !column.isAutoIncrementOriginal()
                || !oldSequenceName.equals(targetSequenceName))) {
            this.sqlList.add("CREATE SEQUENCE " + DBUtil.wrap(param.getSchema(), targetSequenceName, DBDialect.ORACLE)
                    + " START WITH 1 INCREMENT BY 1 NOCACHE;");
        }
        this.sqlList.add("CREATE OR REPLACE TRIGGER " + DBUtil.wrap(param.getSchema(), targetTriggerName, DBDialect.ORACLE)
                + " BEFORE INSERT ON " + tableFullName
                + " FOR EACH ROW WHEN (NEW." + targetColumnName + " IS NULL) BEGIN SELECT "
                + DBUtil.wrap(param.getSchema(), targetSequenceName, DBDialect.ORACLE)
                + ".NEXTVAL INTO :NEW." + targetColumnName + " FROM DUAL; END;");
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
        builder.append(" ").append(column.getType());
        if (column.supportSize() && column.getSize() != null) {
            builder.append("(").append(column.getSize());
            if (column.supportDigits() && column.getDigits() != null) {
                builder.append(",").append(column.getDigits());
            }
            builder.append(")");
        } else if (column.supportValue() && column.getValue() != null) {
            builder.append("(").append(column.getValue()).append(")");
        }
        boolean isIdentity = column.isAutoIncrement() && column.isIdentityAutoIncrementType();
        if (created && isIdentity) {
            builder.append(" GENERATED BY DEFAULT ON NULL AS IDENTITY");
        }
        if (!column.isAutoIncrement() && column.getDefaultValueFix() != null) {
            builder.append(" DEFAULT ").append(column.getDefaultValueFix());
        }
        if (isIdentity) {
            // IDENTITY列由Oracle强制为NOT NULL。
        } else if (column.isNullable()) {
            builder.append(" NULL");
        } else {
            builder.append(" NOT NULL");
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
     * 生成单条表修改SQL
     *
     * @param param 表修改参数
     * @return 单条SQL
     */
    public static String generateSqlSingle(OracleAlertTableParam param) {
        return new OracleTableAlertSqlGenerator().generateSingle(param);
    }

}
