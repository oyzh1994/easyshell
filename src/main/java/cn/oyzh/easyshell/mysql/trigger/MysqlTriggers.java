package cn.oyzh.easyshell.mysql.trigger;

import cn.oyzh.easyshell.db.DBObjectList;

import java.util.List;

/**
 * db表触发器列表
 *
 * @author oyzh
 * @since 2024/07/10
 */
public class MysqlTriggers extends DBObjectList<MysqlTrigger> {

    public MysqlTriggers() {

    }

    public MysqlTriggers(List<MysqlTrigger> list) {
        super.addAll(list);
    }
}
