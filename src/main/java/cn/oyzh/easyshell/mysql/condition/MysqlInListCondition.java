package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.i18n.I18nHelper;

/**
 * 在列表条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlInListCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlInListCondition INSTANCE = new MysqlInListCondition();

    /**
     * 构造在列表条件
     */
    public MysqlInListCondition() {
        super(I18nHelper.inList(), "IN");
    }

    /**
     * 构造在列表条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public MysqlInListCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition instanceof String str) {
            String[] arr = str.split(",");
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(",").append(DBUtil.wrapData(s, DBDialect.MYSQL));
            }
            if (!sb.isEmpty()) {
                return this.getValue() + " (" + sb.substring(1) + ")";
            }
        }
        return super.wrapCondition(condition);
    }
}
