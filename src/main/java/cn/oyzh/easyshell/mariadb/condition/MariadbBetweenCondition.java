package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collection;

/**
 * 介于条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbBetweenCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbBetweenCondition INSTANCE = new MariadbBetweenCondition();

    /**
     * 构造介于条件
     */
    public MariadbBetweenCondition() {
        super(I18nHelper.between(), "BETWEEN");
    }

    /**
     * 构造介于条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public MariadbBetweenCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return super.wrapBetweenCondition(condition);
    }
}
