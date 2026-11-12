package cn.oyzh.easyshell.postgresql.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlHelper;
import cn.oyzh.easyshell.postgresql.check.PostgresqlCheck;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.foreignKey.PostgresqlForeignKey;
import cn.oyzh.easyshell.postgresql.index.PostgresqlIndex;
import cn.oyzh.easyshell.postgresql.table.PostgresqlAlertTableParam;
import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.easyshell.postgresql.trigger.PostgresqlTrigger;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * PostgreSQL表结构修改SQL生成器，根据表变更参数生成对应的 ALTER TABLE 等语句
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlTableAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 执行SQL生成，依次处理主键、字段、索引、外键、检查、触发器、表空间和表注释的变更
     *
     * @param param 表修改参数
     */
    private void _generate(PostgresqlAlertTableParam param) {
        PostgresqlTable table = param.getTable();
        String fullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.POSTGRESQL);
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
                    .append(" SET TABLESPACE ")
                    .append(DBUtil.wrap(table.getTableSpace(), DBDialect.POSTGRESQL)).append(";");
            this.sqlList.add(builder.toString());
        }
        // 修改表注释
        if (table.hasComment()) {
            StringBuilder builder = new StringBuilder();
            builder.append("COMMENT ON TABLE ")
                    .append(fullName)
                    .append(" IS ")
                    .append(DBUtil.wrapData(table.getComment(), DBDialect.POSTGRESQL)).append(";");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 触发器处理
     *
     * @param param 表修改参数
     */
    protected void triggerHandle(PostgresqlAlertTableParam param) {
        DBObjects<PostgresqlTrigger> triggers = param.getTriggers();
        String schema = param.getSchema();
        String tableFullName = DBUtil.wrap(schema, param.tableName(), DBDialect.POSTGRESQL);
        // 删除、变更的语句先执行，否则可能异常
        for (PostgresqlTrigger trigger : triggers) {
            if (DBObjectList.isDeleted(trigger) || DBObjectList.isChanged(trigger)) {
                String triggerName = StringUtil.isBlank(trigger.originalName()) ? trigger.getName() : trigger.originalName();
                if (StringUtil.isNotBlank(triggerName)) {
                    String functionFullName = DBUtil.wrap(schema, param.tableName() + "_" + triggerName + "_fn", DBDialect.POSTGRESQL);
                    // 先删除触发器，再删除触发器函数
                    StringBuilder builder = new StringBuilder();
                    builder.append("DROP TRIGGER ")
                            .append(DBUtil.wrap(triggerName, DBDialect.POSTGRESQL))
                            .append(" ON ")
                            .append(tableFullName)
                            .append(";");
                    this.sqlList.add(builder.toString());
                    builder = new StringBuilder();
                    builder.append("DROP FUNCTION IF EXISTS ")
                            .append(functionFullName)
                            .append("();");
                    this.sqlList.add(builder.toString());
                }
            }
            if (DBObjectList.isChanged(trigger) || DBObjectList.isCreated(trigger)) {
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
    }

    /**
     * 字段处理
     *
     * @param param 表修改参数
     */
    protected void columnHandle(PostgresqlAlertTableParam param) {
        StringBuilder builder;
        String fullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.POSTGRESQL);

        // 字段重命名，PostgreSQL每列一条语句
        for (PostgresqlColumn column : param.getColumns()) {
            if (PostgresqlColumns.isChanged(column) && column.isNameChanged()) {
                builder = new StringBuilder();
                builder.append("ALTER TABLE ")
                        .append(fullName)
                        .append(" RENAME COLUMN ")
                        .append(DBUtil.wrap(column.originalName(), DBDialect.POSTGRESQL))
                        .append(" TO ")
                        .append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 删除字段，PostgreSQL不支持在一条语句中混合其他列操作
        for (PostgresqlColumn column : param.getColumns()) {
            if (PostgresqlColumns.isDeleted(column)) {
                builder = new StringBuilder();
                builder.append("ALTER TABLE ")
                        .append(fullName)
                        .append(" DROP COLUMN ")
                        .append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 新增字段，PostgreSQL每列一条语句
        for (PostgresqlColumn column : param.getColumns()) {
            if (PostgresqlColumns.isCreated(column)) {
                builder = new StringBuilder();
                builder.append("ALTER TABLE ")
                        .append(fullName)
                        .append(" ADD COLUMN ")
                        .append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL));
                this.appendColumnType(builder, column, true);
                builder.append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 修改字段
        for (PostgresqlColumn column : param.getColumns()) {
            if (PostgresqlColumns.isChanged(column)) {
                boolean isIdentity = column.supportAutoIncrement() && column.isAutoIncrement()
                        && !this.isSerialType(column.getType());
                boolean identityChanged = column.isAutoIncrementChanged() || column.isAutoIncrementTypeChanged();
                // 删除原有IDENTITY
                if (param.isExistAutoIncrement() && identityChanged) {
                    builder = new StringBuilder();
                    builder.append("ALTER TABLE ")
                            .append(fullName)
                            .append(" ALTER COLUMN ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL))
                            .append(" DROP IDENTITY IF EXISTS;");
                    this.sqlList.add(builder.toString());
                }
                // 修改字段类型：只有类型、长度或小数位真正变化时才生成 TYPE 语句，
                // 避免仅修改注释/默认值等属性时改写原始字段类型定义。
                boolean typeChanged = column.isTypeChanged()
                        || column.isSizeChanged()
                        || column.isDigitsChanged();
                if (typeChanged) {
                    builder = new StringBuilder();
                    builder.append("ALTER TABLE ")
                            .append(fullName)
                            .append(" ALTER COLUMN ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL))
                            .append(" TYPE");
                    this.appendColumnTypeOnly(builder, column);
                    builder.append(";");
                    this.sqlList.add(builder.toString());
                }
                // 修改默认值，PostgreSQL自增列不能有DEFAULT约束
                if (column.isDefaultValueChanged()) {
                    builder = new StringBuilder();
                    builder.append("ALTER TABLE ")
                            .append(fullName)
                            .append(" ALTER COLUMN ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL));
                    Object defaultValue = column.getDefaultValueFix();
                    if (!isIdentity && defaultValue != null) {
                        builder.append(" SET DEFAULT ").append(defaultValue);
                    } else {
                        builder.append(" DROP DEFAULT");
                    }
                    builder.append(";");
                    this.sqlList.add(builder.toString());
                } else if (identityChanged) {
                    // 切换自增属性时，需清理原有的序列默认值或普通默认值
                    if ((isIdentity && column.getDefaultValue() != null)
                            || (!isIdentity && column.isDefaultValueSequence())) {
                        builder = new StringBuilder();
                        builder.append("ALTER TABLE ")
                                .append(fullName)
                                .append(" ALTER COLUMN ")
                                .append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL))
                                .append(" DROP DEFAULT;");
                        this.sqlList.add(builder.toString());
                    }
                }
                // 修改非空约束：仅在可空属性真正变化时生成语句
                if (column.isNullableChanged()) {
                    builder = new StringBuilder();
                    builder.append("ALTER TABLE ")
                            .append(fullName)
                            .append(" ALTER COLUMN ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL));
                    if (column.isNullable()) {
                        builder.append(" DROP NOT NULL;");
                    } else {
                        builder.append(" SET NOT NULL;");
                    }
                    this.sqlList.add(builder.toString());
                }
                // 新增IDENTITY
                if (isIdentity && identityChanged) {
                    builder = new StringBuilder();
                    builder.append("ALTER TABLE ")
                            .append(fullName)
                            .append(" ALTER COLUMN ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL))
                            .append(" ADD ").append(this.identityClause(column)).append(";");
                    this.sqlList.add(builder.toString());
                }
            }
        }

        // 注释
        for (PostgresqlColumn column : param.getColumns()) {
            if (PostgresqlColumns.isChanged(column) || PostgresqlColumns.isCreated(column)) {
                if (column.hasComment() && (column.isCommentChanged() || PostgresqlColumns.isCreated(column))) {
                    StringBuilder commentBuilder = new StringBuilder();
                    commentBuilder.append("COMMENT ON COLUMN ")
                            .append(DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.POSTGRESQL))
                            .append(".")
                            .append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL))
                            .append(" IS ")
                            .append(DBUtil.wrapData(column.getComment(), DBDialect.POSTGRESQL))
                            .append(";");
                    this.sqlList.add(commentBuilder.toString());
                }
            }
        }
    }

    /**
     * 主键处理
     *
     * @param param 表修改参数
     */
    protected void primaryKeyHandle(PostgresqlAlertTableParam param) {
        int len;
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.POSTGRESQL);

        // 删除主键，使用原主键名称
        if (param.isExistPrimaryKey()) {
            for (String primaryKey : param.getPrimaryKeys()) {
                if (StringUtil.isBlank(primaryKey)) {
                    continue;
                }
                builder = new StringBuilder();
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append(" DROP CONSTRAINT ");
                builder.append(DBUtil.wrap(primaryKey, DBDialect.POSTGRESQL));
                builder.append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 新增主键
        builder = new StringBuilder();
        builder.append("ALTER TABLE ").append(tableFullName).append(" ");
        builder.append(" ADD PRIMARY KEY (");
        len = builder.length();
        for (PostgresqlColumn column : param.primaryKeys()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL));
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
    protected void indexHandle(PostgresqlAlertTableParam param) {
        DBObjects<PostgresqlIndex> indexes = param.getIndexes();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.POSTGRESQL);

        // 删除索引
        for (PostgresqlIndex index : indexes) {
            // 索引删除用独立DROP INDEX语句，PostgreSQL支持模式限定索引名
            if (DBObjectList.isDeleted(index) || DBObjectList.isChanged(index)) {
                String indexName = StringUtil.isBlank(index.originalName()) ? index.getName() : index.originalName();
                if (StringUtil.isNotBlank(indexName)) {
                    StringBuilder builder = new StringBuilder("DROP INDEX ");
                    builder.append(DBUtil.wrap(param.getSchema(), indexName, DBDialect.POSTGRESQL))
                            .append(";");
                    this.sqlList.add(builder.toString());
                }
            }
        }

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
     * 外键处理
     *
     * @param param 表修改参数
     */
    protected void foreignKeyHandle(PostgresqlAlertTableParam param) {
        DBObjects<PostgresqlForeignKey> foreignKeys = param.getForeignKeys();
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.POSTGRESQL);

        // 删除外键
        for (PostgresqlForeignKey foreignKey : foreignKeys) {
            if (DBObjectList.isDeleted(foreignKey) || DBObjectList.isChanged(foreignKey)) {
                String fkName = foreignKey.originalName();
                // 名称为null是临时数据
                if (StringUtil.isNotBlank(fkName)) {
                    StringUtil.clear(builder);
                    builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                    builder.append(" DROP CONSTRAINT ")
                            .append(DBUtil.wrap(foreignKey.originalName(), DBDialect.POSTGRESQL))
                            .append(";");
                    this.sqlList.add(builder.toString());
                }
            }
        }

        // 新增外键
        for (PostgresqlForeignKey foreignKey : foreignKeys) {
            if (DBObjectList.isCreated(foreignKey) || DBObjectList.isChanged(foreignKey)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append(" ADD CONSTRAINT ")
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
    protected void checkHandle(PostgresqlAlertTableParam param) {
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.POSTGRESQL);
        DBObjects<PostgresqlCheck> checks = param.getChecks();

        // 删除检查
        for (PostgresqlCheck check : checks) {
            if (DBObjectList.isDeleted(check) || DBObjectList.isChanged(check)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append("DROP CONSTRAINT ")
                        .append(DBUtil.wrap(check.originalName(), DBDialect.POSTGRESQL))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 新增、编辑检查
        for (PostgresqlCheck check : checks) {
            if (DBObjectList.isCreated(check) || DBObjectList.isChanged(check)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append(" ADD CONSTRAINT ")
                        .append(DBUtil.wrap(check.getName(), DBDialect.POSTGRESQL))
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
    private void appendColumnType(StringBuilder builder, PostgresqlColumn column, boolean created) {
        this.appendColumnTypeOnly(builder, column);
        boolean isIdentity = column.supportAutoIncrement() && column.isAutoIncrement()
                && !this.isSerialType(column.getType());
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
     * @param column  列
     */
    private void appendColumnTypeOnly(StringBuilder builder, PostgresqlColumn column) {
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
     * 生成表修改SQL列表
     *
     * @param param 表修改参数
     * @return SQL列表
     */
    public List<String> generate(PostgresqlAlertTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条表修改SQL
     *
     * @param param 表修改参数
     * @return 单条SQL
     */
    public String generateSingle(PostgresqlAlertTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成表修改SQL列表
     *
     * @param param 表修改参数
     * @return SQL列表
     */
    public static List<String> generateSql(PostgresqlAlertTableParam param) {
        return new PostgresqlTableAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条表修改SQL
     *
     * @param param 表修改参数
     * @return 单条SQL
     */
    public static String generateSqlSingle(PostgresqlAlertTableParam param) {
        return new PostgresqlTableAlertSqlGenerator().generateSingle(param);
    }

}
