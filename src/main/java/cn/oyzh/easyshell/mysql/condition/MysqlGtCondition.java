package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 大于条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlGtCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlGtCondition INSTANCE = new MysqlGtCondition();

    /**
     * 构造大于条件
     */
    public MysqlGtCondition() {
        super(I18nHelper.gt(), ">");
    }
}
