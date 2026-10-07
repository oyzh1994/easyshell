package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 为空条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlEmptyCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlEmptyCondition INSTANCE = new MysqlEmptyCondition();

    /**
     * 构造为空条件
     */
    public MysqlEmptyCondition() {
        super(I18nHelper.isEmpty(), "=''", false);
    }

}
