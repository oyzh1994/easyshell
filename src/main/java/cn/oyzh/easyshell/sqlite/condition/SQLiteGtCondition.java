package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 大于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteGtCondition extends SQLiteCondition {

    public static final SQLiteGtCondition INSTANCE = new SQLiteGtCondition();

    public SQLiteGtCondition() {
        super(I18nHelper.gt(), ">");
    }
}
