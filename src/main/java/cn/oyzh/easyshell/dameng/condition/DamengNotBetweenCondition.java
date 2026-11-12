package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dameng.condition.DamengCondition;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.Collection;

/**
 * 不介于区间条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengNotBetweenCondition extends DamengBetweenCondition {

    /**
     * 单例实例
     */
    public final static DamengNotBetweenCondition INSTANCE = new DamengNotBetweenCondition();

    /**
     * 构造不介于区间条件
     */
    public DamengNotBetweenCondition() {
        super(I18nHelper.notBetween(), "NOT BETWEEN");
    }

}
