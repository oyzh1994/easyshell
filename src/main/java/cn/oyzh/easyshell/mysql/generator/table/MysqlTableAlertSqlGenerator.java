package cn.oyzh.easyshell.mysql.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.check.MysqlCheck;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.foreignKey.MysqlForeignKey;
import cn.oyzh.easyshell.mysql.index.MysqlIndex;
import cn.oyzh.easyshell.mysql.table.MysqlAlertTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.mysql.trigger.MysqlTrigger;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/11
 */
public class MysqlTableAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 变更标志位
     */
    private boolean changeFlag;

    private void _generate(MysqlAlertTableParam param) {
        this.sqlList = new ArrayList<>();
        this.sqlBuilder = new StringBuilder();
        String dbName = param.dbName();
        String tableName = param.tableName();
        MysqlTable table = param.getTable();
        if (param.hasForeignKey()) {
            this.foreignKeyHandle2(param);
        }
        this.sqlBuilder.append("ALTER TABLE ")
                .append(DBUtil.wrap(dbName, tableName, DBDialect.MYSQL))
                .append("\n");
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
        // 删除最后一个字符
        if (this.changeFlag) {
            StringUtil.deleteLast(this.sqlBuilder, ",");
            this.changeFlag = false;
        }
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
            this.sqlBuilder.append(" COMMENT = ").append(DBUtil.wrapData(table.getComment(), DBDialect.MYSQL)).append(",");
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

    public List<String> generate(MysqlAlertTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    public String generateSingle(MysqlAlertTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 触发器处理
     *
     * @param param 参数
     */
    protected void triggerHandle(MysqlAlertTableParam param) {
        DBObjects<MysqlTrigger> triggers = param.getTriggers();
        // 删除、变更的语句先执行，否则可能异常
        for (MysqlTrigger trigger : triggers) {
            if (DBObjectList.isDeleted(trigger) || DBObjectList.isChanged(trigger)) {
                StringBuilder builder = new StringBuilder();
                builder.append("DROP TRIGGER ")
                        .append(DBUtil.wrap(trigger.originalName(), DBDialect.MYSQL))
                        .append(";\n");
                this.sqlList.add(builder.toString());
            }
        }
        for (MysqlTrigger trigger : triggers) {
            if (DBObjectList.isChanged(trigger) || DBObjectList.isCreated(trigger)) {
                StringBuilder builder = new StringBuilder();
                builder.append("CREATE TRIGGER ")
                        .append(DBUtil.wrap(trigger.getName(), DBDialect.MYSQL))
                        .append(" ")
                        .append(trigger.getPolicy())
                        .append(" ON ")
                        .append(DBUtil.wrap(param.tableName(), DBDialect.MYSQL))
                        .append(" FOR EACH ROW ")
                        .append(trigger.getDefinition())
                        .append(";\n");
                this.sqlList.add(builder.toString());
            }
        }
    }

    /**
     * 字段处理
     *
     * @param builder 语句
     * @param param   参数
     */
    protected void columnHandle(StringBuilder builder, MysqlAlertTableParam param) {
        // 删除语句先执行，否则可能异常
        for (MysqlColumn column : param.getColumns()) {
            // 删除字段
            if (MysqlColumns.isDeleted(column)) {
                builder.append(" DROP COLUMN ")
                        .append(DBUtil.wrap(column.getName(), DBDialect.MYSQL))
                        .append(",\n");
                this.changeFlag = true;
            }
        }
        for (MysqlColumn column : param.getColumns()) {
            // 修改或者新增字段
            if (MysqlColumns.isChanged(column) || MysqlColumns.isCreated(column)) {
                if (column.isCreated()) {
                    builder.append(" ADD COLUMN ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.MYSQL));
                } else if (column.isNameChanged()) {
                    builder.append(" CHANGE COLUMN ")
                            .append(DBUtil.wrap(column.originalName(), DBDialect.MYSQL))
                            .append(" ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.MYSQL));
                } else {
                    builder.append(" MODIFY COLUMN ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.MYSQL));
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
                    if (StringUtil.isNotBlank(column.getCharset())) {
                        builder.append(" CHARACTER SET ").append(column.getCharset());
                    }
                    if (StringUtil.isNotBlank(column.getCollation())) {
                        builder.append(" COLLATE ").append(column.getCollation());
                    }
                }

                // 默认值
                if (column.supportDefaultValue() && column.getDefaultValueFix() != null) {
                    builder.append(" DEFAULT ").append((column.getDefaultValueFix()));
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
                    builder.append(" COMMENT ").append(DBUtil.wrapData(column.getComment(), DBDialect.MYSQL));
                }
                builder.append(",\n");
                this.changeFlag = true;
            }
        }
        // // 删除最后一个字符
        // StringUtil.deleteLast(builder, ",");
    }

    /**
     * 主键处理
     *
     * @param builder 语句
     * @param param   参数
     */
    protected void primaryKeyHandle(StringBuilder builder, MysqlAlertTableParam param) {
        // if (!builder.toString().endsWith(",")) {
        //     builder.append(",");
        // }
        if (param.isExistPrimaryKey()) {
            builder.append(" DROP PRIMARY KEY,\n");
            this.changeFlag = true;
        }
        List<MysqlColumn> keyList = param.primaryKeys();
        if (!keyList.isEmpty()) {
            builder.append(" ADD PRIMARY KEY (");
            for (MysqlColumn column : keyList) {
                builder.append(DBUtil.wrap(column.getName(), DBDialect.MYSQL));
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
            builder.append(") USING BTREE,\n");
            this.changeFlag = true;
        }
        // // 删除最后一个字符
        // StringUtil.deleteLast(builder, ",");
    }

    /**
     * 处理索引
     *
     * @param builder 语句
     * @param param   参数
     */
    protected void indexHandle(StringBuilder builder, MysqlAlertTableParam param) {
        // if(!builder.toString().endsWith(",")){
        //     builder.append(",");
        // }
        DBObjects<MysqlIndex> indexes = param.getIndexes();
        // 删除、变更的语句先执行，否则可能异常
        for (MysqlIndex index : indexes) {
            // 索引删除、变更
            if (DBObjectList.isDeleted(index) || DBObjectList.isChanged(index)) {
                builder.append("DROP INDEX ")
                        .append(DBUtil.wrap(index.originalName(), DBDialect.MYSQL))
                        .append(",\n");
                this.changeFlag = true;
            }
        }
        for (MysqlIndex index : indexes) {
            // 索引新增、变更
            if (DBObjectList.isCreated(index) || DBObjectList.isChanged(index)) {
                // 新增索引
                builder.append(" ADD");
                // 类型名称
                if (index.typeName() != null) {
                    builder.append(" ").append(index.typeName());
                }
                builder.append(" INDEX ")
                        .append(DBUtil.wrap(index.getName(), DBDialect.MYSQL))
                        .append(" (");
                for (MysqlIndex.IndexColumn column : index.getColumns()) {
                    builder.append(DBUtil.wrap(column.getColumnName(), DBDialect.MYSQL));
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
                    builder.append(" COMMENT ").append(DBUtil.wrapData(index.getComment(), DBDialect.MYSQL));
                }
                // 拼接,
                builder.append(",\n");
                this.changeFlag = true;
            }
        }
        // // 删除最后一个字符
        // if (builder.toString().endsWith(",")) {
        //     builder.deleteCharAt(builder.length() - 1);
        // }
    }

    /**
     * 添加外键
     *
     * @param builder 语句
     * @param param   参数
     */
    protected void foreignKeyHandle1(StringBuilder builder, MysqlAlertTableParam param) {
        DBObjects<MysqlForeignKey> foreignKeys = param.getForeignKeys();
        if (!foreignKeys.hasCreated() && !foreignKeys.hasChanged()) {
            return;
        }
        // if(!builder.toString().endsWith(",")){
        //     builder.append(",");
        // }
        for (MysqlForeignKey foreignKey : foreignKeys.filterList(DBObjectList.TYPE_CHANGED, DBObjectList.TYPE_CREATED)) {
            // 新增外键
            builder.append(" ADD CONSTRAINT ")
                    .append(DBUtil.wrap(foreignKey.getName(), DBDialect.MYSQL))
                    .append(" FOREIGN KEY (");
            for (String column : foreignKey.getColumns()) {
                builder.append(DBUtil.wrap(column, DBDialect.MYSQL)).append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append(")")
                    .append(" REFERENCES ")
                    .append(DBUtil.wrap(foreignKey.getPrimaryKeyDatabase(), foreignKey.getPrimaryKeyTable(), DBDialect.MYSQL))
                    .append(" (");
            for (String column : foreignKey.getPrimaryKeyColumns()) {
                builder.append(DBUtil.wrap(column, DBDialect.MYSQL)).append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append(")")
                    .append(" ON DELETE ").append(foreignKey.getDeletePolicy())
                    .append(" ON UPDATE ").append(foreignKey.getUpdatePolicy());
            // 拼接,
            builder.append(",\n");
            this.changeFlag = true;
        }
        // StringUtil.deleteLast(builder, ",");
    }

    /**
     * 删除外键
     *
     * @param param 参数
     */
    protected void foreignKeyHandle2(MysqlAlertTableParam param) {
        DBObjects<MysqlForeignKey> foreignKeys = param.getForeignKeys();
        if (!foreignKeys.hasChanged() && !foreignKeys.hasDeleted()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("ALTER TABLE ")
                .append(DBUtil.wrap(param.dbName(), param.tableName(), DBDialect.MYSQL));
        builder.append("\n");
        for (MysqlForeignKey foreignKey : foreignKeys.filterList(DBObjectList.TYPE_DELETED, DBObjectList.TYPE_CHANGED)) {
            String fkName = foreignKey.originalName();
            // 名称为null是临时数据
            if (StringUtil.isNotBlank(fkName)) {
                builder.append(" DROP FOREIGN KEY ")
                        .append(DBUtil.wrap(foreignKey.originalName(), DBDialect.MYSQL))
                        .append(",\n");
            }
        }
        StringUtil.deleteLast(builder, ",");
        builder.append(";");
        this.sqlList.add(builder.toString());
    }

    protected void checkHandle(StringBuilder builder, MysqlAlertTableParam param) {
        // if (!builder.toString().endsWith(",")) {
        //     builder.append(",");
        // }
        DBObjects<MysqlCheck> checks = param.getChecks();
        // 删除、变更语句先执行，否则可能异常
        for (MysqlCheck check : checks) {
            // 检查删除、变更
            if (DBObjectList.isDeleted(check) || DBObjectList.isChanged(check)) {
                builder.append("DROP CONSTRAINT ")
                        .append(DBUtil.wrap(check.originalName(), DBDialect.MYSQL))
                        .append(",\n");
                this.changeFlag = true;
            }
        }
        for (MysqlCheck check : checks) {
            // 检查新增、变更
            if (DBObjectList.isCreated(check) || DBObjectList.isChanged(check)) {
                builder.append(" ADD CONSTRAINT ")
                        .append(DBUtil.wrap(check.getName(), DBDialect.MYSQL))
                        .append(" CHECK (")
                        .append(check.getClause())
                        .append(")");
                // 拼接,
                builder.append(",\n");
                this.changeFlag = true;
            }
        }
        // StringUtil.deleteLast(builder, ",");
    }

    public static List<String> generateSql(MysqlAlertTableParam param) {
        return new MysqlTableAlertSqlGenerator().generate(param);
    }

    public static String generateSqlSingle(MysqlAlertTableParam param) {
        return new MysqlTableAlertSqlGenerator().generateSingle(param);
    }
}
