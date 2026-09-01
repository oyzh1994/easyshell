package cn.oyzh.easyshell.mysql.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.easyshell.mysql.check.MysqlCheck;
import cn.oyzh.easyshell.mysql.check.MysqlChecks;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.foreignKey.MysqlForeignKey;
import cn.oyzh.easyshell.mysql.foreignKey.MysqlForeignKeys;
import cn.oyzh.easyshell.mysql.index.MysqlIndex;
import cn.oyzh.easyshell.mysql.index.MysqlIndexes;
import cn.oyzh.easyshell.mysql.table.MysqlCreateTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.mysql.trigger.MysqlTrigger;
import cn.oyzh.easyshell.util.db.ShellDBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/11
 */
public class MysqlTableCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 变更标志位
     */
    private boolean changeFlag;

    private void _generate(MysqlCreateTableParam param) {
        this.sqlList = new ArrayList<>();
        this.sqlBuilder = new StringBuilder();
        String dbName = param.dbName();
        MysqlTable table = param.getTable();
        String tableName = param.tableName();
        this.sqlBuilder.append("CREATE TABLE ")
                .append(ShellDBUtil.wrap(dbName, tableName, DBDialect.MYSQL))
                .append(" ( \n");
        // 字段
        if (param.hasColumns()) {
            this.columnHandle(this.sqlBuilder, param);
        }
        // 主键
        this.primaryKeyHandle(this.sqlBuilder, param);
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
        this.sqlBuilder.append(" )");
        // 表字符集
        if (table.hasCharset()) {
            this.sqlBuilder.append(" CHARACTER SET = ").append(table.getCharset()).append(",");
            this.changeFlag = true;
        }
        // 表排序
        if (table.hasCollation()) {
            this.sqlBuilder.append(" COLLATE = ").append(table.getCollation()).append(",");
            this.changeFlag = true;
        }
        // 表引擎
        if (table.hasEngine()) {
            this.sqlBuilder.append(" ENGINE = ").append(table.getEngine()).append(",");
            this.changeFlag = true;
        }
        // 表注释
        if (table.hasComment()) {
            this.sqlBuilder.append(" COMMENT = ").append(ShellDBUtil.wrapData(table.getComment(), DBDialect.MYSQL)).append(",");
            this.changeFlag = true;
        }
        // 行格式
        if (table.hasRowFormat()) {
            this.sqlBuilder.append(" ROW_FORMAT = ").append(table.getRowFormat()).append(",");
            this.changeFlag = true;
        }
        // 表自动递增
        if (table.hasAutoIncrement()) {
            this.sqlBuilder.append(" AUTO_INCREMENT = ").append(table.getAutoIncrement()).append(",");
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
    }

    public List<String> generate(MysqlCreateTableParam param) {
        this._generate(param);
        return super.buildSql();
    }

    public String generateSingle(MysqlCreateTableParam param) {
        this._generate(param);
        return super.buildSqlSingle();
    }

    protected void triggerHandle(MysqlCreateTableParam param) {
        for (MysqlTrigger trigger : param.getTriggers()) {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TRIGGER ")
                    .append(ShellDBUtil.wrap(trigger.getName(), DBDialect.MYSQL))
                    .append(" ")
                    .append(trigger.getPolicy())
                    .append(" ON ")
                    .append(ShellDBUtil.wrap(param.tableName(), DBDialect.MYSQL))
                    .append(" FOR EACH ROW ")
                    .append(trigger.getDefinition())
                    .append(";");
            this.sqlList.add(builder.toString());
        }
    }

    protected void columnHandle(StringBuilder builder, MysqlCreateTableParam param) {
        for (MysqlColumn column : param.getColumns()) {
            builder.append(ShellDBUtil.wrap(column.getName(), DBDialect.MYSQL));
            // 字段类型
            builder.append(" ").append(column.getType());

            // 字段长度
            if (column.supportSize() && column.getSize() != null) {
                builder.append("(").append(column.getSize());
                // 小数位
                if (column.supportDigits() && column.getDigits() != null) {
                    builder.append(",").append(column.getDigits());
                }
                builder.append(")");
            } else if (column.supportValue() && column.getValue() != null) {// 值
                builder.append("(").append(column.getValue()).append(")");
            }

            // 无符号
            if (column.supportUnsigned() && column.isUnsigned()) {
                builder.append(" UNSIGNED ");
            }

            // 填充零
            if (column.supportZeroFill() && column.isZeroFill()) {
                builder.append(" ZEROFILL ");
            }

            // 字符集及排序
            if (column.supportCharset()) {
                if (StringUtil.isNotBlank(column.getCharset())) {
                    builder.append(" CHARACTER SET ").append(column.getCharset());
                }
                if (StringUtil.isNotBlank(column.getCollation())) {
                    builder.append(" COLLATE ").append(column.getCollation());
                }
            }

            // 默认值
            if (column.supportDefaultValue() && column.getDefaultValueFix() != null) {
                builder.append(" DEFAULT ").append(ShellDBUtil.wrapData(column.getDefaultValueFix(), DBDialect.MYSQL));
            }

            // 可为null
            if (column.isNullable()) {
                builder.append(" NULL");
            } else {
                builder.append(" NOT NULL");
            }

            // 根据时间戳更新
            if (column.supportTimestamp() && column.isUpdateOnCurrentTimestamp()) {
                builder.append(" ON UPDATE CURRENT_TIMESTAMP(0)");
            }

            // 自动递增
            if (column.supportAutoIncrement() && column.isAutoIncrement()) {
                builder.append(" AUTO_INCREMENT ");
            }

            // 注释
            if (column.hasComment()) {
                builder.append(" COMMENT ").append(ShellDBUtil.wrapData(column.getComment(), DBDialect.MYSQL));
            }
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    protected void primaryKeyHandle(StringBuilder builder, MysqlCreateTableParam param) {
        List<MysqlColumn> keyList = param.primaryKeys();
        if (!keyList.isEmpty()) {
            builder.append(" PRIMARY KEY (");
            for (MysqlColumn column : keyList) {
                builder.append(ShellDBUtil.wrap(column.getName(), DBDialect.MYSQL))
                        .append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append("),\n");
            this.changeFlag = true;
        }
    }

    protected void indexHandle(StringBuilder builder, MysqlCreateTableParam param) {
        MysqlIndexes indexes = param.getIndexes();
        for (MysqlIndex index : indexes) {
            // 新增索引
            if (index.isUnique()) {
                builder.append(" UNIQUE");
            }
            builder.append(" INDEX ")
                    .append(ShellDBUtil.wrap(index.getName(), DBDialect.MYSQL))
                    .append(" (");
            for (MysqlIndex.IndexColumn column : index.getColumns()) {
                builder.append(ShellDBUtil.wrap(column.getColumnName(), DBDialect.MYSQL));
                if (column.getSubPart() != null && column.getSubPart() > 0) {
                    builder.append("(").append(column.getSubPart()).append(")");
                }
                builder.append(",");
            }
            // 删除最后一个字符
            StringUtil.deleteLast(builder, ",");
            builder.append(") ");
            // 方法名称
            if (index.methodName() != null) {
                builder.append(" USING ").append(index.methodName());
            }
            if (index.getComment() != null) {
                builder.append(" COMMENT ").append(ShellDBUtil.wrapData(index.getComment(), DBDialect.MYSQL));
            }
            // 拼接,
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    protected void foreignKeyHandle(StringBuilder builder, MysqlCreateTableParam param) {
        MysqlForeignKeys foreignKeys = param.getForeignKeys();
        for (MysqlForeignKey foreignKey : foreignKeys) {
            // 新增外键
            builder.append(" CONSTRAINT ")
                    .append(ShellDBUtil.wrap(foreignKey.getName(), DBDialect.MYSQL))
                    .append(" FOREIGN KEY (");
            for (String column : foreignKey.getColumns()) {
                builder.append(ShellDBUtil.wrap(column, DBDialect.MYSQL)).append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append(")")
                    .append(" REFERENCES ")
                    .append(ShellDBUtil.wrap(foreignKey.getPrimaryKeyDatabase(), foreignKey.getPrimaryKeyTable(), DBDialect.MYSQL))
                    .append(" (");
            for (String column : foreignKey.getPrimaryKeyColumns()) {
                builder.append(ShellDBUtil.wrap(column, DBDialect.MYSQL)).append(",");
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

    protected void checkHandle(StringBuilder builder, MysqlCreateTableParam table) {
        MysqlChecks checks = table.getChecks();
        for (MysqlCheck check : checks) {
            builder.append(" CONSTRAINT ")
                    .append(ShellDBUtil.wrap(check.getName(), DBDialect.MYSQL))
                    .append(" CHECK (")
                    .append(check.getClause())
                    .append(")");
            // 拼接,
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    public static List<String> generateSql(MysqlCreateTableParam param) {
        return new MysqlTableCreateSqlGenerator().generate(param);
    }

    public static String generateSqlSingle(MysqlCreateTableParam param) {
        return new MysqlTableCreateSqlGenerator().generateSingle(param);
    }
}
