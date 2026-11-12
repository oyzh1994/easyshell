package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengContainsCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengContainsCondition INSTANCE = new DamengContainsCondition();

    /**
     * 构造包含条件
     */
    public DamengContainsCondition() {
        super(I18nHelper.contains(), "LIKE");
    }

    public DamengContainsCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapValue("%" + condition + "%");
        }
        return super.wrapValue(condition);
    }
}
