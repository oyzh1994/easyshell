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

    @Override
    public DBDialect getDialect() {
        return DBDialect.DAMENG;
    }
}
