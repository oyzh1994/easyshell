package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 不以指定值结尾条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteNotEndWithCondition extends SQLiteEndWithCondition {

    public static final SQLiteNotEndWithCondition INSTANCE = new SQLiteNotEndWithCondition();

    public SQLiteNotEndWithCondition() {
        super(I18nHelper.notEndWith(), "NOT LIKE");
    }
}
