package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlEmptyCondition extends MysqlCondition {

    public final static MysqlEmptyCondition INSTANCE = new MysqlEmptyCondition();

    public MysqlEmptyCondition() {
        super(I18nHelper.isEmpty(), "=''", false);
    }

}
