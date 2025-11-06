package cn.oyzh.easyshell.mysql.check;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.DBObjectStatus;

/**
 * @author oyzh
 * @since 2024/09/11
 */
public class MysqlCheck extends DBObjectStatus implements ObjectCopier<MysqlCheck> {

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 名称
     */
    private String name;

    /**
     * 子语句
     */
    private String clause;

    public MysqlCheck() {

    }

    public MysqlCheck(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public boolean isNameChanged() {
        return super.checkOriginalData("name", this.name);
    }

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    public void setClause(String clause) {
        this.clause = clause;
        super.putOriginalData("clause", clause);
    }

    public boolean isClauseChanged() {
        return super.checkOriginalData("clause", this.clause);
    }

    @Override
    public void copy(MysqlCheck check) {
        if (check != null) {
            this.name = check.name;
            this.dbName = check.dbName;
            this.clause = check.clause;
            this.tableName = check.tableName;
        }
    }

    public boolean isInvalid() {
        return StringUtil.isBlank(this.name) || StringUtil.isBlank(this.clause);
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getName() {
        return name;
    }

    public String getClause() {
        return clause;
    }
}
