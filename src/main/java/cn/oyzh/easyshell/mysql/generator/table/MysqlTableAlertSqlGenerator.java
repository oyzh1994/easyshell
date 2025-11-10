package cn.oyzh.easyshell.mysql.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.db.DBObjectList;
import cn.oyzh.easyshell.mysql.check.MysqlCheck;
import cn.oyzh.easyshell.mysql.check.MysqlChecks;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.foreignKey.MysqlForeignKey;
import cn.oyzh.easyshell.mysql.foreignKey.MysqlForeignKeys;
import cn.oyzh.easyshell.mysql.index.MysqlIndex;
import cn.oyzh.easyshell.mysql.index.MysqlIndexes;
import cn.oyzh.easyshell.mysql.table.MysqlAlertTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.mysql.trigger.MysqlTrigger;
import cn.oyzh.easyshell.mysql.trigger.MysqlTriggers;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/11
 */
public class MysqlTableAlertSqlGenerator {

    private List<String> sqlList;

    private StringBuilder sqlBuilder;

    public String generate(MysqlAlertTableParam param) {
        this.sqlList = new ArrayList<>();
        this.sqlBuilder = new StringBuilder();
        String dbName = param.dbName();
        String tableName = param.tableName();
        MysqlTable table = param.getTable();
        if (param.hasForeignKey()) {
            this.foreignKeyHandle2(this.sqlBuilder, param);
        }
        this.sqlBuilder.append("ALTER TABLE ")
                .append(ShellMysqlUtil.wrap(dbName, tableName, DBDialect.MYSQL))
                .append(" \n");
        // 字段
        if (param.columnChanged()) {
            this.columnHandle(this.sqlBuilder, param);
        }
        // 主键
        if (param.primaryKeyChanged()) {
            this.primaryKeyHandle(this.sqlBuilder, param);
        }
        // 索引
        if (param.hasIndex()) {
            this.indexHandle(this.sqlBuilder, param);
        }
        // 外键
        if (param.hasForeignKey()) {
            this.foreignKeyHandle1(this.sqlBuilder, param);
        }
        // 检查
        if (param.hasCheck()) {
            this.checkHandle(this.sqlBuilder, param);
        }
        // 表字符集
        if (table.hasCharset()) {
            this.sqlBuilder.append(" CHARACTER SET = ").append(table.getCharset()).append(",");
        }
        // 表排序
        if (table.hasCollation()) {
            this.sqlBuilder.append(" COLLATE = ").append(table.getCollation()).append(",");
        }
        // 表引擎
        if (table.hasEngine()) {
            this.sqlBuilder.append(" ENGINE = ").append(table.getEngine()).append(",");
        }
        // 表注释
        if (table.hasComment()) {
            this.sqlBuilder.append(" COMMENT = ").append(ShellMysqlUtil.wrapData(table.getComment())).append(",");
        }
        // 行格式
        if (table.hasRowFormat()) {
            this.sqlBuilder.append(" ROW_FORMAT = ").append(table.getRowFormat()).append(",");
        }
        // 表自动递增
        if (table.hasAutoIncrement()) {
            this.sqlBuilder.append(" AUTO_INCREMENT = ").append(table.getAutoIncrement()).append(",");
        }
        this.sqlBuilder.append(";");
        // 表触发器
        if (param.hasTrigger()) {
            this.triggerHandle(param);
        }
        return this.buildSql();
    }

    private String buildSql() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.sqlBuilder);
        for (String sql : sqlList) {
            builder.append("\n").append(sql);
        }
        return builder.toString().trim();
    }

    protected void triggerHandle(MysqlAlertTableParam param) {
        MysqlTriggers triggers = param.getTriggers();
        for (MysqlTrigger trigger : triggers) {
            if (MysqlTriggers.isDeleted(trigger) || MysqlTriggers.isChanged(trigger)) {
                StringBuilder builder = new StringBuilder();
                builder.append("DROP TRIGGER ")
                        .append(ShellMysqlUtil.wrap(trigger.originalName(), DBDialect.MYSQL))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
            if (MysqlTriggers.isChanged(trigger) || MysqlTriggers.isCreated(trigger)) {
                StringBuilder builder = new StringBuilder();
                builder.append("CREATE TRIGGER ")
                        .append(ShellMysqlUtil.wrap(trigger.getName(), DBDialect.MYSQL))
                        .append(" ")
                        .append(trigger.getPolicy())
                        .append(" ON ")
                        .append(ShellMysqlUtil.wrap(param.tableName(), DBDialect.MYSQL))
                        .append(" FOR EACH ROW ")
                        .append(trigger.getDefinition())
                        .append(";");
                this.sqlList.add(builder.toString());
            }
        }
    }

    protected void columnHandle(StringBuilder builder, MysqlAlertTableParam param) {
        for (MysqlColumn column : param.getColumns()) {
            // 修改或者新增字段
            if (MysqlColumns.isChanged(column) || MysqlColumns.isCreated(column)) {
                if (column.isCreated()) {
                    builder.append(" ADD COLUMN ")
                            .append(ShellMysqlUtil.wrap(column.getName(), DBDialect.MYSQL));
                } else if (column.isNameChanged()) {
                    builder.append(" CHANGE COLUMN ")
                            .append(ShellMysqlUtil.wrap(column.originalName(), DBDialect.MYSQL))
                            .append(" ")
                            .append(ShellMysqlUtil.wrap(column.getName(), DBDialect.MYSQL));
                } else {
                    builder.append(" MODIFY COLUMN ")
                            .append(ShellMysqlUtil.wrap(column.getName(), DBDialect.MYSQL));
                }
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
                    if (column.getCharset() != null) {
                        builder.append(" CHARACTER SET ").append(column.getCharset());
                    }
                    if (column.getCollation() != null) {
                        builder.append(" COLLATE ").append(column.getCollation());
                    }
                }

                // 默认值
                if (column.supportDefaultValue() && column.getDefaultValue() != null) {
                    builder.append(" DEFAULT ").append(ShellMysqlUtil.wrapData(column.getDefaultValueString()));
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
                    builder.append(" COMMENT ").append(ShellMysqlUtil.wrapData(column.getComment()));
                }
                builder.append(",");
            } else if (MysqlColumns.isDeleted(column)) {// 删除字段
                builder.append(" DROP COLUMN ")
                        .append(ShellMysqlUtil.wrap(column.getName(), DBDialect.MYSQL))
                        .append(",");
            }
        }
        // 删除最后一个字符
        StringUtil.deleteLast(builder, ",");
    }

    protected void primaryKeyHandle(StringBuilder builder, MysqlAlertTableParam table) {
        if(!builder.toString().endsWith(",")){
            builder.append(",");
        }
        if (table.isExistPrimaryKey()) {
            builder.append(" DROP PRIMARY KEY,");
        }
        List<MysqlColumn> keyList = table.primaryKeys();
        if (!keyList.isEmpty()) {
            builder.append(" ADD PRIMARY KEY (");
            for (MysqlColumn column : keyList) {
                builder.append(ShellMysqlUtil.wrap(column.getName(), DBDialect.MYSQL));
                if (column.supportKeySize()) {
                    if (column.getPrimaryKeySize() != null) {
                        builder.append("(").append(column.getPrimaryKeySize()).append(")");
                    } else if (column.getSize() != null) {
                        builder.append("(").append(Math.min(column.getSize(), 100)).append(")");
                    } else {
                        builder.append("(").append(100).append(")");
                    }
                }
                builder.append(",");
            }
            // 删除最后一个字符
            StringUtil.deleteLast(builder, ",");
            builder.append(") USING BTREE,");
        }
        // // 删除最后一个字符
        // StringUtil.deleteLast(builder, ",");
    }

    protected void indexHandle(StringBuilder builder, MysqlAlertTableParam param) {
        // if(!builder.toString().endsWith(",")){
        //     builder.append(",");
        // }
        MysqlIndexes indexes = param.getIndexes();
        for (MysqlIndex index : indexes) {
            // 索引删除、变更
            if (MysqlIndexes.isDeleted(index) || MysqlIndexes.isChanged(index)) {
                builder.append("DROP INDEX ")
                        .append(ShellMysqlUtil.wrap(index.originalName(), DBDialect.MYSQL))
                        .append(",");
            }
            // 索引新增、变更
            if (MysqlIndexes.isCreated(index) || MysqlIndexes.isChanged(index)) {
                // 新增索引
                builder.append(" ADD");
                // 类型名称
                if (index.typeName() != null) {
                    builder.append(" ").append(index.typeName());
                }
                builder.append(" INDEX ")
                        .append(ShellMysqlUtil.wrap(index.getName(), DBDialect.MYSQL))
                        .append(" (");
                for (MysqlIndex.IndexColumn column : index.getColumns()) {
                    builder.append(ShellMysqlUtil.wrap(column.getColumnName(), DBDialect.MYSQL));
                    if (column.getSubPart() != null && column.getSubPart() > 0) {
                        builder.append("(").append(column.getSubPart()).append(")");
                    }
                    builder.append(",");
                }
                // 删除最后一个字符
                StringUtil.deleteLast(builder);
                builder.append(") ");
                // 方法名称
                if (index.methodName() != null) {
                    builder.append(" USING ").append(index.methodName());
                }
                if (index.getComment() != null) {
                    builder.append(" COMMENT ").append(ShellMysqlUtil.wrapData(index.getComment()));
                }
                // 拼接,
                builder.append(",");
            }
        }
        // // 删除最后一个字符
        // if (builder.toString().endsWith(",")) {
        //     builder.deleteCharAt(builder.length() - 1);
        // }
    }

    protected void foreignKeyHandle1(StringBuilder builder, MysqlAlertTableParam table) {
        MysqlForeignKeys foreignKeys = table.getForeignKeys();
        if (!foreignKeys.hasCreated() && !foreignKeys.hasChanged()) {
            return;
        }
        // if(!builder.toString().endsWith(",")){
        //     builder.append(",");
        // }
        for (MysqlForeignKey foreignKey : foreignKeys.filterList(DBObjectList.TYPE_CHANGED, DBObjectList.TYPE_CREATED)) {
            // 新增外键
            builder.append(" ADD CONSTRAINT ")
                    .append(ShellMysqlUtil.wrap(foreignKey.getName(), DBDialect.MYSQL))
                    .append(" FOREIGN KEY (");
            for (String column : foreignKey.getColumns()) {
                builder.append(ShellMysqlUtil.wrap(column, DBDialect.MYSQL)).append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append(")")
                    .append(" REFERENCES ")
                    .append(ShellMysqlUtil.wrap(foreignKey.getPrimaryKeyDatabase(), foreignKey.getPrimaryKeyTable(), DBDialect.MYSQL))
                    .append(" (");
            for (String column : foreignKey.getPrimaryKeyColumns()) {
                builder.append(ShellMysqlUtil.wrap(column, DBDialect.MYSQL)).append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append(")")
                    .append(" ON DELETE ").append(foreignKey.getDeletePolicy())
                    .append(" ON UPDATE ").append(foreignKey.getUpdatePolicy());
            // 拼接,
            builder.append(",");
        }
        StringUtil.deleteLast(builder, ",");
    }

    protected void foreignKeyHandle2(StringBuilder builder, MysqlAlertTableParam param) {
        MysqlForeignKeys foreignKeys = param.getForeignKeys();
        if (!foreignKeys.hasChanged() && !foreignKeys.hasDeleted()) {
            return;
        }
        // StringBuilder builder = new StringBuilder();
        builder.append("ALTER TABLE ")
                .append(ShellMysqlUtil.wrap(param.dbName(), param.tableName(), DBDialect.MYSQL));
        for (MysqlForeignKey foreignKey : foreignKeys.filterList(DBObjectList.TYPE_DELETED, DBObjectList.TYPE_CHANGED)) {
            String fkName = foreignKey.originalName();
            // 名称为null是临时数据
            if (StringUtil.isNotBlank(fkName)) {
                this.sqlBuilder.append(" DROP FOREIGN KEY ")
                        .append(ShellMysqlUtil.wrap(foreignKey.originalName(), DBDialect.MYSQL))
                        .append(",");
            }
        }
        StringUtil.deleteLast(builder, ",");
        builder.append(";");
    }

    protected void checkHandle(StringBuilder builder, MysqlAlertTableParam param) {
        if (!builder.toString().endsWith(",")) {
            builder.append(",");
        }
        MysqlChecks checks = param.getChecks();
        for (MysqlCheck check : checks) {
            // 检查删除、变更
            if (MysqlChecks.isDeleted(check) || MysqlChecks.isChanged(check)) {
                builder.append("DROP CONSTRAINT ")
                        .append(ShellMysqlUtil.wrap(check.originalName(), DBDialect.MYSQL))
                        .append(",");
            }
            // 检查新增、变更
            if (MysqlChecks.isCreated(check) || MysqlChecks.isChanged(check)) {
                builder.append(" ADD CONSTRAINT ")
                        .append(ShellMysqlUtil.wrap(check.getName(), DBDialect.MYSQL))
                        .append(" CHECK (")
                        .append(check.getClause())
                        .append(")");
                // 拼接,
                builder.append(",");
            }
            StringUtil.deleteLast(builder, ",");
        }
    }

    public static String generateSql(MysqlAlertTableParam param) {
        return new MysqlTableAlertSqlGenerator().generate(param);
    }
}
