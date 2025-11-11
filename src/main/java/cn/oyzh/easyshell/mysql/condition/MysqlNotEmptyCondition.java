package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotEmptyCondition extends MysqlCondition {

    public final static MysqlNotEmptyCondition INSTANCE = new MysqlNotEmptyCondition();

    public MysqlNotEmptyCondition() {
        super(I18nHelper.notIsEmpty(), "!=''", false);
    }

}
