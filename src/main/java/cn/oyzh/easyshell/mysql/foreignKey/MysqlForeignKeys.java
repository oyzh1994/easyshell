package cn.oyzh.easyshell.mysql.foreignKey;

import cn.oyzh.easyshell.db.DBObjectList;

import java.util.Collection;

/**
 * db外键列表
 *
 * @author oyzh
 * @since 2024/07/10
 */
public class MysqlForeignKeys extends DBObjectList<MysqlForeignKey> {

    public MysqlForeignKeys() {

    }

    public MysqlForeignKeys(Collection<MysqlForeignKey> list) {
        super.addAll(list);
    }
}



