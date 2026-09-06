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

    public String wrapCondition(Object condition) {
        Object d = DBUtil.wrapData(condition, DBDialect.MYSQL);
        return d == null ? null : d.toString();
    }

    @Override
    public String wrapCondition(String columnName, Object condition) {
        if (this.isRequireCondition()) {
            return condition == null ? this.getValue() : this.getValue() + " " + this.wrapCondition(condition);
        }
        return this.wrapCondition(condition);
    }
}
