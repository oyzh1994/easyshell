package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 在列表条件
 * <p>
 * SqlServer使用IN (...)，列表值按逗号拆分，字符串使用标准单引号转义。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerInListCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerInListCondition INSTANCE = new SqlServerInListCondition();

    /**
     * 构造在列表条件
     */
    public SqlServerInListCondition() {
        super(I18nHelper.inList(), "IN");
    }

    /**
     * 构造在列表条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public SqlServerInListCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        String wrapped = super.wrapListCondition(condition);
        return wrapped == null ? null : this.getValue() + " " + wrapped;
    }
}
