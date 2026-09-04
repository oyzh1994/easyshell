package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * 在列表条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class DamengInListCondition extends DamengCondition {

    public final static DamengInListCondition INSTANCE = new DamengInListCondition();

    public DamengInListCondition() {
        super(I18nHelper.inList(), "IN");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return this.getValue() + " (" + DBUtil.wrapData(condition, DBDialect.DAMENG) + ")";
        }
        return super.wrapCondition(condition);
    }
}
