package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 非空条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteNotNullCondition extends SQLiteCondition {

    public static final SQLiteNotNullCondition INSTANCE = new SQLiteNotNullCondition();

    public SQLiteNotNullCondition() {
        super(I18nHelper.notIsNull(), "IS NOT NULL", false);
    }
}
