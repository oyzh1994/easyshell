package cn.oyzh.easyshell.db;


import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.internal.ShellPrototype;
import com.alibaba.druid.DbType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据库类型(方言)
 *
 * @author oyzh
 * @since 2024/2/20
 */
public enum DBDialect {
    MYSQL;

    public DbType dbType() {
        switch (this) {
            case MYSQL:
                return DbType.mysql;
            default:
                return DbType.mysql;
        }
    }

    public static List<DBDialect> valueList() {
        List<DBDialect> list = new ArrayList<>();
        Collections.addAll(list, values());
        return list;
    }

    public static DBDialect of(String type) {
        if(StringUtil.equalsIgnoreCase(type, ShellPrototype.MYSQL)) {
            return MYSQL;
        }
        return MYSQL;
    }


}
