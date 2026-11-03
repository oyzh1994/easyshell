package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 等于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteEqCondition extends SQLiteCondition {

    public static final SQLiteEqCondition INSTANCE = new SQLiteEqCondition();

    public SQLiteEqCondition() {
        super(I18nHelper.eq(), "=");
    }
}
