package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 不介于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteNotBetweenCondition extends SQLiteBetweenCondition {

    public static final SQLiteNotBetweenCondition INSTANCE = new SQLiteNotBetweenCondition();

    public SQLiteNotBetweenCondition() {
        super(I18nHelper.notBetween(), "NOT BETWEEN");
    }
}
