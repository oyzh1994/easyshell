package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 在列表条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteInListCondition extends SQLiteCondition {

    public static final SQLiteInListCondition INSTANCE = new SQLiteInListCondition();

    public SQLiteInListCondition() {
        super(I18nHelper.inList(), "IN");
    }

    public SQLiteInListCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return this.getValue() + " " +super.wrapListCondition(condition);
    }
}
