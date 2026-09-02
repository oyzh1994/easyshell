package cn.oyzh.easyshell.dameng.check;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBCheck;
import cn.oyzh.fx.db.DBObjectStatus;

/**
 * @author oyzh
 * @since 2024/09/11
 */
public class DamengCheck extends DBObjectStatus implements DBCheck, ObjectCopier<DamengCheck> {

    /**
     * 库名称
     */
    private String schema;

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

    public DamengCheck() {

    }

    public DamengCheck(String name) {
        this.name = name;
    }

    @Override
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
    public void copy(DamengCheck check) {
        if (check != null) {
            this.name = check.name;
            this.schema = check.schema;
            this.clause = check.clause;
            this.tableName = check.tableName;
        }
    }

    @Override
    public boolean isInvalid() {
        return DBCheck.super.isInvalid() || StringUtil.isBlank(this.clause);
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getClause() {
        return clause;
    }
}
