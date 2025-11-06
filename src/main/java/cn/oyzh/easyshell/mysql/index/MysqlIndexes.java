package cn.oyzh.easyshell.mysql.index;

import cn.oyzh.easyshell.mysql.DBObjectList;

import java.util.Collection;

/**
 * db表索引
 *
 * @author oyzh
 * @since 2024/01/24
 */
public class MysqlIndexes extends DBObjectList<MysqlIndex> {

    public MysqlIndexes() {

    }

    public MysqlIndexes(Collection<MysqlIndex> list) {
        super.addAll(list);
    }
}
