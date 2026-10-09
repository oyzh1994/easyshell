package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不介于区间条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbNotBetweenCondition extends MariadbBetweenCondition {

    /**
     * 单例实例
     */
    public final static MariadbNotBetweenCondition INSTANCE = new MariadbNotBetweenCondition();

    /**
     * 构造不介于区间条件
     */
    public MariadbNotBetweenCondition() {
        super(I18nHelper.notBetween(), "NOT BETWEEN");
    }

    // @Override
    // public String wrapCondition(Object condition) {
    //     if (condition instanceof Object[] arr) {
    //         return this.getValue() + " " + DBUtil.wrapData(arr[0]) + " AND " + DBUtil.wrapData(arr[1]);
    //     }
    //     if (condition instanceof Collection<?> coll) {
    //         return this.getValue() + " " + DBUtil.wrapData(CollectionUtil.get(coll, 0)) + " AND " + DBUtil.wrapData(CollectionUtil.get(coll, 1));
    //     }
    //     return super.wrapCondition(condition);
    // }
}
