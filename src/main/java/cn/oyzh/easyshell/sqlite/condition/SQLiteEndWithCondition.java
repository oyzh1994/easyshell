package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * SQLite 以指定值结尾条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteEndWithCondition extends SQLiteCondition {

    public static final SQLiteEndWithCondition INSTANCE = new SQLiteEndWithCondition();

    public SQLiteEndWithCondition() {
        super(I18nHelper.endWith(), "LIKE");
    }

    /**
     * 构造以指定值结尾条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public SQLiteEndWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : super.wrapCondition("%" + condition);
    }
}
