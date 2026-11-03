package cn.oyzh.easyshell.sqlite.check;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.fx.db.DBCheck;
import cn.oyzh.fx.db.DBObject;

/**
 * SQLite 表检查约束。
 *
 * @since 2026-11-02
 */
public class SQLiteCheck extends DBObject implements DBCheck, ObjectCopier<SQLiteCheck> {

    private String dbName;

    private String tableName;

    private String name;

    private String clause;

    public SQLiteCheck() {
    }

    public SQLiteCheck(String name) {
        this.setName(name);
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
        super.putOriginalData("dbName", dbName);
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        super.putOriginalData("tableName", tableName);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    public boolean isNameChanged() {
        return super.checkOriginalData("name", this.name);
    }

    public String getClause() {
        return this.clause;
    }

    public void setClause(String clause) {
        this.clause = clause;
        super.putOriginalData("clause", clause);
    }

    public boolean isClauseChanged() {
        return super.checkOriginalData("clause", this.clause);
    }

    @Override
    public boolean isInvalid() {
        return StringUtil.isBlank(this.name) || StringUtil.isBlank(this.clause);
    }

    @Override
    public void copy(SQLiteCheck check) {
        if (check != null) {
            this.setDbName(check.getDbName());
            this.setTableName(check.getTableName());
            this.setName(check.getName());
            this.setClause(check.getClause());
        }
    }

    public String toConstraintSql() {
        String sql = "CHECK (" + this.clause.trim() + ")";
        return StringUtil.isBlank(this.name) ? sql
                : "CONSTRAINT " + SQLiteGeneratorUtil.quote(this.name) + " " + sql;
    }
}
