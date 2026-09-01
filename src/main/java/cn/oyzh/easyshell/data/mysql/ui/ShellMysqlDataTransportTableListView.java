package cn.oyzh.easyshell.data.mysql.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.dto.DBTransportObject;
import cn.oyzh.easyshell.data.db.ui.DBDataTransportObjectListView;
import cn.oyzh.easyshell.mysql.table.MysqlTable;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportTableListView extends DBDataTransportObjectListView {

    public void of(List<MysqlTable> tables) {
        List<DBTransportObject> list = CollectionUtil.newArrayList();
        for (MysqlTable table : tables) {
            DBTransportObject obj = new DBTransportObject();
            obj.setName(table.getName());
            list.add(obj);
        }
        this.init(list);
    }

}
