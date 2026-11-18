package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.oracle.condition.OracleCondition;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.Collection;

/**
 * 不介于区间条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleNotBetweenCondition extends OracleBetweenCondition {

    /**
     * 单例实例
     */
    public final static OracleNotBetweenCondition INSTANCE = new OracleNotBetweenCondition();

    /**
     * 构造不介于区间条件
     */
    public OracleNotBetweenCondition() {
        super(I18nHelper.notBetween(), "NOT BETWEEN");
    }

}
