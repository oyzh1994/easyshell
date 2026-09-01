package cn.oyzh.easyshell.data.mysql.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
import cn.oyzh.easyshell.mysql.view.MysqlView;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportViewListView extends DBDataTransportObjectListView {

    public void of(List<MysqlView> views) {
        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
        for (MysqlView view : views) {
            DBDataTransportObject obj = new DBDataTransportObject();
            obj.setName(view.getName());
            list.add(obj);
        }
        this.init(list);
    }
}
