package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.i18n.I18nHelper;

/**
 * 在列表条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MysqlInListCondition extends MysqlCondition {

    public final static MysqlInListCondition INSTANCE = new MysqlInListCondition();

    public MysqlInListCondition() {
        super(I18nHelper.inList(), "IN");
    }

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
