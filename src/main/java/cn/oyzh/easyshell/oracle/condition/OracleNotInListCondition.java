package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * 不在列表条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleNotInListCondition extends OracleInListCondition {

    /**
     * 单例实例
     */
    public final static OracleNotInListCondition INSTANCE = new OracleNotInListCondition();

    /**
     * 构造不在列表条件
     */
    public OracleNotInListCondition() {
        super(I18nHelper.notInList(), "NOT IN");
    }
}
