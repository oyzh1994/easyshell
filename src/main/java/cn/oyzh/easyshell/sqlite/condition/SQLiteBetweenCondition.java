package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collection;

/**
 * SQLite 介于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteBetweenCondition extends SQLiteCondition {

    public static final SQLiteBetweenCondition INSTANCE = new SQLiteBetweenCondition();

    public SQLiteBetweenCondition() {
        super(I18nHelper.between(), "BETWEEN");
    }

    public SQLiteBetweenCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return super.wrapBetweenCondition(condition);
    }
}
