package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collection;

/**
 * SQLite 介于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteBetweenCondition extends SQLiteCondition {

    public static final SQLiteBetweenCondition INSTANCE = new SQLiteBetweenCondition();

    public SQLiteBetweenCondition() {
        super(I18nHelper.between(), "BETWEEN");
    }

    public SQLiteBetweenCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        Object first = null;
        Object second = null;
        if (condition instanceof Object[] values && values.length >= 2) {
            first = values[0];
            second = values[1];
        } else if (condition instanceof Collection<?> values && values.size() >= 2) {
            first = CollectionUtil.get(values, 0);
            second = CollectionUtil.get(values, 1);
        }
        if (first == null || second == null) {
            return null;
        }
        return this.wrapValue(first) + " AND " + this.wrapValue(second);
    }
}
