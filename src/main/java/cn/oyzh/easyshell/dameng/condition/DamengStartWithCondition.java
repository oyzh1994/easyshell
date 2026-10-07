package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值开头条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengStartWithCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengStartWithCondition INSTANCE = new DamengStartWithCondition();

    /**
     * 构造以指定值开头条件
     */
    public DamengStartWithCondition() {
        super(I18nHelper.startWith(), "LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition);
        }
        return super.wrapCondition(condition);
    }
}
