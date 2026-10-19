package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值结尾条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengEndWithCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengEndWithCondition INSTANCE = new DamengEndWithCondition();

    /**
     * 构造以指定值结尾条件
     */
    public DamengEndWithCondition() {
        super(I18nHelper.endWith(), "LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition(condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
