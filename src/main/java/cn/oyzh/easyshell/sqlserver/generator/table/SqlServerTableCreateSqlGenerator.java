package cn.oyzh.easyshell.sqlserver.generator.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerHelper;
import cn.oyzh.easyshell.sqlserver.check.SqlServerCheck;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.foreignKey.SqlServerForeignKey;
import cn.oyzh.easyshell.sqlserver.index.SqlServerIndex;
import cn.oyzh.easyshell.sqlserver.table.SqlServerCreateTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.trigger.SqlServerTrigger;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * SqlServer建表SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerTableCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 变更标志位
     */
    private boolean changeFlag;

    /**
     * 执行SQL生成，依次处理字段、主键、索引、外键、检查、表空间、触发器和表注释
     *
     * @param param 建表参数
     */
    private void _generate(SqlServerCreateTableParam param) {
        String schema = param.schema();
        SqlServerTable table = param.getTable();
        String tableName = param.tableName();
        String fullName = DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER);
        this.sqlBuilder.append("CREATE TABLE ")
                .append(fullName)
                .append(" ( \n");
        // 字段
        if (param.hasColumns()) {
            this.columnHandle(this.sqlBuilder, param);
        }
        // 主键
        if (CollectionUtil.isNotEmpty(param.primaryKeys())) {
            this.primaryKeyHandle(this.sqlBuilder, param);
        }
        // 索引
        if (param.hasIndex()) {
            this.indexHandle(param);
        }
        // 外键
        if (param.hasForeignKey()) {
            this.foreignKeyHandle(this.sqlBuilder, param);
        }
        // 检查
        if (param.hasCheck()) {
            this.checkHandle(this.sqlBuilder, param);
        }
        // 删除最后一个字符
        if (this.changeFlag) {
            StringUtil.deleteLast(this.sqlBuilder, ",");
            this.changeFlag = false;
        }
        this.sqlBuilder.append("\n)");
        // SQL Server 文件组，位于建表语句末尾
        if (table.hasTableSpace()) {
            this.sqlBuilder.append(" ON ")
                    .append(DBUtil.wrap(table.getTableSpace(), DBDialect.SQLSERVER));
        }
        this.sqlBuilder.append(";");
        // 表触发器
        if (param.hasTrigger()) {
            this.triggerHandle(param);
        }
        // SQL Server 表注释
        if (StringUtil.isNotBlank(table.getComment())) {
            this.sqlList.add(this.commentSql(schema, tableName, null, table.getComment()));
        }
    }

    /**
     * 追加表触发器定义SQL
     *
     * @param param 建表参数
     */
    protected void triggerHandle(SqlServerCreateTableParam param) {
        DBObjects<SqlServerTrigger> triggers = param.getTriggers();
        String schema = param.schema();
        String tableFullName = DBUtil.wrap(schema, param.tableName(), DBDialect.SQLSERVER);
        for (SqlServerTrigger trigger : triggers) {
            // SQL Server 触发器直接包含动作语句
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
                    .append(DBUtil.wrap(trigger.getName(), DBDialect.SQLSERVER))
                    .append(" ON ")
                    .append(tableFullName)
                    .append(" ")
                    .append(policy[0])
                    .append(" ")
                    .append(policy[1])
                    .append(" AS\n")
                    .append(body);
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 追加字段定义，并生成列注释SQL
     *
     * @param builder SQL构建器
     * @param param   建表参数
     */
    protected void columnHandle(StringBuilder builder, SqlServerCreateTableParam param) {
        for (SqlServerColumn column : param.getColumns()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.SQLSERVER));
            if (column.isGenerated()) {
                builder.append(" AS (")
                        .append(column.getGeneratedExpression())
                        .append(")");
                if (StringUtil.equalsIgnoreCase(column.getGeneratedStorage(), "PERSISTED")) {
                    builder.append(" PERSISTED");
                }
                if (StringUtil.isNotBlank(column.getComment())) {
                    this.sqlList.add(this.commentSql(
                            param.schema(), param.tableName(), column.getName(), column.getComment()));
                }
                builder.append(",\n");
                this.changeFlag = true;
                continue;
            }
            builder.append(" ").append(column.getType());

            // 字段长度
            if (column.supportSize() && column.getSize() != null) {
                builder.append("(").append(column.getSize());
                // 小数位
                if (column.supportDigits() && column.getDigits() != null) {
                    builder.append(",").append(column.getDigits());
                }
                builder.append(")");
            } else if (column.supportValue() && column.getValue() != null) {
                builder.append("(").append(column.getValue()).append(")");
            }

            // SQL Server 自增列使用 IDENTITY
            boolean isIdentity = column.supportAutoIncrement() && column.isAutoIncrement();

            // 默认值（SqlServer自增列不能有DEFAULT约束）
            if (!isIdentity && column.getDefaultValueFix() != null) {
                builder.append(" DEFAULT ").append(column.getDefaultValueFix());
            }

            // 自增定义必须位于 NULL 约束之前
            if (isIdentity) {
                builder.append(" ").append(this.identityClause(column));
            }

            // 可为null
            if (column.isNullable()) {
                builder.append(" NULL");
            } else {
                builder.append(" NOT NULL");
            }

            // SQL Server 列注释使用扩展属性
            if (StringUtil.isNotBlank(column.getComment())) {
                this.sqlList.add(this.commentSql(
                        param.schema(), param.tableName(), column.getName(), column.getComment()));
            }
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    /**
     * 追加主键定义
     *
     * @param builder SQL构建器
     * @param param   建表参数
     */
    protected void primaryKeyHandle(StringBuilder builder, SqlServerCreateTableParam param) {
        builder.append(" PRIMARY KEY (");
        for (SqlServerColumn column : param.primaryKeys()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.SQLSERVER))
                    .append(",");
        }
        StringUtil.deleteLast(builder, ",");
        builder.append("),\n");
        this.changeFlag = true;
    }

    /**
     * 索引处理
     *
     * @param param 表创建参数
     */
    protected void indexHandle(SqlServerCreateTableParam param) {
        DBObjects<SqlServerIndex> indexes = param.getIndexes();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.SQLSERVER);

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
     * 追加外键定义
     *
     * @param builder SQL构建器
     * @param param   建表参数
     */
    protected void foreignKeyHandle(StringBuilder builder, SqlServerCreateTableParam param) {
        DBObjects<SqlServerForeignKey> foreignKeys = param.getForeignKeys();
        for (SqlServerForeignKey foreignKey : foreignKeys) {
            builder.append(" CONSTRAINT ")
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
            builder.append(",");
            this.changeFlag = true;
        }
    }

    /**
     * 追加检查约束定义
     *
     * @param builder SQL构建器
     * @param table   建表参数
     */
    protected void checkHandle(StringBuilder builder, SqlServerCreateTableParam table) {
        DBObjects<SqlServerCheck> checks = table.getChecks();
        for (SqlServerCheck check : checks) {
            builder.append(" CONSTRAINT ")
                    .append(DBUtil.wrap(check.getName(), DBDialect.SQLSERVER))
                    .append(" CHECK (")
                    .append(check.getClause())
                    .append(")");
            // 拼接,
            builder.append(",\n");
            this.changeFlag = true;
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
        String objectName = columnName == null
                ? DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER)
                : DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER)
                + "." + DBUtil.wrap(columnName, DBDialect.SQLSERVER);
        return "EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value="
                + "N" + DBUtil.wrapString(String.valueOf(comment), DBDialect.SQLSERVER)
                + ", @level0type=N'SCHEMA', @level0name="
                + "N" + DBUtil.wrapString(schema, DBDialect.SQLSERVER)
                + ", @level1type=N'TABLE', @level1name="
                + "N" + DBUtil.wrapString(tableName, DBDialect.SQLSERVER)
                + (columnName == null ? "" : ", @level2type=N'COLUMN', @level2name="
                + "N" + DBUtil.wrapString(columnName, DBDialect.SQLSERVER))
                + ";";
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
     * 生成建表SQL列表
     *
     * @param param 建表参数
     * @return SQL列表
     */
    public List<String> generate(SqlServerCreateTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条建表SQL
     *
     * @param param 建表参数
     * @return 单条SQL
     */
    public String generateSingle(SqlServerCreateTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成建表SQL列表
     *
     * @param param 建表参数
     * @return SQL列表
     */
    public static List<String> generateSql(SqlServerCreateTableParam param) {
        return new SqlServerTableCreateSqlGenerator().generate(param);
    }

    /**
     * 生成单条建表SQL
     *
     * @param param 建表参数
     * @return 单条SQL
     */
    public static String generateSqlSingle(SqlServerCreateTableParam param) {
        return new SqlServerTableCreateSqlGenerator().generateSingle(param);
    }
}
