package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不是NULL条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotNullCondition extends MysqlCondition {

    public final static MysqlNotNullCondition INSTANCE = new MysqlNotNullCondition();

    public MysqlNotNullCondition() {
        super(I18nHelper.notIsNull(), "IS NOT NULL", false);
    }
}
