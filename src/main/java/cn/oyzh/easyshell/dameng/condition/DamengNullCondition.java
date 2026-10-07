package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 为空条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengNullCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengNullCondition INSTANCE = new DamengNullCondition();

    /**
     * 构造为空条件
     */
    public DamengNullCondition() {
        super(I18nHelper.isNull(), "IS NULL", false);
    }
}
