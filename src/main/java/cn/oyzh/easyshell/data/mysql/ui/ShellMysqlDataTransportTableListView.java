package cn.oyzh.easyshell.data.mysql.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.db.dto.DBDataTransportObject;
import cn.oyzh.easyshell.data.db.ui.DBDataTransportObjectListView;
import cn.oyzh.easyshell.mysql.table.MysqlTable;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportTableListView extends DBDataTransportObjectListView {

    public void of(List<MysqlTable> tables) {
        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
        for (MysqlTable table : tables) {
            DBDataTransportObject obj = new DBDataTransportObject();
            obj.setName(table.getName());
            list.add(obj);
        }
        this.init(list);
    }

}
