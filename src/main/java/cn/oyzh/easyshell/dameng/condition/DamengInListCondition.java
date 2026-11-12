package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 在列表条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengInListCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengInListCondition INSTANCE = new DamengInListCondition();

    /**
     * 构造在列表条件
     */
    public DamengInListCondition() {
        super(I18nHelper.inList(), "IN");
    }

    public DamengInListCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return super.wrapListCondition(condition);
    }
}
