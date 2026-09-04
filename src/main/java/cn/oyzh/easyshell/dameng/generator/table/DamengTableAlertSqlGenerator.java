package cn.oyzh.easyshell.dameng.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.check.DamengCheck;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.foreignKey.DamengForeignKey;
import cn.oyzh.easyshell.dameng.index.DamengIndex;
import cn.oyzh.easyshell.dameng.table.DamengAlertTableParam;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.trigger.DamengTrigger;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;
import org.h2.engine.DbObject;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/11
 */
public class DamengTableAlertSqlGenerator extends DBSqlGenerator {

    private void _generate(DamengAlertTableParam param) {
        DamengTable table = param.getTable();
        String fullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.DAMENG);
        // 修改索引，应在修改字段前
        if (param.primaryKeyChanged()) {
            this.primaryKeyHandle(param);
        }
        // 修改字段
        if (param.columnChanged()) {
            this.columnHandle(param);
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
                    .append(" MOVE TABLESPACE ")
                    .append(table.getTableSpace()).append(";");
            this.sqlList.add(builder.toString());
        }
        // 修改表注释
        if (table.hasComment()) {
            StringBuilder builder = new StringBuilder();
            builder.append("COMMENT ON TABLE ")
                    .append(fullName)
                    .append(" IS ")
                    .append(DBUtil.wrapData(table.getComment(), DBDialect.DAMENG)).append(";");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 触发器处理
     *
     * @param param 参数
     */
    protected void triggerHandle(DamengAlertTableParam param) {
        DBObjects<DamengTrigger> triggers = param.getTriggers();
        // 删除、变更的语句先执行，否则可能异常
        for (DamengTrigger trigger : triggers) {
            if (DBObjectList.isDeleted(trigger) || DBObjectList.isChanged(trigger)) {
                StringBuilder builder = new StringBuilder();
                builder.append("DROP TRIGGER ")
                        .append(DBUtil.wrap(trigger.originalName(), DBDialect.DAMENG))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
            if (DBObjectList.isChanged(trigger) || DBObjectList.isCreated(trigger)) {
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
                        .append(body)
                        .append(" END;");
                this.sqlList.add(builder.toString());
            }
        }
    }

    /**
     * 字段处理
     *
     * @param param 参数
     */
    protected void columnHandle(DamengAlertTableParam param) {
        int len;
        StringBuilder builder = new StringBuilder();
        String fullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.DAMENG);

        // 字段重命名
        builder.append("ALTER TABLE ")
                .append(fullName)
                .append(" ");
        len = builder.length();
        for (DamengColumn column : param.getColumns()) {
            if (DamengColumns.isChanged(column) && column.isNameChanged()) {
                // 达梦 RENAME COLUMN 只支持 old TO new，不含类型信息
                builder.append(" RENAME COLUMN ")
                        .append(DBUtil.wrap(column.originalName(), DBDialect.DAMENG))
                        .append(" TO ")
                        .append(DBUtil.wrap(column.getName(), DBDialect.DAMENG))
                        .append(",\n");
            }
        }
        // 删除最后一个字符
        StringUtil.deleteLast(builder, ",");
        StringUtil.deleteLast(builder, "\n");
        if (len != builder.length()) {
            builder.append(";");
            this.sqlList.add(builder.toString());
        }

        // 删除字段
        StringUtil.clear(builder);
        builder.append("ALTER TABLE ")
                .append(fullName)
                .append(" ");
        len = builder.length();
        for (DamengColumn column : param.getColumns()) {
            if (DamengColumns.isDeleted(column)) {
                builder.append(" DROP COLUMN ")
                        .append(DBUtil.wrap(column.getName(), DBDialect.DAMENG))
                        .append(",\n");
            }
        }
        // 删除最后一个字符
        StringUtil.deleteLast(builder, ",");
        StringUtil.deleteLast(builder, "\n");
        if (len != builder.length()) {
            builder.append(";");
            this.sqlList.add(builder.toString());
        }

        // 新增字段
        StringUtil.clear(builder);
        builder.append("ALTER TABLE ")
                .append(fullName)
                .append(" ");
        builder.append(" ADD COLUMN (\n");
        len = builder.length();
        for (DamengColumn column : param.getColumns()) {
            if (DamengColumns.isCreated(column)) {
                builder.append("\t");
                builder.append(DBUtil.wrap(column.getName(), DBDialect.DAMENG));
                this.appendColumnType(builder, column, true);
                builder.append(",\n");
            }
        }
        // 删除最后一个字符
        StringUtil.deleteLast(builder, ",");
        if (len != builder.length()) {
            builder.append(");");
            this.sqlList.add(builder.toString());
        }

        // 修改字段
        for (DamengColumn column : param.getColumns()) {
            if (DamengColumns.isChanged(column)) {
                StringUtil.clear(builder);
                boolean dropIdentity = false;
                if (param.isExistAutoIncrement()) {
                    if (column.isAutoIncrement()) {
                        dropIdentity = true;
                    } else if (column.isAutoIncrementChanged() && !column.isAutoIncrement()) {
                        dropIdentity = true;
                    }
                }
                if (dropIdentity) {
                    builder.append("ALTER TABLE ")
                            .append(fullName)
                            .append(" DROP IDENTITY;");
                    this.sqlList.add(builder.toString());
                }
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ")
                        .append(fullName)
                        .append(" MODIFY ")
                        .append(DBUtil.wrap(column.getName(), DBDialect.DAMENG));
                this.appendColumnType(builder, column, false);
                builder.append(";");
                this.sqlList.add(builder.toString());
                StringUtil.clear(builder);
                if (column.isAutoIncrement()) {
                    builder.append("ALTER TABLE ")
                            .append(fullName)
                            .append(" ADD COLUMN ")
                            .append(DBUtil.wrap(column.getName(), DBDialect.DAMENG))
                            .append(" IDENTITY(1, 1);");
                    this.sqlList.add(builder.toString());
                }
            }
        }

        // 注释
        for (DamengColumn column : param.getColumns()) {
            if (DamengColumns.isChanged(column) || DamengColumns.isCreated(column)) {
                if (column.hasComment() && column.isCommentChanged()) {
                    String commentSql = """
                            COMMENT ON COLUMN "$1"."$2"."$3" IS '$4';
                            """;
                    commentSql = commentSql.replace("$1", param.getSchema());
                    commentSql = commentSql.replace("$2", column.getTableName());
                    commentSql = commentSql.replace("$3", column.getName());
                    commentSql = commentSql.replace("$4", column.getComment());
                    this.sqlList.add(commentSql);
                }
            }
        }
    }

    /**
     * 主键处理
     *
     * @param param 参数
     */
    protected void primaryKeyHandle(DamengAlertTableParam param) {
        int len;
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.DAMENG);

        // 删除主键
        if (param.isExistPrimaryKey()) {
            for (String primaryKey : param.getPrimaryKeys()) {
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append(" DROP CONSTRAINT ");
                builder.append(DBUtil.wrap(primaryKey, DBDialect.DAMENG));
                builder.append(" CASCADE;");
                this.sqlList.add(builder.toString());
            }
        }

        // 新增主键
        StringUtil.clear(builder);
        builder.append("ALTER TABLE ").append(tableFullName).append(" ");
        builder.append(" ADD PRIMARY KEY (");
        len = builder.length();
        for (DamengColumn column : param.primaryKeys()) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.DAMENG));
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
        StringUtil.deleteLast(builder, ",");
        if (len != builder.length()) {
            builder.append(");");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 索引处理
     *
     * @param param 参数
     */
    protected void indexHandle(DamengAlertTableParam param) {
        DBObjects<DamengIndex> indexes = param.getIndexes();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.DAMENG);

        // 删除索引
        for (DamengIndex index : indexes) {
            // 索引删除用独立DROP INDEX语句（达梦不支持 ALTER TABLE DROP INDEX）
            if (DBObjectList.isDeleted(index) || DBObjectList.isChanged(index)) {
                StringBuilder builder = new StringBuilder("DROP INDEX");
                builder.append(DBUtil.wrap(index.originalName(), DBDialect.DAMENG))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 新增、编辑索引
        for (DamengIndex index : indexes) {
            // 索引新增用独立CREATE INDEX语句
            if (DBObjectList.isCreated(index) || DBObjectList.isChanged(index)) {
                StringBuilder builder = new StringBuilder("CREATE ");
                // 达梦不支持 FULLTEXT/SPATIAL，仅保留 UNIQUE
                if (index.typeName() != null && !"FULLTEXT".equalsIgnoreCase(index.typeName()) && !"SPATIAL".equalsIgnoreCase(index.typeName())) {
                    builder.append(index.typeName()).append(" ");
                }
                builder.append("INDEX ")
                        .append(DBUtil.wrap(index.getName(), DBDialect.DAMENG))
                        .append(" ON ").append(tableFullName)
                        .append(" (");
                for (DamengIndex.IndexColumn column : index.getColumns()) {
                    builder.append(DBUtil.wrap(column.getColumnName(), DBDialect.DAMENG));
                    builder.append(",");
                }
                StringUtil.deleteLast(builder, ",");
                builder.append(");");
                this.sqlList.add(builder.toString());
            }
        }
    }

    /**
     * 添加外键
     *
     * @param param 参数
     */
    protected void foreignKeyHandle(DamengAlertTableParam param) {
        DBObjects<DamengForeignKey> foreignKeys = param.getForeignKeys();
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.DAMENG);

        // 删除外键
        for (DamengForeignKey foreignKey : foreignKeys) {
            if (DBObjectList.isDeleted(foreignKey) || DBObjectList.isChanged(foreignKey)) {
                String fkName = foreignKey.originalName();
                // 名称为null是临时数据
                if (StringUtil.isNotBlank(fkName)) {
                    StringUtil.clear(builder);
                    builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                    builder.append(" DROP CONSTRAINT ")
                            .append(DBUtil.wrap(foreignKey.originalName(), DBDialect.DAMENG))
                            .append(";");
                    this.sqlList.add(builder.toString());
                }
            }
        }

        // 新增外键
        for (DamengForeignKey foreignKey : foreignKeys) {
            if (DBObjectList.isCreated(foreignKey) || DBObjectList.isChanged(foreignKey)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append(" ADD CONSTRAINT ")
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
                builder.append(";");
                this.sqlList.add(builder.toString());
            }
        }
    }

    /**
     * 检查处理
     *
     * @param param 参数
     */
    protected void checkHandle(DamengAlertTableParam param) {
        StringBuilder builder = new StringBuilder();
        String tableFullName = DBUtil.wrap(param.getSchema(), param.tableName(), DBDialect.DAMENG);
        DBObjects<DamengCheck> checks = param.getChecks();

        // 删除检查
        for (DamengCheck check : checks) {
            if (DBObjectList.isDeleted(check) || DBObjectList.isChanged(check)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append("DROP CONSTRAINT ")
                        .append(DBUtil.wrap(check.originalName(), DBDialect.DAMENG))
                        .append(";");
                this.sqlList.add(builder.toString());
            }
        }

        // 新增、编辑检查
        for (DamengCheck check : checks) {
            if (DBObjectList.isCreated(check) || DBObjectList.isChanged(check)) {
                StringUtil.clear(builder);
                builder.append("ALTER TABLE ").append(tableFullName).append(" ");
                builder.append(" ADD CONSTRAINT ")
                        .append(DBUtil.wrap(check.getName(), DBDialect.DAMENG))
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
     */
    private void appendColumnType(StringBuilder builder, DamengColumn column, boolean created) {
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
        boolean isIdentity = column.supportAutoIncrement() && column.isAutoIncrement();
        if (!isIdentity && column.getDefaultValueFix() != null) {
            builder.append(" DEFAULT ").append(DBUtil.wrapData(column.getDefaultValueFix(), DBDialect.DAMENG));
        }
        if (column.isNullable()) {
            builder.append(" NULL");
        } else {
            builder.append(" NOT NULL");
        }
        if (isIdentity && created) {
            builder.append(" IDENTITY(1,1)");
        }
    }

    public List<String> generate(DamengAlertTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    public String generateSingle(DamengAlertTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    public static List<String> generateSql(DamengAlertTableParam param) {
        return new DamengTableAlertSqlGenerator().generate(param);
    }

    public static String generateSqlSingle(DamengAlertTableParam param) {
        return new DamengTableAlertSqlGenerator().generateSingle(param);
    }

}
