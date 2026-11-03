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
        if (!(condition instanceof String value) || value.isBlank()) {
            return null;
        }
        StringBuilder builder = new StringBuilder("(");
        String[] items = value.split(",");
        for (int index = 0; index < items.length; index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(this.wrapValue(items[index].trim()));
        }
        return builder.append(')').toString();
    }
}
