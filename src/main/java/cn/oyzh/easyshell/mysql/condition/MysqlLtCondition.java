package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlLtCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlLtCondition INSTANCE = new MysqlLtCondition();

    /**
     * 构造小于条件
     */
    public MysqlLtCondition() {
        super(I18nHelper.lt(), "<");
    }
}
