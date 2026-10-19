package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值结尾条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbEndWithCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbEndWithCondition INSTANCE = new MariadbEndWithCondition();

    /**
     * 构造以指定值结尾条件
     */
    public MariadbEndWithCondition() {
        super(I18nHelper.endWith(), "LIKE");
    }

    /**
     * 构造以指定值结尾条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public MariadbEndWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition);
        }
        return super.wrapCondition(condition);
    }
}
