package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 包含条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteContainsCondition extends SQLiteCondition {

    public static final SQLiteContainsCondition INSTANCE = new SQLiteContainsCondition();

    public SQLiteContainsCondition() {
        super(I18nHelper.contains(), "LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : super.wrapCondition("%" + condition + "%");
    }
}
