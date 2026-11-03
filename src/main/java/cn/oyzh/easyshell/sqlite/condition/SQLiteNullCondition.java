package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 为空条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteNullCondition extends SQLiteCondition {

    public static final SQLiteNullCondition INSTANCE = new SQLiteNullCondition();

    public SQLiteNullCondition() {
        super(I18nHelper.isNull(), "IS NULL", false);
    }
}
