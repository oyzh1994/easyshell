package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 大于等于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteGtEqCondition extends SQLiteCondition {

    public static final SQLiteGtEqCondition INSTANCE = new SQLiteGtEqCondition();

    public SQLiteGtEqCondition() {
        super(I18nHelper.gtEq(), ">=");
    }
}
