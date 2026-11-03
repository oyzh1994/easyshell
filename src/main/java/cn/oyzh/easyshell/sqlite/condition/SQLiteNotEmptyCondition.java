package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 非空字符串条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteNotEmptyCondition extends SQLiteCondition {

    public static final SQLiteNotEmptyCondition INSTANCE = new SQLiteNotEmptyCondition();

    public SQLiteNotEmptyCondition() {
        super(I18nHelper.notIsEmpty(), "<> ''", false);
    }
}
