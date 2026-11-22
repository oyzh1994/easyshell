package cn.oyzh.easyshell.sqlserver.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerHelper;
import cn.oyzh.easyshell.sqlserver.check.SqlServerCheck;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.foreignKey.SqlServerForeignKey;
import cn.oyzh.easyshell.sqlserver.index.SqlServerIndex;
import cn.oyzh.easyshell.sqlserver.table.SqlServerAlertTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.trigger.SqlServerTrigger;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * SqlServer表结构修改SQL生成器，根据表变更参数生成对应的 ALTER TABLE 等语句
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerTableAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 执行SQL生成，依次处理主键、字段、索引、外键、检查、触发器、表空间和表注释的变更
     *
     * @param param 表修改参数
     */
    private void _generate(SqlServerAlertTableParam param) {
        SqlServerTable table = param.getTable();
        String fullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.SQLSERVER);
        // SQL Server不允许直接修改被主键引用的列，必须先移除主键，改列后再恢复
        boolean updatePrimaryKey = param.primaryKeyChanged()
                || this.primaryKeyColumnNeedsAlter(param);
        if (updatePrimaryKey) {
            this.dropPrimaryKey(param);
        }
        // 修改字段
        if (param.columnChanged()) {
            this.columnHandle(param);
        }
        // 主键变更，应在修改字段后
        if (updatePrimaryKey) {
            this.addPrimaryKey(param);
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
        if (table.hasComment()) {
            this.sqlList.add(this.commentSql(param.getSchema(), param.tableName(), null, table.getComment()));
        }
    }

    /**
     * 触发器处理
     *
     * @param param 表修改参数
     */
    protected void triggerHandle(SqlServerAlertTableParam param) {
        DBObjects<SqlServerTrigger> triggers = param.getTriggers();
        String schema = param.getSchema();
        String tableFullName = DBUtil.wrap(schema, param.tableName(), DBDialect.SQLSERVER);
        // 删除、变更的语句先执行，否则可能异常
        for (SqlServerTrigger trigger : triggers) {
            if (DBObjectList.isDeleted(trigger) || DBObjectList.isChanged(trigger)) {
                String triggerName = StringUtil.isBlank(trigger.originalName()) ? trigger.getName() : trigger.originalName();
                if (StringUtil.isNotBlank(triggerName)) {
                    this.sqlList.add("DROP TRIGGER IF EXISTS "
                            + DBUtil.wrap(schema, triggerName, DBDialect.SQLSERVER) + ";");
                }
            }
            if (DBObjectList.isChanged(trigger) || DBObjectList.isCreated(trigger)) {
                // SqlServer触发器必须有名称和触发器函数体
                if (StringUtil.isBlank(trigger.getName()) || StringUtil.isBlank(trigger.getDefinition())) {
                    continue;
                }
                String body = ShellSqlServerHelper.normalizeTriggerBody(trigger.getDefinition(), trigger.getPolicy());
                if (body == null) {
                    continue;
                }
                StringBuilder builder = new StringBuilder();
                String[] policy = this.parseTriggerPolicy(trigger.getPolicy());
                builder.append("CREATE OR ALTER TRIGGER ")
                        .append(DBUtil.wrap(schema, trigger.getName(), DBDialect.SQLSERVER))
                        .append(" ON ").append(tableFullName)
                        .append(" ").append(policy[0]).append(" ").append(policy[1])
                        .append(" AS\n").append(body);
                this.sqlList.add(builder.toString());
            }
        }
    }

    /**
     * 字段处理
     *
     * @param param 表修改参数
     */
    protected void columnHandle(SqlServerAlertTableParam param) {
        StringBuilder builder;
        String fullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.SQLSERVER);

        // 字段重命名，SqlServer每列一条语句
        for (SqlServerColumn column : param.getColumns()) {
            if (SqlServerColumns.isChanged(column) && column.isNameChanged()) {
                builder = new StringBuilder();
                builder.append("EXEC sp_rename ")
                        .append(DBUtil.wrapString(fullName + "." + DBUtil.wrap(
                                column.originalName(), DBDialect.SQLSERVER), DBDialect.SQLSERVER))
                        .append(", ")
                        .append(DBUtil.wrapString(column.getName(), DBDialect.SQLSERVER))
                        .append(", N'COLUMN';");
                this.sqlList.add(builder.toString());
            }
        }

        // 删除字段，SqlServer不支持在一条语句中混合其他列操作
        for (SqlServerColumn column : param.getColumns()) {
            if (SqlServerColumns.isDeleted(column)) {
                builder = new StringBuilder();
                builder.append("ALTER TABLE ")
                        .append(fullName)
                        .append(" DROP COLUMN ")
                        .append(DBUtil.wrap(column.getName(), DBDialect.SQLSERVER))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 修改字段
        for (SqlServerColumn column : param.getColumns()) {
            if (SqlServerColumns.isChanged(column)) {
                if (column.requiresGeneratedColumnRebuild()) {
                    String originalColumn = StringUtil.blankToDefault(
                            column.originalName(), column.getName());
                    this.sqlList.add("ALTER TABLE " + fullName + " DROP COLUMN "
                            + DBUtil.wrap(originalColumn, DBDialect.SQLSERVER) + ";");
                    builder = new StringBuilder("ALTER TABLE ")
                            .append(fullName).append(" ADD ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.SQLSERVER));
                    this.appendColumnType(builder, column, true);
                    builder.append(";");
                    this.sqlList.add(builder.toString());
                    continue;
                }
                boolean isIdentity = column.isAutoIncrement();
                boolean typeChanged = !StringUtil.equalsIgnoreCase(
                        column.getTypeOriginal(), column.getType())
                        || column.isSizeChanged()
                        || column.isDigitsChanged();
                if (typeChanged) {
                    builder = new StringBuilder();
                    builder.append("ALTER TABLE ")
                            .append(fullName)
                            .append(" ALTER COLUMN ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.SQLSERVER))
                            .append(" ");
                    this.appendColumnTypeOnly(builder, column);
                    builder.append(column.isNullable() ? " NULL" : " NOT NULL");
                    builder.append(";");
                    this.sqlList.add(builder.toString());
                }
                boolean dropDefault = !isIdentity && column.isDefaultValueChanged();
                if (dropDefault) {
                    this.sqlList.add("DECLARE @DF SYSNAME; SELECT @DF = DC.NAME "
                            + "FROM SYS.DEFAULT_CONSTRAINTS DC JOIN SYS.COLUMNS C "
                            + "ON C.OBJECT_ID = DC.PARENT_OBJECT_ID AND C.COLUMN_ID = DC.PARENT_COLUMN_ID "
                            + "WHERE DC.PARENT_OBJECT_ID = OBJECT_ID(N'"
                            + fullName.replace("'", "''") + "') AND C.NAME = N'"
                            + column.getName().replace("'", "''") + "'; "
                            + "IF @DF IS NOT NULL EXEC('ALTER TABLE " + fullName.replace("'", "''")
                            + " DROP CONSTRAINT ' + QUOTENAME(@DF));");
                    Object defaultValue = column.getDefaultValueFix();
                    if (!isIdentity && defaultValue != null) {
                        this.sqlList.add("ALTER TABLE " + fullName + " ADD DEFAULT " + defaultValue
                                + " FOR " + DBUtil.wrap(column.getName(), DBDialect.SQLSERVER) + ";");
                    }
                }
                // 修改非空约束：仅在可空属性真正变化时生成语句
                if (column.isNullableChanged()) {
                    builder = new StringBuilder();
                    builder.append("ALTER TABLE ")
                            .append(fullName)
                            .append(" ALTER COLUMN ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.SQLSERVER))
                            .append(" ");
                    this.appendColumnTypeOnly(builder, column);
                    builder.append(column.isNullable() ? " NULL;" : " NOT NULL;");
                    this.sqlList.add(builder.toString());
                }
                // SQL Server不支持在ALTER COLUMN中追加IDENTITY，
                // 因此界面仅允许新列选择IDENTITY。
            }
        }

        // 新增字段放在既有字段修改之后，避免新增计算列依赖的列尚未完成类型变更
        for (SqlServerColumn column : param.getColumns()) {
            if (SqlServerColumns.isCreated(column)) {
                builder = new StringBuilder();
                builder.append("ALTER TABLE ")
                        .append(fullName)
                        .append(" ADD ")
                        .append(DBUtil.wrap(column.getName(), DBDialect.SQLSERVER));
                this.appendColumnType(builder, column, true);
                builder.append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 注释
        for (SqlServerColumn column : param.getColumns()) {
            if (SqlServerColumns.isChanged(column) || SqlServerColumns.isCreated(column)) {
                if (column.hasComment() && (column.isCommentChanged() || SqlServerColumns.isCreated(column))) {
                    this.sqlList.add(this.commentSql(param.getSchema(), param.tableName(),
                            column.getName(), column.getComment()));
                }
            }
        }
    }

    /**
     * 主键处理
     *
     * @param param 表修改参数
     */
    protected void primaryKeyHandle(SqlServerAlertTableParam param) {
        this.dropPrimaryKey(param);
        this.addPrimaryKey(param);
    }

    /**
     * 删除主键约束
     *
     * @param param 表修改参数
     */
    private void dropPrimaryKey(SqlServerAlertTableParam param) {
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.SQLSERVER);
        // 删除主键，使用原主键名称
        if (param.isExistPrimaryKey()) {
            for (String primaryKey : param.getPrimaryKeys()) {
                if (StringUtil.isBlank(primaryKey)) {
                    continue;
                }
                this.sqlList.add("ALTER TABLE " + tableFullName + " DROP CONSTRAINT "
                        + DBUtil.wrap(primaryKey, DBDialect.SQLSERVER) + ";");
            }
        }
    }

    /**
     * 新增主键约束
     *
     * @param param 表修改参数
     */
    private void addPrimaryKey(SqlServerAlertTableParam param) {
        int len;
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.SQLSERVER);
        String primaryKeyName = param.isExistPrimaryKey()
                ? param.getPrimaryKeys().stream()
                        .filter(StringUtil::isNotBlank)
                        .findFirst()
                        .orElse(null)
                : null;
        // 新增主键
        builder.append("ALTER TABLE ").append(tableFullName).append(" ");
        builder.append(" ADD ");
        if (StringUtil.isNotBlank(primaryKeyName)) {
            builder.append("CONSTRAINT ")
                    .append(DBUtil.wrap(primaryKeyName, DBDialect.SQLSERVER))
                    .append(" ");
        }
        builder.append("PRIMARY KEY (");
        len = builder.length();
        for (SqlServerColumn column : param.primaryKeys()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.SQLSERVER));
            builder.append(",");
        }
        StringUtil.deleteLast(builder, ",");
        if (len != builder.length()) {
            builder.append(");");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 判断是否需要先移除主键才能修改其字段定义
     *
     * @param param 表修改参数
     * @return 是否需要重建主键
     */
    private boolean primaryKeyColumnNeedsAlter(SqlServerAlertTableParam param) {
        if (!param.isExistPrimaryKey()) {
            return false;
        }
        for (SqlServerColumn column : param.primaryKeys()) {
            if (SqlServerColumns.isChanged(column) && (column.requiresGeneratedColumnRebuild()
                    || !StringUtil.equalsIgnoreCase(column.getTypeOriginal(), column.getType())
                    || column.isSizeChanged()
                    || column.isDigitsChanged()
                    || column.isNullableChanged())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 索引处理
     *
     * @param param 表修改参数
     */
    protected void indexHandle(SqlServerAlertTableParam param) {
        DBObjects<SqlServerIndex> indexes = param.getIndexes();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.SQLSERVER);

        // 删除索引
        for (SqlServerIndex index : indexes) {
            // 索引删除用独立DROP INDEX语句，SqlServer支持模式限定索引名
            if (DBObjectList.isDeleted(index) || DBObjectList.isChanged(index)) {
                String indexName = StringUtil.isBlank(index.originalName()) ? index.getName() : index.originalName();
                if (StringUtil.isNotBlank(indexName)) {
                    StringBuilder builder = new StringBuilder("DROP INDEX ");
                    builder.append(DBUtil.wrap(indexName, DBDialect.SQLSERVER))
                            .append(" ON ").append(tableFullName)
                            .append(";");
                    this.sqlList.add(builder.toString());
                }
            }
        }

        // 新增、编辑索引
        for (SqlServerIndex index : indexes) {
            // 索引新增用独立CREATE INDEX语句
            if (DBObjectList.isCreated(index) || DBObjectList.isChanged(index)) {
                StringBuilder builder = new StringBuilder("CREATE ");
                if (StringUtil.equalsIgnoreCase(index.typeName(), "UNIQUE")) {
                    builder.append("UNIQUE ");
                }
                if (StringUtil.isNotBlank(index.getMethod())) {
                    builder.append(index.getMethod()).append(" ");
                }
                builder.append("INDEX ")
                        .append(DBUtil.wrap(index.getName(), DBDialect.SQLSERVER))
                        .append(" ON ").append(tableFullName);
                builder.append(" (");
                for (SqlServerIndex.IndexColumn column : index.getColumns()) {
                    builder.append(DBUtil.wrap(column.getColumnName(), DBDialect.SQLSERVER));
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
    protected void foreignKeyHandle(SqlServerAlertTableParam param) {
        DBObjects<SqlServerForeignKey> foreignKeys = param.getForeignKeys();
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.SQLSERVER);

        // 删除外键
        for (SqlServerForeignKey foreignKey : foreignKeys) {
            if (DBObjectList.isDeleted(foreignKey) || DBObjectList.isChanged(foreignKey)) {
                String fkName = foreignKey.originalName();
                // 名称为null是临时数据
                if (StringUtil.isNotBlank(fkName)) {
                    StringUtil.clear(builder);
                    builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                    builder.append(" DROP CONSTRAINT ")
                            .append(DBUtil.wrap(foreignKey.originalName(), DBDialect.SQLSERVER))
                            .append(";");
                    this.sqlList.add(builder.toString());
                }
            }
        }

        // 新增外键
        for (SqlServerForeignKey foreignKey : foreignKeys) {
            if (DBObjectList.isCreated(foreignKey) || DBObjectList.isChanged(foreignKey)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append(" ADD CONSTRAINT ")
                        .append(DBUtil.wrap(foreignKey.getName(), DBDialect.SQLSERVER))
                        .append(" FOREIGN KEY (");
                for (String column : foreignKey.getColumns()) {
                    builder.append(DBUtil.wrap(column, DBDialect.SQLSERVER)).append(",");
                }
                StringUtil.deleteLast(builder, ",");
                builder.append(")")
                        .append(" REFERENCES ")
                        .append(DBUtil.wrap(foreignKey.getReferencedDatabase(), foreignKey.getReferencedTable(), DBDialect.SQLSERVER))
                        .append(" (");
                for (String column : foreignKey.getReferencedColumns()) {
                    builder.append(DBUtil.wrap(column, DBDialect.SQLSERVER)).append(",");
                }
                StringUtil.deleteLast(builder, ",");
                builder.append(")");
                // SqlServer 支持 ON UPDATE / ON DELETE 策略
                if (StringUtil.isNotBlank(foreignKey.getUpdatePolicy())) {
                    builder.append(" ON UPDATE ").append(foreignKey.getUpdatePolicy());
                }
                if (StringUtil.isNotBlank(foreignKey.getDeletePolicy())) {
                    builder.append(" ON DELETE ").append(foreignKey.getDeletePolicy());
                }
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
    protected void checkHandle(SqlServerAlertTableParam param) {
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.SQLSERVER);
        DBObjects<SqlServerCheck> checks = param.getChecks();

        // 删除检查
        for (SqlServerCheck check : checks) {
            if (DBObjectList.isDeleted(check) || DBObjectList.isChanged(check)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append("DROP CONSTRAINT ")
                        .append(DBUtil.wrap(check.originalName(), DBDialect.SQLSERVER))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 新增、编辑检查
        for (SqlServerCheck check : checks) {
            if (DBObjectList.isCreated(check) || DBObjectList.isChanged(check)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append(" ADD CONSTRAINT ")
                        .append(DBUtil.wrap(check.getName(), DBDialect.SQLSERVER))
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
    private void appendColumnType(StringBuilder builder, SqlServerColumn column, boolean created) {
        if (column.isGenerated()) {
            builder.append(" AS (")
                    .append(column.getGeneratedExpression())
                    .append(")");
            if (StringUtil.equalsIgnoreCase(column.getGeneratedStorage(), "PERSISTED")) {
                builder.append(" PERSISTED");
            }
            return;
        }
        this.appendColumnTypeOnly(builder, column);
        boolean isIdentity = column.supportAutoIncrement() && column.isAutoIncrement();
        if (!isIdentity && column.getDefaultValueFix() != null) {
            builder.append(" DEFAULT ").append(column.getDefaultValueFix());
        }
        if (column.isNullable()) {
            builder.append(" NULL");
        } else {
            builder.append(" NOT NULL");
        }
        if (isIdentity && created) {
            builder.append(" ").append(this.identityClause(column));
        }
    }

    /**
     * 追加列类型与长度定义
     *
     * @param builder SQL构建器
     * @param column           列
     */
    private void appendColumnTypeOnly(StringBuilder builder, SqlServerColumn column) {
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
    }

    /**
     * 获取自增列定义
     *
     * @param column 字段
     * @return 自增列定义
     */
    private String identityClause(SqlServerColumn column) {
        return "IDENTITY(1,1)";
    }

    private String commentSql(String schema, String tableName, String columnName, Object comment) {
        String sql = "IF EXISTS (SELECT 1 FROM SYS.EXTENDED_PROPERTIES WHERE NAME=N'MS_Description' "
                + "AND MAJOR_ID=OBJECT_ID(N'" + DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER)
                .replace("'", "''") + "')"
                + (columnName == null ? " AND MINOR_ID=0"
                : " AND MINOR_ID=COLUMNPROPERTY(OBJECT_ID(N'"
                + DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER).replace("'", "''")
                + "'),N'" + columnName.replace("'", "''") + "','ColumnId')")
                + ") EXEC sys.sp_updateextendedproperty "
                + "@name=N'MS_Description', @value="
                + "N" + DBUtil.wrapString(String.valueOf(comment), DBDialect.SQLSERVER)
                + ", @level0type=N'SCHEMA', @level0name="
                + "N" + DBUtil.wrapString(schema, DBDialect.SQLSERVER)
                + ", @level1type=N'TABLE', @level1name="
                + "N" + DBUtil.wrapString(tableName, DBDialect.SQLSERVER)
                + (columnName == null ? "" : ", @level2type=N'COLUMN', @level2name="
                + "N" + DBUtil.wrapString(columnName, DBDialect.SQLSERVER))
                + "; ELSE EXEC sys.sp_addextendedproperty "
                + "@name=N'MS_Description', @value="
                + "N" + DBUtil.wrapString(String.valueOf(comment), DBDialect.SQLSERVER)
                + ", @level0type=N'SCHEMA', @level0name="
                + "N" + DBUtil.wrapString(schema, DBDialect.SQLSERVER)
                + ", @level1type=N'TABLE', @level1name="
                + "N" + DBUtil.wrapString(tableName, DBDialect.SQLSERVER)
                + (columnName == null ? "" : ", @level2type=N'COLUMN', @level2name="
                + "N" + DBUtil.wrapString(columnName, DBDialect.SQLSERVER))
                + ";";
        return sql;
    }

    /**
     * 解析触发器策略为触发时机与触发事件
     *
     * @param policy 触发器策略
     * @return 索引0为触发时机，索引1为触发事件
     */
    private String[] parseTriggerPolicy(String policy) {
        String timing = "AFTER";
        String events = StringUtil.isBlank(policy) ? "INSERT" : policy.trim();
        String upper = events.toUpperCase();
        if (upper.startsWith("INSTEAD OF ")) {
            timing = "INSTEAD OF";
            events = events.substring("INSTEAD OF ".length()).trim();
        } else if (upper.startsWith("BEFORE ")) {
            timing = "BEFORE";
            events = events.substring("BEFORE ".length()).trim();
        } else if (upper.startsWith("AFTER ")) {
            timing = "AFTER";
            events = events.substring("AFTER ".length()).trim();
        }
        if (StringUtil.isBlank(events)) {
            events = "INSERT";
        }
        return new String[]{timing, events};
    }

    /**
     * 生成表修改SQL列表
     *
     * @param param 表修改参数
     * @return SQL列表
     */
    public List<String> generate(SqlServerAlertTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条表修改SQL
     *
     * @param param 表修改参数
     * @return 单条SQL
     */
    public String generateSingle(SqlServerAlertTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成表修改SQL列表
     *
     * @param param 表修改参数
     * @return SQL列表
     */
    public static List<String> generateSql(SqlServerAlertTableParam param) {
        return new SqlServerTableAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条表修改SQL
     *
     * @param param 表修改参数
     * @return 单条SQL
     */
    public static String generateSqlSingle(SqlServerAlertTableParam param) {
        return new SqlServerTableAlertSqlGenerator().generateSingle(param);
    }

}
