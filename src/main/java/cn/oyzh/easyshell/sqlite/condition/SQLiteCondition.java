package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBCondition;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * SQLite 查询条件基类。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public abstract class SQLiteCondition extends DBCondition {

    /**
     * 构造条件
     */
    public SQLiteCondition() {
        super();
    }

    /**
     * 构造条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public SQLiteCondition(String name, String value) {
        super(name, value);
    }

    /**
     * 构造条件
     *
     * @param name             条件名称
     * @param value            条件值
     * @param requireCondition 是否需要输入条件值
     */
    public SQLiteCondition(String name, String value, boolean requireCondition) {
        super(name, value, requireCondition);
    }

    /**
     * 包装条件值
     *
     * @param condition 条件值
     * @return 包装后的条件值
     */
    public String wrapCondition(Object condition) {
        return condition == null ? null : this.wrapValue(condition);
    }

    @Override
    public String wrapCondition(String columnName, Object condition) {
        if (!this.isRequireCondition()) {
            return this.getValue();
        }
        String wrapped = this.wrapCondition(condition);
        return wrapped == null ? null : this.getValue() + " " + wrapped;
    }

    /**
     * 包装条件值
     *
     * @param value 条件值
     * @return 包装后的条件值
     */
    protected String wrapValue(Object value) {
        return String.valueOf(DBUtil.wrapData(value, DBDialect.SQLITE));
    }
}
