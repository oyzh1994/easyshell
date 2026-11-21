package cn.oyzh.easyshell.oracle.generator.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.check.OracleCheck;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.foreignKey.OracleForeignKey;
import cn.oyzh.easyshell.oracle.index.OracleIndex;
import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.easyshell.oracle.table.OracleCreateTableParam;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.trigger.OracleTrigger;
import cn.oyzh.easyshell.util.oracle.ShellOracleColumnUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * Oracle建表SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleTableCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 变更标志位
     */
    private boolean changeFlag;

    /**
     * 当前数据库是否支持IDENTITY；11g自动降级为序列触发器。
     */
    private boolean identitySupported = true;

    /**
     * 执行SQL生成，依次处理字段、主键、索引、外键、检查、表空间、触发器和表注释
     *
     * @param param 建表参数
     */
    private void _generate(OracleCreateTableParam param) {
        String schema = param.schema();
        OracleTable table = param.getTable();
        String tableName = param.tableName();
        String fullName = DBUtil.wrap(schema, tableName, DBDialect.ORACLE);
        this.sqlBuilder.append("CREATE TABLE ")
                .append(DBUtil.wrap(schema, tableName, DBDialect.ORACLE))
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
        // Oracle表空间
        if (table.hasTableSpace()) {
            this.sqlBuilder.append(" TABLESPACE ")
                    .append(DBUtil.wrap(table.getTableSpace(), DBDialect.ORACLE));
        }
        this.sqlBuilder.append(";");
        this.autoIncrementHandle(param);
        // 表触发器
        if (param.hasTrigger()) {
            this.triggerHandle(param);
        }
        // Oracle表注释
        if (StringUtil.isNotBlank(table.getComment())) {
            StringBuilder builder = new StringBuilder();
            builder.append("COMMENT ON TABLE ")
                    .append(DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE))
                    .append(" IS ")
                    .append(DBUtil.wrapData(table.getComment(), DBDialect.ORACLE)).append(";");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 追加表触发器定义SQL
     *
     * @param param 建表参数
     */
    protected void triggerHandle(OracleCreateTableParam param) {
        DBObjects<OracleTrigger> triggers = param.getTriggers();
        for (OracleTrigger trigger : triggers) {
            StringBuilder builder = new StringBuilder();
            if (trigger.getDefinition() == null) {
                continue;
            }
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

    /**
     * 追加字段定义，并生成列注释SQL
     *
     * @param builder SQL构建器
     * @param param   建表参数
     */
    protected void columnHandle(StringBuilder builder, OracleCreateTableParam param) {
        for (OracleColumn column : param.getColumns()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE));
            builder.append(" ").append(column.getType());

            // 字段长度
            Integer size = column.getSize() != null
                    ? column.getSize()
                    : ShellOracleColumnUtil.requiredSize(column.getType());
            if (column.supportSize() && size != null) {
                builder.append("(").append(size);
                // 小数位
                if (column.supportDigits() && column.getDigits() != null) {
                    builder.append(",").append(column.getDigits());
                }
                builder.append(")");
            } else if (column.supportValue() && column.getValue() != null) {
                builder.append("(").append(column.getValue()).append(")");
            }

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
            if (isIdentity) {
                builder.append(" GENERATED BY DEFAULT ON NULL AS IDENTITY");
            }

            // 默认值（Oracle自增列由IDENTITY或序列触发器维护）
            if (!column.isGenerated() && !column.isAutoIncrement()
                    && column.getDefaultValueFix() != null) {
                builder.append(" DEFAULT ").append(column.getDefaultValueFix());
            }

            // 可为null
            if (isIdentity) {
                // IDENTITY列由Oracle强制为NOT NULL。
            } else if (column.isNullable()) {
                builder.append(" NULL");
            } else {
                builder.append(" NOT NULL");
            }

            // Oracle列注释使用独立COMMENT ON COLUMN语句，ALTER TABLE中跳过
            if (StringUtil.isNotBlank(column.getComment())) {
                String commentSql = "COMMENT ON COLUMN "
                        + DBUtil.wrap(column.getSchema(), column.getTableName(), DBDialect.ORACLE)
                        + "." + DBUtil.wrap(column.getName(), DBDialect.ORACLE)
                        + " IS " + DBUtil.wrapData(column.getComment(), DBDialect.ORACLE) + ";";
                this.sqlList.add(commentSql);
            }
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    /**
     * 生成Oracle 11g及12c+均可执行的序列和触发器式自增实现。
     *
     * @param param 建表参数
     */
    protected void autoIncrementHandle(OracleCreateTableParam param) {
        for (OracleColumn column : param.getColumns()) {
            if (!column.isAutoIncrement() || this.isIdentity(column)) {
                continue;
            }
            String sequenceName = autoIncrementObjectName(param.tableName(), column.getName(), "SEQ");
            String triggerName = autoIncrementObjectName(param.tableName(), column.getName(), "TRG");
            String sequenceFullName = DBUtil.wrap(param.getSchema(), sequenceName, DBDialect.ORACLE);
            String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE);
            String columnName = DBUtil.wrap(column.getName(), DBDialect.ORACLE);
            this.sqlList.add("CREATE SEQUENCE " + sequenceFullName + " START WITH 1 INCREMENT BY 1 NOCACHE;");
            this.sqlList.add("CREATE OR REPLACE TRIGGER " + DBUtil.wrap(param.getSchema(), triggerName, DBDialect.ORACLE)
                    + " BEFORE INSERT ON " + tableFullName
                    + " FOR EACH ROW WHEN (NEW." + columnName + " IS NULL) BEGIN SELECT " + sequenceFullName
                    + ".NEXTVAL INTO :NEW." + columnName + " FROM DUAL; END;");
        }
    }

    /**
     * 生成兼容Oracle 11g 30字符标识符限制的对象名称。
     */
    public static String autoIncrementObjectName(String tableName, String columnName, String suffix) {
        String raw = tableName + "_" + columnName;
        String normalized = raw.toUpperCase().replaceAll("[^A-Z0-9_$#]", "_");
        int maxBaseLength = 30 - suffix.length() - 1;
        return normalized.substring(0, Math.min(normalized.length(), maxBaseLength)) + "_" + suffix;
    }

    /**
     * 追加主键定义
     *
     * @param builder SQL构建器
     * @param param   建表参数
     */
    protected void primaryKeyHandle(StringBuilder builder, OracleCreateTableParam param) {
        builder.append(" PRIMARY KEY (");
        for (OracleColumn column : param.primaryKeys()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE))
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
    protected void indexHandle(OracleCreateTableParam param) {
        DBObjects<OracleIndex> indexes = param.getIndexes();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.ORACLE);

        // 新增、编辑索引
        for (OracleIndex index : indexes) {
            // 索引新增用独立CREATE INDEX语句
            if (DBObjectList.isCreated(index) || DBObjectList.isChanged(index)) {
                StringBuilder builder = new StringBuilder("CREATE ");
                builder.append(indexTypePrefix(index));
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
     * 追加外键定义
     *
     * @param builder SQL构建器
     * @param param   建表参数
     */
    protected void foreignKeyHandle(StringBuilder builder, OracleCreateTableParam param) {
        DBObjects<OracleForeignKey> foreignKeys = param.getForeignKeys();
        for (OracleForeignKey foreignKey : foreignKeys) {
            builder.append(" CONSTRAINT ")
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
                    .append(deletePolicy(foreignKey.getDeletePolicy()));
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
    protected void checkHandle(StringBuilder builder, OracleCreateTableParam table) {
        DBObjects<OracleCheck> checks = table.getChecks();
        for (OracleCheck check : checks) {
            builder.append(" CONSTRAINT ")
                    .append(DBUtil.wrap(check.getName(), DBDialect.ORACLE))
                    .append(" CHECK (")
                    .append(check.getClause())
                    .append(")");
            // 拼接,
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    /**
     * 转换Oracle支持的外键删除策略。
     */
    protected static String deletePolicy(String policy) {
        if (StringUtil.isBlank(policy) || StringUtil.equalsAnyIgnoreCase(policy, "NO ACTION", "RESTRICT")) {
            return "";
        }
        return " ON DELETE " + policy;
    }

    protected static String indexTypePrefix(OracleIndex index) {
        StringBuilder prefix = new StringBuilder();
        if (index.isUnique()) {
            prefix.append("UNIQUE ");
        } else if ("BITMAP".equalsIgnoreCase(index.methodName())) {
            prefix.append("BITMAP ");
        }
        return prefix.toString();
    }

    /**
     * 生成建表SQL列表
     *
     * @param param 建表参数
     * @return SQL列表
     */
    public List<String> generate(OracleCreateTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 按数据库能力生成建表SQL列表。
     *
     * @param param             建表参数
     * @param identitySupported 是否支持IDENTITY
     * @return SQL列表
     */
    public List<String> generate(OracleCreateTableParam param, boolean identitySupported) {
        this.identitySupported = identitySupported;
        return this.generate(param);
    }

    /**
     * 生成单条建表SQL
     *
     * @param param 建表参数
     * @return 单条SQL
     */
    public String generateSingle(OracleCreateTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成建表SQL列表
     *
     * @param param 建表参数
     * @return SQL列表
     */
    public static List<String> generateSql(OracleCreateTableParam param) {
        return new OracleTableCreateSqlGenerator().generate(param);
    }

    /**
     * 按数据库能力生成建表SQL列表。
     *
     * @param param             建表参数
     * @param identitySupported 是否支持IDENTITY
     * @return SQL列表
     */
    public static List<String> generateSql(OracleCreateTableParam param, boolean identitySupported) {
        return new OracleTableCreateSqlGenerator().generate(param, identitySupported);
    }

    /**
     * 生成单条建表SQL
     *
     * @param param 建表参数
     * @return 单条SQL
     */
    public static String generateSqlSingle(OracleCreateTableParam param) {
        return new OracleTableCreateSqlGenerator().generateSingle(param);
    }

    private boolean isIdentity(OracleColumn column) {
        return this.identitySupported && column.isAutoIncrement() && column.isIdentityAutoIncrementType();
    }
}
