package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlLtCondition extends MysqlCondition {

    public final static MysqlLtCondition INSTANCE = new MysqlLtCondition();

    public MysqlLtCondition() {
        super(I18nHelper.lt(), "<");
    }
}
