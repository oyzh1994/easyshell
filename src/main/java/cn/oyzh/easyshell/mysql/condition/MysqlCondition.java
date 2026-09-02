package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBCondition;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * 条件
 *
 * @author oyzh
 * @since 2024/06/26
 */
public abstract class MysqlCondition extends DBCondition {

    public MysqlCondition() {
        super();
    }

    public MysqlCondition(String name, String value) {
        super(name, value);
    }

    public MysqlCondition(String name, String value, boolean requireCondition) {
        super(name, value, requireCondition);
    }

    @Override
    public String wrapCondition() {
        return (String) super.wrapCondition();
    }

    @Override
    public String wrapCondition(String columnName) {
        return (String) super.wrapCondition(columnName);
    }

    @Override
    public String wrapCondition(Object condition) {
        return (String) super.wrapCondition(condition);
    }

    @Override
    public String wrapCondition(String columnName, Object condition) {
        if (this.isRequireCondition()) {
            return condition == null ? this.getValue() : this.getValue() + " " + DBUtil.wrapData(condition, DBDialect.MYSQL);
        }
        return this.getValue();
    }
}
