package cn.oyzh.easyshell.mysql.event;

import cn.oyzh.easyshell.db.DBObjectList;

import java.util.List;

/**
 *
 * @author oyzh
 * @since 2024/07/10
 */
public class MysqlEvents extends DBObjectList<MysqlEvent> {

    public MysqlEvents() {

    }

    public MysqlEvents(List<MysqlEvent> list) {
        super.addAll(list);
    }
}
