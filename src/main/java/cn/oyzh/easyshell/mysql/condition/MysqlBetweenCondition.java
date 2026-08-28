package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.db.DBDialect;
import cn.oyzh.easyshell.util.db.ShellDBUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collection;

/**
 * 介于条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MysqlBetweenCondition extends MysqlCondition {

    public final static MysqlBetweenCondition INSTANCE = new MysqlBetweenCondition();

    public MysqlBetweenCondition() {
        super(I18nHelper.between(), "BETWEEN");
    }

    public MysqlBetweenCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition instanceof Object[] arr) {
            return this.getValue() + " " + ShellDBUtil.wrapData(arr[0], DBDialect.MYSQL) + " AND " + ShellDBUtil.wrapData(arr[1], DBDialect.MYSQL);
        }
        if (condition instanceof Collection<?> coll) {
            return this.getValue() + " " + ShellDBUtil.wrapData(CollectionUtil.get(coll, 0), DBDialect.MYSQL) + " AND " + ShellDBUtil.wrapData(CollectionUtil.get(coll, 1), DBDialect.MYSQL);
        }
        return super.wrapCondition(condition);
    }
}
