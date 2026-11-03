package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 小于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteLtCondition extends SQLiteCondition {

    public static final SQLiteLtCondition INSTANCE = new SQLiteLtCondition();

    public SQLiteLtCondition() {
        super(I18nHelper.lt(), "<");
    }
}
