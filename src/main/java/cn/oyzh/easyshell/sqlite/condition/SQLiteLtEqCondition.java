package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 小于等于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteLtEqCondition extends SQLiteCondition {

    public static final SQLiteLtEqCondition INSTANCE = new SQLiteLtEqCondition();

    public SQLiteLtEqCondition() {
        super(I18nHelper.ltEq(), "<=");
    }
}
