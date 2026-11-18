package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 为空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleNullCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleNullCondition INSTANCE = new OracleNullCondition();

    /**
     * 构造为空条件
     */
    public OracleNullCondition() {
        super(I18nHelper.isNull(), "IS NULL", false);
    }
}
