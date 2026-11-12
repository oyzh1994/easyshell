package cn.oyzh.easyshell.postgresql.generator.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlHelper;
import cn.oyzh.easyshell.postgresql.check.PostgresqlCheck;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.foreignKey.PostgresqlForeignKey;
import cn.oyzh.easyshell.postgresql.index.PostgresqlIndex;
import cn.oyzh.easyshell.postgresql.table.PostgresqlCreateTableParam;
import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.easyshell.postgresql.trigger.PostgresqlTrigger;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * PostgreSQL建表SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlTableCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 变更标志位
     */
    private boolean changeFlag;

    /**
     * 执行SQL生成，依次处理字段、主键、索引、外键、检查、表空间、触发器和表注释
     *
     * @param param 建表参数
     */
    private void _generate(PostgresqlCreateTableParam param) {
        String schema = param.schema();
        PostgresqlTable table = param.getTable();
        String tableName = param.tableName();
        String fullName = DBUtil.wrap(schema, tableName, DBDialect.POSTGRESQL);
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
        // PostgreSQL表空间，位于建表语句末尾
        if (table.hasTableSpace()) {
            this.sqlBuilder.append(" TABLESPACE ")
                    .append(DBUtil.wrap(table.getTableSpace(), DBDialect.POSTGRESQL));
        }
        this.sqlBuilder.append(";");
        // 表触发器
        if (param.hasTrigger()) {
            this.triggerHandle(param);
        }
        // PostgreSQL表注释
        if (StringUtil.isNotBlank(table.getComment())) {
            StringBuilder builder = new StringBuilder();
            builder.append("COMMENT ON TABLE ")
                    .append(fullName)
                    .append(" IS ")
                    .append(DBUtil.wrapData(table.getComment(), DBDialect.POSTGRESQL)).append(";");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 追加表触发器定义SQL
     *
     * @param param 建表参数
     */
    protected void triggerHandle(PostgresqlCreateTableParam param) {
        DBObjects<PostgresqlTrigger> triggers = param.getTriggers();
        String schema = param.schema();
        String tableFullName = DBUtil.wrap(schema, param.tableName(), DBDialect.POSTGRESQL);
        for (PostgresqlTrigger trigger : triggers) {
            // PostgreSQL触发器必须有名称和触发器函数体
            if (StringUtil.isBlank(trigger.getName()) || StringUtil.isBlank(trigger.getDefinition())) {
                continue;
            }
            String functionName = param.tableName() + "_" + trigger.getName() + "_fn";
            String functionFullName = DBUtil.wrap(schema, functionName, DBDialect.POSTGRESQL);
            String body = ShellPostgresqlHelper.normalizeTriggerBody(trigger.getDefinition(), trigger.getPolicy());
            if (body == null) {
                continue;
            }
            // PostgreSQL触发器函数
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE OR REPLACE FUNCTION ")
                    .append(functionFullName)
                    .append("() RETURNS trigger LANGUAGE plpgsql AS $$\n")
                    .append(body)
                    .append("\n$$;");
            this.sqlList.add(builder.toString());
            // PostgreSQL触发器
            String[] policy = this.parseTriggerPolicy(trigger.getPolicy());
            builder = new StringBuilder();
            builder.append("CREATE TRIGGER ")
                    .append(DBUtil.wrap(trigger.getName(), DBDialect.POSTGRESQL))
                    .append(" ")
                    .append(policy[0])
                    .append(" ")
                    .append(policy[1])
                    .append(" ON ")
                    .append(tableFullName)
                    .append(" FOR EACH ROW EXECUTE FUNCTION ")
                    .append(functionFullName)
                    .append("();");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 追加字段定义，并生成列注释SQL
     *
     * @param builder SQL构建器
     * @param param   建表参数
     */
    protected void columnHandle(StringBuilder builder, PostgresqlCreateTableParam param) {
        for (PostgresqlColumn column : param.getColumns()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL));
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

            // PostgreSQL自增列使用 IDENTITY（SERIAL 系列类型本身即自增，不能再叠加 IDENTITY）
            boolean isIdentity = column.supportAutoIncrement() && column.isAutoIncrement()
                    && !this.isSerialType(column.getType());

            // 默认值（PostgreSQL自增列不能有DEFAULT约束）
            if (!isIdentity && column.getDefaultValueFix() != null) {
                builder.append(" DEFAULT ").append(column.getDefaultValueFix());
            }

            // 可为null
            if (column.isNullable()) {
                builder.append(" NULL");
            } else {
                builder.append(" NOT NULL");
            }

            // PostgreSQL自增列统一使用 GENERATED ... AS IDENTITY
            if (isIdentity) {
                builder.append(" ").append(this.identityClause(column));
            }

            // PostgreSQL列注释使用独立COMMENT ON COLUMN语句
            if (StringUtil.isNotBlank(column.getComment())) {
                StringBuilder commentBuilder = new StringBuilder();
                commentBuilder.append("COMMENT ON COLUMN ")
                        .append(DBUtil.wrap(param.schema(), param.tableName(), DBDialect.POSTGRESQL))
                        .append(".")
                        .append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL))
                        .append(" IS ")
                        .append(DBUtil.wrapData(column.getComment(), DBDialect.POSTGRESQL))
                        .append(";");
                this.sqlList.add(commentBuilder.toString());
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
    protected void primaryKeyHandle(StringBuilder builder, PostgresqlCreateTableParam param) {
        builder.append(" PRIMARY KEY (");
        for (PostgresqlColumn column : param.primaryKeys()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL))
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
    protected void indexHandle(PostgresqlCreateTableParam param) {
        DBObjects<PostgresqlIndex> indexes = param.getIndexes();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.POSTGRESQL);

        // 新增、编辑索引
        for (PostgresqlIndex index : indexes) {
            // 索引新增用独立CREATE INDEX语句
            if (DBObjectList.isCreated(index) || DBObjectList.isChanged(index)) {
                StringBuilder builder = new StringBuilder("CREATE ");
                // PostgreSQL仅支持 UNIQUE 前缀，FULLTEXT/SPATIAL 等类型忽略
                if (StringUtil.equalsIgnoreCase(index.typeName(), "UNIQUE")) {
                    builder.append("UNIQUE ");
                }
                builder.append("INDEX ")
                        .append(DBUtil.wrap(index.getName(), DBDialect.POSTGRESQL))
                        .append(" ON ").append(tableFullName);
                // PostgreSQL索引方法（BTREE/HASH/GIN/GIST/BRIN/SPGIST）
                if (StringUtil.isNotBlank(index.methodName())) {
                    builder.append(" USING ").append(index.methodName());
                }
                builder.append(" (");
                for (PostgresqlIndex.IndexColumn column : index.getColumns()) {
                    builder.append(DBUtil.wrap(column.getColumnName(), DBDialect.POSTGRESQL));
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
    protected void foreignKeyHandle(StringBuilder builder, PostgresqlCreateTableParam param) {
        DBObjects<PostgresqlForeignKey> foreignKeys = param.getForeignKeys();
        for (PostgresqlForeignKey foreignKey : foreignKeys) {
            builder.append(" CONSTRAINT ")
                    .append(DBUtil.wrap(foreignKey.getName(), DBDialect.POSTGRESQL))
                    .append(" FOREIGN KEY (");
            for (String column : foreignKey.getColumns()) {
                builder.append(DBUtil.wrap(column, DBDialect.POSTGRESQL)).append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append(")")
                    .append(" REFERENCES ")
                    .append(DBUtil.wrap(foreignKey.getReferencedDatabase(), foreignKey.getReferencedTable(), DBDialect.POSTGRESQL))
                    .append(" (");
            for (String column : foreignKey.getReferencedColumns()) {
                builder.append(DBUtil.wrap(column, DBDialect.POSTGRESQL)).append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append(")");
            // PostgreSQL 支持 ON UPDATE / ON DELETE 策略
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
    protected void checkHandle(StringBuilder builder, PostgresqlCreateTableParam table) {
        DBObjects<PostgresqlCheck> checks = table.getChecks();
        for (PostgresqlCheck check : checks) {
            builder.append(" CONSTRAINT ")
                    .append(DBUtil.wrap(check.getName(), DBDialect.POSTGRESQL))
                    .append(" CHECK (")
                    .append(check.getClause())
                    .append(")");
            // 拼接,
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    /**
     * 是否为SERIAL系列类型
     *
     * @param type 类型
     * @return 结果
     */
    private boolean isSerialType(String type) {
        return StringUtil.equalsAnyIgnoreCase(type, "SERIAL", "BIGSERIAL", "SMALLSERIAL",
                "SERIAL2", "SERIAL4", "SERIAL8");
    }

    /**
     * 获取自增列定义
     *
     * @param column 字段
     * @return 自增列定义
     */
    private String identityClause(PostgresqlColumn column) {
        String autoIncrementType = column.getAutoIncrementType();
        if (StringUtil.isNotBlank(autoIncrementType) && StringUtil.containsIgnoreCase(autoIncrementType, "ALWAYS")) {
            return "GENERATED ALWAYS AS IDENTITY";
        }
        return "GENERATED BY DEFAULT AS IDENTITY";
    }

    /**
     * 解析触发器策略为触发时机与触发事件
     *
     * @param policy 触发器策略
     * @return 索引0为触发时机，索引1为触发事件
     */
    private String[] parseTriggerPolicy(String policy) {
        String timing = "BEFORE";
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
    public List<String> generate(PostgresqlCreateTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条建表SQL
     *
     * @param param 建表参数
     * @return 单条SQL
     */
    public String generateSingle(PostgresqlCreateTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成建表SQL列表
     *
     * @param param 建表参数
     * @return SQL列表
     */
    public static List<String> generateSql(PostgresqlCreateTableParam param) {
        return new PostgresqlTableCreateSqlGenerator().generate(param);
    }

    /**
     * 生成单条建表SQL
     *
     * @param param 建表参数
     * @return 单条SQL
     */
    public static String generateSqlSingle(PostgresqlCreateTableParam param) {
        return new PostgresqlTableCreateSqlGenerator().generateSingle(param);
    }
}
