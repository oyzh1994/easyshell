package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.i18n.I18nHelper;

/**
 * 在列表条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbInListCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbInListCondition INSTANCE = new MariadbInListCondition();

    /**
     * 构造在列表条件
     */
    public MariadbInListCondition() {
        super(I18nHelper.inList(), "IN");
    }

    /**
     * 构造在列表条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public MariadbInListCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition instanceof String str) {
            String[] arr = str.split(",");
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(",").append(DBUtil.wrapData(s, DBDialect.MARIADB));
            }
            if (!sb.isEmpty()) {
                return this.getValue() + " (" + sb.substring(1) + ")";
            }
        }
        return super.wrapCondition(condition);
    }
}
