package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBCondition;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * MariaDB数据库查询条件基类
 *
 * @author oyzh
 * @since 2026-10-09
 */
public abstract class MariadbCondition extends DBCondition {

    /**
     * 构造条件
     */
    public MariadbCondition() {
        super();
    }

    /**
     * 构造条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public MariadbCondition(String name, String value) {
        super(name, value);
    }

    /**
     * 构造条件
     *
     * @param name             条件名称
     * @param value            条件值
     * @param requireCondition 是否需要输入条件值
     */
    public MariadbCondition(String name, String value, boolean requireCondition) {
        super(name, value, requireCondition);
    }

    @Override
    public DBDialect getDialect() {
        return DBDialect.MARIADB;
    }
}
