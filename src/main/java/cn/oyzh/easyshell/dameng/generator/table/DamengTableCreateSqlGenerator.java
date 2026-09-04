package cn.oyzh.easyshell.dameng.generator.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.check.DamengCheck;
import cn.oyzh.easyshell.dameng.check.DamengChecks;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.foreignKey.DamengForeignKey;
import cn.oyzh.easyshell.dameng.foreignKey.DamengForeignKeys;
import cn.oyzh.easyshell.dameng.index.DamengIndex;
import cn.oyzh.easyshell.dameng.index.DamengIndexes;
import cn.oyzh.easyshell.dameng.table.DamengCreateTableParam;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.trigger.DamengTrigger;
import cn.oyzh.easyshell.dameng.trigger.DamengTriggers;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * 达梦建表SQL生成器
 *
 * @author oyzh
 * @since 2024/09/11
 */
public class DamengTableCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 变更标志位
     */
    private boolean changeFlag;

    private void _generate(DamengCreateTableParam param) {
        String schema = param.schema();
        DamengTable table = param.getTable();
        String tableName = param.tableName();
        String fullName = DBUtil.wrap(schema, tableName, DBDialect.DAMENG);
        this.sqlBuilder.append("CREATE TABLE ")
                .append(DBUtil.wrap(schema, tableName, DBDialect.DAMENG))
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
            this.indexHandle(this.sqlBuilder, param);
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
        this.sqlBuilder.append(")");
        // 达梦表空间
        if (table.hasTableSpace()) {
            this.sqlBuilder.append(" STORAGE (ON ").append(table.getTableSpace()).append("),");
            this.changeFlag = true;
        }
        // 删除最后一个字符
        if (this.changeFlag) {
            StringUtil.deleteLast(this.sqlBuilder, ",");
            this.changeFlag = false;
        }
        this.sqlBuilder.append(";");
        // 表触发器
        if (param.hasTrigger()) {
            this.triggerHandle(param);
        }
        // 达梦表注释
        if (StringUtil.isNotBlank(table.getComment())) {
            StringBuilder builder = new StringBuilder();
            builder.append("COMMENT ON TABLE ")
                    .append(fullName)
                    .append(" IS ")
                    .append(DBUtil.wrapData(table.getComment(), DBDialect.DAMENG)).append(";");
            this.sqlList.add(builder.toString());
        }
    }

    protected void triggerHandle(DamengCreateTableParam param) {
        DamengTriggers triggers = param.getTriggers();
        for (DamengTrigger trigger : triggers) {
            StringBuilder builder = new StringBuilder();
            String body = trigger.getDefinition().trim();
            if (!body.endsWith(";")) {
                body = body + ";";
            }
            builder.append("CREATE OR REPLACE TRIGGER ")
                    .append(DBUtil.wrap(trigger.getName(), DBDialect.DAMENG))
                    .append(" ")
                    .append(trigger.getPolicy())
                    .append(" ON ")
                    .append(DBUtil.wrap(param.tableName(), DBDialect.DAMENG))
                    .append(" FOR EACH ROW BEGIN ")
                    .append(body).append(" END;");
            this.sqlList.add(builder.toString());
        }
    }

    protected void columnHandle(StringBuilder builder, DamengCreateTableParam param) {
        for (DamengColumn column : param.getColumns()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.DAMENG));
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

            // 达梦自增列使用 IDENTITY
            boolean isIdentity = column.supportAutoIncrement() && column.isAutoIncrement();

            // 默认值（达梦自增列不能有DEFAULT约束）
            if (!isIdentity && column.getDefaultValueFix() != null) {
                builder.append(" DEFAULT ").append(DBUtil.wrapData(column.getDefaultValueFix(), DBDialect.DAMENG));
            }

            // 可为null
            if (column.isNullable()) {
                builder.append(" NULL");
            } else {
                builder.append(" NOT NULL");
            }

            // 达梦自增列使用 IDENTITY
            if (isIdentity) {
                builder.append(" IDENTITY(1,1)");
            }

            // 达梦列注释使用独立COMMENT ON COLUMN语句，ALTER TABLE中跳过
            if (StringUtil.isNotBlank(column.getComment())) {
                String commentSql = """
                        COMMENT ON COLUMN "$1"."$2"."$3" IS '$4';
                        """;
                commentSql = commentSql.replace("$1", column.getSchema());
                commentSql = commentSql.replace("$2", column.getTableName());
                commentSql = commentSql.replace("$3", column.getName());
                commentSql = commentSql.replace("$4", column.getComment());
                this.sqlList.add(commentSql);
            }
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    protected void primaryKeyHandle(StringBuilder builder, DamengCreateTableParam param) {
        builder.append(" PRIMARY KEY (");
        for (DamengColumn column : param.primaryKeys()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.DAMENG))
                    .append(",");
        }
        StringUtil.deleteLast(builder, ",");
        builder.append("),\n");
        this.changeFlag = true;
    }

    protected void indexHandle(StringBuilder builder, DamengCreateTableParam param) {
        DamengIndexes indexes = param.getIndexes();
        for (DamengIndex index : indexes) {
            // 新增索引
            if (index.isUnique()) {
                builder.append(" UNIQUE");
            }
            builder.append(" INDEX ")
                    .append(DBUtil.wrap(index.getName(), DBDialect.DAMENG))
                    .append(" (");
            for (DamengIndex.IndexColumn column : index.getColumns()) {
                builder.append(DBUtil.wrap(column.getColumnName(), DBDialect.DAMENG));
                builder.append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append(") ");
            // 方法名称
            if (index.methodName() != null) {
                builder.append(" USING ").append(index.methodName());
            }
            // 拼接,
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    protected void foreignKeyHandle(StringBuilder builder, DamengCreateTableParam param) {
        DamengForeignKeys foreignKeys = param.getForeignKeys();
        for (DamengForeignKey foreignKey : foreignKeys) {
            builder.append(" CONSTRAINT ")
                    .append(DBUtil.wrap(foreignKey.getName(), DBDialect.DAMENG))
                    .append(" FOREIGN KEY (");
            for (String column : foreignKey.getColumns()) {
                builder.append(DBUtil.wrap(column, DBDialect.DAMENG)).append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append(")")
                    .append(" REFERENCES ")
                    .append(DBUtil.wrap(foreignKey.getPrimaryKeyDatabase(), foreignKey.getPrimaryKeyTable(), DBDialect.DAMENG))
                    .append(" (");
            for (String column : foreignKey.getPrimaryKeyColumns()) {
                builder.append(DBUtil.wrap(column, DBDialect.DAMENG)).append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append(")")
                    .append(" ON DELETE ").append(foreignKey.getDeletePolicy())
                    .append(" ON UPDATE ").append(foreignKey.getUpdatePolicy());
            // 拼接,
            builder.append(",");
            this.changeFlag = true;
        }
    }

    protected void checkHandle(StringBuilder builder, DamengCreateTableParam table) {
        DamengChecks checks = table.getChecks();
        for (DamengCheck check : checks) {
            builder.append(" CONSTRAINT ")
                    .append(DBUtil.wrap(check.getName(), DBDialect.DAMENG))
                    .append(" CHECK (")
                    .append(check.getClause())
                    .append(")");
            // 拼接,
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    public List<String> generate(DamengCreateTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    public String generateSingle(DamengCreateTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    public static List<String> generateSql(DamengCreateTableParam param) {
        return new DamengTableCreateSqlGenerator().generate(param);
    }

    public static String generateSqlSingle(DamengCreateTableParam param) {
        return new DamengTableCreateSqlGenerator().generateSingle(param);
    }
}
