package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBCondition;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * H2 查询条件基类。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public abstract class H2Condition extends DBCondition {

    /**
     * 构造条件
     */
    public H2Condition() {
        super();
    }

    /**
     * 构造条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public H2Condition(String name, String value) {
        super(name, value);
    }

    /**
     * 构造条件
     *
     * @param name             条件名称
     * @param value            条件值
     * @param requireCondition 是否需要输入条件值
     */
    public H2Condition(String name, String value, boolean requireCondition) {
        super(name, value, requireCondition);
    }

    @Override
    public DBDialect getDialect() {
        return DBDialect.H2;
    }
}
