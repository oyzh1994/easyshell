package cn.oyzh.easyshell.mariadb.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.check.MariadbCheck;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.foreignKey.MariadbForeignKey;
import cn.oyzh.easyshell.mariadb.index.MariadbIndex;
import cn.oyzh.easyshell.mariadb.table.MariadbCreateTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.mariadb.trigger.MariadbTrigger;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * MariaDB创建表SQL生成器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbTableCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 变更标志位
     */
    private boolean changeFlag;

    /**
     * 生成SQL片段
     *
     * @param param 创建表参数
     */
    private void _generate(MariadbCreateTableParam param) {
        this.sqlList = new ArrayList<>();
        this.sqlBuilder = new StringBuilder();
        String dbName = param.dbName();
        MariadbTable table = param.getTable();
        String tableName = param.tableName();
        this.sqlBuilder.append("CREATE TABLE ")
                .append(DBUtil.wrap(dbName, tableName, DBDialect.MARIADB))
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
            this.sqlBuilder.append(" COMMENT = ").append(DBUtil.wrapData(table.getComment(), DBDialect.MARIADB)).append(",");
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

    /**
     * 生成SQL列表
     *
     * @param param 创建表参数
     * @return SQL列表
     */
    public List<String> generate(MariadbCreateTableParam param) {
        this._generate(param);
        return super.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建表参数
     * @return SQL语句
     */
    public String generateSingle(MariadbCreateTableParam param) {
        this._generate(param);
        return super.buildSqlSingle();
    }

    /**
     * 触发器处理
     *
     * @param param 创建表参数
     */
    protected void triggerHandle(MariadbCreateTableParam param) {
        for (MariadbTrigger trigger : param.getTriggers()) {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TRIGGER ")
                    .append(DBUtil.wrap(trigger.getName(), DBDialect.MARIADB))
                    .append(" ")
                    .append(trigger.getPolicy())
                    .append(" ON ")
                    .append(DBUtil.wrap(param.tableName(), DBDialect.MARIADB))
                    .append(" FOR EACH ROW ")
                    .append(trigger.getDefinition())
                    .append(";");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 字段处理
     *
     * @param builder 语句
     * @param param   创建表参数
     */
    protected void columnHandle(StringBuilder builder, MariadbCreateTableParam param) {
        for (MariadbColumn column : param.getColumns()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.MARIADB));
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
                builder.append(" DEFAULT ").append(column.getDefaultValueFix());
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
                builder.append(" COMMENT ").append(DBUtil.wrapData(column.getComment(), DBDialect.MARIADB));
            }
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    /**
     * 主键处理
     *
     * @param builder 语句
     * @param param   创建表参数
     */
    protected void primaryKeyHandle(StringBuilder builder, MariadbCreateTableParam param) {
        List<MariadbColumn> keyList = param.primaryKeys();
        if (!keyList.isEmpty()) {
            builder.append(" PRIMARY KEY (");
            for (MariadbColumn column : keyList) {
                builder.append(DBUtil.wrap(column.getName(), DBDialect.MARIADB))
                        .append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append("),\n");
            this.changeFlag = true;
        }
    }

    /**
     * 索引处理
     *
     * @param builder 语句
     * @param param   创建表参数
     */
    protected void indexHandle(StringBuilder builder, MariadbCreateTableParam param) {
        DBObjects<MariadbIndex> indexes = param.getIndexes();
        for (MariadbIndex index : indexes) {
            // 新增索引
            if (index.isUnique()) {
                builder.append(" UNIQUE");
            }
            builder.append(" INDEX ")
                    .append(DBUtil.wrap(index.getName(), DBDialect.MARIADB))
                    .append(" (");
            for (MariadbIndex.IndexColumn column : index.getColumns()) {
                builder.append(DBUtil.wrap(column.getColumnName(), DBDialect.MARIADB));
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
                builder.append(" COMMENT ").append(DBUtil.wrapData(index.getComment(), DBDialect.MARIADB));
            }
            // 拼接,
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    /**
     * 外键处理
     *
     * @param builder 语句
     * @param param   创建表参数
     */
    protected void foreignKeyHandle(StringBuilder builder, MariadbCreateTableParam param) {
        DBObjects<MariadbForeignKey> foreignKeys = param.getForeignKeys();
        for (MariadbForeignKey foreignKey : foreignKeys) {
            // 新增外键
            builder.append(" CONSTRAINT ")
                    .append(DBUtil.wrap(foreignKey.getName(), DBDialect.MARIADB))
                    .append(" FOREIGN KEY (");
            for (String column : foreignKey.getColumns()) {
                builder.append(DBUtil.wrap(column, DBDialect.MARIADB)).append(",");
            }
            StringUtil.deleteLast(builder, ",");
            builder.append(")")
                    .append(" REFERENCES ")
                    .append(DBUtil.wrap(foreignKey.getPrimaryKeyDatabase(), foreignKey.getPrimaryKeyTable(), DBDialect.MARIADB))
                    .append(" (");
            for (String column : foreignKey.getPrimaryKeyColumns()) {
                builder.append(DBUtil.wrap(column, DBDialect.MARIADB)).append(",");
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

    /**
     * 检查约束处理
     *
     * @param builder 语句
     * @param table   创建表参数
     */
    protected void checkHandle(StringBuilder builder, MariadbCreateTableParam table) {
        DBObjects<MariadbCheck> checks = table.getChecks();
        for (MariadbCheck check : checks) {
            builder.append(" CONSTRAINT ")
                    .append(DBUtil.wrap(check.getName(), DBDialect.MARIADB))
                    .append(" CHECK (")
                    .append(check.getClause())
                    .append(")");
            // 拼接,
            builder.append(",\n");
            this.changeFlag = true;
        }
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建表参数
     * @return SQL列表
     */
    public static List<String> generateSql(MariadbCreateTableParam param) {
        return new MariadbTableCreateSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建表参数
     * @return SQL语句
     */
    public static String generateSqlSingle(MariadbCreateTableParam param) {
        return new MariadbTableCreateSqlGenerator().generateSingle(param);
    }
}
