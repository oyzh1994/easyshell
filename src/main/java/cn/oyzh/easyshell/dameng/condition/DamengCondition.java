package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBCondition;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * 达梦数据库查询条件基类
 *
 * @author oyzh
 * @since 2025-11-06
 */
public abstract class DamengCondition extends DBCondition {

    /**
     * 构造条件
     */
    public DamengCondition() {
        super();
    }

    /**
     * 构造条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public DamengCondition(String name, String value) {
        super(name, value);
    }

    /**
     * 构造条件
     *
     * @param name             条件名称
     * @param value            条件值
     * @param requireCondition 是否需要输入条件值
     */
    public DamengCondition(String name, String value, boolean requireCondition) {
        super(name, value, requireCondition);
    }

    /**
     * 包装条件值
     *
     * @param condition 条件值
     * @return 包装后的条件值
     */
    public String wrapCondition(Object condition) {
        Object d = DBUtil.wrapData(condition, DBDialect.DAMENG);
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
