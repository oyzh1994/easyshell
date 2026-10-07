package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值开头条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotStartWithCondition extends MysqlStartWithCondition {

    /**
     * 单例实例
     */
    public final static MysqlNotStartWithCondition INSTANCE = new MysqlNotStartWithCondition();

    /**
     * 构造不以指定值开头条件
     */
    public MysqlNotStartWithCondition() {
        super(I18nHelper.notStartWith(), "NOT LIKE");
    }

    // @Override
    // public String wrapCondition(Object condition) {
    //     if (condition != null) {
    //         return super.wrapCondition("%" + condition);
    //     }
    //     return super.wrapCondition(condition);
    // }
}
