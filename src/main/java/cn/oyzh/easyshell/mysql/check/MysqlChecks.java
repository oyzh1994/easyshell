package cn.oyzh.easyshell.mysql.check;

import cn.oyzh.easyshell.mysql.DBObjectList;

import java.util.List;

/**
 * db外键列表
 *
 * @author oyzh
 * @since 2024/07/10
 */
public class MysqlChecks extends DBObjectList<MysqlCheck> {

    public MysqlChecks() {

    }

    public MysqlChecks(List<MysqlCheck> list) {
        super.addAll(list);
    }
}
