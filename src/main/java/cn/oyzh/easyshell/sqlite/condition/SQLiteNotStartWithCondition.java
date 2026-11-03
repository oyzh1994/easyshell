package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 不以指定值开头条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteNotStartWithCondition extends SQLiteStartWithCondition {

    public static final SQLiteNotStartWithCondition INSTANCE = new SQLiteNotStartWithCondition();

    public SQLiteNotStartWithCondition() {
        super(I18nHelper.notStartWith(), "NOT LIKE");
    }
}
