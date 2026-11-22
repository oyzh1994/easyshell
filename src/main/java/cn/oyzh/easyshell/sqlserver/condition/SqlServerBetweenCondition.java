package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 介于条件
 * <p>
 * SqlServer使用BETWEEN ... AND ...，区间值按SqlServer字面量规则包装。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerBetweenCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerBetweenCondition INSTANCE = new SqlServerBetweenCondition();

    /**
     * 构造介于条件
     */
    public SqlServerBetweenCondition() {
        super(I18nHelper.between(), "BETWEEN");
    }

    /**
     * 构造介于条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public SqlServerBetweenCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return this.getValue() + " " + super.wrapBetweenCondition(condition);
    }
}
