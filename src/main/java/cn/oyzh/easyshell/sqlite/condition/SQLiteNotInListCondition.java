package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 不在列表条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteNotInListCondition extends SQLiteInListCondition {

    public static final SQLiteNotInListCondition INSTANCE = new SQLiteNotInListCondition();

    public SQLiteNotInListCondition() {
        super(I18nHelper.notInList(), "NOT IN");
    }
}
