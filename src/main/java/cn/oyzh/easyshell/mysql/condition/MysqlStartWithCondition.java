package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值开头条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlStartWithCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlStartWithCondition INSTANCE = new MysqlStartWithCondition();

    /**
     * 构造以指定值开头条件
     */
    public MysqlStartWithCondition() {
        super(I18nHelper.startWith(), "LIKE");
    }

    /**
     * 构造以指定值开头条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public MysqlStartWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition(condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
