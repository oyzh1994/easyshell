package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 空字符串条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteEmptyCondition extends SQLiteCondition {

    public static final SQLiteEmptyCondition INSTANCE = new SQLiteEmptyCondition();

    public SQLiteEmptyCondition() {
        super(I18nHelper.isEmpty(), "= ''", false);
    }
}
