package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 不包含条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteNotContainsCondition extends SQLiteCondition {

    public static final SQLiteNotContainsCondition INSTANCE = new SQLiteNotContainsCondition();

    public SQLiteNotContainsCondition() {
        super(I18nHelper.notContains(), "NOT LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : super.wrapCondition("%" + condition + "%");
    }
}
