package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 是NULL条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNullCondition extends MysqlCondition {

    public final static MysqlNullCondition INSTANCE = new MysqlNullCondition();

    public MysqlNullCondition() {
        super(I18nHelper.isNull(), "IS NULL", false);
    }
}
