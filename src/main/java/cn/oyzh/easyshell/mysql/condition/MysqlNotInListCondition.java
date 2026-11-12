package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不在列表条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlNotInListCondition extends MysqlInListCondition {

    /**
     * 单例实例
     */
    public final static MysqlNotInListCondition INSTANCE = new MysqlNotInListCondition();

    /**
     * 构造不在列表条件
     */
    public MysqlNotInListCondition() {
        super(I18nHelper.notInList(), "NOT IN");
    }
}
