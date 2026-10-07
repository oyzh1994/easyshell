package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不为空条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlNotEmptyCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlNotEmptyCondition INSTANCE = new MysqlNotEmptyCondition();

    /**
     * 构造不为空条件
     */
    public MysqlNotEmptyCondition() {
        super(I18nHelper.notIsEmpty(), "!=''", false);
    }

}
