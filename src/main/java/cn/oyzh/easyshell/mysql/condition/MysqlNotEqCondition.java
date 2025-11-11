package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不等于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotEqCondition extends MysqlCondition {

    public final static MysqlNotEqCondition INSTANCE = new MysqlNotEqCondition();

    public MysqlNotEqCondition() {
        super(I18nHelper.notEq(), "!=");
    }
}
