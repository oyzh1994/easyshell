package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 不等于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteNotEqCondition extends SQLiteCondition {

    public static final SQLiteNotEqCondition INSTANCE = new SQLiteNotEqCondition();

    public SQLiteNotEqCondition() {
        super(I18nHelper.notEq(), "!=");
    }
}
