package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collection;

/**
 * 介于条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlBetweenCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlBetweenCondition INSTANCE = new MysqlBetweenCondition();

    /**
     * 构造介于条件
     */
    public MysqlBetweenCondition() {
        super(I18nHelper.between(), "BETWEEN");
    }

    /**
     * 构造介于条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public MysqlBetweenCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition instanceof Object[] arr) {
            return this.getValue() + " " + DBUtil.wrapData(arr[0], DBDialect.MYSQL) + " AND " + DBUtil.wrapData(arr[1], DBDialect.MYSQL);
        }
        if (condition instanceof Collection<?> coll) {
            return this.getValue() + " " + DBUtil.wrapData(CollectionUtil.get(coll, 0), DBDialect.MYSQL) + " AND " + DBUtil.wrapData(CollectionUtil.get(coll, 1), DBDialect.MYSQL);
        }
        return super.wrapCondition(condition);
    }
}
