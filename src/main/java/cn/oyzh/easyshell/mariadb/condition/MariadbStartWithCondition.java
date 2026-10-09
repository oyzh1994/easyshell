package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值开头条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbStartWithCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbStartWithCondition INSTANCE = new MariadbStartWithCondition();

    /**
     * 构造以指定值开头条件
     */
    public MariadbStartWithCondition() {
        super(I18nHelper.startWith(), "LIKE");
    }

    /**
     * 构造以指定值开头条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public MariadbStartWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition(condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
