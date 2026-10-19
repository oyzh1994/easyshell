package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 等于条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlEqCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlEqCondition INSTANCE = new MysqlEqCondition();

    /**
     * 构造等于条件
     */
    public MysqlEqCondition() {
        super(I18nHelper.eq(), "=");
    }
}
