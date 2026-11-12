package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 以指定值开头条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteStartWithCondition extends SQLiteCondition {

    public static final SQLiteStartWithCondition INSTANCE = new SQLiteStartWithCondition();

    public SQLiteStartWithCondition() {
        super(I18nHelper.startWith(), "LIKE");
    }

    public SQLiteStartWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : super.wrapValue(condition + "%");
    }
}
